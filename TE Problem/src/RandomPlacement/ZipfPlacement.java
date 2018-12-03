package RandomPlacement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
import org.apache.commons.math3.distribution.ZipfDistribution;

public class ZipfPlacement {
	
	@SuppressWarnings({ "resource", "unchecked" })
	public static void main(String[] args)throws IOException{
		
		int node_num = 23;
		//number of cache nodes, 4 in edge, 5 in core. (No.12-20 are cache nodes)!!!
		int core_cache_node_num = 5;
		int edge_cache_node_num = 4;
		int req_node_num = 16; //only edge nodes can generate requests
		int video_num = 0;
		
		double exp; //zipf exponent
		System.out.println("Zipf exponent: ");
		Scanner scanner = new  Scanner(System.in);
		exp = scanner.nextDouble();
		
		int capacity; //storage capacity of each cache node
		System.out.println("Storage capacity: ");
		capacity = scanner.nextInt();
		
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

		Vector nodes = new Vector<>();
		for (int i = 0; i < node_num; i++)
		{
			nodes.add(new Vector<Integer>());
		}

		
		//video placement
		ZipfDistribution distribution = new ZipfDistribution(video_num, exp);
		//CDF of zipf distribution:
		Vector<Double> cdf = new Vector<Double>();
		for (int i = 0; i < video_num; ++i)
		{
			cdf.add(distribution.cumulativeProbability(i));
		}
		//edge cache (follows zipf distribution):
		for (int i = 12; i < 12 + edge_cache_node_num; ++i) //video_id begins with 0
		{
			for (int j = 0; j < capacity; ++j)
			{
				int iter = 0;
				int it = 0;
				double rand_num = Math.random();
				while(it < cdf.size())
				{
					if(rand_num < cdf.get(it))
					{
						iter = ((Vector)nodes.get(i)).indexOf(it-1);
						if(iter == -1)
						{
							((Vector)nodes.get(i)).add(it-1);
							break;
						}
						else
						{
							rand_num = Math.random();
							continue;
						}
					}
					it++;
				}
				if (it == cdf.size())
				{
					((Vector)nodes.get(i)).add(it-1);
				}
			}
		}
		//core cache (each node caches all videos in library):
		for (int i = 12 + edge_cache_node_num; i < 12 + edge_cache_node_num + core_cache_node_num; ++i)
		{
			for (int j = 0; j < video_num; ++j)
			{
				((Vector)nodes.get(i)).add(j);
			}
		}
		
		//output file
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
