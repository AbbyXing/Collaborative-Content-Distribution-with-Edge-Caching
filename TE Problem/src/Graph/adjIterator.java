package Graph;

public class adjIterator{
	
	private DenseGraph G;
	private int i;
	private int v;
	
	public adjIterator(DenseGraph G, int v)
	{
		this.G = G;
		this.v = v;
		this.i = 0;
	}
	
	public Edge beg()
	{
		i = -1;
		return nxt();
	}
	
	public Edge nxt()
	{
		for(i++; i < G.V(); i++) //******为啥有两个 i++ ?
		{
			if(G.edge(v, i) != null)
			{
				return G.getAdj().get(v).get(i);
			}
		}
		return null;
	}
	
	public boolean end()
	{
		return i >= G.V();
	}
}
