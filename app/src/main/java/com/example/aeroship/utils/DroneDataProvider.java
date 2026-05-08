package com.example.aeroship.utils;

import com.example.aeroship.models.DronePackage;

import java.util.HashMap;
import java.util.Map;

public class DroneDataProvider {

    public static Map<String, Map<String, DronePackage>> getDroneData() {

        Map<String, Map<String, DronePackage>> sectorMap = new HashMap<>();

        Map<String, DronePackage> photography = new HashMap<>();
        photography.put("Low", new DronePackage(
                "Photography Basic",
                "1080p HD Camera\n25 min flight\n3km range\nManual Control",
                2000
        ));
        photography.put("Mid", new DronePackage(
                "Photography Advanced",
                "4K Camera\n40 min flight\n7km range\nGPS Hover\nFollow Mode",
                4000
        ));
        photography.put("High", new DronePackage(
                "Photography Pro",
                "8K Camera\n60 min flight\n15km range\nAI Tracking\nObstacle Avoidance",
                7000
        ));
        sectorMap.put("Photography", photography);

        Map<String, DronePackage> wedding = new HashMap<>();
        wedding.put("Low", new DronePackage(
                "Wedding Basic",
                "2K Camera\n30 min flight\nSlow Motion",
                3000
        ));
        wedding.put("Mid", new DronePackage(
                "Wedding Advanced",
                "4K HDR\n45 min flight\nSmart Orbit Mode",
                5000
        ));
        wedding.put("High", new DronePackage(
                "Wedding Cinematic",
                "8K HDR\n60 min flight\nAI Couple Tracking\nCinematic Filters",
                9000
        ));
        sectorMap.put("Wedding", wedding);

        Map<String, DronePackage> farming = new HashMap<>();
        farming.put("Low", new DronePackage(
                "Farming Basic",
                "5L Spray Tank\n20 min operation\nManual Spray",
                2500
        ));
        farming.put("Mid", new DronePackage(
                "Farming Advanced",
                "10L Tank\nGPS Spray\nSoil Scan",
                4500
        ));
        farming.put("High", new DronePackage(
                "Farming Smart",
                "15L Tank\nAI Disease Detection\nAuto Mapping",
                8000
        ));
        sectorMap.put("Farming", farming);

        Map<String, DronePackage> surveillance = new HashMap<>();
        surveillance.put("Low", new DronePackage(
                "Surveillance Basic",
                "HD Camera\nNight Vision\n5km Range",
                3000
        ));
        surveillance.put("Mid", new DronePackage(
                "Surveillance Advanced",
                "4K Zoom\nThermal Vision\n10km Range",
                5500
        ));
        surveillance.put("High", new DronePackage(
                "Surveillance AI",
                "AI Motion Detection\nInfrared Fusion\n20km Range",
                10000
        ));
        sectorMap.put("Surveillance", surveillance);

        Map<String, DronePackage> delivery = new HashMap<>();
        delivery.put("Low", new DronePackage(
                "Delivery Basic",
                "2kg Payload\n20 min flight\nManual Drop",
                2000
        ));
        delivery.put("Mid", new DronePackage(
                "Delivery Advanced",
                "5kg Payload\n35 min flight\nGPS Navigation",
                4000
        ));
        delivery.put("High", new DronePackage(
                "Delivery Heavy",
                "10kg Payload\n50 min flight\nAI Route Optimization",
                7500
        ));
        sectorMap.put("Delivery", delivery);

        Map<String, DronePackage> construction = new HashMap<>();
        construction.put("Low", new DronePackage(
                "Construction Basic",
                "HD Mapping\n25 min flight\nManual Survey",
                3000
        ));
        construction.put("Mid", new DronePackage(
                "Construction Advanced",
                "4K Inspection\n3D Mapping\nLaser Sensor",
                5000
        ));
        construction.put("High", new DronePackage(
                "Construction Pro",
                "AI Structural Scan\n8K Lens\nBIM Support",
                9000
        ));
        sectorMap.put("Construction", construction);

        Map<String, DronePackage> event = new HashMap<>();
        event.put("Low", new DronePackage(
                "Event Basic",
                "2K Live\n30 min flight\nWide Capture",
                2500
        ));
        event.put("Mid", new DronePackage(
                "Event Advanced",
                "4K Broadcast\n45 min flight\nMulti Angle",
                4500
        ));
        event.put("High", new DronePackage(
                "Event Pro",
                "8K Live\nAI Crowd Tracking\n60 min flight",
                8500
        ));
        sectorMap.put("Event", event);

        return sectorMap;
    }
}
