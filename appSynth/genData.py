import sys,os
import numpy as np
import random

numFeatures = 5
numSources = 1000
numTuples = 10000
tuplesPerSource = 100

# creating SrcFeats
srcFeatsDensity = 0.2
srcFeats = np.random.rand(numFeatures + 1, numSources + 1) < srcFeatsDensity
srcFeats = 1 * srcFeats
srcFeats[:,0] = np.zeros(numFeatures + 1)
srcFeats[0,:] = np.zeros(numSources + 1)

# creating feature weights and accuracies
L1 = 5
sigma = int(sys.argv[1]); #noise term
featureWeights = np.random.laplace(0,L1,(numFeatures + 1))
featureWeights[0] = 0
srcAccs = np.dot(np.transpose(srcFeats),featureWeights)
srcAccs = srcAccs + 2 * (float(sigma) / numSources) * (np.random.rand(numSources + 1) - 0.5)
srcAccs = 1 / (1 + np.exp(srcAccs))

Omega = [[]]
for j in range(1, numSources):
	Omega = Omega + [[]]
	for l in range(1,tuplesPerSource):
		Omega[j] = Omega[j] + [random.randint(1, numTuples)];

datafolder = "data" + str(sigma) + "/"
if not os.path.exists(datafolder):
	    os.makedirs(datafolder)


features_file = datafolder + "srcFeatures.csv"
groundtruth_file = datafolder + "groundtruth.csv"
positivevotes_file = datafolder + "positiveVotes.csv"
negativevotes_file = datafolder + "negativeVotes.csv"

f_features = open(features_file, 'w')
f_groundtruth = open(groundtruth_file, 'w')
f_positivevotes = open(positivevotes_file, 'w')
f_negativevotes = open(negativevotes_file, 'w')

for k in range(1,numFeatures):
	for j in range(1,numSources):
		if (srcFeats[k,j] == 1):
			f_features.write('{},{}\n'.format(j,k))

for i in range(1,numTuples):
	f_groundtruth.write('{},true\n'.format(i))

for j in range(1, numSources):
	for k in Omega[j]:
		obs = random.random() < srcAccs[j]
		if obs:
			f_positivevotes.write('{},{}\n'.format(j,k))
		else:
			f_negativevotes.write('{},{}\n'.format(j,k))



