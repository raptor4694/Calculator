package calculator.expressions;

import calculator.Scope;
import calculator.Visitor;
import calculator.values.Number;
import calculator.values.Real;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
public class ExpressionIndexPostfixIncrement extends ExpressionIndex
		implements ExpressionPostfixIncrement {
	
	public ExpressionIndexPostfixIncrement(Expression array, Expression index) {
		super(array, index);
	}
	
	@Override
	public String toCompiledString() {
		return "<%s[%s] INC>".format((Object) array.toCompiledString(),
				index.toCompiledString());
	}
	
	@Override
	public Object eval(Scope scope) {
		Number[] array = evalNumberReference(scope);
		int index = lastIndex - 1;
		Number last = array[index];
		array[index] = array[index].plus(Real.ONE);
		return last;
	}
	
	@Override
	public String toEvalString() {
		return getNameString(false) + "++";
	}
	
	@Override
	public String toString() {
		return "IndexPostfixIncrement{array=%s,index=%s}".format(array, index);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitPostfixIncrement(this);
	}
	
}
