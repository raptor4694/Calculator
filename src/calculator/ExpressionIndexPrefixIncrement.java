package calculator;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
public class ExpressionIndexPrefixIncrement extends ExpressionIndex
		implements ExpressionPrefixIncrement {
	
	public ExpressionIndexPrefixIncrement(Expression array, Expression index) {
		super(array, index);
	}
	
	@Override
	public String toCompiledString() {
		return "<INC %s[%s]>".format((Object) array.toCompiledString(),
				index.toCompiledString());
	}
	
	@Override
	public Object eval(Scope scope) {
		Number[] array = evalNumberReference(scope);
		int index = lastIndex - 1;
		
		return array[index] = array[index].plus(Real.ONE);
	}
	
	@Override
	public String toEvalString() {
		return "++" + getNameString(false);
	}
	
	@Override
	public String toString() {
		return "IndexPrefixIncrement{array=%s,index=%s}".format(array, index);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitPrefixIncrement(this);
	}
}
