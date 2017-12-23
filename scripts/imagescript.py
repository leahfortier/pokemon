import urllib

for num in range(720, 802):
    source = "http://www.serebii.net/pokedex-sm/icon/" + str(num) + ".png"
    dest = "C:\\Users\\leahf_000\\Documents\\IdeaProjects\\Pokemon++\\rec\\images\\tiles\\partyTiles\\" + str(num).zfill(3) + "-small.png"
    urllib.urlretrieve(source, dest)
