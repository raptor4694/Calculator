package calculator;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
public class ExpressionIndexPostfixDecrement extends ExpressionIndex
		implements ExpressionPostfixDecrement {
	
	public ExpressionIndexPostfixDecrement(Expression array, Expression index) {
		super(array, index);
	}
	
	@Override
	public String toCompiledString() {
		return "<%s[%s] DEC>".format((Object) array.toCompiledString(),
				index.toCompiledString());
	}
	
	@Override
	public Object eval(Scope scope) {
		Number[] array = evalNumberReference(scope);
		int index = lastIndex - 1;
		Number last = array[index];
		array[index] = array[index].minus(Real.ONE);
		return last;
	}
	
	@Override
	public String toEvalString() {
		return getNameString(false) + "--";
	}
	
	@Override
	public String toString() {
		return "IndexPostfixDecrement{array=%s,index=%s}".format(array, index);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitPostfixDecrement(this);
	}
}
