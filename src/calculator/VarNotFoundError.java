package calculator;

public class VarNotFoundError extends CalculatorError {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8542983925515530577L;
	
	public VarNotFoundError(String name) {
		super("variable not found: " + name);
	}
}
