import urllib

for num in range(1, 802):
    source = "http://www.serebii.net/art/th/" + str(num) + ".png"
    dest = "C:\\Users\\leahf_000\\Documents\\IdeaProjects\\Pokemon++\\rec\\images\\tiles\\pokedexTiles\\" + str(num).zfill(3) + ".png"
    urllib.urlretrieve(source, dest)
