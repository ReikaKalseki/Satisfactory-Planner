package fxexpansions;

import java.io.IOException;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public abstract class WindowBase<C extends FXMLControllerBase> {

	public final Stage window;
	public final Scene display;
	public final C controller;

	public final Image icon;

	protected WindowBase(String title, String fxmlName, Image icon) throws IOException {
		window = new Stage();
		if (icon != null)
			window.getIcons().add(icon);
		//window.setHeight(640);
		window.setResizable(true);

		this.icon = icon;

		GuiInstance<C> gui = GuiInstance.loadFXML(fxmlName, this);
		controller = gui.controller;

		display = new Scene(gui.rootNode);

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