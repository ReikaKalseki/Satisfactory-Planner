package fxexpansions;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.function.Consumer;

import Reika.SatisfactoryPlanner.GUI.GuiSystem;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;

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
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
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
			e.printStackTrace();
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

	public final <C extends FXMLControllerBase> GuiInstance<C> loadNestedFXML(String fxml, Pane container) throws IOException {
		return this.loadNestedFXML(fxml, inner -> container.getChildren().add(inner.rootNode));
	}

	public final <C extends FXMLControllerBase> GuiInstance<C> loadNestedFXML(String fxml, TabPane container) throws IOException {
		return this.loadNestedFXML(fxml, inner -> {Tab t = new Tab(); t.setContent(inner.rootNode); container.getTabs().add(t);});
	}

	public final <C extends FXMLControllerBase> GuiInstance<C> loadNestedFXML(String fxml, Tab container) throws IOException {
		return this.loadNestedFXML(fxml, inner -> {container.setContent(inner.rootNode);});
	}

	public final <C extends FXMLControllerBase> GuiInstance<C> loadNestedFXML(String fxml, GridPane container, int col, int row) throws IOException {
		return this.loadNestedFXML(fxml, inner -> container.add(inner.rootNode, col, row));
	}

	public final <C extends FXMLControllerBase> GuiInstance<C> loadNestedFXML(String fxml, Consumer<GuiInstance> acceptor) throws IOException {
		if (container == null)
			throw new RuntimeException("You can only load nested FXML in post-init, after the window is initialized!");
		GuiInstance<C> ret = GuiInstance.loadFXML(fxml, container);
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

	protected final File openDirDialog(String title, File dir) {
		DirectoryChooser fc = new DirectoryChooser();
		if (dir != null && dir.exists() && dir.isDirectory())
			fc.setInitialDirectory(dir);
		fc.setTitle("Choose "+title+" directory");
		return fc.showDialog(container);
	}

	protected final File openFileDialog(String title, File dir, FileChooser.ExtensionFilter... filters) {
		FileChooser fc = new FileChooser();
		if (dir != null && dir.exists() && dir.isDirectory())
			fc.setInitialDirectory(dir);
		fc.setTitle("Choose "+title+" file");
		if (filters.length > 0) {
			for (FileChooser.ExtensionFilter extFilter : filters) {
				fc.getExtensionFilters().add(extFilter);
			}
			fc.setSelectedExtensionFilter(filters[0]);
		}
		return fc.showOpenDialog(container);
	}

	protected final File openSaveAsDialog(String initialName, File dir) {
		FileChooser fc = new FileChooser();
		if (dir != null && dir.exists() && dir.isDirectory())
			fc.setInitialDirectory(dir);
		fc.setInitialFileName(initialName);
		fc.setTitle("Choose file");
		return fc.showSaveDialog(container);
	}

	protected final void setVisible(Node n, boolean visible) {
		n.setVisible(visible);
		n.setManaged(visible);
	}

	public void onWindowResize() {

	}


}

