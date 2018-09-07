package calculator.expressions;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.function.Supplier;

import calculator.Printer;
import calculator.Scope;
import calculator.Visitor;
import calculator.values.Complex;
import calculator.values.Function;
import calculator.values.MethodFunction;
import calculator.values.Number;
import calculator.values.Real;
import calculator.values.UserFunction;
import lombok.SneakyThrows;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
public class ExpressionLiteral implements Expression {
	public final Supplier<? extends Object> value;
	public final boolean constantValue;
	
	public ExpressionLiteral(Supplier<? extends Object> supplier) {
		value = supplier;
		constantValue = false;
	}
	
	public ExpressionLiteral(final Object obj) {
		if (obj instanceof java.lang.Number) {
			final Real r = Real.valueOf(((java.lang.Number) obj).doubleValue());
			value = () -> r;
		} else
			value = () -> obj;
		constantValue = true;
	}
	
	public Object getValue() {
		return value.get();
	}
	
	@Override
	public String toCompiledString() {
		Object obj = value.get();
		if (obj instanceof MethodFunction) {
			obj = ((MethodFunction) obj).getQualifiedName();
		} else if (obj instanceof UserFunction) {
			UserFunction func = (UserFunction) obj;
			obj = new ExpressionFunctionDefinition(func).toString();
		} else if (obj instanceof Number[]) {
			Number[] arr = (Number[]) obj;
			
			if (arr.length == 0)
				obj = "new Number[0]";
			else
				obj = Arrays.toString(arr);
		} else if (obj instanceof String)
			obj = '"' + ((String) obj).replace("\"", "\\\"") + '"';
		return "(" + obj + ")";
	}
	
	@Override
	public String toString() {
		String objStr;
		Object obj = value.get();
		if (obj instanceof MethodFunction) {
			MethodFunction func = (MethodFunction) obj;
			objStr = "MethodFunction{class=%s,name=\"%s\"}".format(
					(Object) func.getDeclaringClass().getName(), func.getName());
		} else if (obj instanceof UserFunction) {
			UserFunction func = (UserFunction) obj;
			objStr = "UserFunction{name=\"%s\",varnames=%s,body=%s}".format(
					(Object) func.getName(), Arrays.toString(func.varnames),
					func.body);
		} else if (obj instanceof Number[]) {
			Number[] arr = (Number[]) obj;
			objStr = "[";
			for (int i = 0; i < arr.length; i++) {
				if (i != 0)
					objStr += ", ";
				objStr += toString(arr[i]);
			}
			objStr += "]";
		} else if (obj instanceof String)
			objStr = '"' + ((String) obj).replace("\"", "\\\"") + '"';
		else if (obj instanceof Number)
			objStr = toString((Number) obj);
		else
			objStr = obj.toString();
		return "Literal{constantValue=%s,value=%s}".format((Boolean) constantValue,
				objStr);
	}
	
	private static String toString(Number n) {
		if (n instanceof Real)
			return "Real{value=%s}".format(Double.valueOf(((Real) n).value));
		else
			return "Complex{real=%s,imag=%s}".format(
					Double.valueOf(((Complex) n).real),
					Double.valueOf(((Complex) n).imag));
	}
	
	@Override
	public Object eval(Scope scope) {
		return value.get();
	}
	
	@Override
	@SneakyThrows
	public String toEvalString() {
		Object obj = value.get();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos), oldOut = System.out;
		
		if (obj instanceof Function) {
			if (obj instanceof MethodFunction) {
				ps.print(((MethodFunction) obj).getQualifiedName());
			} else {
				ps.print(((Function) obj).toString());
			}
		} else if (obj instanceof String) {
			ps.print('"');
			ps.print((String) obj);
			ps.print('"');
		} else {
			
			System.setOut(ps);
			Printer.println(obj);
			System.setOut(oldOut);
		}
		
		return baos.toString("UTF-8").trim();
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitLiteral(this);
	}
	
}
