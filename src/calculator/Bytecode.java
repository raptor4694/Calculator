package calculator;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lombok.experimental.UtilityClass;

public @UtilityClass class Bytecode {
	
	public static final byte END = 0;
	
	// opcodes
	
	/*
	 * ABS <expr>
	 */
	// public static final byte ABS = 1;
	
	/*
	 * DOUBLE <8 bytes>
	 */
	public static final byte DOUBLE = 1;
	
	/*
	 * UNARY <opid> <expr>
	 */
	public static final byte UNARY = 2;
	
	/*
	 * BINARY <elementwise 0/1> <opid> <expr> <expr>
	 */
	public static final byte BINARY = 3;
	
	/*
	 * IF <conditional 0/1> <expr condition> <expr thenpart> (END | <expr elsepart>)
	 */
	public static final byte IF = 4;
	
	/*
	 * WHILE <byte unusued> <expr condition> <expr body>
	 */
	public static final byte WHILE = 5;
	
	/*
	 * FOR <string varname> <expr start> <expr end> (END | <expr increment>) <expr body>
	 */
	public static final byte FOR = 6;
	
	/*
	 * FOREACH <string varname> <expr array> <expr body>
	 */
	public static final byte FOREACH = 7;
	
	/*
	 * FOREACH2 <string varname1> <string varname2> <expr array> <expr body>
	 */
	public static final byte FOREACH2 = 8;
	
	/*
	 * CALL <expr function> <arguments> END
	 */
	public static final byte CALL = 9;
	
	/*
	 * FUNCDEF (END | <string name>) <string arguments> END <expr body>
	 */
	public static final byte FUNCDEF = 10;
	
	/*
	 * SETDIM <string> <expr>
	 */
	public static final byte SETDIM = 11;
	
	/*
	 * DELETE <string variable>
	 */
	public static final byte DELETE = 12;
	
	/*
	 * DOLLAR
	 */
	public static final byte DOLLAR = 13;
	
	/*
	 * ARRAYLITERAL <column literal 0/1> <elements> END
	 */
	public static final byte ARRAYLITERAL = 14;
	
	/*
	 * STRING <bytes> END
	 */
	public static final byte STRING = 15;
	
	/*
	 * LOCAL {<string> (END | SETDIM <expr value> | FUNCDEF <string arguments> END <expr body> | OPCODES_COUNT <expr value>)} END
	 */
	public static final byte LOCAL = 16;
	
	/*
	 * INDEX <expr array> <expr index>
	 */
	public static final byte INDEX = 17;
	
	/*
	 * INDEX2 <expr matrix> <expr row> <expr column>
	 */
	public static final byte INDEX2 = 18;
	
	/*
	 * MUTLI [exprs] END
	 */
	public static final byte MULTI = 19;
	
	/*
	 * MUTLIPLY_CHAIN <exprs> END
	 */
	public static final byte MULTIPLY_CHAIN = 20;
	
	/*
	 * RETURN (END | <expr>)
	 */
	public static final byte RETURN = 21;
	
	/*
	 * TRY <expr body> <expr else>
	 */
	public static final byte TRY = 22;
	
	/*
	 * VARIABLE <string name>
	 */
	public static final byte VARIABLE = 23;
	
	/*
	 * VECTOR <type X=0/Y=1/Z=2> <expr>
	 */
	// public static final byte VECTOR = 24;
	
	/*
	 * COMPLEX <number real> <number imag>
	 * where <number> is: (DOUBLE <8 bytes> | INT <4 bytes> | SHORT <2 bytes> | BYTE <byte>)
	 */
	public static final byte COMPLEX = 24;
	
	/*
	 * ASSIGN <expr> <expr value>
	 */
	public static final byte ASSIGN = 25;
	
	/*
	 * ASSIGNOP <opid> <expr> <expr value>
	 */
	public static final byte ASSIGNOP = 26;
	
	/*
	 * PREFIX_INC <expr>
	 */
	public static final byte PREFIX_INC = 27;
	
	/*
	 * PREFIX_DEC <expr>
	 */
	public static final byte PREFIX_DEC = 28;
	
	/*
	 * POSTFIX_INC <expr>
	 */
	public static final byte POSTFIX_INC = 29;
	
	/*
	 * POSTFIX_DEC <expr>
	 */
	public static final byte POSTFIX_DEC = 30;
	
	/*
	 * COMPARISON_CHAIN <size of operators> {<elementwise 0/1> <opid>} <exprs>
	 */
	public static final byte COMPARISON_CHAIN = 31;
	
	/*
	 * VECTOR_X <expr>
	 */
	public static final byte VECTOR_X = 32;
	
	/*
	 * VECTOR_Y <expr>
	 */
	public static final byte VECTOR_Y = 33;
	
	/*
	 * VECTOR_Z <expr>
	 */
	public static final byte VECTOR_Z = 34;
	
	/*
	 * METHOD <string classname> <string name>
	 */
	public static final byte METHOD = 35;
	
	/*
	 * BREAK
	 */
	public static final byte BREAK = 36;
	
	/*
	 * OPERATOR <opid>
	 */
	public static final byte OPERATOR = 37;
	
	/*
	 * INT <4 bytes>
	 */
	public static final byte INT = 38;
	
	/*
	 * SHORT <2 bytes>
	 */
	public static final byte SHORT = 39;
	
	/*
	 * BYTE <byte>
	 */
	public static final byte BYTE = 40;
	
	/*
	 * ZERO
	 */
	public static final byte ZERO = 41;
	
	/*
	 * ONE
	 */
	public static final byte ONE = 42;
	
	/*
	 * E
	 */
	public static final byte E = 43;
	
	/*
	 * PI
	 */
	public static final byte PI = 44;
	
	/*
	 * IMAGINARY (DOUBLE <8 bytes> | INT <4 bytes> | SHORT <2 bytes>)
	 */
	public static final byte IMAGINARY = 45;
	
	/*
	 * I
	 */
	public static final byte I = 46;
	
	public static final byte OPCODES_COUNT = 47;
	
	public static String nameOf(byte opcode) {
		String value = OPCODE_TO_NAME.get(opcode);
		if (value == null)
			throw new IllegalArgumentException(
					"No opcode for byte " + opcode);
		return value;
	}
	
	public static byte fromName(String name) {
		Byte value = NAME_TO_OPCODE.get(name);
		if (value == null)
			throw new IllegalArgumentException(
					"\"" + name + "\" does not refer to any opcode");
		return value;
	}
	
	public static byte[] toByteArray(double d) {
		return ByteBuffer.wrap(new byte[Double.BYTES]).putDouble(
				d).array();
	}
	
	public static double toDouble(byte[] bytes) {
		return ByteBuffer.wrap(bytes).getDouble();
	}
	
	private static final Map<String, Byte> NAME_TO_OPCODE;
	private static final Map<Byte, String> OPCODE_TO_NAME;
	
	static {
		HashMap<String, Byte> nameToOpcode = new HashMap<>();
		HashMap<Byte, String> opcodeToName = new HashMap<>();
		
		final int validMods =
				Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
		for (Field field : Bytecode.class.getFields()) {
			if (field.getModifiers() == validMods
					&& field.getType() == byte.class) {
				try {
					nameToOpcode.put(field.getName(),
							field.getByte(null));
					opcodeToName.put(field.getByte(null),
							field.getName());
				} catch (IllegalArgumentException
						| IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		
		NAME_TO_OPCODE = Collections.unmodifiableMap(nameToOpcode);
		OPCODE_TO_NAME = Collections.unmodifiableMap(opcodeToName);
	}
}
