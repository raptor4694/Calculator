package calculator.expressions;

import calculator.CalculatorError;
import calculator.Console;
import calculator.Scope;
import calculator.Visitor;
import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionTry implements Expression {
	public final Expression body, elsepart;
	
	@Override
	public Object eval(Scope scope) {
		try {
			return body.eval(
					body instanceof ExpressionMulti? new Scope(scope) : scope);
		} catch (CalculatorError | IndexOutOfBoundsException e) {
			Console.lastError = e;
			return elsepart.eval(
					elsepart instanceof ExpressionMulti? new Scope(scope) : scope);
		}
	}
	
	@Override
	public Object evalOptionalValue(Scope scope) {
		try {
			return body.evalOptionalValue(
					body instanceof ExpressionMulti? new Scope(scope) : scope);
		} catch (CalculatorError | IndexOutOfBoundsException e) {
			Console.lastError = e;
			return elsepart.evalOptionalValue(
					elsepart instanceof ExpressionMulti? new Scope(scope) : scope);
		}
	}
	
	@Override
	public String toEvalString() {
		return "try "
				+ (body instanceof ExpressionMulti
						? "{ " + body.toEvalString() + " } " : body.toEvalString())
				+ " else "
				+ (elsepart instanceof ExpressionMulti
						? "{ " + elsepart.toEvalString() + " }"
						: elsepart.toEvalString());
	}
	
	@Override
	public String toCompiledString() {
		return "<TRY %s ELSE %s>".format((Object) body.toCompiledString(),
				elsepart.toCompiledString());
	}
	
	@Override
	public String toString() {
		return "Try{body=%s,elsepart=%s}".format(body, elsepart);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitTry(this);
	}
}
