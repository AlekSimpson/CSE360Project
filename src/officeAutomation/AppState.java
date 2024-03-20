package officeAutomation;

import java.security.spec.InvalidKeySpecException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import java.util.Map;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

// this class will a singleton that keeps track of the states of every node in the application
// that way when we want to update or access the state of a node we have a single instance to instantiate and work with
public class AppState {
	final static int WIDTH = 1600;
	final static int HEIGHT = 900;

	private static AppState singleInstance = null;
	private static EventHandler eventHandler = null;
	
	static ArrayList<Map<String, Node>> sceneNodesMapList;
	static ArrayList<Scene> scenes;
	static ArrayList<StackPane> sceneRoots;
	boolean appIsRunning;
	static Stage primaryStage;
	static int currentSceneID;
	int sceneAmount;
	
	private AppState() {
		eventHandler = new EventHandler();
		appIsRunning = true;
		currentSceneID = AppScene.LoginScene.getValue();
		sceneAmount = 9;
		sceneNodesMapList = new ArrayList<Map<String, Node>>();
		scenes = new ArrayList<Scene>();
		sceneRoots = new ArrayList<StackPane>();
		for (int i = 0; i < sceneAmount; i++) {
			StackPane root = new StackPane();
			sceneRoots.add(root);
			sceneNodesMapList.add(new HashMap<String, Node>());
			scenes.add(new Scene(root, WIDTH, HEIGHT));
		}
		
		// setup scenes
		setupLoginScene();
		setupSignUpScene();
		setupPatientMainViewScene();
		//printAllNodes(); // <-- for debug
	}
	
	public static synchronized AppState getInstance() {
		if (singleInstance == null) {
			return new AppState();
		}
		
		return singleInstance;
	}
	
	private static Map<String, Node> currAppNodes() {
		return sceneNodesMapList.get(currentSceneID);
	}
	
	public static void addNodes(Node... nodes) {
		for (Node n : nodes) {
			currAppNodes().put(n.getId(), n);
		}
	}
	
	public static void addNodes(int index, Node... nodes) {
		for (Node n : nodes) {
			sceneNodesMapList.get(index).put(n.getId(), n);
		}
	}
	
	public void setPrimaryStage(Stage ps) {
		primaryStage = ps;
        primaryStage.setTitle("Doctors Office Automation System");
	}
	
	public Scene getCurrentScene() {
		scenes.get(currentSceneID).getStylesheets().add("//src/officeAutomation/stylesheet.css");
		return scenes.get(currentSceneID);
	}
	
	public int getNodesLength() {
		return currAppNodes().size();
	}
	
	// for debugging
	@SuppressWarnings("unused")
	private void printAllNodes() {
		int i = 0;
		for (Map<String, Node> m : sceneNodesMapList) {
			System.out.printf("Nodes: %s\n", i);
			for (String s : m.keySet()) {
				System.out.println(s);
			}
			System.out.println("---------------");		
			i++;
		}
	}
	
	public Node getNode(String id) {
		return currAppNodes().get(id);
	}
	
	public Node getNode(int i, String id) {
		return sceneNodesMapList.get(i).get(id);
	}
	
	private static void navigateToScene(AppScene scene) {
		currentSceneID = scene.getValue();
		primaryStage.setScene(scenes.get(currentSceneID));
	}
	
	static void setupPatientMainViewScene() {
		int index = AppScene.PatientMainViewScene.getValue();
		StackPane root = sceneRoots.get(index);
		
		Label titleLabel = new Label("patient main page");
		titleLabel.setId("titleLabel");
		titleLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, 30));
		titleLabel.setPadding(new Insets(100, 0, 0, 0));
		
		VBox mainStack  = new VBox(200, titleLabel);
		mainStack.setAlignment(Pos.TOP_CENTER);
		
		root.getChildren().add(mainStack);
		addNodes(index, titleLabel);
	}
	
	static void setupLoginScene() {
		int index = AppScene.LoginScene.getValue();
		StackPane root = sceneRoots.get(index);
		
		Label titleLabel = new Label("Sign In");
		titleLabel.setId("titleLabel");
		
		TextField fullnameField = new TextField("Fullname (Firstname Lastname)");
		fullnameField.setId("fullnameField");
		TextField birthdayField = new TextField("mm/dd/yyyy");
		birthdayField.setId("birthdayField");
		PasswordField passwordField = new PasswordField();
		passwordField.setId("passwordField");

		Button loginButton = new Button();
		loginButton.setId("loginButton");
		loginButton.setText("Login");

		Button signUpButton = new Button();
		signUpButton.setId("signUpButton");
		signUpButton.setText("Sign Up");
		
		// setup input fields
		fullnameField.setPromptText("Username");
		passwordField.setPromptText("Password");
		
		// title label setup
		titleLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, 30));
		titleLabel.setPadding(new Insets(100, 0, 0, 0));
		
		VBox bodyStack = new VBox(20, fullnameField, birthdayField, passwordField, loginButton, signUpButton);
		bodyStack.setMaxWidth(WIDTH * 0.3);
		
		VBox mainStack = new VBox(200, titleLabel, bodyStack);
		mainStack.setAlignment(Pos.TOP_CENTER);
		
		root.getChildren().add(mainStack);
		addNodes(index, titleLabel, fullnameField, passwordField, loginButton, signUpButton, birthdayField);
		
		// setup event handlers for buttons
		loginButton.setOnAction(e -> {
			try {
				eventHandler.handleLogin();
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		});

		signUpButton.setOnAction(e -> {
			navigateToScene(AppScene.SignUpScene);
		});
	}
	
	static void setupSignUpScene() {
		int index = AppScene.SignUpScene.getValue();
		StackPane root = sceneRoots.get(index);

        // initialize main view elements
        Label titleLabel = new Label("Sign Up");
        titleLabel.setId("titleLabel");
        Label greetings = new Label("Welcome, please create an account"); // Title
        greetings.setId("greetings");
        TextField firstnameField = new TextField("Firstname"); // first name field
        firstnameField.setId("firstnameField");
        TextField lastnameField = new TextField("Lastname"); // last name field
        lastnameField.setId("lastnameField");
        TextField dateOfBirth = new TextField("mm/dd/yyyy"); // date of birth field
        dateOfBirth.setId("dateOfBirth");
        PasswordField passFieldOne = new PasswordField(); // password field
        passFieldOne.setId("passFieldOne");
        PasswordField passFieldTwo = new PasswordField(); // confirm password field
        passFieldTwo.setId("passFieldTwo");
        Button signUpButton = new Button();
        signUpButton.setId("signUpButton");

        // setup view elements
        passFieldOne.setPromptText("Password");
        passFieldTwo.setPromptText("Confirm your password");
        titleLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, 30));
        titleLabel.setPadding(new Insets(100, 0, 0, 0));

        // setup sign up button
    	signUpButton.setText("Sign Up");

        
        VBox fieldStack = new VBox(20, greetings, firstnameField, lastnameField, dateOfBirth, passFieldOne, passFieldTwo, signUpButton);
        fieldStack.setMaxWidth(WIDTH * 0.3);
        
        VBox mainStack = new VBox(200, titleLabel, fieldStack);
        mainStack.setAlignment(Pos.TOP_CENTER);
        
        root.getChildren().add(mainStack);
        addNodes(index, titleLabel, greetings, firstnameField, lastnameField, dateOfBirth, passFieldOne, passFieldTwo, signUpButton);
        
        // setup event handlers last
        signUpButton.setOnAction(e -> {
        	try {
        		eventHandler.handleSignUp();
			}
        	catch (Exception e1) {
				e1.printStackTrace();
			}
        });
	}

	private class EventHandler {
		public void handleLogin() throws Exception {
			String fullname = ((TextField) getNode("fullnameField")).getText();
			String birthdayRawText = ((TextField) getNode("birthdayField")).getText();
			String password = ((PasswordField) getNode("passwordField")).getText();
			PatientDate date = new PatientDate();
			try {
				date = new PatientDate(birthdayRawText);
			}
			catch (Exception e) {
				// TODO: display error to user
				System.out.println("Invalid date format, date must follow the form mm/dd/yyyy");
			}
			
			String[] delimitedFullname = fullname.split(" ");
			String puid = getUniqueID(delimitedFullname[0], delimitedFullname[1], date); // patient unique
			
			Patient patientToLogin; 
			try {
				patientToLogin = new Patient(puid, password, false);			
				navigateToScene(AppScene.PatientMainViewScene);
			}
			catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		
		public void handleSignUp() throws Exception {
			String passFieldOne = ((PasswordField) getNode("passFieldOne")).getText();
			String passFieldTwo = ((PasswordField) getNode("passFieldTwo")).getText();
			String firstname = ((TextField) getNode("firstnameField")).getText();
			String lastname = ((TextField) getNode("lastnameField")).getText();
			String dateOfBirth = ((TextField) getNode("dateOfBirth")).getText();
			PatientDate date;

			try {
				date = new PatientDate(dateOfBirth);
			}
			catch(Exception e) {
				System.out.println(e.toString());
				return;
			}


			if (passFieldOne.equals(passFieldTwo)) {
				// create account
				try {
					Patient newPatient = new Patient(firstname, lastname, date, passFieldOne);
					newPatient.save(passFieldOne, false);
					
					navigateToScene(AppScene.LoginScene);
				}
				catch (Exception e) {
					System.out.println("Error: could not create new user");
					System.out.println(e.toString());
				}
				return;
			}
			
			// else output an error 
			System.out.println("Sorry your passwords do not match");
		}
		
		private String getUniqueID(String firstname, String lastname, PatientDate date) {
			int numberTag = date.day + date.month + date.year;
			// create id buffer to build the id
			StringBuilder buffer = new StringBuilder();
			buffer.append(firstname);
			buffer.append(lastname);
			buffer.append(numberTag);
			
			// return id
			return buffer.toString();
		}
	}
}
