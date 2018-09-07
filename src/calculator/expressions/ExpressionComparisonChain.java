package calculator.expressions;

import static calculator.functions.Functions.*;

import java.util.ArrayList;

import calculator.Scope;
import calculator.Visitor;
import calculator.values.EnumOperator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
public class ExpressionComparisonChain implements Expression {
	public final ArrayList<Expression> exprs;
	public final ArrayList<Op> operators;
	
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Op {
		public final EnumOperator operator;
		public final boolean elementWise;
		
		@Override
		public String toString() {
			return "{operator=%s,elementWise=%s}".format(operator,
					Boolean.valueOf(elementWise));
		}
		
		private static final Op[][] VALUES;
		
		static {
			EnumOperator[] operators = EnumOperator.values();
			
			VALUES = new Op[operators.length][2];
			
			for (EnumOperator operator : operators) {
				VALUES[operator.ordinal()][0] = new Op(operator, false);
				VALUES[operator.ordinal()][1] = new Op(operator, true);
			}
		}
		
		public static Op of(EnumOperator operator, boolean elementWise) {
			return VALUES[operator.ordinal()][elementWise? 1 : 0];
		}
	}
	
	public ExpressionComparisonChain(ArrayList<Expression> exprs,
			ArrayList<Op> operators) {
		if (exprs.size() < 2 || exprs.size() % 2 == 1)
			throw new IllegalArgumentException("Invalid number of expressions");
		if (operators.size() != exprs.size() - 1)
			throw new IllegalArgumentException("Invalid number of operators");
		this.exprs = exprs;
		this.operators = operators;
	}
	
	@Override
	public Object eval(Scope scope) {
		Object last, current;
		last = exprs.get(0).eval(scope);
		for (int opIdx = 0; opIdx < operators.size(); opIdx++) {
			Op op = operators.get(opIdx);
			current = exprs.get(opIdx + 1).eval(scope);
			Object result = op.operator.call(op.elementWise, last, current);
			if (!toBoolean(result))
				return 0.0;
			last = current;
		}
		return 1.0;
	}
	
	@Override
	public String toEvalString() {
		StringBuilder b = new StringBuilder();
		b.append(exprs.get(0).toEvalString());
		for (int i = 0; i < operators.size(); i++) {
			Op op = operators.get(i);
			Expression ex = exprs.get(i + 1);
			b.append(' ');
			if (op.elementWise)
				b.append('.');
			b.append(op.operator.getSymbol()).append(' ').append(ex.toEvalString());
		}
		return b.toString();
	}
	
	@Override
	public String toCompiledString() {
		StringBuilder b = new StringBuilder("<CHAIN ");
		b.append(exprs.get(0).toCompiledString());
		for (int i = 0; i < operators.size(); i++) {
			Op op = operators.get(i);
			Expression ex = exprs.get(i + 1);
			b.append(' ');
			if (op.elementWise)
				b.append('.');
			b.append(op.operator).append(' ').append(ex.toCompiledString());
		}
		b.append('>');
		return b.toString();
	}
	
	@Override
	public String toString() {
		return "ComparisonChain{exprs=%s,operators=%s}".format(exprs, operators);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitComparisonChain(this);
	}
}
