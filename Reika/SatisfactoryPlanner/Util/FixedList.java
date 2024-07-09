package Reika.SatisfactoryPlanner.Util;

import java.util.Collection;
import java.util.LinkedList;

public class FixedList<E> extends LinkedList<E> {

	public final int limit;

	public FixedList(int amt) {
		limit = amt;
	}

	@Override
	public void addLast(E obj) {
		super.addLast(obj);

		while (this.size() > limit)
			this.removeFirst();
	}

	@Override
	public boolean add(E obj) {
		this.addLast(obj);
		return true;
	}

	@Override
	public void add(int idx, E obj) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int idx, Collection<? extends E> obj) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends E> obj) {
		for (E e : obj)
			this.add(e);
		return true;
	}

}
