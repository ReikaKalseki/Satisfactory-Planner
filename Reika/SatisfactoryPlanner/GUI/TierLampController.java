package Reika.SatisfactoryPlanner.GUI;

import fxexpansions.SizedControllerBase;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;


public class TierLampController extends SizedControllerBase {

	public final int tier;

	private final StackPane root = new StackPane();
	private final Label tierLabel = new Label();

	private boolean isLit;

	public TierLampController(int tier) {
		this.tier = tier;

		root.setMinHeight(32);
		root.setMaxHeight(32);
		root.setMinWidth(32);
		root.setMaxWidth(32);
		root.setAlignment(Pos.CENTER);/*
		tierLabel.setFitHeight(32);
		tierLabel.setFitWidth(32);
		tierLabel.setSmooth(true);
		tierLabel.setPreserveRatio(true);
		tierLabel.setImage(new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/tier_"+tier+"0.png")));*/
		tierLabel.setText(String.valueOf(tier));
		root.getChildren().add(tierLabel);
		isLit = true;
		this.setState(false);
	}

	public void setState(boolean lit) {
		if (lit == isLit)
			return;
		isLit = lit;
		//tierLabel.setImage(new Image(Main.class.getResourceAsStream("Resources/Graphics/Icons/tier_"+tier+"_"+(lit ? "1" : "0")+".png")));
		if (lit) {
			root.setStyle("-fx-background-color: #E69344;");
			tierLabel.setStyle("-fx-font-fill: #fff; -fx-font-weight: bold;");
		}
		else {
			root.setStyle("-fx-background-color: #7D7D7D;");
			tierLabel.setStyle("-fx-text-fill: #fff; -fx-font-weight: 500;");
		}

	}

	@Override
	public Parent getRootNode() {
		return root;
	}

	@Override
	public double getWidth() {
		return 32;
	}

	@Override
	public double getHeight() {
		return 32;
	}

	@Override
	public String toString() {
		return "Tier "+tier;
	}

}
