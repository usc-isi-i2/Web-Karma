'''
Created on Jul 27, 2012

@author: bowu
'''

class Translator(object):
    '''
    tranlated the token based scripts into string based expressions
    '''
    def __init__(self,script):
        '''
        Constructor
        '''
        self.token2str = { 'NUM':'[\d]+',\
                      'LWRD':'[a-z]+',\
                      'UWRD':'[A-Z]+',\
                      'BNK':'[\s]+',\
                      'SYB':'[^(0-9|a-z|A-Z|\s)]',\
                      'WORD':'[a-z|A-Z]',\
                      'ANY':'',\
                      }
    def translate(self,script):
        for (key,value) in self.token2str.iteritems():
            script = script.replace(key,value)
        return script