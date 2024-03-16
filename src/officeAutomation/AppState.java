package officeAutomation;

import java.util.ArrayList;
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
	final int WIDTH = 1920;
	final int HEIGHT = 1800;

	private static AppState singleInstance = null;
	
	ArrayList<Map<String, Node>> sceneNodesMapList;
	ArrayList<Scene> scenes;
	ArrayList<StackPane> sceneRoots;
	boolean appIsRunning;
	Stage primaryStage;
	int currentSceneID;
	int sceneAmount;
	
	private AppState() {
		appIsRunning = true;
		currentSceneID = AppScenes.SignUpPage.getValue();
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
	}
	
	public static synchronized AppState getInstance() {
		if (singleInstance == null) {
			return new AppState();
		}
		
		return singleInstance;
	}
	
	private Map<String, Node> currAppNodes() {
		return sceneNodesMapList.get(currentSceneID);
	}

	public void setCurrentSceneID(int newID) {
		currentSceneID = newID;
	}
	
	public void addNodes(Node... nodes) {
		for (Node n : nodes) {
			currAppNodes().put(n.getId(), n);
		}
	}
	
	private void addNodesAtIndex(int i, Node... nodes) {
		for (Node n : nodes) {
			sceneNodesMapList.get(i).put(n.getId(), n);
		}
	}
	
	public void setPrimaryStage(Stage ps) {
		primaryStage = ps;
        primaryStage.setTitle("Doctors Office Automation System");
	}
	
	public Scene getCurrentScene() {
		return scenes.get(currentSceneID);
	}
	
	public int getNodesLength() {
		return currAppNodes().size();
	}
	
	public void printNodes() {
		System.out.println("Nodes:");
		for (String s : currAppNodes().keySet()) {
			System.out.println(s);
		}
		System.out.println("---------------");
	}
	
	public Node getNode(String id) {
		return currAppNodes().get(id);
	}
	
	public Node getNode(int i, String id) {
		return sceneNodesMapList.get(i).get(id);
	}
	
	void setupSignUpScene() throws Exception {
		int index = AppScenes.SignUpPage.getValue();
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
        fieldStack.setMaxWidth(WIDTH * 0.3); // fieldStack width is 70% of screen width
        
        VBox mainStack = new VBox(200, titleLabel, fieldStack);
        mainStack.setAlignment(Pos.TOP_CENTER);
        
        root.getChildren().add(mainStack);
        addNodesAtIndex(index, titleLabel, greetings, firstnameField, lastnameField, dateOfBirth, passFieldOne, passFieldTwo, signUpButton);
        
        // setup event handlers last
        EventHandler handler = new EventHandler();
        signUpButton.setOnAction(e -> {
        	try {
        		handler.handleSignUp();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        });
	}
}
