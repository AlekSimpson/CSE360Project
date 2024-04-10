package officeAutomation;

public class Medication {
	public String name;
	public double doseInMg;
	public String pharmacyAddress;
	
	Medication(String input) {
		String[] del = input.split(":");
		name = del[0];
		doseInMg = Double.parseDouble(del[1]);
		pharmacyAddress = del[2];
	}
	Medication(String n, double dim, String address) {
		name = n;
		doseInMg = dim;
		pharmacyAddress = address;
	}
}
