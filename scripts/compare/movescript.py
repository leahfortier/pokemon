#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import requests
from lxml import html

from scripts.serebii.parse_util import add_row_values, add_img_row_values
from scripts.util import Timer


class MoveParser:
    def __init__(self, attack_name: str):
        page = requests.get('https://serebii.net/attackdex-swsh/' + lookup_name + '.shtml')
        tree = html.fromstring(page.text)
        self.main_table = tree.xpath('/html/body/div[1]/div[2]/main/div[2]/table[2]')[0]

        # Table with alternate generations
        if 'Gen VIII Dex' in self.main_table.text_content():
            self.main_table = tree.xpath('/html/body/div[1]/div[2]/main/div[2]/table[3]')[0]

        self.rows = self.main_table.xpath('tr')
        self.row_index = 1

        self.values = [attack_name]

    def next_table(self):
        self.main_table = self.main_table.getnext()
        self.rows = self.main_table.xpath('tr')
        self.row_index = 1

    def add_img_row(self, *column_indices: int):
        add_img_row_values(self.rows, self.row_index, self.values, *column_indices)

    def add_next_row(self, *column_indices: int, advance = True):
        if advance:
            self.row_index += 2
        add_row_values(self.rows, self.row_index, self.values, *column_indices)

    def skip_row_title(self, title: str):
        if title in self.rows[self.row_index + 1].text_content().strip():
            self.row_index += 2


timer = Timer()

f = open("moves.in", "r")
out = open("moves.out", "w")
for i, attack_name in enumerate(f):
    attack_name = attack_name.strip()
    print(i, attack_name)

    lookup_name = attack_name.lower().replace(" ", "")
    if lookup_name == "judgement":
        lookup_name = "judgment"

    parser = MoveParser(attack_name)
    row_index = parser.row_index
    rows = parser.rows
    values = parser.values

    # Name, Type, Category
    parser.add_img_row(2, 3)
    if parser.values[-1] == "Other":
        parser.values[-1] = "Status"

    # PP, Base Power, Accuracy
    parser.add_next_row(1, 2, 3)

    # Description
    parser.add_next_row(1)

    # Only some moves have this row
    parser.skip_row_title("In-Depth Effect:")

    # Secondary Effect and Effect Rate
    parser.add_next_row(2)

    # Max Move things
    parser.skip_row_title("Corresponding Max Move")

    # Crit Rate, Speed Priority, Target
    parser.add_next_row(1, 2)

    # I don't know I guess it's a new fucking table now?
    parser.next_table()

    # Physical Contact, Sound-Type, Punch Move, Biting Move, Snatchable
    parser.add_next_row(1, 2, 3, 4, 5, advance=False)

    # Gravity, Defrost, Magic Bounce, Protected, Mirror Move
    parser.add_next_row(1, 2, 3, 4, 5)

    for value in values:
        out.write(value + '\n')

f.close()
out.close()

timer.print()
