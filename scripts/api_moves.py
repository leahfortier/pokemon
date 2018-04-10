#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import pokebase as pb
from util import replace_special, add_value, namesies

def filter_val(filtered):
    assert len(filtered) == 1
    val = filtered[0]
    val = replace_special(val)
    val = val.replace('\n', ' ')
    return val

def get_values(attack_name):
    attack_name = attack_name.strip()    
    values = [attack_name]
    print(i, attack_name)
    
    lookup_name = attack_name.lower().replace(" ", "-").replace(".", "").replace("'", "")
    if lookup_name == "judgement":
        lookup_name = "judgment"
    
    try:
        attack = pb.move(lookup_name)
    except ValueError:
        assert attack_name in ['Photon Geyser', 'Mind Blown', 'Plasma Fists']
        print("ValueError for " + attack_name + "!!!!!")
        return
    
    # Make sure attack name is the same
    api_attack_name = filter_val([n.name for n in attack.names if n.language.name == 'en'])
    if api_attack_name == "Judgment":
        api_attack_name = "Judgement"    
    assert attack_name == api_attack_name
    
    # Flava flav
    flavor_text = filter_val([ft.flavor_text for ft in attack.flavor_text_entries if ft.language.name == 'en' and ft.version_group.name == 'sun-moon'])
    add_value(values, flavor_text)
    
    # Type, target, category, accuracy, pp, power, effect chance, priority
    add_value(values, namesies(attack.type.name))
    add_value(values, namesies(attack.target.name))
    add_value(values, namesies(attack.damage_class.name))
    add_value(values, str(attack.accuracy))
    add_value(values, str(attack.pp))
    add_value(values, str(attack.power))
    add_value(values, str(attack.effect_chance))
    add_value(values, str(attack.priority))
    
    # Stat changes
    stat_changes = []
    for stat_change in attack.stat_changes:
        stat_name = stat_change.stat.name.upper().replace('-', '_').replace('SPECIAL', 'SP')
        if stat_name == "HP":
            assert len(attack.stat_changes) == 6
            continue
        stat_changes.append(stat_name + " " + str(stat_change.change))
    add_value(values, str(len(stat_changes)) + " " + " ".join(stat_changes))
    
    # Lots more metadata
    meta = attack.meta
    add_value(values, namesies(meta.category.name).replace('+', '_'))
    add_value(values, namesies(meta.ailment.name))
    add_value(values, str(meta.min_hits))
    add_value(values, str(meta.max_hits))
    add_value(values, str(meta.min_turns))
    add_value(values, str(meta.max_turns))
    add_value(values, str(meta.drain))
    add_value(values, str(meta.healing))
    add_value(values, str(meta.crit_rate))
    add_value(values, str(meta.ailment_chance))
    add_value(values, str(meta.flinch_chance))
    add_value(values, str(meta.stat_chance))
    
    short_effect = filter_val([eff.short_effect for eff in attack.effect_entries if eff.language.name == 'en'])
    add_value(values, short_effect)
    
    effect = filter_val([eff.effect for eff in attack.effect_entries if eff.language.name == 'en'])
    add_value(values, effect)
    
    return values

f = open("moves.in", "r")
out = open("api_moves.out", "w")
for i, attack_name in enumerate(f):
    values = get_values(attack_name)
    if values is None:
        continue
    
    for value in values:
        out.write(value + '\n')
        
f.close()
out.close()
