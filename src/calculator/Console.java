package calculator;

import static calculator.Functions.*;
import static calculator.Printer.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

public @UtilityClass class Console {
	
	Scanner keys;
	Scope scope;
	Expression last;
	Object ans;
	Parser parser;
	
	void run() {
		
		keys = new Scanner(System.in);
		
		scope = new Scope();
		
		scope.with(Functions.class);
		scope.with(Operations.class);
		scope.with(Printer.class);
		scope.with(Console.class);
		scope.setVariable("ans", 0.0);
		
		parser = new Parser("");
		
		welcome();
		
		System.out.print(">> ");
		String input = keys.nextLine();
		
		while (!"quit".equals(input)) {
			eval0(input);
			
			System.out.print(">> ");
			input = keys.nextLine();
		}
		
		keys.close();
		
	}
	
	@func("read content of file into current scope")
	public void load(@param("filename") String filename) {
		throw new AssertionError();
	}
	
	private void welcome() {
		System.out.println("Java Calculator v2.3");
		System.out.println("by Lucas Rezac");
		System.out.println(
				"To begin, try typing a math expression, or\ntype \"help\" for a list of functions.");
		System.out.println("===============================================\n");
	}
	
	@func("sets all occurrences of one token to be replaced with a different one")
	public void Alias(@param("token") String tk1, @param("token") String tk2) {
		Tokenizer tkzr = new Tokenizer(tk1);
		Token selected = tkzr.nextToken();
		if (tkzr.nextToken().kind != TokenKind.EOF
				|| selected.kind == TokenKind.NUMBER)
			throw new SyntaxError(selected.pos + selected.toString().length(),
					TokenKind.EOF, tkzr.currentToken().kind);
		tkzr = new Tokenizer(tk2);
		Token alias = tkzr.nextToken();
		if (tkzr.nextToken().kind != TokenKind.EOF)
			throw new SyntaxError(alias.pos + alias.toString().length(),
					TokenKind.EOF, tkzr.currentToken().kind);
		parser.alias(selected, alias);
	}
	
	@func("removes an alias")
	public void Alias(@param("token") String alias) {
		Tokenizer tkzr = new Tokenizer(alias);
		Token token = tkzr.nextToken();
		if (tkzr.nextToken().kind != TokenKind.EOF)
			throw new SyntaxError(token);
		parser.unalias(token);
	}
	
	@func("returns the current directory")
	public String dir() {
		return System.getProperty("user.dir");
	}
	
	@func("returns the directory relative to the current directory")
	public String dir(String str) {
		return Paths.get(str).toAbsolutePath().toString();
	}
	
	@func("changes the directory")
	public void cd(String newdir) {
		Path p = Paths.get(newdir).toAbsolutePath().normalize();
		if (!Files.exists(p) || !Files.isDirectory(p))
			throw new CalculatorError(p + " does not exist/is not a directory");
		System.setProperty("user.dir", p.toString());
	}
	
	@func("lists files in current directory")
	public void ls() {
		try {
			Files.list(Paths.get(System.getProperty("user.dir"))).sorted(
					(p1, p2) -> {
						boolean isDir1 = Files.isDirectory(p1);
						boolean isDir2 = Files.isDirectory(p2);
						return isDir1 == isDir2? 0 : isDir1 && !isDir2? -1 : 1;
					}).forEach(p -> {
						print(p.getFileName());
						if (Files.isDirectory(p))
							print('\\');
						println();
					});
		} catch (IOException e) {
			throw new CalculatorError(e);
		}
	}
	
	@func("runs a command")
	public int run(@param("command") String command) {
		try {
			return new ProcessBuilder("cmd", "/c", command).inheritIO().directory(
					new File(System.getProperty("user.dir"))).start().waitFor();
		} catch (InterruptedException e) {
			return 1;
		} catch (IOException e) {
			throw new CalculatorError(e);
		}
	}
	
	@func("writes to a file")
	public void write(@param("filename") String filename, String contents) {
		try {
			Files.write(Paths.get(filename), contents.getBytes());
		} catch (IOException e) {
			throw new CalculatorError(e);
		}
	}
	
	@func("appends to a file")
	public void append(@param("filename") String filename, String contents) {
		try {
			Files.write(Paths.get(filename), contents.getBytes(),
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			throw new CalculatorError(e);
		}
	}
	
	@func("returns 1 if the file exists, 0 if it does not")
	public boolean fileExists(@param("filename") String filename) {
		return Files.exists(Paths.get(filename));
	}
	
	@func("returns 1 if the file is a directory, 0 if it is not")
	public boolean isDirectory(@param("filename") String filename) {
		return Files.isDirectory(Paths.get(filename));
	}
	
	@func("deletes the given filename, returns 1 if successful, 0 if not")
	public boolean DeleteFile(@param("filename") String filename) {
		try {
			return Files.deleteIfExists(Paths.get(filename));
		} catch (IOException e) {
			lastError = e;
			return false;
		}
	}
	
	@func("saves the last expression to the given filename")
	public void SaveLastExpr(@param("filename to save to") String filename) {
		if (last == null)
			println("No last expr");
		else {
			if (!filename.endsWith(".math"))
				filename += ".math";
			try {
				Files.write(Paths.get(filename), last.toEvalString().getBytes());
				println("Saved to " + filename);
			} catch (IOException e) {
				throw new CalculatorError(e);
			}
		}
	}
	
	@func("saves the compiled result of the last expression to the given filename")
	public void CompileLastExpr(@param("filename to save to") String filename) {
		if (last == null)
			println("No last expr");
		else {
			if (!filename.endsWith(".mrep"))
				filename += ".mrep";
			try {
				Files.write(Paths.get(filename), last.toCompiledString().getBytes());
				println("Saved to " + filename);
			} catch (IOException e) {
				throw new CalculatorError(e);
			}
		}
	}
	
	@func("saves the compiled result of the last expression to the given filename")
	public void CompileLastExprBytes(@param("filename to save to") String filename) {
		if (last == null)
			println("No last expr");
		else {
			if (!filename.endsWith(".mcmp"))
				filename += ".mcmp";
			try {
				Files.write(Paths.get(filename), BytecodeCompiler.compile(last));
				println("Saved to " + filename);
			} catch (IOException e) {
				throw new CalculatorError(e);
			}
		}
	}
	
	@func("print a list of possible functions")
	public void help() {
		println("List of functions: ");
		println("(do help(function) for additional info)");
		
		ArrayList<String> funcNames = new ArrayList<>();
		for (Object obj : scope.values()) {
			if (obj instanceof MethodFunction || obj instanceof EnumOperator
					&& obj != EnumOperator.SCIENTIFIC_NOTATION)
				funcNames.add(((Function) obj).getName());
		}
		funcNames.sort((a, b) -> a.compareToIgnoreCase(b));
		
		while (funcNames.size() > 20 && funcNames.get(0).length() < 120) {
			int col1_length = 0, col2_length = 0;
			
			for (int i = 0; i < 20; i++) {
				String s = funcNames.get(i);
				if (s.length() > col1_length)
					col1_length = s.length();
			}
			
			for (int i = 20, end = Math.min(40, funcNames.size()); i < end; i++) {
				String s = funcNames.get(i);
				if (s.length() > col2_length)
					col2_length = s.length();
			}
			
			String format = "%-" + col1_length + "s     %-" + col2_length + "s";
			
			for (int i = 0; i < 20 && funcNames.size() > 20; i++) {
				String s1 = funcNames.get(i);
				String s2 = funcNames.remove(20);
				funcNames.set(i, String.format(format, s1, s2));
			}
		}
		
		for (String funcName : funcNames)
			println(funcName);
		
		/*ArrayList<Function> functions = new ArrayList<>();
		for (Object obj : scope.localValues()) {
			if (obj instanceof MethodFunction
					|| obj instanceof EnumOperator) {
				// System.out.println(((Function) obj).getDescription());
				functions.add((Function) obj);
			}
		}
		functions.sort((f1,
				f2) -> f1.getName().compareToIgnoreCase(f2.getName()));
		for (Function f : functions) {
			println(f.getName());
		}*/
	}
	
	@func("get descriptive help on a particular function")
	public void help(Function f) {
		println(f.getDescription());
	}
	
	@func("get help about a particular topic")
	public void help(String topic) {
		switch (topic) {
		case "if":
			println("If Expression:\n" + "Syntax:\n"
					+ "  if <expr> then <expr> else <expr>\n"
					+ "  if <expr> then { <exprs> } else { <exprs> }");
			break;
		case "while":
			println("While Loop:\n" + "Syntax:\n" + "  while ( <expr> ) <expr>\n"
					+ "  while ( <expr> ) { <exprs> }");
			break;
		
		case "for":
			println("For Loop:\n" + "Syntax:\n"
					+ "  for ( <variable>, <start>, <end>[, <increment>] ) <expr>\n"
					+ "  for ( <variable>, <start>, <end>[, <increment>] ) { <exprs> }");
		case "foreach":
		case "for each":
		case "for-each":
			println("Single For-Each Loop:\n" + "Syntax:\n"
					+ "  for ( <variable> : <expr> ) <expr>\n"
					+ "  for ( <variable> : <expr> ) { <exprs> }");
			println("Double For-Each Loop:\n" + "Syntax:\n"
					+ "  for ( <variable>, <variable> : <expr> ) <expr>\n"
					+ "  for ( <variable>, <variable> : <expr> ) { <exprs> }");
			break;
		case "try":
			println("Try-Else Block:\n" + "Syntax:\n" + "  try <expr> else <expr>\n"
					+ "  try { <exprs> } else { <exprs> }");
			break;
		case "local":
			println("Local Variables:\n" + "Syntax:\n"
					+ "  local <variable> [= <expr>] [, ...]\n"
					+ "  local dim ( <variable> ) = <expr> [, ...]\n"
					+ "  local <name> ( [args...] ) = <expr> [, ...]\n" + "");
			break;
		case "strings":
			println("Strings:\n" + "Syntax:\n" + "  \"(characters)\"\n"
					+ "Put the '\"' into a string by escaping it with \\");
			break;
		case "topics":
			println("Help topics are:\n"
					+ "if, while, for, for-each, try, local, strings, info");
			break;
		case "info":
			welcome();
			break;
		default:
			println("Unknown chapter \"" + topic + "\"");
		}
	}
	
	@func("get user input as a string")
	public Object input() {
		return keys.nextLine();
	}
	
	@func("prints the full stack trace of the last error")
	public void PrintLastError() {
		if (lastError == null)
			println("No last error");
		else
			lastError.printStackTrace(System.out);
	}
	
	@func("prints the last expression, in formatted form")
	public void LastExprToStr() {
		if (last == null)
			println("No last expression");
		else {
			String s = last.toEvalString();
			println(s);
			scope.setVariable("ans", s);
		}
	}
	
	@func("prints the compilation of the last expression")
	public void CompileLastExpr() {
		if (last == null)
			println("No last expression");
		else {
			String s = last.toCompiledString();
			println(s);
			scope.setVariable("ans", s);
		}
	}
	
	@func("prints the bytecode of the last expression")
	public void CompileLastExprBytes() {
		if (last == null)
			println("No last expression");
		else {
			byte[] bytes = BytecodeCompiler.compile(last);
			for (byte b : bytes) {
				print(Byte.toString(b));
				print(' ');
			}
			println();
			
		}
	}
	
	@func("prints an informative view of the last parsed expression")
	public void LastExprInfo() {
		if (last == null)
			println("No last expression");
		else {
			String s = last.toString();
			println(s);
			scope.setVariable("ans", s);
		}
	}
	
	@func("clears the screen")
	@SneakyThrows
	public void clear() {
		// System.out.print("\033[H\033[2J");
		// System.out.flush();
		// printedLine = true;
		new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
	}
	
	Exception lastError;
	private boolean first = false;
	
	public Object expr(String str) {
		return Parser.parse(str).eval(scope);
	}
	
	@func("compiles the given expression to the internal representation")
	public String compile(String expr) {
		return Parser.parse(expr).toCompiledString();
	}
	
	@func("converts internal representation of expression to evaluatable string")
	public String decompile(String expr) {
		return CompiledParser.parse(expr).toEvalString();
	}
	
	private void eval0(String str) {
		if ((str = str.trim()).isEmpty())
			return;
		
		switch (str.charAt(0)) {
		case '*':
		case '+':
		case '/':
			str = "ans" + str;
		}
		
		try {
			
			Expression e = parser.reset(str).parse();
			first = true;
			eval1(e);
			first = false;
		} catch (CalculatorError e) {
			println(e.toString());
			lastError = e;
		}
	}
	
	private void eval1(Expression e) {
		if (e instanceof ExpressionMulti) {
			boolean temp = first;
			first = false;
			for (Expression x : ((ExpressionMulti) e).exprs) {
				eval1(x);
			}
			if (first = temp && !printedLine) {
				println();
			}
			return;
		}
		try {
			
			Object value;
			
			try {
				value = doEval(e);
			} catch (ExpressionReturn ex) {
				value = ex.lastValue;
			}
			if (value != null && !(value instanceof MethodFunction)
					&& !(value instanceof EnumOperator))
				scope.setVariable("ans", ans = value);
			
			if ((value instanceof MethodFunction || value instanceof EnumOperator)
					&& ((Function) value).getName() != null
					&& (!(e instanceof ExpressionNamed)
							|| (e instanceof ExpressionVariable
									&& ((Function) value).getName().equals(
											((ExpressionVariable) e).variable)))) {
				Function f = (Function) value;
				if (f.minArgCount() == 0 && e instanceof ExpressionVariable) {
					value = f.callOptionalValue(scope);
					
					if (value != null) {
						if (!(value instanceof Function)) {
							scope.setVariable("ans", ans = value);
							
							printValue(value, 0);
						} else {
							println(((Function) value).getDescription());
						}
					} else if (first && !printedLine) {
						println();
					}
				} else {
					println(f.getDescription());
				}
			} else if (e instanceof ExpressionNamed) {
				if (e instanceof ExpressionPostfixIncDec) {
					Real add = Real.valueOf(
							e instanceof ExpressionPostfixIncrement? 1 : -1);
					if (value instanceof Number)
						value = ((Number) value).plus(add);
					else if (value instanceof Number[]) {
						Number[] array = ((Number[]) value).clone();
						for (int i = 0; i < array.length; i++) {
							array[i] = array[i].plus(add);
						}
						value = array;
					} else if (value instanceof Number[][]) {
						Number[][] matrix = (Number[][]) copy((Number[][]) value);
						for (Number[] array : matrix) {
							for (int i = 0; i < array.length; i++) {
								array[i] = array[i].plus(add);
							}
						}
						value = matrix;
					}
				}
				String s = ((ExpressionNamed) e).getNameString() + " = ";
				if (value != null) {
					print(s);
					printValue(value, s.length());
				} else if (first && !printedLine) {
					println();
				}
			} else if (value != null && !(e instanceof ExpressionDeleteVariable)) {
				printValue(value, 0);
			} else if (first && !printedLine) {
				println();
			}
		} catch (CalculatorError ex) {
			println(ex.toString());
			lastError = ex;
		} catch (IndexOutOfBoundsException ex) {
			println("Error: dimension");
			lastError = ex;
		} catch (NullPointerException ex) {
			ex.printStackTrace();
			lastError = ex;
		} catch (ExpressionBreak b) {
			println("Error: unexpected break");
		} finally {
			last = e;
		}
	}
	
	private void printValue(Object value, int indent) {
		if (value instanceof String) {
			printString((String) value);
		} else if (value instanceof MethodFunction) {
			println(((Function) value).getName());
		} else if (value instanceof Object[][]) {
			printMatrix((Object[][]) value, indent);
		} else if (value instanceof Object[]) {
			printArray((Object[]) value);
		} else {
			println(value);
		}
	}
	
	private void printArray(Object[] array) {
		print('{');
		for (int i = 0; i < array.length; i++) {
			if (i != 0)
				print(", ");
			if (array[i] instanceof String) {
				print('"');
				print(array[i].toString().replace("\\", "\\\\").replaceAll("\"",
						"\\\""));
				print('"');
			} else
				print(array[i]);
		}
		println('}');
	}
	
	private Object doEval(Expression e) {
		if (e instanceof ExpressionIncDec) {
			boolean postfix = e instanceof ExpressionPostfixIncDec;
			boolean add = e instanceof ExpressionIncrement;
			if (e instanceof ExpressionMatrixIndex) {
				return e.eval(scope);
			} else if (e instanceof ExpressionIndex) {
				ExpressionIndex ex = (ExpressionIndex) e;
				Object obj1 = ex.array.eval(scope);
				
				Object obj = ex.index.eval(scope);
				
				if (!(obj instanceof Real) || !isInt((Real) obj))
					throw new TypeError("index");
				
				ex.lastIndex = ((Real) obj).intValue();
				
				if (ex.lastIndex < 1)
					throw new DimensionError();
				
				int index = ex.lastIndex - 1;
				
				if (obj1 instanceof Number[]) {
					Number[] array = (Number[]) obj1;
					if (postfix) {
						Number last = array[index];
						if (add)
							array[index] = last.plus(Real.ONE);
						else
							array[index] = last.minus(Real.ONE);
						return last;
					} else {
						if (add)
							array[index] = array[index].plus(Real.ONE);
						else
							array[index] = array[index].minus(Real.ONE);
						return array[index];
					}
				} else if (obj1 instanceof Number[][]) {
					Number[][] matrix = (Number[][]) obj1;
					Number[] array = matrix[index];
					Number[] result = postfix? array.clone() : array;
					if (add) {
						for (int i = 0; i < array.length; i++)
							array[i] = array[i].plus(Real.ONE);
						
					} else {
						for (int i = 0; i < array.length; i++)
							array[i] = array[i].minus(Real.ONE);
					}
					return result;
				} else
					throw new TypeError("not an array");
			} else {
				Object obj = ((ExpressionIncDec) e).evalReference(scope);
				
				if (obj instanceof Real) {
					return e.eval(scope);
				} else if (obj instanceof Number[]) {
					Number[] array = (Number[]) obj;
					Number[] result = postfix? array.clone() : array;
					if (add) {
						for (int i = 0; i < array.length; i++)
							array[i] = array[i].plus(Real.ONE);
					} else {
						for (int i = 0; i < array.length; i++)
							array[i] = array[i].minus(Real.ONE);
					}
					return result;
				} else if (obj instanceof Number[][]) {
					Number[][] matrix = (Number[][]) obj;
					Number[][] result = postfix? (Number[][]) copy(matrix) : matrix;
					if (add) {
						for (Number[] array : matrix) {
							for (int i = 0; i < array.length; i++)
								array[i] = array[i].plus(Real.ONE);
						}
					} else {
						for (Number[] array : matrix) {
							for (int i = 0; i < array.length; i++)
								array[i] = array[i].minus(Real.ONE);
						}
					}
					return result;
				} else
					throw new TypeError();
			}
		} else {
			return e.evalOptionalValue(scope);
		}
	}
	
	private void printMatrix(Object[][] matrix, int indent) {
		List<List<String>> elements = new ArrayList<>();
		int[] maxLengths = new int[columnCount(matrix)];
		
		for (Object[] array : matrix) {
			List<String> list = new ArrayList<>();
			for (int i = 0; i < array.length; i++) {
				String s =
						(array[i] instanceof String
								? '"' + array[i].toString().replace("\\",
										"\\\\").replaceAll("\"", "\\\"") + '"'
								: Printer.toString(array[i]));
				list.add(s);
				if (s.length() > maxLengths[i]) {
					maxLengths[i] = s.length();
				}
			}
			elements.add(list);
		}
		
		for (List<String> list : elements) {
			for (int i = 0; i < list.size(); i++) {
				String s = list.get(i);
				if (s.length() < maxLengths[i]) {
					while (s.length() < maxLengths[i])
						s = " " + s;
					list.set(i, s);
				}
			}
		}
		
		print('{');
		for (int r = 0; r < elements.size(); r++) {
			List<String> row = elements.get(r);
			if (r != 0) {
				println(',');
				for (int i = 0; i < indent; i++) {
					print(' ');
				}
				print(' ');
			}
			print('{');
			for (int c = 0; c < row.size(); c++) {
				if (c != 0) {
					print(", ");
				}
				print(row.get(c));
			}
			print('}');
		}
		println('}');
	}
	
	private void printString(String s) {
		print('"');
		print(s.replace("\\", "\\\\").replace("\"", "\\\""));
		println('"');
	}
}
