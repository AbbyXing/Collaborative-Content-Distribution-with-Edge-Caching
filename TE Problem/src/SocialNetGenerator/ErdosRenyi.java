package SocialNetGenerator;
/******************************************************************************
 * 
 * Repeatedly add random edges (with replacement) to a graph on n
 * vertices until the graph is connected.
 * 
 *Implements a network generator for the so-called Erdos-Renyi random graph 
 * model G(n,M). It was first described by Erdos and Renyi in their Book 
 * "On random graphs I" in 1959. The model generates a network topology G(n,M) 
 * with a given number of nodes (n) and a specified number of edges (M). Until 
 * the targeted number of edges in the system is reached, two nodes are selected 
 * uniformly at random and an edge between them is created if it does not 
 * already exist. 
 ******************************************************************************/

public class ErdosRenyi {

	int v; // number of devices
	
	// number of random edges (with replacement) needed for an n-vertex
    // graph to become connected
	
	public ErdosRenyi(int v) 
	{
		this.v = v;
	}
	public static int count(int n) 
	{
		int edges = 0;
		
		return edges;
	}
    public static void main(String[] args) 
    {
     
    }

}
