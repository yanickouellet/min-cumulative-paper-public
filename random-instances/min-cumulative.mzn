include "fzn_mincumul.mzn";

constraint if useMinCumul then 
  min_cumulative(Start, processing_time, Height, [demand | t in TIMEPOINTS_WITHOUT_HORIZON])
endif;