from typing import Union

from scripts.forms import AddedPokes


class FormConfig:
    def __init__(self, num: int) -> None:
        self.base_num = num  # type: int
        self.id = num        # type: Union[int, str]

        # If any of the following are true, will use the corresponding info from the base pokemon instead
        self.use_base_exp = False        # type: bool
        self.use_base_stats = False      # type: bool
        self.use_base_abilities = False  # type: bool

        # Use the same base experience and base stats as Charizard (will also be substituted)
        if num == AddedPokes.MEGA_CHARIZARD.value:
            self.base_num = 6
            self.id = "charizard-mega-x"
            self.use_base_exp = True
            self.use_base_stats = True
        # Use Mawile's abilities since Huge Power is what makes Mega Mawile insane
        elif num == AddedPokes.MEGA_MAWILE.value:
            self.base_num = 303
            self.id = "mawile-mega"
            self.use_base_abilities = True
        # Asbel uses a modified version of Absol's stats
        elif num == AddedPokes.MEGA_ABSOL.value:
            self.base_num = 359
            self.id = "absol-mega"
            self.use_base_stats = True
        elif num == AddedPokes.MEGA_SABLEYE.value:
            self.base_num = 302
            self.id = "sableye-mega"
        elif num == AddedPokes.ALOLAN_RAICHU.value:
            self.base_num = 26
            self.id = "raichu-alola"
        elif num == AddedPokes.ALOLAN_SANDSHREW.value:
            self.base_num = 27
            self.id = "sandshrew-alola"
        elif num == AddedPokes.ALOLAN_SANDSLASH.value:
            self.base_num = 28
            self.id = "sandslash-alola"
        elif num == AddedPokes.ALOLAN_VULPIX.value:
            self.base_num = 37
            self.id = "vulpix-alola"
        elif num == AddedPokes.ALOLAN_NINETALES.value:
            self.base_num = 38
            self.id = "ninetales-alola"
        elif num == AddedPokes.ALOLAN_GRIMER.value:
            self.base_num = 88
            self.id = "grimer-alola"
        elif num == AddedPokes.ALOLAN_MUK.value:
            self.base_num = 89
            self.id = "muk-alola"
        elif num == AddedPokes.ALOLAN_EXEGGUTOR.value:
            self.base_num = 103
            self.id = "exeggutor-alola"
        elif num == AddedPokes.ALOLAN_MAROWAK.value:
            self.base_num = 105
            self.id = "marowak-alola"
        # Uses modified mega stats
        elif num == AddedPokes.MEGA_BANETTE.value:
            self.base_num = 354
            self.id = "banette-mega"
        elif num == AddedPokes.MIDNIGHT_LYCANROC.value:
            self.base_num = 745
            self.id = "lycanroc-midnight"
        elif num == AddedPokes.DUSK_LYCANROC.value:
            self.base_num = 745
            self.id = "lycanroc-dusk"
        # Meltan and Melmetal are not currently in the PokeAPI -- just use Bulbasaur as placeholder information
        elif num in [808, 809]:
            self.base_num = 1
            self.id = 'bulbasaur'
