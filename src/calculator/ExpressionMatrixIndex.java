package calculator;

import static calculator.Functions.*;

import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@RequiredArgsConstructor
public class ExpressionMatrixIndex implements ExpressionReferenceable {
	protected final Expression matrix, row, column;
	protected int lastRow, lastColumn;
	
	@Override
	public String toCompiledString() {
		return "<IDX %s[%s, %s]>".format(
				(Object) matrix.toCompiledString(),
				row.toCompiledString(), column.toCompiledString());
	}
	
	@Override
	public Object eval(Scope scope) {
		// Number[][] matrix = evalReference(scope);
		Object obj = matrix.eval(scope);
		
		if (obj instanceof String) {
			int length = ((String) obj).length();
			evalIndex(scope, length, length);
			return ((String) obj).substring(lastRow - 1,
					lastColumn - 1);
		} else if (!(obj instanceof Number[][]))
			throw new TypeError("not a matrix");
		
		Number[][] matrix = (Number[][]) obj;
		
		evalIndex(scope, rowCount(matrix), columnCount(matrix));
		
		return matrix[lastRow - 1][lastColumn - 1];
	}
	
	public final Number[][] evalReference(Scope scope) {
		Number[][] matrix;
		
		Object obj;
		
		obj = this.matrix.eval(scope);
		
		if (!(obj instanceof Number[][]))
			throw new TypeError("not a matrix");
		
		matrix = (Number[][]) obj;
		
		evalIndex(scope, rowCount(matrix), columnCount(matrix));
		
		return matrix;
	}
	
	private final void evalIndex(Scope scope, int rowCount,
			int columnCount) {
		scope = new Scope(scope);
		scope.setVariableLocally("$", Real.valueOf(rowCount));
		Object obj = row.eval(scope);
		
		if (!(obj instanceof Real) || !isInt((Real) obj))
			throw new TypeError("index");
		
		lastRow = ((Real) obj).intValue();
		
		scope.setVariableLocally("$", Real.valueOf(columnCount));
		obj = column.eval(scope);
		
		if (!(obj instanceof Number) || !isInt((Real) obj))
			throw new TypeError("index");
		
		lastColumn = ((Real) obj).intValue();
		
		if (lastRow < 1 || lastColumn < 1 || lastRow > rowCount
				|| lastColumn > columnCount)
			throw new DimensionError();
	}
	
	@Override
	public ExpressionMatrixIndexAssign toAssign(Expression value) {
		return new ExpressionMatrixIndexAssign(matrix, row, column,
				value);
	}
	
	@Override
	public ExpressionMatrixIndexAssignOp toAssignOp(
			EnumOperator operator, Expression value) {
		return new ExpressionMatrixIndexAssignOp(matrix, row, column,
				operator, value);
	}
	
	@Override
	public ExpressionMatrixIndexPostfixDecrement toPostfixDecrement() {
		return new ExpressionMatrixIndexPostfixDecrement(matrix, row,
				column);
	}
	
	@Override
	public ExpressionMatrixIndexPostfixIncrement toPostfixIncrement() {
		return new ExpressionMatrixIndexPostfixIncrement(matrix, row,
				column);
	}
	
	@Override
	public ExpressionMatrixIndexPrefixDecrement toPrefixDecrement() {
		return new ExpressionMatrixIndexPrefixDecrement(matrix, row,
				column);
	}
	
	@Override
	public ExpressionMatrixIndexPrefixIncrement toPrefixIncrement() {
		return new ExpressionMatrixIndexPrefixIncrement(matrix, row,
				column);
	}
	
	public final String getNameString(boolean useLastRowColumn) {
		String row, column;
		if (useLastRowColumn) {
			row = String.valueOf(lastRow);
			column = String.valueOf(lastColumn);
		} else {
			row = this.row.toEvalString();
			column = this.column.toEvalString();
		}
		return matrix.toEvalString() + "[" + row + ", " + column + "]";
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
		return "MatrixIndex{matrix=%s,row=%s,column=%s}".format(matrix,
				row, column);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitIndex(this);
	}
}
