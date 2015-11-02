# Python scripts for modeling the British Library data 
# using the DPLA ontology.

def blAggregationUri(recordId):
    #return getTextHash(recordId)
    return recordId


def blSourceResourceUri(recordId):
    return blAggregationUri(recordId)+"/sourceResource"


def blAgentUri(lastFirst):
    str = lastFirst.strip().replace('.','').lower()
    list = str.split(',')
    if len(list) == 2:
    	str = list[1] + " " + list[0]
	return "person/" + str.replace(' ','')


def blCleanAgent(name):
	return name.strip().replace('.','')


def blTopicN(topic, index):
	list = topic.split('/')
	return list[index].strip().replace('.','')

def blTopic1(topic):
	return blTopicN(topic,0)

def blTopic2(topic):
	return blTopicN(topic,1)


def blTopicUri(topic):
	return "topic/"+mungeForUri(topic)


def blCollectionTitle(series, number):
	return (series + " " + number).strip()


def blCollectioncUri(title):
	title = title.strip()
	if title :
		return "colection/"+mungeForUri(title)
