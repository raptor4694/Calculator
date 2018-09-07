package calculator.expressions;

import static calculator.functions.Functions.*;

import calculator.Scope;
import calculator.Visitor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@RequiredArgsConstructor
public class ExpressionIf implements Expression {
	public final Expression condition;
	public final Expression thenpart;
	public final Expression elsepart;
	
	@Override
	public Object eval(Scope scope) {
		if (toBoolean(condition.eval(scope))) {
			if (thenpart instanceof ExpressionMulti) {
				scope = new Scope(scope);
			}
			Object result = thenpart.eval(scope);
			
			if (thenpart instanceof ExpressionMulti)
				scope.setVariable("ans", result);
			
			return result;
		} else if (elsepart == null) {
			return scope.getVariable("ans");
		} else {
			if (elsepart instanceof ExpressionMulti) {
				scope = new Scope(scope);
			}
			Object result = elsepart.eval(scope);
			
			if (elsepart instanceof ExpressionMulti)
				scope.setVariable("ans", result);
			
			return result;
		}
	}
	
	@Override
	public Object evalOptionalValue(Scope scope) {
		if (toBoolean(condition.eval(scope))) {
			if (thenpart instanceof ExpressionMulti) {
				scope = new Scope(scope);
			}
			Object result = thenpart.evalOptionalValue(scope);
			
			if (result != null && thenpart instanceof ExpressionMulti)
				scope.setVariable("ans", result);
			
			return result;
		} else if (elsepart == null) {
			return scope.getVariable("ans");
		} else {
			if (elsepart instanceof ExpressionMulti) {
				scope = new Scope(scope);
			}
			
			Object result = elsepart.evalOptionalValue(scope);
			
			if (result != null && elsepart instanceof ExpressionMulti)
				scope.setVariable("ans", result);
			
			return result;
		}
	}
	
	@Override
	public String toCompiledString() {
		if (elsepart == null)
			return "<IF %s THEN %s>".format((Object) condition.toCompiledString(),
					thenpart.toCompiledString());
		else
			return "<IF %s THEN %s ELSE %s>".format(
					(Object) condition.toCompiledString(),
					thenpart.toCompiledString(), elsepart.toCompiledString());
	}
	
	@Override
	public String toEvalString() {
		String s = "if";
		if (!(condition instanceof ExpressionParenthesis))
			s += " ";
		s += condition.toEvalString() + " then ";
		if (thenpart instanceof ExpressionMulti) {
			s += "{ " + thenpart.toEvalString() + " }";
		} else
			s += thenpart.toEvalString();
		
		if (elsepart != null) {
			s += " else ";
			if (elsepart instanceof ExpressionMulti) {
				s += "{" + elsepart.toEvalString() + " }";
			} else
				s += elsepart.toEvalString();
		}
		
		return s;
	}
	
	@Override
	public String toString() {
		return "If{condition=%s,thenpart=%s,elsepart=%s}".format(condition, thenpart,
				elsepart);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitIf(this);
	}
}
