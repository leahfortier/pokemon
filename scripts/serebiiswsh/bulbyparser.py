from typing import Dict

import requests
from lxml import html


class PokeBase:
    def __init__(self, row) -> None:
        self.name = str(row[2].text_content().strip())
        parenthesis = self.name.find('(')
        if parenthesis != -1:
            self.name = self.name[:parenthesis].strip()

        self.base_exp = int(row[3].text_content().replace("*", "").strip())

        self.evs = [0]*6
        for i in range(0, 6):
            self.evs[i] = int(row[4 + i].text_content().strip())


def get_base_map() -> Dict[str, PokeBase]:
    page = requests.get('https://bulbapedia.bulbagarden.net/wiki/List_of_Pok%C3%A9mon_by_effort_value_yield')
    tree = html.fromstring(page.text)

    table = tree.xpath('//*[@id="mw-content-text"]/table[1]/tr')
    form_map = {}
    for i, row in enumerate(table):
        # Schema
        if i == 0:
            continue

        num = row[0].text_content().strip()

        # First one is likely the base form
        if num in form_map:
            continue

        form_map[num] = PokeBase(row)

    return form_map
