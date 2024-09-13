package Reika.SatisfactoryPlanner.Data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Setting;
import Reika.SatisfactoryPlanner.Data.PowerOverride.LinearIncreasePower;
import Reika.SatisfactoryPlanner.Util.CountMap;
import Reika.SatisfactoryPlanner.Util.Errorable.ErrorableWithArgument;
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

	private static boolean gameJSONFound;

	static {
		for (ClassType t : ClassType.values()) {
			for (String s : t.classTypes)
				lookup.put(s, t);
		}
	}

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

	public static void sort() {
		Logging.instance.log(String.format("Sorting data with %d items, %d recipes, %d building recipes, %d buildings, %d generators, and %d vehicles", allItemsSorted.size(), allAutoRecipesSorted.size(), allBuildingRecipesSorted.size(), allBuildingsSorted.size(), allGeneratorsSorted.size(), allVehiclesSorted.size()));
		Collections.sort(allItemsSorted);
		Collections.sort(allAutoRecipesSorted);
		Collections.sort(allBuildingRecipesSorted);
		Collections.sort(allBuildingsSorted);
		Collections.sort(allGeneratorsSorted);
		Collections.sort(allVehiclesSorted);
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
		FunctionalBuilding b = building == null ? null : (FunctionalBuilding)lookupBuilding(building);
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

	public static void loadVanillaData() {
		Logging.instance.log("Loading vanilla data");
		ClassType.RESOURCE.parsePending();
		ClassType.ITEM.parsePending();
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
		loadCustomItemFolder(Main.getRelativeFile("CustomDefinitions/Items"), "Custom", f -> copyTemplate(f, "item"));
		loadCustomBuildingFolder(Main.getRelativeFile("CustomDefinitions/Buildings"), "Custom", f -> copyTemplate(f, "building"));
		loadCustomRecipeFolder(Main.getRelativeFile("CustomDefinitions/Recipes"), "Custom", f -> copyTemplate(f, "recipe"));
		loadCustomMilestoneFolder(Main.getRelativeFile("CustomDefinitions/Milestones"), "Custom", f -> copyTemplate(f, "milestone"));
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
			String name = mod.getName();
			Logging.instance.log("Checking mod "+name);
			File f = new File(mod, "ContentLib");
			if (f.exists()) {
				loadCustomItemFolder(new File(f, "Items"), name, null);
				//loadCustomBuildingFolder(new File(f, "CustomBuildings"), name, null);
				loadCustomRecipeFolder(new File(f, "Recipes"), name, null);
				loadCustomMilestoneFolder(new File(f, "Schematics"), name, null);
			}
			else {
				Logging.instance.log("No ContentLib. Skipping.");
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
					FunctionalBuilding r = new FunctionalBuilding(data.getString("ID"), data.getString("Name"), data.getString("Icon"), data.getInt("PowerCost"));
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
					e.printStackTrace();
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

					Milestone m = new Milestone(obj.getInt("Tier"), obj.getString("Name"));
					//TODO "Cost" field?
					if (obj.has("DependsOn")) {
						for (Object o : obj.getJSONArray("DependsOn")) {
							String ulock = (String)o;
							if (allMilestones.containsKey(ulock)) {
								m.addDependency(lookupMilestone(ulock));
							}
						}
					}
					boolean flag = false;
					for (Object o : obj.getJSONArray("Recipes")) {
						String ulock = (String)o;
						if (allRecipes.containsKey(ulock)) {
							flag = true;
							m.addRecipe(lookupRecipe(ulock));
						}
					}
					if (flag)
						allMilestones.put(obj.getString("ID"), m);
				}
				catch (Exception e) {
					Logging.instance.log("Failed to parse custom milestone definition file "+f2.getAbsolutePath());
					e.printStackTrace();
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
					/* fuck modular java
				JsonObject data = new Gson().fromJson(new BufferedReader(new FileReader(f2)), JsonObject.class);
				boolean alt = data.has("alternate") && data.get("alternate").getAsBoolean();
				Recipe r = new Recipe(data.get("name").getAsString(), alt);
				for (Entry<String, JsonElement> e : data.get("ingredients").getAsJsonObject().entrySet()) {
					r.addIngredient(lookupItem(e.getKey()), e.getValue().getAsInt());
				}
				for (Entry<String, JsonElement> e : data.get("products").getAsJsonObject().entrySet()) {
					r.addProduct(lookupItem(e.getKey()), e.getValue().getAsInt());
				}*/
					Logging.instance.log("Loading recipe file "+f2);
					JSONObject obj = new JSONObject(FileUtils.readFileToString(f2, Charsets.UTF_8));
					String disp = obj.getString("Name");
					String id = obj.has("ID") ? obj.getString("ID") : f2.getName().replace("Recipe_", "");
					Recipe old = allRecipes.get(id);
					boolean isDelta = false;
					if (old != null) {
						isDelta = obj.has("Delta") && obj.getBoolean("Delta");
						if (isDelta)
							Logging.instance.log("Found a custom recipe definition for ID '"+id+"', marked as a delta to "+old+".");
						else
							Logging.instance.log("Found a custom recipe definition for ID '"+id+"', which is already mapped to "+old+". It will be replaced.");
						allAutoRecipesSorted.remove(old);
					}
					String build = obj.has("ProducedIn") ? obj.getJSONArray("ProducedIn").getString(0)+"_C" : null;
					FunctionalBuilding crafter = build == null ? null : (FunctionalBuilding)lookupBuilding(build);
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
					JSONArray ing = obj.has("Ingredients") ? obj.getJSONArray("Ingredients") : null;
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
					JSONArray prod = obj.has("Products") ? obj.getJSONArray("Products") : null;
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
							String name = milestones.getString(i)+"_C";
							if (allMilestones.containsKey(name))
								r.addMilestone(lookupMilestone(name));
						}
					}
					allAutoRecipesSorted.add(r);
					allRecipes.put(r.id, r);
					Logging.instance.log("Registered custom recipe type "+r);
				}
				catch (Exception e) {
					Logging.instance.log("Failed to parse custom recipe definition file "+f2.getAbsolutePath());
					e.printStackTrace();
				}
			}
		}
	}

	private static void loadCustomItemFolder(File f0, String mod, ErrorableWithArgument<File> createTemplate) throws Exception {
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
					String form = obj.getString("Form");
					boolean gas = form.equalsIgnoreCase("gas") || form.equalsIgnoreCase("plasma");
					boolean fluid = form.equalsIgnoreCase("liquid") || gas;
					String disp = obj.getString("Name");
					String desc = obj.getString("Description");
					String id = obj.has("ID") ? obj.getString("ID") : f2.getName().replace("Item_", "");
					String icon = obj.has("Icon") ? obj.getString("Icon") : id;
					String cat = obj.getString("Category");
					float nrg = obj.getFloat("EnergyValue");
					boolean resource = obj.has("ResourceItem");
					if (fluid) {
						String clr = obj.has("Color") ? obj.getString("Color") : null;
						Fluid f = new Fluid(id, disp, icon, desc, cat, nrg, clr == null ? Color.BLACK : parseColor(clr));
						f.markModded(mod);
						allItems.put(f.id, f);
						allItemsSorted.add(f);
						if (resource)
							frackableFluids.add(f);
					}
					else {
						Item i = new Item(id, disp, icon, desc, cat, nrg);
						i.markModded(mod);
						allItems.put(i.id, i);
						allItemsSorted.add(i);
						if (resource)
							mineableItems.add(i);
					}
				}
				catch (Exception e) {
					Logging.instance.log("Failed to parse custom item definition file "+f2.getAbsolutePath());
					e.printStackTrace();
				}
			}
		}
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
		Milestone.resetTiers();
		Resource.resetIconCheck();
		//Logging.instance.log(String.format("Cleared data with %d items, %d recipes, %d building recipes, %d buildings, %d generators, and %d vehicles", allItemsSorted.size(), allAutoRecipesSorted.size(), allBuildingRecipesSorted.size(), allBuildingsSorted.size(), allGeneratorsSorted.size(), allVehiclesSorted.size()));
	}

	public static enum ClassType {
		ITEM("/Script/CoreUObject.Class'/Script/FactoryGame.FGItemDescriptor'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGItemDescriptorBiomass'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGItemDescriptorNuclearFuel'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGEquipmentDescriptor'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGConsumableDescriptor'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGAmmoTypeInstantHit'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGAmmoTypeProjectile'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGAmmoTypeSpreadshot'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGPowerShardDescriptor'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGItemDescriptorPowerBoosterFuel'"),
		RESOURCE("/Script/CoreUObject.Class'/Script/FactoryGame.FGResourceDescriptor'"),
		RECIPE("/Script/CoreUObject.Class'/Script/FactoryGame.FGRecipe'"),
		GENERATOR("/Script/CoreUObject.Class'/Script/FactoryGame.FGBuildableGeneratorFuel'", "/Script/CoreUObject.Class'/Script/FactoryGame.FGBuildableGeneratorNuclear'"/*, "/Script/CoreUObject.Class'/Script/FactoryGame.FGBuildableGeneratorGeoThermal'"*/),
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