package calculator.expressions;

import static calculator.functions.Functions.*;

import calculator.Scope;
import calculator.Visitor;
import calculator.errors.DimensionError;
import calculator.errors.TypeError;
import calculator.values.EnumOperator;
import calculator.values.Number;
import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionZAssignOp implements ExpressionNamed {
	public final Expression array;
	public final EnumOperator operator;
	public final Expression value;
	
	@Override
	public Object eval(Scope scope) {
		Object obj = array.eval(scope);
		
		if (!(obj instanceof Number[]))
			throw new TypeError("not an array");
		
		Number[] array = (Number[]) obj;
		
		if (array.length != 3)
			throw new DimensionError();
		
		return array[2] =
				toNumber(operator.call(scope, array[2], value.eval(scope)));
	}
	
	@Override
	public String toEvalString() {
		return "%s %s= %s".format((Object) getNameString(), operator.getSymbol(),
				value.toEvalString());
	}
	
	@Override
	public String getNameString() {
		return array.toEvalString() + ".z";
	}
	
	@Override
	public String toCompiledString() {
		switch (operator) {
		case ADD:
		case SUBTRACT:
			return "<%s %s %s <.Z %s>>".format(operator, value.toCompiledString(),
					operator.getVerb(), array.toCompiledString());
		default:
			return "<%s <.Z %s> %s %s>".format(operator, array.toCompiledString(),
					operator.getVerb(), value.toCompiledString());
		}
	}
	
	@Override
	public String toString() {
		return "ZAssignOp{array=%s,operator=%s,value=%s}".format(array, operator,
				value);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitAssignOp(this);
	}
}
