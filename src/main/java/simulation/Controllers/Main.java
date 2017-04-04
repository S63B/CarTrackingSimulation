package simulation.Controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import simulation.SimulationController;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.Callable;

import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.REQUEST_TIMEOUT;


/**
 * Created by Uber on 21-3-2017
 */
@RestController
public class Main {

    private SimulationController simulationController;

    private boolean restarting;

    @PostConstruct
    public void init() {
        try {
            simulationController = new SimulationController();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @RequestMapping(value = "/restart", method = RequestMethod.POST)
    public Response restart() {
        if (!restarting){
            restarting = true;
                Callable<Response> callable = () -> {
                    try {
                        if (simulationController != null){
                            simulationController.stop();
                            killApplication();
                        }

                        Thread.sleep(2000);

                        simulationController = new SimulationController();
                        restarting = false;

                        return Response.status(OK).build();
                    } catch (IOException | InterruptedException e) {
                        return Response.status(REQUEST_TIMEOUT).entity(e).build();
                    }
                };
            try {
                return callable.call();
            } catch (Exception e) {
                return Response.status(REQUEST_TIMEOUT).entity(e).build();
            }
        }else{
            return Response.status(REQUEST_TIMEOUT).entity("Already restarting, please wait").build();
        }

    }

    @RequestMapping(value = "/vehicle", method = RequestMethod.POST)
    public Response addVehicle(@RequestParam String licensePlate) {
        simulationController.addVehicleToQueue(licensePlate);
        return Response.status(OK).build();
    }

    @RequestMapping(value = "/vehicles", method = RequestMethod.POST)
    public Response addVehicles(@RequestParam int amount) {
        simulationController.addVehiclesToQueue(amount);
        return Response.status(OK).build();
    }

    @RequestMapping(value = "/vehicles", method = RequestMethod.GET)
    public Response getVehicles() {
        try {
            return Response.status(OK).entity(simulationController.getVehiclesSafe()).build();
        } catch (InterruptedException e) {
            return Response.status(REQUEST_TIMEOUT).entity(e).build();
        }
    }

    private void killApplication() throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder("taskkill", "/f", "/t", "/im", "sumo-gui.exe");
        builder.start();
    }

}
