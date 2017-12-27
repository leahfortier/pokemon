#!/usr/bin/env python3
# -*- coding: utf-8 -*-

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
    
class FormConfig:
    def __init__(self, num):
        self.formName = None
        self.normalForm = True
        self.lookupNum = num
        self.name = None
        self.formIndex = 0
        self.isMega = False
        imageSuffix = ""
        baseExpSuffix = ""
        megaSuffix = ""
        
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
        # Kyurem, Greninja, Zygarde, Rockruff
        elif num in [646, 658, 718, 744]:
            self.formName = "Standard"
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
        # Tornadus/Thundurus/Landorus
        elif num in [641, 642, 645]:
            self.formName = "Incarnate"
        # Meloetta
        elif num == 648:
            self.formName = "Aria"
        # Hoopa
        elif num == 720:
            self.formName = "Hoopa Confined"
        # Stupid dancing bird
        elif num == 741:
            self.formName = "Baile Style"
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
            megaSuffix = " X"
        elif num == AddedPokes.MEGA_MAWILE.value:
            self.lookupNum = 303
            self.isMega = True
            self.name = "Kuchiito"
        elif num == AddedPokes.MEGA_ABSOL.value:
            self.lookupNum = 359
            self.isMega = True
            self.name = "Asbel"
        elif num == AddedPokes.DUSK_LYCANROC.value:
            self.formName = "Dusk"
            self.normalForm = False
            self.lookupNum = 745
            self.name = "Lugarugan"
            self.formIndex = 2
            imageSuffix = "-d"
        
        # Fucking special cases because Serebii can be super inconsistent
        self.evFormName = self.formName
        # Darminatan
        if num == 555:
            self.evFormName = "Standard"
        # Kyurem
        elif num == 646:
            self.evFormName = "Kyurem"
        
        # Basculin, Meowstic, Magearna (fucking Soul-Heart has a dash)
        self.useAbilitiesList = num in [550, 678, 801]
        
        self.formImageName = str(self.lookupNum).zfill(3) + imageSuffix
        self.baseExpName = str(self.lookupNum).zfill(3) + baseExpSuffix
        
        if self.isMega:
            self.megaName = "Mega Evolution" + megaSuffix
        else:
            assert megaSuffix == ""
        
    def hasForm(self, row, formIndex):
        # No form index implies there is only the normal form or all forms are treated the same
        if formIndex is None:
            return True
        
        for form in row[formIndex][0][0].getchildren():
            if self.checkForm(form[0]):
                return True
            
        return False
    
    def hasFormFromTable(self, table):
        hasImage = False
        for form in table.getchildren():
            if form.tag != "img":
                continue
            
            hasImage = True
            if self.checkForm(form):
                return True
    
        # If you didn't find any image tags, then there are not multiple forms
        # So the only form is the normal form        
        return not hasImage
    
    def checkForm(self, form):
        imageName = form.attrib["src"]
        if imageName.endswith('/' + self.formImageName + '.png'):
            return True