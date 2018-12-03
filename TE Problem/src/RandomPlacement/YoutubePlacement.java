package RandomPlacement;

import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import NetSimulator.VideoNode;

import java.util.Scanner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream ;
import java.io.FileReader;
import java.io.IOException;

public class YoutubePlacement {
	
	@SuppressWarnings({ "resource", "unchecked" })
	public static void main(String[] args)throws IOException{
		
		int node_num = 23;
		int cache_node_num = 9; //number of cache nodes, 4 in edge, 5 in core. (No.12-20 are cache nodes)!!!
		int req_node_num = 16; //only edge nodes can generate requests
		int video_num = 0;
		int copy_num = 5; //avg replica for each video
		
		// Read video description file, pick 200 videos, and write in a "video.dat" file:
		File file1 = new File("sizerate.txt"); //Youtube dataset.
		FileOutputStream video_out_file = new FileOutputStream("video.dat"); //output file
		BufferedReader video_in_file=null;
		String temp = null;
		String str = null;
		String vbr = new String("VBR");
		try
		{
			video_in_file = new BufferedReader(new FileReader(file1));
			while((temp=video_in_file.readLine())!=null)
			{
				if(video_num < 200)
				{
					String[] array = temp.split("\t");
					if(array.length == 4  && array[3].equals(vbr) == false)
					{
						int duration = Integer.parseInt(array[1]);
						double coding_rate = Double.parseDouble(array[3]);
						int file_size = duration * (int)coding_rate;
						if(duration <= 150 && duration >= 50 && coding_rate >= 300 && coding_rate <= 350)
						{
							str = video_num + "\t" + coding_rate + "\t" + duration + "\t" + file_size + "\n";
							video_out_file.write(str.getBytes());
							video_num++;
						}	
					}
				}
				else
					break;
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		video_in_file.close();
		video_out_file.close();
		System.out.println("video num:" + video_num);
		
		Random rand =new Random();
		Vector nodes = new Vector<>();
		for (int i = 0; i < node_num; i++)
		{
			nodes.add(new Vector<Integer>());
		}

		
		//video placement
		int copies = 0;
		int selnode = 0;
		int iter = 0;
		for (int i = 0; i < video_num; ++i) //video_id begins with 0
		{
			copies = copy_num;
			for (int j = 0; j < copies; ++j)
			{
				selnode = 12 + rand.nextInt(cache_node_num); //node_id begins with 0, cache_node takes higher ids
				//System.out.println(selnode);
				iter = ((Vector)nodes.get(selnode)).indexOf(i);
				//System.out.println(iter);
				while(iter != -1)
				{
					selnode = 12 + rand.nextInt(cache_node_num);
					iter = ((Vector)nodes.get(selnode)).indexOf(i);
				}
				((Vector)nodes.get(selnode)).add(i);			
			}
		}
		
		FileOutputStream place = null;		
		place = new FileOutputStream("placement.dat");
		str = null;
		str = node_num + "\t" + req_node_num + "\t" + video_num + "\n";
		place.write(str.getBytes());
		for (int i = 0; i < node_num; ++i)
		{
			str = i + "\t" + ((Vector)nodes.get(i)).size() + "\t";
			place.write(str.getBytes());
			for (Iterator<Integer> iters = ((Vector)nodes.get(i)).iterator(); iters.hasNext();)
			{
				str = iters.next() + "\t";
				place.write(str.getBytes());
			}
			str = -1 + "\n";
			place.write(str.getBytes());
		}
		
		System.out.println("videos have been placed!\n");
	}

}
