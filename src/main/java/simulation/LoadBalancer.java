package simulation;

import com.google.common.collect.Lists;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kevin on 28-3-2017
 */
public class LoadBalancer {

    ArrayList<ArrayList<SimVehicle>> vehicleLists;
    int pulseGroup;

    public LoadBalancer() {
        vehicleLists = new ArrayList<>();
        generateArrayLists();
        pulseGroup = 0;

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Runnable pulse = () -> pulseNextGroup();
        executor.scheduleAtFixedRate(pulse, 5, 2, TimeUnit.SECONDS);
    }

    public void addLoad(List<SimVehicle> vehicles) {
        int groupPartition = 0;
        int vehiclesPerGroup = (int) Math.ceil((double)vehicles.size() / 15);

        if (!vehicles.isEmpty()) {
            for (List<SimVehicle> partition : Lists.partition(vehicles, vehiclesPerGroup)) {
                vehicleLists.get(groupPartition).addAll(partition);
                groupPartition++;
            }
        } else {
            System.out.println("Vehiclelist Empty");
        }
    }

    private void pulseNextGroup() {
        for (SimVehicle v : vehicleLists.get(pulseGroup)) {
            String httpPost = "http://192.168.24.120:8080/pol?license_plate="+v.getVehicle().getID()+"&lat="+v.getLocation().getY()+"&lng="+v.getLocation().getX()+"&timestamp="+v.getTimestamp();
            try {
                HttpUriRequest request = new HttpPost(httpPost);
                HttpClientBuilder.create().build().execute(request);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        System.out.println("Group pulsed:" +pulseGroup + " Amount of cars:" + vehicleLists.get(pulseGroup).size());
        vehicleLists.get(pulseGroup).clear();

        if (pulseGroup < 14) {
            pulseGroup++;
        } else {
            pulseGroup = 0;
        }
    }

    private void generateArrayLists() {
        for (int i = 0; i < 15; i++) {
            ArrayList<SimVehicle> arrayList = new ArrayList();
            vehicleLists.add(i, arrayList);
        }
    }
}
