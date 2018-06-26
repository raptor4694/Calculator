package calculator;

public interface ExpressionReferenceable extends ExpressionNamed {
	Expression toAssign(Expression value);
	
	Expression toAssignOp(EnumOperator operator, Expression value);
	
	Expression toPrefixIncrement();
	
	Expression toPrefixDecrement();
	
	Expression toPostfixIncrement();
	
	Expression toPostfixDecrement();
}
