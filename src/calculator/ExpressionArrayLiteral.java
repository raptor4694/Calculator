package calculator;

import java.util.Arrays;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
public class ExpressionArrayLiteral implements Expression {
	Expression[] elems;
	
	public ExpressionArrayLiteral(Expression... elems) {
		this.elems = elems;
	}
	
	@Override
	public Object eval(Scope scope) {
		Class<?> type = null;
		
		Object[] objs = new Object[elems.length];
		
		int size = -1;
		
		for (int i = 0; i < elems.length; i++) {
			Object obj = elems[i].eval(scope);
			
			if (obj instanceof java.lang.Number) {
				obj = Real.valueOf(
						((java.lang.Number) obj).doubleValue());
			}
			
			if (obj instanceof Number[][])
				throw new CalculatorError("too deeply-nested array");
			if (type == null) {
				if (obj instanceof Number)
					type = Number.class;
				else if (obj instanceof Number[])
					type = Number[].class;
				else
					throw new TypeError(
							obj.getClass().getSimpleName());
			} else if (!type.isAssignableFrom(obj.getClass())) {
				throw new TypeError("element type: "
						+ obj.getClass().getSimpleName());
			}
			
			if (type == Number[].class) {
				if (size == -1)
					size = ((Number[]) obj).length;
				else if (size != ((Number[]) obj).length)
					throw new DimensionError();
			}
			
			objs[i] = obj;
		}
		
		if (type == Number.class) {
			
			Number[] result = new Number[objs.length];
			
			for (int i = 0; i < objs.length; i++)
				result[i] = (Number) objs[i];
			
			return result;
			
		} else if (type == Number[].class) {
			
			Number[][] result = new Number[objs.length][];
			
			for (int i = 0; i < objs.length; i++)
				result[i] = ((Number[]) objs[i]).clone();
			
			return result;
		} else
			throw new TypeError(type.getSimpleName());
		
	}
	
	@Override
	public String toEvalString() {
		StringBuilder b = new StringBuilder("{");
		for (int i = 0; i < elems.length; i++) {
			if (i != 0)
				b.append(", ");
			b.append(elems[i].toEvalString());
		}
		b.append('}');
		return b.toString();
	}
	
	@Override
	public String toCompiledString() {
		StringBuilder b = new StringBuilder("{");
		for (int i = 0; i < elems.length; i++) {
			if (i != 0)
				b.append(", ");
			b.append(elems[i].toCompiledString());
		}
		b.append('}');
		return b.toString();
	}
	
	@Override
	public String toString() {
		return "ArrayLiteral{elems=%s}".format(
				(Object) Arrays.toString(elems));
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitArrayLiteral(this);
	}
}
