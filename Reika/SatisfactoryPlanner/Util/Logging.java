package Reika.SatisfactoryPlanner.Util;


public class Logging {

	public static final Logging instance = new Logging();

	private Logging() {

	}

	public void log(String msg) {
		System.out.println("["+Thread.currentThread().getName()+"]: "+msg);
	}

}
