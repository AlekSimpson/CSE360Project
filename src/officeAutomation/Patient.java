package officeAutomation;

import java.util.ArrayList;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Patient extends Account {
	ArrayList<Visit> visits;
	MedicalStaff currentDoctor;

	public Patient(String fn, String ln, int a, PatientDate date) throws InvalidKeySpecException, IOException, ParseException {
		super(fn, ln, "none listed", date, 0, a);
		
		visits = new ArrayList<Visit>();
	}
	
	// used for creating a new Patient ONLY, that is why the password is passed in so the accountHash can be initialized
	public Patient(String fn, String ln, PatientDate date, String password) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException, ParseException {
		super(fn, ln, "none listed", date, 0, 0);
		
		// initialize the accountHash
		SecurityHandler handler = SecurityHandler.getHandler();
		AppResult<String> result = handler.getPasswordHash(password);
		if (result.isOk()) {
			accountHash = result.andThen();
		}else {
			// output the error to the console
			System.out.println(result.orElse().throwError());
		}
		
		visits = new ArrayList<Visit>();
	}
	
	public Patient(String patientID, String password, boolean isMedicalStaff) throws Exception {
		super();
		// read file
		String path = "./src/officeAutomation/ApplicationData/metadata.json";
		String jsonText = Files.readString(Paths.get(path), StandardCharsets.UTF_8);
		
		// parse json and get user's saved accountHash to authenticate the user
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(jsonText);
		JSONObject patients = (JSONObject) json.get("patients");
		accountHash = (String) patients.get(patientID);
		try {
			loadPatientDataFromFile(patientID, password, isMedicalStaff);
		} catch (Exception e) {
			throw e;
		}
	}
	
	public Patient(String uid, String hash) throws InvalidKeySpecException, IOException, ParseException {
		super();

		UID = uid;
		accountHash = hash;
		visits = new ArrayList<Visit>();
	}
	
	public Patient() throws InvalidKeySpecException, IOException, ParseException {
		// default constructor only exists to initialize a patient variable as a placeholder
		super();
		visits = new ArrayList<Visit>();
	}
	
	// 
	// MARK: Methods for Core functionality 
	//

	public AppResult<ArrayList<Visit>> getVisits(boolean isDoctorOrNurseAccess) {
		AppResult<ArrayList<Visit>> result;
		result = new AppResult<ArrayList<Visit>>(visits, new ApplicationError());

		if (age < 12 && isDoctorOrNurseAccess) {
			ApplicationError err = new ApplicationError("Permission Denied", "You cannot access records of patient under the age of 12.");
			result = new AppResult<ArrayList<Visit>>(null, err);
		}
		return result;
	}
	
	public boolean userIsAuthentic(String password) throws InvalidKeySpecException {
		if (accountHash == null) {
			// cannot authenticate if user is not loaded
			return false;
		}

		// hash the password
		SecurityHandler handler = SecurityHandler.getHandler();
		AppResult<String> passHashResult = handler.getPasswordHash(password);
		String passHash;
		if (passHashResult.isErr()) {
			return false;
		}
		passHash = passHashResult.andThen();
		
		// compare it against the saved hash
		return passHash.equals(accountHash);
	}
	
	@SuppressWarnings("unchecked")
	// TODO: write system to make it so that medical staff can also save changes to the records
	public void save(String password, boolean isMedicalStaff) throws Exception {
		// check password is correct
		if (!userIsAuthentic(password)) {
			System.out.println("Incorrect password, cannot save patient data.");
			return;
		}
		// encode to json
		JSONObject json = new JSONObject();
		json.put("firstname", firstname);
		json.put("lastname", lastname);
		json.put("UID", UID);
		json.put("email", email == null ? "" : email);
		json.put("accountHash", accountHash);
		json.put("phoneNumber", String.format("%d", phoneNumber));
		json.put("dateOfBirth", dateOfBirth.toString());
		json.put("age", String.format("%d", age));

		JSONObject visitsJson = new JSONObject();
		if (!visits.isEmpty()) {
			int i = 0;
			for (Visit v : visits) {
				JSONObject visitJson = new JSONObject();
				visitJson.put("age", v.age);
				visitJson.put("bodyTemp", v.bodyTemp);
				visitJson.put("pulseRate", v.pulseRate);
				visitJson.put("respirationRate", v.respirationRate);
				visitJson.put("systollicPressure", v.bloodPressure.systollicPressure);
				visitJson.put("diastollicPressure", v.bloodPressure.diastollicPressure);
				visitsJson.put(String.format("%d", i), visitJson);
				i++;
			}		
		}
		json.put("visits", visitsJson);
		
		String jsonText = json.toString();
		
		// encrypt json text
		String encryptedText = SecurityHandler.encrypt(jsonText, stringSecretKey, stringSalt);
		
		// write to file
		Path filepath = Paths.get(String.format("./src/officeAutomation/ApplicationData/%s.json", UID));
		Files.write(filepath, encryptedText.getBytes());
		
		// save the accountHash in a non encrypted file so we can load the account again later
		saveAccountHash();
	}
	
	// the patient ID needs to be passed in because this method will be called when the user is signing in and the stored ID will not be decrypted yet
	// TODO: figure out a way to authenticate the password
	public void loadPatientDataFromFile(String patientID, String password, boolean isMedicalStaff) throws Exception {
		if (!userIsAuthentic(password)) {
			// TODO: present error to user
			throw new Exception("Incorrect Password, cannot sign in");
		}
		currentDoctor = MedicalStaff.getInstance();

		// read file
		String path = String.format("./src/officeAutomation/ApplicationData/%s.json", patientID);
		String encryptedText = Files.readString(Paths.get(path), StandardCharsets.UTF_8);
		
		// decrypt data
		String decryptedText = SecurityHandler.decrypt(encryptedText, stringSecretKey, stringSalt);
		
		// parse decrypted json
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(decryptedText);
		
		// load data into object
		firstname = (String) json.get("firstname");
		lastname = (String) json.get("lastname");
		UID = (String) json.get("UID");
		email = (String) json.get("email");
		accountHash = (String) json.get("accountHash");
		phoneNumber = Integer.parseInt((String) json.get("phoneNumber"));
		
		String dateStr = (String) json.get("dateOfBirth");
		dateOfBirth = new PatientDate(dateStr);
		age = Integer.parseInt((String) json.get("age"));
		
		// load records
		JSONObject visitObject = (JSONObject) json.get("visits");
		for (int i = 0; i < visitObject.size(); i++) {
			JSONObject visElement = (JSONObject) visitObject.get(String.format("%d", i));
			int age = (int) visElement.get("age");
			double weightInPounds = (double) visElement.get("weightInPounds");
			double bodyTemp = (double) visElement.get("bodyTemp");
			int pulseRate = (int) visElement.get("pulseRate");
			int respirationRate = (int) visElement.get("respirationRate");
			int systollicPressure = (int) visElement.get("systollicPressure");
			int diastollicPressure = (int) visElement.get("diastollicPressure");
			Visit pr = new Visit(
					age, weightInPounds,
					bodyTemp, pulseRate,
					respirationRate, systollicPressure,
					diastollicPressure
				);
			visits.add(pr);
			i++;
		}
	}
	
	@Override
	public void sendMessage(String t, String subject, String m) {
		String name  = firstname + " " + lastname;
		Message.composeAndSendMessage(subject, t, "office", name, UID, m);
	}

	//
	// MARK: GETTERS AND SETTERS
	//
	
	public void addVisit(Visit visit) {
		visits.add(visit);
	}
}