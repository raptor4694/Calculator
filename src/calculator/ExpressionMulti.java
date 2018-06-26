package calculator;

import java.util.Arrays;

import lombok.NonNull;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
public class ExpressionMulti implements Expression {
	@NonNull
	Expression[] exprs;
	
	public ExpressionMulti(Expression... exprs) {
		this.exprs = exprs;
	}
	
	@Override
	public Object eval(Scope scope) {
		scope = new SemiLocalScope(scope);
		Object last = null;
		for (int i = 0; i < exprs.length; i++) {
			Expression expr = exprs[i];
			scope.setVariable("ans",
					last = i == exprs.length - 1? expr.eval(scope)
							: expr.evalOptionalValue(scope));
		}
		return last;
	}
	
	@Override
	public Object evalOptionalValue(Scope scope) {
		scope = new SemiLocalScope(scope);
		Object last = null;
		for (Expression expr : exprs)
			scope.setVariable("ans",
					last = expr.evalOptionalValue(scope));
		return last;
	}
	
	@Override
	public String toEvalString() {
		String s = "";
		for (int i = 0; i < exprs.length; i++) {
			if (i != 0)
				s += "; ";
			s += exprs[i].toEvalString();
		}
		return s;
	}
	
	@Override
	public String toCompiledString() {
		StringBuilder b = new StringBuilder("<MULTI ");
		for (Expression ex : exprs) {
			b.append(ex.toCompiledString()).append("; ");
		}
		b.append('>');
		return b.toString();
	}
	
	@Override
	public String toString() {
		return "Multi{exprs=%s}".format(
				(Object) Arrays.toString(exprs));
	}
	
	private static class SemiLocalScope extends Scope {
		public SemiLocalScope(Scope parent) {
			super(parent);
		}
		
		@Override
		public void setVariable(String varname, Object value) {
			if (value == null)
				return;
			if (variables.containsKey(varname)) {
				variables.put(varname, value);
			} else if ("ans".equals(varname)) {
				super.setVariable(varname, value);
			} else {
				parent.setVariable(varname, value);
			}
		}
		
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitMulti(this);
	}
	
}
