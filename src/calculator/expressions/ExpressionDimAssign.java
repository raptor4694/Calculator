package calculator.expressions;

import static calculator.functions.Functions.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import calculator.Scope;
import calculator.Visitor;
import calculator.errors.DimensionError;
import calculator.errors.TypeError;
import calculator.values.Number;
import calculator.values.Real;
import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionDimAssign implements ExpressionNamed {
	public final String variable;
	public final Expression value;
	
	protected Predicate<String> getHasVariableFunction(Scope scope) {
		return scope::hasVariable;
	}
	
	protected java.util.function.Function<String, Object> getGetVariableFunction(
			Scope scope) {
		return scope::getVariable;
	}
	
	protected BiConsumer<String, Object> getSetVariableFunction(Scope scope) {
		return scope::setVariable;
	}
	
	@Override
	public Object eval(Scope scope) {
		Predicate<String> hasVariable = getHasVariableFunction(scope);
		java.util.function.Function<String, Object> getVariable =
				getGetVariableFunction(scope);
		BiConsumer<String, Object> setVariable = getSetVariableFunction(scope);
		
		if (hasVariable.test(variable)) {
			Object arrayObj = getVariable.apply(variable);
			
			if (arrayObj instanceof Object[][]) {
				Object[][] matrix = (Object[][]) arrayObj;
				
				final Object obj = value.eval(scope);
				
				check(obj instanceof Number[], TypeError);
				
				Number[] sizeArr = (Number[]) obj;
				check(sizeArr.length == 2, DimensionError);
				check(sizeArr[0] instanceof Real, TypeError);
				check(sizeArr[1] instanceof Real, TypeError);
				check(isInt(sizeArr[0]), TypeError);
				check(isInt(sizeArr[1]), TypeError);
				
				int rows = ((Real) sizeArr[0]).intValue();
				int columns = ((Real) sizeArr[1]).intValue();
				
				check(rows > 0, DimensionError);
				check(columns > 0, DimensionError);
				
				int columnCount = columnCount(matrix);
				
				if (rows < rowCount(matrix)) {
					Object[][] result = (Object[][]) Array.newInstance(
							arrayObj.getClass().getComponentType(), rows);
					System.arraycopy(matrix, 0, result, 0, rows);
					matrix = result;
				} else if (rows > rowCount(matrix)) {
					Object[][] result = (Object[][]) Array.newInstance(
							arrayObj.getClass().getComponentType(), rows);
					
					System.arraycopy(matrix, 0, result, 0, matrix.length);
					Object initialValue;
					if (matrix instanceof String[][])
						initialValue = "";
					else if (matrix instanceof Number[][])
						initialValue = Real.ZERO;
					else
						throw new TypeError();
					for (int i = 0; i < rows; i++) {
						Arrays.fill(matrix[i] = (Object[]) Array.newInstance(
								arrayObj.getClass().getComponentType().getComponentType(),
								columns), initialValue);
					}
					matrix = result;
				}
				
				if (columns > columnCount) {
					for (int r = 0; r < rows; r++) {
						if (matrix[r].length != columns) {
							Object[] n = (Object[]) Array.newInstance(
									matrix[r].getClass().getComponentType(),
									columns);
							int length = matrix[r].length;
							System.arraycopy(matrix[r], 0, n, 0, length);
							if (n instanceof Number[])
								Arrays.fill(n, length, columns, Real.ZERO);
							else if (n instanceof String[])
								Arrays.fill(n, length, columns, "");
							else
								throw new TypeError();
							matrix[r] = n;
						}
					}
				} else if (columns < columnCount(matrix)) {
					for (int r = 0; r < rows; r++) {
						if (matrix[r].length != columns) {
							Object[] n = (Object[]) Array.newInstance(
									matrix[r].getClass().getComponentType(),
									columns);
							System.arraycopy(matrix[r], 0, n, 0, columns);
							matrix[r] = n;
						}
					}
				}
				
				setVariable.accept(variable, matrix);
				
				return obj;
			} else if (arrayObj instanceof Object[]) {
				Object[] array = (Object[]) arrayObj;
				
				final Object obj = value.eval(scope);
				
				check(obj instanceof Real, TypeError);
				check(isInt((Real) obj), TypeError);
				
				int i = ((Real) obj).intValue();
				
				check(i >= 0, DimensionError);
				
				Object[] result = (Object[]) Array.newInstance(
						array.getClass().getComponentType(), i);
				
				System.arraycopy(array, 0, result, 0, Math.min(i, array.length));
				
				if (i > array.length) {
					if (array instanceof String[])
						Arrays.fill(result, array.length, i, "");
					else if (array instanceof Number[])
						Arrays.fill(result, array.length, i, Real.ZERO);
					else
						throw new TypeError();
				}
				
				setVariable.accept(variable, result);
				
				return obj;
			} else {
				
				final Object obj = value.eval(scope);
				
				if (obj instanceof Real) {
					check(isInt((Real) obj), TypeError);
					
					int i = ((Real) obj).intValue();
					
					check(i >= 0, DimensionError);
					
					if (arrayObj instanceof Number) {
						Number[] result = new Number[i];
						
						Arrays.fill(result, Real.ZERO);
						
						result[0] = (Number) arrayObj;
						
						setVariable.accept(variable, result);
					} else if (arrayObj instanceof String) {
						String[] result = new String[i];
						
						Arrays.fill(result, "");
						
						result[0] = (String) arrayObj;
						
						setVariable.accept(variable, result);
					} else
						throw new TypeError();
				} else if (obj instanceof Number[]) {
					Number[] sizeArr = (Number[]) obj;
					check(sizeArr.length == 2, DimensionError);
					check(sizeArr[0] instanceof Real, TypeError);
					check(sizeArr[1] instanceof Real, TypeError);
					check(isInt(sizeArr[0]), TypeError);
					check(isInt(sizeArr[1]), TypeError);
					
					int rows = ((Real) sizeArr[0]).intValue();
					int columns = ((Real) sizeArr[1]).intValue();
					
					check(rows > 0, DimensionError);
					check(columns > 0, DimensionError);
					
					if (arrayObj instanceof Number) {
						Number[][] result = new Number[rows][columns];
						
						for (int r = 0; r < rows; r++)
							Arrays.fill(result[r], Real.ZERO);
						
						result[0][0] = (Number) arrayObj;
						
						setVariable.accept(variable, result);
					} else if (arrayObj instanceof String) {
						String[][] result = new String[rows][columns];
						
						for (int r = 0; r < rows; r++)
							Arrays.fill(result[r], "");
						
						result[0][0] = (String) arrayObj;
						
						setVariable.accept(variable, result);
					} else
						throw new TypeError();
				} else
					throw new TypeError();
				
				return obj;
			}
		} else {
			Object obj = value.eval(scope);
			if (obj instanceof Real) {
				check(isInt((Real) obj), TypeError);
				int size = ((Real) obj).intValue();
				check(size >= 0, DimensionError);
				
				Number[] n = new Number[size];
				
				Arrays.fill(n, Real.ZERO);
				
				setVariable.accept(variable, n);
				
				return obj;
			} else if (obj instanceof Number[]) {
				Number[] sizeArr = (Number[]) obj;
				check(sizeArr.length == 2, DimensionError);
				check(sizeArr[0] instanceof Real, TypeError);
				check(sizeArr[1] instanceof Real, TypeError);
				check(isInt(sizeArr[0]), TypeError);
				check(isInt(sizeArr[1]), TypeError);
				
				int rows = ((Real) sizeArr[0]).intValue();
				int columns = ((Real) sizeArr[1]).intValue();
				
				check(rows > 0, DimensionError);
				check(columns > 0, DimensionError);
				
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
		return "DimAssign{variable=\"%s\",value=%s}".format((Object) variable,
				value);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitDimAssign(this);
	}
	
}
