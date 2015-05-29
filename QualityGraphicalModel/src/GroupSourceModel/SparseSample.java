package GroupSourceModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class maintains information on the state of different variables during Gibb's sampling. The Gibb's sampling consists
 * of in-place changes in values of the variables. Every so often, we 'save' a snapshot of the current state in a GroundingSample object
 *  
 * @author manasrj
 */
public class SparseSample {
	ModelInstance modelInstance;
	int numTuples;
	Map<Integer, Boolean> tupleTruths;
	List<Map<Integer, Boolean>> groupTupleBeliefs;
	List<Map<Integer, Map<Integer, Boolean>>> sourceGroupTupleBeliefs;
	
	Map<Integer, Set<Integer>> groupOutputs;
	Map<Integer, Set<Integer>> outputGroups;	
	
	Set<Integer> isFixed;

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
	
	// T_i
	Boolean getVal (int i) {
		if (tupleTruths.containsKey(i)) {
			return tupleTruths.get(i);			
		} else {
			return false;
		}
	}

	// G_{k,i}
	Boolean getVal (int i, int k) {
		if (groupOutputs.get(k).contains(i)) {
			return groupTupleBeliefs.get(k).get(i);			
		} else {
			return false;
		}
	}

	// S_{j,k,i}
	Boolean getVal (int i, int j, int k) {
		if (modelInstance.sourceOutputs.get(j).contains(i)) {
			return sourceGroupTupleBeliefs.get(j).get(k).get(i);			
		} else {
			return false;
		}
	}
	
	
	SparseSample (ModelInstance modelInstance) {
		this.modelInstance = modelInstance;
		
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
		
		groupOutputs = new HashMap<Integer, Set<Integer>>();
		outputGroups = new HashMap<Integer, Set<Integer>>();
		for (int i  = 0; i < modelInstance.numTuples; i++) {
			outputGroups.put(i, new HashSet<Integer>());
		}
		for (int k  = 0; k < modelInstance.getNumGroups(); k++) {
			groupOutputs.put(k, new HashSet<Integer>());
		}
		for (int j = 0; j < modelInstance.getNumSources(); j++) {
			for (int i : modelInstance.sourceOutputs.get(j)) {
				for (int k : modelInstance.sourceGroups.get(j)) {
					outputGroups.get(i).add(k);
					groupOutputs.get(k).add(i);
				}
			}
		}
		
		isFixed = new HashSet<Integer>();
		tupleTruths = new HashMap<Integer, Boolean>();
		numTuples = modelInstance.numTuples;
		for (int i : modelInstance.tupleTruth.keySet()) {
			isFixed.add(i);
			tupleTruths.put(i, modelInstance.tupleTruth.get(i));
			if (tupleTruths.get(i)) {
				tupleTrue++;
			} else {
				tupleFalse++;
			}
		}
		for (int i = 0; i < modelInstance.numTuples; i++) {
			Double tupleTrueProbability = tupleTruthProb(); // Need better way to choose this? (We aren't using it currently)
			// NOTE: What would happen if we initialized tuples based on number of outputting sources rather than randomly? Closer to equilibrium state, but any local optima problems?
			final Set<Integer> sources = modelInstance.outputSources.get(i);
			if (sources.isEmpty()) {
				tupleFalse++; 
				continue; // Don't even store this tuple explicitly
			} else if (sources.size() == 1) { // NOTE: Need to choose the '1' threshold better. 
				tupleTruths.put(i, false);
				tupleFalse++;
			} else {
				tupleTruths.put(i, true);
				tupleTrue++;
			}
		}
		
		groupTupleBeliefs = new ArrayList<Map<Integer, Boolean>>();
		for (int k = 0; k < modelInstance.getNumGroups(); k++) {
			Map<Integer, Boolean> groupTupleBeliefsMap = new HashMap<Integer, Boolean>();
			for (int i = 0; i < modelInstance.numTuples; i++) {
				boolean groupTupleBelief;
				boolean tupleTruth = getVal(i);
				if (!groupOutputs.get(k).contains(i)) {
					groupTupleBelief = false;
				} else {
					if (Math.random() < groupBeliefProb(k, tupleTruth)) {
						groupTupleBelief = true;
					} else {
						groupTupleBelief = false;
					}
					groupTupleBeliefsMap.put(i, groupTupleBelief);					
				}
				updateGroupCount(k, groupTupleBelief, tupleTruth, 1);
			}
			groupTupleBeliefs.add(groupTupleBeliefsMap);
		}
		
		sourceGroupTupleBeliefs = new ArrayList<Map<Integer, Map<Integer, Boolean>>>();
		for (int j = 0; j < modelInstance.getNumSources(); j++) {
			Map<Integer, Map<Integer, Boolean>> sourceGroupTupleBeliefsMap = new HashMap<Integer, Map<Integer, Boolean>>();
			for (int k : modelInstance.sourceGroups.get(j)) {
				Map<Integer, Boolean> groupTupleBeliefsMap = new HashMap<Integer, Boolean>();
				for (int i = 0; i < modelInstance.numTuples; i++) {
					boolean groupTupleBelief = groupTupleBeliefs.get(k).get(i);
					boolean sourceGroupTupleBelief;
					if (!modelInstance.sourceOutputs.get(j).contains(i)) {
						sourceGroupTupleBelief = false;
					} else {
						if (Math.random() < sourceBeliefProb(j, groupTupleBelief)) {
							sourceGroupTupleBelief = true;
						} else {
							sourceGroupTupleBelief = false;
						}
						groupTupleBeliefsMap.put(i, sourceGroupTupleBelief);						
					}
					updateSourceCount(j, sourceGroupTupleBelief, groupTupleBelief, 1);
				}
				sourceGroupTupleBeliefsMap.put(k, groupTupleBeliefsMap);
			}
			
			sourceGroupTupleBeliefs.add(sourceGroupTupleBeliefsMap);
		}
	}
	
	// To add: terms for mutually exclusive tuple groups 
	private void changeTupleTruth(int i) {
		if (isFixed.contains(i)) {
			return;
		}
		
		Double trueWeight = 0.0;
		Double falseWeight = 0.0;
		
		trueWeight += Math.log(tupleTruthProb());
		for (int k = 0; k < modelInstance.getNumGroups(); k++) {
			if (groupTupleBeliefs.get(k).get(i)) {
				trueWeight += Math.log(groupBeliefProb(k, true));
				falseWeight += Math.log(groupBeliefProb(k, false));
			} else {
				trueWeight += Math.log(1 - groupBeliefProb(k, true));
				falseWeight += Math.log(1 - groupBeliefProb(k, false));				
			}
		}
		
		final Double odds = Math.exp(trueWeight - falseWeight);
		if (Math.random() < odds / (1 + odds)) {
			if (!tupleTruths.get(i)) {
				tupleTruths.put(i, true);
				tupleTrue++;
				tupleFalse--;
				for (int k = 0; k < modelInstance.getNumGroups(); k++) {
					updateGroupCount(k, groupTupleBeliefs.get(k).get(i), true, 1);
					updateGroupCount(k, groupTupleBeliefs.get(k).get(i), false, -1);
				}
			} 
		} else {
			if (tupleTruths.get(i)) {
				tupleTruths.put(i, false);
				tupleTrue--;
				tupleFalse++;
				for (int k = 0; k < modelInstance.getNumGroups(); k++) {
					updateGroupCount(k, groupTupleBeliefs.get(k).get(i), true, -1);
					updateGroupCount(k, groupTupleBeliefs.get(k).get(i), false, 1);
				}
			}
		}
	}
	
	private void changeGroupBelief(int i, int k) {
		Double trueWeight = 0.0;
		Double falseWeight = 0.0;
		
		trueWeight += Math.log(groupBeliefProb(k, tupleTruths.get(i)));
		falseWeight += Math.log(1 - groupBeliefProb(k, tupleTruths.get(i)));
		
		for (int j : modelInstance.groupSources.get(k)) {
			if (sourceGroupTupleBeliefs.get(j).get(k).get(i)) {
				trueWeight += Math.log(sourceBeliefProb(j, true));
				falseWeight += Math.log(sourceBeliefProb(j, false));
			} else {
				trueWeight += Math.log(1 - sourceBeliefProb(j, true));
				falseWeight += Math.log(1 - sourceBeliefProb(j, false));				
			}
		}
		
		final Double odds = Math.exp(trueWeight - falseWeight);
		if (Math.random() < odds / (1 + odds)) {
			if (!groupTupleBeliefs.get(k).get(i)) {
				groupTupleBeliefs.get(k).put(i, true);
				updateGroupCount(k, true, tupleTruths.get(i), 1);
				updateGroupCount(k, false, tupleTruths.get(i), -1);
				for (int j : modelInstance.groupSources.get(k)) {
					updateSourceCount(j, sourceGroupTupleBeliefs.get(j).get(k).get(i), true, 1);
					updateSourceCount(j, sourceGroupTupleBeliefs.get(j).get(k).get(i), false, -1);
				}
			} 
		} else {
			if (groupTupleBeliefs.get(k).get(i)) {
				groupTupleBeliefs.get(k).put(i, false);
				updateGroupCount(k, true, tupleTruths.get(i), -1);
				updateGroupCount(k, false, tupleTruths.get(i), 1);
				for (int j : modelInstance.groupSources.get(k)) {
					updateSourceCount(j, sourceGroupTupleBeliefs.get(j).get(k).get(i), true, -1);
					updateSourceCount(j, sourceGroupTupleBeliefs.get(j).get(k).get(i), false, 1);
				}
			} 
		}
	}
	
	private void changeSourceGroupBelief(int i, int j, int k) {
		Double trueWeight = 0.0;
		Double falseWeight = 0.0;
		
		trueWeight += Math.log(sourceBeliefProb(j, groupTupleBeliefs.get(k).get(i)));
		falseWeight += Math.log(1 - sourceBeliefProb(j, groupTupleBeliefs.get(k).get(i)));
		
		boolean falseOr = false; // The Or of all other beliefs for this source-tuple pair
		final boolean trueOr = true;
		for (int kk : modelInstance.sourceGroups.get(j)) {
			if (kk == k) {
				continue;
			}
			if (sourceGroupTupleBeliefs.get(j).get(kk).get(i)) {
				falseOr = true;
				break;
			}
		}
		
		// Below: change 0.001 to epsilon, a parameter set in the modelInstance.
		trueWeight += Math.log(trueOr == modelInstance.sourceOutputs.get(j).contains(i) ? 1.0 : modelInstance.epsilon);
		falseWeight += Math.log(falseOr == modelInstance.sourceOutputs.get(j).contains(i) ? 1.0 : modelInstance.epsilon);
		
		final Double odds = Math.exp(trueWeight - falseWeight);
		if (Math.random() < odds / (1 + odds)) {
			if (!sourceGroupTupleBeliefs.get(j).get(k).get(i)) {
				sourceGroupTupleBeliefs.get(j).get(k).put(i, true);
				updateSourceCount(j, true, groupTupleBeliefs.get(k).get(i), 1);
				updateSourceCount(j, false, groupTupleBeliefs.get(k).get(i), -1);
			}
		} else {
			if (sourceGroupTupleBeliefs.get(j).get(k).get(i)) {
				sourceGroupTupleBeliefs.get(j).get(k).put(i, false);
				updateSourceCount(j, true, groupTupleBeliefs.get(k).get(i), -1);
				updateSourceCount(j, false, groupTupleBeliefs.get(k).get(i), 1);
			}
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
	
	List<SparseSample> GibbsSampling (final int numSamples, final int burnIn, final int thinFactor) {
		List<SparseSample> samples = new ArrayList<SparseSample>();
		for (long iter = 1; iter <= burnIn + (numSamples - 1) * thinFactor; iter++) {
			for (int i = 0; i < modelInstance.numTuples; i++) {
				for (int k = 0; k < modelInstance.getNumGroups(); k++) {
					// Do this in random order to prevent early j's from being true'd first for source-outputs?
					for (int j : modelInstance.groupSources.get(k)) {
						changeSourceGroupBelief(i, j, k);
					}
					changeGroupBelief(i, k);
				}
				changeTupleTruth(i);
			}
			
			if (iter >= burnIn && (iter - burnIn) % thinFactor == 0) {
				samples.add(saveState());
			}
		}
		return samples;
	}
	
	SparseSample saveState() {
		List<Boolean> gTupleTruths = new ArrayList<Boolean>();
		List<List<Boolean>> gGroupTupleBeliefs = new ArrayList<List<Boolean>>();
		List<Map<Integer, List<Boolean>>> gSourceGroupTupleBeliefs = new ArrayList<Map<Integer, List<Boolean>>>();

		//gTupleTruths.addAll(tupleTruths);
		for (int k = 0; k < modelInstance.getNumGroups(); k++) {
			List<Boolean> groupTupleBeliefsList = new ArrayList<Boolean>();
			//groupTupleBeliefsList.addAll(groupTupleBeliefs.get(k));
			gGroupTupleBeliefs.add(groupTupleBeliefsList);
		}
		
		for (int j = 0; j < modelInstance.getNumSources(); j++) {
			Map<Integer, List<Boolean>> sourceGroupTupleBeliefsMap = new HashMap<Integer, List<Boolean>>();
			for (int k : modelInstance.sourceGroups.get(j)) {
				List<Boolean> groupTupleBeliefsList = new ArrayList<Boolean>();
				//groupTupleBeliefsList.addAll(sourceGroupTupleBeliefs.get(j).get(k));
				sourceGroupTupleBeliefsMap.put(k, groupTupleBeliefsList);
			}
			gSourceGroupTupleBeliefs.add(sourceGroupTupleBeliefsMap);
		}
		
		//return new SparseSample(modelInstance, gTupleTruths, gGroupTupleBeliefs, gSourceGroupTupleBeliefs, tupleTrue, 
		//		tupleFalse, groupTrueTrue, groupTrueFalse, groupFalseTrue, groupFalseFalse, sourceTrueTrue, sourceTrueFalse, 
		//		sourceFalseTrue, sourceFalseFalse);
		return null;
	}
}
