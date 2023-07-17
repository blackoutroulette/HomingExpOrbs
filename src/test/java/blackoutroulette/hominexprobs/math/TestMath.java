package blackoutroulette.hominexprobs.math;


import blackoutroulette.homingexporbs.math.Vec3d;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class TestMath {

    @Test
    public void testRotX(){
        Vec3d vec = new Vec3d(0, 1, 0);
        vec.rotX((float)Math.PI / 2F);
        assertEquals(0, vec.x, 1E-12);
        assertEquals(0, vec.y, 1E-12);
        assertEquals(1, vec.z, 1E-12);
    }

    @Test
    public void testRotY(){
        Vec3d vec = new Vec3d(1, 0, 0);
        vec.rotY((float)-Math.PI / 2F);
        assertEquals(0, vec.x, 1E-12);
        assertEquals(0, vec.y, 1E-12);
        assertEquals(1, vec.z, 1E-12);
    }

    @Test
    public void testRotZ(){
        Vec3d vec = new Vec3d(0, 1, 0);
        vec.rotZ((float)-Math.PI / 2F);
        assertEquals(1, vec.x, 1E-12);
        assertEquals(0, vec.y, 1E-12);
        assertEquals(0, vec.z, 1E-12);
    }

    @Test
    public void testAtan2(){
        Vec3d vec = new Vec3d(0,0,1);
        assertEquals(Math.PI / 2D, vec.angleXZPlane(), 1E-12);

        vec = new Vec3d(0, 1, 0);
        assertEquals(0D, vec.angleXZPlane());

        vec = new Vec3d(1, 0, 0);
        assertEquals(0D, vec.angleXZPlane());
    }
}

