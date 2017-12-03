# -*- coding: latin-1 -*-

from lxml import html
import requests

global infoTable

def namesies(stringsies):
    return stringsies.replace(' ', '_').replace('-', '_').upper()

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

with open ("temp.txt", "w") as f:
    for num in range(1, 802):
#    for num in [1]:
        page = requests.get('http://www.serebii.net/pokedex-sm/' + str(num).zfill(3) + '.shtml')
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

        print("Name: " + name)

        maleRatio = infoTable.xpath('tr[2]/td[5]')[0]

        # Genderless Pokemon
        if maleRatio.text != None:
            maleRatio = -1
        else:
            maleRatio = int(float(maleRatio.xpath('table/tr[1]/td[2]')[0].text[:-1]))                                 
        print("Male Ratio: " + str(maleRatio))

        # Wormadam is stupid
        if num == 413:
            type1 = 'Bug'
            type2 = 'Grass'
        # Rotom
        elif num == 479:
            type1 = 'Electric'
            type2 = 'Ghost'
        # Shaymin
        elif num == 492:
            type1 = 'Grass'
            type2 = 'No_Type'
        # Darmanitan, Vulpix, Ninetales
        elif num == 555 or num == 37 or num == 38:
            type1 = 'Fire'
            type2 = 'No_Type'
        # GROSS SHIT and Meowth and Persian
        elif num == 19 or num == 20 or num == 52 or num == 53:
            type1 = 'Normal'
            type2 = 'No_Type'
        # Raichu
        elif num == 26:
            type1 = 'Electric'
            type2 = 'No_Type'
        # Sandshrew and Sandslash and Diglett and Dugtrio and Marowak
        elif num == 27 or num == 28 or num == 50 or num == 51 or num == 105:
            type1 = 'Ground'
            type2 = 'No_Type'
        # Geodude and Graveler and Golem
        elif num == 74 or num == 75 or num == 76:
            type1 = 'Rock'
            type2 = 'Ground'
        # Grimer and Muk
        elif num == 88 or num == 89:
            type1 = 'Poison'
            type2 = 'No_Type'
        # Exeggutor
        elif num == 103:
            type1 = 'Grass'
            type2 = 'Psychic'
        # Meloetta
        elif num == 648:
            type1 = 'Normal'
            type2 = 'Psychic'
        # Stupid dancing bird
        elif num == 741:
            type1 = 'Fire'
            type2 = 'Flying'
        # Hoopa
        elif num == 720:
            type1 = 'Psychic'
            type2 = 'Ghost'
        # Necrozma
        elif num == 800:
            type1 = 'Psychic'
            type2 = 'No_Type'
        else:
            types = infoTable.xpath('tr[2]/td[6]/a/img')
            type1 = types[0].attrib["src"]
            type1 = type1[type1.find("type") + 5 : -4].capitalize()

            # Check for a second type
            if len(types) == 2:
                type2 = types[1].attrib["src"]
                type2 = type2[type2.find("type") + 5 : -4].capitalize()
            else:
                type2 = "No_Type"
                
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
        height = height.split("/")[0]
        height = height.split("'")
        height = int(height[0])*12 + int(height[1].replace('"', ''))
        print("Height: " + str(height))

        # Remove the lbs from the end of weight
        weight = infoTable.xpath('tr[4]/td[3]')[0].text
        weight = weight.split("/")[0].replace(' ', '')
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
            
    
        growthRate = infoTable.xpath('tr[4]/td[1]')[0].text_content()
        growthRate = growthRate[growthRate.find("Points") + 6 : ]
        print("Growth Rate: " + growthRate)

        # Not in the mood to deal with Deoxys's multiple forms and effort yields
        # Attack, Sp. Attack, and Speed
        if num == 386:
            evs = [0, 1, 0, 1, 0, 1]
        # Same with Wormadam
        # 2 Sp. Def
        elif num == 413:
            evs = [0, 0, 0, 0, 2, 0]
        # And Shaymin
        # 3 HP
        elif num == 492:
            evs = [3, 0, 0, 0, 0, 0]
        # And Darmanitan
        # 2 Attack
        elif num == 555:
            evs = [0, 2, 0, 0, 0, 0]
        # And Tornadus and Thundurus and Landorus Note: Landorus actually has sp.attack but that's stupid its attack is higher
        # 3 Attack
        elif num == 641 or num == 642 or num == 645:
            evs = [0, 3, 0, 0, 0, 0]
        # And Kyurem
        # HP, Attack, and Sp. Attack
        elif num == 646:
            evs = [1, 1, 0, 1, 0, 0]
        # And Meloetta
        # Sp Attack, Sp. Defense, and Speed
        elif num == 648:
            evs = [0, 0, 0, 1, 1, 1]
        else:
            evs = [0]*6
            evString = infoTable.xpath('tr[4]/td[3]')[0].text_content()

            while True:
                evIndex = evString.find("Point(s)")
                if evIndex == -1:
                    break
                
                ev = evString[: evIndex - 1]
                evString = evString[evIndex + 8 :]
                
                if "Alola Form" in ev:
                    break
                
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

        print("Effort Values: " + str(evs))
        updateTable('Abilities')
        abilities = infoTable.xpath('tr[2]/td/a/b')
        ability1 = abilities[0].text

        if len(abilities) >= 2:
            ability2 = abilities[1].text
        else:
            ability2 = "No_Ability"
            
        print("Ability1: " + ability1)    
        print("Ability2: " + ability2)

        # Egg Group table
        if num < 650:
            infoTable = infoTable.getnext().getnext()
        else:
#            updateTable('Egg Groups')
            index = 5
            infoTable = mainDiv.xpath('table[5]')[0]
#        if not updateTable('Egg Groups'):
#            print('FAIL')
        eggGroup = infoTable.xpath('tr[2]/td[2]')[0]
        if eggGroup.text != None:
            eggGroup1 = "None"
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
        poke = u'é'
        rightTick = u'\u2019'
        dashy = u'\u2014'
        leftQuote = u'\u201c'
        rightQuote = u'\u201d'
        flavorText = flavorText.replace(poke, "\u00e9").replace('  ', ' ').replace(rightTick, "'").replace(dashy, "--").replace(leftQuote, "\"").replace(rightQuote, "\"")
        print("Flavor Text: " + flavorText)

        print("Attacks:")
        if updateTable('Ultra Sun/Ultra Moon Level Up', 
                       'Sun/Moon Level Up', 
                       'Standard Level Up',
                       'Generation VII Level Up'):
            attacks = []
            for i in range(2, len(infoTable) - 1, 2):
                level = infoTable[i][0].text

                if level == 'Evolve':
                    level = -1
                elif level == dashy:
                    level = 0
                    
                attack = infoTable[i][1][0].text
                attacks.append(str(level) + " " + namesies(attack))
                print(str(int(level)) + " " + attack)

        print("TMS:")
        tms = []
        if updateTable('TM & HM Attacks'):        
            for i in range(2, len(infoTable) - 1, 2):
                attack = infoTable[i][1][0].text

                if attack == "Frustration" or attack == "Return" or attack == "Quash":
                    continue
                
                tms.append(attack)
                print(attack)

        print("Egg Moves:")
        eggMoves = []
        if updateTable('Egg Moves '):
            for i in range(2, len(infoTable) - 1, 2):
                attack = infoTable[i][0][0].text

                if attack in ["Helping Hand", "Ally Switch", "After You", "Wide Guard", "Quash", "Rage Powder", "Follow Me"]:
                    continue

                if attack == "Ion Deluge":
                    attack = "Electrify"
                
                eggMoves.append(attack)
                print(attack)

        print("Move Tutor Moves:")
        tutorMoves = []
        if updateTable('Move Tutor Attacks'):
            table = infoTable.xpath('thead/tr')
            for i in range(2, len(table) - 1, 2):
                attack = table[i][0][0].text

                if attack in ["Helping Hand", "After You"]:
                    continue
                
                tutorMoves.append(attack)
                print(attack)
        if updateTable('Ultra Sun/Ultra Moon Move Tutor Attacks'):
            table = infoTable.xpath('thead/tr')
            for i in range(2, len(table) - 1, 2):
                attack = table[i][0][0].text

                if attack in ["Helping Hand", "After You"]:
                    continue
                
                tutorMoves.append(attack)
                print(attack)

        # Stats
        stats = [0]*6
        updateTable('Stats')
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
            page = requests.get('http://bulbapedia.bulbagarden.net/wiki/' + name)
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
