import sys
from sets import Set
import urllib2

srcFeatures = {}

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
                srcFeatures[srcId]['Rank'] = rank

            # Rank in Country
            if inputLine.find("The rank by country is calculated using a combination") != -1:
                lidx += 9
                inputLine = pageLines[lidx]
                cRank = inputLine[:inputLine.index('<')]
                cRank = cRank.replace(',','')
                srcFeatures[srcId]['Country Rank'] = cRank

            # Country
            if inputLine.find("<strong>Estimated Pageviews</strong>") != -1:
                lidx += 8
                inputLine = pageLines[lidx]
                country = inputLine[inputLine.index("<span class='text-inline'>")+len("<span class='text-inline'>"):inputLine.index("</span></span></td>")]
                srcFeatures[srcId]['Country'] = country

            # Bounce rate
            if inputLine.find("Bounce Rate</h4>") != -1:
                lidx += 3
                inputLine = pageLines[lidx]
                bounceRate = inputLine[:inputLine.index("%")]
                bounceRate = bounceRate.replace(',','')
                srcFeatures[srcId]['Bounce Rate'] = bounceRate

            # Daily pageviews per visitor
            if inputLine.find("Daily Pageviews per Visitor</h4>") != -1:
                lidx += 3
                inputLine = pageLines[lidx]
                pageViews = inputLine[:inputLine.index("<")]
                pageViews = pageViews.replace(',','')
                srcFeatures[srcId]['Daily Page Views Per Visitor'] = pageViews

            # Daily time on site per visitor
            if inputLine.find("Daily Time on Site</h4>") != -1:
                lidx += 3
                inputLine = pageLines[lidx]
                time = inputLine[:inputLine.index("<")]
                time = time.replace(',','')
                srcFeatures[srcId]['Daily Time on Site'] = time

            # Search Visits
            if inputLine.find("Search Visits</h4>") != -1:
                lidx += 3
                inputLine = pageLines[lidx]
                visits = inputLine[:inputLine.index("%")]
                visits = visits.replace(',','')
                srcFeatures[srcId]['Search Visits'] = visits

            # Total Sites Linking In
            if inputLine.find("Total Sites Linking In</h5>") != -1:
                lidx += 1
                inputLine = pageLines[lidx]
                links = inputLine[inputLine.index(">")+1:inputLine.index("</span")]
                links = links.replace(',','')
                srcFeatures[srcId]['Total Sites Linking In'] = links

            lidx += 1
            if lidx >= totalLines:
                break

        return 0
    except:
        return -1

# Read sources and extract features
sources = {}
sFile = open('data/sourceSites.txt','r')
for l in sFile.readlines():
    l = l.rstrip("\n")
    l = l.split("\t")
    # l[0] -> id in raw dataset
    # l[1] -> website for alexa crawling
    srcFeatures[l[0]] = {}
    status = getFeatures(l[0],l[1])
    if status == 0:
        sources[l[0]] = l[1]


# Read raw input
rawInput = open('data/stock-2011-07-01.txt','r')
sourcesInput = {}
stockSymbols = {}
for l in rawInput.readlines():
    l = l.rstrip("\n")
    l = l.split("\t")
    # check if source in sources to consider
    if l[0] in sources:
        if l[0] not in sourcesInput:
            sourcesInput[l[0]] = {}
        # grab stock symbol
        symbol = l[1]
        # grab source volume info
        if l[6] != '' and l[6].find('+') == -1 and l[6]!= '0' and l[6] != '0.00' and l[6] != 'null' and l[6] != 'vol.  0' and l[6].find('-') == -1:
            if l[6].find('k') != -1:
                volume = l[6].replace('k','0').replace('.','')
            elif l[6].find('vol.') != -1:
                volume = l[6].replace('vol.','').lstrip(' ').replace(',','').rstrip(".00")
            elif l[6].find('mil') != -1:
                l[6] = l[6].replace('mil','').rstrip(' ')+'0000'
                volume = l[6].replace('.','')
            elif l[6].find('m') != -1 and l[6].find('mil') == -1:
                volume = l[6].replace('m','0000').replace('.','')
            else:
                volume = l[6].replace(',','').rstrip(".00")

            sourcesInput[l[0]][symbol]= volume
            if symbol not in stockSymbols:
                stockSymbols[symbol] = Set([])
            stockSymbols[symbol].add(volume)


# grab symbol truth
symbolTruth = {}
truthInput = open('data/stock-2011-07-01-nasdaq-com.txt','r')
for l in truthInput.readlines():
    l = l.rstrip("\n")
    l = l.split("\t")
    symbol = l[0]
    volume = l[5].replace(',','')
    symbolTruth[symbol] = volume
truthInput.close()

# Print stock symbol volumes
stockVolumes = open('data/stockVolumes.tsv','w')
for s in stockSymbols:
    for v in stockSymbols[s]:
        newline = str(s)+"\t"+str(v)+"\t"
        if s in symbolTruth:
            if v == symbolTruth[s]:
                newline += "true\n"
            else:
                newline += "false\n"
        else:
            newline += "N/A\n"
        stockVolumes.write(newline)
stockVolumes.close()

srcObservations = open('data/srcStockVolumes.tsv','w')

# Print source stock symbol volumes
for src in sourcesInput:
    for s in sourcesInput[src]:
        newline = str(src)+"\t"+str(s)+"\t"+str(sourcesInput[src][s])+"\ttrue\n"
        srcObservations.write(newline)
srcObservations.close()

# Print source features
srcFeaturesOut = open('data/srcFeatures.csv','w')
for src in srcFeatures:
    for k in srcFeatures[src]:
        newline = str(src)+","+k+"="+srcFeatures[src][k]+"\n"
        srcFeaturesOut.write(newline)
srcFeaturesOut.close()




