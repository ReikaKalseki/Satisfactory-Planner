package Reika.SatisfactoryPlanner.Data;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Data.Constants.MinerTier;
import Reika.SatisfactoryPlanner.Data.Constants.Purity;
import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;


public class SolidResourceNode extends BaseResourceNode<Item> {

	public final MinerTier minerLevel;

	public SolidResourceNode(Item c, Purity p, MinerTier m) {
		super(c, p);
		minerLevel = m;
	}

	private SolidResourceNode(JSONObject obj) {
		this((Item)Database.lookupItem(obj.getString("item")), Purity.valueOf(obj.getString("purity")), MinerTier.valueOf(obj.getString("miner")));
		this.setClockSpeed(obj.getFloat("clock"));
	}

	@Override
	public int getYield() {
		return purityLevel == null || minerLevel == null ? 0 : (int)(purityLevel.getSolidYield()*minerLevel.speedMultiplier*this.getClockSpeed());
	}

	@Override
	public FunctionalBuilding getBuilding() {
		return minerLevel.getMiner();
	}

	@Override
	public ResourceSupplyType getType() {
		return ResourceSupplyType.MINER;
	}

	@Override
	public void save(JSONObject block) {
		super.save(block);
		block.put("miner", minerLevel.name());
	}

}
