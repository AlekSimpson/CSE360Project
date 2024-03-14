package officeAutomation;

public class ApplicationError {
	private static String errorName;
	private static String errorMessage;
	
	ApplicationError() {
		errorName = "Application Error";
		errorMessage = "An error occurred in the Application";
	}
	
	ApplicationError(String en, String em) {
		errorName = en;
		errorMessage = em;
	}
	
	public String getName() {
		return errorName;
	}
	
	public String throwError() {
		StringBuilder builder = new StringBuilder();
		builder.append(errorName + ":");
		builder.append("\n");
		builder.append(errorMessage);
		return builder.toString();
	}
}
