import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

/*
 * Author: Dale Angus (daleangus@hotmail.com)
 * 
 * Breadth First Search
 * Given an undirected graph, compute the length of a shortest path between s and t
 * 
 */
public class BFS {
	static final int infinity = Integer.MAX_VALUE;

	private static int distance(ArrayList<Integer>[] adj, int s, int t) {
		int[] dist = new int[adj.length];
		for (int i = 0; i < dist.length; i++)
			dist[i] = infinity;

		Queue<Integer> q = new LinkedList<Integer>();
		q.add(s);
		dist[s] = 0;
		// write your code here
		go(adj, q, dist, t);

		return (dist[t] == infinity ? -1 : dist[t]);
	}

	private static void go(ArrayList<Integer>[] adj, Queue<Integer> q, int[] dist, int t) {
		boolean found = false;
		while (!q.isEmpty() && !found) {
			int u = q.peek();
			// System.out.println(q);
			for (int i = 0; i < adj[u].size(); i++) {
				int v = adj[u].get(i);
				if (dist[v] == infinity) {
					dist[v] = dist[u] + 1;
					// System.out.println(u + "-->" + v);
					q.add(v);
				}
				if (v == t) {
					found = true;
					break;
				}
			}
			q.remove();
		}
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
			adj[y - 1].add(x - 1);
		}
		int x = scanner.nextInt() - 1;
		int y = scanner.nextInt() - 1;
		System.out.println(distance(adj, x, y));
	}
}
