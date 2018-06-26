package calculator;

import static calculator.TokenKind.*;

import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;

public class CompiledParser extends Parser {
	public static Expression parse(String s) {
		return new CompiledParser(s).parse();
	}
	
	public CompiledParser(String s) {
		super(s);
	}
	
	protected CompiledParser(Tokenizer tkzr) {
		super(tkzr);
	}
	
	private void accept(String word) {
		if (token.kind != WORD || !word.equals(token.stringValue()))
			throw new SyntaxError(token.pos,
					"expected '" + word + "', got " + token);
		nextToken();
	}
	
	private boolean eat(String word) {
		if (token.kind == WORD && token.stringValue().equals(word)) {
			nextToken();
			return true;
		}
		return false;
	}
	
	@Override
	public Expression parse() {
		nextToken();
		Expression result = expr();
		if (token.kind != EOF) {
			throw new SyntaxError(token);
		}
		return result;
	}
	
	@SneakyThrows
	Expression expr() {
		Expression e;
		switch (token.kind) {
		case LBRACE: {
			nextToken();
			ArrayList<Expression> elems = new ArrayList<>();
			Boolean columnLiteral = null;
			if (token.kind != RBRACE) {
				do {
					elems.add(expr());
					if (columnLiteral == null) {
						columnLiteral = token.kind == BAR;
					}
				} while (eat(columnLiteral? BAR : COMMA));
			}
			accept(RBRACE);
			Expression[] arr = elems.toArray(new Expression[0]);
			e = columnLiteral? new ExpressionColumnLiteral(arr)
					: new ExpressionArrayLiteral(arr);
			break;
		}
		case LPAREN: {
			nextToken();
			switch (token.kind) {
			case STRING:
				e = new ExpressionLiteral(token.stringValue());
				nextToken();
				break;
			case NUMBER:
				e = new ExpressionLiteral(token.doubleValue());
				nextToken();
				break;
			case LBRACKET: {
				nextToken();
				ArrayList<Number> numbers = new ArrayList<>();
				if (token.kind != RBRACKET) {
					do {
						if (token.kind != NUMBER)
							throw new SyntaxError(token);
						numbers.add(Real.valueOf(token.doubleValue()));
					} while (eat(COMMA));
				}
				accept(RBRACKET);
				e = new ExpressionLiteral(
						numbers.toArray(new Number[0]));
				break;
			}
			case WORD: {
				StringBuilder b =
						new StringBuilder(token.stringValue());
				nextToken();
				while (eat(DOT)) {
					if (token.kind != WORD)
						throw new SyntaxError(token);
					b.append('.').append(token.stringValue());
					nextToken();
				}
				String str = b.toString();
				int i = str.lastIndexOf('.');
				if (i == -1)
					throw new SyntaxError(token.pos, DOT, token.kind);
				String className = str.substring(0, i);
				String funcName = str.substring(i + 1);
				Class<?> clazz = Class.forName(className);
				e = new ExpressionLiteral(
						new MethodFunction(clazz, funcName));
				break;
			}
			default:
				if (tkzr.eater.eat("new Number[0]")) {
					nextToken();
					e = new ExpressionLiteral(() -> new Number[0]);
				} else {
					throw new SyntaxError(token);
				}
			}
			accept(RPAREN);
			break;
		}
		case LT: {
			nextToken();
			switch (token.kind) {
			case WORD: {
				String name = token.stringValue();
				switch (name) {
				case "SET": {
					nextToken();
					switch (token.kind) {
					case STRING: {
						String varname = token.stringValue();
						nextToken();
						accept("TO");
						Expression value = expr();
						e = new ExpressionAssign(varname, value);
						break;
					}
					case LT:
					case LPAREN:
					case LBRACE: {
						Expression array = expr();
						if (isVectorElem(array)
								&& token.kind != LBRACKET) {
							accept("TO");
							Expression value = expr();
							e = ((ExpressionReferenceable) array).toAssign(
									value);
						} else {
							accept(LBRACKET);
							Expression idx1 = expr();
							if (eat(COMMA)) {
								Expression idx2 = expr();
								accept(RBRACKET);
								accept("TO");
								Expression value = expr();
								e = new ExpressionMatrixIndexAssign(
										array, idx1, idx2, value);
							} else {
								accept(RBRACKET);
								accept("TO");
								Expression value = expr();
								e = new ExpressionIndexAssign(array,
										idx1, value);
							}
						}
						break;
					}
					case WORD:
						if (token.stringValue().equals("DIM")) {
							nextToken();
							if (token.kind != STRING)
								throw new SyntaxError(token);
							String varname = token.stringValue();
							nextToken();
							accept("TO");
							Expression value = expr();
							e = new ExpressionDimAssign(varname,
									value);
							break;
						}
					default:
						throw new SyntaxError(token);
					}
					break;
				}
				case "SUBTRACT":
				case "ADD": {
					nextToken();
					EnumOperator operator = EnumOperator.valueOf(name);
					Expression value = expr();
					accept(operator.getVerb());
					switch (token.kind) {
					case STRING: {
						String varname = token.stringValue();
						nextToken();
						e = new ExpressionAssignOp(varname, operator,
								value);
						break;
					}
					case LT:
					case LPAREN:
					case LBRACE: {
						Expression array = expr();
						if (isVectorElem(array)
								&& token.kind != LBRACKET) {
							e = ((ExpressionReferenceable) array).toAssignOp(
									operator, value);
						} else {
							accept(LBRACKET);
							Expression idx1 = expr();
							if (eat(COMMA)) {
								Expression idx2 = expr();
								accept(RBRACKET);
								e = new ExpressionMatrixIndexAssignOp(
										array, idx1, idx2, operator,
										value);
							} else {
								accept(RBRACKET);
								e = new ExpressionIndexAssignOp(array,
										idx1, operator, value);
							}
						}
						break;
					}
					default:
						throw new SyntaxError(token);
					}
					break;
				}
				case "DEC":
				case "INC": {
					boolean inc = name.equals("INC");
					nextToken();
					switch (token.kind) {
					case STRING: {
						String varname = token.stringValue();
						nextToken();
						e = inc? new ExpressionVarPrefixIncrement(
								varname)
								: new ExpressionVarPrefixDecrement(
										varname);
						break;
					}
					case LT:
					case LPAREN:
					case LBRACE: {
						Expression array = expr();
						if (isVectorElem(array)
								&& token.kind != LBRACKET) {
							ExpressionReferenceable ref =
									(ExpressionReferenceable) array;
							e = inc? ref.toPrefixIncrement()
									: ref.toPrefixDecrement();
						} else {
							accept(LBRACKET);
							Expression idx1 = expr();
							if (eat(COMMA)) {
								Expression idx2 = expr();
								accept(RBRACKET);
								e = inc? new ExpressionMatrixIndexPrefixIncrement(
										array, idx1, idx2)
										: new ExpressionMatrixIndexPrefixDecrement(
												array, idx1, idx2);
							} else {
								accept(RBRACKET);
								e = inc? new ExpressionIndexPrefixIncrement(
										array, idx1)
										: new ExpressionIndexPrefixDecrement(
												array, idx1);
							}
						}
						break;
					}
					default:
						throw new SyntaxError(token);
					}
					break;
				}
				case "BREAK":
					nextToken();
					e = new ExpressionBreak();
					break;
				case "SIZE":
					nextToken();
					e = new ExpressionDollar();
					break;
				case "CHAIN": {
					nextToken();
					ArrayList<Expression> exprs = new ArrayList<>();
					ArrayList<ExpressionComparisonChain.Op> ops =
							new ArrayList<>();
					exprs.add(expr());
					do {
						boolean elementWise = eat(DOT);
						if (token.kind != WORD)
							throw new SyntaxError(token);
						try {
							EnumOperator operator =
									EnumOperator.valueOf(
											token.stringValue());
							if (operator.cardinality != 2)
								throw new SyntaxError(token);
							ops.add(ExpressionComparisonChain.Op.of(
									operator, elementWise));
						} catch (IllegalArgumentException ex) {
							throw new SyntaxError(token);
						}
						nextToken();
						exprs.add(expr());
					} while (token.kind != GT && token.kind != EOF);
					e = new ExpressionComparisonChain(exprs, ops);
					break;
				}
				case "IFEXPR":
				case "IF": {
					boolean conditional = name.endsWith("IFEXPR");
					nextToken();
					Expression cond = expr();
					accept("THEN");
					Expression thenpart = expr(), elsepart;
					if (eat("ELSE")) {
						elsepart = expr();
					} else
						elsepart = null;
					e = conditional
							? new ExpressionConditional(cond, thenpart,
									elsepart)
							: new ExpressionIf(cond, thenpart,
									elsepart);
					break;
				}
				case "FOR": {
					nextToken();
					if (token.kind != STRING)
						throw new SyntaxError(token.pos, STRING,
								token.kind);
					String varname = token.stringValue();
					nextToken();
					accept("FROM");
					Expression start = expr();
					accept("TO");
					Expression end = expr(), increment;
					if (eat("INCREMENT")) {
						increment = expr();
					} else
						increment = null;
					accept("DO");
					Expression body = expr();
					e = new ExpressionFor(varname, start, end,
							increment, body);
					break;
				}
				case "FOREACH": {
					nextToken();
					if (token.kind != STRING)
						throw new SyntaxError(token.pos, STRING,
								token.kind);
					String varname = token.stringValue();
					nextToken();
					accept("IN");
					Expression array = expr();
					accept("DO");
					Expression body = expr();
					e = new ExpressionForEachSingle(varname, array,
							body);
					break;
				}
				case "FOREACH2": {
					nextToken();
					if (token.kind != STRING)
						throw new SyntaxError(token.pos, STRING,
								token.kind);
					String varname1 = token.stringValue();
					nextToken();
					accept(COMMA);
					if (token.kind != STRING)
						throw new SyntaxError(token.pos, STRING,
								token.kind);
					String varname2 = token.stringValue();
					nextToken();
					accept("IN");
					Expression array = expr();
					accept("DO");
					Expression body = expr();
					e = new ExpressionForEachDouble(varname1, varname2,
							array, body);
					break;
				}
				case "CALL": {
					nextToken();
					Expression function = expr();
					Expression[] args = new Expression[0];
					if (eat("WITH")) {
						accept(LPAREN);
						ArrayList<Expression> exprs =
								new ArrayList<>();
						do {
							exprs.add(expr());
						} while (eat(COMMA));
						accept(RPAREN);
						args = exprs.toArray(args);
					}
					e = new ExpressionFunctionCall(function, args);
					break;
				}
				case "FUNC": {
					nextToken();
					String funcname;
					if (eat("LAMBDA")) {
						funcname = null;
					} else {
						if (token.kind != STRING)
							throw new SyntaxError(token.pos, STRING,
									token.kind);
						funcname = token.stringValue();
						nextToken();
					}
					accept(LPAREN);
					List<String> argNamesList = new ArrayList<>();
					if (token.kind != RPAREN) {
						do {
							if (token.kind != WORD)
								throw new SyntaxError(token);
							argNamesList.add(token.stringValue());
							nextToken();
						} while (eat(COMMA));
					}
					accept(RPAREN);
					Expression body = expr();
					e = new ExpressionFunctionDefinition(
							new UserFunction(funcname,
									argNamesList.toArray(
											new String[0]),
									body));
					break;
				}
				case "MULTI": {
					nextToken();
					List<Expression> exprsList = new ArrayList<>();
					while (token.kind != GT) {
						exprsList.add(expr());
						accept(SEMI);
					}
					e = new ExpressionMulti(
							exprsList.toArray(new Expression[0]));
					break;
				}
				case "MULTIPLY_CHAIN": {
					nextToken();
					ArrayList<Expression> exprsList =
							new ArrayList<>();
					do {
						exprsList.add(expr());
					} while (eat(COMMA));
					e = new ExpressionMultiplyChain(exprsList);
					break;
				}
				case "RET": {
					nextToken();
					Expression expr;
					if (token.kind != GT) {
						expr = expr();
					} else
						expr = null;
					e = new ExpressionReturn(expr);
					break;
				}
				case "WHILE": {
					nextToken();
					Expression cond = expr();
					accept("DO");
					Expression body = expr();
					e = new ExpressionWhile(cond, body);
					break;
				}
				case "TRY": {
					Expression body = expr();
					accept("ELSE");
					Expression elsepart = expr();
					e = new ExpressionTry(body, elsepart);
				}
				case "DEL": {
					nextToken();
					if (token.kind != STRING)
						throw new SyntaxError(token.pos, STRING,
								token.kind);
					e = new ExpressionDeleteVariable(
							token.stringValue());
					nextToken();
					break;
				}
				case "LOCAL": {
					nextToken();
					List<ExpressionLocal.Def> defs = new ArrayList<>();
					do {
						defs.add(def());
					} while (eat(COMMA));
					
					e = new ExpressionLocal(
							defs.toArray(new ExpressionLocal.Def[0]));
					break;
				}
				case "ABS": {
					nextToken();
					e = new ExpressionAbs(expr());
					break;
				}
				default:
					try {
						EnumOperator operator =
								EnumOperator.valueOf(name);
						nextToken();
						switch (token.kind) {
						case STRING: {
							String varname = token.stringValue();
							nextToken();
							accept(operator.getVerb());
							Expression value = expr();
							e = new ExpressionAssignOp(varname,
									operator, value);
							break;
						}
						case LT:
						case LPAREN:
						case LBRACE: {
							Expression array = expr();
							if (eat(LBRACKET)) {
								Expression idx1 = expr();
								if (eat(COMMA)) {
									Expression idx2 = expr();
									accept(RBRACKET);
									accept(operator.getVerb());
									Expression value = expr();
									e = new ExpressionMatrixIndexAssignOp(
											array, idx1, idx2,
											operator, value);
									
								} else {
									accept(RBRACKET);
									accept(operator.getVerb());
									Expression value = expr();
									e = new ExpressionIndexAssignOp(
											array, idx1, operator,
											value);
								}
							} else {
								if (token.kind != GT)
									throw new SyntaxError(token);
								if (operator.cardinality != 1)
									throw new SyntaxError(token);
								e = new ExpressionUnaryOperator(
										operator, array);
							}
							break;
						}
						default:
							throw new SyntaxError(token);
						}
					} catch (IllegalArgumentException ex) {
						throw new SyntaxError(token.pos,
								"unknown operation: " + name);
					}
				}
				break;
			}
			case LT:
			case LPAREN:
			case LBRACE: {
				Expression lhs = expr();
				switch (token.kind) {
				case WORD: {
					String name = token.stringValue();
					if (isVectorElem(lhs)) {
						ExpressionReferenceable ref =
								(ExpressionReferenceable) lhs;
						if (name.equals("INC")) {
							e = ref.toPostfixIncrement();
							nextToken();
							break;
						} else if (name.equals("DEC")) {
							e = ref.toPostfixDecrement();
							nextToken();
							break;
						}
					}
					try {
						EnumOperator operator =
								EnumOperator.valueOf(name);
						if (operator.cardinality != 2)
							throw new SyntaxError(token);
						nextToken();
						Expression rhs = expr();
						e = new ExpressionBinaryOperator(lhs, operator,
								rhs);
					} catch (IllegalArgumentException ex) {
						throw new SyntaxError(token);
					}
					break;
				}
				case LBRACKET: {
					nextToken();
					Expression idx1 = expr();
					if (eat(COMMA)) {
						Expression idx2 = expr();
						accept(RBRACKET);
						if (eat("INC")) {
							e = new ExpressionMatrixIndexPostfixIncrement(
									lhs, idx1, idx2);
						} else {
							accept("DEC");
							e = new ExpressionMatrixIndexPostfixDecrement(
									lhs, idx1, idx2);
						}
					} else {
						accept(RBRACKET);
						if (eat("INC")) {
							e = new ExpressionIndexPostfixIncrement(
									lhs, idx1);
						} else {
							accept("DEC");
							e = new ExpressionIndexPostfixDecrement(
									lhs, idx1);
						}
					}
					break;
				}
				case DOT: {
					nextToken();
					if (token.kind != WORD)
						throw new SyntaxError(token);
					String name = token.stringValue();
					try {
						EnumOperator operator =
								EnumOperator.valueOf(name);
						if (operator.cardinality != 2)
							throw new SyntaxError(token);
						nextToken();
						Expression rhs = expr();
						e = new ExpressionElementwiseBinaryOperator(
								lhs, operator, rhs);
					} catch (IllegalArgumentException ex) {
						throw new SyntaxError(token);
					}
					break;
				}
				case GT:
					e = new ExpressionParenthesis(lhs);
					break;
				default:
					throw new SyntaxError(token);
				}
				break;
			}
			case DOT: {
				nextToken();
				if (token.kind != WORD)
					throw new SyntaxError(token.pos, WORD, token.kind);
				// nextToken();
				if (eat("X")) {
					e = new ExpressionX(expr());
				} else if (eat("Y")) {
					e = new ExpressionY(expr());
				} else {
					accept("Z");
					e = new ExpressionZ(expr());
				}
				break;
			}
			case STRING: {
				String name = token.stringValue();
				nextToken();
				switch (token.kind) {
				case WORD: {
					switch (token.stringValue()) {
					case "INC":
						nextToken();
						e = new ExpressionVarPostfixIncrement(name);
						break;
					case "DEC":
						nextToken();
						e = new ExpressionVarPostfixDecrement(name);
						break;
					default:
						throw new SyntaxError(token);
					}
					break;
				}
				case GT:
					e = new ExpressionVariable(name);
					break;
				default:
					throw new SyntaxError(token);
				}
				break;
			}
			default:
				throw new SyntaxError(token);
			}
			
			accept(GT);
			break;
		}
		
		default:
			throw new SyntaxError(token);
		}
		return e;
	}
	
	private ExpressionLocal.Def def() {
		accept(LT);
		if (token.kind != WORD)
			throw new SyntaxError(token.pos, WORD, token.kind);
		ExpressionLocal.Def result;
		switch (token.stringValue()) {
		case "DEF": {
			nextToken();
			if (token.kind != STRING)
				throw new SyntaxError(token.pos, STRING, token.kind);
			String varname = token.stringValue();
			nextToken();
			if (eat("IS")) {
				result = new ExpressionLocal.DefImpl(varname, expr());
			} else {
				result = new ExpressionLocal.DefImpl(varname);
			}
			break;
		}
		case "SET": {
			nextToken();
			accept("DIM");
			if (token.kind != STRING)
				throw new SyntaxError(token.pos, STRING, token.kind);
			String varname = token.stringValue();
			nextToken();
			accept("TO");
			result = new ExpressionLocal.DimDef(varname, expr());
			break;
		}
		case "FUNC": {
			nextToken();
			String funcname;
			
			if (token.kind != STRING)
				throw new SyntaxError(token.pos, STRING, token.kind);
			funcname = token.stringValue();
			nextToken();
			
			accept(LPAREN);
			List<String> argNamesList = new ArrayList<>();
			if (token.kind != RPAREN) {
				do {
					if (token.kind != WORD)
						throw new SyntaxError(token);
					argNamesList.add(token.stringValue());
					nextToken();
				} while (eat(COMMA));
			}
			accept(RPAREN);
			Expression body = expr();
			result = new ExpressionLocal.FuncDef(new UserFunction(
					funcname, argNamesList.toArray(new String[0]),
					body));
			break;
		}
		default:
			throw new SyntaxError(token);
		}
		accept(GT);
		return result;
	}
	
	private static boolean isVectorElem(Expression e) {
		return e instanceof ExpressionX || e instanceof ExpressionY
				|| e instanceof ExpressionZ;
	}
}
