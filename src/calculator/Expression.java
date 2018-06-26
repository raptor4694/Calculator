package calculator;

public interface Expression {
	
	Object eval(Scope scope);
	
	String toEvalString();
	
	String toCompiledString();
	
	default byte[] toBytecode() {
		return BytecodeCompiler.compile(this);
	}
	
	default void accept(Visitor v) {
		v.visitExpression(this);
	}
	
	default Object evalOptionalValue(Scope scope) {
		return eval(scope);
	}
	
	default Expression skipParens() {
		Expression result = this;
		while (result instanceof ExpressionParenthesis)
			result = ((ExpressionParenthesis) result).expr;
		return result;
	}
	
}