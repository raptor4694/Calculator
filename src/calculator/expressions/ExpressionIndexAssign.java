package calculator.expressions;

import calculator.Scope;
import calculator.Visitor;
import calculator.errors.TypeError;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
public class ExpressionIndexAssign extends ExpressionIndex
		implements ExpressionNamed {
	public final Expression value;
	
	public ExpressionIndexAssign(Expression array, Expression index,
			Expression value) {
		super(array, index);
		this.value = value;
	}
	
	@Override
	public String toCompiledString() {
		return "<SET %s[%s] TO %s>".format((Object) array.toCompiledString(),
				index.toCompiledString(), value.toCompiledString());
	}
	
	@Override
	public Object eval(Scope scope) {
		Object[] array = evalReference(scope);
		
		Object obj = value.eval(scope);
		
		try {
			return array[lastIndex - 1] = obj;
		} catch (ArrayStoreException e) {
			throw new TypeError(e);
		}
	}
	
	@Override
	public String toEvalString() {
		return getNameString(false) + " = " + value.toEvalString();
	}
	
	@Override
	public String toString() {
		return "IndexAssign{array=%s,index=%s,value=%s}".format(array, index, value);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitAssign(this);
	}
	
}
