package Reika.SatisfactoryPlanner.Data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import Reika.SatisfactoryPlanner.Main;
import Reika.SatisfactoryPlanner.Util.Logging;

public class Database {

	private static final HashMap<String, Building> allBuildings = new HashMap();
	private static final ArrayList<Building> allBuildingsSorted = new ArrayList();

	private static final HashMap<String, Consumable> allItems = new HashMap();
	private static final ArrayList<Consumable> allItemsSorted = new ArrayList();

	private static final ArrayList<Recipe> allRecipesSorted = new ArrayList();
	private static final HashMap<String, Recipe> allRecipes = new HashMap();

	private static final ArrayList<Item> mineableItems = new ArrayList();
	private static final ArrayList<Fluid> frackableFluids = new ArrayList();

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
				allBuildings.put(r.name, r);
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
				}*/
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
			allItems.put(i.name, i);
			allItemsSorted.add(i);
			if (Boolean.parseBoolean(def.data.get("mineable")))
				mineableItems.add(i);
			Logging.instance.log("Loaded item "+i);
		}
		for (File f2 : new File(f, "Fluid").listFiles()) {
			DefFile def = new DefFile(f2);
			Fluid i = new Fluid(def.name, def.data.get("icon"));
			allItems.put(i.name, i);
			allItemsSorted.add(i);
			if (Boolean.parseBoolean(def.data.get("frackable")))
				frackableFluids.add(i);
			Logging.instance.log("Loaded fluid "+i);
		}
		Collections.sort(allItemsSorted);
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

	public static List<Building> getAllBuildings() {
		return Collections.unmodifiableList(allBuildingsSorted);
	}

	public static List<Consumable> getAllItems() {
		return Collections.unmodifiableList(allItemsSorted);
	}

	public static List<Recipe> getAllRecipes() {
		return Collections.unmodifiableList(allRecipesSorted);
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

}
