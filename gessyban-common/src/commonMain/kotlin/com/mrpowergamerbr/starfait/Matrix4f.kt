package com.mrpowergamerbr.starfait

import kotlin.math.cos
import kotlin.math.sin

// From JOML, only the generic (non-properties) paths
class Matrix4f {
    var m00 = 0f
    var m01 = 0f
    var m02 = 0f
    var m03 = 0f
    var m10 = 0f
    var m11 = 0f
    var m12 = 0f
    var m13 = 0f
    var m20 = 0f
    var m21 = 0f
    var m22 = 0f
    var m23 = 0f
    var m30 = 0f
    var m31 = 0f
    var m32 = 0f
    var m33 = 0f

    /**
     * Create a new [Matrix4f] and set it to identity.
     */
    constructor() {
        this.m00 = 1.0f
        this.m11 = 1.0f
        this.m22 = 1.0f
        this.m33 = 1.0f
    }

    /**
     * Create a new [Matrix4f] and make it a copy of the given matrix.
     */
    constructor(mat: Matrix4f) {
        set(mat)
    }

    /**
     * Store the values of the given matrix `m` into `this` matrix.
     *
     * @param m
     * the matrix to copy the values from
     * @return this
     */
    fun set(m: Matrix4f): Matrix4f {
        this.m00 = m.m00
        this.m01 = m.m01
        this.m02 = m.m02
        this.m03 = m.m03
        this.m10 = m.m10
        this.m11 = m.m11
        this.m12 = m.m12
        this.m13 = m.m13
        this.m20 = m.m20
        this.m21 = m.m21
        this.m22 = m.m22
        this.m23 = m.m23
        this.m30 = m.m30
        this.m31 = m.m31
        this.m32 = m.m32
        this.m33 = m.m33
        return this
    }

    // Chainable setters, like JOML's mXY(float)
    fun m00(m00: Float): Matrix4f { this.m00 = m00; return this }
    fun m01(m01: Float): Matrix4f { this.m01 = m01; return this }
    fun m02(m02: Float): Matrix4f { this.m02 = m02; return this }
    fun m03(m03: Float): Matrix4f { this.m03 = m03; return this }
    fun m10(m10: Float): Matrix4f { this.m10 = m10; return this }
    fun m11(m11: Float): Matrix4f { this.m11 = m11; return this }
    fun m12(m12: Float): Matrix4f { this.m12 = m12; return this }
    fun m13(m13: Float): Matrix4f { this.m13 = m13; return this }
    fun m20(m20: Float): Matrix4f { this.m20 = m20; return this }
    fun m21(m21: Float): Matrix4f { this.m21 = m21; return this }
    fun m22(m22: Float): Matrix4f { this.m22 = m22; return this }
    fun m23(m23: Float): Matrix4f { this.m23 = m23; return this }
    fun m30(m30: Float): Matrix4f { this.m30 = m30; return this }
    fun m31(m31: Float): Matrix4f { this.m31 = m31; return this }
    fun m32(m32: Float): Matrix4f { this.m32 = m32; return this }
    fun m33(m33: Float): Matrix4f { this.m33 = m33; return this }

    /**
     * Multiply this matrix by the supplied `right` matrix and store the result in `dest`.
     *
     *
     * If `M` is `this` matrix and `R` the `right` matrix,
     * then the new matrix will be `M * R`. So when transforming a
     * vector `v` with the new matrix by using `M * R * v`, the
     * transformation of the right matrix will be applied first!
     *
     * @param right
     * the right operand of the matrix multiplication
     * @param dest
     * the destination matrix, which will hold the result
     * @return dest
     */
    fun mul(right: Matrix4f, dest: Matrix4f): Matrix4f {
        val nm00 = m00 * right.m00 + (m10 * right.m01 + (m20 * right.m02 + m30 * right.m03))
        val nm01 = m01 * right.m00 + (m11 * right.m01 + (m21 * right.m02 + m31 * right.m03))
        val nm02 = m02 * right.m00 + (m12 * right.m01 + (m22 * right.m02 + m32 * right.m03))
        val nm03 = m03 * right.m00 + (m13 * right.m01 + (m23 * right.m02 + m33 * right.m03))
        val nm10 = m00 * right.m10 + (m10 * right.m11 + (m20 * right.m12 + m30 * right.m13))
        val nm11 = m01 * right.m10 + (m11 * right.m11 + (m21 * right.m12 + m31 * right.m13))
        val nm12 = m02 * right.m10 + (m12 * right.m11 + (m22 * right.m12 + m32 * right.m13))
        val nm13 = m03 * right.m10 + (m13 * right.m11 + (m23 * right.m12 + m33 * right.m13))
        val nm20 = m00 * right.m20 + (m10 * right.m21 + (m20 * right.m22 + m30 * right.m23))
        val nm21 = m01 * right.m20 + (m11 * right.m21 + (m21 * right.m22 + m31 * right.m23))
        val nm22 = m02 * right.m20 + (m12 * right.m21 + (m22 * right.m22 + m32 * right.m23))
        val nm23 = m03 * right.m20 + (m13 * right.m21 + (m23 * right.m22 + m33 * right.m23))
        val nm30 = m00 * right.m30 + (m10 * right.m31 + (m20 * right.m32 + m30 * right.m33))
        val nm31 = m01 * right.m30 + (m11 * right.m31 + (m21 * right.m32 + m31 * right.m33))
        val nm32 = m02 * right.m30 + (m12 * right.m31 + (m22 * right.m32 + m32 * right.m33))
        val nm33 = m03 * right.m30 + (m13 * right.m31 + (m23 * right.m32 + m33 * right.m33))
        dest.m00 = nm00
        dest.m01 = nm01
        dest.m02 = nm02
        dest.m03 = nm03
        dest.m10 = nm10
        dest.m11 = nm11
        dest.m12 = nm12
        dest.m13 = nm13
        dest.m20 = nm20
        dest.m21 = nm21
        dest.m22 = nm22
        dest.m23 = nm23
        dest.m30 = nm30
        dest.m31 = nm31
        dest.m32 = nm32
        dest.m33 = nm33
        return dest
    }

    /**
     * Multiply this matrix by the supplied `right` matrix and store the result in `this`.
     *
     *
     * If `M` is `this` matrix and `R` the `right` matrix,
     * then the new matrix will be `M * R`. So when transforming a
     * vector `v` with the new matrix by using `M * R * v`, the
     * transformation of the right matrix will be applied first!
     *
     * @param right
     * the right operand of the matrix multiplication
     * @return this
     */
    fun mul(right: Matrix4f): Matrix4f {
        return mul(right, this)
    }

    /**
     * Invert this matrix and write the result into `dest`.
     *
     * @param dest
     * will hold the result
     * @return dest
     */
    fun invert(dest: Matrix4f): Matrix4f {
        val a = m00 * m11 - m01 * m10
        val b = m00 * m12 - m02 * m10
        val c = m00 * m13 - m03 * m10
        val d = m01 * m12 - m02 * m11
        val e = m01 * m13 - m03 * m11
        val f = m02 * m13 - m03 * m12
        val g = m20 * m31 - m21 * m30
        val h = m20 * m32 - m22 * m30
        val i = m20 * m33 - m23 * m30
        val j = m21 * m32 - m22 * m31
        val k = m21 * m33 - m23 * m31
        val l = m22 * m33 - m23 * m32
        var det = a * l - b * k + c * j + d * i - e * h + f * g
        det = 1.0f / det
        val nm00 = ( m11 * l + (-m12 * k + ( m13 * j))) * det
        val nm01 = (-m01 * l + ( m02 * k + (-m03 * j))) * det
        val nm02 = ( m31 * f + (-m32 * e + ( m33 * d))) * det
        val nm03 = (-m21 * f + ( m22 * e + (-m23 * d))) * det
        val nm10 = (-m10 * l + ( m12 * i + (-m13 * h))) * det
        val nm11 = ( m00 * l + (-m02 * i + ( m03 * h))) * det
        val nm12 = (-m30 * f + ( m32 * c + (-m33 * b))) * det
        val nm13 = ( m20 * f + (-m22 * c + ( m23 * b))) * det
        val nm20 = ( m10 * k + (-m11 * i + ( m13 * g))) * det
        val nm21 = (-m00 * k + ( m01 * i + (-m03 * g))) * det
        val nm22 = ( m30 * e + (-m31 * c + ( m33 * a))) * det
        val nm23 = (-m20 * e + ( m21 * c + (-m23 * a))) * det
        val nm30 = (-m10 * j + ( m11 * h + (-m12 * g))) * det
        val nm31 = ( m00 * j + (-m01 * h + ( m02 * g))) * det
        val nm32 = (-m30 * d + ( m31 * b + (-m32 * a))) * det
        val nm33 = ( m20 * d + (-m21 * b + ( m22 * a))) * det
        dest.m00 = nm00
        dest.m01 = nm01
        dest.m02 = nm02
        dest.m03 = nm03
        dest.m10 = nm10
        dest.m11 = nm11
        dest.m12 = nm12
        dest.m13 = nm13
        dest.m20 = nm20
        dest.m21 = nm21
        dest.m22 = nm22
        dest.m23 = nm23
        dest.m30 = nm30
        dest.m31 = nm31
        dest.m32 = nm32
        dest.m33 = nm33
        return dest
    }

    /**
     * Invert this matrix.
     *
     * @return this
     */
    fun invert(): Matrix4f {
        return invert(this)
    }

    /**
     * Apply scaling to `this` matrix by scaling the base axes by the given x,
     * y and z factors and store the result in `dest`.
     *
     *
     * If `M` is `this` matrix and `S` the scaling matrix,
     * then the new matrix will be `M * S`. So when transforming a
     * vector `v` with the new matrix by using `M * S * v`, the scaling will be applied first!
     *
     * @param x
     * the factor of the x component
     * @param y
     * the factor of the y component
     * @param z
     * the factor of the z component
     * @param dest
     * will hold the result
     * @return dest
     */
    fun scale(x: Float, y: Float, z: Float, dest: Matrix4f): Matrix4f {
        dest.m00 = m00 * x
        dest.m01 = m01 * x
        dest.m02 = m02 * x
        dest.m03 = m03 * x
        dest.m10 = m10 * y
        dest.m11 = m11 * y
        dest.m12 = m12 * y
        dest.m13 = m13 * y
        dest.m20 = m20 * z
        dest.m21 = m21 * z
        dest.m22 = m22 * z
        dest.m23 = m23 * z
        dest.m30 = m30
        dest.m31 = m31
        dest.m32 = m32
        dest.m33 = m33
        return dest
    }

    /**
     * Apply scaling to this matrix by scaling the base axes by the given x,
     * y and z factors.
     *
     *
     * If `M` is `this` matrix and `S` the scaling matrix,
     * then the new matrix will be `M * S`. So when transforming a
     * vector `v` with the new matrix by using `M * S * v`, the scaling will be applied first!
     *
     * @param x
     * the factor of the x component
     * @param y
     * the factor of the y component
     * @param z
     * the factor of the z component
     * @return this
     */
    fun scale(x: Float, y: Float, z: Float): Matrix4f {
        return scale(x, y, z, this)
    }

    /**
     * Apply a translation to this matrix by translating by the given number of
     * units in x, y and z and store the result in `dest`.
     *
     *
     * If `M` is `this` matrix and `T` the translation
     * matrix, then the new matrix will be `M * T`. So when
     * transforming a vector `v` with the new matrix by using
     * `M * T * v`, the translation will be applied first!
     *
     * @param x
     * the offset to translate in x
     * @param y
     * the offset to translate in y
     * @param z
     * the offset to translate in z
     * @param dest
     * will hold the result
     * @return dest
     */
    fun translate(x: Float, y: Float, z: Float, dest: Matrix4f): Matrix4f {
        val nm30 = m00 * x + (m10 * y + (m20 * z + m30))
        val nm31 = m01 * x + (m11 * y + (m21 * z + m31))
        val nm32 = m02 * x + (m12 * y + (m22 * z + m32))
        val nm33 = m03 * x + (m13 * y + (m23 * z + m33))
        dest.m00 = m00
        dest.m01 = m01
        dest.m02 = m02
        dest.m03 = m03
        dest.m10 = m10
        dest.m11 = m11
        dest.m12 = m12
        dest.m13 = m13
        dest.m20 = m20
        dest.m21 = m21
        dest.m22 = m22
        dest.m23 = m23
        dest.m30 = nm30
        dest.m31 = nm31
        dest.m32 = nm32
        dest.m33 = nm33
        return dest
    }

    /**
     * Apply a translation to this matrix by translating by the given number of
     * units in x, y and z.
     *
     *
     * If `M` is `this` matrix and `T` the translation
     * matrix, then the new matrix will be `M * T`. So when
     * transforming a vector `v` with the new matrix by using
     * `M * T * v`, the translation will be applied first!
     *
     * @param x
     * the offset to translate in x
     * @param y
     * the offset to translate in y
     * @param z
     * the offset to translate in z
     * @return this
     */
    fun translate(x: Float, y: Float, z: Float): Matrix4f {
        return translate(x, y, z, this)
    }

    /**
     * Apply rotation about the Z axis to this matrix by rotating the given amount of radians
     * and store the result in `dest`.
     *
     *
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     *
     *
     * If `M` is `this` matrix and `R` the rotation matrix,
     * then the new matrix will be `M * R`. So when transforming a
     * vector `v` with the new matrix by using `M * R * v`, the
     * rotation will be applied first!
     *
     * @param ang
     * the angle in radians
     * @param dest
     * will hold the result
     * @return dest
     */
    fun rotateZ(ang: Float, dest: Matrix4f): Matrix4f {
        val sin = sin(ang.toDouble()).toFloat()
        val cos = cos(ang.toDouble()).toFloat()
        return rotateTowardsXY(sin, cos, dest)
    }

    /**
     * Apply rotation about the Z axis to this matrix by rotating the given amount of radians.
     *
     *
     * When used with a right-handed coordinate system, the produced rotation will rotate a vector
     * counter-clockwise around the rotation axis, when viewing along the negative axis direction towards the origin.
     * When used with a left-handed coordinate system, the rotation is clockwise.
     *
     *
     * If `M` is `this` matrix and `R` the rotation matrix,
     * then the new matrix will be `M * R`. So when transforming a
     * vector `v` with the new matrix by using `M * R * v`, the
     * rotation will be applied first!
     *
     * @param ang
     * the angle in radians
     * @return this
     */
    fun rotateZ(ang: Float): Matrix4f {
        return rotateZ(ang, this)
    }

    /**
     * Apply rotation about the X axis to this matrix by rotating the given amount of radians
     * and store the result in `dest`.
     *
     *
     * If `M` is `this` matrix and `R` the rotation matrix,
     * then the new matrix will be `M * R`. So when transforming a
     * vector `v` with the new matrix by using `M * R * v`, the
     * rotation will be applied first!
     *
     * @param ang
     * the angle in radians
     * @param dest
     * will hold the result
     * @return dest
     */
    fun rotateX(ang: Float, dest: Matrix4f): Matrix4f {
        val sin = sin(ang.toDouble()).toFloat()
        val cos = cos(ang.toDouble()).toFloat()
        val lm10 = m10
        val lm11 = m11
        val lm12 = m12
        val lm13 = m13
        val lm20 = m20
        val lm21 = m21
        val lm22 = m22
        val lm23 = m23
        dest.m20 = lm10 * -sin + lm20 * cos
        dest.m21 = lm11 * -sin + lm21 * cos
        dest.m22 = lm12 * -sin + lm22 * cos
        dest.m23 = lm13 * -sin + lm23 * cos
        dest.m10 = lm10 * cos + lm20 * sin
        dest.m11 = lm11 * cos + lm21 * sin
        dest.m12 = lm12 * cos + lm22 * sin
        dest.m13 = lm13 * cos + lm23 * sin
        dest.m00 = m00
        dest.m01 = m01
        dest.m02 = m02
        dest.m03 = m03
        dest.m30 = m30
        dest.m31 = m31
        dest.m32 = m32
        dest.m33 = m33
        return dest
    }

    /**
     * Apply rotation about the X axis to this matrix by rotating the given amount of radians.
     *
     * @param ang
     * the angle in radians
     * @return this
     */
    fun rotateX(ang: Float): Matrix4f {
        return rotateX(ang, this)
    }

    /**
     * Apply rotation about the Y axis to this matrix by rotating the given amount of radians
     * and store the result in `dest`.
     *
     *
     * If `M` is `this` matrix and `R` the rotation matrix,
     * then the new matrix will be `M * R`. So when transforming a
     * vector `v` with the new matrix by using `M * R * v`, the
     * rotation will be applied first!
     *
     * @param ang
     * the angle in radians
     * @param dest
     * will hold the result
     * @return dest
     */
    fun rotateY(ang: Float, dest: Matrix4f): Matrix4f {
        val sin = sin(ang.toDouble()).toFloat()
        val cos = cos(ang.toDouble()).toFloat()
        // add temporaries for dependent values
        val nm00 = m00 * cos + m20 * -sin
        val nm01 = m01 * cos + m21 * -sin
        val nm02 = m02 * cos + m22 * -sin
        val nm03 = m03 * cos + m23 * -sin
        // set non-dependent values directly
        dest.m20 = m00 * sin + m20 * cos
        dest.m21 = m01 * sin + m21 * cos
        dest.m22 = m02 * sin + m22 * cos
        dest.m23 = m03 * sin + m23 * cos
        // set other values
        dest.m00 = nm00
        dest.m01 = nm01
        dest.m02 = nm02
        dest.m03 = nm03
        dest.m10 = m10
        dest.m11 = m11
        dest.m12 = m12
        dest.m13 = m13
        dest.m30 = m30
        dest.m31 = m31
        dest.m32 = m32
        dest.m33 = m33
        return dest
    }

    /**
     * Apply rotation about the Y axis to this matrix by rotating the given amount of radians.
     *
     * @param ang
     * the angle in radians
     * @return this
     */
    fun rotateY(ang: Float): Matrix4f {
        return rotateY(ang, this)
    }

    /**
     * Apply rotation to this matrix by rotating the given amount of radians
     * about the specified `(x, y, z)` axis and store the result in `dest`.
     *
     *
     * The axis described by the three components needs to be a unit vector.
     *
     *
     * If `M` is `this` matrix and `R` the rotation matrix,
     * then the new matrix will be `M * R`. So when transforming a
     * vector `v` with the new matrix by using `M * R * v`, the
     * rotation will be applied first!
     *
     * @param ang
     * the angle in radians
     * @param x
     * the x component of the axis
     * @param y
     * the y component of the axis
     * @param z
     * the z component of the axis
     * @param dest
     * will hold the result
     * @return dest
     */
    fun rotate(ang: Float, x: Float, y: Float, z: Float, dest: Matrix4f): Matrix4f {
        val s = sin(ang.toDouble()).toFloat()
        val c = cos(ang.toDouble()).toFloat()
        val C = 1.0f - c
        val xx = x * x
        val xy = x * y
        val xz = x * z
        val yy = y * y
        val yz = y * z
        val zz = z * z
        val rm00 = xx * C + c
        val rm01 = xy * C + z * s
        val rm02 = xz * C - y * s
        val rm10 = xy * C - z * s
        val rm11 = yy * C + c
        val rm12 = yz * C + x * s
        val rm20 = xz * C + y * s
        val rm21 = yz * C - x * s
        val rm22 = zz * C + c
        return rotate3x3(rm00, rm01, rm02, rm10, rm11, rm12, rm20, rm21, rm22, dest)
    }

    /**
     * Apply rotation to this matrix by rotating the given amount of radians
     * about the specified `(x, y, z)` axis.
     *
     *
     * The axis described by the three components needs to be a unit vector.
     *
     * @param ang
     * the angle in radians
     * @param x
     * the x component of the axis
     * @param y
     * the y component of the axis
     * @param z
     * the z component of the axis
     * @return this
     */
    fun rotate(ang: Float, x: Float, y: Float, z: Float): Matrix4f {
        return rotate(ang, x, y, z, this)
    }

    /**
     * Apply the rotation transformation of the given [Quaternionf] to this matrix and store
     * the result in `dest`.
     *
     *
     * If `M` is `this` matrix and `Q` the rotation matrix obtained from the given quaternion,
     * then the new matrix will be `M * Q`. So when transforming a
     * vector `v` with the new matrix by using `M * Q * v`,
     * the quaternion rotation will be applied first!
     *
     * @param quat
     * the [Quaternionf]
     * @param dest
     * will hold the result
     * @return dest
     */
    fun rotate(quat: Quaternionf, dest: Matrix4f): Matrix4f {
        val w2 = quat.w * quat.w
        val x2 = quat.x * quat.x
        val y2 = quat.y * quat.y
        val z2 = quat.z * quat.z
        val zw = quat.z * quat.w
        val dzw = zw + zw
        val xy = quat.x * quat.y
        val dxy = xy + xy
        val xz = quat.x * quat.z
        val dxz = xz + xz
        val yw = quat.y * quat.w
        val dyw = yw + yw
        val yz = quat.y * quat.z
        val dyz = yz + yz
        val xw = quat.x * quat.w
        val dxw = xw + xw
        val rm00 = w2 + x2 - z2 - y2
        val rm01 = dxy + dzw
        val rm02 = dxz - dyw
        val rm10 = -dzw + dxy
        val rm11 = y2 - z2 + w2 - x2
        val rm12 = dyz + dxw
        val rm20 = dyw + dxz
        val rm21 = dyz - dxw
        val rm22 = z2 - y2 - x2 + w2
        return rotate3x3(rm00, rm01, rm02, rm10, rm11, rm12, rm20, rm21, rm22, dest)
    }

    /**
     * Apply the rotation transformation of the given [Quaternionf] to this matrix.
     *
     * @param quat
     * the [Quaternionf]
     * @return this
     */
    fun rotate(quat: Quaternionf): Matrix4f {
        return rotate(quat, this)
    }

    /**
     * Multiplies the upper 3x3 of this matrix by the given rotation matrix and stores the result in `dest`
     */
    private fun rotate3x3(
        rm00: Float, rm01: Float, rm02: Float,
        rm10: Float, rm11: Float, rm12: Float,
        rm20: Float, rm21: Float, rm22: Float,
        dest: Matrix4f
    ): Matrix4f {
        val nm00 = m00 * rm00 + (m10 * rm01 + m20 * rm02)
        val nm01 = m01 * rm00 + (m11 * rm01 + m21 * rm02)
        val nm02 = m02 * rm00 + (m12 * rm01 + m22 * rm02)
        val nm03 = m03 * rm00 + (m13 * rm01 + m23 * rm02)
        val nm10 = m00 * rm10 + (m10 * rm11 + m20 * rm12)
        val nm11 = m01 * rm10 + (m11 * rm11 + m21 * rm12)
        val nm12 = m02 * rm10 + (m12 * rm11 + m22 * rm12)
        val nm13 = m03 * rm10 + (m13 * rm11 + m23 * rm12)
        dest.m20 = m00 * rm20 + (m10 * rm21 + m20 * rm22)
        dest.m21 = m01 * rm20 + (m11 * rm21 + m21 * rm22)
        dest.m22 = m02 * rm20 + (m12 * rm21 + m22 * rm22)
        dest.m23 = m03 * rm20 + (m13 * rm21 + m23 * rm22)
        dest.m00 = nm00
        dest.m01 = nm01
        dest.m02 = nm02
        dest.m03 = nm03
        dest.m10 = nm10
        dest.m11 = nm11
        dest.m12 = nm12
        dest.m13 = nm13
        dest.m30 = m30
        dest.m31 = m31
        dest.m32 = m32
        dest.m33 = m33
        return dest
    }

    /**
     * Apply rotation about the Z axis to align the local `+X` towards `(dirX, dirY)` and store the result in `dest`.
     *
     *
     * If `M` is `this` matrix and `R` the rotation matrix,
     * then the new matrix will be `M * R`. So when transforming a
     * vector `v` with the new matrix by using `M * R * v`, the
     * rotation will be applied first!
     *
     *
     * The vector `(dirX, dirY)` must be a unit vector.
     *
     * @param dirX
     * the x component of the normalized direction
     * @param dirY
     * the y component of the normalized direction
     * @param dest
     * will hold the result
     * @return dest
     */
    fun rotateTowardsXY(dirX: Float, dirY: Float, dest: Matrix4f): Matrix4f {
        val nm00 = m00 * dirY + m10 * dirX
        val nm01 = m01 * dirY + m11 * dirX
        val nm02 = m02 * dirY + m12 * dirX
        val nm03 = m03 * dirY + m13 * dirX
        dest.m10 = m00 * -dirX + m10 * dirY
        dest.m11 = m01 * -dirX + m11 * dirY
        dest.m12 = m02 * -dirX + m12 * dirY
        dest.m13 = m03 * -dirX + m13 * dirY
        dest.m00 = nm00
        dest.m01 = nm01
        dest.m02 = nm02
        dest.m03 = nm03
        dest.m20 = m20
        dest.m21 = m21
        dest.m22 = m22
        dest.m23 = m23
        dest.m30 = m30
        dest.m31 = m31
        dest.m32 = m32
        dest.m33 = m33
        return dest
    }

    /**
     * Apply an orthographic projection transformation for a right-handed coordinate system
     * using the given NDC z range to this matrix and store the result in `dest`.
     *
     *
     * If `M` is `this` matrix and `O` the orthographic projection matrix,
     * then the new matrix will be `M * O`. So when transforming a
     * vector `v` with the new matrix by using `M * O * v`, the
     * orthographic projection transformation will be applied first!
     *
     *
     * Reference: [http://www.songho.ca](http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho)
     *
     * @param left
     * the distance from the center to the left frustum edge
     * @param right
     * the distance from the center to the right frustum edge
     * @param bottom
     * the distance from the center to the bottom frustum edge
     * @param top
     * the distance from the center to the top frustum edge
     * @param zNear
     * near clipping plane distance
     * @param zFar
     * far clipping plane distance
     * @param zZeroToOne
     * whether to use Vulkan's and Direct3D's NDC z range of `[0..+1]` when `true`
     * or whether to use OpenGL's NDC z range of `[-1..+1]` when `false`
     * @param dest
     * will hold the result
     * @return dest
     */
    fun ortho(left: Float, right: Float, bottom: Float, top: Float, zNear: Float, zFar: Float, zZeroToOne: Boolean, dest: Matrix4f): Matrix4f {
        // calculate right matrix elements
        val rm00 = 2.0f / (right - left)
        val rm11 = 2.0f / (top - bottom)
        val rm22 = (if (zZeroToOne) 1.0f else 2.0f) / (zNear - zFar)
        val rm30 = (left + right) / (left - right)
        val rm31 = (top + bottom) / (bottom - top)
        val rm32 = (if (zZeroToOne) zNear else (zFar + zNear)) / (zNear - zFar)
        // perform optimized multiplication
        // compute the last column first, because other columns do not depend on it
        dest.m30 = m00 * rm30 + m10 * rm31 + m20 * rm32 + m30
        dest.m31 = m01 * rm30 + m11 * rm31 + m21 * rm32 + m31
        dest.m32 = m02 * rm30 + m12 * rm31 + m22 * rm32 + m32
        dest.m33 = m03 * rm30 + m13 * rm31 + m23 * rm32 + m33
        dest.m00 = m00 * rm00
        dest.m01 = m01 * rm00
        dest.m02 = m02 * rm00
        dest.m03 = m03 * rm00
        dest.m10 = m10 * rm11
        dest.m11 = m11 * rm11
        dest.m12 = m12 * rm11
        dest.m13 = m13 * rm11
        dest.m20 = m20 * rm22
        dest.m21 = m21 * rm22
        dest.m22 = m22 * rm22
        dest.m23 = m23 * rm22
        return dest
    }

    /**
     * Apply an orthographic projection transformation for a right-handed coordinate system
     * using OpenGL's NDC z range of `[-1..+1]` to this matrix.
     *
     *
     * If `M` is `this` matrix and `O` the orthographic projection matrix,
     * then the new matrix will be `M * O`. So when transforming a
     * vector `v` with the new matrix by using `M * O * v`, the
     * orthographic projection transformation will be applied first!
     *
     *
     * Reference: [http://www.songho.ca](http://www.songho.ca/opengl/gl_projectionmatrix.html#ortho)
     *
     * @param left
     * the distance from the center to the left frustum edge
     * @param right
     * the distance from the center to the right frustum edge
     * @param bottom
     * the distance from the center to the bottom frustum edge
     * @param top
     * the distance from the center to the top frustum edge
     * @param zNear
     * near clipping plane distance
     * @param zFar
     * far clipping plane distance
     * @return this
     */
    fun ortho(left: Float, right: Float, bottom: Float, top: Float, zNear: Float, zFar: Float): Matrix4f {
        return ortho(left, right, bottom, top, zNear, zFar, false, this)
    }

    /**
     * Transform/multiply the given 3D-vector, as if it was a 4D-vector with w=1, by
     * this matrix and store the result in `dest`.
     *
     *
     * The given 3D-vector is treated as a 4D-vector with its w-component being 1.0, so it
     * will represent a position/location in 3D-space rather than a direction. This method is therefore
     * not suited for perspective projection transformations as it will not save the
     * `w` component of the transformed vector.
     *
     * @param v
     * the vector to transform
     * @param dest
     * will hold the result
     * @return dest
     */
    fun transformPosition(v: Vector3f, dest: Vector3f): Vector3f {
        val x = v.x
        val y = v.y
        val z = v.z
        dest.x = m00 * x + (m10 * y + (m20 * z + m30))
        dest.y = m01 * x + (m11 * y + (m21 * z + m31))
        dest.z = m02 * x + (m12 * y + (m22 * z + m32))
        return dest
    }

    /**
     * Store this matrix in column-major order into the given float array.
     *
     * @param arr
     * the array to write the matrix values into
     * @return the passed in array
     */
    fun get(arr: FloatArray): FloatArray {
        return get(arr, 0)
    }

    /**
     * Store this matrix in column-major order into the given float array at the given offset.
     *
     * @param arr
     * the array to write the matrix values into
     * @param offset
     * the offset into the array
     * @return the passed in array
     */
    fun get(arr: FloatArray, offset: Int): FloatArray {
        arr[offset + 0] = m00
        arr[offset + 1] = m01
        arr[offset + 2] = m02
        arr[offset + 3] = m03
        arr[offset + 4] = m10
        arr[offset + 5] = m11
        arr[offset + 6] = m12
        arr[offset + 7] = m13
        arr[offset + 8] = m20
        arr[offset + 9] = m21
        arr[offset + 10] = m22
        arr[offset + 11] = m23
        arr[offset + 12] = m30
        arr[offset + 13] = m31
        arr[offset + 14] = m32
        arr[offset + 15] = m33
        return arr
    }
}
