package NetSimulator;

import java.util.Vector;

public class VideoNode {

	public int video_id_; //begins with 0
	public int video_size_;
	public double code_rate_;
	public int duration_;
	private Vector<Integer> cache_nodes_;
	
	public VideoNode (int video_id)
	{
		video_id_ = video_id;
		cache_nodes_ = new Vector<Integer>();
	}
	
	public void AddCacheNode (int node_id)
	{
		cache_nodes_.add(node_id);
	}
	
	public Boolean IsinCacheNode (int node_id)
	{
		if((cache_nodes_.indexOf(node_id)) != -1)
		{
			return true;
		}
		return false;
	}
	
	public Vector<Integer> GetNodes ()
	{
		return cache_nodes_;
	}
	
	public int ReportCacheNum ()
	{
		return cache_nodes_.size();
	}
}
