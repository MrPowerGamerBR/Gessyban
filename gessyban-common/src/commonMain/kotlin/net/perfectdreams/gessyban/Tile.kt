package net.perfectdreams.gessyban

sealed class Tile {
    var x = 0
    var z = 0

    class Player : Tile() {
        var hasMovedAtLeastOnce = false
        var lastMove = LastMove.UP

        enum class LastMove {
            UP,
            DOWN,
            LEFT,
            RIGHT
        }
    }
    class Wall : Tile()
    class Box : Tile()
    class PressurePlate : Tile()
}