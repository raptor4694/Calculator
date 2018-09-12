package calculator.expressions;

import calculator.Scope;
import calculator.Visitor;
import calculator.errors.DimensionError;
import calculator.errors.TypeError;
import calculator.values.Number;
import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionZAssign implements ExpressionNamed {
	public final Expression array, value;
	
	@Override
	public Object eval(Scope scope) {
		Object obj = array.eval(scope);
		
		if (!(obj instanceof Number[]))
			throw new TypeError("not an array");
		
		Number[] array = (Number[]) obj;
		
		if (array.length != 3)
			throw new DimensionError();
		
		obj = value.eval(scope);
		
		if (!(obj instanceof Number))
			throw new TypeError();
		
		return array[2] = (Number) obj;
	}
	
	@Override
	public String toEvalString() {
		return getNameString() + " = " + value.toEvalString();
	}
	
	@Override
	public String getNameString() {
		return array.toEvalString() + ".z";
	}
	
	@Override
	public String toCompiledString() {
		return "<SET <.Z %s> TO %s>".format((Object) array.toCompiledString(),
				value.toCompiledString());
	}
	
	@Override
	public String toString() {
		return "ZAssign{array=%s,value=%s}".format(array, value);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitAssign(this);
	}
	
}
