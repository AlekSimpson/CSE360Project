package officeAutomation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Pharmacy {
	public String physicalAddress;
	public long phoneNumber;
	public String email;
	public String chainName;
	private final static String pharmaciesPath = "./src/officeAutomation/ApplicationData/pharmacies.json";
	
	public Pharmacy(String address, String name, long pn, String e) {
		physicalAddress = address;
		chainName = name;
		phoneNumber = pn;
		email = e;
	}
	
	public String toString() {
		return String.format("[%s] - [%s] - [%s]", chainName, physicalAddress, String.valueOf(phoneNumber));
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSON() {
		JSONObject pharmacyObj = new JSONObject();
		pharmacyObj.put("physicalAddress", physicalAddress);
		pharmacyObj.put("phoneNumber", String.valueOf(phoneNumber));
		pharmacyObj.put("email", email);
		pharmacyObj.put("chainName", chainName);
		
		return pharmacyObj;
	}
	
	@SuppressWarnings("unchecked")
	public void saveNewPharmacy() throws IOException, ParseException {
		File pharmaciesFile = new File(pharmaciesPath);
		JSONParser parser = new JSONParser();
		String fileText = Files.readString(pharmaciesFile.toPath());
		
		JSONObject rootObj = new JSONObject();
		if (fileText != "") {
			rootObj = (JSONObject) parser.parse(fileText);
		}
		
		JSONObject thisJson = toJSON();
		rootObj.put(chainName, thisJson);
		
		String jsonText = rootObj.toString();
		Path filepath = Paths.get(pharmaciesPath);
		Files.write(filepath, jsonText.getBytes());
	}
	
	public static ArrayList<Pharmacy> fromJSON() throws IOException, ParseException {
		ArrayList<Pharmacy> pharmacies = new ArrayList<Pharmacy>();
		JSONParser parser = new JSONParser();
		File pharmaciesFile = new File(pharmaciesPath);
		
		String fileText = Files.readString(pharmaciesFile.toPath());
		JSONObject rootObj = (JSONObject) parser.parse(fileText);
		
		if (rootObj.keySet().isEmpty()) {
			return pharmacies;
		}

		String currKey;
		JSONObject currPharmacyObject;
		Pharmacy currPharmacy;
		for (Object raw : rootObj.keySet()) {
			currKey = (String) raw;
			currPharmacyObject = (JSONObject) rootObj.get(currKey);
			
			String physAdd = (String) currPharmacyObject.get("physicalAddress");
			long pn = Long.parseLong((String) currPharmacyObject.get("phoneNumber"));
			String em = (String) currPharmacyObject.get("email"); 
			String name = (String) currPharmacyObject.get("chainName");
			
			currPharmacy = new Pharmacy(physAdd, name, pn, em);
			pharmacies.add(currPharmacy);
		}
		
		return pharmacies;
	}
}
