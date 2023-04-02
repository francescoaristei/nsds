#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

/**
 * Group number: 21
 *
 * Group members
 * Francesco Aristei - 10804304
 * Dario Mazzola - 10650009
 * Giampiero Repole - 10543357
 *
 **/

// Set DEBUG 1 if you want car movement to be deterministic
#define DEBUG 1

const int num_segments = 256;

const int num_iterations = 1000;
const int count_every = 10;

const double alpha = 0.5;
const int max_in_per_sec = 10;

// Returns the number of car that enter the first segment at a given iteration.
int create_random_input() {
#if DEBUG
  return 1;
#else
  return rand() % max_in_per_sec;
#endif
}

// Returns 1 if a car needs to move to the next segment at a given iteration, 0 otherwise.
int move_next_segment() {
#if DEBUG
  return 1;
#else
  return (((double) rand()) /RAND_MAX) < alpha ? 1 : 0;
#endif
}


int main(int argc, char** argv) { 
  MPI_Init(NULL, NULL);

  int rank;
  int num_procs;
  MPI_Comm_rank(MPI_COMM_WORLD, &rank);
  MPI_Comm_size(MPI_COMM_WORLD, &num_procs);
  srand(time(NULL) + rank);
  
  // TODO: define and init variables
  int arraySize = (num_segments/num_procs);
  int *carsPerSegment = (int*) malloc(arraySize * sizeof(int));
  memset((void *) carsPerSegment, 0, arraySize * sizeof(int));

  int carsMoving = 0;
  int previousSegment = 0;
  int numEntering = 0;
  int numFirstPosition;
  int numberOfCars;

  // Simulate for num_iterations iterations
  for (int it = 1; it <= num_iterations; ++it) { 
    previousSegment = 0;
    // Move cars across segments
    // New cars may enter in the first segment
    // Cars may exit from the last segment
    numEntering = 0;

    // The process P0 starts the interaction
    if(rank == 0) {
      numEntering = create_random_input();
    }
    else {
      MPI_Recv(&numEntering, 1, MPI_INT, rank-1, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
    }

    numFirstPosition = carsPerSegment[0];

    for (int i=0; i<arraySize; i++) {
      carsMoving = 0;
      numberOfCars = carsPerSegment[i];

      for(int j=0; j<numberOfCars; j++) {

        int action = move_next_segment();
        if (action == 1) {
          carsMoving++;
        }
        
      }
      carsPerSegment[i] = carsPerSegment[i] + previousSegment - carsMoving;

      previousSegment = carsMoving;
    }

    carsPerSegment[0] += numEntering;

    // If the process is not the last one, it sends the number of cars exiting 
    //from its part of the array to the process the comes next
    if(rank != num_procs -1){
      MPI_Send(&carsMoving, 1, MPI_INT, rank+1, 0, MPI_COMM_WORLD);
    }
    
    // When needed, compute the overall sum
    if (it%count_every == 0) {
      int process_sum = 0;

      int global_sum = 0;

      for(int i=0; i<arraySize; i++) {
        process_sum += carsPerSegment[i];
      }

      // compute global sum
      MPI_Reduce(&process_sum, &global_sum, 1, MPI_INT, MPI_SUM, 0, MPI_COMM_WORLD);
      
      if (rank == 0) {
	      printf("Iteration: %d, sum: %d\n", it, global_sum);
      }
    }
  }

  // deallocate dynamic variables, if needed
  free(carsPerSegment);

  MPI_Barrier(MPI_COMM_WORLD);
  MPI_Finalize();
}
