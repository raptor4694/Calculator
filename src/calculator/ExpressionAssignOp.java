package calculator;

import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionAssignOp implements ExpressionNamed {
	String variable;
	EnumOperator operator;
	Expression expr;
	
	@Override
	public Object eval(Scope scope) {
		Object var = scope.getVariable(variable);
		Object result = operator.call(scope, var, expr.eval(scope));
		scope.setVariable(variable, result);
		return result;
	}
	
	@Override
	public String toEvalString() {
		return "%s %s= %s".format((Object) variable,
				operator.getSymbol(), expr.toEvalString());
	}
	
	@Override
	public String toCompiledString() {
		switch (operator) {
		case ADD:
		case SUBTRACT:
			return "<%s %s %s \"%s\">".format(operator,
					expr.toCompiledString(), operator.getVerb(),
					variable);
		default:
			return "<%s \"%s\" %s %s>".format(operator, variable,
					operator.getVerb(), expr.toCompiledString());
		}
		
	}
	
	@Override
	public String toString() {
		return "AssignOp{variable=\"%s\",operator=%s,expr=%s}".format(
				(Object) variable, operator, expr);
	}
	
	@Override
	public String getNameString() {
		return variable;
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitAssignOp(this);
	}
	
}
