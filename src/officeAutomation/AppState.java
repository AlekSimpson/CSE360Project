package officeAutomation;

import java.io.IOException;
import java.util.ArrayList;

import org.json.simple.parser.ParseException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

// This class is application specific and solely is for managing scene setup functions for the app scenes
public class AppState extends AbstractAppState {
	
	private static AppState singleInstance = null;
	private static int CURRENT_INDEX = 0;
	private static StackPane CURRENT_ROOT = null;
	
	private AppState() {
		super();
		
		// setup scenes
		setupLoginScene();
		setupSignUpScene();
		setupPatientMainViewScene();
		setupVisitationLogScene();
		setupMessagingPortalScene();
		setupAccountInfoScene();
		setupEditInfoScene();
		setupComposeNewMessageScene();
		setupDoctorMainViewScene();
		setupMessageChatDetailView();
		setupDoctorsPatientsListScene();
		setupDoctorViewPatientScene();
		setupDoctorVisitationLogScene();
		setupDoctorAddNewVisit();
		setupDoctorPharmaciesListScene();
		setupDoctorAddPharmacyScene();
		setupDoctorMessagingPortalScene();
		setupDoctorComposeNewMessageScene();
	}
	
	public static synchronized AppState getInstance() {
		if (singleInstance == null) {
			return new AppState();
		}
		
		return singleInstance;
	}
	
	/*
	 * Mark: Utility Methods
	 * 
	 */
	
	static Button logoutButton() {
		Button logoutButton = button("logoutButton", "Logout", e -> {
			currentlyLoggedIn = null;
			focusedMessage = null;
			focusedPatient = null;
			navigateToScene(AppScene.LoginScene);
		});	
		return logoutButton;
	}
	
	static HBox createForm(String formTitle, String id) {
		Label titleLabel = label(formTitle, Font.font("Helvetica", FontWeight.NORMAL, 15));
		TextField field = textfield("", id);

		HBox stack = new HBox(10, titleLabel, field);
		stack.setAlignment(Pos.TOP_CENTER);
		return stack;
	}
	
	private static ListView<Button> listview(String id) {
		ListView<Button> listview = new ListView<Button>();
		listview.setPrefSize(500, 500);
		listview.setId(id);
		addNodes(CURRENT_INDEX, listview);

		return listview;
	}

	private static Button navigationButton(
		String id, 
		String text, 
		AppScene dest
	) {
		Button navButton = new Button();
		navButton.setId(id);
		navButton.setText(text);	
		addNodes(CURRENT_INDEX, navButton);

		navButton.setOnAction(e -> {
			navigateToScene(dest);
		});
		return navButton;
	}
	
	private static Button button(
		String id, 
		String text, 
		EventHandler<ActionEvent> value
	) {
		Button navButton = new Button();
		navButton.setId(id);
		navButton.setText(text);	
		addNodes(CURRENT_INDEX, navButton);

		navButton.setOnAction(value);
		return navButton;
	}
	
	private static Label label(String title, Font font) {
		Label label = new Label(title);
		label.setFont(font);
		return label;
	}
	
	private static Label label(String title, String id, Font font, Color color) {
		Label label = new Label(title);
		label.setFont(font);
		label.setId(id);
		label.setTextFill(color);
		addNodes(CURRENT_INDEX, label);
		return label;
	}
	
	private static TextField textfield(String placeholder, String id) {
        TextField field = new TextField(placeholder); // first name field
        field.setId(id);	
        addNodes(CURRENT_INDEX, field);
        return field;
	}
	
	private static TextArea textarea(String id) {
        TextArea field = new TextArea("Type here..."); // first name field
        field.setPrefSize(500, 500);
        field.setPrefRowCount(50);
        field.setId(id);	
        addNodes(CURRENT_INDEX, field);
        return field;
	}
	
	private static TextField textfield(
		String placeholder, 
		String id, 
		String prompt
	) {
		TextField field = new TextField(placeholder);
		field.setId(id);
		addNodes(CURRENT_INDEX, field);
		field.setPromptText(prompt);
		return field;
	}
	
	private static PasswordField passfield(
		String promptText, String id
	) {
		PasswordField field = new PasswordField();
		field.setId(id);
        field.setPromptText(promptText);
        addNodes(CURRENT_INDEX, field);
		return field;
	}
	
	@SuppressWarnings("unchecked")
	private static void customNavigateToMessagingPortal() {
		navigateToScene(AppScene.MessagingPortalScene);	
		Button[] messagesViewMessageItems;
		try {
			((ListView<Button>) sceneNodesMapList.get(currentSceneID).get("messagesView")).getItems().clear();
			currentlyLoggedIn.mailbox.clear();
			//staffAccount.mailbox.clear();
			messagesViewMessageItems = currentlyLoggedIn.getMailboxInboxItems();
			((ListView<Button>) sceneNodesMapList.get(currentSceneID).get("messagesView")).getItems().addAll(messagesViewMessageItems);
		} catch (Exception exception) {
			System.out.println("AppState: couldn't read messages");
			exception.printStackTrace();
		}	
	}

	@SuppressWarnings("unchecked")
	private static HBox makeTopBar() {
		Button homeButton = navigationButton("homeButton", "Home", AppScene.PatientMainViewScene);

		Button visitsButton = button("visitsButton", "Visits", e -> {
			eventHandler.patientNavigateToVisitsScene();
		});

		Button messagesButton = button("messagesButton", "Messages", e -> {
			customNavigateToMessagingPortal();
		});

		Button accountButton = button("accountButton", "Account", e -> {
			eventHandler.handleAccountTabPressed();
		});
		
		Button logoutButton = logoutButton();
		HBox topBarStack = new HBox(100, homeButton, visitsButton, messagesButton, accountButton, logoutButton);
		topBarStack.setAlignment(Pos.CENTER);
		return topBarStack;
	}
	
	@SuppressWarnings("unchecked")
	private static void customPatientEventHandler() {
		navigateToScene(AppScene.DoctorPatientsListScene);
		MedicalStaff medStaffLoggedIn = (MedicalStaff) currentlyLoggedIn;
		ArrayList<Button> patientButtons = new ArrayList<Button>();
		((ListView<Button>) sceneNodesMapList.get(currentSceneID).get("patientsView")).getItems().clear();

		//Patient currentPatient;
		for (String key : medStaffLoggedIn.getPatients().keySet()) {
			Patient currentPatient = medStaffLoggedIn.getPatients().get(key);
			Button b = button(key, key, ev -> {
				try {
					eventHandler.handleDocPatientButtonPress(currentPatient.getUID(), currentPatient.accountHash);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			});
			patientButtons.add(b);
		}
		Button[] buttonsCopy = new Button[patientButtons.size()];
		int i = 0;
		for (Button b : patientButtons) {
			buttonsCopy[i] = b;
			i++;
		}
		
		((ListView<Button>) sceneNodesMapList.get(currentSceneID).get("patientsView")).getItems().addAll(buttonsCopy);
	}
	
	@SuppressWarnings("unchecked")
	private static HBox makeDoctorTopBar() {
		Button homeButton = navigationButton("homeButton", "Home", AppScene.DoctorMainViewScene);
		Button patientsButton = button("patientsButton", "Patients", e -> {
			customPatientEventHandler();
		});

		Button pharmaciesButton = button("pharmaciesButton", "Pharmacies", e -> {
			navigateToScene(AppScene.DoctorPharmaciesListScene);
			
			try {
				((ListView<Button>) sceneNodesMapList.get(currentSceneID).get("pharmaciesList")).getItems().clear();
				ArrayList<Pharmacy> savedPharmacies = Pharmacy.fromJSON();
				ArrayList<Button> pharmacyItemsList = new ArrayList<Button>();
				Button current;
				for (Pharmacy ph : savedPharmacies) {
					current = new Button();
					current.setText(ph.toString());
					current.setOnAction(ev -> { /* TODO */ });
					pharmacyItemsList.add(current);
				}

				Button[] pharmacyItems = new Button[savedPharmacies.size()];
				int i = 0;
				for (Button b : pharmacyItemsList) {
					pharmacyItems[i] = b;
					i++;
				}
				((ListView<Button>) sceneNodesMapList.get(currentSceneID).get("pharmaciesList")).getItems().addAll(pharmacyItems);
			} catch (IOException | ParseException exception) {
				System.out.println("App State couldn't read pharmacies");
				exception.printStackTrace();
			}
		});
		
		Button messagesButton = button("messagesButton", "Messages", e -> {
			navigateToScene(AppScene.DoctorMessagingPortalScene);	
			((ListView<Button>) sceneNodesMapList.get(currentSceneID).get("messagesView")).getItems().clear();
			// load messages to present in messaging portal scene
			// note: important that naviateToScene() is called before we get the mailbox items
			Button[] messagesViewMessageItems;
			try {
				((ListView<Button>) sceneNodesMapList.get(currentSceneID).get("messagesView")).getItems().clear();
				messagesViewMessageItems = currentlyLoggedIn.getMailboxInboxItems();
				((ListView<Button>) sceneNodesMapList.get(currentSceneID).get("messagesView")).getItems().addAll(messagesViewMessageItems);
			} catch (Exception exception) {
				System.out.println("AppState: couldn't read messages");
				exception.printStackTrace();
			}
		});

		Button logoutButton = logoutButton();
		HBox topBarStack = new HBox(100, homeButton, patientsButton, messagesButton, pharmaciesButton, logoutButton);
		topBarStack.setAlignment(Pos.CENTER);
		return topBarStack;	
	}
	
	/*
	 * Mark: PATIENT VIEWS
	 * 
	 */

	// Mark: setupPatientMainViewScene
	static void setupPatientMainViewScene() {
		CURRENT_INDEX = AppScene.PatientMainViewScene.getValue();
		CURRENT_ROOT = sceneRoots.get(CURRENT_INDEX);
		
		// Setup top bar navigation buttons
		HBox topBarStack = makeTopBar();

		// Setup welcomeBack node elements
		Label welcomeBackLabel = label("Welcome Back!", Font.font("Helvetica", FontWeight.SEMI_BOLD, 25));
		Label recentVisitsLabel = label("Recent Visits:", Font.font("Helvetica", FontWeight.NORMAL, 20));
		
		ListView<Button> visitsView = listview("recentVisitsView");
		
		// Setup button stack elements
		Button messagingCenter = button("messagingCenter", "Messaging Center", e -> {
			customNavigateToMessagingPortal();
		});

		Button accountCenter = button("accountCenter", "Account", e -> {
			eventHandler.handleAccountTabPressed();
		});
		
		// Setup layout of scene
		VBox welcomeBackStack = new VBox(25, welcomeBackLabel, recentVisitsLabel, visitsView);
		VBox buttonStack = new VBox(50, messagingCenter, accountCenter);
		HBox bodyStack = new HBox(100, welcomeBackStack, buttonStack);
		bodyStack.setAlignment(Pos.CENTER);
		
		VBox mainStack = new VBox(200, topBarStack, bodyStack);
		mainStack.setAlignment(Pos.TOP_CENTER);
		
		CURRENT_ROOT.getChildren().add(mainStack);
	}
	
	// Mark: setupVisitationLogScene
	static void setupVisitationLogScene() {
		CURRENT_INDEX = AppScene.VisitationLogScene.getValue();
		CURRENT_ROOT = sceneRoots.get(CURRENT_INDEX);
		
		Label titleLabel = label("Visitation Log", Font.font("Helvetica", FontWeight.NORMAL, 20));

		HBox topBarStack = makeTopBar();
		
		ListView<Button> visitsView = listview("visitsView");
		
		VBox bodyStack = new VBox(20, titleLabel, visitsView);
		bodyStack.setAlignment(Pos.TOP_CENTER);
		
		VBox mainStack = new VBox(200, topBarStack, bodyStack);
		mainStack.setAlignment(Pos.TOP_CENTER);
		CURRENT_ROOT.getChildren().add(mainStack);
	}

	// Mark: setupMessagingPortalScene
	static void setupMessagingPortalScene() {
		CURRENT_INDEX = AppScene.MessagingPortalScene.getValue();
		CURRENT_ROOT = sceneRoots.get(CURRENT_INDEX);
		
		Label titleLabel = label("Inbox", Font.font("Helvetica", FontWeight.BOLD, 30));
		
		HBox topBarStack = makeTopBar();
		topBarStack.setAlignment(Pos.CENTER);
		
		ListView<Button> messagesView = listview("messagesView");
		
		Button newMessageButton = navigationButton("newMessage", "New Message", AppScene.ComposeNewMessageScene);
		
		HBox contentStack = new HBox(20, messagesView, newMessageButton);
		contentStack.setAlignment(Pos.CENTER);
		
		VBox bodyStack = new VBox(20, titleLabel, contentStack);
		bodyStack.setAlignment(Pos.CENTER);
		
		VBox mainStack = new VBox(200, topBarStack, bodyStack);
		mainStack.setAlignment(Pos.TOP_CENTER);
		CURRENT_ROOT.getChildren().add(mainStack);
	}
	
	static void setupDoctorMessagingPortalScene() {
		CURRENT_INDEX = AppScene.DoctorMessagingPortalScene.getValue();
		CURRENT_ROOT = sceneRoots.get(CURRENT_INDEX);
		
		Label titleLabel = label("Inbox", Font.font("Helvetica", FontWeight.BOLD, 30));
		
		HBox topBarStack = makeDoctorTopBar();
		topBarStack.setAlignment(Pos.CENTER);
		
		ListView<Button> messagesView = listview("messagesView");
		
		Button newMessageButton = navigationButton("newMessage", "New Message", AppScene.DoctorComposeNewMessageScene);
		
		HBox contentStack = new HBox(20, messagesView, newMessageButton);
		contentStack.setAlignment(Pos.CENTER);
		
		VBox bodyStack = new VBox(20, titleLabel, contentStack);
		bodyStack.setAlignment(Pos.CENTER);
		
		VBox mainStack = new VBox(200, topBarStack, bodyStack);
		mainStack.setAlignment(Pos.TOP_CENTER);
		CURRENT_ROOT.getChildren().add(mainStack);	
	}
	
	static void setupComposeNewMessageScene() {
		CURRENT_INDEX = AppScene.ComposeNewMessageScene.getValue();
		CURRENT_ROOT = sceneRoots.get(CURRENT_INDEX);
		
		Label titleLabel = label("Compose...", Font.font("Helvetica", FontWeight.BOLD, 30));

		Label toLabel = label("To: ", Font.font("Helvetica", FontWeight.BOLD, 30));
		TextField toField = textfield("", "toField");
		HBox toStack = new HBox(10, toLabel, toField);
		//toStack.setAlignment(Pos.CENTER);
		
		Label subjectLabel = label("Subject: ", Font.font("Helvetica", FontWeight.BOLD, 30));
		TextField subjectField = textfield("", "subjectField");
		HBox subjectStack = new HBox(10, subjectLabel, subjectField);
		//subjectStack.setAlignment(Pos.CENTER);

		TextArea composeBox = textarea("composeBox");
		
		Button sendButton = button("sendButton", "Send", e -> {
			try {
				eventHandler.handleMessageSend();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		});
		Button deleteButton = button("deleteButton", "Delete", e -> {
			try {
				eventHandler.handleMessageDelete();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		
		HBox topBarStack = makeTopBar();
		topBarStack.setAlignment(Pos.CENTER);

		VBox composeStack = new VBox(10, toStack, subjectStack, composeBox);
		composeStack.setAlignment(Pos.CENTER_LEFT);
		VBox actionStack = new VBox(10, sendButton, deleteButton);
		actionStack.setAlignment(Pos.CENTER);

		HBox bodyStack = new HBox(10, composeStack, actionStack);
		bodyStack.setAlignment(Pos.CENTER);
		
		VBox mainStack = new VBox(50, topBarStack, titleLabel, bodyStack);
		mainStack.setAlignment(Pos.TOP_CENTER);
		CURRENT_ROOT.getChildren().add(mainStack);
	}
	
		static void setupDoctorComposeNewMessageScene() {
		CURRENT_INDEX = AppScene.DoctorComposeNewMessageScene.getValue();
		CURRENT_ROOT = sceneRoots.get(CURRENT_INDEX);
		
		Label titleLabel = label("Compose...", Font.font("Helvetica", FontWeight.BOLD, 30));

		Label toLabel = label("To: ", Font.font("Helvetica", FontWeight.BOLD, 30));
		TextField toField = textfield("", "toField");
		HBox toStack = new HBox(10, toLabel, toField);
		//toStack.setAlignment(Pos.CENTER);
		
		Label subjectLabel = label("Subject: ", Font.font("Helvetica", FontWeight.BOLD, 30));
		TextField subjectField = textfield("", "subjectField");
		HBox subjectStack = new HBox(10, subjectLabel, subjectField);
		//subjectStack.setAlignment(Pos.CENTER);

		TextArea composeBox = textarea("composeBox");
		
		Button sendButton = button("sendButton", "Send", e -> {
			try {
				eventHandler.handleMessageSend();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		});
		Button deleteButton = button("deleteButton", "Delete", e -> {
			try {
				eventHandler.handleMessageDelete();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		
		HBox topBarStack = makeTopBar();
		topBarStack.setAlignment(Pos.CENTER);

		VBox composeStack = new VBox(10, toStack, subjectStack, composeBox);
		composeStack.setAlignment(Pos.CENTER_LEFT);
		VBox actionStack = new VBox(10, sendButton, deleteButton);
		actionStack.setAlignment(Pos.CENTER);

		HBox bodyStack = new HBox(10, composeStack, actionStack);
		bodyStack.setAlignment(Pos.CENTER);
		
		VBox mainStack = new VBox(50, topBarStack, titleLabel, bodyStack);
		mainStack.setAlignment(Pos.TOP_CENTER);
		CURRENT_ROOT.getChildren().add(mainStack);
	}
	
	static void setupMessageChatDetailView() {
		CURRENT_INDEX = AppScene.ViewMessageDetailScene.getValue();
		CURRENT_ROOT = sceneRoots.get(CURRENT_INDEX);
		
		//Label recipientLabel = label("name of person being messaged", "recipientLabel", Font.font("Helvetica", FontWeight.NORMAL, 15), Color.BLACK);
		
		Label fromLabel = label("From: ", Font.font("Helvetica", FontWeight.NORMAL, 15));
		Label fromField = label("", "fromField", Font.font("Helvetica", FontWeight.NORMAL, 15), Color.BLACK);
		HBox fromStack = new HBox(10, fromLabel, fromField);
		
		Label subjectLabel = label("Subject: ", Font.font("Helvetica", FontWeight.NORMAL, 15));
		Label subjectField = label("", "subjectField", Font.font("Helvetica", FontWeight.NORMAL, 15), Color.BLACK);
		HBox subjectStack = new HBox(10, subjectLabel, subjectField);

		TextArea composeBox = textarea("composeBox");
		
		Button replyButton = button("replyButton", "Reply", e -> {
			try {
				eventHandler.handleMessageReply();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		});
		
		HBox topBarStack = makeTopBar();
		topBarStack.setAlignment(Pos.CENTER);

		VBox composeStack = new VBox(10, fromStack, subjectStack, composeBox);
		composeStack.setAlignment(Pos.CENTER_LEFT);

		HBox bodyStack = new HBox(10, composeStack, replyButton);
		bodyStack.setAlignment(Pos.CENTER);
		
		VBox mainStack = new VBox(75, topBarStack, bodyStack);
		mainStack.setAlignment(Pos.TOP_CENTER);
		CURRENT_ROOT.getChildren().add(mainStack);
	}
	
	// Mark: setupAccountInfoScene
	static void setupAccountInfoScene() {
		CURRENT_INDEX = AppScene.AccountInfoScene.getValue();
		CURRENT_ROOT = sceneRoots.get(CURRENT_INDEX);
		
		HBox topBarStack = makeTopBar();
		topBarStack.setAlignment(Pos.CENTER);

		Label titleLabel = label("Account Information: ", Font.font("Helvetica", FontWeight.BOLD, 30));

		Font labelFont = Font.font("Helvetica", FontWeight.NORMAL, 20);
		VBox accountInfoBox = new VBox(5);
		accountInfoBox.getChildren().addAll(
				label("Email: ", "emailLabel", labelFont, Color.BLACK),
				label("Phone Number: ", "phoneNumberLabel", labelFont, Color.BLACK),
				label("Physical Address: ", "addressLabel", labelFont, Color.BLACK)
		);
		
		VBox otherInformation = new VBox(5);
		otherInformation.getChildren().addAll(
				label("Account ID: ", "idLabel", labelFont, Color.BLACK),
				label("Fullname: ", "fullnameLabel", labelFont, Color.BLACK),
				label("Age: ", "ageLabel", labelFont, Color.BLACK),
				label("Date of Birth: ", "dobLabel", labelFont, Color.BLACK)
		);
		otherInformation.setAlignment(Pos.CENTER);
		
		Button newEditButton = navigationButton("newEdit", "Edit Account Information", AppScene.EditInfoScene);
		
		HBox contactInformation = new HBox(20, accountInfoBox, newEditButton);
		contactInformation.setAlignment(Pos.CENTER);
		
		VBox bodyStack = new VBox(20, contactInformation, otherInformation);
		bodyStack.setAlignment(Pos.CENTER_LEFT);
		
		VBox mainStack = new VBox(50, topBarStack, titleLabel, bodyStack);
		mainStack.setAlignment(Pos.TOP_CENTER);
		CURRENT_ROOT.getChildren().add(mainStack);
	} 
	
	static void setupEditInfoScene() {
		CURRENT_INDEX = AppScene.EditInfoScene.getValue();
		CURRENT_ROOT = sceneRoots.get(CURRENT_INDEX);
		
		HBox topBar = makeTopBar(); 
		
		Label titleLabel = label("Edit Account Information: ", Font.font("Helvetica", FontWeight.BOLD, 30));
		
		Button saveButton = button("saveButton", "Save", e -> {
			eventHandler.handleSaveAccountInfo();
		});
		Button backButton = navigationButton("backButton", "Cancel", AppScene.AccountInfoScene);

		VBox editInfoBox = new VBox(5);
		editInfoBox.getChildren().addAll(
				createForm("Email: ", "emailField"),
				createForm("Phone Number: ", "phoneNumberField"),
				createForm("Physical Address: ", "addressField"),
				createForm("Firstname: ", "firstnameField"),
				createForm("Lastname: ", "lastnameField"),
				createForm("Age: ", "ageField"),
				saveButton,
				backButton
		);
		editInfoBox.setAlignment(Pos.CENTER);
		
		VBox mainStack = new VBox(50, topBar, titleLabel, editInfoBox);
		mainStack.setAlignment(Pos.TOP_CENTER);
		CURRENT_ROOT.getChildren().add(mainStack);
	}
	
	// Mark: setupLoginScene
	static void setupLoginScene() {
		CURRENT_INDEX = AppScene.LoginScene.getValue();
		CURRENT_ROOT = sceneRoots.get(CURRENT_INDEX);
		
		Label errorDisplayLabel = label("", "ERROR LABEL", Font.font("Helvetica", FontWeight.BOLD, 15), Color.RED);
		errorDisplayLabel.setVisible(false);

		Label titleLabel = label("Sign In", Font.font("Helvetica", FontWeight.BOLD, 30));
		titleLabel.setPadding(new Insets(100, 0, 0, 0));
		
		TextField fullnameField = textfield("Fullname (Firstname Lastname)", "fullnameField", "Username");
		TextField birthdayField = textfield("mm/dd/yyyy", "birthdayField");
		PasswordField passwordField = passfield("Password", "passwordField");

		Button loginButton = button("loginButton", "Login", e -> {
			try {
				eventHandler.handleLogin();
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		});
		Button signUpButton = navigationButton("signUpButton", "Sign Up", AppScene.SignUpScene);
		
		VBox bodyStack = new VBox(20, fullnameField, birthdayField, passwordField, loginButton, signUpButton, errorDisplayLabel);
		bodyStack.setMaxWidth(WIDTH * 0.3);
		
		VBox mainStack = new VBox(200, titleLabel, bodyStack);
		mainStack.setAlignment(Pos.TOP_CENTER);
		
		CURRENT_ROOT.getChildren().add(mainStack);
	}
	
	// Mark: setupSignUpScene
	static void setupSignUpScene() {
		CURRENT_INDEX = AppScene.SignUpScene.getValue();
		CURRENT_ROOT = sceneRoots.get(CURRENT_INDEX);

        // initialize main view elements
		Label errorDisplayLabel = label("", "ERROR LABEL", Font.font("Helvetica", FontWeight.BOLD, 15), Color.RED);
		errorDisplayLabel.setVisible(false);

		Label titleLabel = label("Sign Up", Font.font("Helvetica", FontWeight.BOLD, 30));
        titleLabel.setPadding(new Insets(100, 0, 0, 0));

        Label greetings = new Label("Welcome, please create an account"); // Title
        TextField firstnameField = textfield("Firstname", "firstnameField");
        TextField lastnameField = textfield("Lastname", "lastnameField");
        TextField dateOfBirth = textfield("mm/dd/yyyy", "dateOfBirth");
        PasswordField passFieldOne = passfield("Password", "passFieldOne");
        PasswordField passFieldTwo = passfield("Confirm your password", "passFieldTwo");
        Button signUpButton = button("signUpButton", "Sign Up", e -> {
        	try {
        		eventHandler.handleSignUp();
			}
        	catch (Exception e1) {
				e1.printStackTrace();
			}
        });

        VBox fieldStack = new VBox(20, 
        		greetings, 
        		firstnameField, 
        		lastnameField, 
        		dateOfBirth, 
        		passFieldOne, 
        		passFieldTwo, 
        		signUpButton, 
        		errorDisplayLabel
        	);
        fieldStack.setMaxWidth(WIDTH * 0.3);
        
        VBox mainStack = new VBox(200, titleLabel, fieldStack);
        mainStack.setAlignment(Pos.TOP_CENTER);
        
        CURRENT_ROOT.getChildren().add(mainStack);
	}
	

	/*
	 * 
	 * MARK: MEDICAL STAFF VIEWS
	 * 
	 */
	
	// where med staff can view individual patients
	// TODO: when a doctor exits this view entirely the patient being viewed should be signed out
	static void setupDoctorViewPatientScene() {
		CURRENT_INDEX = AppScene.DoctorViewPatientScene.getValue();
		CURRENT_ROOT = sceneRoots.get(CURRENT_INDEX);
		
		HBox topBar = makeDoctorTopBar();

		Label patientTitleLabel = label("Viewing Patient: ", "patientTitleLabel", Font.font("Helvetica", FontWeight.NORMAL, 25), Color.BLACK);
		Label basicInfoLabel = label("", "basicInfoLabel", Font.font("Helvetica", FontWeight.NORMAL, 15), Color.BLACK);
		Label healthRecordsLabel = label("", "healthRecords", Font.font("Helvetica", FontWeight.NORMAL, 15), Color.BLACK);

		Button visitsHistoryButton = button("visitsHistoryButton", "View Visit History", e -> {});
		Button addNewVisit = button("addNewVisit", "Add New Visit", e -> {
			eventHandler.handleAddNewVisit();
		} );

		VBox bodyStack = new VBox(20, patientTitleLabel, basicInfoLabel, healthRecordsLabel, visitsHistoryButton, addNewVisit);
		bodyStack.setAlignment(Pos.CENTER);
		
		VBox mainStack = new VBox(100, topBar, bodyStack);
		mainStack.setAlignment(Pos.TOP_CENTER);
		CURRENT_ROOT.getChildren().add(mainStack);
	}
	
	// the main view of the doctor side of the app
	@SuppressWarnings("unchecked")
	static void setupDoctorMainViewScene() {
		CURRENT_INDEX = AppScene.DoctorMainViewScene.getValue();
		CURRENT_ROOT = sceneRoots.get(CURRENT_INDEX);
		
		HBox topBar = makeDoctorTopBar();
		Label titleLabel = label("Welcome Back!", Font.font("Helvatica", FontWeight.BOLD, 30));

		Button createNewVisit = button("createNewVisit", "Create New Visit", e -> {
			customPatientEventHandler();
		});

		Button findAPatient = button("findAPatient", "Find a Patient", e -> {
			customPatientEventHandler();
		});

		Button findPharmacy = button("findPharmacy", "Find a Pharmacy", e -> {
			navigateToScene(AppScene.DoctorPharmaciesListScene);
			
			try {
				((ListView<Button>) sceneNodesMapList.get(currentSceneID).get("pharmaciesList")).getItems().clear();
				ArrayList<Pharmacy> savedPharmacies = Pharmacy.fromJSON();
				ArrayList<Button> pharmacyItemsList = new ArrayList<Button>();
				Button current;
				for (Pharmacy ph : savedPharmacies) {
					current = new Button();
					current.setText(ph.toString());
					current.setOnAction(ev -> { /* TODO */ });
					pharmacyItemsList.add(current);
				}

				Button[] pharmacyItems = new Button[savedPharmacies.size()];
				int i = 0;
				for (Button b : pharmacyItemsList) {
					pharmacyItems[i] = b;
					i++;
				}
				((ListView<Button>) sceneNodesMapList.get(currentSceneID).get("pharmaciesList")).getItems().addAll(pharmacyItems);
			} catch (IOException | ParseException exception) {
				System.out.println("App State couldn't read pharmacies");
				exception.printStackTrace();
			}
		});
		Button messagingCenter = button("messagingCenter", "Messaging Center", e -> {
			navigateToScene(AppScene.DoctorMessagingPortalScene);	
			// load messages to present in messaging portal scene
			// note: important that naviateToScene() is called before we get the mailbox items
			Button[] messagesViewMessageItems;
			try {
				((ListView<Button>) sceneNodesMapList.get(currentSceneID).get("messagesView")).getItems().clear();
				messagesViewMessageItems = currentlyLoggedIn.getMailboxInboxItems();
				((ListView<Button>) sceneNodesMapList.get(currentSceneID).get("messagesView")).getItems().addAll(messagesViewMessageItems);
			} catch (Exception exception) {
				System.out.println("AppState: couldn't read messages");
				exception.printStackTrace();
			}
		});

		HBox firstSet = new HBox(10, createNewVisit, findAPatient);
		firstSet.setAlignment(Pos.CENTER);
		HBox secondSet = new HBox(10, findPharmacy, messagingCenter);
		secondSet.setAlignment(Pos.CENTER);

        VBox mainStack = new VBox(100, topBar, titleLabel, firstSet, secondSet);
        mainStack.setAlignment(Pos.TOP_CENTER);
		
		CURRENT_ROOT.getChildren().add(mainStack);
	}
	
	// the view that shows all the current patients that the office has
	static void setupDoctorsPatientsListScene() {
		CURRENT_INDEX = AppScene.DoctorPatientsListScene.getValue();
		CURRENT_ROOT = sceneRoots.get(CURRENT_INDEX);
		
		HBox topBar = makeDoctorTopBar();
		Label titleLabel = label("Patient Search", Font.font("Helvatica", FontWeight.NORMAL, 30));
		titleLabel.setAlignment(Pos.TOP_LEFT);
		
		// updated in makeDoctorTopBar, in the event handler of the patients tab button
		ListView<Button> patientsView = listview("patientsView"); 
		
        VBox mainStack = new VBox(50, topBar, titleLabel, patientsView);
        mainStack.setAlignment(Pos.TOP_CENTER);	
		
		CURRENT_ROOT.getChildren().add(mainStack);
	}
	
	// this is the view that shows all the visits for a specific patient
	static void setupDoctorVisitationLogScene() {
		CURRENT_INDEX = AppScene.DoctorVisitationLogScene.getValue();
		CURRENT_ROOT = sceneRoots.get(CURRENT_INDEX);
		
		Label titleLabel = label("Visitation Log", Font.font("Helvetica", FontWeight.NORMAL, 20));

		HBox topBarStack = makeDoctorTopBar();
		
		ListView<Button> visitsView = listview("visitsView");
		
		VBox bodyStack = new VBox(20, titleLabel, visitsView);
		bodyStack.setAlignment(Pos.TOP_CENTER);
		
		VBox mainStack = new VBox(200, topBarStack, bodyStack);
		mainStack.setAlignment(Pos.TOP_CENTER);
		CURRENT_ROOT.getChildren().add(mainStack);
	}
	
	// this is the view where the nurse inputs the findings from the patient visit
	static void setupDoctorAddNewVisit() {
		CURRENT_INDEX = AppScene.DoctorAddNewVisitView.getValue();
		CURRENT_ROOT = sceneRoots.get(CURRENT_INDEX);
		
		HBox topBar = makeDoctorTopBar();
		
		HBox visitDate = createForm("Date:", "dateForm");
		HBox heightForm = createForm("Height:", "heightForm");
		HBox weightForm = createForm("Weight:", "weightForm");
		HBox pulseForm = createForm("Pulse Rate: ", "pulseRateForm");
		HBox respForm = createForm("Respiration Rate: ", "respForm");
		HBox bodyTempForm = createForm("Body Temperature:", "bodyTempForm");
		HBox bloodPressForm = createForm("Blood Pressure:", "bloodPressForm");
		HBox allergiesForm = createForm("Known Allergies:", "allergiesForm");
		HBox currentMedsForm = createForm("Current Medications:", "currentMedsForm");
		HBox otherConcernsForm = createForm("Other Concerns:", "otherConcernsForm");
		HBox findings = createForm("Findings", "findingsForm");
		HBox medNameForm = createForm("Medication Name:", "medNameForm");
		HBox medDoseForm = createForm("Medication Dose:", "medDoseForm");
		HBox pharmacyAddressForm = createForm("Pharmacy Physical Address:", "pharmAddressForm");
		Button saveVisitButton = button("saveVisitButton", "Save Visit", e -> {});
		
		VBox bodyStack = new VBox(20,
				    visitDate,
					heightForm,
					weightForm,
					pulseForm,
					respForm,
					bodyTempForm,
					bloodPressForm,
					allergiesForm,
					currentMedsForm,
					otherConcernsForm,
					findings,
					medNameForm,
					medDoseForm,
					pharmacyAddressForm,
					saveVisitButton
				);
		bodyStack.setAlignment(Pos.TOP_CENTER);
		
		VBox mainStack = new VBox(100, topBar, bodyStack);
		mainStack.setAlignment(Pos.TOP_CENTER);
		CURRENT_ROOT.getChildren().add(mainStack);
	}
	
	static void setupDoctorPharmaciesListScene() {
		CURRENT_INDEX = AppScene.DoctorPharmaciesListScene.getValue();
		CURRENT_ROOT = sceneRoots.get(CURRENT_INDEX);
		
		HBox topBar = makeDoctorTopBar();
		
		Label titleLabel = label("Pharmacy List", Font.font("Helvetica", FontWeight.BOLD, 25));
		Button addNew = navigationButton("addNewPharmacyButton", "Add New", AppScene.DoctorAddNewPharmacyScene);
		ListView<Button> pharmaciesList = listview("pharmaciesList");
		
		VBox bodyStack = new VBox(5);
		bodyStack.setAlignment(Pos.CENTER);
		bodyStack.getChildren().addAll(
				new HBox(50, titleLabel, addNew),
				pharmaciesList
		);

		VBox mainStack = new VBox(50, topBar, bodyStack);
		CURRENT_ROOT.getChildren().add(mainStack);
	}
	
	static void setupDoctorAddPharmacyScene() {
		CURRENT_INDEX = AppScene.DoctorAddNewPharmacyScene.getValue();
		CURRENT_ROOT = sceneRoots.get(CURRENT_INDEX);
		
		HBox topBar = makeDoctorTopBar();
		
		VBox actionStack = new VBox(10);
		actionStack.getChildren().addAll(
				button("saveButton", "Save", e -> {
					eventHandler.handleAddNewPharmacy();
				}),
				navigationButton("cancelButton", "Cancel", AppScene.DoctorMainViewScene)
		);
		actionStack.setAlignment(Pos.CENTER);

		VBox addPharmStack = new VBox(10);
		addPharmStack.getChildren().addAll(
				createForm("Physical Address: ", "physAddField"),
				createForm("Phone Number: ", "phoneNumberField"),
				createForm("Email: ", "emailField"),
				createForm("Chain: ", "chainField")
		);
		addPharmStack.setAlignment(Pos.CENTER);
		
		VBox bodyStack = new VBox(50);
		Label addNewPharmLabel = label("Adding New Pharmacy: ", Font.font("Helvetica", FontWeight.BOLD, 25));
		addNewPharmLabel.setAlignment(Pos.CENTER);
		HBox horizontalStack = new HBox(10, addPharmStack, actionStack);
		horizontalStack.setAlignment(Pos.CENTER);
		bodyStack.getChildren().addAll(
				addNewPharmLabel,
				horizontalStack
		);
		bodyStack.setAlignment(Pos.TOP_CENTER);
		
		VBox mainStack = new VBox(50, topBar, bodyStack);
		mainStack.setAlignment(Pos.TOP_CENTER);
		CURRENT_ROOT.getChildren().add(mainStack);
	}
}