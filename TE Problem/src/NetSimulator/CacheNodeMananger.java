package NetSimulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import javafx.util.Pair;

import org.apache.commons.math3.distribution.ZipfDistribution;

import NetSimulator.CacheNode.ObjDemand;

public class CacheNodeMananger {
	
	public class TempRequest 
	{
		int slotId;
		int nodeId;
		int videoId;
		int deviceId;

		TempRequest() 
		{
			slotId = 0;
			nodeId = 0;
			videoId = 0;
			deviceId = 0;
		}
		TempRequest(int slot_id, int node_id, int video_id, int device_id) 
		{
			slotId = slot_id;
			nodeId = node_id;
			videoId = video_id;
			deviceId = device_id;
		}
	}

	private static CacheNodeMananger instance_ = new CacheNodeMananger();
	private Network net_;
	// A list of cache nodes
	private Map<Integer, CacheNode> cache_list_; 
	// indexed by pair <slot, node>, then list of requested video ids
	private Multimap<Pair<Integer, Integer>, TempRequest> request_list_; 

	private int time_slot_;
	private int req_node_num_;
	private int video_num;
	
	
	public CacheNodeMananger()
	{
		request_list_ = ArrayListMultimap.create();
		cache_list_ = new HashMap<Integer, CacheNode>();
	}
	
	public int Init() throws IOException
	{
		//add all cache nodes into cache_list
		File file1 = new File("nodes.dat");
		BufferedReader nodes = null;
		String temp = null;
		int fromid, link_id, node_id;
		try
		{
			nodes = new BufferedReader(new FileReader(file1));
			while((temp = nodes.readLine()) != null)
			{
				String[] array = temp.split("\t");
				node_id = Integer.parseInt(array[0]);

				CacheNode node = null;
				if (cache_list_.containsKey(node_id) == false) 
				{
					node = new CacheNode(node_id);
					cache_list_.put(node_id, node);
				}
				node = cache_list_.get(node_id);
				fromid = Integer.parseInt(array[1]);
				link_id = Integer.parseInt(array[2]);
				
				//System.out.println(node_id+"\t"+fromid+"\t"+link_id);
				
				int i = 2;
				while (link_id != -1)
				{
					node.AddLink(fromid, link_id);
					link_id = Integer.parseInt(array[++i]);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		nodes.close();

		// Read from file to set req_node_num_
		File file2 = new File("placement.dat");
		BufferedReader placement = null;
		int node_number, video_number;
		try
		{
			placement = new BufferedReader(new FileReader(file2));
			temp = placement.readLine();
			String[] array = temp.split("\t");
			node_number = Integer.parseInt(array[0]);
			req_node_num_ = Integer.parseInt(array[1]);
			video_number = Integer.parseInt(array[2]);
			this.video_num = video_number;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		placement.close();

		return 0;
	}
	
	public int GetCacheNodeNum() 
	{
		return cache_list_.size();
	}
	
	public CacheNode GetNode(int nodeid) 
	{
		return cache_list_.get(nodeid); // nodeid begins with 0
	}

	public static CacheNodeMananger GetInstance() 
	{
		if (instance_ == null)
		{
			instance_ = new CacheNodeMananger();
		}
		return instance_;
	}
	public void SetNetwork(Network net) 
	{
		net_ = net;
	}
	
	public Network GetNetwork() 
	{
		return net_;
	}

	//create all devices:
	@SuppressWarnings("resource")
	public void CreateAllDevices(int avg_per_node, double area_radius) throws IOException
	{
		File file1 = new File("device.dat");
		BufferedReader device_in_file=null;
		String temp = null;
		device_in_file = new BufferedReader(new FileReader(file1));
		
		for (Map.Entry<Integer, CacheNode> entry : cache_list_.entrySet()) 
		{
			Map<Integer, DeviceNode> devices = new HashMap<Integer, DeviceNode>();
			for(int i = 0; i < avg_per_node; ++i)
			{
				if((temp = device_in_file.readLine())!=null)
				{
					String[] array = temp.split("\t");
					int node_id = Integer.parseInt(array[0]);
					int device_count = Integer.parseInt(array[1]);
					int device_id = Integer.parseInt(array[2]);
					double x_pos = Double.parseDouble(array[3]);
					double y_pos = Double.parseDouble(array[4]);
					DeviceNode temp_device = new DeviceNode(device_id, x_pos, y_pos, node_id);
					//System.out.println(device_id + "\t"+x_pos+"\t"+y_pos+"\t"+node_id);
					devices.put(device_count, temp_device);
				}
			}
			// Associate these devices with the node
			entry.getValue().AssociateDevices(devices); 
		}
	}
	
	// Generate a new file for requests
	public void GenerateAllRequests(int tot_slot_num, int traffic_density, double exp) throws IOException 
	{
		String filename = "requestpatterns.dat";
		FileOutputStream rp;		
		rp = new FileOutputStream(filename);		
		String str = null;

		for (int time_slot = 0; time_slot < tot_slot_num; time_slot++) 
		{
			for (Map.Entry<Integer, CacheNode> entry : cache_list_.entrySet()) 
			{
				int node_id = entry.getValue().GetId();

				// *********** center node (node 0) does not have direct requests *************
				/*if ((entry.getValue() != null) && (node_id == 0))  
				{
					continue;
				}*/					
				if (node_id >= req_node_num_) // only node 0 to node req_node_num_-1 have requests
				{	
					continue;
				}

				int req_num = entry.getValue().GenerateRequestsNum(traffic_density);
				Vector<Integer> video_ids = entry.getValue().GenerateRequestsVideos(req_num, video_num, exp);
				Vector<Integer> device_ids = entry.getValue().GenerateRequestsDevices(req_num);
				for (int i = 0; i < req_num; ++i) 
				{
					str = time_slot + "\t" + node_id + "\t" + video_ids.get(i) + "\t" + 
				    device_ids.get(i) + "\t" + -1 + "\n";
				    // -1 to indicate end of line
					rp.write(str.getBytes());
				}
			}
		}
		rp.close();
	}
	
	// Read all requests from existing file
	public void ImportRequets() throws IOException 
	{
		File file3 = new File("requestpatterns.dat");
		BufferedReader rp = null;
		String temp=null;
		int temp_slot_id, temp_node_id, temp_video_id, temp_device_id, temp_end;
		try
		{
			rp = new BufferedReader(new FileReader(file3));
			while((temp=rp.readLine())!=null)
			{
				String[] array = temp.split("\t");
				temp_slot_id = Integer.parseInt(array[0]);
				temp_node_id = Integer.parseInt(array[1]);
				temp_video_id = Integer.parseInt(array[2]);
				temp_device_id = Integer.parseInt(array[3]);
				temp_end = Integer.parseInt(array[4]); // temp_end = -1 to indicate end of line
				TempRequest req = new TempRequest(temp_slot_id, temp_node_id, temp_video_id, temp_device_id);
				request_list_.put(new Pair<Integer,Integer>(temp_slot_id, temp_node_id), req);
				//request_list_.insert(std::make_pair(std::make_pair(temp_slot_id, temp_node_id), req));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		rp.close();
	}
	
	// Return Requests at node_id in time_slot
	public Vector<TempRequest> GetWholeReqBlock(int time_slot, int node_id)
	{
		Pair<Integer, Integer> p = new Pair<>(time_slot,node_id);
		Vector<TempRequest> result = new Vector<TempRequest>();
		Collection<TempRequest> it_range = request_list_.get(p);
		int it_range_size = it_range.size();
		TempRequest[] itRange = it_range.toArray(new TempRequest[it_range_size]);
		for (int i = 0; i < it_range_size; i++)
		{
			result.add(itRange[i]);
		}
		return result;
	}
	
	// Return video IDs of remaining requests not fulfilled by user devices (seeds)
	public Vector<TempRequest> FilterRequests(int time_slot, int node_id, double trans_range, int cover_size)
	{
		Pair<Integer, Integer> p = new Pair<>(time_slot,node_id);
		Vector<Integer> newreq_video_ids = new Vector<Integer>();
	    Vector<Integer> newreq_device_ids = new Vector<Integer>();
	    Vector<TempRequest> result = new Vector<TempRequest>(); //Vector<TempRequest>
	    Collection<TempRequest> it_range = request_list_.get(p);
		int it_range_size = it_range.size();
		TempRequest[] itRange = it_range.toArray(new TempRequest[it_range_size]);
		for (int i = 0; i < it_range_size; i++)
		{
			newreq_video_ids.add(itRange[i].videoId);
			newreq_device_ids.add(itRange[i].deviceId);
			result.add(itRange[i]); //Vector<TempRequest>
		}
		int tot_req_num = newreq_video_ids.size();
		if (tot_req_num == 0) 
		{
			return new Vector<TempRequest>();
		}
		if (cover_size == 0) // does not filter any request
		{
			return result;
		}
		
		// Get cover_set of each request device
		Set<Integer> uncovered_req_ids = new HashSet<Integer>();
		Vector<Set<Integer> > all_cover_sets = new Vector<Set<Integer>>();
		for (int i_src = 0; i_src < tot_req_num; ++i_src) 
		{
			int src_device_id = newreq_device_ids.get(i_src);
			Set<Integer> cover_set = new HashSet<Integer>();
			cover_set.add(i_src);
			uncovered_req_ids.add(i_src);

			for (int i_dst = 0; i_dst < tot_req_num; ++i_dst) 
			{
				if (i_dst == i_src)
				{
					continue;
				}
				int dst_device_id = newreq_device_ids.get(i_dst);
				// Get cover_set for devices within transmission range and request same video
				if ((newreq_video_ids.get(i_dst) == newreq_video_ids.get(i_src))
						&& (GetNode(node_id).GetDeviceDistance(src_device_id, dst_device_id) <= trans_range)) 
				{
					cover_set.add(i_dst);
				}
			}
			all_cover_sets.add(cover_set);
		}

		// Run max coverage algorithm to filter out some requests
		Set<Integer> final_sel_req_ids = new HashSet<Integer>();
		while (uncovered_req_ids.isEmpty() == false) 
		{
			Set<Integer> sel_covered_ids = new HashSet<Integer>();
			int max_uncovered_num = 0;
			int sel_req_id = 0;
			int temp_req_id = 0;
			for(Iterator<Set<Integer>> it = all_cover_sets.iterator();it.hasNext();)
			{
				Set<Integer> iteratorSet = it.next();
				Set<Integer> temp_covered_ids = new HashSet<Integer>();
				temp_covered_ids.addAll(iteratorSet);
				temp_covered_ids.retainAll(uncovered_req_ids);
				if (max_uncovered_num < temp_covered_ids.size())
				{
					max_uncovered_num = temp_covered_ids.size();
					
					//swap sel_covered_ids and temp_covered_ids:
					Set<Integer> tempSwapSet = new HashSet<Integer>();
					tempSwapSet.addAll(sel_covered_ids);
					sel_covered_ids.clear();
					sel_covered_ids.addAll(temp_covered_ids);
					temp_covered_ids.clear();
					temp_covered_ids.addAll(tempSwapSet);
					
					sel_req_id = temp_req_id;
				}
				++temp_req_id;
			}
			//System.out.println(max_uncovered_num);
			
			if ((final_sel_req_ids.size() < cover_size) && (max_uncovered_num >= 1)) 
			{
				final_sel_req_ids.add(sel_req_id);
				for(Iterator<Integer> it = sel_covered_ids.iterator(); it.hasNext();)
				{
					int tempInt = it.next();
					uncovered_req_ids.remove(tempInt);
				}
			}
			else
			{
				break;
			}
		}

		Vector<Integer> sel_video_ids = new Vector<Integer>();
		for (Iterator<Integer> it = final_sel_req_ids.iterator(); it.hasNext();) 
		{ // selected
			int tempInt = it.next();
			sel_video_ids.add(newreq_video_ids.get(tempInt));
		}
		for (Iterator<Integer> it = uncovered_req_ids.iterator(); it.hasNext();) 
		{ // uncovered
			int tempInt = it.next();
			sel_video_ids.add(newreq_video_ids.get(tempInt));
		}

		// Video IDs of remaining requests that cannot be fulfilled by user devices: sel_video_ids.
		//return sel_video_ids;
		Vector<TempRequest> filtered_result = new Vector<TempRequest>();
		for(int i = 0; i < sel_video_ids.size(); i++)
		{
			int filtered_videoid = sel_video_ids.get(i);
			int j = 0;
			while(j < result.size())
			{
				if(result.get(j).videoId == filtered_videoid)
				{
					filtered_result.add(result.get(j)); //find the first TempRequest that is sel_video_id
					break;
				}
				j++;
			}
		}
		return filtered_result; 
	}
	
	
	
	// Find sources and paths for incomplete video requests
	public Vector<Double> ProcessRequests(int strategy_mode, int time_slot,
			double preroll_time, double buff_time, double trans_range, int cover_size)
	{
		time_slot_ = time_slot;

		int cur_dmds_num = 0; //current demands number
		int new_dmds_num = 0; //new demands number
		for (Map.Entry<Integer, CacheNode> entry : cache_list_.entrySet()) 
		{
			int node_id = entry.getValue().GetId(); //cache node ID
			// *********** center node (node 0) does not have direct requests *************
			//!!!!!!CHANGE: node 0 also has requests!!!!!!
			/*if ((entry.getValue() != null) && (node_id == 0))  
			{	
				continue;
			}*/
			if (time_slot > 0)
			{
				cur_dmds_num += entry.getValue().UpdateRequestData(time_slot, net_.GetSlotDuration());
			}
			// Filter out requests for each node:
			Vector<TempRequest> sel_video_ids = FilterRequests(time_slot, node_id, trans_range, cover_size);
			//Vector<TempRequest> sel_video_ids = GetWholeReqBlock(time_slot, node_id);

			// Import new requests that cannot be fulfilled by user devices(seeds):
			new_dmds_num += entry.getValue().AddRequests(time_slot, sel_video_ids, preroll_time); 
		}

		for (Map.Entry<Integer, CacheNode> entry : cache_list_.entrySet()) 
		{
			if (buff_time > 0)
			{
				// Apply the adaptive-rate approach:
				entry.getValue().SetRequestRate(time_slot, net_.GetSlotDuration(), buff_time); 
				
				// Apply the fixed-rate approach:
				//entry.getValue().SetRequestRate();
			}
		}

		// Screen video demands with rate 0, which will not be considered:
		int valid_dmds_num = 0;
		for (Map.Entry<Integer, CacheNode> node_entry : cache_list_.entrySet()) 
		{
			Vector<ObjDemand> demands = node_entry.getValue().demand_list_;
			for (int dmd_itr = 0; dmd_itr < demands.size(); dmd_itr++) 
			{
				if (demands.get(dmd_itr).demand > 0)
				{
					valid_dmds_num++;
				}
			}
		}

		net_.SetRemDemandsNum(valid_dmds_num);

		Vector<Double> result = new Vector<Double>();
		if (valid_dmds_num == 0) 
		{
			result.add((double) 0);
			return result;
		}

		// Nearest-source: result[0]: max link utilization; result[1]: target buffer time
		// Max concurrent flow: result[0]: estimate of max link utilization; result[1]: exact max link utilization;
		// result[2]: lower bound of max link utilization; result[3]: target buffer time,
		// Hierarchical flexible approach: result[0]: max link utilization; result[1]: target buffer time

		// Compute flows for video requests
		double epsilon = 0.1;
		if (buff_time > 0) 
		{ // buff_time is preset
			if (strategy_mode == 1) // Nearest source approach
			{ 
				if (result.isEmpty() == true)
				{
					result.add(net_.NearestSource());
				}
				else
				{
					result.set(0, net_.NearestSource());
				}
			}
			else if (strategy_mode == 2)  // Hierarchical routing approach
			{
				if (result.isEmpty())
				{
					result.add(net_.FlexHierFlows_PerSlot());
				}
				else
				{
					result.set(0, net_.FlexHierFlows_PerSlot());
				}
			}
			else if (strategy_mode == 0) //Random source approach
			{
				if (result.isEmpty())
				{
					result.add(net_.RandomSource());
				}
				else
				{
					result.set(0, net_.RandomSource());
				}
			}
			else if (strategy_mode == 3) //Maximum Concurrent Flow
			{
				result = net_.MultiConcurFlow(epsilon);
			}
			
			result.add(buff_time);
			return result;
		}

		//buffer time is not present:
		double util_bound = 0.9; // Upper bound of max link utilization
		int buff_time_min = 0; // Lower bound of target buffer time
		int buff_time_max = 150; // An infeasible upper bound for target buffer time, to be modified
		
		//use binary & secant search to determine the buff_time_1:
		double temp_buff_time_0 = Math.floor(((double)buff_time_max + (double)buff_time_min)/2);
		double utility_0;
		double temp_buff_time_1;
		double utility_1;
		double temp_buff_time_2;
		double utility_2;
		//calculate y0:
		System.out.println("Try buff time: " + temp_buff_time_0 + " in [" + buff_time_min + "," + buff_time_max + "]");getClass();
		for (Map.Entry<Integer, CacheNode> entry : cache_list_.entrySet())
		{
			// Adaptive-rate approach
			entry.getValue().SetRequestRate(time_slot, net_.GetSlotDuration(), (double)temp_buff_time_0);
		}
		if (strategy_mode == 1) // Nearest source approach
		{ 
			if (result.isEmpty())
			{
				result.add(net_.NearestSource());
			}
			else
			{
				result.set(0, net_.NearestSource());
			}
		}
		else if (strategy_mode == 2) // Hierarchical routing approach
		{ 
			if (result.isEmpty())
			{
				result.add(net_.FlexHierFlows_PerSlot());
			}
			else
			{
				result.set(0, net_.FlexHierFlows_PerSlot());
			}
		}
		else if (strategy_mode == 0) //Random source approach
		{
			if (result.isEmpty())
			{
				result.add(net_.RandomSource());
			}
			else
			{
				result.set(0, net_.RandomSource());
			}
		}
		else if (strategy_mode == 3) //Maximum Concurrent Flow
		{
			result = net_.MultiConcurFlow(epsilon);
		}
		utility_0 = result.get(0) - util_bound;
		System.out.println(utility_0);
		
		// Apply binary search to determine buff_time_1:
		if (result.get(0) > 2 * util_bound) 
		{
			result.add((double)temp_buff_time_0);
			temp_buff_time_1 = buff_time_min;
		}
		else if (result.get(0) > util_bound)
		{
			result.add((double)temp_buff_time_0);
			temp_buff_time_1 = (buff_time_min + temp_buff_time_0) / 2;
		}
		else
		{
			result.add((double)temp_buff_time_0);
			temp_buff_time_1 = buff_time_max;
		}
		//System.out.println(temp_buff_time_1);
		
		//calculate y1:
		System.out.println("Try buff time: " + temp_buff_time_1 + " in [" + buff_time_min + "," + buff_time_max + "]");getClass();
		for (Map.Entry<Integer, CacheNode> entry : cache_list_.entrySet())
		{
			// Adaptive-rate approach
			entry.getValue().SetRequestRate(time_slot, net_.GetSlotDuration(), (double)temp_buff_time_1);
		}
		if (strategy_mode == 1) // Nearest source approach
		{ 
			if (result.isEmpty())
			{
				result.add(net_.NearestSource());
			}
			else
			{
				result.set(0, net_.NearestSource());
			}
		}
		else if (strategy_mode == 2) // Hierarchical routing approach
		{ 
			if (result.isEmpty())
			{
				result.add(net_.FlexHierFlows_PerSlot());
			}
			else
			{
				result.set(0, net_.FlexHierFlows_PerSlot());
			}
		}
		else if (strategy_mode == 0) //Random source approach
		{
			if (result.isEmpty())
			{
				result.add(net_.RandomSource());
			}
			else
			{
				result.set(0, net_.RandomSource());
			}
		}
		else if (strategy_mode == 3) //Maximum Concurrent Flow
		{
			result = net_.MultiConcurFlow(epsilon);
		}
		utility_1 = result.get(0) - util_bound;
		//System.out.println(utility_1);
		
		//secant loop:
		while(true)
		{
			//x(n+1) =
			temp_buff_time_2 = temp_buff_time_1 + (utility_1 * (temp_buff_time_0 - temp_buff_time_1) / (utility_1 - utility_0));
			if (temp_buff_time_2 > buff_time_max)
			{
				temp_buff_time_2 = buff_time_max;
			}
			//y(n+1) = 
			System.out.println("Try buff time: " + temp_buff_time_2 + " in [" + buff_time_min + "," + buff_time_max + "]");getClass();
			for (Map.Entry<Integer, CacheNode> entry : cache_list_.entrySet())
			{
				// Adaptive-rate approach
				entry.getValue().SetRequestRate(time_slot, net_.GetSlotDuration(), (double)temp_buff_time_2);
			}
			if (strategy_mode == 1) // Nearest source approach
			{ 
				if (result.isEmpty())
				{
					result.add(net_.NearestSource());
				}
				else
				{
					result.set(0, net_.NearestSource());
				}
			}
			else if (strategy_mode == 2) // Hierarchical routing approach
			{ 
				if (result.isEmpty())
				{
					result.add(net_.FlexHierFlows_PerSlot());
				}
				else
				{
					result.set(0, net_.FlexHierFlows_PerSlot());
				}
			}
			else if (strategy_mode == 0) //Random source approach
			{
				if (result.isEmpty())
				{
					result.add(net_.RandomSource());
				}
				else
				{
					result.set(0, net_.RandomSource());
				}
			}
			else if (strategy_mode == 3) //Maximum Concurrent Flow
			{
				result = net_.MultiConcurFlow(epsilon);
			}
			utility_2 = result.get(0) - util_bound;
			
			//while break condition:
			if (temp_buff_time_2 == buff_time_max)
			{
				result.add((double)temp_buff_time_2);
				break;
			}
			if (Math.abs(utility_2) < 0.000001)
			{
				result.add((double)temp_buff_time_2);
				break;
			}
			if (temp_buff_time_2 < 1.0 && utility_2 + util_bound > util_bound)
			{
				result.add((double)temp_buff_time_2);
				break;
			}
			
			//exchange:
			temp_buff_time_0 = temp_buff_time_1;
			utility_0 = utility_1;
			temp_buff_time_1 = temp_buff_time_2;
			utility_1 = utility_2;
		}
		return result;
	}

}
