package calculator.expressions;

import calculator.Scope;
import calculator.Visitor;
import calculator.values.Number;
import calculator.values.Real;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
public class ExpressionMatrixIndexPrefixDecrement extends ExpressionMatrixIndex
		implements ExpressionPrefixDecrement {
	
	public ExpressionMatrixIndexPrefixDecrement(Expression matrix, Expression row,
			Expression column) {
		super(matrix, row, column);
	}
	
	@Override
	public String toCompiledString() {
		return "<DEC %s[%s, %s]>".format((Object) matrix.toCompiledString(),
				row.toCompiledString(), column.toCompiledString());
	}
	
	@Override
	public Object eval(Scope scope) {
		Number[][] matrix = evalNumberReference(scope);
		int row = lastRow - 1, column = lastColumn - 1;
		
		return matrix[row][column] = matrix[row][column].minus(Real.ONE);
	}
	
	@Override
	public String toEvalString() {
		return "--" + getNameString(false);
	}
	
	@Override
	public String toString() {
		return "MatrixIndexPrefixDecrement{matrix=%s,row=%s,column=%s}".format(
				matrix, row, column);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitPrefixDecrement(this);
	}
	
}
