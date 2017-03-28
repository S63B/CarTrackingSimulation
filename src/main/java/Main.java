import simulation.SimulationController;

import java.io.IOException;
import java.util.*;

/**
 * Created by Uber on 21-3-2017
 */
public class Main {

    private static SimulationController simulationController;

    public static void main(String[] args) {
        try {
            simulationController = new SimulationController();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        if (simulationController != null){
            keepConsoleAlive();
        }
    }

    /**
     * Keeps the console alive so the user can keep adding cars.
     */
    private static void keepConsoleAlive() {
        Scanner scanner = new Scanner(System.in);

        while (true){
            System.out.println("How many cars do you want to spawn?");

            int createCars = scanner.nextInt();
            simulationController.addVehiclesToQueue(createCars);
        }
    }

}
