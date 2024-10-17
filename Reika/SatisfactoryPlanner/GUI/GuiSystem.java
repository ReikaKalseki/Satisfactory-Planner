package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.Objects.Resource;
import Reika.SatisfactoryPlanner.GUI.Windows.SplashScreenController;
import Reika.SatisfactoryPlanner.Util.BitflagMap;
import Reika.SatisfactoryPlanner.Util.JavaUtil;
import Reika.SatisfactoryPlanner.Util.Logging;

import fxexpansions.GuiInstance;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
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

		splashGui = GuiInstance.loadFXMLWindow("SplashScreen", loadingStage, null, "Satisfactory Planner");
		loadingStage.show();
		loadingStage.sizeToScene();
		loadingStage.setResizable(false);
		loadingStage.centerOnScreen();
		loadingStage.setAlwaysOnTop(true);

		GuiUtil.queueTask("Loading Game Data", this::parseData, (id) -> this.loadMainUI());
	}

	private void parseData(UUID task) throws Exception {
		if (Database.wasGameJSONFound()) {
			Main.parseGameData();
		}
		else {
			CompletableFuture<Void> wait = new CompletableFuture();
			GuiUtil.runOnJFXThread(() -> {
				GuiUtil.raiseDialog(AlertType.WARNING, "Game Data Not Found", "The game data JSON was not found. This probably means the specified game install directory is incorrect. Correct this in the settings to get automatic inclusion of vanilla content.", a -> {
					a.initOwner(loadingStage); //keeps it on top
					Button settingBtn = (Button)a.getDialogPane().lookupButton(ButtonType.NEXT);
					settingBtn.setText("Settings");
					settingBtn.addEventFilter(ActionEvent.ACTION, e -> {
						Stage put = new Stage();
						try {
							GuiInstance.loadFXMLWindow("Settings", put, loadingStage, "Application Settings");
						}
						catch (IOException e1) {
							Logging.instance.log(e1);
						}
						put.sizeToScene();
						put.showAndWait();
					});
				}, 600, ButtonType.OK, ButtonType.NEXT);
				wait.complete(null);
			});
			while (!wait.isDone())
				Thread.sleep(50);
		}
	}

	private void loadMainUI() throws IOException {
		if (Resource.areAnyIconsMissing()) {
			String warn = "";
			String iconDumpBtn = "To get detailed instructions on how to obtain, install, and use IconDumper, click the 'IconDumper' button below.";
			if (Resource.doAnyIconsExist()) {
				if (Resource.areAnyVanillaIconsMissing()) {
					warn = "Some items, buildings, etc are missing fetchable in-game icons, including vanilla ones. The tool will use backup copies of icons, but this may not be fully up to date with the game.\n\nYou can try updating your copy of this application, or, for automatic inclusion of all icons, including from mods, you can install the IconDumper mod.\n"+iconDumpBtn;
				}
				else {
					warn = "Some modded items, buildings, etc are missing fetchable in-game icons.\n\nAs it is impossible to include all modded icons, to have automatic icon loading, you will need to use the IconDumper mod.\n"+iconDumpBtn;
				}
			}
			else {
				warn = "No in-game icons for items, buildings, etc were found.\nThe tool will use backup copies of icons, but this may not be fully up to date with the game and will not include icons for modded assets.\n\nFor automatic inclusion of all icons, including from mods, you can install the IconDumper mod.\n"+iconDumpBtn;
			}
			GuiUtil.raiseDialog(AlertType.WARNING, "No Icons Found", warn, a -> {
				a.initOwner(loadingStage); //keeps it on top
				Button icoDmpBtn = (Button)a.getDialogPane().lookupButton(ButtonType.NEXT);
				String iconDumpUse = "Download and install the Satisfactory Mod Manager, then install the IconDumper mod. Once loaded into a world with everything unlocked, open its UI and dump all icons (at least 128x recommended size), of all types.";//"Download And Installation:\nClick the 'Install' button below to download and install the mod.\n\nUsage:\nLoad the game in a save with everything unlocked, scroll through the codex to force the game to load all icons, and then close the codex and hit Ctrl-R to export. There should now be an 'Icons' folder in the game directory. Restart this application.";
				icoDmpBtn.setText("IconDumper");
				icoDmpBtn.addEventFilter(ActionEvent.ACTION, e -> {
					GuiUtil.raiseDialog(AlertType.INFORMATION, "IconDumper Instructions", iconDumpUse, a2 -> {
						a2.initOwner(loadingStage);/*
						Button downloadBtn = (Button)a2.getDialogPane().lookupButton(ButtonType.NEXT);
						downloadBtn.setText("Install");
						downloadBtn.addEventFilter(ActionEvent.ACTION, e2 -> {Main.installIconDumper(); e2.consume();});*/
					}, 800, ButtonType.OK/*, ButtonType.NEXT*/);
					e.consume();
				});
			}, 800, ButtonType.OK, ButtonType.NEXT);
		}
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

	public static Stage getMainStage() {
		return primaryStage;
	}
}
