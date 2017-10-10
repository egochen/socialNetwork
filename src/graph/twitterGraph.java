/**
 * 
 */
package graph;

import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import util.GraphLoader;

/**
 * @author Chenyang Wu.
 * 
 */
public class twitterGraph implements Graph {

	private Map<Integer, ArrayList<Integer>> outboundMap;
	private Map<Integer, ArrayList<Integer>> inboundMap;
	private HashMap<Integer, Integer> inboundCount;
	private Set<Integer> visitedNodes;
	public twitterGraph() {
		outboundMap = new HashMap<Integer, ArrayList<Integer>>();
		inboundMap = new HashMap<Integer, ArrayList<Integer>>();
		inboundCount = new HashMap<Integer, Integer>();
	}
	
	/* (non-Javadoc)
	 * @see graph.Graph#addVertex(int)
	 */
	@Override
	public void addVertex(int num) {
		// TODO Auto-generated method stub
		if (!outboundMap.containsKey(num)) {
			ArrayList<Integer> neighbors = new ArrayList<>();
			outboundMap.put(num, neighbors); 
		}
		if (!inboundMap.containsKey(num)) {
			ArrayList<Integer> neighbors = new ArrayList<>();
			inboundMap.put(num, neighbors); 
		}
	}

	/* (non-Javadoc)
	 * @see graph.Graph#addEdge(int, int)
	 */
	@Override
	public void addEdge(int from, int to) {
		// TODO Auto-generated method stub
		if (!outboundMap.containsKey(from)) 
			throw new IllegalArgumentException("Vertex not added to the graph!");

		(outboundMap.get(from)).add(to);
		(inboundMap.get(to)).add(from);
	}

	/*
	 * Easier task
	 * Suggest users to follow for a given node V
	 */
	private List<Integer> usersToFollow(int V) {
		// keeps records of how many times node nn can be reached from V by 2nd degree connection
		HashMap<Integer, Integer> popularUsers = new HashMap<>();
		for (int n : outboundMap.get(V)) {
			for (int nn : outboundMap.get(n)) {
				if (nn != V) {
					if (!popularUsers.containsKey(nn)) {
						popularUsers.put(nn, 1);
					} else {
						popularUsers.put(nn, popularUsers.get(nn)+1);
					}
				}
			}
		}
		//sort popularUsers by value.
		Set<Map.Entry<Integer, Integer>> popularUserSet = popularUsers.entrySet();
		List<Integer> usersToFollow = popularUserSet.stream()
				.sorted(comparing(Entry<Integer, Integer>::getValue).reversed())
				.filter(entry -> entry.getValue() > 3)
				.map(Entry::getKey)
				.collect(Collectors.toList());

		return usersToFollow;
	}
	
	/*
	 * More difficult task
	 * Get minimum dominating set by greedy method
	 */
	public List<Integer> getDominantSet() {
		this.inboundCount = new HashMap<>();
		this.visitedNodes = new HashSet<>();
		for (int v : inboundMap.keySet()) {
			inboundCount.put(v, inboundMap.get(v).size());
		}
		List<Integer> res = new ArrayList<>();
		while (visitedNodes.size() < inboundMap.size()) {
			int maxInbound = maxInboundVertex();
			updateVisited(maxInbound);
			res.add(maxInbound);
		}
		return res;
	}
	/*
	 * Add node V, as well as all its followers, to the visited set, and update the inboundCount map by 
	 * removing the nodes that are following(connected to) V
	 */
	private void updateVisited(int V) {
		visitedNodes.add(V);
		if (inboundMap.get(V) != null) {
			visitedNodes.addAll(inboundMap.get(V));
			for (int n : inboundMap.get(V)) {
				inboundCount.remove(n);
			}
		}
		inboundCount.remove(V);
	}
	
	/*
	 * Get the index with most inbound edges. 
	 * inboundCount: <Vertex, numOfInboundEdges>
	 */
	private int maxInboundVertex() {
		int maxInboundVertex = 0;
		int maxCount = 0;
		for (int V : inboundCount.keySet()) {
			for (int n : inboundMap.get(V)) {
				if (visitedNodes != null && visitedNodes.contains(n)) {
					inboundCount.put(V, Math.max(0, inboundCount.get(V) - 1));
				}
			}
			if (inboundCount.get(V) >= maxCount) {
				maxInboundVertex = V;
				maxCount = inboundCount.get(V);
			}
		}
		return maxInboundVertex;
	}
	/*
	 * get neighbors of a vertex
	 */
	public List<Integer> getNeighbors(int v) {
		return outboundMap.get(v);
	}
	
	/* (non-Javadoc)
	 * @see graph.Graph#getEgonet(int)
	 */
	@Override
	public Graph getEgonet(int center) {
		// TODO Auto-generated method stub
		twitterGraph egoNet = new twitterGraph();
		egoNet.addVertex(center);
		ArrayList<Integer> neighbors = outboundMap.get(center);
		//add center vertex and its connections
		for (int v: neighbors) {
			egoNet.addVertex(v);
			egoNet.addEdge(center, v);
		}
		for (int v: neighbors) {
			for (int n: outboundMap.get(v)) {
				if (n == center || neighbors.indexOf(n)>=0) {
					egoNet.addEdge(v, n);
				}
			}
		}
		return egoNet;
	}

	/* (non-Javadoc)
	 * @see graph.Graph#getSCCs()
	 */
	@Override
	public List<Graph> getSCCs() {
		// TODO Auto-generated method stub
		Deque<Integer> vertices = new LinkedList<>();
		for (int ele: outboundMap.keySet()) {
			vertices.push(ele);
		}
		Deque<Integer> reversed = this.dfs(this, vertices);		
		List<Graph> scc = this.dfsGraph(this.transpose(), reversed);
		return scc;
	}

	/* (non-Javadoc)
	 * @see graph.Graph#exportGraph()
	 */
	@Override
	public HashMap<Integer, HashSet<Integer>> exportGraph() {
		// TODO Auto-generated method stub
		HashMap<Integer, HashSet<Integer>> toexport = new HashMap<>();
		for (int key: outboundMap.keySet()) {
			HashSet<Integer> distinctConnections = new HashSet<Integer>(outboundMap.get(key));
			toexport.put(key, distinctConnections);
		}
		return toexport;
	}
	/*
	 * return the transpose of a graph
	 */
	public twitterGraph transpose() {
		twitterGraph transGraph = new twitterGraph();
		for (int v: outboundMap.keySet()) {
			transGraph.addVertex(v);
		}
		for (int v: outboundMap.keySet()) {
			for (int n : outboundMap.get(v)) {
				transGraph.addEdge(n, v);
			}
		}

		return transGraph;
	}
	
	private Deque<Integer> dfs(twitterGraph G, Deque<Integer> vertices) {
		Set<Integer> visited = new HashSet<>();
		Deque<Integer> finished = new LinkedList<>();
		while (!vertices.isEmpty()) {
			int v = vertices.pop();
			if (!visited.contains(v)) {
				dfsVisit(G, v, visited, finished);
			}
		}
		return finished;
	}
	
	private List<Graph> dfsGraph(twitterGraph G, Deque<Integer> vertices) {
		Set<Integer> visited = new HashSet<>();
		List<Graph> scc = new ArrayList<>();
		while (!vertices.isEmpty()) {
			Deque<Integer> finished = new LinkedList<>();
			int v = vertices.pop();
			if (!visited.contains(v)) {
				dfsVisit(G, v, visited, finished);
			}
			if (finished.size() > 0) {
				Graph g = new twitterGraph();
				for (int i: finished) {
					g.addVertex(i);
				}
				scc.add(g);
			}
		}
		return scc;
	}
	
	private void dfsVisit(twitterGraph G, int v, Set<Integer> visited, Deque<Integer> finished) {
		visited.add(v);
		for (int n : G.getNeighbors(v)) {
			if (!visited.contains(n)) {
				dfsVisit(G, n, visited, finished);
			}
		}
		finished.push(v);
	}
	
	
	@Override
	public String toString() {
		String s = "";
		for (int key: outboundMap.keySet()) {
			s += String.valueOf(key) + ": ";
			for (int v: outboundMap.get(key)) {
				s += String.valueOf(v) + ", ";
			}
			s += "\n";
		}
		return s;
	}
	public static void main(String[] args)
	{
	    twitterGraph smallTestGraph = new twitterGraph();
		GraphLoader.loadGraph(smallTestGraph, "data/small_test_graph.txt");
//		System.out.println(smallTestGraph.usersToFollow(4));
//		List<Integer> dominantSet = smallTestGraph.getDominantSet();
//		System.out.println(dominantSet);
	}
}
