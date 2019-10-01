from typing import IO

from scripts.pokeapi.parser import Parser


class PokemonInfo:
    def __init__(self, num: int):
        parser = Parser(num)

        self.num = num
        self.name = parser.get_name()
        print("#" + str(num).zfill(3), self.name)

        self.stats, self.evs = parser.get_stats_evs()
        print("Stats:", self.stats)
        print("Effort Values:", self.evs)

        self.base_exp = parser.get_base_experience()
        print("Base EXP:", self.base_exp)

        self.growth_rate = parser.get_growth_rate()
        print("Growth Rate:", self.growth_rate)

        self.types = parser.get_types()
        print("Type:", self.types)

        self.capture_rate = parser.get_capture_rate()
        print("Capture Rate:", self.capture_rate)

        self.male_ratio = parser.get_gender_ratio()
        print("Male Ratio:", self.male_ratio)

        self.abilities = parser.get_abilities()
        print("Abilities:", self.abilities)

        self.classification = parser.get_classification()
        print("Classification:", self.classification)

        self.height = parser.get_height()
        print("Height:", self.height)

        self.weight = parser.get_weight()
        print("Weight:", self.weight)

        self.flavor_text = parser.get_flavor_text()
        print("Flavor Text:", self.flavor_text)

        self.egg_steps = parser.get_egg_steps()
        print("Egg Steps:", self.egg_steps)

        self.egg_groups = parser.get_egg_groups()
        print("Egg Groups:", self.egg_groups)

        self.level_up_moves, self.learnable_moves = parser.get_moves()
        print("Level-up Moves:", self.level_up_moves)
        print("Learnable Moves:", self.learnable_moves)

        print()

    # Writes all pokemon info to f in the relevant order
    def write(self, f: IO) -> None:
        f.write(str(self.num) + '\n')
        f.write(self.name + '\n')

        stats = [str(stat) for stat in self.stats]
        f.write(' '.join(stats) + '\n')

        f.write(str(self.base_exp) + '\n')
        f.write(self.growth_rate + '\n')
        f.write(' '.join(self.types) + '\n')

        f.write(str(self.capture_rate) + '\n')

        evs = [str(ev) for ev in self.evs]
        f.write(' '.join(evs) + '\n')

        # TODO: Evolutions
        f.write('NONE\n')

        # TODO: Wild Hold Items
        f.write('0\n')

        f.write(str(self.male_ratio) + '\n')
        f.write(' '.join(self.abilities) + '\n')
        f.write(str(self.classification) + '\n')
        f.write(str(self.height) + ' ')
        f.write(str(self.weight) + ' ')
        f.write(str(self.flavor_text) + '\n')
        f.write(str(self.egg_steps) + '\n')
        f.write(' '.join(self.egg_groups) + '\n')

        f.write(str(len(self.level_up_moves)) + '\n')
        for attack in self.level_up_moves:
            f.write(attack + '\n')

        f.write(str(len(self.learnable_moves)) + '\n')
        for attack in self.learnable_moves:
            f.write(attack + '\n')

        f.write('\n')