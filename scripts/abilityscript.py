#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import requests
from lxml import html

from util import add_row_values

f = open("abilities.in", "r")
out = open("abilities.out", "w")
for i, ability_name in enumerate(f):
    ability_name = ability_name.strip()
    values = [ability_name]
    print(i, ability_name)
    
    lookup_name = ability_name.lower().replace(" ", "")
    
    page = requests.get('http://www.serebii.net/abilitydex/' + lookup_name + '.shtml')
    tree = html.fromstring(page.text)
    main_table = tree.xpath('/html/body/table[2]/tr[2]/td[2]/font/p[2]')[0].getnext()

    add_row_values(main_table, 3, values, 1)
    
    for value in values:
        out.write(value + '\n')
f.close()
out.close()