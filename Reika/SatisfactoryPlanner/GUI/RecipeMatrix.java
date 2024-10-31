package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.List;

import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.ItemConsumerProducer;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Data.Objects.Recipe;
import Reika.SatisfactoryPlanner.GUI.RecipeMatrixContainer.MatrixType;

import javafx.scene.layout.GridPane;

public class RecipeMatrix extends RecipeMatrixBase {

	public RecipeMatrix(Factory f) {
		super(f, MatrixType.MAIN);
	}

	@Override
	public void rebuildGrid() throws IOException {
		this.computeIO();
		List<ItemConsumerProducer> recipes = this.getRecipes();
		titlesRow = this.addRow();
		titleGapRow = this.addRow();
		minorRowGaps.clear();
		for (int i = 0; i < recipes.size(); i++) {
			this.addRow();
			if (i < recipes.size()-1)
				minorRowGaps.add(this.addRow()); //separator
		}
		buttonColumn = this.addColumn(); //delete
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
		//this.createDivider(inoutGapColumn, titlesRow, 1);
		this.createDivider(buildingGapColumn, titlesRow, 1);
		this.createRowDivider(titleGapRow, 0);
		for (int row : minorRowGaps)
			this.createRowDivider(row, 2);

		GridPane gp = this.getGrid();
		for (int i = 0; i < inputs.size()-1; i++) {
			Consumable c = inputs.get(i);
			int idx = ingredientsStartColumn+inputs.indexOf(c)*2;
			this.createDivider(idx+1, titleGapRow+1, 2);
		}
		for (int i = 0; i < outputs.size()-1; i++) {
			Consumable c = outputs.get(i);
			int idx = productsStartColumn+outputs.indexOf(c)*2;
			this.createDivider(idx+1, titleGapRow+1, 2);
		}

		this.addTitles();
	}

	@Override
	protected void onClickPrefixButton(Recipe r) {
		owner.removeRecipe(r);
	}

	@Override
	protected String getPrefixButtonIcon() {
		return "button-cancel-icon";
	}

	@Override
	public void onSetCount(Recipe r, double count) {

	}

	@Override
	public void onUpdateIO() {

	}

}
