import urllib

from scripts.serebii.form_config import AddedPokes, FormConfig

for num in range(1, list(AddedPokes)[-1].value + 1):
    form_config = FormConfig(num)
    print("#" + str(num).zfill(3))

    # Party tiles
    source = "http://www.serebii.net/pokedex-sm/icon/" + form_config.form_image_name + ".png"
    dest = "../rec/images/tiles/partyTiles/" + str(num).zfill(3) + "-small.png"
    urllib.request.urlretrieve(source, dest)

    # Pokedex tiles
    source = "https://www.serebii.net/art/th/" + form_config.pokedex_image_name + ".png"
    dest = "../rec/images/tiles/pokedexTiles/" + str(num).zfill(3) + ".png"
    urllib.request.urlretrieve(source, dest)
