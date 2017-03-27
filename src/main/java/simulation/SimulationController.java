package simulation;

import it.polito.appeal.traci.*;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kevin on 27-3-2017.
 */

public class SimulationController {
    private static SumoTraciConnection connection;
    private static File file = new File("sumofiles/config.sumo.cfg");
    private static final String config_file = file.getAbsolutePath();
    private static int carCount = 1;
    private static boolean pulseNextTick = false;
    private static boolean addCarsNextTick = false;
    private static int vehiclesToAdd;
    private static boolean initiated = false;

    static Thread simTimeTickThread;

    //Repos
    static Repository<Route> routeRepo;
    static Repository<VehicleType> vehicleTypeRepo;
    static Repository<Vehicle> vehicleRepo;

    public static void init(){
        if(!initiated) {
            try {
                // Connection settings
                connection = new SumoTraciConnection(config_file, 0);
                connection.runServer(true);

                // Gets repositories from the connection
                routeRepo = connection.getRouteRepository();
                vehicleTypeRepo = connection.getVehicleTypeRepository();

                // Start simulation
                startSimLoop();
                System.out.println("Simulation running");

                // Add first 10 vehicles
                addVehiclesToQueue(10);

                // Start pulsing to tracking server every 30seconds
                ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
                Runnable pulse = () -> {
                    pulseNextTick = true;
                };
                executor.scheduleAtFixedRate(pulse, 5, 10, TimeUnit.SECONDS);
            } catch (IOException e) {
                System.out.println(e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            initiated = true;
        }
    }

    /**
     * Pulses the server with Car/Lat/Lon info
     */
    private static void pulseServer() {
        try {
            for (Vehicle v : vehicleRepo.getAll().values()) {
                PositionConversionQuery positionConversionQuery = connection.getSimulationData().queryPositionConversion();
                positionConversionQuery.setPositionToConvert(v.getPosition(), true);
                Point2D a = positionConversionQuery.get();
                System.out.println(v.getID() + "Lat/Lon: " + a.getY() + " " + a.getX());
            }
            System.out.println("Amount of cars pulsed:" + vehicleRepo.getAll().values().size());
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Adds the set amount of vehicles to the queue to be added in the simulation next tick
     *
     * @param amount
     */
    public static void addVehiclesToQueue(int amount) {
        vehiclesToAdd = amount;
        addCarsNextTick = true;
    }

    /**
     * Generates random vehicles and adds them to the simulation.
     */
    public static void addVehiclesToSimulation() {
        try {
            VehicleType type = vehicleTypeRepo.getByID("car");

            Random randomRoute = new Random();
            String routeId;

            for (int i = 0; i < vehiclesToAdd; i++) {
                routeId = String.valueOf(randomRoute.nextInt(5840));
                Route route = routeRepo.getByID(String.valueOf(routeId));

                AddVehicleQuery query = connection.queryAddVehicle();
                query.setVehicleData("car" + carCount, type, route, null, connection.getCurrentSimTime() + 1, 0, 0);
                query.run();

                carCount++;
            }
            connection.nextSimStep();
            vehicleRepo = connection.getVehicleRepository();

            //Starting the stimulation again.
            System.out.println("Total amount of cars spawned:" + vehiclesToAdd);


            // connection.getSimulationData().queryPositionConversion().setPositionToConvert(meh.getPosition(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stars the simTimeTickThread.
     */
    private static void startSimLoop() {
        simTimeTickThread = new Thread(() -> simTimeTickLoop());
        simTimeTickThread.start();
    }

    /**
     * Controls the Time Ticks from the simulation.
     * Any requests or queries to the simulations should be done AFTER the nextSimStep.
     */
    private static void simTimeTickLoop() {
        while (true) {
            try {
                connection.nextSimStep();
                if (pulseNextTick) {
                    pulseNextTick = false;
                    pulseServer();
                }
                if (addCarsNextTick) {
                    addCarsNextTick = false;
                    addVehiclesToSimulation();
                }
            } catch (Exception exception) {
                System.out.println("Error:" + exception.toString());
                break;
            }
        }
    }
}

