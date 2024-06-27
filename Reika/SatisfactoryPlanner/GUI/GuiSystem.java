package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.io.InputStream;

import Reika.SatisfactoryPlanner.Main;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GuiSystem extends Application {

	private static Font font;
	private static Image icon;
	private static HostServices service;

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.hide();

		service = this.getHostServices();

		font = Font.loadFont(Main.class.getResourceAsStream("Resources/Fonts/OpenSans-Regular.ttf"), 12);
		if (font == null) {
			System.err.println("Could not load font file!");
			font = Font.font(12);
		}

		InputStream in = this.getClass().getResourceAsStream("icon.png");
		if (in != null)
			icon = new Image(in);

		//this.setFont(root, MainWindow.getGUI().getFont(10));
		Main.isJFXActive = true;
		new MainWindow().show();
	}

	public static Font getFont(double size) {
		return Font.font(font.getFamily(), size);
	}

	public static String getFontStyle() {
		return "-fx-font-family: \""+getFont(1).getFamily()+"\";";
	}

	public static String getFontStyle(int size) {
		return "-fx-font-family: \""+getFont(1).getFamily()+"\"; -fx-font-size: "+size+"px;";
	}

	public static Font getDefaultFont() {
		return font;
	}

	public static Image getIcon() {
		return icon;
	}

	public static HostServices getHSVC() {
		return service;
	}

	public static GuiInstance loadFXML(String fxml, WindowBase window) throws IOException {
		FXMLLoader loader = new FXMLLoader(Main.class.getResource("Resources/FXML/"+fxml+".fxml"));
		Parent root = loader.load();
		ControllerBase c = loader.getController();
		c.postInit(window);
		return new GuiInstance(root, c);
	}

	static class GuiInstance {

		protected final Parent rootNode;
		protected final ControllerBase controller;

		private GuiInstance(Parent root, ControllerBase c) {
			rootNode = root;
			controller = c;
		}

	}

	public static class MainWindow extends WindowBase {

		private static MainWindow gui;

		public MainWindow() throws IOException {
			super("Satisfactory Planner", "mainUI-Dynamic");
			gui = this;

			window.setWidth(1200);
			window.setHeight(900);
		}

		@Override
		protected void onCloseButtonClicked() {
			Platform.exit();
		}

		public static MainWindow getGUI() {
			return gui;
		}

	}

	public static class DialogWindow extends WindowBase {

		protected DialogWindow(String title, String fxmlName, WindowBase from) throws IOException {
			super(title, fxmlName);

			window.initModality(Modality.APPLICATION_MODAL);
			window.initOwner(from.window);
		}

		@Override
		public void show() throws IOException {
			window.toFront();
			super.show();
		}

	}
}
