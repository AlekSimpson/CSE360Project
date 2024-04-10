package officeAutomation;

import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MedicalStaff extends Account {
	private HashMap<String, Patient> patients;
	private HashMap<String, Pharmacy> pharmaciesAddressed;
	private HashMap<String, Pharmacy> pharmaciesNamed;
	private static MedicalStaff singleInstance = null;
	private final String savedDataPath = "./src/officeAutomation/ApplicationData/metadata.json";
	
	private MedicalStaff() throws InvalidKeySpecException, IOException, ParseException { // nurse by default
		// hardcoding the Office account because that won't change
		//     firsntame,  ln,       email,              phone number,  uid)
		super("Doctor's", "Office", "doctors@office.com", 6022100011L, "office"); 
		// medical staff password: medstaffpassword 

		patients = new HashMap<String, Patient>();
		pharmaciesAddressed = new HashMap<String, Pharmacy>();
		pharmaciesNamed = new HashMap<String, Pharmacy>();
		loadPatientsFromJson();
		//for (String k : patients.keySet()) {
		//	System.out.println(k);
		//}
	}
	
	public static synchronized MedicalStaff getInstance() throws InvalidKeySpecException, IOException, ParseException {
		if (singleInstance == null) {
			return new MedicalStaff();
		}
		
		return singleInstance;
	}
	
	public boolean isMedStaff() {
		return true;
	}
	
	private void loadPatientsFromJson() throws IOException, ParseException, InvalidKeySpecException {
		// get out file
		File metadata = new File(savedDataPath);
		
		// initialize json parser
		JSONParser parser = new JSONParser();
		String fileText = Files.readString(metadata.toPath());
		
		// check if file is empty
		if (fileText == "") {
			// no patients have been logged yet, no patients to load
			return;
		}
		
		// parse the file text
		JSONObject rootObj = (JSONObject) parser.parse(fileText);
		
		Object jsonObj = rootObj.get("patients");
		if (jsonObj == null) {
			// no patients have been logged yet, no patients to load
			return;
		}
		
		// isolate the patients object
		JSONObject patientsObj = (JSONObject) rootObj.get("patients");

		String ks;
		String currentHash;
		for (Object key : patientsObj.keySet()) {
			ks = (String) key;
			if (ks.equals("office"))
				continue;
			currentHash = (String) patientsObj.get(ks);
			patients.put(ks, new Patient(ks, currentHash));
		}
	}
	
	public boolean authenticaLogin(String password) throws InvalidKeySpecException {
		AppResult<String> result = SecurityHandler.getHandler().getPasswordHash(password);
		if (result.isErr()) {
			System.out.println("couldn't get inputted password hash");
			return false;
		}
		String inputtedHash = result.andThen();
		
		return (inputtedHash.equals(accountHash));
	}
	
	public HashMap<String, Patient> getPatients() {
		return patients;
	}
	
	public HashMap<String, Pharmacy> getPharmacies() {
		return pharmaciesNamed;
	}
	
	public void sendMessage(String to, String tUID, String subject, String messageText) {
		Message.composeAndSendMessage(subject, to, tUID, "Doctor ___", UID, messageText);
	}
	
	/*
	@Override
	public void sendMessage(String to, String subject, String m) {
		String name  = firstname + " " + lastname;
		Message.composeAndSendMessage(subject, to, toUID, from, fromUID, message);
	}
	 */
	
	public void addPharmacy(String physAdd, String name, int phone, String email) {
		Pharmacy newPharm = new Pharmacy(physAdd, name, phone, email);
		pharmaciesNamed.put(name, newPharm);
		pharmaciesAddressed.put(physAdd, newPharm);
	}
	
	public void removePharmacy(String input) {
		Pharmacy result = pharmaciesAddressed.remove(input);
		if (result == null) // want the method to work for both a lookup by address and name
			result = pharmaciesNamed.remove(input);
	}
	
	public AppResult<Pharmacy> searchPharmacies(String input) {
		Pharmacy found = pharmaciesAddressed.get(input);
		if (found == null) // want the method to work for both a lookup by address and name
			found = pharmaciesNamed.get(input);

		AppResult<Pharmacy> res = new AppResult<Pharmacy>(found, new ApplicationError());
		return res;
	}
	
	// returns null if not found
	public Patient searchPatients(String uid) {
		return patients.get(uid);
	}
	
	public void addPatient(Patient p) {
		patients.put(p.getFirstname(), p);
	}
}
