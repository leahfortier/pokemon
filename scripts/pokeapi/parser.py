from typing import List, Dict, Tuple

import pokebase

from scripts.forms import Stat
from scripts.pokeapi.form_config import FormConfig
from scripts.move import Move, LevelUpMoves
from scripts.substitution import attack_substitution, learnable_attack_substitution, learnable_attack_additions, \
    ability_substitution, type_substitution, name_substitution, gender_substitution, stat_substitution, \
    effort_substitution, capture_rate_substitution, level_up_attack_additions
from scripts.util import namesies, remove_suffix, decimeters_to_inches, hectograms_to_lbs, replace_new_lines, \
    replace_special

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


# Class to parse PokeAPI info for the specified Pokemon
class Parser:
    def __init__(self, num: int):
        self.num = num
        self.form_config = FormConfig(num)

        # Look up the pokemon and species information
        self.base_pokemon = pokebase.pokemon(self.form_config.base_num)
        self.species = self.base_pokemon.species

        # Set the actual pokemon
        # Most cases will be the same as the base
        if self.num == self.form_config.base_num:
            self.pokemon = self.base_pokemon
        # Added pokemon need their form (Mega, Alolan, etc.) looked up through the specified id
        else:
            self.pokemon = pokebase.pokemon(self.form_config.id)

    # Returns either the pokemon or the base pokemon depending on is_base
    def _get_pokemon(self, is_base: bool):
        if is_base:
            return self.base_pokemon
        else:
            return self.pokemon

    # Returns the English name from the list of the species' names
    # Ex: 'Bulbasaur'
    def get_name(self) -> str:
        # Check if this Pokemon has a hardcoded name
        name = name_substitution(self.num)
        if name == '':
            # If the language is English ('en'), return the entry's name
            # Ex: {'language': {'name': 'en', 'url': 'https://pokeapi.co/api/v2/language/9/'}, 'name': 'Bulbasaur'}
            name = next(entry.name for entry in self.species.names if entry.language.name == 'en')

        # Important for things like Farfetch'd vs Farfetch’d
        return replace_special(name)

    # Returns the base stats and given effort values in a tuple
    # Ex: ([45, 49, 49, 65, 65, 45], [0, 0, 0, 1, 0, 0])
    def get_stats_evs(self) -> Tuple[List[int], List[int]]:
        stats = [0]*6
        evs = [0]*6

        # Check if getting the stats from this pokemon or its base form
        pokemon = self._get_pokemon(self.form_config.use_base_stats)
        for stat in pokemon.stats:
            stat_index = _get_stat(stat.stat.name).value
            stats[stat_index] = stat.base_stat
            evs[stat_index] = stat.effort

        # Adjust stats/EVs if applicable
        stat_substitution(self.num, stats)
        effort_substitution(self.num, evs)

        return stats, evs

    # Returns the base experience
    # Ex: 64
    def get_base_experience(self) -> int:
        # Check if getting the experience from this pokemon or its base form
        pokemon = self._get_pokemon(self.form_config.use_base_exp)
        return pokemon.base_experience

    # Returns the growth rate as a namesies string
    # Ex: 'MEDIUM_SLOW'
    def get_growth_rate(self) -> str:
        growth_rate = _get_growth_rate(self.species.growth_rate.name)
        return namesies(growth_rate)

    # Returns the type of the Pokemon as a size 2 list of namesies strings
    # Single-typed Pokemon will have 'NO_TYPE' as second type
    # Ex: ['GRASS', 'POISON']
    def get_types(self) -> List[str]:
        types = ['', 'NO_TYPE']
        for poke_type in self.pokemon.types:
            types[poke_type.slot - 1] = namesies(poke_type.type.name)

        # Replace types if applicable
        types = type_substitution(self.num, types)

        assert len(types) == 2
        assert types[0] != ''
        assert types[0] != 'NO_TYPE'

        return types

    # Returns the capture rate
    # Ex: 45
    def get_capture_rate(self) -> int:
        return capture_rate_substitution(self.num, self.species.capture_rate)

    # Returns the the chance of this Pokémon being female, in eighths; or -1 for genderless
    # Ex: 1 (Bulbasaur is female 1/8th (12.5%) of the time)
    def get_gender_ratio(self) -> int:
        # Get the gender ratio and adjust if applicable
        female_ratio = self.species.gender_rate
        female_ratio = gender_substitution(self.num, female_ratio)
        return female_ratio

    # Returns the abilities as namesies strings in a list of size 2
    # If only has one regular ability, use the hidden ability as second
    # If it only has one valid ability, the second will be 'NO_ABILITY'
    # Ex: ['OVERGROW', 'CHLOROPHYLL']
    def get_abilities(self) -> List[str]:
        abilities = ['']*3

        # Add each ability in the corresponding slot in the list
        pokemon = self._get_pokemon(self.form_config.use_base_abilities)
        for ability in pokemon.abilities:
            ability_name = namesies(ability.ability.name)

            # Replace/remove the ability if applicable
            abilities[ability.slot - 1] = ability_substitution(self.num, ability_name)

            # Hidden abilities must be in the third slot
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

        # Replace new lines and special characters
        flavor_text = replace_new_lines(flavor_text)
        flavor_text = replace_special(flavor_text)

        return flavor_text

    # Returns the base egg steps (not hatch counter)
    # Ex: 5120
    def get_egg_steps(self) -> int:
        # Note: I use egg steps slightly differently than the main game because the main game hatches eggs really
        # weird with these hatch counter things and I just count down directly and don't care :/
        return self.species.hatch_counter * 256

    # Returns the egg groups as namesies strings in a list either size 1 or 2
    # Ex: ['MONSTER', 'GRASS'] or ['BUG'] or ['NO_EGGS']
    def get_egg_groups(self) -> List[str]:
        # Get them egg groupies
        egg_groups = [namesies(egg_group.name) for egg_group in self.species.egg_groups]

        # Just feel like these are backwards even though the order doesn't matter
        egg_groups.reverse()

        assert len(egg_groups) in [1, 2]
        return egg_groups

    # Returns the level-up moves and the learnable moves as a tuple
    # Level-up moves will be a list of strings of the format '<level> <ATTACK_NAME>' (Ex: '7 LEECH_SEED')
    #   and will be sorted by level order
    # Learnable moves (egg moves, move tutors, tms) will be a list of strings of namesies attack names
    # Ex: (['0 TACKLE', '3 GROWL', '7 LEECH_SEED', '9 VINE_WHIP', ...., '37 SEED_BOMB'],
    #      ['SWORDS_DANCE', 'SOLAR_BEAM', 'PETAL_DANCE', 'TOXIC', 'DOUBLE_TEAM', ...])
    def get_moves(self) -> Tuple[List[str], List[str]]:
        # Maps from version name to list of Moves in that version
        version_to_moves = {}  # type: Dict[str, List[Move]]
        for entry in self.pokemon.moves:
            for version_entry in entry.version_group_details:
                version_name = version_entry['version_group']['name']
                version_to_moves.setdefault(version_name, []).append(_get_move(entry, version_entry))

        # Get the moves of the most relevant version
        version = next(version for version in PAIRED_VERSIONS if version in version_to_moves)
        moves = version_to_moves[version]

        # Create move lists from version's moves
        level_up_moves = LevelUpMoves()
        learnable_moves = []  # type: List[str]
        for move in moves:
            attack_name = namesies(move.name)

            # Level-up moves
            if move.learn_method == 'level-up':
                # Potentially replace this move with another one
                attack_name = attack_substitution(self.num, attack_name)
                if attack_name == '':
                    # Should only be removing default moves
                    assert move.level_learned == 1
                    continue

                level_up_moves.add(move.level_learned, attack_name)
            # All other learnable moves
            # Did you know that if you bred Pikachu holding Light Ball the Pichu will know Volt Tackle??
            elif move.learn_method in ['machine', 'egg', 'tutor', 'light-ball-egg']:
                # Don't include unimplemented moves
                attack_name = learnable_attack_substitution(attack_name)
                if attack_name == '':
                    continue

                learnable_moves.append(attack_name)
            # Form change is for Rotom
            elif not (self.num == 479 and move.learn_method == 'form-change'):
                raise Exception('Unknown move learn method ' + move.learn_method + ' for ' + move.name)

        level_up_attack_additions(self.num, level_up_moves)

        # Add any manually added learnable moves
        learnable_moves.extend(learnable_attack_additions(self.num))

        return level_up_moves.get(), learnable_moves


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


# Replaces the growth rate with alternative names if applicable
def _get_growth_rate(growth_rate: str) -> str:
    if growth_rate == 'medium':
        return 'medium-fast'
    elif growth_rate == 'fast-then-very-slow':
        return 'fluctuating'
    elif growth_rate == 'slow-then-very-fast':
        return 'erratic'
    else:
        return growth_rate


# Parses the move entry into a Move object
def _get_move(move_entry, version_entry: Dict) -> Move:
    name = move_entry.move.name
    learn_method = version_entry['move_learn_method']['name']
    level_learned = version_entry['level_learned_at'] if learn_method == 'level-up' else None
    return Move(name, level_learned, learn_method)
