# -*- coding: utf-8 -*- 
'''
Created on Aug 22, 2012

@author: bowu
'''
from Feature import *
from sets import *
class FeatureFactory:
    def __init__(self):
        self.datatable = []
        self.specialchars = ("#",";",",","!","~","@","\$","%","\^","&","\*","\("\
                       ,"\)","_","-","{","}","\[","\]","\"","\'",":","\?","<",">","\.","\/","\\\\","\d+","[A-Z]+","[a-z]+","[\s]")
        self.classes = [];
    def createFeature(self,value,label):
        self.classes.append(label)
        tmplist = []        
        for c in self.specialchars:
            ft = CntFeature("attr_"+c,value,c)
            score = ft.computerScore();
            tmplist.append(score)
        pt1 = PercentageFeature("attr_pernum",value,"\d+")
        pt2 = PercentageFeature("attr_perU",value,"[A-Z]+")
        pt3 = PercentageFeature("attr_perL",value,"[a-z]+")
        tmplist.append(pt1.computerScore())
        tmplist.append(pt2.computerScore())
        tmplist.append(pt3.computerScore())
        self.datatable.append(tmplist)
    def getFeatureNames(self):
        return self.specialchars
    def getClasses(self):
        return self.classes
if __name__ == "__main__":
    s = ["Bulevar kralja Aleksandra*156","Dositejeva*22","Bobby's Restaurant","1 Lombard street","5th ave","09/07/2008"]
    ff = FeatureFactory()
    for value in s:
        ff.createFeature(value, "-1")
