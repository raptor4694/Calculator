package calculator;

import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionVariable implements ExpressionReferenceable {
	String variable;
	
	@Override
	public String toCompiledString() {
		return "<\"%s\">".format((Object) variable);
	}
	
	@Override
	public String getNameString() {
		return variable;
	}
	
	@Override
	public String toEvalString() {
		return variable;
	}
	
	@Override
	public String toString() {
		return "Variable{variable=\"%s\"}".format((Object) variable);
	}
	
	@Override
	public Object eval(Scope scope) {
		return scope.getVariable(variable);
	}
	
	@Override
	public ExpressionAssign toAssign(Expression value) {
		return new ExpressionAssign(variable, value);
	}
	
	@Override
	public ExpressionAssignOp toAssignOp(EnumOperator operator,
			Expression value) {
		return new ExpressionAssignOp(variable, operator, value);
	}
	
	@Override
	public ExpressionVarPrefixIncrement toPrefixIncrement() {
		return new ExpressionVarPrefixIncrement(variable);
	}
	
	@Override
	public ExpressionVarPrefixDecrement toPrefixDecrement() {
		return new ExpressionVarPrefixDecrement(variable);
	}
	
	@Override
	public ExpressionVarPostfixIncrement toPostfixIncrement() {
		return new ExpressionVarPostfixIncrement(variable);
	}
	
	@Override
	public ExpressionVarPostfixDecrement toPostfixDecrement() {
		return new ExpressionVarPostfixDecrement(variable);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitVariable(this);
	}
	
}
