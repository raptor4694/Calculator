package calculator.expressions;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import calculator.Scope;
import calculator.Visitor;
import calculator.values.Real;
import calculator.values.UserFunction;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
public class ExpressionLocal implements Expression {
	
	public static interface Def extends Expression {}
	
	public final Def[] defs;
	
	public ExpressionLocal(Def[] defs) {
		if (defs.length == 0)
			throw new IllegalArgumentException("No definitions given");
		this.defs = defs;
	}
	
	@Override
	public Object eval(Scope scope) {
		Object last = null;
		for (Def def : defs) {
			last = def.eval(scope);
		}
		if (last == null)
			throw new AssertionError();
		return last;
	}
	
	@Override
	public Object evalOptionalValue(Scope scope) {
		Object last = null;
		for (Def def : defs) {
			last = def.evalOptionalValue(scope);
		}
		return last;
	}
	
	@Override
	public String toEvalString() {
		String s = "local ";
		for (int i = 0; i < defs.length; i++) {
			if (i != 0)
				s += ", ";
			s += defs[i].toEvalString();
		}
		return s;
	}
	
	@Override
	public String toCompiledString() {
		String s = "<LOCAL ";
		for (int i = 0; i < defs.length; i++) {
			if (i != 0)
				s += ", ";
			s += defs[i].toCompiledString();
		}
		return s + ">";
	}
	
	@Override
	public String toString() {
		return "Local{defs=%s}".format((Object) Arrays.toString(defs));
	}
	
	@RequiredArgsConstructor
	public static class DefImpl implements Def {
		public final String name;
		public final Expression value;
		
		public DefImpl(String name) {
			this(name, null);
		}
		
		@Override
		public Object eval(Scope scope) {
			Object result = (value == null)? Real.ZERO : value.eval(scope);
			scope.setVariableLocally(name, result);
			return result;
		}
		
		@Override
		public Object evalOptionalValue(Scope scope) {
			if (value == null) {
				scope.setVariableLocally(name, Real.ZERO);
				return null;
			} else {
				Object result = value.eval(scope);
				scope.setVariableLocally(name, result);
				return result;
			}
		}
		
		@Override
		public String toString() {
			return "DefImpl{name=\"%s\",value=%s}".format((Object) name, value);
		}
		
		@Override
		public String toEvalString() {
			return value == null? name : name + " = " + value.toEvalString();
		}
		
		@Override
		public String toCompiledString() {
			return value == null? "<DEF \"%s\">".format((Object) name)
					: "<DEF \"%s\" IS %s>".format((Object) name,
							value.toCompiledString());
		}
		
		@Override
		public void accept(Visitor v) {
			v.visitDef(this);
		}
	}
	
	public static class DimDef extends ExpressionDimAssign implements Def {
		
		public DimDef(String variable, Expression value) {
			super(variable, value);
		}
		
		@Override
		protected Predicate<String> getHasVariableFunction(Scope scope) {
			return scope::hasLocalVariable;
		}
		
		@Override
		protected BiConsumer<String, Object> getSetVariableFunction(Scope scope) {
			return scope::setVariableLocally;
		}
		
		@Override
		public String toString() {
			return "DimDef{variable=\"%s\",value=%s}".format((Object) variable,
					value);
		}
		
		@Override
		public void accept(Visitor v) {
			v.visitDef(this);
		}
		
	}
	
	public static class FuncDef extends ExpressionFunctionDefinition implements Def {
		
		public FuncDef(UserFunction function) {
			super(function);
			if (function.getName() == null)
				throw new NullPointerException("function name cannot be null");
		}
		
		@Override
		protected BiConsumer<String, Object> getSetVariableFunction(Scope scope) {
			return scope::setVariableLocally;
		}
		
		@Override
		public String toString() {
			return "FuncDef{function=UserFunction{name=\"%s\",varnames=%s,body=%s}}".format(
					(Object) function.getName(), Arrays.toString(function.varnames),
					function.body);
		}
		
		@Override
		public void accept(Visitor v) {
			v.visitDef(this);
		}
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitLocal(this);
	}
}
