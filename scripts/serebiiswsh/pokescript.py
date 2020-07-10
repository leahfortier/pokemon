from typing import Union

from scripts.forms import AddedPokes
from scripts.serebiiswsh.bulbyparser import get_base_map
from scripts.serebiiswsh.form_config import FormConfig
from scripts.serebiiswsh.parser import Parser
from scripts.substitution import effort_substitution
from scripts.util import Timer, namesies


# PokeAPI still not updated and just starting the serebii script over for gen 8 since there are a lot of changes
# anyhow and I am so confused
with open("../../temp.txt", "w") as f:
    timer = Timer()

    gen_8 = range(810, 891)
    galarian = range(AddedPokes.GALARIAN_MEOWTH.value, AddedPokes.GALARIAN_YAMASK.value + 1)
    base_map = get_base_map()

    for num in [*gen_8, *galarian]:
        form = FormConfig(num)
        base = base_map[form.base_exp_name]
        print("#" + str(num).zfill(3) + " " + base.name)

        # Starts at table holding name and classification rows
        parser = Parser(num, base, form)

        # Name, Other Names, No., Gender Ratio, Type
        row = parser.info_table.xpath('tr[2]')[0]

        name = parser.get_name()
        print("Name:", name)

        female_ratio = parser.get_female_ratio()
        print("Female Ratio: " + str(female_ratio))

        types = parser.get_types(form)
        print("Type: ", types)

        classification = parser.get_classification()
        print("Classification: " + classification)

        height = parser.get_height(form)
        print("Height:", height)

        weight = parser.get_weight(form)
        print("Weight:", weight)

        catch_rate = parser.get_catch_rate()
        print("Capture Rate: " + str(catch_rate))

        egg_steps = parser.get_egg_steps()
        print("Egg Steps: " + str(egg_steps))

        abilities = parser.get_abilities()
        print("Abilities:", abilities)

        growth_rate = parser.get_growth_rate()
        print("Growth Rate: " + growth_rate)

        # Next table - Type effectiveness chart
        parser.get_next()

        # Next table - Wild hold items and egg groups
        parser.get_next()

        egg_groups = parser.get_egg_groups()
        print("Egg Groups:", egg_groups)

        flavor_text = parser.get_flavor_text()
        print("Flavor Text:", flavor_text)

        level_up = parser.get_level_up_moves(form)
        print("Level-up Attacks:", level_up)

        learnable = parser.get_learnable_moves(form)
        print("Learnable Attacks:", learnable)

        stats = parser.get_stats(form)
        print("Stats:", stats)

        evs = base.evs
        effort_substitution(num, base.evs)
        print("Effort Values:", evs)

        base_exp = base.base_exp
        print("Base EXP:", base_exp)

        f.write(str(num) + '\n')
        f.write(str(name) + '\n')

        stats = [str(stat) for stat in stats]
        f.write(' '.join(stats) + '\n')

        f.write(str(base_exp) + '\n')
        f.write(namesies(growth_rate) + '\n')
        f.write(namesies(types[0]) + ' ')
        f.write(namesies(types[1]) + '\n')

        f.write(str(catch_rate) + '\n')

        evs = [str(ev) for ev in evs]
        f.write(' '.join(evs) + '\n')

        # TODO: Evolutions
        f.write('NONE\n')

        # TODO: Wild Hold Items
        f.write('0\n')

        f.write(str(female_ratio) + '\n')
        f.write(' '.join(abilities) + '\n')
        f.write(str(classification) + '\n')
        f.write(str(height) + ' ')
        f.write(str(weight) + '\n')
        f.write(str(flavor_text) + '\n')
        f.write(str(egg_steps) + '\n')
        f.write(' '.join(egg_groups) + '\n')

        f.write(str(len(level_up)) + '\n')
        f.write('\n'.join(level_up) + '\n')

        f.write(str(len(learnable)) + '\n')
        f.write('\n'.join(learnable) + '\n')

        f.write('\n')

    timer.print()
