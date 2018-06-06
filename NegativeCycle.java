import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

/*
 * Author: Dale Angus (daleangus@hotmail.com)
 * 
 * Determine if a directed graph with possible negative weights
 * contains a cycle of negative weight.
 * 
 * Application in detecting anomalies in FX rates.
 * 
 */
public class NegativeCycle {
	static final int infinity = Integer.MAX_VALUE;

	private static int negativeCycle(ArrayList<Integer>[] adj, ArrayList<Integer>[] cost) {

		// Go through every vertex as starting point
		// and apply the Dijkstra algorithm.
		for (int s = 0; s < adj.length; s++) {

			int[] dist = new int[adj.length];
			boolean[] visited = new boolean[adj.length];

			for (int i = 0; i < adj.length; i++) {
				dist[i] = infinity;
			}
			dist[s] = 0;

			Queue<Integer> q = new LinkedList<Integer>();
			q.add(s);

			while (!q.isEmpty()) {

				// get minimum cost from those in the queue
				Iterator<Integer> iter = q.iterator();
				int minCost = infinity;
				int u = -1;
				while (iter.hasNext()) {
					int someV = iter.next();
					if (u == -1)
						u = someV;
					int c = dist[someV];
					if (c < minCost) {
						u = someV;
						minCost = c;
					}
				}

				// update the weights
				if (!visited[u])
					for (int i = 0; i < adj[u].size(); i++) {
						int next = adj[u].get(i);
						if (!visited[next])
							if (dist[next] > dist[u] + cost[u].get(i)) {
								dist[next] = dist[u] + cost[u].get(i);
								q.add(next);
							}
					}

				visited[u] = true;
				q.remove(u);
			}

			// System.out.print("From: " + s + "\t");
			// for (int i = 0; i < dist.length; i++)
			// System.out.print(dist[i] + "\t");
			// for (int i = 0; i < adj.length; i++)
			// System.out.print(adj[i] + "\t");
			// System.out.print("\t");
			// for (int i = 0; i < cost.length; i++)
			// System.out.print(cost[i] + "\t");
			// System.out.println("");

			// Check if there is cycle with negative weight
			for (int j = 0; j < adj.length; j++) {
				if (dist[j] == infinity)
					continue;
				if (adj[j].contains(s)) {

					int idx = adj[j].indexOf(s);
					int cycleCost = dist[j] + cost[j].get(idx);
					if (cycleCost < 0) {
						return 1; // there is
					}
				}
			}
		}
		return 0; // there is none

	}

	// 10 9 1 2 1 6 7 1 8 9 1 9 10 1 3 4 1 7 8 1 4 5 1 5 6 1 2 3 1

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		int n = scanner.nextInt();
		int m = scanner.nextInt();
		ArrayList<Integer>[] adj = (ArrayList<Integer>[]) new ArrayList[n];
		ArrayList<Integer>[] cost = (ArrayList<Integer>[]) new ArrayList[n];
		for (int i = 0; i < n; i++) {
			adj[i] = new ArrayList<Integer>();
			cost[i] = new ArrayList<Integer>();
		}
		for (int i = 0; i < m; i++) {
			int x, y, w;
			x = scanner.nextInt();
			y = scanner.nextInt();
			w = scanner.nextInt();
			adj[x - 1].add(y - 1);
			cost[x - 1].add(w);
		}
		System.out.println(negativeCycle(adj, cost));
	}
}
