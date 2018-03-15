import urllib

from forms import AddedPokes, FormConfig

for num in range(1, list(AddedPokes)[-1].value + 1):
    formConfig = FormConfig(num)
    print("#" + str(num).zfill(3))
    
    # Party tiles
    source = "http://www.serebii.net/pokedex-sm/icon/" + formConfig.formImageName + ".png"
    dest = "../rec/images/tiles/partyTiles/" + str(num).zfill(3) + "-small.png"
    urllib.request.urlretrieve(source, dest)
    
    # Pokedex tiles
    source = "https://www.serebii.net/art/th/" + formConfig.pokedexImageName + ".png"
    dest = "../rec/images/tiles/pokedexTiles/" + str(num).zfill(3) + ".png"
    urllib.request.urlretrieve(source, dest)
