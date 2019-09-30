#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from enum import Enum, auto
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


class Stat(Enum):
    HP = 0
    ATTACK = 1
    DEFENSE = 2
    SP_ATTACK = 3
    SP_DEFENSE = 4
    SPEED = 5


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


class Move:
    def __init__(self, move_entry, version_entry: Dict):
        self.name = move_entry.move.name
        self.learn_method = version_entry['move_learn_method']['name']
        self.level_learned = None
        if self.learn_method == 'level-up':
            self.level_learned = version_entry['level_learned_at']


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


# Original Pokes require an enum since their number is subject to change
class AddedPokes(Enum):
    MEGA_CHARIZARD = 810
    MEGA_MAWILE = auto()
    MEGA_ABSOL = auto()
    MEGA_SABLEYE = auto()
    ALOLAN_RAICHU = auto()
    ALOLAN_SANDSHREW = auto()
    ALOLAN_SANDSLASH = auto()
    ALOLAN_VULPIX = auto()
    ALOLAN_NINETALES = auto()
    ALOLAN_GRIMER = auto()
    ALOLAN_MUK = auto()
    ALOLAN_EXEGGUTOR = auto()
    ALOLAN_MAROWAK = auto()
    MEGA_BANNETTE = auto()
    MIDNIGHT_LYCANROC = auto()
    DUSK_LYCANROC = auto()


class FormConfig:
    def __init__(self, num: int) -> None:
        self.form_name = None
        self.ev_form_name = None
        self.type_form_name = None
        self.normal_form = True
        self.lookup_num = num
        self.name = None
        self.form_index = 0
        self.is_mega = False
        self.use_mega_stats = False
        self.use_mega_abilities = True
        self.ev_diffs = [0] * 6
        self.is_alolan = False
        base_exp_suffix = None
        image_suffix = None
        mega_suffix = ""

        # Flabebe has a stupid name with stupid special characters
        if num == 669:
            self.name = "Flabebe"
        elif num == 29:
            self.name = "Nidoran F"
        elif num == 32:
            self.name = "Nidoran M"

        # Pokemon with Alolan forms
        if num in [19, 20, 26, 27, 28, 37, 38, 50, 51, 52, 53, 74, 75, 76, 88, 89, 103, 105]:
            self.form_name = "Normal"
        # Castform
        elif num == 351:
            self.form_name = "Normal"
        # Deoxys
        elif num == 386:
            self.form_name = "Normal"
        # Wormadam is stupid
        elif num == 413:
            self.form_name = "Plant Cloak"
        # Rotom
        elif num == 479:
            self.form_name = "Rotom"
        # Giratina
        elif num == 487:
            self.form_name = "Altered"
        # Shaymin
        elif num == 492:
            self.form_name = "Land"
        # Darmanitan
        elif num == 555:
            self.form_name = "Normal"
            self.ev_form_name = "Standard"
        # Tornadus/Thundurus/Landorus
        elif num in [641, 642, 645]:
            self.form_name = "Incarnate"
        # Kyurem
        elif num == 646:
            self.form_name = "Standard"
            self.ev_form_name = "Kyurem"
        # Meloetta
        elif num == 648:
            self.form_name = "Aria"
        # Greninja
        elif num == 658:
            self.form_name = "Standard"
        # Zygarde
        elif num == 718:
            self.form_name = "Standard"
        # Hoopa
        elif num == 720:
            self.form_name = "Hoopa Confined"
        # Stupid dancing bird
        elif num == 741:
            self.form_name = "Baile Style"
        # Rockruff
        elif num == 744:
            self.form_name = "Standard"
        # Lycanroc
        elif num == 745:
            self.form_name = "Midday"
        # Necrozma
        elif num == 800:
            self.form_name = "Normal"

        elif num == AddedPokes.MEGA_CHARIZARD.value:
            self.lookup_num = 6
            self.is_mega = True
            self.name = "Rizardon"
            mega_suffix = " X"
            image_suffix = "-mx"
            base_exp_suffix = ""  # Use the same base exp as Charizard
        elif num == AddedPokes.MEGA_MAWILE.value:
            self.lookup_num = 303
            self.is_mega = True
            self.name = "Kuchiito"
            self.use_mega_stats = True
            self.use_mega_abilities = False
            self.ev_diffs[Stat.DEFENSE.value] += 1
        elif num == AddedPokes.MEGA_ABSOL.value:
            self.lookup_num = 359
            self.is_mega = True
            self.name = "Asbel"
            self.ev_diffs[Stat.ATTACK.value] += 1
        elif num == AddedPokes.MEGA_SABLEYE.value:
            self.lookup_num = 302
            self.is_mega = True
            self.name = "Yamirami"
            self.use_mega_stats = True
            self.ev_diffs[Stat.ATTACK.value] -= 1
            self.ev_diffs[Stat.DEFENSE.value] += 1
            self.ev_diffs[Stat.SP_DEFENSE.value] += 1
        elif num == AddedPokes.ALOLAN_RAICHU.value:
            self.lookup_num = 26
            self.name = "Silph Surfer"
            self.is_alolan = True
        elif num == AddedPokes.ALOLAN_SANDSHREW.value:
            self.lookup_num = 27
            self.name = "Snowshrew"
            self.is_alolan = True
        elif num == AddedPokes.ALOLAN_SANDSLASH.value:
            self.lookup_num = 28
            self.name = "Snowslash"
            self.is_alolan = True
        elif num == AddedPokes.ALOLAN_VULPIX.value:
            self.lookup_num = 37
            self.name = "Yukikon"
            self.is_alolan = True
        elif num == AddedPokes.ALOLAN_NINETALES.value:
            self.lookup_num = 38
            self.name = "Kyukon"
            self.is_alolan = True
        elif num == AddedPokes.ALOLAN_GRIMER.value:
            self.lookup_num = 88
            self.name = "Sleima"
            self.is_alolan = True
        elif num == AddedPokes.ALOLAN_MUK.value:
            self.lookup_num = 89
            self.name = "Sleimok"
            self.is_alolan = True
        elif num == AddedPokes.ALOLAN_EXEGGUTOR.value:
            self.lookup_num = 103
            self.name = "Kokonatsu"
            self.is_alolan = True
        elif num == AddedPokes.ALOLAN_MAROWAK.value:
            self.lookup_num = 105
            self.name = "GaraGara"
            self.is_alolan = True
        elif num == AddedPokes.MEGA_BANNETTE.value:
            self.lookup_num = 354
            self.name = "Jupetta"
            self.is_mega = True
            self.use_mega_stats = True
            self.ev_diffs[Stat.ATTACK.value] += 1
        elif num == AddedPokes.MIDNIGHT_LYCANROC.value:
            self.form_name = "Midnight"
            self.normal_form = False
            self.lookup_num = 745
            self.name = "Lougaroc"
            self.form_index = 1
            image_suffix = "-m"
        elif num == AddedPokes.DUSK_LYCANROC.value:
            self.form_name = "Dusk"
            self.normal_form = False
            self.lookup_num = 745
            self.name = "Lugarugan"
            self.form_index = 2
            image_suffix = "-d"

        if self.is_alolan:
            self.form_name = "Alola"
            self.normal_form = False
            self.type_form_name = "Alolan"
            self.form_index = 1
            image_suffix = "-a"

        if self.is_mega:
            self.mega_name = "Mega Evolution" + mega_suffix
            if base_exp_suffix is None:
                base_exp_suffix = "M"
            if image_suffix is None:
                image_suffix = "-m"
        else:
            assert not self.use_mega_stats
            assert mega_suffix == ""

        if base_exp_suffix is None:
            base_exp_suffix = ""
        if image_suffix is None:
            image_suffix = ""

        self.base_exp_name = str(self.lookup_num).zfill(3) + base_exp_suffix
        self.form_image_name = str(self.lookup_num).zfill(3) + image_suffix
        self.pokedex_image_name = str(self.lookup_num) + image_suffix

        if self.ev_form_name is None:
            self.ev_form_name = self.form_name
        if self.type_form_name is None:
            self.type_form_name = self.form_name

        # Basculin, Meowstic, Magearna (fucking Soul-Heart has a dash)
        self.use_abilities_list = num in [550, 678, 801]

    def has_form(self, row, form_index):
        # No form index implies there is only the normal form or all forms are treated the same
        if form_index is None:
            return True

        for form in row[form_index][0][0].getchildren():
            if self.check_form(form[0]):
                return True

        return False

    def has_form_from_table(self, table):
        has_image = False
        for form in table.getchildren():
            if form.tag != "img":
                continue

            has_image = True
            if self.check_form(form):
                return True

        # If you didn't find any image tags, then there are not multiple forms
        # So the only form is the normal form        
        return not has_image

    def check_form(self, form):
        image_name = form.attrib["src"]
        if image_name.endswith('/' + self.form_image_name + '.png'):
            return True
