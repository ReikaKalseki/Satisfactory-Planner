package Reika.SatisfactoryPlanner.GUI.Components;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Data.Factory;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Data.Objects.Item;
import Reika.SatisfactoryPlanner.GUI.GuiUtil;

import fxexpansions.SizedControllerBase;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;


public class ItemOutputController extends SizedControllerBase {

	private final VBox root = new VBox();

	public final Consumable item;

	private CheckBox sinkSwitch;

	private final ProductButton button;

	private final Factory factory;

	public ItemOutputController(Consumable c, Factory f) {
		item = c;
		factory = f;

		button = new ProductButton();

		root.setSpacing(4);
		root.setAlignment(Pos.TOP_CENTER);
		root.getChildren().add(button);
		if (item instanceof Item && ((Item)item).isSinkable()) {
			sinkSwitch = new CheckBox("Sink");
			sinkSwitch.setAlignment(Pos.TOP_CENTER);
			sinkSwitch.setMaxWidth(Double.MAX_VALUE);
			sinkSwitch.setSelected(f.isProductSinking(c));
			sinkSwitch.selectedProperty().addListener((val, old, nnew) -> f.setProductSinking(item, nnew));
			root.getChildren().add(sinkSwitch);
		}

		root.getProperties().put("outputitem", Boolean.TRUE);
	}

	@Override
	public Parent getRootNode() {
		return root;
	}

	@Override
	public double getWidth() {
		return 40;
	}

	@Override
	public double getHeight() {
		return 40+sinkSwitch.getHeight()+root.getSpacing();
	}

	private class ProductButton extends Button {

		private ProductButton() {
			int size = 64;//32;
			Pane p = new Pane();
			ImageView ico = new ImageView(item.createIcon(size));
			p.getChildren().add(ico);
			GuiUtil.setTooltip(this, item.displayName);
			this.setPrefWidth(size);
			this.setPrefHeight(size);
			this.setMinHeight(Region.USE_PREF_SIZE);
			this.setMaxHeight(Region.USE_PREF_SIZE);
			this.setMinWidth(Region.USE_PREF_SIZE);
			this.setMaxWidth(Region.USE_PREF_SIZE);
			this.setOnAction(e -> {
				factory.removeProduct(item);
			});
			p.setPrefWidth(size);
			p.setPrefHeight(size);
			p.setMinHeight(Region.USE_PREF_SIZE);
			p.setMaxHeight(Region.USE_PREF_SIZE);
			p.setMinWidth(Region.USE_PREF_SIZE);
			p.setMaxWidth(Region.USE_PREF_SIZE);
			ImageView img = new ImageView(new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/delete.png"), 16, 16, true, true));
			img.layoutXProperty().bind(p.widthProperty().subtract(img.getImage().getWidth()));
			img.layoutYProperty().bind(p.heightProperty().subtract(img.getImage().getHeight()));
			p.getChildren().add(img);
			this.setGraphic(p);
		}


	}

}
