package simulation;

import it.polito.appeal.traci.*;

import java.awt.geom.Point2D;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kevin on 27-3-2017.
 */

public class SimulationController {
    private SumoTraciConnection connection;
    private File file = new File("sumofiles/config.sumo.cfg");
    private final String config_file = file.getAbsolutePath();
    private int carCount = 1;
    private boolean pulseNextTick = false;
    private boolean addCarsNextTick = false;
    private int vehiclesToAdd;
    private LoadBalancer loadBalancer;

    //Repos
    private Repository<Route> routeRepo;
    private Repository<VehicleType> vehicleTypeRepo;
    private Repository<Vehicle> vehicleRepo;
    private ArrayList<String> kentekens;

    public SimulationController() throws IOException {
        try {
            // Load kentekens
            LoadKentekens();

            // Connection settings
            connection = new SumoTraciConnection(config_file, 0);
            connection.addOption("start", "1");
            connection.runServer(true);

            // Gets repositories from the connection
            routeRepo = connection.getRouteRepository();
            vehicleTypeRepo = connection.getVehicleTypeRepository();

            // Start simulation
            startSimLoop();
            System.out.println("Simulation running");

            // Add first 10 vehicles
            addVehiclesToQueue(10);

            // Create load balancer
            loadBalancer = new LoadBalancer();

            // Start pulsing to tracking server every 30seconds
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            Runnable pulse = () -> {
                pulseNextTick = true;
            };
            executor.scheduleAtFixedRate(pulse, 5, 30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void LoadKentekens() throws IOException {
        //Get file from resources folder
        kentekens = new ArrayList<>();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("PersonenautoKentekens.csv").getFile());

        String csvFile = file.getAbsolutePath();
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] country = line.split(cvsSplitBy);
                kentekens.add(country[0]);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Pulses the server with Car/Lat/Lon info
     */
    private void pulseServer() {
        try {
            ArrayList<SimVehicle> simVehicles = new ArrayList<>();

            for (Vehicle v :  vehicleRepo.getAll().values()) {
                PositionConversionQuery positionConversionQuery = connection.getSimulationData().queryPositionConversion();
                positionConversionQuery.setPositionToConvert(v.getPosition(), true);

                Point2D locationPoint = positionConversionQuery.get();
                simVehicles.add(new SimVehicle(v, locationPoint));
            }
            loadBalancer.addLoad(simVehicles);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds the set amount of vehicles to the queue to be added in the simulation next tick
     *
     * @param amount
     */
    public void addVehiclesToQueue(int amount) {
        vehiclesToAdd = amount;
        addCarsNextTick = true;
    }

    /**
     * Generates random vehicles and adds them to the simulation.
     */
    public void addVehiclesToSimulation() {
        try {
            VehicleType type = vehicleTypeRepo.getByID("car");

            Random randomRoute = new Random();
            String routeId;

            for (int i = 0; i < vehiclesToAdd; i++) {
                routeId = String.valueOf(randomRoute.nextInt(5840));
                Route route = routeRepo.getByID(String.valueOf(routeId));

                AddVehicleQuery query = connection.queryAddVehicle();
                query.setVehicleData(kentekens.get(carCount), type, route, null, connection.getCurrentSimTime() + 1, 0, 0);
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
    private void startSimLoop() {
        new Thread(() -> simTimeTickLoop()).start();
    }

    /**
     * Controls the Time Ticks from the simulation.
     * Any requests or queries to the simulations should be done AFTER the nextSimStep.
     */
    private void simTimeTickLoop() {
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

