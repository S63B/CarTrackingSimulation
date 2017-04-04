package simulation;

import it.polito.appeal.traci.*;

import java.awt.geom.Point2D;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kevin on 27-3-2017.
 */
public class SimulationController {
    private SumoTraciConnection connection;
    private int carCount = 1;
    private LoadBalancer loadBalancer;

    private List<Job> jobs;
    private List<Job> resultJobs;

    //Repos
    private Repository<Route> routeRepo;
    private Repository<VehicleType> vehicleTypeRepo;
    private Repository<Vehicle> vehicleRepo;
    private ArrayList<String> kentekens;

    public SimulationController() throws IOException {
        try {
            // Load kentekens
            LoadKentekens();

            // Jobs
            jobs = new ArrayList<>();
            resultJobs = new ArrayList<>();

            // Connection settings
            File file = new File("sumofiles/config.sumo.cfg");
            connection = new SumoTraciConnection(file.getAbsolutePath(), 0);
            connection.addOption("start", "1");
            connection.runServer(true);

            // Gets repositories from the connection
            routeRepo = connection.getRouteRepository();
            vehicleTypeRepo = connection.getVehicleTypeRepository();

            // Start simulation
            startSimLoop();
            System.out.println("simulation.Controllers.Simulation running");

            // Add first 10 vehicles
            addVehiclesToQueue(10);

            // Create load balancer
            loadBalancer = new LoadBalancer();

            // Start pulsing to tracking server every 30seconds
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            Runnable pulse = () -> {
                jobs.add(new Job(new Runnable() {
                    @Override
                    public void run() {
                        pulseServer();
                    }
                }));
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
        String line = "";
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] country = line.split(cvsSplitBy);
                kentekens.add(country[0]);
            }
        }
    }

    /**
     * Pulses the server with Car/Lat/Lon info
     */
    private void pulseServer() {
        loadBalancer.addLoad(getVehicles());
    }

    public List<SimVehicle> getVehiclesSafe() throws InterruptedException {
        Job<List<SimVehicle>> job = new Job<>();
        job.setRunnable(new Runnable() {
            @Override
            public void run() {
                job.setResult(getVehicles());
            }
        });

        resultJobs.add(job);

        while (job.getResult() == null){
            Thread.sleep(100);
        }

        return job.getResult();
    }

    private List<SimVehicle> getVehicles(){
        try {
                ArrayList<SimVehicle> simVehicles = new ArrayList<>();

            for (Vehicle v : vehicleRepo.getAll().values()) {
                PositionConversionQuery positionConversionQuery = connection.getSimulationData().queryPositionConversion();
                positionConversionQuery.setPositionToConvert(v.getPosition(), true);

                Point2D locationPoint = positionConversionQuery.get();
                simVehicles.add(new SimVehicle(v, locationPoint));
            }

            return simVehicles;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Adds the set amount of vehicles to the queue to be added in the simulation next tick
     *
     * @param amount
     */
    public void addVehiclesToQueue(int amount) {
        jobs.add(new Job(new Runnable() {
            @Override
            public void run() {
                addVehiclesToSimulation(amount);
            }
        }));
    }

    public void addVehicleToQueue(String licensePlate){
        jobs.add(new Job(new Runnable() {
            @Override
            public void run() {
                List<SimVehicle> vehicles = getVehicles();
                if (vehicles != null){
                    for (SimVehicle v : vehicles){
                        if (v.getLicensePlate().equals(licensePlate)){
                            return;
                        }
                    }
                }

                addVehicleToSimulation(licensePlate);
            }
        }));
    }

    /**
     * Generates random vehicles and adds them to the simulation.
     */
    public void addVehiclesToSimulation(int amount) {
        try {
            for (int i = 0; i < amount; i++) {
                addVehicleToSimulation(kentekens.get(carCount));
                carCount++;
            }

            connection.nextSimStep();
            vehicleRepo = connection.getVehicleRepository();

            //Starting the stimulation again.
            System.out.println("Total amount of cars spawned:" + amount);

            // connection.getSimulationData().queryPositionConversion().setPositionToConvert(meh.getPosition(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addVehicleToSimulation(String licensePlate){
        try{
            VehicleType type = vehicleTypeRepo.getByID("car");

            Random randomRoute = new Random();
            String routeId = String.valueOf(randomRoute.nextInt(5840));
            Route route = routeRepo.getByID(String.valueOf(routeId));

            AddVehicleQuery query = connection.queryAddVehicle();
            query.setVehicleData(licensePlate, type, route, null, connection.getCurrentSimTime() + 1, 0, 0);
            query.run();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void stop(){
        jobs.add(new Job(new Runnable() {
            @Override
            public void run() {
                try {
                    connection.close();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));
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

                while (resultJobs.size() > 0){
                    resultJobs.get(0).execute();
                    resultJobs.remove(0);
                }

                while (jobs.size() > 0){
                    jobs.get(0).execute();
                    jobs.remove(0);
                }
            } catch (Exception exception) {
                System.out.println("Error:" + exception.toString());
                break;
            }
        }
    }
}

