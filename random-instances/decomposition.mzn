% Demand met
constraint forall(t in TIMEPOINTS_WITHOUT_HORIZON)(
  if useDecomposition then
    sum(i in TASKS)(bool2int(Start[i] <= t /\ t < Start[i] + processing_time[i]) * Height[i]) >= demand
  endif
);