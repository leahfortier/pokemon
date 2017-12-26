#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from types import SimpleNamespace
from enum import Enum, auto

# Original Pokes require an enum since their number is subject to change
class AddedPokes(Enum):
    MEGA_CHARIZARD = 803
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
    
def getFormConfig(num):
    formConfig = SimpleNamespace()
    
    formConfig.formName = None
    formConfig.normalForm = True
    formConfig.lookupNum = num
    formConfig.name = None
    formConfig.formIndex = 0
    imageSuffix = ""
    
    # Pokemon with Alolan forms
    if num in [19, 20, 26, 27, 28, 37, 38, 50, 51, 52, 53, 74, 75, 76, 88, 89, 103, 105]:
        formConfig.formName = "Normal"
    # Kyurem, Greninja, Zygarde, Rockruff
    elif num in [646, 658, 718, 744]:
        formConfig.formName = "Standard"
    # Deoxys
    elif num == 386:
        formConfig.formName = "Normal"
    # Wormadam is stupid
    elif num == 413:
        formConfig.formName = "Plant Cloak"
    # Rotom
    elif num == 479:
        formConfig.formName = "Rotom"
    # Giratina
    elif num == 487:
        formConfig.formName = "Altered"
    # Shaymin
    elif num == 492:
        formConfig.formName = "Land"
    # Darmanitan
    elif num == 555:
        formConfig.formName = "Normal"
    # Tornadus/Thundurus/Landorus
    elif num in [641, 642, 645]:
        formConfig.formName = "Incarnate"
    # Meloetta
    elif num == 648:
        formConfig.formName = "Aria"
    # Hoopa
    elif num == 720:
        formConfig.formName = "Hoopa Confined"
    # Stupid dancing bird
    elif num == 741:
        formConfig.formName = "Baile Style"
    # Lycanroc
    elif num == 745:
        formConfig.formName = "Midday"
    # Necrozma
    elif num == 800:
        formConfig.formName = "Normal"
        
    elif num == AddedPokes.DUSK_LYCANROC.value:
        formConfig.formName = "Dusk"
        formConfig.normalForm = False
        formConfig.lookupNum = 745
        formConfig.name = "Lugarugan"
        formConfig.formIndex = 2
        imageSuffix = "-d"
    
    # Fucking special cases because Serebii can be super inconsistent
    formConfig.evFormName = formConfig.formName
    # Darminatan
    if num == 555:
        formConfig.evFormName = "Standard"
    # Kyurem
    elif num == 646:
        formConfig.evFormName = "Kyurem"
    
    # Basculin, Meowstic, Magearna (fucking Soul-Heart has a dash)
    formConfig.useAbilitiesList = num in [550, 678, 801]
    
    formConfig.formImageName = str(formConfig.lookupNum).zfill(3) + imageSuffix
    
    return formConfig

