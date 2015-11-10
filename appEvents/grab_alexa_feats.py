import sys
import random
from sets import Set
import urllib2
import sympy
import cPickle as pickle

srcFeatures = {}
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

def getFeatures(srcId, srcName):
    siteString = "http://www.alexa.com/siteinfo/";
    urlString = siteString + srcName
    try:
        response = urllib2.urlopen(urlString)
        page = response.read()
        pageLines = page.split("\n")
        totalLines = len(pageLines)
        lidx = 0
        while True:
            inputLine = pageLines[lidx]

            # Rank
            if inputLine.find("Global rank icon") != -1:
                lidx += 2
                inputLine = pageLines[lidx]
                rank = inputLine[:inputLine.index('<')]
                rank = rank.replace(',','')
                srcFeatures[srcId]['Rank'] = int(rank)
                featureValues['Rank'].append(int(rank))

            # Rank in Country
            if inputLine.find("The rank by country is calculated using a combination") != -1:
                lidx += 9
                inputLine = pageLines[lidx]
                cRank = inputLine[:inputLine.index('<')]
                cRank = cRank.replace(',','')
                srcFeatures[srcId]['Country Rank'] = int(cRank)
                featureValues['Country Rank'].append(int(cRank))

            # Country
            #if inputLine.find("<strong>Estimated Pageviews</strong>") != -1:
            #    lidx += 8
            #    inputLine = pageLines[lidx]
            #    country = inputLine[inputLine.index("<span class='text-inline'>")+len("<span class='text-inline'>"):inputLine.index("</span></span></td>")]
            #    srcFeatures[srcId]['Country'] = country

            # New Country
            if inputLine.find('<span class="countryRank">') != -1:
                lidx += 2
                inputLine = pageLines[lidx]
                country = inputLine[inputLine.index("title='")+len("title='"):inputLine.index("'>")]
                srcFeatures[srcId]['Country'] = country
                featureValues['Country'].append(country)

            # Bounce rate
            if inputLine.find("Bounce Rate</h4>") != -1:
                lidx += 3
                inputLine = pageLines[lidx]
                bounceRate = inputLine[:inputLine.index("%")]
                bounceRate = bounceRate.replace(',','')
                srcFeatures[srcId]['Bounce Rate'] = float(bounceRate)
                featureValues['Bounce Rate'].append(float(bounceRate))

            # Daily pageviews per visitor
            if inputLine.find("Daily Pageviews per Visitor</h4>") != -1:
                lidx += 3
                inputLine = pageLines[lidx]
                pageViews = inputLine[:inputLine.index("<")]
                pageViews = pageViews.replace(',','')
                srcFeatures[srcId]['Daily Page Views Per Visitor'] = float(pageViews)
                featureValues['Daily Page Views Per Visitor'].append(float(pageViews))

            # Daily time on site per visitor
            if inputLine.find("Daily Time on Site</h4>") != -1:
                lidx += 3
                inputLine = pageLines[lidx]
                time = inputLine[:inputLine.index("<")]
                time = time.replace(',','')
                time = time.split(':')
                totalTime = int(time[0])*60 + int(time[1])
                srcFeatures[srcId]['Daily Time on Site'] = totalTime
                featureValues['Daily Time on Site'].append(totalTime)

            # Search Visits
            if inputLine.find("Search Visits</h4>") != -1:
                lidx += 3
                inputLine = pageLines[lidx]
                visits = inputLine[:inputLine.index("%")]
                visits = visits.replace(',','')
                srcFeatures[srcId]['Search Visits'] = float(visits)
                featureValues['Search Visits'].append(float(visits))

            # Total Sites Linking In
            if inputLine.find("Total Sites Linking In</h5>") != -1:
                lidx += 1
                inputLine = pageLines[lidx]
                links = inputLine[inputLine.index(">")+1:inputLine.index("</span")]
                links = links.replace(',','')
                srcFeatures[srcId]['Total Sites Linking In'] = int(links)
                featureValues['Total Sites Linking In'].append(int(links))

            lidx += 1
            if lidx >= totalLines:
                break

        return 0
    except:
	print "Unexpected error:", sys.exc_info()[0]
        return -1



# Read sources and extract features
sources = {}
lineReached = 0
sFile = open('data/sources.csv','r')
for l in sFile.readlines():
    l = l.rstrip("\n")
    lineReached += 1
    print l
    # l[0] -> id in raw dataset
    # l[1] -> website for alexa crawling    
    if l > 5451 and l < 6588:
        srcFeatures[l] = {}
        status = getFeatures(l,l)
        if status == 0:
            print "\n\n lineReached = ",lineReached
            sources[l] = l

pickle.dump(sources,open("sourcesCrawled54516588.pkl","wb"))
pickle.dump(srcFeatures,open("srcFeatsCrawled54516588.pkl","wb"))







