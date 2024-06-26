package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.Recipe;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;

public class RecipeMatrix extends RecipeMatrixBase {

	private final ArrayList<Recipe> recipes = new ArrayList();

	private final Runnable changeCallback;

	protected int addNewRow;
	protected int deleteColumn;

	public RecipeMatrix() {
		this(null);
	}

	public RecipeMatrix(Runnable onChange) {
		changeCallback = onChange;
	}

	public void addRecipe(Recipe r) {
		if (recipes.contains(r))
			return;
		recipes.add(r);
		Collections.sort(recipes);
		if (changeCallback != null)
			changeCallback.run();
	}

	public void removeRecipe(Recipe r) {
		recipes.remove(r);
		if (changeCallback != null)
			changeCallback.run();
	}

	@Override
	public GridPane createGrid(ControllerBase con) throws IOException {
		GridPane gp = new GridPane();
		this.computeIO();
		addNewRow = this.addRow(gp);
		titlesRow = this.addRow(gp);
		titleGapRow = this.addRow(gp);
		minorRowGaps.clear();
		for (int i = 0; i < recipes.size(); i++) {
			this.addRow(gp);
			if (i < recipes.size()-1)
				minorRowGaps.add(this.addRow(gp)); //separator
		}
		deleteColumn = this.addColumn(gp); //delete
		nameColumn = this.addColumn(gp); //name
		mainGapColumn = this.addColumn(gp); //separator
		minorColumnGaps.clear();

		this.addInputColumns(gp);

		inoutGapColumn = this.addColumn(gp); //separator

		this.addOutputColumns(gp);

		ingredientsStartColumn = mainGapColumn+1;
		productsStartColumn = inoutGapColumn+1;

		recipeEntries.clear();

		for (int i = 0; i < recipes.size(); i++) {
			this.addRecipeRow(con, gp, recipes.get(i), i);
		}
		this.createDivider(gp, mainGapColumn, titlesRow, 0);
		this.createDivider(gp, inoutGapColumn, titlesRow, 1);
		this.createRowDivider(gp, titleGapRow, 0);
		for (int row : minorRowGaps)
			this.createRowDivider(gp, row, 2);

		ArrayList<Recipe> li = new ArrayList(Database.getAllRecipes());
		li.removeAll(recipes);
		ChoiceBox<Recipe> cb = new ChoiceBox(FXCollections.observableList(li));
		cb.setConverter(new StringConverter<Recipe>() {
			@Override
			public String toString(Recipe r) {
				return r == null ? "" : r.name;
			}

			@Override
			public Recipe fromString(String id) {
				return Database.lookupRecipe(id);
			}
		});
		cb.getSelectionModel().selectedItemProperty().addListener((val, old, nnew) -> {
			this.addRecipe(nnew);
		});
		cb.setDisable(li.isEmpty());
		//cb.setPrefWidth(-1);
		cb.setPrefHeight(32);
		cb.setMinHeight(Region.USE_PREF_SIZE);
		cb.setMaxHeight(Region.USE_PREF_SIZE);
		cb.setMinWidth(Region.USE_PREF_SIZE);
		cb.setMaxWidth(Region.USE_PREF_SIZE);
		cb.minWidthProperty().bind(gp.widthProperty().subtract(24));
		gp.add(cb, 0, addNewRow);
		gp.setColumnSpan(cb, GridPane.REMAINING);

		this.addTitles(gp);

		gp.getColumnConstraints().get(0).setMinWidth(32);

		gp.setHgap(4);
		gp.setVgap(4);
		//gp.setGridLinesVisible(true);
		return gp;
	}

	@Override
	protected int addRecipeRow(ControllerBase con, GridPane gp, Recipe r, int i) throws IOException {
		int rowIndex = super.addRecipeRow(con, gp, r, i);
		Button b = new Button();
		b.setGraphic(new ImageView(new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/delete.png"))));
		b.setPrefWidth(32);
		b.setPrefHeight(32);
		b.setMinHeight(Region.USE_PREF_SIZE);
		b.setMaxHeight(Region.USE_PREF_SIZE);
		b.setMinWidth(Region.USE_PREF_SIZE);
		b.setMaxWidth(Region.USE_PREF_SIZE);
		b.setOnAction(e -> {
			this.removeRecipe(r);
		});
		gp.add(b, deleteColumn, rowIndex);
		return rowIndex;
	}

	@Override
	public List<Recipe> getRecipes() {
		return Collections.unmodifiableList(recipes);
	}

}
