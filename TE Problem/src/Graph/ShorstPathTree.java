package Graph;

import java.util.Vector;

public class ShorstPathTree {
	
	private DenseGraph G;
	private Vector<Edge> spt; // Shortest path tree
	private Vector<Double> wt; // Total weight to reach node i over shortest path
	
	public Edge pathR (int v)  
	{ // Return an immediate edge on the shortest path tree that reaches vertex v
		return spt.get(v);
	}
	
	public double dist (int v) 
	{
		return wt.get(v);
	}

	public ShorstPathTree (DenseGraph G, int s)
	{
		this.G = G;
		this.spt = new Vector<Edge>();
		for (int i = 0; i< G.V(); i++)
		{
			spt.add(i, null);
		}
		this.wt = new Vector<Double>();
		for (int i = 0; i < G.V(); i++)
		{
			wt.add(i, (double) G.V());
		}

		PQi pQ = new PQi(G.V(), wt);
		for (int v = 0; v < G.V(); v++)
		{
			pQ.insert(v);
		}
		wt.set(s, 0.0);
		pQ.lower(s);
		while (pQ.empty() == false)
		{
			int v = pQ.getmin();
			//System.out.println(v);
			if (v != s && spt.get(v) == null)
			{
				return;
			}
			adjIterator A = new adjIterator(G, v);
			for (Edge e = A.beg(); !A.end(); e = A.nxt())
			{
				int w = e.w;
				double P = wt.get(v) + e.wt;
				if (P < wt.get(w))
				{
					wt.set(w, P);
					pQ.lower(w);
					spt.set(w, e);
				}
			}
		}
	}


}
