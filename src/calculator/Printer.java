package calculator;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

public @UtilityClass class Printer {
	public static boolean printedLine = false;
	
	public void println() {
		System.out.println();
		printedLine = true;
	}
	
	public void println(String s) {
		System.out.println(s);
		printedLine = true;
	}
	
	public void print(String s) {
		System.out.print(s);
		printedLine = false;
	}
	
	public void println(char c) {
		System.out.println(c);
		printedLine = true;
	}
	
	public void print(char c) {
		System.out.print(c);
		printedLine = false;
	}
	
	public void println(Number d) {
		print(d);
		println();
	}
	
	public void print(Number d) {
		System.out.print(d);
		printedLine = false;
	}
	
	public void println(Number[] array) {
		print(array);
		println();
	}
	
	public void print(Number[] array) {
		print('{');
		for (int i = 0; i < array.length; i++) {
			if (i != 0)
				print(", ");
			print(array[i]);
		}
		print('}');
		printedLine = false;
	}
	
	public void println(Number[][] matrix) {
		print(matrix);
		println();
	}
	
	public void print(Number[][] matrix) {
		print('{');
		for (int i = 0; i < matrix.length; i++) {
			if (i != 0)
				print(", ");
			print(matrix[i]);
		}
		print('}');
		printedLine = false;
	}
	
	public String toString(Number n) {
		return n.toString();
	}
	
	public String toString(Number[] array) {
		return toString((Object) array);
	}
	
	public String toString(Number[][] matrix) {
		return toString((Object) matrix);
	}
	
	public String toString(String s) {
		return s;
	}
	
	public String toString(Function f) {
		return f.toString();
	}
	
	@SneakyThrows
	String toString(Object obj) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos), oldOut = System.out;
		System.setOut(ps);
		print(obj);
		System.setOut(oldOut);
		return baos.toString("UTF-8");
	}
	
	public void println(Object obj) {
		print(obj);
		println();
	}
	
	public void print(Object obj) {
		printedLine = false;
		if (obj instanceof Number[])
			print((Number[]) obj);
		else if (obj instanceof Number[][])
			print((Number[][]) obj);
		else if (obj instanceof Number)
			print((Number) obj);
		else if (obj instanceof String) {
			print((String) obj);
		} else
			System.out.print(obj);
	}
}
