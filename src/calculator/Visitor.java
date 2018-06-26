package calculator;

public interface Visitor {
	void visitExpression(Expression expr);
	
	void visitAbs(ExpressionAbs expr);
	
	void visitArrayLiteral(ExpressionArrayLiteral expr);
	
	void visitArrayLiteral(ExpressionColumnLiteral expr);
	
	void visitBinary(ExpressionBinaryOperator expr);
	
	default void visitBinary(
			ExpressionElementwiseBinaryOperator expr) {
		visitBinary((ExpressionBinaryOperator) expr);
	}
	
	void visitBreak(ExpressionBreak expr);
	
	void visitIf(ExpressionIf expr);
	
	default void visitIf(ExpressionConditional expr) {
		visitIf((ExpressionIf) expr);
	}
	
	void visitComparisonChain(ExpressionComparisonChain expr);
	
	void visitDelete(ExpressionDeleteVariable expr);
	
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
	
	void visitPrefixIncrement(
			ExpressionMatrixIndexPrefixIncrement expr);
	
	void visitPrefixIncrement(ExpressionXPrefixIncrement expr);
	
	void visitPrefixIncrement(ExpressionYPrefixIncrement expr);
	
	void visitPrefixIncrement(ExpressionZPrefixIncrement expr);
	
	void visitPrefixDecrement(ExpressionVarPrefixDecrement expr);
	
	void visitPrefixDecrement(ExpressionIndexPrefixDecrement expr);
	
	void visitPrefixDecrement(
			ExpressionMatrixIndexPrefixDecrement expr);
	
	void visitPrefixDecrement(ExpressionXPrefixDecrement expr);
	
	void visitPrefixDecrement(ExpressionYPrefixDecrement expr);
	
	void visitPrefixDecrement(ExpressionZPrefixDecrement expr);
	
	void visitPostfixIncrement(ExpressionVarPostfixIncrement expr);
	
	void visitPostfixIncrement(ExpressionIndexPostfixIncrement expr);
	
	void visitPostfixIncrement(
			ExpressionMatrixIndexPostfixIncrement expr);
	
	void visitPostfixIncrement(ExpressionXPostfixIncrement expr);
	
	void visitPostfixIncrement(ExpressionYPostfixIncrement expr);
	
	void visitPostfixIncrement(ExpressionZPostfixIncrement expr);
	
	void visitPostfixDecrement(ExpressionVarPostfixDecrement expr);
	
	void visitPostfixDecrement(ExpressionIndexPostfixDecrement expr);
	
	void visitPostfixDecrement(
			ExpressionMatrixIndexPostfixDecrement expr);
	
	void visitPostfixDecrement(ExpressionXPostfixDecrement expr);
	
	void visitPostfixDecrement(ExpressionYPostfixDecrement expr);
	
	void visitPostfixDecrement(ExpressionZPostfixDecrement expr);
}
