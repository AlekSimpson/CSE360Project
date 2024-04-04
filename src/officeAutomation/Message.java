package officeAutomation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Message {
	private String ID;
	String to;
	String toUID;
	String from;
	String fromUID;
	String message;
	private boolean isUnread;
	private final static String messagesFilePath = "./src/officeAutomation/ApplicationData/metadata.json";
	int messageChainIndex;
	
	private Message() {}
	
	private Message(String t, String tuid, String fr, String fuid, String m, int mci) {
		to = t;
		from = fr;
		message = m;	
		
		toUID = tuid;
		fromUID = fuid;
		
		isUnread = true;
		messageChainIndex = mci;
		generateID();
	}
	
	public static ArrayList<Message> fromJSON(String accountID, String id) throws IOException, ParseException {
		ArrayList<Message> messages = new ArrayList<Message>();
		// Message m = new Message();
		JSONParser parser = new JSONParser();
		File messagesFile = new File(messagesFilePath);
		String fileText = Files.readString(messagesFile.toPath());
		JSONObject rootObj = (JSONObject) parser.parse(fileText);

		JSONObject accountObj = (JSONObject) rootObj.get(accountID);
		if (accountObj == null) // user has no messages
			return null;
		
		JSONObject messageObj = (JSONObject) accountObj.get(id);
		JSONObject currJson;
		Message currMessage;
		for (int i = 0; i < messageObj.size(); i++) {
			currMessage = new Message();
			currJson = (JSONObject) messageObj.get(i);
			currMessage.ID = (String) currJson.get("ID");
			currMessage.to = (String) currJson.get("to");
			currMessage.toUID = (String) currJson.get("toUID");
			currMessage.from = (String) currJson.get("from");
			currMessage.fromUID = (String) currJson.get("fromUID");
			currMessage.message = (String) currJson.get("message");
			currMessage.isUnread = (boolean) currJson.get("isUnread");
			currMessage.messageChainIndex = (int) currJson.get("messageChainIndex");
		}

		return messages;
	}
	
	public Message composeAndSendMessage(String to, String tuid, String fr, String fuid, String mes) {
		Message m = new Message(to, tuid, fr, fuid, mes, 0);
		try {
			m.sendMessage(false);
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		return m;
	}
	
	public Message reply(String message) {
		Message m = new Message(from, fromUID, to, toUID, message, messageChainIndex + 1);
		try {
			m.sendMessage(true);
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		return m;
	}
	
	public String readMessage() {
		isUnread = false;
		return message;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJSON() {
		JSONObject messageObj = new JSONObject();
		messageObj.put("ID", ID);
		messageObj.put("to", to);
		messageObj.put("toUID", toUID);
		messageObj.put("from", from);
		messageObj.put("fromUID", fromUID);
		messageObj.put("message", message);
		messageObj.put("isUnread", isUnread);
		messageObj.put("messageChainIndex", messageChainIndex);
		
		return messageObj;
	}
	
	@SuppressWarnings("unchecked")
	private void sendMessage(boolean isReply) throws IOException, ParseException {
		File messagesFile = new File(messagesFilePath);
		JSONParser parser = new JSONParser();
		String fileText = Files.readString(messagesFile.toPath());
		
		JSONObject rootObj = new JSONObject();
		if (fileText != "") {
			rootObj = (JSONObject) parser.parse(fileText);
		}

		// if user doesn't have inbox make one
		Object accObj = rootObj.get(toUID);
		if (accObj == null) {
			JSONObject inbox = new JSONObject();
			rootObj.put(toUID, inbox);
		}
		
		JSONObject accountInbox = (JSONObject) rootObj.get(toUID);
		JSONObject appendObj = accountInbox;

		if (isReply) {
			// because of the logic flow of the application a reply should always have an object to find
			appendObj = (JSONObject) accountInbox.get(ID);
			appendObj.put(messageChainIndex, toJSON());
		}
		else {
			appendObj.put(ID, toJSON());
		}
		
		String jsonText = rootObj.toString();
		Path filepath = Paths.get(messagesFilePath);
		Files.write(filepath, jsonText.getBytes());
	}
	
	private void generateID() {
		Random rand = new Random();
		byte[] nullArray = new byte[15];
		rand.nextBytes(nullArray);
		String randomizedStringID = new String(nullArray, Charset.forName("UTF-8"));

		ID = randomizedStringID;
	}
}
