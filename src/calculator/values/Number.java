package calculator.values;

public interface Number {
	Number negate();
	
	Number abs();
	
	Number plus(Real r);
	
	Number plus(Complex c);
	
	Number minus(Real r);
	
	Number minus(Complex c);
	
	Number times(Real r);
	
	Number times(Complex c);
	
	Number divide(Real r);
	
	Number divide(Complex c);
	
	Number pow(Real r);
	
	Number pow(Complex c);
	
	default Number plus(Number n) {
		if (n instanceof Complex)
			return this.plus((Complex) n);
		return this.plus((Real) n);
	}
	
	default Number minus(Number n) {
		if (n instanceof Complex)
			return this.minus((Complex) n);
		return this.minus((Real) n);
	}
	
	default Number times(Number n) {
		if (n instanceof Complex)
			return this.times((Complex) n);
		return this.times((Real) n);
	}
	
	default Number divide(Number n) {
		if (n instanceof Complex)
			return this.divide((Complex) n);
		return this.divide((Real) n);
	}
	
	default Number pow(Number n) {
		if (n instanceof Complex)
			return this.pow((Complex) n);
		return this.pow((Real) n);
	}
	
}
