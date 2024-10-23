package fxexpansions;

import java.io.IOException;
import java.util.HashSet;
import java.util.function.Consumer;

import Reika.SatisfactoryPlanner.GUI.GuiSystem;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;
import Reika.SatisfactoryPlanner.Util.Logging;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public abstract class FXMLControllerBase extends ControllerBase  {

	protected FXMLControllerBase owner;

	private Stage container;
	private Parent rootNode;

	private final HashSet<KeyCode> pressedKeys = new HashSet();

	@FXML
	public final void initialize() {
		try {
			this.init(GuiSystem.getHSVC());
		}
		catch (IOException e) {
			Logging.instance.log(e);
		}
	}

	@Override
	public final Parent getRootNode() {
		return rootNode;
	}

	public Stage getWindow() {
		return container;
	}

	protected final void close() {
		container.close();
	}

	final void setRoot(Parent root) {
		rootNode = root;
	}

	public abstract void init(HostServices services) throws IOException;

	protected void postInit(Stage w) throws IOException {
		container = w;
		container.widthProperty().addListener((v, o, n) -> {this.onWindowResize();});
		container.heightProperty().addListener((v, o, n) -> {this.onWindowResize();});
		Scene s = w.getScene();
		s.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			if (pressedKeys.add(event.getCode()))
				this.onKeyPressed(event.getCode());
		});
		s.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
			if (pressedKeys.remove(event.getCode()))
				this.onKeyReleased(event.getCode());
		});

		GuiUtil.initWidgets(this);
	}

	protected void onKeyPressed(KeyCode code) {

	}

	protected void onKeyReleased(KeyCode code) {

	}

	public final <C extends FXMLControllerBase> GuiInstance<C> loadNestedFXML(String fxml, Pane parent) throws IOException {
		return this.loadNestedFXML(fxml, inner -> parent.getChildren().add(inner.rootNode));
	}

	public final <C extends FXMLControllerBase> GuiInstance<C> loadNestedFXML(String fxml, TabPane parent) throws IOException {
		return this.loadNestedFXML(fxml, inner -> {Tab t = new Tab(); t.setContent(inner.rootNode); parent.getTabs().add(t);});
	}

	public final <C extends FXMLControllerBase> GuiInstance<C> loadNestedFXML(String fxml, Tab parent) throws IOException {
		return this.loadNestedFXML(fxml, inner -> {parent.setContent(inner.rootNode);});
	}

	public final <C extends FXMLControllerBase> GuiInstance<C> loadNestedFXML(String fxml, GridPane parent, int col, int row) throws IOException {
		return this.loadNestedFXML(fxml, inner -> parent.add(inner.rootNode, col, row));
	}

	public final <C extends FXMLControllerBase> GuiInstance<C> loadNestedFXML(String fxml, Consumer<GuiInstance> acceptor) throws IOException {
		if (container == null)
			throw new RuntimeException("You can only load nested FXML in post-init, after the window is initialized!");
		GuiInstance<C> ret = GuiInstance.loadFXML(fxml, container);
		if (acceptor != null)
			acceptor.accept(ret);
		ret.controller.owner = this;
		return ret;
	}

	public final void openChildWindow(String title, String fxml) throws IOException {
		this.openChildWindow(title, fxml, null);
	}

	public final void openChildWindow(String title, String fxml, Consumer<GuiInstance<FXMLControllerBase>> callback) throws IOException {
		if (container == null)
			throw new RuntimeException("You can only load nested FXML in post-init, after the window is initialized!");
		Stage put = new Stage();
		GuiInstance<FXMLControllerBase> gui = GuiInstance.loadFXMLWindow(fxml, put, container, title);
		gui.controller.owner = this;
		if (callback != null) {
			callback.accept(gui);
		}
		put.sizeToScene();
		put.show();
	}

	protected final void setVisible(Node n, boolean visible) {
		n.setVisible(visible);
		n.setManaged(visible);
	}

	public void onWindowResize() {

	}


}

