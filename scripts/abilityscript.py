#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import requests
from lxml import html

from util import add_row_values

f = open("abilities.in", "r")
out = open("abilities.out", "w")
for i, abilityName in enumerate(f):
    abilityName = abilityName.strip()
    values = [abilityName]
    print(i, abilityName)
    
    lookupName = abilityName.lower().replace(" ", "")
    
    page = requests.get('http://www.serebii.net/abilitydex/' + lookupName + '.shtml')
    tree = html.fromstring(page.text)
    mainTable = tree.xpath('/html/body/table[2]/tr[2]/td[2]/font/p[2]')[0].getnext()

    add_row_values(mainTable, 3, values, 1)
    
    for value in values:
        out.write(value + '\n')
f.close()
out.close()