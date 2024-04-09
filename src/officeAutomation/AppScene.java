package officeAutomation;

public enum AppScene {
	LoginScene(0),
	SignUpScene(1),
	
	// patient views
	PatientMainViewScene(2),
	MessagingPortalScene(3),
	ComposeNewMessageScene(4), 
	ViewMessageDetailScene(5),
	VisitationLogScene(6),
	VisitPageScene(7),  // for viewing individual patient visit records
	AccountInfoScene(8),       
	EditInfoScene(9),
	
	// doctor views
	DoctorMainViewScene(10),
	DoctorPatientsListScene(11),
	DoctorPharmaciesListScene(12);
	
	private final int value;
	public static final int amount = 13;
	
	AppScene(int v) {
		value = v;
	}
	
	public int getValue() {
		return value;
	}
}
