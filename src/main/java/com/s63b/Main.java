package com.s63b;

import de.tudresden.ws.container.*;
import de.tudresden.sumo.util.*;
import de.tudresden.sumo.cmd.Simulation;
import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.util.Sumo;
import de.tudresden.sumo.util.SumoCommand;
import de.tudresden.ws.container.SumoGeometry;
import de.tudresden.ws.container.SumoPosition2D;
import it.polito.appeal.traci.SumoTraciConnection;
import it.polito.appeal.traci.TraCIException;

import java.io.File;
import java.util.Random;
import java.util.Scanner;

public class Main {
    static String sumo_bin = "C:/Program Files (x86)/DLR/Sumo/bin/sumo-gui.exe";
    static File file = new File("sumofiles/config.sumo.cfg");
    static final String config_file = file.getAbsolutePath();
    static Thread simTimeTickThread;
    static boolean simIsRunning;
    static int simTime;
    static int carsCreated = 0;
    /* todo info over vehicle constructor
    Vehicle.add(Vehicle ID, typeID, routeID, departTime, pos, speed, byte (Depart)lane);
    */

    public static void main(String[] args) {
        //start Simulation
        SumoTraciConnection conn = new SumoTraciConnection(sumo_bin, config_file);
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
        Scanner scanner = new Scanner(System.in);
        System.out.println("How many cars do you want to spawn?");
        int createCars = scanner.nextInt();
        spawnCars(conn, createCars);
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
}