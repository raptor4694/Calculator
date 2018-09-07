package calculator.values;

import calculator.Scope;

public interface Function {
	Object call(Scope scope, Object... args);
	
	default Object callOptionalValue(Scope scope, Object... args) {
		return call(scope, args);
	}
	
	String getName();
	
	String getDescription();
	
	int maxArgCount();
	
	int minArgCount();
	
	@Deprecated
	boolean returnsValue();
}
