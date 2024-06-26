package Reika.SatisfactoryPlanner.GUI;

import java.io.IOException;

import Reika.SatisfactoryPlanner.Data.Consumable;
import Reika.SatisfactoryPlanner.Util.GuiUtil;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;

public class ItemViewController extends ControllerBase {

	@FXML
	private Label amount;

	@FXML
	private VBox countContainer;

	@FXML
	private Line divider;

	@FXML
	private ImageView icon;

	private int baseAmount;

	@Override
	public void init(HostServices services) throws IOException {
		divider.endXProperty().bind(countContainer.widthProperty().subtract(2));
	}

	public void setItem(Consumable c, int amt) {
		baseAmount = amt;
		icon.setImage(new Image(c.getIcon(), 32, 32, true, true));
		amount.setText(String.valueOf(amt));
		GuiUtil.setTooltip(icon, c.name);
	}

	public void setScale(int scale) {
		amount.setText(String.valueOf(baseAmount*scale));
	}

	@Override
	protected void postInit(WindowBase w) throws IOException {
		super.postInit(w);

	}

}

