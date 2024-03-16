package officeAutomation;

import java.security.spec.InvalidKeySpecException;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class EventHandler {
	public void handleSignUp() throws Exception {
		AppState appState = AppState.getInstance();
		String test = ((PasswordField) appState.getNode(AppScenes.SignUpPage.getValue(), "passFieldOne")).getText();
		System.out.println(test);

		//String passFieldOne = ((PasswordField) appState.getNode("passFieldOne")).getText();
		//String passFieldTwo = ((PasswordField) appState.getNode("passFieldTwo")).getText();
		//String firstname = ((TextField) appState.getNode("firstnameField")).getText();
		//String lastname = ((TextField) appState.getNode("lastnameField")).getText();
		//String dateOfBirth = ((TextField) appState.getNode("dateOfBirth")).getText();
		//int[] dateTuple;

		//try {
		//	dateTuple = parseInputtedDate(dateOfBirth);		
		//}
		//catch(Exception e) {
		//	System.out.println(e.toString());
		//	return;
		//}


		//if (passFieldOne.equals(passFieldTwo)) {
		//	// create account
		//	try {
		//		Patient newPatient = new Patient(firstname, lastname, dateTuple[0], dateTuple[1], dateTuple[2], passFieldOne);
		//		newPatient.save(passFieldOne, false);
		//	} catch (InvalidKeySpecException e) {
		//		System.out.println("Error: could not create new user");
		//		System.out.println(e.toString());
		//	}
		//	return;
		//}
		
		// else output an error 
		//System.out.println("Sorry your passwords do not match");
	}
	
	private int[] parseInputtedDate(String text) {
		int[] dateTuple = {0, 0, 0}; // year, month, day

		String[] delimited = text.split("/");
		if (delimited.length != 3) {
			System.out.println("please use the correct date format when inputting the date (mm/dd/yyyy)");
			return dateTuple;
		}

		int day, month, year;
		try {
			day = Integer.parseInt(delimited[1]);
			month = Integer.parseInt(delimited[0]);
			year = Integer.parseInt(delimited[2]);		
		}
		catch (Exception e) {
			throw e;
		}

		dateTuple[0] = year;
		dateTuple[1] = month;
		dateTuple[2] = day;
		return dateTuple;
	}
}
