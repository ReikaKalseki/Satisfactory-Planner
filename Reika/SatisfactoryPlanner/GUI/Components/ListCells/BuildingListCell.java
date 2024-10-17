package Reika.SatisfactoryPlanner.GUI.Components.ListCells;

import Reika.SatisfactoryPlanner.Data.Objects.Buildables.Building;

import javafx.scene.Node;

public class BuildingListCell<B extends Building> extends DecoratedListCell<B> {

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