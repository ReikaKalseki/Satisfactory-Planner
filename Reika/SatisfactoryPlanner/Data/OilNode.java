package Reika.SatisfactoryPlanner.Data;

import Reika.SatisfactoryPlanner.Data.Constants.Purity;


public class OilNode extends BaseResourceNode {

	public OilNode(Purity p) {
		super(Database.lookupItem("Crude Oil"), p);
	}

	@Override
	public int getYield() {
		return (int)(purityLevel.getOilYield()*this.getClockSpeed());
	}

}
