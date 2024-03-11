#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <papi.h>

using namespace std;

#define SYSTEMTIME clock_t

 
void OnMult(int m_ar, int m_br) 
{
	
	SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
	

		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;



	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);



    Time1 = clock();

	for(i=0; i<m_ar; i++)
	{	for( j=0; j<m_br; j++)
		{	temp = 0;
			for( k=0; k<m_ar; k++)
			{	
				temp += pha[i*m_ar+k] * phb[k*m_br+j];
			}
			phc[i*m_ar+j]=temp;
		}
	}


    Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

    free(pha);
    free(phb);
    free(phc);
	
	
}

// add code here for line x line matriz multiplication
void OnMultLine(int m_ar, int m_br)
{
	SYSTEMTIME Time1, Time2;

	char st[100];
	int i, j, k;

	double *pha, *phb, *phc;

	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i = 0; i < m_ar; i++){
		for(j = 0; j < m_ar; j++){
			pha[i*m_ar + j] = (double)1.0; 
		}
	}

	for(i = 0; i < m_br; i++){
		for(j = 0; j < m_br; j++){
			phb[i * m_br + j] = (double)(i+1);
		}
	}

	Time1 = clock();

	for(i = 0; i < m_ar; i++){
		for(k = 0; k < m_ar; k++){
			for(j = 0; j < m_br; j++){
				phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
			}
		}
	}

	Time2 = clock();

	sprintf(st, "Time : %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);

	free(pha);
	free(phb);
	free(phc);
    
}

// add code here for block x block matriz multiplication
void OnMultBlock(int m_ar, int m_br, int bkSize)
{
        SYSTEMTIME Time1, Time2;
	
	char st[100];
	int i, j, k;

	double *pha, *phb, *phc;
	
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;



	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);


    Time1 = clock();

	int num_blocks = m_ar/bkSize; //numero de blocos a dividir as matrizes
	
 
	for (int line_matrix_a = 0; line_matrix_a < num_blocks; line_matrix_a++) { //percorre as linhas de blocos da matriz A
		
		for(int block_index=0; block_index < num_blocks; block_index++) { //percorre os blocos da linha atual

			for(int col_matrix_b=0; col_matrix_b < num_blocks; col_matrix_b++) { //percorre as colunas de blocos da matriz b
				int next_line_a = (line_matrix_a + 1) * bkSize; //proxima linha de blocos da matriz A
				
				for(int i = line_matrix_a * bkSize; i < next_line_a; i++) { //percorre as linhas do bloco atual da matriz A
					int next_block_a = (block_index + 1) * bkSize; //proxima coluna dentro do bloco atual da matriz A

					for (int k = block_index * bkSize; k < next_block_a; k++) { //percorre as colunas do bloco atual da matriz A
						int next_block_b = (col_matrix_b+1)*bkSize; //proxima coluna dentro do bloco atual da matriz B

						for (int j = col_matrix_b * bkSize; j < next_block_b; j++) { //percorre as colunas de um bloco da matriz B
							phc[i * m_ar + j] += pha[i * m_ar + k] * phb[k * m_ar + j];
						}
					}
				}
			}
		}
	}

	Time2 = clock();

	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);

	free(pha);
	free(phb);
	free(phc);
}



void handle_error (int retval)
{
  printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
  exit(1);
}

void init_papi() {
  int retval = PAPI_library_init(PAPI_VER_CURRENT);
  if (retval != PAPI_VER_CURRENT && retval < 0) {
    printf("PAPI library version mismatch!\n");
    exit(1);
  }
  if (retval < 0) handle_error(retval);

  std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
            << " MINOR: " << PAPI_VERSION_MINOR(retval)
            << " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
}


int main (int argc, char *argv[])
{

	char c;
	int lin, col, blockSize;
	int op;
	
	int EventSet = PAPI_NULL;
  	long long values[4];
	long long *values_ptr = values;
  	int ret;
	

	ret = PAPI_library_init( PAPI_VER_CURRENT );
	if ( ret != PAPI_VER_CURRENT ){
		cout << "FAIL" << endl;
	}

	ret = PAPI_create_eventset(&EventSet);
	if (ret != PAPI_OK){
		cout << "ERROR: create eventset" << endl;
	}

	ret = PAPI_add_event(EventSet,PAPI_L1_DCM );
	if (ret != PAPI_OK){
		cout << "ERROR: PAPI_L1_DCM" << endl;
	}

	ret = PAPI_add_event(EventSet,PAPI_L2_DCM);
	if (ret != PAPI_OK){
		cout << "ERROR: PAPI_L2_DCM" << endl;
	}

	ret = PAPI_add_event(EventSet,PAPI_TOT_INS);
	if (ret != PAPI_OK){
		cout << "ERROR: PAPI_TOT_INS" << endl;
	}

	ret = PAPI_add_event(EventSet,PAPI_TOT_CYC);
	if (ret != PAPI_OK){
		cout << "ERROR: PAPI_TOT_CYC" << endl;
	}



	op=1;
	do {
		cout << endl << "1. Multiplication" << endl;
		cout << "2. Line Multiplication" << endl;
		cout << "3. Block Multiplication" << endl;
		cout << "Selection?: ";
		cin >>op;
		if (op == 0)
			break;
		printf("Dimensions: lins=cols ? ");
   		cin >> lin;
   		col = lin;


		// Start counting

		ret = PAPI_start(EventSet);
		if (ret != PAPI_OK){
			cout << "ERROR: Start PAPI" << endl;
		}

		SYSTEMTIME Time1, Time2;
		char st[100];

		switch (op){
			case 1: 
				OnMult(lin, col);
				break;
			case 2:
				OnMultLine(lin, col);  
				break;
			case 3:
				cout << "Block Size? ";
				cin >> blockSize;
				Time1 = clock();
				OnMultBlock(lin, col, blockSize);
				Time2 = clock();
				sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
                cout << st; 
				break;

		}


		ret = PAPI_stop(EventSet, values_ptr);
  		if (ret != PAPI_OK){
			cout << "ERROR: Stop PAPI" << endl;
		}

		cout << "L1 DCM: " << values[0] << endl;
		cout << "L2 DCM: " << values[1] << endl;
		cout << "Total Cycles: " << values[2] << endl;
		cout << "Total Instructions: " << values[3] << endl;
		
		if (values[3] != 0) {
        	cout << "CPI: " << static_cast<double>((values[2]*1.0)) / values[3] << endl;
    	} 
		else {
        	cout << "CPI: N/A (no instructions counted)" << endl;
    	}


		ret = PAPI_reset( EventSet );
		if ( ret != PAPI_OK )
			std::cout << "FAIL reset" << endl; 



	}while (op != 0);

	ret = PAPI_remove_event( EventSet, PAPI_L1_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L2_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_destroy_eventset( &EventSet );
	if ( ret != PAPI_OK )
		std::cout << "FAIL destroy" << endl;
}


