package Reika.SatisfactoryPlanner.GUI.Components.ListCells;

import Reika.SatisfactoryPlanner.Data.Objects.Milestone;

import javafx.scene.Node;

public class MilestoneListCell extends DecoratedListCell<Milestone> {

	public MilestoneListCell(String ptext, boolean isButton) {
		super(ptext, isButton);
		//Logging.instance.log("Creating item list cell "+ptext);
	}

	@Override
	protected String getString(Milestone obj) {
		return obj.displayName+" (T"+obj.getTier()+")";
	}

	@Override
	protected Node createDecoration(Milestone obj) {
		return null;
	}

}