package calculator.values;

import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

public class NumberUnit implements Number {
	private final Amount<?> amount;
	
	private NumberUnit(Amount<?> amt) {
		amount = amt;
	}
	
	public static NumberUnit valueOf(double d, Unit<?> unit) {
		return new NumberUnit(Amount.valueOf(d, unit));
	}
	
	public Amount<?> getAmount() {
		return amount;
	}
	
	@SuppressWarnings("unchecked")
	public double doubleValue() {
		return amount.doubleValue((Unit) amount.getUnit());
	}
	
	public Unit<?> getUnit() {
		return amount.getUnit();
	}
	
	@Override
	public Number negate() {
		return null;
	}
	
	@Override
	public Number abs() {
		return null;
	}
	
	@Override
	public Number plus(Real r) {
		return null;
	}
	
	@Override
	public Number plus(Complex c) {
		return null;
	}
	
	@Override
	public Number minus(Real r) {
		return null;
	}
	
	@Override
	public Number minus(Complex c) {
		return null;
	}
	
	@Override
	public Number times(Real r) {
		return null;
	}
	
	@Override
	public Number times(Complex c) {
		return null;
	}
	
	@Override
	public Number divide(Real r) {
		return null;
	}
	
	@Override
	public Number divide(Complex c) {
		return null;
	}
	
	@Override
	public Number pow(Real r) {
		return null;
	}
	
	@Override
	public Number pow(Complex c) {
		return null;
	}
	
}
