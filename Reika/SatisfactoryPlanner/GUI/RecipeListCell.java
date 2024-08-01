package Reika.SatisfactoryPlanner.GUI;

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

class RecipeListCell extends DecoratedListCell<Recipe> {

	public RecipeListCell(String ptext, boolean isButton) {
		super(ptext, isButton);
	}

	@Override
	protected String getString(Recipe obj) {
		return obj.displayName+" (T"+obj.getTier()+")";
	}

	@Override
	protected Node createDecoration(Recipe r) {
		HBox ingredients = new HBox();
		HBox products = new HBox();
		ingredients.setSpacing(8);
		products.setSpacing(8);
		ingredients.setMinWidth(Region.USE_PREF_SIZE);
		ingredients.setMaxWidth(Region.USE_PREF_SIZE);
		products.setMinWidth(Region.USE_PREF_SIZE);
		products.setMaxWidth(Region.USE_PREF_SIZE);
		ingredients.setAlignment(Pos.CENTER_RIGHT);
		products.setAlignment(Pos.CENTER_LEFT);
		ingredients.prefWidthProperty().bind(ingredients.spacingProperty().multiply(Recipe.getMaxIngredients()-1).add(Recipe.getMaxIngredients()*32));
		products.prefWidthProperty().bind(products.spacingProperty().multiply(Recipe.getMaxProducts()-1).add(Recipe.getMaxProducts()*32));

		for (Consumable c : r.getIngredientsPerMinute().keySet())
			ingredients.getChildren().add(new ImageView(c.createIcon()));
		for (Consumable c : r.getProductsPerMinute().keySet())
			products.getChildren().add(new ImageView(c.createIcon()));
		HBox itemBar = new HBox();
		itemBar.getChildren().add(ingredients);
		ImageView img = new ImageView(new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/arrow-right-small.png")));
		itemBar.getChildren().add(img);
		HBox.setMargin(img, new Insets(0, 4, 0, 4));
		itemBar.getChildren().add(products);
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
		graphicBar.getChildren().add(itemBar);
		Rectangle rect = new Rectangle();
		rect.setFill(UIConstants.FADE_COLOR);
		rect.setWidth(4);
		rect.setHeight(32);
		graphicBar.getChildren().add(rect);
		graphicBar.getChildren().add(buildingBar);
		graphicBar.setSpacing(24);
		return graphicBar;

	}

}