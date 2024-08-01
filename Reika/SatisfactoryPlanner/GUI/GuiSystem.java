package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.io.InputStream;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Util.BitflagMap;
import Reika.SatisfactoryPlanner.Util.JavaUtil;

import fxexpansions.WindowBase;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GuiSystem extends Application {

	private static final BitflagMap<Font, FontModifier> fontMap = new BitflagMap();

	private static Image icon;
	private static HostServices service;

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.hide();

		service = this.getHostServices();

		fontMap.put(Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans-Regular.ttf"), 12));
		fontMap.put(Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans-Bold.ttf"), 12), FontModifier.BOLD);
		fontMap.put(Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans-SemiBold.ttf"), 12), FontModifier.SEMIBOLD);
		fontMap.put(Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans-Italic.ttf"), 12), FontModifier.ITALIC);
		fontMap.put(Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans-BoldItalic.ttf"), 12), FontModifier.ITALIC, FontModifier.BOLD);
		fontMap.put(Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans-SemiBoldItalic.ttf"), 12), FontModifier.ITALIC, FontModifier.SEMIBOLD);
		fontMap.put(Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans_Condensed-Regular.ttf"), 12), FontModifier.CONDENSED);
		fontMap.put(Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans_Condensed-Bold.ttf"), 12), FontModifier.BOLD, FontModifier.CONDENSED);
		fontMap.put(Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans_Condensed-SemiBold.ttf"), 12), FontModifier.SEMIBOLD, FontModifier.CONDENSED);
		fontMap.put(Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans_Condensed-Italic.ttf"), 12), FontModifier.ITALIC, FontModifier.CONDENSED);
		fontMap.put(Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans_Condensed-BoldItalic.ttf"), 12), FontModifier.ITALIC, FontModifier.BOLD, FontModifier.CONDENSED);
		fontMap.put(Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans_Condensed-SemiBoldItalic.ttf"), 12), FontModifier.ITALIC, FontModifier.SEMIBOLD, FontModifier.CONDENSED);

		InputStream in = this.getClass().getResourceAsStream("icon.png");
		if (in != null)
			icon = new Image(in);


		Main.isJFXActive = true;
		if (!Database.wasGameJSONFound()) {
			GuiUtil.raiseDialog(AlertType.WARNING, "Game Data Not Found", "The game data JSON was not found. This probably means the specified game install directory is incorrect. Correct this in the settings to get automatic inclusion of vanilla content.", ButtonType.OK);
		}

		//this.setFont(root, MainWindow.getGUI().getFont(10));
		new MainWindow().show();
	}
	/*
	public static Font getFont(double size) {
		return Font.font(font.getFamily(), size);
	}

	public static String getFontStyle() {
		return "-fx-font-family: \""+getFont(1).getFamily()+"\";";
	}

	public static String getFontStyle(int size) {
		return "-fx-font-family: \""+getFont(1).getFamily()+"\"; -fx-font-size: "+size+"px;";
	}
	 */
	public static Font getDefaultFont() {
		return getFont();
	}

	public static Font getFont(FontModifier... settings) {
		return fontMap.get(settings);
	}

	public static String getFontStyle(FontModifier... settings) {
		String ret= "-fx-font-family: \""+getFont(settings).getFamily()+"\";";
		if (JavaUtil.makeListFromArray(settings).contains(FontModifier.BOLD))
			ret += "-fx-font-weight: bold;";
		return ret;
	}

	public static Image getIcon() {
		return icon;
	}

	public static HostServices getHSVC() {
		return service;
	}

	public static enum FontModifier {
		SEMIBOLD,
		BOLD,
		ITALIC,
		CONDENSED,
	}

	public static class PreWindow extends WindowBase {

		protected PreWindow(String title, String fxmlName) throws IOException {
			super(title, fxmlName, getIcon());

			window.initModality(Modality.APPLICATION_MODAL);
		}

		@Override
		public void show() throws IOException {
			window.toFront();
			super.show();
		}

	}

	public static class MainWindow extends WindowBase<MainGuiController> {

		private static MainWindow gui;

		public MainWindow() throws IOException {
			super("Satisfactory Planner", "mainUI-Dynamic", getIcon());
			gui = this;

			//window.setWidth(1200);
			//window.setHeight(900);
		}

		@Override
		protected void onCloseButtonClicked() {
			Platform.exit();
		}

		public static MainWindow getGUI() {
			return gui;
		}

	}

	public static class SettingsWindow extends WindowBase<SettingsController> {

		public SettingsWindow() throws IOException {
			super("Application Settings", "Settings", getIcon());
		}

		@Override
		protected void onCloseButtonClicked() {
			try {
				Setting.applyChanges();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
