package calculator.expressions;

import calculator.Scope;
import calculator.Scope.FileScope;
import calculator.Visitor;
import calculator.errors.TypeError;
import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionForEachSingle implements Expression {
	public final String variable;
	public final Expression array, body;
	
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
		
		scope = new FileScope(scope);
		Object last = null;
		
		if (obj instanceof Object[][]) {
			
			Object[][] matrix = (Object[][]) obj;
			
			for (Object[] array : matrix) {
				last = evalBody(scope, evalValueFunction, array);
			}
			
		} else if (obj instanceof Object[]) {
			
			Object[] array = (Object[]) obj;
			
			for (Object n : array) {
				last = evalBody(scope, evalValueFunction, n);
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
		return "for(%s : %s) ".format((Object) variable, array.toEvalString())
				+ (body instanceof ExpressionMulti? "{ " + body.toEvalString() + " }"
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
