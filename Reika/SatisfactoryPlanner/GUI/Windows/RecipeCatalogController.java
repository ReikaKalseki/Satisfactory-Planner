package Reika.SatisfactoryPlanner.GUI.Windows;

import java.io.IOException;
import java.util.TreeMap;

import org.controlsfx.control.SearchableComboBox;

import Reika.SatisfactoryPlanner.InternalIcons;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Data.Objects.Recipe;
import Reika.SatisfactoryPlanner.GUI.GuiSystem;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;
import Reika.SatisfactoryPlanner.GUI.GuiUtil.SearchableSelector;
import Reika.SatisfactoryPlanner.GUI.Components.ListCells.DecoratedListCell;
import Reika.SatisfactoryPlanner.GUI.Components.ListCells.ItemListCell;
import Reika.SatisfactoryPlanner.GUI.Components.ListCells.RecipeListCell;

import fxexpansions.FXMLControllerBase;
import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class RecipeCatalogController extends FXMLControllerBase {

	@FXML
	private Button addButton;

	@FXML
	private GridPane generalRecipeList;

	@FXML
	private SearchableComboBox<Consumable> itemDropdown;

	@FXML
	private GridPane makingRecipeList;

	@FXML
	private SearchableComboBox<Recipe> recipeDropdown;

	@FXML
	private GridPane usingRecipeList;

	@FXML
	private ScrollPane makingScroller;

	@FXML
	private ScrollPane usingScroller;

	@FXML
	private ScrollPane recipeScroller;

	private final TreeMap<Recipe, RecipeRow> makingNodes = new TreeMap();
	private final TreeMap<Recipe, RecipeRow> usingNodes = new TreeMap();
	private final TreeMap<Recipe, RecipeRow> recipeNodes = new TreeMap();

	@Override
	public void init(HostServices services) throws IOException {
		GuiUtil.setButtonEvent(addButton, () -> {
			Factory f = GuiSystem.getMainGUI().controller.getFactory();
			f.addRecipes(recipeNodes.keySet());
		});

		itemDropdown.setButtonCell(new ItemListCell("Choose Item...", true));
		itemDropdown.setCellFactory(c -> new ItemListCell("", false));
		itemDropdown.setItems(FXCollections.observableArrayList(Database.getAllItems()));
		itemDropdown.getSelectionModel().selectedItemProperty().addListener((val, old, nnew) -> {
			this.clear(makingRecipeList, makingNodes);
			this.clear(usingRecipeList, usingNodes);
			if (nnew != null) {
				for (Recipe r : Recipe.getAllRecipesMaking(nnew)) {
					makingNodes.put(r, this.createCellContent(makingRecipeList, r, this.getAddRecipeButton(r), nnew));
				}
				for (Recipe r : Recipe.getAllRecipesUsing(nnew)) {
					usingNodes.put(r, this.createCellContent(usingRecipeList, r, this.getAddRecipeButton(r), nnew));
				}
			}
			((Region)makingScroller.lookup("ScrollPane .viewport")).setCache(false);
			((Region)usingScroller.lookup("ScrollPane .viewport")).setCache(false);
			((Region)recipeScroller.lookup("ScrollPane .viewport")).setCache(false);
		});

		recipeDropdown.setItems(FXCollections.observableArrayList(Database.getAllAutoRecipes()));
		GuiUtil.setupAddSelector(recipeDropdown, new SearchableSelector<Recipe>(){
			@Override
			public void accept(Recipe t) {
				RecipeCatalogController.this.addRecipe(t);
				RecipeCatalogController.this.getWindow().sizeToScene();
			}

			@Override
			public DecoratedListCell<Recipe> createListCell(String text, boolean button) {
				return new RecipeListCell(text, button);
			}

			@Override
			public String getEntryTypeName() {
				return "Recipe";
			}

			@Override
			public String getActionName() {
				return "Add";
			}

			@Override
			public boolean clearOnSelect() {
				return true;
			}
		});
	}

	private void clear(GridPane gp, TreeMap<Recipe, RecipeRow> map) {
		gp.getChildren().clear();
		gp.getRowConstraints().clear();
		map.clear();
	}

	@Override
	protected void postInit(Stage w) throws IOException {
		super.postInit(w);

		GuiUtil.initWidgets(this);
	}

	private RecipeRow createCellContent(GridPane gp, Recipe item, Button b, Consumable ref) {
		if (item == null)
			return null;
		int row = gp.getRowCount();
		RowConstraints rc = new RowConstraints();
		Label lb = new Label(item.displayName);
		HBox io = RecipeListCell.buildIODisplay(item, false, 1, ref);
		rc.setMinHeight(40);
		gp.getRowConstraints().add(rc);
		gp.add(lb, 0, row);
		lb.setMaxHeight(Double.MAX_VALUE);
		lb.setMaxWidth(Double.MAX_VALUE);
		gp.add(io, 1, row);
		if (b != null) {
			b.setMaxHeight(Double.MAX_VALUE);
			b.setMaxWidth(Double.MAX_VALUE);
			gp.add(b, 2, row);
		}
		if (row%2 == 1) {
			lb.getStyleClass().add("table-row-darken");
			io.getStyleClass().add("table-row-darken");
			if (b != null)
				b.getStyleClass().add("table-row-darken");
		}
		return new RecipeRow(item, row, lb, io, b);
	}

	private Button getAddRecipeButton(Recipe item) {
		Button b = new Button("Add To Recipe List");
		b.setOnAction((e) -> this.addRecipe(item));
		return b;
	}

	private void addRecipe(Recipe item) {
		Button b = new Button();
		b.setGraphic(InternalIcons.DELETE.createImageView());
		b.setMinWidth(40);
		b.setOnAction((e) -> this.removeRecipe(item));
		RecipeRow r = this.createCellContent(generalRecipeList, item, b, null);
		recipeNodes.put(item, r);
		r = makingNodes.get(item);
		if (r != null)
			r.disable();
		r = usingNodes.get(item);
		if (r != null)
			r.disable();
	}

	private void removeRecipe(Recipe item) {
		RecipeRow r = recipeNodes.get(item);
		if (r != null) {
			generalRecipeList.getChildren().remove(r.label);
			generalRecipeList.getChildren().remove(r.recipeView);
			if (r.button != null)
				generalRecipeList.getChildren().remove(r.button);
			for (RecipeRow rr : recipeNodes.values())
				if (rr.rowIndex > r.rowIndex)
					rr.setRow(generalRecipeList, rr.rowIndex-1);
			generalRecipeList.getRowConstraints().remove(r.rowIndex);
		}
		r = makingNodes.get(item);
		if (r != null)
			r.enable();
		r = usingNodes.get(item);
		if (r != null)
			r.enable();
	}

	private void setSize() {
		this.getWindow().sizeToScene();
		this.getWindow().setHeight(Math.min(this.getWindow().getHeight(), Screen.getPrimary().getVisualBounds().getHeight()*0.8));
	}

	private static class RecipeRow {

		private final Recipe recipe;
		private final Label label;
		private final Node recipeView;
		private final Button button;

		private int rowIndex;

		private RecipeRow(Recipe r, int idx, Label lb, Node n, Button b) {
			recipe = r;
			rowIndex = idx;
			label = lb;
			recipeView = n;
			button = b;

			label.setCache(false);
			recipeView.setCache(false);
			if (b != null)
				b.setCache(false);
		}

		public void setRow(GridPane gp, int idx) {
			rowIndex = idx;
			gp.setRowIndex(label, rowIndex);
			gp.setRowIndex(recipeView, rowIndex);
			if (button != null)
				gp.setRowIndex(button, rowIndex);
		}

		public void disable() {
			if (button != null)
				button.setDisable(true);
		}

		public void enable() {
			if (button != null)
				button.setDisable(false);
		}

	}

}

