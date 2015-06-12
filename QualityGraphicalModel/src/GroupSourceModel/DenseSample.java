package GroupSourceModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.out;

/**
 * This class maintains information on the state of different variables during Gibb's sampling. The Gibb's sampling consists
 * of in-place changes in values of the variables. Every so often, we 'save' a snapshot of the current state in a GroundingSample object
 *  
 * @author manasrj
 */
public class DenseSample implements Serializable {
	public ModelInstance modelInstance;
	public List<Boolean> tupleTruths;
	public List<List<Boolean>> groupTupleBeliefs;
	public List<Map<Integer, List<Boolean>>> sourceGroupTupleBeliefs;
	
	public List<Boolean> isFixed;

	// number of true and false tuples
	public Integer tupleTrue;
	public Integer tupleFalse;
	
	// tupleTruth-groupTupleBelief pair value counts for each group. 
	// groupXY[k] is the number of times group k believes X when Y is the case.
	public List<Integer> groupTrueTrue;
	public List<Integer> groupTrueFalse;
	public List<Integer> groupFalseTrue;
	public List<Integer> groupFalseFalse;
	
	// groupTupleBelief-sourceGroupTupleBelief pair value counts  for each source
	// sourceXY[j] is the number of times source j believes X when Y is the case.
	public List<Integer> sourceTrueTrue;
	public List<Integer> sourceTrueFalse;
	public List<Integer> sourceFalseTrue;
	public List<Integer> sourceFalseFalse;
	
	public DenseSample(ModelInstance modelInstance) {
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
		
		isFixed = new ArrayList<Boolean>();
		tupleTruths = new ArrayList<Boolean>();
		for (int i = 0; i < modelInstance.getNumTuples(); i++) {
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
		for (int i = 0; i < modelInstance.getNumTuples(); i++) {
			Double tupleTrueProbability = tupleTruthProb();
			// NOTE: What would happen if we initialized tuples based on number of outputting sources rather than randomly? Closer to equilibrium state, but any local optima problems?
			if (!isFixed.get(i)) {
				if (Math.random() < tupleTrueProbability) {
					tupleTruths.set(i, true);
					tupleTrue++;
				} else {
					tupleTruths.set(i, false);
					tupleFalse++;
				}
			}
		}
		
		groupTupleBeliefs = new ArrayList<List<Boolean>>();
		for (int k = 0; k < modelInstance.getNumGroups(); k++) {
			List<Boolean> groupTupleBeliefsList = new ArrayList<Boolean>();
			for (int i = 0; i < modelInstance.getNumTuples(); i++) {
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
		for (int j = 0; j < modelInstance.getNumSources(); j++) {
			Map<Integer, List<Boolean>> sourceGroupTupleBeliefsMap = new HashMap<Integer, List<Boolean>>();
			for (int k : modelInstance.sourceGroups.get(j)) {
				List<Boolean> groupTupleBeliefsList = new ArrayList<Boolean>();
				for (int i = 0; i < modelInstance.getNumTuples(); i++) {
					boolean groupTupleBelief = groupTupleBeliefs.get(k).get(i);
					boolean sourceGroupTupleBelief;
					if (modelInstance.sourceOutputs.get(j).contains(i)) {
					//if (Math.random() < sourceBeliefProb(j, groupTupleBelief)) {
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
	
	DenseSample (ModelInstance modelInstance, List<Boolean> tupleTruths, List<List<Boolean>> groupTupleBeliefs,
			List<Map<Integer, List<Boolean>>> sourceGroupTupleBeliefs, Integer tupleTrue, Integer tupleFalse, 
			List<Integer> groupTrueTrue, List<Integer> groupTrueFalse, List<Integer> groupFalseTrue, 
			List<Integer> groupFalseFalse, List<Integer> sourceTrueTrue, List<Integer> sourceTrueFalse, 
			List<Integer> sourceFalseTrue, List<Integer> sourceFalseFalse) {
		this.modelInstance = modelInstance;
		this.tupleTruths = tupleTruths;
		this.groupTupleBeliefs = groupTupleBeliefs;
		this.sourceGroupTupleBeliefs = sourceGroupTupleBeliefs;
	
		this.tupleTrue = tupleTrue;
		this.tupleFalse = tupleFalse;
		
		this.groupTrueTrue = groupTrueTrue;
		this.groupTrueFalse = groupTrueFalse;
		this.groupFalseTrue = groupFalseTrue;
		this.groupFalseFalse = groupFalseFalse;

		this.sourceTrueTrue = sourceTrueTrue;
		this.sourceTrueFalse = sourceTrueFalse;
		this.sourceFalseTrue = sourceFalseTrue;
		this.sourceFalseFalse = sourceFalseFalse;
	}
	
	// T_i
	Boolean getVal (int i) {
		return tupleTruths.get(i);
	}

	// G_{k,i}
	Boolean getVal (int i, int k) {
		return groupTupleBeliefs.get(k).get(i);
	}

	// S_{j,k,i}
	Boolean getVal (int i, int j, int k) {
		return sourceGroupTupleBeliefs.get(j).get(k).get(i);
	}
	
	// To add: terms for mutually exclusive tuple groups 
	private void changeTupleTruth(int i) {
		if (isFixed.get(i)) {
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
				tupleTruths.set(i, true);
				tupleTrue++;
				tupleFalse--;
				for (int k = 0; k < modelInstance.getNumGroups(); k++) {
					updateGroupCount(k, groupTupleBeliefs.get(k).get(i), true, 1);
					updateGroupCount(k, groupTupleBeliefs.get(k).get(i), false, -1);
				}
			} 
		} else {
			if (tupleTruths.get(i)) {
				tupleTruths.set(i, false);
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
				groupTupleBeliefs.get(k).set(i, true);
				updateGroupCount(k, true, tupleTruths.get(i), 1);
				updateGroupCount(k, false, tupleTruths.get(i), -1);
				for (int j : modelInstance.groupSources.get(k)) {
					updateSourceCount(j, sourceGroupTupleBeliefs.get(j).get(k).get(i), true, 1);
					updateSourceCount(j, sourceGroupTupleBeliefs.get(j).get(k).get(i), false, -1);
				}
			} 
		} else {
			if (groupTupleBeliefs.get(k).get(i)) {
				groupTupleBeliefs.get(k).set(i, false);
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
				sourceGroupTupleBeliefs.get(j).get(k).set(i, true);
				updateSourceCount(j, true, groupTupleBeliefs.get(k).get(i), 1);
				updateSourceCount(j, false, groupTupleBeliefs.get(k).get(i), -1);
			}
		} else {
			if (sourceGroupTupleBeliefs.get(j).get(k).get(i)) {
				sourceGroupTupleBeliefs.get(j).get(k).set(i, false);
				updateSourceCount(j, true, groupTupleBeliefs.get(k).get(i), -1);
				updateSourceCount(j, false, groupTupleBeliefs.get(k).get(i), 1);
			}
		}
	}
	
	public double tupleTruthProb () {
		return (tupleTrue + 0.0) / (tupleTrue + tupleFalse);
	}
	
	public double groupBeliefProb (int groupId, boolean condition) {
		if (condition) {
			return (groupTrueTrue.get(groupId) + 0.0) / (groupTrueTrue.get(groupId) + groupFalseTrue.get(groupId));
		} else {
			return (groupTrueFalse.get(groupId) + 0.0) / (groupTrueFalse.get(groupId) + groupFalseFalse.get(groupId));			
		}
	}
	
	public double sourceBeliefProb (int sourceId, boolean condition) {
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
	
	public List<DenseSample> GibbsSampling (final int numSamples, final int burnIn, final int thinFactor) {
		List<DenseSample> samples = new ArrayList<DenseSample>();
		for (long iter = 1; iter <= burnIn + (numSamples - 1) * thinFactor; iter++) {
			for (int i = 0; i < modelInstance.getNumTuples(); i++) {
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
	
	DenseSample saveState() {
		List<Boolean> gTupleTruths = new ArrayList<Boolean>();
		List<List<Boolean>> gGroupTupleBeliefs = new ArrayList<List<Boolean>>();
		List<Map<Integer, List<Boolean>>> gSourceGroupTupleBeliefs = new ArrayList<Map<Integer, List<Boolean>>>();

		gTupleTruths.addAll(tupleTruths);
		for (int k = 0; k < modelInstance.getNumGroups(); k++) {
			List<Boolean> groupTupleBeliefsList = new ArrayList<Boolean>();
			groupTupleBeliefsList.addAll(groupTupleBeliefs.get(k));
			gGroupTupleBeliefs.add(groupTupleBeliefsList);
		}
		
		for (int j = 0; j < modelInstance.getNumSources(); j++) {
			Map<Integer, List<Boolean>> sourceGroupTupleBeliefsMap = new HashMap<Integer, List<Boolean>>();
			for (int k : modelInstance.sourceGroups.get(j)) {
				List<Boolean> groupTupleBeliefsList = new ArrayList<Boolean>();
				groupTupleBeliefsList.addAll(sourceGroupTupleBeliefs.get(j).get(k));
				sourceGroupTupleBeliefsMap.put(k, groupTupleBeliefsList);
			}
			gSourceGroupTupleBeliefs.add(sourceGroupTupleBeliefsMap);
		}
		
		return new DenseSample (modelInstance, gTupleTruths, gGroupTupleBeliefs, gSourceGroupTupleBeliefs, tupleTrue, 
				tupleFalse, groupTrueTrue, groupTrueFalse, groupFalseTrue, groupFalseFalse, sourceTrueTrue, sourceTrueFalse, 
				sourceFalseTrue, sourceFalseFalse);
	}
	
}
