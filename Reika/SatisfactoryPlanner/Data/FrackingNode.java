package Reika.SatisfactoryPlanner.Data;

import Reika.SatisfactoryPlanner.Data.Constants.Purity;


public class FrackingNode extends BaseResourceNode<Fluid> {

	public FrackingNode(Fluid c, Purity p) {
		super(c, p);
	}

	@Override
	public int getYield() {
		return purityLevel == null ? 0 : (int)(purityLevel.getFrackingYield()*this.getClockSpeed());
	}

}
