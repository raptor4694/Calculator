package calculator.expressions;

import java.util.ArrayList;
import java.util.Arrays;

import calculator.DimensionError;
import calculator.Scope;
import calculator.TypeError;
import calculator.Visitor;
import calculator.values.Number;
import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionColumnLiteral implements Expression {
	public final Expression[] exprs;
	
	@Override
	public String toCompiledString() {
		String str = "{";
		
		for (int i = 0; i < exprs.length; i++) {
			if (i != 0)
				str += " | ";
			str += exprs[i].toCompiledString();
		}
		return str + "}";
	}
	
	@Override
	public Object eval(Scope scope) {
		ArrayList<Number[]> list = new ArrayList<>();
		
		int size = -1;
		
		for (Expression expr : exprs) {
			Object obj = expr.eval(scope);
			
			if (!(obj instanceof Number[]))
				throw new TypeError();
			
			Number[] arr = (Number[]) obj;
			
			if (size == -1)
				size = arr.length;
			else if (size != arr.length)
				throw new DimensionError();
			
			list.add(arr);
		}
		
		Number[][] result = new Number[size][list.size()];
		
		for (int r = 0; r < size; r++) {
			for (int c = 0; c < list.size(); c++) {
				result[r][c] = list.get(c)[r];
			}
		}
		
		return result;
	}
	
	@Override
	public String toEvalString() {
		String s = "{";
		for (int i = 0; i < exprs.length; i++) {
			if (i != 0)
				s += " | ";
			s += exprs[i].toEvalString();
		}
		return s + "}";
	}
	
	@Override
	public String toString() {
		return "ColumnLiteral{exprs=%s}".format((Object) Arrays.toString(exprs));
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitArrayLiteral(this);
	}
	
}
