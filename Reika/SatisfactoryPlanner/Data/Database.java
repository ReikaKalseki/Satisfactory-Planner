package Reika.SatisfactoryPlanner.Data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Setting;
import Reika.SatisfactoryPlanner.Data.PowerOverride.LinearIncreasePower;
import Reika.SatisfactoryPlanner.Data.Objects.Consumable;
import Reika.SatisfactoryPlanner.Data.Objects.Fluid;
import Reika.SatisfactoryPlanner.Data.Objects.Fuel;
import Reika.SatisfactoryPlanner.Data.Objects.Item;
import Reika.SatisfactoryPlanner.Data.Objects.Milestone;
import Reika.SatisfactoryPlanner.Data.Objects.Recipe;
import Reika.SatisfactoryPlanner.Data.Objects.Resource;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.Building;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.Building.BuildingCategory;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.CraftingBuilding;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.FunctionalBuilding;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.Generator;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.LogisticBuilding;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.SimpleProductionBuilding;
import Reika.SatisfactoryPlanner.Data.Objects.Buildables.Vehicle;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.ResourceMiner;
import Reika.SatisfactoryPlanner.Data.Objects.ResourceSupplies.TransportLine;
import Reika.SatisfactoryPlanner.Util.CountMap;
import Reika.SatisfactoryPlanner.Util.Errorable.ErrorableWithArgument;
import Reika.SatisfactoryPlanner.Util.JSONUtil;
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

	private static final ArrayList<Milestone> allMilestonesSorted = new ArrayList();
	private static final HashMap<String, Milestone> allMilestones = new HashMap();

	private static final TreeMap<String, File> modList = new TreeMap();

	private static final HashMap<String, ClassType> lookup = new HashMap();

	private static boolean gameJSONFound;

	static {
		for (ClassType t : ClassType.values()) {
			for (String s : t.classTypes)
				lookup.put(s, t);
		}
	}

	public static Milestone lookupMilestone(String name) {
		if (name.endsWith("_C_C"))
			name = name.substring(0, name.length()-2);
		Milestone c = allMilestones.get(name);
		if (c == null)
			throw new IllegalArgumentException("No such milestone '"+name+"'");
		return c;
	}

	public static Building lookupBuilding(String name) {
		if (name.endsWith("_C_C"))
			name = name.substring(0, name.length()-2);
		if (name.startsWith("Desc_"))
			name = "Build_"+name.substring(5);
		Building c = allBuildings.get(name);
		if (c == null)
			throw new IllegalArgumentException("No such building '"+name+"'");
		return c;
	}

	public static Consumable lookupItem(String name) {
		if (name.endsWith("_C_C"))
			name = name.substring(0, name.length()-2);
		Consumable c = allItems.get(name);
		if (c == null)
			throw new IllegalArgumentException("No such item '"+name+"'");
		return c;
	}

	public static Recipe lookupRecipe(String name) {
		if (name.endsWith("_C_C"))
			name = name.substring(0, name.length()-2);
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

	public static Collection<Recipe> getAllRecipes() {
		return Collections.unmodifiableCollection(allRecipes.values());
	}

	public static List<Milestone> getAllMilestones() {
		return Collections.unmodifiableList(allMilestonesSorted);
	}

	public static List<Item> getMineables() {
		return Collections.unmodifiableList(mineableItems);
	}

	public static List<Fluid> getFrackables() {
		return Collections.unmodifiableList(frackableFluids);
	}

	public static boolean areRecipesLoaded() {
		return !allRecipes.isEmpty();
	}

	public static boolean areItemsLoaded() {
		return !allItems.isEmpty();
	}

	public static boolean areMilestonesLoaded() {
		return !allMilestones.isEmpty();
	}

	public static void sort() {
		Logging.instance.log(String.format("Sorting data with %d items, %d recipes, %d building recipes, %d buildings, %d generators, and %d vehicles", allItemsSorted.size(), allAutoRecipesSorted.size(), allBuildingRecipesSorted.size(), allBuildingsSorted.size(), allGeneratorsSorted.size(), allVehiclesSorted.size()));
		Collections.sort(allItemsSorted);
		Collections.sort(allAutoRecipesSorted);
		Collections.sort(allBuildingRecipesSorted);
		Collections.sort(allBuildingsSorted);
		Collections.sort(allGeneratorsSorted);
		Collections.sort(allVehiclesSorted);
		Collections.sort(allMilestonesSorted);
		Collections.sort(mineableItems);
		Collections.sort(frackableFluids);
	}

	public static void parseGameJSON() throws IOException {
		if (gameJSONFound) {
			File f = getGameJSON();
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
	}

	public static void checkForGameJSON() {
		File f = getGameJSON();
		gameJSONFound = f.exists() && f.isFile();
	}

	public static boolean wasGameJSONFound() {
		return gameJSONFound;
	}

	private static File getGameJSON() {
		return new File(Setting.GAMEDIR.getCurrentValue(), "CommunityResources/Docs/en-US.json");
	}

	private static void parseItemsJSON(JSONObject obj, boolean resource) {
		String id = obj.getString("ClassName");
		Logging.instance.log("Parsing JSON elem "+id);
		String form = obj.getString("mForm");
		boolean gas = form.equalsIgnoreCase("RF_GAS") || form.equalsIgnoreCase("RF_PLASMA");
		boolean fluid = form.equalsIgnoreCase("RF_LIQUID") || gas;
		String disp = obj.getString("mDisplayName");
		String desc = obj.getString("mDescription");
		String ico = convertIDToIcon(id);
		if (fluid) {
			String clr = obj.getString(gas ? "mGasColor" : "mFluidColor");
			Fluid f = new Fluid(id, disp, ico, desc, obj.getString("NativeClass"), obj.getFloat("mEnergyValue"), parseColor(clr), gas);
			allItems.put(f.id, f);
			allItemsSorted.add(f);
			if (resource)
				frackableFluids.add(f);
		}
		else {
			Item i = new Item(id, disp, ico, desc, obj.getString("NativeClass"), obj.getFloat("mEnergyValue"), getStackSize(obj), obj.getInt("mResourceSinkPoints"), obj.getFloat("mRadioactiveDecay"));
			allItems.put(i.id, i);
			allItemsSorted.add(i);
			if (resource)
				mineableItems.add(i);
		}
	}

	private static int getStackSize(JSONObject obj) {
		String id = obj.has("StackSize") ? obj.getString("StackSize") : obj.getString("mStackSize");
		switch(id.toLowerCase(Locale.ENGLISH)) {
			case "one":
			case "ss_one":
				return 1;
			case "small":
			case "ss_small":
				return 50;
			case "medium":
			case "ss_medium":
				return 100;
			case "big":
			case "ss_big":
				return 200;
			case "huge":
			case "ss_huge":
				return 500;
			default:
				return 0;
		}
	}

	private static String convertIDToIcon(String id) {
		return id.replace("Build_", "Desc_");//id.substring(id.indexOf('_')+1, id.length()-2);// strip Desc_ and _C //obj.getString("mPersistentBigIcon");
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
		int end = iid.lastIndexOf('"');
		if (end == -1)
			end = iid.length();
		iid = iid.substring(iid.lastIndexOf('/')+1, end);
		iid = iid.substring(iid.lastIndexOf('.')+1);
		if (iid.endsWith("_C'"))
			iid = iid.substring(0, iid.length()-1);
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
		CraftingBuilding b = building == null ? null : (CraftingBuilding)lookupBuilding(building);
		boolean xmas = id.startsWith("Desc_Xmas") || id.startsWith("Recipe_Fireworks") || disp.startsWith("FICSMAS") || obj.getString("mRelevantEvents").contains("EV_Christmas");
		Recipe r = new Recipe(id, disp, b, Float.parseFloat(time), xmas);
		tokenizeUERecipeString(in, (c, amt) -> r.addIngredient(c, amt));
		float varConst = obj.getFloat("mVariablePowerConsumptionConstant");
		if (varConst > 0) {
			float varFac = obj.getFloat("mVariablePowerConsumptionFactor"); //range = factor - const to factor + const over recipe
			r.powerOverride = new LinearIncreasePower(varFac, varConst);
		}
		if (r.productionBuilding == null) { //is a buildable
			String bid = parseID(out.replace("Desc_", "Build_"));
			if (allBuildings.containsKey(bid)) {
				Building bb = lookupBuilding(bid);/*
				for (Entry<Consumable, Integer> e : r.getDirectCost().entrySet()) {
					bb.addIngredient((Item)e.getKey(), e.getValue());
				}*/
				bb.setRecipe(r);
				Logging.instance.log("Set "+bb+" recipe: "+bb.getConstructionCost());
			}
			allBuildingRecipesSorted.add(r);
		}
		else {
			tokenizeUERecipeString(out, (c, amt) -> r.addProduct(c, amt));
			allAutoRecipesSorted.add(r);
		}
		Logging.instance.log("Registered recipe type "+r);
		allRecipes.put(r.id, r);
	}

	private static void tokenizeUERecipeString(String rec, BiConsumer<Consumable, Integer> user) {
		if (Strings.isNullOrEmpty(rec))
			return;
		//old: rec.substring(2, rec.length()-2).split("\\),\\(")
		while (rec.charAt(0) == '(') //trim wrapping parens
			rec = rec.substring(1, rec.length()-1);
		for (String s : rec.split("\\),\\(")) {
			while (s.charAt(0) == '(') //trim wrapping parens
				s = s.substring(1, s.length()-1);
			String[] parts = s.split(",");
			String id = parts[0].split("=")[1];
			if (id.charAt(0) == '"') //trim wrapping "
				id = id.substring(1, id.length()-1);
			int idx1 = id.indexOf('\'');
			int idx2 = id.lastIndexOf('\'');
			if (idx2 > idx1)
				id = id.substring(idx1+1, idx2);
			Consumable c = lookupItem(parseID(id));
			int amt = Integer.parseInt(parts[1].split("=")[1]);
			if (c instanceof Fluid)
				amt /= Constants.LIQUID_SCALAR; //they store fluids in mB
			user.accept(c, amt);
		}
	}

	private static CountMap<Consumable> tokenizeUERecipeString(String rec) {
		CountMap<Consumable> map = new CountMap();
		tokenizeUERecipeString(rec, (item, amt) -> map.set(item, amt));
		return map;
	}
	/*
	private static void parseBuildingJSON(JSONObject obj) {
		String id = obj.getString("ClassName");
		Logging.instance.log("Parsing JSON elem "+id);
		String disp = obj.getString("mDisplayName");
		Building r = new Building(id, disp, convertIDToIcon(id));
		allBuildings.put(r.id, r);
		allBuildingsSorted.add(r);
	}
	 */
	private static void parseFunctionalBuildingJSON(JSONObject obj, BuildingCategory cat) {
		String id = obj.getString("ClassName");
		Logging.instance.log("Parsing JSON elem "+id);
		String disp = obj.getString("mDisplayName");
		float pwr = Float.parseFloat(obj.getString("mPowerConsumption"));
		float pwrExp = Float.parseFloat(obj.getString("mPowerConsumptionExponent")); //FIXME handle this for overclock!
		float smrExp = Float.parseFloat(obj.getString("mProductionBoostPowerConsumptionExponent")); //FIXME handle this for overclock!
		int somerslots = JSONUtil.getInt(obj, "mProductionShardSlotSize", 0);
		FunctionalBuilding r = constructBuilding(cat, id, disp, convertIDToIcon(id), pwr, pwrExp, smrExp, somerslots);
		if (r instanceof SimpleProductionBuilding)
			((SimpleProductionBuilding)r).setDelay(obj.getFloat("mTimeToProduceItem"));
		allBuildings.put(r.id, r);
		allBuildingsSorted.add(r);
	}

	private static FunctionalBuilding constructBuilding(BuildingCategory cat, String id, String disp, String string, float pwr, float pwrExp, float smrExp, int somerslots) {
		switch(cat) {
			case CRAFTER:
				return new CraftingBuilding(id, disp, convertIDToIcon(id), pwr, pwrExp, smrExp, somerslots);
			case SIMPLEPROD:
				return new SimpleProductionBuilding(id, disp, convertIDToIcon(id), pwr, pwrExp, smrExp, somerslots);
			case MINER:
				return new ResourceMiner(id, disp, convertIDToIcon(id), pwr, pwrExp, smrExp, somerslots);
			case LOGISTIC:
				return new LogisticBuilding(id, disp, convertIDToIcon(id), pwr, pwrExp, smrExp, somerslots);
			default:
				throw new IllegalArgumentException("Unrecognized building type: "+cat);
		}
	}

	private static void parseBeltJSON(JSONObject obj) {
		String id = obj.getString("ClassName");
		Logging.instance.log("Parsing JSON elem "+id);
		String disp = obj.getString("mDisplayName");
		String spd = obj.getString("mSpeed"); //TODO is 2x item transfer rate/min
		Building r = new TransportLine(id, disp, convertIDToIcon(id));
		allBuildings.put(r.id, r);
		allBuildingsSorted.add(r);
	}

	private static void parsePipeJSON(JSONObject obj) {
		String id = obj.getString("ClassName");
		Logging.instance.log("Parsing JSON elem "+id);
		String disp = obj.getString("mDisplayName");
		String spd = obj.getString("mFlowLimit"); //TODO is per second, so x60 for /min
		Building r = new TransportLine(id, disp, convertIDToIcon(id));
		allBuildings.put(r.id, r);
		allBuildingsSorted.add(r);
	}

	private static void parseGeneratorJSON(JSONObject obj) {
		String id = obj.getString("ClassName");
		Logging.instance.log("Parsing JSON elem "+id);
		String disp = obj.getString("mDisplayName");
		String pwr = obj.getString("mPowerProduction");
		float suppl = JSONUtil.getFloat(obj, "mSupplementalToPowerRatio", 0);
		JSONArray fuels = JSONUtil.getArray(obj, "mFuel", null);
		Generator r = new Generator(id, disp, convertIDToIcon(id), Float.parseFloat(pwr), suppl);
		if (fuels != null) {
			//String fuelForm = obj.getString("mFuelResourceForm"); removed in 1.0
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
				}/* does not happen anymore with 1.0
				else if (item.startsWith("FGItemDescriptor")) {
					for (Consumable c : Consumable.getForClass(item)) {
						if (c.energyValue > 0 && ((fuelForm.equalsIgnoreCase("RF_SOLID") && c instanceof Item) || (fuelForm.equalsIgnoreCase("RF_FLUID") && c instanceof Fluid))) {
							Fuel f = new Fuel(r, c, secondItem, outItem, outItem == null ? 0 : fuel.getInt("mByproductAmount"));
							r.addFuel(f);
						}
					}
				}*/
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
		//boolean isAltRecipe = id.startsWith("Schematic_Alternate_");
		String disp = obj.getString("mDisplayName");
		int tier = Integer.parseInt(obj.getString("mTechTier"));
		JSONArray unlocks = obj.getJSONArray("mUnlocks");
		JSONArray deps = obj.getJSONArray("mSchematicDependencies");
		Milestone m = new Milestone(id, tier, disp);
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
		if (flag) {
			allMilestones.put(id, m);
			allMilestonesSorted.add(m);
		}
	}

	public static void loadVanillaData() {
		Logging.instance.log("Loading vanilla data");
		ClassType.RESOURCE.parsePending();
		ClassType.ITEM.parsePending();
		Item i = new Item("Desc_HardDrive_C", "Hard Drive", convertIDToIcon("Desc_HardDrive_C"), "", "Hardcoded", 0, 50, 0, 0); //missing from json???
		allItems.put(i.id, i);
		allItemsSorted.add(i);
		ClassType.SIMPLEPROD.parsePending();
		ClassType.CRAFTER.parsePending();
		ClassType.MINER.parsePending();
		ClassType.GENERATOR.parsePending();
		ClassType.STATION.parsePending();
		ClassType.BELT.parsePending();
		ClassType.PIPE.parsePending();
		//ClassType.MISCBUILD.parsePending();
		ClassType.VEHICLE.parsePending();
		ClassType.RECIPE.parsePending();
		ClassType.MILESTONE.parsePending();
	}

	public static void loadCustomData() throws Exception {
		Logging.instance.log("Loading direct custom data");
		File ico = Main.getRelativeFile("CustomDefinitions/Icons");
		if (!ico.exists()) {
			Logging.instance.log("No custom item icon folder exists; creating.");
			ico.mkdirs();
		}
		loadCustomItemFolder(Main.getRelativeFile("CustomDefinitions/Items"), "Custom", false, f -> copyTemplate(f, "item"));
		loadCustomBuildingFolder(Main.getRelativeFile("CustomDefinitions/Buildings"), "Custom", f -> copyTemplate(f, "building"));
		loadCustomMilestoneFolder(Main.getRelativeFile("CustomDefinitions/Milestones"), "Custom", f -> copyTemplate(f, "milestone"));
		loadCustomRecipeFolder(Main.getRelativeFile("CustomDefinitions/Recipes"), "Custom", f -> copyTemplate(f, "recipe"));
	}

	private static void copyTemplate(File f, String name) throws IOException {
		name = "template_"+name+".json";
		File f2 = new File(f, name);
		InputStream from = Main.class.getResourceAsStream("Resources/Examples/"+name);
		FileUtils.copyInputStreamToFile(from, f2);
	}

	public static void loadModdedData() throws Exception {
		if (!Main.getModsFolder().exists())
			return;
		Logging.instance.log("Loading mod data");
		for (File mod : Main.getModsFolder().listFiles()) {
			if (mod.isDirectory()) {
				String name = mod.getName();
				if (name.startsWith(".git"))
					continue;
				Logging.instance.log("Checking mod "+name);
				File f = new File(mod, "ContentLib");
				if (f.exists()) {
					loadCustomItemFolder(new File(f, "Items"), name, true, null);
					//loadCustomBuildingFolder(new File(f, "CustomBuildings"), name, null);
					loadCustomMilestoneFolder(new File(f, "Schematics"), name, null);
					loadCustomRecipeFolder(new File(f, "Recipes"), name, null);
				}
				else {
					Logging.instance.log("No ContentLib. Skipping.");
					f = null;
				}
				modList.put(name, f);
			}
		}
	}

	private static void loadCustomBuildingFolder(File f, String mod, ErrorableWithArgument<File> createTemplate) throws Exception {
		if (!f.exists()) {
			if (createTemplate != null) {
				Logging.instance.log("No custom building folder exists, creating templates.");
				f.mkdirs();
				createTemplate.run(f);
			}
			return;
		}
		Logging.instance.log("Loading buildings from "+f.getCanonicalPath());
		for (File f2 : f.listFiles()) {
			if (f2.getName().endsWith(".json") && !f2.getName().startsWith("template")) {
				try {
					Logging.instance.log("Loading building file "+f2);
					JSONObject data = new JSONObject(FileUtils.readFileToString(f2, Charsets.UTF_8));
					int somerslots = JSONUtil.getInt(data, "SomersloopSlots", 0);
					String type = JSONUtil.getString(data, "BuildingType", BuildingCategory.CRAFTER.name());
					float pwrExp = JSONUtil.getFloat(data, "OverclockingPowerExponent", Constants.DEFAULT_OVERCLOCK_EXPONENT);
					float smrExp = JSONUtil.getFloat(data, "SomersloopPowerExponent", Constants.DEFAULT_SOMERSLOOP_EXPONENT);
					FunctionalBuilding r = constructBuilding(BuildingCategory.valueOf("type"), data.getString("ID"), data.getString("Name"), data.getString("Icon"), data.getInt("PowerCost"), pwrExp, smrExp, somerslots);
					if (r instanceof SimpleProductionBuilding)
						((SimpleProductionBuilding)r).setDelay(data.getFloat("ProductionInterval"));
					/*JSONArray ing = data.getJSONArray("Ingredients");
				for (Object o : ing) {
					JSONObject inner = (JSONObject)o;
					r.addIngredient((Item)lookupItem(inner.getString("Item")), inner.getInt("Amount"));
				}
				if (data.has("tier"))
					r.addMilestone(data.getInt("tier"));
					 */
					String rec = data.getString("recipe");
					r.setRecipe(lookupRecipe(rec));
					Logging.instance.log("Loaded custom building "+r);
					allBuildings.put(r.id, r);
					allBuildingsSorted.add(r);
				}
				catch (Exception e) {
					Logging.instance.log("Failed to parse custom building definition file "+f2.getAbsolutePath());
					Logging.instance.log(e);
				}
			}
		}
	}

	private static void loadCustomMilestoneFolder(File f, String mod, ErrorableWithArgument<File> createTemplate) throws Exception {
		if (!f.exists()) {
			if (createTemplate != null) {
				Logging.instance.log("No custom milestone folder exists, creating templates.");
				f.mkdirs();
				createTemplate.run(f);
			}
			return;
		}
		Logging.instance.log("Loading milestones from "+f.getCanonicalPath());
		for (File f2 : f.listFiles()) {
			if (f2.getName().endsWith(".json") && !f2.getName().startsWith("template")) {
				try {
					Logging.instance.log("Loading milestone file "+f2);
					JSONObject obj = new JSONObject(FileUtils.readFileToString(f2, Charsets.UTF_8));

					String id = JSONUtil.getString(obj, "ID", f2.getName().replace("Schematic_", ""));
					Milestone m = new Milestone(id, obj.getInt("Tier"), obj.getString("Name"));
					//TODO "Cost" field?
					if (obj.has("DependsOn")) {
						for (Object o : obj.getJSONArray("DependsOn")) {
							String ulock = (String)o;
							if (allMilestones.containsKey(ulock)) {
								m.addDependency(lookupMilestone(ulock));
							}
						}
					}
					for (Object o : obj.getJSONArray("Recipes")) {
						String ulock = (String)o;
						if (allRecipes.containsKey(ulock))
							m.addRecipe(lookupRecipe(ulock));
					}
					allMilestones.put(id, m);
					allMilestonesSorted.add(m);
				}
				catch (Exception e) {
					Logging.instance.log("Failed to parse custom milestone definition file "+f2.getAbsolutePath());
					Logging.instance.log(e);
				}
			}
		}
	}

	private static void loadCustomRecipeFolder(File f, String mod, ErrorableWithArgument<File> createTemplate) throws Exception {
		if (!f.exists()) {
			if (createTemplate != null) {
				Logging.instance.log("No custom recipe folder exists, creating templates.");
				f.mkdirs();
				createTemplate.run(f);
			}
			return;
		}
		Logging.instance.log("Loading recipes from "+f.getCanonicalPath());
		for (File f2 : f.listFiles()) {
			if (f2.getName().endsWith(".json") && !f2.getName().startsWith("template")) {
				try {
					Recipe r = parseCustomRecipeFile(f2, mod, true);
					allAutoRecipesSorted.add(r);
					allRecipes.put(r.id, r);
					Logging.instance.log("Registered custom recipe type "+r);
				}
				catch (Exception e) {
					Logging.instance.log("Failed to parse custom recipe definition file "+f2.getAbsolutePath());
					Logging.instance.log(e);
				}
			}
		}
	}

	public static Recipe parseCustomRecipeFile(File f2, String mod, boolean allowDelta) throws Exception {
		Logging.instance.log("Loading recipe file "+f2);
		JSONObject obj = new JSONObject(FileUtils.readFileToString(f2, Charsets.UTF_8));
		String disp = obj.getString("Name");
		String id = JSONUtil.getString(obj, "ID", f2.getName().replace("Recipe_", ""));
		Recipe old = allRecipes.get(id);
		boolean isDelta = false;
		if (old != null && allowDelta) {
			isDelta = JSONUtil.getBoolean(obj, "Delta");
			if (isDelta)
				Logging.instance.log("Found a custom recipe definition for ID '"+id+"', marked as a delta to "+old+".");
			else
				Logging.instance.log("Found a custom recipe definition for ID '"+id+"', which is already mapped to "+old+". It will be replaced.");
			allAutoRecipesSorted.remove(old);
		}
		String build = obj.has("ProducedIn") ? obj.getJSONArray("ProducedIn").getString(0)+"_C" : null;
		CraftingBuilding crafter = build == null ? null : (CraftingBuilding)lookupBuilding(build);
		if (!isDelta) {
			if (crafter == null)
				throw new IllegalArgumentException("Invalid recipe definition - is not a delta but lacks a building");
		}
		Recipe r = isDelta ? old : new Recipe(id, disp, crafter, obj.getFloat("ManufacturingDuration"), false);
		if (isDelta) {
			if (obj.has("ManufacturingDuration"))
				r = new Recipe(r.id, r.displayName, r.productionBuilding, obj.getFloat("ManufacturingDuration"), r.isFicsmas);
			if (crafter != null)
				r = new Recipe(r.id, r.displayName, crafter, r.craftingTime, r.isFicsmas);
		}
		else {
			r.markModded(mod);
		}
		JSONArray ing = JSONUtil.getArray(obj, "Ingredients", null);
		if (ing == null && !isDelta)
			throw new IllegalArgumentException("Invalid recipe definition - is not a delta but lacks ingredients");
		if (ing != null && isDelta)
			r.clearIngredients();
		for (Object o : ing) {
			JSONObject inner = (JSONObject)o;
			Consumable c = lookupItem(inner.getString("Item")+"_C");
			int amt = inner.getInt("Amount");
			if (c instanceof Fluid)
				amt /= Constants.LIQUID_SCALAR;
			r.addIngredient(c, amt);
		}
		JSONArray prod = JSONUtil.getArray(obj, "Products", null);
		if (prod == null && !isDelta)
			throw new IllegalArgumentException("Invalid recipe definition - is not a delta but lacks products");
		if (prod != null && isDelta)
			r.clearProducts();
		for (Object o : prod) {
			JSONObject inner = (JSONObject)o;
			Consumable c = lookupItem(inner.getString("Item")+"_C");
			int amt = inner.getInt("Amount");
			if (c instanceof Fluid)
				amt /= Constants.LIQUID_SCALAR;
			r.addProduct(c, amt);
		}
		if (obj.has("UnlockedBy")) {
			JSONArray milestones = obj.getJSONArray("UnlockedBy");
			for (int i = 0; i < milestones.length(); i++) {
				String name = milestones.getString(i);
				if (allMilestones.containsKey(name)) {
					r.addMilestone(lookupMilestone(name));
				}
				else {
					name = name+"_C";
					if (allMilestones.containsKey(name))
						r.addMilestone(lookupMilestone(name));
				}
			}
		}
		return r;
	}

	private static void loadCustomItemFolder(File f0, String mod, boolean contentLib, ErrorableWithArgument<File> createTemplate) throws Exception {
		if (!f0.exists()) {
			if (createTemplate != null) {
				Logging.instance.log("No custom item folder exists, creating templates.");
				f0.mkdirs();
				createTemplate.run(f0);
			}
			return;
		}
		Logging.instance.log("Loading items from "+f0.getCanonicalPath());
		for (File f2 : f0.listFiles()) {
			if (f2.getName().endsWith(".json") && !f2.getName().startsWith("template")) {
				try {
					JSONObject obj = new JSONObject(FileUtils.readFileToString(f2, Charsets.UTF_8));
					String form = JSONUtil.getString(obj, "Form", "Solid");
					boolean gas = form.equalsIgnoreCase("gas") || form.equalsIgnoreCase("plasma");
					boolean fluid = form.equalsIgnoreCase("liquid") || gas;
					String disp = obj.getString("Name");
					String desc = obj.getString("Description");
					String id = JSONUtil.getString(obj, "ID", f2.getName().replace("Item_", "").replace(".json", ""))+"_C";
					if (id.endsWith("_C_C"))
						id = id.substring(0, id.length()-2);
					String icon;
					File iconFolder = null;
					if (contentLib) {
						String visName = JSONUtil.getString(obj, "VisualKit", f2.getName().replace(".json", ""));
						File visKit = new File(f0.getParentFile(), "VisualKits/"+visName+".json");
						if (visKit.exists()) {
							JSONObject vis = new JSONObject(FileUtils.readFileToString(visKit, Charsets.UTF_8));
							icon = vis.getString("BigIcon");
							iconFolder = new File(f0.getParentFile(), "Icons");
						}
						else {
							icon = null;
						}
					}
					else {
						icon = JSONUtil.getString(obj, "Icon", id);
						iconFolder = Main.getRelativeFile("CustomDefinitions/Icons");
					}
					String cat = obj.getString("Category");
					float nrg = JSONUtil.getFloat(obj, "EnergyValue", 0);
					boolean resource = obj.has("ResourceItem");
					if (fluid) {
						String clr = JSONUtil.getString(obj, "Color", null);
						Fluid f = new Fluid(id, disp, icon, desc, cat, nrg, clr == null ? Color.BLACK : parseColor(clr), gas);
						f.markModded(mod);
						f.setIconFolder(iconFolder);
						allItems.put(f.id, f);
						allItemsSorted.add(f);
						if (resource)
							frackableFluids.add(f);
					}
					else {
						Item i = new Item(id, disp, icon, desc, cat, nrg, getStackSize(obj), JSONUtil.getInt(obj, "ResourceSinkPoints", 0), JSONUtil.getFloat(obj, "RadioactiveDecay", 0));
						i.markModded(mod);
						i.setIconFolder(iconFolder);
						allItems.put(i.id, i);
						allItemsSorted.add(i);
						if (resource)
							mineableItems.add(i);
					}
				}
				catch (Exception e) {
					Logging.instance.log("Failed to parse custom item definition file "+f2.getAbsolutePath());
					Logging.instance.log(e);
				}
			}
		}
	}

	public static File exportCustomRecipe(Recipe r, String mod, Function<File, Boolean> alreadyExistsHandler) throws Exception {
		File dir = Main.getRelativeFile("CustomDefinitions/Recipes");
		if (!Strings.isNullOrEmpty(mod) && modList.containsKey(mod))
			dir = new File(Main.getModsFolder(), mod+"/ContentLib/Recipes");
		dir.mkdirs();
		return exportCustomRecipe(r, new File(dir, r.id+".json"), alreadyExistsHandler);
	}

	public static File exportCustomRecipe(Recipe r, File f, Function<File, Boolean> alreadyExistsHandler) throws Exception {
		if (f.exists() && !alreadyExistsHandler.apply(f))
			return null;
		JSONObject root = new JSONObject();
		JSONArray ingredients = new JSONArray();
		JSONArray products = new JSONArray();
		JSONArray milestones = new JSONArray();
		root.put("ID", r.id);
		root.put("Name", r.displayName);
		root.put("ManufacturingDuration", r.craftingTime);
		JSONArray arr = new JSONArray();
		arr.put(r.productionBuilding.id.replaceAll("_C$", ""));
		root.put("ProducedIn", arr);

		for (Entry<Consumable, Integer> e : r.getDirectCost().entrySet()) {
			JSONObject block = new JSONObject();
			Consumable c = e.getKey();
			block.put("Item", c.id.replaceAll("_C$", ""));
			int amt = e.getValue();
			if (c instanceof Fluid)
				amt *= Constants.LIQUID_SCALAR;
			block.put("Amount", amt);
			ingredients.put(block);
		}
		root.put("Ingredients", ingredients);

		for (Entry<Consumable, Integer> e : r.getDirectProducts().entrySet()) {
			JSONObject block = new JSONObject();
			Consumable c = e.getKey();
			block.put("Item", c.id.replaceAll("_C$", ""));
			int amt = e.getValue();
			if (c instanceof Fluid)
				amt *= Constants.LIQUID_SCALAR;
			block.put("Amount", amt);
			products.put(block);
		}
		root.put("Products", products);

		for (Milestone m : r.getMilestones())
			milestones.put(m.id.replaceAll("_C$", ""));
		root.put("UnlockedBy", milestones);

		JSONUtil.saveFile(f, root);

		Main.parseGameData();
		return f;
	}

	public static void clear() {
		gameJSONFound = false;
		allBuildings.clear();
		allBuildingsSorted.clear();
		allGeneratorsSorted.clear();
		allItems.clear();
		allItemsSorted.clear();
		allAutoRecipesSorted.clear();
		allBuildingRecipesSorted.clear();
		allRecipes.clear();
		allVehicles.clear();
		allVehiclesSorted.clear();
		mineableItems.clear();
		frackableFluids.clear();
		allMilestones.clear();
		allMilestonesSorted.clear();
		modList.clear();
		Milestone.resetTiers();
		Resource.resetIconCheck();
		//Logging.instance.log(String.format("Cleared data with %d items, %d recipes, %d building recipes, %d buildings, %d generators, and %d vehicles", allItemsSorted.size(), allAutoRecipesSorted.size(), allBuildingRecipesSorted.size(), allBuildingsSorted.size(), allGeneratorsSorted.size(), allVehiclesSorted.size()));
	}

	public static Set<String> getModList() {
		return Collections.unmodifiableSet(modList.keySet());
	}

	public static File getContentLibFolder(String mod) {
		return modList.get(mod);
	}

	public static enum ClassType {
		ITEM("/Script/CoreUObject.Class'/Script/FactoryGame.FGItemDescriptor'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGItemDescriptorBiomass'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGItemDescriptorNuclearFuel'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGEquipmentDescriptor'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGConsumableDescriptor'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGAmmoTypeInstantHit'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGAmmoTypeProjectile'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGAmmoTypeSpreadshot'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGPowerShardDescriptor'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGItemDescriptorPowerBoosterFuel'"),
		RESOURCE("/Script/CoreUObject.Class'/Script/FactoryGame.FGResourceDescriptor'"),
		RECIPE("/Script/CoreUObject.Class'/Script/FactoryGame.FGRecipe'"),
		GENERATOR("/Script/CoreUObject.Class'/Script/FactoryGame.FGBuildableGeneratorFuel'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGBuildableGeneratorNuclear'"/*, "/Script/CoreUObject.Class'/Script/FactoryGame.FGBuildableGeneratorGeoThermal'"*/),
		CRAFTER("/Script/CoreUObject.Class'/Script/FactoryGame.FGBuildableManufacturer'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGBuildableManufacturerVariablePower'"),
		SIMPLEPROD("/Script/CoreUObject.Class'/Script/FactoryGame.FGBuildableFactorySimpleProducer'"),
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
			pendingParses.clear();
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
					parseFunctionalBuildingJSON(obj, BuildingCategory.CRAFTER);
					break;
				case SIMPLEPROD:
					parseFunctionalBuildingJSON(obj, BuildingCategory.SIMPLEPROD);
					break;
				case MINER:
					parseFunctionalBuildingJSON(obj, BuildingCategory.MINER);
					break;
				case STATION:
					parseFunctionalBuildingJSON(obj, BuildingCategory.LOGISTIC);
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