package Reika.SatisfactoryPlanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Setting.SettingRef;
import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.Recipe;
import Reika.SatisfactoryPlanner.GUI.GuiSystem;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;
import Reika.SatisfactoryPlanner.GUI.MainGuiController;
import Reika.SatisfactoryPlanner.GUI.RecipeListCell;
import Reika.SatisfactoryPlanner.Util.FixedList;
import Reika.SatisfactoryPlanner.Util.JSONUtil;
import Reika.SatisfactoryPlanner.Util.JavaUtil;
import Reika.SatisfactoryPlanner.Util.Logging;

import fxexpansions.GuiInstance;
import javafx.application.Application;
import javafx.application.Platform;

public class Main {

	public static boolean isJFXActive;

	private static final boolean isCompiled = Main.class.getResource("Main.class").toString().startsWith("jrt:");
	public static final Path executionLocation = Paths.get("").toAbsolutePath();

	public static final SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	private static final File settingsFile = getRelativeFile("settings.dat");
	private static final File recentFilesFile = getRelativeFile("recent.dat");

	private static final FixedList<File> recentFiles = new FixedList(10);

	private static boolean isClosing;

	public static void main(String[] args) throws Exception {
		//System.out.println("Running with effective root of "+effectiveRoot.getCanonicalPath());
		Logging.instance.log("Running in compiled environment: "+isCompiled+" @ "+executionLocation);
		Logging.instance.log("Relative root: "+getRelativeFile(""));
		Logging.instance.log("JMX Settings: "+System.getProperty("com.sun.management.jmxremote")+" (port "+System.getProperty("com.sun.management.jmxremote.port")+")");
		/*
		File f = new File("debug.txt");
		f.createNewFile();
		FileUtils.writeLines(f, Arrays.asList(Main.class.getResource("Main.class").toString(), isCompiled, executionLocation.toString(), getRelativeFile("").getCanonicalPath()));
		 */
		Logging.instance.log("Loading settings from "+settingsFile.getCanonicalPath());
		JSONObject settings = JSONUtil.readFile(settingsFile);
		for (SettingRef s : Setting.getSettings()) {
			if (settings.has(s.name))
				s.setting.parse(settings.getString(s.name));
		}

		if (recentFilesFile.exists()) {
			for (String s : FileUtils.readLines(recentFilesFile, Charset.defaultCharset())) {
				recentFiles.add(new File(s));
			}
		}

		Platform.setImplicitExit(false);

		Database.checkForGameJSON();

		Logging.instance.log("===================");
		Logging.instance.log("Starting Gui System");
		Logging.instance.log("===================");
		Application.launch(GuiSystem.class, args);
		Logging.instance.log("===================");
		Logging.instance.log("Gui System Closed");
		Logging.instance.log("===================");

		for (SettingRef s : Setting.getSettings()) {
			settings.put(s.name, s.setting.getString());
		}
		Logging.instance.log("Saving settings to "+settingsFile.getCanonicalPath());
		JSONUtil.saveFile(settingsFile, settings);
		isClosing = true;
		JavaUtil.stopThreads();
		Platform.exit();
		Logging.instance.log("Closing Application");
		Logging.instance.flushLog();
	}

	public static void parseGameData() throws Exception {
		Logging.instance.log("===================");
		Logging.instance.log("Parsing Recipe/Item Data");
		Logging.instance.log("===================");
		Database.clear();
		Database.checkForGameJSON();
		GuiSystem.setSplashProgress(2);
		Database.parseGameJSON();
		GuiSystem.setSplashProgress(10);
		Database.loadVanillaData();
		GuiSystem.setSplashProgress(60);
		Database.loadModdedData();
		GuiSystem.setSplashProgress(70);
		Database.loadCustomData();
		GuiSystem.setSplashProgress(75);
		for (Recipe r : Database.getAllRecipes()) {
			r.updateBuildingTier();
			for (Consumable c : r.getProductsPerMinute().keySet()) {
				c.addRecipe(r);
			}
		}
		Database.sort();
		for (Consumable c : Database.getAllItems())
			c.createIcon(); //cache default icon size
		GuiInstance<MainGuiController> main = GuiSystem.getMainGUI();
		if (main != null) {
			RecipeListCell.init();
			Platform.runLater(() -> {
				main.controller.rebuildEntireUI();
				main.controller.setFactory(new Factory());
				main.controller.layout();
			});
		}
		GuiSystem.setSplashProgress(80);
		try {
			Thread.sleep(50); //let splash screen catch up
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		Logging.instance.log("===================");
		Logging.instance.log("Recipe/Item Data Parsed");
		Logging.instance.log("===================");
	}

	public static void updateMainUI() {
		GuiInstance<MainGuiController> main = GuiSystem.getMainGUI();
		if (main != null)
			GuiUtil.runOnJFXThread(() -> main.controller.updateStats(true));
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

	public static File getRelativeFile(String path) {
		return isCompiled ? new File(path).getAbsoluteFile() : new File("src/main/java/Reika/SatisfactoryPlanner/", path);
	}

	public static InputStream getResourceStream(String path) throws FileNotFoundException {
		return isCompiled ? Main.class.getResourceAsStream(path) : new FileInputStream(new File(path));
	}

	public static File getModsFolder() {
		return new File(Setting.GAMEDIR.getCurrentValue(), "FactoryGame/Mods");
	}

	public static boolean isCompiled() {
		return isCompiled;
	}

	public static boolean isClosing() {
		return isClosing;
	}
	/*
	public static void installIconDumper() {
		String url = "https://cdn.discordapp.com/attachments/1253701121533284474/1253724210275024916/IconDumper-Windows.zip?ex=66e05d28&is=66df0ba8&hm=9c55b19057838dde0bed76ccf3bdb6e78f80f8053a7bab4a69d95f0c4e236bb8&";

	}*/
}
