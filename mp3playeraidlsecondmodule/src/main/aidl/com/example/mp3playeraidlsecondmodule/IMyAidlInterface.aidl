// IMyAidlInterface.aidl
package com.example.mp3playeraidlsecondmodule;

// Declare any non-default types here with import statements

interface IMyAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
//    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString);

    int currentPosition();
    int getMaxDuration();
    void start();
    void stop();
    int getMediaStatus();
}