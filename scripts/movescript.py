#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import requests
from lxml import html

from util import get_image_name, add_row_values

f = open("moves.in", "r")
out = open("moves.out", "w")
for i, attack_name in enumerate(f):
    attack_name = attack_name.strip()
    values = [attack_name]
    print(i, attack_name)

    lookup_name = attack_name.lower().replace(" ", "")
    if lookup_name == "judgement":
        lookup_name = "judgment"

    page = requests.get('http://www.serebii.net/attackdex-sm/' + lookup_name + '.shtml')
    tree = html.fromstring(page.text)
    main_table = tree.xpath('/html/body/table[2]/tr[2]/td[2]/font/div[3]/font/p')[-1].getnext()

    row_index = 1

    # Name, Type, Category
    row = main_table[row_index]
    attack_type = get_image_name(row.xpath('td[2]/a/img')[0])
    category = get_image_name(row.xpath('td[3]/a/img')[0])
    if category == "Other":
        category = "Status"
    values.append(attack_type)
    values.append(category)
    print(attack_type, category)

    # PP, Base Power, Accuracy
    row_index += 2
    add_row_values(main_table, row_index, values, 1, 2, 3)

    # Description
    row_index += 2
    add_row_values(main_table, row_index, values, 1)

    if main_table[row_index + 1].text_content().strip() == "In-Depth Effect:":
        row_index += 2

    # Secondary Effect and Effect Chance
    row_index += 2
    add_row_values(main_table, row_index, values, 2)

    # Z-Move things
    row_index += 2

    # Crit Rate, Speed Priority, Target
    row_index += 2
    add_row_values(main_table, row_index, values, 1, 2)

    # I don't know I guess it's a new fucking table now?
    main_table = main_table.getnext()
    row_index = 1

    # Physical Contact, Sound-Type, Punch Move, Snatchable, Z-Move
    add_row_values(main_table, row_index, values, 1, 2, 3, 4)

    # Defrost, Triple Battle, Magic Bounce, Protected, Mirror Move
    row_index += 2
    add_row_values(main_table, row_index, values, 1, 3, 4, 5)

    for value in values:
        value = value.replace('(SMUSUM)', '').strip()
        out.write(value + '\n')
f.close()
out.close()
