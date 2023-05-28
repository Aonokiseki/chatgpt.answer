package utility;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
/**
 * 有向图
 *
 * @param <T>
 * @param <E>
 */
public class DirectedGraph<T1 extends Comparable<T1>, T2 extends Number> extends Graph<T1, T2>{

	public DirectedGraph() {
		super();
	}
	
	public DirectedGraph(Set<Vertex<T1>> vertexs) {
		super(vertexs);
	}
	
	public DirectedGraph(Set<Vertex<T1>> vertexs, Set<Edge<T2>> edges) {
		super(vertexs, edges);
	}
	
	@Override
	public boolean addVertex(Vertex<T1> vertex) {
		if(dictionary.containsKey(vertex.id) || adjacency.containsKey(vertex.id) || inverseAdjacency.containsKey(vertex.id))
			return false;
		dictionary.put(vertex.id, vertex);
		adjacency.put(vertex.id, new HashSet<Arc<T2>>());
		inverseAdjacency.put(vertex.id, new HashSet<Arc<T2>>());
		return true;
	}

	@Override
	public boolean addEdge(Edge<T2> edge) {
		if(dictionary == null || dictionary.isEmpty())
			return false;
		int startid = edge.getStartid();
		int endid = edge.getEndid();
		T2 comparableKey = edge.getWeight();
		if(startid == endid)
			return false;
		if(!dictionary.containsKey(startid) || !dictionary.containsKey(endid))
			return false;
		edgeSet.add(edge);
		if(!adjacency.containsKey(startid))
			adjacency.put(startid, new HashSet<Arc<T2>>());
		if(!inverseAdjacency.containsKey(endid))
			inverseAdjacency.put(endid, new HashSet<Arc<T2>>());
		boolean addTo = adjacency.get(startid).add(new Arc<T2>(endid, comparableKey, startid));
		boolean addFrom = inverseAdjacency.get(endid).add(new Arc<T2>(startid, comparableKey, endid));
		if(addTo && addFrom)
			this.countOfArc++;
		return addTo && addFrom;
	}

	@Override
	public String toString() {
		return String.format("{adjacency=%s, inverseAdjacency=%s}", adjacency, inverseAdjacency);
	}
	
	/**
	 * 反转有向图
	 * @return DirectedGraph&ltT1, T2&gt 
	 */
	public DirectedGraph<T1, T2> reverse(){
		Set<Vertex<T1>> vertexs = new HashSet<Vertex<T1>>();
		for(Entry<Integer, Vertex<T1>> e : this.dictionary.entrySet())
			vertexs.add(e.getValue());
		Set<Edge<T2>> edges = new HashSet<Edge<T2>>();
		for(Edge<T2> edge : edgeSet)
			edges.add(new Edge<T2>(edge.getEndid(), edge.getStartid(), edge.getWeight()));
		DirectedGraph<T1, T2> reverseDirectedGraph = new DirectedGraph<T1, T2>(vertexs, edges);
		return reverseDirectedGraph;
	}
	
	/**
	 * 有向图的可达性
	 */
	public static class Accessibility{
		private Map<Integer, Boolean> marked;
		
		public <T1 extends Comparable<T1>, T2 extends Number> Accessibility(
				DirectedGraph<T1, T2> graph, Set<Integer> vertexIds){
			this.marked = new HashMap<Integer, Boolean>();
			initialize(graph, vertexIds);
		}
		private <T1 extends Comparable<T1>, T2 extends Number> void initialize(
				DirectedGraph<T1, T2> graph, Set<Integer> vertexIds){
			for(int vertexId : vertexIds) {
				if(marked.containsKey(vertexId) && marked.get(vertexId).booleanValue())
					continue;
				depthFirstSearch(graph, vertexId);
			}
		}
		private <T1 extends Comparable<T1>, T2 extends Number> void depthFirstSearch(
				DirectedGraph<T1, T2> graph, int vertexId){
			marked.put(vertexId, true);
			if(graph.adjacency == null || !graph.adjacency.containsKey(vertexId))
				return;
			for(Arc<T2> neighbour : graph.adjacency.get(vertexId)) {
				if(marked.containsKey(neighbour.getId()) && marked.get(neighbour.getId()).booleanValue())
					continue;
				depthFirstSearch(graph, neighbour.getId());
			}
		}
		public Map<Integer, Boolean> result(){
			return Collections.unmodifiableMap(marked);
		}
	}
	
	/**
	 * 寻找有向环
	 */
	public static class CycleDetecting {
		private Map<Integer, Boolean> marked;
		private Map<Integer, Integer> edgeTo;
		private List<Integer> cycleStack;
		private Map<Integer, Boolean> onStack;
		/* 所有构成环的节点 */
		private List<List<Integer>> cycles;
		
		public <T1 extends Comparable<T1>, T2 extends Number> CycleDetecting(DirectedGraph<T1, T2> graph){
			this.marked = new HashMap<Integer, Boolean>();
			this.edgeTo = new HashMap<Integer, Integer>();
			this.cycleStack = null;
			this.onStack = new HashMap<Integer, Boolean>();
			this.cycles = new ArrayList<List<Integer>>();
			initialize(graph);
		}
		private <T1 extends Comparable<T1>, T2 extends Number> void initialize(DirectedGraph<T1, T2> graph){
			Set<Integer> vertexIds = graph.adjacency.keySet();
			for(int vertexId : vertexIds) {
				if(!marked.containsKey(vertexId) || !marked.get(vertexId).booleanValue())
					depthFirstSearch(graph, vertexId);
			}
		}
		private <T1 extends Comparable<T1>, T2 extends Number> void depthFirstSearch(
				DirectedGraph<T1, T2> graph, int vertexId){
			marked.put(vertexId, true);
			if(graph.adjacency == null || !graph.adjacency.containsKey(vertexId))
				return;
			onStack.put(vertexId, true);
			for(Arc<T2> neighbour : graph.adjacency.get(vertexId)) {
				if(!marked.containsKey(neighbour.getId()) || !marked.get(neighbour.getId()).booleanValue()) {
					edgeTo.put(neighbour.getId(), vertexId);
					depthFirstSearch(graph, neighbour.getId());
				}
				else if(onStack.containsKey(neighbour.getId()) && onStack.get(neighbour.getId()).booleanValue()) {
					cycleStack = new LinkedList<Integer>();
					for(int pathFinder = vertexId; pathFinder != neighbour.getId(); pathFinder = edgeTo.get(pathFinder))
						cycleStack.add(0, pathFinder);
					cycleStack.add(0, neighbour.getId());
					cycleStack.add(0, vertexId);
					cycles.add(new ArrayList<Integer>(cycleStack));
				}
			}
			onStack.put(vertexId, false);
		}
		public List<List<Integer>> cycles(){
			return this.cycles;
		}
	}
	/**
	  *  基于深度优先搜索的顶点排序
	 */
	public static class DepthFirstOrder{
		private Map<Integer, Boolean> marked;
		private List<Integer> preorder;
		private List<Integer> postorder;
		private List<Integer> reversePost;
		
		public <T1 extends Comparable<T1>, T2 extends Number> DepthFirstOrder(DirectedGraph<T1, T2> graph){
			this.marked = new HashMap<Integer, Boolean>();
			this.preorder = new LinkedList<Integer>();
			this.postorder = new LinkedList<Integer>();
			this.reversePost = new LinkedList<Integer>();
			initialize(graph);
		}
		private <T1 extends Comparable<T1>, T2 extends Number> void initialize(DirectedGraph<T1, T2> graph) {
			Set<Integer> vertexIds = graph.adjacency.keySet();
			for(int vertexId : vertexIds) {
				if(!marked.containsKey(vertexId) || !marked.get(vertexId).booleanValue())
					depthFirstSearch(graph, vertexId);
			}
		}
		private <T1 extends Comparable<T1>, T2 extends Number> void depthFirstSearch(
				DirectedGraph<T1, T2> graph, int vertexId) {
			preorder.add(vertexId);
			marked.put(vertexId, true);
			if(graph.adjacency != null && graph.adjacency.containsKey(vertexId)) 
				for(Arc<T2> neighbour : graph.adjacency.get(vertexId))
					if(!marked.containsKey(neighbour.getId()) || !marked.get(neighbour.getId()).booleanValue())
						depthFirstSearch(graph, neighbour.getId());
			postorder.add(vertexId);
			reversePost.add(0, vertexId);
		}
		public List<Integer> preorder(){
			return preorder;
		}
		public List<Integer> postorder(){
			return postorder;
		}
		public List<Integer> reversePost(){
			return reversePost;
		}
	}
	/**
	  * 拓扑排序<br/>
	  * 有向无环图的顶点逆后序排列<br/>
	  * 拓扑排序不唯一, 除非满足条件: 当且仅当拓扑排序中每一对相邻顶点之间都存在一条有向边
	  */
	public static class Topological{
		private List<Integer> order;
		
		public Topological() {
			this.order = new LinkedList<Integer>();
		}
		public <T1 extends Comparable<T1>, T2 extends Number>Topological(DirectedGraph<T1, T2> graph){
			DirectedGraph.CycleDetecting cyclefinder = new DirectedGraph.CycleDetecting(graph);
			if(cyclefinder.cycles().isEmpty()) {
				DirectedGraph.DepthFirstOrder depthFirstOrder = new DirectedGraph.DepthFirstOrder(graph);
				order = depthFirstOrder.reversePost();
			}
		}
		public List<Integer> order(){
			return order;
		}
		public boolean isDAG() {
			return order.isEmpty();
		}
	}
	/**
	 * 有向图计算强联通分量
	 */
	public static class Kosaraju{
		private Map<Integer, Boolean> marked;
		private Map<Integer, Integer> group;
		private int count;
		
		public <T1 extends Comparable<T1>, T2 extends Number> Kosaraju(DirectedGraph<T1, T2> graph){
			this.marked = new HashMap<Integer, Boolean>();
			this.group = new HashMap<Integer, Integer>();
			this.count = 0;
			initialize(graph);
		}
		private <T1 extends Comparable<T1>, T2 extends Number> void initialize(DirectedGraph<T1, T2> graph) {
			DirectedGraph.DepthFirstOrder depthFirstOrder = new DirectedGraph.DepthFirstOrder(graph);
			for(int vertexId : depthFirstOrder.reversePost()) {
				if(!marked.containsKey(vertexId) || !marked.get(vertexId).booleanValue()) {
					depthFirstSearch(graph, vertexId);
					count++;
				}
			}
		}
		private <T1 extends Comparable<T1>, T2 extends Number> void depthFirstSearch(
				DirectedGraph<T1, T2> graph, int vertexId) {
			marked.put(vertexId, true);
			group.put(vertexId, count);
			if(graph.adjacency != null && graph.adjacency.containsKey(vertexId))
				for(Arc<T2> neighbour : graph.adjacency.get(vertexId))
					if(!marked.containsKey(neighbour.getId()) || !marked.get(neighbour.getId()).booleanValue())
						depthFirstSearch(graph, neighbour.getId());
		}
		public boolean stronglyConnected(int vertexId1, int vertexId2) {
			if(!group.containsKey(vertexId1) || !group.containsKey(vertexId2))
				return false;
			return group.get(vertexId1) == group.get(vertexId2);
		}
		public int group(int vertexId) {
			if(!group.containsKey(vertexId))
				return -1;
			return group.get(vertexId);
		}
		public int count() {
			return count;
		}
	}
 }
