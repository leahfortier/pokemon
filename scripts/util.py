#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import re

poke = u'é'
rightTick = u'\u2019'
dashy = u'\u2014'
leftQuote = u'\u201c'
rightQuote = u'\u201d'

def namesies(stringsies):
    stringsies = stringsies.strip().replace(' ', '_').replace('-', '_').replace('\'', '').upper()
    if stringsies == 'CONVERSION_2':
        stringsies = 'CONVERSION2'
    elif stringsies == 'RKS_SYSTEM':
        stringsies = 'RKSSYSTEM'
    return stringsies

def removePrefix(string, prefix):
    assert string.startswith(prefix)
    return string[len(prefix):]

# Listsies should be a list of strings 
# This will remove all empty and whitespace characters from the list and return it
def removeEmpty(listsies):
    temp = []
    for string in listsies:
        if string.strip() == "":
            temp.append(string)
    for empty in temp:
        listsies.remove(empty)
    
def indexSwap(arr, i, j):
    temp = arr[i]
    arr[i] = arr[j]
    arr[j] = temp
    
# I don't know why this works for category as well as type but it does
def getImageName(imageElement):
    imageName = imageElement.attrib["src"]
    return imageName[imageName.find("type") + 5 : -4].capitalize()

# types should be an array that points to img elements
def getTypes(typeImages):
    assert len(typeImages) == 1 or len(typeImages) == 2
    
    types = ["No_Type"]*2
    for i, typeImage in enumerate(typeImages):
        # imageName is of the form "...type/<typeName>.gif"
        types[i] = getImageName(typeImage)
        
    return types
    
# Removes the form/forme suffix from the end
def normalizeForm(form):
    return re.sub(" Forme?$", "", form).strip()

# Column indices should be specified as 1-indexed
def addRowValues(mainTable, rowIndex, values, *columnIndices):
    row = mainTable[rowIndex]
    for columnIndex in columnIndices:
        value = row.xpath('td')[columnIndex - 1].text.strip()
        value = replaceSpecial(value)
        values.append(value)
        print(value)
        
def replaceSpecial(s):
    return s.replace(poke, "\u00e9").replace('  ', ' ').replace(rightTick, "'").replace(dashy, "--").replace(leftQuote, "\"").replace(rightQuote, "\"")