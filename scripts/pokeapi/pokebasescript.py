import time

from scripts.pokeapi.pokemoninfo import PokemonInfo

with open("../../temp.txt", "w") as f:
    start_time = time.time()

    # for num in range(1, list(AddedPokes)[-1].value + 1):
    # for num in range(1, 808): # Currently breaks at Meltan
    for num in [1]:
        pokemon = PokemonInfo(num)
        pokemon.write(f)

    end_time = time.time()
    total_seconds = int(end_time - start_time)
    minutes = total_seconds // 60
    seconds = total_seconds % 60
    print(minutes, "Minutes,", seconds, "Seconds")

