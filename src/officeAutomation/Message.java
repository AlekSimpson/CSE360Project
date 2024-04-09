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
	String subject;
	// if the message is a reply to another message then the id of the message it is replying to is stored here
	String idOfMessageRepliedTo; 
	private boolean isUnread;
	private final static String messagesFilePath = "./src/officeAutomation/ApplicationData/messages.json";
	int messageChainIndex;
	
	private Message() {}
	
	Message(String sub, String t, String tuid, String fr, String fuid, String m, int mci) {
		to = t;
		from = fr;
		message = m;	
		
		toUID = tuid;
		fromUID = fuid;
		
		isUnread = true;
		messageChainIndex = mci;
		subject = sub;
		generateID();
	}
	
	public static ArrayList<Message> fromJSON(String accountID, String id) throws IOException, ParseException {
		ArrayList<Message> messages = new ArrayList<Message>();
		JSONParser parser = new JSONParser();
		File messagesFile = new File(messagesFilePath);
		String fileText = Files.readString(messagesFile.toPath());
		JSONObject rootObj = (JSONObject) parser.parse(fileText);

		JSONObject accountObj = (JSONObject) rootObj.get(accountID);
		if (accountObj == null) // user has no messages
			return null;
		
		// NOTE: THIS IS ASSUMING THE MESSAGE IS NOT A REPLY CHAIN
		JSONObject messageObj = (JSONObject) accountObj.get(id);
		Message currMessage = new Message();
		currMessage.ID = (String) messageObj.get("ID");
		currMessage.to = (String) messageObj.get("to");
		currMessage.toUID = (String) messageObj.get("toUID");
		currMessage.from = (String) messageObj.get("from");
		currMessage.fromUID = (String) messageObj.get("fromUID");
		currMessage.subject = (String) messageObj.get("subject");
		currMessage.message = (String) messageObj.get("message");
		currMessage.isUnread = (boolean) messageObj.get("isUnread");
		currMessage.messageChainIndex = Integer.parseInt((String) messageObj.get("messageChainIndex"));
		messages.add(currMessage);

		return messages;
	}
	
	public static Message composeAndSendMessage(String sub, String to, String tuid, String fr, String fuid, String mes) {
		Message m = new Message(sub, to, tuid, fr, fuid, mes, 0);
		try {
			m.sendMessage(false);
			System.out.println("message sent");
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		return m;
	}
	
	public String getID() {
		return ID;
	}
	
	public boolean isUnread() {
		return isUnread;
	}
	
	public Message reply(String message) {
		Message m = new Message("Re:" + subject, from, fromUID, to, toUID, message, messageChainIndex + 1);
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
		messageObj.put("subject", subject);
		messageObj.put("messageChainIndex", String.valueOf(messageChainIndex));
		
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
		int messageId = rand.nextInt(99999 - 10000) + 99999;
		String randomizedStringID = Integer.toString(messageId);

		ID = randomizedStringID;
	}
}
