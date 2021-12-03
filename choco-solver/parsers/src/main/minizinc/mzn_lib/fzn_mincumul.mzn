predicate min_cumulative(array[int] of var int: Starts, array[int] of var int: ProcessingTimes, array[int] of var int: Heights, array[int] of int: demand) =
    minCumulativeChoco(Starts, ProcessingTimes, Heights, demand);

predicate minCumulativeChoco(array[int] of var int: Starts, array[int] of var int: ProcessingTimes, array[int] of var int: Heights, array[int] of int: demand);
