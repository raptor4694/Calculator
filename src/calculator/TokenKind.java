package calculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import calculator.functions.Functions;
import calculator.values.EnumOperator;
import calculator.values.MethodFunction;

public enum TokenKind {
	// End Of File
	EOF,
	
	// unary operators
	MODULO ("%", null, EnumOperator.PERCENT),
	HASHTAG ("#", null, EnumOperator.CARDINALITY),
	BANG ("!", null, EnumOperator.NOT),
	// vectors element access
	DOTX (".x"),
	DOTY (".y"),
	DOTZ (".z"),
	
	// binary operators
	PLUS ("+", EnumOperator.ADD),
	SUB ("-", EnumOperator.SUBTRACT, EnumOperator.NEGATE),
	STAR ("*", EnumOperator.MULTIPLY),
	SLASH ("/", EnumOperator.DIVIDE),
	CARET ("^", EnumOperator.POW),
	MOD ("mod", EnumOperator.MOD),
	BAR ("|", new MethodFunction(Functions.class, "abs"), EnumOperator.OR, null),
	AMP ("&", EnumOperator.AND),
	CROSS ("><", EnumOperator.CROSS),
	// E ("E", EnumOperator.SCIENTIFIC_NOTATION), // 2E-6 for example
	// comparison operators
	EQEQ ("==", EnumOperator.EQ),
	BANGEQ ("!=", EnumOperator.NE),
	LT ("<", EnumOperator.LT),
	GT (">", EnumOperator.GT),
	LTEQ ("<=", EnumOperator.LE),
	GTEQ (">=", EnumOperator.GE),
	
	// unary assignment operators
	PLUSPLUS ("++"),
	SUBSUB ("--"),
	
	// binary assignment operators
	EQ ("="),
	PLUSEQ ("+=", EnumOperator.ADD),
	SUBEQ ("-=", EnumOperator.SUBTRACT),
	STAREQ ("*=", EnumOperator.MULTIPLY),
	SLASHEQ ("/=", EnumOperator.DIVIDE),
	CARETEQ ("^=", EnumOperator.POW),
	BAREQ ("|=", EnumOperator.OR),
	AMPEQ ("&=", EnumOperator.AND),
	DOTEQ (".="),
	
	/**
	 * @deprecated As the NOT operator is already element-wise only, this is
	 *             unnecessary
	 */
	@Deprecated
	DOTBANG (".!", EnumOperator.NOT),
	
	// element-wise operators
	DOTPLUS (".+", EnumOperator.ADD),
	DOTSUB (".-", EnumOperator.SUBTRACT),
	DOTSTAR (".*", EnumOperator.MULTIPLY),
	DOTSLASH ("./", EnumOperator.DIVIDE),
	DOTCARET (".^", EnumOperator.POW),
	DOTMOD (".mod", EnumOperator.MOD),
	DOTBAR (".|", EnumOperator.OR),
	DOTAMP (".&", EnumOperator.AND),
	// DOTCROSS(".><", EnumOperator.CROSS),
	// DOTE(".E", EnumOperator.SCIENTIFIC_NOTATION),
	DOTEQEQ (".==", EnumOperator.EQ),
	DOTBANGEQ (".!=", EnumOperator.NE),
	DOTLT (".<", EnumOperator.LT),
	DOTGT (".>", EnumOperator.GT),
	DOTLTEQ (".<=", EnumOperator.LE),
	DOTGTEQ (".>=", EnumOperator.GE),
	
	// unusued
	AT ("@"),
	TILDE ("~"),
	UNDERSCORE ("_"),
	APOS ("'"),
	GRAVE ("`"),
	BACKSLASH ("\\"),
	
	// grouping
	LPAREN ("("),
	RPAREN (")"),
	LBRACKET ("["),
	RBRACKET ("]"),
	LBRACE ("{"),
	RBRACE ("}"),
	
	// miscellaneous
	COMMA (","),
	SEMI (";"),
	COLON (":"),
	QUES ("?"),
	DOT ("."),
	SIGMA ("Σ"),
	DIM ("dim", Functions.class),
	COMPILE ("compile", Console.class),
	DECOMPILE ("decompile", Console.class),
	DOLLAR ("$"),
	ARROW ("=>"),
	
	// trigonometric functions
	SIN ("sin", Functions.class),
	SINH ("sinh", Functions.class),
	COS ("cos", Functions.class),
	COSH ("cosh", Functions.class),
	TAN ("tan", Functions.class),
	TANH ("tanh", Functions.class),
	CSC ("csc", Functions.class),
	CSCH ("csch", Functions.class),
	SEC ("sec", Functions.class),
	SECH ("sech", Functions.class),
	COT ("cot", Functions.class),
	COTH ("coth", Functions.class),
	ASIN ("asin", Functions.class),
	ASINH ("asinh", Functions.class),
	ACOS ("acos", Functions.class),
	ACOSH ("acosh", Functions.class),
	ATAN ("atan", Functions.class),
	ATANH ("atanh", Functions.class),
	ACSC ("acsc", Functions.class),
	ACSCH ("acsch", Functions.class),
	ASEC ("asec", Functions.class),
	ASECH ("asech", Functions.class),
	ACOT ("acot", Functions.class),
	ACOTH ("acoth", Functions.class),
	
	// constants
	e ("e"),
	pi ("pi"),
	i ("i"),
	NaN ("NaN"),
	Infinity ("Infinity"),
	
	// control flow
	FOR ("for"),
	IF ("if"),
	THEN ("then"),
	ELSE ("else"),
	WHILE ("while"),
	BREAK ("break"),
	RETURN ("return"),
	TRY ("try"),
	DELETE ("delete"),
	LOCAL ("local"),
	ALIAS ("Alias", Console.class),
	
	// literals
	NUMBER,
	WORD,
	STRING,
	
	// DO NOT USE
	@Deprecated
	CUSTOM;
	
	static final TokenKind[] SORTED_VALS;
	static final Map<String, TokenKind> NAMED_TOKENS_MAP, BY_NAME;
	
	public final String symbol;
	private final EnumOperator unaryOperator;
	private final EnumOperator binaryOperator;
	private final MethodFunction function;
	private Set<Section> sections = null;
	
	private TokenKind() {
		this(null, null, null);
	}
	
	private TokenKind(String s) {
		this(s, null, null);
	}
	
	private TokenKind(String s, EnumOperator op) {
		this(s, op, null);
	}
	
	private TokenKind(String s, MethodFunction func, EnumOperator op2,
			EnumOperator op1) {
		symbol = s;
		function = func;
		unaryOperator = op1;
		binaryOperator = op2;
	}
	
	private TokenKind(String s, Class<?> functionClass) {
		symbol = s;
		binaryOperator = unaryOperator = null;
		function = new MethodFunction(functionClass, s);
	}
	
	private TokenKind(String s, EnumOperator op2, EnumOperator op1) {
		symbol = s;
		binaryOperator = op2;
		unaryOperator = op1;
		function = null;
	}
	
	@Override
	public String toString() {
		if (symbol == null)
			return super.toString();
		else
			return "'" + symbol + "'";
	}
	
	public Set<Section> getSections() {
		if (sections == null) {
			Section[] values = Section.values();
			Collection<Section> sections = new ArrayList<>(values.length);
			for (Section section : values) {
				if (section.contains(this))
					sections.add(section);
			}
			this.sections = EnumSet.copyOf(sections);
		}
		return sections;
	}
	
	public static TokenKind bySymbol(String sym) {
		return BY_NAME.get(sym);
	}
	
	public EnumOperator getUnaryOperator() {
		if (unaryOperator == null)
			throw new NullPointerException("unary operator");
		return unaryOperator;
	}
	
	public EnumOperator getBinaryOperator() {
		if (binaryOperator == null)
			throw new NullPointerException("binary operator");
		return binaryOperator;
	}
	
	public MethodFunction getFunction() {
		if (function == null)
			throw new NullPointerException("function");
		return function;
	}
	
	public static enum Section implements Iterable<TokenKind> {
		UNARY_PREFIX_OPERATORS (SUB, HASHTAG, BANG),
		UNARY_POSTFIX_OPERATORS (MODULO),
		BINARY_OPERATORS (PLUS, SUB, STAR, SLASH, CARET, MOD, BAR, AMP, CROSS),
		COMPARISON_OPERATORS (EQEQ, BANGEQ, LT, GT, LTEQ, GTEQ),
		VECTOR_ELEMENT_ACCESS (DOTX, DOTY, DOTZ),
		UNARY_ASSIGNMENT_OPERATORS (PLUSPLUS, SUBSUB),
		BINARY_ASSIGNMENT_OPERATORS (EQ, PLUSEQ, SUBEQ, STAREQ, SLASHEQ, CARETEQ,
				BAREQ, AMPEQ),
		ELEMENTWISE_OPERATORS (DOTBANG, DOTPLUS, DOTSUB, DOTSTAR, DOTSLASH, DOTCARET,
				DOTMOD, DOTBAR, DOTAMP, DOTEQEQ, DOTBANGEQ, DOTLT, DOTGT, DOTLTEQ,
				DOTGTEQ),
		GROUPING (LPAREN, RPAREN, LBRACKET, RBRACKET, LBRACE, RBRACE),
		MISC (COMMA, SEMI, COLON, QUES, DOT, SIGMA, DIM, DOLLAR, ARROW),
		TRIG_FUNCTIONS (SIN, SINH, ASIN, ASINH, COS, COSH, ACOS, ACOSH, TAN, TANH,
				ATAN, ATANH, CSC, CSCH, ACSC, ACSCH, SEC, SECH, ASEC, ASECH, COT,
				COTH, ACOT, ACOTH),
		CONSTANTS (e, pi, i, NaN, Infinity),
		LITERALS (WORD, NUMBER, STRING),
		CONTROL_FLOW (IF, THEN, ELSE, WHILE, BREAK, RETURN, FOR, TRY, DELETE),
		END_OF_FILE (EOF),
		UNUSED (AT, TILDE, UNDERSCORE, APOS, GRAVE, BACKSLASH);
		
		private EnumSet<TokenKind> tokens;
		
		private Section() {
			tokens = EnumSet.noneOf(TokenKind.class);
		}
		
		private Section(TokenKind kind1) {
			tokens = EnumSet.of(kind1);
		}
		
		private Section(TokenKind kind1, TokenKind... kinds) {
			tokens = EnumSet.of(kind1, kinds);
		}
		
		public boolean contains(TokenKind kind) {
			return tokens.contains(kind);
		}
		
		@Override
		public Iterator<TokenKind> iterator() {
			return tokens.iterator();
		}
	}
	
	static {
		TokenKind[] vals = values();
		ArrayList<TokenKind> nonNamedTokens = new ArrayList<>();
		Map<String, TokenKind> tokensMap = new HashMap<>(),
				namedTokensMap = new HashMap<>();
		for (TokenKind kind : vals) {
			if (kind.symbol != null) {
				if (kind.symbol.codePoints().allMatch(Eater::isWordChar)) {
					namedTokensMap.put(kind.symbol, kind);
				} else {
					nonNamedTokens.add(kind);
				}
				tokensMap.put(kind.symbol, kind);
			}
		}
		// NAMED_TOKENS = namedTokens.toArray(new TokenKind[0]);
		SORTED_VALS = nonNamedTokens.toArray(new TokenKind[0]);
		BY_NAME = Collections.unmodifiableMap(tokensMap);
		NAMED_TOKENS_MAP = Collections.unmodifiableMap(namedTokensMap);
		Arrays.sort(SORTED_VALS, (t1, t2) -> t1.symbol == null? 1
				: t2.symbol == null? -1
						: t1.symbol.length() > t2.symbol.length()? -1
								: t1.symbol.length() == t2.symbol.length()? 0 : 1);
		
	}
}
