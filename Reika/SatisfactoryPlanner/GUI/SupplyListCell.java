package Reika.SatisfactoryPlanner.GUI;

import Reika.SatisfactoryPlanner.Data.ResourceSupply;

import javafx.scene.Node;

class SupplyListCell<B extends ResourceSupply> extends DecoratedListCell<B> {

	public SupplyListCell(String ptext, boolean isButton) {
		super(ptext, isButton);
	}

	@Override
	protected String getString(B obj) {
		return obj.getLocationIcon().getDisplayName();
	}

	@Override
	protected Node createDecoration(B obj) {
		return obj.getLocationIcon().createImageView();
	}

}