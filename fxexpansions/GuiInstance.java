package fxexpansions;

import java.io.IOException;
import java.util.function.Consumer;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.GUI.GuiSystem;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class GuiInstance<C extends ControllerBase> {

	public final Parent rootNode;
	public final C controller;

	public GuiInstance(Parent root, C c) {
		rootNode = root;
		controller = c;
	}

	public static <C extends FXMLControllerBase> GuiInstance<C> loadFXML(String fxml, Stage window) throws IOException {
		return loadFXML(fxml, window, null);
	}

	public static <C extends FXMLControllerBase> GuiInstance<C> loadFXML(String fxml, Stage window, Consumer<Parent> rootHandler) throws IOException {
		FXMLLoader loader = new FXMLLoader(Main.class.getResource("Resources/FXML/"+fxml+".fxml"));
		//loader.setControllerFactory(c -> c.getConstructor(HostServices.class).newInstance(null));
		Parent root = loader.load();
		if (rootHandler != null)
			rootHandler.accept(root);

		FXMLControllerBase c = loader.getController();
		c.setRoot(root);
		c.postInit(window);
		Scene s = window.getScene();
		s.getStylesheets().add(Main.class.getResource("Resources/CSS/style.css").toString());
		return new GuiInstance(root, c);
	}

	public static <C extends FXMLControllerBase> GuiInstance<C> loadFXMLWindow(String fxml, Stage window, Stage owner, String title) throws IOException {
		Consumer<Parent> callback = (p) -> {
			window.setResizable(true);
			window.setTitle(title);
			window.setScene(new Scene(p));
			window.initStyle(StageStyle.DECORATED);
			if (owner != null)
				window.initOwner(owner);
			if (GuiSystem.getIcon() != null)
				window.getIcons().add(GuiSystem.getIcon());
		};
		return loadFXML(fxml, window, callback);
	}

	@Override
	public String toString() {
		return rootNode+" x "+controller;
	}

}