# -*- coding: latin-1 -*-

import math
import re
import requests
import time
from lxml import html

from forms import Stat, AddedPokes, FormConfig
from parser import Parser
from substitutions import attackSubstitution, abilitySubstitution, typeSubstitution
from util import namesies, removePrefix, removeEmpty, indexSwap, getTypes, normalizeForm, replaceSpecial, dashy


def getBaseExpMap():
    page = requests.get('https://bulbapedia.bulbagarden.net/wiki/List_of_Pok%C3%A9mon_by_effort_value_yield')
    tree = html.fromstring(page.text)

    table = tree.xpath('//*[@id="mw-content-text"]/table[1]/tr')
    baseExpMap = {}
    for i, row in enumerate(table):
        # Schema
        if i == 0:
            continue

        num = row[0].text_content().strip()
        baseExp = int(row[3].text_content().replace("*", "").strip())
        
        # First one is likely the base form
        if num in baseExpMap:
            continue
        
        baseExpMap[num] = baseExp
    
    return baseExpMap

with open ("../temp.txt", "w") as f:
    startTime = time.time()
    
    baseExpMap = getBaseExpMap()    
    for num in range(1, list(AddedPokes)[-1].value + 1):
#    for num in [1]:
        formConfig = FormConfig(num)
        parser = Parser(formConfig.lookupNum)
        
        infoIndex = 2
        if formConfig.isMega:
            # First row is Mega Evolution title
            infoIndex += 1
            assert parser.updateTable(formConfig.megaName)
            
        # Picture, Name, Other Names, No., Gender Ratio, Type
        row = parser.infoTable.xpath('tr[' + str(infoIndex) +  ']')[0]
            
        formConfig.lookupName = row.xpath('td[2]')[0].text
        if formConfig.name is None:
            formConfig.name = formConfig.lookupName
        name = formConfig.name
        print("#" + str(num).zfill(3) + " Name: " + name)

        maleRatio = row.xpath('td[5]')[0]

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

        typesCell = row.xpath('td[6]')[0]
        types = typesCell.xpath('a/img')
        if len(types) > 0:
            types = getTypes(types)
        # Multiple forms
        else:
            types = None
            forms = typesCell.xpath('table[1]/tr')
            for form in forms:
                typeFormName = normalizeForm(form[0].text)
                if typeFormName == formConfig.typeFormName:
                    types = getTypes(form[1].xpath('a/img'))
                    break
                    
        types = typeSubstitution(num, types)
        type1 = types[0]
        type2 = types[1]
                
        print("Type1: " + type1)
        print("Type2: " + type2)
        
        # Next row of the info table (skip two for the schema of the row)
        # Classification, Height, Weight, Capture Rate, Base Egg Steps
        infoIndex += 2
        row = parser.infoTable.xpath('tr[' + str(infoIndex) +  ']')[0]

        # Hoopa apparently has a different classification for its different forms
        if num == 720:
            classification = 'Mischief'
        # Remove the Pokemon text from the end of classification
        else:
            classification = row.xpath('td[1]')[0].text[:-8]
        print("Classification: " + classification)

        # Height is specified in ft'in'' format -- convert to inches
        height = row.xpath('td[2]')[0].text
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
        weight = row.xpath('td[3]')[0].text
        weight = weight.split("/")
        weightIndex = formConfig.formIndex
        if len(weight) <= weightIndex:
            weightIndex = 0
        weight = weight[weightIndex].strip()
        weight = weight[:-3].strip()
        weight = float(weight)
        print("Weight: " + str(weight))

        # Minior apparently has different catch rates for its different forms
        if num == 774:
            captureRate = 30
        else:
            captureRate = int(row.xpath('td[4]')[0].text)
        print("Capture Rate: " + str(captureRate))

        eggSteps = row.xpath('td[5]')[0].text.replace(",", "").strip()
        if eggSteps == "":
            # Apparently this is a pretty universal base egg step value for legendaries/Pokemon that cannot breed...?
            eggSteps = 30720            
        eggSteps = int(eggSteps)
        print("Egg Steps: " + str(eggSteps))

        if formConfig.isMega and not formConfig.useMegaAbilities:
            parser.restoreBackup()

        assert parser.updateTable('Abilities')
        ability1 = None
        ability2 = None
        if formConfig.useAbilitiesList:
            abilities = parser.infoTable.xpath('tr[2]/td/a/b')
            ability1 = abilities[0].text
    
            if len(abilities) >= 2:
                ability2 = abilities[1].text
            else:
                ability2 = "No_Ability"
        else:
            allAbilities = parser.infoTable.xpath('tr[1]/td')[0].text_content()
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

        # Next table -- the one with the abilities and such
        parser.restoreBackup()
        parser.nextTable()
        
        # Experience Growth, Base Happiness, Effort Values Earned, S.O.S. Calling
        row = parser.infoTable.xpath('tr[4]')[0]
    
        growthRate = list(row.xpath('td[1]')[0].itertext())[1]
        print("Growth Rate: " + growthRate)

        evStrings = row.xpath('td[3]')[0].itertext()
        
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

        if formConfig.evFormName is None:
            evs = evMap[defaultForm]
        if formConfig.evFormName not in evMap:
            assert formConfig.normalForm or len(evMap) == 1
            evs = evMap[defaultForm]
        else:
            evs = evMap[formConfig.evFormName]
            
        # Swap Attack and Sp. Attack for Rizardon
        if num == AddedPokes.MEGA_CHARIZARD.value:
            indexSwap(evs, Stat.ATTACK.value, Stat.SP_ATTACK.value)

        # Add diffs
        evs = [sum(x) for x in zip(evs, formConfig.evDiffs)]
        
        print("Effort Values: " + str(evs))
        
        # Egg Group table
        parser.nextTable()
        parser.nextTable()
        
        eggGroup = parser.infoTable.xpath('tr[2]/td[2]')[0]
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

        if parser.updateTable('Flavor Text'):
            flavorText = parser.infoTable.xpath('tr[2]/td[2]')[0].text
            if flavorText == 'Sun':
                flavorText = parser.infoTable.xpath('tr[2]/td[3]')[0].text
            if flavorText is None:
                # infoTable.xpath('td[3]')[0].text == 'Ultra Sun' for this case
                flavorText = parser.infoTable.xpath('td[4]')[0].text
        else:
            flavorText = 'None'
        
        # Replace the special e character in the flavor text
        flavorText = replaceSpecial(flavorText)
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
        
        assert parser.updateTable(*levelUpTables)
        attacks = []
        for i in range(2, len(parser.infoTable) - 1, 2):
            level = parser.infoTable[i][0].text

            if level == 'Evolve':
                level = -1
            elif level == dashy:
                level = 0
                
            attack = parser.infoTable[i][1][0].text
            attack = attackSubstitution(num, attack)
            if attack is None:
                assert level == 0
                continue
            
            attacks.append(str(level) + " " + namesies(attack))
            print(str(int(level)) + " " + attack)

        print("TMS:")
        tms = []
        if parser.updateTable('TM & HM Attacks'):    
            schema = parser.infoTable[1]
            attackIndex = parser.getSchemaIndex(schema, "Attack Name")
            formIndex = parser.getSchemaIndex(schema, "Form")
            
            for i in range(2, len(parser.infoTable) - 1, 2):
                row = parser.infoTable[i]
                
                attack = row[attackIndex][0].text
                if attack in ["Frustration", "Return", "Quash"]:
                    continue
                
                if not formConfig.hasForm(row, formIndex):
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
        if parser.updateTable('Egg Moves '):
            schema = parser.infoTable[1]
            attackIndex = parser.getSchemaIndex(schema, "Attack Name")
            
            for i in range(2, len(parser.infoTable) - 1, 2):
                row = parser.infoTable[i]
                
                attack = row[attackIndex][0].text
                if attack == "Ion Deluge":
                    attack = "Electrify"
                elif attack in ["Helping Hand", "Ally Switch", "After You", "Wide Guard", "Quash", "Rage Powder", "Follow Me", "Spotlight"]:
                    continue
                    
                # This column does not have a name in the schema
                # It is always present since it additionally contains the details
                # For Pokemon with multiple forms, these will additionally be included here
                detailsCol = row[-1]
                if not formConfig.hasFormFromTable(detailsCol):
                    continue
                
                eggMoves.append(attack)
                print(attack)

        print("Move Tutor Moves:")
        tutorMoves = []
        if parser.updateTable('Move Tutor Attacks'):
            table = parser.infoTable.xpath('thead/tr')
            
            schema = table[1]
            attackIndex = parser.getSchemaIndex(schema, "Attack Name")
            formIndex = parser.getSchemaIndex(schema, "Form")
            
            for i in range(2, len(table) - 1, 2):
                row = table[i]
                
                attack = row[attackIndex][0].text
                if attack in ["Helping Hand", "After You", "Ally Switch"]:
                    continue
                
                if not formConfig.hasForm(row, formIndex):
                    continue
                
                tutorMoves.append(attack)
                print(attack)
        if parser.updateTable('Ultra Sun/Ultra Moon Move Tutor Attacks'):
            table = parser.infoTable.xpath('thead/tr')
            
            schema = table[1]
            attackIndex = parser.getSchemaIndex(schema, "Attack Name")
            formIndex = parser.getSchemaIndex(schema, "Form")
            
            for i in range(2, len(table) - 1, 2):
                row = table[i]
                
                attack = row[attackIndex][0].text
                if attack in ["Helping Hand", "After You", "Ally Switch"]:
                    continue
                
                if not formConfig.hasForm(row, formIndex):
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
        if formConfig.useMegaStats:
            # Not sure this will work for all cases -- particularly for multiple megas
            statsTables = ["Stats - Mega Evolution"]
        elif formConfig.isAlolan:
            statsTables = ["Stats - Alolan " + formConfig.lookupName]
        elif not formConfig.normalForm:
            statsTables = ["Stats - " + formConfig.formName + " Form", "Stats"]
        else:
            statsTables = ["Stats"]
        assert parser.updateTable(*statsTables)
        
        stats = [0]*6
        for i in range(0, len(stats)):
            stats[i] = int(parser.infoTable.xpath('tr[3]/td[' + str(2 + i) + ']')[0].text)

        # Decrease Absol's attack since it has an evolution now
        if num == 359:
            stats[Stat.ATTACK.value] -= 30
        # Use Charizard's stats with modifications
        if num == AddedPokes.MEGA_CHARIZARD.value:
            indexSwap(stats, Stat.ATTACK.value, Stat.SP_ATTACK.value)
            indexSwap(stats, Stat.DEFENSE.value, Stat.SP_DEFENSE.value)
            stats[Stat.ATTACK.value] += 10
            stats[Stat.SPEED.value] -= 10
        # Use Absol's stats with increase speed
        if num == AddedPokes.MEGA_ABSOL.value:
            stats[Stat.SPEED.value] += 20
        # Decrease mega attack stats
        if num == AddedPokes.MEGA_BANNETTE.value:
            stats[Stat.ATTACK.value] -= 35
            stats[Stat.SP_ATTACK.value] -= 10
            
        print("Stats: " + str(stats))

        baseExp = baseExpMap[formConfig.baseExpName]
        if formConfig.isAlolan and formConfig.baseExpName + "A" in baseExpMap:
            baseExp = baseExpMap[formConfig.baseExpName + "A"]
        
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
