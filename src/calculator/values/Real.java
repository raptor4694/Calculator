package calculator.values;

import static calculator.functions.Functions.*;

import java.util.HashMap;
import java.util.Map;

import javax.measure.unit.Unit;

import calculator.errors.TypeError;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Real implements Number {
	private static final Map<Double, Real> interns = new HashMap<>();
	public static final Real NaN = new Real(Double.NaN), ZERO = new Real(0.0),
			ONE = new Real(1.0), E = new Real(Math.E), PI = new Real(Math.PI),
			TWO = new Real(2.0), ONE_HALF = new Real(0.5),
			LN_10 = new Real(Math.log(10)),
			INFINITY = new Real(Double.POSITIVE_INFINITY);
	
	static {
		interns.put(Double.NaN, NaN);
		interns.put(0.0, ZERO);
		interns.put(1.0, ONE);
		interns.put(Math.E, E);
		interns.put(Math.PI, PI);
		interns.put(2.0, TWO);
		interns.put(0.5, ONE_HALF);
		interns.put(LN_10.value, LN_10);
		interns.put(Double.POSITIVE_INFINITY, INFINITY);
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
		if (d == LN_10.value)
			return LN_10;
		if (d == Double.POSITIVE_INFINITY)
			return INFINITY;
		Real result = interns.get(d);
		if (result == null) {
			interns.put(d, result = new Real(d));
		}
		return result;
	}
	
	public final double value;
	
	public double doubleValue() {
		return value;
	}
	
	public int intValue() {
		return (int) value;
	}
	
	public boolean isInt() {
		return intValue() == doubleValue();
	}
	
	@Override
	public String toString() {
		if (isInt())
			return Integer.toString(intValue());
		else
			return Double.toString(value);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Real)
			return value == ((Real) obj).value;
		return false;
	}
	
	@Override
	public Real negate() {
		return valueOf(-value);
	}
	
	@Override
	public Number abs() {
		return value < 0? valueOf(-value) : this;
	}
	
	@Override
	public Number inverse() {
		check(value != 0.0, DimensionError);
		return value == 1.0? this : valueOf(1.0 / value);
	}
	
	@Override
	public Number sqrt() {
		if (value < 0)
			return Complex.sqrt(value, 0.0);
		return valueOf(Math.sqrt(value));
	}
	
	@Override
	public Number cbrt() {
		return valueOf(Math.cbrt(value));
	}
	
	@Override
	public Number root(int root) {
		if (root == 1) {
			return this;
		} else if (root == 2) {
			return sqrt();
		} else if (root == 3) {
			return cbrt();
		} else {
			check(root != 0, DimensionError);
			if (root % 2 == 0 && value < 0) {
				return Complex.pow(value, 0.0, 1.0 / root, 0.0);
			} else {
				return valueOf(Math.pow(value, 1.0 / root));
			}
		}
	}
	
	@Override
	public Number plus(Real r) {
		return valueOf(value + r.value);
	}
	
	@Override
	public Number plus(Complex c) {
		return Complex.valueOf(value + c.real, c.imag);
	}
	
	@Override
	public Number plus(Amount a) {
		if (a.unit.isCompatible(Unit.ONE))
			return Amount.valueOf(value + a.value, a.unit);
		throw new TypeError();
	}
	
	@Override
	public Number minus(Real r) {
		return valueOf(value - r.value);
	}
	
	@Override
	public Number minus(Complex c) {
		return Complex.valueOf(value - c.real, -c.imag);
	}
	
	@Override
	public Number minus(Amount a) {
		if (a.unit.isCompatible(Unit.ONE))
			return Amount.valueOf(value - a.value, a.unit);
		throw new TypeError();
	}
	
	@Override
	public Number times(Real r) {
		return valueOf(value * r.value);
	}
	
	@Override
	public Number times(Complex c) {
		return Complex.valueOf(value * c.real, value * c.imag);
	}
	
	@Override
	public Number times(Amount a) {
		return Amount.valueOf(value * a.value, a.unit);
	}
	
	@Override
	public Number divide(Real r) {
		return valueOf(value / r.value);
	}
	
	@Override
	public Number divide(Complex c) {
		return Complex.divide(value, 0, c.real, c.imag);
	}
	
	@Override
	public Number divide(Amount a) {
		return Amount.valueOf(value / a.value, Amount.inverseOf(a.unit));
	}
	
	@Override
	public Number pow(Real r) {
		if (r.value == 0)
			return Real.ONE;
		return valueOf(Math.pow(value, r.value));
	}
	
	@Override
	public Number pow(Complex c) {
		return Complex.pow(value, 0, c.real, c.imag);
	}
	
	@Override
	public Number pow(Amount a) {
		throw new TypeError();
	}
	
	@Override
	public boolean isGreaterThan(Real r) {
		return value > r.value;
	}
	
	@Override
	public boolean isGreaterThan(Complex c) {
		throw new TypeError();
	}
	
	@Override
	public boolean isGreaterThan(Amount a) {
		check(a.unit.isCompatible(Unit.ONE), TypeError);
		return value > a.value;
	}
	
	@Override
	public boolean isLessThan(Real r) {
		return value < r.value;
	}
	
	@Override
	public boolean isLessThan(Complex c) {
		throw new TypeError();
	}
	
	@Override
	public boolean isLessThan(Amount a) {
		check(a.unit.isCompatible(Unit.ONE), TypeError);
		return value < a.value;
	}
	
	@Override
	public boolean isGreaterThanOrEqualTo(Real r) {
		return value >= r.value;
	}
	
	@Override
	public boolean isGreaterThanOrEqualTo(Complex c) {
		throw new TypeError();
	}
	
	@Override
	public boolean isGreaterThanOrEqualTo(Amount a) {
		check(a.unit.isCompatible(Unit.ONE), TypeError);
		return value >= a.value;
	}
	
	@Override
	public boolean isLessThanOrEqualTo(Real r) {
		return value <= r.value;
	}
	
	@Override
	public boolean isLessThanOrEqualTo(Complex c) {
		throw new TypeError();
	}
	
	@Override
	public boolean isLessThanOrEqualTo(Amount a) {
		check(a.unit.isCompatible(Unit.ONE), TypeError);
		return value <= a.value;
	}
	
}
