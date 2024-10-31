package Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Data.Constants.PipeTier;
import Reika.SatisfactoryPlanner.Data.Constants.Purity;
import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.Objects.Fluid;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.FunctionalBuilding;


public class OilNode extends BaseResourceNode<OilNode, Fluid> {

	public OilNode(Purity p) {
		super((Fluid)Database.lookupItem("Desc_LiquidOil_C"), p);
	}

	private OilNode(JSONObject obj) {
		this(Purity.valueOf(obj.getString("purity")));
		this.setClockSpeed(obj.getFloat("clock"));
	}

	@Override
	public double getYield() {
		return purityLevel == null ? 0 : (int)(purityLevel.getOilYield()*this.getClockSpeed());
	}

	@Override
	public FunctionalBuilding getBuilding() {
		return (FunctionalBuilding)Database.lookupBuilding("Build_OilPump_C");
	}

	@Override
	public ResourceSupplyType getType() {
		return ResourceSupplyType.OIL;
	}

	@Override
	public OilNode duplicate() {
		return new OilNode(purityLevel);
	}

	@Override
	public int getMaximumThroughput() {
		return PipeTier.TWO.maxThroughput;
	}

	@Override
	public int getSubSortIndex() {
		return 2;
	}

}
