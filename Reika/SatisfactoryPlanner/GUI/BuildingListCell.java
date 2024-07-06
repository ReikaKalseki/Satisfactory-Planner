package Reika.SatisfactoryPlanner.GUI;

import Reika.SatisfactoryPlanner.Data.Building;

import javafx.scene.Node;

class BuildingListCell<B extends Building> extends DecoratedListCell<B> {

	public BuildingListCell(String ptext, boolean isButton) {
		super(ptext, isButton);
	}

	@Override
	protected String getString(B obj) {
		return obj.displayName;
	}

	@Override
	protected Node createDecoration(B obj) {
		return obj.createImageView();
	}

}