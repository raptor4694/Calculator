package calculator.errors;

import calculator.Bytecode;

public class BytecodeException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8684149651856759759L;
	
	public BytecodeException(int pos, byte on) {
		super("at offset " + pos + " on byte " + on
				+ (on >= 0 && on < Bytecode.OPCODES_COUNT
						? "(" + Bytecode.nameOf(on) + ")" : ""));
	}
	
	public BytecodeException(String msg) {
		super(msg);
	}
	
	public BytecodeException(int position) {
		super("at position: " + position);
	}
	
	public BytecodeException() {}
}
