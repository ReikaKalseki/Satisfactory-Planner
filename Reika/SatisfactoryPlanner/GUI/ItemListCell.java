package Reika.SatisfactoryPlanner.GUI;

import Reika.SatisfactoryPlanner.Data.Consumable;

import javafx.scene.Node;

class ItemListCell<C extends Consumable> extends DecoratedListCell<C> {

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