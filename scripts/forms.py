#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from enum import Enum, auto


class Stat(Enum):
    HP = 0
    ATTACK = 1
    DEFENSE = 2
    SP_ATTACK = 3
    SP_DEFENSE = 4
    SPEED = 5


# Original Pokes require an enum since their number is subject to change
class AddedPokes(Enum):
    MEGA_CHARIZARD = 808
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
    def __init__(self, num):
        self.formName = None
        self.evFormName = None
        self.typeFormName = None
        self.normalForm = True
        self.lookupNum = num
        self.name = None
        self.formIndex = 0
        self.isMega = False
        self.useMegaStats = False
        self.useMegaAbilities = True
        self.evDiffs = [0]*6
        self.isAlolan = False
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
            self.formName = "Normal"
        # Deoxys
        elif num == 386:
            self.formName = "Normal"
        # Wormadam is stupid
        elif num == 413:
            self.formName = "Plant Cloak"
        # Rotom
        elif num == 479:
            self.formName = "Rotom"
        # Giratina
        elif num == 487:
            self.formName = "Altered"
        # Shaymin
        elif num == 492:
            self.formName = "Land"
        # Darmanitan
        elif num == 555:
            self.formName = "Normal"
            self.evFormName = "Standard"
        # Tornadus/Thundurus/Landorus
        elif num in [641, 642, 645]:
            self.formName = "Incarnate"
        # Kyurem
        elif num == 646:
            self.formName = "Standard"
            self.evFormName = "Kyurem"
        # Meloetta
        elif num == 648:
            self.formName = "Aria"
        # Greninja
        elif num == 658:
            self.formName = "Standard"
        # Zygarde
        elif num == 718:
            self.formName = "Standard"
        # Hoopa
        elif num == 720:
            self.formName = "Hoopa Confined"
        # Stupid dancing bird
        elif num == 741:
            self.formName = "Baile Style"
        # Rockruff
        elif num == 744:
            self.formName = "Standard"
        # Lycanroc
        elif num == 745:
            self.formName = "Midday"
        # Necrozma
        elif num == 800:
            self.formName = "Normal"
            
        elif num == AddedPokes.MEGA_CHARIZARD.value:
            self.lookupNum = 6
            self.isMega = True
            self.name = "Rizardon"
            mega_suffix = " X"
            image_suffix = "-mx"
            base_exp_suffix = ""  # Use the same base exp as Charizard
        elif num == AddedPokes.MEGA_MAWILE.value:
            self.lookupNum = 303
            self.isMega = True
            self.name = "Kuchiito"
            self.useMegaStats = True
            self.useMegaAbilities = False
            self.evDiffs[Stat.DEFENSE.value] += 1
        elif num == AddedPokes.MEGA_ABSOL.value:
            self.lookupNum = 359
            self.isMega = True
            self.name = "Asbel"
            self.evDiffs[Stat.ATTACK.value] += 1
        elif num == AddedPokes.MEGA_SABLEYE.value:
            self.lookupNum = 302
            self.isMega = True
            self.name = "Yamirami"
            self.useMegaStats = True
            self.evDiffs[Stat.ATTACK.value] -= 1
            self.evDiffs[Stat.DEFENSE.value] += 1
            self.evDiffs[Stat.SP_DEFENSE.value] += 1
        elif num == AddedPokes.ALOLAN_RAICHU.value:
            self.lookupNum = 26
            self.name = "Silph Surfer"
            self.isAlolan = True
        elif num == AddedPokes.ALOLAN_SANDSHREW.value:
            self.lookupNum = 27
            self.name = "Snowshrew"
            self.isAlolan = True
        elif num == AddedPokes.ALOLAN_SANDSLASH.value:
            self.lookupNum = 28
            self.name = "Snowslash"
            self.isAlolan = True
        elif num == AddedPokes.ALOLAN_VULPIX.value:
            self.lookupNum = 37
            self.name = "Yukikon"
            self.isAlolan = True
        elif num == AddedPokes.ALOLAN_NINETALES.value:
            self.lookupNum = 38
            self.name = "Kyukon"
            self.isAlolan = True
        elif num == AddedPokes.ALOLAN_GRIMER.value:
            self.lookupNum = 88
            self.name = "Sleima"
            self.isAlolan = True
        elif num == AddedPokes.ALOLAN_MUK.value:
            self.lookupNum = 89
            self.name = "Sleimok"
            self.isAlolan = True
        elif num == AddedPokes.ALOLAN_EXEGGUTOR.value:
            self.lookupNum = 103
            self.name = "Kokonatsu"
            self.isAlolan = True
        elif num == AddedPokes.ALOLAN_MAROWAK.value:
            self.lookupNum = 105
            self.name = "GaraGara"
            self.isAlolan = True
        elif num == AddedPokes.MEGA_BANNETTE.value:
            self.lookupNum = 354
            self.name = "Jupetta"
            self.isMega = True
            self.useMegaStats = True
            self.evDiffs[Stat.ATTACK.value] += 1
        elif num == AddedPokes.MIDNIGHT_LYCANROC.value:
            self.formName = "Midnight"
            self.normalForm = False
            self.lookupNum = 745
            self.name = "Lougaroc"
            self.formIndex = 1
            image_suffix = "-m"
        elif num == AddedPokes.DUSK_LYCANROC.value:
            self.formName = "Dusk"
            self.normalForm = False
            self.lookupNum = 745
            self.name = "Lugarugan"
            self.formIndex = 2
            image_suffix = "-d"
        
        if self.isAlolan:
            self.formName = "Alola"
            self.normalForm = False
            self.typeFormName = "Alolan"
            self.formIndex = 1
            image_suffix = "-a"
        
        # Basculin, Meowstic, Magearna (fucking Soul-Heart has a dash)
        self.useAbilitiesList = num in [550, 678, 801]
        
        if self.isMega:
            self.megaName = "Mega Evolution" + mega_suffix
            if base_exp_suffix is None:
                base_exp_suffix = "M"
            if image_suffix is None:
                image_suffix = "-m"
        else:
            assert not self.useMegaStats
            assert mega_suffix == ""
            
        if base_exp_suffix is None:
            base_exp_suffix = ""
        if image_suffix is None:
            image_suffix = ""
            
        self.baseExpName = str(self.lookupNum).zfill(3) + base_exp_suffix
        self.formImageName = str(self.lookupNum).zfill(3) + image_suffix
        self.pokedexImageName = str(self.lookupNum) + image_suffix
        
        if self.evFormName is None:
            self.evFormName = self.formName
        if self.typeFormName is None:
            self.typeFormName = self.formName

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
        if image_name.endswith('/' + self.formImageName + '.png'):
            return True
