package calculator;

import static calculator.Operations.*;
import static calculator.Operators.*;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Supplier;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

public @UtilityClass class Functions {
	private Random rand = new Random();
	
	public final int TYPE_REAL = 0, TYPE_COMPLEX = 1, TYPE_ARRAY = 2,
			TYPE_MATRIX = 3, TYPE_FUNCTION = 4, TYPE_STRING = 5,
			TYPE_STRING_ARRAY = 6, TYPE_2D_STRING_ARRAY = 7;
	
	@func("returns type of argument: 0 = real, 1 = complex, 2 = number array, 3 = number matrix, 4 = function, 5 = string, 6 = string array, 7 = 2-dimensional string array")
	public int typeof(Real d) {
		return TYPE_REAL;
	}
	
	@func
	public int typeof(Complex d) {
		return TYPE_COMPLEX;
	}
	
	@func
	public int typeof(Number[] d) {
		return TYPE_ARRAY;
	}
	
	@func
	public int typeof(Number[][] d) {
		return TYPE_MATRIX;
	}
	
	@func
	public int typeof(Function f) {
		return TYPE_FUNCTION;
	}
	
	@func
	public int typeof(String s) {
		return TYPE_STRING;
	}
	
	@func
	public int typeof(String[] a) {
		return TYPE_STRING_ARRAY;
	}
	
	@func
	public int typeof(String[][] a) {
		return TYPE_2D_STRING_ARRAY;
	}
	
	@func("import a Java function via fully-qualified name string")
	public Function _import(String s) {
		int i = s.lastIndexOf('.');
		if (i == -1)
			throw new CalculatorError("invalid function location");
		String className = s.substring(0, i);
		String funcName = s.substring(i + 1);
		try {
			return new MethodFunction(Class.forName(className), funcName);
		} catch (ClassNotFoundException e) {
			throw new CalculatorError(e.getMessage(), e);
		}
	}
	
	/////// SIZE ///////
	
	@func("size of array")
	public Number dim(Object[] array) {
		return Real.valueOf(array.length);
	}
	
	/*public Number dim(String[] array) {
		return Real.valueOf(array.length);
	}*/
	
	@func("size of matrix {rows, columns}")
	public Number[] dim(Object[][] matrix) {
		return new Number[] {	Real.valueOf(rowCount(matrix)),
								Real.valueOf(columnCount(matrix))};
	}
	
	@func("length of string")
	public Number length(String s) {
		return Real.valueOf(s.length());
	}
	
	/////// NUMBER FUNCTIONS ///////
	
	@func
	public Number ln(Complex c) {
		return Complex.ln(c.real, c.imag);
	}
	
	@func
	public Number ln(Real r) {
		if (r.value <= 0)
			return Complex.ln(r.value, 0.0);
		return Real.valueOf(Math.log(r.value));
	}
	
	static Number ln(Number n) {
		if (n instanceof Real)
			return ln((Real) n);
		else
			return ln((Complex) n);
	}
	
	@func("sets the seed to use in the random number generator")
	public void setRandomSeed(@param("int") Real seed) {
		check(seed.value == (long) seed.value, TypeError.class);
		
		rand.setSeed((long) seed.value);
	}
	
	@func("returns random, uniformly-distributed number between 0 and 1")
	public double rand() {
		return rand.nextDouble();
	}
	
	@func("random integer")
	public int randInt(@param("bound") Real upper) {
		check(isInt(upper), TypeError.class);
		
		return rand.nextInt(castInt(upper));
	}
	
	public int randInt(@param("lower") Real lower, @param("upper") Real upper) {
		check(isInt(lower), TypeError.class);
		check(isInt(upper), TypeError.class);
		
		int lowerInt = castInt(lower);
		
		return rand.nextInt(castInt(upper) - lowerInt) + lowerInt;
	}
	
	@func("returns an array of random integers between 0 and 10")
	public Number[] randBin(@param("size") Number size) {
		check(isInt(size), TypeError.class);
		Number[] result = new Number[castInt(size)];
		for (int i = 0; i < result.length; i++) {
			result[i] = Real.valueOf(rand.nextInt(11));
		}
		return result;
	}
	
	@func("returns an array of random integers")
	public Number[] randBin(@param("size") Number size,
			@param("bound") Number upper) {
		check(isInt(size), TypeError.class);
		check(isInt(upper), TypeError.class);
		int upperInt = castInt(upper);
		Number[] result = new Number[castInt(size)];
		for (int i = 0; i < result.length; i++) {
			result[i] = Real.valueOf(rand.nextInt(upperInt));
		}
		return result;
	}
	
	public Number[] randBin(@param("size") Number size, @param("lower") Number lower,
			@param("upper") Number upper) {
		check(isInt(size), TypeError.class);
		check(isInt(lower), TypeError.class);
		check(isInt(upper), TypeError.class);
		int lowerInt = castInt(lower);
		int upperInt = castInt(upper) - lowerInt;
		Number[] result = new Number[castInt(size)];
		for (int i = 0; i < result.length; i++) {
			result[i] = Real.valueOf(rand.nextInt(upperInt) + lowerInt);
		}
		return result;
	}
	
	@func("returns a matrix of random integers between 0 and 10")
	public Number[][] randM(@param("rows") Number rows,
			@param("columns") Number columns) {
		check(isInt(rows), TypeError.class);
		check(isInt(columns), TypeError.class);
		
		int rowCount = castInt(rows);
		int columnCount = castInt(columns);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = Real.valueOf(rand.nextInt(11));
			}
		}
		return result;
	}
	
	@func("returns a matrix of random integers")
	public Number[][] randM(@param("rows") Number rows,
			@param("columns") Number columns, @param("bound") Number bound) {
		check(isInt(rows), TypeError.class);
		check(isInt(columns), TypeError.class);
		check(isInt(bound), TypeError.class);
		
		int rowCount = castInt(rows);
		int columnCount = castInt(columns);
		int boundInt = castInt(bound);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = Real.valueOf(rand.nextInt(boundInt));
			}
		}
		return result;
	}
	
	@func("copies an array")
	public Object[] copy(Object[] array) {
		return array.clone();
	}
	
	@func("copies a matrix")
	public Object[][] copy(Object[][] matrix) {
		Object[][] result =
				(Object[][]) Array.newInstance(matrix.getClass().getComponentType(),
						rowCount(matrix));
		for (int r = 0; r < rowCount(matrix); r++) {
			result[r] = matrix[r].clone();
		}
		return result;
	}
	
	//////// SET OPERATIONS ////////
	
	@func("appends the value to the end of a copy of the set")
	public Number[] append(Number[] set, Number n) {
		Number[] result = new Number[set.length + 1];
		System.arraycopy(set, 0, result, 0, set.length);
		result[set.length] = n;
		return result;
	}
	
	@func
	public String[] append(Number[] set, String s) {
		if (set.length != 0)
			throw new TypeError();
		return new String[] {s};
	}
	
	@func
	public String[] append(String[] set, String s) {
		String[] result = new String[set.length + 1];
		System.arraycopy(set, 0, result, 0, set.length);
		result[set.length] = s;
		return result;
	}
	
	@func("appends the second set to the end of a copy of the first set")
	public Number[] append(Number[] set, Number[] set2) {
		Number[] result = new Number[set.length + set2.length];
		System.arraycopy(set, 0, result, 0, set.length);
		System.arraycopy(set2, 0, result, set.length, set2.length);
		return result;
	}
	
	@func
	public String[] append(Number[] set, String[] set2) {
		if (set.length != 0)
			throw new TypeError();
		return set2.clone();
	}
	
	@func
	public String[] append(String[] set, String[] set2) {
		String[] result = new String[set.length + set2.length];
		System.arraycopy(set, 0, result, 0, set.length);
		System.arraycopy(set2, 0, result, set.length, set2.length);
		return result;
	}
	
	@func("sample variance (square of standard deviation)")
	public Number variance(Number[] set) {
		// s² = (Σ (x-x̄)²)/(n-1)
		Number average = mean(set); // x̄
		
		Number sum = Real.ZERO;
		
		// Σ (x-x̄)²
		for (Number x : set) {
			Number value = x.minus(average);
			sum = sum.plus(value.times(value));
		}
		// Nm1 = n minus 1 = n-1
		Number Nm1 = Real.valueOf(set.length - 1);
		
		return sum.divide(Nm1);
	}
	
	@func("population variance (square of standard deviation)")
	public Number pVariance(Number[] set) {
		// σ² = (Σ (x-μ)²)/n
		Number average = mean(set); // μ
		
		Number sum = Real.ZERO;
		
		// Σ (x-x̄)²
		for (Number x : set) {
			Number value = x.minus(average);
			sum = sum.plus(value.times(value));
		}
		Number n = Real.valueOf(set.length);
		
		return sum.divide(n);
	}
	
	@func("sample standard deviation")
	public Number stdDev(Number[] set) {
		Number variance = variance(set);
		return multiply(variance, variance);
	}
	
	@func("population standard deviation")
	public Number popStdDev(Number[] set) {
		Number variance = pVariance(set);
		return multiply(variance, variance);
	}
	
	@func("variability (range) of a set is the max - min")
	public Number range(Number[] set) {
		return Operators.subtract(max(set), min(set));
	}
	
	@func("adds up the numbers in the set")
	public Number sum(Number[] set) {
		Number sum = Real.ZERO;
		for (Number d : set)
			sum = Operators.add(sum, d);
		return sum;
	}
	
	@func("the middle of a sorted set of numbers (first sorts the set)")
	public Number median(Number[] set) {
		check(set.length > 0, DimensionError.class);
		
		// optimization
		if (set.length == 1)
			return set[0];
		else if (set.length == 2)
			return Operators.add(set[0], set[1]).divide(Real.TWO);
		
		set = sort(set.clone());
		
		if (set.length % 2 == 0) {
			int i = set.length / 2;
			return add(set[i], set[i + 1]).divide(Real.TWO);
		} else {
			return set[set.length / 2];
		}
	}
	
	@func("the average value")
	public Number mean(Number[] set) {
		check(set.length > 0, DimensionError.class);
		return sum(set).divide(Real.valueOf(set.length));
	}
	
	@func("returns the modes of a set of numbers, as a new set")
	public Number[] modes(Number[] set) {
		// optimization
		if (set.length == 0)
			return new Number[0];
		if (set.length == 1)
			return set.clone();
		if (set.length == 2) {
			if (set[0] == set[1])
				return new Number[] {set[0]};
			else
				return new Number[0];
		}
		
		// actual mode algorithm
		
		Number[] values = new Number[set.length];
		int valuesLength = 0;
		int[] frequencies = new int[set.length];
		
		// count the frequencies
		for (Number d : set) {
			int i = indexOf0(values, d, valuesLength);
			if (i == -1) {
				++frequencies[valuesLength];
				values[valuesLength++] = d;
			} else {
				++frequencies[i];
			}
		}
		
		// find greatest frequency, and the indexes of the numbers it occurrs at
		int maxFreq = 0;
		int[] maxFreqIndexes = new int[valuesLength];
		int maxFreqIndexesLength = 0;
		for (int i = 0; i < valuesLength; i++) {
			if (frequencies[i] > maxFreq) {
				maxFreq = frequencies[i];
				Arrays.fill(maxFreqIndexes, 0);
				maxFreqIndexesLength = 1;
				maxFreqIndexes[0] = i;
			} else if (frequencies[i] == maxFreq) {
				maxFreqIndexes[maxFreqIndexesLength++] = i;
			}
		}
		
		// if each number appears only once, then there are no modes
		if (maxFreq <= 1)
			return new Number[0];
		// compile the modes into a new array
		Number[] result = new Number[maxFreqIndexesLength];
		for (int i = 0; i < maxFreqIndexesLength; i++) {
			result[i] = values[maxFreqIndexes[i]];
		}
		return sort(result);
	}
	
	@func("returns the lowest-valued mode of a set of numbers")
	public Number mode(Number[] set) {
		Number[] modes = modes(set);
		if (modes.length == 0)
			throw new CalculatorError("set does not have mode");
		return min(modes);
	}
	
	@func("gets the index of a value in the set, or 0 if it does not have it")
	public int indexOf(Number[] set, Number value) {
		return indexOf0(set, value, set.length) + 1;
	}
	
	public int indexOf(String[] set, String value) {
		return indexOf0(set, value, set.length) + 1;
	}
	
	<T> int indexOf0(T[] set, T value, int length) {
		for (int i = 0; i < length; i++) {
			if (set[i].equals(value))
				return i;
		}
		return -1;
	}
	
	//////// MATRIX OPERATIONS ////////
	
	@func("get number of rows in matrix")
	public int rowCount(Object[][] matrix) {
		return matrix.length;
	}
	
	@func("get number of columns in matrix")
	public int columnCount(Object[][] matrix) {
		return matrix[0].length;
	}
	
	@func("identity matrix")
	public Number[][] identity(@param("size") Number size) {
		check(isInt(size), TypeError.class);
		int length = castInt(size);
		return identity(length);
	}
	
	Number[][] identity(int length) {
		Number[][] result = new Number[length][length];
		for (int i = 0; i < length; i++)
			result[i][i] = Real.ONE;
		return result;
	}
	
	@func("inverse of matrix")
	public Number[][] invert(Number matrix[][]) {
		check(isSquare(matrix), DimensionError.class);
		
		int n = matrix.length;
		
		// optimization
		if (n == 1) {
			return new Number[][] {{matrix[0][0]}};
		} else if (n == 2) {
			Number a = matrix[0][0];
			Number b = matrix[0][1];
			Number c = matrix[1][0];
			Number d = matrix[1][1];
			
			Number factor = multiply(a, d).minus(multiply(b, c));
			
			return new Number[][] {	{d.divide(factor), b.negate().divide(factor)},
									{c.negate().divide(factor), a.divide(factor)}};
		}
		
		Number result[][] = new Number[n][n];
		Number b[][] = new Number[n][n];
		for (int i = 0; i < n; i++) {
			Arrays.fill(result[i], Real.ZERO);
			Arrays.fill(b[i], Real.ZERO);
		}
		int index[] = new int[n];
		for (int i = 0; i < n; ++i) {
			b[i][i] = Real.ONE;
		}
		
		// Transform the matrix into an upper triangle
		gaussian(matrix, index);
		
		// Update the matrix b[i][j] with the ratios stored
		for (int i = 0; i < n - 1; ++i)
			for (int j = i + 1; j < n; ++j)
				for (int k = 0; k < n; ++k) {
					Number factor = b[index[i]][k];
					if (factor == null) {
						throw new NullPointerException("b[(index[" + i + "] = "
								+ index[i] + ")][" + k + "] = null");
					}
					b[index[j]][k] =
							b[index[j]][k].minus(matrix[index[j]][i].times(factor));
				}
			
		// Perform backward substitutions
		for (int i = 0; i < n; ++i) {
			result[n - 1][i] =
					b[index[n - 1]][i].divide(matrix[index[n - 1]][n - 1]);
			for (int j = n - 2; j >= 0; --j) {
				result[j][i] = b[index[j]][i];
				for (int k = j + 1; k < n; ++k) {
					result[j][i] = result[j][i].minus(
							matrix[index[j]][k].times(result[k][i]));
				}
				result[j][i] = result[j][i].divide(matrix[index[j]][j]);
			}
		}
		
		// Round
		for (int r = 0; r < n; r++) {
			for (int c = 0; c < n; c++) {
				if (result[r][c] instanceof Real) {
					result[r][c] = Real.valueOf(round((Real) result[r][c], 14));
				}
			}
		}
		
		return result;
	}
	
	// Method to carry out the partial-pivoting Gaussian
	// elimination. Here index[] stores pivoting order.
	private void gaussian(Number a[][], int index[]) {
		int n = index.length;
		Number c[] = new Number[n];
		
		// Initialize the index
		for (int i = 0; i < n; ++i)
			index[i] = i;
		
		// Find the rescaling factors, one from each row
		for (int i = 0; i < n; ++i) {
			Real c1 = Real.ZERO;
			for (int j = 0; j < n; ++j) {
				Real c0 = abs(a[i][j]);
				if (c0.value > c1.value)
					c1 = c0;
			}
			c[i] = c1;
		}
		
		// Search the pivoting element from each column
		int k = 0;
		for (int j = 0; j < n - 1; ++j) {
			Real pi1 = Real.ZERO;
			for (int i = j; i < n; ++i) {
				Number pi0 = abs(a[index[i]][j]);
				pi0 = pi0.divide(c[index[i]]);
				check(pi0 instanceof Real, TypeError.class);
				if (((Real) pi0).value > pi1.value) {
					pi1 = (Real) pi0;
					k = i;
				}
			}
			
			// Interchange rows according to the pivoting order
			int itmp = index[j];
			index[j] = index[k];
			index[k] = itmp;
			for (int i = j + 1; i < n; ++i) {
				Number pj = a[index[i]][j].divide(a[index[j]][j]);
				
				// Record pivoting ratios below the diagonal
				a[index[i]][j] = pj;
				
				// Modify other elements accordingly
				for (int l = j + 1; l < n; ++l)
					a[index[i]][l] = a[index[i]][l].minus(pj.times(a[index[j]][l]));
			}
		}
	}
	
	@func("reduced row echelon form")
	public Number[][] rref(Number[][] matrix) {
		matrix = (Number[][]) copy(matrix);
		
		final int rowCount = rowCount(matrix);
		final int columnCount = columnCount(matrix);
		
		int lead = 0;
		for (int r = 0; r < rowCount; r++) {
			if (columnCount <= lead)
				break;
			int i = r;
			while (matrix[i][lead].equals(Real.ZERO)) {
				i++;
				if (i == rowCount) {
					i = r;
					lead++;
					if (columnCount == lead) {
						lead--;
						break;
					}
				}
			}
			RowSwap(matrix, r, i);
			Number div = matrix[r][lead];
			if (!div.equals(Real.ZERO)) {
				for (int c = 0; c < columnCount; c++)
					matrix[r][c] = matrix[r][c].divide(div);
			}
			for (int j = 0; j < rowCount; j++) {
				if (j != r) {
					Number sub = matrix[j][lead];
					for (int k = 0; k < columnCount; k++)
						matrix[j][k] = matrix[j][k].minus(sub.times(matrix[r][k]));
				}
			}
			lead++;
		}
		return matrix;
	}
	
	@func("returns 1 if the matrix is square, 0 otherwise")
	public boolean isSquare(Number[][] matrix) {
		return rowCount(matrix) == columnCount(matrix);
	}
	
	private Number[][] minor(Number[][] matrix, int x, int y) {
		assert isSquare(matrix);
		int length = matrix.length - 1;
		Number[][] result = new Number[length][length];
		for (int r = 0; r < length; r++) {
			for (int c = 0; c < length; c++) {
				if (r < x && c < y)
					result[r][c] = matrix[r][c];
				else if (r >= x && c < y)
					result[r][c] = matrix[r + 1][c];
				else if (r < x && c >= y)
					result[r][c] = matrix[r][c + 1];
				else // r > x && c > y
					result[r][c] = matrix[r + 1][c + 1];
			}
		}
		return result;
	}
	
	@func("determinant")
	public Number det(Number[][] matrix) {
		check(isSquare(matrix), DimensionError.class);
		switch (matrix.length) {
		case 1:
			return matrix[0][0];
		case 2:
			return multiply(matrix[0][0], matrix[1][1]).minus(
					multiply(matrix[1][0], matrix[0][1]));
		default:
			boolean subtract = false;
			Number sum = Real.ZERO;
			for (int i = 0; i < matrix.length; i++) {
				Number d = multiply(matrix[0][i], det(minor(matrix, 0, i)));
				if (subtract)
					sum = sum.minus(d);
				else
					sum = sum.plus(d);
				subtract = !subtract;
			}
			return sum;
		}
	}
	
	@func("permanant")
	public Number perm(Number[][] matrix) {
		check(isSquare(matrix), DimensionError.class);
		switch (matrix.length) {
		case 1:
			return matrix[0][0];
		case 2:
			return multiply(matrix[0][0], matrix[1][1]).plus(
					multiply(matrix[1][0], matrix[0][1]));
		default:
			Number sum = Real.ZERO;
			for (int i = 0; i < matrix.length; i++)
				sum = add(sum, multiply(matrix[0][i], perm(minor(matrix, 0, i))));
			return sum;
		}
	}
	
	//////// NUMBER OPERATIONS ////////
	
	@func("greatest common divisor of 2 integers")
	public Number gcd(@param("int") Number a, @param("int") Number b) {
		check(areInts(a, b), TypeError.class);
		
		return Real.valueOf(gcd(((Real) a).intValue(), ((Real) b).intValue()));
	}
	
	private int gcd(int a, int b) {
		if (a == 0)
			return b;
		if (b == 0)
			return a;
		if (a > b)
			return gcd(b, a % b);
		return gcd(a, b % a);
	}
	
	@func("least common multiple of 2 integers")
	public Number lcm(@param("int") Number a, @param("int") Number b) {
		check(areInts(a, b), TypeError.class);
		
		return Real.valueOf(lcm(((Real) a).intValue(), ((Real) b).intValue()));
	}
	
	private int lcm(int m, int n) {
		int lcm = (n == m || n == 1)? m : (m == 1? n : 0);
		if (lcm == 0) {
			int mm = m, nn = n;
			while (mm != nn) {
				while (mm < nn)
					mm += m;
				while (nn < mm)
					nn += n;
			}
			lcm = mm;
		}
		return lcm;
	}
	
	@func("strips number of decimals")
	public int trunc(Real d) {
		return d.intValue();
	}
	
	@func("round to nearest whole number")
	public double round(Real d) {
		return Math.round(d.value);
	}
	
	@func("round to nearest number of decimals")
	public double round(Real d, @param("int") Real numDecimals) {
		check(isInt(numDecimals), TypeError.class);
		check(numDecimals.value >= 0, DimensionError.class);
		BigDecimal bd = BigDecimal.valueOf(d.value);
		return bd.round(new MathContext(castInt(numDecimals) + 1)).doubleValue();
	}
	
	double round(Real d, int numDecimals) {
		check(numDecimals >= 0, DimensionError.class);
		BigDecimal bd = BigDecimal.valueOf(d.value);
		return bd.round(new MathContext(numDecimals)).doubleValue();
	}
	
	double round(double d, int numDecimals) {
		check(numDecimals >= 0, DimensionError.class);
		BigDecimal bd = BigDecimal.valueOf(d);
		return bd.round(new MathContext(numDecimals)).doubleValue();
	}
	
	@func("e^x")
	public Number exp(Real x) {
		return Real.valueOf(Math.exp(x.value));
	}
	
	Number exp(Number x) {
		if (x instanceof Real)
			return exp((Real) x);
		else
			return exp(x);
	}
	
	@func("(e^x) - 1")
	public Real expm1(Real d) {
		return Real.valueOf(Math.expm1(d.value));
	}
	
	@func("log base 10")
	public Number log(Real d) {
		if (d.value <= 0.0) {
			return Complex.ln(d.value, 0).divide(Real.LN_10);
		} else
			return Real.valueOf(Math.log(d.value));
	}
	
	@func
	public Number log(Complex c) {
		return Complex.ln(c.real, c.imag);
	}
	
	@func("greatest neighboring whole number")
	public Number ceil(Real d) {
		return Real.valueOf(Math.ceil(d.value));
	}
	
	@func("lowest neighboring whole number")
	public Number floor(Real d) {
		return Real.valueOf(Math.floor(d.value));
	}
	
	@func("square root")
	public Number sqrt(Real r) {
		if (r.value < 0)
			return Complex.valueOf(Math.sqrt(-r.value), 1);
		return Real.valueOf(Math.sqrt(r.value));
	}
	
	@func
	public Number sqrt(Complex c) {
		return Complex.sqrt(c.real, c.imag);
	}
	
	Number sqrt(Number n) {
		if (n instanceof Real)
			return sqrt((Real) n);
		return sqrt((Complex) n);
	}
	
	@func("cube root")
	public Number cbrt(Real d) {
		return Real.valueOf(Math.cbrt(d.value));
	}
	
	@func
	public Number cbrt(Complex c) {
		return Complex.pow(c.real, c.imag, 1.0 / 3.0, 0.0);
	}
	
	@func("greater of two numbers")
	public Number max(Real a, Real b) {
		return a.value > b.value? a : b;
	}
	
	@func("lesser of two numbers")
	public Number min(Real a, Real b) {
		return a.value < b.value? a : b;
	}
	
	@func("greatest in set of numbers")
	public Number max(Number[] a) {
		check(a.length > 0, DimensionError.class);
		Number max = a[0];
		check(max instanceof Real, TypeError.class);
		
		for (int i = 1; i < a.length; i++) {
			check(a[i] instanceof Real, TypeError.class);
			if (castDouble(a[i]) > castDouble(max))
				max = a[i];
		}
		return max;
	}
	
	@func("lowest in set of numbers")
	public Number min(Number[] a) {
		check(a.length > 0, DimensionError.class);
		Number min = a[0];
		check(min instanceof Real, TypeError.class);
		
		for (int i = 1; i < a.length; i++) {
			check(a[i] instanceof Real, TypeError.class);
			if (castDouble(a[i]) < castDouble(min))
				min = a[i];
		}
		return min;
	}
	
	@func("absolute value")
	public Real abs(Real d) {
		return Real.valueOf(Math.abs(d.value));
	}
	
	@func
	public Real abs(Complex c) {
		return c.abs();
	}
	
	Real abs(Number n) {
		if (n instanceof Real)
			return abs((Real) n);
		else
			return abs((Complex) n);
	}
	
	@func("convert degrees to radians")
	public Number toRadians(Real degrees) {
		return Real.valueOf(Math.toRadians(degrees.value));
	}
	
	@func("convert radians to degrees")
	public Number toDegrees(Real radians) {
		return Real.valueOf(Math.toDegrees(radians.value));
	}
	
	@func("inverse hyperbolic sine")
	public Number asinh(Real d) {
		return Real.valueOf(Math.log(Math.sqrt(d.value * d.value + 1) + d.value));
	}
	
	@func("inverse hyperbolic cosine")
	public Number acosh(Real d) {
		return Real.valueOf(
				Math.log(d.value + Math.sqrt(d.value - 1) * Math.sqrt(d.value + 1)));
	}
	
	@func("inverse hyperbolic tangent")
	public Number atanh(Real d) {
		return Real.valueOf(0.5 * (Math.log(d.value + 1) - Math.log(1 - d.value)));
	}
	
	@func("exact inverse hyperbolic tanget")
	public Number atan(Real y, Real x) {
		return Real.valueOf(Math.atan2(y.value, x.value));
	}
	
	@func("exact inverse cotanget")
	public Number acot(Real x, Real y) {
		return atan(y, x);
	}
	
	@func("sine")
	public Number sin(Real r) {
		return Real.valueOf(Math.sin(r.value));
	}
	
	@func
	public Number sin(Complex c) {
		return Complex.sin(c);
	}
	
	Number sin(Number n) {
		if (n instanceof Complex)
			return sin((Complex) n);
		return sin((Real) n);
	}
	
	@func
	public Number[] sin(Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		Number[] result = new Number[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = sin(array[i]);
		}
		return result;
	}
	
	@func
	public Number[][] sin(Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = sin(matrix[r][c]);
			}
		}
		return result;
	}
	
	@func("inverse sine")
	public Number asin(Real r) {
		return Real.valueOf(Math.asin(r.value));
	}
	
	@func
	public Number asin(Complex c) {
		return Complex.asin(c);
	}
	
	Number asin(Number n) {
		if (n instanceof Complex)
			return asin((Complex) n);
		return asin((Real) n);
	}
	
	@func
	public Number[] asin(Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		Number[] result = new Number[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = asin(array[i]);
		}
		return result;
	}
	
	@func
	public Number[][] asin(Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = asin(matrix[r][c]);
			}
		}
		return result;
	}
	
	@func("hyperbolic sine")
	public Number sinh(Real r) {
		return Real.valueOf(Math.sinh(r.value));
	}
	
	@func
	public Number sinh(Complex c) {
		return Complex.sinh(c);
	}
	
	Number sinh(Number n) {
		if (n instanceof Complex)
			return sinh((Complex) n);
		return sinh((Real) n);
	}
	
	@func
	public Number[] sinh(Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		Number[] result = new Number[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = sinh(array[i]);
		}
		return result;
	}
	
	@func
	public Number[][] sinh(Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = sinh(matrix[r][c]);
			}
		}
		return result;
	}
	
	/*@func("inverse hyperbolic sine")
	public Number asinh(Real r) {
		return Real.valueOf(Math.asinh(r.value));
	}*/
	
	@func
	public Number asinh(Complex c) {
		return Complex.asinh(c);
	}
	
	Number asinh(Number n) {
		if (n instanceof Complex)
			return asinh((Complex) n);
		return asinh((Real) n);
	}
	
	@func
	public Number[] asinh(Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		Number[] result = new Number[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = asinh(array[i]);
		}
		return result;
	}
	
	@func
	public Number[][] asinh(Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = asinh(matrix[r][c]);
			}
		}
		return result;
	}
	
	@func("cosecant")
	public Number csc(Real r) {
		return Real.valueOf(1.0 / Math.sin(r.value));
	}
	
	@func
	public Number csc(Complex c) {
		return Real.ONE.divide(Complex.sin(c));
	}
	
	Number csc(Number n) {
		if (n instanceof Complex)
			return csc((Complex) n);
		return csc((Real) n);
	}
	
	@func
	public Number[] csc(Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		Number[] result = new Number[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = csc(array[i]);
		}
		return result;
	}
	
	@func
	public Number[][] csc(Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = csc(matrix[r][c]);
			}
		}
		return result;
	}
	
	@func("inverse cosecant")
	public Number acsc(Real r) {
		return Real.valueOf(Math.asin(1.0 / r.value));
	}
	
	@func
	public Number acsc(Complex c) {
		Number n = divide(Real.ONE, c);
		if (n instanceof Complex)
			return Complex.asin((Complex) n);
		return Real.valueOf(Math.asin(((Real) n).value));
	}
	
	Number acsc(Number n) {
		if (n instanceof Complex)
			return acsc((Complex) n);
		return acsc((Real) n);
	}
	
	@func
	public Number[] acsc(Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		Number[] result = new Number[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = acsc(array[i]);
		}
		return result;
	}
	
	@func
	public Number[][] acsc(Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = acsc(matrix[r][c]);
			}
		}
		return result;
	}
	
	@func("hyperbolic cosecant")
	public Number csch(Real r) {
		return Real.valueOf(1.0 / Math.sinh(r.value));
	}
	
	@func
	public Number csch(Complex c) {
		return Real.ONE.divide(Complex.sinh(c));
	}
	
	Number csch(Number n) {
		if (n instanceof Complex)
			return csch((Complex) n);
		return csch((Real) n);
	}
	
	@func
	public Number[] csch(Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		Number[] result = new Number[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = csch(array[i]);
		}
		return result;
	}
	
	@func
	public Number[][] csch(Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = csch(matrix[r][c]);
			}
		}
		return result;
	}
	
	@func("inverse hyperbolic cosecant")
	public Number acsch(Real r) {
		return asinh(divide(Real.ONE, r));
	}
	
	@func
	public Number acsch(Complex c) {
		return asinh(divide(Real.ONE, c));
	}
	
	Number acsch(Number n) {
		return asinh(Real.ONE.divide(n));
	}
	
	@func
	public Number[] acsch(Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		Number[] result = new Number[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = acsch(array[i]);
		}
		return result;
	}
	
	@func
	public Number[][] acsch(Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = acsch(matrix[r][c]);
			}
		}
		return result;
	}
	
	@func("cosine")
	public Number cos(Real r) {
		return Real.valueOf(Math.cos(r.value));
	}
	
	@func
	public Number cos(Complex c) {
		return Complex.cos(c);
	}
	
	Number cos(Number n) {
		if (n instanceof Complex)
			return cos((Complex) n);
		return cos((Real) n);
	}
	
	@func
	public Number[] cos(Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		Number[] result = new Number[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = cos(array[i]);
		}
		return result;
	}
	
	@func
	public Number[][] cos(Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = cos(matrix[r][c]);
			}
		}
		return result;
	}
	
	@func("inverse cosine")
	public Number acos(Real r) {
		return Real.valueOf(Math.acos(r.value));
	}
	
	@func
	public Number acos(Complex c) {
		return Complex.acos(c);
	}
	
	Number acos(Number n) {
		if (n instanceof Complex)
			return acos((Complex) n);
		return acos((Real) n);
	}
	
	@func
	public Number[] acos(Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		Number[] result = new Number[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = acos(array[i]);
		}
		return result;
	}
	
	@func
	public Number[][] acos(Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = acos(matrix[r][c]);
			}
		}
		return result;
	}
	
	@func("hyperbolic cosine")
	public Number cosh(Real r) {
		return Real.valueOf(Math.cosh(r.value));
	}
	
	@func
	public Number cosh(Complex c) {
		return Complex.cosh(c);
	}
	
	Number cosh(Number n) {
		if (n instanceof Complex)
			return cosh((Complex) n);
		return cosh((Real) n);
	}
	
	@func
	public Number[] cosh(Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		Number[] result = new Number[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = cosh(array[i]);
		}
		return result;
	}
	
	@func
	public Number[][] cosh(Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = cosh(matrix[r][c]);
			}
		}
		return result;
	}
	
	/*@func("inverse hyperbolic cosine")
	public Number acosh(Real r) {
		return Real.valueOf(Math.acosh(r.value));
	}*/
	
	@func
	public Number acosh(Complex c) {
		return Complex.acosh(c);
	}
	
	Number acosh(Number n) {
		if (n instanceof Complex)
			return acosh((Complex) n);
		return acosh((Real) n);
	}
	
	@func
	public Number[] acosh(Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		Number[] result = new Number[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = acosh(array[i]);
		}
		return result;
	}
	
	@func
	public Number[][] acosh(Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = acosh(matrix[r][c]);
			}
		}
		return result;
	}
	
	@func("secant")
	public Number sec(Real r) {
		return Real.valueOf(1.0 / Math.cos(r.value));
	}
	
	@func
	public Number sec(Complex c) {
		return Real.ONE.divide(Complex.cos(c));
	}
	
	Number sec(Number n) {
		if (n instanceof Complex)
			return sec((Complex) n);
		return sec((Real) n);
	}
	
	@func
	public Number[] sec(Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		Number[] result = new Number[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = sec(array[i]);
		}
		return result;
	}
	
	@func
	public Number[][] sec(Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = sec(matrix[r][c]);
			}
		}
		return result;
	}
	
	@func("inverse secant")
	public Number asec(Real r) {
		return Real.valueOf(Math.acos(1.0 / r.value));
	}
	
	@func
	public Number asec(Complex c) {
		Number n = divide(Real.ONE, c);
		if (n instanceof Complex)
			return Complex.acos((Complex) n);
		return Real.valueOf(Math.acos(((Real) n).value));
	}
	
	Number asec(Number n) {
		if (n instanceof Complex)
			return asec((Complex) n);
		return asec((Real) n);
	}
	
	@func
	public Number[] asec(Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		Number[] result = new Number[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = asec(array[i]);
		}
		return result;
	}
	
	@func
	public Number[][] asec(Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = asec(matrix[r][c]);
			}
		}
		return result;
	}
	
	@func("hyperbolic secant")
	public Number sech(Real r) {
		return Real.valueOf(1.0 / Math.cosh(r.value));
	}
	
	@func
	public Number sech(Complex c) {
		return Real.ONE.divide(Complex.cosh(c));
	}
	
	Number sech(Number n) {
		if (n instanceof Complex)
			return sech((Complex) n);
		return sech((Real) n);
	}
	
	@func
	public Number[] sech(Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		Number[] result = new Number[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = sech(array[i]);
		}
		return result;
	}
	
	@func
	public Number[][] sech(Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = sech(matrix[r][c]);
			}
		}
		return result;
	}
	
	@func("inverse hyperbolic secant")
	public Number asech(Real r) {
		return acosh(divide(Real.ONE, r));
	}
	
	@func
	public Number asech(Complex c) {
		return acosh(divide(Real.ONE, c));
	}
	
	Number asech(Number n) {
		return acosh(Real.ONE.divide(n));
	}
	
	@func
	public Number[] asech(Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		Number[] result = new Number[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = asech(array[i]);
		}
		return result;
	}
	
	@func
	public Number[][] asech(Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = asech(matrix[r][c]);
			}
		}
		return result;
	}
	
	@func("tangent")
	public Number tan(Real r) {
		return Real.valueOf(Math.tan(r.value));
	}
	
	@func
	public Number tan(Complex c) {
		return Complex.tan(c);
	}
	
	Number tan(Number n) {
		if (n instanceof Complex)
			return tan((Complex) n);
		return tan((Real) n);
	}
	
	@func
	public Number[] tan(Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		Number[] result = new Number[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = tan(array[i]);
		}
		return result;
	}
	
	@func
	public Number[][] tan(Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = tan(matrix[r][c]);
			}
		}
		return result;
	}
	
	@func("inverse tangent")
	public Number atan(Real r) {
		return Real.valueOf(Math.atan(r.value));
	}
	
	@func
	public Number atan(Complex c) {
		return Complex.atan(c);
	}
	
	Number atan(Number n) {
		if (n instanceof Complex)
			return atan((Complex) n);
		return atan((Real) n);
	}
	
	@func
	public Number[] atan(Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		Number[] result = new Number[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = atan(array[i]);
		}
		return result;
	}
	
	@func
	public Number[][] atan(Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = atan(matrix[r][c]);
			}
		}
		return result;
	}
	
	@func("hyperbolic tangent")
	public Number tanh(Real r) {
		return Real.valueOf(Math.tanh(r.value));
	}
	
	@func
	public Number tanh(Complex c) {
		return Complex.tanh(c);
	}
	
	Number tanh(Number n) {
		if (n instanceof Complex)
			return tanh((Complex) n);
		return tanh((Real) n);
	}
	
	@func
	public Number[] tanh(Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		Number[] result = new Number[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = tanh(array[i]);
		}
		return result;
	}
	
	@func
	public Number[][] tanh(Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = tanh(matrix[r][c]);
			}
		}
		return result;
	}
	
	/*@func("inverse hyperbolic tangent")
	public Number atanh(Real r) {
		return Real.valueOf(Math.atanh(r.value));
	}*/
	
	@func
	public Number atanh(Complex c) {
		return Complex.atanh(c);
	}
	
	Number atanh(Number n) {
		if (n instanceof Complex)
			return atanh((Complex) n);
		return atanh((Real) n);
	}
	
	@func
	public Number[] atanh(Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		Number[] result = new Number[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = atanh(array[i]);
		}
		return result;
	}
	
	@func
	public Number[][] atanh(Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = atanh(matrix[r][c]);
			}
		}
		return result;
	}
	
	@func("cotangent")
	public Number cot(Real r) {
		return Real.valueOf(1.0 / Math.tan(r.value));
	}
	
	@func
	public Number cot(Complex c) {
		return Real.ONE.divide(Complex.tan(c));
	}
	
	Number cot(Number n) {
		if (n instanceof Complex)
			return cot((Complex) n);
		return cot((Real) n);
	}
	
	@func
	public Number[] cot(Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		Number[] result = new Number[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = cot(array[i]);
		}
		return result;
	}
	
	@func
	public Number[][] cot(Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = cot(matrix[r][c]);
			}
		}
		return result;
	}
	
	@func("inverse cotangent")
	public Number acot(Real r) {
		return Real.valueOf(Math.atan(1.0 / r.value));
	}
	
	@func
	public Number acot(Complex c) {
		Number n = divide(Real.ONE, c);
		if (n instanceof Complex)
			return Complex.atan((Complex) n);
		return Real.valueOf(Math.atan(((Real) n).value));
	}
	
	Number acot(Number n) {
		if (n instanceof Complex)
			return acot((Complex) n);
		return acot((Real) n);
	}
	
	@func
	public Number[] acot(Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		Number[] result = new Number[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = acot(array[i]);
		}
		return result;
	}
	
	@func
	public Number[][] acot(Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = acot(matrix[r][c]);
			}
		}
		return result;
	}
	
	@func("hyperbolic cotangent")
	public Number coth(Real r) {
		return Real.valueOf(1.0 / Math.tanh(r.value));
	}
	
	@func
	public Number coth(Complex c) {
		return Real.ONE.divide(Complex.tanh(c));
	}
	
	Number coth(Number n) {
		if (n instanceof Complex)
			return coth((Complex) n);
		return coth((Real) n);
	}
	
	@func
	public Number[] coth(Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		Number[] result = new Number[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = coth(array[i]);
		}
		return result;
	}
	
	@func
	public Number[][] coth(Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = coth(matrix[r][c]);
			}
		}
		return result;
	}
	
	@func("inverse hyperbolic cotangent")
	public Number acoth(Real r) {
		return atanh(divide(Real.ONE, r));
	}
	
	@func
	public Number acoth(Complex c) {
		return atanh(divide(Real.ONE, c));
	}
	
	Number acoth(Number n) {
		return atanh(Real.ONE.divide(n));
	}
	
	@func
	public Number[] acoth(Number[] array) {
		check(array.length > 0, DimensionError.class);
		
		Number[] result = new Number[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = acoth(array[i]);
		}
		return result;
	}
	
	@func
	public Number[][] acoth(Number[][] matrix) {
		int rowCount = rowCount(matrix);
		int columnCount = columnCount(matrix);
		check(rowCount > 0 && columnCount > 0, DimensionError.class);
		
		Number[][] result = new Number[rowCount][columnCount];
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				result[r][c] = acoth(matrix[r][c]);
			}
		}
		return result;
	}
	
	//////// MISC OPERATIONS ////////
	
	@SuppressWarnings("unused")
	private void check(boolean condition,
			Supplier<? extends CalculatorError> errorSupplier) {
		if (!condition)
			throw errorSupplier.get();
	}
	
	@SneakyThrows
	void check(boolean condition, Class<? extends CalculatorError> errorType) {
		if (!condition)
			throw errorType.newInstance();
	}
	
	@SneakyThrows
	void check(boolean condition, Class<? extends CalculatorError> errorType,
			Object... args) {
		if (!condition) {
			Class<?>[] parameterTypes = new Class<?>[args.length];
			for (int i = 0; i < args.length; i++)
				parameterTypes[i] = args[i].getClass();
			Constructor<? extends CalculatorError> constructor =
					errorType.getConstructor(parameterTypes);
			throw constructor.newInstance(args);
		}
	}
	
	boolean isInt(Real r) {
		return r.isInt();
	}
	
	boolean isInt(double d) {
		return (int) d == d;
	}
	
	boolean isInt(Number n) {
		return n instanceof Real && ((Real) n).isInt();
	}
	
	int castInt(Number n) {
		return (int) ((Real) n).value;
	}
	
	double castDouble(Number n) {
		return ((Real) n).value;
	}
	
	boolean areInts(Number n1, Number n2) {
		return isInt(n1) && isInt(n2);
	}
	
	Number toNumber(boolean bool) {
		return bool? Real.ONE : Real.ZERO;
	}
	
	Number toNumber(Object obj) {
		if (obj instanceof Boolean)
			return toNumber((Boolean) obj);
		else if (obj instanceof Number)
			return (Number) obj;
		else
			throw new AssertionError();
	}
	
	Number toNumber(Boolean bool) {
		return toNumber((boolean) bool);
	}
	
	boolean toBoolean(Number d) {
		if (d instanceof Real)
			return toBoolean((Real) d);
		else
			return toBoolean((Complex) d);
	}
	
	boolean toBoolean(Real r) {
		return r.value != 0.0;
	}
	
	boolean toBoolean(Complex c) {
		return c.real != 0.0 || c.imag != 0.0;
	}
	
	boolean toBoolean(Object[] array) {
		if (array.length == 0)
			return false;
		for (Object d : array)
			if (toBoolean(d))
				return true;
		return false;
	}
	
	boolean toBoolean(Object[][] matrix) {
		if (matrix.length == 0)
			return false;
		for (Object[] array : matrix) {
			if (toBoolean(array))
				return true;
		}
		return false;
	}
	
	boolean toBoolean(String s) {
		return !s.isEmpty();
	}
	
	boolean toBoolean(Object obj) {
		if (obj instanceof Number)
			return toBoolean((Number) obj);
		else if (obj instanceof Object[][])
			return toBoolean((Object[][]) obj);
		else if (obj instanceof Object[])
			return toBoolean((Object[]) obj);
		else if (obj instanceof String)
			return toBoolean((String) obj);
		else if (obj instanceof Function)
			return true;
		else
			throw new TypeError(obj.getClass().getSimpleName());
	}
	
	Object evalValue(Object obj) {
		if (obj instanceof java.lang.Number)
			return Real.valueOf(((java.lang.Number) obj).doubleValue());
		else if (obj instanceof Boolean)
			return toNumber((Boolean) obj);
		else
			return obj;
	}
}
