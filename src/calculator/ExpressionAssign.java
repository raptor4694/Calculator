package calculator;

import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionAssign implements ExpressionNamed {
	String variable;
	Expression expr;
	
	@Override
	public Object eval(Scope scope) {
		Object value = expr.eval(scope);
		scope.setVariable(variable, value);
		
		return value;
	}
	
	@Override
	public String toEvalString() {
		return variable + " = " + expr.toEvalString();
	}
	
	@Override
	public String toCompiledString() {
		return "<SET \"%s\" TO %s>".format((Object) variable,
				expr.toCompiledString());
	}
	
	@Override
	public String toString() {
		return "Assign{variable=\"%s\",expr=%s}".format(
				(Object) variable, expr);
	}
	
	@Override
	public String getNameString() {
		return variable;
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitAssign(this);
	}
	
}
