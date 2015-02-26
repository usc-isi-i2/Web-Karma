# -*- coding: utf-8 -*- 
'''
Created on Jul 27, 2012

@author: bowu
'''
from edu.isi.karma.cleaning import InterpreterType
import re
from Translator import  *
import string
from FunctionList import *
import FunctionList
import sys
class Interpreter(InterpreterType):
    def __init__(self, script):
	script = script.encode("utf-8","ignore")
	script = script.decode("utf-8")
        trans = Translator(script)
        self.script=trans.translate(script) 
    def execute(self,value):
        value = value.encode("utf-8","ignore")
	value = value.decode("utf-8")
        FunctionList.Function_Debug = False;
        value = eval(self.script)
	return str(value).decode("utf-8")
    def execute_debug(self,value):
        FunctionList.Function_Debug = True;
        value = value.encode("utf-8","ignore")
	value = value.decode("utf-8")
        value = eval(self.script)
        return str(value).decode("utf-8")

if __name__ == "__main__":
    s = "1 normandie ave, Los angels"
    scripts = '''substr(value,indexOf(value,"SYB","[.|\s]+"),indexOf(value,"[a-z]+","END"))+substr(value,indexOf(value,"[a-z]+","SYB"),indexOf(value,",","[.|\s]+"))+substr(value,indexOf(value,"START","[.]+"),indexOf(value,"[a-z]+",","))'''
    a = Interpreter(scripts)
    a.execute(s)
