package NetSimulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.PriorityQueue;
import java.util.Queue;

import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.google.common.util.concurrent.RateLimiter;

import Graph.DenseGraph;
import Graph.Edge;
import Graph.ShorstPathTree;
import NetSimulator.CacheNode.ObjDemand;

public class Network {
	
	//******public typedef DenseGRAPH<Edge> NetGraph;
	
	private VideoManager video_mgr_; // Video manager
	private CacheNodeMananger node_mgr_; // Cache node manager
	private DenseGraph graph_; // Graph of cache nodes
	private DenseGraph rgraph_; // Reversed graph of cache nodes

	private Vector<CacheNode> cache_nodes_; // List of cache nodes
	private Vector<Vector<Integer> > video_nodes_; // Indexed by video ID, contain a list of cache nodes that stores each video

	private int node_num_; // Total number of all nodes, including cache nodes and request nodes
	private int video_num_; // Total number of different videos in the request library
	private int rem_demands_num_; // Remaining video requests to be fulfilled
	private double slot_duration_; // Unit: seconds
	
	private double RandomOneRealNumber(double from, double to) {

		final int limits = 10000;
		double interval = to - from;

		Random rand =new Random();		
		int value = rand.nextInt(limits);
		double fraction = ((double) value) / limits;
		double data_gen = from + interval * fraction;

		return data_gen;
	}
	

	public Network(double slot_dur)
	{
		slot_duration_ = slot_dur;
		rem_demands_num_ = 0;
		cache_nodes_ = new Vector<CacheNode>();
		video_nodes_ = new Vector<Vector<Integer> >();
		video_mgr_ = new VideoManager();
		node_mgr_ = new CacheNodeMananger();
	}

	public int InitNetwork() throws IOException
	{
		// Read link file, create graph of nodes and links
		File file = new File("links.dat");
		BufferedReader reader=null;
		String temp=null;
		reader=new BufferedReader(new FileReader(file));
		temp = reader.readLine();
		int node_num_ = Integer.parseInt(temp);
		graph_ = new DenseGraph(node_num_);
		rgraph_ = new DenseGraph(node_num_);
		int linkid;
		int from, to;
		double capacity;		
		int count = 0;
		try
		{
			while((temp=reader.readLine())!=null)
			{
				String[] array = temp.split("\t");
				linkid = Integer.parseInt(array[0]);
				from = Integer.parseInt(array[1]);
				to = Integer.parseInt(array[2]);
				capacity = Double.parseDouble(array[3]);
				count++;
				double cost = RandomOneRealNumber(0.5, 3.0);
				// unit cost of this link (from, to), not used in this project:
				cost = Math.pow(2, cost); 
				graph_.insert(new Edge(cost, from, to, capacity));
				rgraph_.insert(new Edge(cost, to, from, capacity)); // Reversed graph
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		reader.close();
		System.out.println("Link Loaded: ");
		
		
		// Initiate cache node manager
		node_mgr_ = CacheNodeMananger.GetInstance();
		node_mgr_.SetNetwork(this);
		if (node_mgr_.Init() != 0) {
			return -1;
		}
		for (int i = 0; i < node_num_; ++i) {
			CacheNode node = node_mgr_.GetNode(i) ;
			cache_nodes_.add(node);
		}
		System.out.println("CacheNode manager initiated");
		
		
		// Initiate video manager
		video_mgr_ = VideoManager.GetInstance();
		video_mgr_.SetNetwork(this);
		if (video_mgr_.Init() != 0) 
		{
			return -1;
		}
		video_num_ = video_mgr_.GetVideoNum();
		for (int i = 0; i < video_num_; ++i) 
		{ // videoid begins with 0
			VideoNode video_node = video_mgr_.GetVideo(i);
			Vector<Integer> temp1 = video_node.GetNodes();
			video_nodes_.add(new Vector<Integer>());
			video_nodes_.set(i, temp1);
			//video_nodes_.get(i).swap(temp1);
		}
		System.out.println("Video manager initiated");
		return 0;
	}

	public double GetSlotDuration() 
	{
		return slot_duration_;
	}

	public CacheNode GetNode(int nodeid) 
	{
		if (nodeid >= cache_nodes_.size()) 
		{
			return null;
		}
		return cache_nodes_.get(nodeid);
	}

	public int GetRemDemandsNum()  
	{
		return rem_demands_num_;
	}

	public void SetRemDemandsNum(int remDemandsNum) 
	{
		rem_demands_num_ = remDemandsNum;
	}


	// TE solution 0 : Use random source for each video request
	public double RandomSource()
	{
		long start=System.currentTimeMillis();   //获取开始时间

		int m = rgraph_.E(); // Number of edges in reversed graph
		Vector<Edge> redges = rgraph_.getEdges();
		for (int i = 0; i < m; ++i) 
		{
			redges.get(i).wt = 1;
			//rgraph_.setEdgeWeight(redges.get(i), 1);
			redges.get(i).flow = 0;
		}
		int node_num = cache_nodes_.size();

		for (int i = 1; i < node_num; ++i) 
		{ // Exclude center node (node 0)
			Vector<ObjDemand> demands = cache_nodes_.get(i).demand_list_;
			
			ShorstPathTree sp = new ShorstPathTree(rgraph_, i);
			//DijkstraShortestPath<Integer, Edge> dijkstraAlg = new DijkstraShortestPath<>(rgraph_);
			//ShortestPathAlgorithm.SingleSourcePaths<Integer, Edge> iPaths = dijkstraAlg.getPaths(i);

			for (Iterator<ObjDemand> itr = demands.iterator(); itr.hasNext();) 
			{
				ObjDemand  objD_itr = itr.next();
				int vid = objD_itr.objId;
				
				// Available cache nodes for this video:
				Vector<Integer> avnodes = video_mgr_.GetVideo(vid).GetNodes(); 

				// Find a random source node
				Random rand = new Random();
				int rand_num = rand.nextInt(avnodes.size());
				int sel_source = avnodes.get(rand_num);
				objD_itr.sourceId = sel_source;
				// Add video demand rate into the flow rate of each edge along the path:
				/*for(Iterator<Edge> iterator = iPaths.getPath(sel_source).getEdgeList().iterator(); iterator.hasNext();)
				{
					Edge e = iterator.next();
					e.flow += objD_itr.demand; 
				}*/
				while (true) 
				{
					Edge e = sp.pathR(sel_source);
					if (sel_source == i)
						break;
					e.flow += objD_itr.demand; // Add video demand rate into the flow rate of each edge along the path
					sel_source = e.v;
				}
			}
		}
		double max_util_exa = 0; // Exact max link utilization
		for (int i = 0; i < m; ++i) 
		{
			double temp = redges.get(i).flow / redges.get(i).cp;
			if (temp > max_util_exa) 
			{
				max_util_exa = temp;
			}
		}
		System.out.print("Max link utilization with nearest source: "+ max_util_exa);

		long end=System.currentTimeMillis();
		System.out.println(", Time: " + (end-start)+"ms"); //以毫秒(ms)计时

		return max_util_exa;
	}
	
	
	//TE solution 1 : Nearest Source
	public double NearestSource()
	{
		long start=System.currentTimeMillis();   //获取开始时间

		int m = rgraph_.E(); // Number of edges in reversed graph
		Vector<Edge> redges = rgraph_.getEdges();
		for (int i = 0; i < m; ++i) 
		{
			redges.get(i).wt = 1;
			//rgraph_.setEdgeWeight(redges.get(i), 1);
			redges.get(i).flow = 0;
		}
		int node_num = cache_nodes_.size();

		for (int i = 1; i < node_num; ++i) 
		{ // Exclude center node (node 0)
			Vector<ObjDemand> demands = cache_nodes_.get(i).demand_list_;
			
			ShorstPathTree sp = new ShorstPathTree(rgraph_, i);
			//DijkstraShortestPath<Integer, Edge> dijkstraAlg = new DijkstraShortestPath<>(rgraph_);
			//ShortestPathAlgorithm.SingleSourcePaths<Integer, Edge> iPaths = dijkstraAlg.getPaths(i);

			for (Iterator<ObjDemand> itr = demands.iterator(); itr.hasNext();) 
			{
				ObjDemand  objD_itr = itr.next();
				int vid = objD_itr.objId;
				//System.out.println(vid);
				
				// Available cache nodes for this video:
				Vector<Integer> avnodes = video_mgr_.GetVideo(vid).GetNodes(); 

				// Find the shortest available node and path
				// Input is reverse graph, so destination node in shortest path tree of reversed graph is source node for video delivery
				int sel_source = avnodes.get(0);
				//double dist = iPaths.getPath(avnodes.get(0)).getWeight();
				double dist = sp.dist(avnodes.get(0));
				if (avnodes.size() > 1) 
				{
					for (int sn = 1; sn < avnodes.size(); ++sn) 
					{
						if (sp.dist(avnodes.get(sn)) < dist) 
						{
							dist = sp.dist(avnodes.get(sn));
							sel_source = avnodes.get(sn);
						}
					}
				}
				objD_itr.sourceId = sel_source;
				while (true) 
				{
					Edge e = sp.pathR(sel_source);
					if (sel_source == i)
						break;
					e.flow += objD_itr.demand; // Add video demand rate into the flow rate of each edge along the path
					//System.out.println(objD_itr.demand);
					sel_source = e.v;
				}
				/*if (avnodes.size() > 1) 
				{
					for (int sn = 1; sn < avnodes.size(); ++sn) 
					{
						if (iPaths.getPath(avnodes.get(sn)).getWeight() < dist) 
						{
							dist = iPaths.getPath(avnodes.get(sn)).getWeight();
							sel_source = avnodes.get(sn);
						}
					}
				}
				objD_itr.sourceId = sel_source;
				// Add video demand rate into the flow rate of each edge along the path:
				for(Iterator<Edge> iterator = iPaths.getPath(sel_source).getEdgeList().iterator(); iterator.hasNext();)
				{
					Edge e = iterator.next();
					e.flow += objD_itr.demand; 
				}*/
			}
		}

		double max_util_exa = 0; // Exact max link utilization
		for (int i = 0; i < m; ++i) 
		{
			double temp = redges.get(i).flow / redges.get(i).cp;
			if (temp > max_util_exa) 
			{
				max_util_exa = temp;
			}
		}
		System.out.print("Max link utilization with nearest source: "+ max_util_exa);

		long end=System.currentTimeMillis();
		System.out.println(", Time: " + (end-start)+"ms"); //以毫秒(ms)计时

		return max_util_exa;
	}
	
	// TE solution 2 : Flexible hierarchical request routing.
	// See: “Design and analysis of collaborative EPC and RAN caching for LTE mobile networks,” Computer Networks, vol. 33, no. 1, pp. 80–95, 2015.
	public class SelectDemand implements Comparable<SelectDemand>
	{
		int req_node_id;
		int demand_index;
		double demand_rate;
		int sel_src_id;
		double rate_diff;
		int direct_link_flag; // 1: direct link; 0: indirect link

		SelectDemand() 
		{
			req_node_id = 0;
			demand_index = 0;
			demand_rate = 0;
			sel_src_id = -1; // not initialized yet
			rate_diff = 0;
			direct_link_flag = 0;
		}
		SelectDemand(int node_id, int dmd_index, double rate, int src_id, double diff, int direct) 
		{
			req_node_id = node_id;
			demand_index = dmd_index;
			demand_rate = rate;
			sel_src_id = src_id;
			rate_diff = diff;
			direct_link_flag = direct;
		}
		@Override
		public int compareTo(SelectDemand o) 
		{
			int sub = new Double(this.rate_diff).compareTo(new Double(o.rate_diff));
			return sub;
		}
	}
	
	void UpdateSelDemand(SelectDemand dmd, int node_id, int dmd_index, double rate, int src_id, double diff, int direct) 
	{
		dmd.req_node_id = node_id;
		dmd.demand_index = dmd_index;
		dmd.demand_rate = rate;
		dmd.sel_src_id = src_id;
		dmd.rate_diff = diff;
		dmd.direct_link_flag = direct;
	}
	
	boolean operator (SelectDemand a, SelectDemand b) 
	{ // For sorting order of priority queue
		return a.rate_diff > b.rate_diff;
	}
	
	public double FlexHierFlows_PerSlot()
	{
		long start  = System.currentTimeMillis(); //start time
		
		int m = rgraph_.E(); // edge_number
		Vector<Edge> redges = rgraph_.getEdges();
		for (int i = 0; i < m; ++i) 
		{
			redges.get(i).wt = 1;
			//rgraph_.setEdgeWeight(redges.get(i), 1);
			redges.get(i).flow = 0;
		}
		int node_num = cache_nodes_.size();
		
		// Initialize status of video demands
		Vector< Vector<Integer> > dmd_status = new Vector<Vector<Integer>>();
		for (int i = 0; i < node_num; ++i) 
		{ 	// Not every node has demands
		    Vector<Integer> dmd_status_per_node = new Vector<Integer>(); // Create an empty row
		    Vector<ObjDemand> demands = cache_nodes_.get(i).demand_list_; //get demands in one node
		    for (int j = 0; j < demands.size(); ++j) 
		    {
		    	dmd_status_per_node.add(0); // Add an element (column) to the row
		    }
		    dmd_status.add(dmd_status_per_node); // Add the row to the main vector
		}
		
		Queue<SelectDemand> dmd_sorted_list = new PriorityQueue<SelectDemand>(); //priority queue
		
		for (int i = 1; i < node_num; ++i)
		{
			Vector<ObjDemand> demands = cache_nodes_.get(i).demand_list_;
			ShorstPathTree sp = new ShorstPathTree(rgraph_, i); //shorest path tree sp
			for (int j = 0; j < demands.size(); ++j)
			{
				if ((dmd_status.get(i)).get(j) == 1)
				{
					continue;
				}
				int vid = demands.get(j).objId; //get video id of the demand
				Vector<Integer> avnodes = video_mgr_.GetVideo(vid).GetNodes(); //nodes that cached this video
				if (avnodes.size() == 0) // Video is not cached in any node
				{
					continue;
				}
				double max_rate_diff = 0; //no more capacity
				int sel_source = 0; //selected source
				int sel_link_type = 0;
				boolean first_direct_link_found = false;
				for (int sn = 0; sn < avnodes.size(); ++sn) //原本是unsigned int
				{
					Edge e0 = rgraph_.edge(i, avnodes.get(sn));
					if (e0 == null) // No direct link
					{
						continue; //pass to the next sn
					}
					sel_link_type = 1; // Direct link = 1 (found a direct link)
					double temp_rate_diff = (e0.cp - e0.flow) - demands.get(j).demand; //rate difference
					if (first_direct_link_found == false)
					{
						first_direct_link_found = true; //(have found a direct link)
						max_rate_diff = temp_rate_diff;
						sel_source = avnodes.get(sn);
					}
					else //first_direct_link_found == true
					{
						if (temp_rate_diff > max_rate_diff) 
						{
							max_rate_diff = temp_rate_diff;
							sel_source = avnodes.get(sn);
						}
					}
				}
				if ((first_direct_link_found == true) && (max_rate_diff >= 0))
				{
					SelectDemand new_dmd = new SelectDemand(i, j, demands.get(j).demand, sel_source, max_rate_diff, sel_link_type);
					dmd_sorted_list.add(new_dmd);
					continue; // Check next demand
				}
				
				
				// Search for an indirect link (find a core node that cached this video); core cache node is 16-20.
				double distance = sp.dist(avnodes.get(0));
				for (int sn = 0; sn < avnodes.size(); ++sn)
				{
					if(avnodes.get(sn) >= 16 && avnodes.get(sn) <= 20)
					{
						sel_source = avnodes.get(sn);
						distance = sp.dist(avnodes.get(sn));
						break; //found the first core cache node that has this video
					}
				}
				//calculate rate_diff:
				double temp_rate_diff = Double.MAX_VALUE;
				int temp_node = sel_source;
				while (true) 
				{
					Edge e = sp.pathR(temp_node);
					if (temp_node == i)
						break;
					if((e.cp - e.flow) < temp_rate_diff)
					{
						temp_rate_diff = e.cp - e.flow;
					}
					temp_node = e.v;
				}
				temp_rate_diff -= demands.get(j).demand; 
				/*
				Edge e1 = rgraph_.edge(i, 0); // source <-- i (request node) in reversed graph
				Edge e2 = rgraph_.edge(0, avnodes.get(0)); //path: request node -> 0 -> first source node
				double temp_rate_diff = Math.min((e1.cp - e1.flow),
						(e2.cp - e2.flow)) - demands.get(j).demand;
						*/
				if (first_direct_link_found == false)
				{
					max_rate_diff = temp_rate_diff;
					//sel_source = avnodes.get(0);
					sel_link_type = 0; // Indirect link = 0
				}
				else //first_direct_link_found == true
				{
					if (temp_rate_diff > max_rate_diff) 
					{
						max_rate_diff = temp_rate_diff;
						//sel_source = avnodes.get(0);
						sel_link_type = 0; //use the indirect link???
					}
				}
				for (int sn = sel_source; sn < avnodes.size(); ++sn) //calculate rate_diff of other core cache nodes
				{
					if(avnodes.get(sn) >= 16 && avnodes.get(sn) <= 20)
					{
						temp_node = avnodes.get(sn);
						while (true) 
						{
							Edge e = sp.pathR(temp_node);
							if (temp_node == i)
								break;
							if((e.cp - e.flow) < temp_rate_diff)
							{
								temp_rate_diff = e.cp - e.flow;
							}
							temp_node = e.v;
						}
						temp_rate_diff -= demands.get(j).demand; 
					}
					/*
					e1 = rgraph_.edge(i, 0);
					e2 = rgraph_.edge(0, avnodes.get(sn));
					temp_rate_diff = Math.min((e1.cp - e1.flow),
							(e2.cp - e2.flow)) - demands.get(j).demand;
					*/
					
					if (temp_rate_diff > max_rate_diff) //choose the source node that can have the largest rate_diff
					{
						max_rate_diff = temp_rate_diff;
						sel_source = avnodes.get(sn);
						sel_link_type = 0; // Indirect link = 0
					}
				}
				SelectDemand new_dmd = new SelectDemand(i, j, demands.get(j).demand,
						sel_source, max_rate_diff, sel_link_type);
				dmd_sorted_list.add(new_dmd);
			}// End of video demands
		}// End of nodes
		
		//add flow:
		while (!dmd_sorted_list.isEmpty())
		{
			SelectDemand next_dmd = dmd_sorted_list.poll(); //return head and remove top
			if ((dmd_status.get(next_dmd.req_node_id)).get(next_dmd.demand_index) == 0) //dmd_status[i][j]
			{
				(dmd_status.get(next_dmd.req_node_id)).set(next_dmd.demand_index,1); //change unprocessed status to processed
			}
			else
			{
				System.out.println("Error.");
			}
			if (next_dmd.direct_link_flag == 1) //first_direct_link_found == true
			{
				Edge e0 = rgraph_.edge(next_dmd.req_node_id,
						next_dmd.sel_src_id);
				e0.flow += next_dmd.demand_rate; //add flows
			}
			else if (next_dmd.direct_link_flag == 0) //first_direct_link_found == false
			{
				ShorstPathTree sp = new ShorstPathTree(rgraph_, next_dmd.req_node_id);
				int sel_source = next_dmd.sel_src_id;
				while (true) 
				{
					Edge e = sp.pathR(sel_source);
					if (sel_source == next_dmd.req_node_id)
						break;
					e.flow += next_dmd.demand_rate; // Add video demand rate into the flow rate of each edge along the path
					//System.out.println(objD_itr.demand);
					sel_source = e.v;
				}
				/*
				Edge e1 = rgraph_.edge(next_dmd.req_node_id, 0);
				Edge e2 = rgraph_.edge(0, next_dmd.sel_src_id);
				e1.flow += next_dmd.demand_rate; //add flows
				e2.flow += next_dmd.demand_rate;
				*/
			}
		}
		// compute Exact max link utilization
		double max_util_exa = 0; 
		for (int i = 0; i < m; ++i) //m = edge_num
		{
			double temp = redges.get(i).flow / redges.get(i).cp;
			if (temp > max_util_exa) 
			{
				max_util_exa = temp;
			}
		}
		System.out.println("Max utilization with hierarchical approach: "+ max_util_exa);
		long end  = System.currentTimeMillis(); //end time
		System.out.println(", Time: " + (end-start) + "ms"); //以毫秒(ms)计时

		return max_util_exa;
	}
	
	@SuppressWarnings("unchecked")
	public double MaxConcurFlow(double epsilon) //epsilon ε is accuracy
	{
		int node_num = cache_nodes_.size();
		int m = rgraph_.E();
		double delta = Math.pow(1.0 + epsilon, -1 * (1 - epsilon) / epsilon) * Math.pow((1 - epsilon) / m, 1 / epsilon); // δ
		Vector<Edge> redges = rgraph_.getEdges();
		
		for (int i = 0; i < m; ++i) 
		{
			redges.get(i).length = delta / redges.get(i).cp; // Initialize l(e) = δ / c(e)
			redges.get(i).wt = redges.get(i).length;
			//rgraph_.setEdgeWeights(redges.get(i), redges.get(i).length); //set weight
			redges.get(i).flow = 0; // Reset flows
		}
		
		int phases = 0; //phase
		double Dl = rgraph_.Dl(); //calculate D(l)
		
		while(Dl < 1)
		{
			phases++; //phase
			//System.out.println(Dl);
			
			for (int i = 0; i < node_num; ++i) //iterator 1 to v do:
			{
				Vector<ObjDemand> demands = cache_nodes_.get(i).demand_list_; //get demands list
				if (demands.size() == 0)
				{
					continue;
				}
				Vector<Double> demand_rate = new Vector<Double>(); // initialize ~d(j,k)
				Vector<Integer> req_ids = new Vector<Integer>(); //request ids
				for (int k = 0; k < demands.size(); k++)
				{
					demand_rate.add(demands.get(k).demand);
					req_ids.add(demands.get(k).objId);
				}
				
				//record and count the number of used source nodes for each demand from node i:
				Map<Integer, Vector<Integer>> used_avnodes = new HashMap<>(); //record used available source nodes for each demand
				int threshhold = 2; //threshold of source nodes
				Map<Integer, Integer> used_source_count = new HashMap<>(); //record the number of used source nodes for each demand
				for(int k = 0; k < demands.size(); ++k)
				{
					// initialize.
					used_avnodes.put(k, new Vector<Integer>());
					used_source_count.put(k, 0);
				}
				
				
				//step
				int step = 0;
				while(rgraph_.Dl()< 1)
				{
					ShorstPathTree sp = new ShorstPathTree(rgraph_, i);
					//DijkstraShortestPath<Integer, Edge> dijkstraAlg = new DijkstraShortestPath<>(rgraph_);
					//ShortestPathAlgorithm.SingleSourcePaths<Integer, Edge> iPaths = dijkstraAlg.getPaths(i);
					boolean no_demand = true;
					Map<Integer, Double> step_demand = new HashMap<>(); //<demand id, flow amount>
					
					for (int k = 0; k < demands.size(); ++k) 
					{ // Go over all commodities of common source
						if (demand_rate.get(k) <= 0) 
						{ // Check only remaining demand
							continue;
						}
						no_demand = false; //still some demands not fullfilled
						int source_id;
						int vid = req_ids.get(k);
						Vector<Integer> avnodes = video_nodes_.get(vid); //available source nodes
						if(used_source_count.get(k) >= threshhold)
						{
							// If achieve threshold, only use used_avnodes:
							avnodes = used_avnodes.get(k);
						}
						
						double dist = sp.dist(avnodes.get(0));
						//double dist = iPaths.getPath(avnodes.get(0)).getWeight();
						
						// Find the shortest available node and path
						source_id = avnodes.get(0);
						
						if (avnodes.size() > 1) {
							for (int sn = 1; sn < avnodes.size(); ++sn) {
								if (sp.dist(avnodes.get(sn)) < dist) {
									dist = sp.dist(avnodes.get(sn));
									source_id = avnodes.get(sn);
								}
							}
						}
						/*if (avnodes.size() > 1) 
						{
							for (int sn = 1; sn < avnodes.size(); ++sn) 
							{
								if (iPaths.getPath(avnodes.get(sn)).getWeight() < dist) 
								{
									dist = iPaths.getPath(avnodes.get(sn)).getWeight();
									source_id = avnodes.get(sn);
								}
							}
						}*/
						
						int sel_node = source_id;
						step_demand.put(k, demand_rate.get(k)); //record demands that will be added into flow
						
						// Add sel_node into used_avnodes and update count:
						int count = used_source_count.get(k);
						count++;
						used_source_count.put(k, count);
						used_avnodes.get(k).add(sel_node);
						
						// Add video demand rate into the flow rate of each edge along the path:
						while (sel_node != i) {
							Edge e = sp.pathR(sel_node);
							e.tmpflow += demand_rate.get(k); 
							sel_node = e.v;
						}
						/*for(Iterator<Edge> iterator = (iPaths.getPath(sel_node).getEdgeList()).iterator(); iterator.hasNext();)
						{
							Edge e = iterator.next();
							e.tmpflow += demand_rate.get(k); 
						}*/
					}
					
					if(no_demand == true)
						break; //***break from while loop: all demands in this node have been completed.
					
					//calculate ρ (find max)
					double rho = 0.0;
					Vector<Edge> addedFlowEdges = new Vector<Edge>(); //edges that added flow
					for (int j = 0; j < m; j++)
					{
						if(redges.get(j).tmpflow > 0.0)
						{
							double temp_rho = redges.get(j).tmpflow / redges.get(j).cp;
							if(temp_rho > rho)
							{
								rho = temp_rho;
							}
							addedFlowEdges.add(redges.get(j));
						}
					}
					rho = rho > 1.0 ? rho : 1.0;
					
					for(Iterator<Edge> itr = addedFlowEdges.iterator(); itr.hasNext();)
					{
						Edge e = itr.next();
						double actual_add_flow = e.tmpflow / rho;   //f = d / ρ
						e.flow += actual_add_flow;   //y(P) = y(P) + f
						e.length *= 1 + epsilon * actual_add_flow / e.cp; //update length: length function
						e.wt = e.length;
						//rgraph_.setEdgeWeight(e, e.length);   //update weight
						e.tmpflow = 0.0;   //reset tempflow
					}
					
					//update ~d(j,k)
					for(Map.Entry<Integer, Double> entry : step_demand.entrySet())
					{
						double f = entry.getValue() / rho;
						f  = demand_rate.get(entry.getKey()) - f;
						demand_rate.set(entry.getKey(), f);   //d = d - f
					}
					step += 1;
				} // end while: step
			} // end for: iterator 1 to v
			Dl = rgraph_.Dl(); //update Dl
		} // end while
		
		//scale down y(P) by log1+ε((1+ε)/δ) -> scaler
		double scaler = Math.log((1 + epsilon) / delta) / Math.log(1 + epsilon);
		for (Iterator<Edge> iterator = redges.iterator(); iterator.hasNext();)
		{
			Edge e = iterator.next();
			e.flow /= scaler; //y(P) = y(P)/scaler
		}
		
		double beta = (phases - 1) / scaler; // phese - 1 = β * scaler
		System.out.println("Estimated beta value is: " + beta);
		return beta; // return β
	}
	
	public Vector<Double> MultiConcurFlow(double epsilon)
	{
		long start = System.currentTimeMillis(); //start time
		// 2-approximation algorithm for β
		// larger β results in more phases.
		// to ensure β is at least 1 but not to large, 1 <= β <= 2.
		int node_num = cache_nodes_.size();
		double epsilon1 = 0.2062994740159; //choose ε properly: (1 - ε)^(-3) = 1 + ω = 2
		double demand_scaler_beta = MaxConcurFlow(epsilon1); // β that used for scaling demand
		System.out.println("beta1: "+demand_scaler_beta);
		
		for(int i = 0; i < node_num; i++)
		{
			Vector<ObjDemand> demands = cache_nodes_.get(i).demand_list_; //get demands at node i
			for(Iterator<ObjDemand> iterator = demands.iterator(); iterator.hasNext();)
			{
				ObjDemand objd = iterator.next();
				objd.demand *= demand_scaler_beta / 2.0; // all the demands are multiplied by ~β/2.
			}
		}
		
		//calculate β using new demands:
		double max_beta_scaled = MaxConcurFlow(epsilon);
		System.out.println("beta2: "+max_beta_scaled);
		//π = :
		double max_beta = max_beta_scaled * demand_scaler_beta / 2.0; //because demands have been multiplied by ~β/2
		double estimate_max_util = 1 / max_beta; // estimated max link utilization: λ = 1/π
		
		// Convert from y(P) to x(P), unscaled to original demands
		Vector<Edge> redges = rgraph_.getEdges();
		double exact_max_util = 0; // Initialize exact max link utilization
		for(Iterator<Edge> itr = redges.iterator(); itr.hasNext();)
		{
			Edge e = itr.next();
			e.flow /= max_beta;  // x(P) = y(P) / π
			//find maximum util
			double temp_max_util = e.flow / e.cp;
			if(temp_max_util > exact_max_util)
			{
				exact_max_util = temp_max_util;
			}
		}
		double max_util_lower = estimate_max_util / Math.pow(1 - epsilon1, -3); //choose ε such that (1-ε)^(-3) = 1+ω
		
		//print
		System.out.print("Max utilization estimate: " + estimate_max_util + 
				", max utilization exat: " + exact_max_util + ", max utilization lower bound: "
				+ max_util_lower);
		
		// Recover original demands
		for(int i = 0; i < node_num; i++)
		{
			Vector<ObjDemand> demands = cache_nodes_.get(i).demand_list_;
			for(Iterator<ObjDemand> iterator = demands.iterator(); iterator.hasNext();)
			{
				ObjDemand objd = iterator.next();
				objd.demand /= (demand_scaler_beta / 2.0);
			}
		}
		//write reuslts file
		Vector<Double> result = new Vector<Double>();
		result.add(estimate_max_util);
		result.add(exact_max_util);
		result.add(max_util_lower);
		
		long end = System.currentTimeMillis(); //end time
		System.out.println(", Time: " + (end - start) + "ms");
		
		return result;
	}

}
