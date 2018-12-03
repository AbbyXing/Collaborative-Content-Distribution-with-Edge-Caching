package NetSimulator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import org.apache.commons.math3.distribution.ZipfDistribution;

import NetSimulator.CacheNodeMananger.TempRequest;

public class CacheNode {
	
	private VideoManager video_mgr_; //VideoManager
	private CacheNodeMananger node_mgr_; //CacheNodeMananger
	
	private long time_slot_;
	private int node_id_;
	private int last_request_id_;
	
	private Map<Integer, Vector<Integer> > links_;
	private Map<Integer, DeviceNode> device_list_;
	
	private int cache_video_num_;
	private Vector<Integer> videos_; //Videos cached at this node
	private Vector<Integer> no_videos_; //Videos not cached at this node
	private int notinsize_; //Number of non-cached videos
	
	public Vector<ObjDemand> demand_list_; // List of video demands for this node
	
	public class ObjDemand {
		int requestId; 
		int objId; //video ID
		int sourceId; //video server ID
		double demand; // demand for video transmission rate -> [demand rate]!!!
		long startSlot; // time slot when video request begins
		double prerollTime; // pre-buffering time before video playback starts
		double remDataSize; // amount of video data not received yet
		double buffDataSize; // amount of buffered data
		double buffPlayTime; // play time of buffered data
		
		public ObjDemand()
		{
			requestId = 0;
			objId = 0;
			sourceId = 0;
			demand = 0;
			startSlot = 0;
			prerollTime = 0;
			remDataSize = 0;
			buffDataSize = 0;
			buffPlayTime = 0;
		}
		
		public ObjDemand(int reqid, int objid, double dmd, long time_slot_, double rem, double pre) 
		{
			requestId = reqid;
			objId = objid;
			demand = dmd;
			startSlot = time_slot_;
			remDataSize = rem;
			prerollTime = pre;

			buffDataSize = 0;
			buffPlayTime = 0;
			sourceId = 0;
		}
	}
	
	private static int getPossionVariable(double lamda) 
	{
		int x = 0;
		double y = Math.random(), cdf = getPossionProbability(x, lamda);
		while (cdf < y) 
		{
			x++;
			cdf += getPossionProbability(x, lamda);
		}
		return x;
	}
	
	private static double getPossionProbability(int k, double lamda) 
	{
		double c = Math.exp(-lamda), sum = 1;
		for (int i = 1; i <= k; i++) 
		{
			sum *= lamda / i;
		}
		return sum * c;
	}
	
	public CacheNode(int nodeid)
	{
		this.node_id_ = nodeid;
		last_request_id_ = -1;
		video_mgr_ = VideoManager.GetInstance();
		node_mgr_ = CacheNodeMananger.GetInstance();
		links_ = new HashMap<Integer, Vector<Integer> >();
		device_list_ = new HashMap<Integer, DeviceNode>();
		videos_ = new Vector<Integer>();
		no_videos_ = new Vector<Integer>();
		demand_list_ = new Vector<ObjDemand>();
	}
	
	public int GetId() 
	{
		return node_id_;
	}
	
	public void AddLink (int to_node, int link_id)
	{
		if(links_.containsKey(to_node) == false)
		{
			links_.put(to_node, new Vector<Integer>());
		}
		links_.get(to_node).add(link_id);
	}
	
	public void SetInNodeVideos(int video_num, Vector<Integer> videos)
	{
		//videos.swap(videos); //videos cached at this node
		cache_video_num_ = video_num;
		
		Vector<Boolean> innode = new Vector<Boolean>();
		notinsize_ = 0;
		for (int i = 0; i< cache_video_num_; i++)
		{
			innode.add(true);
		}
		if(videos.isEmpty() == false)
		{
			for (int i = 0; i < videos.size(); i++)
			{
				innode.set(videos.get(i), false);
			}
		}
		for (int i = 0; i < cache_video_num_; i++)
		{
			if (innode.get(i) == true)
			{
				no_videos_.add(i);
			}
		}
		innode.clear();
		notinsize_ = no_videos_.size();
		Collections.shuffle(no_videos_); //随机重排
		//random_shuffle(no_videos_.firstElement(), no_videos_.lastElement()); //随机重排
	}
	
	public void AssociateDevices(Map<Integer, DeviceNode> devices)
	{
		this.device_list_ = devices;
	}
	
	//get distance between device and BS:
	public double GetDeviceDistanceBS(int device_id)
	{
		DeviceNode device;
		if(device_list_.containsKey(device_id % (device_list_.size())) != false)
		{
			device = device_list_.get(device_id % (device_list_.size()));
		}
		else
		{
			return -1;
		}
		double x, y;
		x = device.GetDeviceLocationX();
		y = device.GetDeviceLocationY();
		
		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}
	
	//get distance between two devices:
	public double GetDeviceDistance(int device_id_1, int device_id_2)
	{
		DeviceNode device_1, device_2;
		if(device_list_.containsKey(device_id_1) != false)
		{
			device_1 = device_list_.get(device_id_1);
		}
		else
		{
			return -1;
		}
		if(device_list_.containsKey(device_id_2) != false)
		{
			device_2 = device_list_.get(device_id_2);
		}
		else
		{
			return -1;
		}
		double x_1, y_1, x_2, y_2;
		x_1 = device_1.GetDeviceLocationX();
		y_1 = device_1.GetDeviceLocationY();
		x_2 = device_2.GetDeviceLocationX();
		y_2 = device_2.GetDeviceLocationY();
		
		return Math.sqrt(Math.pow(x_1 - x_2, 2) + Math.pow(y_1 - y_2, 2));
	}
	
	// All requests of this node
	public int GenerateRequestsNum(int traffic_density)
	{
		int req_num = getPossionVariable(traffic_density);

		return req_num;
	}
	
	public Vector<Integer> GenerateRequestsVideos (int req_num, int video_num,double exp)
	{
		ZipfDistribution distribution = new ZipfDistribution(video_num, exp);
		//CDF of zipf distribution:
		Vector<Double> cdf = new Vector<Double>();
		Vector<Integer> video_ids = new Vector<Integer>();
		for (int i = 0; i < video_num; ++i)
		{
			cdf.add(distribution.cumulativeProbability(i));
		}
		for (int i = 0; i < req_num; ++i)
		{
			double rand_num = Math.random();
			int it = 0;
			while(it < cdf.size())
			{
				if(rand_num < cdf.get(it))
				{
					video_ids.add(it-1);
					break;
				}
				it++;
			}
			if (it == cdf.size())
			{
				video_ids.add(it-1);
			}
		}
		
		return video_ids;
	}
	
	public Vector<Integer> GenerateRequestsDevices (int req_num)
	{
		Random rand = new Random();
		Vector<Integer> device_ids = new Vector<Integer>();
		DeviceNode temp_device_node = null;
		/*for(Map.Entry<Integer, DeviceNode> entry: device_list_.entrySet())
		{
			temp_device_node = entry.getValue();
			if (temp_device_node != null) 
			{
				break;
            }
		}*/
		for (int i = 0; i < req_num; ++i) 
		{
			int min_id = device_list_.get(0).GetDeviceId();
			//int max_id = device_list_.get(device_list_.size()-1).GetDeviceId(); // not end(), which is past-the-end element
			int deviceid = min_id + rand.nextInt(device_list_.size()); // which device initiates the request
			device_ids.add(deviceid);
		}		
		return device_ids;
	}
	
	// Only requests for current time slot
	public int AddRequests(int time_slot,  Vector<TempRequest> cur_requests, double preroll_time)
	{
		int req_num = cur_requests.size();
		int in_count = 0;
		for (int i = 0; i < req_num; ++i) 
		{
			int videoid = cur_requests.get(i).videoId;
			double rem_size = video_mgr_.GetVideo(videoid).video_size_;
			
			//use the log-distance path loss model & Shannon limit to determine the demand rate:
			int deviceid = cur_requests.get(i).deviceId;
			//get the cache_node_id that contains that device_id:
			int cache_node_id;
			if(deviceid < 500)
			{
				cache_node_id = 0;
			}
			else
			{
				cache_node_id = deviceid / 500;
				//cache_node_id ++;
			}
			double d = node_mgr_.GetNode(cache_node_id).GetDeviceDistanceBS(deviceid); //get distance between device & BS
			//System.out.println(d);
			double pathloss_dB = 128.1 + (37.6 * Math.log10(d/1000)); //d in km.
			double pathloss_ratio = Math.pow(10, pathloss_dB/10); //the ratio of transmit power to received power
			double bandwidth = 1.4 * Math.pow(10, 6); //B(w) = 1.4Mhz.
			int RB = 6; //resource blocks = 6.
			double transPower_BS = RB * 316.22776602; //in (mW); BS trans power per RB = 25dBm.
			//exponential distribution:
			double lamda = 1.0;
			double z = Math.random(); //z is between 0 and 1.0
			double x = -(1 / lamda) * Math.log(z);
			double reveivedPower = x * transPower_BS / pathloss_ratio; //in (mW).
			double noisePower = 3.9810717055 * Math.exp(-17) * bandwidth; //in (mW); noise density = -164dBm.
			//Shannon limit:
			double rate = bandwidth * (Math.log((double)1+reveivedPower/noisePower) / Math.log((double)2));
			if(rate > 1000000)
				rate = 1000000;
			//System.out.println(rate);
			
			//double rate = 1.2 * video_mgr_.GetVideo(videoid).code_rate_; // should be improved, this is the transmission rate, flow demand rate
			if (videos_.contains(videoid) == false) 
			{
				int req_id = AddObjDemand(videoid, rate, rem_size, preroll_time);
				in_count++;
			}
		}
		return in_count;
	}
	
	public int AddObjDemand(int objid, double demand, double rem_size, double preroll_time)
	{
		++last_request_id_;
		ObjDemand dmd = new ObjDemand(last_request_id_, objid, demand, time_slot_, rem_size, preroll_time);
		demand_list_.add(dmd);
		return last_request_id_;
	}
	
	// Determine the data transmission rate for the video request: A fixed-rate approach
	public void SetRequestRate() 
	{
		for (int i = 0; i < demand_list_.size(); ++i) 
		{
			VideoNode video = video_mgr_.GetVideo(demand_list_.get(i).objId);
			if (video == null) 
			{
				System.out.println("Video in the demand not found.");
				break;
			}
			else 
			{
				// Transmission demand rate is fixed to be 1.2 times of video coding rate:
				//demand_list_.get(i).demand = 1.2 * video.code_rate_; 
				// Transmission demand rate is fixed to be log-distance model: do nothing
			}
		}
	}
	
	// Determine the data transmission rate for the video request: An adaptive-rate approach
	public void SetRequestRate(int time_slot, double slot_duration, double buff_time) 
	{
		for (int i = 0; i < demand_list_.size(); ++i) 
		{
			VideoNode video = video_mgr_.GetVideo(demand_list_.get(i).objId);
			if (video == null) 
			{
				System.out.println("Video in the demand not found.");
				break;
			}
			else 
			{
				// Determine the data transmission rate so that the buffered data can last for a duration defined in buff_time
				double update_dmd_rate;
				double total_buff_time = demand_list_.get(i).buffDataSize / video.code_rate_;
				double temp = (time_slot - demand_list_.get(i).startSlot) * slot_duration - demand_list_.get(i).prerollTime;
				double play_time = (temp > 0) ? temp : 0;
				double rem_play_time = total_buff_time - play_time; // can be negative

				double rem_data_time = demand_list_.get(i).remDataSize / video.code_rate_;
				// buff_time is not longer than remaining duration
				double valid_buff_time = (buff_time <= rem_data_time) ? buff_time : rem_data_time; 

				if (rem_play_time >= valid_buff_time) 
				{
					update_dmd_rate = 0;
				}
				else 
				{
					update_dmd_rate = (valid_buff_time - rem_play_time) * video.code_rate_ / slot_duration;
				}
				if(demand_list_.get(i).demand > update_dmd_rate)
				{
					demand_list_.get(i).demand = update_dmd_rate;
				}
			}
		}
	}
	
	public int UpdateRequestData(int time_slot, double slot_duration) 
	{
		time_slot_ = time_slot;
		Vector<Integer> finish_dmd_ids = new Vector<Integer>(); // Some video requests will be finished after this time slot
		for (int i = 0; i < demand_list_.size(); ++i) 
		{
			VideoNode video = video_mgr_.GetVideo(demand_list_.get(i).objId);
			if (video == null) 
			{
				System.out.println("Video in the demand not found.");
				break;
			}
			else 
			{
				// Update each video request
				demand_list_.get(i).buffDataSize += demand_list_.get(i).demand * slot_duration;
				demand_list_.get(i).remDataSize -= demand_list_.get(i).demand * slot_duration;
				demand_list_.get(i).buffPlayTime = demand_list_.get(i).buffDataSize / video.code_rate_;
				double play_time = (time_slot - demand_list_.get(i).startSlot) * slot_duration - demand_list_.get(i).prerollTime;
				// Video playback has started
				if (play_time > 0) 
				{
					play_time = (play_time <= video.duration_) ? play_time : video.duration_;
					demand_list_.get(i).buffPlayTime -= play_time;
				}

				if (demand_list_.get(i).remDataSize <= 0)
				{
					//System.out.println(i);
					finish_dmd_ids.add(i); // Record index of video demand that is finished and to be removed
				}
			}
		}
		// Remove completed video demands from the highest index to keep element order
		//System.out.println(finish_dmd_ids.size());
		int i = finish_dmd_ids.size() - 1;
		while (i >= 0) 
		{
			demand_list_.remove(0 + finish_dmd_ids.get(i));
			//System.out.println(finish_dmd_ids.get(i));
			i--;
			//System.out.println(demand_list_.size());
			//demand_list_.erase(demand_list_.begin() + finish_dmd_ids[i]);
		}

		return demand_list_.size();
	}

}
