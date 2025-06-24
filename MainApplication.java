import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.*;

public class MainApplication extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private Map<String, Double> marketPrices;
    private Graph roadNetwork;

    private class Location {
        String name;
        double latitude;
        double longitude;
        String type; 
        String address;
        String contact;
        String services; 

        Location(String name, double latitude, double longitude, String type, String address, String contact, String services) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.type = type;
            this.address = address;
            this.contact = contact;
            this.services = services;
        }
    }

    private class Edge {
        Location destination;
        double distance;

        Edge(Location destination, double distance) {
            this.destination = destination;
            this.distance = distance;
        }
    }

    private class Graph {
        Map<Location, List<Edge>> adjacencyList;

        Graph() {
            adjacencyList = new HashMap<>();
        }

        void addLocation(Location location) {
            adjacencyList.putIfAbsent(location, new ArrayList<>());
        }

        void addRoad(Location source, Location destination) {
            double distance = calculateDistance(
                source.latitude, source.longitude,
                destination.latitude, destination.longitude
            );
            adjacencyList.get(source).add(new Edge(destination, distance));
            adjacencyList.get(destination).add(new Edge(source, distance));
        }

        List<Location> findNearestCenters(Location start, int limit) {
            Map<Location, Double> distances = new HashMap<>();
            Map<Location, Location> previousLocations = new HashMap<>();
            PriorityQueue<Location> queue = new PriorityQueue<>(
                Comparator.comparingDouble(distances::get)
            );

            // Initialize distances
            for (Location loc : adjacencyList.keySet()) {
                distances.put(loc, Double.POSITIVE_INFINITY);
            }
            distances.put(start, 0.0);
            queue.add(start);

            while (!queue.isEmpty()) {
                Location current = queue.poll();

                for (Edge edge : adjacencyList.get(current)) {
                    Location neighbor = edge.destination;
                    double newDistance = distances.get(current) + edge.distance;

                    if (newDistance < distances.get(neighbor)) {
                        distances.put(neighbor, newDistance);
                        previousLocations.put(neighbor, current);
                        queue.remove(neighbor);
                        queue.add(neighbor);
                    }
                }
            }

            // Filter and sort government centers
            List<Location> centers = new ArrayList<>();
            for (Location loc : adjacencyList.keySet()) {
                if (loc.type.equals("GOVT_CENTER")) {
                    centers.add(loc);
                }
            }

            centers.sort(Comparator.comparingDouble(distances::get));
            return centers.subList(0, Math.min(limit, centers.size()));
        }
    }

    private String[] districts = {
    "Almora", "Chamoli","Bageshwar", "Champawat", "Dehradun",
    "Haridwar", "Nainital", "Pauri Garhwal", "Pithoragarh", 
    "Rudraprayag", "Tehri Garhwal", "Udham Singh Nagar", "Uttarkashi"
    };

    private String[] crops = {"Wheat", "Rice", "Sugarcane", "Tomato", "Maize"};

    public MainApplication() {
        setTitle("Crop Sowing Advisor");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        initializeMarketPrices();
        initializeRoadNetwork();

        quickSort(districts, 0, districts.length - 1);
        quickSort(crops, 0, crops.length - 1);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(240, 240, 240));

        mainPanel.add(createMainMenu(), "menu");

        add(mainPanel);
        setVisible(true);
    }

    private void initializeMarketPrices() {
        marketPrices = new HashMap<>();
        // Simulated market prices - in real app, this would come from a market API
        for (String crop : crops) {
            marketPrices.put(crop, Math.random() * 1000 + 500);
        }
    }

    private void initializeRoadNetwork() {
        roadNetwork = new Graph();
        Map<String, Location> districtLocations = new HashMap<>();

        // Initialize all district locations with actual coordinates
        districtLocations.put("Almora", new Location("Almora", 29.5973, 79.6609, "DISTRICT", "Almora City", "", ""));
        districtLocations.put("Chamoli", new Location("Chamoli", 30.4030, 79.3207, "DISTRICT", "Chamoli City", "", ""));
        districtLocations.put("Bageshwar", new Location("Bageshwar", 29.8367, 79.7696, "DISTRICT", "Bageshwar City", "", ""));
        districtLocations.put("Champawat", new Location("Champawat", 29.3355, 80.0784, "DISTRICT", "Champawat City", "", ""));
        districtLocations.put("Dehradun", new Location("Dehradun", 30.3165, 78.0322, "DISTRICT", "Dehradun City", "", ""));
        districtLocations.put("Haridwar", new Location("Haridwar", 29.9457, 78.1642, "DISTRICT", "Haridwar City", "", ""));
        districtLocations.put("Nainital", new Location("Nainital", 29.3919, 79.4542, "DISTRICT", "Nainital City", "", ""));
        districtLocations.put("Pauri Garhwal", new Location("Pauri Garhwal", 30.0856, 78.7776, "DISTRICT", "Pauri City", "", ""));
        districtLocations.put("Pithoragarh", new Location("Pithoragarh", 29.5820, 80.2185, "DISTRICT", "Pithoragarh City", "", ""));
        districtLocations.put("Rudraprayag", new Location("Rudraprayag", 30.2847, 78.9839, "DISTRICT", "Rudraprayag City", "", ""));
        districtLocations.put("Tehri Garhwal", new Location("Tehri Garhwal", 30.3833, 78.4800, "DISTRICT", "Tehri City", "", ""));
        districtLocations.put("Udham Singh Nagar", new Location("Udham Singh Nagar", 29.0274, 79.5280, "DISTRICT", "USN City", "", ""));
        districtLocations.put("Uttarkashi", new Location("Uttarkashi", 30.7292, 78.4439, "DISTRICT", "Uttarkashi City", "", ""));

        // Add district locations to graph
        for (Location loc : districtLocations.values()) {
            roadNetwork.addLocation(loc);
        }

        // Initialize government centers for each district
        for (String district : districts) {
            Location districtLoc = districtLocations.get(district);
            if (districtLoc != null) {
                // Add KVK
                Location kvk = new Location(
                    "Krishi Vigyan Kendra - " + district,
                    districtLoc.latitude + 0.01,
                    districtLoc.longitude + 0.01,
                    "GOVT_CENTER",
                    "Main Road, " + district,
                    "1800-XXX-XXXX",
                    "Crop Research, Training, Soil Testing"
                );
                roadNetwork.addLocation(kvk);
                roadNetwork.addRoad(districtLoc, kvk);

                // Add Agriculture Department
                Location agriDept = new Location(
                    "Agriculture Department - " + district,
                    districtLoc.latitude - 0.01,
                    districtLoc.longitude - 0.01,
                    "GOVT_CENTER",
                    "Government Complex, " + district,
                    "1800-XXX-XXXX",
                    "Subsidies, Schemes, Technical Support"
                );
                roadNetwork.addLocation(agriDept);
                roadNetwork.addRoad(districtLoc, agriDept);

                // Add Soil Testing Lab
                Location soilLab = new Location(
                    "Soil Testing Lab - " + district,
                    districtLoc.latitude + 0.02,
                    districtLoc.longitude - 0.02,
                    "GOVT_CENTER",
                    "Research Complex, " + district,
                    "1800-XXX-XXXX",
                    "Soil Analysis, Fertilizer Recommendations"
                );
                roadNetwork.addLocation(soilLab);
                roadNetwork.addRoad(districtLoc, soilLab);

                // Add Horticulture Department
                Location hortDept = new Location(
                    "Horticulture Department - " + district,
                    districtLoc.latitude - 0.02,
                    districtLoc.longitude + 0.02,
                    "GOVT_CENTER",
                    "Horticulture Complex, " + district,
                    "1800-XXX-XXXX",
                    "Fruit/Vegetable Cultivation, Plant Protection"
                );
                roadNetwork.addLocation(hortDept);
                roadNetwork.addRoad(districtLoc, hortDept);

                // Add Seed Testing Lab
                Location seedLab = new Location(
                    "Seed Testing Lab - " + district,
                    districtLoc.latitude + 0.03,
                    districtLoc.longitude + 0.01,
                    "GOVT_CENTER",
                    "Seed Research Center, " + district,
                    "1800-XXX-XXXX",
                    "Seed Quality Testing, Certification"
                );
                roadNetwork.addLocation(seedLab);
                roadNetwork.addRoad(districtLoc, seedLab);
            }
        }

        // Add inter-district connections (simplified road network)
        for (int i = 0; i < districts.length; i++) {
            for (int j = i + 1; j < districts.length; j++) {
                Location loc1 = districtLocations.get(districts[i]);
                Location loc2 = districtLocations.get(districts[j]);
                if (loc1 != null && loc2 != null) {
                    double distance = calculateDistance(
                        loc1.latitude, loc1.longitude,
                        loc2.latitude, loc2.longitude
                    );
                    if (distance < 100) { // Only connect nearby districts
                        roadNetwork.addRoad(loc1, loc2);
                    }
                }
            }
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }

    private JPanel createMainMenu() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(40, 60, 40, 60));
        panel.setBackground(new Color(240, 240, 240));

        // Title Panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(240, 240, 240));
        
        JLabel title = new JLabel("Crop Sowing Advisor", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(new Color(34, 139, 34));
        title.setBorder(new EmptyBorder(20, 10, 20, 10));
        
        JLabel subtitle = new JLabel("Empowering Farmers with Smart Agricultural Solutions", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(new Color(100, 100, 100));
        
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 20, 30));
        buttonPanel.setBorder(new EmptyBorder(40, 100, 40, 100));
        buttonPanel.setBackground(new Color(240, 240, 240));
        
        JButton farmerBtn = createStyledButton("Register as Farmer");
        JButton expertBtn = createStyledButton("Register as Expert");
        
        // Add icons to buttons
        try {
            ImageIcon farmerIcon = new ImageIcon(getClass().getResource("/icons/farmer_icon.png"));
            ImageIcon expertIcon = new ImageIcon(getClass().getResource("/icons/expert_icon.png"));
            
            // Scale icons to appropriate size
            Image farmerImg = farmerIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            Image expertImg = expertIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            
            farmerBtn.setIcon(new ImageIcon(farmerImg));
            expertBtn.setIcon(new ImageIcon(expertImg));
        } catch (Exception e) {
            System.out.println("Icons not found. Using text-only buttons.");
        }
        
        farmerBtn.addActionListener(e -> openFarmerRegistration());
        expertBtn.addActionListener(e -> openExpertRegistration());

        buttonPanel.add(farmerBtn);
        buttonPanel.add(expertBtn);

        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        return panel;
    }

    private void openFarmerRegistration() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(240, 240, 240));

        // Title
        JLabel title = new JLabel("Farmer Registration", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(34, 139, 34));
        panel.add(title, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(240, 240, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(20);
        JTextField aadhaarField = new JTextField(20);
        JComboBox<String> districtBox = new JComboBox<>(districts);
        JTextField villageField = new JTextField(20);

        // Style the input fields
        Dimension fieldSize = new Dimension(300, 35);
        nameField.setPreferredSize(fieldSize);
        aadhaarField.setPreferredSize(fieldSize);
        districtBox.setPreferredSize(fieldSize);
        villageField.setPreferredSize(fieldSize);

        // Add components to form panel
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Aadhaar Number:"), gbc);
        gbc.gridx = 1;
        formPanel.add(aadhaarField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("District:"), gbc);
        gbc.gridx = 1;
        formPanel.add(districtBox, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Village:"), gbc);
        gbc.gridx = 1;
        formPanel.add(villageField, gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(240, 240, 240));
        
        JButton registerBtn = createStyledButton("Register");
        JButton cancelBtn = createStyledButton("Cancel", new Color(220, 53, 69));
        
        buttonPanel.add(registerBtn);
        buttonPanel.add(cancelBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Create custom dialog
        JDialog dialog = new JDialog(this, "Farmer Registration", true);
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        // Add action listeners
        registerBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String aadhaar = aadhaarField.getText().trim();
            String district = (String) districtBox.getSelectedItem();
            String village = villageField.getText().trim();

            if (name.isEmpty() || aadhaar.isEmpty() || village.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "All fields are required.", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!Pattern.matches("\\d{12}", aadhaar)) {
                JOptionPane.showMessageDialog(dialog, 
                    "Invalid Aadhaar number. Please enter 12 digits.", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                String checkSql = "SELECT id, name FROM farmers WHERE aadhaar_number = ?";
                PreparedStatement checkPs = conn.prepareStatement(checkSql);
                checkPs.setString(1, aadhaar);
                ResultSet rs = checkPs.executeQuery();

                if (rs.next()) {
                    String existingName = rs.getString("name");
                    int farmerId = rs.getInt("id");
                    JOptionPane.showMessageDialog(dialog, 
                        "Welcome back, " + existingName + "!\nYour Farmer ID: " + farmerId,
                        "Welcome Back",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    showFarmerPanel(existingName);
                } else {
                    String insertSql = "INSERT INTO farmers (name, aadhaar_number, district, village, regdate) " +
                                     "VALUES (?, ?, ?, ?, NOW())";
                    PreparedStatement insertPs = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                    insertPs.setString(1, name);
                    insertPs.setString(2, aadhaar);
                    insertPs.setString(3, district);
                    insertPs.setString(4, village);
                    insertPs.executeUpdate();

                    rs = insertPs.getGeneratedKeys();
                    if (rs.next()) {
                        int farmerId = rs.getInt(1);
                        JOptionPane.showMessageDialog(dialog, 
                            "Farmer registered successfully!\nYour Farmer ID: " + farmerId,
                            "Registration Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        showFarmerPanel(name);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Database error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void showFarmerPanel(String farmerName) {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(240, 240, 240));

        // Top Panel with back button and farmer details
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(240, 240, 240));
        
        JButton backBtn = createStyledButton("Back");
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "menu"));
        
        JPanel farmerDetailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        farmerDetailsPanel.setBackground(new Color(240, 240, 240));
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, district, village FROM farmers WHERE name = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, farmerName);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                JLabel farmerIdLabel = new JLabel("Farmer ID: " + rs.getInt("id"));
                JLabel districtLabel = new JLabel("District: " + rs.getString("district"));
                JLabel villageLabel = new JLabel("Village: " + rs.getString("village"));
                
                farmerIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                districtLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                villageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                
                farmerDetailsPanel.add(farmerIdLabel);
                farmerDetailsPanel.add(new JLabel(" | "));
                farmerDetailsPanel.add(districtLabel);
                farmerDetailsPanel.add(new JLabel(" | "));
                farmerDetailsPanel.add(villageLabel);
            }
        } catch (SQLException ex) {
            JLabel errorLabel = new JLabel("Error fetching farmer details");
            errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            farmerDetailsPanel.add(errorLabel);
        }
        
        topPanel.add(backBtn, BorderLayout.WEST);
        topPanel.add(farmerDetailsPanel, BorderLayout.CENTER);

        // Center Panel with main functionality
        JPanel centerPanel = new JPanel(new BorderLayout(20, 20));
        centerPanel.setBackground(new Color(240, 240, 240));
        
        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(new Color(240, 240, 240));
        inputPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Farmer Dashboard",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 16)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> districtBox = new JComboBox<>(districts);
        JComboBox<String> cropBox = new JComboBox<>(crops);
        JTextField latField = new JTextField();
        JTextField lonField = new JTextField();

        Dimension fieldSize = new Dimension(200, 30);
        districtBox.setPreferredSize(fieldSize);
        cropBox.setPreferredSize(fieldSize);
        latField.setPreferredSize(fieldSize);
        lonField.setPreferredSize(fieldSize);

        // Add components to input panel
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Select District:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(districtBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Select Crop:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(cropBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Your Latitude:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(latField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("Your Longitude:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(lonField, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(240, 240, 240));
        
        JButton getInfoBtn = createStyledButton("Get Advice");
        JButton findCentresBtn = createStyledButton("Find Nearest Govt. Centres");
        JButton marketPriceBtn = createStyledButton("Check Market Prices");
        
        buttonPanel.add(getInfoBtn);
        buttonPanel.add(findCentresBtn);
        buttonPanel.add(marketPriceBtn);

        // Result Panel
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBackground(new Color(240, 240, 240));
        resultPanel.setBorder(BorderFactory.createTitledBorder("Results"));
        
        JTextArea resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        resultPanel.add(scrollPane, BorderLayout.CENTER);

        // Add all panels to center panel
        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(buttonPanel, BorderLayout.CENTER);
        centerPanel.add(resultPanel, BorderLayout.SOUTH);

        // Add all panels to main panel
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        // Add action listeners
        findCentresBtn.addActionListener(e -> {
            String district = (String) districtBox.getSelectedItem();
            Location districtLoc = roadNetwork.adjacencyList.keySet().stream()
                .filter(loc -> loc.name.equals(district) && loc.type.equals("DISTRICT"))
                .findFirst()
                .orElse(null);

            if (districtLoc != null) {
                try {
                    double farmerLat = Double.parseDouble(latField.getText().trim());
                    double farmerLon = Double.parseDouble(lonField.getText().trim());
                    
                    Location farmerLoc = new Location(
                        "Farmer Location",
                        farmerLat,
                        farmerLon,
                        "FARMER",
                        "Current Location",
                        "",
                        ""
                    );
                    roadNetwork.addLocation(farmerLoc);
                    roadNetwork.addRoad(farmerLoc, districtLoc);

                    List<Location> nearestCenters = roadNetwork.findNearestCenters(farmerLoc, 5);
                    
                    StringBuilder result = new StringBuilder();
                    result.append("5 Nearest Government Centres from your location:\n\n");
                    
                    for (Location center : nearestCenters) {
                        result.append("* ").append(center.name).append("\n");
                        result.append("  Address: ").append(center.address).append("\n");
                        result.append("  Contact: ").append(center.contact).append("\n");
                        result.append("  Services: ").append(center.services).append("\n");
                        result.append("  Distance: ").append(String.format("%.1f", 
                            calculateDistance(farmerLat, farmerLon,
                                            center.latitude, center.longitude))).append(" km\n\n");
                    }
                    
                    resultArea.setText(result.toString());
                } catch (NumberFormatException ex) {
                    resultArea.setText("Please enter valid latitude and longitude coordinates.");
                }
            } else {
                resultArea.setText("District not found in the network.");
            }
        });

        getInfoBtn.addActionListener(e -> {
            String district = (String) districtBox.getSelectedItem();
            String crop = (String) cropBox.getSelectedItem();

            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "SELECT advice FROM advice WHERE district=? AND crop=?";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, district);
                ps.setString(2, crop);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    resultArea.setText("Hello " + farmerName + ",\n\n📌 Advice:\n" + rs.getString("advice"));
                } else {
                    resultArea.setText("⚠️ No advice found for " + crop + " in " + district + ".");
                }
            } catch (SQLException ex) {
                resultArea.setText("❗ Error fetching advice: " + ex.getMessage());
            }
        });

        marketPriceBtn.addActionListener(e -> {
            String crop = (String) cropBox.getSelectedItem();
            double price = marketPrices.get(crop);
            resultArea.setText("💰 Market Price for " + crop + ":\n"
                    + "Current Price: ₹" + String.format("%.2f", price) + " per quintal\n"
                    + "Last Updated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        });

        mainPanel.add(panel, "farmerPanel");
        cardLayout.show(mainPanel, "farmerPanel");
    }

    private void openExpertRegistration() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(240, 240, 240));

        // Title
        JLabel title = new JLabel("Expert Registration", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(34, 139, 34));
        panel.add(title, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(240, 240, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(20);
        JTextField regField = new JTextField(20);

        // Style the input fields
        Dimension fieldSize = new Dimension(300, 35);
        nameField.setPreferredSize(fieldSize);
        regField.setPreferredSize(fieldSize);

        // Add components to form panel
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Registration Number:"), gbc);
        gbc.gridx = 1;
        formPanel.add(regField, gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(240, 240, 240));
        
        JButton registerBtn = createStyledButton("Register", new Color(70, 130, 180));
        JButton cancelBtn = createStyledButton("Cancel", new Color(220, 53, 69));
        
        buttonPanel.add(registerBtn);
        buttonPanel.add(cancelBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Create custom dialog
        JDialog dialog = new JDialog(this, "Expert Registration", true);
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        // Add action listeners
        registerBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String regNo = regField.getText().trim();

            if (name.isEmpty() || regNo.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "All fields are required.", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                String check = "SELECT id FROM experts WHERE name=? AND registration_number=?";
                PreparedStatement ps = conn.prepareStatement(check);
                ps.setString(1, name);
                ps.setString(2, regNo);
                ResultSet rs = ps.executeQuery();

                int expertId;
                if (rs.next()) {
                    expertId = rs.getInt("id");
                    JOptionPane.showMessageDialog(dialog, 
                        "Welcome back, " + name + "!",
                        "Welcome Back",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    String insert = "INSERT INTO experts (name, registration_number) VALUES (?, ?)";
                    ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, name);
                    ps.setString(2, regNo);
                    ps.executeUpdate();
                    rs = ps.getGeneratedKeys();
                    rs.next();
                    expertId = rs.getInt(1);
                    JOptionPane.showMessageDialog(dialog, 
                        "Expert registered successfully!",
                        "Registration Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                }
                dialog.dispose();
                showExpertPanel(name, expertId);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Database error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void showExpertPanel(String expertName, int expertId) {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(240, 240, 240));

        // Top Panel with back button and expert details
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(240, 240, 240));
        
        JButton backBtn = createStyledButton("Back", new Color(100, 100, 100));
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "menu"));
        
        JPanel expertDetailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        expertDetailsPanel.setBackground(new Color(240, 240, 240));
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, registration_number FROM experts WHERE name = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, expertName);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                JLabel expertIdLabel = new JLabel("Expert ID: " + rs.getInt("id"));
                JLabel regNoLabel = new JLabel("Registration Number: " + rs.getString("registration_number"));
                
                expertIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                regNoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                
                expertDetailsPanel.add(expertIdLabel);
                expertDetailsPanel.add(new JLabel(" | "));
                expertDetailsPanel.add(regNoLabel);
            }
        } catch (SQLException ex) {
            JLabel errorLabel = new JLabel("Error fetching expert details");
            errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            expertDetailsPanel.add(errorLabel);
        }
        
        topPanel.add(backBtn, BorderLayout.WEST);
        topPanel.add(expertDetailsPanel, BorderLayout.CENTER);

        // Center Panel with main functionality
        JPanel centerPanel = new JPanel(new BorderLayout(20, 20));
        centerPanel.setBackground(new Color(240, 240, 240));
        
        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(new Color(240, 240, 240));
        inputPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Expert Dashboard",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 16)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> districtBox = new JComboBox<>(districts);
        JComboBox<String> cropBox = new JComboBox<>(crops);

        Dimension fieldSize = new Dimension(200, 30);
        districtBox.setPreferredSize(fieldSize);
        cropBox.setPreferredSize(fieldSize);

        // Add components to input panel
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Select District:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(districtBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Select Crop:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(cropBox, gbc);

        // Advice Panel
        JPanel advicePanel = new JPanel(new BorderLayout(10, 10));
        advicePanel.setBackground(new Color(240, 240, 240));
        advicePanel.setBorder(BorderFactory.createTitledBorder("Enter Advice"));

        JTextArea infoArea = new JTextArea(5, 30);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(infoArea);
        advicePanel.add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(240, 240, 240));
        
        JButton addBtn = createStyledButton("Add Advice", new Color(70, 130, 180));
        JLabel statusLabel = new JLabel("");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        buttonPanel.add(addBtn);
        buttonPanel.add(statusLabel);

        // Add all panels to center panel
        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(advicePanel, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add action listener for Add Advice button
        addBtn.addActionListener(e -> {
            String district = (String) districtBox.getSelectedItem();
            String crop = (String) cropBox.getSelectedItem();
            String advice = infoArea.getText().trim();

            if (!advice.isEmpty()) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String insert = "INSERT INTO advice (district, crop, advice, expert_id) VALUES (?, ?, ?, ?)";
                    PreparedStatement ps = conn.prepareStatement(insert);
                    ps.setString(1, district);
                    ps.setString(2, crop);
                    ps.setString(3, advice);
                    ps.setInt(4, expertId);
                    ps.executeUpdate();
                    statusLabel.setText("✅ Advice added successfully!");
                    infoArea.setText("");
                } catch (SQLException ex) {
                    statusLabel.setText("❗ Error: " + ex.getMessage());
                }
            } else {
                statusLabel.setText("⚠️ Advice cannot be empty.");
            }
        });

        // Add all panels to main panel
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        mainPanel.add(panel, "expertPanel");
        cardLayout.show(mainPanel, "expertPanel");
    }

    private JButton createStyledButton(String text) {
        return createStyledButton(text, new Color(60, 179, 113));
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(color);
        btn.setForeground(new Color(0, 0, 0));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 2),
            new EmptyBorder(12, 25, 12, 25)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(color.brighter());
                btn.setForeground(new Color(0, 0, 0));
                btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(color);
                btn.setForeground(new Color(0, 0, 0));
                btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
            }
        });
        
        return btn;
    }

    private void quickSort(String[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }

    private int partition(String[] arr, int low, int high) {
        String pivot = arr[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (arr[j].compareToIgnoreCase(pivot) < 0) {
                i++;
                String temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        String temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;
        return i + 1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApplication());
    }
}
