package calculator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import calculator.values.EnumOperator;
import calculator.values.Function;
import calculator.values.MethodFunction;
import calculator.values.Real;
import lombok.NonNull;

public class Scope {
	protected Map<String, Object> variables = new HashMap<>();
	public final Scope parent;
	protected boolean isTopLevel = false;
	
	public Scope() {
		this(new TopLevelScope());
	}
	
	public Scope(@NonNull Scope parent) {
		this.parent = parent;
	}
	
	private Scope(boolean b) {
		parent = null;
	}
	
	public void setTopLevel() {
		isTopLevel = true;
	}
	
	public boolean hasImmutableVariable(String varname) {
		if ("ans".equals(varname))
			return false;
		if (variables.containsKey(varname)) {
			Object obj = variables.get(varname);
			return obj instanceof MethodFunction || obj instanceof EnumOperator;
		} else
			return parent.hasImmutableVariable(varname);
	}
	
	public boolean hasVariable(String varname) {
		return variables.containsKey(varname) || parent.hasVariable(varname);
	}
	
	public boolean hasLocalVariable(String varname) {
		return variables.containsKey(varname);
	}
	
	public Object getVariable(String varname) {
		if (!hasVariable(varname))
			throw new VarNotFoundError(varname);
		return variables.containsKey(varname)? variables.get(varname)
				: parent.getVariable(varname);
	}
	
	public void setVariable(String varname, Object value) {
		if (value == null)
			return;
		if (value instanceof java.lang.Number)
			value = Real.valueOf(((java.lang.Number) value).doubleValue());
		if (hasVariable(varname)) {
			Object result = getVariable(varname);
			if ((result instanceof MethodFunction || result instanceof EnumOperator)
					&& ((Function) result).getName().equals(varname))
				throw new CannotRedefVarError(varname);
			if (variables.containsKey(varname)) {
				variables.put(varname, value);
			} else
				parent.setVariable(varname, value);
		} else {
			variables.put(varname, value);
		}
	}
	
	public void setVariableLocally(String varname, Object value) {
		if (value == null)
			return;
		if (value instanceof java.lang.Number)
			value = Real.valueOf(((java.lang.Number) value).doubleValue());
		if (variables.containsKey(varname)) {
			Object result = getVariable(varname);
			if ((result instanceof MethodFunction || result instanceof EnumOperator)
					&& ((Function) result).getName().equals(varname))
				throw new CannotRedefVarError(varname);
		}
		variables.put(varname, value);
	}
	
	public Object deleteVariable(String varname) {
		if (!hasVariable(varname))
			throw new VarNotFoundError(varname);
		if (variables.containsKey(varname)) {
			Object obj = variables.get(varname);
			if (obj instanceof MethodFunction)
				throw new CannotRedefVarError(varname);
			return variables.remove(varname);
		}
		return parent.deleteVariable(varname);
	}
	
	public void deleteAllVariables() {
		Scope scope = this;
		while (!scope.isTopLevel) {
			scope.deleteLocalVariables();
			scope = scope.parent;
		}
	}
	
	public void deleteLocalVariables() {
		if (isTopLevel)
			return;
		for (String varName : new HashSet<>(localVarNames())) {
			try {
				deleteVariable(varName);
			} catch (CannotRedefVarError e) {}
		}
	}
	
	public void with(Class<?> c) {
		if (c.isPrimitive() || c.isArray())
			throw new IllegalArgumentException(
					"Cannot be a primitive type or an array");
		
		Set<String> names = new HashSet<>();
		for (Method method : c.getDeclaredMethods()) {
			if (!names.contains(method.getName())) {
				names.add(method.getName());
				int mods = method.getModifiers();
				if (Modifier.isStatic(mods) && Modifier.isPublic(mods)
						&& (method.getAnnotation(func.class) != null
								|| method.getAnnotation(operator.class) != null)) {
					if (!hasVariable(method.getName())) {
						MethodFunction func =
								new MethodFunction(c, method.getName());
						setVariable(func.getName(), func);
					}
				}
			}
		}
		
	}
	
	public Collection<Object> localValues() {
		return Collections.unmodifiableCollection(variables.values());
	}
	
	public Collection<Object> values() {
		Collection<Object> result = new ArrayList<>(variables.values());
		Scope parent = this.parent;
		while (parent != null) {
			result.addAll(parent.localValues());
			parent = parent.parent;
		}
		return Collections.unmodifiableCollection(result);
	}
	
	public Set<String> localVarNames() {
		return Collections.unmodifiableSet(variables.keySet());
	}
	
	public Set<String> varNames() {
		Set<String> result = new HashSet<>(variables.keySet());
		Scope parent = this.parent;
		while (parent != null) {
			result.addAll(parent.localVarNames());
			parent = parent.parent;
		}
		return Collections.unmodifiableSet(result);
	}
	
	private static class TopLevelScope extends Scope {
		
		private TopLevelScope() {
			super(false);
			isTopLevel = true;
		}
		
		@Override
		public boolean hasImmutableVariable(String varname) {
			return hasVariable(varname);
		}
		
		@Override
		public boolean hasVariable(String varname) {
			return EnumOperator.byLowerCase.containsKey(varname);
		}
		
		@Override
		public boolean hasLocalVariable(String varname) {
			return hasVariable(varname);
		}
		
		@Override
		public Object getVariable(String varname) {
			if (!hasVariable(varname))
				throw new VarNotFoundError(varname);
			return EnumOperator.byLowerCase.get(varname);
		}
		
		@Override
		public void setVariable(String varname, Object value) {
			if (hasVariable(varname))
				throw new CannotRedefVarError(varname);
			throw new AssertionError();
		}
		
		@Override
		public void setVariableLocally(String varname, Object value) {
			if (hasVariable(varname))
				throw new CannotRedefVarError(varname);
			throw new AssertionError();
		}
		
		@Override
		public Object deleteVariable(String varname) {
			if (hasVariable(varname))
				throw new CannotRedefVarError(varname);
			throw new AssertionError();
		}
		
		@Override
		public Collection<Object> localValues() {
			return Collections.unmodifiableCollection(
					EnumOperator.byLowerCase.values());
		}
		
		@Override
		public Set<String> localVarNames() {
			return Collections.unmodifiableSet(EnumOperator.byLowerCase.keySet());
		}
		
		@Override
		public Collection<Object> values() {
			return localValues();
		}
		
		@Override
		public Set<String> varNames() {
			return localVarNames();
		}
	}
	
	public boolean isTopLevel() {
		return isTopLevel;
	}
}
