package Reika.SatisfactoryPlanner.Data;

import Reika.SatisfactoryPlanner.Data.Constants.Purity;


public class FrackingNode extends BaseResourceNode {

	public FrackingNode(Fluid c, Purity p) {
		super(c, p);
	}

	@Override
	public int getYield() {
		return (int)(purityLevel.getFrackingYield()*this.getClockSpeed());
	}

}
