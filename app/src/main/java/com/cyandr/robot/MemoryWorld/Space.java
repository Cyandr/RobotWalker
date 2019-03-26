package com.cyandr.robot.MemoryWorld;

public class Space extends BaseSpace {


    public class ReferencePoint extends BaseSpace
    {
        String Name;


        double offSetX,offSetY,offSetZ;

        double[] getGlobalCoord()
        {

            return new double[]{offSetX+X,offSetY+Y,offSetZ+Z};
        }
    }



}
