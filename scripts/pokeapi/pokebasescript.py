import requests

from scripts.forms import AddedPokes
from scripts.pokeapi.pokemoninfo import PokemonInfo
from scripts.util import Timer

with open("../../temp.txt", "w") as f:
    timer = Timer()

    for num in range(1, list(AddedPokes)[-1].value + 1):
    # for num in [1]:
        try:
            pokemon = PokemonInfo(num)
            pokemon.write(f)
        except requests.exceptions.HTTPError as error:
            f.write("#" + str(num).zfill(3) + " ERROR: " + str(error) + "\n\n")

    timer.print()

