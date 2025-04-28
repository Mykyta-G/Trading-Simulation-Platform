import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class ProfileManager {
    private JFrame parentFrame;
    private Map<String, Integer> stocksOwned;
    private Map<String, Double> currentPrices;
    private double[] balance;
    private File profilesDirectory;
    private String currentUsername = null;
    private boolean isLoggedIn = false;
    private Timer autoSaveTimer;
    private JLabel profileLabel;
    private JButton profileButton;
    
    // New fields for tracking portfolio performance
    private Map<String, List<StockTransaction>> transactionHistory;
    private Map<String, Double> purchaseTotals; // Total amount spent on each stock
    private Map<String, Double> initialPrices; // Price of each stock when user first logged in
    
    // UI update callback
    public Runnable updateStockPanels;
    
    // Inner class to track stock transactions
    private class StockTransaction implements Serializable {
        private String type; // "BUY" or "SELL"
        private double price;
        private int quantity;
        private long timestamp;
        
        public StockTransaction(String type, double price, int quantity) {
            this.type = type;
            this.price = price;
            this.quantity = quantity;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getFormattedDate() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
        }
        
        public String toString() {
            return type + " " + quantity + " @ $" + String.format("%.2f", price) + 
                   " on " + getFormattedDate();
        }
    }

    public ProfileManager(JFrame parentFrame, Map<String, Integer> stocksOwned, double[] balance, Map<String, Double> currentPrices) {
        this.parentFrame = parentFrame;
        this.stocksOwned = stocksOwned;
        this.balance = balance;
        this.currentPrices = currentPrices;
        
        // Initialize new tracking structures
        this.transactionHistory = new HashMap<>();
        this.purchaseTotals = new HashMap<>();
        this.initialPrices = new HashMap<>();
        
        // Create profiles directory
        profilesDirectory = new File(System.getProperty("user.home"), "StockSimulator");
        if (!profilesDirectory.exists()) {
            profilesDirectory.mkdirs();
        }
        
        // Create auto-save timer
        autoSaveTimer = new Timer(5000, e -> {
            if (isLoggedIn) {
                saveUserData();
            }
        });
        autoSaveTimer.setRepeats(false);
    }
    
    // Create UI components for the top right corner
    public JPanel createProfilePanel() {
        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        profilePanel.setBackground(new Color(35, 47, 62));
        
        profileLabel = new JLabel("Not logged in");
        profileLabel.setForeground(Color.WHITE);
        profileLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // Profile button with better icon - using a more stylish user icon
        profileButton = new JButton();
        // Use a nicer icon instead of emoji
        profileButton.setIcon(createProfileIcon(35, 47, 62));
        profileButton.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        profileButton.setForeground(Color.WHITE);
        profileButton.setBackground(new Color(45, 55, 65));
        profileButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 70, 80), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        profileButton.setFocusPainted(false);
        profileButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        profileButton.addActionListener(e -> {
            if (isLoggedIn) {
                showProfileMenu();
            } else {
                showLoginDialog();
            }
        });
        
        profilePanel.add(profileLabel);
        profilePanel.add(profileButton);
        
        return profilePanel;
    }
    
    // Create a stylish profile icon
    private ImageIcon createProfileIcon(int r, int g, int b) {
        // Create an image for the icon
        int size = 24;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw a circle for the head
        g2d.setColor(new Color(240, 240, 240));
        int headSize = size / 2;
        g2d.fillOval(size/4, size/8, headSize, headSize);
        
        // Draw the body
        g2d.fillOval(size/4 - 2, size/2, headSize + 4, headSize + 4);
        
        // Clean up
        g2d.dispose();
        
        return new ImageIcon(image);
    }
    
    // Show login dialog as a popup on the main screen
    private void showLoginDialog() {
        // Create a glass pane effect with a semi-transparent background
        JPanel glassPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, 150)); // Semi-transparent black
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        glassPane.setOpaque(false);
        glassPane.setLayout(new GridBagLayout()); // Center content
        
        // Create main dialog panel
        JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.setBackground(new Color(35, 47, 62));
        dialogPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 70, 80), 2),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        
        // Create header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 35, 45));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel headerLabel = new JLabel("User Account");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        
        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Arial", Font.BOLD, 20));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(new Color(25, 35, 45));
        closeButton.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> {
            // Remove glass pane
            parentFrame.setGlassPane(new JPanel());
            parentFrame.getGlassPane().setVisible(false);
        });
        headerPanel.add(closeButton, BorderLayout.EAST);
        
        // Create tabbed pane for login and register
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(35, 47, 62));
        tabbedPane.setForeground(Color.WHITE);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Style the tabbed pane
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                shadow = new Color(35, 47, 62);
                darkShadow = new Color(35, 47, 62);
                highlight = new Color(35, 47, 62);
                lightHighlight = new Color(35, 47, 62);
            }
            
            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                if (isSelected) {
                    g.setColor(new Color(50, 168, 82)); // Green highlight for selected tab
                    g.fillRect(x, y + h - 2, w, 2); // Bottom border only
                } else {
                    g.setColor(new Color(60, 70, 80));
                    g.fillRect(x, y + h - 1, w, 1); // Thinner bottom border for unselected
                }
            }
            
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                g.setColor(isSelected ? new Color(35, 47, 62) : new Color(25, 35, 45));
                g.fillRect(x, y, w, h);
            }
            
            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                // Don't paint content border
            }
        });
        
        // Login panel
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(new Color(35, 47, 62));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 5, 10, 5);
        
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JTextField usernameField = new JTextField(20);
        styleTextField(usernameField);
        
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(Color.WHITE);
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JPasswordField passwordField = new JPasswordField(20);
        styleTextField(passwordField);
        
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        styleButton(loginButton);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        loginPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        loginPanel.add(usernameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        loginPanel.add(passwordField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 5, 10, 5);
        loginPanel.add(loginButton, gbc);
        
        // Register panel
        JPanel registerPanel = new JPanel(new GridBagLayout());
        registerPanel.setBackground(new Color(35, 47, 62));
        registerPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JLabel newUsernameLabel = new JLabel("Username:");
        newUsernameLabel.setForeground(Color.WHITE);
        newUsernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JTextField newUsernameField = new JTextField(20);
        styleTextField(newUsernameField);
        
        JLabel newPasswordLabel = new JLabel("Password:");
        newPasswordLabel.setForeground(Color.WHITE);
        newPasswordLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JPasswordField newPasswordField = new JPasswordField(20);
        styleTextField(newPasswordField);
        
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setForeground(Color.WHITE);
        confirmPasswordLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JPasswordField confirmPasswordField = new JPasswordField(20);
        styleTextField(confirmPasswordField);
        
        JButton registerButton = new JButton("Register");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        styleButton(registerButton);
        
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 5, 10, 5);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        registerPanel.add(newUsernameLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        registerPanel.add(newUsernameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        registerPanel.add(newPasswordLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        registerPanel.add(newPasswordField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        registerPanel.add(confirmPasswordLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        registerPanel.add(confirmPasswordField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 5, 10, 5);
        registerPanel.add(registerButton, gbc);
        
        // Add panels to tabbed pane
        tabbedPane.addTab("Login", loginPanel);
        tabbedPane.addTab("Register", registerPanel);
        
        // Add action listeners
        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                showMessage("Please enter both username and password", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (authenticate(username, password)) {
                login(username);
                // Remove glass pane
                parentFrame.setGlassPane(new JPanel());
                parentFrame.getGlassPane().setVisible(false);
            } else {
                showMessage("Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        registerButton.addActionListener(e -> {
            String username = newUsernameField.getText().trim();
            String password = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                showMessage("Please enter both username and password", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                showMessage("Passwords do not match", "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (userExists(username)) {
                showMessage("Username already exists", "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (registerUser(username, password)) {
                login(username);
                // Remove glass pane
                parentFrame.setGlassPane(new JPanel());
                parentFrame.getGlassPane().setVisible(false);
            } else {
                showMessage("Failed to register user", "Registration Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Add components to dialog
        dialogPanel.add(headerPanel, BorderLayout.NORTH);
        dialogPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Set preferred size for dialog
        dialogPanel.setPreferredSize(new Dimension(400, 450));
        
        // Add dialog to glass pane
        glassPane.add(dialogPanel);
        
        // Set and show glass pane
        parentFrame.setGlassPane(glassPane);
        parentFrame.getGlassPane().setVisible(true);
    }
    
    // Show profile menu
    private void showProfileMenu() {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(new Color(35, 47, 62));
        menu.setBorder(BorderFactory.createLineBorder(new Color(60, 70, 80)));
        
        JMenuItem profileItem = new JMenuItem("Profile: " + currentUsername);
        profileItem.setForeground(Color.WHITE);
        profileItem.setBackground(new Color(35, 47, 62));
        profileItem.setEnabled(false);
        
        JMenuItem saveItem = new JMenuItem("Save Profile Data");
        saveItem.setForeground(Color.WHITE);
        saveItem.setBackground(new Color(35, 47, 62));
        
        JMenuItem addNoteItem = new JMenuItem("Add Trading Note");
        addNoteItem.setForeground(Color.WHITE);
        addNoteItem.setBackground(new Color(35, 47, 62));
        
        JMenuItem viewNotesItem = new JMenuItem("View Notes");
        viewNotesItem.setForeground(Color.WHITE);
        viewNotesItem.setBackground(new Color(35, 47, 62));
        
        // New portfolio stats menu item
        JMenuItem portfolioStatsItem = new JMenuItem("Portfolio Statistics");
        portfolioStatsItem.setForeground(Color.WHITE);
        portfolioStatsItem.setBackground(new Color(35, 47, 62));
        
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.setForeground(new Color(220, 53, 69));
        logoutItem.setBackground(new Color(35, 47, 62));
        
        saveItem.addActionListener(e -> saveUserData());
        addNoteItem.addActionListener(e -> addNote());
        viewNotesItem.addActionListener(e -> viewNotes());
        portfolioStatsItem.addActionListener(e -> viewPortfolioStats());
        logoutItem.addActionListener(e -> logout());
        
        menu.add(profileItem);
        menu.addSeparator();
        menu.add(portfolioStatsItem); // Add the new item
        menu.add(saveItem);
        menu.add(addNoteItem);
        menu.add(viewNotesItem);
        menu.addSeparator();
        menu.add(logoutItem);
        
        menu.show(profileButton, 0, profileButton.getHeight());
    }
    
    // Show message dialog
    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(parentFrame, message, title, messageType);
    }
    
    // Style text fields
    private void styleTextField(JTextField field) {
        field.setBackground(new Color(45, 55, 65));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 70, 80), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }
    
    // Style buttons
    private void styleButton(JButton button) {
        button.setBackground(new Color(50, 168, 82));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(40, 158, 72), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
    }
    
    // Check if user exists
    private boolean userExists(String username) {
        File userFile = new File(profilesDirectory, username + ".profile");
        return userFile.exists();
    }
    
    // Register a new user
    private boolean registerUser(String username, String password) {
        try {
            File userFile = new File(profilesDirectory, username + ".profile");
            try (PrintWriter writer = new PrintWriter(new FileWriter(userFile))) {
                writer.println("username:" + username);
                writer.println("password:" + password);
                writer.println("balance:" + balance[0]);
                writer.println("created:" + System.currentTimeMillis());
            }
            
            // Create empty notes file
            File notesFile = new File(profilesDirectory, username + ".notes");
            try (PrintWriter writer = new PrintWriter(new FileWriter(notesFile))) {
                writer.println("# Trading Notes for " + username);
                writer.println("# Created on " + new Date());
                writer.println();
            }
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Authenticate user
    private boolean authenticate(String username, String password) {
        File userFile = new File(profilesDirectory, username + ".profile");
        if (!userFile.exists()) {
            return false;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("password:")) {
                    String storedPassword = line.substring("password:".length());
                    return password.equals(storedPassword);
                }
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Login user
    private void login(String username) {
        currentUsername = username;
        isLoggedIn = true;
        profileLabel.setText(username);
        
        // Update button appearance for logged-in state
        profileButton.setIcon(createLoggedInProfileIcon());
        
        // Load user data first
        loadUserData();
        
        // Update current prices in the initialPrices map for any new stocks
        for (String stockName : currentPrices.keySet()) {
            if (!initialPrices.containsKey(stockName)) {
                initialPrices.put(stockName, currentPrices.get(stockName));
            }
        }
        
        // Always force UI update on login, even if loadUserData didn't think data changed
        if (updateStockPanels != null) {
            SwingUtilities.invokeLater(updateStockPanels);
        }
        
        autoSaveTimer.start();
    }
    
    // Logout user
    private void logout() {
        saveUserData(); // Save data before logging out
        currentUsername = null;
        isLoggedIn = false;
        profileLabel.setText("Not logged in");
        
        // Reset button appearance
        profileButton.setIcon(createProfileIcon(35, 47, 62));
        
        autoSaveTimer.stop();
        
        // Clear tracking data
        transactionHistory.clear();
        purchaseTotals.clear();
        initialPrices.clear();
    }
    
    // Load user data
    private void loadUserData() {
        if (!isLoggedIn) return;
        
        File userFile = new File(profilesDirectory, currentUsername + ".profile");
        if (!userFile.exists()) return;
        
        // Clear existing data
        transactionHistory.clear();
        purchaseTotals.clear();
        initialPrices.clear();
        
        // Track if we need to update the UI
        boolean dataChanged = false;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("balance:")) {
                    try {
                        double savedBalance = Double.parseDouble(line.substring("balance:".length()));
                        if (balance[0] != savedBalance) {
                            balance[0] = savedBalance;
                            dataChanged = true;
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                } else if (line.startsWith("stock:")) {
                    // Format: stock:STOCKNAME:QUANTITY
                    String[] parts = line.split(":");
                    if (parts.length == 3) {
                        String stockName = parts[1];
                        int quantity = Integer.parseInt(parts[2]);
                        if (stocksOwned.containsKey(stockName)) {
                            // Check if we actually need to update
                            if (stocksOwned.get(stockName) != quantity) {
                                stocksOwned.put(stockName, quantity);
                                dataChanged = true;
                            }
                        }
                    }
                } else if (line.startsWith("transaction:")) {
                    // Format: transaction:STOCKNAME:TYPE:PRICE:QUANTITY:TIMESTAMP
                    String[] parts = line.split(":");
                    if (parts.length == 6) {
                        String stockName = parts[1];
                        String type = parts[2];
                        double price = Double.parseDouble(parts[3]);
                        int quantity = Integer.parseInt(parts[4]);
                        long timestamp = Long.parseLong(parts[5]);
                        
                        if (!transactionHistory.containsKey(stockName)) {
                            transactionHistory.put(stockName, new ArrayList<>());
                        }
                        
                        StockTransaction transaction = new StockTransaction(type, price, quantity);
                        transaction.timestamp = timestamp;
                        transactionHistory.get(stockName).add(transaction);
                        
                        // Update purchase totals for BUY transactions
                        if (type.equals("BUY")) {
                            Double currentTotal = purchaseTotals.getOrDefault(stockName, 0.0);
                            purchaseTotals.put(stockName, currentTotal + price);
                        }
                    }
                } else if (line.startsWith("initialPrice:")) {
                    // Format: initialPrice:STOCKNAME:PRICE
                    String[] parts = line.split(":");
                    if (parts.length == 3) {
                        String stockName = parts[1];
                        double price = Double.parseDouble(parts[2]);
                        initialPrices.put(stockName, price);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Update UI if data changed
        if (dataChanged && updateStockPanels != null) {
            SwingUtilities.invokeLater(updateStockPanels);
        }
    }
    
    // Save user data
    public void saveUserData() {
        if (!isLoggedIn) return;
        
        try {
            File userFile = new File(profilesDirectory, currentUsername + ".profile");
            List<String> lines = new ArrayList<>();
            
            // If file exists, read existing content
            if (userFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.startsWith("balance:") && 
                            !line.startsWith("stock:") && 
                            !line.startsWith("transaction:") &&
                            !line.startsWith("initialPrice:")) {
                            lines.add(line);
                        }
                    }
                }
            }
            
            // Add current balance and stocks
            lines.add("balance:" + balance[0]);
            for (Map.Entry<String, Integer> entry : stocksOwned.entrySet()) {
                if (entry.getValue() > 0) {
                    lines.add("stock:" + entry.getKey() + ":" + entry.getValue());
                }
            }
            
            // Add initial prices
            for (Map.Entry<String, Double> entry : initialPrices.entrySet()) {
                lines.add("initialPrice:" + entry.getKey() + ":" + entry.getValue());
            }
            
            // Add transactions
            for (Map.Entry<String, List<StockTransaction>> entry : transactionHistory.entrySet()) {
                String stockName = entry.getKey();
                for (StockTransaction transaction : entry.getValue()) {
                    lines.add("transaction:" + stockName + ":" + 
                              transaction.type + ":" + 
                              transaction.price + ":" + 
                              transaction.quantity + ":" + 
                              transaction.timestamp);
                }
            }
            
            // Write updated content
            try (PrintWriter writer = new PrintWriter(new FileWriter(userFile))) {
                for (String line : lines) {
                    writer.println(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Add a note
    private void addNote() {
        if (!isLoggedIn) return;
        
        JDialog noteDialog = new JDialog(parentFrame, "Add Trading Note", true);
        noteDialog.setLayout(new BorderLayout());
        noteDialog.getContentPane().setBackground(new Color(35, 47, 62));
        
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setBackground(new Color(35, 47, 62));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JTextArea noteArea = new JTextArea(10, 40);
        noteArea.setBackground(new Color(45, 55, 65));
        noteArea.setForeground(Color.WHITE);
        noteArea.setCaretColor(Color.WHITE);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 70, 80), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JScrollPane scrollPane = new JScrollPane(noteArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        JButton addSummaryButton = new JButton("Add Portfolio Summary");
        styleButton(addSummaryButton);
        addSummaryButton.addActionListener(e -> {
            noteArea.append("\n" + generatePortfolioSummary());
        });
        
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBackground(new Color(35, 47, 62));
        
        JButton saveButton = new JButton("Save Note");
        styleButton(saveButton);
        saveButton.addActionListener(e -> {
            String noteText = noteArea.getText().trim();
            if (!noteText.isEmpty()) {
                saveNote(noteText);
                noteDialog.dispose();
            }
        });
        
        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftButtonPanel.setBackground(new Color(35, 47, 62));
        leftButtonPanel.add(addSummaryButton);
        
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightButtonPanel.setBackground(new Color(35, 47, 62));
        rightButtonPanel.add(saveButton);
        
        buttonPanel.add(leftButtonPanel, BorderLayout.WEST);
        buttonPanel.add(rightButtonPanel, BorderLayout.EAST);
        
        contentPanel.add(new JLabel("Your trading note:"), BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        noteDialog.add(contentPanel, BorderLayout.CENTER);
        noteDialog.pack();
        noteDialog.setLocationRelativeTo(parentFrame);
        noteDialog.setVisible(true);
    }
    
    // Generate portfolio summary
    private String generatePortfolioSummary() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder summary = new StringBuilder();
        
        summary.append("--- Portfolio Summary (").append(sdf.format(new Date())).append(") ---\n");
        summary.append("Cash Balance: $").append(String.format("%.2f", balance[0])).append("\n\n");
        
        double totalPortfolioValue = balance[0];
        double totalProfitLoss = 0.0;
        boolean hasStocks = false;
        
        // Market Overview
        summary.append("MARKET OVERVIEW:\n");
        for (Map.Entry<String, Double> entry : initialPrices.entrySet()) {
            String stockName = entry.getKey();
            if (currentPrices.containsKey(stockName)) {
                double performance = getMarketPerformance(stockName);
                summary.append("- ").append(stockName)
                       .append(": ").append(performance >= 0 ? "+" : "")
                       .append(String.format("%.2f", performance)).append("% since first login\n");
            }
        }
        
        summary.append("\nSTOCKS OWNED:\n");
        for (Map.Entry<String, Integer> entry : stocksOwned.entrySet()) {
            String stockName = entry.getKey();
            int quantity = entry.getValue();
            
            if (quantity > 0 && currentPrices.containsKey(stockName)) {
                hasStocks = true;
                double price = currentPrices.get(stockName);
                double value = price * quantity;
                double avgPurchase = getAveragePurchasePrice(stockName);
                double profitLoss = getProfitLoss(stockName);
                totalProfitLoss += profitLoss;
                
                summary.append("- ").append(stockName).append(": ")
                       .append(quantity).append(" shares, current price: $")
                       .append(String.format("%.2f", price)).append("\n")
                       .append("  Avg purchase: $").append(String.format("%.2f", avgPurchase))
                       .append(", current value: $").append(String.format("%.2f", value)).append("\n")
                       .append("  P/L: ").append(profitLoss >= 0 ? "+" : "")
                       .append("$").append(String.format("%.2f", profitLoss)).append(" (")
                       .append(profitLoss >= 0 ? "+" : "")
                       .append(String.format("%.2f", (price - avgPurchase) / avgPurchase * 100))
                       .append("%)\n");
                
                totalPortfolioValue += value;
            }
        }
        
        if (!hasStocks) {
            summary.append("No stocks owned.\n");
        }
        
        summary.append("\nTOTAL PORTFOLIO VALUE: $").append(String.format("%.2f", totalPortfolioValue)).append("\n");
        if (hasStocks) {
            summary.append("TOTAL PROFIT/LOSS: ").append(totalProfitLoss >= 0 ? "+" : "")
                   .append("$").append(String.format("%.2f", totalProfitLoss)).append("\n");
        }
        
        // Add recent transactions if available
        if (!transactionHistory.isEmpty()) {
            summary.append("\nRECENT TRANSACTIONS:\n");
            int count = 0;
            
            // Gather all transactions
            List<Object[]> allTransactions = new ArrayList<>();
            for (Map.Entry<String, List<StockTransaction>> entry : transactionHistory.entrySet()) {
                String stockName = entry.getKey();
                for (StockTransaction transaction : entry.getValue()) {
                    allTransactions.add(new Object[]{transaction, stockName});
                }
            }
            
            // Sort by timestamp (most recent first)
            allTransactions.sort((a, b) -> 
                Long.compare(((StockTransaction)b[0]).timestamp, ((StockTransaction)a[0]).timestamp));
            
            // Display only the most recent 5 transactions
            for (int i = 0; i < allTransactions.size() && i < 5; i++) {
                StockTransaction transaction = (StockTransaction)allTransactions.get(i)[0];
                String stockName = (String)allTransactions.get(i)[1];
                
                summary.append("- ").append(transaction.getFormattedDate())
                       .append(": ").append(transaction.type)
                       .append(" ").append(stockName)
                       .append(" x").append(transaction.quantity)
                       .append(" @ $").append(String.format("%.2f", transaction.price))
                       .append("\n");
                count++;
            }
            
            if (count == 0) {
                summary.append("No recent transactions.\n");
            }
        }
        
        summary.append("-------------------------------\n");
        
        return summary.toString();
    }
    
    // Save a note
    private void saveNote(String noteText) {
        if (!isLoggedIn) return;
        
        File notesFile = new File(profilesDirectory, currentUsername + ".notes");
        try {
            // Append to existing notes file
            try (PrintWriter writer = new PrintWriter(new FileWriter(notesFile, true))) {
                writer.println("--- Note added on " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " ---");
                writer.println(noteText);
                writer.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // View notes
    private void viewNotes() {
        if (!isLoggedIn) return;
        
        File notesFile = new File(profilesDirectory, currentUsername + ".notes");
        if (!notesFile.exists()) {
            showMessage("No notes found for this profile", "Notes", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(notesFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Error reading notes: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JDialog notesDialog = new JDialog(parentFrame, "Your Trading Notes", true);
        notesDialog.setLayout(new BorderLayout());
        notesDialog.getContentPane().setBackground(new Color(35, 47, 62));
        
        JTextArea notesArea = new JTextArea(content.toString());
        notesArea.setBackground(new Color(25, 35, 45));
        notesArea.setForeground(Color.WHITE);
        notesArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        notesArea.setEditable(false);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(notesArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        notesDialog.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(35, 47, 62));
        
        JButton closeButton = new JButton("Close");
        styleButton(closeButton);
        closeButton.addActionListener(e -> notesDialog.dispose());
        
        buttonPanel.add(closeButton);
        notesDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        notesDialog.pack();
        notesDialog.setLocationRelativeTo(parentFrame);
        notesDialog.setVisible(true);
    }
    
    // Check if user is logged in
    public boolean isLoggedIn() {
        return isLoggedIn;
    }
    
    // Get current username
    public String getCurrentUsername() {
        return currentUsername;
    }
    
    // Called when application is closing
    public void onExit() {
        if (isLoggedIn) {
            saveUserData();
        }
    }
    
    // Create an icon for logged-in users
    private ImageIcon createLoggedInProfileIcon() {
        // Create an image for the icon
        int size = 24;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw a circle for the head
        g2d.setColor(new Color(80, 200, 120)); // Green for logged in
        int headSize = size / 2;
        g2d.fillOval(size/4, size/8, headSize, headSize);
        
        // Draw the body
        g2d.fillOval(size/4 - 2, size/2, headSize + 4, headSize + 4);
        
        // Clean up
        g2d.dispose();
        
        return new ImageIcon(image);
    }
    
    // Add a method to record a stock purchase
    public void recordPurchase(String stockName, double price) {
        if (!isLoggedIn) return;
        
        // Record transaction
        if (!transactionHistory.containsKey(stockName)) {
            transactionHistory.put(stockName, new ArrayList<>());
        }
        transactionHistory.get(stockName).add(new StockTransaction("BUY", price, 1));
        
        // Update purchase totals
        Double currentTotal = purchaseTotals.getOrDefault(stockName, 0.0);
        purchaseTotals.put(stockName, currentTotal + price);
        
        // Record initial price if not already set
        if (!initialPrices.containsKey(stockName)) {
            initialPrices.put(stockName, price);
        }
        
        // Save data
        saveUserData();
    }
    
    // Add a method to record a stock sale
    public void recordSale(String stockName, double price) {
        if (!isLoggedIn) return;
        
        // Record transaction
        if (!transactionHistory.containsKey(stockName)) {
            transactionHistory.put(stockName, new ArrayList<>());
        }
        transactionHistory.get(stockName).add(new StockTransaction("SELL", price, 1));
        
        // Save data
        saveUserData();
    }
    
    // Calculate average purchase price for a stock
    public double getAveragePurchasePrice(String stockName) {
        Integer owned = stocksOwned.get(stockName);
        if (owned == null || owned == 0) return 0.0;
        
        Double totalSpent = purchaseTotals.getOrDefault(stockName, 0.0);
        return totalSpent / owned;
    }
    
    // Calculate profit/loss for a stock
    public double getProfitLoss(String stockName) {
        Integer owned = stocksOwned.get(stockName);
        if (owned == null || owned == 0) return 0.0;
        
        Double currentPrice = currentPrices.get(stockName);
        if (currentPrice == null) return 0.0;
        
        Double avgPurchase = getAveragePurchasePrice(stockName);
        return (currentPrice - avgPurchase) * owned;
    }
    
    // Calculate percentage change since initial price
    public double getMarketPerformance(String stockName) {
        Double initial = initialPrices.get(stockName);
        Double current = currentPrices.get(stockName);
        
        if (initial == null || current == null) return 0.0;
        
        return ((current - initial) / initial) * 100.0;
    }
    
    // Add a method to view portfolio statistics
    public void viewPortfolioStats() {
        if (!isLoggedIn) return;
        
        JDialog statsDialog = new JDialog(parentFrame, "Portfolio Statistics", true);
        statsDialog.setLayout(new BorderLayout());
        statsDialog.getContentPane().setBackground(new Color(35, 47, 62));
        
        // Create tabbed pane for different stats views
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(35, 47, 62));
        tabbedPane.setForeground(Color.WHITE);
        
        // Overview panel
        JPanel overviewPanel = createStatsPanel(generatePortfolioSummary());
        tabbedPane.addTab("Overview", overviewPanel);
        
        // Transaction History panel
        JPanel historyPanel = createTransactionHistoryPanel();
        tabbedPane.addTab("Transaction History", historyPanel);
        
        // Performance Chart panel
        JPanel chartPanel = createPerformancePanel();
        tabbedPane.addTab("Performance", chartPanel);
        
        // Add tabbed pane to dialog
        statsDialog.add(tabbedPane, BorderLayout.CENTER);
        
        // Add close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(35, 47, 62));
        
        JButton closeButton = new JButton("Close");
        styleButton(closeButton);
        closeButton.addActionListener(e -> statsDialog.dispose());
        buttonPanel.add(closeButton);
        
        statsDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Set size and display
        statsDialog.setSize(700, 500);
        statsDialog.setLocationRelativeTo(parentFrame);
        statsDialog.setVisible(true);
    }
    
    private JPanel createStatsPanel(String content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(35, 47, 62));
        
        JTextArea textArea = new JTextArea(content);
        textArea.setEditable(false);
        textArea.setBackground(new Color(25, 35, 45));
        textArea.setForeground(Color.WHITE);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createTransactionHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(35, 47, 62));
        
        // Create column headers
        String[] columnNames = {"Date", "Stock", "Type", "Quantity", "Price", "Total"};
        
        // Count total transactions
        int totalTransactions = 0;
        for (List<StockTransaction> transactions : transactionHistory.values()) {
            totalTransactions += transactions.size();
        }
        
        // Create data array
        Object[][] data = new Object[totalTransactions][6];
        
        // Fill data array
        int row = 0;
        for (Map.Entry<String, List<StockTransaction>> entry : transactionHistory.entrySet()) {
            String stockName = entry.getKey();
            for (StockTransaction transaction : entry.getValue()) {
                data[row][0] = transaction.getFormattedDate();
                data[row][1] = stockName;
                data[row][2] = transaction.type;
                data[row][3] = transaction.quantity;
                data[row][4] = "$" + String.format("%.2f", transaction.price);
                data[row][5] = "$" + String.format("%.2f", transaction.price * transaction.quantity);
                row++;
            }
        }
        
        // Create table with custom renderer for colors
        JTable table = new JTable(data, columnNames) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                
                // Color rows based on transaction type
                if (getValueAt(row, 2).equals("BUY")) {
                    comp.setForeground(new Color(40, 167, 69)); // Green for buys
                } else {
                    comp.setForeground(new Color(220, 53, 69)); // Red for sells
                }
                
                return comp;
            }
        };
        
        // Style the table
        table.setBackground(new Color(25, 35, 45));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(60, 70, 80));
        table.setRowHeight(25);
        table.getTableHeader().setBackground(new Color(45, 55, 65));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createPerformancePanel() {
        // Simple text panel for now as chart implementation would require more code
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(35, 47, 62));
        
        StringBuilder content = new StringBuilder();
        content.append("MARKET PERFORMANCE SUMMARY\n\n");
        
        // Overall market performance
        for (Map.Entry<String, Double> entry : initialPrices.entrySet()) {
            String stockName = entry.getKey();
            Double initialPrice = entry.getValue();
            Double currentPrice = currentPrices.get(stockName);
            
            if (currentPrice != null && initialPrice != null) {
                double performance = ((currentPrice - initialPrice) / initialPrice) * 100.0;
                content.append(stockName).append(" Performance:\n");
                content.append("Initial Price: $").append(String.format("%.2f", initialPrice)).append("\n");
                content.append("Current Price: $").append(String.format("%.2f", currentPrice)).append("\n");
                content.append("Change: ").append(performance >= 0 ? "+" : "")
                       .append(String.format("%.2f", performance)).append("%\n\n");
            }
        }
        
        // Portfolio performance
        content.append("YOUR PORTFOLIO PERFORMANCE\n\n");
        
        double totalInvested = 0.0;
        double totalCurrentValue = 0.0;
        
        for (Map.Entry<String, Integer> entry : stocksOwned.entrySet()) {
            String stockName = entry.getKey();
            Integer quantity = entry.getValue();
            
            if (quantity > 0 && purchaseTotals.containsKey(stockName) && currentPrices.containsKey(stockName)) {
                double invested = purchaseTotals.get(stockName);
                double currentValue = currentPrices.get(stockName) * quantity;
                double performance = ((currentValue - invested) / invested) * 100.0;
                
                totalInvested += invested;
                totalCurrentValue += currentValue;
                
                content.append(stockName).append(" Holdings:\n");
                content.append("Invested: $").append(String.format("%.2f", invested)).append("\n");
                content.append("Current Value: $").append(String.format("%.2f", currentValue)).append("\n");
                content.append("Profit/Loss: ").append(performance >= 0 ? "+" : "")
                       .append(String.format("%.2f", performance)).append("%\n\n");
            }
        }
        
        if (totalInvested > 0) {
            double overallPerformance = ((totalCurrentValue - totalInvested) / totalInvested) * 100.0;
            content.append("OVERALL PORTFOLIO PERFORMANCE: ").append(overallPerformance >= 0 ? "+" : "")
                   .append(String.format("%.2f", overallPerformance)).append("%\n");
        } else {
            content.append("No investments to calculate performance.\n");
        }
        
        return createStatsPanel(content.toString());
    }

    // Add a new method to update stock panels
    public void updateStockPanels() {
        // This method will be called from Main.java
        // We'll add implementation there since ProfileManager doesn't have direct access to UI panels
    }
} 