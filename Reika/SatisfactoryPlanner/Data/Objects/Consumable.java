package Reika.SatisfactoryPlanner.Data.Objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import Reika.SatisfactoryPlanner.Data.Database;
import Reika.SatisfactoryPlanner.Util.Logging;
import Reika.SatisfactoryPlanner.Util.MultiMap;

public abstract class Consumable extends Resource implements Comparable<Consumable> {

	public final String description;
	public final String nativeClass;

	public final float energyValue;

	public final boolean isFindable;
	public final boolean isEquipment;
	public final boolean isBiomass;
	public final boolean isFicsmas;
	public final boolean isAlien;
	public final boolean isRawResource;

	private static final ArrayList<String> findableOrder = new ArrayList();
	private static final HashSet<String> remainsIDs = new HashSet();
	private static final ArrayList<String> oreOrder = new ArrayList();
	private static final ArrayList<String> alienOrder = new ArrayList();

	private static final MultiMap<String, Consumable> byCategory = new MultiMap();

	private final ArrayList<Recipe> recipesMaking = new ArrayList();

	private int minimumTier = 999;

	static {
		findableOrder.add("Desc_Nut_C");
		findableOrder.add("Desc_Berry_C");
		findableOrder.add("Desc_Shroom_C");

		findableOrder.add("Desc_Crystal_C");
		findableOrder.add("Desc_Crystal_mk2_C");
		findableOrder.add("Desc_Crystal_mk3_C");
		findableOrder.add("Desc_CrystalShard_C");
		findableOrder.add("Desc_HardDrive_C");
		findableOrder.add("Desc_WAT1_C");
		findableOrder.add("Desc_WAT2_C");

		findableOrder.add("Desc_Gift_C");

		//findableIDs.add("Desc_Mycelia_C");

		alienOrder.add("Desc_WAT1_C");
		alienOrder.add("Desc_WAT2_C");
		alienOrder.add("Desc_SAM_C");
		alienOrder.add("Desc_SAMIngot_C");
		alienOrder.add("Desc_SAMFluctuator_C");
		alienOrder.add("Desc_FicsiteIngot_C");
		alienOrder.add("Desc_FicsiteMesh_C");
		alienOrder.add("Desc_Ficsonium_C");
		alienOrder.add("Desc_FicsoniumFuelRod_C");

		remainsIDs.add("Desc_HogParts_C");
		remainsIDs.add("Desc_SpitterParts_C");
		remainsIDs.add("Desc_StingerParts_C");
		remainsIDs.add("Desc_HatcherParts_C");

		oreOrder.add("Desc_OreIron_C");
		oreOrder.add("Desc_OreCopper_C");
		oreOrder.add("Desc_Stone_C");
		oreOrder.add("Desc_Coal_C");
		oreOrder.add("Desc_OreGold_C");
		oreOrder.add("Desc_RawQuartz_C");
		oreOrder.add("Desc_Sulfur_C");
		oreOrder.add("Desc_OreBauxite_C");
		oreOrder.add("Desc_OreUranium_C");
		oreOrder.add("Desc_SAM_C");
	}

	protected Consumable(String id, String dn, String img, String desc, String cat, float nrg) {
		super(id, dn, img);
		description = desc;
		nativeClass = cat;
		energyValue = nrg;
		isRawResource = cat.equalsIgnoreCase("FGResourceDescriptor");
		isEquipment = cat.equalsIgnoreCase("FGEquipmentDescriptor");
		isFindable = findableOrder.contains(id);
		isAlien = alienOrder.contains(id);
		isBiomass = (cat.equalsIgnoreCase("FGItemDescriptorBiomass") && energyValue > 0) || remainsIDs.contains(id) || id.equalsIgnoreCase("Desc_AlienProtein_C") || id.equalsIgnoreCase("Desc_AlienDNACapsule_C");
		isFicsmas = id.startsWith("Desc_Xmas") || id.equalsIgnoreCase("Desc_Snow_C") || displayName.startsWith("FICSMAS") || id.startsWith("Desc_Fireworks_Projectile") || id.startsWith("BP_EquipmentDescriptorCandyCane_C");
		byCategory.addValue(cat, this);
		Logging.instance.log("Registered item type "+this);
	}

	public Consumable addRecipe(Recipe r) {
		recipesMaking.add(r);
		minimumTier = Math.min(r.getTier(), minimumTier);
		return this;
	}

	public int getTier() {
		if (isFindable || isBiomass || (isRawResource && this instanceof Item))
			return -1;
		if (id.equalsIgnoreCase("Desc_Water_C"))
			return Database.areMilestonesLoaded() ? Database.lookupRecipe("Recipe_WaterPump_C").getTier() : 3; //hardcoded fallbacks for during boot
		if (Database.getFrackables().contains(this))
			return Database.areMilestonesLoaded() ? Database.lookupRecipe("Recipe_FrackingSmasher_C").getTier() : 7;
		return Math.min(Milestone.getMaxTier(), minimumTier);
	}

	public int compareTo(Consumable c) {
		if (this == c)
			return 0;
		if (this.getTier() != c.getTier())
			return Integer.compare(this.getTier(), c.getTier());
		int ret = String.CASE_INSENSITIVE_ORDER.compare(id, c.id);
		int nrgComp = Float.compare(energyValue, c.energyValue);
		if (nrgComp != 0)
			ret = nrgComp;
		int group1 = this.getSortGroup();
		int group2 = c.getSortGroup();

		if (group1 != group2)
			return Integer.compare(group1, group2);

		if (isRawResource)
			return c.isRawResource ? this.subsort(c, oreOrder, ret) : -1;
		else if (c.isRawResource)
			return 1;

		if (isAlien)
			return c.isAlien ? this.subsort(c, alienOrder, ret) : 1;
		else if (c.isAlien)
			return -1;

		if (isFindable)
			return this.subsort(c, findableOrder, ret);

		return ret;
	}

	private int subsort(Consumable c, ArrayList<String> order, int fallback) {
		int idx1 = order.indexOf(id);
		int idx2 = order.indexOf(c.id);
		if (idx1 == -1 || idx2 == -1)
			return idx1 == idx2 ? fallback : idx1 == -1 ? 1 : -1;
		return Integer.compare(idx1, idx2);
	}

	@Override
	protected final String getIconFolder() {
		return "Items";
	}

	public static Collection<Consumable> getForClass(String nat) {
		return Collections.unmodifiableCollection(byCategory.get(nat));
	}

	public int getSortGroup() {
		if (isRawResource)
			return 0;
		else if (isFindable)
			return 1;
		else if (isBiomass)
			return 2;
		return 3;
	}

}
