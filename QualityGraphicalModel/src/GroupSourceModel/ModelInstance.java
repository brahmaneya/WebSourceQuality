package GroupSourceModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.System.out;

/**
 * Class to describe an instance of our probabilistic graphical model, describing how sources outputs tuples. 
 * 
 * Conventions: i is always an index over tuples, j over sources, and k over groups.
 * @author manasrj
 */
public class ModelInstance implements Serializable {
	// Instance Size 
	public final int numTuples;
	public final int numGroups;
	public final int numSources;
	
	// Instance Structure
	public final List<Set<Integer>> groupSources;
	public final List<Set<Integer>> sourceGroups;
	public final List<Set<Integer>> sourceOutputs;
	public final List<Set<Integer>> outputSources;
	
	// Boolean variable counts for conjugate prior
	// tupleTruth-groupTupleBelief pair value counts 
	public final List<Integer> groupTrueTrueInit;
	public final List<Integer> groupTrueFalseInit;
	public final List<Integer> groupFalseTrueInit;
	public final List<Integer> groupFalseFalseInit;
	
	// groupTupleBelief-sourceGroupTupleBelief pair value counts 
	public List<Integer> sourceTrueTrueInit;
	public List<Integer> sourceTrueFalseInit;
	public List<Integer> sourceFalseTrueInit;
	public List<Integer> sourceFalseFalseInit;
	
	// Labelled Data
	public Map<Integer, Boolean> tupleTruth;
	
	// Probability penalty for having OR_{k} of sourceGroupTupleBeliefs[k][j][i] disagree with sourceOutput[j][i].
	final Double epsilon = 0.00001;
	
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
	
	void analyzeModel (int numSamples, int burnIn, int thinFactor, Double tuplePrior, Map<Integer, Double> groupFPs, 
			Map<Integer, Double> groupFNs, Map<Integer, Double> sourceFPs, Map<Integer, Double> sourceFNs) {
		DenseSample denseSample = new DenseSample(this);
		List<DenseSample> samples = denseSample.GibbsSampling(numSamples, burnIn, thinFactor);
		
		
	}
	
	@Override
	public String toString () {
		String ans = "";
		ans = String.format("%d, %d, %d \n", numTuples, getNumGroups(), getNumSources());
		return ans;
	}
	
	public int getNumTuples() {
		return numTuples;
	}

	public int getNumGroups() {
		return numGroups;
	}

	public int getNumSources() {
		return numSources;
	}

	public static void main (String[] args) {
		final int numTuples = 2000;
		final int numGroups = 3;
		final int numSources = 12;
		
		List<List<Integer>> groupSources = new ArrayList<List<Integer>>();
		List<List<Integer>> sourceOutputs = new ArrayList<List<Integer>>();

		Double groupDensity = 0.3;
		
		groupSources.add(new ArrayList<Integer>());
		for (int j = 0; j < numSources; j++) {
			groupSources.get(0).add(j);
		}
		
		for (int k = 1; k < numGroups; k++) {
			groupSources.add(new ArrayList<Integer>());
			for (int j = 0; j < numSources; j++) {
				if (Math.random() < groupDensity) {
					groupSources.get(k).add(j);
				}
			}
		}
		
		List<Boolean> tupleTruths = new ArrayList<Boolean>();
		List<List<Boolean>> groupTupleBeliefs = new ArrayList<List<Boolean>>();
		List<Map<Integer, List<Boolean>>> sourceGroupTupleBeliefs = new ArrayList<Map<Integer, List<Boolean>>>();

		Double tuplePrior = 0.2;
		for (int i = 0; i < numTuples; i++) {
			if (Math.random() < tuplePrior) {
				tupleTruths.add(true);
			} else {
				tupleTruths.add(false);
			}
		}
		
		ArrayList<Double> groupFP = new ArrayList<Double>();
		ArrayList<Double> groupFN = new ArrayList<Double>();
		for (int k = 0; k < numGroups; k++) {
			groupFP.add(0.01);
			groupFN.add(0.3);
		}
		
		for (int k = 0; k < numGroups; k++) {
			List<Boolean> beliefsList = new ArrayList<Boolean>();
			for (int i = 0; i < numTuples; i++) {
				if (tupleTruths.get(i)) {
					if (Math.random() < groupFN.get(k)) {
						beliefsList.add(false);
					} else {
						beliefsList.add(true);
					}
				} else {
					if (Math.random() < groupFP.get(k)) {
						beliefsList.add(true);
					} else {
						beliefsList.add(false);
					}
				}
			}
			groupTupleBeliefs.add(beliefsList);
		}
		
		ArrayList<Double> sourceFP = new ArrayList<Double>();
		ArrayList<Double> sourceFN = new ArrayList<Double>();
		for (int j = 0; j < numSources; j++) {
			sourceFP.add(0.01);
			sourceFN.add(0.7);
		}
		
		for (int j = 0; j < numSources; j++) {
			Map<Integer, List<Boolean>> beliefsMap = new HashMap<Integer, List<Boolean>>();
			for (int k = 0; k < numGroups; k++) {
				if (!groupSources.get(k).contains(j)) {
					continue;
				}
				List<Boolean> beliefsList = new ArrayList<Boolean>();
				for (int i = 0; i < numTuples; i++) {
					if (groupTupleBeliefs.get(k).get(i)) {
						if (Math.random() < sourceFN.get(j)) {
							beliefsList.add(false);
						} else {
							beliefsList.add(true);
						}
					} else {
						if (Math.random() < sourceFP.get(j)) {
							beliefsList.add(true);
						} else {
							beliefsList.add(false);
						}
					}
				}
				beliefsMap.put(k, beliefsList);
			}
			sourceGroupTupleBeliefs.add(beliefsMap);
		}
		
		for (int j = 0; j < numSources; j++) {
			List<Integer> outputs = new ArrayList<Integer>();
			for (int i = 0; i < numTuples; i++) {
				for (int k = 0; k < numGroups; k++) {
					if (groupSources.get(k).contains(j) && sourceGroupTupleBeliefs.get(j).get(k).get(i)) {
						outputs.add(i);
						break;
					}
				}
			}
			sourceOutputs.add(outputs);
		}
		
		List<Integer> groupTrueTrueInit = new ArrayList<Integer>();
		List<Integer> groupTrueFalseInit = new ArrayList<Integer>();
		List<Integer> groupFalseTrueInit = new ArrayList<Integer>();
		List<Integer> groupFalseFalseInit = new ArrayList<Integer>();
		
		for (int k = 0; k < numGroups; k++) {
			groupTrueTrueInit.add(4);
			groupTrueFalseInit.add(4);
			groupFalseTrueInit.add(4);
			groupFalseFalseInit.add(35);
		}
		
		List<Integer> sourceTrueTrueInit = new ArrayList<Integer>();
		List<Integer> sourceTrueFalseInit = new ArrayList<Integer>();
		List<Integer> sourceFalseTrueInit = new ArrayList<Integer>();
		List<Integer> sourceFalseFalseInit = new ArrayList<Integer>();
		
		for (int j = 0; j < numSources; j++) {
			sourceTrueTrueInit.add(4);
			sourceTrueFalseInit.add(4);
			sourceFalseTrueInit.add(4);
			sourceFalseFalseInit.add(35);
		}
		
		Map<Integer, Boolean> tupleTruth = new HashMap<Integer, Boolean>();
		Double trueLabelProb = 0.2;
		Double falseLabelProb = 0.2;
		for (int i = 0; i < numTuples; i++) {
			if (tupleTruths.get(i) && Math.random() < trueLabelProb) {
				tupleTruth.put(i, true);
			} else if (!tupleTruths.get(i) && Math.random() < falseLabelProb) {
				tupleTruth.put(i, false);
			}
		}
		
		Set<Integer> trueTuples = new HashSet<Integer>();
		for (int i = 0; i < numTuples; i++) {
			if (tupleTruths.get(i)) {
				trueTuples.add(i);
			}
		}
		
		Set<Integer> outputtedTuples = new HashSet<Integer>();
		for (int j = 0; j < numSources; j++) {
			outputtedTuples.addAll(sourceOutputs.get(j));
		}
		
		//final int numTrueTuples = trueTuples.size();
		//final int numOutputtedTuples = outputtedTuples.size();

		ModelInstance modelInstance = new ModelInstance(numTuples, numGroups, numSources, groupSources, 
				sourceOutputs, groupTrueTrueInit, groupTrueFalseInit, groupFalseTrueInit, groupFalseFalseInit, 
				sourceTrueTrueInit, sourceTrueFalseInit, sourceFalseTrueInit, sourceFalseFalseInit, tupleTruth) ;
	
		DenseSample denseSample = new DenseSample(modelInstance);
		out.println("Current base truth rate:\t" + denseSample.tupleTruthProb());
		final int numSamples = 4;
		final int burnIn = 10000;
		final int thinFactor = 10000;
		List<DenseSample> samples = denseSample.GibbsSampling(numSamples, burnIn, thinFactor);

		List<Double> tupleTruthExp = new ArrayList<Double>();
		List<List<Double>> groupTupleBeliefExp = new ArrayList<List<Double>>();
		List<Map<Integer, List<Double>>> sourceGroupTupleBeliefExp = new ArrayList<Map<Integer, List<Double>>>();
		
		for(int i = 0; i < modelInstance.numTuples; i++) {
			tupleTruthExp.add(0.0);
		}
		for(int k = 0; k < modelInstance.getNumGroups(); k++) {
			List<Double> tt = new ArrayList<Double>();
			for (int i = 0; i < modelInstance.numTuples; i++) {
				tt.add(0.0);
			}
			groupTupleBeliefExp.add(tt);
		}
		for (int j = 0; j < modelInstance.getNumSources(); j++) {
			Map<Integer, List<Double>> tm = new HashMap<Integer, List<Double>>();
			for (int k : modelInstance.sourceGroups.get(j)) {
				List<Double> tt = new ArrayList<Double>();
				for (int i = 0; i < modelInstance.numTuples; i++) {
					tt.add(0.0);
				}
				tm.put(k,tt);
			}
			sourceGroupTupleBeliefExp.add(tm);	
		}

		for (DenseSample sample : samples) {
			for(int i = 0; i < modelInstance.numTuples; i++) {
				tupleTruthExp.set(i, tupleTruthExp.get(i) + (sample.getVal(i) ? 1.0 : 0.0));
			}
			for(int k = 0; k < modelInstance.getNumGroups(); k++) {
				List<Double> tt = groupTupleBeliefExp.get(k);
				for (int i = 0; i < modelInstance.numTuples; i++) {
					tt.set(i, tt.get(i) + (sample.getVal(i, k) ? 1.0 : 0.0));
				}
			}
			for (int j = 0; j < modelInstance.getNumSources(); j++) {
				Map<Integer, List<Double>> tm = sourceGroupTupleBeliefExp.get(j); 
				for (int k : modelInstance.sourceGroups.get(j)) {
					List<Double> tt = tm.get(k); 
					for (int i = 0; i < modelInstance.numTuples; i++) {
						tt.set(i, tt.get(i) + (sample.getVal(i, j, k) ? 1.0 : 0.0));
					}
				}
			}
		}

		for(int i = 0; i < modelInstance.numTuples; i++) {
			tupleTruthExp.set(i, tupleTruthExp.get(i) / numSamples);
		}
		for(int k = 0; k < modelInstance.getNumGroups(); k++) {
			List<Double> tt = groupTupleBeliefExp.get(k);
			for (int i = 0; i < modelInstance.numTuples; i++) {
				tt.set(i, tt.get(i) / numSamples);
			}
		}
		for (int j = 0; j < modelInstance.getNumSources(); j++) {
			Map<Integer, List<Double>> tm = sourceGroupTupleBeliefExp.get(j); 
			for (int k : modelInstance.sourceGroups.get(j)) {
				List<Double> tt = tm.get(k); 
				for (int i = 0; i < modelInstance.numTuples; i++) {
					tt.set(i, tt.get(i) / numSamples);
				}
			}
		}
		
		for (DenseSample sample : samples.subList(0,1)) {
			out.println(sample.tupleTruthProb());
			for (int k = 0; k < modelInstance.getNumGroups(); k++) {
				out.println(sample.groupBeliefProb(k, true));
				out.println(1 - sample.groupBeliefProb(k, false));				
			}
			for (int j = 0; j < modelInstance.getNumSources(); j++) {
				out.println(sample.sourceBeliefProb(j, true));
				out.println(1 - sample.sourceBeliefProb(j, false));				
			}
			out.println();
		}
		double tt = 0.0;
		double ft = 0.0;
		double tf = 0.0;
		double ff = 0.0;
		for (int i = 0; i < numTuples; i++) {
			if (trueTuples.contains(i)) {
				tt += tupleTruthExp.get(i);
				ft += 1 - tupleTruthExp.get(i);
			} else {
				tf += tupleTruthExp.get(i);
				ff += 1 - tupleTruthExp.get(i);
			}
		}	
		out.println("true acc:\t" + (tt/(tt+ft)));
		out.println("false acc:\t" + (ff/(ff+tf)));

		Map<Integer, Integer> trueSourceCount = new HashMap<Integer, Integer>();
		for (int j = 0; j <= modelInstance.getNumSources(); j++) {
			trueSourceCount.put(j,0);
		}
		
		Map<Integer, Integer> falseSourceCount = new HashMap<Integer, Integer>();
		for (int j = 0; j <= modelInstance.getNumSources(); j++) {
			falseSourceCount.put(j,0);
		}
		for (int i = 0; i < numTuples; i++) {
			int sourceCount = 0;
			for (int j = 0; j < modelInstance.getNumSources(); j++) {
				if (sourceOutputs.get(j).contains(i)) {
					sourceCount++;
				}
			}
			if (tupleTruths.get(i)) {
				trueSourceCount.put(sourceCount, 1 + trueSourceCount.get(sourceCount));
			} else {	
				falseSourceCount.put(sourceCount, 1 + falseSourceCount.get(sourceCount));
			}
		}
		out.println(trueSourceCount.toString());
		out.println(falseSourceCount.toString());
	}
}
