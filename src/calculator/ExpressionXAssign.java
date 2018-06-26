package calculator;

import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionXAssign implements ExpressionNamed {
	Expression array, value;
	
	@Override
	public Object eval(Scope scope) {
		Object obj = array.eval(scope);
		
		if (!(obj instanceof Number[]))
			throw new TypeError("not an array");
		
		Number[] array = (Number[]) obj;
		
		if (array.length != 2 && array.length != 3)
			throw new DimensionError();
		
		obj = value.eval(scope);
		
		if (!(obj instanceof Number))
			throw new TypeError();
		
		return array[0] = (Number) obj;
	}
	
	@Override
	public String toEvalString() {
		return getNameString() + " = " + value.toEvalString();
	}
	
	@Override
	public String getNameString() {
		return array.toEvalString() + ".x";
	}
	
	@Override
	public String toCompiledString() {
		return "<SET <.X %s> TO %s>".format(
				(Object) array.toCompiledString(),
				value.toCompiledString());
	}
	
	@Override
	public String toString() {
		return "XAssign{array=%s,value=%s}".format(array, value);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitAssign(this);
	}
	
}
