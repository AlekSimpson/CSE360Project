package officeAutomation;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
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
		setupComposeNewMessageScene();
		setupDoctorMainViewScene();
		setupMessageChatDetailView();
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
	private static HBox makeTopBar() {
		Button homeButton = navigationButton("homeButton", "Home", AppScene.PatientMainViewScene);
		Button visitsButton = navigationButton("visitsButton", "Visits", AppScene.VisitationLogScene);
		Button messagesButton = button("messagesButton", "Messages", e -> {
			navigateToScene(AppScene.MessagingPortalScene);	
			Button[] messagesViewMessageItems;
			try {
				((ListView<Button>) sceneNodesMapList.get(currentSceneID).get("messagesView")).getItems().clear();
				currentlyLoggedIn.mailbox.clear();
				messagesViewMessageItems = currentlyLoggedIn.getMailboxInboxItems();
				((ListView<Button>) sceneNodesMapList.get(currentSceneID).get("messagesView")).getItems().addAll(messagesViewMessageItems);
			} catch (Exception exception) {
				System.out.println("AppState: couldn't read messages");
				exception.printStackTrace();
			}		
		});
		Button accountButton = navigationButton("accountButton", "Account", AppScene.AccountInfoScene);
		Button logoutButton = button("logoutButton", "Logout", e -> {
			currentlyLoggedIn.mailbox.clear();
			currentlyLoggedIn = null;
			navigateToScene(AppScene.LoginScene);
		});	
		HBox topBarStack = new HBox(100, homeButton, visitsButton, messagesButton, accountButton, logoutButton);
		topBarStack.setAlignment(Pos.CENTER);
		return topBarStack;
	}
	
	@SuppressWarnings("unchecked")
	private static HBox makeDoctorTopBar() {
		Button homeButton = navigationButton("homeButton", "Home", AppScene.DoctorMainViewScene);
		Button patientsButton = navigationButton("patientsButton", "Patients", AppScene.DoctorPatientsListScene);
		Button pharmaciesButton = navigationButton("pharmaciesButton", "Pharmacies", AppScene.DoctorPharmaciesListScene);
		Button messagesButton = button("messagesButton", "Messages", e -> {
			navigateToScene(AppScene.MessagingPortalScene);	
			// load messages to present in messaging portal scene
			// note: important that naviateToScene() is called before we get the mailbox items
			Button[] messagesViewMessageItems;
			try {
				messagesViewMessageItems = currentlyLoggedIn.getMailboxInboxItems();
				((ListView<Button>) sceneNodesMapList.get(currentSceneID).get("messagesView")).getItems().addAll(messagesViewMessageItems);
			} catch (Exception exception) {
				System.out.println("AppState: couldn't read messages");
				exception.printStackTrace();
			}
		});
		Button logoutButton = button("logoutButton", "Logout", e -> {
			currentlyLoggedIn = null;
			navigateToScene(AppScene.LoginScene);
		});	
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
		
		Button[] visitItemButtons = {};
		ListView<Button> visitsView = new ListView<Button>();
		visitsView.getItems().addAll(visitItemButtons);
		visitsView.setPrefSize(500, 500);
		
		// Setup button stack elements
		Button messagingCenter = button("messagingCenter", "Messaging Center", e -> {});
		Button accountCenter = button("accountCenter", "Account", e -> {});
		
		// Setup layout of scene
		VBox welcomeBackStack = new VBox(50, welcomeBackLabel, recentVisitsLabel, visitsView);
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
		
		Label titleLabel = label("Visitation Log Scene", Font.font("Helvetica", FontWeight.BOLD, 30));

		HBox topBarStack = makeTopBar();
		
		VBox mainStack = new VBox(200, titleLabel, topBarStack);
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
		
		Label titleLabel = label("Account Info Scene", Font.font("Helvetica", FontWeight.BOLD, 30));
		
		HBox topBarStack = makeTopBar();
		topBarStack.setAlignment(Pos.CENTER);
		
		VBox mainStack = new VBox(200, titleLabel, topBarStack);
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
	
	static void setupDoctorMainViewScene() {
		CURRENT_INDEX = AppScene.DoctorMainViewScene.getValue();
		CURRENT_ROOT = sceneRoots.get(CURRENT_INDEX);
		
		Label titleLabel = label("Doctor Main View", Font.font("Helvatica", FontWeight.BOLD, 30));
		HBox topBar = makeDoctorTopBar();
		
        VBox mainStack = new VBox(200, topBar, titleLabel);
        mainStack.setAlignment(Pos.TOP_CENTER);
		
		CURRENT_ROOT.getChildren().add(mainStack);
	}
	
}
