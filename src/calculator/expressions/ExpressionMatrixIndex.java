package calculator.expressions;

import static calculator.functions.Functions.*;

import calculator.DimensionError;
import calculator.Scope;
import calculator.TypeError;
import calculator.Visitor;
import calculator.values.EnumOperator;
import calculator.values.Number;
import calculator.values.Real;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@RequiredArgsConstructor
public class ExpressionMatrixIndex implements ExpressionReferenceable {
	public final Expression matrix;
	public final Expression row;
	public final Expression column;
	protected int lastRow, lastColumn;
	
	@Override
	public String toCompiledString() {
		return "<IDX %s[%s, %s]>".format((Object) matrix.toCompiledString(),
				row.toCompiledString(), column.toCompiledString());
	}
	
	@Override
	public Object eval(Scope scope) {
		Object obj = matrix.eval(scope);
		
		if (obj instanceof String) {
			int length = ((String) obj).length();
			evalIndex(scope, length, length);
			return ((String) obj).substring(lastRow - 1, lastColumn);
		} else if (!(obj instanceof Object[][]))
			throw new TypeError("not a matrix");
		
		Object[][] matrix = (Object[][]) obj;
		
		evalIndex(scope, rowCount(matrix), columnCount(matrix));
		
		return matrix[lastRow - 1][lastColumn - 1];
	}
	
	public final Object[][] evalReference(Scope scope) {
		Object[][] matrix;
		
		Object obj;
		
		obj = this.matrix.eval(scope);
		
		if (!(obj instanceof Object[][]))
			throw new TypeError("not a matrix");
		
		matrix = (Object[][]) obj;
		
		evalIndex(scope, rowCount(matrix), columnCount(matrix));
		
		return matrix;
	}
	
	protected final Number[][] evalNumberReference(Scope scope) {
		Object[][] result = evalReference(scope);
		
		check(result instanceof Number[][], TypeError.class);
		
		return (Number[][]) result;
	}
	
	private final void evalIndex(Scope scope, int rowCount, int columnCount) {
		// scope = new Scope(scope);
		Object $ = null;
		if (scope.hasLocalVariable("$")) {
			$ = scope.getVariable("$");
		}
		scope.setVariableLocally("$", Real.valueOf(rowCount));
		Object obj;
		try {
			obj = row.eval(scope);
			
			if (!(obj instanceof Real) || !isInt((Real) obj))
				throw new TypeError("index");
			
			lastRow = ((Real) obj).intValue();
		} finally {
			scope.setVariable("$", $);
		}
		scope.setVariableLocally("$", Real.valueOf(columnCount));
		try {
			obj = column.eval(scope);
			
			if (!(obj instanceof Number) || !isInt((Real) obj))
				throw new TypeError("index");
			
			lastColumn = ((Real) obj).intValue();
			
			if (lastRow < 1 || lastColumn < 1 || lastRow > rowCount
					|| lastColumn > columnCount)
				throw new DimensionError();
		} finally {
			scope.setVariable("$", $);
		}
	}
	
	@Override
	public ExpressionMatrixIndexAssign toAssign(Expression value) {
		return new ExpressionMatrixIndexAssign(matrix, row, column, value);
	}
	
	@Override
	public ExpressionMatrixIndexAssignOp toAssignOp(EnumOperator operator,
			Expression value) {
		return new ExpressionMatrixIndexAssignOp(matrix, row, column, operator,
				value);
	}
	
	@Override
	public ExpressionMatrixIndexPostfixDecrement toPostfixDecrement() {
		return new ExpressionMatrixIndexPostfixDecrement(matrix, row, column);
	}
	
	@Override
	public ExpressionMatrixIndexPostfixIncrement toPostfixIncrement() {
		return new ExpressionMatrixIndexPostfixIncrement(matrix, row, column);
	}
	
	@Override
	public ExpressionMatrixIndexPrefixDecrement toPrefixDecrement() {
		return new ExpressionMatrixIndexPrefixDecrement(matrix, row, column);
	}
	
	@Override
	public ExpressionMatrixIndexPrefixIncrement toPrefixIncrement() {
		return new ExpressionMatrixIndexPrefixIncrement(matrix, row, column);
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
		return "MatrixIndex{matrix=%s,row=%s,column=%s}".format(matrix, row, column);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitIndex(this);
	}
}
