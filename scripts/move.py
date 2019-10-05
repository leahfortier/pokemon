from typing import List


# Class that holds the move name, learn method, and level learned (for 'level-up' moves)
class Move:
    def __init__(self, name: str, level_learned: int, learn_method: str = 'level-up'):
        self.name = name
        self.level_learned = level_learned
        self.learn_method = learn_method

    def __str__(self) -> str:
        return self.name


# Class to hold the list of level up moves for a Pokemon
class LevelUpMoves:
    def __init__(self):
        self.moves = []  # type: List[Move]

    def add(self, level: int, attack_name: str) -> None:
        self.moves.append(Move(attack_name, level))

    def add_default(self, attack_name: str) -> None:
        self.add(1, attack_name)

    def add_evolution(self, attack_name: str) -> None:
        self.add(0, attack_name)

    # Returns a sorted (by level) list of strings in the format '<level> <ATTACK_NAME>' (Ex: '7 LEECH_SEED')
    def get(self) -> List[str]:
        # Remove duplicates for evolution moves and default moves (only keep evolution move)
        evolution_moves = [move.name for move in self.moves if move.level_learned == 0]
        self.moves = [move for move in self.moves if not(move.level_learned == 1 and move.name in evolution_moves)]

        # Level-up moves are sorted by level
        self.moves.sort(key = _attack_level_sort)

        # Convert to a list of strings
        return [str(move.level_learned) + " " + move.name for move in self.moves]


# Used for sorting the level up moves by level
def _attack_level_sort(move: Move) -> int:
    return move.level_learned
