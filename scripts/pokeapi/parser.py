from enum import Enum
from typing import List, Dict, Tuple

import math
import pokebase

from scripts.util import namesies, remove_suffix, decimeters_to_inches, hectograms_to_lbs, replace_new_lines, \
    remove_prefix

INDIVIDUAL_VERSIONS = ['ultra-sun', 'ultra-moon',
                       'sun', 'moon',
                       'alpha-sapphire', 'omega-ruby',
                       'x', 'y',
                       'black-2', 'white-2',
                       'black', 'white']

PAIRED_VERSIONS = ['ultra-sun-ultra-moon',
                   'sun-moon',
                   'omega-ruby-alpha-sapphire',
                   'x-y',
                   'black-2-white-2',
                   'black-white']


# TODO: This is temporarily duplicated and I also don't want it in this file
class Stat(Enum):
    HP = 0
    ATTACK = 1
    DEFENSE = 2
    SP_ATTACK = 3
    SP_DEFENSE = 4
    SPEED = 5


class Move:
    def __init__(self, move_entry, version_entry: Dict):
        self.name = move_entry.move.name
        self.learn_method = version_entry['move_learn_method']['name']
        # self.level_learned = None
        if self.learn_method == 'level-up':
            self.level_learned = version_entry['level_learned_at']

    def __str__(self):
        return self.name

class Parser:
    def __init__(self, num: int):
        self.num = num

        self.pokemon = pokebase.pokemon(num)
        self.species = self.pokemon.species

    # Returns the English name from the list of the species' names
    # Ex: 'Bulbasaur'
    def get_name(self) -> str:
        # If the language is English ('en'), return the entry's name
        # Ex: {'language': {'name': 'en', 'url': 'https://pokeapi.co/api/v2/language/9/'}, 'name': 'Bulbasaur'}
        return next(entry.name for entry in self.species.names if entry.language.name == 'en')

    # Returns the base stats and given effort values in a tuple
    # Ex: ([45, 49, 49, 65, 65, 45], [0, 0, 0, 1, 0, 0])
    def get_stats_evs(self) -> Tuple[List[int], List[int]]:
        stats = [0]*6
        evs = [0]*6
        for stat in self.pokemon.stats:
            stat_index = _get_stat(stat.stat.name).value
            stats[stat_index] = stat.base_stat
            evs[stat_index] = stat.effort
        return stats, evs

    # Returns the base experience
    # Ex: 64
    def get_base_experience(self) -> int:
        return self.pokemon.base_experience

    # Returns the growth rate as a namesies string
    # Ex: 'MEDIUM_SLOW'
    def get_growth_rate(self) -> str:
        growth_rate = namesies(self.species.growth_rate.name)
        if growth_rate == 'MEDIUM':
            growth_rate = 'MEDIUM_FAST'
        elif growth_rate == 'FAST_THEN_VERY_SLOW':
            growth_rate = 'FLUCTUATING'
        elif growth_rate == 'SLOW_THEN_VERY_FAST':
            growth_rate = 'ERRATIC'
        return growth_rate

    # Returns the type of the Pokemon as a size 2 list of namesies strings
    # Ex: ['GRASS', 'POISON']
    def get_types(self) -> List[str]:
        types = ['', 'NO_TYPE']
        for poke_type in self.pokemon.types:
            types[poke_type.slot - 1] = namesies(poke_type.type.name)
        assert len(types) == 2
        assert types[0] != ''
        return types

    # Returns the capture rate
    # Ex: 45
    def get_capture_rate(self) -> int:
        return self.species.capture_rate

    # Returns the percentage that this Pokemon is male (-1 for genderless)
    # Ex: 87
    def get_gender_ratio(self) -> int:
        # TODO: I like the gender in eighths and should convert back to this once everything is settled
        # gender_rate: the chance of this Pokémon being female, in eighths; or -1 for genderless
        male_ratio = self.species.gender_rate
        if male_ratio != -1:
            male_ratio = 100 - 100*male_ratio/8
            if male_ratio > 50:
                male_ratio = math.floor(male_ratio)
            else:
                male_ratio = math.ceil(male_ratio)
            male_ratio = int(male_ratio)
        return male_ratio

    # Returns the abilities as namesies strings in a list of size 2
    # If only has one regular ability, use the hidden ability as second
    # Ex: ['OVERGROW', 'CHLOROPHYLL']
    def get_abilities(self) -> List[str]:
        abilities = ['']*3

        # Add each ability in the corresponding slot in the list
        for ability in self.pokemon.abilities:
            abilities[ability.slot - 1] = namesies(ability.ability.name)

            # Hidden abilities need to be the third slot
            if ability.is_hidden:
                assert ability.slot == 3

        # Remove empty slots
        abilities = [ability for ability in abilities if ability != ''][:2]

        # Only has a single ability -- give empty ability as second
        if len(abilities) == 1:
            abilities.append('NO_ABILITY')

        assert len(abilities) == 2
        return abilities

    # Returns the classification (genus)
    # Ex: 'Seed'
    def get_classification(self) -> str:
        # If the language is English ('en'), return the entry's genus
        # Ex: {'genus': 'Seed Pokémon', 'language': {'name': 'en', 'url': 'https://pokeapi.co/api/v2/language/9/'}}
        classification = next(entry.genus for entry in self.species.genera if entry.language.name == 'en')

        # Remove the Pokemon text from the end of classification
        classification = remove_suffix(classification, ' Pokémon')
        return classification

    # Returns the height in inches
    # Ex: 28
    def get_height(self) -> int:
        # Input height is specified in decimeters
        return decimeters_to_inches(self.pokemon.height)

    # Returns the weight in lbs rounded to the first decimal place
    # Ex: 15.2
    def get_weight(self) -> float:
        # Input weight is specified in hectograms
        return hectograms_to_lbs(self.pokemon.weight)

    # Returns the English flavor text entry from the list of multiple language entries
    # Ex: 'Bulbasaur can be seen napping in bright sunlight. There is a seed on its back. By soaking up the sun’s rays,
    #      the seed grows progressively larger.'
    def get_flavor_text(self) -> str:
        # Get all English versions mapped from version name to entry
        # Ex: {'flavor_text': 'Bulbasaur can be seen napping in bright sunlight.\n
        #                      There is a seed on its back. By soaking up the sun’s rays,\n
        #                      the seed grows progressively larger.',
        # 'language': {'name': 'en', 'url': 'https://pokeapi.co/api/v2/language/9/'},
        # 'version': {'name': 'alpha-sapphire', 'url': 'https://pokeapi.co/api/v2/version/26/'}}
        version_to_flavor_text = { entry.version.name: entry
                                   for entry in self.species.flavor_text_entries
                                   if entry.language.name == 'en' }  # type: Dict[str,]

        # Get the flavor text of the most relevant version in English
        flavor_text = next(version_to_flavor_text[version].flavor_text
                           for version in INDIVIDUAL_VERSIONS
                           if version in version_to_flavor_text)

        # TODO: Probably also need to remove special characters and shit
        flavor_text = replace_new_lines(flavor_text)
        return flavor_text

    # Returns the base egg steps (not hatch counter)
    # Ex: 5120
    def get_egg_steps(self) -> int:
        # Note: I use egg steps slightly differently than the main game because the main game hatches eggs really
        # weird with these hatch counter things and I just count down directly and don't care :/
        return self.species.hatch_counter * 256

    # Returns the egg groups as namesies strings in a size 2 list
    # If there is only one group, the second group will be 'NONE'
    # Ex: ['PLANT', 'MONSTER'] TODO: Make sure to update this example once I know which format I'm using
    def get_egg_groups(self) -> List[str]:
        # TODO: Some of these names are different from the serebii ones and I need to handle that
        egg_groups = [namesies(_get_egg_group(egg_group.name)) for egg_group in self.species.egg_groups]
        if len(egg_groups) == 1:
            egg_groups.append('NONE')
        assert len(egg_groups) == 2
        return egg_groups

    # Returns the moves version, level-up moves, and the learnable moves as a tuple
    # Level-up moves will be a list of strings of the format '<int:level> <string:attackName>' (Ex: '7 LEECH_SEED')
    #   and will be sorted by level order
    # Learnable moves (egg moves, move tutors, tms) will be a list of strings of namesies attack names
    # Ex: ('ultra-sun-ultra-moon',
    #      ['0 TACKLE', '3 GROWL', '7 LEECH_SEED', '9 VINE_WHIP', ...., '37 SEED_BOMB'],
    #      ['SWORDS_DANCE', 'SOLAR_BEAM', 'PETAL_DANCE', 'TOXIC', 'DOUBLE_TEAM', ...])
    def get_moves(self) -> Tuple[str, List[str], List[str]]:
        # Maps from version name to list of Moves in that version
        version_to_moves = {}  # type: Dict[str, List[Move]]
        for entry in self.pokemon.moves:
            for version_entry in entry.version_group_details:
                version_name = version_entry['version_group']['name']
                version_to_moves.setdefault(version_name, []).append(Move(entry, version_entry))

        # Get the moves of the most relevant version
        version = next(version for version in PAIRED_VERSIONS if version in version_to_moves)
        moves = version_to_moves[version]

        # Create move lists from version's moves
        level_up_moves = []   # type: List[str]
        learnable_moves = []  # type: List[str]
        for move in moves:
            attack_name = namesies(move.name)

            # Level-up moves
            if move.learn_method == 'level-up':
                # 0 -> -1 is for learning on evolution
                if move.level_learned == 0:
                    move.level_learned = -1
                # 1 -> 0 is for default moves
                elif move.level_learned == 1:
                    move.level_learned = 0

                level_up_moves.append(str(move.level_learned) + " " + attack_name)
            # All other learnable moves
            # Did you know that if you bred Pikachu holding Light Ball the Pichu will know Volt Tackle??
            # Form change is for Rotom
            elif move.learn_method in ['machine', 'egg', 'tutor', 'light-ball-egg', 'form-change']:
                learnable_moves.append(attack_name)
            else:
                raise Exception('Unknown move learn method ' + move.learn_method + ' for ' + attack_name)

        # Remove duplicates for evolution moves and default moves (only keep evolution move)
        evolution_moves = [remove_prefix(move, "-1 ") for move in level_up_moves if move.startswith("-1 ")]
        for move_name in evolution_moves:
            level_up_moves.remove("0 " + move_name)

        # Level-up moves are sorted by level
        level_up_moves.sort(key = _attack_level_sort)

        return version, level_up_moves, learnable_moves


# Returns the Stat that matches the input name
def _get_stat(stat_name: str) -> Stat:
    if stat_name == 'hp':
        return Stat.HP
    elif stat_name == 'attack':
        return Stat.ATTACK
    elif stat_name == 'defense':
        return Stat.DEFENSE
    elif stat_name == 'special-attack':
        return Stat.SP_ATTACK
    elif stat_name == 'special-defense':
        return Stat.SP_DEFENSE
    elif stat_name == 'speed':
        return Stat.SPEED
    else:
        raise Exception('Unknown stat name ' + stat_name)


# Replaces the egg group with alternative names if applicable
def _get_egg_group(egg_group: str) -> str:
    if egg_group == 'plant':
        return 'grass'
    elif egg_group == 'ground':
        return 'field'
    elif egg_group == 'humanshape':
        return 'human-like'
    elif egg_group == 'indeterminate':
        return 'amorphous'
    elif egg_group == 'no-eggs':
        return 'undiscovered'
    elif egg_group == 'water1':
        return 'water-1'
    elif egg_group == 'water2':
        return 'water-2'
    elif egg_group == 'water3':
        return 'water-3'
    else:
        return egg_group


# Used for sorting the level up moves by level
# Attack should be a string with format '<int:level> <string:attackName>' (Ex: '7 LEECH_SEED')
def _attack_level_sort(attack: str) -> int:
    split = attack.split(' ')
    assert len(split) == 2
    return int(split[0])
