package Reika.SatisfactoryPlanner.Util;

@FunctionalInterface
public interface Errorable {

	public void run() throws Exception;
	//public String getErrorBrief(Exception t);

}