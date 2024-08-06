package Reika.SatisfactoryPlanner.GUI;

import java.io.InputStream;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Util.BitflagMap;
import Reika.SatisfactoryPlanner.Util.JavaUtil;

import fxexpansions.GuiInstance;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class GuiSystem extends Application {

	private static final BitflagMap<Font, FontModifier> fontMap = new BitflagMap();

	private static Image icon;
	private static HostServices service;
	private static GuiInstance<MainGuiController> mainGui;

	@Override
	public void start(Stage primaryStage) throws Exception {
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

		Screen screen = Screen.getPrimary();
		Rectangle2D bounds = screen.getVisualBounds();
		primaryStage.setWidth(bounds.getWidth());
		primaryStage.setHeight(bounds.getHeight());
		mainGui = GuiInstance.loadFXMLWindow("mainUI-Dynamic", primaryStage, null, "Satisfactory Planner");
		//this.setFont(root, MainWindow.getGUI().getFont(10));
		primaryStage.setMaximized(true);
		primaryStage.setOnCloseRequest(e -> Platform.exit());
		primaryStage.show();
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

	public static GuiInstance<MainGuiController> getMainGUI() {
		return mainGui;
	}
}
