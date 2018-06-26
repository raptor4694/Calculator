package calculator;

public class Main {
	public static void main(String[] args) throws Exception {
		
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
