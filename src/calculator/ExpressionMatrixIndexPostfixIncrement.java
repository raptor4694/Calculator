package calculator;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
public class ExpressionMatrixIndexPostfixIncrement extends ExpressionMatrixIndex
		implements ExpressionPostfixIncrement {
	
	public ExpressionMatrixIndexPostfixIncrement(Expression matrix, Expression row,
			Expression column) {
		super(matrix, row, column);
	}
	
	@Override
	public String toCompiledString() {
		return "<%s[%s, %s] INC>".format((Object) matrix.toCompiledString(),
				row.toCompiledString(), column.toCompiledString());
	}
	
	@Override
	public Object eval(Scope scope) {
		Number[][] matrix = evalNumberReference(scope);
		int row = lastRow - 1, column = lastColumn - 1;
		Number last = matrix[row][column];
		matrix[row][column] = last.plus(Real.ONE);
		return last;
	}
	
	@Override
	public String toEvalString() {
		return getNameString(false) + "++";
	}
	
	@Override
	public String toString() {
		return "MatrixIndexPostfixIncrement{matrix=%s,row=%s,column=%s}".format(
				matrix, row, column);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitPostfixIncrement(this);
	}
}
