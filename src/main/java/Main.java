import it.polito.appeal.traci.*;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by Uber on 21-3-2017.
 */
public class Main {
    static SumoTraciConnection connection;
    static File file = new File("sumofiles/config.sumo.cfg");
    static final String config_file = file.getAbsolutePath();
    static int carCount = 1;

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

            startSimLoop();
            System.out.println("Simulation running");

            addVehicles(10);

            keepConsoleAlive();
        } catch (Exception ex) {
            ex.printStackTrace();
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

    public static void startSimLoop(){
        Runnable simTimeTick = new Runnable() {
            public void run() {
                simTimeTickLoop();
            }
        };
        simTimeTickThread = new Thread(simTimeTick);
        simTimeTickThread.start();
    }

    public static void simTimeTickLoop() {
        while (true) {
            try {
                connection.nextSimStep();

                Vehicle car1 = vehicleRepo.getByID("car1");
                PositionConversionQuery positionConversionQuery = connection.getSimulationData().queryPositionConversion();
                positionConversionQuery.setPositionToConvert(car1.getPosition(),true);
                Point2D a = positionConversionQuery.get();

                System.out.println( "Lat: " + a.getY()+ " Lon: "+a.getX());

            } catch (Exception exception) {
                System.out.println("Error:" + exception.toString());
                break;
            }
        }
    }
}
