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
	DoctorViewPatientScene(12),
	DoctorPharmaciesListScene(13),
	DoctorAddNewPharmacyScene(14),
    DoctorVisitationLogScene(15),
    DoctorAddNewVisitView(16),
    DoctorMessagingPortalScene(17),
	DoctorComposeNewMessageScene(18);
	
	private final int value;
	public static final int amount = 19;
	
	AppScene(int v) {
		value = v;
	}
	
	public int getValue() {
		return value;
	}
}
