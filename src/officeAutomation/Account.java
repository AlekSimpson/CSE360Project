package officeAutomation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javafx.scene.control.Button;

public class Account {
	protected final static String stringSecretKey = "programSecretKey";
	protected final static String stringSalt = "programSecretSalt";

	String firstname;
	String lastname;
	protected String UID;
	String email;
	String accountHash;
	PatientDate dateOfBirth;
	long phoneNumber;
	int age;
	public ArrayList<Message> mailbox;
	public boolean isMedStaff;
	
	public Account() {
		firstname = "";
		lastname = "";
		email = "";
		dateOfBirth = null;
		phoneNumber = 0;
		age = 0;
		mailbox = new ArrayList<Message>();
		isMedStaff = false;
	}
	
	// for the medical staff class
	public Account(String fn, String ln, String e, long pn, String uid) throws InvalidKeySpecException {
		firstname = fn;
		lastname = ln;
		email = e;
		dateOfBirth = null;
		phoneNumber = pn;
		UID = uid;
		mailbox = new ArrayList<Message>();
		isMedStaff = true;
		
		AppResult<String> result = SecurityHandler.getHandler().getPasswordHash("medstaffpassword");
		if (result.isOk()) {
			accountHash = result.andThen();
		}
	}
	
	public Account(String fn, String ln, String e, PatientDate date, long pn, int a) {
		firstname = fn;
		lastname = ln;
		email = e;
		dateOfBirth = date;
		phoneNumber = pn;
		age = a;
		mailbox = new ArrayList<Message>();
		isMedStaff = false;
		
		createUniqueID();
	}
	
	public String toString() {
		StringBuilder buff = new StringBuilder();
		
		buff.append("-----------------------\n");
		buff.append(String.format("firstname: %s\n", firstname));
		buff.append(String.format("lastname: %s\n", lastname));
		buff.append(String.format("patientUniqueID: %s\n", UID));
		buff.append(String.format("email: %s\n", email));
		buff.append(String.format("accountHash: %s\n", accountHash));
		buff.append(String.format("dateOfBirth: %s\n", dateOfBirth.toString()));
		buff.append(String.format("phoneNumber: %d\n", phoneNumber));
		buff.append(String.format("age: %d\n", age));
		buff.append("-----------------------\n");
		return buff.toString();
	}
	
	public void setEmail(String e) {
		email = e;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setPhoneNumber(long pn) {
		phoneNumber = pn;
	}
	
	public long getPhoneNumber() {
		return phoneNumber;
	}
	
	public String getName() {
		return String.format("%s %s", firstname, lastname);
	}
	
	public String getFirstname() {
		return firstname;
	}
	
	public String getLastname() {
		return lastname;
	}
	
	public String getUID() {
		return UID;
	}
	
	public int getCurrentAge() {
		return age;
	}
	
	public void updateAge() {
		age+=1;
	}
	
	public void arbitrarilyUpdateAge(int newAge) {
		age = newAge;
	}
	
	public boolean readMailbox() throws IOException, ParseException {
		String mailboxFilePath = "./src/officeAutomation/ApplicationData/messages.json";
		File mailboxFile = new File(mailboxFilePath);
		JSONParser parser = new JSONParser();
		
		String fileText = Files.readString(mailboxFile.toPath());
		
		JSONObject rootObj = (JSONObject) parser.parse(fileText);
		
		JSONObject userInbox = (JSONObject) rootObj.get(UID);
		// check if there is mail to read
		if (userInbox == null) {
			return false;
		}
		else if (userInbox.isEmpty()) {
			return false;
		}
		
		String currKey;
		ArrayList<Message> payload;
		for (Object rawKey : userInbox.keySet()) {
			currKey = (String) rawKey;
			payload = Message.fromJSON(UID, currKey);
			mailbox.addAll(payload);
		}
		
		return true; // found mail to read
	}
	
	@SuppressWarnings("unchecked")
	public void saveAccountHash() throws IOException, ParseException {
		File metadata = new File("./src/officeAutomation/ApplicationData/metadata.json");
		
		// initialize parser and read in file text
		JSONParser parser = new JSONParser();
		String fileText = Files.readString(metadata.toPath());
		
		// if the metadata.json file is empty for some reason then make a new json object
		JSONObject rootObj = new JSONObject();
		if (fileText != "") {
			rootObj = (JSONObject) parser.parse(fileText);
		}

		// if patients list has not yet been created then create it
		Object jsonObj = rootObj.get("patients");
		if (jsonObj == null) {
			rootObj.put("patients", new JSONObject());
		}
		
		JSONObject patients = (JSONObject) rootObj.get("patients");
		patients.put(UID, accountHash);

		String jsonText = rootObj.toString();
		Path filepath = Paths.get("./src/officeAutomation/ApplicationData/metadata.json");
		Files.write(filepath, jsonText.getBytes());
	}
	
	Button[] getMailboxInboxItems() throws Exception {
		boolean mailFound;
		try {
			mailFound = readMailbox();
			
			if (!mailFound) {
				System.out.println("did not find mail");
				return new Button[0];
			}

			ArrayList<Button> inbox = new ArrayList<Button>();

			Button inboxItem;
			for (Message m : mailbox) {
				inboxItem = new Button();
				inboxItem.setId(String.valueOf(m.getID()));
				inboxItem.setOnAction(e -> {
					try {
						AppState.eventHandler.handleMessageButtonPressed(m);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				});
				String itemTitle = m.isUnread() 
						? "Unread:\n" + "From: " + m.from + "\nTo: " + m.to + "\nSubject: " + m.subject 
						: "From: " + m.from + "\nTo: " + m.to + "\nSubject: " + m.subject;
				inboxItem.setText(itemTitle);	
				AppState.addNodes(AppState.currentSceneID, inboxItem);
				inbox.add(inboxItem);
			}

			Button[] buttonArr = new Button[inbox.size()];
			for (int i = 0; i < inbox.size(); i++) {
				buttonArr[i] = inbox.get(i);
			}

			return buttonArr;
		} 
		catch (IOException | ParseException e) {
			throw e;
		}
	}
	
	// function is meant to be overwritten
	public void sendMessage(String recipient, String subject, String messageText) {
		System.out.println("Please use an overrided version of this method");
	}
	
	private void createUniqueID() {
		// we do not want to regenerate a unique ID if one already exists
		if (UID != null) {
			return;
		}
		int numberTag = dateOfBirth.day + dateOfBirth.month + dateOfBirth.year;

		// create id buffer to build the id
		StringBuilder buffer = new StringBuilder();
		buffer.append(firstname);
		buffer.append(lastname);
		buffer.append(numberTag);	 
		
		// set the unique ID
		UID = buffer.toString();
	}
}
