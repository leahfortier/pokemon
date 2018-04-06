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
for i, item_name in enumerate(f):
    item_name = item_name.strip()
    values = [item_name]
    print(i, item_name)
    
    lookup_name = item_name.lower().replace(" ", "").replace("'", "").replace(poke, 'e')
    
    page = requests.get('http://www.serebii.net/itemdex/' + lookup_name + '.shtml')
    tree = html.fromstring(page.text)
    main_table = tree.xpath('/html/body/table[2]/tr[2]/td[2]/font/p[1]')[0].getnext().getnext()
    
    # Sprites, Item Type, Japanese Name, Fling Damage, Price
    row = main_table[1]
    
    # Item Type
    item_type = add_value(values, row[1][0].text)
    
    fling = '0'
    price = '0'
    if item_type != 'Key Item':
        # I don't know some are missing this and it's fucking annoying
        if len(row) == 5:
            # Fling Damage
            fling = row[-2].text
            
        # Price (last column)
        price_element = row[-1].xpath('table/tr/td[2]')[0]
        if price_element.text is not None:
            price = price_element.text
    add_value(values, fling)
    add_value(values, price)
    
    # <br>, <br>, Attainable In
    main_table = main_table.getnext().getnext().getnext()
    
    # <br>, <br>, In-Depth Effect (or Berry Table info things)
    main_table = main_table.getnext().getnext().getnext()
    
    ng_type = 'No_Type'
    ng_pow = '0'
    if item_type == 'Berry':
        # Natural Gift Type, Natural Gift Power, Colour
        row = main_table[1]
        
        ng_type = get_image_name(row[0].xpath('a/img')[0])
        ng_pow = row[1].text
    add_value(values, ng_type)
    add_value(values, ng_pow)
    
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
