# -*- coding: latin-1 -*-

from lxml import html
import requests

global infoTable
global aaa

def checkHeader(header, headerIndex):
    global infoTable
    
    if infoTable.tag == 'table':
        query = infoTable.xpath('tr[1]/td/b')
        if len(query) > 0:
            text = query[0].text
        else:
            query = infoTable.xpath('tr[1]/td')
            if len(query) > 0:
                text = query[0].text
            else:
                text = infoTable.xpath('thead/tr[1]/td')[0].text

            if text is None:
                text = infoTable.xpath('tr[1]/td/font')[0].text

        if not text is None and text[headerIndex:] == header:
            return True

    return False

def updateTable(header):
    return updateTableIndex(header, 0)

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
    for num in range(1, 720):
    #for num in [1]:
        page = requests.get('http://www.serebii.net/pokedex-xy/' + str(num).zfill(3) + '.shtml')
        tree = html.fromstring(page.text)
        mainDiv = tree.xpath('/html/body/table[2]/tr[2]/td[2]/font/div[2]/div')[0];

        tableCheck = True

        if num < 650:
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
            name = "Nidoran"

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
            type2 = 'None'
        # Darmanitan
        elif num == 555:
            type1 = 'Fire'
            type2 = 'None'
        # Meloetta
        elif num == 648:
            type1 = 'Normal'
            type2 = 'Psychic'
        else:
            types = infoTable.xpath('tr[2]/td[6]/a/img')
            type1 = types[0].attrib["src"]
            type1 = type1[type1.find("type") + 5 : -4].capitalize()

            # Check for a second type
            if len(types) == 2:
                type2 = types[1].attrib["src"]
                type2 = type2[type2.find("type") + 5 : -4].capitalize()
            else:
                type2 = "None"
                
        print("Type1: " + type1)
        print("Type2: " + type2)

        # Remove the Pokemon text from the end of weight
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

        # Diancie's capture is still unknown
        if num == 719:
            captureRate = 3
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
        if num < 650:
            infoTable = infoTable.getnext()
        else:
            index = 3
            infoTable = mainDiv.xpath('table[3]')[0]
            
        growthRate = infoTable.xpath('tr[4]/td[1]')[0].text_content()
        growthRate = growthRate[growthRate.find("Points") + 6 : ]

        print("Growth Rate: " + growthRate)

        # Not in the mood to deal with Deoxys's multiple forms and effort yields
        if num == 386:
            evs = [0, 1, 0, 1, 0, 1]
        # Same with Wormadam
        elif num == 413:
            evs = [0, 0, 0, 0, 2, 0]
        # And Shaymin
        elif num == 492:
            evs = [3, 0, 0, 0, 0, 0]
        # And Darmanitan
        elif num == 555:
            evs = [0, 2, 0, 0, 0, 0]
        # And Tornadus
        elif num == 641:
            evs = [0, 3, 0, 0, 0, 0]
        # And Thundurus
        elif num == 642:
            evs = [0, 3, 0, 0, 0, 0]
        # And Landorus Note: it actually has sp.attack but that's stupid its attack is higher
        elif num == 645:
            evs = [0, 3, 0, 0, 0, 0]
        # And Kyurem
        elif num == 646:
            evs = [1, 1, 0, 1, 0, 0]
        # And Meloetta
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

        abilities = infoTable.xpath('tr[2]/td/a/b')
        ability1 = abilities[0].text
        if len(abilities) >= 2:
            ability2 = abilities[1].text
        else:
            ability2 = "None"
            
        print("Ability1: " + ability1)    
        print("Ability2: " + ability2)

        # Egg Group table
        if num < 650:
            infoTable = infoTable.getnext().getnext()
        else:
            index = 5
            infoTable = mainDiv.xpath('table[5]')[0]
            
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

        print("Egg Group1: " + eggGroup1)
        print("Egg Group2: " + eggGroup2)

        updateTable('Flavor Text')

        # Oddish's flavor text is srsly buggin' out
        if num == 43:
            flavorText = 'It often plants its root feet in the ground during the day and sows seeds as it walks about at night.'
        # Same with Darmanitan TODO: Put the degree symbol back in
        elif num == 555:
            flavorText = 'Its internal fire burns at 2,500 F, making enough power that it can destroy a dump truck with one punch.'
        else:
            flavorText = infoTable.xpath('tr[3]/td[2]')[0].text
            
        # Replace the special e character in the flavor text
        u = u'é'
        flavorText = flavorText.replace(u, "\u00e9").replace('  ', ' ')
            
        print("Flavor Text: " + flavorText)

        # If they have the same movelist for X/Y and R/S, then the movelist is displayed as Gen IV Level Up -- Otherwise, we want the updated R/S moves
        if not updateTable('Generation VI Level Up'):
            # The special characters are really fucking things up so just start at the third character when comparing
            updateTableIndex('S Level Up', 3)

        attacks = []
        for i in range(2, len(infoTable) - 1, 2):
            level = infoTable[i][0].text
            
            if type(level) == unicode:
                level = 0
                
            attack = infoTable[i][1][0].text
            attacks.append(str(level) + " " + attack)
            print(str(level) + " " + attack)

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
        if updateTable('Omega Ruby/Alpha Sapphire Move Tutor Attacks'):
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
##        if num == 719:
##            baseExp = 270
##        elif num == 29 or num == 32:
##            baseExp = 55
##        else:
##            page = requests.get('http://bulbapedia.bulbagarden.net/wiki/' + name)
##            tree = html.fromstring(page.text)
##
##            index = 9
##
##            # Pokemon that do not have hidden abilities have a stupid motherfucking format and I'm really fucking pissed
##            if num == 676 or num == 679 or num == 680 or num == 681 or num == 692 or num == 693 or num == 716 or num == 717 or num == 718:
##                index = 8
##            
##            baseExp = int(tree.xpath('//*[@id="mw-content-text"]/table[3]/tr[' + str(index) + ']/td[1]/table/tr/td[3]')[0].text)
##        print("Base EXP: " + str(baseExp))

##        f.write(str(num) + '\n')
##        f.write(str(name) + '\n')
##
##        for stat in stats:
##            f.write(str(stat) + ' ')
##        f.write('\n')
##
##        if len(str(baseExp)) == 1:
##            f.write("BASE EXP: ")
##        
##        f.write(str(baseExp) + '\n')
##        f.write(str(growthRate) + '\n')
##        f.write(str(type1) + ' ')
##        f.write(str(type2) + '\n')
        
##        f.write(str(len(attacks)) + '\n')
##        for attack in attacks:
##            f.write(str(attack) + '\n')

##        f.write(str(len(tms)) + '\n')
##        for attack in tms:
##            f.write(str(attack) + '\n')

##        f.write(str(len(eggMoves)) + '\n')
##        for attack in eggMoves:
##            f.write(str(attack) + '\n')

        f.write(str(len(tutorMoves)) + '\n')
        for attack in tutorMoves:
            f.write(str(attack) + '\n')

##        f.write(str(captureRate) + '\n')
##
##        for ev in evs:
##            f.write(str(ev) + ' ')
##        f.write('\n')
##
##        # TODO: Evolutions
##        f.write('None\n')
##        
##        # TODO: Wild Hold Items
##        f.write('0\n')
##
##        f.write(str(maleRatio) + '\n')
##        f.write(str(ability1) + '\n')
##        f.write(str(ability2) + '\n')
##        f.write(str(classification) + '\n')
##        f.write(str(height) + ' ')
##        f.write(str(weight) + '\n') # Change back to space instead of new line when the flavor text is put back in
##        #f.write(str(flavorText) + '\n')
##        f.write(str(eggSteps) + '\n')
##        f.write(str(eggGroup1) + '\n')
##        f.write(str(eggGroup2) + '\n')
        f.write('\n')
