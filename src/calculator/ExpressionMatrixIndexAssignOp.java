package calculator;

import static calculator.Functions.*;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
public class ExpressionMatrixIndexAssignOp extends
		ExpressionMatrixIndexAssign implements ExpressionNamed {
	final EnumOperator operator;
	
	public ExpressionMatrixIndexAssignOp(Expression matrix,
			Expression row, Expression column, EnumOperator operator,
			Expression value) {
		super(matrix, row, column, value);
		this.operator = operator;
	}
	
	@Override
	public String toCompiledString() {
		switch (operator) {
		case ADD:
		case SUBTRACT:
			return "<%s %s %s %s[%s, %s]>".format(operator,
					value.toCompiledString(), operator.getVerb(),
					matrix.toCompiledString(), row.toCompiledString(),
					column.toCompiledString());
		default:
			return "<%s %s[%s, %s] %s %s>".format(operator,
					matrix.toCompiledString(), row.toCompiledString(),
					column.toCompiledString(), operator.getVerb(),
					value.toCompiledString());
		}
	}
	
	@Override
	public Object eval(Scope scope) {
		Number[][] matrix = evalReference(scope);
		int row = lastRow - 1, column = lastColumn - 1;
		
		Object obj = value.eval(scope);
		
		return matrix[row][column] = toNumber(
				operator.call(scope, matrix[row][column], obj));
	}
	
	@Override
	public String toEvalString() {
		return getNameString(false) + " " + operator.getSymbol() + "= "
				+ value.toEvalString();
	}
	
	@Override
	public String toString() {
		return "MatrixIndexAssignOp{matrix=%s,row=%s,column=%s,operator=%s,value=%s}".format(
				matrix, row, column, operator, value);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitAssignOp(this);
	}
	
}
