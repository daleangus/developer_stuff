package codingtest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
//IMPORT LIBRARY PACKAGES NEEDED BY YOUR PROGRAM
//SOME CLASSES WITHIN A PACKAGE MAY BE RESTRICTED
//DEFINE ANY CLASS AND METHOD NEEDED
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;

//CLASS BEGINS, THIS CLASS IS REQUIRED
/*
 * Retrieve Most Frequently UsedWords
 * Author: Dale Angus (daleangus@hotmail.com)
 */

public class MostFreqUsedWord {
	// METHOD SIGNATURE BEGINS, THIS METHOD IS REQUIRED
	List<String> retrieveMostFrequentlyUsedWords(String literatureText, List<String> wordsToExclude) {

		TreeMap<String, Integer> map = new TreeMap<String, Integer>();
		StringTokenizer st = new StringTokenizer(literatureText, " ");
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			if (wordsToExclude.contains(s)) {
				// System.out.println(s);
				continue;
			} else {
				if (map.containsKey(s)) {
					// System.out.println(s);
					map.put(s, map.get(s) + 1);
				} else {
					// System.out.println(s);
					map.put(s, 1);
				}
			}
		}

		ArrayList<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(map.entrySet());
		// for (Entry<String, Integer> e : list) {
		// System.out.println(e.getKey() + " " + e.getValue());
		// }

		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});
//		for (Entry<String, Integer> e : list) {
//			System.out.println(e.getKey() + " " + e.getValue());
//		}

		List<String> returnValue = new ArrayList<String>();
		int h = 0;
		for (Entry<String, Integer> e : list) {
			if (e.getValue() >= h) {
				returnValue.add(e.getKey());
				h = e.getValue();
			} else
				break;

		}
		return returnValue;
		// WRITE YOUR CODE HERE
	}
	// METHOD SIGNATURE ENDS

	public static void main(String[] args) {
		Solution sol = new Solution();
		ArrayList<String> l = new ArrayList<String>();
		l.add("The");
		l.add("an");
		List<String> a = sol.retrieveMostFrequentlyUsedWords("romeo romeo wherefore art thou romeo", l);
		for (String s : a)
			System.out.println(s);
	}
}
