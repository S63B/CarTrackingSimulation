package simulation;

import it.polito.appeal.traci.Vehicle;

import java.awt.geom.Point2D;

/**
 * Created by Kevin on 28-3-2017.
 */
public class SimVehicle {
    Vehicle vehicle;

    public Point2D getLocation() {
        return location;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    Point2D location;

    public SimVehicle(Vehicle vehicle, Point2D location) {
        this.vehicle = vehicle;
        this.location = location;
    }

}
