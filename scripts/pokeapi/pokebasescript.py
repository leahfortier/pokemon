from scripts.forms import AddedPokes
from scripts.pokeapi.pokemoninfo import PokemonInfo
from scripts.util import Timer

if __name__ == '__main__':
    timer = Timer()
    f = open("../../temp.txt", "w")

    for num in range(1, list(AddedPokes)[-1].value + 1):
    # for num in [1]:
        pokemon = PokemonInfo(num)
        pokemon.write(f)

    f.close()
    timer.print()

