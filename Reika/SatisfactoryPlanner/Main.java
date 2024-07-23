package Reika.SatisfactoryPlanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.GUI.GuiSystem;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.MainWindow;
import Reika.SatisfactoryPlanner.GUI.MainGuiController;
import Reika.SatisfactoryPlanner.GUI.Setting;
import Reika.SatisfactoryPlanner.GUI.Setting.SettingRef;
import Reika.SatisfactoryPlanner.Util.FixedList;
import Reika.SatisfactoryPlanner.Util.JSONUtil;
import Reika.SatisfactoryPlanner.Util.Logging;

import javafx.application.Application;
import javafx.application.Platform;

public class Main {

	public static boolean isJFXActive;

	private static final boolean isCompiled = Main.class.getResource("Main.class").toString().startsWith("jar:");
	public static final File executionLocation = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());

	private static final File settingsFile = getRelativeFile("settings.dat");
	private static final File recentFilesFile = getRelativeFile("recent.dat");

	private static final FixedList<File> recentFiles = new FixedList(10);

	public static void main(String[] args) throws Exception {
		//System.out.println("Running with effective root of "+effectiveRoot.getCanonicalPath());
		Logging.instance.log("Running in compiled environment: "+isCompiled+" @ "+executionLocation);
		Logging.instance.log("Relative root: "+getRelativeFile(""));
		if (recentFilesFile.exists()) {
			for (String s : FileUtils.readLines(recentFilesFile, Charset.defaultCharset())) {
				recentFiles.add(new File(s));
			}
		}

		JSONObject settings = JSONUtil.readFile(settingsFile);
		for (SettingRef s : Setting.getSettings()) {
			if (settings.has(s.name))
				s.setting.parse(settings.getString(s.name));
		}

		Platform.setImplicitExit(false);

		parseGameData();

		Application.launch(GuiSystem.class, args);
		for (SettingRef s : Setting.getSettings()) {
			settings.put(s.name, s.setting.getString());
		}
		JSONUtil.saveFile(settingsFile, settings);
		Platform.exit();
	}

	public static void parseGameData() throws IOException {
		/*
		Database.loadItems();
		Database.loadBuildings();
		Database.loadRecipes();
		 */
		Database.clear();
		Database.parseGameJSON();
		Database.loadVanillaData();
		Database.loadCustomData();
		Database.sort();
		for (Consumable c : Database.getAllItems())
			c.createIcon(); //cache default icon size
		MainWindow main = GuiSystem.MainWindow.getGUI();
		if (main != null) {
			((MainGuiController)main.controller).rebuildLists(true, true);
		}
	}

	public static void addRecentFile(File f) {
		recentFiles.remove(f);
		recentFiles.addLast(f);
		try {
			recentFilesFile.createNewFile();
			FileUtils.writeLines(recentFilesFile, recentFiles);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<File> getRecentFiles() {
		return Collections.unmodifiableList(recentFiles);
	}
	/*
	public static File extractResourceFolder(String name) {
		File f = getRelativeFile("Resources/"+name+"/");
		if (f.isDirectory()) {
			return f;
		}
		else {
			System.out.println("Extracting resource folder '"+name+"'");
			if (f.mkdirs()) {
				//TODO extraction of assets
				return f;
			}
			else {
				throw new RuntimeException("Failed to extract '"+name+"'");
			}
		}
	}
	 */
	public static File getRelativeFile(String path) {
		return isCompiled ? new File(path) : new File("src/main/java/Reika/SatisfactoryPlanner/", path);
	}

	public static InputStream getResourceStream(String path) throws FileNotFoundException {
		return isCompiled ? Main.class.getResourceAsStream(path) : new FileInputStream(new File(path));
	}
}
