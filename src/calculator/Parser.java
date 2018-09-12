package calculator;

import static calculator.TokenKind.*;
import static calculator.TokenKind.Section.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import calculator.errors.SyntaxError;
import calculator.expressions.Expression;
import calculator.expressions.ExpressionAbs;
import calculator.expressions.ExpressionArrayLiteral;
import calculator.expressions.ExpressionBinaryOperator;
import calculator.expressions.ExpressionBreak;
import calculator.expressions.ExpressionColumnLiteral;
import calculator.expressions.ExpressionComparisonChain;
import calculator.expressions.ExpressionConditional;
import calculator.expressions.ExpressionDeleteAll;
import calculator.expressions.ExpressionDeleteLocal;
import calculator.expressions.ExpressionDeleteVariable;
import calculator.expressions.ExpressionDimAssign;
import calculator.expressions.ExpressionDollar;
import calculator.expressions.ExpressionElementwiseBinaryOperator;
import calculator.expressions.ExpressionFor;
import calculator.expressions.ExpressionForEachDouble;
import calculator.expressions.ExpressionForEachSingle;
import calculator.expressions.ExpressionFunctionCall;
import calculator.expressions.ExpressionFunctionDefinition;
import calculator.expressions.ExpressionIf;
import calculator.expressions.ExpressionIndex;
import calculator.expressions.ExpressionLiteral;
import calculator.expressions.ExpressionLocal;
import calculator.expressions.ExpressionMatrixIndex;
import calculator.expressions.ExpressionMulti;
import calculator.expressions.ExpressionMultiplyChain;
import calculator.expressions.ExpressionParenthesis;
import calculator.expressions.ExpressionReferenceable;
import calculator.expressions.ExpressionReturn;
import calculator.expressions.ExpressionTry;
import calculator.expressions.ExpressionUnaryOperator;
import calculator.expressions.ExpressionVariable;
import calculator.expressions.ExpressionWhile;
import calculator.expressions.ExpressionX;
import calculator.expressions.ExpressionY;
import calculator.expressions.ExpressionZ;
import calculator.values.Complex;
import calculator.values.EnumOperator;
import calculator.values.MethodFunction;
import calculator.values.Number;
import calculator.values.Real;
import calculator.values.UserFunction;
import lombok.Getter;
import lombok.NonNull;

public class Parser {
	
	@FunctionalInterface
	public static interface ParserHookExpressionModifier {
		Expression test(Parser input, Expression current);
	}
	
	@FunctionalInterface
	public static interface ParserHookExpressionSupplier {
		Expression test(Parser input);
	}
	
	public static Expression parse(String s) {
		return new Parser(s).parse();
	}
	
	protected Tokenizer tkzr;
	@Getter
	protected Token token, lastToken;
	@Getter
	private boolean inIndex = false;
	@Getter
	HashMap<Token, Token> aliases = new HashMap<>();
	
	public Tokenizer getTokenizer() {
		return tkzr;
	}
	
	public Parser(String s) {
		tkzr = new Tokenizer(s);
	}
	
	protected Parser(Tokenizer tkzr) {
		this.tkzr = tkzr;
		token = tkzr.currentToken();
		lastToken = null;
	}
	
	public boolean isMultiplyChainExprDisabled() {
		return disableMultiplyChain;
	}
	
	public void disableMultiplyChain() {
		disableMultiplyChain = true;
	}
	
	public void reEnableMultiplyChain() {
		disableMultiplyChain = false;
	}
	
	public Parser reset(String s) {
		tkzr = new Tokenizer(s);
		inIndex = false;
		token = null;
		return this;
	}
	
	public void alias(Token selected, Token alias) {
		if (selected.kind == STRING && alias.kind == STRING) {
			Tokenizer tkzr = new Tokenizer(selected.stringValue());
			Token newtkn = tkzr.nextToken();
			if (tkzr.nextToken().kind != EOF)
				throw new SyntaxError(selected);
			selected = newtkn;
			tkzr = new Tokenizer(alias.stringValue());
			newtkn = tkzr.nextToken();
			if (tkzr.nextToken().kind != EOF)
				throw new SyntaxError(selected);
			alias = newtkn;
		}
		aliases.put(selected, alias);
	}
	
	public boolean unalias(Token selected) {
		if (selected.kind == STRING) {
			Tokenizer tkzr = new Tokenizer(selected.stringValue());
			Token newtkn = tkzr.nextToken();
			if (tkzr.nextToken().kind != EOF)
				throw new SyntaxError(selected);
			selected = newtkn;
		}
		return aliases.remove(selected) != null;
	}
	
	public void nextToken() {
		if (token == null || token.kind != EOF) {
			lastToken = token;
			token = tkzr.nextToken();
			
			Token temp = aliases.get(token);
			if (temp != null) {
				token = temp.at(token.pos, token.line);
			}
		}
	}
	
	protected boolean peekToken(TokenKind kind) {
		return tkzr.peekToken(1).kind == kind;
	}
	
	public boolean peekToken(int lookahead, TokenKind kind) {
		return tkzr.peekToken(lookahead).kind == kind;
	}
	
	public boolean eat(TokenKind kind) {
		if (token.kind == kind) {
			nextToken();
			return true;
		}
		return false;
	}
	
	public void accept(TokenKind kind) {
		if (!eat(kind))
			throw new SyntaxError(token, kind);
	}
	
	private boolean disableMultiplyChain = false;
	
	public void printInfoStr() {
		Printer.println("disableMultiplyChain = " + disableMultiplyChain);
		Printer.println("token = " + token);
		Printer.println("Aliases:");
		boolean found = false;
		for (Map.Entry<Token, Token> entry : aliases.entrySet()) {
			if (!found)
				found = true;
			Printer.println("\t" + entry.getKey() + " = " + entry.getValue());
		}
		if (!found)
			Printer.println("\t(none)");
	}
	
	private boolean indicatesStartOfExpr(TokenKind kind) {
		if (disableMultiplyChain)
			return false;
		switch (kind) { //@formatter:off
		case LBRACE: case LPAREN: case NUMBER: case WORD: case STRING:
		case BANG: case HASHTAG: case BAR:
		case SIN: case SINH: case COS: case COSH: case TAN: case TANH:
		case CSC: case CSCH: case SEC: case SECH: case COT: case COTH:
		case ASIN: case ASINH: case ACOS: case ACOSH: case ATAN: case ATANH:
		case ACSC: case ACSCH: case ASEC: case ASECH: case ACOT: case ACOTH:
		case IF: case FOR: case WHILE: case TRY: case LOCAL: case RETURN:
		case BREAK: case DIM: case COMPILE: case DECOMPILE: case BYTECODE: case ALIAS: case MOD:
		case e: case pi: case i: case NaN: case Infinity: //@formatter:on
			return true;
		default:
			return false;
		}
	}
	
	public Expression parse() {
		nextToken();
		if (token.kind == EOF) {
			return new ExpressionMulti();
		}
		Expression result = expr();
		if (token.kind != EOF) {
			throw new SyntaxError(token);
		}
		return result;
	}
	
	public Expression expr() {
		return multi();
	}
	
	public final List<ParserHookExpressionSupplier> multi_hooks_pre =
			new ArrayList<>();
	
	public Expression multi() {
		Expression e;
		for (ParserHookExpressionSupplier hook : multi_hooks_pre) {
			Expression expr = hook.test(this);
			if (expr != null) {
				return multi(expr);
			}
		}
		if (eat(RETURN)) {
			switch (token.kind) {
			case SEMI:
			case EOF:
			case RPAREN:
			case RBRACE:
			case RBRACKET:
			case ELSE:
			case COMMA:
				e = new ExpressionReturn(null);
				break;
			default:
				e = new ExpressionReturn(assign());
			}
		} else {
			switch (token.kind) {
			case IF:
				e = parseIf(this::assign, true);
				break;
			case LOCAL:
				e = parseLocal(this::assign);
				break;
			case TRY:
				e = parseTry(this::assign, true);
				break;
			case FOR:
				e = parseFor(this::assign);
				break;
			case WHILE:
				e = parseWhile(this::assign);
				break;
			case SEMI:
				nextToken();
				e = new ExpressionMulti();
				break;
			case EOF:
			case RPAREN:
			case RBRACE:
			case RBRACKET:
			case ELSE:
			case COMMA:
				return new ExpressionMulti();
			default:
				e = assign();
			}
		}
		return multi(e);
	}
	
	public Expression multi(Expression e) {
		if (eat(SEMI)) {
			if (indicatesStartOfExpr(
					token.kind)/*token.kind != EOF && token.kind != RPAREN && token.kind != RBRACE && token.kind != RBRACKET*/) {
				List<Expression> list = new ArrayList<>();
				list.add(e);
				labelA:
				if (token.kind != SEMI) {
					for (ParserHookExpressionSupplier hook : multi_hooks_pre) {
						Expression expr = hook.test(this);
						if (expr != null) {
							list.add(expr);
							break labelA;
						}
					}
					if (eat(RETURN)) {
						switch (token.kind) {
						case SEMI:
						case EOF:
						case RPAREN:
						case RBRACE:
						case RBRACKET:
						case ELSE:
						case COMMA:
							list.add(new ExpressionReturn(null));
							break;
						default:
							list.add(new ExpressionReturn(assign()));
						}
					} else {
						switch (token.kind) {
						case IF:
							list.add(parseIf(this::assign, true));
							break;
						case LOCAL:
							list.add(parseLocal(this::assign));
							break;
						case TRY:
							list.add(parseTry(this::assign, true));
							break;
						case FOR:
							list.add(parseFor(this::assign));
							break;
						case WHILE:
							list.add(parseWhile(this::assign));
							break;
						case ELSE:
							break labelA;
						default:
							list.add(assign());
						}
						
					}
				}
				label0:
				while (eat(SEMI)) {
					switch (token.kind) {
					case SEMI:
						break;
					case EOF:
					case RPAREN:
					case RBRACE:
					case RBRACKET:
					case ELSE:
					case COMMA:
						break label0;
					case RETURN:
						nextToken();
						switch (token.kind) {
						case SEMI:
						case EOF:
						case RPAREN:
						case RBRACE:
						case RBRACKET:
						case ELSE:
						case COMMA:
							list.add(new ExpressionReturn(null));
							break;
						default:
							list.add(new ExpressionReturn(assign()));
						}
						break;
					default:
						list.add(assign());
					}
				}
				
				e = new ExpressionMulti(list.toArray(new Expression[0]));
			} else {
				e = new ExpressionMulti(e);
			}
		}
		return e;
	}
	
	public Expression assign() {
		Expression e;
		if (eat(RETURN)) {
			switch (token.kind) {
			case SEMI:
			case EOF:
			case RPAREN:
			case RBRACE:
			case RBRACKET:
			case ELSE:
			case COMMA:
				e = new ExpressionReturn(null);
				break;
			default:
				e = new ExpressionReturn(assign());
			}
		} else {
			switch (token.kind) {
			case IF:
				e = parseIf(this::assign, true);
				break;
			case LOCAL:
				e = parseLocal(this::assign);
				break;
			case TRY:
				e = parseTry(this::assign, true);
				break;
			case FOR:
				e = parseFor(this::assign);
				break;
			case WHILE:
				e = parseWhile(this::assign);
				break;
			default:
				e = conditional();
			}
		}
		
		return assign(e);
	}
	
	public final List<ParserHookExpressionModifier> assign_hooks_pre =
			new ArrayList<>(), assign_hooks_post = new ArrayList<>();
	
	@SuppressWarnings("incomplete-switch")
	public Expression assign(Expression e) {
		for (ParserHookExpressionModifier hook : assign_hooks_pre) {
			Expression expr = hook.test(this, e);
			if (expr != null && expr != e) {
				return expr;
			}
		}
		switch (token.kind) {
		case PLUSEQ:
		case SUBEQ:
		case STAREQ:
		case SLASHEQ:
		case CARETEQ:
		case BAREQ:
		case AMPEQ:
			e = e.skipParens();
			if (e instanceof ExpressionReferenceable) {
				EnumOperator operator = token.kind.getBinaryOperator();
				nextToken();
				e = ((ExpressionReferenceable) e).toAssignOp(operator,
						assignRight());
			} else {
				throw new SyntaxError(token.pos,
						"expected reference, got " + e.toEvalString());
			}
			break;
		case DOTEQ:
			e = e.skipParens();
			if (e instanceof ExpressionReferenceable) {
				nextToken();
				if (token.kind != WORD) {
					throw new SyntaxError(token.pos, WORD, token.kind);
				}
				ExpressionReferenceable ref = (ExpressionReferenceable) e;
				String name = token.stringValue();
				nextToken();
				ArrayList<Expression> args = new ArrayList<>();
				args.add(e);
				if (eat(LPAREN)) {
					if (token.kind != RPAREN) {
						do {
							args.add(expr());
						} while (eat(COMMA));
					}
					accept(RPAREN);
				}
				e = ref.toAssign(assign(conditional(or(and(eq(
						comp(add(mul(mul2(postfix(new ExpressionFunctionCall(
								new ExpressionVariable(name), args.toArray(
										new Expression[0])))))))))))));
			} else {
				throw new SyntaxError(token.pos,
						"expected reference, got " + e.toEvalString());
			}
			break;
		case EQ:
			e = e.skipParens();
			
			if (e instanceof ExpressionReferenceable) {
				nextToken();
				e = ((ExpressionReferenceable) e).toAssign(assignRight());
			} else {
				label0:
				if (e instanceof ExpressionFunctionCall) {
					ExpressionFunctionCall call = (ExpressionFunctionCall) e;
					if (call.function instanceof ExpressionVariable) {
						String funcname =
								((ExpressionVariable) call.function).variable;
						Set<String> args = new LinkedHashSet<>();
						for (Expression arg : call.args) {
							if (!(arg instanceof ExpressionVariable)
									|| funcname.equals(
											((ExpressionVariable) arg).variable)
									|| !args.add(
											((ExpressionVariable) arg).variable)) {
								break label0;
							}
						}
						accept(EQ);
						String[] params = new String[args.size()];
						int i = 0;
						for (String str : args) {
							params[i++] = str;
						}
						Expression body = assign();
						if (body instanceof ExpressionMulti)
							body = new ExpressionParenthesis(body);
						UserFunction function =
								new UserFunction(funcname, params, body);
						e = new ExpressionFunctionDefinition(function);
						break;
					} else if (call.function instanceof ExpressionLiteral) {
						ExpressionLiteral literal =
								((ExpressionLiteral) call.function);
						if (literal.constantValue) {
							Object value = literal.getValue();
							if (value == TokenKind.DIM.getFunction()) {
								if (call.args.length != 1) {
									throw new SyntaxError(token.pos,
											"too many arguments to dim()");
								}
								if (!(call.args[0] instanceof ExpressionVariable))
									throw new SyntaxError(token.pos,
											"expected reference, got "
													+ call.args[0].toEvalString());
								accept(EQ);
								e = new ExpressionDimAssign(
										((ExpressionVariable) call.args[0]).variable,
										assign());
								break;
							}
						}
					}
				} /* else if (e instanceof ExpressionDim) {
					ExpressionDim dim = (ExpressionDim) e;
					if (!(dim.array instanceof ExpressionVariable))
						throw new SyntaxError(token.pos,
								"expected reference, got "
										+ dim.array.toEvalString());
					
					accept(EQ);
					e = new ExpressionDimAssign(
							((ExpressionVariable) dim.array).variable,
							assign());
					break;
					}*/
				
				throw new SyntaxError(token.pos, "expected reference, got "
						+ e.toEvalString() + " ( " + e.getClass() + " )");
			}
			break;
		default:
			for (ParserHookExpressionModifier hook : assign_hooks_post) {
				Expression expr = hook.test(this, e);
				if (expr != null && expr != e) {
					return expr;
				}
			}
		}
		
		return e;
	}
	
	public Expression assignRight() {
		if (token.kind == LPAREN && (peekToken(RPAREN) && peekToken(2, ARROW)
				|| (peekToken(WORD) && (peekToken(2, COMMA)
						|| peekToken(2, RPAREN) && peekToken(3, ARROW))))) {
			return parseLambda(this::assign);
		} else {
			if (eat(RETURN)) {
				switch (token.kind) {
				case SEMI:
				case EOF:
				case RPAREN:
				case RBRACE:
				case RBRACKET:
				case ELSE:
				case COMMA:
					return new ExpressionReturn(null);
				default:
					return new ExpressionReturn(assign());
				}
			} else {
				switch (token.kind) {
				case IF:
					return parseIf(this::conditional, true);
				case LOCAL:
					return parseLocal(this::conditional);
				case TRY:
					return parseTry(this::conditional, true);
				case FOR:
					return parseFor(this::conditional);
				case WHILE:
					return parseWhile(this::conditional);
				default:
					return assign();
				}
			}
		}
	}
	
	public Expression conditional() {
		return conditional(mul3());
	}
	
	public final List<ParserHookExpressionModifier> conditional_hooks_pre =
			new ArrayList<>(), conditional_hooks_post = new ArrayList<>();
	
	public Expression conditional(Expression e) {
		for (ParserHookExpressionModifier hook : conditional_hooks_pre) {
			Expression expr = hook.test(this, e);
			if (expr != null && expr != e) {
				return expr;
			}
		}
		if (eat(QUES)) {
			Expression thenpart = assign();
			accept(COLON);
			Expression elsepart = assign();
			e = new ExpressionConditional(e, thenpart, elsepart);
		}
		for (ParserHookExpressionModifier hook : conditional_hooks_post) {
			Expression expr = hook.test(this, e);
			if (expr != null && expr != e) {
				return expr;
			}
		}
		
		return e;
	}
	
	public Expression mul3() {
		Expression e = or();
		outer:
		for (;;) {
			if (eat(LPAREN)) {
				List<Expression> args = new ArrayList<>();
				if (token.kind != RPAREN) {
					do {
						args.add(expr());
					} while (eat(COMMA));
				}
				accept(RPAREN);
				if (args.size() == 1) {
					Expression rhs;
					if (eat(CARET)) {
						rhs = new ExpressionBinaryOperator(args.get(0),
								EnumOperator.POW, prefix());
					} else if (eat(DOTCARET)) {
						rhs = new ExpressionElementwiseBinaryOperator(args.get(0),
								EnumOperator.POW, prefix());
					} else
						rhs = args.get(0);
					
					if (e instanceof ExpressionMultiplyChain) {
						((ExpressionMultiplyChain) e).exprs.add(rhs);
					} else {
						ArrayList<Expression> list = new ArrayList<>();
						list.add(e);
						list.add(rhs);
						e = new ExpressionMultiplyChain(list);
					}
					continue;
					
				}
				if (e instanceof ExpressionMultiplyChain) {
					ExpressionMultiplyChain chain = (ExpressionMultiplyChain) e;
					int last = chain.exprs.size() - 1;
					chain.exprs.set(last, new ExpressionFunctionCall(
							chain.exprs.get(last), args.toArray(new Expression[0])));
				} else {
					e = new ExpressionFunctionCall(e,
							args.toArray(new Expression[0]));
				}
			} else if (token.kind != BAR && indicatesStartOfExpr(token.kind)) {
				if (e instanceof ExpressionMultiplyChain) {
					((ExpressionMultiplyChain) e).exprs.add(or());
				} else {
					ArrayList<Expression> list = new ArrayList<>();
					list.add(e);
					list.add(or());
					e = new ExpressionMultiplyChain(list);
				}
			} else {
				return e;
			}
		}
	}
	
	public Expression or() {
		return or(and());
	}
	
	public final List<ParserHookExpressionModifier> or_hooks_pre = new ArrayList<>(),
			or_hooks_post = new ArrayList<>();
	
	public Expression or(Expression e) {
		outer:
		for (;;) {
			for (ParserHookExpressionModifier hook : or_hooks_pre) {
				Expression expr = hook.test(this, e);
				if (expr != null && expr != e) {
					e = expr;
					continue outer;
				}
			}
			if (eat(BAR))
				e = new ExpressionBinaryOperator(e, EnumOperator.OR, and());
			else if (eat(DOTBAR))
				e = new ExpressionElementwiseBinaryOperator(e, EnumOperator.OR,
						and());
			else {
				for (ParserHookExpressionModifier hook : or_hooks_post) {
					Expression expr = hook.test(this, e);
					if (expr != null && expr != e) {
						e = expr;
						continue outer;
					}
				}
				return e;
			}
		}
	}
	
	public Expression and() {
		return and(eq());
	}
	
	public final List<ParserHookExpressionModifier> and_hooks_pre =
			new ArrayList<>(), and_hooks_post = new ArrayList<>();
	
	public Expression and(Expression e) {
		outer:
		for (;;) {
			for (ParserHookExpressionModifier hook : and_hooks_pre) {
				Expression expr = hook.test(this, e);
				if (expr != null && expr != e) {
					e = expr;
					continue outer;
				}
			}
			if (eat(AMP))
				e = new ExpressionBinaryOperator(e, EnumOperator.AND, eq());
			else if (eat(DOTAMP))
				e = new ExpressionElementwiseBinaryOperator(e, EnumOperator.AND,
						eq());
			else {
				for (ParserHookExpressionModifier hook : and_hooks_post) {
					Expression expr = hook.test(this, e);
					if (expr != null && expr != e) {
						e = expr;
						continue outer;
					}
				}
				return e;
			}
		}
	}
	
	public Expression eq() {
		return eq(comp());
	}
	
	public final List<ParserHookExpressionModifier> eq_hooks_pre = new ArrayList<>(),
			eq_hooks_post = new ArrayList<>();
	
	public Expression eq(Expression e) {
		outer:
		for (;;) {
			for (ParserHookExpressionModifier hook : eq_hooks_pre) {
				Expression expr = hook.test(this, e);
				if (expr != null && expr != e) {
					e = expr;
					continue outer;
				}
			}
			switch (token.kind) {
			case EQEQ:
			case DOTEQEQ:
			case BANGEQ:
			case DOTBANGEQ:
				boolean elementWise = ELEMENTWISE_OPERATORS.contains(token.kind);
				EnumOperator operator = token.kind.getBinaryOperator();
				nextToken();
				if (elementWise) {
					e = new ExpressionElementwiseBinaryOperator(e, operator, comp());
				} else {
					e = new ExpressionBinaryOperator(e, operator, comp());
				}
				break;
			default:
				for (ParserHookExpressionModifier hook : eq_hooks_post) {
					Expression expr = hook.test(this, e);
					if (expr != null && expr != e) {
						e = expr;
						continue outer;
					}
				}
				return e;
			}
		}
	}
	
	public Expression comp() {
		return comp(convert());
	}
	
	public final List<ParserHookExpressionModifier> comp_hooks_pre =
			new ArrayList<>(), comp_hooks_post = new ArrayList<>();
	
	public Expression comp(Expression e) {
		outer:
		for (;;) {
			for (ParserHookExpressionModifier hook : comp_hooks_pre) {
				Expression expr = hook.test(this, e);
				if (expr != null && expr != e) {
					e = expr;
					continue outer;
				}
			}
			switch (token.kind) {
			case DOTLT:
			case DOTGT:
			case DOTLTEQ:
			case DOTGTEQ:
			case LT:
			case GT:
			case LTEQ:
			case GTEQ: {
				@NonNull
				EnumOperator operator = token.kind.getBinaryOperator();
				boolean elementWise = ELEMENTWISE_OPERATORS.contains(token.kind);
				nextToken();
				Expression other = convert();
				if (e instanceof ExpressionComparisonChain) {
					ExpressionComparisonChain chain = (ExpressionComparisonChain) e;
					chain.exprs.add(other);
					chain.operators.add(
							ExpressionComparisonChain.Op.of(operator, elementWise));
				} else {
					ArrayList<Expression> exprs = new ArrayList<>();
					ArrayList<ExpressionComparisonChain.Op> operators =
							new ArrayList<>();
					exprs.add(e);
					exprs.add(other);
					operators.add(
							ExpressionComparisonChain.Op.of(operator, elementWise));
					e = new ExpressionComparisonChain(exprs, operators);
				}
				break;
			}
			default:
				for (ParserHookExpressionModifier hook : comp_hooks_post) {
					Expression expr = hook.test(this, e);
					if (expr != null && expr != e) {
						e = expr;
						continue outer;
					}
				}
				if (e instanceof ExpressionComparisonChain) {
					ExpressionComparisonChain chain = (ExpressionComparisonChain) e;
					if (chain.exprs.size() == 2) {
						assert chain.operators.size() == 1;
						ExpressionComparisonChain.Op op = chain.operators.get(0);
						if (op.elementWise) {
							e = new ExpressionElementwiseBinaryOperator(
									chain.exprs.get(0), op.operator,
									chain.exprs.get(1));
						} else {
							e = new ExpressionBinaryOperator(chain.exprs.get(0),
									op.operator, chain.exprs.get(1));
						}
					}
				}
				return e;
			}
		}
	}
	
	public Expression convert() {
		Expression e = add();
		while (eat(SLIMARROW)) {
			e = new ExpressionBinaryOperator(e, EnumOperator.CONVERT, add());
		}
		return e;
	}
	
	public Expression add() {
		return add(mul());
	}
	
	public final List<ParserHookExpressionModifier> add_hooks_pre =
			new ArrayList<>(), add_hooks_post = new ArrayList<>();
	
	public Expression add(Expression e) {
		outer:
		for (;;) {
			for (ParserHookExpressionModifier hook : add_hooks_pre) {
				Expression expr = hook.test(this, e);
				if (expr != null && expr != e) {
					e = expr;
					continue outer;
				}
			}
			switch (token.kind) {
			case PLUS:
			case DOTPLUS:
			case SUB:
			case DOTSUB:
				boolean elementWise = ELEMENTWISE_OPERATORS.contains(token.kind);
				EnumOperator operator = token.kind.getBinaryOperator();
				nextToken();
				if (elementWise) {
					e = new ExpressionElementwiseBinaryOperator(e, operator, mul());
				} else {
					e = new ExpressionBinaryOperator(e, operator, mul());
				}
				break;
			default:
				for (ParserHookExpressionModifier hook : add_hooks_post) {
					Expression expr = hook.test(this, e);
					if (expr != null && expr != e) {
						e = expr;
						continue outer;
					}
				}
				return e;
			}
		}
	}
	
	public Expression mul() {
		return mul(mul2());
	}
	
	public final List<ParserHookExpressionModifier> mul_hooks_pre =
			new ArrayList<>(), mul_hooks_post = new ArrayList<>();
	
	public Expression mul(Expression e) {
		boolean firstWasNumber = e instanceof ExpressionLiteral
				&& ((ExpressionLiteral) e).constantValue
				&& ((ExpressionLiteral) e).value.get() instanceof Number;
		outer:
		for (;;) {
			for (ParserHookExpressionModifier hook : mul_hooks_pre) {
				Expression expr = hook.test(this, e);
				if (expr != null && expr != e) {
					e = expr;
					continue outer;
				}
			}
			if (firstWasNumber) {
				if (eat(LPAREN)) {
					List<Expression> args = new ArrayList<>();
					if (token.kind != RPAREN) {
						do {
							args.add(expr());
						} while (eat(COMMA));
					}
					accept(RPAREN);
					if (args.size() == 1) {
						Expression rhs;
						if (eat(CARET)) {
							rhs = new ExpressionBinaryOperator(args.get(0),
									EnumOperator.POW, prefix());
						} else if (eat(DOTCARET)) {
							rhs = new ExpressionElementwiseBinaryOperator(
									args.get(0), EnumOperator.POW, prefix());
						} else
							rhs = args.get(0);
						
						if (e instanceof ExpressionMultiplyChain) {
							((ExpressionMultiplyChain) e).exprs.add(rhs);
						} else {
							ArrayList<Expression> list = new ArrayList<>();
							list.add(e);
							list.add(rhs);
							e = new ExpressionMultiplyChain(list);
						}
						continue;
						
					}
					if (e instanceof ExpressionMultiplyChain) {
						ExpressionMultiplyChain chain = (ExpressionMultiplyChain) e;
						int last = chain.exprs.size() - 1;
						chain.exprs.set(last,
								new ExpressionFunctionCall(chain.exprs.get(last),
										args.toArray(new Expression[0])));
					} else {
						e = new ExpressionFunctionCall(e,
								args.toArray(new Expression[0]));
					}
				} else if (token.kind != BAR && indicatesStartOfExpr(token.kind)) {
					if (e instanceof ExpressionMultiplyChain) {
						((ExpressionMultiplyChain) e).exprs.add(mul2());
					} else {
						ArrayList<Expression> list = new ArrayList<>();
						list.add(e);
						list.add(mul2());
						e = new ExpressionMultiplyChain(list);
					}
				} else {
					for (ParserHookExpressionModifier hook : mul_hooks_post) {
						Expression expr = hook.test(this, e);
						if (expr != null && expr != e) {
							e = expr;
							continue outer;
						}
					}
					return e;
				}
			} else
				return e;
		}
	}
	
	public Expression mul2() {
		return mul2(prefix());
	}
	
	public final List<ParserHookExpressionModifier> mul2_hooks_pre =
			new ArrayList<>(), mul2_hooks_post = new ArrayList<>();
	
	public Expression mul2(Expression e) {
		outer:
		for (;;) {
			for (ParserHookExpressionModifier hook : mul2_hooks_pre) {
				Expression expr = hook.test(this, e);
				if (expr != null && expr != e) {
					e = expr;
					continue outer;
				}
			}
			switch (token.kind) {
			case MOD:
				if (peekToken(EOF))
					return e;
			case STAR:
			case DOTSTAR:
			case SLASH:
			case DOTSLASH:
			case CROSS:
			
			case DOTMOD:
				boolean elementWise = ELEMENTWISE_OPERATORS.contains(token.kind);
				EnumOperator operator = token.kind.getBinaryOperator();
				nextToken();
				if (elementWise) {
					e = new ExpressionElementwiseBinaryOperator(e, operator,
							prefix());
				} else {
					e = new ExpressionBinaryOperator(e, operator, prefix());
				}
				break;
			default:
				for (ParserHookExpressionModifier hook : mul2_hooks_post) {
					Expression expr = hook.test(this, e);
					if (expr != null && expr != e) {
						e = expr;
						continue outer;
					}
				}
				return e;
			}
		}
	}
	
	public final List<ParserHookExpressionSupplier> prefix_hooks_pre =
			new ArrayList<>(), prefix_hooks_post = new ArrayList<>();
	
	public Expression prefix() {
		for (ParserHookExpressionSupplier hook : prefix_hooks_pre) {
			Expression expr = hook.test(this);
			if (expr != null) {
				return expr;
			}
		}
		switch (token.kind) {
		case HASHTAG:
		case BANG:
		case SUB: {
			EnumOperator operator = token.kind.getUnaryOperator();
			nextToken();
			return new ExpressionUnaryOperator(operator, pow());
		}
		case DEGREE:
			nextToken();
			return new ExpressionUnaryOperator(EnumOperator.DEGREES_PREFIX, pow());
		case SUBSUB: {
			Expression e = pow().skipParens();
			if (e instanceof ExpressionReferenceable)
				return ((ExpressionReferenceable) e).toPrefixDecrement();
			else
				throw new SyntaxError(token.pos,
						"expected reference, got " + e.toEvalString());
		}
		case PLUSPLUS: {
			Expression e = pow().skipParens();
			if (e instanceof ExpressionReferenceable)
				return ((ExpressionReferenceable) e).toPrefixIncrement();
			else
				throw new SyntaxError(token.pos,
						"expected reference, got " + e.toEvalString());
		}
		default:
			for (ParserHookExpressionSupplier hook : prefix_hooks_post) {
				Expression expr = hook.test(this);
				if (expr != null) {
					return expr;
				}
			}
			return pow();
		}
	}
	
	public Expression pow() {
		return pow(factor());
	}
	
	public final List<ParserHookExpressionModifier> pow_hooks_pre =
			new ArrayList<>(), pow_hooks_post = new ArrayList<>();
	
	public Expression pow(Expression e) {
		for (ParserHookExpressionModifier hook : pow_hooks_pre) {
			Expression expr = hook.test(this, e);
			if (expr != null && expr != e) {
				return expr;
			}
		}
		if (eat(CARET))
			e = new ExpressionBinaryOperator(e, EnumOperator.POW, prefix());
		else if (eat(DOTCARET))
			e = new ExpressionElementwiseBinaryOperator(e, EnumOperator.POW,
					prefix());
		for (ParserHookExpressionModifier hook : pow_hooks_post) {
			Expression expr = hook.test(this, e);
			if (expr != null && expr != e) {
				return expr;
			}
		}
		return e;
	}
	/*static Map<String, MethodFunction> trigFuncs;
	
	static {
		trigFuncs = new HashMap<>();
		String[] funcNames = {"SIN", "SINH", "COS", "COSH", "TAN",
				"TANH", "CSC", "CSCH", "SEC", "SECH", "COT", "COTH",
				"ASIN", "ASINH", "ACOS", "ACOSH", "ATAN", "ATANH",
				"ACSC", "ACSCH", "ASEC", "ASECH", "ACOT", "ACOTH"};
		for (String funcName : funcNames) {
			funcName = funcName.toLowerCase();
			MethodFunction func = new MethodFunction(Functions.class,
					funcName);
			trigFuncs.put(funcName, func);
		}
	}*/
	
	public List<Expression> parseCommas() {
		List<Expression> result = new ArrayList<>();
		do {
			if (token.kind == COMMA || token.kind == EOF) {
				result.add(new ExpressionLiteral(""));
			} else {
				result.add(expr());
			}
			
		} while (eat(COMMA));
		return result;
	}
	
	public final List<ParserHookExpressionSupplier> factor_hooks_pre =
			new ArrayList<>(), factor_hooks_post = new ArrayList<>();
	
	public Expression factor() {
		for (ParserHookExpressionSupplier hook : factor_hooks_pre) {
			Expression expr = hook.test(this);
			if (expr != null) {
				return postfix(expr);
			}
		}
		Expression e;
		switch (token.kind) {
		case NUMBER:
			e = new ExpressionLiteral(Real.valueOf(token.doubleValue()));
			nextToken();
			break;
		case STRING:
			e = new ExpressionLiteral(token.stringValue());
			nextToken();
			break;
		case DIM:
			e = new ExpressionLiteral(token.kind.getFunction());
			nextToken();
			if (eat(LPAREN)) {
				e = new ExpressionFunctionCall(e, new Expression[] {expr()});
				accept(RPAREN);
			}
			break;
		
		case BYTECODE:
		case COMPILE:
			e = new ExpressionLiteral(token.kind.getFunction());
			boolean bytecode = token.kind == BYTECODE;
			nextToken();
			
			if (eat(LPAREN)) {
				Expression result;
				switch (token.kind) {
				case RBRACE:
				case RBRACKET:
				case RPAREN:
					throw new SyntaxError(token);
				case STRING:
				case WORD:
					if (peekToken(RPAREN) || bytecode && peekToken(COMMA)) {
						// e = new ExpressionFunctionCall(e, factor());
						result = factor();
						break;
					}
				default:
					result = new ExpressionLiteral(expr().toEvalString());
					/*if (bytecode) {
						Expression ex = new ExpressionLiteral(expr().toEvalString());
						if (eat(COMMA)) {
							e = new ExpressionFunctionCall(e, ex, expr());
						} else {
							e = new ExpressionFunctionCall(e, ex);
						}
					} else
						e = new ExpressionFunctionCall(e,
								new ExpressionLiteral(expr().toEvalString()));*/
				}
				if (bytecode && eat(COMMA)) {
					e = new ExpressionFunctionCall(e, result, expr());
				} else {
					e = new ExpressionFunctionCall(e, result);
				}
				accept(RPAREN);
			}
			break;
		case DECOMPILE:
			e = new ExpressionLiteral(token.kind.getFunction());
			nextToken();
			if (eat(LPAREN)) {
				switch (token.kind) {
				case LT:
				case LPAREN:
				case LBRACE:
					// try {
					Tokenizer temp = tkzr.dup();
					Expression arg = new CompiledParser(temp).expr();
					e = new ExpressionLiteral(arg.toEvalString());
					tkzr = temp;
					token = tkzr.currentToken();
					break;
					
				// case WORD:
				// case STRING:
				default:
					e = new ExpressionFunctionCall(e, expr());
					break;
				// default:
				// throw new SyntaxError(token);
				}
				accept(RPAREN);
			}
			break;
		case MOD:
			e = new ExpressionLiteral(token.kind.getBinaryOperator());
			nextToken();
			if (eat(LPAREN)) {
				e = new ExpressionFunctionCall(e, expr());
				accept(RPAREN);
			}
			break;
		case ALIAS:
			e = new ExpressionLiteral(token.kind.getFunction());
			token = tkzr.nextToken();
			if (token.kind == LPAREN) {
				Token selected = tkzr.nextToken();
				if (selected.kind == EOF || selected.kind == ALIAS
						|| selected.kind == NUMBER)
					throw new SyntaxError(selected);
				token = tkzr.nextToken();
				if (token.kind == COMMA) {
					Token alias = tkzr.nextToken();
					if (alias.kind == EOF)
						throw new SyntaxError(alias);
					if (selected.kind == STRING && alias.kind != STRING)
						throw new SyntaxError(alias.pos, STRING, alias.kind);
					alias(selected, alias);
					e = new ExpressionFunctionCall(e,
							new ExpressionLiteral(selected.toString()),
							new ExpressionLiteral(alias.toString()));
					token = tkzr.nextToken();
				} else {
					unalias(selected);
					e = new ExpressionFunctionCall(e,
							new ExpressionLiteral(selected.toString()));
				}
				Printer.printedLine = true;
				
				if (token.kind != RPAREN)
					throw new SyntaxError(token.pos, RPAREN, token.kind);
				nextToken();
			} else {
				Token temp = aliases.get(token);
				if (temp != null)
					token = temp.at(token.pos, token.line);
			}
			break;
		//@formatter:off
		case SIN: case SINH: case COS: case COSH: case TAN: case TANH:
		case CSC: case CSCH: case SEC: case SECH: case COT: case COTH:
		case ASIN: case ASINH: case ACOS: case ACOSH: case ATAN: case ATANH:
		case ACSC: case ACSCH: case ASEC: case ASECH: case ACOT: case ACOTH:
		//@formatter:on
		{
			MethodFunction func = token.kind.getFunction();
			nextToken();
			e = new ExpressionLiteral(func);
			if (indicatesStartOfExpr(token.kind))
				e = new ExpressionFunctionCall(e, new Expression[] {prefix()});
			break;
		}
		case IF:
			e = parseIf(this::prefix, false);
			break;
		case LOCAL:
			e = parseLocal(this::prefix);
			break;
		case TRY:
			e = parseTry(this::prefix, false);
			break;
		case FOR:
			e = parseFor(this::prefix);
			break;
		case WHILE:
			e = parseWhile(this::prefix);
			break;
		case BREAK:
			nextToken();
			e = new ExpressionBreak();
			break;
		case DELETE:
			nextToken();
			
			if (eat(STAR)) {
				e = new ExpressionDeleteAll();
			} else if (eat(LOCAL)) {
				e = new ExpressionDeleteLocal();
			} else {
				if (token.kind != WORD)
					throw new SyntaxError(token.pos, WORD, token.kind);
				
				e = new ExpressionDeleteVariable(token.stringValue());
				nextToken();
			}
			break;
		case RETURN:
			nextToken();
			switch (token.kind) {
			case SEMI:
			case EOF:
			case RPAREN:
			case RBRACE:
			case RBRACKET:
			case ELSE:
			case COMMA:
				e = new ExpressionReturn(null);
				break;
			default:
				e = new ExpressionReturn(prefix());
			}
			break;
		case WORD: {
			String funcname = token.stringValue();
			nextToken();
			e = new ExpressionVariable(funcname);
			if (funcname.equals("load") && token.kind == LT) {
				StringBuilder b = new StringBuilder("<");
				nextToken();
				if (token.kind != WORD) {
					accept(WORD);
				}
				b.append(token.stringValue());
				nextToken();
				if (eat(DOT)) {
					b.append('.');
					if (token.kind != WORD) {
						accept(WORD);
					}
					b.append(token.stringValue());
					nextToken();
				}
				accept(GT);
				b.append('>');
				e = new ExpressionFunctionCall(e,
						new ExpressionLiteral(b.toString()));
			} else if (eat(LPAREN)) {
				ArrayList<Expression> args = new ArrayList<>();
				if (token.kind != RPAREN) {
					do {
						args.add(expr());
					} while (eat(COMMA));
				}
				accept(RPAREN);
				
				// test if it is a function definition
				label1:
				if (token.kind == LBRACE) {
					Set<String> argnames = new LinkedHashSet<>();
					for (Expression arg : args) {
						if (!(arg instanceof ExpressionVariable)
								|| funcname.equals(
										((ExpressionVariable) arg).variable)
								|| !argnames.add(
										((ExpressionVariable) arg).variable)) {
							break label1;
						}
					}
					String[] params = argnames.toArray(new String[0]);
					accept(LBRACE);
					Expression body = expr();
					if (!(body instanceof ExpressionMulti))
						body = new ExpressionMulti(body);
					UserFunction function = new UserFunction(funcname, params, body);
					accept(RBRACE);
					e = new ExpressionFunctionDefinition(function);
					break;
				}
				
				e = new ExpressionFunctionCall(e, args.toArray(new Expression[0]));
			}
			break;
		}
		case BAR:
			nextToken();
			e = new ExpressionAbs(and());
			accept(BAR);
			break;
		case LPAREN:
			if (peekToken(RPAREN) && peekToken(2, ARROW)
					|| (peekToken(WORD) && (peekToken(2, COMMA)
							|| peekToken(2, RPAREN) && peekToken(3, ARROW)))) {
				e = parseLambda(this::assign);
			} else {
				nextToken();
				switch (token.kind) {
				case RETURN:
					nextToken();
					switch (token.kind) {
					case SEMI:
					case EOF:
					case RPAREN:
					case RBRACE:
					case RBRACKET:
					case ELSE:
					case COMMA:
						e = new ExpressionReturn(null);
						break;
					default:
						e = new ExpressionReturn(assign());
					}
					break;
				case FOR:
					e = parseFor(this::expr);
					break;
				case IF:
					e = parseIf(this::expr, true);
					break;
				/*case LOCAL:
					e = parseLocal(this::assign);
					break;*/
				case TRY:
					e = parseTry(this::expr, true);
					break;
				case WHILE:
					e = parseWhile(this::expr);
					break;
				/*case LPAREN:
					// better lambda
					if (peekToken(WORD) && (peekToken(2, COMMA)
							|| peekToken(2, RPAREN)
									&& peekToken(3, ARROW))) {
						e = parseLambda(this::assign);
						break;
					}*/
				default:
					e = new ExpressionParenthesis(expr());
				}
				
				accept(RPAREN);
			}
			break;
		case LBRACE:
			nextToken();
			if (eat(RBRACE)) {
				e = new ExpressionLiteral(() -> new Number[0]);
			} else {
				ArrayList<Expression> elems = new ArrayList<>();
				
				do {
					elems.add(expr());
				} while (eat(COMMA));
				
				if (elems.size() == 1) {
					Expression elem = elems.get(0);
					if (elem instanceof ExpressionBinaryOperator
							&& ((ExpressionBinaryOperator) elem).operator == EnumOperator.OR) {
						
						e = new ExpressionColumnLiteral(
								convertOrToList(elem).toArray(new Expression[0]));
					} else if (elem instanceof ExpressionMultiplyChain) {
						e = new ExpressionArrayLiteral(
								((ExpressionMultiplyChain) elem).exprs.toArray(
										new Expression[0]));
					} else {
						e = new ExpressionArrayLiteral(new Expression[] {elem});
					}
				} else {
					e = new ExpressionArrayLiteral(elems.toArray(new Expression[0]));
				}
				accept(RBRACE);
			}
			break;
		case LBRACKET:
			nextToken();
			boolean temp = disableMultiplyChain;
			disableMultiplyChain = true;
			int lineStart = token.line;
			try {
				Expression expr = assign();
				ArrayList<Expression> elems = new ArrayList<>();
				
				ArrayList<Expression> line = new ArrayList<>();
				
				line.add(expr);
				boolean comma = token.kind == COMMA;
				if (comma) {
					while (eat(COMMA)) {
						line.add(assign());
					}
					elems.add(new ExpressionArrayLiteral(
							line.toArray(new Expression[0])));
					line.clear();
					boolean semi = token.kind == SEMI;
					if (semi) {
						while (eat(SEMI)) {
							line.add(assign());
							while (eat(COMMA)) {
								line.add(assign());
							}
							elems.add(new ExpressionArrayLiteral(
									line.toArray(new Expression[0])));
							line.clear();
						}
					} else if (token.kind != RBRACKET)
						while (token.kind != RBRACKET && token.line > lineStart) {
							lineStart = token.line;
							line.add(assign());
							while (token.line == lineStart && eat(COMMA)) {
								line.add(assign());
							}
							elems.add(new ExpressionArrayLiteral(
									line.toArray(new Expression[0])));
							line.clear();
						}
				} else {
					while (token.kind != RBRACKET && token.line == lineStart
							&& token.kind != SEMI) {
						expr = assign();
						if (expr instanceof ExpressionMultiplyChain)
							line.addAll(((ExpressionMultiplyChain) expr).exprs);
						else
							line.add(expr);
					}
					elems.add(new ExpressionArrayLiteral(
							line.toArray(new Expression[0])));
					line.clear();
					boolean semi = token.kind == SEMI;
					if (semi) {
						while (eat(SEMI)) {
							lineStart = token.line;
							while (token.kind != RBRACKET && token.line == lineStart
									&& token.kind != SEMI) {
								line.add(assign());
							}
							elems.add(new ExpressionArrayLiteral(
									line.toArray(new Expression[0])));
							line.clear();
						}
					} else if (token.kind != RBRACKET)
						while (token.kind != RBRACKET && token.line > lineStart) {
							lineStart = token.line;
							line.add(assign());
							while (token.line == lineStart
									&& token.kind != RBRACKET) {
								line.add(assign());
							}
							elems.add(new ExpressionArrayLiteral(
									line.toArray(new Expression[0])));
							line.clear();
						}
				}
				
				if (!eat(RBRACKET))
					throw new SyntaxError(token);
				
				e = new ExpressionArrayLiteral(elems.toArray(new Expression[0]));
				
			} finally {
				disableMultiplyChain = temp;
			}
			break;
		case e:
			nextToken();
			e = new ExpressionLiteral(Real.E);
			break;
		case pi:
			nextToken();
			e = new ExpressionLiteral(Real.PI);
			break;
		case i:
			nextToken();
			e = new ExpressionLiteral(Complex.I);
			break;
		case NaN:
			nextToken();
			e = new ExpressionLiteral(Real.NaN);
			break;
		case Infinity:
			nextToken();
			e = new ExpressionLiteral(Real.INFINITY);
			break;
		case DOLLAR:
			if (!inIndex)
				throw new SyntaxError(token.pos, "'$' outside of index");
			nextToken();
			e = new ExpressionDollar();
			break;
		case LT: {
			nextToken();
			Expression x, y;
			
			x = expr();
			
			accept(COMMA);
			
			y = convert();
			
			if (eat(COMMA)) {
				Expression z = convert();
				
				e = new ExpressionArrayLiteral(x, y, z);
			} else {
				e = new ExpressionArrayLiteral(x, y);
			}
			accept(GT);
			break;
		}
		default:
			for (ParserHookExpressionSupplier hook : factor_hooks_post) {
				Expression expr = hook.test(this);
				if (expr != null) {
					return postfix(expr);
				}
			}
			throw new SyntaxError(token);
		}
		
		return postfix(e);
	}
	
	public final List<ParserHookExpressionModifier> postfix_hooks_pre =
			new ArrayList<>(), postfix_hooks_post = new ArrayList<>();
	
	@SuppressWarnings("incomplete-switch")
	public Expression postfix(Expression e) {
		outer:
		for (;;) {
			for (ParserHookExpressionModifier hook : postfix_hooks_pre) {
				Expression expr = hook.test(this, e);
				if (expr != null && expr != e) {
					e = expr;
					continue outer;
				}
			}
			switch (token.kind) {
			case LBRACKET: {
				nextToken();
				boolean temp = inIndex;
				inIndex = true;
				Expression idx = expr();
				if (eat(COMMA)) {
					Expression c = expr();
					e = new ExpressionMatrixIndex(e, idx, c);
				} else {
					e = new ExpressionIndex(e, idx);
				}
				inIndex = temp;
				accept(RBRACKET);
				break;
			}
			case SUBSUB: {
				e = e.skipParens();
				if (e instanceof ExpressionReferenceable) {
					e = ((ExpressionReferenceable) e).toPostfixDecrement();
					nextToken();
					if (token.kind == SUBSUB)
						throw new SyntaxError(token);
					return e;
				} else {
					throw new SyntaxError(token.pos,
							"expected reference, got " + e.toEvalString());
				}
			}
			case PLUSPLUS: {
				e = e.skipParens();
				if (e instanceof ExpressionReferenceable) {
					e = ((ExpressionReferenceable) e).toPostfixIncrement();
					nextToken();
					if (token.kind == PLUSPLUS)
						throw new SyntaxError(token);
					return e;
				} else {
					throw new SyntaxError(token.pos,
							"expected reference, got " + e.toEvalString());
				}
			}
			case MODULO:
				nextToken();
				if (token.kind == MODULO)
					throw new SyntaxError(token);
				e = new ExpressionUnaryOperator(EnumOperator.PERCENT, e);
				break;
			case DOTX:
				nextToken();
				test:
				switch (token.kind) {
				case DOT:
					if (peekToken(WORD)) {
						Token t = tkzr.peekToken(1);
						switch (t.stringValue()) {
						default:
							break test;
						case "x":
						case "y":
						case "z":
							break;
						}
					}
				case DOTX:
				case DOTY:
				case DOTZ:
					throw new SyntaxError(token);
				}
				e = new ExpressionX(e);
				break;
			case DOTY:
				nextToken();
				test:
				switch (token.kind) {
				case DOT:
					if (peekToken(WORD)) {
						Token t = tkzr.peekToken(1);
						switch (t.stringValue()) {
						default:
							break test;
						case "x":
						case "y":
						case "z":
							break;
						}
					}
				case DOTX:
				case DOTY:
				case DOTZ:
					throw new SyntaxError(token);
				}
				e = new ExpressionY(e);
				break;
			case DOTZ:
				nextToken();
				test:
				switch (token.kind) {
				case DOT:
					if (peekToken(WORD)) {
						Token t = tkzr.peekToken(1);
						switch (t.stringValue()) {
						default:
							break test;
						case "x":
						case "y":
						case "z":
							break;
						}
					}
				case DOTX:
				case DOTY:
				case DOTZ:
					throw new SyntaxError(token);
				}
				e = new ExpressionZ(e);
				break;
			case BANG:
				nextToken();
				e = new ExpressionUnaryOperator(EnumOperator.FACTORIAL, e);
				break;
			case DOT:
				nextToken();
				if (token.kind != WORD)
					throw new SyntaxError(token);
				String name = token.stringValue();
				nextToken();
				ArrayList<Expression> args = new ArrayList<>();
				args.add(e);
				if (eat(LPAREN)) {
					if (token.kind != RPAREN) {
						do {
							args.add(expr());
						} while (eat(COMMA));
					}
					accept(RPAREN);
				}
				e = new ExpressionFunctionCall(new ExpressionVariable(name),
						args.toArray(new Expression[0]));
				break;
			/*case WORD:
				if (token.stringValue().matches("e|E\\d*")) {
					String str = token.stringValue();
					nextToken();
					int i;
					if (str.length() > 1) {
						i = Integer.parseUnsignedInt(str.substring(1));
						if (token.kind == NUMBER)
							throw new SyntaxError(token);
					} else {
						boolean negate = eat(SUB);
						if (token.kind != NUMBER)
							throw new SyntaxError(token.pos, NUMBER, token.kind);
						if (!isInt(token.doubleValue()))
							throw new SyntaxError(token.pos,
									"expected integer, got decimal");
						i = (int) token.doubleValue();
						if (negate)
							i = -i;
						nextToken();
					}
					e = new ExpressionBinaryOperator(e,
							EnumOperator.SCIENTIFIC_NOTATION,
							new ExpressionLiteral(Real.valueOf(i)));
					if (token.kind == WORD
							&& token.stringValue().equalsIgnoreCase("e")) {
						throw new SyntaxError(token);
					}
					break;
				}*/
			case DEGREE:
				if (!indicatesStartOfExpr(tkzr.peekToken(1).kind)) {
					e = new ExpressionUnaryOperator(EnumOperator.DEGREES_POSTFIX, e);
					break;
				}
			default:
				for (ParserHookExpressionModifier hook : postfix_hooks_post) {
					Expression expr = hook.test(this, e);
					if (expr != null && expr != e) {
						e = expr;
						continue outer;
					}
				}
				return e;
			}
		}
	}
	
	public Expression parseFor(Supplier<Expression> bodySupplier) {
		accept(FOR);
		String variable;
		
		accept(LPAREN);
		
		Token last = token;
		
		accept(WORD);
		
		variable = last.stringValue();
		last = null;
		
		if (eat(COLON)) {
			Expression array, body;
			
			array = expr();
			
			accept(RPAREN);
			
			if (eat(LBRACE)) {
				body = expr();
				if (!((body = body.skipParens()) instanceof ExpressionMulti)) {
					body = new ExpressionMulti(body);
				}
				accept(RBRACE);
			} else {
				body = bodySupplier.get();
				if (body instanceof ExpressionMulti)
					body = new ExpressionParenthesis(body);
			}
			
			return new ExpressionForEachSingle(variable, array, body);
		} else {
			Expression start, body;
			
			accept(COMMA);
			
			start = expr();
			
			if (start instanceof ExpressionVariable && eat(COLON)) {
				String variable2;
				Expression array;
				
				variable2 = ((ExpressionVariable) start).variable;
				array = expr();
				
				accept(RPAREN);
				
				if (eat(LBRACE)) {
					body = expr();
					if (!((body = body.skipParens()) instanceof ExpressionMulti)) {
						body = new ExpressionMulti(body);
					}
					accept(RBRACE);
				} else {
					body = bodySupplier.get();
					if (body instanceof ExpressionMulti)
						body = new ExpressionParenthesis(body);
				}
				
				return new ExpressionForEachDouble(variable, variable2, array, body);
			} else {
				
				Expression end, increment;
				
				accept(COMMA);
				
				end = expr();
				
				if (eat(COMMA)) {
					increment = expr();
				} else
					increment = null;
				
				accept(RPAREN);
				
				if (eat(LBRACE)) {
					body = expr();
					if (!((body = body.skipParens()) instanceof ExpressionMulti)) {
						body = new ExpressionMulti(body);
					}
					accept(RBRACE);
				} else {
					body = bodySupplier.get();
					if (body instanceof ExpressionMulti)
						body = new ExpressionParenthesis(body);
				}
				
				return new ExpressionFor(variable, start, end, increment, body);
			}
		}
	}
	
	public ExpressionLocal parseLocal(Supplier<Expression> bodySupplier) {
		accept(LOCAL);
		List<ExpressionLocal.Def> defs = new ArrayList<>();
		
		do {
			defs.add(parseDef(bodySupplier));
		} while (eat(COMMA));
		
		return new ExpressionLocal(defs.toArray(new ExpressionLocal.Def[0]));
	}
	
	public ExpressionLocal.Def parseDef(Supplier<Expression> bodySupplier) {
		
		switch (token.kind) {
		case DIM: {
			nextToken();
			accept(LPAREN);
			if (token.kind != WORD)
				throw new SyntaxError(token.pos, WORD, token.kind);
			String varname = token.stringValue();
			nextToken();
			accept(RPAREN);
			accept(EQ);
			return new ExpressionLocal.DimDef(varname, bodySupplier.get());
		}
		case WORD: {
			String varname = token.stringValue();
			nextToken();
			
			if (eat(EQ)) {
				return new ExpressionLocal.DefImpl(varname, bodySupplier.get());
			} else if (eat(LPAREN)) {
				List<String> paramNames = new ArrayList<>();
				if (token.kind != RPAREN) {
					do {
						if (token.kind != WORD)
							throw new SyntaxError(token.pos, WORD, token.kind);
						paramNames.add(token.stringValue());
						nextToken();
					} while (eat(COMMA));
				}
				accept(RPAREN);
				
				Expression body;
				
				if (eat(LBRACE)) {
					body = expr();
					if (!((body = body.skipParens()) instanceof ExpressionMulti))
						body = new ExpressionMulti(body);
					accept(RBRACE);
				} else {
					accept(EQ);
					body = bodySupplier.get();
					if (body instanceof ExpressionMulti)
						body = new ExpressionParenthesis(body);
				}
				
				return new ExpressionLocal.FuncDef(new UserFunction(varname,
						paramNames.toArray(new String[0]), body));
			} else {
				return new ExpressionLocal.DefImpl(varname);
			}
		}
		default:
			throw new SyntaxError(token);
		}
	}
	
	public ExpressionIf parseIf(Supplier<Expression> bodySupplier,
			boolean useBodySupplierForThenStatement) {
		accept(IF);
		Expression condition = assign();
		Expression thenpart, elsepart;
		
		accept(THEN);
		
		if (eat(LBRACE)) {
			thenpart = expr();
			if (!((thenpart = thenpart.skipParens()) instanceof ExpressionMulti)) {
				thenpart = new ExpressionMulti(thenpart);
			}
			accept(RBRACE);
		} else {
			thenpart =
					useBodySupplierForThenStatement? bodySupplier.get() : prefix();
			if (thenpart instanceof ExpressionMulti)
				thenpart = new ExpressionParenthesis(thenpart);
		}
		
		if (eat(ELSE)) {
			if (eat(LBRACE)) {
				elsepart = expr();
				if (!((elsepart =
						elsepart.skipParens()) instanceof ExpressionMulti)) {
					elsepart = new ExpressionMulti(elsepart);
				}
				accept(RBRACE);
			} else {
				elsepart = bodySupplier.get();
				if (elsepart instanceof ExpressionMulti)
					elsepart = new ExpressionParenthesis(elsepart);
			}
		} else
			elsepart = null;
		
		return new ExpressionIf(condition, thenpart, elsepart);
	}
	
	public ExpressionWhile parseWhile(Supplier<Expression> bodySupplier) {
		accept(WHILE);
		Expression condition, body;
		
		accept(LPAREN);
		
		condition = expr();
		
		accept(RPAREN);
		
		if (eat(LBRACE)) {
			body = expr();
			if (!((body = body.skipParens()) instanceof ExpressionMulti))
				body = new ExpressionMulti(body);
			accept(RBRACE);
		} else {
			body = bodySupplier.get();
			if (body instanceof ExpressionMulti)
				body = new ExpressionParenthesis(body);
		}
		
		return new ExpressionWhile(condition, body);
	}
	
	public ExpressionTry parseTry(Supplier<Expression> bodySupplier,
			boolean useBodySupplierForThenStatement) {
		accept(TRY);
		
		Expression thenpart, elsepart;
		
		if (eat(LBRACE)) {
			thenpart = expr();
			if (!((thenpart = thenpart.skipParens()) instanceof ExpressionMulti)) {
				thenpart = new ExpressionMulti(thenpart);
			}
			accept(RBRACE);
		} else {
			thenpart =
					useBodySupplierForThenStatement? bodySupplier.get() : prefix();
			if (thenpart instanceof ExpressionMulti)
				thenpart = new ExpressionParenthesis(thenpart);
		}
		
		accept(ELSE);
		if (eat(LBRACE)) {
			elsepart = expr();
			if (!((elsepart = elsepart.skipParens()) instanceof ExpressionMulti)) {
				elsepart = new ExpressionMulti(elsepart);
			}
			accept(RBRACE);
		} else {
			elsepart = bodySupplier.get();
			if (elsepart instanceof ExpressionMulti)
				elsepart = new ExpressionParenthesis(elsepart);
		}
		
		return new ExpressionTry(thenpart, elsepart);
	}
	
	public ExpressionFunctionDefinition parseLambda(
			Supplier<Expression> bodySupplier) {
		accept(LPAREN);
		List<String> words = new ArrayList<>();
		if (token.kind != RPAREN) {
			words.add(token.stringValue());
			nextToken();
			while (eat(COMMA)) {
				if (token.kind != WORD)
					throw new SyntaxError(token.pos, WORD, token.kind);
				words.add(token.stringValue());
				nextToken();
			}
		}
		accept(RPAREN);
		accept(ARROW);
		Expression body;
		if (eat(LBRACE)) {
			body = expr();
			if (!((body = body.skipParens()) instanceof ExpressionMulti))
				body = new ExpressionMulti(body);
			accept(RBRACE);
		} else {
			body = bodySupplier.get();
			if (body instanceof ExpressionMulti)
				body = new ExpressionParenthesis(body);
		}
		
		return new ExpressionFunctionDefinition(
				new UserFunction(null, words.toArray(new String[0]), body));
	}
	
	private static List<Expression> convertOrToList(Expression ex) {
		List<Expression> result = new ArrayList<>();
		if (ex instanceof ExpressionBinaryOperator
				&& ((ExpressionBinaryOperator) ex).operator == EnumOperator.OR) {
			ExpressionBinaryOperator e = (ExpressionBinaryOperator) ex;
			result.addAll(convertOrToList(e.lhs));
			result.addAll(convertOrToList(e.rhs));
		} else {
			result.add(ex);
		}
		return result;
	}
	/*
	public boolean indicatesStartOfUnit(Token t) {
		if (t.kind != WORD)
			return false;
		try {
			UnitFormat.getUCUMInstance().parseSingleUnit(t.stringValue(),
					new ParsePosition(0));
			return true;
		} catch (ParseException e) {
			return false;
		}
	}
	
	public Unit parseUnit() {
		Unit result = Unit.ONE;
		switch (token.kind) {
		case WORD:
			result = parseSingleUnit();
			break;
		case LPAREN:
			nextToken();
			result = parseProductUnit();
			accept(RPAREN);
			break;
		}
		while (true) {
			switch (token.kind) {
			case CARET:
				nextToken();
				int exp, root;
				
				if (eat(LPAREN)) {
					if (token.kind != NUMBER)
						accept(NUMBER);
					double d = token.doubleValue();
					if (d != (int) d)
						throw new SyntaxError(token.pos,
								"Expected INTEGER, got DECIMAL");
					
					exp = (int) d;
					
					nextToken();
					
					if (eat(SLASH)) {
						if (token.kind != NUMBER)
							accept(NUMBER);
						d = token.doubleValue();
						if (d != (int) d)
							throw new SyntaxError(token.pos,
									"Expected INTEGER, got DECIMAL");
						root = (int) d;
						nextToken();
					} else {
						root = 1;
					}
					accept(RPAREN);
				} else if (token.kind != NUMBER)
					accept(NUMBER);
				else {
					double d = token.doubleValue();
					if (d != (int) d)
						throw new SyntaxError(token.pos,
								"Expected INTEGER, got DECIMAL");
					nextToken();
					exp = (int) d;
					root = 1;
				}
				
				if (exp != 1) {
					result = result.pow(exp);
				}
				if (root != 1) {
					result = result.root(root);
				}
				break;
			case STAR:
				if (token.kind == NUMBER) {
					double d = token.doubleValue();
					nextToken();
					if (d != 1) {
						result = result.times(d);
					}
				} else {
					result = result.times(parseProductUnit());
				}
				break;
			case SLASH:
				nextToken();
				if (token.kind == NUMBER) {
					double d = token.doubleValue();
					nextToken();
					if (d != 1.0) {
						result = result.divide(d);
					}
				} else {
					result = result.divide(parseProductUnit());
				}
				break;
			case PLUS:
				nextToken();
				if (token.kind == NUMBER) {
					double d = token.doubleValue();
					nextToken();
					if (d != 1.0) {
						result = result.plus(d);
					}
				} else {
					accept(NUMBER);
				}
				break;
			case EOF:
			case RPAREN:
				return result;
			default:
				throw new ParseException("unexpected token " + token,
						pos.getIndex());
			}
			token = nextToken(csq, pos);
		}
	}
	
	protected Unit parseSingleUnit() {
		if (token.kind != WORD)
			accept(WORD);
		String name = token.stringValue();
		nextToken();
		try {
			return UnitFormat.getUCUMInstance().parseSingleUnit(name,
					new ParsePosition(0));
		} catch (ParseException e) {
			throw new SyntaxError(token.pos, "Expected UNIT NAME, got " + name);
		}
	}*/
}
