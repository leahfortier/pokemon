#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import urllib

f = open("ps-images.in", "r")
for i, line in enumerate(f):
    if line.strip() == "":
        continue

    print(line)    
    split = line.split()
    
    source = "https://play.pokemonshowdown.com/sprites/" + split[0]
    dest = "../../Downloads/sunmoonsprites/" + split[1]
    
    user_agent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36"
    request = urllib.request.Request(source, data=None, headers={'User-Agent':user_agent})
    response = urllib.request.urlopen(request)
    
    image_file = open(dest, 'wb')
    image_file.write(response.read())
    image_file.close()
