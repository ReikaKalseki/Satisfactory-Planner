package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.List;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.Recipe;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;

public class RecipeMatrix extends RecipeMatrixBase {

	protected final MatrixColumn deleteColumn = new MatrixColumn();

	public RecipeMatrix(Factory f) {
		super(f);
	}

	@Override
	protected void addInitialColumns() {
		grid.getColumns().add(deleteColumn);
		super.addInitialColumns();
	}

	@Override
	public void createGrid() throws IOException {
		this.computeIO();
		List<Recipe> recipes = this.getRecipes();
		minorRowGaps.clear();
		for (int i = 0; i < recipes.size(); i++) {
			RecipeRow rr = this.addRecipeRow(recipes.get(i));
			if (i < recipes.size()-1)
				minorRowGaps.add(rr.createGap()); //separator
		}
		minorColumnGaps.clear();

		recipeEntries.clear();

		for (GridLine<RowConstraints> row : minorRowGaps)
			this.createRowDivider(row, 2);

		this.addTitles();

		grid.getColumnConstraints().get(0).setMinWidth(32);
		//gp.setGridLinesVisible(true);
	}

	@Override
	protected RecipeRow addRecipeRow(Recipe r) throws IOException {
		RecipeRow rowIndex = super.addRecipeRow(r);
		Button b = new Button();
		b.setGraphic(new ImageView(new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/delete.png"))));
		b.setPrefWidth(32);
		b.setPrefHeight(32);
		b.setMinHeight(Region.USE_PREF_SIZE);
		b.setMaxHeight(Region.USE_PREF_SIZE);
		b.setMinWidth(Region.USE_PREF_SIZE);
		b.setMaxWidth(Region.USE_PREF_SIZE);
		b.setOnAction(e -> {
			owner.removeRecipe(r);
		});
		grid.add(b, deleteColumn.index, rowIndex.getRowIndex());
		return rowIndex;
	}

	@Override
	public List<Recipe> getRecipes() {
		return owner.getRecipes();
	}

	@Override
	public void onSetCount(Recipe r, float count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoaded() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCleared() {
		// TODO Auto-generated method stub

	}

}
