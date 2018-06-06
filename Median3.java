import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.TreeSet;

/* 
 * Author: Dale Angus (daleangus@hotmail.com)
 * 
 * Keep track of the current median of a list as a stream of numbers keep being added to the list.
 * 
 */
public class Median3 {
	static boolean debug = false;

	static TreeSet<Integer> low = new TreeSet<Integer>();
	static TreeSet<Integer> high = new TreeSet<Integer>();
	static int k = 0;
	static long sum = 0;
	static Integer median = null;

	public static void trackMedian(Integer num) {
		if (k < 2) {
			boolean b = (k == 1 ? low.add(num) : high.add(num));
		} else {

			// KEEP ADDING TO THE LAST
			if (num < high.last() && num < high.first()) {
				low.add(num);
			} else {
				high.add(num);
			}

			// balance
			if (high.size() - low.size() == 2) {
				low.add(high.first());
				high.remove(high.first());
			} else if (low.size() - high.size() == 2) {
				high.add(low.last());
				low.remove(low.last());
			}

		}
		if (debug) {
			System.out.println(low);
			System.out.println(high);
		}

		k++;

		if (k % 2 == 0)
			median = (low.last() < high.first() ? low.last() : high.first());
		else {
			median = (low.size() > high.size() ? low.last() : high.first());
		}

		if (debug)
			System.out.println(median);

		// (MOOC test)
		sum += median;
		if (debug)
			System.out.println(k + ": " + sum + " mod 10000 " + sum % 10000);
	}

	public static void main(String[] args) throws IOException {
		Long time = System.currentTimeMillis();

		// Read a "stream" of integers from a file
		BufferedReader br = new BufferedReader(new FileReader("Median.txt"));
		try {
			String line;
			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				trackMedian(Integer.parseInt(line));
			}
		} finally {
			br.close();
		}
		System.out.println("Median: " + median);

		Long now = System.currentTimeMillis();
		System.out.println(now - time + " ms");
	}
}
