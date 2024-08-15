package Reika.SatisfactoryPlanner.Data;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Data.Constants.PipeTier;
import Reika.SatisfactoryPlanner.Data.Constants.RateLimitedSupplyLine;
import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;

public class InputPipe extends LogisticSupply<Fluid> {

	public final PipeTier tier;

	public InputPipe(Fluid i, PipeTier b) {
		super(i);
		tier = b;
	}

	private InputPipe(JSONObject obj) {
		this((Fluid)Database.lookupItem(obj.getString("item")), PipeTier.valueOf(obj.getString("pipe")));
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
	public Resource getLocationIcon() {
		return tier.getBuilding();
	}

	@Override
	public void save(JSONObject block) {
		super.save(block);
		block.put("pipe", tier.name());
	}

	@Override
	public ResourceSupplyType getType() {
		return ResourceSupplyType.PIPE;
	}

	@Override
	public ResourceSupply<Fluid> duplicate() {
		return new InputPipe(resource, tier);
	}

	@Override
	public String getDisplayName() {
		return this.getLocationIcon().displayName;
	}

	@Override
	public int getSubSortIndex() {
		return 1;
	}

}
