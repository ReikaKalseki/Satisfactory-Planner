package Reika.SatisfactoryPlanner.Data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;

import Reika.SatisfactoryPlanner.Util.Logging;

import javafx.scene.paint.Color;

public class Database {

	private static final HashMap<String, Building> allBuildings = new HashMap();
	private static final ArrayList<Building> allBuildingsSorted = new ArrayList();
	private static final ArrayList<Generator> allGeneratorsSorted = new ArrayList();

	private static final HashMap<String, Consumable> allItems = new HashMap();
	private static final ArrayList<Consumable> allItemsSorted = new ArrayList();

	private static final ArrayList<Recipe> allAutoRecipesSorted = new ArrayList();
	private static final ArrayList<Recipe> allBuildingRecipesSorted = new ArrayList();
	private static final HashMap<String, Recipe> allRecipes = new HashMap();

	private static final HashMap<String, Vehicle> allVehicles = new HashMap();
	private static final ArrayList<Vehicle> allVehiclesSorted = new ArrayList();

	private static final ArrayList<Item> mineableItems = new ArrayList();
	private static final ArrayList<Fluid> frackableFluids = new ArrayList();

	private static final HashMap<String, Milestone> allMilestones = new HashMap();

	private static final HashMap<String, ClassType> lookup = new HashMap();

	static {
		for (ClassType t : ClassType.values()) {
			for (String s : t.classTypes)
				lookup.put(s, t);
		}
	}
	/*
	public static void loadBuildings() throws IOException {
		File f = Main.extractResourceFolder("Buildings");
		for (File f2 : f.listFiles()) {
			if (f2.getName().endsWith(".json") && !f2.getName().startsWith("template")) {
				Logging.instance.log("Loading building file "+f2);
				JSONObject data = new JSONObject(FileUtils.readFileToString(f2, Charsets.UTF_8));
				Building r = new Building(data.getString("name"), data.getString("icon"), data.getInt("powerCost"));
				JSONObject ing = data.getJSONObject("ingredients");
				for (String s : ing.keySet()) {
					r.addIngredient((Item)lookupItem(s), ing.getInt(s));
				}
				Logging.instance.log("Loaded building "+r);
				allBuildings.put(r.id, r);
				allBuildingsSorted.add(r);
			}
		}
		Collections.sort(allRecipesSorted);
	}

	public static void loadRecipes() throws IOException {
		File f = Main.extractResourceFolder("Recipes");
		for (File f2 : f.listFiles()) {
			if (f2.getName().endsWith(".json") && !f2.getName().startsWith("template")) {
				/* fuck modular java
				JsonObject data = new Gson().fromJson(new BufferedReader(new FileReader(f2)), JsonObject.class);
				boolean alt = data.has("alternate") && data.get("alternate").getAsBoolean();
				Recipe r = new Recipe(data.get("name").getAsString(), alt);
				for (Entry<String, JsonElement> e : data.get("ingredients").getAsJsonObject().entrySet()) {
					r.addIngredient(lookupItem(e.getKey()), e.getValue().getAsInt());
				}
				for (Entry<String, JsonElement> e : data.get("products").getAsJsonObject().entrySet()) {
					r.addProduct(lookupItem(e.getKey()), e.getValue().getAsInt());
				}*//*
				Logging.instance.log("Loading recipe file "+f2);
				JSONObject data = new JSONObject(FileUtils.readFileToString(f2, Charsets.UTF_8));
				boolean alt = data.has("alternate") && data.getBoolean("alternate");
				Recipe r = new Recipe(data.getString("name"), lookupBuilding(data.getString("building")), alt);
				JSONObject ing = data.getJSONObject("ingredients");
				for (String s : ing.keySet()) {
					r.addIngredient(lookupItem(s), ing.getInt(s));
				}
				JSONObject prod = data.getJSONObject("products");
				for (String s : prod.keySet()) {
					r.addProduct(lookupItem(s), prod.getInt(s));
				}
				Logging.instance.log("Loaded recipe "+r);
				allRecipes.put(r.name, r);
				allRecipesSorted.add(r);
			}
		}
		Collections.sort(allRecipesSorted);
	}

	public static void loadItems() throws IOException {
		File f = Main.extractResourceFolder("Items");
		for (File f2 : new File(f, "Solid").listFiles()) {
			DefFile def = new DefFile(f2);
			Item i = new Item(def.name, def.data.get("icon"));
			allItems.put(i.id, i);
			allItemsSorted.add(i);
			if (Boolean.parseBoolean(def.data.get("mineable")))
				mineableItems.add(i);
			Logging.instance.log("Loaded item "+i);
		}
		for (File f2 : new File(f, "Fluid").listFiles()) {
			DefFile def = new DefFile(f2);
			Fluid i = new Fluid(def.name, def.data.get("icon"));
			allItems.put(i.id, i);
			allItemsSorted.add(i);
			if (Boolean.parseBoolean(def.data.get("frackable")))
				frackableFluids.add(i);
			Logging.instance.log("Loaded fluid "+i);
		}
		Collections.sort(allItemsSorted);
	}
				 */
	public static Milestone lookupMilestone(String name) {
		Milestone c = allMilestones.get(name);
		if (c == null)
			throw new IllegalArgumentException("No such milestone '"+name+"'");
		return c;
	}

	public static Building lookupBuilding(String name) {
		Building c = allBuildings.get(name);
		if (c == null)
			throw new IllegalArgumentException("No such building '"+name+"'");
		return c;
	}

	public static Consumable lookupItem(String name) {
		Consumable c = allItems.get(name);
		if (c == null)
			throw new IllegalArgumentException("No such item '"+name+"'");
		return c;
	}

	public static Recipe lookupRecipe(String name) {
		Recipe c = allRecipes.get(name);
		if (c == null)
			throw new IllegalArgumentException("No such recipe '"+name+"'");
		return c;
	}

	public static Vehicle lookupVehicle(String name) {
		Vehicle c = allVehicles.get(name);
		if (c == null)
			throw new IllegalArgumentException("No such vehicle '"+name+"'");
		return c;
	}

	public static List<Building> getAllBuildings() {
		return Collections.unmodifiableList(allBuildingsSorted);
	}

	public static List<Generator> getAllGenerators() {
		return Collections.unmodifiableList(allGeneratorsSorted);
	}

	public static List<Consumable> getAllItems() {
		return Collections.unmodifiableList(allItemsSorted);
	}

	public static List<Recipe> getAllAutoRecipes() {
		return Collections.unmodifiableList(allAutoRecipesSorted);
	}

	public static List<Recipe> getAllBuildingRecipes() {
		return Collections.unmodifiableList(allBuildingRecipesSorted);
	}

	public static List<Item> getMineables() {
		return Collections.unmodifiableList(mineableItems);
	}

	public static List<Fluid> getFrackables() {
		return Collections.unmodifiableList(frackableFluids);
	}

	private static class DefFile {

		private final File sourceFile;
		private final String name;
		private final HashMap<String, String> data = new HashMap();

		private DefFile(File f) throws IOException {
			sourceFile = f;
			for (String s : Files.readLines(f, Charsets.UTF_8)) {
				String[] parts = s.split("=");
				data.put(parts[0].trim(), parts[1].trim());
			}
			name = data.get("name");
		}

	}

	public static void sort() {
		Collections.sort(allItemsSorted);
		Collections.sort(allAutoRecipesSorted);
		Collections.sort(allBuildingRecipesSorted);
		Collections.sort(allBuildingsSorted);
		Collections.sort(allGeneratorsSorted);
		Collections.sort(allVehiclesSorted);
	}

	public static void parseGameJSON() throws IOException {
		File f = new File("P:/SteamOverflow/steamapps/common/Satisfactory/CommunityResources/Docs/Docs.json");
		String UTF8_BOM = "\uFEFF";
		String file = FileUtils.readFileToString(f, Charsets.UTF_16LE);
		JSONTokener tok = new JSONTokener(file);
		tok.nextValue(); //empty leading string
		Object value = tok.nextValue();
		JSONArray all = (JSONArray)value;
		for (Object o : all) {
			JSONObject obj = (JSONObject)o;
			String nat = obj.getString("NativeClass");
			ClassType ct = lookup.get(nat);
			if (ct != null) {
				for (Object o2 : obj.getJSONArray("Classes")) {
					JSONObject entry = (JSONObject)o2;
					entry.put("NativeClass", nat.substring(nat.lastIndexOf('.')+1, nat.lastIndexOf('\'')));
					ct.pendingParses.add(entry);
				}
			}
		}
	}

	private static void parseItemsJSON(JSONObject obj, boolean resource) {
		String id = obj.getString("ClassName");
		Logging.instance.log("Parsing JSON elem "+id);
		String form = obj.getString("mForm");
		boolean gas = form.equalsIgnoreCase("RF_GAS");
		boolean fluid = form.equalsIgnoreCase("RF_LIQUID") || gas;
		String disp = obj.getString("mDisplayName");
		String desc = obj.getString("mDescription");
		String ico = convertIDToIcon(id);
		if (fluid) {
			String clr = obj.getString(gas ? "mGasColor" : "mFluidColor");
			Fluid f = new Fluid(id, disp, ico, desc, obj.getString("NativeClass"), obj.getFloat("mEnergyValue"), parseColor(clr));
			allItems.put(f.id, f);
			allItemsSorted.add(f);
			if (resource)
				frackableFluids.add(f);
		}
		else {
			Item i = new Item(id, disp, ico, desc, obj.getString("NativeClass"), obj.getFloat("mEnergyValue"));
			allItems.put(i.id, i);
			allItemsSorted.add(i);
			if (resource)
				mineableItems.add(i);
		}
	}

	private static String convertIDToIcon(String id) {
		return id.substring(id.indexOf('_')+1, id.length()-2);// strip Desc_ and _C //obj.getString("mPersistentBigIcon");
	}

	private static Color parseColor(String clr) {
		String[] parts = clr.substring(1, clr.length()-1).split(",");
		int r = 0;
		int g = 0;
		int b = 0;
		int a = 0;
		for (String s : parts) {
			String[] split = s.split("=");
			int raw = Integer.parseInt(split[1]);
			switch(split[0].toLowerCase(Locale.ENGLISH)) {
				case "r":
					r = raw;
					break;
				case "g":
					g = raw;
					break;
				case "b":
					b = raw;
					break;
				case "a":
					a = raw;
					break;
			}
		}
		return new Color(r/255F, g/255F, b/255F, a/255F);
	}

	private static String parseID(String iid) {
		iid = iid.substring(iid.lastIndexOf('/')+1, iid.lastIndexOf('"'));
		iid = iid.substring(iid.lastIndexOf('.')+1);
		return iid;
	}

	private static void parseRecipeJSON(JSONObject obj) {
		String id = obj.getString("ClassName");
		Logging.instance.log("Parsing JSON elem "+id);
		String bld = obj.getString("mProducedIn");
		if (Strings.isNullOrEmpty(bld))
			return;
		bld = bld.substring(1, bld.length()-1); //trim ()
		String[] blds = bld.split(",");
		String building = null;
		for (String e : blds) {
			String parse = parseID(e);
			if (parse.equalsIgnoreCase("BP_BuildGun_C") || parse.equalsIgnoreCase("FGBuildGun"))
				break;
			if (parse.equalsIgnoreCase("BP_WorkBenchComponent_C") || parse.equalsIgnoreCase("Build_AutomatedWorkBench_C") || parse.equalsIgnoreCase("FGBuildableAutomatedWorkBench"))
				continue;
			if (parse.equalsIgnoreCase("BP_WorkshopComponent_C"))
				continue;
			building = parse;
		}
		String disp = obj.getString("mDisplayName");
		String in = obj.getString("mIngredients");
		String out = obj.getString("mProduct");
		String time = obj.getString("mManufactoringDuration");
		FunctionalBuilding b = building == null ? null : (FunctionalBuilding)lookupBuilding(building);
		boolean xmas = id.startsWith("Desc_Xmas") || disp.startsWith("FICSMAS") || obj.getString("mRelevantEvents").contains("EV_Christmas");
		Recipe r = new Recipe(id, disp, b, Float.parseFloat(time), xmas);
		for (String ing : in.substring(2, in.length()-2).split("\\),\\(")) {
			String[] parts = ing.split(",");
			Consumable c = lookupItem(parseID(parts[0].split("=")[1]));
			int amt = Integer.parseInt(parts[1].split("=")[1]);
			if (c instanceof Fluid)
				amt /= Constants.LIQUID_SCALAR; //they store fluids in mB
			r.addIngredient(c, amt);
		}
		if (r.productionBuilding == null) { //is a buildable
			String bid = parseID(out.replace("Desc_", "Build_"));
			if (allBuildings.containsKey(bid)) {
				Building bb = lookupBuilding(bid);
				for (Entry<Consumable, Integer> e : r.getDirectCost().entrySet()) {
					bb.addIngredient((Item)e.getKey(), e.getValue());
				}
				Logging.instance.log("Set "+bb+" recipe: "+bb.getConstructionCost());
			}
		}
		else {
			for (String prod : out.substring(2, out.length()-2).split("\\),\\(")) {
				String[] parts = prod.split(",");
				Consumable c = lookupItem(parseID(parts[0].split("=")[1]));
				int amt = Integer.parseInt(parts[1].split("=")[1]);
				if (c instanceof Fluid)
					amt /= Constants.LIQUID_SCALAR; //they store fluids in mB
				r.addProduct(c, amt);
			}
			allRecipes.put(r.id, r);
			if (r.productionBuilding == null)
				allBuildingRecipesSorted.add(r);
			else
				allAutoRecipesSorted.add(r);
			Logging.instance.log("Registered recipe type "+r);
		}
	}

	private static void parseBuildingJSON(JSONObject obj) {
		String id = obj.getString("ClassName");
		Logging.instance.log("Parsing JSON elem "+id);
		String disp = obj.getString("mDisplayName");
		Building r = new Building(id, disp, convertIDToIcon(id));
		allBuildings.put(r.id, r);
		allBuildingsSorted.add(r);
	}

	private static void parseFunctionalBuildingJSON(JSONObject obj) {
		String id = obj.getString("ClassName");
		Logging.instance.log("Parsing JSON elem "+id);
		String disp = obj.getString("mDisplayName");
		String pwr = obj.getString("mPowerConsumption");
		String pwrExp = obj.getString("mPowerConsumptionExponent"); //FIXME handle this for overclock!
		FunctionalBuilding r = new FunctionalBuilding(id, disp, convertIDToIcon(id), Float.parseFloat(pwr));
		allBuildings.put(r.id, r);
		allBuildingsSorted.add(r);
	}

	private static void parseBeltJSON(JSONObject obj) {
		String id = obj.getString("ClassName");
		Logging.instance.log("Parsing JSON elem "+id);
		String disp = obj.getString("mDisplayName");
		String spd = obj.getString("mSpeed"); //TODO is 2x item transfer rate/min
		Building r = new Building(id, disp, convertIDToIcon(id));
		allBuildings.put(r.id, r);
		allBuildingsSorted.add(r);
	}

	private static void parsePipeJSON(JSONObject obj) {
		String id = obj.getString("ClassName");
		Logging.instance.log("Parsing JSON elem "+id);
		String disp = obj.getString("mDisplayName");
		String spd = obj.getString("mFlowLimit"); //TODO is per second, so x60 for /min
		Building r = new Building(id, disp, convertIDToIcon(id));
		allBuildings.put(r.id, r);
		allBuildingsSorted.add(r);
	}

	private static void parseGeneratorJSON(JSONObject obj) {
		String id = obj.getString("ClassName");
		Logging.instance.log("Parsing JSON elem "+id);
		String disp = obj.getString("mDisplayName");
		String pwr = obj.getString("mPowerProduction");
		String sup = obj.has("mSupplementalToPowerRatio") ? obj.getString("mSupplementalToPowerRatio") : null;
		JSONArray fuels = obj.has("mFuel") ? obj.getJSONArray("mFuel") : null;
		float suppl = Strings.isNullOrEmpty(sup) ? 0 : Float.parseFloat(sup);
		Generator r = new Generator(id, disp, convertIDToIcon(id), Float.parseFloat(pwr), suppl);
		if (fuels != null) {
			String fuelForm = obj.getString("mFuelResourceForm");
			for (Object o : fuels) {
				JSONObject fuel = (JSONObject)o;
				String second = fuel.getString("mSupplementalResourceClass");
				String out = fuel.getString("mByproduct");
				Consumable secondItem = Strings.isNullOrEmpty(second) ? null : lookupItem(second);
				Consumable outItem = Strings.isNullOrEmpty(out) ? null : lookupItem(out);
				String item = fuel.getString("mFuelClass");
				if (item.startsWith("Desc_")) {
					Fuel f = new Fuel(r, lookupItem(item), secondItem, outItem, outItem == null ? 0 : fuel.getInt("mByproductAmount"));
					r.addFuel(f);
				}
				else if (item.startsWith("FGItemDescriptor")) {
					for (Consumable c : Consumable.getForClass(item)) {
						if (c.energyValue > 0 && ((fuelForm.equalsIgnoreCase("RF_SOLID") && c instanceof Item) || (fuelForm.equalsIgnoreCase("RF_FLUID") && c instanceof Fluid))) {
							Fuel f = new Fuel(r, c, secondItem, outItem, outItem == null ? 0 : fuel.getInt("mByproductAmount"));
							r.addFuel(f);
						}
					}
				}
			}
		}
		allBuildings.put(r.id, r);
		allBuildingsSorted.add(r);
		allGeneratorsSorted.add(r);
	}

	private static void parseVehicleJSON(JSONObject obj) {
		String id = obj.getString("ClassName");
		Logging.instance.log("Parsing JSON elem "+id);
		String disp = obj.getString("mDisplayName");
		Vehicle v = new Vehicle(id, disp, convertIDToIcon(id));
		allVehicles.put(v.id, v);
		allVehiclesSorted.add(v);
	}

	private static void parseMilestoneJSON(JSONObject obj) { //also has mCost, ingredients format
		String id = obj.getString("ClassName");
		Logging.instance.log("Parsing JSON elem "+id);
		boolean isAltRecipe = id.startsWith("Schematic_Alternate_");
		String disp = obj.getString("mDisplayName");
		int tier = Integer.parseInt(obj.getString("mTechTier"));
		JSONArray unlocks = obj.getJSONArray("mUnlocks");
		JSONArray deps = obj.getJSONArray("mSchematicDependencies");
		Milestone m = new Milestone(tier, disp);
		for (Object o : deps) {
			JSONObject ulock = (JSONObject)o;
			String type = ulock.getString("Class");
			if (type.equalsIgnoreCase("BP_SchematicPurchasedDependency_C")) {
				String recc = ulock.getString("mSchematics");
				String[] recipes = recc.substring(1, recc.length()-1).split(",");
				for (String s : recipes) {
					String rs = parseID(s);
					if (allMilestones.containsKey(rs)) {
						m.addDependency(lookupMilestone(rs));
					}
				}
			}
		}
		boolean flag = false;
		for (Object o : unlocks) {
			JSONObject ulock = (JSONObject)o;
			String type = ulock.getString("Class");
			if (type.equalsIgnoreCase("BP_UnlockRecipe_C")) {
				String recc = ulock.getString("mRecipes");
				String[] recipes = recc.substring(1, recc.length()-1).split(",");
				for (String s : recipes) {
					String rs = parseID(s);
					if (allRecipes.containsKey(rs)) {
						flag = true;
						m.addRecipe(lookupRecipe(rs));
					}
				}
			}
		}
		if (flag)
			allMilestones.put(id, m);
	}

	public static enum ClassType {
		ITEM("/Script/CoreUObject.Class'/Script/FactoryGame.FGItemDescriptor'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGItemDescriptorBiomass'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGItemDescriptorNuclearFuel'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGEquipmentDescriptor'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGConsumableDescriptor'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGAmmoTypeInstantHit'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGAmmoTypeProjectile'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGAmmoTypeSpreadshot'"),
		RESOURCE("/Script/CoreUObject.Class'/Script/FactoryGame.FGResourceDescriptor'"),
		RECIPE("/Script/CoreUObject.Class'/Script/FactoryGame.FGRecipe'"),
		GENERATOR("/Script/CoreUObject.Class'/Script/FactoryGame.FGBuildableGeneratorFuel'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGBuildableGeneratorNuclear'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGBuildableGeneratorGeoThermal'"),
		CRAFTER("/Script/CoreUObject.Class'/Script/FactoryGame.FGBuildableManufacturer'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGBuildableManufacturerVariablePower'"),
		MINER("/Script/CoreUObject.Class'/Script/FactoryGame.FGBuildableResourceExtractor'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGBuildableWaterPump'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGBuildableFrackingActivator'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGBuildableFrackingExtractor'"),
		STATION("/Script/CoreUObject.Class'/Script/FactoryGame.FGBuildableDockingStation'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGBuildableTrainPlatformCargo'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGBuildableDroneStation'"),
		BELT("/Script/CoreUObject.Class'/Script/FactoryGame.FGBuildableConveyorBelt'"),
		PIPE("/Script/CoreUObject.Class'/Script/FactoryGame.FGBuildablePipeline'"),
		VEHICLE("/Script/CoreUObject.Class'/Script/FactoryGame.FGVehicleDescriptor'"),
		//MISCBUILD("/Script/CoreUObject.Class'/Script/FactoryGame.FGBuildingDescriptor'"),
		MILESTONE("/Script/CoreUObject.Class'/Script/FactoryGame.FGSchematic'"),
		;

		private final HashSet<String> classTypes = new HashSet();

		private final ArrayList<JSONObject> pendingParses = new ArrayList();

		private ClassType(String... ids) {
			for (String s : ids) {
				classTypes.add(s);
			}
		}

		public void parsePending() {
			for (JSONObject obj : pendingParses)
				this.parseObject(obj);
		}

		private void parseObject(JSONObject obj) {
			switch(this) {
				case ITEM:
				case RESOURCE:
					parseItemsJSON(obj, this == RESOURCE);
					break;
				case RECIPE:
					parseRecipeJSON(obj);
					break;
				case GENERATOR:
					parseGeneratorJSON(obj);
					break;
				case CRAFTER:
					parseFunctionalBuildingJSON(obj);
					break;
				case MINER:
					parseFunctionalBuildingJSON(obj);
					break;
				case STATION:
					parseFunctionalBuildingJSON(obj);
					break;
				case BELT:
					parseBeltJSON(obj);
					break;
				case PIPE:
					parsePipeJSON(obj);
					break;
					//case MISCBUILD:
					//	parseBuildingJSON(obj);
					//break;
				case MILESTONE:
					parseMilestoneJSON(obj);
					break;
				case VEHICLE:
					parseVehicleJSON(obj);
					break;
			}
		}
	}

}
