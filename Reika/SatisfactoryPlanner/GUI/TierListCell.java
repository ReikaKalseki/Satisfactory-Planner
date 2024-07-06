package Reika.SatisfactoryPlanner.GUI;

import Reika.SatisfactoryPlanner.Data.Constants.BuildingTier;

import javafx.scene.Node;

class TierListCell<T extends BuildingTier> extends DecoratedListCell<T> {

	public TierListCell(String ptext, boolean isButton) {
		super(ptext, isButton);
	}

	@Override
	protected String getString(T obj) {
		return obj.getBuilding().displayName;
	}

	@Override
	protected Node createDecoration(T obj) {
		return obj.getBuilding().createImageView();
	}

}