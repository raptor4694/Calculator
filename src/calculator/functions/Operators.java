package calculator.functions;

import static calculator.functions.Functions.*;

import java.math.BigDecimal;
import java.math.MathContext;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import calculator.Printer;
import calculator.func;
import calculator.operator;
import calculator.param;
import calculator.errors.DimensionError;
import calculator.errors.TypeError;
import calculator.values.Amount;
import calculator.values.Function;
import calculator.values.Number;
import calculator.values.Real;
import lombok.experimental.UtilityClass;

@SuppressWarnings("rawtypes")
public @UtilityClass class Operators {
	
	@operator
	public Amount convert(Amount in, Unit to) {
		return in.convertTo(to);
	}
	
	@operator
	public Amount convert(Real r, Unit as) {
		return Amount.valueOf(r.value, as);
	}
	
	@operator
	public Amount degrees_postfix(Real in) {
		return Amount.valueOf(in.value, NonSI.DEGREE_ANGLE);
	}
	
	@operator
	public Amount degrees_postfix(Amount in) {
		return in.convertTo(NonSI.DEGREE_ANGLE);
	}
	
	@operator
	public Unit degrees_prefix(Unit in) {
		if (in.equals(SI.COULOMB))
			return SI.CELSIUS;
		else if (in.equals(SI.FARAD))
			return NonSI.FAHRENHEIT;
		throw new TypeError();
	}
	
	@operator
	@func("factorial")
	public Number factorial(Real r) {
		check(r.isInt(), TypeError);
		
		check(r.value >= 0, DimensionError);
		if (r.value == 0 || r.value == 1)
			return r;
		return Real.valueOf(factorial(r.intValue()));
	}
	
	int factorial(int i) {
		if (i <= 1)
			return i;
		return i * factorial(i - 1);
	}
	
	@operator
	@func("cardinality of a set (size of set)")
	public Number cardinality(Object[] set) {
		return dim(set);
	}
	
	@operator
	@func("cardinality of a matrix {rows, columns}")
	public Number[] cardinality(Object[][] matrix) {
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
		return Real.valueOf(d.value / 100);
	}
	
	@operator
	@func("if number == 0, returns 1, otherwise 0")
	public Number not(Number d) {
		return toBoolean(d)? Real.ZERO : Real.ONE;
	}
	
	@operator
	public Number not(Amount d) {
		return toBoolean(d)? Real.ZERO : Real.ONE;
	}
	
	@operator
	public Number not(Object obj) {
		return toBoolean(obj)? Real.ZERO : Real.ONE;
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
	
	/*@operator(reversible = true)
	public Number add(Real r, Complex c) {
		return Complex.valueOf(r.value + c.real, c.imag);
	}
	
	@operator
	public Number add(Complex a, Complex b) {
		return Complex.valueOf(a.real + b.real, a.imag + b.imag);
	}
	
	@operator
	public Number add(Real a, Real b) {
		return Real.valueOf(a.value + b.value);
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
	}*/
	
	@operator
	public Number add(Number a, Number b) {
		return a.plus(b);
	}
	
	@operator(reversible = true)
	public Unit add(Unit a, Real b) {
		return a.plus(b.value);
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
	
	/*@operator
	public Number subtract(Real r, Complex c) {
		return Complex.valueOf(r.value - c.real, c.imag);
	}
	
	@operator
	public Number subtract(Complex c, Real r) {
		return Complex.valueOf(c.real - r.value, c.imag);
	}
	
	@operator
	public Number subtract(Complex a, Complex b) {
		return Complex.valueOf(a.real - b.real, a.imag - b.imag);
	}
	
	@operator
	public Number subtract(Real a, Real b) {
		return Real.valueOf(a.value - b.value);
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
	}*/
	
	@operator
	public Number subtract(Number a, Number b) {
		return a.minus(b);
	}
	
	@operator
	public Unit subtract(Unit a, Real b) {
		return a.plus(-b.value);
	}
	
	/*@operator(reversible = true)
	public Number multiply(Real r, Complex c) {
		return Complex.valueOf(r.value * c.real, r.value * c.imag);
	}
	
	@operator
	public Number multiply(Complex a, Complex b) {
		return Complex.multiply(a.real, a.imag, b.real, b.imag);
	}
	
	@operator
	public Number multiply(Real a, Real b) {
		return Real.valueOf(a.value * b.value);
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
	}*/
	
	@operator
	public Number multiply(Number a, Number b) {
		return a.times(b);
	}
	
	@operator(reversible = true)
	public Amount multiply(Real a, Unit b) {
		return Amount.valueOf(a.value, b);
	}
	
	@operator(reversible = true)
	public Number multiply(Amount a, Unit b) {
		return a.times(Amount.valueOf(1, b));
	}
	
	@operator
	public Unit multiply(Unit a, Unit b) {
		return a.times(b);
	}
	
	/*@operator
	public Number divide(Real r, Complex c) {
		return Complex.divide(r.value, 0.0, c.real, c.imag);
	}
	
	@operator
	public Number divide(Complex c, Real r) {
		return Complex.divide(c.real, c.imag, r.value, 0.0);
	}
	
	@operator
	public Number divide(Complex a, Complex b) {
		return Complex.divide(a.real, a.imag, b.real, b.imag);
	}
	
	@operator
	public Number divide(Real a, Real b) {
		return Real.valueOf(a.value / b.value);
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
	}*/
	
	@operator
	public Number divide(Amount a, Unit b) {
		return a.divide(Amount.valueOf(1, b));
	}
	
	@operator
	public Number divide(Number a, Number b) {
		return a.divide(b);
	}
	
	@operator
	public Amount divide(Real a, Unit b) {
		return Amount.valueOf(a.value, Amount.inverseOf(b));
	}
	
	@operator
	public Unit divide(Unit a, Real b) {
		return a.divide(b.value);
	}
	
	@operator
	public Unit divide(Unit a, Unit b) {
		return a.divide(b);
	}
	
	/*@operator
	public Number pow(Real r, Complex c) {
		return Complex.pow(r.value, 0.0, c.real, c.imag);
	}
	
	@operator
	public Number pow(Complex c, Real r) {
		return Complex.pow(c.real, c.imag, r.value, 0.0);
	}
	
	@operator
	public Number pow(Complex a, Complex b) {
		return Complex.pow(a.real, a.imag, b.real, b.imag);
	}
	
	@operator
	public Number pow(Real a, Real b) {
		return Real.valueOf(Math.pow(a.value, b.value));
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
	}*/
	
	@operator
	public Number pow(Number a, Number b) {
		return a.pow(b);
	}
	
	@operator
	public Unit pow(Unit a, Real b) {
		check(b.isInt(), TypeError);
		int i = b.intValue();
		if (i == -1)
			return Amount.inverseOf(a);
		if (i < 0)
			return Amount.inverseOf(a).pow(-i);
		return a.pow(b.intValue());
	}
	
	/*@operator(reversible = true)
	public Number[] add(Number[] array, Number value) {
		check(array.length > 0, DimensionError);
		Number[] result = new Number[array.length];
		
		for (int i = 0; i < result.length; i++)
			result[i] = array[i] + value;
		
		return result;
	}
	
	@operator
	public Number[] add(Number[] a, Number[] b) {
		check(a.length == b.length, DimensionError);
		check(a.length > 0, DimensionError);
		
		Number[] result = new Number[a.length];
		
		for (int i = 0; i < a.length; i++)
			result[i] = a[i] + b[i];
		
		return result;
	}
	
	@operator(reversible = true)
	public Number[][] add(Number[][] matrix, Number value) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError);
		
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
		check(rowCount(A) == rowCount(B) && columnCount(A) == columnCount(B), DimensionError);
		
		int rowCount = rowCount(A);
		int columnCount = columnCount(A);
		
		check(rowCount > 0 && columnCount > 0, DimensionError);
		
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
		check(array.length > 0, DimensionError);
		Number[] result = new Number[array.length];
		
		for (int i = 0; i < result.length; i++)
			result[i] = array[i] - value;
		
		return result;
	}
	
	@operator
	public Number[] subtract(Number value, Number[] array) {
		check(array.length > 0, DimensionError);
		Number[] result = new Number[array.length];
		
		for (int i = 0; i < result.length; i++)
			result[i] = value - array[i];
		
		return result;
	}
	
	@operator
	public Number[] subtract(Number[] a, Number[] b) {
		check(a.length == b.length, DimensionError);
		check(a.length > 0, DimensionError);
		
		Number[] result = new Number[a.length];
		
		for (int i = 0; i < a.length; i++)
			result[i] = a[i] - b[i];
		
		return result;
	}
	
	@operator
	public Number[][] subtract(Number[][] matrix, Number value) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		
		check(rowCount > 0 && columnCount > 0, DimensionError);
		
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
		
		check(rowCount > 0 && columnCount > 0, DimensionError);
		
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
		check(rowCount(A) == rowCount(B) && columnCount(A) == columnCount(B), DimensionError);
		
		int rowCount = rowCount(A);
		int columnCount = columnCount(B);
		
		check(rowCount > 0 && columnCount > 0, DimensionError);
		
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
		check(a.length == b.length, DimensionError);
		check(a.length > 0, DimensionError);
		
		Number[] result = new Number[a.length];
		
		for (int i = 0; i < a.length; i++)
			result[i] = a[i] * b[i];
		
		return result;
	}*/
	
	// dot product
	@operator
	@func("dot product")
	public Number multiply(Number[] a, Number[] b) {
		check(a.length == b.length, DimensionError);
		check(a.length > 0, DimensionError);
		
		Number sum = Real.ZERO;
		
		for (int i = 0; i < a.length; i++)
			sum = sum.plus(multiply(a[i], b[i]));
		
		return sum;
	}
	
	@operator
	@func("cross product")
	public Number[] cross(@param("vector") Number[] a, @param("vector") Number[] b) {
		check(a.length == b.length, DimensionError);
		check(2 <= a.length && a.length <= 3, DimensionError);
		
		Number x = multiply(a[1], b[2]).minus(multiply(a[2], b[1]));
		Number y = multiply(a[2], b[0]).minus(multiply(a[0], b[2]));
		
		if (a.length == 2)
			return new Number[] {x, y};
		
		Number z = multiply(a[0], b[1]).minus(multiply(a[1], b[0]));
		
		return new Number[] {x, y, z};
	}
	
	/*@operator(reversible = true)r
	public Number[][] multiply(Number[][] matrix, Number value) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError);
		
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
		
		check(n > 0 && m > 0, DimensionError);
		check(p > 0 && rowCount(B) > 0, DimensionError);
		
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
		check(a.length == b.length, DimensionError);
		
		Number[] result = new Number[a.length];
		
		for (int i = 0; i < a.length; i++)
			result[i] = a[i] / b[i];
		
		return result;
	}
	
	@operator
	public Number[][] divide(Number[][] matrix, Number value) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		
		check(rowCount > 0 && columnCount > 0, DimensionError);
		
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
		
		check(rowCount > 0 && columnCount > 0, DimensionError);
		
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
		if (areInts(a, b)) {
			return Real.valueOf(castInt(a) % castInt(b));
		} else {
			BigDecimal A = BigDecimal.valueOf(a.value);
			BigDecimal B = BigDecimal.valueOf(b.value);
			
			return Real.valueOf(A.remainder(B, new MathContext(15)).doubleValue());
		}
	}
	
	@operator
	public Number[][] pow(Number[][] matrix, Number e) {
		if (!isInt(e))
			throw new TypeError();
		if (castInt(e) == -1) {
			return invert(matrix);
		} else {
			check(isSquare(matrix), DimensionError);
			
			int exponent = castInt(e);
			check(exponent >= 0, DimensionError);
			
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
		check(isInt(e), TypeError);
		
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
		check(a.length == b.length, DimensionError);
		
		Number[] result = new Number[a.length];
		
		for (int i = 0; i < a.length; i++)
			result[i] = mod(a[i], b[i]);
		
		return result;
	}
	
	@operator
	public Number[][] mod(Number[][] matrix, Number value) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		
		check(rowCount > 0 && columnCount > 0, DimensionError);
		
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
		
		check(rowCount > 0 && columnCount > 0, DimensionError);
		
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
		check(rowCount(A) == rowCount(B) && columnCount(A) == columnCount(B), DimensionError);
		
		int rowCount = rowCount(A);
		int columnCount = columnCount(B);
		
		check(rowCount > 0 && columnCount > 0, DimensionError);
		
		Number[][] result = new Number[rowCount][columnCount];
		
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = mod(A[r][c], B[r][c]);
			}
		}
		
		return result;
	}*/
	
	/*@operator
	public Number greater(Real a, Real b) {
		return toNumber(a.value > b.value);
	}
	
	@operator
	public Number greater(Real a, Amount b) {
		check(b.unit.isCompatible(Unit.ONE), TypeError);
		return toNumber(a.value > b.value);
	}
	
	@operator
	public Number greater(Amount a, Real b) {
		check(a.unit.isCompatible(Unit.ONE), TypeError);
		return toNumber(a.value > b.value);
	}*/
	
	@operator
	public Number greater(Number a, Number b) {
		return toNumber(a.isGreaterThan(b));
	}
	
	@operator
	public Number greater(Number[] array, Number value) {
		check(array.length > 0, DimensionError);
		
		for (Number d : array) {
			if (d.isLessThanOrEqualTo(value))
				return Real.ZERO;
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number greater(Number value, Number[] array) {
		check(array.length > 0, DimensionError);
		
		for (Number d : array) {
			if (value.isLessThanOrEqualTo(d))
				return Real.ZERO;
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number greater(Number[] a, Number[] b) {
		check(a.length > 0 && b.length > 0, DimensionError);
		
		for (Number d1 : a) {
			for (Number d2 : b) {
				if (d1.isLessThanOrEqualTo(d2))
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number greater(Number[][] matrix, Number value) {
		check(rowCount(matrix) > 0 && columnCount(matrix) > 0, DimensionError);
		
		for (Number[] array : matrix) {
			for (Number d : array) {
				if (d.isLessThanOrEqualTo(value))
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number greater(Number value, Number[][] matrix) {
		check(rowCount(matrix) > 0 && columnCount(matrix) > 0, DimensionError);
		
		for (Number[] array : matrix) {
			for (Number d : array) {
				if (value.isLessThanOrEqualTo(d))
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number greater(Number[][] A, Number[][] B) {
		check(rowCount(A) > 0 && columnCount(A) > 0, DimensionError);
		check(rowCount(B) > 0 && columnCount(B) > 0, DimensionError);
		
		//@formatter:off
		for (Number[] a_arr : A)
		for (Number[] b_arr : B) {
			for (Number a : a_arr)
			for (Number b : b_arr) {
				if (a.isLessThanOrEqualTo(b))
					return Real.ZERO;
			}
		}
		//@formatter:on
		
		return Real.ONE;
	}
	
	@operator
	public Number greater_equal(Number a, Number b) {
		return toNumber(a.isGreaterThanOrEqualTo(b));
	}
	
	@operator
	public Number greater_equal(Number[] array, Number value) {
		check(array.length > 0, DimensionError);
		
		for (Number d : array) {
			if (d.isLessThan(value))
				return Real.ZERO;
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number greater_equal(Number value, Number[] array) {
		check(array.length > 0, DimensionError);
		
		for (Number d : array) {
			if (value.isLessThan(d))
				return Real.ZERO;
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number greater_equal(Number[] a, Number[] b) {
		check(a.length > 0 && b.length > 0, DimensionError);
		
		for (Number d1 : a) {
			for (Number d2 : b) {
				if (d1.isLessThan(d2))
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number greater_equal(Number[][] matrix, Number value) {
		check(rowCount(matrix) > 0 && columnCount(matrix) > 0, DimensionError);
		
		for (Number[] array : matrix) {
			for (Number d : array) {
				if (d.isLessThan(value))
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number greater_equal(Number value, Number[][] matrix) {
		check(rowCount(matrix) > 0 && columnCount(matrix) > 0, DimensionError);
		
		for (Number[] array : matrix) {
			for (Number d : array) {
				if (value.isLessThan(d))
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number greater_equal(Number[][] A, Number[][] B) {
		check(rowCount(A) > 0 && columnCount(A) > 0, DimensionError);
		check(rowCount(B) > 0 && columnCount(B) > 0, DimensionError);
		
		//@formatter:off
		for (Number[] a_arr : A)
		for (Number[] b_arr : B) {
			for (Number a : a_arr)
			for (Number b : b_arr) {
				if (a.isLessThan(b))
					return Real.ZERO;
			}
		}
		//@formatter:on
		
		return Real.ONE;
	}
	
	@operator
	public Number lesser(Number a, Number b) {
		return toNumber(a.isLessThan(b));
	}
	
	@operator
	public Number lesser(Number[] array, Number value) {
		check(array.length > 0, DimensionError);
		
		for (Number d : array) {
			if (d.isGreaterThanOrEqualTo(value))
				return Real.ZERO;
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number lesser(Number value, Number[] array) {
		check(array.length > 0, DimensionError);
		
		for (Number d : array) {
			if (value.isGreaterThanOrEqualTo(d))
				return Real.ZERO;
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number lesser(Number[] a, Number[] b) {
		check(a.length > 0 && b.length > 0, DimensionError);
		
		for (Number d1 : a) {
			for (Number d2 : b) {
				if (d1.isGreaterThanOrEqualTo(d2))
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number lesser(Number[][] matrix, Number value) {
		check(rowCount(matrix) > 0 && columnCount(matrix) > 0, DimensionError);
		
		for (Number[] array : matrix) {
			for (Number d : array) {
				if (d.isGreaterThanOrEqualTo(value))
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number lesser(Number value, Number[][] matrix) {
		check(rowCount(matrix) > 0 && columnCount(matrix) > 0, DimensionError);
		
		for (Number[] array : matrix) {
			for (Number d : array) {
				if (value.isGreaterThanOrEqualTo(d))
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number lesser(Number[][] A, Number[][] B) {
		check(rowCount(A) > 0 && columnCount(A) > 0, DimensionError);
		check(rowCount(B) > 0 && columnCount(B) > 0, DimensionError);
		
		//@formatter:off
		for (Number[] a_arr : A)
		for (Number[] b_arr : B) {
			for (Number a : a_arr)
			for (Number b : b_arr) {
				if (a.isGreaterThanOrEqualTo(b))
					return Real.ZERO;
			}
		}
		//@formatter:on
		
		return Real.ONE;
	}
	
	@operator
	public Number lesser_equal(Number a, Number b) {
		return toNumber(a.isLessThanOrEqualTo(b));
	}
	
	@operator
	public Number lesser_equal(Number[] array, Number value) {
		check(array.length > 0, DimensionError);
		
		for (Number d : array) {
			if (d.isGreaterThan(value))
				return Real.ZERO;
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number lesser_equal(Number value, Number[] array) {
		check(array.length > 0, DimensionError);
		
		for (Number d : array) {
			if (value.isGreaterThan(d))
				return Real.ZERO;
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number lesser_equal(Number[] a, Number[] b) {
		check(a.length > 0 && b.length > 0, DimensionError);
		
		for (Number d1 : a) {
			for (Number d2 : b) {
				if (d1.isGreaterThan(d2))
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number lesser_equal(Number[][] matrix, Number value) {
		check(rowCount(matrix) > 0 && columnCount(matrix) > 0, DimensionError);
		
		for (Number[] array : matrix) {
			for (Number d : array) {
				if (d.isGreaterThan(value))
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number lesser_equal(Number value, Number[][] matrix) {
		check(rowCount(matrix) > 0 && columnCount(matrix) > 0, DimensionError);
		
		for (Number[] array : matrix) {
			for (Number d : array) {
				if (value.isGreaterThan(d))
					return Real.ZERO;
			}
		}
		
		return Real.ONE;
	}
	
	@operator
	public Number lesser_equal(Number[][] A, Number[][] B) {
		check(rowCount(A) > 0 && columnCount(A) > 0, DimensionError);
		check(rowCount(B) > 0 && columnCount(B) > 0, DimensionError);
		
		//@formatter:off
		for (Number[] a_arr : A)
		for (Number[] b_arr : B) {
			for (Number a : a_arr)
			for (Number b : b_arr) {
				if (a.isGreaterThan(b))
					return Real.ZERO;
			}
		}
		//@formatter:on
		
		return Real.ONE;
	}
	
	@operator
	public Number equals(Object a, Object b) {
		return toNumber(a.equals(b));
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
		check(a.length == b.length, DimensionError);
		
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
		check(array.length > 0, DimensionError);
		
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
