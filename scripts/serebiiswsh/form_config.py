from scripts.forms import AddedPokes
from scripts.serebii.parse_util import has_form


class FormConfig:
    def __init__(self, num: int) -> None:
        self.num = num
        self.lookup_num = num
        self.normal_form = True
        self.is_alolan = False
        self.is_galarian = False
        self.form_name = None
        self.type_form_name = None
        base_exp_suffix = None
        image_suffix = None

        # Pokemon with Alolan or Galarian forms
        if num in [26, 37, 38, 50, 51, 52, 53, 77, 78, 79, 83, 110, 122, 222, 263, 264, 554, 555, 562, 618]:
            self.form_name = 'Normal'
        # Rotom
        elif num == 479:
            self.form_name = 'Rotom'
        # Meowstic
        elif num == 678:
            self.form_name = 'Male'
        # Necrozma
        elif num == 800:
            self.form_name = 'Normal'
        # Toxtricity
        elif self.num == 849:
            self.form_name = 'Amped Form'
        # Indeedee
        elif self.num == 876:
            self.form_name = 'Male'
        # Zacian/Zamazenta
        elif self.num in [888, 889]:
            self.form_name = 'Hero of Many Battles'
        # Silph Surfer
        elif self.num == AddedPokes.ALOLAN_RAICHU.value:
            self.lookup_num = 26
            self.is_alolan = True
        # Yukikon
        elif self.num == AddedPokes.ALOLAN_VULPIX.value:
            self.lookup_num = 37
            self.is_alolan = True
        # Kyukon
        elif self.num == AddedPokes.ALOLAN_NINETALES.value:
            self.lookup_num = 38
            self.is_alolan = True
            base_exp_suffix = "A"
        # Nyarth
        elif self.num == AddedPokes.GALARIAN_MEOWTH.value:
            self.lookup_num = 52
            self.is_galarian = True
            # You'd think this should be in the Galarian section but the page is missing most of them...
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
            base_exp_suffix = "G"
        # Cursayon
        elif self.num == AddedPokes.GALARIAN_CORSOLA.value:
            self.lookup_num = 222
            self.is_galarian = True
            base_exp_suffix = "G"
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

        # Can't be both don't even try
        assert not (self.is_alolan and self.is_galarian)
        if self.is_alolan or self.is_galarian:
            assert self.form_name is None
            self.normal_form = False
            if self.is_alolan:
                self.form_name = "Alola"
                self.type_form_name = "Alolan"
                image_suffix = "-a"
            elif self.is_galarian:
                self.form_name = "Galarian"
                image_suffix = "-g"

        if base_exp_suffix is None:
            base_exp_suffix = ""
        if image_suffix is None:
            image_suffix = ""

        self.base_exp_name = str(self.lookup_num).zfill(3) + base_exp_suffix
        self.form_image_name = str(self.lookup_num).zfill(3) + image_suffix
        self.pokedex_image_name = str(self.lookup_num) + image_suffix

        if self.type_form_name is None:
            self.type_form_name = self.form_name

    def has_form(self, row, form_index):
        return has_form(row, form_index, self.form_image_name)
