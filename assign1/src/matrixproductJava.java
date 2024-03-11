import java.util.*;

public class matrixproductJava{

	public static void OnMult(int m_ar, int m_br) {
		
		long Time1, Time2;

		double temp;
		int i, j, k;

		double[] pha = new double[m_ar*m_ar];
		double[] phb = new double[m_ar*m_ar];
		double[] phc = new double[m_ar*m_ar];

		for(i=0; i<m_ar; i++)
			for(j=0; j<m_ar; j++)
				pha[i*m_ar + j] = (double)1.0;

		for(i=0; i<m_br; i++)
			for(j=0; j<m_br; j++)
				phb[i*m_br + j] = (double)(i+1);

		Time1 = System.currentTimeMillis();

		for(i=0; i<m_ar; i++){
			for(j=0; j<m_br; j++){
				temp = 0;
				for( k=0; k<m_ar; k++)
				{	
					temp += pha[i*m_ar+k] * phb[k*m_br+j];
				}
				phc[i*m_ar+j]=temp;
			}
		}

		Time2 = System.currentTimeMillis();

		System.out.printf("Time: %3.3f seconds\n", (double)(Time2-Time1)/1000);
		
		System.out.println("Result matrix: ");
		for (i = 0; i < 1; i++) {
			for (j = 0; j < Math.min(10, m_br); j++) {
				System.out.print(phc[j] + " ");
			}
		}
		System.out.println();
	}

	public static void OnMultLine(int m_ar, int m_br) {
		long Time1, Time2;

		String st; // not used. keeping here just in case
		double temp;
		int i, j, k;

		double[] pha = new double[m_ar*m_ar];
		double[] phb = new double[m_ar*m_ar];
		double[] phc = new double[m_ar*m_ar];

		for(i=0; i<m_ar; i++)
			for(j=0; j<m_ar; j++)
				pha[i*m_ar + j] = (double)1.0;

		for(i=0; i<m_br; i++)
			for(j=0; j<m_br; j++)
				phb[i*m_br + j] = (double)(i+1);
		
		for(i=0; i<m_ar; i++)
			for(j=0; j<m_ar; j++)
				phc[i*m_ar + j] = (double)0;

		Time1 = System.currentTimeMillis();

		for(i=0; i<m_ar; i++)
		{
			for( k=0; k<m_ar; k++)
			{	
				for(j=0; j<m_br; j++)
				{
					phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
				}
			}
		}

		Time2 = System.currentTimeMillis();

		System.out.printf("Time: %3.3f seconds\n", (double)(Time2-Time1)/1000);
		
		System.out.println("Result matrix: ");
		for (i = 0; i < 1; i++) {
			for (j = 0; j < Math.min(10, m_br); j++) {
				System.out.print(phc[j] + " ");
			}
		}
		System.out.println();
	}



	public static void main(String[] args) {	
	
		char c;
		int lin, col, blockSize;
		int op;

		long[] values = new long[20];
		int ret;
		
		Scanner stdin = new Scanner(System.in);

		op=1;
		do{
			System.out.println("1. Multiplication");
			System.out.println("2. Line Multiplication");

			System.out.print("Selection?: ");
			op = stdin.nextInt();

			if(op==0) 
				break;

			System.out.print("Dimensions: lins=cols ? ");
			lin = stdin.nextInt();
			col = lin;

			switch (op){
				case 1: 
					OnMult(lin, col);
					break;
				case 2:
					OnMultLine(lin, col);  
					break;
			}
		} while (op != 0);
	}

}