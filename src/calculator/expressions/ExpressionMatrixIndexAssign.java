package calculator.expressions;

import calculator.Scope;
import calculator.TypeError;
import calculator.Visitor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
public class ExpressionMatrixIndexAssign extends ExpressionMatrixIndex
		implements ExpressionNamed {
	public final Expression value;
	
	public ExpressionMatrixIndexAssign(Expression matrix, Expression row,
			Expression column, Expression value) {
		super(matrix, row, column);
		this.value = value;
	}
	
	@Override
	public String toCompiledString() {
		return "<SET %s[%s, %s] TO %s>".format((Object) matrix.toCompiledString(),
				row.toCompiledString(), column.toCompiledString(),
				value.toCompiledString());
	}
	
	@Override
	public Object eval(Scope scope) {
		Object[][] matrix = evalReference(scope);
		
		Object obj = value.eval(scope);
		
		try {
			return matrix[lastRow - 1][lastColumn - 1] = obj;
		} catch (ArrayStoreException e) {
			throw new TypeError(e);
		}
	}
	
	@Override
	public String toEvalString() {
		return getNameString(false) + " = " + value.toEvalString();
	}
	
	@Override
	public String toString() {
		return "MatrixIndexAssign{matrix=%s,row=%s,column=%s,value=%s}".format(
				matrix, row, column, value);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitAssign(this);
	}
}
