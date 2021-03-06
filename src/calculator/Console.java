package calculator;

import static calculator.Printer.*;
import static calculator.functions.Functions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.measure.converter.ConversionException;

import calculator.Scope.FileScope;
import calculator.errors.BytecodeException;
import calculator.errors.CalculatorError;
import calculator.errors.DimensionError;
import calculator.errors.SyntaxError;
import calculator.errors.TypeError;
import calculator.expressions.Expression;
import calculator.expressions.ExpressionBreak;
import calculator.expressions.ExpressionDeleteVariable;
import calculator.expressions.ExpressionIncDec;
import calculator.expressions.ExpressionIncrement;
import calculator.expressions.ExpressionIndex;
import calculator.expressions.ExpressionLiteral;
import calculator.expressions.ExpressionMatrixIndex;
import calculator.expressions.ExpressionMulti;
import calculator.expressions.ExpressionNamed;
import calculator.expressions.ExpressionPostfixIncDec;
import calculator.expressions.ExpressionPostfixIncrement;
import calculator.expressions.ExpressionReturn;
import calculator.expressions.ExpressionVariable;
import calculator.functions.Functions;
import calculator.functions.Operations;
import calculator.values.EnumOperator;
import calculator.values.Function;
import calculator.values.MethodFunction;
import calculator.values.Number;
import calculator.values.Real;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

public @UtilityClass class Console {
	
	Scanner keys;
	@Getter
	Scope scope;
	@Getter
	Expression last;
	Object ans;
	@Getter
	Parser parser;
	
	void run() {
		try {
			keys = new Scanner(System.in);
			
			scope = new Scope();
			
			scope.with(Functions.class);
			scope.with(Operations.class);
			scope.with(Printer.class);
			scope.with(Console.class);
			scope.setVariable("ans", 0.0);
			scope.setTopLevel();
			
			scope = new Scope(scope);
			
			parser = new Parser("");
			
			parser.factor_hooks_post.add(parser -> {
				if (parser.eat(TokenKind.AT)) {
					Token t = parser.getToken();
					Expression e;
					/*	switch (t.kind) {
						case WORD:
							e = new ExpressionVariable("@" + t.stringValue());
							break;
						case NUMBER:
							e = new ExpressionVariable("@" + t.doubleValue());
							break;
						case STRING:
							e = new ExpressionVariable('"' + t.stringValue() + '"');
							break;
						default:*/
					e = new ExpressionVariable(t.toString());
					// }
					parser.nextToken();
					return e;
				}
				return null;
			});
			
			welcome();
			
			System.out.print(">> ");
			
			String input = null;
			
			while (input == null)
				try {
					input = keys.nextLine();
				} catch (NoSuchElementException e) {}
			
			while (!"quit".equals(input)) {
				eval0(input);
				
				System.out.print(">> ");
				input = null;
				while (input == null)
					try {
						input = keys.nextLine();
					} catch (NoSuchElementException e) {}
			}
			
		} catch (Throwable t) {
			System.err.println(
					"A fatal error has been thrown. The program will terminate.");
			t.printStackTrace();
		} finally {
			keys.close();
		}
	}
	
	@func("set a global variable")
	public void Global(@param("variable name") String varname, Object value,
			@param("") Scope scope) {
		while (!scope.isTopLevel())
			scope = scope.parent;
		scope.setVariable(varname, value);
	}
	
	@func("set a normal variable")
	public void Set(@param("variable name") String varname, Object value,
			@param("") Scope scope) {
		scope.setVariable(varname, value);
	}
	
	@func("set a local variable")
	public void SetLocal(@param("variable name") String varname, Object value,
			@param("") Scope scope) {
		scope.setVariableLocally(varname, value);
	}
	
	@func("set a variable using the parent scope")
	public void SetOuter(@param("variable name") String varname, Object value,
			@param("") Scope scope) {
		scope.parent.setVariable(varname, value);
	}
	
	@func("read content of file into current scope")
	public Object load(@param("filename") String filename) {
		// throw new AssertionError();
		Expression expr = loadExpression(filename);
		Scope s = new FileScope(scope);
		s.setTopLevel();
		try {
			return expr.eval(s);
		} catch (ExpressionReturn r) {
			return r.value;
		}
	}
	
	@func("read file as raw text data")
	public String[] read(@param("filename") String filename) {
		File file = new File(dir() + "\\" + filename);
		try {
			return new String(Files.readAllBytes(file.toPath())).split("\\R");
		} catch (IOException e) {
			throw new CalculatorError(e);
		}
	}
	
	@func("list currently defined variables")
	public void Env() {
		Scope scope = Console.scope;
		Collection<String> vars = new HashSet<>();
		while (!scope.isTopLevel()) {
			vars.addAll(scope.localVarNames());
			scope = scope.parent;
		}
		Collections.sort((List<String>) (vars = new ArrayList<>(vars)),
				String::compareTo);
		for (String s : vars) {
			Object val = Console.scope.getVariable(s);
			if (val instanceof Function && ((Function) val).getName() != null) {
				println(val);
			} else {
				print(s + " = ");
				println(val);
			}
		}
	}
	
	public Expression loadExpression(String filename) {
		Expression result;
		if (filename.startsWith("<") && filename.endsWith(">")) {
			filename = filename.substring(1, filename.length() - 1);
			int i = filename.lastIndexOf('.');
			if (i == -1) {
				filename += ".math";
			}
			try (Scanner scan = new Scanner(Console.class.getResourceAsStream(
					"/resources/lib/" + filename))) {
				
				scan.useDelimiter("\\A");
				
				String str = scan.next();
				
				if (filename.endsWith(".math")) {
					
					str = removeComments(str.trim());
					
					result = Console.parser.reset(str).parse();
				} else if (filename.endsWith(".mcmp")) {
					result = BytecodeParser.parse(str.getBytes());
				} else if (filename.endsWith(".mrep")) {
					result = CompiledParser.parse(str.trim());
				} else {
					throw new CalculatorError(
							"Do not know how to read this file extension");
				}
			} catch (Exception e) {
				throw new CalculatorError(e);
			}
		} else {
			File file = new File(dir() + "\\" + filename);
			
			if (file.getName().endsWith(".math")) {
				try {
					result = Console.parser.reset(removeComments(new String(
							Files.readAllBytes(file.toPath())).trim())).parse();
				} catch (IOException e) {
					// System.out.println(file.getAbsolutePath());
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
			} else if (file.getName().endsWith(".csv")) {
				Pattern numberPattern = Pattern.compile(
						"(\\d+(\\.\\d*)?|\\.\\d+)(,(\\d+(\\.\\d*)?|\\.\\d+))*");
				try {
					List<String> lines = Files.readAllLines(file.toPath());
					boolean numberFormat = true;
					for (String line : lines) {
						if (!numberPattern.matcher(line).matches()) {
							numberFormat = false;
							break;
						}
					}
					
					if (numberFormat) {
						Number[][] numbers = new Number[lines.size()][];
						for (int i = 0; i < lines.size(); i++) {
							String[] split = lines.get(i).split(",");
							numbers[i] = new Number[split.length];
							for (int j = 0; j < split.length; j++) {
								numbers[i][j] =
										Real.valueOf(Double.parseDouble(split[j]));
							}
						}
						result = new ExpressionLiteral(numbers);
					} else {
						String[][] strings = new String[lines.size()][];
						for (int i = 0; i < lines.size(); i++) {
							strings[i] = lines.get(i).split(",");
						}
						result = new ExpressionLiteral(strings);
					}
				} catch (IOException e) {
					throw new CalculatorError(e);
				}
			} else {
				throw new CalculatorError(
						"Do not know how to read this file extension");
			}
		}
		return result;
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
			Files.write(Paths.get(dir() + "\\" + filename), contents.getBytes());
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
		
		funcNames.add("if");
		funcNames.add("while");
		funcNames.add("for");
		funcNames.add("local");
		funcNames.add("functions");
		funcNames.add("return");
		funcNames.add("strings");
		funcNames.add("info");
		funcNames.add("topics");
		funcNames.sort((a, b) -> a.compareToIgnoreCase(b));
		
		for (int i = 0; i < funcNames.size(); i++) {
			String name = funcNames.get(i);
			switch (name) {
			case "if":
			case "while":
			case "for":
			case "local":
			case "functions":
			case "return":
			case "strings":
			case "info":
			case "topics":
				funcNames.set(i, '"' + name + '"');
				break;
			}
		}
		
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
		case "then":
		case "else":
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
					+ "if, while, for, for-each, try, local, strings, info, return, functions");
			break;
		case "delete":
			println("Delete Variable:\n" + "Syntax:\n" + "  delete <variable>");
			break;
		case "info":
			welcome();
			break;
		case "return":
			println("Return from Function:\n" + "Syntax:\n" + "  return <expr>");
			break;
		case "functions":
			println("Functions:\n" + "Syntax:\n"
					+ "  <name> ( [args...] ) = <expr>\n"
					+ "  <name> ( [args...] ) = { <exprs> }\n"
					+ "  ( [args...] ) => <expr>\n"
					+ "  ( [args...] ) => { <exprs> }");
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
	
	public Exception lastError;
	private boolean first = false;
	
	public Object expr(String str) {
		return Parser.parse(str).eval(scope);
	}
	
	@func("compiles the given expression to the explicit representation")
	public String compile(String expr) {
		try {
			File f = new File(dir() + "//" + expr);
			if (f.exists()) {
				return loadExpression(expr).toCompiledString();
			}
		} catch (Exception e) {}
		return Parser.parse(expr).toCompiledString();
	}
	
	@func("compiles the given expression to bytes and prints them to the console")
	public Number[] bytecode(String expr) {
		byte[] bytes = Parser.parse(expr).toBytecode();
		Number[] nums = new Number[bytes.length];
		for (int i = 0; i < nums.length; i++) {
			nums[i] = Real.valueOf(bytes[i]);
		}
		return nums;
	}
	
	@func("compiles the given expression to bytes and saves them to the given file")
	public void bytecode(@param("expression") String expr,
			@param("filename") String filename) {
		byte[] bytes = Parser.parse(expr).toBytecode();
		try {
			Files.write(Paths.get(dir() + "\\" + filename), bytes);
		} catch (IOException e) {
			throw new CalculatorError(e);
		}
	}
	
	@func("converts explicit representation of expression to evaluatable string")
	public String decompile(String expr) {
		return CompiledParser.parse(expr).toEvalString();
	}
	
	@func("converts an array of bytes (bytecode) into an evaluatable string")
	public String decompile(Number[] arr) {
		byte[] bytes = new byte[arr.length];
		for (int i = 0; i < arr.length; i++) {
			check(arr[i] instanceof Real, TypeError);
			Real r = (Real) arr[i];
			check(r.doubleValue() == (byte) r.intValue(), TypeError);
			bytes[i] = (byte) r.doubleValue();
		}
		try {
			return BytecodeParser.parse(bytes).toEvalString();
		} catch (BytecodeException e) {
			throw new CalculatorError(e);
		}
	}
	
	public String removeComments(String s) {
		StringBuilder b = new StringBuilder();
		boolean inString = false, escape = false, inSLComment = false,
				inMLComment = false;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (inString) {
				if (escape) {
					escape = false;
				} else
					switch (c) {
					case '"':
						inString = false;
						break;
					case '\\':
						escape = true;
						break;
					}
				b.append(c);
			} else if (inSLComment) {
				switch (c) {
				case '\r':
					if (i + 1 < s.length() && s.charAt(i + 1) == '\n')
						i++;
				case '\n':
					inSLComment = false;
				}
			} else if (inMLComment) {
				if (c == '*' && i + 1 < s.length() && s.charAt(i + 1) == '/') {
					inMLComment = false;
					i++;
				}
			} else {
				switch (c) {
				case '/':
					if (i + 1 < s.length()) {
						switch (s.charAt(i + 1)) {
						case '*':
							i++;
							inMLComment = true;
							continue;
						case '/':
							i++;
							inSLComment = true;
							continue;
						}
					}
					break;
				case '"':
					inString = true;
				}
				b.append(c);
			}
		}
		return b.toString();
	}
	
	private void eval0(String str) {
		if ((str = removeComments(str.trim())).isEmpty())
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
		} catch (ConversionException e) {
			println("Error: units");
			lastError = e;
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
