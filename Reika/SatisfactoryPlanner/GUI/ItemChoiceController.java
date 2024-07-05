package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.function.Consumer;

import org.controlsfx.control.SearchableComboBox;

import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Database;

import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

@Deprecated
public class ItemChoiceController extends ControllerBase {

	@FXML
	private Button acceptButton;

	@FXML
	private SearchableComboBox<Consumable> dropdown;

	private Consumer<Consumable> callback;

	@Override
	public void init(HostServices services) throws IOException {

	}

	@Override
	protected void postInit(WindowBase w) throws IOException {
		super.postInit(w);
		this.setFont(this.getRootNode(), GuiSystem.getDefaultFont());

		ObservableList<Consumable> li = FXCollections.observableArrayList(Database.getAllItems());
		dropdown.setItems(li);

		dropdown.setButtonCell(new ItemListCell("Choose Item...", true));
		dropdown.setCellFactory(c -> new ItemListCell("", false));

		dropdown.getSelectionModel().selectedItemProperty().addListener((val, old, nnew) -> {
			acceptButton.setDisable(nnew == null);
		});
		acceptButton.setOnAction(e -> {callback.accept(dropdown.getSelectionModel().getSelectedItem());});
	}

	public void setCallback(Consumer<Consumable> c) {
		callback = c;
	}

}

