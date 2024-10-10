package Reika.SatisfactoryPlanner.Data;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.Data.Constants.PipeTier;
import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;

public class InputPipe extends TieredLogisticSupply<InputPipe, PipeTier, Fluid> {

	public InputPipe(Fluid i, PipeTier b) {
		super(i, b);
	}

	private InputPipe(JSONObject obj) {
		this((Fluid)Database.lookupItem(obj.getString("item")), PipeTier.valueOf(obj.getString("pipe")));
		this.setAmount(obj.getInt("amount"));
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
	public InputPipe duplicate() {
		return new InputPipe(resource, tier);
	}

	@Override
	public int getSubSortIndex() {
		return 1;
	}

	@Override
	public PipeTier[] getTierValues() {
		return PipeTier.values();
	}

}
