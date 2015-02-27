# -*- coding: utf-8 -*- 
'''
Created on Jul 27, 2012

@author: bowu
'''
from FeatureFactory import *
import math
import pickle
import re
import string
'''template function section'''
Function_Debug = False
def Cap(str):
    if Function_Debug:
        return colorCoding(str,_cap)
    else:
        return _cap(str)
def _cap(str):
    if str == "":
        return ""    
    return str[:1].upper()+str[1:]
def Uppercase(str):
    if Function_Debug:
        return colorCoding(str,_uppercase)
    else:
        return _uppercase(str)
def _uppercase(str):
    return str.upper()
def Lowercase(str):
    if Function_Debug:
        return colorCoding(str, _lowercase)
    else:
        return _lowercase(str)
def _lowercase(str):
    return str.lower();
def Firstletter(str):
    if Function_Debug:
        return colorCoding(str, _firstletter)
    else:
        return _firstletter(str)
def _firstletter(str):
    if str == "":
        return ""
    return str[0:1]
def colorCoding(value, func):
    pattern = re.compile("{_S}(.+){_C}(.+){_C}(.+){_S}")
    m = pattern.match(value)
    ret = []
    if m:
        #check the {L}       
        ret = [m.group(1), m.group(2), m.group(3), 0]
    else:
        pattern1 = re.compile("{_L}(.+){_C}(.+){_C}(.+){_L}")
        m1 = pattern1.match(value)
        if m1:
            return [m.group(1), m.group(2), m.group(3), 1]
        else:
            ret = []
    res = ""
    if len(ret) != 4:
        return "";
    ret[2] = func(ret[2])
    if ret[3] == 1:
        res = '{_L}%s{_C}%s{_C}%s{_L}'%(ret[0],ret[1],ret[2])
    else:
        res = '{_S}%s{_C}%s{_C}%s{_S}'%(ret[0],ret[1],ret[2])
    return res
def indexOf(str, lregx, rregx, cnt=0):
        '''find the position'''
        pos = -1
        patternstr = "(" + lregx + ")" + rregx
        pattern = re.compile(patternstr)
        tpos = 0
        poslist = []
        pre = -1
        while True:
            if tpos >= len(str):  # a bug in python regobj search method starpos 
                break
            m = pattern.search(str, tpos)
            if m == None:
                break
            if len(m.groups()) < 2:
                tpos = m.start() + 1
            else:
                if lregx == "^" or rregx == "$":
                    tpos = m.start()+1
                else:
                    tpos = m.start() + len(m.group(2))
            cpos = m.start() + len(m.group(1))
            if cpos > pre:
                poslist.append(cpos)
                pre = cpos
        index = 0;
        if cnt > 0:
            index = cnt - 1
        else:
            index = len(poslist) + cnt
        if len(poslist) == 0 or index >= len(poslist) or index < 0:
            return None
        return poslist[index]
        
def loop(value, stript):
    res = "";
    cnt = 1;
    while True:
        tmpstript = stript
        if tmpstript.find("counter") == -1:
            break
        tmpstript = tmpstript.replace("counter", str(cnt))
        s = eval(tmpstript)
        if s.find("_FATAL_ERROR_") != -1:
            break
        res += s
        cnt += 1
    if Function_Debug:
        return "{_L}%s{_L}"%res
    else:
        return res
def substr(str, p1, p2):
    '''get substring'''
    if (p1 == None or p1 < 0) and p2 != None:
        return "<_2_FATAL_ERROR_>"
    if p1 != None and (p2 == None or p2 > len(str)):
        return "<_2_FATAL_ERROR_>"
    if p1 == None and p2 == None:
        return "<_3_FATAL_ERROR_>"
    if p1>p2:
        return "<_1_FATAL_ERROR_>"
        
    if Function_Debug:
        return "{_S}%d{_C}%d{_C}%s{_S}"%(p1,p2,str[p1:p2])
    res = str[p1:p2]
    if res != None:
        return res
    else:
        return "<_1_FATAL_ERROR_>"
def foreach(elems, exps):
    '''for each '''
    for i in range(len(elems)):
        value = elems[i]
        elems[i] = eval(exps)
    return elems
def switch(tuplelist):
    for (condi, expre) in tuplelist:
        c = str(condi)
        if c == "True":
            return expre # already evaluated directly return
def getClass(setting, value):
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
        
