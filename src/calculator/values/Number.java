package calculator.values;

import calculator.errors.TypeError;

public interface Number {
	Number negate();
	
	Number abs();
	
	Number sqrt();
	
	Number cbrt();
	
	Number root(int root);
	
	Number inverse();
	
	Number plus(Real r);
	
	Number plus(Complex c);
	
	Number plus(Amount a);
	
	Number minus(Real r);
	
	Number minus(Complex c);
	
	Number minus(Amount a);
	
	Number times(Real r);
	
	Number times(Complex c);
	
	Number times(Amount a);
	
	Number divide(Real r);
	
	Number divide(Complex c);
	
	Number divide(Amount a);
	
	Number pow(Real r);
	
	Number pow(Complex c);
	
	Number pow(Amount a);
	
	boolean isGreaterThan(Real r);
	
	boolean isGreaterThan(Complex c);
	
	boolean isGreaterThan(Amount a);
	
	boolean isLessThan(Real r);
	
	boolean isLessThan(Complex c);
	
	boolean isLessThan(Amount a);
	
	boolean isGreaterThanOrEqualTo(Real r);
	
	boolean isGreaterThanOrEqualTo(Complex c);
	
	boolean isGreaterThanOrEqualTo(Amount a);
	
	boolean isLessThanOrEqualTo(Real r);
	
	boolean isLessThanOrEqualTo(Complex c);
	
	boolean isLessThanOrEqualTo(Amount a);
	
	default Number plus(Number n) {
		if (n instanceof Complex)
			return this.plus((Complex) n);
		else if (n instanceof Real)
			return this.plus((Real) n);
		else if (n instanceof Amount)
			return this.plus((Amount) n);
		else
			throw new TypeError();
	}
	
	default Number minus(Number n) {
		if (n instanceof Complex)
			return this.minus((Complex) n);
		else if (n instanceof Real)
			return this.minus((Real) n);
		else if (n instanceof Amount)
			return this.minus((Amount) n);
		else
			throw new TypeError();
	}
	
	default Number times(Number n) {
		if (n instanceof Complex)
			return this.times((Complex) n);
		else if (n instanceof Real)
			return this.times((Real) n);
		else if (n instanceof Amount)
			return this.times((Amount) n);
		else
			throw new TypeError();
	}
	
	default Number divide(Number n) {
		if (n instanceof Complex)
			return this.divide((Complex) n);
		else if (n instanceof Real)
			return this.divide((Real) n);
		else if (n instanceof Amount)
			return this.divide((Amount) n);
		else
			throw new TypeError();
	}
	
	default Number pow(Number n) {
		if (n instanceof Complex)
			return this.pow((Complex) n);
		else if (n instanceof Real)
			return this.pow((Real) n);
		else if (n instanceof Amount)
			return this.pow((Amount) n);
		else
			throw new TypeError();
	}
	
	default boolean isGreaterThan(Number n) {
		if (n instanceof Complex)
			return this.isGreaterThan((Complex) n);
		else if (n instanceof Real)
			return this.isGreaterThan((Real) n);
		else if (n instanceof Amount)
			return this.isGreaterThan((Amount) n);
		else
			throw new TypeError();
	}
	
	default boolean isLessThan(Number n) {
		if (n instanceof Complex)
			return this.isLessThan((Complex) n);
		else if (n instanceof Real)
			return this.isLessThan((Real) n);
		else if (n instanceof Amount)
			return this.isLessThan((Amount) n);
		else
			throw new TypeError();
	}
	
	default boolean isGreaterThanOrEqualTo(Number n) {
		if (n instanceof Complex)
			return this.isGreaterThanOrEqualTo((Complex) n);
		else if (n instanceof Real)
			return this.isGreaterThanOrEqualTo((Real) n);
		else if (n instanceof Amount)
			return this.isGreaterThanOrEqualTo((Amount) n);
		else
			throw new TypeError();
	}
	
	default boolean isLessThanOrEqualTo(Number n) {
		if (n instanceof Complex)
			return this.isLessThanOrEqualTo((Complex) n);
		else if (n instanceof Real)
			return this.isLessThanOrEqualTo((Real) n);
		else if (n instanceof Amount)
			return this.isLessThanOrEqualTo((Amount) n);
		else
			throw new TypeError();
	}
	
}
