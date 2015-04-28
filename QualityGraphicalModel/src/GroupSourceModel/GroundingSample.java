package GroupSourceModel;

import java.util.List;
import java.util.Map;

public class GroundingSample {
	ModelInstance modelInstance;
	List<Boolean> tupleTruths;
	List<List<Boolean>> groupTupleBeliefs;
	List<Map<Integer, List<Boolean>>> sourceGroupTupleBeliefs;
	
	Integer tupleTrue;
	Integer tupleFalse;
	
	List<Integer> groupTrueTrue;
	List<Integer> groupTrueFalse;
	List<Integer> groupFalseTrue;
	List<Integer> groupFalseFalse;
	
	List<Integer> sourceTrueTrue;
	List<Integer> sourceTrueFalse;
	List<Integer> sourceFalseTrue;
	List<Integer> sourceFalseFalse;

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
	
	GroundingSample (ModelInstance modelInstance, List<Boolean> tupleTruths, List<List<Boolean>> groupTupleBeliefs,
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
	
	// The functions below are same as those for liveSample. Maybe there's a way to reFactor the code to use it better.
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
}
