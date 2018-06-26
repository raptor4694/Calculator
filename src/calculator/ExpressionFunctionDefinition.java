package calculator;

import java.util.Arrays;
import java.util.function.BiConsumer;

import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionFunctionDefinition implements Expression {
	protected UserFunction function;
	
	protected BiConsumer<String, Object> getSetVariableFunction(
			Scope scope) {
		return scope::setVariable;
	}
	
	@Override
	public Object eval(Scope scope) {
		if (function.getName() != null)
			getSetVariableFunction(scope).accept(function.getName(),
					function);
		return function;
	}
	
	@Override
	public String toEvalString() {
		return function.toString();
	}
	
	@Override
	public String toCompiledString() {
		StringBuilder b = new StringBuilder();
		String s = Arrays.toString(function.varnames);
		b.append("<FUNC ");
		if (function.name == null) {
			b.append("LAMBDA");
		} else {
			b.append('"').append(function.name).append('"');
		}
		b.append(" (").append(s, 1, s.length() - 1).append(
				") ").append(function.body.toCompiledString()).append(
						'>');
		return b.toString();
	}
	
	@Override
	public String toString() {
		return "FunctionDefinition{function=UserFunction{name=\"%s\",varnames=%s,body=%s}}".format(
				(Object) function.getName(),
				Arrays.toString(function.varnames), function.body);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitFunctionDef(this);
	}
	
}
