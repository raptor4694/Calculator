package calculator;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
public class ExpressionMatrixIndexPrefixIncrement extends ExpressionMatrixIndex
		implements ExpressionPrefixIncrement {
	
	public ExpressionMatrixIndexPrefixIncrement(Expression matrix, Expression row,
			Expression column) {
		super(matrix, row, column);
	}
	
	@Override
	public String toCompiledString() {
		return "<INC %s[%s, %s]>".format((Object) matrix.toCompiledString(),
				row.toCompiledString(), column.toCompiledString());
	}
	
	@Override
	public Object eval(Scope scope) {
		Number[][] matrix = evalNumberReference(scope);
		int row = lastRow - 1, column = lastColumn - 1;
		
		return matrix[row][column] = matrix[row][column].plus(Real.ONE);
	}
	
	@Override
	public String toEvalString() {
		return "++" + getNameString(false);
	}
	
	@Override
	public String toString() {
		return "MatrixIndexPrefixIncrement{matrix=%s,row=%s,column=%s}".format(
				matrix, row, column);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitPrefixIncrement(this);
	}
}
