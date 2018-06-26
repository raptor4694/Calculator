package calculator;

import static calculator.Functions.*;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionDimAssign implements ExpressionNamed {
	protected String variable;
	protected Expression value;
	
	protected Predicate<String> getHasVariableFunction(Scope scope) {
		return scope::hasVariable;
	}
	
	protected java.util.function.Function<String, Object> getGetVariableFunction(
			Scope scope) {
		return scope::getVariable;
	}
	
	protected BiConsumer<String, Object> getSetVariableFunction(
			Scope scope) {
		return scope::setVariable;
	}
	
	@Override
	public Object eval(Scope scope) {
		Predicate<String> hasVariable = getHasVariableFunction(scope);
		java.util.function.Function<String, Object> getVariable =
				getGetVariableFunction(scope);
		BiConsumer<String, Object> setVariable =
				getSetVariableFunction(scope);
		
		if (hasVariable.test(variable)) {
			Object arrayObj = getVariable.apply(variable);
			
			if (arrayObj instanceof Number) {
				Number num = (Number) arrayObj;
				
				final Object obj = value.eval(scope);
				
				if (obj instanceof Real) {
					check(isInt((Real) obj), TypeError.class);
					
					int i = ((Real) obj).intValue();
					
					check(i >= 0, DimensionError.class);
					
					Number[] result = new Number[i];
					
					Arrays.fill(result, Real.ZERO);
					
					result[0] = num;
					
					setVariable.accept(variable, result);
					
				} else if (obj instanceof Number[]) {
					Number[] sizeArr = (Number[]) obj;
					check(sizeArr.length == 2, DimensionError.class);
					check(sizeArr[0] instanceof Real, TypeError.class);
					check(sizeArr[1] instanceof Real, TypeError.class);
					check(isInt(sizeArr[0]), TypeError.class);
					check(isInt(sizeArr[1]), TypeError.class);
					
					int rows = ((Real) sizeArr[0]).intValue();
					int columns = ((Real) sizeArr[1]).intValue();
					
					check(rows > 0, DimensionError.class);
					check(columns > 0, DimensionError.class);
					
					Number[][] result = new Number[rows][columns];
					
					for (int r = 0; r < rows; r++)
						Arrays.fill(result[r], Real.ZERO);
					
					result[0][0] = num;
					
					setVariable.accept(variable, result);
				} else
					throw new TypeError();
				
				return obj;
			} else if (arrayObj instanceof Number[]) {
				Number[] array = (Number[]) arrayObj;
				
				final Object obj = value.eval(scope);
				
				check(obj instanceof Real, TypeError.class);
				check(isInt((Real) obj), TypeError.class);
				
				int i = ((Real) obj).intValue();
				
				check(i >= 0, DimensionError.class);
				
				Number[] result = new Number[i];
				
				System.arraycopy(array, 0, result, 0,
						Math.min(i, array.length));
				
				if (i > array.length) {
					Arrays.fill(result, array.length, i, Real.ZERO);
				}
				
				setVariable.accept(variable, result);
				
				return obj;
			} else if (arrayObj instanceof Number[][]) {
				Number[][] matrix = (Number[][]) arrayObj;
				
				final Object obj = value.eval(scope);
				
				check(obj instanceof Number[], TypeError.class);
				
				Number[] sizeArr = (Number[]) obj;
				check(sizeArr.length == 2, DimensionError.class);
				check(sizeArr[0] instanceof Real, TypeError.class);
				check(sizeArr[1] instanceof Real, TypeError.class);
				check(isInt(sizeArr[0]), TypeError.class);
				check(isInt(sizeArr[1]), TypeError.class);
				
				int rows = ((Real) sizeArr[0]).intValue();
				int columns = ((Real) sizeArr[1]).intValue();
				
				check(rows > 0, DimensionError.class);
				check(columns > 0, DimensionError.class);
				
				int columnCount = columnCount(matrix);
				
				if (rows < rowCount(matrix)) {
					Number[][] result = new Number[rows][];
					System.arraycopy(matrix, 0, result, 0, rows);
					matrix = result;
				} else if (rows > rowCount(matrix)) {
					Number[][] result = new Number[rows][];
					System.arraycopy(matrix, 0, result, 0,
							matrix.length);
					for (int i = 0; i < rows; i++) {
						Arrays.fill(matrix[i] = new Number[columns],
								Real.ZERO);
					}
					matrix = result;
				}
				
				if (columns > columnCount) {
					for (int r = 0; r < rows; r++) {
						if (matrix[r].length != columns) {
							Number[] n = new Number[columns];
							int length = matrix[r].length;
							System.arraycopy(matrix[r], 0, n, 0,
									length);
							Arrays.fill(n, length, columns, Real.ZERO);
							matrix[r] = n;
						}
					}
				} else if (columns < columnCount(matrix)) {
					for (int r = 0; r < rows; r++) {
						if (matrix[r].length != columns) {
							Number[] n = new Number[columns];
							System.arraycopy(matrix[r], 0, n, 0,
									columns);
							matrix[r] = n;
						}
					}
				}
				
				setVariable.accept(variable, matrix);
				
				return obj;
			} else
				throw new TypeError(
						arrayObj.getClass().getSimpleName().toLowerCase());
		} else {
			Object obj = value.eval(scope);
			if (obj instanceof Real) {
				check(isInt((Real) obj), TypeError.class);
				int size = ((Real) obj).intValue();
				check(size >= 0, DimensionError.class);
				
				Number[] n = new Number[size];
				
				Arrays.fill(n, Real.ZERO);
				
				setVariable.accept(variable, n);
				
				return obj;
			} else if (obj instanceof Number[]) {
				Number[] sizeArr = (Number[]) obj;
				check(sizeArr.length == 2, DimensionError.class);
				check(sizeArr[0] instanceof Real, TypeError.class);
				check(sizeArr[1] instanceof Real, TypeError.class);
				check(isInt(sizeArr[0]), TypeError.class);
				check(isInt(sizeArr[1]), TypeError.class);
				
				int rows = ((Real) sizeArr[0]).intValue();
				int columns = ((Real) sizeArr[1]).intValue();
				
				check(rows > 0, DimensionError.class);
				check(columns > 0, DimensionError.class);
				
				Number[][] m = new Number[rows][columns];
				
				for (int r = 0; r < rows; r++)
					Arrays.fill(m[r], Real.ZERO);
				
				setVariable.accept(variable, m);
				
				return obj;
			} else
				throw new TypeError();
		}
	}
	
	@Override
	public String toEvalString() {
		return getNameString() + " = " + value.toEvalString();
	}
	
	@Override
	public String getNameString() {
		return "dim(" + variable + ")";
	}
	
	@Override
	public String toCompiledString() {
		return "<SET DIM \"%s\" TO %s>".format((Object) variable,
				value.toCompiledString());
	}
	
	@Override
	public String toString() {
		return "DimAssign{variable=\"%s\",value=%s}".format(
				(Object) variable, value);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitDimAssign(this);
	}
	
}
