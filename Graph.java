package utility;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 图<br/><br/>
 * 
 * <div>
 * <div>
 * 这是一个抽象类, 有四个成员变量:
 * </div><br/>
 * <ul>
 *  <li><b>边集 (<code>edgeSet</code>)</b> : 边经过检查(去重, 排除自环)后的集合</li>
 * 	<li><b>顶点字典 (<code>dictionary</code>)</b> : key = 顶点id, value = 顶点本身</li>
 * 	<li><b>邻接表 (<code>adjacency</code>)</b> : key = 顶点id, value = 从 <code>key</code> 表示的顶点出发, 能到达相邻节点的 <b>弧 (<code>Arc</code>)</b></li>
 * 	<li><b>逆邻接表 (<code>inverseAdjacency</code>)</b> : key = 顶点id, value = 从其它顶点出发, 能到达 <code>key</code> 表示的顶点的 <b>弧 (<code>Arc</code>)</b></li>
 * </ul>
 * <div>
 * 	构造器内完成初始化 (邻接表和逆邻接表的构造), 顺序为先完成顶点 (<code>Vertex</code>) 的初始化, 确定图中顶点的数量和标志, 
 *  然后再考虑 边 (<code>Edge</code>) 的初始化，一旦输入的边有任意一个顶点id没有在 顶点字典 (<code>dictionary</code>) 内, 就应该舍弃这条边
 *  </div><br/>
 *  <div>
 *  初始化时调用了一个抽象方法 <b><code>addEdge(Edge edge)</code></b>,这个方法之所以要定义为抽象的, 
 *  是因为要考虑<b>有向图</b>和<b>无向图</b>的差异。对于无向图而言, 逆邻接表应该永远为空, 
 *  也即 <b><code>inverseAdjacency.isEmpty() == true</code></b> 永远成立
 *  </div><br/>
 *  <div>
 *  在实例化图的子类特别是无向带权图时, 应当尽量使用 <code>LinkedHashSet&ltEdge&gt</code>, 防止集合插入顺序不同带来的权值不符合预期的现象 
 *  </div>
 * </div><br/>
 *
 * @param <T1> 要求实现 Comparable&ltT1&gt 接口, 可用作节点的关键字排序
 * @param <T2> 要求实现 Number 接口, 用于计算带权图
 */
public abstract class Graph<T1 extends Comparable<T1>, T2 extends Number> {
	protected int countOfArc;
	protected Set<Edge<T2>> edgeSet;
	protected Map<Integer, Vertex<T1>> dictionary;
	protected Map<Integer, Set<Arc<T2>>> adjacency;
	protected Map<Integer, Set<Arc<T2>>> inverseAdjacency;
	
	public Graph() {
		this.edgeSet = new HashSet<Edge<T2>>();
		this.dictionary = new HashMap<Integer, Vertex<T1>>();
		this.adjacency = new HashMap<Integer, Set<Arc<T2>>>();
		this.inverseAdjacency = new HashMap<Integer, Set<Arc<T2>>>();
	}
	
	public Graph(Set<Vertex<T1>> vertexs) {
		this();
		initializeVertexs(vertexs);
	}
	
	public Graph(Set<Vertex<T1>> vertexs, Set<Edge<T2>> edges){
		this(vertexs);
		initializeEdges(edges);
	}
	private void initializeVertexs(Set<Vertex<T1>> vertexs) {
		if(vertexs == null)
			return;
		Iterator<Vertex<T1>> iterator = vertexs.iterator();
		Vertex<T1> vertex = null;
		while(iterator.hasNext()) {
			vertex = iterator.next();
			dictionary.put(vertex.getId(), vertex);
		}
	}
	private void initializeEdges(Set<Edge<T2>> edges) {
		if(edges == null || edges.isEmpty())
			return;
		Iterator<Edge<T2>> edge = edges.iterator();
		while(edge.hasNext())
			addEdge(edge.next());
	}
	
	public int countOfVertex() {
		return dictionary.size();
	}
	
	public int countOfArc() {
		return countOfArc;
	}
	
	public int recalculateCountOfArc() {
		int result = 0;
		for(int vertexId : adjacency.keySet())
			result += adjacency.get(vertexId).size();
		countOfArc = result;
		return countOfArc;
	}
	
	public abstract boolean addVertex(Vertex<T1> vertex);
	
	public abstract boolean addEdge(Edge<T2> edge);
	
	public Map<Integer, Set<Arc<T2>>> unmodifiableAdjacency(){
		return Collections.unmodifiableMap(adjacency);
	}
	public Map<Integer, Set<Arc<T2>>> unmodifiableInverseAdjacency(){
		return Collections.unmodifiableMap(inverseAdjacency);
	}
	public Map<Integer, Vertex<T1>> unmodifiableVertexDictionary(){
		return Collections.unmodifiableMap(dictionary);
	}
	public Set<Edge<T2>> unmodifiableEdgeSet(){
		return Collections.unmodifiableSet(edgeSet);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(adjacency, inverseAdjacency);
	}
	
	@Override
	public boolean equals(Object object) {
		if(this == object)
			return true;
		if(!(object instanceof Graph))
			return false;
		@SuppressWarnings("unchecked")
		Graph<T1, T2> another = (Graph<T1, T2>) object;
		boolean adjacencyEquals = this.adjacency.equals(another.adjacency);
		boolean inverseAdjacencyEquals = this.inverseAdjacency.equals(another.inverseAdjacency);
		return adjacencyEquals && inverseAdjacencyEquals;
	}
	@Override
	public String toString() {
		return String.format("Graph {vertexDictionary=%s, adjacency=%s, inverseAdjacency=%s}", 
				dictionary, adjacency, inverseAdjacency);
	}
	
	/**
	 * 深度优先搜索路径
	 */
	public static class DepthFirstPaths{
		/* key=节点id, value=该节点是否被标记过, 取值true|false */
		private Map<Integer, Boolean> marked;
		/* key=当前的目的地节点, value=目的地节点的前一个节点 */
		private Map<Integer, Integer> from;
		private int startid;
		
		
		/**
		 * DEBUG
		 * @param <T1>
		 * @param <T2>
		 * @param graph
		 * @param sources
		 */
		public <T1 extends Comparable<T1>, T2 extends Number> DepthFirstPaths(Graph<T1, T2> graph, Iterable<Integer> sources){
			Set<Integer> vertexIds = graph.unmodifiableVertexDictionary().keySet();
			marked = new HashMap<Integer, Boolean>(vertexIds.size());
			from = new HashMap<Integer, Integer>();
			Iterator<Integer> iterator = sources.iterator();
			while(iterator.hasNext()) {
				int vertexId = iterator.next();
				if(marked.containsKey(vertexId) && marked.getOrDefault(vertexId, false))
					continue;
				depthFirstSearch(graph, vertexId);
			}
		}
		
		public <T1 extends Comparable<T1>, T2 extends Number> DepthFirstPaths(Graph<T1, T2> graph, int startid) {
			Set<Integer> vertexIds = graph.unmodifiableVertexDictionary().keySet();
			marked = new HashMap<Integer, Boolean>(vertexIds.size());
			from = new HashMap<Integer, Integer>();
			this.startid = startid;
			depthFirstSearch(graph, startid);
		}
		private <T1 extends Comparable<T1>, T2 extends Number> void depthFirstSearch(Graph<T1, T2> graph, int origin) {
			marked.put(origin, true);
			Map<Integer, Set<Arc<T2>>> adjacency = graph.unmodifiableAdjacency();
			if(adjacency == null || !adjacency.containsKey(origin))
				return;
			for(Arc<T2> arc : adjacency.get(origin)) {
				if(marked.containsKey(arc.id) && marked.get(arc.id).booleanValue())
					continue;
				from.put(arc.id, origin);
				depthFirstSearch(graph, arc.id);
			}
		}
		
		public boolean hasPathTo(int destination) {
			return marked.containsKey(destination) && marked.get(destination).booleanValue();
		}
		
		public List<Integer> pathTo(int destination){
			List<Integer> path = new LinkedList<Integer>();
			if(!hasPathTo(destination))
				return path;
			int currentId = destination;
			while(currentId != startid && from.containsKey(currentId)) {
				path.add(0, currentId);
				currentId = from.get(currentId);
			}
			path.add(0, startid);
			return path;
		}
	}
	/**
	 * 广度优先搜索路径
	 */
	public static class BreadthFirstPaths{
		/* key=节点id, value=该节点是否被标记过, 取值true|false */
		private Map<Integer, Boolean> marked;
		/* key=当前的目的地节点, value=目的地节点的前一个节点 */
		private Map<Integer, Integer> from;
		private int startid;
		
		public <T1 extends Comparable<T1>, T2 extends Number> BreadthFirstPaths(Graph<T1, T2> graph, int startid) {
			this.marked = new HashMap<Integer, Boolean>();
			this.from = new HashMap<Integer, Integer>();
			this.startid = startid;
			breadthFirstSearch(graph, startid);
		}
		private <T1 extends Comparable<T1>, T2 extends Number> void breadthFirstSearch(Graph<T1, T2> graph, int origin) {
			List<Integer> queue = new LinkedList<Integer>();
			marked.put(origin, true);
			queue.add(origin);
			Map<Integer, Set<Arc<T2>>> adjacency = graph.unmodifiableAdjacency();
			if(!adjacency.containsKey(origin))
				return;
			Set<Arc<T2>> neighbours = null;
			while(!queue.isEmpty()) {
				int current = queue.remove(0);
				neighbours = adjacency.get(current);
				if(neighbours == null || neighbours.isEmpty())
					continue;
				for(Arc<T2> neighbour : neighbours) {
					if(marked.containsKey(neighbour.id) && marked.get(neighbour.id).booleanValue())
						continue;
					from.put(neighbour.id, current);
					marked.put(neighbour.id, true);
					queue.add(neighbour.id);
				}
			}
		}
		
		public boolean hasPathTo(int destination) {
			return marked.containsKey(destination) && marked.get(destination).booleanValue();
		}
		
		public List<Integer> pathTo(int destination){
			List<Integer> path = new LinkedList<Integer>();
			if(!hasPathTo(destination))
				return path;
			int currentId = destination;
			while(currentId != startid && from.containsKey(currentId)) {
				path.add(0, currentId);
				currentId = from.get(currentId);
			}
			path.add(0, startid);
			return path;
		}
	}
	/**
	 * 寻找所有连通分量 
	 */
	public static class ConnectedComponent{
		private Map<Integer, Boolean> marked;
		/* key=节点id, value=所属连通分量的编号 */
		private Map<Integer, Integer> group;
		private int count;
		
		public <T1 extends Comparable<T1>, T2 extends Number> ConnectedComponent(Graph<T1, T2> graph){
			marked = new HashMap<Integer, Boolean>();
			group = new HashMap<Integer, Integer>();
			count = 0;
			initialize(graph);
		}
		private <T1 extends Comparable<T1>, T2 extends Number> void initialize(Graph<T1, T2> graph) {
			Set<Integer> vertexIds = graph.dictionary.keySet();
			for(int vertexId : vertexIds) {
				if(marked.containsKey(vertexId) && marked.get(vertexId).booleanValue())
					continue;
				depthFirstSearch(graph, vertexId);
				count++;
			}
		}
		private <T1 extends Comparable<T1>, T2 extends Number> void depthFirstSearch(Graph<T1, T2> graph, int vertexId) {
			marked.put(vertexId, true);
			group.put(vertexId, count);
			if(graph.adjacency == null || !graph.adjacency.containsKey(vertexId))
				return;
			for(Arc<T2> neighbour : graph.adjacency.get(vertexId)) {
				if(marked.containsKey(neighbour.id) && marked.get(neighbour.id).booleanValue())
					continue;
				depthFirstSearch(graph, neighbour.id);
			}
		}
		
		public boolean connected(int vertexId1, int vertexId2) {
			return group.get(vertexId1) == group.get(vertexId2);
		}
		public int whichGroup(int vertexId) {
			return group.get(vertexId);
		}
		public int groupCount() {
			return count;
		}
	}
	/**
	 * 弧<br/><br/>
	 * 
	 * 注意和 <b>边 <code>Edge</code></b> 的区别<br>
	 * <ul>
	 * 	<li><b>弧 (<code>Arc</code>)</b> 的实例对象是经过 <b>图 (<code>Graph</code>)</b> 初始化后而来, 赋值保证合法; <b>边 (<code>Edge</code>)</b> 则没有要求 
	 * 	<li><b>弧 (<code>Arc</code>)</b> 参与组成邻接表 (<code>adjacency</code>) 和 逆邻接表 (<code>inverseAdjacency</code>); <b>边 (<code>Edge</code>)</b> 作为外部输入数据的数据结构</li>
	 * </ul>
	 *
	 * @param <T>
	 */
	public static class Arc<T extends Number>{
		private int id;
		private T weight;
		private int fromId;
		
		public Arc(int id, int fromId) {
			this.id = id;
			this.fromId = fromId;
		}
		public Arc(int id, T weight, int fromId) {
			this(id, fromId);
			this.weight = weight;
		}

		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public T getWeight() {
			return weight;
		}
		public void setWeight(T weight) {
			this.weight = weight;
		}
		public int getFromId() {
			return fromId;
		}
		public void setFromId(int fromId) {
			this.fromId = fromId;
		}
		@Override
		public int hashCode() {
			return Objects.hash(id, fromId);
		}
		@Override
		public boolean equals(Object object) {
			if(this == object)
				return true;
			if(!(object instanceof Arc))
				return false;
			@SuppressWarnings("unchecked")
			Arc<T> another = (Arc<T>) object;
			return another.fromId == this.fromId && another.id == this.id;
		}
		@Override
		public String toString() {
			return "{id=" + id + ", cost=" + weight + ", fromId=" + fromId + "}";
		}
	}
	/**
	 * 顶点类
	 *
	 * @param <T>
	 */
	public static class Vertex<T extends Comparable<T>>{
		protected int id;
		protected T comparableKey;
		
		public Vertex(int id, T comparableKey) {
			this.id = id;
			this.comparableKey = comparableKey;
		}
		
		@Override
		public String toString() {
			return String.format("{id=%d, comparableKey=%s}", id, comparableKey);
		}
		public int getId() {
			return id;
		}
		public T getKey() {
			return comparableKey;
		}
		public void setKey(T comparableKey) {
			this.comparableKey = comparableKey;
		}
		@Override
		public int hashCode() {
			return Objects.hash(id);
		}
		@Override
		public boolean equals(Object o) {
			if(o == this)
				return true;
			if(!(o instanceof Vertex))
				return false;
			@SuppressWarnings("unchecked")
			Vertex<T> another =  (Vertex<T>) o;
			return another.id == this.id;
		}
	}
	/**
	 * 边
	 *
	 * @param <T>
	 */
	public static class Edge<T extends Number>{
		private int startid;
		private int endid;
		private T weight;
		
		public Edge(int startid, int endid) {
			this.startid = startid;
			this.endid = endid;
		}
		public Edge(int startid, int endid, T weight) {
			this(startid, endid);
			this.weight = weight;
		}
		
		public int getStartid() {
			return startid;
		}
		public void setStartid(int startid) {
			this.startid = startid;
		}
		public int getEndid() {
			return endid;
		}
		public void setEndid(int endid) {
			this.endid = endid;
		}
		public T getWeight() {
			return weight;
		}
		public void setWeight(T weight) {
			this.weight = weight;
		}
		@Override
		public String toString() {
			return String.format("{startid=%d, endid=%d, weight=%s}", startid, endid, weight);
		}
		@Override
		public int hashCode() {
			return Objects.hash(startid, endid);
		}
		@Override
		public boolean equals(Object obj) {
			if(obj == this)
				return true;
			if(!(obj instanceof Edge))
				return false;
			@SuppressWarnings("unchecked")
			Edge<T> another = (Edge<T>) obj;
			boolean isSame = (this.startid == another.startid && this.endid == another.endid);
			return isSame;
		}
	}
	
	public static void main(String[] args) {
		Set<Graph.Vertex<Integer>> vertexs = new HashSet<Graph.Vertex<Integer>>();
		for(int i=0; i<13; i++)
			vertexs.add(new Graph.Vertex<Integer>(i, i));
		Set<Graph.Edge<Integer>> edges = new LinkedHashSet<Graph.Edge<Integer>>();
		edges.add(new Graph.Edge<Integer>(0, 1, 1));
		edges.add(new Graph.Edge<Integer>(0, 5, 1));
		edges.add(new Graph.Edge<Integer>(0, 6, 1));
		edges.add(new Graph.Edge<Integer>(2, 0, 1));
		edges.add(new Graph.Edge<Integer>(2, 3, 1));
		edges.add(new Graph.Edge<Integer>(3, 2, 1));
		edges.add(new Graph.Edge<Integer>(3, 5, 1));
		edges.add(new Graph.Edge<Integer>(4, 2, 1));
		edges.add(new Graph.Edge<Integer>(4, 3, 1));
		edges.add(new Graph.Edge<Integer>(5, 4, 1));
		edges.add(new Graph.Edge<Integer>(6, 0, 1));
		edges.add(new Graph.Edge<Integer>(6, 4, 1));
		edges.add(new Graph.Edge<Integer>(6, 9, 1));
		edges.add(new Graph.Edge<Integer>(7, 6, 1));
		edges.add(new Graph.Edge<Integer>(7, 8, 1));
		edges.add(new Graph.Edge<Integer>(8, 7, 1));
		edges.add(new Graph.Edge<Integer>(8, 9, 1));
		edges.add(new Graph.Edge<Integer>(9, 10, 1));
		edges.add(new Graph.Edge<Integer>(9, 11, 1));
		edges.add(new Graph.Edge<Integer>(9, 12, 1));
		edges.add(new Graph.Edge<Integer>(10, 12, 1));
		edges.add(new Graph.Edge<Integer>(11, 4, 1));
		edges.add(new Graph.Edge<Integer>(11, 12, 1));
		edges.add(new Graph.Edge<Integer>(12, 9, 1));
		DirectedGraph<Integer, Integer> graph = new DirectedGraph<Integer, Integer>(vertexs, edges);
		DirectedGraph.Kosaraju kosaraju = new DirectedGraph.Kosaraju(graph);
		System.out.println(kosaraju.stronglyConnected(13, 14));
	}
}
