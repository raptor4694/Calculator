package calculator;

import java.util.ArrayList;

import calculator.errors.SyntaxError;

public class Tokenizer {
	Eater eater;
	
	private ArrayList<Token> tokens = new ArrayList<>();
	private int pos = 0;
	private Token EOF = null;
	
	public Tokenizer(String str) {
		eater = new Eater(str);
	}
	
	public Tokenizer(Tokenizer tkzr) {
		eater = tkzr.eater.dup();
		tokens = new ArrayList<>(tkzr.tokens);
		pos = tkzr.pos;
		EOF = tkzr.EOF;
	}
	
	public Tokenizer dup() {
		return new Tokenizer(this);
	}
	
	public Token nextToken() {
		eater.eatWhite();
		if (pos < tokens.size()) {
			return tokens.get(pos++);
		} else if (eater.eof()) {
			if (EOF == null) {
				EOF = new Token(eater.getPos(), eater.getLine(), TokenKind.EOF);
				pos = tokens.size();
				tokens.add(EOF);
			}
			return EOF;
		} else {
			Token t = parseToken();
			eater.eatWhite();
			pos++;
			tokens.add(t);
			return t;
		}
	}
	
	public Token currentToken() {
		return tokens.get(pos - 1);
	}
	
	public Token peekToken(int lookahead) {
		int temp_pos = pos;
		for (int i = 0; i < lookahead; i++) {
			nextToken();
		}
		Token t = tokens.get(tokens.size() - 1);
		pos = temp_pos;
		return t;
	}
	
	private Token parseToken() {
		int pos = eater.getPos(), line = eater.getLine();
		if (eater.isDigit(eater.getCh())
				|| eater.getCh() == '.' && eater.isDigit(eater.peek(1))) {
			double d = eater.nextNumber();
			return new Token(pos, line, d);
		} else if (Eater.isWordChar(eater.getCh())) {
			String word = eater.nextWord();
			if (TokenKind.NAMED_TOKENS_MAP.containsKey(word)) {
				TokenKind kind = TokenKind.NAMED_TOKENS_MAP.get(word);
				if (word.charAt(0) != 'a'
						&& TokenKind.Section.TRIG_FUNCTIONS.contains(kind)) {
					if (eater.eatWord("^-1")) {
						kind = TokenKind.NAMED_TOKENS_MAP.get("a" + word);
					}
				}
				return new Token(pos, line, kind);
			}
			return new Token(pos, line, word);
		} else if (eater.getCh() == '"') {
			eater.next();
			// int startpos = eater.getPos();
			boolean escape = false;
			StringBuilder b = new StringBuilder();
			while (!eater.eof() && (escape || eater.getCh() != '"')) {
				if (escape) {
					switch (eater.getCh()) {
					case '\\':
						b.append('\\');
						break;
					case 'n':
						b.append('\n');
						break;
					case 't':
						b.append('\t');
						break;
					case '"':
					case '\'':
						b.appendCodePoint(eater.getCh());
						break;
					default:
						b.append('\\');
						b.appendCodePoint(eater.getCh());
					}
					escape = false;
					/*if (eater.getCh() == '\\') {
						if (escape)
							b.append('\\');
						escape = !escape;*/
				} else {
					if (eater.getCh() == '\\')
						escape = true;
					else
						b.appendCodePoint(eater.getCh());
					
				}
				
				eater.next();
			}
			
			if (escape)
				b.append('\\');
			
			if (!eater.eat('"'))
				throw new SyntaxError(eater.getPos(), "Unclosed string literal");
			
			String str = b.toString();
			return new Token(pos, line, TokenKind.STRING, str);
		} else {
			for (TokenKind kind : TokenKind.SORTED_VALS) {
				
				if (Character.isLetter(kind.symbol.charAt(kind.symbol.length() - 1))
						? eater.eatWord(kind.symbol) : eater.eat(kind.symbol)) {
					return new Token(pos, line, kind);
				}
			}
			throw new SyntaxError(pos, eater.getCh());
		}
	}
}
