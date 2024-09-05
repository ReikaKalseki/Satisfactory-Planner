package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Util.BitflagMap;
import Reika.SatisfactoryPlanner.Util.JavaUtil;
import Reika.SatisfactoryPlanner.Util.Logging;

import fxexpansions.GuiInstance;
import javafx.animation.PauseTransition;
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
import javafx.util.Duration;

public class GuiSystem extends Application {

	private static final BitflagMap<Font, FontModifier> fontMap = new BitflagMap();

	private static Image icon;
	private static HostServices service;
	private static Stage loadingStage;
	private static Stage primaryStage;
	private static GuiInstance<MainGuiController> mainGui;
	private static GuiInstance<SplashScreenController> splashGui;

	@Override
	public void start(Stage primary) throws Exception {
		primaryStage = primary;
		loadingStage = new Stage();
		Logging.instance.log("Gui System Initializing");
		service = this.getHostServices();

		primaryStage.setOnCloseRequest(e -> Platform.exit());
		loadingStage.setOnCloseRequest(e -> Platform.exit());

		fontMap.put(Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans/Regular.ttf"), 12));
		fontMap.put(Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans/Bold.ttf"), 12), FontModifier.BOLD);
		fontMap.put(Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans/SemiBold.ttf"), 12), FontModifier.SEMIBOLD);
		fontMap.put(Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans/Italic.ttf"), 12), FontModifier.ITALIC);
		fontMap.put(Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans/BoldItalic.ttf"), 12), FontModifier.ITALIC, FontModifier.BOLD);
		fontMap.put(Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans/SemiBoldItalic.ttf"), 12), FontModifier.ITALIC, FontModifier.SEMIBOLD);
		fontMap.put(Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans/Condensed-Regular.ttf"), 12), FontModifier.CONDENSED);
		fontMap.put(Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans/Condensed-Bold.ttf"), 12), FontModifier.BOLD, FontModifier.CONDENSED);
		fontMap.put(Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans/Condensed-SemiBold.ttf"), 12), FontModifier.SEMIBOLD, FontModifier.CONDENSED);
		fontMap.put(Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans/Condensed-Italic.ttf"), 12), FontModifier.ITALIC, FontModifier.CONDENSED);
		fontMap.put(Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans/Condensed-BoldItalic.ttf"), 12), FontModifier.ITALIC, FontModifier.BOLD, FontModifier.CONDENSED);
		fontMap.put(Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans/Condensed-SemiBoldItalic.ttf"), 12), FontModifier.ITALIC, FontModifier.SEMIBOLD, FontModifier.CONDENSED);

		icon = new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/appicon.png"));

		Main.isJFXActive = true;
		if (!Database.wasGameJSONFound()) {
			GuiUtil.raiseDialog(AlertType.WARNING, "Game Data Not Found", "The game data JSON was not found. This probably means the specified game install directory is incorrect. Correct this in the settings to get automatic inclusion of vanilla content.", ButtonType.OK);
		}

		splashGui = GuiInstance.loadFXMLWindow("SplashScreen", loadingStage, null, "Satisfactory Planner");
		loadingStage.show();
		loadingStage.sizeToScene();
		loadingStage.setResizable(false);
		loadingStage.centerOnScreen();
		loadingStage.setAlwaysOnTop(true);
		GuiUtil.queueTask("Loading Game Data", (id) -> Main.parseGameData(), (id) -> this.loadMainUI());
	}

	private void loadMainUI() throws IOException {
		Screen screen = Screen.getPrimary();
		Rectangle2D bounds = screen.getVisualBounds();
		primaryStage.setWidth(bounds.getWidth());
		primaryStage.setHeight(bounds.getHeight());
		Logging.instance.log("Loading main UI");
		mainGui = GuiInstance.loadFXMLWindow("mainUI-Dynamic", primaryStage, null, "Satisfactory Planner");
		Logging.instance.log("Main UI constructed");
		//this.setFont(root, MainWindow.getGUI().getFont(10));
		GuiSystem.setSplashProgress(95);
		primaryStage.setMaximized(true);
		primaryStage.show();
		Logging.instance.log("Main UI loaded, closing splash screen");

		PauseTransition timer = new PauseTransition(Duration.millis(50)); //let splash stick for just long enough to show more progress
		timer.setOnFinished(e -> loadingStage.close());
		timer.play();
	}

	public static boolean isSplashShowing() {
		return loadingStage != null && loadingStage.isShowing();
	}

	public static void setSplashProgress(double pct) {
		if (isSplashShowing())
			splashGui.controller.setProgress(pct);
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
