package Reika.SatisfactoryPlanner.Util;

import java.util.HashMap;

public class BitflagMap<V, E extends Enum> {

	private final HashMap<Integer, V> data = new HashMap();
	/*
	public BitflagMap(Class<E> flagEnum) {
		this(flagEnum.getEnumConstants());
	}

	public BitflagMap(E[] flagEnum) {

	}
	 */
	private int makeFlags(E... flags) {
		int ret = 0;
		for (E e : flags) {
			ret |= (1 << (e.ordinal()));
		}
		return ret;
	}

	public V get(E... flags) {
		return data.get(this.makeFlags(flags));
	}

	public V put(V val, E... flags) {
		return data.put(this.makeFlags(flags), val);
	}

	public void clear() {
		data.clear();
	}

	@Override
	public String toString() {
		return data.toString();
	}

}
