package calculator.errors;

public class TypeError extends CalculatorError {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8743694303243822713L;
	
	public TypeError() {
		super("data type");
	}
	
	public TypeError(String msg) {
		super("data type: " + msg);
	}
	
	public TypeError(String expected, String got) {
		super("data type: expected " + expected + ", got " + got);
	}
	
	public TypeError(Throwable t) {
		super("data type", t);
	}
}
