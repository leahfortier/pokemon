#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from lxml import html
import requests
from util import getImageName, addRowValues

f = open("moves.in", "r")
out = open("moves.out", "w")
for i, attackName in enumerate(f):
    attackName = attackName.strip()
    values = [attackName]
    print(i, attackName)
    
    lookupName = attackName.lower().replace(" ", "")
    if lookupName == "judgement":
        lookupName = "judgment"
    
    page = requests.get('http://www.serebii.net/attackdex-sm/' + lookupName + '.shtml')
    tree = html.fromstring(page.text)
    mainTable = tree.xpath('/html/body/table[2]/tr[2]/td[2]/font/div[3]/font/p')[-1].getnext()
    
    rowIndex = 1
    
    # Name, Type, Category
    row = mainTable[rowIndex]
    attackType = getImageName(row.xpath('td[2]/a/img')[0])
    category = getImageName(row.xpath('td[3]/a/img')[0])
    if category == "Other":
        category = "Status"
    values.append(attackType)
    values.append(category)
    print(attackType, category)
    
    # PP, Base Power, Accuracy
    rowIndex += 2
    addRowValues(mainTable, rowIndex, values, 1, 2, 3)
    
    # Description
    rowIndex += 2
    addRowValues(mainTable, rowIndex, values, 1)
    
    if mainTable[rowIndex + 1].text_content().strip() == "In-Depth Effect:":
        rowIndex += 2
    
    # Secondary Effect and Effect Chance
    rowIndex += 2
    addRowValues(mainTable, rowIndex, values, 2)
    
    # Z-Move things
    rowIndex += 2
    
    # Crit Rate, Speed Priority, Target
    rowIndex += 2
    addRowValues(mainTable, rowIndex, values, 1, 2)
    
    # I don't know I guess it's a new fucking table now?
    mainTable = mainTable.getnext()
    rowIndex = 1
    
    # Physical Contact, Sound-Type, Punch Move, Snatchable, Z-Move
    addRowValues(mainTable, rowIndex, values, 1, 2, 3, 4)
    
    # Defrost, Triple Battle, Magic Bounce, Protected, Mirror Move
    rowIndex += 2
    addRowValues(mainTable, rowIndex, values, 1, 3, 4, 5)
    
    for value in values:
        out.write(value + '\n')
f.close()
out.close()