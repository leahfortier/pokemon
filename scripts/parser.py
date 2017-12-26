#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from lxml import html
import requests

class Parser:
    def __init__(self, lookupNum):
        self.lookupNum = lookupNum
        self.tableCheck = True
        
        page = requests.get('http://www.serebii.net/pokedex-sm/' + str(lookupNum).zfill(3) + '.shtml')
        tree = html.fromstring(page.text)
        self.mainDiv = tree.xpath('/html/body/table[2]/tr[2]/td[2]/font/div[2]/div')[0];
        
        if lookupNum < 722:
            self.index = 1
            self.infoTable = self.mainDiv.xpath('p[1]')[0].getnext()
        else:
            self.index = 2
            self.infoTable = self.mainDiv.xpath('table[2]')[0]
    
    def updateTable(self, *headers):
        for header in headers:
            if self.updateTableIndex(header, 0):
                return True
        return False
    
    def updateTableIndex(self, header, headerIndex):
        tempIndex = self.index
        tempTableCheck = self.tableCheck
        tempInfoTable = self.infoTable
    
        while True:
            if self.checkHeader(header, headerIndex):
                return True
    
            if self.lookupNum < 650:
                self.infoTable = self.infoTable.getnext()
                    
                if self.infoTable == None:
                    self.index += 1
                    self.infoTable = self.mainDiv.xpath('p[' + str(self.index) + ']')
    
                    if len(self.infoTable) == 0:
                        self.index = tempIndex;
                        self.infoTable = tempInfoTable
                        return False
    
                    self.infoTable = self.infoTable[0].getnext()
            else:
                if self.tableCheck:                
                    self.index += 1
                    self.infoTable = self.mainDiv.xpath('table[' + str(self.index) + ']')
    
                    if len(self.infoTable) == 0:
                        self.index = 1
                        self.tableCheck = False
                        self.infoTable = self.mainDiv.xpath('p[1]')[0].getnext()
                    else:
                        self.infoTable = self.infoTable[0]
                else:
                    self.infoTable = self.infoTable.getnext()
    
                    if self.infoTable == None:
                        self.index += 1
                        self.infoTable = self.mainDiv.xpath('p[' + str(self.index) + ']')
    
                        if len(self.infoTable) == 0:
                            self.index = tempIndex;
                            self.tableCheck = tempTableCheck
                            self.infoTable = tempInfoTable
                            return False
    
                        self.infoTable = self.infoTable[0].getnext()

    def getSchemaIndex(self, schema, columnName):
        for index, column in enumerate(schema.getchildren()):
            if column.text == columnName:
                return index

    def getElementText(self, element):
        text = element.text
        if not text is None:
            return text
        return element.text_content()
    
    def getQueryText(self, query):
        for querychild in query:
            text = self.getElementText(querychild)
            if not text is None:
                return text
            for child in querychild.getchildren():
                text = self.getElementText(child)
                if not text is None:
                    return text
    
    def checkQueries(self, *queries):
        for queryString in queries:
            query = self.infoTable.xpath(queryString)
            text = self.getQueryText(query)
            if not text is None:
                return text
    
    def checkHeader(self, header, headerIndex):
        if self.infoTable.tag == 'table':
            text = self.checkQueries('tr[1]/td/b', 'tr[1]/td', 'thead/tr[1]/td')
            if not text is None and text[headerIndex:] == header:
                return True
    
        return False