package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.Recipe;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.GuiInstance;
import Reika.SatisfactoryPlanner.Util.MultiMap;

import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public abstract class RecipeMatrixBase {

	protected int titlesRow;
	protected int titleGapRow;
	protected final HashSet<Integer> minorRowGaps = new HashSet();

	protected int nameColumn;
	protected int mainGapColumn;
	protected int buildingGapColumn;
	protected int buildingColumn;
	protected final HashSet<Integer> minorColumnGaps = new HashSet();

	protected int inoutGapColumn;
	protected int ingredientsStartColumn;
	protected int productsStartColumn;

	protected Label nameLabel;
	protected Label consumptionLabel;
	protected Label productionLabel;
	protected Label buildingLabel;

	protected ArrayList<Consumable> inputs;
	protected ArrayList<Consumable> outputs;

	protected final MultiMap<Recipe, GuiInstance> recipeEntries = new MultiMap();

	public final Factory owner;

	protected RecipeMatrixBase(Factory f) {
		owner = f;
	}

	public abstract List<Recipe> getRecipes();

	protected int getMultiplier(Recipe r) {
		return 1;
	}

	public abstract GridPane createGrid(ControllerBase con) throws IOException;

	protected final void computeIO() {
		inputs = new ArrayList(owner.getAllIngredients());
		outputs = new ArrayList(owner.getAllProducedItems());
		Collections.sort(inputs);
		Collections.sort(outputs);
	}

	protected final void addInputColumns(GridPane gp) {
		for (int i = 0; i < inputs.size(); i++) {
			this.addColumn(gp);
			if (i < inputs.size()-1)
				minorColumnGaps.add(this.addColumn(gp)); //separator
		}
		if (inputs.isEmpty())
			this.addColumn(gp); //space for "consuming" title
		if (inputs.size() <= 1)
			gp.getColumnConstraints().get(gp.getColumnCount()-1).setMinWidth(96);
	}

	protected final void addOutputColumns(GridPane gp) {
		for (int i = 0; i < outputs.size(); i++) {
			this.addColumn(gp);
			if (i < outputs.size()-1)
				minorColumnGaps.add(this.addColumn(gp)); //separator
		}
		if (outputs.isEmpty())
			this.addColumn(gp); //space for "producing" title
		if (outputs.size() <= 1)
			gp.getColumnConstraints().get(gp.getColumnCount()-1).setMinWidth(96);
	}

	protected int addRecipeRow(ControllerBase con, GridPane gp, Recipe r, int i) throws IOException {
		Label lb = new Label(r.displayName);
		lb.setFont(Font.font(lb.getFont().getFamily(), FontWeight.BOLD, FontPosture.REGULAR, 14));
		GuiUtil.sizeToContent(lb);
		int rowIndex = titleGapRow+1+i*2;
		gp.add(lb, nameColumn, rowIndex);
		for (Entry<Consumable, Float> e : r.getIngredientsPerMinute().entrySet()) {
			Consumable c = e.getKey();
			GuiInstance gui = con.loadNestedFXML("ItemView", gp, ingredientsStartColumn+inputs.indexOf(c)*2, rowIndex);
			((ItemViewController)gui.controller).setItem(c, e.getValue()*this.getMultiplier(r));
			recipeEntries.addValue(r, gui);
		}
		for (Entry<Consumable, Float> e : r.getProductsPerMinute().entrySet()) {
			Consumable c = e.getKey();
			GuiInstance gui = con.loadNestedFXML("ItemView", gp, productsStartColumn+outputs.indexOf(c)*2, rowIndex);
			((ItemViewController)gui.controller).setItem(c, e.getValue()*this.getMultiplier(r));
			recipeEntries.addValue(r, gui);
		}
		gp.add(r.productionBuilding.createImageView(), buildingColumn, rowIndex);

		this.createDivider(gp, mainGapColumn, rowIndex, 0);
		this.createDivider(gp, inoutGapColumn, rowIndex, 1);
		for (int col : minorColumnGaps)
			this.createDivider(gp, col, rowIndex, 2);
		this.createDivider(gp, buildingGapColumn, rowIndex, 1);
		return rowIndex;
	}

	protected void addTitles(GridPane gp) {
		nameLabel = new Label("Item Name");
		nameLabel.setFont(Font.font(nameLabel.getFont().getFamily(), FontWeight.BOLD, 16));
		gp.add(nameLabel, nameColumn, titlesRow);
		consumptionLabel = new Label("Consuming");
		consumptionLabel.setFont(Font.font(consumptionLabel.getFont().getFamily(), FontWeight.BOLD, 16));
		gp.add(consumptionLabel, ingredientsStartColumn, titlesRow);
		productionLabel = new Label("Producing");
		productionLabel.setFont(Font.font(productionLabel.getFont().getFamily(), FontWeight.BOLD, 16));
		gp.add(productionLabel, productsStartColumn, titlesRow);
		buildingLabel = new Label("In");
		buildingLabel.setFont(Font.font(productionLabel.getFont().getFamily(), FontWeight.BOLD, 16));
		gp.add(buildingLabel, buildingColumn, titlesRow);
		gp.setColumnSpan(consumptionLabel, productsStartColumn-ingredientsStartColumn);
		gp.setColumnSpan(productionLabel, buildingGapColumn-productsStartColumn);
		gp.setColumnSpan(buildingLabel, 1);
	}

	protected final int addRow(GridPane gp) {
		RowConstraints cc = new RowConstraints();
		cc.setPrefHeight(Region.USE_COMPUTED_SIZE);
		cc.setMaxHeight(Region.USE_COMPUTED_SIZE);
		cc.setMinHeight(Region.USE_COMPUTED_SIZE);
		gp.getRowConstraints().add(cc);
		return gp.getRowConstraints().size()-1;
	}

	protected final int addColumn(GridPane gp) {
		ColumnConstraints cs = new ColumnConstraints();
		gp.getColumnConstraints().add(cs);
		cs.setPrefWidth(Region.USE_COMPUTED_SIZE);
		cs.setMaxWidth(Region.USE_COMPUTED_SIZE);
		cs.setMinWidth(Region.USE_COMPUTED_SIZE);
		return gp.getColumnConstraints().size()-1;
	}

	protected final void createRowDivider(GridPane gp, int row, int tier) {
		String c = "000000ff";
		int w = 8;
		switch(tier) {
			case 0:
				c = "7f7f7fff";
				break;
			case 1:
				c = "b2b2b2ff";
				w = 4;
				break;
			case 2:
				c = "d9d9d9ff";
				w = 1;
				break;
		}
		ObservableList<ColumnConstraints> li = gp.getColumnConstraints();
		for (int i = 0; i < li.size(); i++) {/*
			Rectangle rect = new Rectangle();
			rect.setFill(c);
			li.get(i).setFillWidth(true);
			//rect.widthProperty().bind(li.get(i).maxWidthProperty().subtract(2));
			//rect.widthProperty().bind(gp.widthProperty().subtract(4+8+4+minorColumnGaps.size()).divide(li.size()-minorColumnGaps.size()+2));
			rect.setHeight(w);
			//rect.setWidth(gp.getCellBounds(i, row).getWidth());
			gp.add(rect, i, row);/*
			AnchorPane ap = new AnchorPane();
			ap.getChildren().add(rect);
			ap.setBottomAnchor(rect, 0D);
			ap.setTopAnchor(rect, 0D);
			ap.setLeftAnchor(rect, 0D);
			ap.setRightAnchor(rect, 0D);
			gp.add(ap, i, row);*/
			HBox hb = new HBox();
			hb.setMaxWidth(Double.POSITIVE_INFINITY);
			hb.setMaxHeight(w);
			//hb.setMinWidth(1);
			hb.setMinHeight(w);
			hb.setStyle("-fx-background-color: #"+c+";");
			gp.add(hb, i, row);
		}
	}

	protected final void createDivider(GridPane gp, int col, int row, int tier) {
		Rectangle rect = new Rectangle();
		Color c = Color.BLACK;
		int w = 8;
		switch(tier) {
			case 0:
				c = Color.gray(0.5);
				break;
			case 1:
				c = Color.gray(0.7);
				w = 4;
				break;
			case 2:
				c = Color.gray(0.85);
				w = 1;
				break;
		}
		rect.setFill(c);
		//rect.widthProperty().bind(gp.getColumnConstraints().get(split).maxWidthProperty().subtract(2));
		//rect.heightProperty().bind(gp.getRowConstraints().get(i).maxHeightProperty().subtract(2));
		rect.setWidth(w);
		rect.setHeight(32);
		gp.add(rect, col, row);
	}
	/*
	private Node createItem(Consumable c, int amt) {
		HBox hb = new HBox();
		hb.setAlignment(Pos.CENTER_LEFT);
		ImageView img = new ImageView(new Image(c.getIcon(), 32, 32, true, true));
		img.setFitHeight(32);
		img.setFitWidth(32);
		img.setSmooth(true);
		img.setCache(true);
		hb.setSpacing(0);
		hb.getChildren().add(img);
		Label lb = new Label(amt+"/min");
		lb.setFont(Font.font(lb.getFont().getFamily(), 14));
		hb.getChildren().add(lb);
		GuiUtil.setTooltip(img, c.name);
		return hb;
	}*/
	/*
	protected final int getColumnWidth(GridPane gp, int col) {
		if (col == deleteColumn)
			return 32;
		else if (col == mainGapColumn)
			return 8;
		else if (col == inoutGapColumn)
			return 4;
		else if (minorColumnGaps.contains(col))
			return 1;
		else
			return -1;
	}
	 */
}
