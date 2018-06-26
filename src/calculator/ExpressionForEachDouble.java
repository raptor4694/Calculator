package calculator;

import static calculator.Functions.*;

import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionForEachDouble implements Expression {
	String variable1, variable2;
	Expression array, body;
	
	@Override
	public Object eval(Scope scope) {
		Object result = evalAll(scope, body::eval);
		
		return result == null? scope.getVariable("ans") : result;
	}
	
	@Override
	public Object evalOptionalValue(Scope scope) {
		return evalAll(scope, body::evalOptionalValue);
	}
	
	private Object evalAll(Scope scope,
			java.util.function.Function<Scope, Object> evalValueFunction) {
		Object obj = array.eval(scope);
		
		scope = new Scope(scope);
		
		Object last = null;
		
		if (obj instanceof Object[][]) {
			Object[][] matrix = (Object[][]) obj;
			
			for (int r = 0; r < rowCount(matrix); r++) {
				for (int c = 0; c < columnCount(matrix); c++) {
					last = evalBody(scope, evalValueFunction, Real.valueOf(r + 1),
							Real.valueOf(c + 1));
				}
			}
		} else if (obj instanceof Object[]) {
			Object[] array = (Object[]) obj;
			
			for (int i = 0; i < array.length; i++) {
				last = evalBody(scope, evalValueFunction, Real.valueOf(i + 1),
						array[i]);
			}
		} else
			throw new TypeError("not a matrix");
		
		return last;
	}
	
	private Object evalBody(final Scope parent,
			java.util.function.Function<Scope, Object> evalValueFunction,
			Object element1, Object element2) {
		Scope scope = new Scope(parent);
		scope.setVariableLocally(variable1, element1);
		scope.setVariableLocally(variable2, element2);
		
		Object result = evalValueFunction.apply(scope);
		
		if (result != null)
			parent.setVariableLocally("ans", result);
		
		return result;
	}
	
	@Override
	public String toEvalString() {
		return "for(%s, %s : %s) ".format((Object) variable1, variable2,
				array.toEvalString())
				+ (body instanceof ExpressionMulti? "{ " + body.toEvalString() + " }"
						: body.toEvalString());
	}
	
	@Override
	public String toCompiledString() {
		return "<FOREACH2 \"%s\", \"%s\" IN %s DO %s>".format((Object) variable1,
				variable2, array.toCompiledString(), body.toCompiledString());
	}
	
	@Override
	public String toString() {
		return "ForEachDouble{variable1=\"%s\",variable2=\"%s\",array=%s,body=%s}".format(
				(Object) variable1, variable2, array, body);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitFor(this);
	}
	
}
