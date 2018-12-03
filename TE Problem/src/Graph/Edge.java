package Graph;

import org.jgrapht.graph.DefaultWeightedEdge;

public class Edge /*extends DefaultWeightedEdge*/{
	
	public int v, w; // v, w: end nodes of edge v-->w
	public double cp; // Capacity of edge
	public double wt; // Weight of edge
	public double flow; // Flow amount over edge
	public double tmpflow;
	public double cost; // Unit cost of edge
	public double length; // Length of edge
	
	public Edge (double c, int v, int w, double cp)
	{
		//super();
		wt = 0.0;
		flow = 0.0;
		tmpflow = 0.0;
		
		this.v = v;
		this.w = w;
		this.cp = cp;		
		this.cost = c;
	}

}
