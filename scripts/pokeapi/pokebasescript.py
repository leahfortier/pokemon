import requests

from scripts.forms import AddedPokes
from scripts.pokeapi.pokemoninfo import PokemonInfo
from scripts.util import Timer

with open("../../temp.txt", "w") as f:
    timer = Timer()

    for num in range(1, list(AddedPokes)[-1].value + 1):
    # for num in [1]:
        pokemon = PokemonInfo(num)
        pokemon.write(f)

    timer.print()

