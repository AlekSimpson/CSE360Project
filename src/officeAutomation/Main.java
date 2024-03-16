package officeAutomation;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
	AppState appState = AppState.getInstance();

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		appState.setPrimaryStage(primaryStage);
		appState.setupSignUpScene();
        primaryStage.setScene(appState.getCurrentScene());
        primaryStage.show();
	}
}
