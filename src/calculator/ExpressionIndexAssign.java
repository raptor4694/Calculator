package calculator;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
public class ExpressionIndexAssign extends ExpressionIndex
		implements ExpressionNamed {
	protected final Expression value;
	
	public ExpressionIndexAssign(Expression array, Expression index,
			Expression value) {
		super(array, index);
		this.value = value;
	}
	
	@Override
	public String toCompiledString() {
		return "<SET %s[%s] TO %s>".format(
				(Object) array.toCompiledString(),
				index.toCompiledString(), value.toCompiledString());
	}
	
	@Override
	public Object eval(Scope scope) {
		Number[] array = evalReference(scope);
		
		Object obj = value.eval(scope);
		
		if (!(obj instanceof Number))
			throw new TypeError();
		
		Number value = (Number) obj;
		
		return array[lastIndex - 1] = value;
	}
	
	@Override
	public String toEvalString() {
		return getNameString(false) + " = " + value.toEvalString();
	}
	
	@Override
	public String toString() {
		return "IndexAssign{array=%s,index=%s,value=%s}".format(array,
				index, value);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitAssign(this);
	}
	
}
