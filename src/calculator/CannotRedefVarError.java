package calculator;

public class CannotRedefVarError extends CalculatorError {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4911470416545686670L;
	
	public CannotRedefVarError(String varname) {
		super("cannot redefine variable: " + varname);
	}
}
