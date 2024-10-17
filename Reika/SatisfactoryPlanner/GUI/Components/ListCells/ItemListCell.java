package Reika.SatisfactoryPlanner.GUI.Components.ListCells;

import Reika.SatisfactoryPlanner.Data.Objects.Consumable;

import javafx.scene.Node;

public class ItemListCell<C extends Consumable> extends DecoratedListCell<C> {

	public ItemListCell(String ptext, boolean isButton) {
		super(ptext, isButton);
		//Logging.instance.log("Creating item list cell "+ptext);
	}

	@Override
	protected String getString(Consumable obj) {
		return obj.displayName;
	}

	@Override
	protected Node createDecoration(Consumable obj) {
		return obj.createImageView();
	}

}