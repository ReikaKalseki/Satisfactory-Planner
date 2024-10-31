package Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies;

import java.io.File;
import java.util.function.Consumer;

import org.json.JSONObject;

import Reika.SatisfactoryPlanner.InternalIcons;
import Reika.SatisfactoryPlanner.NamedIcon;
import Reika.SatisfactoryPlanner.Data.Constants.ResourceSupplyType;
import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Data.Warning;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.Building;

public class FromFactorySupply<R extends Consumable> implements ResourceSupply<FromFactorySupply<R>, R> {

	public final R item;
	public double amount;
	public final String sourceFactory;
	public final File sourceFactoryFile;

	public FromFactorySupply(R c, double amt, String name, File f) {
		item = c;
		amount = amt;
		sourceFactory = name;
		sourceFactoryFile = f;
	}

	private FromFactorySupply(JSONObject obj) {
		this((R)Database.lookupItem(obj.getString("item")), obj.getDouble("amount"), obj.getString("name"), new File(obj.getString("path")));
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
	public double getYield() {
		return amount;
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

	@Override
	public int fineCompare(FromFactorySupply r) {
		return String.CASE_INSENSITIVE_ORDER.compare(sourceFactory, r.sourceFactory);
	}

}
