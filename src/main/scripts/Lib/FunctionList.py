'''
Created on Jul 27, 2012

@author: bowu
'''
import re
import string
import math
from FeatureFactory import *
import pickle
'''template function section'''
def indexOf(str,lregx,rregx,cnt=0):
        '''find the position'''
        pos = 0
        if lregx == "START":
           pos = 0
           return pos
        if rregx =="END":
           pos = len(str)
           return pos
        if cnt > 0:
            cnt = cnt-1
        pattern = "("+lregx+")"+rregx
        list = []
        for m in re.finditer(pattern, str):
            list.append(m)
        if (cnt<0 and (-cnt)>len(list)) or (cnt >=0 and cnt >=len(list)):
            return None
        m = list[cnt]
        if m!= None:
            pos = m.start()+len(m.group(1))
        else:
            print "cannot evaluate "+"("+lregx+")"+rregx+" in "+str
            pos = -1
            return None
        return pos
def loop(value,stript):
    res = "";
    cnt = 1;
    while True:
        tmpstript = stript
        tmpstript = tmpstript.replace("counter",str(cnt))
        s = eval(tmpstript)
        if s.find("<_FATAL_ERROR_>")!=-1:
            break
        res += s
        cnt += 1
    return res
def substr(str,p1,p2):
    '''get substring'''
    if p1 == None or p2 == None:
        return "<_FATAL_ERROR_>"
    res = str[p1:p2]
    if res != None:
        return res
    else:
        print '''cannot get substring using'''+str+'('+p1+','+p2+')'
        return "<_FATAL_ERROR_>"
def foreach(elems,exps):
    '''for each '''
    for i in range(len(elems)):
        value = elems[i]
        elems[i] = eval(exps)
    return elems
def switch(tuplelist):
    for (condi,expre) in tuplelist:
        c = str(condi)
        if c == "True":
            return expre # already evaluated directly return
def getClass(setting,value):
    setting = setting.decode("string-escape")
    #print setting
    classifier = pickle.loads(setting)
    feature = FeatureFactory()
    feature.createFeature(value, "")
    dict = {};
    dict['attributes'] = {}
    attributes = []
    line = feature.datatable[0]
    for i in range(len(line)):
        dict['attributes'][str(i)] = line[i]
        attributes.append(str(i))
    res = classifier.predict(dict)
    r = max(res.iterkeys(), key=lambda k: res[k])     
    return r        
        