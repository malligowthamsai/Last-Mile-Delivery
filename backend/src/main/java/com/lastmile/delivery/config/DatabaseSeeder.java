package com.lastmile.delivery.config;

import com.lastmile.delivery.entity.*;
import com.lastmile.delivery.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private RateCardRepository rateCardRepository;

    @Autowired
    private CodSurchargeRepository codSurchargeRepository;

    @Autowired
    private AgentProfileRepository agentProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        logger.info("🌱 Starting database seeding...");

        // 1. Seed Admin User
        userRepository.findByEmail("admin@lastmile.com").orElseGet(() -> {
            User admin = User.builder()
                    .name("Super Admin")
                    .email("admin@lastmile.com")
                    .phone("9999999999")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            logger.info("✅ Admin seeded: admin@lastmile.com");
            return admin;
        });

        // 2. Seed Sample Customer
        userRepository.findByEmail("customer@test.com").orElseGet(() -> {
            User customer = User.builder()
                    .name("Test Customer")
                    .email("customer@test.com")
                    .phone("8888888888")
                    .passwordHash(passwordEncoder.encode("customer123"))
                    .role(Role.CUSTOMER)
                    .build();
            userRepository.save(customer);
            logger.info("✅ Test Customer seeded: customer@test.com");
            return customer;
        });

        // 3. Seed Zones
        List<String> zoneNames = Arrays.asList(
                "North Mumbai", "South Mumbai", "Thane", "Navi Mumbai", "Pune",
                "Bengaluru Central", "Bengaluru East", "Bengaluru North",
                "Hyderabad Central", "Hyderabad West", "Vijayawada"
        );

        Map<String, Zone> zoneMap = new HashMap<>();
        for (String name : zoneNames) {
            Zone zone = zoneRepository.findByName(name).orElseGet(() -> {
                Zone z = Zone.builder().name(name).build();
                return zoneRepository.save(z);
            });
            zoneMap.put(name, zone);
        }
        logger.info("✅ Zones seeded: {} zones", zoneMap.size());

        // 4. Seed Areas
        List<AreaSeed> areas = Arrays.asList(
                // North Mumbai
                new AreaSeed("Borivali", "400066", "North Mumbai"),
                new AreaSeed("Kandivali", "400067", "North Mumbai"),
                new AreaSeed("Malad", "400064", "North Mumbai"),
                new AreaSeed("Andheri", "400069", "North Mumbai"),
                // South Mumbai
                new AreaSeed("Colaba", "400005", "South Mumbai"),
                new AreaSeed("Fort", "400001", "South Mumbai"),
                new AreaSeed("Worli", "400018", "South Mumbai"),
                new AreaSeed("Dadar", "400014", "South Mumbai"),
                // Thane
                new AreaSeed("Thane West", "400601", "Thane"),
                new AreaSeed("Thane East", "400603", "Thane"),
                new AreaSeed("Mulund", "400080", "Thane"),
                // Navi Mumbai
                new AreaSeed("Vashi", "400703", "Navi Mumbai"),
                new AreaSeed("Nerul", "400706", "Navi Mumbai"),
                new AreaSeed("Kharghar", "410210", "Navi Mumbai"),
                // Pune
                new AreaSeed("Shivajinagar", "411005", "Pune"),
                new AreaSeed("Hadapsar", "411028", "Pune"),
                new AreaSeed("Kothrud", "411038", "Pune"),
                // Bengaluru Central
                new AreaSeed("MG Road", "560001", "Bengaluru Central"),
                new AreaSeed("Shivajinagar", "560051", "Bengaluru Central"),
                new AreaSeed("Richmond Town", "560025", "Bengaluru Central"),
                new AreaSeed("Indiranagar", "560038", "Bengaluru Central"),
                // Bengaluru East
                new AreaSeed("Whitefield", "560066", "Bengaluru East"),
                new AreaSeed("Marathahalli", "560037", "Bengaluru East"),
                new AreaSeed("Electronic City", "560100", "Bengaluru East"),
                new AreaSeed("BTM Layout", "560076", "Bengaluru East"),
                // Bengaluru North
                new AreaSeed("Hebbal", "560024", "Bengaluru North"),
                new AreaSeed("Yeshwanthpur", "560022", "Bengaluru North"),
                new AreaSeed("Yelahanka", "560064", "Bengaluru North"),
                // Hyderabad Central
                new AreaSeed("Secunderabad", "500003", "Hyderabad Central"),
                new AreaSeed("Begumpet", "500016", "Hyderabad Central"),
                new AreaSeed("Himayatnagar", "500029", "Hyderabad Central"),
                // Hyderabad West
                new AreaSeed("HITEC City", "500081", "Hyderabad West"),
                new AreaSeed("Gachibowli", "500032", "Hyderabad West"),
                new AreaSeed("Kondapur", "500084", "Hyderabad West"),
                // Vijayawada
                new AreaSeed("Benz Circle", "520010", "Vijayawada"),
                new AreaSeed("Governorpet", "520002", "Vijayawada"),
                new AreaSeed("Vijayawada One Town", "520001", "Vijayawada"),
                new AreaSeed("Patamata", "520007", "Vijayawada")
        );

        int areasCreated = 0;
        for (AreaSeed as : areas) {
            if (areaRepository.findByPincode(as.pincode).isEmpty()) {
                Zone z = zoneMap.get(as.zoneName);
                if (z != null) {
                    areaRepository.save(Area.builder()
                            .name(as.name)
                            .pincode(as.pincode)
                            .zone(z)
                            .build());
                    areasCreated++;
                }
            }
        }
        logger.info("✅ Areas seeded: {} mapped areas", areasCreated);

        // 5. Seed Rate Cards Matrix
        int rateCardsCreated = 0;
        List<Zone> allZones = new ArrayList<>(zoneMap.values());
        for (Zone fromZone : allZones) {
            for (Zone toZone : allZones) {
                boolean isIntra = fromZone.getId().equals(toZone.getId());
                for (OrderType orderType : OrderType.values()) {
                    if (rateCardRepository.findByZoneFromIdAndZoneToIdAndOrderType(
                            fromZone.getId(), toZone.getId(), orderType).isEmpty()) {
                        
                        double ratePerKg = isIntra 
                                ? (orderType == OrderType.B2B ? 25.0 : 35.0) 
                                : (orderType == OrderType.B2B ? 40.0 : 55.0);
                        double minCharge = isIntra 
                                ? (orderType == OrderType.B2B ? 50.0 : 60.0) 
                                : (orderType == OrderType.B2B ? 80.0 : 100.0);

                        RateCard card = RateCard.builder()
                                .zoneFrom(fromZone)
                                .zoneTo(toZone)
                                .orderType(orderType)
                                .ratePerKg(ratePerKg)
                                .minCharge(minCharge)
                                .build();
                        rateCardRepository.save(card);
                        rateCardsCreated++;
                    }
                }
            }
        }
        logger.info("✅ Rate Cards seeded: {} cards", rateCardsCreated);

        // 6. Seed COD Surcharges
        if (codSurchargeRepository.findByOrderType(OrderType.B2C).isEmpty()) {
            codSurchargeRepository.save(CodSurcharge.builder().orderType(OrderType.B2C).surchargeFlat(30.0).build());
        }
        if (codSurchargeRepository.findByOrderType(OrderType.B2B).isEmpty()) {
            codSurchargeRepository.save(CodSurcharge.builder().orderType(OrderType.B2B).surchargeFlat(50.0).build());
        }
        logger.info("✅ COD Surcharges seeded (B2C=₹30, B2B=₹50)");

        // 7. Seed Sample Agents
        Zone northMumbai = zoneMap.get("North Mumbai");
        if (northMumbai != null && userRepository.findByEmail("agent1@lastmile.com").isEmpty()) {
            User agent1 = User.builder()
                    .name("Raju Kumar")
                    .email("agent1@lastmile.com")
                    .phone("7777777777")
                    .passwordHash(passwordEncoder.encode("agent123"))
                    .role(Role.AGENT)
                    .build();
            agent1 = userRepository.save(agent1);

            agentProfileRepository.save(AgentProfile.builder()
                    .user(agent1)
                    .zone(northMumbai)
                    .isAvailable(true)
                    .build());
            logger.info("✅ Agent 1 seeded: Raju Kumar (North Mumbai)");
        }

        Zone southMumbai = zoneMap.get("South Mumbai");
        if (southMumbai != null && userRepository.findByEmail("agent2@lastmile.com").isEmpty()) {
            User agent2 = User.builder()
                    .name("Priya Singh")
                    .email("agent2@lastmile.com")
                    .phone("6666666666")
                    .passwordHash(passwordEncoder.encode("agent123"))
                    .role(Role.AGENT)
                    .build();
            agent2 = userRepository.save(agent2);

            agentProfileRepository.save(AgentProfile.builder()
                    .user(agent2)
                    .zone(southMumbai)
                    .isAvailable(true)
                    .build());
            logger.info("✅ Agent 2 seeded: Priya Singh (South Mumbai)");
        }

        logger.info("🎉 Database seeding complete!");
    }

    private static class AreaSeed {
        String name;
        String pincode;
        String zoneName;

        AreaSeed(String name, String pincode, String zoneName) {
            this.name = name;
            this.pincode = pincode;
            this.zoneName = zoneName;
        }
    }
}
