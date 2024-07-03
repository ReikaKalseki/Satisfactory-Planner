package Reika.SatisfactoryPlanner.Data;

import Reika.SatisfactoryPlanner.Data.Constants.Purity;


public class OilNode extends BaseResourceNode<Fluid> {

	public OilNode(Purity p) {
		super((Fluid)Database.lookupItem("Desc_LiquidOil_C"), p);
	}

	@Override
	public int getYield() {
		return purityLevel == null ? 0 : (int)(purityLevel.getOilYield()*this.getClockSpeed());
	}

	@Override
	public Building getBuilding() {
		return Database.lookupBuilding("Build_OilPump_C");
	}

}
