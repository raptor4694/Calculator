package calculator.expressions;

import calculator.Scope;

public interface ExpressionIncDec extends ExpressionNamed {
	Object evalReference(Scope scope);
}
