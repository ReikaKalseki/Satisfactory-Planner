/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.SatisfactoryPlanner.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class JavaUtil {

	private static final ExecutorService threader = Executors.newFixedThreadPool(4);

	public static final Comparator<Comparable> reverseComparator = new Comparator<Comparable>() {

		@Override
		public int compare(Comparable o1, Comparable o2) {
			return o2.compareTo(o1);
		}

	};

	/** A complement to Java's built-in List-to-Array. Args: Array of any object (ints, strings, etc). */
	public static <E> HashSet<E> makeSetFromArray(E[] obj) {
		HashSet li = new HashSet();
		for (int i = 0; i < obj.length; i++) {
			li.add(obj[i]);
		}
		return li;
	}

	/** A complement to Java's built-in List-to-Array. Args: Array of any object (ints, strings, etc). */
	public static <E> ArrayList makeListFromArray(E[] obj) {
		ArrayList<E> li = new ArrayList();
		for (int i = 0; i < obj.length; i++) {
			li.add(obj[i]);
		}
		return li;
	}

	public static ArrayList<Integer> makeIntListFromArray(int... obj) {
		ArrayList li = new ArrayList();
		for (int i = 0; i < obj.length; i++) {
			li.add(obj[i]);
		}
		return li;
	}

	public static ArrayList<Byte> makeIntListFromArray(byte... obj) {
		ArrayList li = new ArrayList();
		for (int i = 0; i < obj.length; i++) {
			li.add(obj[i]);
		}
		return li;
	}

	public static <E> ArrayList<E> makeListFrom(E obj) {
		ArrayList<E> li = new ArrayList();
		li.add(obj);
		return li;
	}

	public static <E> ArrayList makeListFrom(E... obj) {
		ArrayList<E> li = new ArrayList();
		for (int i = 0; i < obj.length; i++) {
			li.add(obj[i]);
		}
		return li;
	}

	public static <T, E> T getHashMapKeyByValue(HashMap<T,E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (value.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	public static <K,T> boolean collectionMapContainsValue(Map<K, Collection<T>> map, T value) {
		for (Collection<T> c : map.values()) {
			if (c != null && c.contains(value))
				return true;
		}
		return false;
	}

	public static <E> E getRandomListEntry(Random rand, List<E> li) {
		return li.isEmpty() ? null : li.get(rand.nextInt(li.size()));
	}

	public static <E> E getRandomCollectionEntry(Random rand, Collection<E> c) {
		return c.isEmpty() ? null : c instanceof List ? getRandomListEntry(rand, (List<E>)c) : getRandomListEntry(rand, new ArrayList<E>(c));
	}

	public static void cycleList(List li, int n) {
		if (li.isEmpty())
			return;
		boolean neg = n < 0;
		n = Math.abs(n);
		for (int i = 0; i < n; i++) {
			if (neg) {
				Object o = li.remove(0);
				li.add(o);
			}
			else {
				Object o = li.remove(li.size()-1);
				li.add(0, o);
			}
		}
	}

	public static void cycleLinkedList(LinkedList li, int n) {
		if (li.isEmpty())
			return;
		boolean neg = n < 0;
		n = Math.abs(n);
		for (int i = 0; i < n; i++) {
			if (neg) {
				Object o = li.removeFirst();
				li.addLast(o);
			}
			else {
				Object o = li.removeLast();
				li.addFirst(o);
			}
		}
	}

	public static <V> Collection<V> combineCollections(Collection<V>... colls) {
		Collection<V> ret = new ArrayList();
		for (int i = 0; i < colls.length; i++) {
			ret.addAll(colls[i]);
		}
		return ret;
	}

	public static int[] splitLong(long val) {
		int l1 = (int)(val >>> 32);
		int l2 = (int)(val & 0xFFFFFFFFL);
		return new int[]{l1, l2};
	}

	public static long buildLong(int l1, int l2) {
		return ((long)l1 << 32) | (l2 & 0xffffffffL);
	}

	public static byte[] splitInt(int val) {
		byte[] ret = new byte[4];
		ret[0] = (byte)((val) & 255);
		ret[1] = (byte)((val >>> 8) & 255);
		ret[2] = (byte)((val >>> 16) & 255);
		ret[3] = (byte)((val >>> 24) & 255);
		return ret;
	}

	public static int buildInt(byte b1, byte b2, byte b3, byte b4) {
		return (b1 & 255) | ((b2 & 255) << 8) | ((b3 & 255) << 16) | ((b4 & 255) << 24);
	}

	public static double buildDoubleFromInts(int i1, int i2) {
		/*
		byte[] arr = new byte[8];
		ByteBuffer buf = ByteBuffer.wrap(arr);
		buf.putInt(i1);
		buf.putInt(i2);
		return ByteBuffer.wrap(arr).getDouble();
		 */
		return Double.longBitsToDouble(buildLong(i1, i2));
	}

	public static int[] splitDoubleToInts(double val) {
		/*
		byte[] arr = new byte[8];
		ByteBuffer.wrap(arr).putDouble(val);
		ByteBuffer buf = ByteBuffer.wrap(arr);
		int i1 = buf.getInt();
		int i2 = buf.getInt();
		return new int[]{i1, i2};
		 */
		return splitLong(Double.doubleToRawLongBits(val));
	}

	public static byte flipBits(byte get) {
		return (byte)(Integer.reverse(get) >>> 24);
	}

	public static int[] getLinearArray(int size) {
		return getLinearArray(0, size-1);
	}

	public static int[] getLinearArray(int from, int to) {
		int[] n = new int[to-from+1];
		for (int i = from; i <= to; i++)
			n[i-from] = i;
		return n;
	}

	public static void queueTask(Runnable r) {
		threader.submit(r);
	}

	public static void queueTask(Errorable e, Consumer<Exception> errorHandler) {
		queueTask(() -> {
			try {
				e.run();
			}
			catch (Exception ex) {
				errorHandler.accept(ex);
			}
		});
	}

	public static void stopThreads() {
		threader.shutdown();
	}

	public static <E extends Comparable<E>> List<E> sorted(Collection<E> c) {
		ArrayList<E> li = new ArrayList(c);
		Collections.sort(li);
		return li;
	}

	public static <E> List<E> sorted(Collection<E> c, Comparator<E> comp) {
		ArrayList<E> li = new ArrayList(c);
		Collections.sort(li, comp);
		return li;
	}

	public static <K, V> void resort(TreeMap<K, V> map) {
		HashMap<K, V> temp = new HashMap(map); //not treemap since it will try to preserve the old sort
		map.clear();
		map.putAll(temp);
	}
}
