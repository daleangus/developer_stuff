import static java.util.Arrays.asList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/*
 * Author: Dale Angus (daleangus@hotmail.com)
 * 
 * QuickSort implementation
 * MOOC Test
 *  
 */
public class QuickSort {

	public static void main(String[] args) throws IOException {
		Long time = System.currentTimeMillis();

		// put list of numers in an ArrayList
		ArrayList<Integer> arrList = new ArrayList<Integer>();

		// Read a "stream" of integers from a file
		BufferedReader br = new BufferedReader(new FileReader("QuickSort.txt"));
		try {
			String line;
			while ((line = br.readLine()) != null) {
				arrList.add(Integer.parseInt(line));
			}
		} finally {
			br.close();
		}

		// copy the list to an int[]
		int[] arr = new int[arrList.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = arrList.get(i);
		}

		// Question 1 - Use the first element as the QuickSort pivot
		System.out.println(quickSort(Arrays.copyOf(arr, arr.length), new ChoosePivot("first")));

		// Question 2 - Use the last element as the QuickSort pivot
		System.out.println(quickSort(Arrays.copyOf(arr, arr.length), new ChoosePivot("last")));

		// Question 3 - - Use the median as the QuickSort pivot
		System.out.println(quickSort(Arrays.copyOf(arr, arr.length), new ChoosePivot("median")));

		Long now = System.currentTimeMillis();
		System.out.println(now - time + " ms");
	}

	// helper method
	private static void swap(int[] arr, int i, int j) {
		int tmp = arr[i];
		arr[i] = arr[j];
		arr[j] = tmp;
	}

	// QuickSort
	private static int quickSort(int[] arr, ChoosePivot choosePivot) {
		if (arr.length <= 1)
			return 0;

		swap(arr, 0, choosePivot.get(arr));

		int pos = partition(arr);
		int comparisonsl = quickSort(Arrays.copyOfRange(arr, 0, pos), choosePivot);
		int comparisonsr = quickSort(Arrays.copyOfRange(arr, pos + 1, arr.length), choosePivot);

		return arr.length - 1 + comparisonsl + comparisonsr;

	}

	// Partition algorithm
	private static int partition(int[] arr) {
		int i = 1;
		for (int j = 1; j < arr.length; j++) {
			if (arr[j] < arr[0]) {
				swap(arr, j, i++);
			}
		}

		swap(arr, 0, i - 1);
		return i - 1;
	}
}

/*
 * Helper Class for getting the "first", "last" or "median" pivot value
 */
class ChoosePivot {
	String strategy = "";

	ChoosePivot(String strategy) {
		this.strategy = strategy;
	}

	public int get(int[] arr) {
		if ("last".equals(strategy)) {
			return arr.length - 1;
		} else if ("median".equals(strategy)) {
			int m = arr.length % 2 == 0 ? arr.length / 2 - 1 : arr.length / 2;

			List<Integer> order = asList(0, m, arr.length - 1);
			Collections.sort(order, new Comparator<Integer>() {
				@Override
				public int compare(Integer x, Integer y) {
					if (arr[x] < arr[y])
						return 1;
					else if (arr[x] > arr[y])
						return -1;
					return 0;
				}
			});
			return order.get(1);
		}
		return 0; // default "first"
	}
}
