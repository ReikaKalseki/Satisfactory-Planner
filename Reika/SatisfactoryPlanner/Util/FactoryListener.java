package Reika.SatisfactoryPlanner.Util;

import Reika.SatisfactoryPlanner.Data.Factory;

public interface FactoryListener {

	public void setFactory(Factory f);

	public void onContentsChange();

	public void onFileChange();

}
