# -*- coding: utf-8 -*- 
'''
Created on Jul 28, 2012

@author: bowu
'''
from FunctionList import *
from Interpreter import *
from ast import *
def test0():
    '''test concatenation'''
    s = "13 Jan 2008 00:00:00 +0000"
    scripts = '''substr(value,indexOf(value,'START','NUM',1),12)+loop(value,"substr(value,-11,18)")+substr(value,12,-12)
'''
    a = Interpreter(scripts)
    print a.execute(s)
def test1():
    '''test loop \"\" is used to post evaluation of a expression'''
    value = "International Bussiness Machine"
    expression = '''substr(value,indexOf(value,',','UWRD',-1),indexOf(value,'LWRD','END',-1))+substr(value,indexOf(value,'LWRD','SYB',1),-6)+substr(value,indexOf(value,'START','NUM',1),indexOf(value,'LWRD','SYB',-1))'''
    loopexp = "foreach(value.split(),\"substr(value,indexOf(value,'START','UWRD'),indexOf(value,'UWRD','LWRD'))\")"
    e = Interpreter(loopexp)
    print e.execute(value)
def test2():
    exp = '''x+2'''
    print literal_eval(exp)
def test3():
    '''test embeded loop'''
    value = "International Bussiness Machine"
    loopexp = '''foreach(foreach(value.split(),\"substr(value,indexOf(value,'START','UWRD'),indexOf(value,'UWRD','LWRD'))\"),\"value+'See'\")'''
    e = Interpreter(loopexp)
    print e.execute(value)
def test4():
    '''test switch function'''
    condi1 = "len(value) < 10"
    condi2 = "len(value) < 10"
    loopexp1 = "foreach(value.split(),\"substr(value,indexOf(value,'START','UWRD'),indexOf(value,'UWRD','LWRD'))\")"
    loopexp2 = '''foreach(foreach(value.split(),\"substr(value,indexOf(value,'START','UWRD'),indexOf(value,'UWRD','LWRD'))\"),\"value+'see'\")'''
    tuplist = "[("+condi1+","+loopexp1+"),("+condi2+","+loopexp2+")]"
    sw = "switch("+tuplist+")"
    value = "International Bussiness Machine"
    e = Interpreter(sw)
    print e.execute(value)

def test5():
    '''test loop'''
    stript = '''substr(value,indexOf(value,'NUM','\.',1),indexOf(value,'\.','22',1))+'edu'+'/'+'images'+'/'+substr(value,indexOf(value,'ANY','NUM',-1),-12)+'/'+substr(value,indexOf(value,'START','NUM',1),-12)+substr(value,-9,-8)+substr(value,5,16)
'''
    value = "1978.43.8_1a.jpg"
    e = Interpreter(stript)
    print e.execute(value)
if __name__ == "__main__":
    option = "0"
    eval("test"+option+"()")