#!/usr/bin/env python3
# -*- coding: utf-8 -*-
from typing import List

import math
import requests
from lxml import html

from scripts.move import LevelUpMoves
from scripts.serebiiswsh.bulbyparser import PokeBase
from scripts.serebiiswsh.form_config import FormConfig
from scripts.serebii.parse_util import get_query_text, get_types, normalize_form, get_schema_index, \
    substitute_egg_group, substitute_ability, slash_form, check_form, substitute_classification
from scripts.substitution import stat_substitution, gender_substitution, type_substitution, ability_substitution, \
    egg_group_substitution, attack_substitution, learnable_attack_substitution, capture_rate_substitution, \
    name_substitution, level_up_attack_additions, learnable_attack_additions
from scripts.util import replace_special, namesies, replace_new_lines, dashy


class Parser:
    def __init__(self, num: int, base: PokeBase, form: FormConfig):
        self.num = num
        self.name = base.name

        url_suffix = replace_special(base.name.lower().replace(' ', ''))
        page = requests.get('http://www.serebii.net/pokedex-swsh/' + url_suffix)
        tree = html.fromstring(page.text)
        self.main_div = tree.xpath('/html/body/div[1]/div[2]/main/div[2]')[0]

        self.info_table = self.main_div.xpath('table[3]')[0]

        # Extra table that has previous generation links (does not exist for gen 8 Pokemon)
        if form.lookup_num < 810:
            self.get_next()

        # Name, Other Names, No., Gender Ratio, Type
        self.name_row = self.info_table.xpath('tr[2]')[0]

        # Classification, Height, Weight, Capture Rate, Base Egg Steps
        self.class_row = self.info_table.xpath('tr[4]')[0]

    def update_table(self, *headers):
        for header in headers:
            if self._update_table_header(header):
                return True
        return False

    def _update_table_header(self, header):
        # Store current tables and indexes and such to restore if table not found
        temp_info_table = self.info_table

        while True:
            # We found the header we're looking for!
            if self.check_header(header):
                return True

            self.get_next()
            if self.info_table is None:
                # Header not found -- restore original index values and return false
                self.info_table = temp_info_table
                return False

    def get_next(self):
        # Try traditional next element first
        self.info_table = self.info_table.getnext()

    def check_queries(self, *queries):
        for query_string in queries:
            query = self.info_table.xpath(query_string)
            text = get_query_text(query)
            if text is not None:
                return text

    def check_header(self, header):
        if self.info_table.tag == 'table':
            text = self.check_queries('tr[1]/td/b', 'tr[1]/td/h3', 'tr[1]/td', 'thead/tr[1]/td')
            if text is not None and text == header:
                return True
        return False

    def get_name(self) -> str:
        # Check if this Pokemon has a hardcoded name
        name = name_substitution(self.num)

        # If not, parse the name
        if name == '':
            name = self.name_row.xpath('td[1]')[0].text
            assert name == self.name

        return replace_special(name)

    def get_female_ratio(self) -> int:
        gender_cell = self.name_row.xpath('td[4]')[0]

        # Genderless Pokemon
        if gender_cell.text is not None:
            assert gender_cell.text == self.name + ' is Genderless'
            female_ratio = -1
        else:
            # Remove the % from the end and convert to float
            male_percentage = math.ceil(float(gender_cell.xpath('table/tr[1]/td[2]')[0].text[:-1]))

            # For some weird reason ratios are fucked up on serebii so 87.5% shows up as 88.14% for example
            # (ideally ceil function would not be necessary)
            female_ratio = math.ceil((100 - male_percentage) / 100 * 8)

        return gender_substitution(self.num, female_ratio)

    def get_types(self, form_config: FormConfig) -> [int, int]:
        types_cell = self.name_row.xpath('td[5]')[0]
        types = types_cell.xpath('a/img')

        # Multiple forms
        if len(types) == 0:
            types = None
            forms = types_cell.xpath('table[1]/tr')
            for form in forms:
                type_form_name = normalize_form(form[0].text)
                if type_form_name == form_config.type_form_name:
                    types = form[1].xpath('a/img')
                    break

        types = get_types(types)
        return type_substitution(self.num, types)

    def get_classification(self, form: FormConfig) -> str:
        class_cell = self.class_row.xpath('td[1]')[0]
        classification = class_cell.text

        # Different classifications for different forms (most will not use this)
        for text in class_cell.itertext():
            if '(' in text:
                # Only really checks for Galarian form right now so that might need to be adjusted in the future
                if 'Galar' not in text and form.is_galarian:
                    continue

                # Remove form from end of classification
                text = text[:text.find('(')].strip()

            classification = text

            # Normal form will always be first
            if form.normal_form:
                break

        # Yep Serebii definitely has some typos and it's not my favorite
        assert classification[-8:] in [' Pokémon', 'Pokémonn']

        # Remove the Pokemon text from the end of classification
        classification = classification[:-8].strip()

        return substitute_classification(self.num, classification)

    def get_height(self, form: FormConfig) -> int:
        height_cell = self.class_row.xpath('td[2]')[0]
        height = slash_form(height_cell.text, form.normal_form)

        # Height is specified in ft'in'' format -- convert to inches
        height = height.split("'")
        assert len(height) == 2
        return int(height[0]) * 12 + int(height[1].replace('"', ''))

    def get_weight(self, form: FormConfig) -> float:
        weight_cell = self.class_row.xpath('td[3]')[0]
        weight = slash_form(weight_cell.text, form.normal_form)

        # Remove the lbs from the end of weight
        return float(weight[:-3])

    def get_catch_rate(self) -> int:
        capture_rate_cell = self.class_row.xpath('td[4]')[0]
        return capture_rate_substitution(self.num, int(capture_rate_cell.text))

    def get_egg_steps(self) -> int:
        egg_steps_cell = self.class_row.xpath('td[5]')[0]
        egg_steps = egg_steps_cell.text.replace(",", "").strip()
        if egg_steps == "":
            # Apparently this is a pretty universal base egg step value for legendaries/Pokemon that cannot breed...?
            egg_steps = 30720
        return int(egg_steps)

    def get_abilities(self, form: FormConfig) -> List[str]:
        # Next table - Abilities and Exp. Growth row
        self.get_next()

        abilities_cell = self.info_table.xpath('tr[2]/td')[0]

        abilities = []
        in_form = form.normal_form

        for ability_row in abilities_cell.getchildren():
            if ability_row.tag == 'a':
                ability_cell = ability_row.xpath('b')
                if len(ability_cell) == 0 or not in_form:
                    continue

                ability_name = substitute_ability(self.num, ability_cell[0].text)

                # Replace/remove the ability if applicable
                abilities.append(ability_substitution(self.num, ability_name))
            elif ability_row.tag == 'b':
                # New form -- start or finish
                if 'Abilities' in ability_row.text:
                    if in_form:
                        break
                    elif form.form_name in ability_row.text:
                        in_form = True

        # Remove empty slots
        abilities = [ability for ability in abilities if ability != '']

        assert len(abilities) in [1, 2, 3]
        return abilities

    def get_growth_rate(self):
        # Experience Growth, Base Happiness, Effort Values Earned, Dynamax Capable
        row = self.info_table.xpath('tr[4]')[0]

        growth_cell = row.xpath('td[1]')[0]
        return list(growth_cell.itertext())[1]

    def get_egg_groups(self):
        egg_group_cell = self.info_table.xpath('tr[2]/td[2]')[0]

        egg_groups = []
        if egg_group_cell.text is not None:
            egg_groups.append('NO_EGGS')
        else:
            egg_groups.append(egg_group_cell.xpath('table/tr[1]/td[2]/a')[0].text)
            egg_group2 = egg_group_cell.xpath('table/tr[2]/td[2]/a')

            if len(egg_group2) > 0:
                egg_groups.append(egg_group2[0].text)

        egg_groups = [substitute_egg_group(egg_group) for egg_group in egg_groups]
        egg_groups = egg_group_substitution(self.num, egg_groups)

        assert len(egg_groups) in [1, 2]
        return egg_groups

    def get_flavor_text(self, form: FormConfig):
        flavor_text = 'None'
        if self.update_table('Flavor Text'):
            flavor_index = 2

            table = self.info_table.xpath('tr')
            for i in range(1, len(table), 1):
                row = table[i]

                # If second column says 'Sword' instead of flavor text, then first column is a form image
                if row.xpath('td[2]')[0].text == 'Sword':
                    # If not the correct form, skip to the next form
                    if not check_form(row.xpath('td[1]/img')[0], form.form_image_name):
                        continue
                    # Correct form, but advance index because having alternate forms adds a column for images
                    else:
                        flavor_index = 3

                flavor_text = row.xpath('td[' + str(flavor_index) + ']')[0].text
                break

        # Replace new lines and special characters
        flavor_text = replace_new_lines(flavor_text)
        flavor_text = replace_special(flavor_text)

        return flavor_text

    def get_level_up_moves(self, form: FormConfig) -> [str]:
        table_names = []
        if form.normal_form:
            table_names.append('Standard Level Up')
        elif form.is_alolan:
            table_names.append('Alola Form Level Up')
        elif form.is_galarian:
            table_names.append('Galarian Form Level Up')
        if form.form_name is not None:
            table_names.append('Level Up - ' + form.form_name)
        assert self.update_table(*table_names)

        attacks = LevelUpMoves()
        for i in range(2, len(self.info_table) - 1, 2):
            level = self.info_table[i][0].text

            if level == 'Evolve':
                level = 0
            elif level == dashy:
                level = 1
            else:
                level = int(level)

            attack = self.info_table[i][1][0].text
            attack = attack_substitution(self.num, namesies(attack))
            if attack == '':
                assert level == 1
                continue

            attacks.add(level, attack)

        level_up_attack_additions(self.num, attacks)

        return attacks.get()

    def get_learnable_moves(self, form: FormConfig) -> [str]:
        attacks = []
        attacks.extend(self._get_learnable_moves(form, 'Technical Machine Attacks'))
        attacks.extend(self._get_learnable_moves(form, 'Technical Record Attacks'))
        attacks.extend(self._get_learnable_moves(form, 'Egg Moves'))
        attacks.extend(self._get_learnable_moves(form, 'Move Tutor Attacks'))
        attacks.extend(self._get_learnable_moves(form, 'Isle of Armor Move Tutor Attacks'))

        # Add any manually added learnable moves
        attacks.extend(learnable_attack_additions(self.num))

        return attacks

    def _get_learnable_moves(self, form: FormConfig, table_name: str) -> [str]:
        attacks = []
        if self.update_table(table_name):
            table = self.info_table.xpath('tr')
            if len(table) == 0:
                table = self.info_table.xpath('thead/tr')

            schema = table[1]
            attack_index = get_schema_index(schema, "Attack Name")
            form_index = get_schema_index(schema, "Form")

            for i in range(2, len(table) - 1, 2):
                row = table[i]

                attack = namesies(row[attack_index][0].text)

                if not form.has_form(row, form_index):
                    continue

                # Don't include unimplemented moves
                attack = learnable_attack_substitution(attack)
                if attack == '':
                    continue

                attacks.append(attack)
        return attacks

    def get_stats(self, form: FormConfig) -> [int]:
        table_name = 'Stats'
        if form.is_alolan:
            table_name += ' - Alolan ' + self.name
        elif form.is_galarian:
            table_name += ' - Galarian ' + self.name
        assert self.update_table(table_name)
        stats = [0]*6
        for i in range(0, len(stats)):
            stats[i] = int(self.info_table.xpath('tr[3]/td[' + str(2 + i) + ']')[0].text)

        stat_substitution(self.num, stats)
        return stats

