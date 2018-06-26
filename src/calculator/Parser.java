package calculator;

import static calculator.Functions.*;
import static calculator.TokenKind.*;
import static calculator.TokenKind.Section.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import lombok.NonNull;

public class Parser {
	public static Expression parse(String s) {
		return new Parser(s).parse();
	}
	
	protected Tokenizer tkzr;
	protected Token token;
	private boolean inIndex = false;
	HashMap<Token, Token> aliases = new HashMap<>();
	
	public Parser(String s) {
		tkzr = new Tokenizer(s);
	}
	
	protected Parser(Tokenizer tkzr) {
		this.tkzr = tkzr;
		token = tkzr.currentToken();
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
	
	protected void nextToken() {
		if (token == null || token.kind != EOF) {
			token = tkzr.nextToken();
			
			Token temp = aliases.get(token);
			if (temp != null) {
				token = temp.at(token.pos);
			}
		}
	}
	
	protected boolean peekToken(TokenKind kind) {
		return tkzr.peekToken(1).kind == kind;
	}
	
	protected boolean peekToken(int lookahead, TokenKind kind) {
		return tkzr.peekToken(lookahead).kind == kind;
	}
	
	protected boolean eat(TokenKind kind) {
		if (token.kind == kind) {
			nextToken();
			return true;
		}
		return false;
	}
	
	protected void accept(TokenKind kind) {
		if (!eat(kind))
			throw new SyntaxError(token.pos, kind, token.kind);
	}
	
	private static boolean indicatesStartOfExpr(TokenKind kind) {
		switch (kind) { //@formatter:off
		case LBRACE: case LPAREN: case NUMBER: case WORD: case STRING:
		case BANG: case HASHTAG: case BAR:
		case SIN: case SINH: case COS: case COSH: case TAN: case TANH:
		case CSC: case CSCH: case SEC: case SECH: case COT: case COTH:
		case ASIN: case ASINH: case ACOS: case ACOSH: case ATAN: case ATANH:
		case ACSC: case ACSCH: case ASEC: case ASECH: case ACOT: case ACOTH:
		case IF: case FOR: case WHILE: case TRY: case LOCAL: case RETURN:
		case BREAK: case DIM: case COMPILE: case DECOMPILE: case ALIAS: case MOD:
		case e: case pi: case i: case NaN: case Infinity: //@formatter:on
			return true;
		default:
			return false;
		}
	}
	
	public Expression parse() {
		nextToken();
		if (token.kind == EOF)
			return new ExpressionMulti();
		Expression result = expr();
		if (token.kind != EOF) {
			throw new SyntaxError(token);
		}
		return result;
	}
	
	private Expression expr() {
		return multi();
	}
	
	private Expression multi() {
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
		if (eat(SEMI) && indicatesStartOfExpr(
				token.kind)/*token.kind != EOF && token.kind != RPAREN && token.kind != RBRACE && token.kind != RBRACKET*/) {
			List<Expression> list = new ArrayList<>();
			list.add(e);
			labelA:
			if (token.kind != SEMI) {
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
		}
		return e;
	}
	
	@SuppressWarnings("incomplete-switch")
	private Expression assign() {
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
				
				throw new SyntaxError(token.pos,
						"expected reference, got " + e.toEvalString());
			}
			break;
		}
		
		return e;
	}
	
	private Expression assignRight() {
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
	
	private Expression conditional() {
		Expression e = or();
		
		if (eat(QUES)) {
			Expression thenpart = assign();
			accept(COLON);
			Expression elsepart = assign();
			e = new ExpressionConditional(e, thenpart, elsepart);
		}
		
		return e;
	}
	
	private Expression or() {
		Expression e = and();
		for (;;) {
			if (eat(BAR))
				e = new ExpressionBinaryOperator(e, EnumOperator.OR, and());
			else if (eat(DOTBAR))
				e = new ExpressionElementwiseBinaryOperator(e, EnumOperator.OR,
						and());
			else
				return e;
		}
	}
	
	private Expression and() {
		Expression e = eq();
		for (;;) {
			if (eat(AMP))
				e = new ExpressionBinaryOperator(e, EnumOperator.AND, eq());
			else if (eat(DOTAMP))
				e = new ExpressionElementwiseBinaryOperator(e, EnumOperator.AND,
						eq());
			else
				return e;
		}
	}
	
	private Expression eq() {
		Expression e = comp();
		for (;;) {
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
				return e;
			}
		}
	}
	
	private Expression comp() {
		Expression e = add();
		for (;;) {
			/*if (eat(LT))
				e = new ExpressionBinaryOperator(e, EnumOperator.LT, add());
			else if (eat(GT))
				e = new ExpressionBinaryOperator(e, EnumOperator.GT, add());
			else if (eat(LTEQ))
				e = new ExpressionBinaryOperator(e, EnumOperator.LE, add());
			else if (eat(GTEQ))
				e = new ExpressionBinaryOperator(e, EnumOperator.GE, add());
			else
				return e;*/
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
				Expression other = add();
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
	
	private Expression add() {
		Expression e = mul();
		for (;;) {
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
				return e;
			}
		}
	}
	
	private Expression mul() {
		Expression e = mul2();
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
					((ExpressionMultiplyChain) e).exprs.add(mul2());
				} else {
					ArrayList<Expression> list = new ArrayList<>();
					list.add(e);
					list.add(mul2());
					e = new ExpressionMultiplyChain(list);
				}
			} else
				return e;
		}
	}
	
	private Expression mul2() {
		Expression e = prefix();
		for (;;) {
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
				return e;
			}
		}
	}
	
	private Expression prefix() {
		switch (token.kind) {
		case HASHTAG:
		case BANG:
		case SUB: {
			EnumOperator operator = token.kind.getUnaryOperator();
			nextToken();
			return new ExpressionUnaryOperator(operator, pow());
		}
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
			return pow();
		}
	}
	
	private Expression pow() {
		Expression e = factor();
		if (eat(CARET))
			e = new ExpressionBinaryOperator(e, EnumOperator.POW, prefix());
		else if (eat(DOTCARET))
			e = new ExpressionElementwiseBinaryOperator(e, EnumOperator.POW,
					prefix());
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
	
	@SuppressWarnings("incomplete-switch")
	private Expression factor() {
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
		case COMPILE:
			e = new ExpressionLiteral(token.kind.getFunction());
			nextToken();
			if (eat(LPAREN)) {
				switch (token.kind) {
				case RBRACE:
				case RBRACKET:
				case RPAREN:
					throw new SyntaxError(token);
				case STRING:
				case WORD:
					if (peekToken(RPAREN)) {
						e = new ExpressionFunctionCall(e, factor());
						break;
					}
				default:
					e = new ExpressionLiteral(expr().toCompiledString());
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
					try {
						Tokenizer temp = tkzr.dup();
						Expression arg = new CompiledParser(temp).expr();
						e = new ExpressionLiteral(arg.toEvalString());
						tkzr = temp;
						token = tkzr.currentToken();
						break;
					} catch (SyntaxError ex) {
						ex.printStackTrace();
					}
				case WORD:
				case STRING:
					e = new ExpressionFunctionCall(e, expr());
					break;
				default:
					throw new SyntaxError(token);
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
					token = temp.at(token.pos);
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
			if (token.kind != WORD)
				throw new SyntaxError(token.pos, WORD, token.kind);
			
			e = new ExpressionDeleteVariable(token.stringValue());
			nextToken();
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
			if (eat(LPAREN)) {
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
				/*if (bar) {
					boolean temp = this.enableOr;
					this.enableOr = false;
					
					do {
						elems.add(expr());
					} while (eat(BAR));
					
					this.enableOr = temp;
					
					e = new ExpressionColumnLiteral(elems.toArray(new Expression[0]));
				} else {*/
				
				do {
					elems.add(expr());
				} while (eat(COMMA));
				
				if (elems.size() == 1
						&& elems.get(0) instanceof ExpressionBinaryOperator
						&& ((ExpressionBinaryOperator) elems.get(
								0)).operator == EnumOperator.OR) {
					// ArrayList<Expression> list = new ArrayList<>();
					
					Expression ex = elems.get(0);
					
					e = new ExpressionColumnLiteral(
							convertOrToList(ex).toArray(new Expression[0]));
				} else {
					e = new ExpressionArrayLiteral(elems.toArray(new Expression[0]));
				}
				accept(RBRACE);
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
			
			y = add();
			
			if (eat(COMMA)) {
				Expression z = add();
				
				e = new ExpressionArrayLiteral(x, y, z);
			} else {
				e = new ExpressionArrayLiteral(x, y);
			}
			accept(GT);
			break;
		}
		default:
			throw new SyntaxError(token);
		}
		
		for (;;) {
			switch (token.kind) {
			case E: {
				nextToken();
				boolean negate = eat(SUB);
				if (token.kind != NUMBER)
					throw new SyntaxError(token.pos, NUMBER, token.kind);
				if (!isInt(token.doubleValue()))
					throw new SyntaxError(token.pos,
							"expected integer, got decimal");
				int i = (int) token.doubleValue();
				if (negate)
					i = -i;
				nextToken();
				e = new ExpressionBinaryOperator(e, EnumOperator.SCIENTIFIC_NOTATION,
						new ExpressionLiteral(Real.valueOf(i)));
				if (token.kind == E) {
					throw new SyntaxError(token);
				}
				break;
			}
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
				switch (token.kind) {
				case DOTX:
				case DOTY:
				case DOTZ:
					throw new SyntaxError(token);
				}
				e = new ExpressionX(e);
				break;
			case DOTY:
				nextToken();
				switch (token.kind) {
				case DOTX:
				case DOTY:
				case DOTZ:
					throw new SyntaxError(token);
				}
				e = new ExpressionY(e);
				break;
			case DOTZ:
				nextToken();
				switch (token.kind) {
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
			default:
				return e;
			}
		}
	}
	
	private Expression parseFor(Supplier<Expression> bodySupplier) {
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
	
	private ExpressionLocal parseLocal(Supplier<Expression> bodySupplier) {
		accept(LOCAL);
		List<ExpressionLocal.Def> defs = new ArrayList<>();
		
		do {
			defs.add(parseDef(bodySupplier));
		} while (eat(COMMA));
		
		return new ExpressionLocal(defs.toArray(new ExpressionLocal.Def[0]));
	}
	
	private ExpressionLocal.Def parseDef(Supplier<Expression> bodySupplier) {
		
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
	
	private ExpressionIf parseIf(Supplier<Expression> bodySupplier,
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
	
	private ExpressionWhile parseWhile(Supplier<Expression> bodySupplier) {
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
	
	private ExpressionTry parseTry(Supplier<Expression> bodySupplier,
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
	
	private ExpressionFunctionDefinition parseLambda(
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
}
