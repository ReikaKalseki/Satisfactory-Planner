package Reika.SatisfactoryPlanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.GUI.GuiSystem;
import Reika.SatisfactoryPlanner.Util.Logging;

import javafx.application.Application;
import javafx.application.Platform;

public class Main {

	public static boolean isJFXActive;

	private static final boolean isCompiled = Main.class.getResource("Main.class").toString().startsWith("jar:");
	public static final File executionLocation = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());

	public static void main(String[] args) throws Exception {
		//System.out.println("Running with effective root of "+effectiveRoot.getCanonicalPath());
		Logging.instance.log("Running in compiled environment: "+isCompiled+" @ "+executionLocation);
		Logging.instance.log("Relative root: "+getRelativeFile(""));
		Platform.setImplicitExit(false);
		/*
		Database.loadItems();
		Database.loadBuildings();
		Database.loadRecipes();
		 */
		Database.parseGameJSON();
		for (Consumable c : Database.getAllItems())
			c.createIcon(); //cache default icon size
		Application.launch(GuiSystem.class, args);
		Platform.exit();
	}

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

	public static File getRelativeFile(String path) {
		return isCompiled ? new File(path) : new File("src/main/java/Reika/SatisfactoryPlanner/", path);
	}

	public static InputStream getResourceStream(String path) throws FileNotFoundException {
		return isCompiled ? Main.class.getResourceAsStream(path) : new FileInputStream(new File(path));
	}
}
