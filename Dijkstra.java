import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

/*
 * Author: Dale Angus (daleangus@hotmail.com)
 * 
 * Compute the weight of a short path between two vertices
 * give a directed graph with positive edge weights.
 * 
 */
public class Dijkstra {
	static final int infinity = Integer.MAX_VALUE;

	/*
	 * 4 4 1 2 1 4 1 2 2 3 2 1 3 5 1 3
	 */
	private static int distance(ArrayList<Integer>[] adj, ArrayList<Integer>[] cost, int s, int t) {
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
			// REMOVED. You have to figure this out if you are taking a MOOC course.

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

		return (dist[t] != infinity ? dist[t] : -1);
	}

	@SuppressWarnings("resource")
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
		int x = scanner.nextInt() - 1;
		int y = scanner.nextInt() - 1;
		System.out.println(distance(adj, cost, x, y));
	}
}
