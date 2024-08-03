package Reika.SatisfactoryPlanner;

@Deprecated
public class FactoryManagementThread extends Thread {

	public static final FactoryManagementThread instance = new FactoryManagementThread();

	private boolean isRunning = true;

	private FactoryManagementThread() {
		super("Factory Management");
		this.setDaemon(true);
	}

	@Override
	public void run() {
		while (isRunning) {

		}
	}

	public void shutdown() {
		isRunning = false;
	}

	static {
		//instance.start();
	}

}
