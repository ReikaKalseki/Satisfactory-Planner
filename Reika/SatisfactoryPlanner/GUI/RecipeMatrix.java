package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.List;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.Recipe;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

public class RecipeMatrix extends RecipeMatrixBase {

	public final Factory owner;

	protected int deleteColumn;

	public RecipeMatrix(Factory f) {
		owner = f;
	}

	@Override
	public GridPane createGrid(ControllerBase con) throws IOException {
		GridPane gp = new GridPane();
		this.computeIO();
		List<Recipe> recipes = this.getRecipes();
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

		buildingGapColumn = this.addColumn(gp);
		buildingColumn = this.addColumn(gp);

		ingredientsStartColumn = mainGapColumn+1;
		productsStartColumn = inoutGapColumn+1;

		recipeEntries.clear();

		for (int i = 0; i < recipes.size(); i++) {
			this.addRecipeRow(con, gp, recipes.get(i), i);
		}
		this.createDivider(gp, mainGapColumn, titlesRow, 0);
		this.createDivider(gp, inoutGapColumn, titlesRow, 1);
		this.createDivider(gp, buildingGapColumn, titlesRow, 1);
		this.createRowDivider(gp, titleGapRow, 0);
		for (int row : minorRowGaps)
			this.createRowDivider(gp, row, 2);

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
			owner.removeRecipe(r);
		});
		gp.add(b, deleteColumn, rowIndex);
		return rowIndex;
	}

	@Override
	public List<Recipe> getRecipes() {
		return owner.getRecipes();
	}

}
