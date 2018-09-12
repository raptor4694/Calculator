package calculator.expressions;

import calculator.Scope;
import calculator.Visitor;
import calculator.errors.TypeError;
import calculator.values.Number;
import calculator.values.Real;
import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionVarPrefixIncrement implements ExpressionPrefixIncrement {
	public final String variable;
	
	@Override
	public Object eval(Scope scope) {
		Object obj = evalReference(scope);
		if (!(obj instanceof Number))
			throw new TypeError();
		scope.setVariable(variable, obj = ((Number) obj).plus(Real.ONE));
		return obj;
	}
	
	@Override
	public Object evalReference(Scope scope) {
		return scope.getVariable(variable);
	}
	
	@Override
	public String toCompiledString() {
		return "<INC \"%s\">".format((Object) variable);
	}
	
	@Override
	public String toEvalString() {
		return "++" + variable;
	}
	
	@Override
	public String toString() {
		return "VarPrefixIncrement{variable=\"%s\"}".format((Object) variable);
	}
	
	@Override
	public String getNameString() {
		return variable;
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitPrefixIncrement(this);
	}
	
}
