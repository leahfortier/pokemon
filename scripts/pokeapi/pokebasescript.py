import requests
import time

from scripts.forms import AddedPokes
from scripts.pokeapi.pokemoninfo import PokemonInfo

with open("../../temp.txt", "w") as f:
    start_time = time.time()

    # for num in range(1, list(AddedPokes)[-1].value + 1):
    for num in range(1, 808):  # Currently breaks at Meltan
    # for num in [1]:
        try:
            pokemon = PokemonInfo(num)
            pokemon.write(f)
        except requests.exceptions.HTTPError as error:
            # For some reason this starting failing suddenly for #314 Illumise and #350 Milotic
            f.write("#" + str(num).zfill(3) + " ERROR: " + str(error) + "\n\n")

    end_time = time.time()
    total_seconds = int(end_time - start_time)
    minutes = total_seconds // 60
    seconds = total_seconds % 60
    print(minutes, "Minutes,", seconds, "Seconds")

