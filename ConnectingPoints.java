import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/*
 * Author: Dale Angus
 * 
 * Implementation of Kruskal's algorithm.
 * https://en.wikipedia.org/wiki/Kruskal%27s_algorithm
 * 
 */
public class ConnectingPoints {
	static boolean debug = false;

	private static double minimumDistance(int[] x, int[] y) {

		// create points
		ArrayList<Point> arrPoint = new ArrayList<Point>();
		for (int i = 0; i < x.length; i++) {
			Point p = new Point(x[i], y[i]);
			arrPoint.add(p);
		}

		// create edges
		ArrayList<Edge> arrEdge = new ArrayList<Edge>();
		for (int i = 0; i < arrPoint.size(); i++) {
			for (int j = arrPoint.size() - 1; j > i; j--) {
				Edge e = new Edge(arrPoint.get(i), arrPoint.get(j));
				arrEdge.add(e);
			}
		}
		// sort edges according to distance
		Collections.sort(arrEdge, new Comparator<Edge>() {
			public int compare(Edge e1, Edge e2) {
				return e1.getDistance().compareTo(e2.getDistance());
			}
		});
		// Check sort order
		if (debug)
			for (int i = 0; i < arrEdge.size(); i++) {
				Edge e = arrEdge.get(i);
				System.out.println(e.u.x + "," + e.u.y + " " + e.v.x + "," + e.v.y + " " + e.getDistance());
			}

		// create list of sets containing individual points
		ArrayList<Set<Point>> arrSetPoint = new ArrayList<Set<Point>>();
		for (int i = 0; i < arrPoint.size(); i++) {
			Set<Point> setPoint = new HashSet<Point>();
			setPoint.add(arrPoint.get(i));
			arrSetPoint.add(setPoint);
		}

		Set<Edge> setEdge = new HashSet<Edge>();
		for (int i = 0; i < arrEdge.size(); i++) {
			Edge e1 = arrEdge.get(i);

			// find the set with u
			Set<Point> setWithU = null;
			for (int si = 0; si < arrSetPoint.size(); si++) {
				if (arrSetPoint.get(si).contains(e1.u)) {
					setWithU = arrSetPoint.get(si);
					break;
				}
			}

			// find the set with v
			Set<Point> setWithV = null;
			for (int si = 0; si < arrSetPoint.size(); si++) {
				if (arrSetPoint.get(si).contains(e1.v)) {
					setWithV = arrSetPoint.get(si);
					break;
				}
			}

			// check if set with u and set with v are the same
			if (setWithU == setWithV) {
				continue;
			}

			// union set with u and set with v
			setWithU.addAll(setWithV);
			// remove set with v
			arrSetPoint.remove(setWithV);

			setEdge.add(e1);
			if (debug)
				System.out.println("Added Edge " + e1.u.x + "," + e1.u.y + " " + e1.v.x + "," + e1.v.y + " "
							+ e1.getDistance());
			}
		}

		double totalDistance = 0;
		Iterator<Edge> iter = setEdge.iterator();
		while (iter.hasNext()) {
			totalDistance += iter.next().getDistance();
		}
		return totalDistance;
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		int n = scanner.nextInt();
		int[] x = new int[n];
		int[] y = new int[n];
		for (int i = 0; i < n; i++) {
			x[i] = scanner.nextInt();
			y[i] = scanner.nextInt();
		}
		System.out.println(minimumDistance(x, y));
	}
}

class Point {
	int x;
	int y;

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
}

class Edge {
	Point u;
	Point v;

	public Edge(Point u, Point v) {
		this.u = u;
		this.v = v;
	}

	public Double getDistance() {
		double dist = Math.sqrt(Math.pow(u.x - v.x, 2) + Math.pow(u.y - v.y, 2));
		return dist;
	}
}
