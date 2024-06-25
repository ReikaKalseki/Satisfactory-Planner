package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.Recipe;
import Reika.SatisfactoryPlanner.GUI.GuiSystem.GuiInstance;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;

public class RecipeMatrix extends RecipeMatrixBase {

	private final ArrayList<Recipe> recipes = new ArrayList();
	//private final MultiMap<Consumable, Recipe> allIngredients = new MultiMap().setNullEmpty();
	//private final MultiMap<Consumable, Recipe> allProducts = new MultiMap().setNullEmpty();

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
		recipes.add(r);/*
		for (Entry<Consumable, Integer> e : r.getCost().entrySet()) {
			Consumable c = e.getKey();
			Collection<Recipe> li = allIngredients.get(c);
			if (li != null)
				li.add(r);
		}
		for (Entry<Consumable, Integer> e : r.getProducts().entrySet()) {
			Consumable c = e.getKey();
			Collection<Recipe> li = allProducts.get(c);
			if (li != null)
				li.add(r);
		}*/
		Collections.sort(recipes);
		if (changeCallback != null)
			changeCallback.run();
	}

	public void removeRecipe(Recipe r) {
		recipes.remove(r);/*
		for (Entry<Consumable, Integer> e : r.getCost().entrySet()) {
			Consumable c = e.getKey();
			Collection<Recipe> li = allIngredients.get(c);
			if (li != null) {
				li.remove(r);
				if (li.isEmpty())
					allIngredients.remove(c);
			}
		}
		for (Entry<Consumable, Integer> e : r.getProducts().entrySet()) {
			Consumable c = e.getKey();
			Collection<Recipe> li = allProducts.get(c);
			if (li != null) {
				li.remove(r);
				if (li.isEmpty())
					allProducts.remove(c);
			}
		}*/
		if (changeCallback != null)
			changeCallback.run();
	}

	@Override
	public GridPane createGrid(ControllerBase con) throws IOException {
		GridPane gp = new GridPane();
		//HashSet<Consumable> in = new HashSet();
		//HashSet<Consumable> out = new HashSet();
		ArrayList<Consumable> in = new ArrayList(this.getAllIngredients());
		ArrayList<Consumable> out = new ArrayList(this.getAllProducts());
		addNewRow = this.addRow(gp);
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
		deleteColumn = this.addColumn(gp); //delete
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
		ingredientsStartColumn = /*2*/mainGapColumn+1;
		productsStartColumn = /*2+in.size()+1*/inoutGapColumn+1;
		Collections.sort(in);
		Collections.sort(out);
		for (int i = 0; i < recipes.size(); i++) {
			Recipe r = recipes.get(i);
			Label lb = new Label(r.name);
			lb.setFont(Font.font(lb.getFont().getFamily(), FontWeight.BOLD, FontPosture.REGULAR, 14));
			int rowIndex = titleGapRow+1+i*2;
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
		}
		this.createDivider(gp, mainGapColumn, titlesRow, 0);
		this.createDivider(gp, inoutGapColumn, titlesRow, 1);
		this.createRowDivider(gp, titleGapRow, 0);
		for (int row : minorRowGaps)
			this.createRowDivider(gp, row, 2);

		//GuiInstance gui = con.loadNestedFXML("AddRecipeRow", gp, /*2+in.indexOf(c)*/0, addNewRow);
		//((AddRecipeToGridController)gui.controller).setGrid(this);
		//gp.setColumnSpan(gui.rootNode, GridPane.REMAINING);
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

	@Override
	public List<Recipe> getRecipes() {
		return Collections.unmodifiableList(recipes);
	}

}
