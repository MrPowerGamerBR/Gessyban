package com.mrpowergamerbr.starfait

// From JOML
class Quaternionf(var x: Float, var y: Float, var z: Float, var w: Float) {
    /**
     * Create a new [Quaternionf] and initialize it with `(x=0, y=0, z=0, w=1)`,
     * where `(x, y, z)` is the vector part of the quaternion and `w` is the real/scalar part.
     */
    constructor() : this(0f, 0f, 0f, 1f)
}
