package officeAutomation;

public class PatientRecord {
	public int age;
	public double weightInPounds;

	// patient vitals
	public double bodyTemp;
	public int pulseRate;
	public int respirationRate; // breathes per minute
	public BloodPressure bloodPressure;
	
	PatientRecord(int a, double wip, double bt, int pr, int rp, int sp, int dt) {
		age = a;
		weightInPounds = wip;
		bodyTemp = bt;
		pulseRate = pr;
		respirationRate = rp;
		bloodPressure = new BloodPressure(sp, dt);
	}
}
