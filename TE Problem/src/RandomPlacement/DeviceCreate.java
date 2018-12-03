package RandomPlacement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.math3.distribution.UniformRealDistribution;

public class DeviceCreate {
	
	public static void main(String[] args)throws IOException
	{
		int request_node_num = 16;
		int avg_per_node = 500;
		int device_id = 0;
		int area_radius = 150;
		UniformRealDistribution uniform_real_distribution1 = new UniformRealDistribution(0.006, 0.2);
		UniformRealDistribution uniform_real_distribution = new UniformRealDistribution(0, 0.8);
		
		FileOutputStream device_out_file = new FileOutputStream("device.dat"); //output file
		String str = null;
		
		for(int i = 0; i < request_node_num; i++)
		{
			int device_count = 0;
			for(int j = 0; j < avg_per_node; j++)
			{
				double radius_rand = uniform_real_distribution.sample();
				double radius_rand1 = uniform_real_distribution1.sample();
				double angle_rand = uniform_real_distribution.sample();
				double x_pos;
				double y_pos;
				
				if(device_count < 300)
				{
					x_pos = area_radius * Math.sqrt(radius_rand1) * Math.cos(2 * Math.acos(-1) * angle_rand);
					y_pos = area_radius * Math.sqrt(radius_rand) * Math.sin(2 * Math.acos(-1) * angle_rand);
				}
				else
				{
					x_pos = area_radius * Math.sqrt(radius_rand) * Math.cos(2 * Math.acos(-1) * angle_rand);
					y_pos = area_radius * Math.sqrt(radius_rand) * Math.sin(2 * Math.acos(-1) * angle_rand);
				}
				
				str = i + "\t" + device_count + "\t" + device_id + "\t" + x_pos + "\t" + y_pos + "\n";
				device_out_file.write(str.getBytes());
				device_id++;
				device_count++;
			}
		}
		System.out.println("Devices have been created!");
	}

}
