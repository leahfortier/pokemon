#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import requests
from lxml import html

from util import poke, getImageName, checkHeader, addValue


def updateTable(table, header):
    while True:
        if table is None or checkHeader(table, header):
            return table
        
        table = table.getnext()

f = open("items.in", "r")
out = open("items.out", "w")
for i, itemName in enumerate(f):
    itemName = itemName.strip()
    values = [itemName]
    print(i, itemName)
    
    lookupName = itemName.lower().replace(" ", "").replace("'", "").replace(poke, 'e')
    
    page = requests.get('http://www.serebii.net/itemdex/' + lookupName + '.shtml')
    tree = html.fromstring(page.text)
    mainTable = tree.xpath('/html/body/table[2]/tr[2]/td[2]/font/p[1]')[0].getnext().getnext()
    
    # Sprites, Item Type, Japanese Name, Fling Damage, Price
    row = mainTable[1]
    
    # Item Type
    itemType = addValue(values, row[1][0].text)
    
    fling = '0'
    price = '0'
    if itemType != 'Key Item':
        # I don't know some are missing this and it's fucking annoying
        if len(row) == 5:
            # Fling Damage
            fling = row[-2].text
            
        # Price (last column)
        priceElement = row[-1].xpath('table/tr/td[2]')[0]
        if not priceElement.text is None:
            price = priceElement.text
    addValue(values, fling)
    addValue(values, price)
    
    # <br>, <br>, Attainable In
    mainTable = mainTable.getnext().getnext().getnext()
    
    # <br>, <br>, In-Depth Effect (or Berry Table info things)
    mainTable = mainTable.getnext().getnext().getnext()
    
    ngType = 'No_Type'
    ngPow = '0'
    if itemType == 'Berry':
        # Natural Gift Type, Natural Gift Power, Colour
        row = mainTable[1]
        
        ngType = getImageName(row[0].xpath('a/img')[0])
        ngPow = row[1].text
    addValue(values, ngType)
    addValue(values, ngPow)
    
    # <br>, Flavour Text
    mainTable = updateTable(mainTable, 'Flavour Text')
    
    # Last Flavor Text row should be the most updated one
    row = mainTable.xpath('tr')[-1]
    
    # Last column holds the flavor text (first ones have the game name)
    addValue(values, row[-1].text)
    
    for value in values:
        out.write(value + '\n')
f.close()
out.close()