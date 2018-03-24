#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import requests
from lxml import html

from util import poke, get_image_name, check_header, add_value


def update_table(table, header):
    while True:
        if table is None or check_header(table, header):
            break

        table = table.getnext()
    return table


f = open("items.in", "r")
out = open("items.out", "w")
for i, itemName in enumerate(f):
    itemName = itemName.strip()
    values = [itemName]
    print(i, itemName)
    
    lookupName = itemName.lower().replace(" ", "").replace("'", "").replace(poke, 'e')
    
    page = requests.get('http://www.serebii.net/itemdex/' + lookupName + '.shtml')
    tree = html.fromstring(page.text)
    main_table = tree.xpath('/html/body/table[2]/tr[2]/td[2]/font/p[1]')[0].getnext().getnext()
    
    # Sprites, Item Type, Japanese Name, Fling Damage, Price
    row = main_table[1]
    
    # Item Type
    itemType = add_value(values, row[1][0].text)
    
    fling = '0'
    price = '0'
    if itemType != 'Key Item':
        # I don't know some are missing this and it's fucking annoying
        if len(row) == 5:
            # Fling Damage
            fling = row[-2].text
            
        # Price (last column)
        priceElement = row[-1].xpath('table/tr/td[2]')[0]
        if priceElement.text is not None:
            price = priceElement.text
    add_value(values, fling)
    add_value(values, price)
    
    # <br>, <br>, Attainable In
    main_table = main_table.getnext().getnext().getnext()
    
    # <br>, <br>, In-Depth Effect (or Berry Table info things)
    main_table = main_table.getnext().getnext().getnext()
    
    ngType = 'No_Type'
    ngPow = '0'
    if itemType == 'Berry':
        # Natural Gift Type, Natural Gift Power, Colour
        row = main_table[1]
        
        ngType = get_image_name(row[0].xpath('a/img')[0])
        ngPow = row[1].text
    add_value(values, ngType)
    add_value(values, ngPow)
    
    # <br>, Flavour Text
    main_table = update_table(main_table, 'Flavour Text')
    
    # Last Flavor Text row should be the most updated one
    row = main_table.xpath('tr')[-1]
    
    # Last column holds the flavor text (first ones have the game name)
    add_value(values, row[-1].text)
    
    for value in values:
        out.write(value + '\n')
f.close()
out.close()
