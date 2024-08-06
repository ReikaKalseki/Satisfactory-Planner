package fxexpansions;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import Reika.SatisfactoryPlanner.GUI.GuiSystem;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public abstract class FXMLControllerBase extends ControllerBase  {

	protected FXMLControllerBase owner;

	private Stage container;
	private Parent rootNode;

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

		this.setFont(this.getRootNode(), GuiSystem.getDefaultFont());
	}

	public final <C extends FXMLControllerBase> GuiInstance<C> loadNestedFXML(String fxml, Pane container) throws IOException {
		return this.loadNestedFXML(fxml, inner -> container.getChildren().add(inner));
	}

	public final <C extends FXMLControllerBase> GuiInstance<C> loadNestedFXML(String fxml, TabPane container) throws IOException {
		return this.loadNestedFXML(fxml, inner -> {Tab t = new Tab(); t.setContent(inner); container.getTabs().add(t);});
	}

	public final <C extends FXMLControllerBase> GuiInstance<C> loadNestedFXML(String fxml, Tab container) throws IOException {
		return this.loadNestedFXML(fxml, inner -> {container.setContent(inner);});
	}

	public final <C extends FXMLControllerBase> GuiInstance<C> loadNestedFXML(String fxml, GridPane container, int col, int row) throws IOException {
		return this.loadNestedFXML(fxml, inner -> container.add(inner, col, row));
	}

	public final <C extends FXMLControllerBase> GuiInstance<C> loadNestedFXML(String fxml, Consumer<Parent> acceptor) throws IOException {
		if (container == null)
			throw new RuntimeException("You can only load nested FXML in post-init, after the window is initialized!");
		GuiInstance<C> ret = GuiInstance.loadFXML(fxml, container);
		acceptor.accept(ret.rootNode);
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

