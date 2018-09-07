package calculator.expressions;

import calculator.TokenKind;
import calculator.Visitor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
public class ExpressionAbs extends ExpressionFunctionCall {
	
	public ExpressionAbs(Expression arg) {
		super(new ExpressionLiteral(TokenKind.BAR.getFunction()),
				new Expression[] {arg});
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitAbs(this);
	}
	
	@Override
	public String toEvalString() {
		return "|%s|".format((Object) args[0].toEvalString());
	}
	
	@Override
	public String toCompiledString() {
		return "<ABS %s>".format((Object) args[0].toCompiledString());
	}
	
	@Override
	public String toString() {
		return "Abs{arg=%s}".format(args[0]);
	}
	
}
