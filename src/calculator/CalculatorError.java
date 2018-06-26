package calculator;

public class CalculatorError extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2592792041287834586L;
	
	public CalculatorError() {}
	
	public CalculatorError(String msg) {
		super(msg);
	}
	
	public CalculatorError(Throwable cause) {
		super(cause);
	}
	
	public CalculatorError(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	@Override
	public String toString() {
		return "Error: " + getMessage();
	}
}
