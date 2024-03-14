package officeAutomation;

public class AppResult<T> {
	T value;
	ApplicationError error;
	
	AppResult(T v, ApplicationError e) {
		value = v;
		error = e;
	}
	
	public boolean isOk() {
		return value != null;
	}
	
	// should only be called after isOk() is checked
	public T andThen() {
		return value;
	}
	
	public boolean isErr() {
		return value == null;
	}
	
	public ApplicationError orElse() {
		return error;
	}
}
