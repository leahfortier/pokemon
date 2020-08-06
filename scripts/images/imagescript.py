import requests

from scripts.serebii.form_config import AddedPokes
from scripts.serebiiswsh.form_config import FormConfig


def write_image(source: str, dest: str):
    r = requests.get(source)
    open(dest, 'wb').write(r.content)


for num in range(1, list(AddedPokes)[-1].value + 1):
    form_config = FormConfig(num)
    print("#" + str(num).zfill(3))

    # Party tiles
    source = "https://serebii.net/pokedex-swsh/icon/" + form_config.form_image_name + ".png"
    dest = "../../rec/images/tiles/partyTiles/" + str(num).zfill(3) + "-small.png"
    write_image(source, dest)

    # Pokedex tiles
    source = "https://www.serebii.net/art/th/" + form_config.pokedex_image_name + ".png"
    dest = "../../rec/images/tiles/pokedexTiles/" + str(num).zfill(3) + ".png"
    write_image(source, dest)
