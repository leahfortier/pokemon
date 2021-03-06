# -*- coding: latin-1 -*-

import re

import math
import requests
from lxml import html

from scripts.forms import AddedPokes, Stat
from scripts.serebii.form_config import FormConfig
from scripts.serebii.parse_util import get_types, normalize_form, get_schema_index, substitute_egg_group, \
    substitute_ability, substitute_classification
from scripts.serebii.parser import Parser
from scripts.substitution import attack_substitution, ability_substitution, type_substitution, \
    learnable_attack_additions, gender_substitution, stat_substitution, effort_substitution
from scripts.util import namesies, remove_prefix, remove_empty, replace_special, dashy, Timer


def get_base_exp_map():
    page = requests.get('https://bulbapedia.bulbagarden.net/wiki/List_of_Pok%C3%A9mon_by_effort_value_yield')
    tree = html.fromstring(page.text)

    table = tree.xpath('//*[@id="mw-content-text"]/table[1]/tr')
    base_exp_map = {}
    for i, row in enumerate(table):
        # Schema
        if i == 0:
            continue

        num = row[0].text_content().strip()
        base_exp = int(row[3].text_content().replace("*", "").strip())

        # First one is likely the base form
        if num in base_exp_map:
            continue

        base_exp_map[num] = base_exp

    # Meltan/Melmetal aren't in this page yet
    base_exp_map['808'] = 135
    base_exp_map['809'] = 270

    return base_exp_map


with open("../../temp.txt", "w") as f:
    timer = Timer()

    base_exp_map = get_base_exp_map()
    for num in range(1, list(AddedPokes)[-1].value + 1):
#    for num in [1]:
        form_config = FormConfig(num)
        parser = Parser(form_config.lookup_num)

        info_index = 2
        if form_config.is_mega:
            # First row is Mega Evolution title
            info_index += 1
            assert parser.update_table(form_config.mega_name)

        # Picture, Name, Other Names, No., Gender Ratio, Type
        row = parser.info_table.xpath('tr[' + str(info_index) + ']')[0]

        form_config.lookup_name = row.xpath('td[2]')[0].text
        if form_config.name is None:
            form_config.name = form_config.lookup_name
        name = form_config.name
        print("#" + str(num).zfill(3) + " Name: " + name)

        male_ratio = row.xpath('td[5]')[0]

        # Genderless Pokemon
        if male_ratio.text is not None:
            male_ratio = -1
        else:
            # Remove the % from the end and convert to float
            male_ratio = float(male_ratio.xpath('table/tr[1]/td[2]')[0].text[:-1])
            if male_ratio > 50:
                male_ratio = math.floor(male_ratio)
            else:
                male_ratio = math.ceil(male_ratio)

        # TODO: Gender ratio format was changed to female ratio in eighths
        male_ratio = gender_substitution(num, male_ratio)
        print("Male Ratio: " + str(male_ratio))

        types_cell = row.xpath('td[6]')[0]
        types = types_cell.xpath('a/img')
        if len(types) > 0:
            types = get_types(types)
        # Multiple forms
        else:
            types = None
            forms = types_cell.xpath('table[1]/tr')
            for form in forms:
                type_form_name = normalize_form(form[0].text)
                if type_form_name == form_config.type_form_name:
                    types = get_types(form[1].xpath('a/img'))
                    break

        types = type_substitution(num, types)
        type1 = types[0]
        type2 = types[1]

        print("Type1: " + type1)
        print("Type2: " + type2)

        # Next row of the info table (skip two for the schema of the row)
        # Classification, Height, Weight, Capture Rate, Base Egg Steps
        info_index += 2
        row = parser.info_table.xpath('tr[' + str(info_index) + ']')[0]

        # Hoopa apparently has a different classification for its different forms
        if num == 720:
            classification = 'Mischief'
        # Remove the Pokemon text from the end of classification
        else:
            classification = row.xpath('td[1]')[0].text[:-8]
        classification = substitute_classification(classification)
        print("Classification: " + classification)

        # Height is specified in ft'in'' format -- convert to inches
        height = row.xpath('td[2]')[0].text
        height = height.split("/")
        height_index = form_config.form_index
        if len(height) <= height_index:
            height_index = 0
        height = height[height_index].strip()
        height = height.split("'")
        assert len(height) == 2
        height = int(height[0]) * 12 + int(height[1].replace('"', ''))
        print("Height: " + str(height))

        # Remove the lbs from the end of weight
        weight = row.xpath('td[3]')[0].text
        weight = weight.split("/")
        weight_index = form_config.form_index
        if len(weight) <= weight_index:
            weight_index = 0
        weight = weight[weight_index].strip()
        weight = weight[:-3].strip()
        weight = float(weight)
        print("Weight: " + str(weight))

        # Minior apparently has different catch rates for its different forms
        if num == 774:
            capture_rate = 30
        else:
            capture_rate = int(row.xpath('td[4]')[0].text)
        print("Capture Rate: " + str(capture_rate))

        egg_steps = row.xpath('td[5]')[0].text.replace(",", "").strip()
        if egg_steps == "":
            # Apparently this is a pretty universal base egg step value for legendaries/Pokemon that cannot breed...?
            egg_steps = 30720
        egg_steps = int(egg_steps)
        print("Egg Steps: " + str(egg_steps))

        if form_config.is_mega and not form_config.use_mega_abilities:
            parser.restore_backup()

        assert parser.update_table('Abilities')
        ability1 = None
        ability2 = None
        if form_config.use_abilities_list:
            abilities = parser.info_table.xpath('tr[2]/td/a/b')
            ability1 = abilities[0].text

            if len(abilities) >= 2:
                ability2 = abilities[1].text
            else:
                ability2 = "No_Ability"
        else:
            all_abilities = parser.info_table.xpath('tr[1]/td')[0].text_content()
            all_abilities = remove_prefix(all_abilities, "Abilities: ")
            all_abilities = all_abilities.replace("(Hidden)", "")
            all_abilities = all_abilities.replace("(Hidden Ability)", "")
            all_abilities = re.split("\)", all_abilities)
            remove_empty(all_abilities)
            for form_abilities in all_abilities:
                form_index = form_abilities.rfind("(")
                if form_index == -1:
                    # No form specified -- there should only be the normal form
                    assert len(all_abilities) == 1
                    assert form_config.normal_form
                else:
                    assert len(all_abilities) > 1
                    form = form_abilities[form_index + 1:].strip()
                    form = normalize_form(form)
                    if form_config.form_name != form:
                        continue
                    form_abilities = form_abilities[:form_index]
                form_abilities = form_abilities.strip()
                abilities = re.split("-", form_abilities)
                if abilities[0].strip() == "":
                    abilities = abilities[1:]
                ability1 = abilities[0].strip()
                if len(abilities) > 1:
                    ability2 = abilities[1].strip()
                else:
                    ability2 = "No_Ability"
                break
        assert ability1 is not None
        assert ability2 is not None

        # TODO: I have completely broken this and I don't feel like fixing it here when I need to fix everything
        # else in this file anyways but the point is that this method now removes instead of replacing so it should
        # default to the HA now if any of these are empty
        ability1 = ability_substitution(num, substitute_ability(ability1))
        ability2 = ability_substitution(num, substitute_ability(ability2))
        if ability1 == 'No_Ability':
            temp_ability = ability1
            ability1 = ability2
            ability2 = temp_ability

        print("Ability1: " + ability1)
        print("Ability2: " + ability2)

        # Next table -- Experience Growth, Base Happiness, Effort Values Earned, S.O.S. Calling
        parser.restore_backup()
        parser.get_next()
        row = parser.info_table.xpath('tr[4]')[0]

        growth_rate = list(row.xpath('td[1]')[0].itertext())[1]
        print("Growth Rate: " + growth_rate)

        ev_strings = row.xpath('td[3]')[0].itertext()

        # If no form is specified, use this in the mapping
        default_form = "FormNotSpecified"
        form = default_form
        ev_map = {form: [0] * 6}
        for ev_string in ev_strings:
            ev_index = ev_string.find(" Point(s)")

            # String doesn't contain EV info -- new form name
            if ev_index == -1:
                form = normalize_form(ev_string)
                assert form not in ev_map
                ev_map[form] = [0] * 6
                continue

            ev = ev_string[:ev_index]
            evs = ev_map[form]

            stat = ev[2:]
            value = int(ev[0])

            if stat == "HP":
                evs[Stat.HP.value] = value
            elif stat == "Attack":
                evs[Stat.ATTACK.value] = value
            elif stat == "Defense":
                evs[Stat.DEFENSE.value] = value
            elif stat == "Sp. Attack":
                evs[Stat.SP_ATTACK.value] = value
            elif stat == "Sp. Defense":
                evs[Stat.SP_DEFENSE.value] = value
            elif stat == "Speed":
                evs[Stat.SPEED.value] = value
            else:
                raise Exception(stat)

        if form_config.ev_form_name is None:
            evs = ev_map[default_form]
        if form_config.ev_form_name not in ev_map:
            assert form_config.normal_form or len(ev_map) == 1
            evs = ev_map[default_form]
        else:
            evs = ev_map[form_config.ev_form_name]

        # Add diffs
        effort_substitution(num, evs)
        print("Effort Values: " + str(evs))

        # Egg Group table
        parser.get_next()
        parser.get_next()

        # Gen 1 (not including Mew I guess) plus Meltan line have the Catch/Transfer Candy section
        egg_index = 2
        if form_config.lookup_num < 151 or num in [808, 809]:
            egg_index += 1

        egg_group = parser.info_table.xpath('tr[2]/td[' + str(egg_index) + ']')[0]
        if egg_group.text is not None:
            egg_group1 = "Undiscovered"
            egg_group2 = "None"
        else:
            egg_group1 = egg_group.xpath('table/tr[1]/td[2]/a')[0].text
            egg_group2 = egg_group.xpath('table/tr[2]/td[2]/a')

            if len(egg_group2) == 0:
                egg_group2 = "None"
            else:
                egg_group2 = egg_group2[0].text

        egg_group1 = substitute_egg_group(egg_group1)
        egg_group2 = substitute_egg_group(egg_group2)

        print("Egg Group1: " + egg_group1)
        print("Egg Group2: " + egg_group2)

        if parser.update_table('Flavor Text'):
            flavor_text = parser.info_table.xpath('tr[2]/td[2]')[0].text
            if flavor_text in ['Sun', "Let's Go, Pikachu!"]:
                flavor_text = parser.info_table.xpath('tr[2]/td[3]')[0].text
            if flavor_text is None:
                # infoTable.xpath('td[3]')[0].text == 'Ultra Sun' for this case
                flavor_text = parser.info_table.xpath('td[4]')[0].text
        else:
            flavor_text = 'None'

        # Replace the special e character in the flavor text
        flavor_text = replace_special(flavor_text)
        print("Flavor Text: " + flavor_text)

        print("Attacks:")
        if form_config.normal_form:
            level_up_tables = ['Ultra Sun/Ultra Moon Level Up',
                               'Ultra Sun / Ultra Moon Level Up',
                               'Sun/Moon Level Up',
                               'Sun / Moon Level Up',
                               'Standard Level Up',
                               'Generation VII Level Up',
                               'Let\'s Go Level Up']
        else:
            suffix = " - " + form_config.form_name + " Form"
            level_up_tables = ['Ultra Sun/Ultra Moon Level Up' + suffix,
                               'Ultra Sun / Ultra Moon Level Up' + suffix,
                               'Sun/Moon Level Up' + suffix,
                               'Sun / Moon Level Up' + suffix,
                               form_config.form_name + " Form Level Up"]

        assert parser.update_table(*level_up_tables)
        attacks = []
        for i in range(2, len(parser.info_table) - 1, 2):
            level = parser.info_table[i][0].text

            if level == 'Evolve':
                level = 0
            elif level == dashy:
                level = 1

            attack = parser.info_table[i][1][0].text
            attack = attack_substitution(num, namesies(attack))
            if attack == '':
                assert level == 1
                continue

            attacks.append(str(level) + " " + attack)
            print(str(int(level)) + " " + attack)

        print("TMS:")
        tms = []
        if parser.update_table('TM & HM Attacks'):
            schema = parser.info_table[1]
            attack_index = get_schema_index(schema, "Attack Name")
            form_index = get_schema_index(schema, "Form")

            for i in range(2, len(parser.info_table) - 1, 2):
                row = parser.info_table[i]

                attack = row[attack_index][0].text
                if attack in ["Frustration", "Return", "Quash"]:
                    continue

                if not form_config.has_form(row, form_index):
                    continue

                tms.append(attack)
                print(attack)
        additions = learnable_attack_additions(num)
        for attack in additions:
            tms.append(attack)
            print(attack)

        print("Egg Moves:")
        egg_moves = []
        if parser.update_table('Egg Moves '):
            schema = parser.info_table[1]
            attack_index = get_schema_index(schema, "Attack Name")

            for i in range(2, len(parser.info_table) - 1, 2):
                row = parser.info_table[i]

                attack = row[attack_index][0].text
                if attack == "Ion Deluge":
                    attack = "Electrify"
                elif attack in ["Helping Hand", "Ally Switch", "After You", "Wide Guard", "Quash", "Rage Powder",
                                "Follow Me", "Spotlight"]:
                    continue

                # This column does not have a name in the schema
                # It is always present since it additionally contains the details
                # For Pokemon with multiple forms, these will additionally be included here
                details_col = row[-1]
                if not form_config.has_form_from_table(details_col):
                    continue

                egg_moves.append(attack)
                print(attack)

        print("Move Tutor Moves:")
        tutor_moves = []
        if parser.update_table('Move Tutor Attacks'):
            table = parser.info_table.xpath('thead/tr')

            schema = table[1]
            attack_index = get_schema_index(schema, "Attack Name")
            form_index = get_schema_index(schema, "Form")

            for i in range(2, len(table) - 1, 2):
                row = table[i]

                attack = row[attack_index][0].text
                if attack in ["Helping Hand", "After You", "Ally Switch"]:
                    continue

                if not form_config.has_form(row, form_index):
                    continue

                tutor_moves.append(attack)
                print(attack)
        if parser.update_table('Ultra Sun/Ultra Moon Move Tutor Attacks'):
            table = parser.info_table.xpath('thead/tr')

            schema = table[1]
            attack_index = get_schema_index(schema, "Attack Name")
            form_index = get_schema_index(schema, "Form")

            for i in range(2, len(table) - 1, 2):
                row = table[i]

                attack = row[attack_index][0].text
                if attack in ["Helping Hand", "After You", "Ally Switch"]:
                    continue

                if not form_config.has_form(row, form_index):
                    continue

                tutor_moves.append(attack)
                print(attack)

        #        print("Transfer Moves:")
        #        if updateTable('Transfer Only Moves '):
        #            schema = infoTable[1]
        #            attackIndex = getSchemaIndex(schema, "Attack Name")
        #            methodIndex = getSchemaIndex(schema, "Method")
        #
        #            startIndex = 2
        #            if infoTable[2][0].tag == "th":
        #                startIndex = 3
        #
        #            for i in range(startIndex, len(infoTable) - 1, 2):
        #                row = infoTable[i]
        #
        #                attack = row[attackIndex][0].text
        #                method = row[methodIndex].text
        #
        #                if "Gen VI" in method:
        #                    tms.append(attack)
        #                    print(attack)

        # Stats
        if form_config.use_mega_stats:
            # Not sure this will work for all cases -- particularly for multiple megas
            stats_table = ["Stats - Mega Evolution"]
        elif form_config.is_alolan:
            stats_table = ["Stats - Alolan " + form_config.lookup_name]
        elif not form_config.normal_form:
            stats_table = ["Stats - " + form_config.form_name + " Form", "Stats"]
        else:
            stats_table = ["Stats"]
        assert parser.update_table(*stats_table)

        stats = [0] * 6
        for i in range(0, len(stats)):
            stats[i] = int(parser.info_table.xpath('tr[3]/td[' + str(2 + i) + ']')[0].text)

        stat_substitution(num, stats)
        print("Stats: " + str(stats))

        base_exp = base_exp_map[form_config.base_exp_name]
        if form_config.is_alolan and form_config.base_exp_name + "A" in base_exp_map:
            base_exp = base_exp_map[form_config.base_exp_name + "A"]

        print("Base EXP: " + str(base_exp))

        f.write(str(num) + '\n')
        f.write(str(name) + '\n')

        stats = [str(stat) for stat in stats]
        f.write(' '.join(stats) + '\n')

        if len(str(base_exp)) == 1:
            f.write("BASE EXP: ")

        f.write(str(base_exp) + '\n')
        f.write(namesies(growth_rate) + '\n')
        f.write(namesies(type1) + ' ')
        f.write(namesies(type2) + '\n')

        f.write(str(capture_rate) + '\n')

        evs = [str(ev) for ev in evs]
        f.write(' '.join(evs) + '\n')

        # TODO: Evolutions
        f.write('NONE\n')

        # TODO: Wild Hold Items
        f.write('0\n')

        f.write(str(male_ratio) + '\n')
        f.write(namesies(ability1) + ' ')
        f.write(namesies(ability2) + '\n')
        f.write(str(classification) + '\n')
        f.write(str(height) + ' ')
        f.write(str(weight) + '\n')
        f.write(str(flavor_text) + '\n')
        f.write(str(egg_steps) + '\n')
        f.write(str(egg_group1) + ' ')
        f.write(str(egg_group2) + '\n')

        f.write(str(len(attacks)) + '\n')
        for attack in attacks:
            f.write(attack + '\n')

        f.write(str(len(tms) + len(egg_moves) + len(tutor_moves)) + '\n')
        for attack in tms:
            f.write(namesies(attack) + '\n')
        for attack in egg_moves:
            f.write(namesies(attack) + '\n')
        for attack in tutor_moves:
            f.write(namesies(attack) + '\n')

        f.write('\n')

    timer.print()
