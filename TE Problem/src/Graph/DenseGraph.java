package Graph;

import java.util.Set;
import java.util.Vector;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;

public class DenseGraph /*extends SimpleDirectedWeightedGraph*/{
	
	private int Vcnt; // Vertex count
	private int Ecnt; // Edge count
	private boolean directGraph; // Directional graph or not
	private Vector<Vector<Edge> > adj; // Adjacency matrix adj[v][w] = edge
	private Vector<Edge> edges_;
	
	public DenseGraph (int V)
	{
		//use JGraphT:
		/*super(Edge.class);
		for (int i = 0; i < V; i++)
		{
			this.addVertex(i);
		}
		this.Vcnt = V;
		this.directGraph = true;
		this.Ecnt = 0;*/
		
		//use PQi:
		this.Vcnt = V;
		this.directGraph = true;
		this.Ecnt = 0;
		this.adj = new Vector<>();
		edges_ = new Vector<Edge>();
		
		for (int i = 0; i <= V; i++)
		{
			adj.add(new Vector<Edge>());
			for (int j = 0; j <= V; j++)
			{
				((Vector)adj.get(i)).add(null); //******* 0 还是 null ?
			}
		}
		
	}
	
	public int V()
	{
		return Vcnt;
		//return super.vertexSet().size();
	}
	
	public int E()
	{
		return Ecnt;
		//return super.edgeSet().size();
	}
	
	public boolean directed()
	{
		return true; //all graphs are directed
	}
	
	public Vector<Edge> getEdges()
	{
		return edges_;
		/*Set set = edgeSet();
		Vector<Edge> edges = new Vector<Edge>(set);
		return edges;*/
	}
	
	public Vector<Vector<Edge> > getAdj()
	{
		return adj;
	}
	
	//insert edge
	public void insert(Edge e)
	{
		//addEdge(e.v, e.w, e);
		
		int v = e.v;
		int w = e.w;
		if ((Edge) ((Vector)adj.get(v)).get(w) == null)
        {
			Ecnt++;
        }
        ((Vector)adj.get(v)).set(w, e);
		edges_.add(e);

		if (directGraph == false)
		{
			((Vector)adj.get(w)).set(v, e);
		}
		
	}
	
	// Remove edge
	public void remove(Edge e) 
	{
		//removeEdge(e);
		
		int v = e.v;
		int w = e.w;

		if ((Edge) ((Vector)adj.get(v)).get(w) != null)
		{
			Ecnt--;
		}
		((Vector)adj.get(v)).set(w, 0);

		if (directGraph == false)
		{
			((Vector)adj.get(w)).set(v, 0);
		}
			
	}
	
	public double Dl() 
	{
		double dl = 0.0;
		for (int i = 0; i < edges_.size(); i++) 
		{
			// Sum of product of capacity c(e) and l(e)
			dl += (getEdges().get(i).length) * (getEdges().get(i).cp); 
		}
		return dl;
	}
	
	public Edge edge(int v, int w)
	{
		//return (Edge) getEdge(v, w);
		return (Edge) ((Vector)adj.get(v)).get(w);
	}
	
	/*public void setEdgeWeights(Edge edge,double w)
	{
		setEdgeWeight(edge, w);
	}*/

}
