package Reika.SatisfactoryPlanner.Util;

@FunctionalInterface
public interface Errorable {

	public void run() throws Exception;
	//public String getErrorBrief(Exception t);

	@FunctionalInterface
	public static interface ErrorableWithArgument<A> {

		public void run(A arg) throws Exception;
		//public String getErrorBrief(Exception t);

	}

}