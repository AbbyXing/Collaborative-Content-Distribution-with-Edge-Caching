package Graph;

import java.util.Vector;

import javax.xml.crypto.dsig.keyinfo.KeyValue;

public class PQi {
	
	private int d, N;
	private Vector<Integer> pq, qp; 
	private Vector<Double> a; //***** <keyType> ???

	private void exch(int i, int j)
	{ 
		int t = pq.get(i); 
		pq.set(i, pq.get(j));
		pq.set(j, t);
		qp.set(pq.get(i), i);
		qp.set(pq.get(j), j);
	}

	private void fixUp(int k)
	{ 
		while (k > 1 && a.get(pq.get((k+d-2)/d)) > a.get(pq.get(k)))
		{ 
			exch(k, (k+d-2)/d); 
			k = (k+d-2)/d; 
		} 
	}

	private void fixDown(int k, int N)
	{ 
		int j;
		while ((j = d*(k-1)+2) <= N)
		{ 
			for (int i = j+1; i < j+d && i <= N; i++)
			{
				if (a.get(pq.get(j)) > a.get(pq.get(i))) 
				{
					j = i;
				}
			}
			if (!(a.get(pq.get(k)) > a.get(pq.get(j)))) 
			{
				break;
			}
			exch(k, j); 
			k = j;
		}
	}
	
	//PQi construct function
	public PQi (int N, Vector<Double> a)
	{
		this.d = 3;
		this.N = N;
		this.a = a;
		pq = new Vector<Integer>();
		qp = new Vector<Integer>();
		for (int i = 0; i < N+1; i++)
		{
			pq.add(0);
			qp.add(0);
		}
	}

	public boolean empty() //***返回型本来是int，改成boolean了
	{ 
		return N == 0; 
	}
	
	public void insert(int v) 
	{ 
		pq.add(++N, v);
		qp.set(v, N);
		fixUp(N); 
	}

	public int getmin()
	{ 
		exch(1, N); 
		fixDown(1, N-1); 
		return pq.get(N--); 
	}

	public void lower(int k)
	{ 
		fixUp(qp.get(k));
	}

}
