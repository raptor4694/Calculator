package calculator;

import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@RequiredArgsConstructor
public class ExpressionFor implements Expression {
	final String variable;
	final Expression start, end, increment, body;
	
	private double lastStart, lastEnd, lastIncrement;
	
	@Override
	public Object eval(Scope scope) {
		
		Object result = evalAll(scope, body::eval);
		
		return result == null? scope.getVariable("ans") : result;
	}
	
	@Override
	public Object evalOptionalValue(Scope scope) {
		return evalAll(scope, body::evalOptionalValue);
	}
	
	private void evalHeader(Scope scope) {
		Object obj = start.eval(scope);
		
		if (!(obj instanceof Real))
			throw new TypeError("start");
		
		lastStart = ((Real) obj).doubleValue();
		
		obj = end.eval(scope);
		
		if (!(obj instanceof Real))
			throw new TypeError("end");
		
		lastEnd = ((Real) obj).doubleValue();
		
		if (increment == null) {
			lastIncrement = lastStart > lastEnd? -1 : 1;
		} else {
			obj = increment.eval(scope);
			
			if (!(obj instanceof Real))
				throw new TypeError("increment");
			
			lastIncrement = ((Real) obj).doubleValue();
		}
	}
	
	private Object evalBody(Scope scope, double start, double end,
			double increment,
			java.util.function.Function<Scope, Object> evalValueFunction) {
		
		Scope parent = scope;
		
		Object last = null;
		
		try {
			for (double d = start; start < end? d <= end : d >= end;
					d += increment) {
				
				scope = new Scope(parent);
				scope.setVariableLocally(variable, d);
				
				last = evalValueFunction.apply(scope);// body.evalOptionalValue(scope);
				
				if (last != null)
					parent.setVariableLocally("ans", last);
			}
		} catch (ExpressionBreak b) {}
		
		return last;
	}
	
	private Object evalAll(Scope scope,
			java.util.function.Function<Scope, Object> evalValueFunction) {
		evalHeader(scope);
		return evalBody(new Scope(scope), lastStart, lastEnd,
				lastIncrement, evalValueFunction);
	}
	
	@Override
	public String toEvalString() {
		String s = "for(" + variable + ", " + start.toEvalString()
				+ ", " + end.toEvalString();
		if (increment != null) {
			s += ", " + increment.toEvalString();
		}
		return s + ") "
				+ (body instanceof ExpressionMulti
						? "{ " + body.toEvalString() + " }"
						: body.toEvalString());
	}
	
	@Override
	public String toCompiledString() {
		if (increment == null)
			return "<FOR \"%s\" FROM %s TO %s DO %s>".format(
					(Object) variable, start.toCompiledString(),
					end.toCompiledString(), body.toCompiledString());
		else
			return "<FOR \"%s\" FROM %s TO %s INCREMENT %s DO %s>".format(
					(Object) variable, start.toCompiledString(),
					end.toCompiledString(),
					increment.toCompiledString(),
					body.toCompiledString());
	}
	
	@Override
	public String toString() {
		return "For{variable=\"%s\",start=%s,end=%s,increment=%s,body=%s}".format(
				(Object) variable, start, end, increment, body);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitFor(this);
	}
	
}
