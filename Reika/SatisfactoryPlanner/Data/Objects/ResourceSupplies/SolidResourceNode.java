package Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Data.Constants.BeltTier;
import Reika.SatisfactoryPlanner.Data.Constants.MinerTier;
import Reika.SatisfactoryPlanner.Data.Constants.Purity;
import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.Objects.Item;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.FunctionalBuilding;


public class SolidResourceNode extends BaseResourceNode<SolidResourceNode, Item> {

	public MinerTier minerLevel;

	public SolidResourceNode(Item c, Purity p) {
		this(c, p, MinerTier.ONE);
	}

	public SolidResourceNode(Item c, Purity p, MinerTier m) {
		super(c, p);
		minerLevel = m;
	}

	private SolidResourceNode(JSONObject obj) {
		this((Item)Database.lookupItem(obj.getString("item")), Purity.valueOf(obj.getString("purity")));
		if (obj.has("miner"))
			minerLevel = MinerTier.valueOf(obj.getString("miner"));
		this.setClockSpeed(obj.getFloat("clock"));
	}

	@Override
	public float getYield() {
		return purityLevel == null || minerLevel == null ? 0 : (int)(purityLevel.getSolidYield()*minerLevel.speedMultiplier*this.getClockSpeed());
	}

	@Override
	public FunctionalBuilding getBuilding() {
		return (FunctionalBuilding)minerLevel.getBuilding();
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

	@Override
	public SolidResourceNode duplicate() {
		SolidResourceNode ret = new SolidResourceNode(resource, purityLevel);
		ret.minerLevel = minerLevel;
		return ret;
	}

	@Override
	public int getMaximumThroughput() {
		return BeltTier.SIX.maxThroughput;
	}

	@Override
	public int getSubSortIndex() {
		return 0;
	}

	@Override
	public int fineCompare(SolidResourceNode r) {
		return super.fineCompare(r)*1000+minerLevel.compareTo(r.minerLevel);
	}

}
