package Reika.SatisfactoryPlanner.Data;

import Reika.SatisfactoryPlanner.Data.Constants.MinerTier;
import Reika.SatisfactoryPlanner.Data.Constants.Purity;


public class SolidResourceNode extends BaseResourceNode {

	public final MinerTier minerLevel;

	public SolidResourceNode(Item c, Purity p, MinerTier m) {
		super(c, p);
		minerLevel = m;
	}

	@Override
	public int getYield() {
		return (int)(purityLevel.getSolidYield()*minerLevel.speedMultiplier*this.getClockSpeed());
	}

}
