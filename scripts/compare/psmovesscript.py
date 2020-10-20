#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import requests

url = 'https://raw.githubusercontent.com/smogon/pokemon-showdown/master/data/moves.ts'

with open("ps-moves.txt", "w") as f:
    file = requests.get(url).text
    
    begin_string = 'export const Moves: {[moveid: string]: MoveData} = {'
    start_index = file.find(begin_string)
    end_index = file.rfind('}')

    if start_index < 0 or end_index < 0:
        print('could not find substring ' + begin_string)
    else:
        f.write(file[start_index + len(begin_string) -1 : end_index + 1])
