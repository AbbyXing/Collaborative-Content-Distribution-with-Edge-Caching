package NetSimulator;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import com.sun.xml.internal.bind.v2.model.core.ID;

import java.io.FileOutputStream ;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class VideoManager {
	
	private Network net_; //The associated with Network class
	public static VideoManager instance_ = new VideoManager();
	public Map<Integer, VideoNode> video_list_; //The list of managed videos
	
	public VideoManager ()
	{
		video_list_ = new HashMap<Integer, VideoNode>();
	}
	
	public int Init () throws IOException
	{
		int node_number;
		int req_node_number;
		int video_number = 200;
		int video_id_count = 0;
		
		// Read video description file
		File file2 = new File("video.dat"); //Youtube dataset.
		BufferedReader video_in_file=null;
		String temp = null;
		try
		{
			video_in_file = new BufferedReader(new FileReader(file2));
			while((temp=video_in_file.readLine())!=null)
			{
				double code_rate = 0;
				int duration = 0, video_size = 0, video_id = 0;
				String[] array = temp.split("\t");
				video_id = Integer.parseInt(array[0]);; //because the first volume is string
				code_rate = Double.parseDouble(array[1]);
				duration = Integer.parseInt(array[2]);
				video_size = Integer.parseInt(array[3]);

				VideoNode video = new VideoNode(video_id);
				video.code_rate_ = code_rate;
				video.duration_ = duration;
				video.video_size_ = video_size;
				video_list_.put(video_id, video);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		video_in_file.close();
		
		// If this video description file does not exist, create one (don't need to create)s
		/*else 
		{
			Random rand =new Random();
			String str = null;
			FileOutputStream video_out_file = new FileOutputStream("video.dat");
			for (int video_id = 0; video_id < video_number; video_id++)
			{
				VideoNode video = new VideoNode(video_id);
				video.code_rate_ = 512; //coding rate of video, unit: kbps
				video.duration_ = 40 + 10 * (1 + rand.nextInt(21)); // Duration of video, unit: seconds
				video.video_size_ = video.code_rate_ * video.duration_; //Data size of video. unit: kbits
				video_list_.put(video_id, video);
				
				str = video_id + "\t" + video.code_rate_ + "\t" + video.duration_ + "\t" + 
				video.video_size_ + "\n";
				video_out_file.write(str.getBytes());
			}
			video_out_file.close();
		}
		*/
		System.out.println("Video rate and duration configured");
		
		
		//Read video placement file
		File file1 = new File("placement.dat"); //replaced by Youtube dataset.
		BufferedReader placement=null;
		int node_id;
		int video_id;
		
		placement=new BufferedReader(new FileReader(file1));
		temp = placement.readLine();
		String[] array = temp.split("\t");
		node_number = Integer.parseInt(array[0]);
		req_node_number = Integer.parseInt(array[1]);
		//video_number = Integer.parseInt(array[2]);	
		try
		{
			while((temp=placement.readLine())!=null)
			{
				int cache_video_num;
				array = temp.split("\t");
				node_id = Integer.parseInt(array[0]);
				cache_video_num = Integer.parseInt(array[1]);
				
				Vector<Integer> videos = new Vector<Integer>();
				int i = 0;
				for(i = 0; i < cache_video_num; i++)
				{
					video_id = Integer.parseInt(array[2+i]);
					videos.add(video_id);

					// Add the video to the cache node
					video_list_.get(video_id).AddCacheNode(node_id);
				}
				
				video_id = Integer.parseInt(array[2+cache_video_num]);
				if(video_id != -1)
				{
					System.out.println("Invalid placement file");
				}
				
				CacheNodeMananger.GetInstance().GetNode(node_id).SetInNodeVideos(video_number, videos);

				videos.clear();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
				
		// Check and show whether any video has zero copy
		for (Map.Entry<Integer, VideoNode> entry : video_list_.entrySet())
		{
			if(entry.getValue().ReportCacheNum() <1)
			{
				System.out.println("Video: " + entry.getKey() + ", no cached copy");
			}
		}				
		placement.close();
		
		
		//*******************************************************************************
					
		return 0;
	}
	
	
	public int GetVideoNum ()
	{
		return video_list_.size();
	}
	
	public VideoNode GetVideo (int video_id)
	{
		if(video_list_.containsKey(video_id) == false)
		{
			return null;
		}
		return video_list_.get(video_id);
	}
	
	public static VideoManager GetInstance ()
	{
		if(instance_ == null)
		{
			instance_ = new VideoManager();
		}
		return instance_;
	}
	
	public void SetNetwork (Network net)
	{
		net_ = net;
	}
	
	public Network GetNetwork ()
	{
		return net_;
	}
}
