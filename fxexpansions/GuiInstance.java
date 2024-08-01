package fxexpansions;

import java.io.IOException;
import java.util.function.Consumer;

import Reika.SatisfactoryPlanner.Main;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class GuiInstance<C extends ControllerBase> {

	public final Parent rootNode;
	public final C controller;

	public GuiInstance(Parent root, C c) {
		rootNode = root;
		controller = c;
	}

	public static <C extends FXMLControllerBase> GuiInstance<C> loadFXML(String fxml, WindowBase window) throws IOException {
		return loadFXML(fxml, window, null);
	}

	public static <C extends FXMLControllerBase> GuiInstance<C> loadFXML(String fxml, WindowBase window, Consumer<Parent> rootHandler) throws IOException {
		FXMLLoader loader = new FXMLLoader(Main.class.getResource("Resources/FXML/"+fxml+".fxml"));
		//loader.setControllerFactory(c -> c.getConstructor(HostServices.class).newInstance(null));
		Parent root = loader.load();
		if (rootHandler != null)
			rootHandler.accept(root);
		FXMLControllerBase c = loader.getController();
		c.setRoot(root);
		c.postInit(window);
		return new GuiInstance(root, c);
	}

	@Override
	public String toString() {
		return rootNode+" x "+controller;
	}

}