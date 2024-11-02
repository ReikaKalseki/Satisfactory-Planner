package Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Data.Constants.BeltTier;
import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.Objects.Item;

public class InputBelt extends TieredLogisticSupply<InputBelt, BeltTier, Item> {

	public InputBelt(Item i, BeltTier b) {
		super(i, b);
	}

	private InputBelt(JSONObject obj) {
		this((Item)Database.lookupItem(obj.getString("item")), BeltTier.valueOf(obj.getString("belt")));
		this.setAmount(obj.getDouble("amount"));
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
	public InputBelt duplicate() {
		return new InputBelt(resource, tier);
	}

	@Override
	public int getSubSortIndex() {
		return 0;
	}

	@Override
	public BeltTier[] getTierValues() {
		return BeltTier.values();
	}

}
