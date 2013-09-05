# -*- coding: utf-8 -*- 
'''
Created on Aug 22, 2012

@author: bowu
'''
'''
format feature used to capture the differences between input format.
1. cnt of special symbols
'''
import re
class CntFeature:
    def __init__(self,name,value,tar):
        self.name = name
        self.value = value
        self.tar = tar
    def computerScore(self):
        cnt = 0
        # print self.tar
        pattern = re.compile(self.tar)
        list = pattern.findall(self.value)
        cnt = len(list) 
        return cnt*1.0
class PercentageFeature:
     def __init__(self,name,value,tar):
        self.name = name
        self.value = value
        self.tar = tar
     def computerScore(self):
        cnt = 0
        # print self.tar
        pattern = re.compile(self.tar)
        list = pattern.findall(self.value)
        tarleng = 0
        for a in list:
            tarleng += len(a)
        if len(self.value) == 0:
            return 10000
        return tarleng*1.0/len(self.value)
    
