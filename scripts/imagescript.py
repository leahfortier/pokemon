import urllib

for num in range(803, 808):
    print("#" + str(num).zfill(3))
    
    # Party tiles
    source = "http://www.serebii.net/pokedex-sm/icon/" + str(num) + ".png"
    dest = "../rec/images/tiles/partyTiles/" + str(num).zfill(3) + "-small.png"
    urllib.request.urlretrieve(source, dest)
    
    # Pokedex tiles
    source = "https://www.serebii.net/art/th/" + str(num) + ".png"
    dest = "../rec/images/tiles/pokedexTiles/" + str(num).zfill(3) + ".png"
    urllib.request.urlretrieve(source, dest)
