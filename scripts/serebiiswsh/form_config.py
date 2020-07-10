import re

from scripts.forms import AddedPokes
from scripts.serebii.parse_util import has_form

class FormConfig:
    def __init__(self, num: int) -> None:
        # Set manually in set_num
        self.num = num
        self.lookup_num = num
        self.normal_form = True
        self.is_galarian = False
        self.form_name = None
        base_exp_suffix = None
        image_suffix = None

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
        # Nyarth
        elif self.num == AddedPokes.GALARIAN_MEOWTH.value:
            self.lookup_num = 52
            self.is_galarian = True
            # You'd think this should be in the Galarian section but the page is missing the other forms
            base_exp_suffix = "G"
        # Unita
        elif self.num == AddedPokes.GALARIAN_PONTYA.value:
            self.lookup_num = 77
            self.is_galarian = True
        # Wisteridash
        elif self.num == AddedPokes.GALARIAN_RAPIDASH.value:
            self.lookup_num = 78
            self.is_galarian = True
        # Squirfetch'd
        elif self.num == AddedPokes.GALARIAN_FARFETCHD.value:
            self.lookup_num = 83
            self.is_galarian = True
        # Smogogo
        elif self.num == AddedPokes.GALARIAN_WEEZING.value:
            self.lookup_num = 110
            self.is_galarian = True
        # Mr. Rime Jr.
        elif self.num == AddedPokes.GALARIAN_MR_MIME.value:
            self.lookup_num = 122
            self.is_galarian = True
        # Cursayon
        elif self.num == AddedPokes.GALARIAN_CORSOLA.value:
            self.lookup_num = 222
            self.is_galarian = True
        # Zigzaton
        elif self.num == AddedPokes.GALARIAN_ZIGZAGOON.value:
            self.lookup_num = 263
            self.is_galarian = True
        # Massuguma
        elif self.num == AddedPokes.GALARIAN_LINOONE.value:
            self.lookup_num = 264
            self.is_galarian = True
        # Darumakice
        elif self.num == AddedPokes.GALARIAN_DARUMAKA.value:
            self.lookup_num = 554
            self.is_galarian = True
        # Darmaniyeti
        elif self.num == AddedPokes.GALARIAN_DARMANITAN.value:
            self.lookup_num = 555
            self.is_galarian = True
        # Yarune
        elif self.num == AddedPokes.GALARIAN_YAMASK.value:
            self.lookup_num = 562
            self.is_galarian = True

        if self.is_galarian:
            assert self.form_name is None
            self.normal_form = False
            self.form_name = "Galarian"
            image_suffix = "-g"

        if base_exp_suffix is None:
            base_exp_suffix = ""
        if image_suffix is None:
            image_suffix = ""

        self.form_image_name = str(self.lookup_num).zfill(3) + image_suffix
        self.base_exp_name = str(self.lookup_num).zfill(3) + base_exp_suffix

    def has_form(self, row, form_index):
        return has_form(row, form_index, self.form_image_name)