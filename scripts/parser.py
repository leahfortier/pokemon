#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import requests
from lxml import html

from scripts.util import get_query_text


class Parser:
    def __init__(self, lookup_num):
        self.lookup_num = lookup_num
        self.table_check = True
        self.div_check = True

        page = requests.get('http://www.serebii.net/pokedex-sm/' + str(lookup_num).zfill(3) + '.shtml')
        tree = html.fromstring(page.text)
        self.main_div = tree.xpath('/html/body/table[2]/tr[2]/td[2]/font/div[2]/div')[0]

        if lookup_num < 722:
            self.index = 1
            self.info_table = self.main_div.xpath('p[1]')[0].getnext()
        else:
            self.index = 2
            self.info_table = self.main_div.xpath('table[' + str(self.index) + ']')[0]

        self.backup_table = self.info_table
        self.backup_index = self.index
        self.backup()

    def update_table(self, *headers):
        for header in headers:
            if self.update_table_header(header):
                return True
        return False

    def update_table_header(self, header):
        # Store current tables and indexes and such to restore if table not found
        temp_index = self.index
        temp_table_check = self.table_check
        temp_div_check = self.div_check
        temp_info_table = self.info_table
        
        while True:
            # We found the header we're looking for!
            if self.check_header(header):
                return True

            self.get_next()
            if self.info_table is None:
                # Header not found -- restore original index values and return false
                self.index = temp_index
                self.table_check = temp_table_check
                self.div_check = temp_div_check
                self.info_table = temp_info_table
                return False

    def get_next(self):
        # Try traditional next element first
        self.info_table = self.info_table.getnext()
        
        if self.info_table is None:
            # If that fails check the next p element
            self.index += 1
            self.info_table = self.main_div.xpath('p[' + str(self.index) + ']')

            if len(self.info_table) > 0:
                self.info_table = self.info_table[0].getnext()
            else:
                self.info_table = None
                if self.lookup_num < 152 and self.div_check:
                    # Gen 1 decided to be different with Let's Go and put 
                    # everything starting from attacks into a separate div....
                    self.div_check = False
                    self.info_table = self.main_div.xpath('div/ul/li/table')[0]
                elif self.lookup_num >= 650 and self.table_check:
                    self.index += 1
                    self.info_table = self.main_div.xpath('table[' + str(self.index) + ']')

                    if len(self.info_table) == 0:
                        self.index = 1
                        self.table_check = False
                        self.info_table = self.main_div.xpath('p[1]')[0].getnext()
                    else:
                        self.info_table = self.info_table[0]

    def get_schema_index(self, schema, column_name):
        for index, column in enumerate(schema.getchildren()):
            if column.text == column_name:
                return index

    def check_queries(self, *queries):
        for query_string in queries:
            query = self.info_table.xpath(query_string)
            text = get_query_text(query)
            if text is not None:
                return text

    def check_header(self, header):
        if self.info_table.tag == 'table':
            text = self.check_queries('tr[1]/td/b', 'tr[1]/td', 'thead/tr[1]/td')
            if text is not None and text == header:
                return True
        return False

    def backup(self):
        self.backup_table = self.info_table
        self.backup_index = self.index
        self.backup_div = self.div_check

    def restore_backup(self):
        self.info_table = self.backup_table
        self.index = self.backup_index
        self.div_check = self.backup_div
