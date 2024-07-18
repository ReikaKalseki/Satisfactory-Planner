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

public class RecipeMatrix extends RecipeMatrixBase {

	protected int deleteColumn;

	public RecipeMatrix(Factory f) {
		super(f);
	}

	@Override
	public void rebuildGrid() throws IOException {
		this.computeIO();
		List<Recipe> recipes = this.getRecipes();
		titlesRow = this.addRow();
		titleGapRow = this.addRow();
		minorRowGaps.clear();
		for (int i = 0; i < recipes.size(); i++) {
			this.addRow();
			if (i < recipes.size()-1)
				minorRowGaps.add(this.addRow()); //separator
		}
		deleteColumn = this.addColumn(); //delete
		nameColumn = this.addColumn(); //name
		mainGapColumn = this.addColumn(); //separator
		minorColumnGaps.clear();

		this.addInputColumns();

		inoutGapColumn = this.addColumn(); //separator

		this.addOutputColumns();

		buildingGapColumn = this.addColumn();
		buildingColumn = this.addColumn();

		ingredientsStartColumn = mainGapColumn+1;
		productsStartColumn = inoutGapColumn+1;

		for (int i = 0; i < recipes.size(); i++) {
			this.addRecipeRow(recipes.get(i), i);
		}
		this.createDivider(mainGapColumn, titlesRow, 0);
		this.createDivider(inoutGapColumn, titlesRow, 1);
		this.createDivider(buildingGapColumn, titlesRow, 1);
		this.createRowDivider(titleGapRow, 0);
		for (int row : minorRowGaps)
			this.createRowDivider(row, 2);

		this.addTitles();
	}

	@Override
	protected RecipeRow addRecipeRow(Recipe r, int i) throws IOException {
		RecipeRow rowIndex = super.addRecipeRow(r, i);
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
		grid.add(b, deleteColumn, rowIndex.rowIndex);
		return rowIndex;
	}

	@Override
	public void onSetCount(Recipe r, float count) {

	}

}
