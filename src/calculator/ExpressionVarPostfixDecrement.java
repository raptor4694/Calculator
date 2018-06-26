package calculator;

import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionVarPostfixDecrement
		implements ExpressionPostfixDecrement {
	String variable;
	
	@Override
	public String toCompiledString() {
		return "<\"%s\" DEC>".format((Object) variable);
	}
	
	@Override
	public Object eval(Scope scope) {
		Object obj = evalReference(scope);
		if (!(obj instanceof Number))
			throw new TypeError();
		scope.setVariable(variable, ((Number) obj).minus(Real.ONE));
		return obj;
	}
	
	@Override
	public Object evalReference(Scope scope) {
		return scope.getVariable(variable);
	}
	
	@Override
	public String getNameString() {
		return variable;
	}
	
	@Override
	public String toString() {
		return "VarPostfixDecrement{variable=\"%s\"}".format(
				(Object) variable);
	}
	
	@Override
	public String toEvalString() {
		return variable + "--";
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitPostfixDecrement(this);
	}
	
}
