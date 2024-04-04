package officeAutomation;

public class Medication {
	public String name;
	public double doseInMg;
	public String pharmacyAddress;
	
	Medication(String n, double dim, String address) {
		name = n;
		doseInMg = dim;
		pharmacyAddress = address;
	}
}
