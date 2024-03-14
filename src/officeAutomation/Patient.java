package officeAutomation;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.spec.InvalidKeySpecException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;

public class Patient {
	String firstname;
	String lastname;
	String patientUniqueID;
	String email;
	String accountHash;
	Date dateOfBirth;
	int phoneNumber;
	int age;
	ArrayList<PatientRecord> records;
	// TODO: Doctor currentDoctor;
	
	Patient(String fn, String ln, String pui, int a, int year, int month, int day) {
		firstname = fn;
		lastname = ln;
		patientUniqueID = pui;
		age = a;
		
		// set the date of birth
		Calendar cal = Calendar.getInstance();
		cal.set(year + 1900, month, day);
		dateOfBirth = cal.getTime();
	}
	
	Patient(String patientID, String password, boolean isMedicalStaff) {
		try {
			loadPatientDataFromFile(patientID, password, isMedicalStaff);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		// hash the password
		SecurityHandler handler = SecurityHandler.getHandler();
		AppResult<String> passHashResult = handler.getPasswordHash(password);
		String passHash;
		if (passHashResult.isErr()) {
			return false;
		}
		passHash = passHashResult.andThen();

		// compare it against the saved hash
		return (passHash == accountHash);
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
		json.put("email", email);
		json.put("phoneNumber", phoneNumber);

		DateFormat formatter = DateFormat.getDateInstance(DateFormat.DEFAULT);
		json.put("dateOfBirth", formatter.format(dateOfBirth));

		json.put("age", age);

		JSONObject recordsJson = new JSONObject();
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
		json.put("records", recordsJson);
		
		String jsonText = json.toString();
		
		// encrypt json text
		SecretKeySpec keyspec = new SecretKeySpec(password.getBytes(), "AES");
		String encryptedText = crypto(jsonText, keyspec, Cipher.ENCRYPT_MODE);
		
		// write to file
		FileWriter fw = new FileWriter(String.format("%s.json", patientUniqueID));
		fw.write(encryptedText);
		fw.close();
	}
	
	// the patient ID needs to be passed in because this method will be called when the user is signing and the stored ID will not be decrypted yet
	// TODO: figure out a way to authenticate the password
	public void loadPatientDataFromFile(String patientID, String password, boolean isMedicalStaff) throws Exception {
		// read file
		String path = String.format("./ApplicationData/%s.json", patientID);
		String encryptedText = Files.readString(Paths.get(path), StandardCharsets.UTF_8);
		
		// decrypt data
		SecretKeySpec keyspec = new SecretKeySpec(password.getBytes(), "AES");
		String decryptedText = crypto(encryptedText, keyspec, Cipher.DECRYPT_MODE);
		
		// parse decrypted json
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(decryptedText);
		
		// load data into object
		firstname = (String) json.get("firstname");
		lastname = (String) json.get("lastname");
		patientUniqueID = (String) json.get("patientUnqiueID");
		email = (String) json.get("email");
		phoneNumber = (int) json.get("phoneNumber");
		
		DateFormat formatter = DateFormat.getDateInstance(DateFormat.DEFAULT);
		dateOfBirth = (Date) formatter.parse((String) json.get("dateOfBirth"));

		age = (int) json.get("age");
		// load records
		JSONObject recordObject = (JSONObject) json.get("records");
		String[] keys = {"age", "weightInPounds", "bodyTemp", "pulseRate", "respirationRate", "systollicPressure", "diastollicPressure"};
		int i = 0;
		for (String key : keys) {
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
	
	// depending on the opmode, will either decrypt or encrypt the text
	// opmode options: Cipher.ENCRYPT_MODE, Cipher.DECRYPT_MODE
	public static String crypto(String text, SecretKey key, int opmode) throws Exception {
		Cipher cipher = Cipher.getInstance("AES");
		byte[] textBytes = text.getBytes();
		cipher.init(opmode, key);
		byte[] bytes = cipher.doFinal(textBytes);

		switch (opmode) {
		case Cipher.ENCRYPT_MODE:
			Encoder encoder = Base64.getEncoder();
			String cryptoText = encoder.encodeToString(bytes);
			return cryptoText;
		case Cipher.DECRYPT_MODE:
			return new String(bytes);
		default:
			return "";
		}
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