package GroupSourceModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class maintains information on the state of different variables during Gibb's sampling. The Gibb's sampling consists
 * of in-place changes in values of the variables. Every so often, we 'save' a snapshot of the current state in a GroundingSample object
 *  
 * @author manasrj
 */
public class LiveSample {
	ModelInstance modelInstance;
	List<Boolean> tupleTruths;
	List<List<Boolean>> groupTupleBeliefs;
	List<Map<Integer, List<Boolean>>> sourceGroupTupleBeliefs;
	
	List<Boolean> isFixed;

	// number of true and false tuples
	Integer tupleTrue;
	Integer tupleFalse;
	
	// tupleTruth-groupTupleBelief pair value counts for each group. 
	// groupXY[k] is the number of times group k believes X when Y is the case.
	List<Integer> groupTrueTrue;
	List<Integer> groupTrueFalse;
	List<Integer> groupFalseTrue;
	List<Integer> groupFalseFalse;
	
	// groupTupleBelief-sourceGroupTupleBelief pair value counts  for each source
	// sourceXY[j] is the number of times source j believes X when Y is the case.
	List<Integer> sourceTrueTrue;
	List<Integer> sourceTrueFalse;
	List<Integer> sourceFalseTrue;
	List<Integer> sourceFalseFalse;
	
	LiveSample(ModelInstance modelInstance) {
		tupleTrue = 0;
		tupleFalse = 0;
		
		groupTrueTrue = new ArrayList<Integer>();
		groupTrueFalse = new ArrayList<Integer>();
		groupFalseTrue = new ArrayList<Integer>();
		groupFalseFalse = new ArrayList<Integer>();
		groupTrueTrue.addAll(modelInstance.groupTrueTrueInit);
		groupTrueFalse.addAll(modelInstance.groupTrueFalseInit);
		groupFalseTrue.addAll(modelInstance.groupFalseTrueInit);
		groupFalseFalse.addAll(modelInstance.groupFalseFalseInit);
		
		sourceTrueTrue = new ArrayList<Integer>();
		sourceTrueFalse = new ArrayList<Integer>();
		sourceFalseTrue = new ArrayList<Integer>();
		sourceFalseFalse = new ArrayList<Integer>();
		sourceTrueTrue.addAll(modelInstance.sourceTrueTrueInit);
		sourceTrueFalse.addAll(modelInstance.sourceTrueFalseInit);
		sourceFalseTrue.addAll(modelInstance.sourceFalseTrueInit);
		sourceFalseFalse.addAll(modelInstance.sourceFalseFalseInit);
		
		isFixed = new ArrayList<Boolean>();
		tupleTruths = new ArrayList<Boolean>();
		for (int i = 0; i < modelInstance.numTuples; i++) {
			isFixed.add(false);
			tupleTruths.add(false);
		}
		for (int i : modelInstance.tupleTruth.keySet()) {
			isFixed.set(i, true);
			tupleTruths.set(i, modelInstance.tupleTruth.get(i));
			if (tupleTruths.get(i)) {
				tupleTrue++;
			} else {
				tupleFalse++;
			}
		}
		for (int i = 0; i < modelInstance.numTuples; i++) {
			if (!isFixed.get(i)) {
				if (Math.random() < tupleTruthProb()) {
					tupleTruths.set(i, true);
					tupleTrue++;
				} else {
					tupleTruths.set(i, false);
					tupleFalse++;
				}
			}
		}
		
		groupTupleBeliefs = new ArrayList<List<Boolean>>();
		for (int k = 0; k < modelInstance.numGroups; k++) {
			List<Boolean> groupTupleBeliefsList = new ArrayList<Boolean>();
			for (int i = 0; i < modelInstance.numTuples; i++) {
				boolean groupTupleBelief;
				boolean tupleTruth = tupleTruths.get(i);
				if (Math.random() < groupBeliefProb(k, tupleTruth)) {
					groupTupleBelief = true;
				} else {
					groupTupleBelief = false;
				}
				groupTupleBeliefsList.add(groupTupleBelief);
				updateGroupCount(k, groupTupleBelief, tupleTruth, 1);
			}
			groupTupleBeliefs.add(groupTupleBeliefsList);
		}
		
		sourceGroupTupleBeliefs = new ArrayList<Map<Integer, List<Boolean>>>();
		for (int j = 0; j < modelInstance.numSources; j++) {
			Map<Integer, List<Boolean>> sourceGroupTupleBeliefsMap = new HashMap<Integer, List<Boolean>>();
			for (int k : modelInstance.sourceGroups.get(j)) {
				List<Boolean> groupTupleBeliefsList = new ArrayList<Boolean>();
				for (int i = 0; i < modelInstance.numTuples; i++) {
					boolean groupTupleBelief = groupTupleBeliefs.get(k).get(i);
					boolean sourceGroupTupleBelief;
					if (Math.random() < sourceBeliefProb(j, groupTupleBelief)) {
						sourceGroupTupleBelief = true;
					} else {
						sourceGroupTupleBelief = false;
					}
					groupTupleBeliefsList.add(sourceGroupTupleBelief);
					updateSourceCount(j, sourceGroupTupleBelief, groupTupleBelief, 1);
				}
				sourceGroupTupleBeliefsMap.put(k, groupTupleBeliefsList);
			}
			
			sourceGroupTupleBeliefs.add(sourceGroupTupleBeliefsMap);
		}
	}
	
	double tupleTruthProb () {
		return (tupleTrue + 0.0) / (tupleTrue + tupleFalse);
	}
	
	double groupBeliefProb (int groupId, boolean condition) {
		if (condition) {
			return (groupTrueTrue.get(groupId) + 0.0) / (groupTrueTrue.get(groupId) + groupFalseTrue.get(groupId));
		} else {
			return (groupTrueFalse.get(groupId) + 0.0) / (groupTrueFalse.get(groupId) + groupFalseFalse.get(groupId));			
		}
	}
	
	double sourceBeliefProb (int sourceId, boolean condition) {
		if (condition) {
			return (sourceTrueTrue.get(sourceId) + 0.0) / (sourceTrueTrue.get(sourceId) + sourceFalseTrue.get(sourceId));
		} else {
			return (sourceTrueFalse.get(sourceId) + 0.0) / (sourceTrueFalse.get(sourceId) + sourceFalseFalse.get(sourceId));			
		}
	}
		
	private void updateGroupCount (int groupId, boolean groupBelief, boolean tupleTruth, int change) {
		if (groupBelief) {
			if (tupleTruth) {
				groupTrueTrue.set(groupId, change + groupTrueTrue.get(groupId));
			} else {
				groupTrueFalse.set(groupId, change + groupTrueFalse.get(groupId));
			}
		} else {
			if (tupleTruth) {
				groupFalseTrue.set(groupId, change + groupFalseTrue.get(groupId));
			} else {
				groupFalseFalse.set(groupId, change + groupFalseFalse.get(groupId));
			}
		}
	}
	
	private void updateSourceCount (int sourceId, boolean sourceBelief, boolean groupBelief, int change) {
		if (sourceBelief) {
			if (groupBelief) {
				sourceTrueTrue.set(sourceId, change + sourceTrueTrue.get(sourceId));
			} else {
				sourceTrueFalse.set(sourceId, change + sourceTrueFalse.get(sourceId));
			}
		} else {
			if (groupBelief) {
				sourceFalseTrue.set(sourceId, change + sourceFalseTrue.get(sourceId));
			} else {
				sourceFalseFalse.set(sourceId, change + sourceFalseFalse.get(sourceId));
			}
		}
	}
	
	GroundingSample saveState() {
		List<Boolean> gTupleTruths = new ArrayList<Boolean>();
		List<List<Boolean>> gGroupTupleBeliefs = new ArrayList<List<Boolean>>();
		List<Map<Integer, List<Boolean>>> gSourceGroupTupleBeliefs = new ArrayList<Map<Integer, List<Boolean>>>();

		gTupleTruths.addAll(tupleTruths);
		for (int k = 0; k < modelInstance.numGroups; k++) {
			List<Boolean> groupTupleBeliefsList = new ArrayList<Boolean>();
			groupTupleBeliefsList.addAll(groupTupleBeliefs.get(k));
			gGroupTupleBeliefs.add(groupTupleBeliefsList);
		}
		
		for (int j = 0; j < modelInstance.numSources; j++) {
			Map<Integer, List<Boolean>> sourceGroupTupleBeliefsMap = new HashMap<Integer, List<Boolean>>();
			for (int k : modelInstance.sourceGroups.get(j)) {
				List<Boolean> groupTupleBeliefsList = new ArrayList<Boolean>();
				groupTupleBeliefsList.addAll(sourceGroupTupleBeliefs.get(j).get(k));
				sourceGroupTupleBeliefsMap.put(k, groupTupleBeliefsList);
			}
			gSourceGroupTupleBeliefs.add(sourceGroupTupleBeliefsMap);
		}
		
		return new GroundingSample(modelInstance, gTupleTruths, gGroupTupleBeliefs, gSourceGroupTupleBeliefs);
	}
}
