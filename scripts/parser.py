#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import requests
from lxml import html

from util import get_query_text


class Parser:
    def __init__(self, lookup_num):
        self.lookup_num = lookup_num
        self.table_check = True

        page = requests.get('http://www.serebii.net/pokedex-sm/' + str(lookup_num).zfill(3) + '.shtml')
        tree = html.fromstring(page.text)
        self.main_div = tree.xpath('/html/body/table[2]/tr[2]/td[2]/font/div[2]/div')[0]

        if lookup_num < 722:
            self.index = 1
            self.info_table = self.main_div.xpath('p[1]')[0].getnext()
        else:
            self.index = 2
            # Zeraora -- "This Pokédex entry is for a Pokémon that has yet to be officially revealed or released. Proceed with caution."
            if self.lookup_num == 807:
                self.index += 1
            self.info_table = self.main_div.xpath('table[' + str(self.index) + ']')[0]

        self.backup_table = self.info_table
        self.backup_index = self.index
        self.backup()

    def update_table(self, *headers):
        for header in headers:
            if self.update_table_index(header, 0):
                return True
        return False

    def update_table_index(self, header, header_index):
        temp_index = self.index
        temp_table_check = self.table_check
        temp_info_table = self.info_table

        while True:
            if self.check_header(header, header_index):
                return True

            if self.lookup_num < 650:
                self.info_table = self.info_table.getnext()

                if self.info_table is None:
                    self.index += 1
                    self.info_table = self.main_div.xpath('p[' + str(self.index) + ']')

                    if len(self.info_table) == 0:
                        self.index = temp_index
                        self.info_table = temp_info_table
                        return False

                    self.info_table = self.info_table[0].getnext()
            else:
                if self.table_check:
                    self.index += 1
                    self.info_table = self.main_div.xpath('table[' + str(self.index) + ']')

                    if len(self.info_table) == 0:
                        self.index = 1
                        self.table_check = False
                        self.info_table = self.main_div.xpath('p[1]')[0].getnext()
                    else:
                        self.info_table = self.info_table[0]
                else:
                    self.info_table = self.info_table.getnext()

                    if self.info_table is None:
                        self.index += 1
                        self.info_table = self.main_div.xpath('p[' + str(self.index) + ']')

                        if len(self.info_table) == 0:
                            self.index = temp_index
                            self.table_check = temp_table_check
                            self.info_table = temp_info_table
                            return False

                        self.info_table = self.info_table[0].getnext()

    # I don't think this works in all scenarios but this shit is way too complicated to understand
    def next_table(self):
        if self.lookup_num < 722:
            self.info_table = self.info_table.getnext()
        else:
            self.index += 1
            self.info_table = self.main_div.xpath('table[' + str(self.index) + ']')[0]

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

    def check_header(self, header, header_index):
        if self.info_table.tag == 'table':
            text = self.check_queries('tr[1]/td/b', 'tr[1]/td', 'thead/tr[1]/td')
            if text is not None and text[header_index:] == header:
                return True
        return False

    def backup(self):
        self.backup_table = self.info_table
        self.backup_index = self.index

    def restore_backup(self):
        self.info_table = self.backup_table
        self.index = self.backup_index
