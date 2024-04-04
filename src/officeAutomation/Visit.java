package officeAutomation;

import java.util.ArrayList;

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

	Visit(int a, double wip, double bt, int pr, int rp, int sp, int dt) {
		age = a;
		weightInPounds = wip;
		bodyTemp = bt;
		pulseRate = pr;
		respirationRate = rp;
		bloodPressure = new BloodPressure(sp, dt);
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
