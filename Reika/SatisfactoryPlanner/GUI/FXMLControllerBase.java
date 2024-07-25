package Reika.SatisfactoryPlanner.GUI;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import Reika.SatisfactoryPlanner.GUI.GuiSystem.DialogWindow;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.GuiInstance;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public abstract class FXMLControllerBase extends ControllerBase  {

	protected FXMLControllerBase owner;

	protected WindowBase container;

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
		return container.root;
	}

	protected final void close() {
		container.window.close();
	}

	public abstract void init(HostServices services) throws IOException;

	protected void postInit(WindowBase w) throws IOException {
		container = w;
		container.window.widthProperty().addListener((v, o, n) -> {this.onWindowResize();});
		container.window.heightProperty().addListener((v, o, n) -> {this.onWindowResize();});

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
		GuiInstance<C> ret = GuiSystem.loadFXML(fxml, container);
		acceptor.accept(ret.rootNode);
		ret.controller.owner = this;
		return ret;
	}

	public final void openFXMLDialog(String title, String fxml) throws IOException {
		this.openFXMLDialog(title, fxml, null);
	}

	public final void openFXMLDialog(String title, String fxml, Consumer<FXMLControllerBase> callback) throws IOException {
		if (container == null)
			throw new RuntimeException("You can only load nested FXML in post-init, after the window is initialized!");
		DialogWindow dialog = new DialogWindow(title, fxml, container);
		dialog.controller.owner = this;
		if (callback != null) {
			callback.accept(dialog.controller);
		}
		dialog.window.sizeToScene();
		dialog.show();
	}

	public final void openChildWindow(WindowBase w) throws IOException {
		if (container == null)
			throw new RuntimeException("You can only load nested FXML in post-init, after the window is initialized!");
		w.controller.owner = this;
		w.window.sizeToScene();
		w.show();
	}

	protected final File openDirDialog(String title, File dir) {
		DirectoryChooser fc = new DirectoryChooser();
		if (dir != null && dir.exists() && dir.isDirectory())
			fc.setInitialDirectory(dir);
		fc.setTitle("Choose "+title+" directory");
		return fc.showDialog(container.window);
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
		return fc.showOpenDialog(container.window);
	}

	protected final File openSaveAsDialog(String initialName, File dir) {
		FileChooser fc = new FileChooser();
		if (dir != null && dir.exists() && dir.isDirectory())
			fc.setInitialDirectory(dir);
		fc.setInitialFileName(initialName);
		fc.setTitle("Choose file");
		return fc.showSaveDialog(container.window);
	}

	protected final void setVisible(Node n, boolean visible) {
		n.setVisible(visible);
		n.setManaged(visible);
	}

	public final void setFont(Node n, Font f) {
		if (n instanceof TextInputControl) {
			Font fp = ((TextInputControl)n).getFont();
			double size = fp != null ? fp.getSize() : 12;
			((TextInputControl)n).setFont(f);
		}
		if (n instanceof Labeled) {
			Font fp = ((Labeled)n).getFont();
			double size = fp != null ? fp.getSize() : 12;
			if (n instanceof TitledPane) {
				size = 14;
			}
			((Labeled)n).setFont(f);
			this.setFont(((Labeled)n).getGraphic(), f);
		}
		if (n instanceof ComboBox) {
			n.setStyle(GuiSystem.getFontStyle());
		}
		if (n instanceof ChoiceBox) {
			n.setStyle(GuiSystem.getFontStyle());
		}
		if (n instanceof Parent) {
			for (Node n2 : ((Parent)n).getChildrenUnmodifiable()) {
				this.setFont(n2, f);
			}
		}
	}

	public void onWindowResize() {

	}


}

