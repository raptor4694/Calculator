package calculator;

import static java.lang.Character.UnicodeBlock.*;

import lombok.Getter;

public final class Eater {
	private final String str;
	private final int[] codePoints;
	@Getter
	private int pos;
	@Getter
	private int ch;
	
	public Eater(String s) {
		str = s;
		codePoints = s.codePoints().toArray();
		setPos(0);
	}
	
	public Eater(Eater eater) {
		str = eater.str;
		codePoints = eater.codePoints;
		pos = eater.pos;
		ch = eater.ch;
	}
	
	public Eater dup() {
		return new Eater(this);
	}
	
	public int length() {
		return codePoints.length;
	}
	
	public void setPos(int newPos) {
		if (newPos >= length()) {
			pos = length();
			ch = 0;
		} else {
			ch = codePoints[pos = newPos];
		}
	}
	
	public int getLine() {
		int line = 0;
		for (int i = 0; i < pos; i++) {
			if (codePoints[i] == '\r' && i + 1 < codePoints.length
					&& codePoints[i + 1] != '\n' || codePoints[i] == '\n') {
				++line;
			}
		}
		return line;
	}
	
	public void next() {
		setPos(pos + 1);
	}
	
	public int peek(int lookahead) {
		if (lookahead + pos >= length())
			return 0;
		return codePoints[lookahead + pos];
	}
	
	public boolean eat(char c) {
		if (ch == c) {
			next();
			return true;
		}
		return false;
	}
	
	public void eatWhite() {
		while (!eof() && Character.isWhitespace(ch))
			next();
	}
	
	public boolean eat(String s) {
		if (str.regionMatches(pos, s, 0, s.length())) {
			setPos(pos + s.length());
			return true;
		}
		return false;
	}
	
	public boolean eatWord(String s) {
		if (pos + s.length() > length())
			return false;
		if (pos + s.length() < length() && isWordChar(codePoints[pos + s.length()]))
			return false;
		return eat(s);
	}
	
	public boolean isDigit(int ch) {
		return '0' <= ch && ch <= '9';
	}
	
	public double nextNumber() {
		int startpos = pos;
		boolean dot = eat('.');
		while (isDigit(ch) || !dot && ch == '.') {
			next();
		}
		if (eat('E') || eat('e')) {
			if (!eat('-'))
				eat('+');
			while (isDigit(ch))
				next();
		}
		return Double.parseDouble(str.substring(startpos, pos));
	}
	
	public String nextWord() {
		int startpos = pos;
		while (isWordChar(ch))
			next();
		return str.substring(startpos, pos);
	}
	
	public boolean eof() {
		return pos >= length();
	}
	
	public static boolean isWordChar(int c) {
		return Character.isLetterOrDigit(c) || c == '_'
				|| Character.UnicodeBlock.of(c) == COMBINING_DIACRITICAL_MARKS;
	}
	
	@Override
	public String toString() {
		return str;
	}
}
