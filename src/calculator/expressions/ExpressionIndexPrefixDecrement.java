package calculator.expressions;

import calculator.Scope;
import calculator.Visitor;
import calculator.values.Number;
import calculator.values.Real;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
public class ExpressionIndexPrefixDecrement extends ExpressionIndex
		implements ExpressionPrefixDecrement {
	
	public ExpressionIndexPrefixDecrement(Expression array, Expression index) {
		super(array, index);
	}
	
	@Override
	public String toCompiledString() {
		return "<DEC %s[%s]>".format((Object) array.toCompiledString(),
				index.toCompiledString());
	}
	
	@Override
	public Object eval(Scope scope) {
		Number[] array = evalNumberReference(scope);
		int index = lastIndex - 1;
		
		return array[index] = array[index].minus(Real.ONE);
	}
	
	@Override
	public String toEvalString() {
		return "--" + getNameString(false);
	}
	
	@Override
	public String toString() {
		return "IndexPrefixDecrement{array=%s,index=%s}".format(array, index);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitPrefixDecrement(this);
	}
	
}
