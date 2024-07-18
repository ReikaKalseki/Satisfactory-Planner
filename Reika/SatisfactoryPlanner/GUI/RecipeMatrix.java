package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.Recipe;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

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
	protected RecipeRow addRecipeRow(Recipe r) throws IOException {
		RecipeRow rr = super.addRecipeRow(r);
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
		rr.addNode(deleteColumn, b);
		return rr;
	}

	@Override
	public void onSetCount(Recipe r, float count) {

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
