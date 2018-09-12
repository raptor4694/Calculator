package calculator;

import static calculator.functions.Functions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

import calculator.values.Function;
import calculator.values.Number;
import calculator.values.Real;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

public @UtilityClass class Printer {
	public static boolean printedLine = false;
	
	@func("prints a new line to the console")
	public void println() {
		System.out.println();
		printedLine = true;
	}
	
	@func
	public void println(String s) {
		System.out.println(s);
		printedLine = true;
	}
	
	@func
	public void print(String s) {
		System.out.print(s);
		printedLine = false;
	}
	
	@func
	public void print(Unit u) {
		System.out.print(UnitFormat.getInstance().format(u));
		printedLine = false;
	}
	
	@func
	public void println(Unit u) {
		print(u);
		println();
	}
	
	@func
	public void println(char c) {
		System.out.println(c);
		printedLine = true;
	}
	
	@func
	public void print(char c) {
		System.out.print(c);
		printedLine = false;
	}
	
	@func
	public void println(Number d) {
		print(d);
		println();
	}
	
	@func
	public void print(Number d) {
		System.out.print(d);
		printedLine = false;
	}
	
	@func
	public void println(Object[] array) {
		print(array);
		println();
	}
	
	@func
	public void print(Object[] array) {
		print('{');
		for (int i = 0; i < array.length; i++) {
			if (i != 0)
				print(", ");
			print(array[i]);
		}
		print('}');
		printedLine = false;
	}
	
	@func
	public void println(Object[][] matrix) {
		print(matrix);
		println();
	}
	
	@func
	public void print(Object[][] matrix) {
		print('{');
		for (int i = 0; i < matrix.length; i++) {
			if (i != 0)
				print(", ");
			print(matrix[i]);
		}
		print('}');
		printedLine = false;
	}
	
	@func
	public String toString(Number n) {
		return n.toString();
	}
	
	@func
	public String toString(Real r, Real radix) {
		check(radix.isInt(), TypeError);
		check(r.isInt(), TypeError);
		return Long.toString((long) r.doubleValue(), radix.intValue());
	}
	
	@func
	public String toUnsignedString(Real r, Real radix) {
		check(radix.isInt(), TypeError);
		check(r.isInt(), TypeError);
		return Long.toUnsignedString((long) r.doubleValue(), radix.intValue());
	}
	
	@func
	public String toString(Object[] array) {
		return toString((Object) array);
	}
	
	@func
	public String toString(Object[][] matrix) {
		return toString((Object) matrix);
	}
	
	@func
	public String toString(String s) {
		return s;
	}
	
	@func
	public String toString(Function f) {
		return f.toString();
	}
	
	@SneakyThrows
	public String toString(Object obj) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos), oldOut = System.out;
		System.setOut(ps);
		print(obj);
		System.setOut(oldOut);
		return baos.toString("UTF-8");
	}
	
	@func("prints output to console, appending new line afterward")
	public void println(Object obj) {
		print(obj);
		println();
	}
	
	@func("prints output to console, no new line afterward")
	public void print(Object obj) {
		printedLine = false;
		if (obj instanceof Object[][])
			print((Object[][]) obj);
		else if (obj instanceof Object[])
			print((Object[]) obj);
		else if (obj instanceof Number)
			print((Number) obj);
		else if (obj instanceof Unit)
			print((Unit) obj);
		else if (obj instanceof String)
			print((String) obj);
		else
			System.out.print(obj);
	}
}
