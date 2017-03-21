package com.s63b;

import de.tudresden.sumo.cmd.Simulation;
import de.tudresden.sumo.cmd.Vehicle;
import it.polito.appeal.traci.SumoTraciConnection;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class Main {
    static String sumo_bin;
    // todo You have to download the sumo files from the Drive and put the in the root folder of this project
    static String config_file;
    static Thread simTimeTickThread;
    static boolean simIsRunning;
    static int simTime;
    static int carsCreated = 0;
    static SumoTraciConnection conn;
    /* todo info over vehicle constructor
    Vehicle.add(Vehicle ID, typeID, routeID, departTime, pos, speed, byte (Depart)lane);
    */

    public static void main(String[] args) {
        Map<String, String> arguments = new HashMap<>();
        for (int i = 0; i < args.length; i++){
            if (args[i].contains("=")){
                String[] values = args[i].split("=");
                String key = values[0];
                String value = values[1];
                arguments.put(key, value);
            }else{
                System.out.println("Warning: Invalid argument '" + args[i] + "'");
            }
        }

        if (arguments.containsKey("sumo_bin")){
            sumo_bin = arguments.get("sumo_bin");
        }else{
            System.out.println("Missing required argument 'sumo_bin'");
            return;
        }

        if (arguments.containsKey("sumo_config")){
            config_file = arguments.get("sumo_config");
        }else{
            System.out.println("Missing required argument 'sumo_config'");
            return;
        }




        //start Simulation
        conn = new SumoTraciConnection(sumo_bin, config_file);
        //set some options
        conn.addOption("step-length", "0.1"); //timestep 100

        try {
            //start TraCI
            conn.runServer();
            conn.do_timestep();

            simIsRunning = true;
            Runnable simTimeTick = () -> simTimeTickLoop(conn);
            (simTimeTickThread = new Thread(simTimeTick)).start();
            System.out.println("Simulation running");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        spawnCars(conn, 100);

        keepConsoleAlive();
    }

    private static void keepConsoleAlive(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("How many cars do you want to spawn?");
        int createCars = scanner.nextInt();
        spawnCars(conn, createCars);
        keepConsoleAlive();
    }

    public static void simTimeTickLoop(SumoTraciConnection conn) {
        while (true) {
                try {
                    conn.do_timestep();
                    simTime = (Integer) conn.do_job_get(Simulation.getCurrentTime());
                } catch (Exception exception) {
                    System.out.println("Error:" + exception.toString());
                    break;
                }
        }
    }

    public static void spawnCars(SumoTraciConnection conn, int amountOfCars) {
        System.out.println("start of spawning cars");
        try {
            Random randomRoute = new Random();
            int route;
            for (int i = 0; i < amountOfCars; i++) {
                carsCreated++;
                route = randomRoute.nextInt(5840);
                conn.do_job_set(Vehicle.add("car" + carsCreated, "car", String.valueOf(route), simTime, 1, 0, (byte) 0));
                System.out.println("Vehicle created:"+carsCreated);
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }
        System.out.println("Done creating all cars. Amount created:"+amountOfCars);
    }

    public static String getResourceFolder(){
        String fileName = "application.properties";
        String resourcePath = Main.class.getClassLoader().getResource(fileName).getPath();
        return resourcePath.substring(1, resourcePath.length() - fileName.length());
    }
}