package Reika.SatisfactoryPlanner.GUI;

import Reika.SatisfactoryPlanner.Data.FunctionalBuilding;

import javafx.scene.Node;

class BuildingListCell extends DecoratedListCell<FunctionalBuilding> {

	public BuildingListCell(String ptext, boolean isButton) {
		super(ptext, isButton);
	}

	@Override
	protected String getString(FunctionalBuilding obj) {
		return obj.displayName;
	}

	@Override
	protected Node createDecoration(FunctionalBuilding obj) {
		return obj.createImageView();
	}

}