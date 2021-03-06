package calculator;

import static calculator.Bytecode.*;

import java.io.ByteArrayOutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import calculator.errors.BytecodeException;
import calculator.expressions.Expression;
import calculator.expressions.ExpressionArrayLiteral;
import calculator.expressions.ExpressionBinaryOperator;
import calculator.expressions.ExpressionColumnLiteral;
import calculator.expressions.ExpressionComparisonChain;
import calculator.expressions.ExpressionConditional;
import calculator.expressions.ExpressionDeleteAll;
import calculator.expressions.ExpressionDeleteLocal;
import calculator.expressions.ExpressionDeleteVariable;
import calculator.expressions.ExpressionDimAssign;
import calculator.expressions.ExpressionDollar;
import calculator.expressions.ExpressionElementwiseBinaryOperator;
import calculator.expressions.ExpressionFor;
import calculator.expressions.ExpressionForEachDouble;
import calculator.expressions.ExpressionForEachSingle;
import calculator.expressions.ExpressionFunctionCall;
import calculator.expressions.ExpressionFunctionDefinition;
import calculator.expressions.ExpressionIf;
import calculator.expressions.ExpressionIndex;
import calculator.expressions.ExpressionLiteral;
import calculator.expressions.ExpressionLocal;
import calculator.expressions.ExpressionMatrixIndex;
import calculator.expressions.ExpressionMulti;
import calculator.expressions.ExpressionMultiplyChain;
import calculator.expressions.ExpressionReferenceable;
import calculator.expressions.ExpressionReturn;
import calculator.expressions.ExpressionTry;
import calculator.expressions.ExpressionUnaryOperator;
import calculator.expressions.ExpressionVariable;
import calculator.expressions.ExpressionWhile;
import calculator.expressions.ExpressionX;
import calculator.expressions.ExpressionY;
import calculator.expressions.ExpressionZ;
import calculator.values.Complex;
import calculator.values.EnumOperator;
import calculator.values.MethodFunction;
import calculator.values.Number;
import calculator.values.Real;
import calculator.values.UserFunction;
import lombok.SneakyThrows;

public class BytecodeParser {
	public static Expression parse(byte[] bytes) throws BytecodeException {
		return new BytecodeParser(bytes).parse();
	}
	
	private ByteBuffer buf;
	private String[] variables;
	
	private BytecodeParser(byte[] bytes) {
		buf = ByteBuffer.wrap(bytes);
	}
	
	/*private void next() throws BytecodeException {
		if (pos == bytes.length - 1) {
			b = -1;
			pos = bytes.length;
		} else if (pos == bytes.length) {
			throw new BytecodeException("Unexpected EOF");
		} else
			b = bytes[++pos];
	}*/
	
	public Expression parse() throws BytecodeException {
		variables = new String[buf.getInt()];
		for (int i = 0; i < variables.length; i++) {
			variables[i] = string();
		}
		Expression result = expr();
		/*if (buf.remaining() > 0)
			throw new BytecodeException(
					"Expected EOF at offset " + buf.position());*/
		return result;
	}
	
	@SneakyThrows(ClassNotFoundException.class)
	private Expression expr() throws BytecodeException {
		buf.mark();
		switch (buf.get()) {
		case END:
			return null;
		case ARRAYLITERAL: {
			boolean columnLiteral = bool();
			List<Expression> exprs = newList();
			Expression e = expr();
			while (e != null) {
				exprs.add(e);
				e = expr();
			}
			if (exprs.isEmpty())
				return new ExpressionLiteral(() -> new Number[0]);
			return columnLiteral? new ExpressionColumnLiteral(toArray(exprs))
					: new ExpressionArrayLiteral(toArray(exprs));
		}
		case UNARY:
			return new ExpressionUnaryOperator(operator(), expr());
		case BINARY: {
			boolean elementWise = bool();
			EnumOperator oper = operator();
			Expression lhs = expr(), rhs = expr();
			return elementWise
					? new ExpressionElementwiseBinaryOperator(lhs, oper, rhs)
					: new ExpressionBinaryOperator(lhs, oper, rhs);
		}
		case IF: {
			boolean conditional = bool();
			Expression cond = expr(), then = expr(), other = expr();
			return conditional? new ExpressionConditional(cond, then, other)
					: new ExpressionIf(cond, then, other);
		}
		case WHILE:
			buf.get();
			return new ExpressionWhile(expr(), expr());
		case FOR:
			return new ExpressionFor(variable(), expr(), expr(), expr(), expr());
		case FOREACH:
			return new ExpressionForEachSingle(variable(), expr(), expr());
		case FOREACH2:
			return new ExpressionForEachDouble(variable(), variable(), expr(),
					expr());
		case CALL: {
			Expression func = expr();
			List<Expression> args = newList();
			Expression e = expr();
			while (e != null) {
				args.add(e);
				e = expr();
			}
			return new ExpressionFunctionCall(func, toArray(args));
		}
		case FUNCDEF: {
			String name;
			int i = buf.getInt();
			if (i == 0)
				name = null;
			else
				name = variables[i - 1];
			String[] args = new String[buf.getInt()];
			for (i = 0; i < args.length; i++) {
				args[i] = variable();
			}
			return new ExpressionFunctionDefinition(
					new UserFunction(name, args, expr()));
		}
		case METHOD: {
			Class<?> clazz = Class.forName(string());
			String name = string();
			return new ExpressionLiteral(new MethodFunction(clazz, name));
		}
		case OPERATOR:
			return new ExpressionLiteral(operator());
		case SETDIM:
			return new ExpressionDimAssign(variable(), expr());
		case DELETE:
			return new ExpressionDeleteVariable(variable());
		case DELALL:
			return bool()? new ExpressionDeleteAll() : new ExpressionDeleteLocal();
		case DOLLAR:
			return new ExpressionDollar();
		case DOUBLE:
		case INT:
		case SHORT:
		case BYTE:
		case ZERO:
		case ONE:
		case E:
		case PI:
			buf.reset();
			return new ExpressionLiteral(Real.valueOf(number()));
		case COMPLEX:
			return new ExpressionLiteral(Complex.valueOf(number(), number()));
		case IMAGINARY:
			return new ExpressionLiteral(Complex.valueOf(0, number()));
		case I:
			return new ExpressionLiteral(Complex.I);
		case STRING:
			return new ExpressionLiteral(string());
		case LOCAL: {
			List<ExpressionLocal.Def> defs = newList();
			byte b;
			do {
				String name = variable();
				switch (b = buf.get()) {
				case END:
					defs.add(new ExpressionLocal.DefImpl(name));
					break;
				case SETDIM:
					defs.add(new ExpressionLocal.DimDef(name, expr()));
					break;
				case ASSIGN:
					defs.add(new ExpressionLocal.DefImpl(name, expr()));
					break;
				case FUNCDEF: {
					String[] args = new String[buf.getInt()];
					for (int i = 0; i < args.length; i++) {
						args[i] = variable();
					}
					defs.add(new ExpressionLocal.FuncDef(
							new UserFunction(name, args, expr())));
					break;
				}
				default:
					throw new BytecodeException(buf.position() - 1);
				}
				buf.mark();
				b = buf.get();
				buf.reset();
			} while (b != 0);
			buf.get();
			return new ExpressionLocal(defs.toArray(new ExpressionLocal.Def[0]));
		}
		case INDEX:
			return new ExpressionIndex(expr(), expr());
		case INDEX2:
			return new ExpressionMatrixIndex(expr(), expr(), expr());
		case MULTI: {
			List<Expression> exprs = newList();
			Expression e = expr();
			while (e != null) {
				exprs.add(e);
				e = expr();
			}
			return new ExpressionMulti(toArray(exprs));
		}
		case MULTIPLY_CHAIN: {
			ArrayList<Expression> exprs = newList();
			Expression e = expr();
			while (e != null) {
				exprs.add(e);
				e = expr();
			}
			return new ExpressionMultiplyChain(exprs);
		}
		case RETURN:
			return new ExpressionReturn(expr());
		case TRY:
			return new ExpressionTry(expr(), expr());
		case VARIABLE:
			return new ExpressionVariable(variable());
		case ASSIGN:
			return ((ExpressionReferenceable) expr()).toAssign(expr());
		case ASSIGNOP: {
			EnumOperator operator = operator();
			return ((ExpressionReferenceable) expr()).toAssignOp(operator, expr());
		}
		case PREFIX_INC:
			return ((ExpressionReferenceable) expr()).toPrefixIncrement();
		case PREFIX_DEC:
			return ((ExpressionReferenceable) expr()).toPrefixDecrement();
		case POSTFIX_INC:
			return ((ExpressionReferenceable) expr()).toPostfixIncrement();
		case POSTFIX_DEC:
			return ((ExpressionReferenceable) expr()).toPostfixDecrement();
		case COMPARISON_CHAIN: {
			int size = buf.getInt();
			ArrayList<Expression> exprs = newList();
			ArrayList<ExpressionComparisonChain.Op> ops = newList();
			for (int i = 0; i < size; i++) {
				boolean elementWise = bool();
				ops.add(ExpressionComparisonChain.Op.of(operator(), elementWise));
			}
			for (int i = 0; i <= size; i++) {
				exprs.add(expr());
			}
			return new ExpressionComparisonChain(exprs, ops);
		}
		case VECTOR_X:
			return new ExpressionX(expr());
		case VECTOR_Y:
			return new ExpressionY(expr());
		case VECTOR_Z:
			return new ExpressionZ(expr());
		default:
			buf.reset();
			throw new BytecodeException(buf.position(), buf.get());
		}
	}
	
	private EnumOperator operator() throws BytecodeException {
		int i = buf.get();
		try {
			return EnumOperator.VALUES[i];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new BytecodeException(buf.position() - 1);
		}
	}
	
	private boolean bool() throws BytecodeException {
		switch (buf.get()) {
		case 0:
			return false;
		case 1:
			return true;
		default:
			throw new BytecodeException(buf.position() - 1);
		}
	}
	
	private String variable() {
		return variables[buf.getInt() - 1];
	}
	
	private String string() throws BytecodeException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			byte b = buf.get();
			while (b != 0) {
				baos.write(b);
				b = buf.get();
			}
		} catch (BufferUnderflowException e) {
			throw new BytecodeException("Reached EOF while parsing string");
		}
		return baos.toString();
	}
	
	private double number() throws BytecodeException {
		switch (buf.get()) {
		case DOUBLE:
			return buf.getDouble();
		case INT:
			return buf.getInt();
		case SHORT:
			return buf.getShort();
		case BYTE:
			return buf.get();
		case ZERO:
			return 0;
		case ONE:
			return 1;
		case E:
			return Math.E;
		case PI:
			return Math.PI;
		default:
			throw new BytecodeException();
		}
	}
	
	private static <T> ArrayList<T> newList() {
		return new ArrayList<>();
	}
	
	private static Expression[] toArray(List<Expression> list) {
		return list.toArray(new Expression[0]);
	}
	
}
