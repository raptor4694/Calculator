package calculator;

public class Main {
	public static void main(String[] args) throws Exception {
		if (args.length != 0) {
			System.setProperty("user.dir", args[0]);
		}
		
		/*Amount<?> v = Amount.valueOf("3 m/s");
		Amount<?> m = Amount.valueOf("2 m");
		Amount<?> F = Amount.valueOf("2 kg*m/s^2");
		
		System.out.println(F.);
		*/
		
		try {
			Console.run();
		} catch (Throwable t) {
			System.err.println(
					"A fatal error has been thrown. The program will terminate.");
			t.printStackTrace();
		}
		/**/
	}
}
