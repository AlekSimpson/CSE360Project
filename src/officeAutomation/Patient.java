package officeAutomation;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;

public class Patient {
	String firstname;
	String lastname;
	String patientUniqueID;
	String email;
	String accountHash;
	PatientDate dateOfBirth;
	int phoneNumber;
	int age;
	ArrayList<PatientRecord> records;
	// TODO: Doctor currentDoctor;
	private final static String stringSecretKey = "programSecretKey";
	private final static String stringSalt = "programSecretSalt";

	Patient(String fn, String ln, int a, PatientDate date) {
		firstname = fn;
		lastname = ln;
		age = a;
		dateOfBirth = date;
		phoneNumber = 0;
		email = "none listed";
		
		// note: unique id MUST be created AFTER the dateOfBirth is set.
		// this method depends on dateOfBirth not being null
		createUniqueID();
	}
	
	// used for creating a new Patient ONLY, that is why the password is passed in so the accountHash can be initialized
	Patient(String fn, String ln, PatientDate date, String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
		firstname = fn;
		lastname = ln;
		dateOfBirth = date;
		
		// initialize the accountHash
		SecurityHandler handler = SecurityHandler.getHandler();
		AppResult<String> result = handler.getPasswordHash(password);
		if (result.isOk()) {
			accountHash = result.andThen();
		}else {
			// output the error to the console
			System.out.println(result.orElse().throwError());
		}
		
		// note: unique id MUST be created AFTER the dateOfBirth is set.
		// this method depends on dateOfBirth not being null
		createUniqueID();

		records = new ArrayList<PatientRecord>();
		phoneNumber = 0;
		email = "none listed";
	}
	
	Patient(String patientID, String password, boolean isMedicalStaff) throws Exception {
		// read file
		String path = "./src/officeAutomation/ApplicationData/metadata.json";
		String jsonText = Files.readString(Paths.get(path), StandardCharsets.UTF_8);
		
		// parse json and get user's saved accountHash to authenticate the user
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(jsonText);
		accountHash = (String) json.get(patientID);
		try {
			loadPatientDataFromFile(patientID, password, isMedicalStaff);
		} catch (Exception e) {
			throw e;
		}
	}
	
	Patient() {
		// default constructor only exists to initialize a patient variable as a placeholder
	}
	
	// 
	// MARK: Methods for Core functionality 
	//

	public AppResult<ArrayList<PatientRecord>> getPatientRecords(boolean isDoctorOrNurseAccess) {
		AppResult<ArrayList<PatientRecord>> result;
		result = new AppResult<ArrayList<PatientRecord>>(records, new ApplicationError());

		if (age < 12 && isDoctorOrNurseAccess) {
			ApplicationError err = new ApplicationError("Permission Denied", "You cannot access records of patient under the age of 12.");
			result = new AppResult<ArrayList<PatientRecord>>(null, err);
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
		json.put("patientUniqueID", patientUniqueID);
		json.put("email", email == null ? "" : email);
		json.put("accountHash", accountHash);
		json.put("phoneNumber", String.format("%d", phoneNumber));
		json.put("dateOfBirth", dateOfBirth.toString());
		json.put("age", String.format("%d", age));

		JSONObject recordsJson = new JSONObject();
		if (!records.isEmpty()) {
			int i = 0;
			for (PatientRecord r : records) {
				JSONObject recordJson = new JSONObject();
				recordJson.put("age", r.age);
				recordJson.put("bodyTemp", r.bodyTemp);
				recordJson.put("pulseRate", r.pulseRate);
				recordJson.put("respirationRate", r.respirationRate);
				recordJson.put("systollicPressure", r.bloodPressure.systollicPressure);
				recordJson.put("diastollicPressure", r.bloodPressure.diastollicPressure);
				recordsJson.put(String.format("%d", i), recordJson);
				i++;
			}		
		}
		json.put("records", recordsJson);
		
		String jsonText = json.toString();
		
		// encrypt json text
		String encryptedText = SecurityHandler.encrypt(jsonText, stringSecretKey, stringSalt);
		
		// write to file
		Path filepath = Paths.get(String.format("./src/officeAutomation/ApplicationData/%s.json", patientUniqueID));
		Files.write(filepath, encryptedText.getBytes());
		
		// save the accountHash in a non encrypted file so we can load the account again later
		saveAccountHash();
	}
	
	@SuppressWarnings("unchecked")
	public void saveAccountHash() throws IOException {
		JSONObject json = new JSONObject();
		json.put(patientUniqueID, accountHash);

		String jsonText = json.toString();
		Path filepath = Paths.get("./src/officeAutomation/ApplicationData/metadata.json");
		Files.write(filepath, jsonText.getBytes());
	}
	
	// the patient ID needs to be passed in because this method will be called when the user is signing in and the stored ID will not be decrypted yet
	// TODO: figure out a way to authenticate the password
	public void loadPatientDataFromFile(String patientID, String password, boolean isMedicalStaff) throws Exception {
		if (!userIsAuthentic(password)) {
			// TODO: present error to user
			throw new Exception("Incorrect Password, cannot sign in");
		}

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
		patientUniqueID = (String) json.get("patientUniqueID");
		email = (String) json.get("email");
		accountHash = (String) json.get("accountHash");
		//phoneNumber = Integer.parseInt((String) json.get("phoneNumber"));
		
		String dateStr = (String) json.get("dateOfBirth");
		dateOfBirth = new PatientDate(dateStr);

		//age = Integer.parseInt((String) json.get("age"));
		
		// load records
		JSONObject recordObject = (JSONObject) json.get("records");
		//String[] keys = {"age", "weightInPounds", "bodyTemp", "pulseRate", "respirationRate", "systollicPressure", "diastollicPressure"};
		for (int i = 0; i < recordObject.size(); i++) {
			JSONObject recElement = (JSONObject) recordObject.get(String.format("%d", i));
			int age = (int) recElement.get("age");
			double weightInPounds = (double) recElement.get("weightInPounds");
			double bodyTemp = (double) recElement.get("bodyTemp");
			int pulseRate = (int) recElement.get("pulseRate");
			int respirationRate = (int) recElement.get("respirationRate");
			int systollicPressure = (int) recElement.get("systollicPressure");
			int diastollicPressure = (int) recElement.get("diastollicPressure");
			PatientRecord pr = new PatientRecord(
					age, weightInPounds,
					bodyTemp, pulseRate,
					respirationRate, systollicPressure,
					diastollicPressure
				);
			records.add(pr);
			i++;
		}
	}
	
	// 
	// MARK: Utility Methods
	//
	
	private void createUniqueID() {
		// we do not want to regenerate a unique ID if one already exists
		if (patientUniqueID != null) {
			return;
		}
		int numberTag = dateOfBirth.day + dateOfBirth.month + dateOfBirth.year;

		// create id buffer to build the id
		StringBuilder buffer = new StringBuilder();
		buffer.append(firstname);
		buffer.append(lastname);
		buffer.append(numberTag);	 
		
		// set the unique ID
		patientUniqueID = buffer.toString();
	}
	
	public String toString() {
		StringBuilder buff = new StringBuilder();
		
		buff.append("-----------------------\n");
		buff.append(String.format("firstname: %s\n", firstname));
		buff.append(String.format("lastname: %s\n", lastname));
		buff.append(String.format("patientUniqueID: %s\n", patientUniqueID));
		buff.append(String.format("email: %s\n", email));
		buff.append(String.format("accountHash: %s\n", accountHash));
		buff.append(String.format("dateOfBirth: %s\n", dateOfBirth.toString()));
		buff.append(String.format("phoneNumber: %d\n", phoneNumber));
		buff.append(String.format("age: %d\n", age));
		buff.append("-----------------------\n");
		return buff.toString();
	}
	
	//
	// MARK: GETTERS AND SETTERS
	//
	
	public String getUniqueID() {
		return patientUniqueID;
	}
	
	public int getCurrentAge() {
		return age;
	}
	
	public void updateAge() {
		age+=1;
	}
	
	public void arbitrarilyUpdateAge(int newAge) {
		age = newAge;
	}

	public void addRecord(PatientRecord pr) {
		records.add(pr);
	}
	
	public void setEmail(String e) {
		email = e;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setPhoneNumber(int pn) {
		phoneNumber = pn;
	}
	
	public int getPhoneNumber() {
		return phoneNumber;
	}
	
	public String getName() {
		return String.format("%s %s", firstname, lastname);
	}
	
	public String getFirstname() {
		return firstname;
	}
	
	public String getLastname() {
		return lastname;
	}
}