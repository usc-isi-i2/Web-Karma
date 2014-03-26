# -*- coding: utf-8 -*- 
'''
Created on Jul 28, 2012

@author: bowu
'''
import re
def pytest1():
    str = "ahdfkjahiu;';'[;'.ahjfgajhfa"
    pattern = "[^(0-9|a-z|A-Z|\s)]+"
    m = re.search(pattern, str)
    print str[m.start():m.end()]
def pytest2():
    ind = 11
    return ind > 11
if __name__ == "__main__":
    print str(pytest2())[0:3]
   