# -*- coding: latin-1 -*-

from lxml import html
import requests
import math
import re
import time
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

global infoTable

def attackSubstitution(num, attack):
    if attack is None:
        raise Exception()
    elif attack == 'After You':
        # Patrat/Watchog
        if num == 504 or num == 505:
            return 'Covet'
        # Lopunny
        elif num == 428:
            return 'Draining Kiss'
        # Petilil
        elif num == 548:
            return 'Heal Bell'
        # Minccino
        elif num == 572:
            return 'Iron Tail'
        # Togetic/Togekiss
        elif num == 176 or num == 468:
            return 'Moonblast'
        # Togepi
        elif num == 175:
            return 'Soft Boiled'
        # Buneary
        elif num == 427:
            return 'Sweet Kiss'
        # Clefairy and Audino
        elif num == 35 or num == 531:
            return 'Wish'
        # Oranguru
        elif num == 765:
            return 'Wonder Room'
        # Maractus
        elif num == 556:
            return 'Wood Hammer'
    elif attack == 'Ally Switch':
        # Kadabra/Alakazam
        if num == 64 or num == 65:
            return 'Barrier'
        # Hoopa
        elif num == 720:
            return 'Magic Room'
    elif attack == 'Follow Me':
        # Sentret/Furret
        if num == 161 or num == 162:
            return 'Covet'
        # Togepi/Togetic
        elif num == 175 or num == 176:
            return 'Draining Kiss'
        # Clefairy
        elif num == 35:
            return 'Mimic'
    elif attack == 'Frustration':
        # Buneary
        if num == 427:
            return 'Fake Tears'
    elif attack == 'Helping Hand':
        # Meowstic
        if num == 678:
            return 'Assist'
        # Jirachi
        elif num == 385:
            return 'Calm Mind'
        # Oricorio
        elif num == 741:
            return 'Captivate'
        # Sentret/Furret
        elif num == 161 or num == 162:
            return 'Charm'
        # Tyrogue and Minccino
        elif num == 236 or num == 572:
            return 'Covet'
        # Volbeat/Illumise
        elif num == 313 or num == 314:
            return 'Dizzy Punch'
        # Marill/Azumarill/Azurill
        elif num == 183 or num == 184 or num == 298:
            return 'Draining Kiss'
        # Gallade
        elif num == 475:
            return 'Dual Chop'
        # Petilil
        elif num == 548:
            return 'Fairy Wind'
        # Audino
        elif num == 531:
            return 'Heal Bell'
        # Growlithe and Lillipup line
        elif num == 58 or num >= 506 and num <= 508:
            return 'Howl'
        # Keldeo
        elif num == 647:
            return 'Icy Wind'
        # Cobalion
        elif num == 638:
            return 'Iron Defense'
        # Cinccino
        elif num == 573:
            return 'Iron Tail'
        # Magearna
        elif num == 801:
            return 'Light Screen'
        # Comfey
        elif num == 764:
            return 'Lucky Chant'
        # Leavanny
        elif num == 542:
            return 'Me First'
        # Alomomola
        elif num == 594:
            return 'Refresh'
        # Nidoran line
        elif num >= 29 and num <= 33:
            return 'Poison Tail'
        # Terrakion
        elif num == 639:
            return 'Stealth Rock'
        # Plusle/Minun
        elif num == 311 or num == 312:
            return 'Sweet Kiss'
        # Virizion
        elif num == 640:
            return 'Synthesis'
        # Latias/Latios and Cottonee
        elif num == 380 or num == 381 or num == 546:
            return 'Tailwind'
        # Cherubi/Cherrim and Pykumuku
        elif num == 420 or num == 421 or num == 771:
            return 'Tickle'
        # Wishiwashi
        elif num == 746:
            return 'Water Sport'
        # Eeveelutions all start with this
        elif num >= 133 and num <= 136 or num == 196 or num == 197 or num == 470 or num == 471 or num == 700:
            return None
    elif attack == 'Instruct':
        # Oranguru
        if num == 765:
            return 'Light Screen'
    elif attack == 'Quash':
        # Sableye
        if num == 302:
            return 'Night Slash'
        # Oranguru
        elif num == 765:
            return 'Psychic Terrain'
        # Murkrow/Honchkrow
        elif num == 198 or num == 430:
            return 'Roost'
    elif attack == 'Rage Powder':
        # Foongus/Amoonguss
        if num == 590 or num == 591:
            return 'Gastro Acid'
        # Paras/Parasect
        elif num == 46 or num == 47:
            return 'Leech Life'
        # Butterfree and Volcarona
        elif num == 12 or num == 637:
            return 'Morning Sun'
        # Hoppip line
        elif num >= 187 and num <= 189:
            return 'Silver Wind'
    elif attack == 'Return':
        # Lopunny
        if num == 428:
            return 'Captivate'
    elif attack == 'Spotlight':
        # Morelull/Shiinotic
        if num == 755 or num == 756:
            return 'Aromatherapy'
        # Starmie
        elif num == 121:
            return 'Cosmic Power'
        # Lanturn
        elif num == 171:
            return 'Soak'
        # Clefairy/Clefable
        elif num == 35 or num == 36:
            return 'Wish'
    elif attack == 'Wide Guard':
        # Mareanie/Toxapex
        if num == 747 or num == 748:
            return 'Acid Armor'
        # Throh
        elif num == 538:
            return 'Brick Break'
        # Mantine/Mantyke
        elif num == 226 or num == 458:
            return 'Dive'
        # Hitmontop
        elif num == 237:
            return 'Drill Run'
        # Mienshao
        elif num == 620:
            return 'Dual Chop'
        # Kingler
        elif num == 99:
            return 'Fury Cutter'
        # Tirtouga/Carracosta
        elif num == 564 or num == 565:
            return 'Iron Defense'
        # Lunala
        elif num == 792:
            return 'Light Screen'
        # Hitmonlee
        elif num == 106:
            return 'Low Kick'
        # Regigagas
        elif num == 486:
            return 'Mega Punch'
        # Avalugg
        elif num == 713:
            return 'Mist'
        # Solgaleo
        elif num == 791:
            return 'Reflect'
        # Alomomola
        elif num == 594:
            return 'Mist'
        # Gallade
        elif num == 475:
            return 'Sacred Sword'
        # Probopass
        elif num == 476:
            return 'Stealth Rock'
        # Araquanid
        elif num == 752:
            return 'Sticky Web'
        # Machamp
        elif num == 68:
            return 'Superpower'
        # Mr. Mime
        elif num == 122:
            return 'Teeter Dance'
        # Celesteela and Guzzlord -- TODO: too lazy to come up with an alternative right now
        elif num == 797 or num == 799:
            return None
    elif attack == 'Ion Deluge':
        # Replace for all Pokemon
        return 'Electrify'
    elif attack == 'Judgment':
        # Intentional spelling change -- applies to all obviously
        return 'Judgement'
    
    return attack

def abilitySubstitution(num, ability):
    if ability == 'Battery':
        # Charjabug
        if num == 737:
            return 'Static'
    elif ability == 'Early Bird':
        # Natu/Xatu -- I just love this ability and I want more Pokemon to have it
        if num == 177 or num == 178:
            return 'Magic Bounce'
    elif ability == 'Friend Guard':
        # Spewpa
        if num == 665:
            return 'No_Ability'
    elif ability == 'Illuminate':
        # Staryu/Starmie and Watchog
        if num == 120 or num == 121 or num == 505:
            return 'Analytic'
        # Volbeat
        elif num == 313:
            return 'Prankster'
        elif num == 755 or num == 756:
            return 'Rain Dish'
        # Chinchou/Lanturn
        elif num == 170 or num == 171:
            return 'Water Absorb'
    elif ability == 'Minus':
        # Klink line
        if num >= 599 and num <= 601:
            return 'No_Ability'
        # Minun
        elif num == 312:
            return 'Static'
    elif ability == 'Power Construct':
        # Zygarde -- this should always be true if inside this if
        if num == 718:
            return 'No_Ability'
    elif ability == 'Plus':
        # Klink line
        if num >= 599 and num <= 601:
            return 'Clear Body'
        # Mareep line
        elif num >= 179 and num <= 181:
            return 'No_Ability'
        # Plusle
        elif num == 311:
            return 'Static'
    elif ability == 'Receiver':
        # Passimian
        if num == 766:
            return 'No_Ability'
    elif ability == 'Run Away':
        # Ponyta/Rapidash should really have this ability
        if num == 77 or num == 78:
            return 'Flame Body'
    elif ability == 'Stall':
        # Sableye -- Prankster is way cooler
        if num == 302:
            return 'Prankster'
    elif ability == 'Symbiosis':
        # Flabebe line
        if num >= 669 and num <= 671:
            return 'Flower Gift'
    elif ability == 'Telepathy':
        # Elgyem/Beheeyem
        if num == 605 or num == 606:
            return 'Analytic'
        # Wobbuffet/Wynaut and Meditite/Medicham and Dialga/Palkia/Giratina 
        # and Oranguru and the Tapus
        elif num == 202 or num == 360 \
                or num == 307 or num == 308 \
                or num == 483 or num == 484 or num == 487 \
                or num == 765 \
                or num >= 785 and num <= 788:
            return 'No_Ability'
    elif ability == 'Zen Mode':
        if num == 555:
            return 'No_Ability'
    
    return ability

# My personal type changes
def typeSubstitution(num, types):
    # Psyduck/Golduck are now Psychic type
    if num == 54 or num == 55:
        assert types == ['Water', 'No_Type']
        return ['Water', 'Psychic']
    # Horsea/Seadra are now Dragon type
    elif num == 116 or num == 117:
        assert types == ['Water', 'No_Type']
        return ['Water', 'Dragon']
    # Gyarados is now Water/Dragon instead of Water/Flying
    elif num == 130:
        assert types == ['Water', 'Flying']
        return ['Water', 'Dragon']
    # Noctowl is now Psychic/Flying
    elif num == 164:
        assert types == ['Normal', 'Flying']
        return ['Psychic', 'Flying']
    # Luxray is now Dark type
    elif num == 405:
        assert types == ['Electric', 'No_Type']
        return ['Electric', 'Dark']
    # Flabebe line is now Grass type
    elif num >= 669 and num <= 671:
        assert types == ['Fairy', 'No_Type']
        return ['Fairy', 'Grass']
    # Goomy line is now Water type
    elif num >= 704 and num <= 706:
        assert types == ['Dragon', 'No_Type']
        return ['Dragon', 'Water']
    
    return types

def getFormConfig(num):
    formConfig = SimpleNamespace()
    
    formConfig.formName = None
    formConfig.normalForm = True
    formConfig.lookupNum = num
    formConfig.name = None
    formConfig.formIndex = 0
    
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
    
    formConfig.formImageName = str(formConfig.lookupNum).zfill(3)
    if not imageSuffix is None:
        formConfig.formImageName += imageSuffix
    
    return formConfig

def namesies(stringsies):
    stringsies = stringsies.strip().replace(' ', '_').replace('-', '_').replace('\'', '').upper()
    if stringsies == 'CONVERSION_2':
        stringsies = 'CONVERSION2'
    elif stringsies == 'RKS_SYSTEM':
        stringsies = 'RKSSYSTEM'
    return stringsies

def removePrefix(string, prefix):
    assert string.startswith(prefix)
    return string[len(prefix):]

# Listsies should be a list of strings 
# This will remove all empty and whitespace characters from the list and return it
def removeEmpty(listsies):
    temp = []
    for string in listsies:
        if string.strip() == "":
            temp.append(string)
    for empty in temp:
        listsies.remove(empty)

def getSchemaIndex(schema, columnName):
    for index, column in enumerate(schema.getchildren()):
        if column.text == columnName:
            return index
    
# types should be an array that points to img elements
def getTypes(types):
    type1 = types[0].attrib["src"]
    type1 = type1[type1.find("type") + 5 : -4].capitalize()

    # Check for a second type
    if len(types) == 2:
        type2 = types[1].attrib["src"]
        type2 = type2[type2.find("type") + 5 : -4].capitalize()
    else:
        type2 = "No_Type"
        
    return [type1, type2]
    
# Removes the form/forme suffix from the end
def normalizeForm(form):
    return re.sub(" Forme?$", "", form).strip()

def hasForm(row, formIndex, formConfig):
    # No form index implies there is only the normal form or all forms are treated the same
    if formIndex is None:
        return True
    
    for form in row[formIndex][0][0].getchildren():
        if checkForm(form[0], formConfig):
            return True
        
    return False

def hasFormFromTable(table, formConfig):
    hasImage = False
    for form in table.getchildren():
        if form.tag != "img":
            continue
        
        hasImage = True
        if checkForm(form, formConfig):
            return True

    # If you didn't find any image tags, then there are not multiple forms
    # So the only form is the normal form        
    return not hasImage

def checkForm(form, formConfig):
    imageName = form.attrib["src"]
    if imageName.endswith('/' + formConfig.formImageName + '.png'):
        return True

def getElementText(element):
    text = element.text
    if not text is None:
        return text
    return element.text_content()

def getQueryText(query):
    for querychild in query:
        text = getElementText(querychild)
        if not text is None:
            return text
        for child in querychild.getchildren():
            text = getElementText(child)
            if not text is None:
                return text

def checkQueries(*queries):
    global infoTable
    
    for queryString in queries:
        query = infoTable.xpath(queryString)
        text = getQueryText(query)
        if not text is None:
            return text

def checkHeader(header, headerIndex):
    global infoTable
    
    if infoTable.tag == 'table':
        text = checkQueries('tr[1]/td/b', 'tr[1]/td', 'thead/tr[1]/td')
        if not text is None and text[headerIndex:] == header:
            return True

    return False

def updateTable(*headers):
    for header in headers:
        if updateTableIndex(header, 0):
            return True
    return False

def updateTableIndex(header, headerIndex):
    global infoTable
    global num
    global index
    global mainDiv
    global tableCheck

    tempIndex = index
    tempTableCheck = tableCheck
    tempInfoTable = infoTable

    while True:
        if checkHeader(header, headerIndex):
            return True

        if num < 650:
            infoTable = infoTable.getnext()
                
            if infoTable == None:
                index += 1
                infoTable = mainDiv.xpath('p[' + str(index) + ']')

                if len(infoTable) == 0:
                    index = tempIndex;
                    infoTable = tempInfoTable
                    return False

                infoTable = infoTable[0].getnext()
        else:
            if tableCheck:                
                index += 1
                infoTable = mainDiv.xpath('table[' + str(index) + ']')

                if len(infoTable) == 0:
                    index = 1
                    tableCheck = False
                    infoTable = mainDiv.xpath('p[1]')[0].getnext()
                else:
                    infoTable = infoTable[0]
            else:
                infoTable = infoTable.getnext()

                if infoTable == None:
                    index += 1
                    infoTable = mainDiv.xpath('p[' + str(index) + ']')

                    if len(infoTable) == 0:
                        index = tempIndex;
                        tableCheck = tempTableCheck
                        infoTable = tempInfoTable
                        return False

                    infoTable = infoTable[0].getnext()

with open ("../temp.txt", "w") as f:
    startTime = time.time()
    
    for num in range(1, 802):
#    for num in [1]:
#    for num in [AddedPokes.DUSK_LYCANROC.value]:
        formConfig = getFormConfig(num)
        
        page = requests.get('http://www.serebii.net/pokedex-sm/' + str(formConfig.lookupNum).zfill(3) + '.shtml')
        tree = html.fromstring(page.text)
        mainDiv = tree.xpath('/html/body/table[2]/tr[2]/td[2]/font/div[2]/div')[0];

        tableCheck = True

        if num < 722:
            index = 1
            infoTable = mainDiv.xpath('p[1]')[0].getnext()
        else:
            index = 2
            infoTable = mainDiv.xpath('table[2]')[0]
            
        name = infoTable.xpath('tr[2]/td[2]')[0].text

        # Flabebe has a stupid name with stupid special characters
        if num == 669:
            name = "Flabebe"
        elif num == 29:
            name = "Nidoran F"
        elif num == 32:
            name = "Nidoran M"
            
        formConfig.lookupName = name
        if formConfig.name is None:
            formConfig.name = name
        name = formConfig.name

        print("Name: " + name)

        maleRatio = infoTable.xpath('tr[2]/td[5]')[0]

        # Genderless Pokemon
        if maleRatio.text != None:
            maleRatio = -1
        else:
            # Remove the % from the end and convert to float
            maleRatio = float(maleRatio.xpath('table/tr[1]/td[2]')[0].text[:-1])
            if maleRatio > 50:
                maleRatio = math.floor(maleRatio)
            else:
                maleRatio = math.ceil(maleRatio)

        # Silcoon/Beautifly, Gardevoir are 100% female now
        if num in [266, 267, 282]:
            assert maleRatio == 50
            maleRatio = 0
        # Cascoon/Dustox, Glalie are 100% male now
        elif num in [268, 269, 362]:
            assert maleRatio == 50
            maleRatio = 100
                                
        print("Male Ratio: " + str(maleRatio))

        types = infoTable.xpath('tr[2]/td[6]/a/img')
        if len(types) > 0:
            types = getTypes(types)
        # Multiple forms
        else:
            types = None
            forms = infoTable.xpath('tr[2]/td[6]/table[1]/tr')
            for form in forms:
                typeFormName = normalizeForm(form[0].text)
                if typeFormName == formConfig.formName:
                    types = getTypes(form[1].xpath('a/img'))
                    break
                    
        types = typeSubstitution(num, types)
        type1 = types[0]
        type2 = types[1]
                
        print("Type1: " + type1)
        print("Type2: " + type2)

        # Hoopa apparently has a different classification for its different forms
        if num == 720:
            classification = 'Mischief'
        # Remove the Pokemon text from the end of classification
        else:
            classification = infoTable.xpath('tr[4]/td[1]')[0].text[:-8]
        print("Classification: " + classification)

        # Height is specified in ft'in'' format -- convert to inches
        height = infoTable.xpath('tr[4]/td[2]')[0].text
        height = height.split("/")
        heightIndex = formConfig.formIndex
        if len(height) <= heightIndex:
            heightIndex = 0
        height = height[heightIndex].strip()
        height = height.split("'")
        assert len(height) == 2
        height = int(height[0])*12 + int(height[1].replace('"', ''))
        print("Height: " + str(height))

        # Remove the lbs from the end of weight
        weight = infoTable.xpath('tr[4]/td[3]')[0].text
        weight = weight.split("/")
        weightIndex = formConfig.formIndex
        if len(weight) <= weightIndex:
            weightIndex = 0
        weight = weight[weightIndex].replace(' ', '')
        weight = weight[:-3]
        weight = float(weight)
        print("Weight: " + str(weight))

        # Minior apparently has different catch rates for its different forms
        if num == 774:
            captureRate = 30
        else:
            captureRate = int(infoTable.xpath('tr[4]/td[4]')[0].text)

        print("Capture Rate: " + str(captureRate))

        eggSteps = infoTable.xpath('tr[4]/td[5]')[0].text.replace(",", "")
        eggSteps = eggSteps.strip()

        # Apparently this is a pretty universal base egg step value for legendaries/Pokemon that cannot breed...?
        if len(eggSteps) == 0:
            eggSteps = 30720

        print("Egg Steps: " + str(eggSteps))

        # Next table -- the one with the abilities and such
        if num < 722:
            infoTable = infoTable.getnext()
        else:
            index = 3
            infoTable = mainDiv.xpath('table[3]')[0]    
    
        growthRate = list(infoTable.xpath('tr[4]/td[1]')[0].itertext())[1]
        print("Growth Rate: " + growthRate)

        evStrings = infoTable.xpath('tr[4]/td[3]')[0].itertext()
        
        # If no form is specified, use this in the mapping
        defaultForm = "FormNotSpecified"
        form = defaultForm
        evMap = {}
        evMap[form] = [0]*6
        for evString in evStrings:
            evIndex = evString.find(" Point(s)")
            
            # String doesn't contain EV info -- new form name
            if evIndex == -1:
                form = normalizeForm(evString)
                assert not form in evMap
                evMap[form] = [0]*6
                continue
            
            ev = evString[:evIndex]
            evs = evMap[form]
            
            stat = ev[2:]
            value = int(ev[0])
            
            if stat == "HP":
                evs[0] = value
            elif stat == "Attack":
                evs[1] = value
            elif stat == "Defense":
                evs[2] = value
            elif stat == "Sp. Attack":
                evs[3] = value
            elif stat == "Sp. Defense":
                evs[4] = value
            elif stat == "Speed":
                evs[5] = value

        if formConfig.evFormName is None:
            evs = evMap[defaultForm]
        if formConfig.evFormName not in evMap:
            assert formConfig.normalForm or len(evMap) == 1
            evs = evMap[defaultForm]
        else:
            evs = evMap[formConfig.evFormName]
        
        print("Effort Values: " + str(evs))
        
        updateTable('Abilities')
        ability1 = None
        ability2 = None
        if formConfig.useAbilitiesList:
            abilities = infoTable.xpath('tr[2]/td/a/b')
            ability1 = abilities[0].text
    
            if len(abilities) >= 2:
                ability2 = abilities[1].text
            else:
                ability2 = "No_Ability"
        else:
            allAbilities = infoTable.xpath('tr[1]/td')[0].text_content()
            allAbilities = removePrefix(allAbilities, "Abilities: ")
            allAbilities = allAbilities.replace("(Hidden)", "")
            allAbilities = allAbilities.replace("(Hidden Ability)", "")
            allAbilities = re.split("\)", allAbilities)
            removeEmpty(allAbilities)
            for formAbilities in allAbilities:
                formIndex = formAbilities.rfind("(")
                if formIndex == -1:
                    # No form specified -- there should only be the normal form
                    assert len(allAbilities) == 1
                    assert formConfig.normalForm
                else:
                    assert len(allAbilities) > 1
                    form = formAbilities[formIndex + 1:].strip()
                    form = normalizeForm(form)
                    if formConfig.formName != form:
                        continue
                    formAbilities = formAbilities[:formIndex]
                formAbilities = formAbilities.strip()
                abilities = re.split("-", formAbilities)
                if abilities[0].strip() == "":
                    abilities = abilities[1:]
                ability1 = abilities[0].strip()
                if len(abilities) > 1:
                    ability2 = abilities[1].strip()
                else:
                    ability2 = "No_Ability"
                break
        assert not ability1 is None
        assert not ability2 is None
            
        ability1 = abilitySubstitution(num, ability1)
        ability2 = abilitySubstitution(num, ability2)
        if ability1 == 'No_Ability':
            tempAbility = ability1
            ability1 = ability2
            ability2 = tempAbility
            
        print("Ability1: " + ability1)    
        print("Ability2: " + ability2)

        # Egg Group table
        if num < 722:
            infoTable = infoTable.getnext().getnext()
        else:
#            updateTable('Egg Groups')
            index = 5
            infoTable = mainDiv.xpath('table[5]')[0]
#        if not updateTable('Egg Groups'):
#            print('FAIL')
        eggGroup = infoTable.xpath('tr[2]/td[2]')[0]
        if eggGroup.text != None:
            eggGroup1 = "Undiscovered"
            eggGroup2 = "None"
        else:
            eggGroup1 = eggGroup.xpath('table/tr[1]/td[2]/a')[0].text
            eggGroup2 = eggGroup.xpath('table/tr[2]/td[2]/a')

            if len(eggGroup2) == 0:
                eggGroup2 = "None"
            else:
                eggGroup2 = eggGroup2[0].text

        eggGroup1 = namesies(eggGroup1)
        eggGroup2 = namesies(eggGroup2)

        print("Egg Group1: " + eggGroup1)
        print("Egg Group2: " + eggGroup2)

        if updateTable('Flavor Text'):
            flavorText = infoTable.xpath('tr[2]/td[2]')[0].text
            if flavorText == 'Sun':
                flavorText = infoTable.xpath('tr[2]/td[3]')[0].text
            if flavorText is None:
                # infoTable.xpath('td[3]')[0].text == 'Ultra Sun' for this case
                flavorText = infoTable.xpath('td[4]')[0].text
        else:
            flavorText = 'None'
        
        # Replace the special e character in the flavor text
        poke = u'�'
        rightTick = u'\u2019'
        dashy = u'\u2014'
        leftQuote = u'\u201c'
        rightQuote = u'\u201d'
        flavorText = flavorText.replace(poke, "\u00e9").replace('  ', ' ').replace(rightTick, "'").replace(dashy, "--").replace(leftQuote, "\"").replace(rightQuote, "\"")
        print("Flavor Text: " + flavorText)

        print("Attacks:")
        if formConfig.normalForm:
            levelUpTables = ['Ultra Sun/Ultra Moon Level Up', 
                             'Ultra Sun / Ultra Moon Level Up', 
                             'Sun/Moon Level Up', 
                             'Sun / Moon Level Up', 
                             'Standard Level Up',
                             'Generation VII Level Up']
        else:
            suffix = " - " + formConfig.formName + " Form"
            levelUpTables = ['Ultra Sun/Ultra Moon Level Up' + suffix,
                             'Ultra Sun / Ultra Moon Level Up' + suffix,
                             'Sun/Moon Level Up' + suffix,
                             'Sun / Moon Level Up' + suffix,
                             formConfig.formName + " Form Level Up"]
        
        if updateTable(*levelUpTables):
            attacks = []
            for i in range(2, len(infoTable) - 1, 2):
                level = infoTable[i][0].text

                if level == 'Evolve':
                    level = -1
                elif level == dashy:
                    level = 0
                    
                attack = infoTable[i][1][0].text
                attack = attackSubstitution(num, attack)
                if attack is None:
                    assert level == 0
                    continue
                
                attacks.append(str(level) + " " + namesies(attack))
                print(str(int(level)) + " " + attack)

        print("TMS:")
        tms = []
        if updateTable('TM & HM Attacks'):    
            schema = infoTable[1]
            attackIndex = getSchemaIndex(schema, "Attack Name")
            formIndex = getSchemaIndex(schema, "Form")
            
            for i in range(2, len(infoTable) - 1, 2):
                row = infoTable[i]
                
                attack = row[attackIndex][0].text
                if attack in ["Frustration", "Return", "Quash"]:
                    continue
                
                if not hasForm(row, formIndex, formConfig):
                    continue
                
                tms.append(attack)
                print(attack)
        # Manually add Fly for:
        # Butterfree, Beedrill, Venomoth, Scyther, Dragonair, Ledyba line, 
        # Natu, Yanma, Gligar, Beautifly, Dustox, Masquerain, Ninjask, 
        # Shedinja, Volbeat, Illumise, Mothim, Vespiquen, Garchomp, Yanmega, 
        # Gliscor, Emolga, Vivillon, Rowlet line, Vikavolt, Cutiefly line
        if num in [12, 15, 49, 123, 148, 165, 166, 
                   177, 193, 207, 267, 269, 284, 291, 
                   292, 313, 314, 414, 416, 445, 469, 
                   472, 587, 666, 722, 723, 724, 738, 742, 743]:
            attack = "Fly"
            tms.append(attack)
            print(attack)

        print("Egg Moves:")
        eggMoves = []
        if updateTable('Egg Moves '):
            schema = infoTable[1]
            attackIndex = getSchemaIndex(schema, "Attack Name")
            
            for i in range(2, len(infoTable) - 1, 2):
                row = infoTable[i]
                
                attack = row[attackIndex][0].text
                if attack == "Ion Deluge":
                    attack = "Electrify"
                elif attack in ["Helping Hand", "Ally Switch", "After You", "Wide Guard", "Quash", "Rage Powder", "Follow Me", "Spotlight"]:
                    continue
                    
                # This column does not have a name in the schema
                # It is always present since it additionally contains the details
                # For Pokemon with multiple forms, these will additionally be included here
                detailsCol = row[-1]
                if not hasFormFromTable(detailsCol, formConfig):
                    continue
                
                eggMoves.append(attack)
                print(attack)

        print("Move Tutor Moves:")
        tutorMoves = []
        if updateTable('Move Tutor Attacks'):
            table = infoTable.xpath('thead/tr')
            
            schema = table[1]
            attackIndex = getSchemaIndex(schema, "Attack Name")
            formIndex = getSchemaIndex(schema, "Form")
            
            for i in range(2, len(table) - 1, 2):
                row = table[i]
                
                attack = row[attackIndex][0].text
                if attack in ["Helping Hand", "After You", "Ally Switch"]:
                    continue
                
                if not hasForm(row, formIndex, formConfig):
                    continue
                
                tutorMoves.append(attack)
                print(attack)
        if updateTable('Ultra Sun/Ultra Moon Move Tutor Attacks'):
            table = infoTable.xpath('thead/tr')
            
            schema = table[1]
            attackIndex = getSchemaIndex(schema, "Attack Name")
            formIndex = getSchemaIndex(schema, "Form")
            
            for i in range(2, len(table) - 1, 2):
                row = table[i]
                
                attack = row[attackIndex][0].text
                if attack in ["Helping Hand", "After You", "Ally Switch"]:
                    continue
                
                if not hasForm(row, formIndex, formConfig):
                    continue
                
                tutorMoves.append(attack)
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
        stats = [0]*6
        statsTables = []
        if not formConfig.normalForm:
            statsTables.append("Stats - " + formConfig.formName + " Form")
        statsTables.append("Stats")
        updateTable(*statsTables)
        for i in range(0, len(stats)):
            stats[i] = int(infoTable.xpath('tr[3]/td[' + str(2 + i) + ']')[0].text)
        print("Stats: " + str(stats))

        # Diancie's base experience is currently unknown
        baseExp = -1
        if num == 719:
            baseExp = 270
        elif num == 29 or num == 32:
            baseExp = 55
        else:
            page = requests.get('http://bulbapedia.bulbagarden.net/wiki/' + formConfig.lookupName)
#            //*[@id="mw-content-text"]/table[2]/tbody/tr[9]/td[1]/table/tbody/tr/td[2]
            tree = html.fromstring(page.text)

            index = 9

            # Pokemon that do not have hidden abilities have a stupid motherfucking format and I'm really fucking pissed
#            if num == 676 or num == 679 or num == 680 or num == 681 or num == 692 or num == 693 or num == 716 or num == 717 or num == 718:
#                index = 8
            
#            baseExp = int(tree.xpath('//*[@id="mw-content-text"]/table[3]/tr[' + str(index) + ']/td[1]/table/tr/td[3]')[0].text)
            baseExp = int(tree.xpath('//*[@id="mw-content-text"]/table[2]/tr[' + str(index) + ']/td[1]/table/tr/td[3]')[0].text)
            if baseExp < 4:
                index = 8
                baseExp = int(tree.xpath('//*[@id="mw-content-text"]/table[2]/tr[' + str(index) + ']/td[1]/table/tr/td[3]')[0].text)
        print("Base EXP: " + str(baseExp))

        f.write(str(num) + '\n')
        f.write(str(name) + '\n')

        stats = [str(stat) for stat in stats]
        f.write(' '.join(stats) + '\n')

        if len(str(baseExp)) == 1:
            f.write("BASE EXP: ")

        f.write(str(baseExp) + '\n')
        f.write(namesies(growthRate) + '\n')
        f.write(namesies(type1) + ' ')
        f.write(namesies(type2) + '\n')
        
        f.write(str(captureRate) + '\n')

        evs = [str(ev) for ev in evs]
        f.write(' '.join(evs) + '\n')

        # TODO: Evolutions
        f.write('NONE\n')
        
        # TODO: Wild Hold Items
        f.write('0\n')

        f.write(str(maleRatio) + '\n')
        f.write(namesies(ability1) + ' ')
        f.write(namesies(ability2) + '\n')
        f.write(str(classification) + '\n')
        f.write(str(height) + ' ')
        f.write(str(weight) + ' ')
        f.write(str(flavorText) + '\n')
        f.write(str(eggSteps) + '\n')
        f.write(str(eggGroup1) + ' ')
        f.write(str(eggGroup2) + '\n')        
        
        f.write(str(len(attacks)) + '\n')
        for attack in attacks:
            f.write(attack + '\n')

        f.write(str(len(tms) + len(eggMoves) + len(tutorMoves)) + '\n')
        for attack in tms:
            f.write(namesies(attack) + '\n')
        for attack in eggMoves:
            f.write(namesies(attack) + '\n')
        for attack in tutorMoves:
            f.write(namesies(attack) + '\n')

        f.write('\n')
        
    endTime = time.time()
    totalSeconds = int(endTime - startTime)
    minutes = totalSeconds // 60
    seconds = totalSeconds % 60
    print(str(minutes) + " Minutes, " + str(seconds) + " Seconds")
