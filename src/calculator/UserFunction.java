package calculator;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserFunction implements Function {
	@Getter
	final String name;
	final String[] varnames;
	final Expression body;
	String description = null;
	
	@Override
	public Object callOptionalValue(Scope scope, Object... args) {
		if (args.length != varnames.length)
			throw new DimensionError();
		
		Object ans = null;
		
		scope = new Scope(scope);
		
		for (int i = 0; i < varnames.length; i++) {
			scope.setVariable(varnames[i], args[i]);
		}
		
		Object result;
		
		try {
			result = body.evalOptionalValue(scope);
		} catch (ExpressionReturn ex) {
			return ex.lastValue;
		}
		
		return result;
	}
	
	@Override
	public Object call(Scope scope, Object... args) {
		Object result = callOptionalValue(scope, args);
		if (result == null)
			throw new CalculatorError(
					(getName() == null? "<lambda function>"
							: getName()) + " does not return a value");
		return result;
	}
	
	@Override
	public boolean returnsValue() {
		return true;
	}
	
	@Override
	public String getDescription() {
		return description == null? toString() : description;
	}
	
	@Override
	public int maxArgCount() {
		return varnames.length;
	}
	
	@Override
	public int minArgCount() {
		return varnames.length;
	}
	
	@Override
	public String toString() {
		String s = Arrays.toString(varnames);
		s = "(" + s.substring(1, s.length() - 1) + ")";
		if (name == null) {
			return s + (body instanceof ExpressionMulti
					? " => { " + body.toEvalString() + " }"
					: " => " + body.toEvalString());
		} else {
			return name + s
					+ (body instanceof ExpressionMulti
							? " { " + body.toEvalString() + " }"
							: " = " + body.toEvalString());
		}
	}
}
