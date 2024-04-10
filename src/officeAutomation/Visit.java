package officeAutomation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.json.simple.JSONObject;

// A
public class Visit {
	public int age;
	public double weightInPounds;
	public double heightInFeet;
	public PatientDate date;

	// patient vitals
	public double bodyTemp;
	public int pulseRate;
	public int respirationRate; // breathes per minute
	public BloodPressure bloodPressure;
	
	// questionnaire data
	public String knownAllergies;
	private ArrayList<Medication> currentMedications;
	public String otherHealthConditions;
	
	// physical test
	public String physicalTestFindings;
	
	Visit() {
		age = 0;
		weightInPounds = 0;
		heightInFeet = 0;
		date = null;
		bodyTemp = 0;
		pulseRate = 0;
		respirationRate = 0;
		bloodPressure = null;
		knownAllergies = "";
		currentMedications = new ArrayList<Medication>();
		otherHealthConditions = "";
		physicalTestFindings = "";
	}

	Visit(
			int a,      // age
			double wip, // weight in pounds
			double hif, // height in feet
			double bt,  // body temp
			String d,   // date
			int pr,     // pulse rate
			int rp,     // respiration rate
			int sp,     // systollic pressure
			int dt,     // diastollic pressure
			String allergies,
			String currMeds,
			String ohc, // otherHealthConditions
			String findings // physical findings
	) throws Exception {
		age = a;
		weightInPounds = wip;
		heightInFeet = hif;
		date = new PatientDate(d);
		bodyTemp = bt;
		pulseRate = pr;
		respirationRate = rp;
		bloodPressure = new BloodPressure(sp, dt);
		knownAllergies = allergies;
		currentMedications = new ArrayList<Medication>();
		otherHealthConditions = ohc;
		physicalTestFindings = findings;
		
		if (!currMeds.equals("") && currMeds.contains(":")) { // safety precaution so that we do not process invalid input
			ArrayList<String> delimited = new ArrayList<String>(Arrays.asList(currMeds.split(" ")));
			for (String s : delimited) {
				Medication med = new Medication(s);
				currentMedications.add(med);
			}		
		}
	}
	

	@SuppressWarnings("unchecked")
	public JSONObject toJSON() {
		JSONObject visitObject = new JSONObject();
		visitObject.put("age", age);
		visitObject.put("weightInPounds", weightInPounds);
		visitObject.put("heightInFeet", heightInFeet);
		visitObject.put("date", date.toString());
		visitObject.put("bodyTemp", bodyTemp);
		visitObject.put("pulseRate", pulseRate);
		visitObject.put("respirationRate", respirationRate);
		visitObject.put("systollicPressure", bloodPressure.systollicPressure);
		visitObject.put("diastollicPressure", bloodPressure.diastollicPressure);
		visitObject.put("knownAllergies", knownAllergies);

		String accumulate = "";
		if (!currentMedications.isEmpty()) {
			for (Medication m : currentMedications) {
				accumulate += (m.toString() + ", ");
			}		
			accumulate = accumulate.substring(0, accumulate.length() - 2); // remove the last ', ' from the end
		}
		visitObject.put("currentMedications", accumulate);

		visitObject.put("otherHealthConditions", otherHealthConditions);
		visitObject.put("physicalTestFindings", physicalTestFindings);
		
		return visitObject;
	}
	
	public static Visit fromJSON(JSONObject visitObject, int index) {
		JSONObject visElement = (JSONObject) visitObject.get(String.format("%d", index));
		int age = Integer.parseInt(String.valueOf(visElement.get("age")));
		double weightInPounds = (double) visElement.get("weightInPounds");
		double heightInFeet = (double) visElement.get("heightInFeet");
		String date = (String) visElement.get("date");
		double bodyTemp = (double) visElement.get("bodyTemp");
		int pulseRate = Integer.parseInt(String.valueOf(visElement.get("pulseRate")));
		int respirationRate = Integer.parseInt(String.valueOf(visElement.get("respirationRate")));
		int systollicPressure = Integer.parseInt(String.valueOf(visElement.get("systollicPressure")));
		int diastollicPressure = Integer.parseInt(String.valueOf(visElement.get("diastollicPressure")));
		String knownAllergies = (String) visElement.get("knownAllergies");
		String currentMedications = (String) visElement.get("currentMedications");
		String otherHealthConditions = (String) visElement.get("otherHealthConditions");
		String findings = (String) visElement.get("physicalTestFindings");
		Visit visit = null;
		try {
			visit = new Visit(age, weightInPounds, heightInFeet, bodyTemp,  
					date, pulseRate, 
					respirationRate, systollicPressure, 
					diastollicPressure, knownAllergies, 
					currentMedications, otherHealthConditions, findings);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return visit;
	}
	
	public void addNewMedication(Medication medication) {
		currentMedications.add(medication);
	}
	
	public String getCurrentMedications() {
		StringBuilder buff = new StringBuilder();
		for (Medication m : currentMedications) {
			buff.append(m.name);
			buff.append(", ");
		}
		buff.deleteCharAt(buff.length()-1);
		buff.deleteCharAt(buff.length()-1);

		return buff.toString();
	}
}
