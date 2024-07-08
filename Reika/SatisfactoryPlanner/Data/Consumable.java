package Reika.SatisfactoryPlanner.Data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import Reika.SatisfactoryPlanner.Util.Logging;
import Reika.SatisfactoryPlanner.Util.MultiMap;

public abstract class Consumable extends Resource implements Comparable<Consumable> {

	public final String description;
	public final String nativeClass;

	public final float energyValue;

	public final boolean isEquipment;
	public final boolean isBiomass;
	public final boolean isFicsmas;

	private static final HashSet<String> findableIDs = new HashSet();

	private static final MultiMap<String, Consumable> byCategory = new MultiMap();

	static {
		findableIDs.add("Desc_HardDrive_C");
		findableIDs.add("Desc_CrystalShard_C");
		findableIDs.add("Desc_Crystal_C");
		findableIDs.add("Desc_Crystal_mk2_C");
		findableIDs.add("Desc_Crystal_mk3_C");
		findableIDs.add("Desc_WTF1_C");
		findableIDs.add("Desc_WTF2_C");
	}

	protected Consumable(String id, String dn, String img, String desc, String cat, float nrg) {
		super(id, dn, img);
		description = desc;
		nativeClass = cat;
		energyValue = nrg;
		isEquipment = cat.equalsIgnoreCase("FGEquipmentDescriptor");
		isBiomass = cat.equalsIgnoreCase("FGItemDescriptorBiomass") && energyValue > 0;
		isFicsmas = id.startsWith("Desc_Xmas") || displayName.startsWith("FICSMAS"); //FIXME misses several including firework and snow
		byCategory.addValue(cat, this);
		Logging.instance.log("Registered item type "+this);
	}

	public boolean isFindables() {
		return findableIDs.contains(id);
	}

	public int compareTo(Consumable c) {
		return String.CASE_INSENSITIVE_ORDER.compare(id, c.id);
	}

	@Override
	protected final String getIconFolder() {
		return "Items";
	}

	public static Collection<Consumable> getForClass(String nat) {
		return Collections.unmodifiableCollection(byCategory.get(nat));
	}

}
