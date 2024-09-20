package Reika.SatisfactoryPlanner.GUI;

import Reika.SatisfactoryPlanner.Data.Milestone;

import javafx.scene.Node;

class MilestoneListCell extends DecoratedListCell<Milestone> {

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