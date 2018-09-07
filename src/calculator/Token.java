package calculator;

import java.util.Objects;

public class Token implements CharSequence {
	
	public final TokenKind kind;
	public final int pos, line;
	
	private String stringValue;
	private double numberValue;
	private String toStringResult = null;
	
	@SuppressWarnings("deprecation")
	public Token(int pos, int line, TokenKind kind) {
		if (kind == TokenKind.CUSTOM)
			throw new IllegalArgumentException("Illegal token kind");
		this.kind = kind;
		this.line = line;
		this.pos = pos;
	}
	
	public Token(int pos, int line, String strValue) {
		this(pos, line, TokenKind.WORD);
		stringValue = strValue;
	}
	
	public Token(int pos, int line, double d) {
		this(pos, line, TokenKind.NUMBER);
		numberValue = d;
	}
	
	public Token(int pos, int line, TokenKind kind, String strValue) {
		this(pos, line, kind);
		stringValue = strValue;
	}
	
	public Token(Token t) {
		kind = t.kind;
		pos = t.pos;
		line = t.line;
		stringValue = t.stringValue;
		numberValue = t.numberValue;
		toStringResult = t.toStringResult;
	}
	
	private Token(Token t, int pos, int line) {
		kind = t.kind;
		this.pos = pos;
		this.line = line;
		stringValue = t.stringValue;
		numberValue = t.numberValue;
		toStringResult = t.toStringResult;
	}
	
	public Token at(int pos, int line) {
		return new Token(this, pos, line);
	}
	
	public String stringValue() {
		if (kind.symbol != null || kind == TokenKind.NUMBER || stringValue == null)
			throw new AssertionError();
		return stringValue;
	}
	
	public double doubleValue() {
		if (kind != TokenKind.NUMBER)
			throw new AssertionError();
		return numberValue;
	}
	
	@Override
	public String toString() {
		if (toStringResult == null) {
			switch (kind) {
			case STRING:
				return toStringResult = "\""
						+ stringValue.replace("\\", "\\\\").replace("\"", "\\\"")
						+ "\"";
			case WORD:
				return toStringResult = stringValue;
			case NUMBER:
				return toStringResult = Double.toString(numberValue);
			case EOF:
				return toStringResult = "<EOF>";
			default:
				return toStringResult = kind.symbol;
			}
		}
		return toStringResult;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public int hashCode() {
		switch (kind) {
		case NUMBER:
			return -TokenKind.CUSTOM.ordinal() - Double.hashCode(doubleValue());
		case WORD:
			return stringValue.hashCode();
		case STRING:
			return -TokenKind.CUSTOM.ordinal() - stringValue.hashCode();
		default:
			return -kind.ordinal();
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Token))
			return false;
		Token tk = (Token) obj;
		return kind == tk.kind && numberValue == tk.numberValue
				&& Objects.equals(stringValue, tk.stringValue);
	}
	
	public boolean contentEquals(CharSequence seq) {
		if (length() != seq.length())
			return false;
		for (int i = 0; i < seq.length(); i++)
			if (charAt(i) != seq.charAt(i))
				return false;
		return true;
	}
	
	@Override
	public int length() {
		return kind == TokenKind.EOF? 0 : toString().length();
	}
	
	@Override
	public char charAt(int index) {
		if (kind == TokenKind.EOF)
			throw new StringIndexOutOfBoundsException(index);
		return toString().charAt(index);
	}
	
	@Override
	public CharSequence subSequence(int start, int end) {
		if (kind == TokenKind.EOF) {
			if (start == 0 && end == 0)
				return "";
			throw new StringIndexOutOfBoundsException(start);
		}
		return toString().subSequence(start, end);
	}
	
}
