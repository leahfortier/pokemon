#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import requests
from lxml import html

from util import get_query_text


class Parser:
    def __init__(self, lookup_num):
        self.lookupNum = lookup_num
        self.tableCheck = True
        
        page = requests.get('http://www.serebii.net/pokedex-sm/' + str(lookup_num).zfill(3) + '.shtml')
        tree = html.fromstring(page.text)
        self.mainDiv = tree.xpath('/html/body/table[2]/tr[2]/td[2]/font/div[2]/div')[0]
        
        if lookup_num < 722:
            self.index = 1
            self.infoTable = self.mainDiv.xpath('p[1]')[0].getnext()
        else:
            self.index = 2
            # Zeraora -- "This Pokédex entry is for a Pokémon that has yet to be officially revealed or released. Proceed with caution."
            if self.lookupNum == 807:
                self.index += 1
            self.infoTable = self.mainDiv.xpath('table[' + str(self.index) + ']')[0]

        self.backupTable = self.infoTable
        self.backupIndex = self.index
        self.backup()
    
    def update_table(self, *headers):
        for header in headers:
            if self.update_table_index(header, 0):
                return True
        return False
    
    def update_table_index(self, header, header_index):
        temp_index = self.index
        temp_table_check = self.tableCheck
        temp_info_table = self.infoTable
    
        while True:
            if self.check_header(header, header_index):
                return True
    
            if self.lookupNum < 650:
                self.infoTable = self.infoTable.getnext()
                    
                if self.infoTable is None:
                    self.index += 1
                    self.infoTable = self.mainDiv.xpath('p[' + str(self.index) + ']')
    
                    if len(self.infoTable) == 0:
                        self.index = temp_index
                        self.infoTable = temp_info_table
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
    
                    if self.infoTable is None:
                        self.index += 1
                        self.infoTable = self.mainDiv.xpath('p[' + str(self.index) + ']')
    
                        if len(self.infoTable) == 0:
                            self.index = temp_index
                            self.tableCheck = temp_table_check
                            self.infoTable = temp_info_table
                            return False
    
                        self.infoTable = self.infoTable[0].getnext()

    # I don't think this works in all scenarios but this shit is way too complicated to understand
    def next_table(self):
        if self.lookupNum < 722:
            self.infoTable = self.infoTable.getnext()
        else:
            self.index += 1
            self.infoTable = self.mainDiv.xpath('table[' + str(self.index) + ']')[0]

    def get_schema_index(self, schema, column_name):
        for index, column in enumerate(schema.getchildren()):
            if column.text == column_name:
                return index

    def check_queries(self, *queries):
        for queryString in queries:
            query = self.infoTable.xpath(queryString)
            text = get_query_text(query)
            if text is not None:
                return text
    
    def check_header(self, header, header_index):
        if self.infoTable.tag == 'table':
            text = self.check_queries('tr[1]/td/b', 'tr[1]/td', 'thead/tr[1]/td')
            if text is not None and text[header_index:] == header:
                return True
        return False
    
    def backup(self):
        self.backupTable = self.infoTable
        self.backupIndex = self.index
        
    def restore_backup(self):
        self.infoTable = self.backupTable
        self.index = self.backupIndex
