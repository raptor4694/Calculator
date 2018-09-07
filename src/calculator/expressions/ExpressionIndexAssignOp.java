package calculator.expressions;

import static calculator.functions.Functions.*;

import calculator.Scope;
import calculator.TypeError;
import calculator.Visitor;
import calculator.values.EnumOperator;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
public class ExpressionIndexAssignOp extends ExpressionIndexAssign
		implements ExpressionNamed {
	public final EnumOperator operator;
	
	public ExpressionIndexAssignOp(Expression array, Expression index,
			@NonNull EnumOperator operator, Expression value) {
		super(array, index, value);
		this.operator = operator;
	}
	
	@Override
	public String toCompiledString() {
		switch (operator) {
		case ADD:
		case SUBTRACT:
			return "<%s %s %s %s[%s]>".format(operator, value.toCompiledString(),
					operator.getVerb(), array.toCompiledString(),
					index.toCompiledString());
		default:
			return "<%s %s[%s] %s %s>".format(operator, array.toCompiledString(),
					index.toCompiledString(), operator.getVerb(),
					value.toCompiledString());
		}
	}
	
	@Override
	public Object eval(Scope scope) {
		Object[] array = evalReference(scope);
		int index = lastIndex - 1;
		
		Object obj = value.eval(scope);
		
		try {
			return array[index] = evalValue(operator.call(scope, array[index], obj));
		} catch (ArrayStoreException e) {
			throw new TypeError(e);
		}
	}
	
	@Override
	public String toEvalString() {
		return getNameString(false) + " " + operator.getSymbol() + "= "
				+ value.toEvalString();
	}
	
	@Override
	public String toString() {
		return "IndexAssignOp{array=%s,index=%s,operator=%s,value=%s}".format(array,
				index, operator, value);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitAssignOp(this);
	}
	
}
