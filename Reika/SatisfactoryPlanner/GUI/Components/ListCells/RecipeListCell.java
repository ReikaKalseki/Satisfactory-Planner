package Reika.SatisfactoryPlanner.GUI.Components.ListCells;

import java.util.HashMap;
import java.util.Map.Entry;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Data.Objects.Recipe;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.FunctionalBuilding;
import Reika.SatisfactoryPlanner.GUI.UIConstants;
import Reika.SatisfactoryPlanner.GUI.Components.ItemRateController;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

public class RecipeListCell extends DecoratedListCell<Recipe> {

	private static final HashMap<Recipe, Node> cachedDecorations = new HashMap();
	//private Node graphic;

	public RecipeListCell(String ptext, boolean isButton) {
		super(ptext, isButton);
		//Logging.instance.log("Creating recipe list cell "+ptext);
	}

	@Override
	protected String getString(Recipe obj) {
		return obj.displayName+" (T"+obj.getTier()+")";
	}

	@Override
	protected void onCreateCellContent(Recipe obj, Node graphic) {
		cachedDecorations.put(obj, graphic);
		//this.graphic = graphic;
	}

	@Override
	protected Node getCachedCellContent(Recipe r) {
		return cachedDecorations.get(r);
		//return graphic;
	}

	@Override
	protected Node createDecoration(Recipe r) {

		HBox buildingBar = new HBox();
		buildingBar.setAlignment(Pos.CENTER_LEFT);
		buildingBar.setSpacing(12);
		FunctionalBuilding b = r.productionBuilding;
		buildingBar.getChildren().add(new ImageView(b.createIcon()));
		buildingBar.getChildren().add(new Label(b.displayName));
		buildingBar.setPrefWidth(160);
		buildingBar.setMinWidth(Region.USE_PREF_SIZE);
		buildingBar.setMaxWidth(Region.USE_PREF_SIZE);
		HBox graphicBar = new HBox();
		graphicBar.getChildren().add(buildIODisplay(r, false, -1));
		Rectangle rect = new Rectangle();
		rect.setFill(UIConstants.FADE_COLOR);
		rect.setWidth(4);
		rect.setHeight(32);
		graphicBar.getChildren().add(rect);
		graphicBar.getChildren().add(buildingBar);
		graphicBar.setSpacing(24);
		return graphicBar;
	}

	public static HBox buildIODisplay(Recipe r, boolean compact, double rateScale) {
		return buildIODisplay(r, compact, rateScale, null);
	}

	public static HBox buildIODisplay(Recipe r, boolean compact, double rateScale, Consumable keyItem) {
		HBox ingredients = new HBox();
		HBox products = new HBox();
		boolean rates = rateScale >= 0;
		ingredients.setSpacing(8);
		products.setSpacing(8);
		if (compact) {
			ingredients.setMinWidth(Region.USE_COMPUTED_SIZE);
			ingredients.setMaxWidth(Region.USE_COMPUTED_SIZE);
			products.setMinWidth(Region.USE_COMPUTED_SIZE);
			products.setMaxWidth(Region.USE_COMPUTED_SIZE);
			ingredients.setPrefWidth(Region.USE_COMPUTED_SIZE);
			products.setPrefWidth(Region.USE_COMPUTED_SIZE);
		}
		else {
			ingredients.setMinWidth(Region.USE_PREF_SIZE);
			ingredients.setMaxWidth(Region.USE_PREF_SIZE);
			products.setMinWidth(Region.USE_PREF_SIZE);
			products.setMaxWidth(Region.USE_PREF_SIZE);
			ingredients.prefWidthProperty().bind(ingredients.spacingProperty().multiply(Recipe.getMaxIngredients()-1).add(Recipe.getMaxIngredients()*(rates ? 40 : 32)));
			products.prefWidthProperty().bind(products.spacingProperty().multiply(Recipe.getMaxProducts()-1).add(Recipe.getMaxProducts()*(rates ? 40 : 32)));
		}
		ingredients.setAlignment(Pos.CENTER_RIGHT);
		products.setAlignment(Pos.CENTER_LEFT);

		for (Entry<Consumable, Double> e : r.getIngredientsPerMinute().entrySet()) {
			if (keyItem != null && e.getKey() == keyItem)
				continue;
			ingredients.getChildren().add(createItemNode(e, rateScale, rates));
		}
		for (Entry<Consumable, Double> e : r.getProductsPerMinute().entrySet()) {
			if (keyItem != null && e.getKey() == keyItem)
				continue;
			products.getChildren().add(createItemNode(e, rateScale, rates));
		}
		if (keyItem != null) {
			if (r.getIngredientsPerMinute().containsKey(keyItem))
				ingredients.getChildren().add(createItemNode(keyItem, r.getIngredientsPerMinute().get(keyItem), rateScale, rates));
			if (r.getProductsPerMinute().containsKey(keyItem))
				products.getChildren().add(0, createItemNode(keyItem, r.getProductsPerMinute().get(keyItem), rateScale, rates));
		}
		HBox itemBar = new HBox();
		itemBar.setAlignment(Pos.CENTER);
		itemBar.getChildren().add(ingredients);
		ImageView img = new ImageView(new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/arrow-right-small.png")));
		itemBar.getChildren().add(img);
		HBox.setMargin(img, new Insets(0, 4, 0, 4));
		itemBar.getChildren().add(products);
		return itemBar;
	}

	private static Node createItemNode(Entry<Consumable, Double> e, double rateScale, boolean rates) {
		return createItemNode(e.getKey(), e.getValue(), rateScale, rates);
	}

	private static Node createItemNode(Consumable c, double amt, double rateScale, boolean rates) {
		if (rates)
			return new ItemRateController(c, amt*rateScale, false).setMinWidth("000.00").getRootNode();
		else
			return new ImageView(c.createIcon());
	}

	public static void init() {/*
		RecipeListCell temp = new RecipeListCell("temp", false);
		cachedDecorations.clear();
		for (Recipe r : Database.getAllAutoRecipes())
			temp.updateItem(r, false);*/
	}

}