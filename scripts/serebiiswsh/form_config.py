import re

import requests
from lxml import html

from scripts.serebii.parse_util import has_form


def get_form_map() -> {}:
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

        form_map[num] = FormConfig(num, row)

    return form_map


class FormConfig:
    def __init__(self, num: str, row) -> None:
        # Set manually in set_num
        self.num = None
        self.normal_form = True
        self.form_name = None
        self.form_image_name = None

        self.lookup_num = int(re.sub("[^0-9]", "", num))

        self.name = str(row[2].text_content().strip())
        parenthesis = self.name.find('(')
        if parenthesis != -1:
            self.name = self.name[:parenthesis].strip()

        self.base_exp = int(row[3].text_content().replace("*", "").strip())

        self.evs = [0]*6
        for i in range(0, 6):
            self.evs[i] = int(row[4 + i].text_content().strip())

    def set_num(self, num: int) -> None:
        self.num = num

        # Toxtricity
        if self.num == 849:
            self.form_name = 'Amped Form'
        # Indeedee
        elif self.num == 876:
            self.form_name = 'Male'
        # Zacian
        elif self.num == 888:
            self.form_name = 'Hero of Many Battles'
        # Zamazenta
        elif self.num == 889:
            self.form_name = 'Hero of Many Battles'

        # Note: will likely need to add image suffix as more forms are added
        self.form_image_name = str(self.lookup_num).zfill(3)

    def has_form(self, row, form_index):
        return has_form(row, form_index, self.form_image_name)