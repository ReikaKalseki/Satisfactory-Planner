package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;

import Reika.SatisfactoryPlanner.GUI.GuiSystem.GuiInstance;

import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public abstract class WindowBase<C extends FXMLControllerBase> {

	public final Stage window;
	public final Scene display;
	public final Parent root;
	public final C controller;

	protected WindowBase(String title, String fxmlName) throws IOException {
		window = new Stage();
		if (GuiSystem.getIcon() != null)
			window.getIcons().add(GuiSystem.getIcon());
		//window.setHeight(640);
		window.setResizable(true);

		GuiInstance<C> gui = GuiSystem.loadFXML(fxmlName, this);
		root = gui.rootNode;
		controller = gui.controller;

		display = new Scene(root);

		display.setFill(Color.rgb(0x22, 0xaa, 0xff));

		window.setTitle(title);
		window.setScene(display);
		window.initStyle(StageStyle.DECORATED);

		window.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				WindowBase.this.closeWindow();
			}
		});
	}

	private void closeWindow() {
		this.onCloseButtonClicked();
		window.close();
	}

	protected void onCloseButtonClicked() {

	}

	public void show() throws IOException {
		//controller.postInit(GuiSystem.service);
		window.showAndWait();
	}
}