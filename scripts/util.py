#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import re

poke = u'é'
right_tick = u'\u2019'
dashy = u'\u2014'
left_quote = u'\u201c'
right_quote = u'\u201d'


def namesies(stringsies):
    return stringsies.strip().replace(' ', '_').replace('-', '_').replace('\'', '').upper()


def remove_prefix(string, prefix):
    assert string.startswith(prefix)
    return string[len(prefix):]


# Listsies should be a list of strings
# This will remove all empty and whitespace characters from the list and return it
def remove_empty(listsies):
    temp = []
    for string in listsies:
        if string.strip() == "":
            temp.append(string)
    for empty in temp:
        listsies.remove(empty)


def index_swap(arr, i, j):
    temp = arr[i]
    arr[i] = arr[j]
    arr[j] = temp


# I don't know why this works for category as well as type but it does
def get_image_name(image_element):
    image_name = image_element.attrib["src"]
    return image_name[image_name.find("type") + 5: -4].capitalize()


# types should be an array that points to img elements
def get_types(type_images):
    assert len(type_images) == 1 or len(type_images) == 2

    types = ["No_Type"] * 2
    for i, type_image in enumerate(type_images):
        # imageName is of the form "...type/<typeName>.gif"
        types[i] = get_image_name(type_image)

    return types


# Removes the form/forme suffix from the end
def normalize_form(form):
    return re.sub(" Forme?$", "", form).strip()


def replace_special(s):
    s = s.replace(poke, "\u00e9")
    s = s.replace('  ', ' ')
    s = s.replace(right_tick, "'")
    s = s.replace(dashy, "--")
    s = s.replace(left_quote, "\"")
    s = s.replace(right_quote, "\"")
    return s


# Column indices should be specified as 1-indexed
def add_row_values(main_table, row_index, values, *column_indices):
    row = main_table[row_index]
    for column_index in column_indices:
        value = row.xpath('td')[column_index - 1].text.strip()
        add_value(values, value)


def add_value(values, value):
    value = value.strip()
    value = replace_special(value)
    values.append(value)
    print(value)
    return value


def get_element_text(element):
    text = element.text
    if text is not None:
        return text
    return element.text_content()


def get_query_text(query):
    for query_child in query:
        text = get_element_text(query_child)
        if text is not None:
            return text
        for child in query_child.getchildren():
            text = get_element_text(child)
            if text is not None:
                return text


def check_queries(table, *queries):
    for query_string in queries:
        query = table.xpath(query_string)
        text = get_query_text(query)
        if text is not None:
            return text


def check_header(table, header):
    if table.tag == 'table':
        text = check_queries(table, 'tr[1]/td/b', 'tr[1]/td', 'thead/tr[1]/td')
        if text is not None and text == header:
            return True
    return False
