package calculator.values;

import static calculator.values.Real.*;

import java.util.HashMap;

import calculator.functions.Functions;
import calculator.functions.Operators;
import lombok.Getter;

public class Complex implements Number {
	
	private static final HashMap<Double, HashMap<Double, Complex>> interns = new HashMap<>();
	public static final Complex I = (Complex) valueOf(0, 1),
			ONE_HALF_I = (Complex) valueOf(0, 0.5);
	
	public static Number valueOf(double r, double c) {
		if (Math.abs(c) < 1e-15)
			c = 0;
		
		if (c == 0.0)
			return Real.valueOf(r);
		// prevents negative zero
		if (r == 0.0)
			r = 0.0;
		HashMap<Double, Complex> map = interns.get(r);
		Complex result;
		
		if (map == null) {
			interns.put(r, map = new HashMap<>());
			map.put(c, result = new Complex(r, c));
		} else {
			result = map.get(c);
			if (result == null)
				map.put(c, result = new Complex(r, c));
		}
		return result;
	}
	
	@Getter
	public final double real, imag;
	
	protected Complex(double d, double d2) {
		real = d;
		if (d2 == 0.0)
			throw new IllegalArgumentException("imaginary part was 0");
		imag = d2;
	}
	
	@Override
	public Number negate() {
		return valueOf(-real, -imag);
	}
	
	@Override
	public Real abs() {
		return Real.valueOf(abs(real, imag));
	}
	
	public Number pow(int i) {
		if (i == 0)
			return Real.ONE;
		if (i < 0) {
			return Real.ONE.divide(pow(-i));
		}
		Number result = this;
		for (int j = 1; j < i; j++)
			result = result.times(this);
		return result;
	}
	
	public static double abs(double a, double b) {
		return Math.sqrt(a * a + b * b);
	}
	
	public static Number sqrt(double real, double imag) {
		double r = abs(real, imag);
		
		double sqrt_r = Math.sqrt(r);
		
		double n_a, n_b, d;
		
		n_a = sqrt_r * (real + r);
		n_b = sqrt_r * imag;
		
		d = abs(real + r, imag);
		
		n_a /= d;
		n_b /= d;
		
		return valueOf(n_a, n_b);
	}
	
	public static Number pow(double a, double b, double c, double d) {
		if (b == 0.0 && d == 0.0)
			return Real.valueOf(Math.pow(a, c));
		
		double p1 = Math.pow(a * a + b * b, c / 2)
				* Math.exp(-d * Math.atan2(b, a));
		double theta = c * Math.atan2(b, a) + 0.5 * d * Math.log(a * a + b * b);
		double real = Functions.round(p1 * Math.cos(theta), 15);
		double cmpx = Functions.round(p1 * Math.sin(theta), 15);
		
		if (cmpx == 0.0)
			return Real.valueOf(real);
		
		return valueOf(real, cmpx);
	}
	
	public static Number divide(double a, double b, double c, double d) {
		double divisor = c * c + d * d;
		double real = (a * c + b * d) / divisor;
		double cmpx = (b * c - a * d) / divisor;
		if (Double.isNaN(real) || Double.isNaN(cmpx))
			return Real.NaN;
		return new Complex(real, cmpx);
	}
	
	public static Complex multiply(double a, double b, double c, double d) {
		double real = a * c - b * d;
		double cmpx = a * d + b * c;
		return new Complex(real, cmpx);
	}
	
	public static Number ln(double a, double b) {
		return valueOf(Math.log(a * a + b * b), Math.atan2(b, a));
	}
	
	private static String toString(double d) {
		if (d == 0.0)
			return "0";
		String str = String.valueOf(d);
		if (str.endsWith(".0"))
			return str.substring(0, str.length() - 2);
		return str;
	}
	
	@Override
	public String toString() {
		if (imag == 0)
			return toString(real);
		if (real == 0) {
			if (imag == 1)
				return "i";
			return toString(imag) + "i";
		}
		return toString(real) + (imag < 0? "" : "+")
				+ (imag == 1.0? "" : toString(imag)) + "i";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Number))
			return false;
		if (obj instanceof Real) {
			return real == ((Real) obj).doubleValue() && imag == 0.0;
		} else {
			Complex c = (Complex) obj;
			return real == c.real && imag == c.imag;
		}
	}
	
	public static Number sin(Complex x) {
		double a = x.real, b = x.imag;
		return valueOf(Math.cosh(b) * Math.sin(a), Math.cos(a) * Math.sinh(b));
	}
	
	public static Number cos(Complex x) {
		double a = x.real, b = x.imag;
		return valueOf(Math.cos(a) * Math.cosh(b), -Math.sin(a) * Math.sinh(b));
	}
	
	public static Number tan(Complex x) {
		return sin(x).divide(cos(x));
	}
	
	public static Number asin(Complex x) {
		return I.negate().times(Functions
				.ln(I.times(x).plus(Functions.sqrt(Real.ONE.minus(x.pow(2))))));
	}
	
	public static Number acos(Complex x) {
		return Real.ONE_HALF.times(Real.PI.minus(Real.TWO.times(asin(x))));
	}
	
	public static Number atan(Complex x) {
		// double a = x.real, b = x.imag;
		Number Ix = I.times(x);
		return ONE_HALF_I.times(Functions.ln(Real.ONE.minus(Ix))
				.minus(Functions.ln(ONE.plus(Ix))));
	}
	
	public static Number atan2(Complex x, Complex y) {
		return I.negate().times(Functions.ln(x.plus(I.times(y))
				.divide(Functions.sqrt(x.times(x).plus(y.times(y))))));
	}
	
	public static Number sinh(Complex x) {
		double a = x.real, b = x.imag;
		return valueOf(Math.sinh(a) * Math.cos(b), Math.cosh(a) * Math.sin(b));
	}
	
	public static Number cosh(Complex x) {
		double a = x.real, b = x.imag;
		return valueOf(Math.cosh(a) * Math.cos(b), Math.sinh(a) * Math.sin(b));
	}
	
	public static Number tanh(Complex x) {
		return sinh(x).divide(cosh(x));
	}
	
	public static Number asinh(Complex x) {
		return Functions.ln(x.plus(Functions.sqrt(ONE.plus(x.times(x)))));
	}
	
	public static Number acosh(Complex x) {
		return Functions.ln(x.plus(Functions.sqrt((Complex) x.minus(ONE))
				.times(Functions.sqrt((Complex) x.plus(ONE)))));
	}
	
	public static Number atanh(Complex x) {
		return ONE_HALF.times(Functions.ln((Complex) x.plus(ONE))
				.minus(Functions.ln((Complex) x.negate().plus(ONE))));
	}
	
	@Override
	public Number plus(Real r) {
		return Operators.add(r, this);
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
		return Operators.multiply(r, this);
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
	
}
