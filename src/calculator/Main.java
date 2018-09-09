package calculator;

public class Main {
	public static void main(String[] args) throws Exception {
		if (args.length != 0) {
			System.setProperty("user.dir", args[0]);
		}
		
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
