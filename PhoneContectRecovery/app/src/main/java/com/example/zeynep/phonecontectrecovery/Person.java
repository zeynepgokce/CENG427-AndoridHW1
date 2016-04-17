package com.example.zeynep.phonecontectrecovery;

/**
 * Created by zeynep on 25.03.2016.
 */

public class Person {
    private String name_surname;
    private String number;
    private int pictureID;

    public Person(String name_surname, String number,int pictureID ) {
        super();
        this.name_surname = name_surname;
        this.number = number;
        this.pictureID =pictureID;
    }

    @Override
    public String toString() {
        return name_surname;
    }

    public String getName() {
        return name_surname;
    }

    public void setName(String name_surname) {
        this.name_surname = name_surname;
    }

    public String getNumber(){
        return number;
    }

    public void setNumber(String phoneNumber)
    {
        number = phoneNumber;
    }
}