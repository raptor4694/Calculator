package calculator.expressions;

import static calculator.functions.Functions.*;

import calculator.Scope;
import calculator.Scope.FileScope;
import calculator.Visitor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@RequiredArgsConstructor
public class ExpressionWhile implements Expression {
	public final Expression condition;
	public final Expression body;
	
	@Override
	public Object eval(Scope scope) {
		Object result = evalBody(scope, body::eval);
		return result == null? scope.getVariable("ans") : result;
	}
	
	@Override
	public Object evalOptionalValue(Scope scope) {
		return evalBody(scope, body::evalOptionalValue);
	}
	
	private Object evalBody(Scope parent,
			java.util.function.Function<Scope, Object> evalValueFunction) {
		parent = new FileScope(parent);
		Object last = null;
		try {
			while (toBoolean(condition.eval(parent))) {
				Scope scope = new Scope(parent);
				last = evalValueFunction.apply(scope);
				if (last != null)
					parent.setVariableLocally("ans", last);
			}
		} catch (ExpressionBreak b) {}
		return last;
	}
	
	@Override
	public String toCompiledString() {
		return "<WHILE %s DO %s>".format((Object) condition.toCompiledString(),
				body.toCompiledString());
	}
	
	@Override
	public String toString() {
		return "While{condition=%s,body=%s}".format(condition, body);
	}
	
	@Override
	public String toEvalString() {
		String s = "while";
		if (condition instanceof ExpressionParenthesis)
			s += condition.toEvalString();
		else
			s += "(" + condition.toEvalString() + ")";
		s += " ";
		if (body instanceof ExpressionMulti) {
			s += "{ " + body.toEvalString() + " }";
		} else
			s += body.toEvalString();
		
		return s;
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitWhile(this);
	}
	
}
