package officeAutomation;

import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.ParseException;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

// this class will a singleton that keeps track of the states of every node in the application
// that way when we want to update or access the state of a node we have a single instance to instantiate and work with
public class AbstractAppState {
	final static int WIDTH = 1600;
	final static int HEIGHT = 900;

	public static AppStateEventHandler eventHandler = null;
	
	static ArrayList<Map<String, Node>> sceneNodesMapList;
	static ArrayList<Scene> scenes;
	static ArrayList<StackPane> sceneRoots;
	boolean appIsRunning;
	static Stage primaryStage;
	static int currentSceneID;
	int sceneAmount;
	static Account currentlyLoggedIn;
	static Message focusedMessage;
	static Patient focusedPatient;
	public static MedicalStaff staffAccount;
	
	protected AbstractAppState() {
		eventHandler = new AppStateEventHandler();
		appIsRunning = true;
		currentSceneID = AppScene.LoginScene.getValue();
		sceneAmount = AppScene.amount;
		sceneNodesMapList = new ArrayList<Map<String, Node>>();
		scenes = new ArrayList<Scene>();
		sceneRoots = new ArrayList<StackPane>();
		for (int i = 0; i < sceneAmount; i++) {
			StackPane root = new StackPane();
			sceneRoots.add(root);
			sceneNodesMapList.add(new HashMap<String, Node>());
			scenes.add(new Scene(root, WIDTH, HEIGHT));
		}

		// initialize the medical staff
		try {
			staffAccount = MedicalStaff.getInstance();
		} catch (InvalidKeySpecException | IOException | ParseException e) {
			e.printStackTrace();
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
	
	@SuppressWarnings("unchecked")
	public static ListView<Button> getList(int i, String id) {
		return (ListView<Button>) sceneNodesMapList.get(i).get(id);
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
	
	//public static class AppQuery<T> {
	//	@SuppressWarnings("unchecked")
	//	public T getNode(int i, String id) {
	//		return (T) sceneNodesMapList.get(i).get(id);
	//	}
	//}

	protected class AppStateEventHandler {
		public void handleMessageSend() throws Exception {
			// TODO: clearError();
			String recipient = ((TextField) getNode("toField")).getText();
			String messageSubject = ((TextField) getNode("subjectField")).getText();
			String messageText = ((TextArea) getNode("composeBox")).getText();
			
			// make and send the message
			if (currentlyLoggedIn.isMedStaff) {
				// TODO: check that id is valid
				System.out.println("recipient: " + recipient);
				String[] delimited = recipient.split(", ");
				System.out.println("delimited:");
				System.out.printf("[0]: %s, [1]: %s\n", delimited[0], delimited[1]);
				((MedicalStaff) currentlyLoggedIn).sendMessage(delimited[0], delimited[1], messageSubject, messageText);
			}
			else {
				currentlyLoggedIn.sendMessage(recipient, messageSubject, messageText);			
			}
		}
		
		public void handleMessageReply() throws Exception {
			// TODO: clearError();
			navigateToScene(AppScene.ComposeNewMessageScene);
			
			((TextField) getNode("toField")).setText(focusedMessage.from + ", " + focusedMessage.fromUID);
			((TextField) getNode("subjectField")).setText("Re: " + focusedMessage.subject);
		}

		@SuppressWarnings("unchecked")
		public void handleLogin() throws Exception {
			clearError();
			String fullname = ((TextField) getNode("fullnameField")).getText();
			String birthdayRawText = ((TextField) getNode("birthdayField")).getText();
			String password = ((PasswordField) getNode("passwordField")).getText();
			
			// check if the user signing is signing into the staff side of the app
			if (fullname.equals("Doctor's Office") && staffAccount.authenticaLogin(password)) {
				loginSuccess(staffAccount, AppScene.DoctorMainViewScene); // log the user in
				return;
			}
			
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
			
			// otherwise they are a patient loggging into the patient side
			Patient patientToLogin; 
			try {
				patientToLogin = new Patient(puid, password, false);			
				loginSuccess(patientToLogin, AppScene.PatientMainViewScene); // log the user in
				
				//recentVisitsView
				if (patientToLogin.visits != null && !patientToLogin.visits.isEmpty()) {
					if (patientToLogin.visits.size() == 1) {
						Button recentVisitsButton = new Button(); // just show the first two
						recentVisitsButton.setText(patientToLogin.visits.get(0).date.toString());
						((ListView<Button>) sceneNodesMapList.get(currentSceneID).get("recentVisitsView")).getItems().addAll(recentVisitsButton);
					}
					else if (patientToLogin.visits.size() == 2) {
						Button[] recentVisitsButtons = new Button[2]; // just show the first two
						Button current;
						int i = 0;
						for (Visit v : patientToLogin.visits) {
							if (i == 2)
								break;

							current = new Button();
							current.setText(v.date.toString());
							recentVisitsButtons[i] = current;
						}
						((ListView<Button>) sceneNodesMapList.get(currentSceneID).get("recentVisitsView")).getItems().addAll(recentVisitsButtons);
					}
				}
			}
			catch (Exception e) {
				ApplicationError err = new ApplicationError("Bad Password", "Please check that ALL your inputted credentials are correct");
				displayError(err);
				return;
			}
		}
		
		public void handleMessageButtonPressed(Message message) throws Exception {
			navigateToScene(AppScene.ViewMessageDetailScene);
			
			focusedMessage = message;
			
			// set recipientLabel with message.to
			((Label) getNode("fromField")).setText(message.from);
			((Label) getNode("subjectField")).setText(message.subject);
			// populate text area with message
			((TextArea) getNode("composeBox")).setText(message.readMessage());
		}
		
		public void handleMessageDelete() throws Exception {
			((TextField) getNode("toField")).setText("");
			((TextField) getNode("subjectField")).setText("");
			((TextArea) getNode("composeBox")).setText("Type here...");	
			
			focusedMessage = null;

			navigateToScene(AppScene.MessagingPortalScene);
		}
		
		public void handleDocPatientButtonPress(String puid, String pHash) throws Exception {
			navigateToScene(AppScene.DoctorViewPatientScene);
			Patient p = new Patient(puid, "", true);
			focusedPatient = p;
			
			((Button) getNode("visitsHistoryButton")).setOnAction(e -> {
				try {
					System.out.println("viewing patient visits");
					handleDoctorVisitLogPressed(p);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			});
			
			((Label) getNode("patientTitleLabel")).setText("View Patient: " + p.getFullname());

			String pn = String.valueOf(p.getPhoneNumber());
			String basicInfoString = String.format(
					"Basic Information:\nFullname: %s\nDate of Birth: %s\n Email: %s\n Phone Number: %s\n Physical Address: N/A\n", 
					p.getFullname(), p.dateOfBirth.toString(), p.getEmail(), pn
				);
			((Label) getNode("basicInfoLabel")).setText(basicInfoString);
		}
		
		@SuppressWarnings("unchecked")
		public void handleDoctorVisitLogPressed(Patient p) throws Exception {
			navigateToScene(AppScene.DoctorVisitationLogScene);
			
			System.out.println("patient name: " + p.getFullname());
			if (p.visits != null && !p.visits.isEmpty()) {
				System.out.println("1");
				ArrayList<Button> buttons = new ArrayList<Button>();
				Button currentButton;
				for (Visit v : p.visits) {
					System.out.println("found visit");
					currentButton = new Button();
					currentButton.setText(v.date.toString());
					currentButton.setOnAction(e -> {});
					buttons.add(currentButton);
				}
				Button[] copyButtons = new Button[buttons.size()];
				int i = 0;
				for (Button b : buttons) {
					copyButtons[i] = b;
					i++;
				}
				
				((ListView<Button>) getNode("visitsView")).getItems().addAll(copyButtons);
			}
		}
		

		
		public void handleAddingNewVisit(Patient p) throws Exception {
			// TODO clearError();
			
			String dateForm = ((TextField) getNode("dateForm")).getText();
			String weight = ((TextField) getNode("weightForm")).getText();
			String height = ((TextField) getNode("heightForm")).getText(); 
			String pulseRate = ((TextField) getNode("pulseRateForm")).getText();
			String respRate = ((TextField) getNode("respForm")).getText();
			String bodyTemp = ((TextField) getNode("bodyTempForm")).getText();
			String bloodPress = ((TextField) getNode("bloodPressForm")).getText(); // systollic/diastollic
			String allergies = ((TextField) getNode("allergiesForm")).getText();
			String currentMeds = ((TextField) getNode("currentMedsForm")).getText(); // medname:dosage:pharmAddress
			String otherConcerns = ((TextField) getNode("otherConcernsForm")).getText();
			String findings = ((TextField) getNode("findingsForm")).getText();
			String medName = ((TextField) getNode("medNameForm")).getText();
			String medDoseForm = ((TextField) getNode("medDoseForm")).getText();
			String pharmAddressForm = ((TextField) getNode("pharmAddressForm")).getText();
			
			if (!PatientDate.validateFormat(dateForm)) {
				// TODO: DISPLAY ERROR, FAIL TO SUBMIT
				System.out.println("date format incorrect");
				return;
			}
			
			if (!bloodPress.equals("") && !bloodPress.contains("/")) {
				// TODO: DISPLAY ERROR, FAIL TO SUBMIT
				System.out.println("Blood pressure input not formatted correctly");
				return;
			}
			if (!currentMeds.equals("") && !currentMeds.toUpperCase().equals("NONE")) {
				if (!currentMeds.contains(":")) {
					// TODO: DISPLAY ERROR, FAIL TO ADD VISIT
					System.out.println("medication not in correct format");
					return;
				}
			}

			if (
				dateForm.equals("") ||
				weight.equals("") ||
				height.equals("") ||
				bodyTemp.equals("") ||
				bloodPress.equals("") ||
				allergies.equals("") ||
				findings.equals("")
			) {
				// display error, all forms must be filled out before submitting
				return;
			}
			
			String[] bloodDelimited = bloodPress.split("/");
			int systollic = Integer.parseInt(bloodDelimited[0]);
			int diastollic = Integer.parseInt(bloodDelimited[1]);
			
			int pr, rr;
			try {
				pr = Integer.parseInt(pulseRate);
				rr = Integer.parseInt(respRate);			
			}
			catch (Exception ex)  {
				// display error
				return;
			}
			
			Visit newVisit = new Visit(
					0,
					Double.parseDouble(weight),
					Double.parseDouble(height),
					Double.parseDouble(bodyTemp),
					dateForm,
					pr,
					rr,
					systollic, 
					diastollic,
					allergies,
					currentMeds,
					otherConcerns,
					findings
				);

			if (p.visits == null) {
				p.visits = new ArrayList<Visit>();
			}
			p.addVisit(newVisit);
			p.save("", true);
			navigateToScene(AppScene.DoctorVisitationLogScene);
			System.out.println("visit added successfully");
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
		
		@SuppressWarnings("unchecked")
		public void patientNavigateToVisitsScene() {
			navigateToScene(AppScene.VisitationLogScene);
			Button[] visitsViewItems;
			((ListView<Button>) sceneNodesMapList.get(currentSceneID).get("visitsView")).getItems().clear();
			Patient patientLoggedIn = (Patient) currentlyLoggedIn;
			ArrayList<Button> buttons = new ArrayList<Button>();
			Button currButton;
			for (Visit v : patientLoggedIn.visits) {
				currButton = new Button();
				currButton.setText(v.date.toString());
				currButton.setOnAction(null);
				buttons.add(currButton);
			}
			
			Button[] buttonsCopy = new Button[buttons.size()];
			int i = 0;
			for (Button b : buttons) {
				buttonsCopy[i] = b;
				i++;
			}
			
			((ListView<Button>) sceneNodesMapList.get(currentSceneID).get("visitsView")).getItems().addAll(buttonsCopy);
		}
		
		protected void loginSuccess(Account loggedIn, AppScene destScene) {
			currentlyLoggedIn = loggedIn;
			((TextField) getNode("fullnameField")).setText("Fullname (Firstname Lastname)");
			((TextField) getNode("birthdayField")).setText("mm/dd/yyyy");
			((TextField) getNode("passwordField")).setText("");
			
			navigateToScene(destScene);
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

		public void handleAddNewVisit() {
			navigateToScene(AppScene.DoctorAddNewVisitView);
			
			((Button) getNode("saveVisitButton")).setOnAction(e -> {
				try {
					handleAddingNewVisit(focusedPatient);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			});
		}

		public void handleAccountTabPressed() {
			navigateToScene(AppScene.AccountInfoScene);
			Patient p = (Patient) currentlyLoggedIn;
			
			((Label) getNode("emailLabel")).setText("Email: " + p.email);
			((Label) getNode("phoneNumberLabel")).setText("Phone Number: " + p.phoneNumber);
			((Label) getNode("addressLabel")).setText("Physical Address: "); // TODO
			((Label) getNode("idLabel")).setText("Account ID: " + p.UID);
			((Label) getNode("fullnameLabel")).setText("Fullname: " + p.getFullname());
			((Label) getNode("ageLabel")).setText("Age: " + p.age);
			((Label) getNode("dobLabel")).setText("Date of Birth: " + p.dateOfBirth);
		}

		public void handleSaveAccountInfo() {
			// TODO display message when info is saved successfully
			Patient p = (Patient) currentlyLoggedIn;
			String[] fields = {
					"emailField", "phoneNumberField",
					"addressField", "firstnameField",
					"lastnameField", "ageField"
			};
			ArrayList<String> fieldsToSave = new ArrayList<String>();
			
			String input;
			for (String f : fields) {
				input = ((TextField) getNode(f)).getText();
				if (!input.equals("")) {
					fieldsToSave.add(f);
				}
			}
			
			for (String f : fieldsToSave) {
				input = ((TextField) getNode(f)).getText();
				boolean success = saveField(p, f, input);
				if (!success) {
					// TODO DISPLAY ERROR
					return;
				}
			}
			
			try {
				p.save("", true);
			} catch (Exception e) {
				System.out.printf("Unable to save patient because:\n%s\n%s\n", e.toString(), e.getStackTrace());
			}
		}
		
		public boolean saveField(Patient p, String field, String value) {
			switch (field) {
			case "emailField":
				p.setEmail(value);
				break;
			case "phoneNumberField":
				if (value.length() != 10) {
					return false;
				}
				p.setPhoneNumber(Long.parseLong(value));
				break;
			case "addressField":
				// TODO
				break;
			case "firstnameField":
				p.firstname = value;
				break;
			case "lastnameField":
				p.lastname = value;
				break;
			case "ageField":
				if (Integer.parseInt(value) >= 100 || Integer.parseInt(value) <= 0) {
					return false;
				}
				p.age = Integer.parseInt(value);
				break;
			case "genderField":
				break; // TODO
			default:
				return false;
			}
			
			return true;
		}

		public void handleAddNewPharmacy() {
			// TODO errors
			System.out.println("curr id: " + currentSceneID);
			String physAdd = ((TextField) getNode("physAddField")).getText();
			String phoneNumber = ((TextField) getNode("phoneNumberField")).getText();
			String email = ((TextField) getNode("emailField")).getText();
			String chain = ((TextField) getNode("chainField")).getText();
			
			if (
				physAdd.equals("") || 
				phoneNumber.equals("") ||
				email.equals("") ||
				chain.equals("")
			) {
				// DISPLAY ERROR
				return;
			}
			
			long number;
			try {
				number = Long.parseLong(phoneNumber);			
			}
			catch (Exception exception) {
				// DISPLAY ERROR
				return;
			}
			
			Pharmacy pharmacy = new Pharmacy(physAdd, chain, number, email);
			try {
				pharmacy.saveNewPharmacy();
				navigateToScene(AppScene.DoctorMainViewScene);
				System.out.println("saved new pharmacy");
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			}
		}
	}
}
