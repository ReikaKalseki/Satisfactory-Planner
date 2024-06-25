package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.Recipe;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.GuiInstance;
import Reika.SatisfactoryPlanner.Util.CountMap;
import Reika.SatisfactoryPlanner.Util.MultiMap;

import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public class ScaledRecipeMatrix extends RecipeMatrixBase {

	private final RecipeMatrix parent;

	private final CountMap<Recipe> scales = new CountMap();
	private final MultiMap<Recipe, GuiInstance> recipeEntries = new MultiMap();

	protected int countGapColumn;
	protected int countColumn;

	protected int sumGapRow;
	protected int sumsRow;

	public ScaledRecipeMatrix(RecipeMatrix r) {
		parent = r;
	}

	@Override
	public GridPane createGrid(ControllerBase con) throws IOException {
		GridPane gp = new GridPane();
		//HashSet<Consumable> in = new HashSet();
		//HashSet<Consumable> out = new HashSet();
		List<Recipe> recipes = this.getRecipes();
		ArrayList<Consumable> in = new ArrayList(this.getAllIngredients());
		ArrayList<Consumable> out = new ArrayList(this.getAllProducts());
		titlesRow = this.addRow(gp);
		titleGapRow = this.addRow(gp);
		minorRowGaps.clear();
		for (int i = 0; i < recipes.size(); i++) {
			//in.addAll(r.getCost().keySet());
			//out.addAll(r.getProducts().keySet());
			/*
			for (Entry<Consumable, Integer> e : r.getCost().entrySet()) {
				Consumable c = e.getKey();
				if (!in.contains(c))
					in.add(c);
			}
			for (Entry<Consumable, Integer> e : r.getProducts().entrySet()) {
				Consumable c = e.getKey();
				if (!out.contains(c))
					out.add(c);
			}*/

			this.addRow(gp);
			if (i < recipes.size()-1)
				minorRowGaps.add(this.addRow(gp)); //separator
		}
		nameColumn = this.addColumn(gp); //name
		mainGapColumn = this.addColumn(gp); //separator
		minorColumnGaps.clear();
		for (int i = 0; i < in.size(); i++) {
			this.addColumn(gp);
			if (i < in.size()-1)
				minorColumnGaps.add(this.addColumn(gp)); //separator
		}
		if (in.isEmpty())
			this.addColumn(gp); //space for "consuming" title
		if (in.size() <= 1)
			gp.getColumnConstraints().get(gp.getColumnCount()-1).setMinWidth(96);
		inoutGapColumn = this.addColumn(gp); //separator
		for (int i = 0; i < out.size(); i++) {
			this.addColumn(gp);
			if (i < out.size()-1)
				minorColumnGaps.add(this.addColumn(gp)); //separator
		}
		if (in.isEmpty())
			this.addColumn(gp); //space for "producing" title
		if (out.size() <= 1)
			gp.getColumnConstraints().get(gp.getColumnCount()-1).setMinWidth(96);
		countGapColumn = this.addColumn(gp);
		countColumn = this.addColumn(gp);
		ingredientsStartColumn = /*2*/mainGapColumn+1;
		productsStartColumn = /*2+in.size()+1*/inoutGapColumn+1;
		Collections.sort(in);
		Collections.sort(out);
		for (int i = 0; i < recipes.size(); i++) {
			Recipe r = recipes.get(i);
			Label lb = new Label(r.name);
			lb.setFont(Font.font(lb.getFont().getFamily(), FontWeight.BOLD, FontPosture.REGULAR, 14));
			int rowIndex = titleGapRow+1+i*2;
			gp.add(lb, nameColumn, rowIndex);
			for (Entry<Consumable, Integer> e : r.getCost().entrySet()) {
				Consumable c = e.getKey();
				GuiInstance gui = con.loadNestedFXML("ItemView", gp, ingredientsStartColumn+in.indexOf(c)*2, rowIndex);
				((ItemViewController)gui.controller).setItem(c, e.getValue());
				//gp.add(this.createItem(c, e.getValue()), 1+in.indexOf(c), i);
			}
			for (Entry<Consumable, Integer> e : r.getProducts().entrySet()) {
				Consumable c = e.getKey();
				GuiInstance gui = con.loadNestedFXML("ItemView", gp, /*2+in.size()+1+out.indexOf(c)*/productsStartColumn+out.indexOf(c)*2, rowIndex);
				((ItemViewController)gui.controller).setItem(c, e.getValue());
				//gp.add(this.createItem(c, e.getValue()), 1+in.size()+out.indexOf(c), i);
			}
			Spinner<Integer> counter = new Spinner();
			counter.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9999, 0));
			counter.setEditable(true);
			counter.setPrefWidth(72);
			counter.setMinWidth(Region.USE_PREF_SIZE);
			counter.setMaxWidth(Region.USE_PREF_SIZE);
			counter.setPrefHeight(32);
			counter.setMinHeight(Region.USE_PREF_SIZE);
			counter.setMaxHeight(Region.USE_PREF_SIZE);
			counter.valueProperty().addListener((val, old, nnew) -> {
				if (nnew != null)
					this.setScale(r, nnew);
			});
			TextField txt = counter.getEditor();
			txt.textProperty().addListener((val, old, nnew) -> {
				if (nnew.length() > 4)
					txt.setText(nnew.substring(0, 4));
				nnew = nnew.replaceAll("[^\\d.]", "");
				txt.setText(nnew);
			});
			gp.add(counter, countColumn, rowIndex);
			/*
			Rectangle rect = new Rectangle();
			rect.setFill(Color.gray(0.7));
			int split = 2+in.size();
			//rect.widthProperty().bind(gp.getColumnConstraints().get(split).maxWidthProperty().subtract(2));
			//rect.heightProperty().bind(gp.getRowConstraints().get(i).maxHeightProperty().subtract(2));
			rect.setWidth(4);
			rect.setHeight(32);
			gp.add(rect, split, i);*/


			/*
			rect = new Rectangle();
			rect.setFill(Color.gray(0.5));
			//rect.widthProperty().bind(gp.getColumnConstraints().get(split).maxWidthProperty().subtract(2));
			//rect.heightProperty().bind(gp.getRowConstraints().get(i).maxHeightProperty().subtract(2));
			rect.setWidth(8);
			rect.setHeight(32);
			gp.add(rect, 1, i);*/
			this.createDivider(gp, mainGapColumn, rowIndex, 0);
			this.createDivider(gp, inoutGapColumn, rowIndex, 1);
			for (int col : minorColumnGaps)
				this.createDivider(gp, col, rowIndex, 2);
			this.createDivider(gp, countGapColumn, rowIndex, 0);
		}
		this.createDivider(gp, mainGapColumn, titlesRow, 0);
		this.createDivider(gp, inoutGapColumn, titlesRow, 1);
		sumGapRow = this.addRow(gp);
		sumsRow = this.addRow(gp);
		this.createRowDivider(gp, titleGapRow, 0);
		this.createRowDivider(gp, sumGapRow, 1);
		for (int row : minorRowGaps)
			this.createRowDivider(gp, row, 2);

		for (int i = 0; i < in.size(); i++) {
			Consumable c = in.get(i);
			GuiInstance gui = con.loadNestedFXML("ItemView", gp, ingredientsStartColumn+in.indexOf(c)*2, sumsRow);
			((ItemViewController)gui.controller).setItem(c, this.getTotalConsumption(c));
		}
		for (int i = 0; i < out.size(); i++) {
			Consumable c = out.get(i);
			GuiInstance gui = con.loadNestedFXML("ItemView", gp, productsStartColumn+out.indexOf(c)*2, sumsRow);
			((ItemViewController)gui.controller).setItem(c, this.getTotalProduction(c));
		}

		//GuiInstance gui = con.loadNestedFXML("AddRecipeRow", gp, /*2+in.indexOf(c)*/0, addNewRow);
		//((AddRecipeToGridController)gui.controller).setGrid(this);
		//gp.setColumnSpan(gui.rootNode, GridPane.REMAINING);
		ArrayList<Recipe> li = new ArrayList(Database.getAllRecipes());
		li.removeAll(recipes);

		Label nm = new Label("Item Name");
		nm.setFont(Font.font(nm.getFont().getFamily(), FontWeight.BOLD, 16));
		gp.add(nm, nameColumn, titlesRow);
		Label cons = new Label("Consuming");
		cons.setFont(Font.font(cons.getFont().getFamily(), FontWeight.BOLD, 16));
		gp.add(cons, ingredientsStartColumn, titlesRow);
		Label prod = new Label("Producing");
		prod.setFont(Font.font(prod.getFont().getFamily(), FontWeight.BOLD, 16));
		gp.add(prod, productsStartColumn, titlesRow);
		gp.setColumnSpan(cons, productsStartColumn-ingredientsStartColumn);
		gp.setColumnSpan(prod, GridPane.REMAINING);

		gp.getColumnConstraints().get(0).setMinWidth(32);

		gp.setHgap(4);
		gp.setVgap(4);
		//gp.setGridLinesVisible(true);
		return gp;
	}

	private void setScale(Recipe r, int amt) {
		scales.set(r, amt);
	}

	@Override
	public List<Recipe> getRecipes() {
		return parent.getRecipes();
	}

}
