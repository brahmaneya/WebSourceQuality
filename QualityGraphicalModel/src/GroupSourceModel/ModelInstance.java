package GroupSourceModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to describe an instance of our probabilistic graphical model, describing how sources outputs tuples. 
 * 
 * Conventions: i is always an index over tuples, j over sources, and k over groups.
 * @author manasrj
 */
public class ModelInstance {
	// Instance Size 
	final int numTuples;
	final int numGroups;
	final int numSources;
	
	// Instance Structure
	final List<Set<Integer>> groupSources;
	final List<Set<Integer>> sourceGroups;
	final List<Set<Integer>> sourceOutputs;
	final List<Set<Integer>> outputSources;
	
	// Boolean variable counts for conjugate prior
	// tupleTruth-groupTupleBelief pair value counts 
	List<Integer> groupTrueTrueInit;
	List<Integer> groupTrueFalseInit;
	List<Integer> groupFalseTrueInit;
	List<Integer> groupFalseFalseInit;
	
	// groupTupleBelief-sourceGroupTupleBelief pair value counts 
	List<Integer> sourceTrueTrueInit;
	List<Integer> sourceTrueFalseInit;
	List<Integer> sourceFalseTrueInit;
	List<Integer> sourceFalseFalseInit;
	
	// Labelled Data
	Map<Integer, Boolean> tupleTruth;
	
	public ModelInstance (int numTuples, int numGroups, int numSources, List<List<Integer>> groupSources, 
			List<List<Integer>> sourceOutputs, List<Integer> groupTrueTrueInit, List<Integer> groupTrueFalseInit,
			List<Integer> groupFalseTrueInit, List<Integer> groupFalseFalseInit, List<Integer> sourceTrueTrueInit,
			List<Integer> sourceTrueFalseInit, List<Integer> sourceFalseTrueInit, List<Integer> sourceFalseFalseInit,
			Map<Integer, Boolean> tupleTruth) {
		this.numTuples = numTuples;
		this.numGroups = numGroups;
		this.numSources = numSources;
		
		this.groupSources = new ArrayList<Set<Integer>>();
		this.sourceGroups = new ArrayList<Set<Integer>>();
		this.sourceOutputs = new ArrayList<Set<Integer>>();
		this.outputSources = new ArrayList<Set<Integer>>();
		for (int i = 0; i < numTuples; i++) {
			this.outputSources.add(new HashSet<Integer>());
		}
		for (int j = 0;j < numSources; j++) {
			this.sourceGroups.add(new HashSet<Integer>());
			this.sourceOutputs.add(new HashSet<Integer>());
		}
		for (int k = 0; k < numGroups; k++) {
			this.groupSources.add(new HashSet<Integer>());
		}
		
		for (int k = 0; k < numGroups; k++) {
			for (int source : groupSources.get(k)) {
				this.groupSources.get(k).add(source);
				this.sourceGroups.get(source).add(k);
			}
		}
		
		for (int j = 0; j < numSources; j++) {
			for (int output : sourceOutputs.get(j)) {
				this.sourceOutputs.get(j).add(output);
				this.outputSources.get(output).add(j);
			}
		}
		
		this.groupTrueTrueInit = groupTrueTrueInit;
		this.groupTrueFalseInit = groupTrueFalseInit;
		this.groupFalseTrueInit = groupFalseTrueInit;
		this.groupFalseFalseInit = groupFalseFalseInit;

		this.sourceTrueTrueInit = sourceTrueTrueInit;
		this.sourceTrueFalseInit = sourceTrueFalseInit;
		this.sourceFalseTrueInit = sourceFalseTrueInit;
		this.sourceFalseFalseInit = sourceFalseFalseInit;
	
		this.tupleTruth = tupleTruth;
	}

}
