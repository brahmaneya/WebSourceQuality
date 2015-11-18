import sys,os
import numpy as np
import random

numFeatures = 20
numSources = 200
numTuples = 1000

# creating SrcFeats
srcFeatsDensity = 0.2
srcFeats = np.random.rand(numFeatures + 1, numSources + 1) < srcFeatsDensity
srcFeats = 1 * srcFeats
srcFeats[:,0] = np.zeros(numFeatures + 1)
srcFeats[0,:] = np.zeros(numSources + 1)

# creating feature weights and accuracies
L1 = 5
featureWeights = np.random.laplace(0,L1,(numFeatures + 1))
featureWeights[0] = 0
srcAccs = np.dot(np.transpose(srcFeats),featureWeights)
srcAccs = 1 / (1 + np.exp(srcAccs))

tuplesPerSource = 5
outputs = np.zeros((numSources + 1, tuplesPerSource + 1))
for j in range(1,numSources):
	for (i in range(1,tuplesPerSource):
		outputs[j + 1, i + 1] = random.random() < srcAccs[j + 1]



