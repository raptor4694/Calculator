package calculator.functions;

import static calculator.functions.Functions.*;

import java.math.BigDecimal;
import java.math.MathContext;

import javax.measure.converter.ConversionException;
import javax.measure.unit.Unit;

import calculator.DimensionError;
import calculator.Printer;
import calculator.TypeError;
import calculator.func;
import calculator.operator;
import calculator.param;
import calculator.values.Complex;
import calculator.values.Function;
import calculator.values.Number;
import calculator.values.Real;
import lombok.experimental.UtilityClass;

public @UtilityClass class Operators {
	
	@operator
	@func("factorial")
	public Number factorial(Real r) {
		check(r.isInt(), TypeError.class);
		
		check(r.doubleValue() >= 0, DimensionError.class);
		if (r.doubleValue() == 0 || r.doubleValue() == 1)
			return r;
		return Real.valueOf(factorial(r.intValue()), r.getUnit());
	}
	
	int factorial(int i) {
		if (i <= 1)
			return i;
		return i * factorial(i - 1);
	}
	
	@operator
	@func("cardinality of a set (size of set)")
	public Number cardinality(Number[] set) {
		return dim(set);
	}
	
	@operator
	@func("cardinality of a matrix {rows, columns}")
	public Number[] cardinality(Number[][] matrix) {
		return dim(matrix);
	}
	
	@operator
	@func("length of string")
	public Number cardinality(String s) {
		return length(s);
	}
	
	@operator
	@func("negative of a number")
	public Number negate(Number d) {
		return d.negate();
	}
	
	@operator
	@func("percentage (divides by 100)")
	public Real percent(Real d) {
		return Real.valueOf(d.doubleValue() / 100, d.getUnit());
	}
	
	@operator
	@func("if number == 0, returns 1, otherwise 0")
	public Number not(Number d) {
		return toBoolean(d)? Real.ZERO : Real.ONE;
	}
	
	@operator
	@func("logical and")
	public Number and(Number a, Number b) {
		return toNumber(toBoolean(a) && toBoolean(b));
	}
	
	@operator(reversible = true)
	public Number and(Number[] array, Number b) {
		return toNumber(toBoolean(array) && toBoolean(b));
	}
	
	@operator(reversible = true)
	public Number and(Number[][] matrix, Number b) {
		return toNumber(toBoolean(matrix) && toBoolean(b));
	}
	
	@operator(reversible = true)
	public Number and(Number[][] matrix, Number[] array) {
		return toNumber(toBoolean(matrix) && toBoolean(array));
	}
	
	@operator
	public Number or(Number a, Number b) {
		return toNumber(toBoolean(a) || toBoolean(b));
	}
	
	@operator(reversible = true)
	public Number or(Number[] array, Number b) {
		return toNumber(toBoolean(array) || toBoolean(b));
	}
	
	@operator(reversible = true)
	public Number or(Number[][] matrix, Number b) {
		return toNumber(toBoolean(matrix) || toBoolean(b));
	}
	
	@operator(reversible = true)
	public Number or(Number[][] matrix, Number[] array) {
		return toNumber(toBoolean(matrix) || toBoolean(array));
	}
	
	@operator(reversible = true)
	public Number add(Real r, Complex c) {
		if (r.getUnit() != Unit.ONE) {
			throw new ConversionException();
		}
		return Complex.valueOf(r.doubleValue() + c.real, c.imag);
	}
	
	@operator
	public Number add(Complex a, Complex b) {
		return Complex.valueOf(a.real + b.real, a.imag + b.imag);
	}
	
	@operator
	public Number add(Real a, Real b) {
		return Real.valueOf(a.value.plus(b.value));
	}
	
	Number add(Number a, Number b) {
		if (a instanceof Complex) {
			Complex ac = (Complex) a;
			if (b instanceof Complex)
				return add(ac, (Complex) b);
			else
				return add((Real) b, ac);
		} else {
			Real r = (Real) a;
			if (b instanceof Complex)
				return add(r, (Complex) b);
			else
				return add(r, (Real) b);
		}
	}
	
	@operator
	public String add(String a, Number n) {
		return a + n.toString();
	}
	
	@operator
	public String add(Number n, String a) {
		return n.toString() + a;
	}
	
	@operator
	public String add(String a, Number[] array) {
		return a + Printer.toString(array);
	}
	
	@operator
	public String add(Number[] array, String a) {
		return Printer.toString(array) + a;
	}
	
	@operator
	public String add(String a, Number[][] matrix) {
		return a + Printer.toString(matrix);
	}
	
	@operator
	public String add(Number[][] matrix, String a) {
		return Printer.toString(matrix) + a;
	}
	
	@operator
	public String add(String a, String b) {
		return a + b;
	}
	
	@operator
	public Number subtract(Real r, Complex c) {
		check(r.getUnit() == Unit.ONE, ConversionException.class);
		return Complex.valueOf(r.doubleValue() - c.real, c.imag);
	}
	
	@operator
	public Number subtract(Complex c, Real r) {
		check(r.getUnit() == Unit.ONE, ConversionException.class);
		return Complex.valueOf(c.real - r.doubleValue(), c.imag);
	}
	
	@operator
	public Number subtract(Complex a, Complex b) {
		return Complex.valueOf(a.real - b.real, a.imag - b.imag);
	}
	
	@operator
	public Number subtract(Real a, Real b) {
		return Real.valueOf(a.value.minus(b.value));
	}
	
	Number subtract(Number a, Number b) {
		if (a instanceof Complex) {
			Complex ac = (Complex) a;
			if (b instanceof Complex)
				return subtract(ac, (Complex) b);
			else
				return subtract((Real) b, ac);
		} else {
			Real r = (Real) a;
			if (b instanceof Complex)
				return subtract(r, (Complex) b);
			else
				return subtract(r, (Real) b);
		}
	}
	
	@operator(reversible = true)
	public Number multiply(Real r, Complex c) {
		check(r.getUnit() == Unit.ONE, ConversionException.class);
		return Complex.valueOf(r.doubleValue() * c.real, r.doubleValue() * c.imag);
	}
	
	@operator
	public Number multiply(Complex a, Complex b) {
		return Complex.multiply(a.real, a.imag, b.real, b.imag);
	}
	
	@operator
	public Number multiply(Real a, Real b) {
		return Real.valueOf(a.value.times(b.value));
	}
	
	Number multiply(Number a, Number b) {
		if (a instanceof Complex) {
			Complex ac = (Complex) a;
			if (b instanceof Complex)
				return multiply(ac, (Complex) b);
			else
				return multiply((Real) b, ac);
		} else {
			Real r = (Real) a;
			if (b instanceof Complex)
				return multiply(r, (Complex) b);
			else
				return multiply(r, (Real) b);
		}
	}
	
	@operator
	public Number divide(Real r, Complex c) {
		check(r.getUnit() == Unit.ONE, ConversionException.class);
		return Complex.divide(r.doubleValue(), 0.0, c.real, c.imag);
	}
	
	@operator
	public Number divide(Complex c, Real r) {
		check(r.getUnit() == Unit.ONE, ConversionException.class);
		return Complex.divide(c.real, c.imag, r.doubleValue(), 0.0);
	}
	
	@operator
	public Number divide(Complex a, Complex b) {
		return Complex.divide(a.real, a.imag, b.real, b.imag);
	}
	
	@operator
	public Number divide(Real a, Real b) {
		return Real.valueOf(a.value.divide(b.value));
	}
	
	Number divide(Number a, Number b) {
		if (a instanceof Complex) {
			Complex ac = (Complex) a;
			if (b instanceof Complex)
				return divide(ac, (Complex) b);
			else
				return divide((Real) b, ac);
		} else {
			Real r = (Real) a;
			if (b instanceof Complex)
				return divide(r, (Complex) b);
			else
				return divide(r, (Real) b);
		}
	}
	
	@operator
	public Number pow(Real r, Complex c) {
		check(r.getUnit() == Unit.ONE, ConversionException.class);
		return Complex.pow(r.doubleValue(), 0.0, c.real, c.imag);
	}
	
	@operator
	public Number pow(Complex c, Real r) {
		check(r.getUnit() == Unit.ONE, ConversionException.class);
		return Complex.pow(c.real, c.imag, r.doubleValue(), 0.0);
	}
	
	@operator
	public Number pow(Complex a, Complex b) {
		return Complex.pow(a.real, a.imag, b.real, b.imag);
	}
	
	@operator
	public Number pow(Real a, Real b) {
		check(b.getUnit() == Unit.ONE, ConversionException.class);
		if (a.getUnit() == Unit.ONE)
			return Real.valueOf(Math.pow(a.doubleValue(), b.doubleValue()));
		else if (b.intValue() != b.doubleValue())
			throw new TypeError();
		else
			return Real.valueOf(a.value.pow(b.intValue()));
	}
	
	Number pow(Number a, Number b) {
		if (a instanceof Complex) {
			Complex ac = (Complex) a;
			if (b instanceof Complex)
				return pow(ac, (Complex) b);
			else
				return pow((Real) b, ac);
		} else {
			Real r = (Real) a;
			if (b instanceof Complex)
				return pow(r, (Complex) b);
			else
				return pow(r, (Real) b);
		}
	}
	
	/*@operator(reversible = true)
	public Number[] add(Number[] array, Number value) {
		check(array.length > 0, DimensionError.class);
		Number[] result = new Number[array.length];
		
		for (int i = 0; i < result.length; i++)
			result[i] = array[i] + value;
		
		return result;
	}
	
	@operator
	public Number[] add(Number[] a, Number[] b) {
		check(a.length == b.length, DimensionError.class);
		check(a.length > 0, DimensionError.class);
		
		Number[] result = new Number[a.length];
		
		for (int i = 0; i < a.length; i++)
			result[i] = a[i] + b[i];
		
		return result;
	}
	
	@operator(reversible = true)
	public Number[][] add(Number[][] matrix, Number value) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = matrix[r][c] + value;
			}
		}
		return result;
	}
	
	@operator
	public Number[][] add(Number[][] A, Number[][] B) {
		check(rowCount(A) == rowCount(B) && columnCount(A) == columnCount(B), DimensionError.class);
		
		int rowCount = rowCount(A);
		int columnCount = columnCount(A);
		
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = A[r][c] + B[r][c];
			}
		}
		
		return result;
	}*/
	
	/*@operator
	public Number[] subtract(Number[] array, Number value) {
		check(array.length > 0, DimensionError.class);
		Number[] result = new Number[array.length];
		
		for (int i = 0; i < result.length; i++)
			result[i] = array[i] - value;
		
		return result;
	}
	
	@operator
	public Number[] subtract(Number value, Number[] array) {
		check(array.length > 0, DimensionError.class);
		Number[] result = new Number[array.length];
		
		for (int i = 0; i < result.length; i++)
			result[i] = value - array[i];
		
		return result;
	}
	
	@operator
	public Number[] subtract(Number[] a, Number[] b) {
		check(a.length == b.length, DimensionError.class);
		check(a.length > 0, DimensionError.class);
		
		Number[] result = new Number[a.length];
		
		for (int i = 0; i < a.length; i++)
			result[i] = a[i] - b[i];
		
		return result;
	}
	
	@operator
	public Number[][] subtract(Number[][] matrix, Number value) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = matrix[r][c] - value;
			}
		}
		return result;
	}
	
	@operator
	public Number[][] subtract(Number value, Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = value - matrix[r][c];
			}
		}
		return result;
	}
	
	@operator
	public Number[][] subtract(Number[][] A, Number[][] B) {
		check(rowCount(A) == rowCount(B) && columnCount(A) == columnCount(B), DimensionError.class);
		
		int rowCount = rowCount(A);
		int columnCount = columnCount(B);
		
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = A[r][c] - B[r][c];
			}
		}
		
		return result;
	}*/
	
	/*@operator(reversible = true)
	public Number[] multiply(Number[] array, Number value) {
		Number[] result = new Number[array.length];
		
		for (int i = 0; i < result.length; i++)
			result[i] = array[i] * value;
		
		return result;
	}
	
	@operator
	public Number[] multiply(Number[] a, Number[] b) {
		check(a.length == b.length, DimensionError.class);
		check(a.length > 0, DimensionError.class);
		
		Number[] result = new Number[a.length];
		
		for (int i = 0; i < a.length; i++)
			result[i] = a[i] * b[i];
		
		return result;
	}*/
	
	// dot product
	@operator
	@func("dot product")
	public Number multiply(Number[] a, Number[] b) {
		check(a.length == b.length, DimensionError.class);
		check(a.length > 0, DimensionError.class);
		
		Number sum = Real.ZERO;
		
		for (int i = 0; i < a.length; i++)
			sum = sum.plus(multiply(a[i], b[i]));
		
		return sum;
	}
	
	@operator
	@func("cross product")
	public Number[] cross(@param("vector") Number[] a, @param("vector") Number[] b) {
		check(a.length == b.length, DimensionError.class);
		check(2 <= a.length && a.length <= 3, DimensionError.class);
		
		Number x = multiply(a[1], b[2]).minus(multiply(a[2], b[1]));
		Number y = multiply(a[2], b[0]).minus(multiply(a[0], b[2]));
		
		if (a.length == 2)
			return new Number[] {x, y};
		
		Number z = multiply(a[0], b[1]).minus(multiply(a[1], b[0]));
		
		return new Number[] {x, y, z};
	}
	
	/*@operato(reversible = true)r
	public Number[][] multiply(Number[][] matrix, Number value) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = matrix[r][c] * value;
			}
		}
		return result;
	}*/
	
	@operator
	@func("matrix multiplication")
	public Number[][] multiply(Number[][] A, Number[][] B) {
		check(columnCount(A) == rowCount(B),
				DimensionError.class/* , "A * B: columnCount(A) != rowCount(B)" */);
		
		int n = columnCount(A);
		int m = rowCount(A);
		int p = columnCount(B);
		
		check(n > 0 && m > 0, DimensionError.class);
		check(p > 0 && rowCount(B) > 0, DimensionError.class);
		
		Number[][] ans = new Number[m][p];
		
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < p; j++) {
				ans[i][j] = Real.ZERO;
				for (int k = 0; k < n; k++) {
					ans[i][j] = ans[i][j].plus(multiply(A[i][k], B[k][j]));
				}
			}
		}
		return ans;
	}
	
	/*@operator
	public Number[] divide(Number[] array, Number value) {
		Number[] result = new Number[array.length];
		
		for (int i = 0; i < result.length; i++)
			result[i] = array[i] / value;
		
		return result;
	}
	
	@operator
	public Number[] divide(Number value, Number[] array) {
		Number[] result = new Number[array.length];
		
		for (int i = 0; i < result.length; i++)
			result[i] = value / array[i];
		
		return result;
	}
	
	@operator
	public Number[] divide(Number[] a, Number[] b) {
		check(a.length == b.length, DimensionError.class);
		
		Number[] result = new Number[a.length];
		
		for (int i = 0; i < a.length; i++)
			result[i] = a[i] / b[i];
		
		return result;
	}
	
	@operator
	public Number[][] divide(Number[][] matrix, Number value) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = matrix[r][c] / value;
			}
		}
		return result;
	}
	
	@operator
	public Number[][] divide(Number value, Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = value / matrix[r][c];
			}
		}
		return result;
	}*/
	
	@operator
	public Number mod(Real a, Real b) {
		if (a.getUnit() == Unit.ONE && b.getUnit() == Unit.ONE) {
			if (areInts(a, b)) {
				return Real.valueOf(castInt(a) % castInt(b));
			} else {
				BigDecimal A = BigDecimal.valueOf(a.doubleValue());
				BigDecimal B = BigDecimal.valueOf(b.doubleValue());
				
				return Real.valueOf(
						A.remainder(B, new MathContext(15)).doubleValue());
			}
		} else
			return Real.valueOf(a.value.divide(b.value));
	}
	
	@operator
	public Number[][] pow(Number[][] matrix, Number e) {
		if (!isInt(e))
			throw new TypeError();
		if (castInt(e) == -1) {
			return invert(matrix);
		} else {
			check(isSquare(matrix), DimensionError.class);
			
			int exponent = castInt(e);
			check(exponent >= 0, DimensionError.class);
			
			if (exponent == 0) {
				return identity(rowCount(matrix));
			}
			
			// optimization
			if (exponent == 1)
				return (Number[][]) copy(matrix);
			
			Number[][] result = matrix;
			
			for (int i = 1; i < exponent; i++) {
				result = multiply(result, matrix);
			}
			
			return result;
		}
	}
	
	@operator
	public Number e(Number a, Real e) {
		check(isInt(e), TypeError.class);
		
		return a.times(Real.valueOf(Math.pow(10, castInt(e))));
	}
	
	/*@operator
	public Number[] mod(Number[] array, Number value) {
		Number[] result = new Number[array.length];
		
		for (int i = 0; i < result.length; i++)
			result[i] = mod(array[i], value);
		
		return result;
	}
	
	@operator
	public Number[] mod(Number value, Number[] array) {
		Number[] result = new Number[array.length];
		
		for (int i = 0; i < result.length; i++)
			result[i] = mod(value, array[i]);
		
		return result;
	}
	
	@operator
	public Number[] mod(Number[] a, Number[] b) {
		check(a.length == b.length, DimensionError.class);
		
		Number[] result = new Number[a.length];
		
		for (int i = 0; i < a.length; i++)
			result[i] = mod(a[i], b[i]);
		
		return result;
	}
	
	@operator
	public Number[][] mod(Number[][] matrix, Number value) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = mod(matrix[r][c], value);
			}
		}
		return result;
	}
	
	@operator
	public Number[][] mod(Number value, Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = mod(value, matrix[r][c]);
			}
		}
		return result;
	}
	
	@operator
	public Number[][] mod(Number[][] A, Number[][] B) {
		check(rowCount(A) == rowCount(B) && columnCount(A) == columnCount(B), DimensionError.class);
		
		int rowCount = rowCount(A);
		int columnCount = columnCount(B);
		
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = mod(A[r][c], B[r][c]);
			}
		}
		
		return result;
	}*/
	
	@operator
	public Number greater(Real a, Real b) {
		return toNumber(a.value.isGreaterThan(b.value));
	}
	
	@operator
	public Number greater(Number[] array, Real value) {
		check(array.length > 0, DimensionError.class);
		
		for (Number d : array) {
			check(d instanceof Real, TypeError.class);
			if (((Real) d).compareTo(value) <= 0)
				return Real.ZERO;
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number greater(Real value, Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		for (Number d : array) {
			check(d instanceof Real, TypeError.class);
			if (value.compareTo((Real) d) <= 0)
				return Real.ZERO;
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number greater(Number[] a, Number[] b) {
		check(a.length > 0 && b.length > 0, DimensionError.class);
		
		for (Number d1 : a) {
			for (Number d2 : b) {
				check(d1 instanceof Real, TypeError.class);
				check(d2 instanceof Real, TypeError.class);
				
				if (castDouble(d1) <= castDouble(d2))
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number greater(Number[][] matrix, Real value) {
		check(rowCount(matrix) > 0 && columnCount(matrix) > 0, DimensionError.class);
		
		for (Number[] array : matrix) {
			for (Number d : array) {
				check(d instanceof Real, TypeError.class);
				if (((Real) d).compareTo(value) <= 0)
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number greater(Real value, Number[][] matrix) {
		check(rowCount(matrix) > 0 && columnCount(matrix) > 0, DimensionError.class);
		
		for (Number[] array : matrix) {
			for (Number d : array) {
				check(d instanceof Real, TypeError.class);
				if (value.compareTo((Real) d) <= 0)
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number greater(Number[][] A, Number[][] B) {
		check(rowCount(A) > 0 && columnCount(A) > 0, DimensionError.class);
		check(rowCount(B) > 0 && columnCount(B) > 0, DimensionError.class);
		
		//@formatter:off
		for (Number[] a_arr : A)
		for (Number[] b_arr : B) {
			for (Number a : a_arr)
			for (Number b : b_arr) {
				check(a instanceof Real, TypeError.class);
				check(b instanceof Real, TypeError.class);
				if (castDouble(a) <= castDouble(b))
					return Real.ZERO;
			}
		}
		//@formatter:on
		
		return Real.ONE;
	}
	
	@operator
	public Number greater_equal(Real a, Real b) {
		return toNumber(a.compareTo(b) >= 0);
	}
	
	@operator
	public Number greater_equal(Number[] array, Real value) {
		check(array.length > 0, DimensionError.class);
		
		for (Number d : array) {
			check(d instanceof Real, TypeError.class);
			if (((Real) d).compareTo(value) < 0)
				return Real.ZERO;
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number greater_equal(Real value, Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		for (Number d : array) {
			check(d instanceof Real, TypeError.class);
			if (value.compareTo((Real) d) < 0)
				return Real.ZERO;
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number greater_equal(Number[] a, Number[] b) {
		check(a.length > 0 && b.length > 0, DimensionError.class);
		
		for (Number d1 : a) {
			for (Number d2 : b) {
				check(d1 instanceof Real, TypeError.class);
				check(d2 instanceof Real, TypeError.class);
				if (((Real) d1).compareTo((Real) d2) < 0)
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number greater_equal(Number[][] matrix, Real value) {
		check(rowCount(matrix) > 0 && columnCount(matrix) > 0, DimensionError.class);
		
		for (Number[] array : matrix) {
			for (Number d : array) {
				check(d instanceof Real, TypeError.class);
				if (((Real) d).compareTo(value) < 0)
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number greater_equal(Real value, Number[][] matrix) {
		check(rowCount(matrix) > 0 && columnCount(matrix) > 0, DimensionError.class);
		
		for (Number[] array : matrix) {
			for (Number d : array) {
				check(d instanceof Real, TypeError.class);
				if (value.compareTo((Real) d) < 0)
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number greater_equal(Number[][] A, Number[][] B) {
		check(rowCount(A) > 0 && columnCount(A) > 0, DimensionError.class);
		check(rowCount(B) > 0 && columnCount(B) > 0, DimensionError.class);
		
		//@formatter:off
		for (Number[] a_arr : A)
		for (Number[] b_arr : B) {
			for (Number a : a_arr)
			for (Number b : b_arr) {
				check(a instanceof Real, TypeError.class);
				check(b instanceof Real, TypeError.class);
				if (((Real)a).compareTo((Real)b) < 0)
					return Real.ZERO;
			}
		}
		//@formatter:on
		
		return Real.ONE;
	}
	
	@operator
	public Number lesser(Real a, Real b) {
		return toNumber(a.compareTo(b) < 0);
	}
	
	@operator
	public Number lesser(Number[] array, Real value) {
		check(array.length > 0, DimensionError.class);
		
		for (Number d : array) {
			check(d instanceof Real, TypeError.class);
			if (((Real) d).compareTo(value) >= 0)
				return Real.ZERO;
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number lesser(Real value, Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		for (Number d : array) {
			check(d instanceof Real, TypeError.class);
			if (value.compareTo((Real) d) >= 0)
				return Real.ZERO;
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number lesser(Number[] a, Number[] b) {
		check(a.length > 0 && b.length > 0, DimensionError.class);
		
		for (Number d1 : a) {
			for (Number d2 : b) {
				check(d1 instanceof Real, TypeError.class);
				check(d2 instanceof Real, TypeError.class);
				
				if (((Real) d1).compareTo((Real) d2) >= 0)
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number lesser(Number[][] matrix, Real value) {
		check(rowCount(matrix) > 0 && columnCount(matrix) > 0, DimensionError.class);
		
		for (Number[] array : matrix) {
			for (Number d : array) {
				check(d instanceof Real, TypeError.class);
				if (((Real) d).compareTo(value) >= 0)
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number lesser(Real value, Number[][] matrix) {
		check(rowCount(matrix) > 0 && columnCount(matrix) > 0, DimensionError.class);
		
		for (Number[] array : matrix) {
			for (Number d : array) {
				check(d instanceof Real, TypeError.class);
				if (value.compareTo((Real) d) >= 0)
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number lesser(Number[][] A, Number[][] B) {
		check(rowCount(A) > 0 && columnCount(A) > 0, DimensionError.class);
		check(rowCount(B) > 0 && columnCount(B) > 0, DimensionError.class);
		
		//@formatter:off
		for (Number[] a_arr : A)
		for (Number[] b_arr : B) {
			for (Number a : a_arr)
			for (Number b : b_arr) {
				check(a instanceof Real, TypeError.class);
				check(b instanceof Real, TypeError.class);
				if (((Real)a).compareTo((Real)b) >= 0)
					return Real.ZERO;
			}
		}
		//@formatter:on
		
		return Real.ONE;
	}
	
	@operator
	public Number lesser_equal(Real a, Real b) {
		return toNumber(a.compareTo(b) <= 0);
	}
	
	@operator
	public Number lesser_equal(Number[] array, Real value) {
		check(array.length > 0, DimensionError.class);
		
		for (Number d : array) {
			check(d instanceof Real, TypeError.class);
			if (((Real) d).compareTo(value) > 0)
				return Real.ZERO;
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number lesser_equal(Real value, Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		for (Number d : array) {
			check(d instanceof Real, TypeError.class);
			if (value.compareTo((Real) d) > 0)
				return Real.ZERO;
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number lesser_equal(Number[] a, Number[] b) {
		check(a.length > 0 && b.length > 0, DimensionError.class);
		
		for (Number d1 : a) {
			for (Number d2 : b) {
				check(d1 instanceof Real, TypeError.class);
				check(d2 instanceof Real, TypeError.class);
				
				if (((Real) d1).compareTo((Real) d2) > 0)
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number lesser_equal(Number[][] matrix, Real value) {
		check(rowCount(matrix) > 0 && columnCount(matrix) > 0, DimensionError.class);
		
		for (Number[] array : matrix) {
			for (Number d : array) {
				check(d instanceof Real, TypeError.class);
				
				if (((Real) d).compareTo(value) > 0)
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number lesser_equal(Real value, Number[][] matrix) {
		check(rowCount(matrix) > 0 && columnCount(matrix) > 0, DimensionError.class);
		
		for (Number[] array : matrix) {
			for (Number d : array) {
				check(d instanceof Real, TypeError.class);
				if (value.compareTo((Real) d) > 0)
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number lesser_equal(Number[][] A, Number[][] B) {
		check(rowCount(A) > 0 && columnCount(A) > 0, DimensionError.class);
		check(rowCount(B) > 0 && columnCount(B) > 0, DimensionError.class);
		
		//@formatter:off
		for (Number[] a_arr : A)
		for (Number[] b_arr : B) {
			for (Number a : a_arr)
			for (Number b : b_arr) {
				check(a instanceof Real, TypeError.class);
				check(b instanceof Real, TypeError.class);
				
				if (((Real)a).compareTo((Real)b) > 0)
					return Real.ZERO;
			}
		}
		//@formatter:on
		
		return Real.ONE;
	}
	
	@operator
	public Number equals(Object a, Object b) {
		return a.equals(b)? Real.ONE : Real.ZERO;
	}
	
	@operator
	public Number equals(String a, String b) {
		return toNumber(a.equals(b));
	}
	
	@operator
	public Number equals(Function a, Function b) {
		return toNumber(a.equals(b));
	}
	
	@operator
	public Number equals(Number a, Number b) {
		return toNumber(a.equals(b));
	}
	
	/*@operator(reversible = true)
	public Number equals(Number[] array, Number value) {
	
		for (Number d : array) {
			if (!d.equals(value))
				return Real.ZERO;
		}
		
		return Real.ONE;
	}*/
	
	@operator
	public Number equals(Number[] a, Number[] b) {
		check(a.length == b.length, DimensionError.class);
		
		for (int i = 0; i < a.length; i++) {
			if (!a[i].equals(b[i]))
				return Real.ZERO;
		}
		
		return Real.ONE;
	}
	
	/*@operator(reversible = true)
	public Number equals(Number[][] matrix, Number value) {
		check(rowCount(matrix) > 0 && columnCount(matrix) > 0,
				DimensionError.class);
		
		for (Number[] array : matrix) {
			for (Number d : array) {
				if (!d.equals(value))
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}*/
	
	@operator
	public Number equals(Number[][] A, Number[][] B) {
		// check(rowCount(A) > 0 && columnCount(A) > 0, DimensionError.class);
		// check(rowCount(B) > 0 && columnCount(B) > 0, DimensionError.class);
		if (rowCount(A) != rowCount(B) || columnCount(A) != columnCount(B))
			return Real.ZERO;
		
		for (int r = 0; r < A.length; r++) {
			for (int c = 0; c < columnCount(A); c++) {
				if (!A[r][c].equals(B[r][c]))
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number not_equals(String a, String b) {
		return toNumber(!a.equals(b));
	}
	
	@operator
	public Number not_equals(Function a, Function b) {
		return toNumber(!a.equals(b));
	}
	
	@operator
	public Number not_equals(Number a, Number b) {
		return toNumber(!a.equals(b));
	}
	
	/*@operator(reversible = true)
	public Number not_equals(Number[] array, Number value) {
		check(array.length > 0, DimensionError.class);
		
		for (Number d : array) {
			if (d.equals(value))
				return Real.ZERO;
		}
		
		return Real.ONE;
	}*/
	
	@operator
	public Number not_equals(Number[] a, Number[] b) {
		return equals(a, b) == Real.ZERO? Real.ONE : Real.ZERO;
	}
	
	/*@operator(reversible = true)
	public Number not_equals(Number[][] matrix, Number value) {
		check(rowCount(matrix) > 0 && columnCount(matrix) > 0,
				DimensionError.class);
		
		for (Number[] array : matrix) {
			for (Number d : array) {
				if (d.equals(value))
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}*/
	
	@operator
	public Number not_equals(Number[][] A, Number[][] B) {
		return equals(A, B) == Real.ZERO? Real.ONE : Real.ZERO;
	}
	
}
