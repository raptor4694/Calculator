package calculator.values;

import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import calculator.functions.Operators;

//@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Real implements Number, Comparable<Real> {
	// private static final Map<Double, Real> interns = new HashMap<>();
	public static final Real NaN = new Real(Double.NaN), ZERO = new Real(0.0),
			ONE = new Real(1.0), E = new Real(Math.E), PI = new Real(Math.PI),
			TWO = new Real(2.0), ONE_HALF = new Real(0.5),
			LN_10 = new Real(Math.log(10)),
			INFINITY = new Real(Double.POSITIVE_INFINITY);
	
	/*static {
		interns.put(Double.NaN, NaN);
		interns.put(0.0, ZERO);
		interns.put(1.0, ONE);
		interns.put(Math.E, E);
		interns.put(Math.PI, PI);
		interns.put(2.0, TWO);
		interns.put(0.5, ONE_HALF);
		interns.put(LN_10.value, LN_10);
		interns.put(Double.POSITIVE_INFINITY, INFINITY);
	}*/
	
	public static Real valueOf(double d, Unit<?> unit) {
		if (unit == Unit.ONE)
			return valueOf(d);
		return new Real(d, unit);
	}
	
	public static Real valueOf(double d) {
		// if (Math.abs(d) < 1e-15)
		// d = 0;
		if (d == 0)
			return ZERO;
		if (d == 1)
			return ONE;
		if (d == 2)
			return TWO;
		if (d == 0.5)
			return ONE_HALF;
		if (d == Math.PI)
			return PI;
		if (d == Math.E)
			return E;
		if (d == LN_10.doubleValue())
			return LN_10;
		if (d == Double.POSITIVE_INFINITY)
			return INFINITY;
		/*Real result = interns.get(d);
		if (result == null) {
			interns.put(d, result = new Real(d));
		}
		return result;*/
		return new Real(Amount.valueOf(d, Unit.ONE));
	}
	
	public static Real valueOf(Amount amount) {
		return new Real(amount);
	}
	
	public final Amount value;
	
	private Real(Amount amt) {
		value = amt;
	}
	
	private Real(double d) {
		this(d, Unit.ONE);
	}
	
	private Real(double d, Unit<?> unit) {
		this(Amount.valueOf(d, unit));
	}
	
	public Amount getAmount() {
		return value;
	}
	
	@Deprecated
	public double doubleValue() {
		return value.getEstimatedValue();
	}
	
	public int intValue() {
		return (int) value.getEstimatedValue();
	}
	
	public boolean isInt() {
		return intValue() == doubleValue();
	}
	
	@Override
	public String toString() {
		String s = String.valueOf(doubleValue());
		if (getUnit() != Unit.ONE) {
			s += " " + getUnit().toString();
		}
		return s;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Real)
			return value.approximates(((Real) obj).value);
		return false;
	}
	
	@Override
	public Real negate() {
		return valueOf(value.times(-1));
	}
	
	@Override
	public Real abs() {
		return valueOf(value.abs());
	}
	
	@Override
	public Number plus(Real r) {
		return Operators.add(this, r);
	}
	
	@Override
	public Number plus(Complex c) {
		return Operators.add(this, c);
	}
	
	@Override
	public Number minus(Real r) {
		return Operators.subtract(this, r);
	}
	
	@Override
	public Number minus(Complex c) {
		return Operators.subtract(this, c);
	}
	
	@Override
	public Number times(Real r) {
		return Operators.multiply(this, r);
	}
	
	@Override
	public Number times(Complex c) {
		return Operators.multiply(this, c);
	}
	
	@Override
	public Number divide(Real r) {
		return Operators.divide(this, r);
	}
	
	@Override
	public Number divide(Complex c) {
		return Operators.divide(this, c);
	}
	
	@Override
	public Number pow(Real r) {
		return Operators.pow(this, r);
	}
	
	@Override
	public Number pow(Complex c) {
		return Operators.pow(this, c);
	}
	
	public Unit<?> getUnit() {
		return value.getUnit();
	}
	
	@Override
	public int compareTo(Real c1) {
		if (equals(c1))
			return 0;
		return value.compareTo(c1.value.to(value.getUnit()));
	}
	
}
