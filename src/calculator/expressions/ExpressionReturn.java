package calculator.expressions;

import calculator.Scope;
import calculator.Visitor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@RequiredArgsConstructor
public class ExpressionReturn extends RuntimeException implements Expression {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2939418053659637738L;
	
	public final Expression value;
	
	public Object lastValue;
	
	@Override
	public Object eval(Scope scope) {
		lastValue = value == null? null : value.eval(scope);
		throw this;
	}
	
	@Override
	public String toEvalString() {
		String s = "return";
		if (value != null)
			s += " " + value.toEvalString();
		return s;
	}
	
	@Override
	public String toCompiledString() {
		return value == null? "<RET>"
				: "<RET %s>".format((Object) value.toCompiledString());
	}
	
	@Override
	public String toString() {
		return "Return{value=%s}".format(value);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitReturn(this);
	}
}
