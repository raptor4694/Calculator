package calculator;

import static calculator.Functions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.function.BiFunction;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
public class ExpressionFunctionCall implements Expression {
	Expression function;
	Expression[] args;
	
	public ExpressionFunctionCall(Expression function, Expression... args) {
		this.function = function;
		this.args = args;
	}
	
	@Override
	public Object eval(Scope scope) {
		Object result = eval0(scope, func -> func::call,
				(expr, scope2) -> expr.eval(scope2));
		if (result == null)
			throw new CalculatorError("must return a value");
		return result;
	}
	
	@Override
	public Object evalOptionalValue(Scope scope) {
		return eval0(scope, func -> func::callOptionalValue,
				(expr, scope2) -> expr.evalOptionalValue(scope2));
	}
	
	private Object eval0(Scope scope,
			java.util.function.Function<Function, BiFunction<Scope, Object[], Object>> callerSupplier,
			BiFunction<Expression, Scope, Object> valueSupplier) {
		Object func = function.eval(scope);
		
		if (func instanceof MethodFunction
				&& ((MethodFunction) func).getDeclaringClass() == Console.class
				&& ((MethodFunction) func).getName().equals("load")
				&& args.length == 1) {
			Object obj = args[0].eval(scope);
			check(obj instanceof String, TypeError.class);
			File file = new File((String) obj);
			Expression result;
			if (file.getName().endsWith(".math")) {
				try {
					result = Console.parser.reset(
							new String(Files.readAllBytes(file.toPath()))).parse();
				} catch (IOException e) {
					throw new CalculatorError(e);
				}
			} else if (file.getName().endsWith(".mcmp")) {
				try {
					result = BytecodeParser.parse(Files.readAllBytes(file.toPath()));
				} catch (IOException | BytecodeException e) {
					throw new CalculatorError(e);
				}
			} else if (file.getName().endsWith(".mrep")) {
				try {
					result = CompiledParser.parse(
							new String(Files.readAllBytes(file.toPath())));
				} catch (IOException e) {
					throw new CalculatorError(e);
				}
			} else
				throw new CalculatorError(
						"Do not know how to read this file extension");
			
			System.out.println("Loaded " + result.toCompiledString());
			
			return valueSupplier.apply(result, scope);
			
		}
		
		Object[] objs = new Object[args.length];
		for (int i = 0; i < objs.length; i++)
			objs[i] = args[i].eval(scope);
		
		if (func instanceof Function) {
			return callerSupplier.apply((Function) func).apply(scope, objs);
		} else if (args.length == 1) {
			return EnumOperator.MULTIPLY.call(scope, func, objs[0]);
		} else
			throw new TypeError("not a function");
	}
	
	@Override
	public String toCompiledString() {
		if (args.length == 0)
			return "<CALL %s>".format((Object) function.toCompiledString());
		StringBuilder b = new StringBuilder("<CALL ");
		b.append(function.toCompiledString()).append(" WITH (");
		for (int i = 0; i < args.length; i++) {
			if (i != 0)
				b.append(", ");
			b.append(args[i].toCompiledString());
		}
		b.append(")>");
		return b.toString();
	}
	
	@Override
	public String toEvalString() {
		String s0 = function.toEvalString();
		if (s0.matches("[a-zA-Z$_][a-zA-Z$_0-9]*(\\.[a-zA-Z$_][a-zA-Z$_0-9]*)+")) {
			int i = s0.lastIndexOf('.');
			s0 = s0.substring(i + 1);
		}
		String s = s0 + "(";
		for (int i = 0; i < args.length; i++) {
			if (i != 0)
				s += ", ";
			s += args[i].toEvalString();
		}
		return s + ")";
	}
	
	@Override
	public String toString() {
		return "FunctionCall{function=%s,args=%s}".format(function,
				Arrays.toString(args));
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitCall(this);
	}
	
}