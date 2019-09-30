from enum import Enum
from typing import List, Dict

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


# Returns the Stat that matches the input name
def get_stat(stat_name: str) -> Stat:
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


# Gets the English entry from the list of multiple language entries
# Ex: {'language': {'name': 'en', 'url': 'https://pokeapi.co/api/v2/language/9/'}, 'name': 'Bulbasaur'}
def get_english(entries: List):
    return next(entry for entry in entries if entry.language.name == 'en')


# Gets the English entry from the list of multiple language entries
# {'flavor_text': 'Bulbasaur can be seen napping in bright sunlight.\n
# There is a seed on its back. By soaking up the sun’s rays,\nthe seed grows progressively larger.',
# 'language': {'name': 'en', 'url': 'https://pokeapi.co/api/v2/language/9/'},
# 'version': {'name': 'alpha-sapphire', 'url': 'https://pokeapi.co/api/v2/version/26/'}}
def get_english_version(entries: List):
    # Get all English versions mapped from version name to entry
    english = { entry.version.name: entry for entry in entries if entry.language.name == 'en' }  # type: Dict[str,]

    # Return the entry of the first specified version in English
    return next(english[version] for version in INDIVIDUAL_VERSIONS if version in english)


# Returns a list of moves for the most relevant version
def get_moves(move_list: List) -> List[Move]:
    # Maps from version name to list of Moves in that version
    version_moves_map = {}  # type: Dict[str, List[Move]]
    for entry in move_list:
        for version_entry in entry.version_group_details:
            version_name = version_entry['version_group']['name']
            version_moves_map.setdefault(version_name, []).append(Move(entry, version_entry))

    # TODO: Obviously remove this but is still convenient sometimes for debugging so leaving it around a little longer
    # for version in versions:
    #     if version in version_moves_map:
    #         print("Move Version:", version)
    #         return version_moves_map[version]
    # raise Exception('No version found for moves')

    # Return the moves of the first specified version
    return next(version_moves_map[version] for version in PAIRED_VERSIONS if version in version_moves_map)


# Used for sorting the level up moves by level
# Attack should be a string with format '<int:level> <string:attackName>' (Ex: '7 LEECH_SEED')
def attack_sort(attack: str) -> int:
    split = attack.split(' ')
    assert len(split) == 2
    return int(split[0])