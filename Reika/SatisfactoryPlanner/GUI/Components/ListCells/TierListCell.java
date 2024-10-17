package Reika.SatisfactoryPlanner.GUI.Components.ListCells;

import Reika.SatisfactoryPlanner.Data.Constants.BuildingTier;

import javafx.scene.Node;

public class TierListCell<T extends BuildingTier> extends DecoratedListCell<T> {

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