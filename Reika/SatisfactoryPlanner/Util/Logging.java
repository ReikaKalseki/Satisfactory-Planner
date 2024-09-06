package Reika.SatisfactoryPlanner.Util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import Reika.SatisfactoryPlanner.Main;

public class Logging {

	public static final Logging instance = new Logging();

	private File currentLogFile;

	private final Thread loggingThread = new Thread(() -> this.runLogger(), "Logging");

	private final SimpleDateFormat logTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private final Date dateInstance = new Date();
	private final ConcurrentLinkedQueue<Byte> logQueue = new ConcurrentLinkedQueue();

	private Logging() {
		System.setOut(new LogDiversionStream(System.out));
		System.setErr(new LogDiversionStream(System.err));
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
		currentLogFile = /*Setting.LOG.getCurrentValue()*/LogOptions.RUNTIME.getOrCreateLogFile();
		if (currentLogFile != null) {
			if (!currentLogFile.exists())
				currentLogFile.createNewFile();
		}
	}

	public void flushLog() throws IOException {
		if (currentLogFile != null) {
			byte[] raw = new byte[logQueue.size()];
			int idx = 0;
			for (Byte b : logQueue) {
				raw[idx] = b;
				idx++;
			}
			Files.write(currentLogFile.toPath(), raw, StandardOpenOption.APPEND);
		}
		logQueue.clear();
	}

	public void log(Throwable e) {
		this.log(e.toString(), System.err);
		e.printStackTrace();
	}

	public void log(String msg) {
		this.log(msg, System.out);
	}

	private void log(String msg, PrintStream buf) {
		dateInstance.setTime(System.currentTimeMillis());
		String log = "["+logTimeStamp.format(dateInstance)+"] [Thread "+Thread.currentThread().getName()+"]: "+msg;
		buf.println(log);
	}

	public static enum LogOptions {
		//NONE,
		//COMMON,
		RUNTIME;

		public File getOrCreateLogFile() {
			switch(this) {/*
				case NONE:
				default:
					return null;
				case COMMON:
					return Main.getRelativeFile("SharedLog.log");*/default:
					case RUNTIME:
						if (!Main.isCompiled())
							return null;
						File f = Main.getRelativeFile("Logs/"+new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date())+".log");
						f.getParentFile().mkdirs();
						return f;
			}
		}
	}

	private class LogDiversionStream extends PrintStream {

		public LogDiversionStream(PrintStream wrapped) {
			super(wrapped);
		}

		@Override
		public void write(byte buf[], int off, int len) {
			super.write(buf, off, len);
			int to = off+len;
			for (int i = off; i < to; i++) {
				logQueue.add(buf[i]);
			}
		}
	}

}
