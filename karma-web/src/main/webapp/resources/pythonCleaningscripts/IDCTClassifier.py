'''
Created on Aug 22, 2012

@author: bowu
'''
from edu.isi.karma.cleaning import PartitionClassifierType
from FeatureFactory import *
import sys
from NaiveBayes import *
import pickle
import re
from FunctionList import *
class IDCTClassifier(PartitionClassifierType):
    def __init__(self):
        self.path = "./"
        self.featureFactory = FeatureFactory()
    def addTrainingData(self, value, label):
        self.featureFactory.createFeature(value, label)
    def learnClassifer(self):
       model = NaiveBayes()
       dict = {};
       dict['cases'] = 1
       attributes = []
       for j in range(len(self.featureFactory.datatable)):
           dict = {};
           dict['cases'] = 1
           dict['attributes'] = {}
           line = self.featureFactory.datatable[j]
           for i in range(len(line)):
               dict['attributes'][str(i)] = line[i]
               attributes.append(str(i))
           dict['label'] = self.featureFactory.classes[j]
           model.add_instances(dict)
       model.set_real(attributes)
       model.train()
       self.model = model
       return pickle.dumps(model).encode('string_escape')
       
    def getLabel(self, value):
        feature = FeatureFactory()
        feature.createFeature(value, "")
        dict = {};
        dict['attributes'] = {}
        attributes = []
        line = feature.datatable[0]
        for i in range(len(line)):
            dict['attributes'][str(i)] = line[i]
            attributes.append(str(i))
        res = self.model.predict(dict)
	r = max(res.iterkeys(),key=lambda k:res[k])
	return r
        
if __name__ == "__main__":
    s = ["Bulevar kralja Aleksandra*156", "Dositejeva*22", "Bobby's Restaurant", "1 Lombard street", "5th ave", "09/07/2008"]
    test = ["Marina Del Ray*1378", "USC*67", "w 37th st", "2817 s normandie ave", "89th ave", "08/07/2007"]
    ff = IDCTClassifier()  
    ff.addTrainingData(s[0], '1')
    ff.addTrainingData(s[1], '1')
    ff.addTrainingData(s[2], '2')
    ff.addTrainingData(s[3], '2')
    ff.addTrainingData(s[4], '2')
    ff.addTrainingData(s[5], '3')
    y = ff.learnClassifer()
    
    for value in test:
        x = str(getClass(y,value)=='1')
        print x =="True"
        
   
