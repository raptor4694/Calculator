package calculator;

import static calculator.Functions.*;

import java.util.Arrays;

import lombok.experimental.UtilityClass;

public @UtilityClass class Operations {
	
	@func("swap rows in a matrix")
	public void RowSwap(Number[][] matrix, Real row1, Real row2) {
		check(row1.isInt(), TypeError.class);
		check(row2.isInt(), TypeError.class);
		
		RowSwap(matrix, row1.intValue(), row2.intValue());
	}
	
	void RowSwap(Number[][] matrix, int row1, int row2) {
		int columnCount = columnCount(matrix);
		for (int c = 0; c < columnCount; c++) {
			Number temp = matrix[row1][c];
			matrix[row1][c] = matrix[row2][c];
			matrix[row2][c] = temp;
		}
	}
	
	@func("swap columns in a matrix")
	public void ColumnSwap(Number[][] matrix, Real col1, Real col2) {
		check(col1.isInt(), TypeError.class);
		check(col2.isInt(), TypeError.class);
		
		ColumnSwap(matrix, col1.intValue(), col2.intValue());
	}
	
	void ColumnSwap(Number[][] matrix, int col1, int col2) {
		int rowCount = rowCount(matrix);
		for (int r = 0; r < rowCount; r++) {
			Number temp = matrix[r][col1];
			matrix[r][col1] = matrix[r][col2];
			matrix[r][col2] = temp;
		}
	}
	
	@func("swap two elements in an array")
	public void Swap(Number[] array, Real idx1, Real idx2) {
		check(idx1.isInt(), TypeError.class);
		check(idx2.isInt(), TypeError.class);
		
		Swap(array, idx1.intValue(), idx2.intValue());
	}
	
	void Swap(Number[] array, int idx1, int idx2) {
		Number temp = array[idx1];
		array[idx1] = array[idx2];
		array[idx2] = temp;
	}
	
	@func("sorts a set of numbers from lowest to highest")
	// modifies the array directly, but also returns it
	public Number[] sort(Number[] set) {
		Arrays.sort(set, (a1, a2) -> {
			check(a1 instanceof Real, TypeError.class);
			check(a2 instanceof Real, TypeError.class);
			return Double.compare(((Real) a1).value,
					((Real) a2).value);
		});
		return set;
	}
	
	@func("parallel sort-merge that breaks the array into sub-arrays that are themselves sorted and then merged")
	public Number[] parallelSort(Number[] set) {
		Arrays.parallelSort(set, (a1, a2) -> {
			check(a1 instanceof Real, TypeError.class);
			check(a2 instanceof Real, TypeError.class);
			return Double.compare(((Real) a1).value,
					((Real) a2).value);
		});
		return set;
	}
	
	@func("fills a set with the given value")
	public void Fill(Number[] set, Number value) {
		Arrays.fill(set, value);
	}
	
	@func("fills a matrix with the given value")
	public void Fill(Number[][] matrix, Number value) {
		for (int r = 0; r < rowCount(matrix); r++)
			for (int c = 0; c < columnCount(matrix); c++) {
				matrix[r][c] = value;
			}
	}
	
	@func("sets the message that is displayed when help(function) is called")
	public void SetDescription(Function func, String description) {
		if (func instanceof MethodFunction)
			throw new CalculatorError(
					"cannot set description of native function "
							+ func.getName());
		((UserFunction) func).description = description;
	}
	
	@func("resets the description of the function")
	public void SetDescription(Function func) {
		SetDescription(func, null);
	}
}
