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
    dest = "../rec/images/temp/" + split[1]
    
    userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36"
    request = urllib.request.Request(source, data=None, headers={'User-Agent':userAgent})
    imageFile = urllib.request.urlopen(request)
    
    fh = open(dest, 'wb')
    fh.write(imageFile.read()) 
    fh.write(imageFile.read()) # Do this twice because shit is fucking weird as shit I hate everything and sometimes still doesn't fuckity python fuck you
