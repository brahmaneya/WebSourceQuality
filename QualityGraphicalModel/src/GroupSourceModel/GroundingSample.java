package GroupSourceModel;

import java.util.List;
import java.util.Map;

public class GroundingSample {
	ModelInstance modelInstance;
	List<Boolean> tupleTruths;
	List<List<Boolean>> groupTupleBeliefs;
	List<Map<Integer, List<Boolean>>> sourceGroupTupleBeliefs;

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
			List<Map<Integer, List<Boolean>>> sourceGroupTupleBeliefs) {
		this.modelInstance = modelInstance;
		this.tupleTruths = tupleTruths;
		this.groupTupleBeliefs = groupTupleBeliefs;
		this.sourceGroupTupleBeliefs = sourceGroupTupleBeliefs;
	}
}
