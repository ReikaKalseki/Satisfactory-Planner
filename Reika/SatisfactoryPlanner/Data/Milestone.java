package Reika.SatisfactoryPlanner.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Reika.SatisfactoryPlanner.Util.Logging;

public class Milestone {

	private final ArrayList<Milestone> dependencies = new ArrayList();
	private final ArrayList<Recipe> associatedRecipes = new ArrayList();

	private int tier = 0;

	public final String displayName;

	private static int maxTier;

	public Milestone(int t, String dis) {
		tier = t;
		displayName = dis;
		Logging.instance.log("Registered milestone "+this);
	}

	public Milestone addRecipe(Recipe r) {
		Logging.instance.log("Added recipe "+r+" to milestone "+this);
		associatedRecipes.add(r);
		r.addMilestone(this);
		return this;
	}

	public Milestone addDependency(Milestone r) {
		dependencies.add(r);
		tier = Math.max(tier, r.tier);
		maxTier = Math.max(maxTier, tier);
		return this;
	}

	@Override
	public String toString() {
		return "T"+tier+": "+displayName;
	}

	public int getTier() {
		return tier;
	}

	public List<Recipe> getRecipes() {
		return Collections.unmodifiableList(associatedRecipes);
	}

	public static int getMaxTier() {
		return maxTier;
	}

	public static void resetTiers() {
		maxTier = 0;
	}

}
