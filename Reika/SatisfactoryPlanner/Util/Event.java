package Reika.SatisfactoryPlanner.Util;


import java.util.ArrayList;
import java.util.function.Consumer;

@Deprecated
public class Event<A> {

	private ArrayList<Consumer<A>> listeners = new ArrayList();

	public void addListener(Consumer<A> c) {
		if (!listeners.contains(c))
			listeners.add(c);
	}

	public void fire(A arg) {
		listeners.forEach(x -> x.accept(arg));
	}
}