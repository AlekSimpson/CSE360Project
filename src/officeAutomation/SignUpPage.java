package officeAutomation;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Label;

public class SignUpPage extends Application {
	final int WIDTH = 1920;
	final int HEIGHT = 1800;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Doctors Office Automation System");
        StackPane root = new StackPane();
        VBox mainStack = new VBox(10);
        mainStack.setAlignment(Pos.CENTER);
        
        
        VBox fieldStack = new VBox(20);


        // Title
        //Label titleLabel = new Label("Welcome, please create an account");

        // first name field
        TextField firstnameField = new TextField("Firstname");
        firstnameField.setMaxWidth(300);

        TextField lastnameField = new TextField("Lastname"); // last name field
        
        TextField dateOfBirth = new TextField("dd/mm/yyyy"); // date of birth field

        PasswordField passFieldOne = new PasswordField(); // password field
        passFieldOne.setPromptText("Password");

        PasswordField passFieldTwo = new PasswordField(); // confirm password field
        passFieldTwo.setPromptText("Confirm your password");

        // sign up button
        Button signUpButton = new Button();
    	signUpButton.setText("Sign Up");
        signUpButton.setOnAction(e -> {
        	handleSignUp(signUpButton);

        });
        
        mainStack.getChildren().add(fieldStack);
        root.getChildren().add(mainStack);
        primaryStage.setScene(new Scene(root, WIDTH, HEIGHT));
        primaryStage.show();
	}
	
	
	private void handleSignUp(Button signUpButton) {
		
	}
}
