package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;

import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.Recipe;

import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;

@Deprecated
public class AddRecipeToGridController extends ControllerBase {


	@FXML
	private Button button;

	@FXML
	private ChoiceBox<Recipe> dropdown;

	private RecipeMatrix grid;

	@Override
	public void init(HostServices services) throws IOException {
		dropdown.setConverter(new StringConverter<Recipe>() {
			@Override
			public String toString(Recipe r) {
				return r == null ? "" : r.name;
			}

			@Override
			public Recipe fromString(String id) {
				return Database.lookupRecipe(id);
			}
		});

		button.setOnAction(e -> {
			grid.addRecipe(dropdown.getSelectionModel().getSelectedItem());
		});

		button.setMaxWidth(Double.POSITIVE_INFINITY);
		((Region)button.getParent()).setMaxWidth(Double.POSITIVE_INFINITY);
	}

	@Override
	protected void postInit(WindowBase w) throws IOException {
		super.postInit(w);

		dropdown.setItems(FXCollections.observableList(Database.getAllRecipes()));

	}

	public void setGrid(RecipeMatrix mat) {
		grid = mat;
	}

}

