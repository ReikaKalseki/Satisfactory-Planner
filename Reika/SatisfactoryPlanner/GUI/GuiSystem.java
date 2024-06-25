package Reika.SatisfactoryPlanner.GUI;

import java.io.File;
import java.io.FileInputStream;
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
import javafx.stage.Stage;

public class GuiSystem extends Application {

	public static Image icon;
	public static HostServices service;

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.hide();

		service = this.getHostServices();

		InputStream in = this.getClass().getResourceAsStream("icon.png");
		if (in != null)
			icon = new Image(in);

		//this.setFont(root, MainWindow.getGUI().getFont(10));
		Main.isJFXActive = true;
		new MainWindow().show();
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

		private Font font;

		public MainWindow() throws IOException {
			super("Satisfactory Planner", "mainUI-Dynamic");
			gui = this;

			File f = this.getFontFile();
			font = f == null ? null : Font.loadFont(new FileInputStream(f), 12);
			if (font == null) {
				System.err.println("Could not load font file!");
				font = Font.font(12);
			}

			window.setWidth(1200);
			window.setHeight(900);
		}

		private File getFontFile() {
			return null;
		}

		@Override
		protected void onCloseButtonClicked() {

			Platform.exit();
		}

		public Font getFont(double size) {
			return Font.font(font.getFamily(), size);
		}

		public String getFontStyle() {
			return "-fx-font-family: \""+this.getFont(1).getFamily()+"\";";
		}

		public String getFontStyle(int size) {
			return "-fx-font-family: \""+this.getFont(1).getFamily()+"\"; -fx-font-size: "+size+"px;";
		}

		public static MainWindow getGUI() {
			return gui;
		}

	}

	/*
	public static void forceUpdate() {
		if (Platform.isFxApplicationThread()) {
			try {
				((MainGuiController)MainWindow.getGUI().controller).updateTick(true);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			Platform.runLater(() -> {
				try {
					((MainGuiController)MainWindow.getGUI().controller).updateTick(true);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
	}*/
}
