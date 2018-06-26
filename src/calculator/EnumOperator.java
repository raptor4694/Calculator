package calculator;

import static calculator.Functions.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;

public enum EnumOperator implements Function {
	ADD ("+", 2),
	SUBTRACT ("-", 2),
	MULTIPLY ("*", 2),
	DIVIDE ("/", 2),
	MOD ("mod", 2),
	NEGATE ("-", 1),
	NOT ("!", 1),
	LT ("lesser", "<", 2),
	GT ("greater", ">", 2),
	LE ("lesser_equal", "<=", 2),
	GE ("greater_equal", ">=", 2),
	EQ ("equals", "==", 2) {
		@Override
		public Object call(boolean elementWise, Object... args) {
			try {
				return super.call(elementWise, args);
			} catch (TypeError te) {
				if (!elementWise && te.getMessage().equals(
						"data type: for function " + getName())) {
					return Real.ZERO;
				} else
					throw te;
			} catch (DimensionError de) {
				if (!elementWise && args.length == 2)
					return Real.ZERO;
				throw de;
			}
		}
	},
	NE ("not_equals", "!=", 2) {
		@Override
		public Object call(boolean elementWise, Object... args) {
			try {
				return super.call(elementWise, args);
			} catch (TypeError te) {
				if (!elementWise && te.getMessage().equals(
						"data type: for function " + getName())) {
					return Real.ZERO;
				} else
					throw te;
			} catch (DimensionError de) {
				if (!elementWise && args.length == 2)
					return Real.ZERO;
				throw de;
			}
		}
	},
	AND ("&", 2),
	OR ("|", 2),
	POW ("^", 2),
	CROSS ("><", 2),
	PERCENT ("%", 1) {
		@Override
		public boolean isPostfix() {
			return true;
		}
	},
	SCIENTIFIC_NOTATION ("e", "E", 2),
	CARDINALITY ("#", 1),
	FACTORIAL ("!", 1) {
		@Override
		public boolean isPostfix() {
			return true;
		}
	};
	
	static Map<String, EnumOperator> byLowerCase;
	static final EnumOperator[] VALUES;
	
	static {
		byLowerCase = new HashMap<>();
		for (EnumOperator oper : VALUES = values()) {
			byLowerCase.put(oper.getName(), oper);
		}
	}
	
	private final MethodFunction function;
	@Getter(AccessLevel.PACKAGE)
	private final String symbol;
	public final int cardinality;
	
	private EnumOperator(String symbol, int cardinality) {
		if (cardinality <= 0)
			throw new IllegalArgumentException("cardinality");
		function = new MethodFunction(Operators.class, toString().toLowerCase());
		this.symbol = symbol;
		this.cardinality = cardinality;
	}
	
	private EnumOperator(String s, String symbol, int cardinality) {
		if (cardinality <= 0)
			throw new IllegalArgumentException("cardinality");
		function = new MethodFunction(Operators.class, s);
		this.symbol = symbol;
		this.cardinality = cardinality;
	}
	
	public boolean isPostfix() {
		if (cardinality != 1)
			throw new UnsupportedOperationException();
		return false;
	}
	
	@Override
	public String getDescription() {
		return function.getDescription();
	}
	
	@Override
	public boolean returnsValue() {
		return function.returnsValue();
	}
	
	String getVerb() {
		switch (this) {
		case ADD:
			return "TO";
		case SUBTRACT:
			return "FROM";
		case MULTIPLY:
		case DIVIDE:
			return "BY";
		case AND:
		case OR:
		case MOD:
		case POW:
			return "WITH";
		default:
			return "->";
		}
	}
	
	@Override
	public Object call(Scope scope, Object... args) {
		return call(false, args);
	}
	
	@SneakyThrows
	public Object call(boolean elementWise, Object... args) {
		if (args.length != cardinality)
			throw new DimensionError();
		if (args.length == 2) {
			Method method;
			if (elementWise || (method =
					MethodFunction.dispatch(function.methods, args)) == null) {
				method = MethodFunction.dispatch(function.methods, Real.ZERO,
						Real.ZERO);
				if (method != null) {
					if (args[0] instanceof Number[]) {
						Number[] array = (Number[]) args[0];
						check(array.length > 0, DimensionError.class);
						
						if (args[1] instanceof Number) {
							Number value = (Number) args[1];
							Number[] result = new Number[array.length];
							
							for (int i = 0; i < result.length; i++)
								result[i] = toNumber(
										method.invoke(null, array[i], value));
							return result;
						} else if (args[1] instanceof Number[]) {
							Number[] array2 = (Number[]) args[1];
							check(array.length == array2.length,
									DimensionError.class);
							
							Number[] result = new Number[array.length];
							
							for (int i = 0; i < array.length; i++)
								result[i] = toNumber(
										method.invoke(null, array[i], array2[i]));
							
							return result;
						} else if (args[1] instanceof Number[][]) {
							Number[][] matrix = (Number[][]) args[1];
							check(array.length == matrix[0].length,
									DimensionError.class);
							
							Number[][] result = new Number[rowCount(
									matrix)][columnCount(matrix)];
							
							for (int r = 0; r < rowCount(matrix); r++) {
								for (int c = 0; c < columnCount(matrix); c++) {
									result[r][c] = toNumber(method.invoke(null,
											array[c], matrix[r][c]));
								}
							}
							return result;
						}
					} else if (args[0] instanceof Number) {
						Number value = (Number) args[0];
						if (args[1] instanceof Number[]) {
							Number[] array = (Number[]) args[1];
							check(array.length > 0, DimensionError.class);
							
							Number[] result = new Number[array.length];
							
							for (int i = 0; i < result.length; i++) {
								try {
									result[i] = toNumber(
											method.invoke(null, value, array[i]));
								} catch (IllegalArgumentException e) {
									if (e.getMessage().equals(
											"argument type mismatch")) {
										throw new TypeError(e);
									}
								}
							}
							return result;
						} else if (args[1] instanceof Number[][]) {
							Number[][] matrix = (Number[][]) args[1];
							check(rowCount(matrix) > 0 && columnCount(matrix) > 0,
									DimensionError.class);
							
							Number[][] result = new Number[rowCount(
									matrix)][columnCount(matrix)];
							
							for (int r = 0; r < rowCount(matrix); r++) {
								for (int c = 0; c < columnCount(matrix); c++) {
									result[r][c] = toNumber(method.invoke(null,
											value, matrix[r][c]));
								}
							}
							return result;
						} else if (args[1] instanceof Number) {
							return method.invoke(null, args);
						}
					} else if (args[0] instanceof Number[][]) {
						Number[][] matrix = (Number[][]) args[0];
						check(rowCount(matrix) > 0 && columnCount(matrix) > 0,
								DimensionError.class);
						
						if (args[1] instanceof Number) {
							Number value = (Number) args[1];
							
							Number[][] result = new Number[rowCount(
									matrix)][columnCount(matrix)];
							
							for (int r = 0; r < rowCount(matrix); r++) {
								for (int c = 0; c < columnCount(matrix); c++) {
									result[r][c] = toNumber(method.invoke(null,
											matrix[r][c], value));
								}
							}
							return result;
						} else if (args[1] instanceof Number[]) {
							Number[] array = (Number[]) args[1];
							check(array.length == matrix[0].length,
									DimensionError.class);
							
							Number[][] result = new Number[rowCount(
									matrix)][columnCount(matrix)];
							
							for (int r = 0; r < rowCount(matrix); r++) {
								for (int c = 0; c < columnCount(matrix); c++) {
									result[r][c] = toNumber(method.invoke(null,
											matrix[r][c], array[c]));
								}
							}
							return result;
						} else if (args[1] instanceof Number[][]) {
							Number[][] matrix2 = (Number[][]) args[1];
							check(rowCount(matrix) == rowCount(matrix2)
									&& columnCount(matrix) == columnCount(matrix2),
									DimensionError.class);
							
							Number[][] result = new Number[rowCount(
									matrix)][columnCount(matrix)];
							
							for (int r = 0; r < rowCount(matrix); r++) {
								for (int c = 0; c < columnCount(matrix); c++) {
									result[r][c] = toNumber(method.invoke(null,
											matrix[r][c], matrix2[r][c]));
								}
							}
							return result;
						}
					}
				}
				throw new TypeError("for function " + getName());
			} else {
				try {
					return method.invoke(null, args);
				} catch (InvocationTargetException ex) {
					throw ex.getCause();
				}
			}
		} else if (args.length == 1) {
			Method method;
			if (elementWise || (method =
					MethodFunction.dispatch(function.methods, args)) == null) {
				method = MethodFunction.dispatch(function.methods, 0.0);
				if (method != null) {
					if (args[0] instanceof Number[]) {
						Number[] array = (Number[]) args[0];
						check(array.length > 0, DimensionError.class);
						
						Number[] result = new Number[array.length];
						
						for (int i = 0; i < array.length; i++)
							result[i] = toNumber(method.invoke(null, array[i]));
						
						return result;
					} else if (args[0] instanceof Number[][]) {
						Number[][] matrix = (Number[][]) args[0];
						check(rowCount(matrix) > 0 && columnCount(matrix) > 0,
								DimensionError.class);
						
						Number[][] result =
								new Number[rowCount(matrix)][columnCount(matrix)];
						
						for (int r = 0; r < rowCount(matrix); r++) {
							for (int c = 0; c < columnCount(matrix); c++) {
								result[r][c] =
										toNumber(method.invoke(null, matrix[r][c]));
							}
						}
						return result;
					}
				}
				throw new TypeError("for function " + getName());
			} else {
				try {
					return method.invoke(null, args);
				} catch (InvocationTargetException e) {
					throw e.getCause();
				}
			}
		} else {
			throw new DimensionError();
		}
	}
	
	@Override
	public String getName() {
		return function.getName();
	}
	
	@Override
	public int maxArgCount() {
		return function.maxArgCount();
	}
	
	@Override
	public int minArgCount() {
		return function.minArgCount();
	}
}
