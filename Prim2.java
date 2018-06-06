package stanford.algorithms;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

/*
 * Question 3
 * 
 * In this programming problem you'll code up Prim's minimum spanning tree
 * algorithm.
 * 
 * Download the text file below. edges.txt
 * 
 * This file describes an undirected graph with integer edge costs. It has the
 * format
 * 
 * [number_of_nodes] [number_of_edges]
 * 
 * [one_node_of_edge_1] [other_node_of_edge_1] [edge_1_cost]
 * 
 * [one_node_of_edge_2] [other_node_of_edge_2] [edge_2_cost]
 * 
 * ...
 * 
 * For example, the third line of the file is "2 3 -8874", indicating that there
 * is an edge connecting vertex #2 and vertex #3 that has cost -8874.
 * 
 * You should NOT assume that edge costs are positive, nor should you assume
 * that they are distinct.
 * 
 * Your task is to run Prim's minimum spanning tree algorithm on this graph. You
 * should report the overall cost of a minimum spanning tree --- an integer,
 * which may or may not be negative --- in the box below.
 * 
 * IMPLEMENTATION NOTES: This graph is small enough that the straightforward
 * O(mn) time implementation of Prim's algorithm should work fine. OPTIONAL: For
 * those of you seeking an additional challenge, try implementing a heap-based
 * version. The simpler approach, which should already give you a healthy
 * speed-up, is to maintain relevant edges in a heap (with keys = edge costs).
 * The superior approach stores the unprocessed vertices in the heap, as
 * described in lecture. Note this requires a heap that supports deletions, and
 * you'll probably need to maintain some kind of mapping between vertices and
 * their positions in the heap. Ans:______________
 * 
 * Author: Dale Angus (daleangus@hotmail.com)
 * 
 * Prim's Algorithm Implementation
 * 
 */
public class Prim {
	public static void main(String[] args) throws Exception {

		BufferedReader br = new BufferedReader(new FileReader("edges.txt"));
		String line = br.readLine();
		int v = Integer.parseInt(line.substring(0, line.indexOf(" ")));
		int e = Integer.parseInt(line.substring(line.indexOf(" ") + 1));
		ArrayList<Integer>[] adj = (ArrayList<Integer>[]) new ArrayList[v];
		ArrayList<Integer>[] cost = (ArrayList<Integer>[]) new ArrayList[v];
		for (int i = 0; i < v; i++) {
			adj[i] = new ArrayList<Integer>();
			cost[i] = new ArrayList<Integer>();
		}
		for (int i = 0; i < e; i++) {
			line = br.readLine();
			int v1 = Integer.parseInt(line.substring(0, line.indexOf(" ")));
			int v2 = Integer.parseInt(line.substring(line.indexOf(" ") + 1, line.lastIndexOf(" ")));
			int c = Integer.parseInt(line.substring(line.lastIndexOf(" ") + 1));
			adj[v1 - 1].add(v2 - 1);
			cost[v1 - 1].add(c);
			adj[v2 - 1].add(v1 - 1);
			cost[v2 - 1].add(c);
		}
		System.out.println(mstCost(adj, cost));
		br.close();
	}

	private static long mstCost(ArrayList<Integer>[] adj, ArrayList<Integer>[] cost) throws Exception {

		VertexPrim[] arrVertexPrim = new VertexPrim[adj.length];
		// create all VertexPrim
		for (int i = 0; i < arrVertexPrim.length; i++) {
			arrVertexPrim[i] = new VertexPrim(i);
		}

		// update each VertexPrim with neighbors
		for (int i = 0; i < arrVertexPrim.length; i++) {
			// REMOVE for the good of MOOC students :)
		}

		Queue<Integer> q = new PriorityQueue<Integer>();

		// start with 0
		arrVertexPrim[0].priority = 0;
		q.add(0);

		long sum = 0;
		while (!q.isEmpty()) {
			Integer priority = q.peek();

			// find the vertex with this priority
			VertexPrim v = null;
			for (int i = 0; i < arrVertexPrim.length; i++) {
				// REMOVE for the good of MOOC students :)
			}

			// update neighbor vertices' priority (cost)
			for (int i = 0; i < cost[v.index].size(); i++) {
				// REMOVE for the good of MOOC students :)
			}

			// mark as used
			v.used = true;
			sum += v.priority;

			// update priority queue
			q.clear();
			for (int i = 0; i < arrVertexPrim.length; i++) {
				if (!arrVertexPrim[i].used && arrVertexPrim[i].priority != Integer.MAX_VALUE)
					q.add(arrVertexPrim[i].priority);
			}
		}

		return sum;
	}
}

class VertexPrim {
	int index = -1;
	int priority = Integer.MAX_VALUE;;
	ArrayList<VertexPrim> neighborVertices = null;
	boolean used = false;

	VertexPrim(int index) {
		this.index = index;
		neighborVertices = new ArrayList<VertexPrim>();
	}

	@Override
	public String toString() {
		return (index + "/" + priority);
	}
}