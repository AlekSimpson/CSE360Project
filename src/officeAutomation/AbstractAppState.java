package officeAutomation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

// this class will a singleton that keeps track of the states of every node in the application
// that way when we want to update or access the state of a node we have a single instance to instantiate and work with
public class AbstractAppState {
	final static int WIDTH = 1600;
	final static int HEIGHT = 900;

	protected static AppStateEventHandler eventHandler = null;
	
	static ArrayList<Map<String, Node>> sceneNodesMapList;
	static ArrayList<Scene> scenes;
	static ArrayList<StackPane> sceneRoots;
	boolean appIsRunning;
	static Stage primaryStage;
	static int currentSceneID;
	int sceneAmount;
	static Patient currentlyLoggedIn;
	
	protected AbstractAppState() {
		eventHandler = new AppStateEventHandler();
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
	}
	
	protected static Map<String, Node> currAppNodes() {
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
		return scenes.get(currentSceneID);
	}
	
	public int getNodesLength() {
		return currAppNodes().size();
	}
	
	// for debugging
	@SuppressWarnings("unused")
	protected void printAllNodes() {
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
	
	public void setLabelText(String id, String text) {
		((Label) currAppNodes().get(id)).setText(text);
	}
	
	public void setLabelVisibility(String id, boolean visible) {
		((Label) currAppNodes().get(id)).setVisible(visible);
	}
	
	public void setTextField(String id, String text) {
		((TextField) currAppNodes().get(id)).setText(text);
	}
	
	public void setButton(String id, String text) {
		((Button) currAppNodes().get(id)).setText(text);
	}
	
	public void displayError(ApplicationError err) {
		setLabelText("ERROR LABEL", err.throwError());
		setLabelVisibility("ERROR LABEL", true);
	}
	
	public void clearError() {
		setLabelText("ERROR LABEL", "");
		setLabelVisibility("ERROR LABEL", false);
	}

	protected static void navigateToScene(AppScene scene) {
		currentSceneID = scene.getValue();
		primaryStage.setScene(scenes.get(currentSceneID));
	}
	
	/*
	 * 
	 * Mark: AppStateEventHandler 
	 *	 This class is sort of like the "toolbox" for the apps scenes
	 *	 to utilize when they receive the correct input
	 */

	protected class AppStateEventHandler {
		public void handleLogin() throws Exception {
			clearError();
			String fullname = ((TextField) getNode("fullnameField")).getText();
			String birthdayRawText = ((TextField) getNode("birthdayField")).getText();
			String password = ((PasswordField) getNode("passwordField")).getText();
			PatientDate date = new PatientDate();
			try {
				date = new PatientDate(birthdayRawText);
			}
			catch (Exception e) {
				// BAD DATE ERROR
				ApplicationError err = new ApplicationError("Bad Date", "Invalid date format, date must follow the form mm/dd/yyyy");
				displayError(err);
				return;
			}
			
			String[] delimitedFullname = fullname.split(" ");
			String puid = getUniqueID(delimitedFullname[0], delimitedFullname[1], date); // patient unique
			
			Patient patientToLogin; 
			try {
				patientToLogin = new Patient(puid, password, false);			
				currentlyLoggedIn = patientToLogin;
				((TextField) getNode("fullnameField")).setText("Fullname (Firstname Lastname)");
				((TextField) getNode("birthdayField")).setText("mm/dd/yyyy");
				((TextField) getNode("passwordField")).setText("");
				
				navigateToScene(AppScene.PatientMainViewScene);
			}
			catch (Exception e) {
				ApplicationError err = new ApplicationError("Bad Password", "Please check that ALL your inputted credentials are correct");
				displayError(err);
				return;
			}
		}
		
		public void handleSignUp() throws Exception {
			clearError();
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
				ApplicationError err = new ApplicationError("Bad Date", "Invalid date format, date must follow the form mm/dd/yyyy");
				displayError(err);
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
					ApplicationError err = new ApplicationError("Error", e.toString());
					displayError(err);
				}
				return;
			}
			
			// else output an error 
			ApplicationError err = new ApplicationError("Bad Credentials", "Sorry your passwords do not match");
			displayError(err);
		}
		
		protected String getUniqueID(String firstname, String lastname, PatientDate date) {
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
