
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

interface SameWords {
	public void index(String word);

	public Collection<String> getSimilarWords(String word);
}

/*
 * Author: Dale Angus (daleangus@hotmail.com)
 * 
 * Implement an index to allow O(1) complexity when retrieving similar words.
 * 
 */
public class SameWordsImpl implements SameWords {
	HashMap<Integer, ArrayList<String>> dict = new HashMap<Integer, ArrayList<String>>();

	@Override
	public void index(String word) {
		char[] c = word.toCharArray();
		Arrays.sort(c);
		String w = "";
		for (char l : c)
			w += l;

		int hc = w.hashCode();
		if (dict.get(hc) == null) {
			ArrayList<String> arr = new ArrayList<String>();
			arr.add(word);
			dict.put(hc, arr);
		} else {
			dict.get(hc).add(word);
		}

	}

	@Override
	public Collection<String> getSimilarWords(String word) {
		char[] c = word.toCharArray();
		Arrays.sort(c);
		String w = "";
		for (char l : c)
			w += l;
		return dict.get(w.hashCode());
	}

	public static void main(String[] args) {
		SameWordsImpl a = new SameWordsImpl();
		a.index("dog");
		a.index("god");

		System.out.println(a.getSimilarWords("dog")); // O(1)!
	}
}
