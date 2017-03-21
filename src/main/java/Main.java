import it.polito.appeal.traci.*;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Uber on 21-3-2017.
 */
public class Main {
    private static SumoTraciConnection connection;
    private static File file = new File("sumofiles/config.sumo.cfg");
    private static final String config_file = file.getAbsolutePath();
    private static int carCount = 1;
    private static boolean pulseNextTick = false;

    static Thread simTimeTickThread;

    //Repos
    static Repository<Route> routeRepo;
    static Repository<VehicleType> vehicleTypeRepo;
    static Repository<Vehicle> vehicleRepo;
    public static void main(String[] args) {

        try {
            connection = new SumoTraciConnection(config_file,0);
            connection.runServer(true);

            routeRepo = connection.getRouteRepository();
            vehicleTypeRepo = connection.getVehicleTypeRepository();
         //   Map<String, Vehicle> vehicles = vehicleRepo.getAll();
            // Start simulation
            startSimLoop();
            System.out.println("Simulation running");

            // Add first 10 vehicles
            addVehicles(10);

            // Start pulsing to tracking server every 30seconds
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            Runnable pulse = () -> {pulseNextTick = true;};
            executor.scheduleAtFixedRate(pulse,5, 5, TimeUnit.SECONDS);

            // Enables the user to keep adding cars
            keepConsoleAlive();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void pulseServer() {
        try {
            for (Vehicle v : vehicleRepo.getAll().values()) {
                PositionConversionQuery positionConversionQuery = connection.getSimulationData().queryPositionConversion();
                positionConversionQuery.setPositionToConvert(v.getPosition(), true);
                Point2D a = positionConversionQuery.get();
                System.out.println( v.getID() +"Lat/Lon: "+  a.getY()+" " + a.getX());
            }
            System.out.println("Amount of cars pulsed:"+vehicleRepo.getAll().values().size());
        }
        catch (IOException e){
            System.out.println(e);
        }
    }

    private static void addVehicles(int amount) {
        try {
            //Temporary stopping the simulation
            simTimeTickThread.stop();

            VehicleType type = vehicleTypeRepo.getByID("car");

            Random randomRoute = new Random();
            String routeId;

            for(int i = 0; i< amount; i++){
                routeId = String.valueOf(randomRoute.nextInt(5840));
                Route route = routeRepo.getByID(String.valueOf(routeId));

                AddVehicleQuery query = connection.queryAddVehicle();
                query.setVehicleData("car" + carCount, type, route, null, connection.getCurrentSimTime()+1, 0, 0);
                query.run();

                carCount++;
            }
            connection.nextSimStep();
            vehicleRepo = connection.getVehicleRepository();

            //Starting the stimulation again.
            startSimLoop();
            System.out.println("Total amount of cars spawned:"+ amount);


            // connection.getSimulationData().queryPositionConversion().setPositionToConvert(meh.getPosition(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void keepConsoleAlive() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("How many cars do you want to spawn?");

        int createCars = scanner.nextInt();
        addVehicles(createCars);

        keepConsoleAlive();
    }

    private static void startSimLoop(){
        Runnable simTimeTick = () -> simTimeTickLoop();
        simTimeTickThread = new Thread(simTimeTick);
        simTimeTickThread.start();
    }

    private static void simTimeTickLoop() {
        while (true) {
            try {
                connection.nextSimStep();
                if(pulseNextTick){
                    pulseServer();
                    pulseNextTick = false;
                }
            } catch (Exception exception) {
                System.out.println("Error:" + exception.toString());
                break;
            }
        }
    }
}
