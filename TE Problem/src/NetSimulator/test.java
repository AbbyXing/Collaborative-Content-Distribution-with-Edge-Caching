package NetSimulator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

public class test {
	
	public static void main(String[] args)throws IOException
	{
		int strategy_mode, req_mode;
		System.out.println("Strategy selection: ");
		Scanner scanner = new  Scanner(System.in);
		strategy_mode = scanner.nextInt(); // Request routing algorithm
		System.out.println("Request generation: ");
		req_mode = scanner.nextInt(); // Generate requests from scratch or load requests from existing file

		int traffic_density;
		System.out.println("Traffic density: ");
		traffic_density = scanner.nextInt();;

		double buff_time; // 10*slot_duration, buff_time = -1, aim at utilization bound
		System.out.println("Buff time: ");
		buff_time = scanner.nextDouble(); // Set to negative if program will determine best buff_time

		int cover_size; // Number of user devices that cache videos and send to others
		System.out.println("Cover number: ");
		cover_size = scanner.nextInt();

		double slot_duration = 6;
		double preroll_time = slot_duration; // preroll <= slot_duration * 1.2, (because initial transmission rate = 1.2*coding rate)
		int total_time_slot = 65;


		// Create the network
		Network net = new Network(slot_duration);
		if (net.InitNetwork() != 0) 
		{
			System.out.println("Initialization error for network, quit");
		}
		System.out.println("Network initialized");
		
		CacheNodeMananger manager = CacheNodeMananger.GetInstance();
		

		// Create user devices
		int device_density = 500; // Number of user devices associated with each cache node
		double area_radius = 150; //(m)???
		double trans_range = 50; //(m)???

		manager.CreateAllDevices(device_density, area_radius);

		// Create user requests
		if (req_mode == 0) //GEN_NEW_REQUESTS
		{
			double zipf_exp; //zipf exponent of request
			System.out.println("Zipf exponent: ");
			zipf_exp = scanner.nextDouble();
			manager.GenerateAllRequests(total_time_slot, traffic_density, zipf_exp);
			manager.ImportRequets();
		}
		else if (req_mode == 1) //APPLY_FILE_REQUESTS
		{
			manager.ImportRequets();
		}

		// Create file to record results
		String str = null;
		str = "util_output_" + strategy_mode + ".dat";
		FileOutputStream util_output = null;
		util_output = new FileOutputStream(str); //需要Append content to end of file
		
		// Loop over time slots
		int time_slot = 0;
		while (time_slot < total_time_slot)
		{
			System.out.println("Slot " + time_slot + ":");
			
			long start = System.currentTimeMillis();   //获取开始时间
			Vector<Double> result = manager.ProcessRequests(strategy_mode,
					time_slot, preroll_time, buff_time, trans_range, cover_size);
			long end = System.currentTimeMillis();
			
			// Output sequence: time slot, utilization (estimate, real, lower-bound),
			// target buffer time, running time
			if (strategy_mode == 1) //Nearest Source
			{
				str = time_slot + "\t" + result.get(0) + "\t" + result.get(1) + "\t"
						+ (end - start)+ "ms" + ";" + "\n";
				util_output.write(str.getBytes());
			}
			else if (strategy_mode == 2) //Felx-Hier
			{
				str = time_slot + "\t" + result.get(0) + "\t" + result.get(1) + "\t"
						+ (end - start)+ "ms" + ";" + "\n";
				util_output.write(str.getBytes());
			}
			else if (strategy_mode == 0) //Random Source
			{
				str = time_slot + "\t" + result.get(0) + "\t" + result.get(1) + "\t"
						+ (end - start)+ "ms" + ";" + "\n";
				util_output.write(str.getBytes());
			}
			else if (strategy_mode == 3) //Maximum Concurrent Flow
			{
				str = time_slot + "\t" + result.get(0) + "\t" + result.get(1) + "\t" + result.get(2) 
				+ "\t" + result.get(3) + "\t" + (end - start)+ "ms" + ";" + "\n";
				util_output.write(str.getBytes());
			}
			System.out.println("------------------------------------");
			++time_slot;
		}
		util_output.close();
	}
}
