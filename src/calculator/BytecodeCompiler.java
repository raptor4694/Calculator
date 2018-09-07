package calculator;

import static calculator.Bytecode.*;

import java.nio.ByteBuffer;

import calculator.expressions.Expression;
import calculator.expressions.ExpressionAbs;
import calculator.expressions.ExpressionArrayLiteral;
import calculator.expressions.ExpressionAssign;
import calculator.expressions.ExpressionAssignOp;
import calculator.expressions.ExpressionBinaryOperator;
import calculator.expressions.ExpressionBreak;
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
import calculator.expressions.ExpressionIndexAssign;
import calculator.expressions.ExpressionIndexAssignOp;
import calculator.expressions.ExpressionIndexPostfixDecrement;
import calculator.expressions.ExpressionIndexPostfixIncrement;
import calculator.expressions.ExpressionIndexPrefixDecrement;
import calculator.expressions.ExpressionIndexPrefixIncrement;
import calculator.expressions.ExpressionLiteral;
import calculator.expressions.ExpressionLocal;
import calculator.expressions.ExpressionLocal.DefImpl;
import calculator.expressions.ExpressionLocal.DimDef;
import calculator.expressions.ExpressionLocal.FuncDef;
import calculator.expressions.ExpressionMatrixIndex;
import calculator.expressions.ExpressionMatrixIndexAssign;
import calculator.expressions.ExpressionMatrixIndexAssignOp;
import calculator.expressions.ExpressionMatrixIndexPostfixDecrement;
import calculator.expressions.ExpressionMatrixIndexPostfixIncrement;
import calculator.expressions.ExpressionMatrixIndexPrefixDecrement;
import calculator.expressions.ExpressionMatrixIndexPrefixIncrement;
import calculator.expressions.ExpressionMulti;
import calculator.expressions.ExpressionMultiplyChain;
import calculator.expressions.ExpressionParenthesis;
import calculator.expressions.ExpressionReturn;
import calculator.expressions.ExpressionTry;
import calculator.expressions.ExpressionUnaryOperator;
import calculator.expressions.ExpressionVarPostfixDecrement;
import calculator.expressions.ExpressionVarPostfixIncrement;
import calculator.expressions.ExpressionVarPrefixDecrement;
import calculator.expressions.ExpressionVarPrefixIncrement;
import calculator.expressions.ExpressionVariable;
import calculator.expressions.ExpressionWhile;
import calculator.expressions.ExpressionX;
import calculator.expressions.ExpressionXAssign;
import calculator.expressions.ExpressionXAssignOp;
import calculator.expressions.ExpressionXPostfixDecrement;
import calculator.expressions.ExpressionXPostfixIncrement;
import calculator.expressions.ExpressionXPrefixDecrement;
import calculator.expressions.ExpressionXPrefixIncrement;
import calculator.expressions.ExpressionY;
import calculator.expressions.ExpressionYAssign;
import calculator.expressions.ExpressionYAssignOp;
import calculator.expressions.ExpressionYPostfixDecrement;
import calculator.expressions.ExpressionYPostfixIncrement;
import calculator.expressions.ExpressionYPrefixDecrement;
import calculator.expressions.ExpressionYPrefixIncrement;
import calculator.expressions.ExpressionZ;
import calculator.expressions.ExpressionZAssign;
import calculator.expressions.ExpressionZAssignOp;
import calculator.expressions.ExpressionZPostfixDecrement;
import calculator.expressions.ExpressionZPostfixIncrement;
import calculator.expressions.ExpressionZPrefixDecrement;
import calculator.expressions.ExpressionZPrefixIncrement;
import calculator.values.Complex;
import calculator.values.EnumOperator;
import calculator.values.MethodFunction;
import calculator.values.Number;
import calculator.values.Real;
import calculator.values.UserFunction;
import lombok.SneakyThrows;

public class BytecodeCompiler implements Visitor {
	public static byte[] compile(Expression expr) {
		return new BytecodeCompiler(expr).getBytes();
	}
	
	private ByteBuffer buf;
	
	private BytecodeCompiler(Expression expr) {
		buf = ByteBuffer.allocate(100000);
		visitExpression(expr);
	}
	
	private byte[] getBytes() {
		byte[] b = buf.array();
		byte[] result = new byte[buf.position()];
		System.arraycopy(b, 0, result, 0, result.length);
		return result;
	}
	
	private void putBool(boolean b) {
		buf.put((byte) (b? 1 : 0));
	}
	
	private void putString(String s) {
		buf.put(s.getBytes());
		buf.put((byte) 0);
	}
	
	private void put(byte b) {
		buf.put(b);
	}
	
	/*private void put(byte[] bytes) {
		buf.put(bytes);
	}*/
	
	private void putDouble(double d) {
		if (d == (int) d) {
			int i = (int) d;
			if (i == 0) {
				put(ZERO);
			} else if (i == 1) {
				put(ONE);
			} else if (i == (byte) i) {
				put(BYTE);
				put((byte) i);
			} else if (i == (short) i) {
				put(SHORT);
				buf.putShort((short) i);
			} else {
				put(INT);
				putInt(i);
			}
		} else if (d == Math.E) {
			put(E);
		} else if (d == Math.PI) {
			put(PI);
		} else {
			put(DOUBLE);
			buf.putDouble(d);
		}
	}
	
	private void putInt(int i) {
		buf.putInt(i);
	}
	
	private void putOperator(EnumOperator operator) {
		buf.put((byte) operator.ordinal());
	}
	
	public void visitExpressions(Expression[] exprs) {
		for (Expression e : exprs) {
			visitExpression(e);
		}
	}
	
	@Override
	public void visitExpression(Expression expr) {
		if (expr == null) {
			put(END);
		} else
			expr.accept(this);
	}
	
	@Override
	public void visitAbs(ExpressionAbs expr) {
		visitCall(expr);
	}
	
	@Override
	public void visitArrayLiteral(ExpressionArrayLiteral expr) {
		put(ARRAYLITERAL);
		putBool(false);
		visitExpressions(expr.elems);
		put(END);
	}
	
	@Override
	public void visitArrayLiteral(ExpressionColumnLiteral expr) {
		put(ARRAYLITERAL);
		putBool(true);
		visitExpressions(expr.exprs);
		put(END);
	}
	
	@Override
	public void visitBinary(ExpressionBinaryOperator expr) {
		put(BINARY);
		putBool(expr instanceof ExpressionElementwiseBinaryOperator);
		putOperator(expr.operator);
		visitExpression(expr.lhs);
		visitExpression(expr.rhs);
	}
	
	@Override
	public void visitBreak(ExpressionBreak expr) {
		put(BREAK);
	}
	
	@Override
	public void visitIf(ExpressionIf expr) {
		put(IF);
		putBool(expr instanceof ExpressionConditional);
		visitExpression(expr.condition);
		visitExpression(expr.thenpart);
		visitExpression(expr.elsepart);
	}
	
	@Override
	public void visitComparisonChain(ExpressionComparisonChain expr) {
		put(COMPARISON_CHAIN);
		putInt(expr.operators.size());
		for (ExpressionComparisonChain.Op op : expr.operators) {
			putBool(op.elementWise);
			putOperator(op.operator);
		}
		expr.exprs.forEach(this::visitExpression);
	}
	
	@Override
	public void visitDelete(ExpressionDeleteVariable expr) {
		put(DELETE);
		putString(expr.variable);
	}
	
	@Override
	public void visitDimAssign(ExpressionDimAssign expr) {
		put(SETDIM);
		putString(expr.variable);
		visitExpression(expr.value);
	}
	
	@Override
	public void visitDollar(ExpressionDollar expr) {
		put(DOLLAR);
	}
	
	@Override
	public void visitFor(ExpressionFor expr) {
		put(FOR);
		putString(expr.variable);
		visitExpression(expr.start);
		visitExpression(expr.end);
		visitExpression(expr.increment);
		visitExpression(expr.body);
	}
	
	@Override
	public void visitFor(ExpressionForEachSingle expr) {
		put(FOREACH);
		putString(expr.variable);
		visitExpression(expr.array);
		visitExpression(expr.body);
	}
	
	@Override
	public void visitFor(ExpressionForEachDouble expr) {
		put(FOREACH2);
		putString(expr.variable1);
		putString(expr.variable2);
		visitExpression(expr.array);
		visitExpression(expr.body);
	}
	
	@Override
	public void visitCall(ExpressionFunctionCall expr) {
		put(CALL);
		visitExpression(expr.function);
		visitExpressions(expr.args);
		put(END);
	}
	
	@Override
	public void visitFunctionDef(ExpressionFunctionDefinition expr) {
		put(FUNCDEF);
		UserFunction func = expr.function;
		if (func.getName() == null)
			put(END);
		else
			putString(func.getName());
		for (String arg : func.varnames) {
			putString(arg);
		}
		put(END);
		visitExpression(func.body);
	}
	
	@Override
	public void visitIndex(ExpressionIndex expr) {
		put(INDEX);
		visitExpression(expr.array);
		visitExpression(expr.index);
	}
	
	@Override
	public void visitIndex(ExpressionMatrixIndex expr) {
		put(INDEX2);
		visitExpression(expr.matrix);
		visitExpression(expr.row);
		visitExpression(expr.column);
	}
	
	@SneakyThrows
	@Override
	public void visitLiteral(ExpressionLiteral expr) {
		Object obj = expr.value.get();
		if (obj instanceof Number[] && ((Number[]) obj).length == 0) {
			put(ARRAYLITERAL);
			put(END);
		} else if (obj instanceof MethodFunction) {
			MethodFunction func = (MethodFunction) obj;
			put(METHOD);
			putString(func.getDeclaringClass().getName());
			putString(func.getName());
		} else if (obj instanceof EnumOperator) {
			put(OPERATOR);
			putOperator((EnumOperator) obj);
		} else if (obj instanceof Real) {
			double d = ((Real) obj).doubleValue();
			putDouble(d);
		} else if (obj instanceof Complex) {
			Complex c = (Complex) obj;
			if (c == Complex.I)
				put(I);
			else if (c.real == 0) {
				put(IMAGINARY);
				putDouble(c.imag);
			} else {
				put(COMPLEX);
				putDouble(c.real);
				putDouble(c.imag);
			}
		} else if (obj instanceof String) {
			put(STRING);
			putString((String) obj);
		} else
			throw new BytecodeException(
					"Cannot convert literal " + obj + " to bytecode");
	}
	
	@Override
	public void visitLocal(ExpressionLocal expr) {
		put(LOCAL);
		for (ExpressionLocal.Def def : expr.defs) {
			def.accept(this);
		}
		put(END);
	}
	
	@Override
	public void visitMultiplyChain(ExpressionMultiplyChain expr) {
		put(MULTIPLY_CHAIN);
		expr.exprs.forEach(this::visitExpression);
		put(END);
	}
	
	@Override
	public void visitMulti(ExpressionMulti expr) {
		put(MULTI);
		visitExpressions(expr.exprs);
		put(END);
	}
	
	@Override
	public void visitParens(ExpressionParenthesis expr) {
		visitExpression(expr.expr);
	}
	
	@Override
	public void visitReturn(ExpressionReturn expr) {
		put(RETURN);
		visitExpression(expr.value);
	}
	
	@Override
	public void visitTry(ExpressionTry expr) {
		put(TRY);
		visitExpression(expr.body);
		visitExpression(expr.elsepart);
	}
	
	@Override
	public void visitUnary(ExpressionUnaryOperator expr) {
		put(UNARY);
		putOperator(expr.operator);
		visitExpression(expr.expr);
	}
	
	@Override
	public void visitVariable(ExpressionVariable expr) {
		put(VARIABLE);
		putString(expr.variable);
	}
	
	@Override
	public void visitWhile(ExpressionWhile expr) {
		put(WHILE);
		visitExpression(expr.condition);
		visitExpression(expr.body);
	}
	
	@Override
	public void visitDef(DefImpl expr) {
		putString(expr.name);
		if (expr.value == null)
			put(END);
		else {
			put(OPCODES_COUNT);
			visitExpression(expr.value);
		}
	}
	
	@Override
	public void visitDef(DimDef expr) {
		putString(expr.variable);
		put(SETDIM);
		visitExpression(expr.value);
	}
	
	@Override
	public void visitDef(FuncDef expr) {
		putString(expr.function.getName());
		put(FUNCDEF);
		for (String arg : expr.function.varnames) {
			putString(arg);
		}
		put(END);
		visitExpression(expr.function.body);
	}
	
	@Override
	public void visitVectorX(ExpressionX expr) {
		put(VECTOR_X);
		visitExpression(expr.array);
	}
	
	@Override
	public void visitVectorY(ExpressionY expr) {
		put(VECTOR_Y);
		visitExpression(expr.array);
	}
	
	@Override
	public void visitVectorZ(ExpressionZ expr) {
		put(VECTOR_Z);
		visitExpression(expr.array);
	}
	
	@Override
	public void visitAssign(ExpressionAssign expr) {
		put(ASSIGN);
		put(VARIABLE);
		putString(expr.variable);
		visitExpression(expr.expr);
	}
	
	@Override
	public void visitAssign(ExpressionIndexAssign expr) {
		put(ASSIGN);
		put(INDEX);
		visitExpression(expr.array);
		visitExpression(expr.index);
		visitExpression(expr.value);
	}
	
	@Override
	public void visitAssign(ExpressionMatrixIndexAssign expr) {
		put(ASSIGN);
		put(INDEX2);
		visitExpression(expr.matrix);
		visitExpression(expr.row);
		visitExpression(expr.column);
		visitExpression(expr.value);
	}
	
	@Override
	public void visitAssign(ExpressionXAssign expr) {
		put(ASSIGN);
		put(VECTOR_X);
		visitExpression(expr.array);
		visitExpression(expr.value);
	}
	
	@Override
	public void visitAssign(ExpressionYAssign expr) {
		put(ASSIGN);
		put(VECTOR_Y);
		visitExpression(expr.array);
		visitExpression(expr.value);
	}
	
	@Override
	public void visitAssign(ExpressionZAssign expr) {
		put(ASSIGN);
		put(VECTOR_Z);
		visitExpression(expr.array);
		visitExpression(expr.value);
	}
	
	@Override
	public void visitAssignOp(ExpressionAssignOp expr) {
		put(ASSIGNOP);
		putOperator(expr.operator);
		put(VARIABLE);
		putString(expr.variable);
		visitExpression(expr.expr);
	}
	
	@Override
	public void visitAssignOp(ExpressionIndexAssignOp expr) {
		put(ASSIGNOP);
		putOperator(expr.operator);
		put(INDEX);
		visitExpression(expr.array);
		visitExpression(expr.index);
		visitExpression(expr.value);
	}
	
	@Override
	public void visitAssignOp(ExpressionMatrixIndexAssignOp expr) {
		put(ASSIGNOP);
		putOperator(expr.operator);
		put(INDEX2);
		visitExpression(expr.matrix);
		visitExpression(expr.row);
		visitExpression(expr.column);
		visitExpression(expr.value);
	}
	
	@Override
	public void visitAssignOp(ExpressionXAssignOp expr) {
		put(ASSIGNOP);
		putOperator(expr.operator);
		put(VECTOR_X);
		visitExpression(expr.array);
		visitExpression(expr.value);
	}
	
	@Override
	public void visitAssignOp(ExpressionYAssignOp expr) {
		put(ASSIGNOP);
		putOperator(expr.operator);
		put(VECTOR_Y);
		visitExpression(expr.array);
		visitExpression(expr.value);
	}
	
	@Override
	public void visitAssignOp(ExpressionZAssignOp expr) {
		put(ASSIGNOP);
		putOperator(expr.operator);
		put(VECTOR_Z);
		visitExpression(expr.array);
		visitExpression(expr.value);
	}
	
	@Override
	public void visitPrefixIncrement(ExpressionVarPrefixIncrement expr) {
		put(PREFIX_INC);
		put(VARIABLE);
		putString(expr.variable);
	}
	
	@Override
	public void visitPrefixIncrement(ExpressionIndexPrefixIncrement expr) {
		put(PREFIX_INC);
		put(INDEX);
		visitExpression(expr.array);
		visitExpression(expr.index);
	}
	
	@Override
	public void visitPrefixIncrement(ExpressionMatrixIndexPrefixIncrement expr) {
		put(PREFIX_INC);
		put(INDEX2);
		visitExpression(expr.matrix);
		visitExpression(expr.row);
		visitExpression(expr.column);
	}
	
	@Override
	public void visitPrefixIncrement(ExpressionXPrefixIncrement expr) {
		put(PREFIX_INC);
		put(VECTOR_X);
		visitExpression(expr.array);
	}
	
	@Override
	public void visitPrefixIncrement(ExpressionYPrefixIncrement expr) {
		put(PREFIX_INC);
		put(VECTOR_Y);
		visitExpression(expr.array);
	}
	
	@Override
	public void visitPrefixIncrement(ExpressionZPrefixIncrement expr) {
		put(PREFIX_INC);
		put(VECTOR_Z);
		visitExpression(expr.array);
	}
	
	@Override
	public void visitPrefixDecrement(ExpressionVarPrefixDecrement expr) {
		put(PREFIX_DEC);
		put(VARIABLE);
		putString(expr.variable);
	}
	
	@Override
	public void visitPrefixDecrement(ExpressionIndexPrefixDecrement expr) {
		put(PREFIX_DEC);
		put(INDEX);
		visitExpression(expr.array);
		visitExpression(expr.index);
	}
	
	@Override
	public void visitPrefixDecrement(ExpressionMatrixIndexPrefixDecrement expr) {
		put(PREFIX_DEC);
		put(INDEX2);
		visitExpression(expr.matrix);
		visitExpression(expr.row);
		visitExpression(expr.column);
	}
	
	@Override
	public void visitPrefixDecrement(ExpressionXPrefixDecrement expr) {
		put(PREFIX_DEC);
		put(VECTOR_X);
		visitExpression(expr.array);
	}
	
	@Override
	public void visitPrefixDecrement(ExpressionYPrefixDecrement expr) {
		put(PREFIX_DEC);
		put(VECTOR_Y);
		visitExpression(expr.array);
	}
	
	@Override
	public void visitPrefixDecrement(ExpressionZPrefixDecrement expr) {
		put(PREFIX_DEC);
		put(VECTOR_Z);
		visitExpression(expr.array);
	}
	
	@Override
	public void visitPostfixIncrement(ExpressionVarPostfixIncrement expr) {
		put(POSTFIX_INC);
		put(VARIABLE);
		putString(expr.variable);
	}
	
	@Override
	public void visitPostfixIncrement(ExpressionIndexPostfixIncrement expr) {
		put(POSTFIX_INC);
		put(INDEX);
		visitExpression(expr.array);
		visitExpression(expr.index);
	}
	
	@Override
	public void visitPostfixIncrement(ExpressionMatrixIndexPostfixIncrement expr) {
		put(POSTFIX_INC);
		put(INDEX2);
		visitExpression(expr.matrix);
		visitExpression(expr.row);
		visitExpression(expr.column);
	}
	
	@Override
	public void visitPostfixIncrement(ExpressionXPostfixIncrement expr) {
		put(POSTFIX_INC);
		put(VECTOR_X);
		visitExpression(expr.array);
	}
	
	@Override
	public void visitPostfixIncrement(ExpressionYPostfixIncrement expr) {
		put(POSTFIX_INC);
		put(VECTOR_Y);
		visitExpression(expr.array);
	}
	
	@Override
	public void visitPostfixIncrement(ExpressionZPostfixIncrement expr) {
		put(POSTFIX_INC);
		put(VECTOR_Z);
		visitExpression(expr.array);
	}
	
	@Override
	public void visitPostfixDecrement(ExpressionVarPostfixDecrement expr) {
		put(POSTFIX_DEC);
		put(VARIABLE);
		putString(expr.variable);
	}
	
	@Override
	public void visitPostfixDecrement(ExpressionIndexPostfixDecrement expr) {
		put(POSTFIX_DEC);
		put(INDEX);
		visitExpression(expr.array);
		visitExpression(expr.index);
	}
	
	@Override
	public void visitPostfixDecrement(ExpressionMatrixIndexPostfixDecrement expr) {
		put(POSTFIX_DEC);
		put(INDEX2);
		visitExpression(expr.matrix);
		visitExpression(expr.row);
		visitExpression(expr.column);
	}
	
	@Override
	public void visitPostfixDecrement(ExpressionXPostfixDecrement expr) {
		put(POSTFIX_DEC);
		put(VECTOR_X);
		visitExpression(expr.array);
	}
	
	@Override
	public void visitPostfixDecrement(ExpressionYPostfixDecrement expr) {
		put(POSTFIX_DEC);
		put(VECTOR_Y);
		visitExpression(expr.array);
	}
	
	@Override
	public void visitPostfixDecrement(ExpressionZPostfixDecrement expr) {
		put(POSTFIX_DEC);
		put(VECTOR_Z);
		visitExpression(expr.array);
	}
	
	@Override
	public void visitDeleteAll(ExpressionDeleteAll expr) {
		put(DELALL);
		putBool(true);
	}
	
	@Override
	public void visitDeleteLocal(ExpressionDeleteLocal expr) {
		put(DELALL);
		putBool(false);
	}
}
