package calculator.expressions;

import calculator.Scope;
import calculator.Visitor;
import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionDeleteVariable implements Expression {
	public final String variable;
	
	@Override
	public Object eval(Scope scope) {
		return scope.deleteVariable(variable);
	}
	
	@Override
	public String toEvalString() {
		return "delete " + variable;
	}
	
	@Override
	public String toCompiledString() {
		return "<DEL \"" + variable + "\">";
	}
	
	@Override
	public String toString() {
		return "DeleteVariable{variable=\"%s\"}".format((Object) variable);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitDelete(this);
	}
}
