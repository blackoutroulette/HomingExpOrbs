package blackoutroulette.homingexporbs.math;

import net.minecraft.util.math.MathHelper;

public class Vec3d {

    public static final float PI = (float) Math.PI;

    /**
     * X coordinate of Vec3D
     */
    public double x;
    /**
     * Y coordinate of Vec3D
     */
    public double y;
    /**
     * Z coordinate of Vec3D
     */
    public double z;

    public Vec3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3d(net.minecraft.util.math.Vec3d v) {
        this(v.x, v.y, v.z);
    }

    public Vec3d(Vec3d v) {
        this(v.x, v.y, v.z);
    }

    public Vec3d clone() {
        return new Vec3d(this);
    }

    public double angleXZPlane() {
        return MathHelper.atan2(this.z, this.x);
    }

    public double angleXYPlane() {
        return MathHelper.atan2(this.y, this.x);
    }

    public void mul(double d) {
        this.x *= d;
        this.y *= d;
        this.z *= d;
    }

    public void mul(Vec3d v) {
        this.x *= v.x;
        this.y *= v.y;
        this.z *= v.z;
    }

    public void div(double d) {
        this.x /= d;
        this.y /= d;
        this.z /= d;
    }

    public void div(Vec3d v) {
        this.x /= v.x;
        this.y /= v.y;
        this.z /= v.z;
    }

    /**
     * Normalizes the vector to a length of 1 (except if it is the zero vector)
     */
    public void normalize() {
        final double l = this.length();
        if (l < 1.0E-4D) {
            this.x = 0;
            this.y = 0;
            this.z = 0;
        } else {
            this.div(l);
        }
    }

    public double dotProduct(Vec3d vec) {
        return this.x * vec.x + this.y * vec.y + this.z * vec.z;
    }

    /**
     * Returns a new vector with the result of this vector x the specified vector.
     */
    public void crossProduct(Vec3d vec) {
        final double x = this.x;
        final double y = this.y;

        this.x = y * vec.z - this.z * vec.y;
        this.y = this.z * vec.x - x * vec.z;
        this.z = x * vec.y - y * vec.x;
    }

    public void sub(Vec3d vec) {
        this.sub(vec.x, vec.y, vec.z);
    }

    public void sub(double x, double y, double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
    }

    public void add(Vec3d vec) {
        this.add(vec.x, vec.y, vec.z);
    }

    /**
     * Adds the specified x,y,z vector components to this vector and returns the resulting vector. Does not change this
     * vector.
     */
    public void add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
    }

    /**
     * Euclidean distance between this and the specified vector, returned as double.
     */
    public double distanceTo(Vec3d vec) {
        return MathHelper.sqrt(squareDistanceTo(vec));
    }

    /**
     * The square of the Euclidean distance between this and the specified vector.
     */
    public double squareDistanceTo(Vec3d v) {
        return squareDistanceTo(v.x, v.y, v.z);
    }

    public double squareDistanceTo(double x, double y, double z) {
        final double d0 = x - this.x;
        final double d1 = y - this.y;
        final double d2 = z - this.z;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    /**
     * Returns the length of the vector.
     */
    public double length() {
        return MathHelper.sqrt(lengthSquared());
    }

    public double lengthSquared() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (!(p_equals_1_ instanceof Vec3d)) {
            return false;
        } else {
            Vec3d vec3d = (Vec3d) p_equals_1_;

            if (Double.compare(vec3d.x, this.x) != 0) {
                return false;
            } else if (Double.compare(vec3d.y, this.y) != 0) {
                return false;
            } else {
                return Double.compare(vec3d.z, this.z) == 0;
            }
        }
    }

    public int hashCode() {
        long j = Double.doubleToLongBits(this.x);
        int i = (int) (j ^ j >>> 32);
        j = Double.doubleToLongBits(this.y);
        i = 31 * i + (int) (j ^ j >>> 32);
        j = Double.doubleToLongBits(this.z);
        i = 31 * i + (int) (j ^ j >>> 32);
        return i;
    }

    public String toString() {
        return "(x:" + this.x + ", y:" + this.y + ", z:" + this.z + ")";
    }

    public void rotX(float angle) {
        final double c = MathHelper.cos(angle);
        final double s = MathHelper.sin(angle);
        final double y = this.y;
        this.y = y * c - this.z * s;
        this.z = y * s + this.z * c;
    }

    public void rotY(float angle) {
        final double c = MathHelper.cos(angle);
        final double s = MathHelper.sin(angle);
        final double x = this.x;
        this.x = x * c + this.z * s;
        this.z = this.z * c - x * s;
    }

    public void rotZ(float angle) {
        final double c = MathHelper.cos(angle);
        final double s = MathHelper.sin(angle);
        final double x = this.x;
        this.x = x * c - this.y * s;
        this.y = x * s + this.y * c;
    }
}
