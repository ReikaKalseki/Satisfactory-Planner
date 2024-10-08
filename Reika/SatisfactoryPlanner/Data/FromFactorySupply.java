package Reika.SatisfactoryPlanner.Data;

import java.io.File;
import java.util.function.Consumer;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.InternalIcons;
import Reika.SatisfactoryPlanner.NamedIcon;
import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;

public class FromFactorySupply<R extends Consumable> implements ResourceSupply<R> {

	public final R item;
	public float amount;
	public final String sourceFactory;
	public final File sourceFactoryFile;

	public FromFactorySupply(R c, float amt, String name, File f) {
		item = c;
		amount = amt;
		sourceFactory = name;
		sourceFactoryFile = f;
	}

	private FromFactorySupply(JSONObject obj) {
		this((R)Database.lookupItem(obj.getString("item")), obj.getFloat("amount"), obj.getString("name"), new File(obj.getString("path")));
	}

	@Override
	public void save(JSONObject block) {
		block.put("item", item.id);
		block.put("amount", amount);
		block.put("name", sourceFactory);
		block.put("path", sourceFactoryFile.getAbsolutePath());
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
		return item.displayName+" from "+sourceFactory;
	}

	@Override
	public int getSubSortIndex() {
		return -1;
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

	@Override
	public Building getBuilding() {
		return null;
	}

}
