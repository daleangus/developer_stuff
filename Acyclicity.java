import java.util.ArrayList;
import java.util.Scanner;
/*
 * Author: Dale Angus (daleangus@hotmail.com)
 * 
 * Is the given directed graph acyclic?
 */
public class Acyclicity {
	private static int acyclic(ArrayList<Integer>[] adj) {

		for (int x = 0; x < adj.length; x++) {
			ArrayList<Integer> inspectedVertex = new ArrayList<Integer>();
			ArrayList<Integer> discoveredVertex = new ArrayList<Integer>();
			// initialize
			discoveredVertex.add(x);

			do {
				if (discoveredVertex.size() == 0)
					break;

				ArrayList<Integer> inspectThisArray = new ArrayList<Integer>();
				inspectThisArray.addAll(discoveredVertex);

				inspectedVertex.addAll(discoveredVertex);
				discoveredVertex.clear();

				for (int i = 0; i < inspectThisArray.size(); i++) {

					discoveredVertex.addAll(adj[inspectThisArray.get(i)]);
					discoveredVertex.removeAll(inspectedVertex);

					if (adj[inspectThisArray.get(i)].contains(x))
						return 1;
				}

			} while (true);
		}
		return 0;
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		int n = scanner.nextInt();
		int m = scanner.nextInt();
		ArrayList<Integer>[] adj = (ArrayList<Integer>[]) new ArrayList[n];
		for (int i = 0; i < n; i++) {
			adj[i] = new ArrayList<Integer>();
		}
		for (int i = 0; i < m; i++) {
			int x, y;
			x = scanner.nextInt();
			y = scanner.nextInt();
			adj[x - 1].add(y - 1);
		}
		System.out.println(acyclic(adj));
	}
}
