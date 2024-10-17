package Reika.SatisfactoryPlanner.Util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.io.FileUtils;

import Reika.SatisfactoryPlanner.Main;

public class Logging {

	public static final Logging instance = new Logging();

	private File currentLogFile;

	private final Thread loggingThread = new Thread(() -> this.runLogger(), "Logging");

	private final Date dateInstance = new Date();
	private final ConcurrentLinkedQueue<String> logQueue = new ConcurrentLinkedQueue();

	private Logging() {
		try {
			this.updateLogPath();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		loggingThread.setDaemon(true);
		loggingThread.start();
	}

	private void runLogger() {
		while (!Main.isClosing()) {
			try {
				this.flushLog();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void updateLogPath() throws IOException {
		if (currentLogFile != null) {
			this.flushLog();
		}
		currentLogFile = this.getOrCreateLogFile();
		if (currentLogFile != null) {
			if (!currentLogFile.exists())
				currentLogFile.createNewFile();
		}
	}

	private File getOrCreateLogFile() {
		if (!Main.isCompiled())
			return null;
		File f = Main.getRelativeFile("Logs/"+new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date())+".log");
		f.getParentFile().mkdirs();
		return f;
	}

	public void flushLog() throws IOException {
		if (currentLogFile != null)
			FileUtils.writeLines(currentLogFile, logQueue, true);
		logQueue.clear();
	}

	public void log(Throwable e) {
		this.log(e.toString(), System.err);
		e.printStackTrace();
	}

	public void log(String msg) {
		this.log(msg, System.out);
	}

	public void log(String msg, PrintStream buf) {
		dateInstance.setTime(System.currentTimeMillis());
		String log = "["+Main.timeStampFormat.format(dateInstance)+"] [Thread "+Thread.currentThread().getName()+"]: "+msg;
		logQueue.add(log);
		synchronized (buf) {
			buf.println(log);
		}
	}

}
