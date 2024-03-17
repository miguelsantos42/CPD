#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <papi.h>
#include <omp.h>

using namespace std;

#define SYSTEMTIME clock_t

void OnMult(int m_ar, int m_br) // realizar a multiplicação de duas matrizes e armazenar o resultado numa outra matriz
								//m_ar e m_br são as dimensões de cada uma das matrizes
{
	
	SYSTEMTIME Time1, Time2;  //Armazenam os tempos no início e no final do processo
	
	char st[100];			//array de caracteres que irá armazenar a string do tempo de execução
	double temp;			//calcular a soma temporária dos produtos
	int i, j, k;

	double *pha, *phb, *phc; 
	//é alocado memória para cada uma das matrizes, cujo o tamanho de memória alocada é o tamanho da matriz ao quadrado multiplicado pelo tamanho de um double
    //no fundo é esta quantidade de memória alocada pois é uma matriz nxn, (n linhas, n colunas)
	pha = (double *)malloc((m_ar * m_ar) * sizeof(double)); 
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	//está a ser montada a matriz pha com valor de 1.0 em todas as posições
	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;
	//pha[i*m_ar + j] = (double)1.0; -> m_Ar é o nºlinhas/nºcolunas;


	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);
	// aqui todos os elemntos da primeira linha estão a ser setado a 1.0
	// na segunda a 2.0
	//na terceira a 3.0 e continua;


	//aqui será calculado o tempo de inicio de multiplicação da matriz
    Time1 = clock();

	//realização da multiplicação 
	for(i=0; i<m_ar; i++)  				//acesso às linha da matriz pha
	{	
		for( j=0; j<m_br; j++)			//acesso às coluna da matriz phb
		{	
			temp = 0;					//irá acumular os resultados da multiplicação
			for( k=0; k<m_ar; k++)		//percorre as colunas da matriz pha e as linhas de phb, variavel k é usada para indexar as colunas de pha e as linhas de phb
			{	
				temp += pha[i*m_ar+k] * phb[k*m_br+j]; //acumular o valor multiplicando o elemento dalinha i e coluna k da matriz A com o elemento da linha K, coluna j da matriz B
			}
			phc[i*m_ar+j]=temp;
			//temp conterá a soma dos produtos dos elementos da linha i da matriz A pelos elementos da coluna j da matriz B
		}
	}


    Time2 = clock(); //capturação do tempo final
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC); //tempo necessário para a multiplicação da matriz
	cout << st;
	//Como CLOCKS_PER_SEC representa o número de ticks por segundo, a divisão nos dá o tempo em segundos

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;
	//é exibida a matriz, no máximo 10 elementos
	//Se m_br for menor que 10, a função min garantirá que apenas os elementos existentes sejam considerados

    free(pha);
    free(phb);
    free(phc);
	
	// está a ser Libera a memória previamente alocada para as matrizes A, B e C
}

void multLineParalel1(int m_ar, int m_br){
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

    double time1= omp_get_wtime();

	#pragma omp parallel for
	for(i = 0; i < m_ar; i++){ 				
		for(k = 0; k < m_ar; k++){			
			for(j = 0; j < m_br; j++){		
				phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
			}
		}
	}

	double time2= omp_get_wtime();

	cout << "Time Paralell 1: " << time2-time1 << endl;

    free(pha);
	free(phb);
	free(phc);
}

void multLineParalel2(int m_ar, int m_br){
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

    double time1= omp_get_wtime();

	#pragma omp parallel
	for(i = 0; i < m_ar; i++){ 				
		for(k = 0; k < m_ar; k++){
            #pragma omp for			
			for(j = 0; j < m_br; j++){		
				phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
			}
		}
	}

	double time2= omp_get_wtime();

	cout << "Time Paralell 2: " << time2-time1 << endl;

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
	
	double sequencial1 = omp_get_wtime();

	for (int i = 0; i < m_ar; i++) {
        for (int k = 0; k < m_ar; k++) {
            for (int j = 0; j < m_br; j++) {
                phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
            }
        }
    }


	double sequencial2 = omp_get_wtime();

	cout << "Time Sequencial: " << sequencial2 - sequencial1 << endl;

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

// i percorre as linhas do bloco atual de A.
// k percorre as colunas do bloco de A e as linhas do bloco correspondente de B.
// j percorre as colunas do bloco de B.


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

	int num_blocks = m_ar/bkSize;  // a matriz é dividida em blocos de tamanho bkSize
	
 
	for (int line_matrix_a = 0; line_matrix_a < num_blocks; line_matrix_a++) { //percorre as linhas de blocos da matriz A
		//itera sobre as linhas de blocos em A (2 linhas de blocos neste exemplo). Primeiro, focamos na primeira linha de blocos de A.
		for(int block_index=0; block_index < num_blocks; block_index++) { //percorre os blocos da linha
			//Dentro da primeira linha de blocos de A, iteramos sobre cada bloco (2 blocos neste exemplo). Começamos com o primeiro bloco de A.
			for(int col_matrix_b=0; col_matrix_b < num_blocks; col_matrix_b++) { //percorre as colunas de blocos da matriz b
				//Para cada bloco de A, iteramos sobre as colunas de blocos em B, para encontrar os blocos de B correspondentes a multiplicar 
				int next_line_a = (line_matrix_a + 1) * bkSize; //proxima linha de blocos da matriz A
				
				for(int i = line_matrix_a * bkSize; i < next_line_a; i++) { //percorre as linhas de um dos blocos da matriz A
					int next_block_a = (block_index + 1) * bkSize;

					for (int k = block_index * bkSize; k < next_block_a; k++) { //percorre as colunas do bloco
						int next_block_b = (col_matrix_b+1)*bkSize;

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
                multLineParalel1(lin, col);
                multLineParalel2(lin, col);
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
