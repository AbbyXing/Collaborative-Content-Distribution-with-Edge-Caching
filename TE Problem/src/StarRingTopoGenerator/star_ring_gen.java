package StarRingTopoGenerator;

import java.util.Random;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream ;
import java.io.FileReader;
import java.io.IOException;


public class star_ring_gen {
	
	private double RandomOneRealNumber(double from, double to) {

		final int limits = 10000;
		double interval = to - from;

		Random rand =new Random();		
		int value = rand.nextInt(limits);
		double fraction = ((double) value) / limits;
		double data_gen = from + interval * fraction;

		return data_gen;
	}
	
	@SuppressWarnings({ "resource", "unused" })
	private void CreateRing(int node_number, double cap) throws IOException {
		
		FileOutputStream links = null;
		FileOutputStream nodes = null;
		
		links = new FileOutputStream("links.dat");
		nodes = new FileOutputStream("nodes.dat");

		int link_id = 0;
		String str = null;
		str = node_number + "\n";
		links.write(str.getBytes());
		for (int i = 1; i < node_number; ++i) {

			str = link_id + "\t" + 0 + "\t" + i + "\t" + cap + "\n";
			links.write(str.getBytes());
			str = 0 + "\t" + i + "\t" + link_id + "\t" + -1 + "\n";
			nodes.write(str.getBytes());
			++link_id;
			
			str = link_id + "\t" + i + "\t" + 0 + "\t" + cap + "\n";
			links.write(str.getBytes());
			str = i + "\t" + 0 + "\t" + link_id + "\t" + -1 + "\n";
			nodes.write(str.getBytes());
			++link_id;

			int next = i % (node_number - 1) + 1;
			
			str = link_id + "\t" + i + "\t" + next + "\t" + cap + "\n";
			links.write(str.getBytes());
			str = i + "\t" + next + "\t" + link_id + "\t" + -1 + "\n";
			nodes.write(str.getBytes());
			++link_id;
			
			str = link_id + "\t" + next + "\t" + i + "\t" + cap + "\n";
			links.write(str.getBytes());
			str = next + "\t" + i + "\t" + link_id + "\t" + -1 + "\n";
			nodes.write(str.getBytes());
			++link_id;
		}
	}

	@SuppressWarnings({ "resource", "unused" })
	private void CreateRandomRing(int node_number, double capacity_lower, double capacity_upper) throws IOException {
		
		FileOutputStream links = null;
		FileOutputStream nodes = null;
		
		links = new FileOutputStream("links.dat");
		nodes = new FileOutputStream("nodes.dat");

		int link_id = 0;
		String str = null;
		str = node_number + "\n";
		links.write(str.getBytes());
		for (int i = 1; i < node_number; ++i) {

			double cap = RandomOneRealNumber(capacity_lower, capacity_upper);

			str = link_id + "\t" + 0 + "\t" + i + "\t" + cap + "\n";
			links.write(str.getBytes());
			str = 0 + "\t" + i + "\t" + link_id + "\t" + -1 + "\n";
			nodes.write(str.getBytes());
			++link_id;
			
			str = link_id + "\t" + i + "\t" + 0 + "\t" + cap + "\n";
			links.write(str.getBytes());
			str = i + "\t" + 0 + "\t" + link_id + "\t" + -1 + "\n";
			nodes.write(str.getBytes());
			++link_id;

			int next = i % (node_number - 1) + 1;
			
			str = link_id + "\t" + i + "\t" + next + "\t" + cap + "\n";
			links.write(str.getBytes());
			str = i + "\t" + next + "\t" + link_id + "\t" + -1 + "\n";
			nodes.write(str.getBytes());
			++link_id;
			
			str = link_id + "\t" + next + "\t" + i + "\t" + cap + "\n";
			links.write(str.getBytes());
			str = next + "\t" + i + "\t" + link_id + "\t" + -1 + "\n";
			nodes.write(str.getBytes());
			++link_id;
		}
		
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args)throws IOException
	{

		int node_number = 50; // default: 30 edge servers with requests, 20 cache nodes
		double cap = 1000000.0; // capacity of links, unit: kbps, default: 1 Gbps
		star_ring_gen starRing = new star_ring_gen();
		System.out.println("Input topology type:");
		System.out.println("1: create Ring\n"+ "2: create Ring with node number\n" 
		+ "3: create Ring with node number and cap\n" + "4: create RandomRing");
		Scanner scanner = new  Scanner(System.in);
		int type = scanner.nextInt();
		if(type == 1)
		{
			starRing.CreateRing(node_number, cap);
		}
		else if(type == 2)
		{
			System.out.println("Input node number:");
			node_number = scanner.nextInt();
			starRing.CreateRing(node_number, cap);
		}
		else if(type == 3)
		{
			System.out.println("Input node number and cap:");
			node_number = scanner.nextInt();
			cap = scanner.nextDouble();
			starRing.CreateRing(node_number, cap);
		}
		else if (type == 4)
		{
			System.out.println("Input node number, cap_lower and cap_upper:");
			node_number = scanner.nextInt();
			double cap_lower = scanner.nextDouble();
			double cap_upper = scanner.nextDouble();
			starRing.CreateRandomRing(node_number, cap_lower,  cap_upper);
		}
		else
		{
			System.out.println("invalid input!");
		}
		
		System.out.println("The network has been created with nodes " + node_number);
		
		
		//*****read file template*****//
		File file=new File("links.dat");
		BufferedReader reader=null;
		String temp=null;
		try
		{
				reader=new BufferedReader(new FileReader(file));
				temp = reader.readLine();
				int nnn = Integer.parseInt(temp);
				System.out.println(nnn);
				while((temp=reader.readLine())!=null)
				{
					String[] array = temp.split("\t");
					int n1 = Integer.parseInt(array[0]);
					int n2 = Integer.parseInt(array[1]);
					int n3 = Integer.parseInt(array[2]);
					double n4 = Double.parseDouble(array[3]);
					System.out.println(n1+"\t"+n2+"\t"+n3+"\t"+n4);
				}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		reader.close();
	}
	

}
