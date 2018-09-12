package calculator.expressions;

import static calculator.functions.Functions.*;

import calculator.Scope;
import calculator.Visitor;
import calculator.errors.DimensionError;
import calculator.errors.TypeError;
import calculator.values.EnumOperator;
import calculator.values.Number;
import calculator.values.Real;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@RequiredArgsConstructor
public class ExpressionIndex implements ExpressionReferenceable {
	public final Expression array;
	public final Expression index;
	public int lastIndex;
	
	@Override
	public String toCompiledString() {
		return "<IDX %s[%s]>".format((Object) array.toCompiledString(),
				index.toCompiledString());
	}
	
	@Override
	public Object eval(Scope scope) {
		// Number[] array = evalReference(scope);
		Object obj;
		
		obj = array.eval(scope);
		
		if (obj instanceof String) {
			evalIndex(scope, ((String) obj).length());
			return ((String) obj).substring(lastIndex - 1, lastIndex);
		} else if (!(obj instanceof Object[]))
			throw new TypeError("not an array");
		
		Object[] array = (Object[]) obj;
		evalIndex(scope, array.length);
		
		return array[lastIndex - 1];
	}
	
	private final void evalIndex(Scope scope, int size) {
		// scope = new Scope(scope);
		Object $ = null;
		if (scope.hasLocalVariable("$")) {
			$ = scope.getVariable("$");
		}
		scope.setVariableLocally("$", Real.valueOf(size));
		try {
			Object obj = index.eval(scope);
			
			if (!(obj instanceof Real) || !isInt((Real) obj))
				throw new TypeError("index");
			
			lastIndex = ((Real) obj).intValue();
			
			if (lastIndex < 1 || lastIndex > size)
				throw new DimensionError();
		} finally {
			if ($ != null) {
				scope.setVariableLocally("$", $);
			}
		}
		
	}
	
	public final Object[] evalReference(Scope scope) {
		Object obj;
		
		obj = array.eval(scope);
		
		if (!(obj instanceof Object[]))
			throw new TypeError("not an array");
		
		Object[] array = (Object[]) obj;
		
		evalIndex(scope, array.length);
		
		return array;
	}
	
	protected final Number[] evalNumberReference(Scope scope) {
		Object[] result = evalReference(scope);
		
		check(result instanceof Number[], TypeError);
		
		return (Number[]) result;
	}
	
	@Override
	public ExpressionIndexAssign toAssign(Expression value) {
		return new ExpressionIndexAssign(array, index, value);
	}
	
	@Override
	public ExpressionIndexAssignOp toAssignOp(EnumOperator operator,
			Expression value) {
		return new ExpressionIndexAssignOp(array, index, operator, value);
	}
	
	@Override
	public ExpressionIndexPostfixDecrement toPostfixDecrement() {
		return new ExpressionIndexPostfixDecrement(array, index);
	}
	
	@Override
	public ExpressionIndexPostfixIncrement toPostfixIncrement() {
		return new ExpressionIndexPostfixIncrement(array, index);
	}
	
	@Override
	public ExpressionIndexPrefixDecrement toPrefixDecrement() {
		return new ExpressionIndexPrefixDecrement(array, index);
	}
	
	@Override
	public ExpressionIndexPrefixIncrement toPrefixIncrement() {
		return new ExpressionIndexPrefixIncrement(array, index);
	}
	
	public final String getNameString(boolean useLastIndex) {
		return array.toEvalString() + "["
				+ (useLastIndex? lastIndex : index.toEvalString()) + "]";
	}
	
	@Override
	public final String getNameString() {
		return getNameString(true);
	}
	
	@Override
	public String toEvalString() {
		return getNameString(false);
	}
	
	@Override
	public String toString() {
		return "Index{array=%s,index=%s}".format(array, index);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitIndex(this);
	}
}
