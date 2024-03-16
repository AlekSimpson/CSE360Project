package officeAutomation;

public enum AppScenes {
	LoginPage(0),
	SignUpPage(1),        	
	PatientMainView(2),
	MessagingPortal(3),   
	ComposeNewMessage(4), 
	VisitationLog(5),     
	VisitPage(6),         
	AccountInfo(7),       
	EditInfo(8);          
	
	private final int value;
	
	AppScenes(int v) {
		value = v;
	}
	
	public int getValue() {
		return value;
	}
}
