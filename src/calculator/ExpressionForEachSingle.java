package calculator;

import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionForEachSingle implements Expression {
	String variable;
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
		
		if (obj instanceof Number[]) {
			
			Number[] array = (Number[]) obj;
			
			for (Number n : array) {
				last = evalBody(scope, evalValueFunction, n);
			}
			
		} else if (obj instanceof Number[][]) {
			
			Number[][] matrix = (Number[][]) obj;
			
			for (Number[] array : matrix) {
				last = evalBody(scope, evalValueFunction, array);
			}
			
		} else
			throw new TypeError("not an array");
		
		return last;
	}
	
	private Object evalBody(final Scope parent,
			java.util.function.Function<Scope, Object> evalValueFunction,
			Object element) {
		Scope scope = new Scope(parent);
		scope.setVariableLocally(variable, element);
		
		Object result = evalValueFunction.apply(scope);
		
		if (result != null)
			parent.setVariableLocally("ans", result);
		
		return result;
	}
	
	@Override
	public String toEvalString() {
		return "for(%s : %s) ".format((Object) variable,
				array.toEvalString())
				+ (body instanceof ExpressionMulti
						? "{ " + body.toEvalString() + " }"
						: body.toEvalString());
	}
	
	@Override
	public String toCompiledString() {
		return "<FOREACH \"%s\" IN %s DO %s>".format((Object) variable,
				array.toCompiledString(), body.toCompiledString());
	}
	
	@Override
	public String toString() {
		return "ForEachSingle{variable=\"%s\",array=%s,body=%s}".format(
				(Object) variable, array, body);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitFor(this);
	}
	
}
