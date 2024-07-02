package Reika.SatisfactoryPlanner.GUI;

import Reika.SatisfactoryPlanner.Data.Building;

import javafx.scene.Node;

class BuildingListCell extends DecoratedListCell<Building> {

	public BuildingListCell(String ptext, boolean isButton) {
		super(ptext, isButton);
	}

	@Override
	protected String getString(Building obj) {
		return obj.displayName;
	}

	@Override
	protected Node createDecoration(Building obj) {
		return obj.createImageView();
	}

}