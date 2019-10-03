#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from typing import List

import time

poke = u'é'
right_tick = u'\u2019'
dashy = u'\u2014'
left_quote = u'\u201c'
right_quote = u'\u201d'


def namesies(stringsies: str) -> str:
    return stringsies.strip().replace(' ', '_').replace('-', '_').replace('\'', '').upper()


def remove_prefix(string: str, prefix: str) -> str:
    assert string.startswith(prefix)
    return string[len(prefix):]


def remove_suffix(string: str, suffix: str) -> str:
    assert string.endswith(suffix)
    return string[:-len(suffix)]


# Rounded to the nearest integer inch
def decimeters_to_inches(decimeters: int) -> int:
    return round(decimeters/.254)


# Rounded to the first decimal lbs
def hectograms_to_lbs(hectograms: int) -> float:
    return round(hectograms*.22046, 1)


# Replaces new lines with spaces and trims the string
def replace_new_lines(s: str) -> str:
    return s.replace('\n', ' ').strip()


# Listsies should be a list of strings
# This will remove all empty and whitespace characters from the list
def remove_empty(listsies: List[str]) -> None:
    temp = [string for string in listsies if string.strip() == '']
    for empty in temp:
        listsies.remove(empty)


# Swaps the contents at indices i and j in arr
def index_swap(arr: List, i: int, j: int) -> None:
    temp = arr[i]
    arr[i] = arr[j]
    arr[j] = temp


def replace_special(s: str) -> str:
    s = s.replace(poke, "\u00e9")
    s = s.replace('  ', ' ')
    s = s.replace(right_tick, "'")
    s = s.replace(dashy, "--")
    s = s.replace(left_quote, "\"")
    s = s.replace(right_quote, "\"")
    return s


# Basic timer class that prints elapsed time
class Timer:
    def __init__(self):
        self.start_time = time.time()

    def print(self):
        end_time = time.time()
        total_seconds = int(end_time - self.start_time)
        minutes = total_seconds // 60
        seconds = total_seconds % 60
        print(minutes, "Minutes,", seconds, "Seconds")
