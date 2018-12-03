package RandomPlacement;

import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import java.util.Scanner;
import java.io.FileOutputStream ;
import java.io.IOException;

public class placement {
	
	@SuppressWarnings("resource")
	public static void main(String[] args)throws IOException{
		
		int node_num = 50;
		int cache_node_num = 20; //internal servers with one cache but no requests
		int req_node_num = node_num - cache_node_num; //requests from node 1 to req_node_num, central node 0 has no requests
		int video_num = 500;
		int copy_num = 5; //avg replica for each video
		
		/*System.out.println("Input node_num, video_num, and copy_num:");
		Scanner scanner = new  Scanner(System.in);
		node_num = scanner.nextInt();
		video_num = scanner.nextInt();
		copy_num = scanner.nextInt();
		*/
		//System.out.println(node_num + " " + video_num + " " + copy_num + "\n");
		
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
				selnode = req_node_num + rand.nextInt(cache_node_num); //node_id begins with 0, cache_node takes higher ids
				//System.out.println(selnode);
				iter = ((Vector)nodes.get(selnode)).indexOf(i);
				//System.out.println(iter);
				while(iter != -1)
				{
					selnode = req_node_num + rand.nextInt(cache_node_num);
					iter = ((Vector)nodes.get(selnode)).indexOf(i);
				}
				((Vector)nodes.get(selnode)).add(i);			
			}
		}
		
		FileOutputStream place = null;		
		place = new FileOutputStream("placement.dat");
		String str = null;
		str = node_num + "\t" + req_node_num + "\t" + video_num + "\n";
		place.write(str.getBytes());
		for (int i = 0; i < node_num; ++i)
		{
			str = i + "\t" + ((Vector)nodes.get(i)).size() + "\t";
			place.write(str.getBytes());
			for (Iterator iters = ((Vector)nodes.get(i)).iterator(); iters.hasNext();)
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
