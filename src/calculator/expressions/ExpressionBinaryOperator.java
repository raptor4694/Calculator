package calculator.expressions;

import static calculator.functions.Functions.*;

import calculator.Scope;
import calculator.Visitor;
import calculator.values.EnumOperator;
import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionBinaryOperator implements Expression {
	public final Expression lhs;
	public final EnumOperator operator;
	public final Expression rhs;
	
	@Override
	public String toCompiledString() {
		return "<%s %s %s>".format((Object) lhs.toCompiledString(), operator,
				rhs.toCompiledString());
	}
	
	@Override
	public Object eval(Scope scope) {
		switch (operator) {
		case OR: {
			Object obj = lhs.eval(scope);
			if (toBoolean(obj))
				return obj;
			else
				return rhs.eval(scope);
		}
		case AND: {
			Object obj = lhs.eval(scope);
			if (toBoolean(obj))
				return rhs.eval(scope);
			else
				return obj;
		}
		default:
			return operator.call(scope, lhs.eval(scope), rhs.eval(scope));
		}
		
	}
	
	@Override
	public String toEvalString() {
		return lhs.toEvalString() + " " + operator.getSymbol() + " "
				+ rhs.toEvalString();
	}
	
	@Override
	public String toString() {
		return "BinaryOperator{lhs=%s,operator=%s,rhs=%s}".format(lhs, operator,
				rhs);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitBinary(this);
	}
}
