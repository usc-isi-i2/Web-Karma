# -*- coding: utf-8 -*- 
from math import exp, pi

class NaiveBayes:    
    
    def __init__(self):
        self.labels = []
        self.stat_labels = {}
        self.numof_instances = 0
        self.attributes = []                
        self.attvals = {}
        self.real_stat = {}
        self.stat_attributes = {}
        self.smoothing = {}
        self.attribute_type = {}  
        self.gaussian = 1 / ((2 * pi) ** (1 / 2))
        
    def set_real(self, attributes):
        for attribute in attributes:
            self.attribute_type[attribute] = 'real' 
            
    def set_smoothing(self, attributes):
        for attribute in attributes.keys():
            self.smoothing[attribute] = attributes[attribute]    
    
    def drop_attributes(self, attributes):
        for attribute in attributes:
            del self.attvals[attribute]
            del self.stat_attributes[attribute]
            del self.attribute_type[attribute]
            if attribute in self.real_stat.keys():
                del self.real_stat[attribute]
            if attribute in self.smoothing.keys():
                del self.smoothing[attribute]        
        new_attributes = []
        for attribute in self.attributes:
            if attribute not in attributes:
                new_attributes.append(attribute)
        self.attributes = new_attributes        
        
    def add_instances(self, params):
        if not 'attributes' in params.keys() or not 'label' in params.keys() or not 'cases' in params.keys():
            raise Exception('Missing instance parameters')
        if len(self.stat_attributes.keys()) == 0:
            for attribute in params['attributes'].keys():
                self.stat_attributes[attribute] = {}
                self.attributes.append(attribute)
                self.attvals[attribute] = []
                if not attribute in self.attribute_type.keys():
                    self.attribute_type[attribute] = 'nominal'
        else:
            for attribute in self.attribute_type.keys():
                if not attribute in params['attributes'].keys():
                    raise Exception('Attribute not given in instance: ' + attribute)                    
        self.numof_instances += params['cases']
        if not params['label'] in self.stat_labels.keys():
            self.labels.append(params['label'])
            self.stat_labels[params['label']] = 0        
        self.stat_labels[params['label']] += params['cases']        
        for attribute in self.stat_attributes.keys():
            if not attribute in params['attributes'].keys():
                raise Exception('Attribute ' + attribute + ' not given')
            attval = params['attributes'][attribute]
            if not attval in self.stat_attributes[attribute].keys():
                self.attvals[attribute].append(attval)
                self.stat_attributes[attribute][attval] = {}
            if not params['label'] in self.stat_attributes[attribute][attval].keys():
                self.stat_attributes[attribute][attval][params['label']] = 0                
            self.stat_attributes[attribute][attval][params['label']] += params['cases']                           
                
    def train(self):
        self.model = {'lprob' : {}, 'cprob' : {}, 'real_stat' : {}}        
        for label in self.stat_labels.keys():
            self.model['lprob'][label] = self.stat_labels[label] * 1.0 / self.numof_instances        
        for attribute in self.stat_attributes.keys():
            if not self.attribute_type[attribute] == 'real':
                self.model['cprob'][attribute] = {}
                for label in self.stat_labels.keys():
                    total = 0
                    attvals = []               
                    for attval in self.stat_attributes[attribute].keys():
                        if label in self.stat_attributes[attribute][attval].keys() and self.stat_attributes[attribute][attval][label] > 0:
                            attvals.append(attval)
                            if not attval in self.model['cprob'][attribute].keys():
                                self.model['cprob'][attribute][attval] = {}
                            self.model['cprob'][attribute][attval][label] = self.stat_attributes[attribute][attval][label]
                            total += self.model['cprob'][attribute][attval][label]
                    if attribute in self.smoothing.keys():
                        uc = self.smoothing[attribute]
                        if uc <= 0:
                            uc = 0.5
                        if not '*' in self.model['cprob'][attribute].keys():
                            self.model['cprob'][attribute]['*'] = {}
                        self.model['cprob'][attribute]['*'][label] = uc;
                        total += uc
                        if '*' in attvals:
                            raise Exception("'*' as attribute value has been reserved")
                        attvals.append('*')
                    for attval in attvals:
                        self.model['cprob'][attribute][attval][label] /= total * 1.0
                        
            else:
                if attribute in self.smoothing.keys():
                    raise Exception('Smoothing has been set for real attribute ' + attribute)
                self.model['real_stat'][attribute] = {}
                for attval in self.stat_attributes[attribute].keys():
                    for label in self.stat_attributes[attribute][attval]:
                        if not label in self.model['real_stat'][attribute]:
                            self.model['real_stat'][attribute][label] = {'sum' : 0, 'count' : 0, 'mean' : 0, 'sigma' : 0}
                        self.model['real_stat'][attribute][label]['sum'] += float(attval) * self.stat_attributes[attribute][attval][label]
                        self.model['real_stat'][attribute][label]['count'] += self.stat_attributes[attribute][attval][label]                    
                        if self.model['real_stat'][attribute][label]['count'] > 0:
                            self.model['real_stat'][attribute][label]['mean'] = self.model['real_stat'][attribute][label]['sum'] * 1.0 / self.model['real_stat'][attribute][label]['count']
                for attval in self.stat_attributes[attribute].keys():
                    for label in self.stat_attributes[attribute][attval]:
                        self.model['real_stat'][attribute][label]['sigma'] += (float(attval) - self.model['real_stat'][attribute][label]['mean']) ** 2 * self.stat_attributes[attribute][attval][label]           
                for label in self.model['real_stat'][attribute]:
                    self.model['real_stat'][attribute][label]['sigma'] = (self.model['real_stat'][attribute][label]['sigma'] * 1.0 / (self.model['real_stat'][attribute][label]['count'])) ** (1 / 2)       
    def predict(self, params):
        if not 'attributes' in params.keys():
            raise Exception('Missing attributes parameter')
        scores = {}
        nsum = 0
        nscores = {}
        for label in self.labels:
            scores[label] = self.model['lprob'][label]
        for attribute in params['attributes'].keys():
            if not attribute in self.attribute_type:
                raise Exception('Unknown attribute ' + attribute)
            if not self.attribute_type[attribute] == 'real':
                attval = params['attributes'][attribute]
                if not attval in self.stat_attributes[attribute] and not attribute in self.smoothing.keys():
                    raise Exception('Attribute value ' + attval + ' not defined')
                for label in self.labels:
                    if attval in self.model['cprob'][attribute] and label in self.model['cprob'][attribute][attval] and self.model['cprob'][attribute][attval][label] > 0:
                        scores[label] *= self.model['cprob'][attribute][attval][label]
                    elif attribute in self.smoothing.keys():
                        scores[label] *= self.model['cprob'][attribute]['*'][label]
                    else:
                        scores[label] = 0
            else:
                for label in self.labels:
                    nscores[label] = self.gaussian * 1.0 / self.model['real_stat'][attribute][label]['sigma'] * exp(-0.5 * ((float(params['attributes'][attribute]) - self.model['real_stat'][attribute][label]['mean']) / self.model['real_stat'][attribute][label]['sigma']) ** 2) # normalize the attribute value
                    nsum += nscores[label]
                if not nsum == 0:
                    for label in self.labels:
                        scores[label] *= nscores[label]
        sumPx = 0
        for label in scores.keys():
            sumPx += scores[label]
        if sumPx == 0: #muliplication can float
            sumPx = 1
        for label in scores.keys():
            scores[label] /= sumPx
        return(scores)
    
    
    def preferredLabel(self, scores):
        maxValue = 0
        prefLabel = None
        for label in scores.keys():
            if scores[label] > maxValue:
                maxValue = scores[label]
                prefLabel = label
        return(prefLabel)
if __name__ == "__main__":
    model = NaiveBayes()
    model.add_instances({'attributes':{'model':0, 'place':0}, 'label':'1', 'cases':1});
    model.add_instances({'attributes':{'model':0, 'place':0}, 'label':'1', 'cases':1});
    model.add_instances({'attributes':{'model':1, 'place':0}, 'label':'2', 'cases':1});
    model.add_instances({'attributes':{'model':1, 'place':0}, 'label':'2', 'cases':1});
    model.add_instances({'attributes':{'model':1, 'place':0}, 'label':'2', 'cases':1});
    model.add_instances({'attributes':{'model':1, 'place':0}, 'label':'2', 'cases':1});
    model.add_instances({'attributes':{'model':1, 'place':1}, 'label':'3', 'cases':1});
    model.add_instances({'attributes':{'model':1, 'place':1}, 'label':'3', 'cases':1});
    model.set_real(["model", "place"])
    model.train() 
    result = model.predict({'attributes' : {'model':-1, 'place':-1}});
    print result