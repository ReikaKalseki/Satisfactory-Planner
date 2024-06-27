package Reika.SatisfactoryPlanner.Data;

import Reika.SatisfactoryPlanner.Data.Constants.MinerTier;
import Reika.SatisfactoryPlanner.Data.Constants.Purity;


public class SolidResourceNode extends BaseResourceNode<Item> {

	public final MinerTier minerLevel;

	public SolidResourceNode(Item c, Purity p, MinerTier m) {
		super(c, p);
		minerLevel = m;
	}

	@Override
	public int getYield() {
		return purityLevel == null || minerLevel == null ? 0 : (int)(purityLevel.getSolidYield()*minerLevel.speedMultiplier*this.getClockSpeed());
	}

}
