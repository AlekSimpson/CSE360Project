package officeAutomation;

public class PatientDate {
	public int day;
	public int month;
	public int year;
	
	PatientDate(String dateStr) throws Exception {
		try {
			parseInputtedDate(dateStr);		
		}
		catch (Exception e) {
			throw e;
		}
	}
	
	PatientDate(int d, int m, int y) {
		day = d;
		month = m;
		year = y;
	}
	
	PatientDate() {
		day = 0;
		month = 0;
		year = 0;
	}
	
	public String toString() {
		return String.format("%d/%d/%d", month, day, year);
	}
	
	private void parseInputtedDate(String text) throws Exception {
		String[] delimited = text.split("/");
		if (delimited.length != 3) {
			throw new Exception("please use the correct date format when inputting the date (mm/dd/yyyy)");
		}

		try {
			day = Integer.parseInt(delimited[1]);
			month = Integer.parseInt(delimited[0]);
			year = Integer.parseInt(delimited[2]);		
		}
		catch (Exception e) {
			throw e;
		}
	}
}
