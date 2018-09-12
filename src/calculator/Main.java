package calculator;

public class Main {
	public static void main(String[] args) throws Exception {
		if (args.length != 0) {
			System.setProperty("user.dir", args[0]);
		}
		/// System.out.println(Unit.valueOf("s^-1").isStandardUnit());
		// System.out.println(Unit.valueOf("N").getClass());
		// AlternateUnit u = (AlternateUnit)SI.NEWTON;
		// System.out.println(Unit.valueOf("mL"));
		/*Unit kgms2 = Unit.valueOf("kg*m/s^2");
		Unit N = SI.NEWTON;
		Unit lbsfts2 = Unit.valueOf("lb*ft/s^2");
		
		System.out.println(kgms2.getStandardUnit());
		System.out.println(N.getStandardUnit());
		System.out.println(lbsfts2.getStandardUnit());
		
		System.out.println(kgms2.equals(N));
		System.out.println(kgms2.isCompatible(N));
		System.out.println(kgms2.isCompatible(lbsfts2));
		
		/**/
		Console.run();
	}
}
