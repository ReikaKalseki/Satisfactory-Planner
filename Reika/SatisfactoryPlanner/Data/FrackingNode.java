package Reika.SatisfactoryPlanner.Data;

import Reika.SatisfactoryPlanner.Data.Constants.PipeTier;
import Reika.SatisfactoryPlanner.Data.Constants.Purity;
import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;


public class FrackingNode extends BaseResourceNode<FrackingNode, Fluid> {

	public FrackingNode(Fluid c, Purity p) {
		super(c, p);
	}

	@Override
	public int getYield() {
		return purityLevel == null ? 0 : (int)(purityLevel.getFrackingYield()*this.getClockSpeed());
	}

	@Override
	public FunctionalBuilding getBuilding() {
		return (FunctionalBuilding)Database.lookupBuilding("Desc_FrackingExtractor_C");
	}

	@Override
	public ResourceSupplyType getType() {
		return ResourceSupplyType.FRACKING;
	}

	@Override
	public FrackingNode duplicate() {
		return new FrackingNode(resource, purityLevel);
	}

	@Override
	public int getMaximumThroughput() {
		return PipeTier.TWO.maxThroughput;
	}

	@Override
	public int getSubSortIndex() {
		return 3;
	}

}
