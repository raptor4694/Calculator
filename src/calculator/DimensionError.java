package calculator;

public class DimensionError extends CalculatorError {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4558005382850506087L;
	
	public DimensionError() {
		super("dimension");
	}
	
	public DimensionError(String msg) {
		super("dimension: " + msg);
	}
	
	public DimensionError(int dimension) {
		super("dimension: " + dimension);
	}
	
	@Override
	public String toString() {
		return super.toString() + "\n" + "(check index >= 1, correct # of function arguments, non-empty array)";
	}
}
