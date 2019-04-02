package com.cyandr.robot.OntActivity;


import com.cyandr.robot.BaseModel;
import com.cyandr.robot.MemoryWorld.Location;
import com.cyandr.robot.MemoryWorld.Movement;
import com.cyandr.robot.MemoryWorld.People;

import java.sql.Time;

public class OntActivity extends BaseModel
{

    Time time;
    Location space;
    People people;
    Object object;

    Movement movement;
    String state;


    // some definitions
    private static String personURI = "http://somewhere/JohnSmith";
    private static String fullName = "Xinhui Yan";


}
