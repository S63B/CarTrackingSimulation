import simulation.SimulationController;

import java.util.*;

/**
 * Created by Uber on 21-3-2017
 */
public class Main {

    public static void main(String[] args) {

        SimulationController.init();

        keepConsoleAlive();
    }

    /**
     * Keeps the console alive so the user can keep adding cars.
     */
    private static void keepConsoleAlive() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("How many cars do you want to spawn?");

        int createCars = scanner.nextInt();
        SimulationController.addVehiclesToQueue(createCars);

        keepConsoleAlive();
    }

}
