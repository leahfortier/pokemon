from enum import Enum, auto


class Stat(Enum):
    HP = 0
    ATTACK = 1
    DEFENSE = 2
    SP_ATTACK = 3
    SP_DEFENSE = 4
    SPEED = 5


# Original Pokes require an enum since their number is subject to change
class AddedPokes(Enum):
    MEGA_CHARIZARD = 891
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
    MEGA_BANETTE = auto()
    MIDNIGHT_LYCANROC = auto()
    DUSK_LYCANROC = auto()
    GALARIAN_MEOWTH = auto()
    GALARIAN_PONTYA = auto()
    GALARIAN_RAPIDASH = auto()
    GALARIAN_FARFETCHD = auto()
    GALARIAN_WEEZING = auto()
    GALARIAN_MR_MIME = auto()
    GALARIAN_CORSOLA = auto()
    GALARIAN_ZIGZAGOON = auto()
    GALARIAN_LINOONE = auto()
    GALARIAN_DARUMAKA = auto()
    GALARIAN_DARMANITAN = auto()
    GALARIAN_YAMASK = auto()
