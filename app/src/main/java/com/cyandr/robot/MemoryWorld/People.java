package com.cyandr.robot.MemoryWorld;

public class People extends  Concept
{

    String Relationship;
    String Name;
    String GivenName;

    int Age;


    enum GENDER {
        MALE,
        FEMALE
    }

    public class Info {

        String telephoneNumber;
        Location address;
        GENDER gender;
        String Email;

    }

}
