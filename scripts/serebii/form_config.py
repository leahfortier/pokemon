#!/usr/bin/env python3
# -*- coding: utf-8 -*-
from scripts.forms import AddedPokes
from scripts.serebii.parse_util import has_form, check_form
from scripts.substitution import Stat


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
        self.is_alolan = False
        base_exp_suffix = None
        image_suffix = None
        mega_suffix = ""

        # TODO: This should use name_substitution
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
        elif num == AddedPokes.MEGA_ABSOL.value:
            self.lookup_num = 359
            self.is_mega = True
            self.name = "Asbel"
        elif num == AddedPokes.MEGA_SABLEYE.value:
            self.lookup_num = 302
            self.is_mega = True
            self.name = "Yamirami"
            self.use_mega_stats = True
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
        elif num == AddedPokes.MEGA_BANETTE.value:
            self.lookup_num = 354
            self.name = "Jupetta"
            self.is_mega = True
            self.use_mega_stats = True
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
        return has_form(row, form_index, self.form_image_name)

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
        return check_form(form, self.form_image_name)
