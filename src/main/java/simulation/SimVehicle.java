package simulation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.polito.appeal.traci.Vehicle;

import java.awt.geom.Point2D;

/**
 * Created by Kevin on 28-3-2017.
 */
public class SimVehicle {
    @JsonIgnore
    private Vehicle vehicle;
    @JsonIgnore
    private Point2D location;

    private String licensePlate;

    @JsonIgnore
    private long timestamp;

    public SimVehicle(Vehicle vehicle, Point2D location) {
        this.vehicle = vehicle;
        this.location = location;
        this.licensePlate = vehicle.getID();
        this.timestamp = System.currentTimeMillis();
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public Point2D getLocation() {
        return location;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public long getTimestamp(){
        return timestamp;
    }
}
