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
	EditInfoScene(8);          
	
	private final int value;
	
	AppScene(int v) {
		value = v;
	}
	
	public int getValue() {
		return value;
	}
}
