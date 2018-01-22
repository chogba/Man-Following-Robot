package com.example.chogba.yolo;

/**
 * Created by chogba on 4/4/17.
 */

public enum TrackingRobotState {
    NoMove(0),
    DriveForwardBackward(1),
    SpinLeftRight(2),
    Lost(3);

    public final int id;

    TrackingRobotState(int id){
        this.id=id;
    }
}
