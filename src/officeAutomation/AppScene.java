package officeAutomation;

public enum AppScene {
	LoginScene(0),
	SignUpScene(1),
	PatientMainViewScene(2),
	MessagingPortalScene(3),
	ComposeNewMessageScene(4), 
	VisitationLogScene(5),
	VisitPageScene(6),  // for viewing individual patient visit records
	AccountInfoScene(7),       
	EditInfoScene(8),
	SinglePatientViewScene(9); 
	
	private final int value;
	public final static int amount = 10;
	
	AppScene(int v) {
		value = v;
	}
	
	public int getValue() {
		return value;
	}
	
	
}
