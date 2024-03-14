package officeAutomation;

public class BloodPressure {
	public int systollicPressure;
	public int diastollicPressure;
	
	BloodPressure() {
		systollicPressure = 0;
		diastollicPressure = 0;
	}
	
	BloodPressure(int sp, int dp) {
		systollicPressure = sp;
		diastollicPressure = dp;
	}
	
	public String displayBloodPressure() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("%d/%d mmHg", systollicPressure, diastollicPressure));
		return builder.toString();
	}
}
