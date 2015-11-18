import sys
import random
from sets import Set
import urllib2
import sympy
import cPickle as pickle

featureValues = {}
featureValues['Rank'] = []
featureValues['Country Rank'] = []
featureValues['Country'] = []
featureValues['Bounce Rate'] = []
featureValues['Daily Page Views Per Visitor'] = []
featureValues['Daily Time on Site'] = []
featureValues['Search Visits'] = []
featureValues['Total Sites Linking In'] = []
maxF = {}
minF = {}

# Read sources and src features
sources = pickle.load(open(sys.argv[1],'r'))
srcFeatures = pickle.load(open(sys.argv[2],'r'))

print len(sources)
print len(srcFeatures)

count = 0
for s in srcFeatures:
	if len(srcFeatures[s]) != 0:
		count += 1
print count 
for src in sources:
	for f in srcFeatures[src]:
		featureValues[f].append(srcFeatures[src][f])

# Examine features
for f in featureValues:
	if f != 'Country':
        	maxF[f] = max(featureValues[f])
        	minF[f] = min(featureValues[f])

# Preprocess source features
allFeatures = {}
id2Feature = {}
featureId = 0
for src in sources:
    for k in srcFeatures[src]: 
        if k not in ['Country']:
            fValue = srcFeatures[src][k]#round((srcFeatures[src][k] - minF[k])*10/(maxF[k] - minF[k]))
	    if k not in allFeatures:
		  allFeatures[k] = featureId
		  id2Feature[featureId] = k
		  featureId += 1
	    srcFeatures[src][k] = fValue
	else:
            fValue = srcFeatures[src][k]

# Form SympY SrcFatureValue Matrix
matrixInput = []
for src in sources:
	srcValues = []
	for i in id2Feature:
		if id2Feature[i] in srcFeatures[src]:
			srcValues.append(srcFeatures[src][id2Feature[i]])
		else:
			srcValues.append(-1)
	matrixInput.append(srcValues)
fSympyMatrix = sympy.Matrix(matrixInput)
print fSympyMatrix
fSympyMatrix.rref()
featuresToUse = fSympyMatrix.rref()[1]

print "Total features = ", len(allFeatures)
print "AffineIndepFeatures = ",len(featuresToUse)
print id2Feature
srcFeaturesOut = open('data/srcFeatures.csv','w')
for src in sources:
	usedFeats = 0
	for f in srcFeatures[src]:
		if f in allFeatures:
			if allFeatures[f] in featuresToUse:
				newline = src+","+f+"="+str(srcFeatures[src][f])+"\n"
				srcFeaturesOut.write(newline)
                usedFeats += 1
srcFeaturesOut.close()





