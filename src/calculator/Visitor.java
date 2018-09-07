package calculator;

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

public interface Visitor {
	void visitExpression(Expression expr);
	
	void visitAbs(ExpressionAbs expr);
	
	void visitArrayLiteral(ExpressionArrayLiteral expr);
	
	void visitArrayLiteral(ExpressionColumnLiteral expr);
	
	void visitBinary(ExpressionBinaryOperator expr);
	
	default void visitBinary(ExpressionElementwiseBinaryOperator expr) {
		visitBinary((ExpressionBinaryOperator) expr);
	}
	
	void visitBreak(ExpressionBreak expr);
	
	void visitIf(ExpressionIf expr);
	
	default void visitIf(ExpressionConditional expr) {
		visitIf((ExpressionIf) expr);
	}
	
	void visitComparisonChain(ExpressionComparisonChain expr);
	
	void visitDelete(ExpressionDeleteVariable expr);
	
	void visitDeleteAll(ExpressionDeleteAll expr);
	
	void visitDeleteLocal(ExpressionDeleteLocal expr);
	
	void visitDimAssign(ExpressionDimAssign expr);
	
	void visitDollar(ExpressionDollar expr);
	
	void visitFor(ExpressionFor expr);
	
	void visitFor(ExpressionForEachSingle expr);
	
	void visitFor(ExpressionForEachDouble expr);
	
	void visitCall(ExpressionFunctionCall expr);
	
	void visitFunctionDef(ExpressionFunctionDefinition expr);
	
	void visitIndex(ExpressionIndex expr);
	
	void visitIndex(ExpressionMatrixIndex expr);
	
	void visitLiteral(ExpressionLiteral expr);
	
	void visitLocal(ExpressionLocal expr);
	
	void visitMultiplyChain(ExpressionMultiplyChain expr);
	
	void visitMulti(ExpressionMulti expr);
	
	void visitParens(ExpressionParenthesis expr);
	
	void visitReturn(ExpressionReturn expr);
	
	void visitTry(ExpressionTry expr);
	
	void visitUnary(ExpressionUnaryOperator expr);
	
	void visitVariable(ExpressionVariable expr);
	
	void visitWhile(ExpressionWhile expr);
	
	void visitDef(ExpressionLocal.DefImpl expr);
	
	default void visitDef(ExpressionLocal.DimDef expr) {
		visitDimAssign(expr);
	}
	
	default void visitDef(ExpressionLocal.FuncDef expr) {
		visitFunctionDef(expr);
	}
	
	void visitVectorX(ExpressionX expr);
	
	void visitVectorY(ExpressionY expr);
	
	void visitVectorZ(ExpressionZ expr);
	
	void visitAssign(ExpressionAssign expr);
	
	void visitAssign(ExpressionIndexAssign expr);
	
	void visitAssign(ExpressionMatrixIndexAssign expr);
	
	void visitAssign(ExpressionXAssign expr);
	
	void visitAssign(ExpressionYAssign expr);
	
	void visitAssign(ExpressionZAssign expr);
	
	void visitAssignOp(ExpressionAssignOp expr);
	
	void visitAssignOp(ExpressionIndexAssignOp expr);
	
	void visitAssignOp(ExpressionMatrixIndexAssignOp expr);
	
	void visitAssignOp(ExpressionXAssignOp expr);
	
	void visitAssignOp(ExpressionYAssignOp expr);
	
	void visitAssignOp(ExpressionZAssignOp expr);
	
	void visitPrefixIncrement(ExpressionVarPrefixIncrement expr);
	
	void visitPrefixIncrement(ExpressionIndexPrefixIncrement expr);
	
	void visitPrefixIncrement(ExpressionMatrixIndexPrefixIncrement expr);
	
	void visitPrefixIncrement(ExpressionXPrefixIncrement expr);
	
	void visitPrefixIncrement(ExpressionYPrefixIncrement expr);
	
	void visitPrefixIncrement(ExpressionZPrefixIncrement expr);
	
	void visitPrefixDecrement(ExpressionVarPrefixDecrement expr);
	
	void visitPrefixDecrement(ExpressionIndexPrefixDecrement expr);
	
	void visitPrefixDecrement(ExpressionMatrixIndexPrefixDecrement expr);
	
	void visitPrefixDecrement(ExpressionXPrefixDecrement expr);
	
	void visitPrefixDecrement(ExpressionYPrefixDecrement expr);
	
	void visitPrefixDecrement(ExpressionZPrefixDecrement expr);
	
	void visitPostfixIncrement(ExpressionVarPostfixIncrement expr);
	
	void visitPostfixIncrement(ExpressionIndexPostfixIncrement expr);
	
	void visitPostfixIncrement(ExpressionMatrixIndexPostfixIncrement expr);
	
	void visitPostfixIncrement(ExpressionXPostfixIncrement expr);
	
	void visitPostfixIncrement(ExpressionYPostfixIncrement expr);
	
	void visitPostfixIncrement(ExpressionZPostfixIncrement expr);
	
	void visitPostfixDecrement(ExpressionVarPostfixDecrement expr);
	
	void visitPostfixDecrement(ExpressionIndexPostfixDecrement expr);
	
	void visitPostfixDecrement(ExpressionMatrixIndexPostfixDecrement expr);
	
	void visitPostfixDecrement(ExpressionXPostfixDecrement expr);
	
	void visitPostfixDecrement(ExpressionYPostfixDecrement expr);
	
	void visitPostfixDecrement(ExpressionZPostfixDecrement expr);
}
