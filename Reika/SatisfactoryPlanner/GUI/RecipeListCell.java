package Reika.SatisfactoryPlanner.GUI;

import java.util.HashMap;
import java.util.Map.Entry;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Data.FunctionalBuilding;
import Reika.SatisfactoryPlanner.Data.Recipe;

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
		graphicBar.getChildren().add(buildIODisplay(r, false, false));
		Rectangle rect = new Rectangle();
		rect.setFill(UIConstants.FADE_COLOR);
		rect.setWidth(4);
		rect.setHeight(32);
		graphicBar.getChildren().add(rect);
		graphicBar.getChildren().add(buildingBar);
		graphicBar.setSpacing(24);
		return graphicBar;
	}

	public static Node buildIODisplay(Recipe r, boolean compact, boolean rates) {
		HBox ingredients = new HBox();
		HBox products = new HBox();
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
			ingredients.prefWidthProperty().bind(ingredients.spacingProperty().multiply(Recipe.getMaxIngredients()-1).add(Recipe.getMaxIngredients()*32));
			products.prefWidthProperty().bind(products.spacingProperty().multiply(Recipe.getMaxProducts()-1).add(Recipe.getMaxProducts()*32));
		}
		ingredients.setAlignment(Pos.CENTER_RIGHT);
		products.setAlignment(Pos.CENTER_LEFT);

		for (Entry<Consumable, Float> e : r.getIngredientsPerMinute().entrySet()) {
			if (rates)
				ingredients.getChildren().add(new ItemRateController(e.getKey(), e.getValue()).getRootNode());
			else
				ingredients.getChildren().add(new ImageView(e.getKey().createIcon()));
		}
		for (Entry<Consumable, Float> e : r.getProductsPerMinute().entrySet()) {
			if (rates)
				products.getChildren().add(new ItemRateController(e.getKey(), e.getValue()).getRootNode());
			else
				products.getChildren().add(new ImageView(e.getKey().createIcon()));
		}
		HBox itemBar = new HBox();
		itemBar.getChildren().add(ingredients);
		ImageView img = new ImageView(new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/arrow-right-small.png")));
		itemBar.getChildren().add(img);
		HBox.setMargin(img, new Insets(0, 4, 0, 4));
		itemBar.getChildren().add(products);
		return itemBar;
	}

	public static void init() {/*
		RecipeListCell temp = new RecipeListCell("temp", false);
		cachedDecorations.clear();
		for (Recipe r : Database.getAllAutoRecipes())
			temp.updateItem(r, false);*/
	}

}