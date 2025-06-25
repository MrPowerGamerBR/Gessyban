package net.perfectdreams.gessyban

import java.io.File

class SokobanGame(val m: Gessyban) {
    val tiles = mutableListOf<Tile>()
    var currentLevel = 1
    var playfieldWidth = 0
    var playfieldHeight = 0

    fun loadMapFromString(level: Int, content: String) {
        tiles.clear()

        for ((z, line) in content.lines().withIndex()) {
            for ((x, char) in line.withIndex()) {
                val tile = when (char) {
                    'P' -> Tile.Player()
                    '#' -> Tile.Wall()
                    'B' -> Tile.Box()
                    'X' -> Tile.PressurePlate()
                    ' ' -> continue
                    else -> error("Unsupported character! $char")
                }

                tile.x = x
                tile.z = z

                tiles.add(tile)
            }
        }

        this.currentLevel = level
        this.playfieldWidth = tiles.maxOf { it.x } + 1
        this.playfieldHeight = tiles.maxOf { it.z } + 1
    }

    fun movePlayer(x: Int, z: Int) {
        val playerTile = tiles.first { it is Tile.Player } as Tile.Player

        playerTile.hasMovedAtLeastOnce = true
        if (x == 1) {
            playerTile.lastMove = Tile.Player.LastMove.RIGHT
        } else if (x == -1) {
            playerTile.lastMove = Tile.Player.LastMove.LEFT
        } else if (z == 1) {
            playerTile.lastMove = Tile.Player.LastMove.UP
        } else if (z == -1) {
            playerTile.lastMove = Tile.Player.LastMove.DOWN
        }


        println("Last move: ${playerTile.lastMove}")

        val currentX = playerTile.x
        val currentZ = playerTile.z

        val newX = currentX + x
        val newZ = currentZ + z

        val tilesInSameSpace = tiles.filter { it.x == newX && it.z == newZ }

        for (tileInSameSpace in tilesInSameSpace) {
            when (tileInSameSpace) {
                is Tile.Box -> {
                    val newBoxTileX = tileInSameSpace.x + x
                    val newBoxTileY = tileInSameSpace.z + z

                    val tilesInSameSpaceBox = tiles.filter { it.x == newBoxTileX && it.z == newBoxTileY }

                    // Same thing...
                    for (tileInSameSpaceBox in tilesInSameSpaceBox) {
                        if (tileInSameSpaceBox is Tile.Wall)
                            return // Cannot move here!
                        if (tileInSameSpaceBox is Tile.Box)
                            return // Cannot move here!
                    }

                    tileInSameSpace.x = newBoxTileX
                    tileInSameSpace.z = newBoxTileY
                }

                is Tile.Wall -> {
                    return // Cannot move!
                }

                is Tile.Player -> {
                    // Impossible!
                }

                is Tile.PressurePlate -> {
                    // Do nothing...
                }
            }
        }

        playerTile.x = newX
        playerTile.z = newZ

        val pressurePlates = tiles.filterIsInstance<Tile.PressurePlate>()

        for (pressurePlate in pressurePlates) {
            val hasBoxOnTop = tiles.filter { it.x == pressurePlate.x && it.z == pressurePlate.z }.any { it is Tile.Box }

            if (!hasBoxOnTop)
                return
        }

        val newLevel = this.currentLevel + 1
        val nextLevelFile = File("assets/maps/map$newLevel.txt")
        if (nextLevelFile.exists())
            loadMapFromString(newLevel, nextLevelFile.readText())
        else
            m.stage = Stage.MainMenu(m)
    }
}