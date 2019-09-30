from typing import List, IO

import math

import pokebase

from scripts.forms import FormConfig, get_stat, get_english, get_english_version, attack_sort, get_moves
from scripts.util import namesies, remove_suffix, decimeters_to_inches, hectograms_to_lbs, remove_new_lines


class PokemonInfo:
    def __init__(self, num: int):
        self.num = num
        form_config = FormConfig(num)

        pokemon = pokebase.pokemon(form_config.lookup_num)
        species = pokemon.species

        # Get the English name
        self.name = get_english(species.names).name
        print("#" + str(num).zfill(3), self.name)

        self.stats = [0]*6
        self.evs = [0]*6
        for stat in pokemon.stats:
            stat_index = get_stat(stat.stat.name).value
            self.stats[stat_index] = stat.base_stat
            self.evs[stat_index] = stat.effort
        print("Stats:", self.stats)
        print("Effort Values:", self.evs)

        self.base_exp = pokemon.base_experience
        print("Base EXP:", self.base_exp)

        self.growth_rate = namesies(species.growth_rate.name)
        print("Growth Rate:", self.growth_rate)

        self.types = ['', 'NO_TYPE']
        for poke_type in pokemon.types:
            self.types[poke_type.slot - 1] = namesies(poke_type.type.name)
        print("Type:", self.types)

        self.capture_rate = species.capture_rate
        print("Capture Rate:", self.capture_rate)

        # TODO: I like the gender in eighths and should convert back to this once everything is settled
        # gender_rate: the chance of this Pokémon being female, in eighths; or -1 for genderless
        self.male_ratio = species.gender_rate
        if self.male_ratio != -1:
            self.male_ratio = 100 - 100*self.male_ratio/8
            if self.male_ratio > 50:
                self.male_ratio = math.floor(self.male_ratio)
            else:
                self.male_ratio = math.ceil(self.male_ratio)
            self.male_ratio = int(self.male_ratio)
        print("Male Ratio:", self.male_ratio)

        # If only has one regular ability, use the hidden ability as second (will be in third slot)
        self.abilities = ['']*3
        for ability in pokemon.abilities:
            self.abilities[ability.slot - 1] = namesies(ability.ability.name)
            if ability.is_hidden:
                assert ability.slot == 3
        self.abilities = [ability for ability in self.abilities if ability != ''][:2]
        if len(self.abilities) == 1:
            self.abilities.append('NO_ABILITY')
        assert len(self.abilities) == 2
        print("Abilities:", self.abilities)

        # Remove the Pokemon text from the end of classification
        self.classification = remove_suffix(get_english(species.genera).genus, ' Pokémon')
        print("Classification:", self.classification)

        self.height = decimeters_to_inches(pokemon.height)
        print("Height:", self.height)

        self.weight = hectograms_to_lbs(pokemon.weight)
        print("Weight:", self.weight)

        # TODO: Probably need to remove special characters and shit
        self.flavor_text = remove_new_lines(get_english_version(species.flavor_text_entries).flavor_text)
        print("Flavor Text:", self.flavor_text)

        # Note: I use egg steps slightly differently than the main game because the main game hatches eggs really
        # weird with these hatch counter things and I just count down directly
        self.egg_steps = species.hatch_counter * 256
        print("Egg Steps:", self.egg_steps)

        # TODO: Some of these names are different from the serebii ones and I need to handle that
        self.egg_groups = [namesies(egg_group.name) for egg_group in species.egg_groups]
        if len(self.egg_groups) == 1:
            self.egg_groups.append('NONE')
        assert len(self.egg_groups) == 2
        print("Egg Groups:", self.egg_groups)

        self.level_up_moves = []   # type: List[str]
        self.learnable_moves = []  # type: List[str]
        moves = get_moves(pokemon.moves)
        for move in moves:
            if move.learn_method == 'level-up':
                # 0 -> -1 is for learning on evolution
                if move.level_learned == 0:
                    move.level_learned = -1
                # 1 -> 0 is for default moves
                elif move.level_learned == 1:
                    move.level_learned = 0

                self.level_up_moves.append(str(move.level_learned) + " " + namesies(move.name))
            # Did you know that if you bred Pikachu holding Light Ball the Pichu will know Volt Tackle??
            # Form change is for Rotom
            elif move.learn_method in ['machine', 'egg', 'tutor', 'light-ball-egg', 'form-change']:
                self.learnable_moves.append(namesies(move.name))
            else:
                raise Exception('Unknown move learn method ' + move.learn_method + ' for ' + move.name)

        self.level_up_moves.sort(key = attack_sort)
        print("Level-up Moves:", self.level_up_moves)
        print("Learnable Moves:", self.learnable_moves)

        print()

    # Writes all pokemon info to f in the relevant order
    def write(self, f: IO) -> None:
        f.write(str(self.num) + '\n')
        f.write(str(self.name) + '\n')

        stats = [str(stat) for stat in self.stats]
        f.write(' '.join(stats) + '\n')

        f.write(str(self.base_exp) + '\n')
        f.write(self.growth_rate + '\n')
        f.write(self.types[0] + ' ')
        f.write(self.types[1] + '\n')

        f.write(str(self.capture_rate) + '\n')

        evs = [str(ev) for ev in self.evs]
        f.write(' '.join(evs) + '\n')

        # TODO: Evolutions
        f.write('NONE\n')

        # TODO: Wild Hold Items
        f.write('0\n')

        f.write(str(self.male_ratio) + '\n')
        f.write(self.abilities[0] + ' ')
        f.write(self.abilities[1] + '\n')
        f.write(str(self.classification) + '\n')
        f.write(str(self.height) + ' ')
        f.write(str(self.weight) + ' ')
        f.write(str(self.flavor_text) + '\n')
        f.write(str(self.egg_steps) + '\n')
        f.write(str(self.egg_groups[0]) + ' ')
        f.write(str(self.egg_groups[1]) + '\n')

        f.write(str(len(self.level_up_moves)) + '\n')
        for attack in self.level_up_moves:
            f.write(attack + '\n')

        f.write(str(len(self.learnable_moves)) + '\n')
        for attack in self.learnable_moves:
            f.write(namesies(attack) + '\n')

        f.write('\n')