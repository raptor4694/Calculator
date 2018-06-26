package calculator;

import lombok.NonNull;

public class ExpressionConditional extends ExpressionIf {
	
	public ExpressionConditional(Expression condition,
			Expression thenpart, @NonNull Expression elsepart) {
		super(condition, thenpart, elsepart);
	}
	
	@Override
	public String toEvalString() {
		return condition.toEvalString() + "? "
				+ thenpart.toEvalString() + " : "
				+ elsepart.toEvalString();
	}
	
	@Override
	public String toString() {
		return "<IFEXPR" + super.toString().substring(3);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitIf(this);
	}
}
