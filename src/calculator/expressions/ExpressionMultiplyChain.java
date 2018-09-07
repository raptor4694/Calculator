package calculator.expressions;

import java.util.ArrayList;

import calculator.Scope;
import calculator.VarNotFoundError;
import calculator.Visitor;
import calculator.values.EnumOperator;
import calculator.values.Function;
import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionMultiplyChain implements Expression {
	public final ArrayList<Expression> exprs;
	
	public ExpressionMultiplyChain() {
		this(new ArrayList<>());
	}
	
	@Override
	public Object evalOptionalValue(Scope scope) {
		Object result = exprs.get(0).eval(scope);
		for (int i = 1; i < exprs.size(); i++) {
			if (result instanceof Function) {
				Function func = (Function) result;
				Object[] args =
						new Object[Math.min(exprs.size() - i, func.maxArgCount())];
				boolean first = i == 1;
				for (int j = 0; j < args.length; i++, j++) {
					Expression expr = exprs.get(i);
					if (expr instanceof ExpressionVariable) {
						try {
							args[j] = expr.eval(scope);
						} catch (VarNotFoundError e) {
							args[j] = ((ExpressionVariable) expr).variable;
						}
					} else {
						args[j] = expr.eval(scope);
					}
				}
				if (first) {
					result = func.callOptionalValue(scope, args);
				} else {
					result = func.call(scope, args);
				}
			} else {
				result = EnumOperator.MULTIPLY.call(scope, result,
						exprs.get(i).eval(scope));
			}
		}
		return result;
	}
	
	@Override
	public Object eval(Scope scope) {
		Object result = exprs.get(0).eval(scope);
		for (int i = 1; i < exprs.size(); i++) {
			if (result instanceof Function) {
				Function func = (Function) result;
				Object[] args =
						new Object[Math.min(exprs.size() - i, func.maxArgCount())];
				for (int j = 0; j < args.length; i++, j++) {
					Expression expr = exprs.get(i);
					if (expr instanceof ExpressionVariable) {
						try {
							args[j] = expr.eval(scope);
						} catch (VarNotFoundError e) {
							args[j] = ((ExpressionVariable) expr).variable;
						}
					} else {
						args[j] = expr.eval(scope);
					}
				}
				
				result = func.call(scope, args);
			} else {
				result = EnumOperator.MULTIPLY.call(scope, result,
						exprs.get(i).eval(scope));
			}
		}
		return result;
	}
	
	@Override
	public String toEvalString() {
		String s = exprs.get(0).toEvalString();
		for (int i = 1; i < exprs.size(); i++) {
			s += " " + exprs.get(i).toEvalString();
		}
		return s;
	}
	
	@Override
	public String toCompiledString() {
		String s = "<MULTIPLY_CHAIN";
		for (int i = 0; i < exprs.size(); i++) {
			if (i != 0)
				s += ",";
			s += " " + exprs.get(i).toCompiledString();
		}
		return s + ">";
	}
	
	@Override
	public String toString() {
		return "MultiplyChain{exprs=%s}".format(exprs);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitMultiplyChain(this);
	}
	
}
