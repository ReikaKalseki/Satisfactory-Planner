package Reika.SatisfactoryPlanner.Data;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Data.Constants.BeltTier;
import Reika.SatisfactoryPlanner.Data.Constants.RateLimitedSupplyLine;
import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;

public class InputBelt extends LogisticSupply<Item> {

	public final BeltTier tier;

	public InputBelt(Item i, BeltTier b) {
		super(i);
		tier = b;
	}

	private InputBelt(JSONObject obj) {
		this((Item)Database.lookupItem(obj.getString("item")), BeltTier.valueOf(obj.getString("belt")));
		this.setAmount(obj.getInt("amount"));
	}

	@Override
	public int getPortCount() {
		return 1;
	}

	@Override
	public RateLimitedSupplyLine getMaximumPortFlow() {
		return tier;
	}

	@Override
	public Resource getIcon() {
		return tier.getBuilding();
	}

	@Override
	public void save(JSONObject block) {
		super.save(block);
		block.put("belt", tier.name());
	}

	@Override
	public ResourceSupplyType getType() {
		return ResourceSupplyType.BELT;
	}

	@Override
	public ResourceSupply<Item> duplicate() {
		return new InputBelt(resource, tier);
	}

	@Override
	public String getDisplayName() {
		return this.getIcon().displayName;
	}

}
