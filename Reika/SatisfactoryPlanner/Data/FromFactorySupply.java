package Reika.SatisfactoryPlanner.Data;

import java.util.function.Consumer;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.InternalIcons;
import Reika.SatisfactoryPlanner.NamedIcon;
import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;

public class FromFactorySupply<R extends Consumable> implements ResourceSupply<R> {

	public final R item;
	public final float amount;
	public final String sourceFactory;

	public FromFactorySupply(R c, float amt, String name) {
		item = c;
		amount = amt;
		sourceFactory = name;
	}

	private FromFactorySupply(JSONObject obj) {
		this((R)Database.lookupItem(obj.getString("item")), obj.getFloat("amount"), obj.getString("name"));
	}

	@Override
	public void save(JSONObject block) {
		block.put("item", item.id);
		block.put("amount", amount);
		block.put("name", sourceFactory);
	}

	@Override
	public ResourceSupplyType getType() {
		return ResourceSupplyType.FACTORY;
	}

	@Override
	public FromFactorySupply<R> duplicate() {
		throw new UnsupportedOperationException();//return new FromFactorySupply(item, amount, sourceFactory);
	}

	@Override
	public String getDisplayName() {
		return item.displayName;
	}

	@Override
	public int getSubSortIndex() {
		return 2;
	}

	@Override
	public NamedIcon getLocationIcon() {
		return InternalIcons.FACTORY;
	}

	@Override
	public int getYield() {
		return (int)amount;
	}

	@Override
	public R getResource() {
		return item;
	}

	@Override
	public void getWarnings(Consumer<Warning> c) {

	}

}