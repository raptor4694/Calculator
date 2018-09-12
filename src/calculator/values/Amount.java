package calculator.values;

import static calculator.functions.Functions.*;

import java.util.HashMap;

import javax.measure.converter.ConversionException;
import javax.measure.converter.UnitConverter;
import javax.measure.unit.AlternateUnit;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.realtime.MemoryArea;

import calculator.errors.TypeError;
import javolution.util.FastComparator;
import javolution.util.FastMap;

@SuppressWarnings({"unchecked", "rawtypes"})
public class Amount implements Number {
	
	static final FastMap<Unit<?>, FastMap<Unit<?>, Unit<?>>> MULT_LOOKUP =
			new FastMap<Unit<?>, FastMap<Unit<?>, Unit<?>>>(
					"UNITS_MULT_LOOKUP").setKeyComparator(FastComparator.DIRECT);
	
	static final FastMap<Unit<?>, Unit<?>> INV_LOOKUP =
			new FastMap<Unit<?>, Unit<?>>("UNITS_INV_LOOKUP").setKeyComparator(
					FastComparator.DIRECT);
	
	static final FastMap<Unit<?>, FastMap<Unit<?>, UnitConverter>> CVTR_LOOKUP =
			new FastMap<Unit<?>, FastMap<Unit<?>, UnitConverter>>(
					"UNITS_CVTR_LOOKUP").setKeyComparator(FastComparator.DIRECT);
	
	public static Unit<?> productOf(Unit<?> left, Unit<?> right) {
		FastMap<Unit<?>, Unit<?>> leftTable = MULT_LOOKUP.get(left);
		if (leftTable == null)
			return calculateProductOf(left, right);
		Unit<?> result = leftTable.get(right);
		if (result == null)
			return calculateProductOf(left, right);
		return result;
	}
	
	private static synchronized Unit<?> calculateProductOf(final Unit<?> left,
			final Unit<?> right) {
		MemoryArea memoryArea = MemoryArea.getMemoryArea(MULT_LOOKUP);
		memoryArea.executeInArea(() -> {
			FastMap<Unit<?>, Unit<?>> leftTable = MULT_LOOKUP.get(left);
			if (leftTable == null) {
				leftTable = new FastMap<Unit<?>, Unit<?>>().setKeyComparator(
						FastComparator.DIRECT);
				MULT_LOOKUP.put(left, leftTable);
			}
			Unit<?> result = leftTable.get(right);
			if (result == null) {
				result = left.times(right);
				leftTable.put(right, result);
			}
		});
		return MULT_LOOKUP.get(left).get(right);
	}
	
	public static Unit<?> inverseOf(Unit<?> unit) {
		Unit<?> inverse = INV_LOOKUP.get(unit);
		if (inverse == null)
			return calculateInverseOf(unit);
		return inverse;
	}
	
	private static synchronized Unit<?> calculateInverseOf(final Unit<?> unit) {
		MemoryArea memoryArea = MemoryArea.getMemoryArea(INV_LOOKUP);
		memoryArea.executeInArea(() -> {
			Unit<?> inverse = INV_LOOKUP.get(unit);
			if (inverse == null) {
				inverse = unit.inverse();
				INV_LOOKUP.put(unit, inverse);
			}
		});
		return INV_LOOKUP.get(unit);
	}
	
	public static UnitConverter converterOf(Unit<?> left, Unit<?> right) {
		FastMap<Unit<?>, UnitConverter> leftTable = CVTR_LOOKUP.get(left);
		if (leftTable == null)
			return calculateConverterOf(left, right);
		UnitConverter result = leftTable.get(right);
		if (result == null)
			return calculateConverterOf(left, right);
		return result;
	}
	
	private static synchronized UnitConverter calculateConverterOf(
			final Unit<?> left, final Unit<?> right) {
		MemoryArea memoryArea = MemoryArea.getMemoryArea(CVTR_LOOKUP);
		memoryArea.executeInArea(() -> {
			FastMap<Unit<?>, UnitConverter> leftTable = CVTR_LOOKUP.get(left);
			if (leftTable == null) {
				leftTable = new FastMap<Unit<?>, UnitConverter>().setKeyComparator(
						FastComparator.DIRECT);
				synchronized (CVTR_LOOKUP) {
					CVTR_LOOKUP.put(left, leftTable);
				}
			}
			UnitConverter result = leftTable.get(right);
			if (result == null) {
				result = left.getConverterTo(right);
				synchronized (leftTable) {
					leftTable.put(right, result);
				}
			}
		});
		return CVTR_LOOKUP.get(left).get(right);
	}
	
	public final double value;
	public final Unit unit;
	
	private Amount(double d, Unit u) {
		value = d;
		unit = u;
	}
	
	public static Amount valueOf(double d) {
		return new Amount(d, Unit.ONE);
	}
	
	private static HashMap<Unit, Unit> CONVERT_UNITS = new HashMap<>();
	static {
		CONVERT_UNITS.put(Unit.valueOf("s^-1"), SI.HERTZ);
		CONVERT_UNITS.put(Unit.valueOf("kg*m/s^2"), SI.NEWTON);
		// CONVERT_UNITS.put(Unit.valueOf("kg*"), /null)
		CONVERT_UNITS.put(Unit.valueOf("kg*m^2/s^2"), SI.JOULE);
	}
	
	public static Amount valueOf(double d, Unit u) {
		if (u.isStandardUnit() && !(u instanceof AlternateUnit)) {
			if (!u.equals(SI.RADIAN) && u.isCompatible(Unit.ONE)) {
				u = Unit.ONE;
			} else {
				for (Unit std : SI.getInstance().getUnits()) {
					if (std instanceof AlternateUnit) {
						if (u.isCompatible(std)) {
							u = std;
							break;
						}
					}
				}
			}
		}
		return new Amount(d, u);
	}
	
	@Override
	public Number negate() {
		return valueOf(-value, unit);
	}
	
	@Override
	public Number abs() {
		return valueOf(Math.abs(value), unit);
	}
	
	@Override
	public Number plus(Real r) {
		if (unit.isCompatible(Unit.ONE))
			return valueOf(value + r.value, unit);
		throw new TypeError();
	}
	
	@Override
	public Number plus(Amount a) {
		a = a.convertTo(unit);
		return valueOf(value + a.value, unit);
	}
	
	@Override
	public Number plus(Complex c) {
		throw new TypeError();
	}
	
	@Override
	public Number minus(Real r) {
		if (unit.isCompatible(Unit.ONE))
			return valueOf(value - r.value, unit);
		throw new TypeError();
	}
	
	@Override
	public Number minus(Amount a) {
		a = a.convertTo(unit);
		return valueOf(value + a.value, unit);
	}
	
	@Override
	public Number minus(Complex c) {
		throw new TypeError();
	}
	
	@Override
	public Number times(Real r) {
		return valueOf(value * r.value, unit);
	}
	
	@Override
	public Number times(Amount a) {
		return valueOf(value * a.value, productOf(unit, a.unit));
	}
	
	@Override
	public Number times(Complex c) {
		throw new TypeError();
	}
	
	@Override
	public Number divide(Real r) {
		return valueOf(value / r.value, unit);
	}
	
	@Override
	public Number divide(Amount a) {
		return valueOf(value / a.value, productOf(unit, inverseOf(a.unit)));
	}
	
	@Override
	public Number divide(Complex c) {
		throw new TypeError();
	}
	
	@Override
	public Number pow(Real r) {
		if (!r.isInt())
			throw new TypeError();
		int i = r.intValue();
		return valueOf(Math.pow(value, r.value), unit.pow(r.intValue()));
	}
	
	@Override
	public Number pow(Amount a) {
		throw new TypeError();
	}
	
	@Override
	public Number pow(Complex c) {
		throw new TypeError();
	}
	
	@Override
	public Number sqrt() {
		return root(2);
	}
	
	@Override
	public Number cbrt() {
		return root(3);
	}
	
	@Override
	public Number root(int root) {
		check(root != 0, DimensionError);
		if (root < 0)
			return root(-root).inverse();
		return valueOf(Math.pow(value, 1.0 / root), unit.root(root));
	}
	
	@Override
	public Number inverse() {
		return valueOf(1.0 / value, inverseOf(unit));
	}
	
	public Amount convertTo(Unit u) {
		check(unit.isCompatible(u), ConversionException);
		return valueOf(converterOf(unit, u).convert(value), u);
	}
	
	@Override
	public boolean isGreaterThan(Real r) {
		check(unit.isCompatible(Unit.ONE), TypeError);
		return value > r.value;
	}
	
	@Override
	public boolean isGreaterThan(Complex c) {
		throw new TypeError();
	}
	
	@Override
	public boolean isGreaterThan(Amount a) {
		Amount a2 = a.convertTo(unit);
		Amount this2 = convertTo(a.unit);
		return value > a2.value || this2.value > a.value;
	}
	
	@Override
	public boolean isLessThan(Real r) {
		check(unit.isCompatible(Unit.ONE), TypeError);
		return value < r.value;
	}
	
	@Override
	public boolean isLessThan(Complex c) {
		throw new TypeError();
	}
	
	@Override
	public boolean isLessThan(Amount a) {
		Amount a2 = a.convertTo(unit);
		Amount this2 = convertTo(a.unit);
		return value < a2.value || this2.value < a.value;
	}
	
	@Override
	public boolean isGreaterThanOrEqualTo(Real r) {
		check(unit.isCompatible(Unit.ONE), TypeError);
		return value >= r.value;
	}
	
	@Override
	public boolean isGreaterThanOrEqualTo(Complex c) {
		throw new TypeError();
	}
	
	@Override
	public boolean isGreaterThanOrEqualTo(Amount a) {
		Amount a2 = a.convertTo(unit);
		Amount this2 = convertTo(a.unit);
		return value >= a2.value || this2.value >= a.value;
	}
	
	@Override
	public boolean isLessThanOrEqualTo(Real r) {
		check(unit.isCompatible(Unit.ONE), TypeError);
		return value <= r.value;
	}
	
	@Override
	public boolean isLessThanOrEqualTo(Complex c) {
		throw new TypeError();
	}
	
	@Override
	public boolean isLessThanOrEqualTo(Amount a) {
		Amount a2 = a.convertTo(unit);
		Amount this2 = convertTo(a.unit);
		return value <= a2.value || this2.value <= a.value;
	}
	
	@Override
	public String toString() {
		if (unit == NonSI.DEGREE_ANGLE)
			return value + "°";
		return value + " " + unit;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Amount) {
			Amount a = (Amount) obj;
			try {
				a = a.convertTo(unit);
			} catch (ConversionException e) {
				return false;
			}
			return value == a.value;
		}
		return false;
	}
	
}
