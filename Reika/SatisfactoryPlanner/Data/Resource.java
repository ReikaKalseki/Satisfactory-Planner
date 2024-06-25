package Reika.SatisfactoryPlanner.Data;

import java.io.InputStream;

import Reika.SatisfactoryPlanner.Main;

public abstract class Resource {

	public final String name;
	public final String iconName;

	protected Resource(String n, String img) {
		name = n;
		iconName = img;
	}

	@Override
	public String toString() {
		return name+" ["+this.getClass().getName()+"]";
	}

	public InputStream getIcon() {
		return Main.class.getResourceAsStream("Resources/Graphics/Icons/"+this.getIconFolder()+"/"+iconName+".png");
	}

	protected abstract String getIconFolder();

}
