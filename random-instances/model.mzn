int: n;
int: demand; 
int: horizon;

set of int: TASKS = 1..n;
set of int: TIMEPOINTS = 0..horizon;
set of int: TIMEPOINTS_WITHOUT_HORIZON = 0..horizon-1;

array[TASKS] of int: est;
array[TASKS] of int: processing_time;
array[TASKS] of int: lct;

% Config parameter
bool: useDecomposition = false;
bool: useMinCumul = true;

% Variable
array[TASKS] of var int: Start;
array[TASKS] of var bool: Height;

var int: TotalEnergy;

constraint forall(i in TASKS)(Start[i] >= est[i]);
constraint forall(i in TASKS)(Start[i] + processing_time[i] <= lct[i]);

include "decomposition.mzn";
include "min-cumulative.mzn";

% Objective
constraint TotalEnergy = sum(i in TASKS)(processing_time[i] * Height[i]);

solve
  ::seq_search([int_search(Height, smallest, indomain_min), int_search(Start, smallest, indomain_min)])
  minimize TotalEnergy;
