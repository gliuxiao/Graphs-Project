package graphs;

import java.util.*;
import java.util.Map.Entry;

/**
 * Implements a graph. We use two maps: one map for adjacency properties 
 * (adjancencyMap) and one map (dataMap) to keep track of the data associated 
 * with a vertex. 
 * 
 * @author Gina Liu
 * 
 * @param <E>
 */
public class Graph<E> {
	/* You must use the following maps in your implementation */
	private HashMap<String, HashMap<String, Integer>> adjacencyMap; //<node name, HashMap<adjacent node, cost to get there>>
	private HashMap<String, E> dataMap;

	//constructor
	public Graph(){
		adjacencyMap = new HashMap<String, HashMap<String, Integer>>();
		dataMap = new HashMap<String, E>();               
	}

	//Adds or updates a directed edge with the specified cost
	public void addDirectedEdge(String startVertexName, String endVertexName, int cost){
		if (!adjacencyMap.containsKey(startVertexName) || !adjacencyMap.containsKey(endVertexName)){
			throw new java.lang.IllegalArgumentException();
		}
		adjacencyMap.get(startVertexName).put(endVertexName, cost);
	}

	//Adds a vertex to the graph by adding to the adjacency map an entry for the vertex.
	public void addVertex(String vertexName, E data){
		if (dataMap.containsKey(vertexName) || adjacencyMap.containsKey(vertexName)){
			throw new java.lang.IllegalArgumentException();
		}
		dataMap.put(vertexName, data);
		HashMap<String, Integer> emptyMap = new HashMap<String, Integer>(); //make empty map
		adjacencyMap.put(vertexName, emptyMap);
	}

	//Computes a non-recursive Breadth-First Search of the specified graph
	public void doBreadthFirstSearch(String startVertexName, CallBack<E> callback){
		if (!adjacencyMap.containsKey(startVertexName)){
			throw new java.lang.IllegalArgumentException();
		}

		HashSet<String> visitedSet = new HashSet<String>();
		ArrayList<String> toCheck = new ArrayList<String>();

		toCheck.add(startVertexName);
		//checking if there are any more nodes to check
		while (!toCheck.isEmpty()){
			String curr = toCheck.get(0);
			if (visitedSet.contains(curr)) {
				toCheck.remove(0);
			} else {
				visitedSet.add(curr);
				callback.processVertex(curr, getData(curr));
				toCheck.remove(0); //item was checked and removed
				toCheck.addAll(getAdjacentVertices(curr).keySet()); //add keys of adjacent vertices
			}	
		}
	}

	//Computes a non-recursive Depth-First Search of the specified graph.
	public void doDepthFirstSearch(String startVertexName, CallBack<E> callback){
		/*
		 * non-recursive DFS, using explict stacks
		 */
		//http://algs4.cs.princeton.edu/41graph/NonrecursiveDFS.java
		if (!adjacencyMap.containsKey(startVertexName)){
			throw new java.lang.IllegalArgumentException();
		}

		LinkedList<String> toCheck = new LinkedList<String>();
		HashSet<String> visitedSet = new HashSet<String>();

		toCheck.add(startVertexName);
		while(!toCheck.isEmpty()){
			String curr = toCheck.get(0);
			if (visitedSet.contains(curr)) {
				toCheck.remove(0);
			} else {
				visitedSet.add(curr);
				callback.processVertex(curr, getData(curr));
				toCheck.remove(0);

				//make iterator to add one by one to front of LinkedList
				Iterator<String> it = getAdjacentVertices(curr).keySet().iterator();
				while(it.hasNext()){
					toCheck.add(0, it.next()); //add as it iterates
				}
			}
		}
	}

	//Computes the shortest path and shortest path cost using Dijkstras's algorithm; returns the lowest cost
	public int doDijkstras(String startVertexName, String endVertexName, ArrayList<String> shortestPath){

		//if any of the vertices are not part of the graph; also accounts for nulls
		if (!adjacencyMap.containsKey(startVertexName) || !adjacencyMap.containsKey(endVertexName)) {
			throw new java.lang.IllegalArgumentException("The map does not contain the item");
		}
		
		//if start = end
		if (startVertexName.equals(endVertexName)){
			shortestPath.add(startVertexName);
			return 0;
		}
		

		//TBS is <solved parent, map of unsolved neighbors>
		HashMap<String, Map<String, Integer>> toBeSolved = new HashMap<String, Map<String, Integer>>(); //Map bc more general
		HashMap<String, Integer> solvedNodes = new HashMap<String, Integer>();
		HashMap<String, Integer> minimumCost = new HashMap<String, Integer>();
		HashMap<String, String> parents = new HashMap<String, String>();

		//add startVertexName to solvedNodes and nodePaths
		solvedNodes.put(startVertexName, 0);

		//puts all adj vertices into toBeSolved
		Map<String, Integer> initial =	getAdjacentVertices(startVertexName);
		
		if (initial.size() > 0){
		toBeSolved.put(startVertexName, getAdjacentVertices(startVertexName));
		}

		//iterate through keys(parents) of toBeSolved", and then the map

		int minCost, tempCost;
		String minNode = null;
		String parent = null;
		minCost = Integer.MAX_VALUE;
		while (!toBeSolved.isEmpty()) {
			minCost = Integer.MAX_VALUE;
			Iterator<String> solvedParentIt = toBeSolved.keySet().iterator();
			while (solvedParentIt.hasNext()) {
				String tempParent = solvedParentIt.next();
				Iterator<Entry<String, Integer>> unsolvedNeighborIt = toBeSolved.get(tempParent).entrySet().iterator();

				//iterate through as long as something in toBeSolved
				while (unsolvedNeighborIt.hasNext()) {
					Entry<String, Integer> pair;
					pair = unsolvedNeighborIt.next(); //pair catches what is spit out by iterator
					tempCost = pair.getValue() + solvedNodes.get(tempParent);
					//if temp is less than min cost, set as new minCost
					if (tempCost < minCost) { 
						minCost = tempCost;
						parent = tempParent;
						minNode = pair.getKey(); //gets the name of the min node
					}
				}
			}
			
			//add minNode to solved node set
			solvedNodes.put(minNode, minCost); //cost of current and previous
			//add minNode, and its parent
			parents.put(minNode, parent);
			//clear because visited all nodes
			toBeSolved.clear();

			if (minNode.equals(endVertexName)) {
				String node = endVertexName;
				shortestPath.add(node);
				while (!node.equals(startVertexName)) {
					node = parents.get(node);	
					shortestPath.add(0, node);
				}
				return minCost;
			}
			
			/*
			 * At this point, we know the minCost, minNode, parent, and have traversed through toBeSolved
			 * of the first solved node
			 */

			//add unsolved adjacent nodes to TBS, and repeat, differentiating between parents
			Iterator<String> solvedKeys = solvedNodes.keySet().iterator(); //go over this later!!!!
			while (solvedKeys.hasNext()) {
				String currKey = solvedKeys.next();
				Map<String, Integer> adjacentVert = getAdjacentVertices(currKey); //returns map
				Map<String, Integer> unsolvedNeighbors = new HashMap<String, Integer>(); //must be HM
				for (String s : adjacentVert.keySet()) {
					//only adds unsolved neighbors
					if (!solvedNodes.containsKey(s)) {
						unsolvedNeighbors.put(s, adjacentVert.get(s));
					}
				}
				if (!unsolvedNeighbors.isEmpty()){
					toBeSolved.put(currKey, unsolvedNeighbors);
				}
			}
		}

	  //if no path exists, return -1
		shortestPath.add("None");
		return -1;

	}


	//Returns a map with information about vertices adjacent to vertexName.
	public Map<String, Integer> getAdjacentVertices(String vertexName){
		return adjacencyMap.get(vertexName);
	}

	//Returns the cost of the edge between startVertexName to endVertexName
	public int getCost(String startVertexName, String endVertexName){
		if (!adjacencyMap.containsKey(startVertexName) || !adjacencyMap.containsKey(endVertexName)){
			throw new java.lang.IllegalArgumentException();
		}
		return adjacencyMap.get(startVertexName).get(endVertexName);
	}

	//Returns the data component associated with the specified vertex.
	public E getData(String vertex) {
		if (!adjacencyMap.containsKey(vertex)){
			throw new java.lang.IllegalArgumentException();
		}
		return dataMap.get(vertex);
	}

	//Returns a Set with all the graph vertices.
	public Set<String> getVertices() {
		return adjacencyMap.keySet();
	}

	//Returns a string with information about the Graph.
	public String toString() {

		TreeSet<String> treeSet = new TreeSet<String>();
		treeSet.addAll(getVertices());

		String print = "Vertices: [";

		Iterator<String> it = treeSet.iterator(); //ascending iterator
		while (it.hasNext()) {
			print += it.next() + ", ";
		}
		print = print.substring(0, print.length()-2);
		print += "]\n";
		print += "Edges:\n";

		it = treeSet.iterator();
		while (it.hasNext()) {
			String next = it.next();
			print += "Vertex(" + next + ")--->";
			print += getAdjacentVertices(next) + "\n";
		}

		return print;
	}
}
