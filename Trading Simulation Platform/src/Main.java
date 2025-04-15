import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyleContext;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.Element;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        // Fix for high DPI displays
        System.setProperty("sun.java2d.uiScale", "1.0");

        // Use array to make balance mutable inside inner classes
        double[] balance = {1000.0};

        // Main frame
        JFrame frame = new JFrame("Stock Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true); // Remove window decorations
        
        // Get screen size and set frame to full screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screenSize);
        
        // Create a custom UI manager to style dialogs
        UIManager.put("OptionPane.background", new Color(35, 47, 62));
        UIManager.put("Panel.background", new Color(35, 47, 62));
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        
        // Style the OK button in dialogs
        UIManager.put("Button.background", new Color(50, 168, 82));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));
        UIManager.put("Button.focus", new Color(50, 168, 82));
        UIManager.put("Button.select", new Color(40, 158, 72));

        // CardLayout to switch between screens
        CardLayout cardLayout = new CardLayout();
        JPanel mainPanel = new JPanel(cardLayout);

        // ------------------- Welcome Screen -------------------
        JPanel welcomeScreen = new JPanel();
        welcomeScreen.setLayout(null);
        welcomeScreen.setBackground(new Color(25, 25, 35)); // Dark blue background

        // Add a stylish logo/header
        JLabel logoLabel = new JLabel("STOCK MARKET SIMULATOR", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Montserrat", Font.BOLD, 48));
        logoLabel.setForeground(Color.WHITE);
        // Center horizontally and position at 15% of screen height
        logoLabel.setBounds(0, (int)(screenSize.height * 0.15), screenSize.width, 70);
        welcomeScreen.add(logoLabel);

        // Add a tagline
        JLabel taglineLabel = new JLabel("Trade. Invest. Profit.", SwingConstants.CENTER);
        taglineLabel.setFont(new Font("Segoe UI", Font.ITALIC, 24));
        taglineLabel.setForeground(new Color(0, 185, 255)); // Light blue
        // Center horizontally and position below the logo
        taglineLabel.setBounds(0, (int)(screenSize.height * 0.25), screenSize.width, 40);
        welcomeScreen.add(taglineLabel);

        // Create a stylish Enter button
        JButton enterButton = new JButton("START TRADING");
        enterButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        enterButton.setBackground(new Color(50, 168, 82)); // Green
        enterButton.setForeground(Color.WHITE);
        // Center horizontally at 50% of screen width and position at 45% of screen height
        int buttonWidth = 300;
        int buttonHeight = 80;
        enterButton.setBounds((screenSize.width - buttonWidth) / 2, 
                             (int)(screenSize.height * 0.45), 
                             buttonWidth, buttonHeight);
        enterButton.setFocusPainted(false);
        enterButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        enterButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        welcomeScreen.add(enterButton);
        
        // Add a quit button
        JButton quitButton = new JButton("QUIT");
        quitButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        quitButton.setBackground(new Color(220, 53, 69)); // Red
        quitButton.setForeground(Color.WHITE);
        // Center horizontally at 50% of screen width and position below enter button
        quitButton.setBounds((screenSize.width - buttonWidth) / 2, 
                           (int)(screenSize.height * 0.6), 
                           buttonWidth, 60);
        quitButton.setFocusPainted(false);
        quitButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        quitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        quitButton.addActionListener(e -> System.exit(0)); // Exit the application
        welcomeScreen.add(quitButton);

        // Add a bottom credit line
        JLabel creditLabel = new JLabel("© 2025 Stock Simulator | Developed by Mykyta Grogul", SwingConstants.CENTER);
        creditLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        creditLabel.setForeground(new Color(150, 150, 150));
        creditLabel.setBounds(0, screenSize.height - 40, screenSize.width, 20);  // Adjusted for screen height
        welcomeScreen.add(creditLabel);

        // ------------------- Simulation Screen -------------------
        JPanel simulationScreen = new JPanel();
        simulationScreen.setLayout(new BorderLayout());
        simulationScreen.setBackground(new Color(240, 242, 245)); // Light gray background

        // Top panel for controls
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(null);
        controlPanel.setPreferredSize(new Dimension(1000, 60)); // Reduced height since we don't need space for Trade button
        controlPanel.setBackground(new Color(35, 47, 62));
        controlPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, new Color(20, 30, 40)));

        // Title
        JLabel titleLabel = new JLabel("TRADING DASHBOARD");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(160, 15, 300, 30);  // Moved right to not overlap with back button
        controlPanel.add(titleLabel);

        // Balance display with icon
        JLabel balanceIcon = new JLabel("💰");
        balanceIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 24));
        balanceIcon.setForeground(new Color(255, 215, 0));  // Gold color for better visibility
        balanceIcon.setBounds(screenSize.width - 240, 15, 30, 30);  // Move to far right corner
        controlPanel.add(balanceIcon);

        JLabel BalanceView = new JLabel("$" + String.format("%.2f", balance[0]));
        BalanceView.setFont(new Font("Segoe UI", Font.BOLD, 22));
        BalanceView.setForeground(new Color(50, 168, 82));
        BalanceView.setBounds(screenSize.width - 200, 15, 200, 30);  // Move to far right corner
        controlPanel.add(BalanceView);

        // Back button with icon
        JButton backButton = new JButton("← Back");
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backButton.setBackground(new Color(70, 80, 90));
        backButton.setForeground(Color.WHITE);
        backButton.setBounds(20, 15, 120, 30);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        controlPanel.add(backButton);

        // Create main content panel with BorderLayout
        JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
        contentPanel.setBackground(new Color(35, 47, 62));

        // Create a text area for console/chat
        JTextPane consoleTextArea = new JTextPane();
        consoleTextArea.setEditable(false);
        consoleTextArea.setBackground(new Color(25, 35, 45));
        
        // Add welcome message with style
        StyledDocument doc = consoleTextArea.getStyledDocument();
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet welcomeStyle = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, new Color(0, 185, 255));
        welcomeStyle = sc.addAttribute(welcomeStyle, StyleConstants.FontFamily, "Monospaced");
        welcomeStyle = sc.addAttribute(welcomeStyle, StyleConstants.FontSize, 12);
        welcomeStyle = sc.addAttribute(welcomeStyle, StyleConstants.Bold, true);
        
        try {
            doc.insertString(0, "Welcome to Stock Simulator!", welcomeStyle);
            doc.insertString(doc.getLength(), "\nYour transactions will appear here.", sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.WHITE));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        consoleTextArea.setCaretPosition(0);
        consoleTextArea.setMargin(new Insets(10, 10, 10, 10));
        consoleTextArea.setCaretColor(new Color(50, 168, 82));

        // Function to append messages to the console
        class ConsoleLogger {
            void log(String message) {
                if (message.startsWith("Bought")) {
                    appendToConsole(message, new Color(40, 167, 69));  // Green for purchases
                } else if (message.startsWith("Sold")) {
                    appendToConsole(message, new Color(220, 53, 69));  // Red for sales
                } else {
                    appendToConsole(message, new Color(255, 193, 7));  // Yellow for warnings/errors
                }
            }
            
            private void appendToConsole(String message, Color color) {
                // Use styled document for colored text
                try {
                    Document doc = consoleTextArea.getDocument();
                    StyleContext sc = StyleContext.getDefaultStyleContext();
                    AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);
                    aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Monospaced");
                    aset = sc.addAttribute(aset, StyleConstants.FontSize, 12);
                    
                    // Add a new line and the message
                    doc.insertString(doc.getLength(), "\n" + message, aset);
                    
                    // Scroll to the bottom
                    consoleTextArea.setCaretPosition(doc.getLength());
                } catch (Exception e) {
                    System.err.println("Error logging to console: " + e.getMessage());
                }
            }
        }
        
        ConsoleLogger logger = new ConsoleLogger();

        // Create left panel for stocks
        JPanel stocksPanel = new JPanel();
        stocksPanel.setLayout(new BoxLayout(stocksPanel, BoxLayout.Y_AXIS));
        stocksPanel.setBackground(new Color(35, 47, 62));
        stocksPanel.setPreferredSize(new Dimension(350, 0));  // Increased width
        stocksPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));  // Added more padding

        // Create a single chart showing all three stocks
        StockChartPanel chartPanel = new StockChartPanel();
        chartPanel.addStock("IKEA", 250);
        chartPanel.addStock("JULA", 225);
        chartPanel.addStock("MAX", 200);

        // Stocks and their prices
        Map<String, Integer> investables = new HashMap<>();
        investables.put("IKEA", 250);
        investables.put("JULA", 225);
        investables.put("MAX", 200);
        
        // Create a map to track current prices
        Map<String, Double> currentPrices = new HashMap<>();
        currentPrices.put("IKEA", 250.0);
        currentPrices.put("JULA", 225.0);
        currentPrices.put("MAX", 200.0);
        
        // Store the trend direction and strength for each stock
        Map<String, Double> stockTrends = new HashMap<>();
        stockTrends.put("IKEA", 0.2);  // Slightly positive trend
        stockTrends.put("JULA", 0.0);  // Neutral trend
        stockTrends.put("MAX", 0.1);   // Slight positive trend
        
        // Track volatility for each stock
        Map<String, Double> stockVolatility = new HashMap<>();
        stockVolatility.put("IKEA", 2.0);  // Medium volatility
        stockVolatility.put("JULA", 1.5);  // Lower volatility
        stockVolatility.put("MAX", 3.0);   // Higher volatility
        
        // Track time since last major price jump
        Map<String, Integer> timeSinceJump = new HashMap<>();
        timeSinceJump.put("IKEA", 0);
        timeSinceJump.put("JULA", 0);
        timeSinceJump.put("MAX", 0);
        
        // Constants for price simulation
        final double TREND_CHANGE_PROBABILITY = 0.05;  // 5% chance of trend change each tick
        final double JUMP_PROBABILITY = 0.01;         // 1% chance of price jump
        final double MAX_TREND = 0.5;                 // Maximum trend strength
        final int JUMP_COOLDOWN = 20;                // Minimum ticks between jumps

        // Add this after your investables map (around line 77):
        Map<String, Integer> stocksOwned = new HashMap<>();  // Track how many of each stock the user owns
        stocksOwned.put("IKEA", 0);
        stocksOwned.put("JULA", 0);
        stocksOwned.put("MAX", 0);

        // Add a Map for stock colors right after your other maps (line ~86):
        Map<String, Color> stockColors = new HashMap<>();
        stockColors.put("IKEA", new Color(0, 92, 185)); // IKEA blue
        stockColors.put("JULA", new Color(220, 53, 69)); // Red
        stockColors.put("MAX", new Color(40, 167, 69)); // Green

        // Add stocks to the left panel instead of deployable grid
        Map<String, JLabel> stockLabels = new HashMap<>();
        for (Map.Entry<String, Integer> entry : investables.entrySet()) {
            String stockName = entry.getKey();
            int stockPrice = entry.getValue();

            JPanel stockPanel = new JPanel();
            stockPanel.setLayout(new BorderLayout());
            stockPanel.setBackground(new Color(35, 47, 62));
            stockPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 70, 80)));
            stockPanel.setPreferredSize(new Dimension(330, 100));  // Increased size
            stockPanel.setMaximumSize(new Dimension(330, 100));  // Increased size
            stockPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 70, 80)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)  // Added internal padding
            ));

            // Left side: stock info
            JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 10));
            infoPanel.setBackground(new Color(35, 47, 62));
            infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 15)); // Reduced right padding
            
            // Top row with stock name and owned count
            JPanel topRow = new JPanel(new BorderLayout(10, 0)); // Reduced horizontal gap
            topRow.setBackground(new Color(35, 47, 62));
            
            JLabel stockLabel = new JLabel(stockName);
            stockLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            if (stockName.equals("JULA")) {
                stockLabel.setForeground(new Color(220, 53, 69));
            } else if (stockName.equals("IKEA")) {
                stockLabel.setForeground(new Color(0, 92, 185));
            } else {
                stockLabel.setForeground(new Color(40, 167, 69));
            }
            topRow.add(stockLabel, BorderLayout.WEST);
            stockLabels.put(stockName, stockLabel);
            
            JLabel ownedLabel = new JLabel("Owned: 0");
            ownedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            ownedLabel.setForeground(Color.WHITE);
            ownedLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5)); // Reduced right padding
            topRow.add(ownedLabel, BorderLayout.EAST);
            
            JLabel priceLabel = new JLabel("$" + String.format("%.2f", (double)stockPrice));
            priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 22)); // Increased font size
            priceLabel.setForeground(Color.WHITE);
            
            infoPanel.add(topRow);
            infoPanel.add(priceLabel);
            stockPanel.add(infoPanel, BorderLayout.CENTER);
            
            // Right side: buttons
            JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 8)); // Increased gap
            buttonPanel.setBackground(new Color(35, 47, 62));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 5)); // Added left padding
            
            JButton buyButton = new JButton("Buy");
            buyButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            buyButton.setBackground(new Color(40, 167, 69));
            buyButton.setForeground(Color.WHITE);
            buyButton.setFocusPainted(false);
            buyButton.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            buyButton.setPreferredSize(new Dimension(70, 35)); // Increased width slightly
            buttonPanel.add(buyButton);
            
            JButton sellButton = new JButton("Sell");
            sellButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            sellButton.setBackground(new Color(220, 53, 69));
            sellButton.setForeground(Color.WHITE);
            sellButton.setEnabled(false);
            sellButton.setFocusPainted(false);
            sellButton.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            sellButton.setPreferredSize(new Dimension(70, 35)); // Increased width slightly
            buttonPanel.add(sellButton);
            
            stockPanel.add(buttonPanel, BorderLayout.EAST);
            stocksPanel.add(stockPanel);
            stocksPanel.add(Box.createRigidArea(new Dimension(0, 10)));

            // When Buy button is clicked
            buyButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Get current price directly from the chart panel
                    double currentPrice = chartPanel.getCurrentPrice(stockName);
                    
                    if (balance[0] >= currentPrice) {
                        // Deduct current price and update balance label
                        balance[0] -= currentPrice;
                        balance[0] = Math.round(balance[0] * 100.0) / 100.0; // Round to 2 decimal places
                        BalanceView.setText("$" + String.format("%.2f", balance[0]));
                        
                        // Increase owned stocks
                        int owned = stocksOwned.get(stockName) + 1;
                        stocksOwned.put(stockName, owned);
                        ownedLabel.setText("Owned: " + owned);
                        
                        // Enable sell button if at least one stock is owned
                        sellButton.setEnabled(true);
                        
                        String message = "Bought 1 " + stockName + " for $" + String.format("%.2f", currentPrice);
                        System.out.println(message);
                        logger.log(message);
                        
                        // Update the chart without changing the price locally
                        // The price is fully managed by the chart panel now
                        chartPanel.updateStock(stockName);
                        
                        // Update the label with new price from chart
                        updateStockLabel(stockName, chartPanel.getCurrentPrice(stockName), stockLabels);
                    } else {
                        // Show popup if not enough money
                        String message = "Not enough balance to buy " + stockName + "!";
                        
                        // Create custom dialog
                        JDialog dialog = new JDialog(frame, "Message", true);
                        dialog.setLayout(new BorderLayout());
                        dialog.getContentPane().setBackground(new Color(35, 47, 62));
                        
                        JPanel messagePanel = new JPanel(new BorderLayout(20, 10));
                        messagePanel.setBackground(new Color(35, 47, 62));
                        messagePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                        
                        // Icon
                        JLabel iconLabel = new JLabel("\uD83D\uDCB8"); // Money with wings emoji
                        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 40));
                        iconLabel.setForeground(new Color(220, 53, 69)); // Red
                        messagePanel.add(iconLabel, BorderLayout.WEST);
                        
                        // Message
                        JLabel messageLabel = new JLabel(message);
                        messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                        messageLabel.setForeground(Color.WHITE);
                        messagePanel.add(messageLabel, BorderLayout.CENTER);
                        
                        // Button panel
                        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                        buttonPanel.setBackground(new Color(35, 47, 62));
                        
                        JButton okButton = new JButton("OK");
                        okButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
                        okButton.setBackground(new Color(50, 168, 82)); // Green
                        okButton.setForeground(Color.WHITE);
                        okButton.setFocusPainted(false);
                        okButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
                        okButton.addActionListener(e2 -> dialog.dispose());
                        buttonPanel.add(okButton);
                        
                        dialog.add(messagePanel, BorderLayout.CENTER);
                        dialog.add(buttonPanel, BorderLayout.SOUTH);
                        
                        dialog.pack();
                        dialog.setLocationRelativeTo(frame);
                        dialog.setVisible(true);
                        
                        logger.log(message);
                    }
                }
            });
            
            // When Sell button is clicked
            sellButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int owned = stocksOwned.get(stockName);
                    
                    if (owned > 0) {
                        // Get current price directly from the chart panel
                        double currentPrice = chartPanel.getCurrentPrice(stockName);
                        
                        // Add price to balance
                        balance[0] += currentPrice;
                        balance[0] = Math.round(balance[0] * 100.0) / 100.0; // Round to 2 decimal places
                        BalanceView.setText("$" + String.format("%.2f", balance[0]));
                        
                        // Decrease owned stocks
                        owned--;
                        stocksOwned.put(stockName, owned);
                        ownedLabel.setText("Owned: " + owned);
                        
                        // Disable sell button if no stocks left
                        if (owned == 0) {
                            sellButton.setEnabled(false);
                        }
                        
                        String message = "Sold 1 " + stockName + " for $" + String.format("%.2f", currentPrice);
                        System.out.println(message);
                        logger.log(message);
                        
                        // Update the chart without changing the price locally
                        // The price is fully managed by the chart panel now
                        chartPanel.updateStock(stockName);
                        
                        // Update the label with new price from chart
                        updateStockLabel(stockName, chartPanel.getCurrentPrice(stockName), stockLabels);
                    }
                }
            });
        }

        // Create a console/chat panel to show transaction history
        JPanel consolePanel = new JPanel(new BorderLayout());
        consolePanel.setBackground(new Color(25, 35, 45));
        consolePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(60, 70, 80)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel consoleTitle = new JLabel("Transaction History");
        consoleTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        consoleTitle.setForeground(Color.WHITE);
        consolePanel.add(consoleTitle, BorderLayout.NORTH);
        
        // Create console panel with styled scrollbar
        JScrollPane scrollPane = new JScrollPane(consoleTextArea);
        scrollPane.setPreferredSize(new Dimension(330, 180));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Hide scrollbar but keep functionality
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Custom styling for the console text area
        consoleTextArea.setMargin(new Insets(10, 10, 10, 10));
        consoleTextArea.setCaretColor(new Color(50, 168, 82)); // Green caret
        
        // Add custom mouse wheel scrolling since scrollbar is hidden
        consoleTextArea.addMouseWheelListener(e -> {
            JScrollBar vBar = scrollPane.getVerticalScrollBar();
            vBar.setValue(vBar.getValue() + e.getUnitsToScroll() * 3);
        });
        
        consolePanel.add(scrollPane, BorderLayout.CENTER);

        // Add stocks and console to the left panel
        JPanel leftContainerPanel = new JPanel(new BorderLayout());
        leftContainerPanel.setBackground(new Color(35, 47, 62));
        leftContainerPanel.add(stocksPanel, BorderLayout.CENTER);
        leftContainerPanel.add(consolePanel, BorderLayout.SOUTH);

        // Add components to content panel
        contentPanel.add(leftContainerPanel, BorderLayout.WEST);
        contentPanel.add(chartPanel, BorderLayout.CENTER);

        // Add panels to simulation screen
        simulationScreen.add(controlPanel, BorderLayout.NORTH);
        simulationScreen.add(contentPanel, BorderLayout.CENTER);

        // Create a timer to update the stock prices
        Timer priceUpdateTimer = new Timer(1000, e -> {
            // Update prices using a more realistic algorithm
            for (String stockName : investables.keySet()) {
                // Get the current price directly from the chart for accuracy
                double price = chartPanel.getCurrentPrice(stockName);
                
                // Update our tracking maps with the actual chart price
                currentPrices.put(stockName, price);
                
                // Update stock label with the chart's actual price
                updateStockLabel(stockName, price, stockLabels);
                
                // The following code is now redundant since we're getting prices from the chart
                // But we'll leave these variables intact for possible future changes
                double trend = stockTrends.get(stockName);
                double volatility = stockVolatility.get(stockName);
                int ticksSinceJump = timeSinceJump.get(stockName);
                
                // Update jump tracking variables
                if (Math.random() < JUMP_PROBABILITY && ticksSinceJump > JUMP_COOLDOWN) {
                    boolean positiveJump = Math.random() < (0.5 + trend);
                    String jumpDirection = positiveJump ? "surged" : "dropped";
                    String message = stockName + " " + jumpDirection + " on market news!";
                    logger.log(message);
                    timeSinceJump.put(stockName, 0);
                } else {
                    timeSinceJump.put(stockName, ticksSinceJump + 1);
                }
            }
        });
        priceUpdateTimer.start();

        // Toggle the deployable grid
        enterButton.addActionListener(e -> {
            cardLayout.show(mainPanel, "Simulation");
            priceUpdateTimer.start();
        });
        backButton.addActionListener(e -> {
            cardLayout.show(mainPanel, "Welcome");
            // Stop chart updates and price updates when going back to welcome screen
            chartPanel.stopUpdating();
            priceUpdateTimer.stop();
        });

        // ------------------- Add Panels to Main -------------------
        mainPanel.add(welcomeScreen, "Welcome");
        mainPanel.add(simulationScreen, "Simulation");

        frame.add(mainPanel);
        frame.setVisible(true);

        // Set custom application icon
        try {
            ImageIcon icon = new ImageIcon(Main.class.getClassLoader().getResource("icon.png"));
            if (icon.getImage() != null) {
                frame.setIconImage(icon.getImage());
            } else {
                System.out.println("Could not load icon");
            }
        } catch (Exception e) {
            System.out.println("Failed to load icon: " + e.getMessage());
        }
    }

    private static void updateStockLabel(String stockName, double currentPrice, Map<String, JLabel> labels) {
        JLabel label = labels.get(stockName);
        if (label != null) {
            // Find the price label (should be in a different hierarchy now)
            JPanel parentPanel = (JPanel)label.getParent().getParent().getParent(); // Get to the main stock panel
            Component[] components = ((JPanel)label.getParent().getParent()).getComponents();
            
            if (components.length > 1 && components[1] instanceof JLabel) {
                JLabel priceLabel = (JLabel)components[1];
                double oldPrice = 0;
                try {
                    String oldText = priceLabel.getText().substring(1); // Remove $
                    oldPrice = Double.parseDouble(oldText);
                } catch (Exception e) {
                    oldPrice = currentPrice;
                }
                
                // Always white text, but could add slight green/red tint
                priceLabel.setForeground(Color.WHITE);
                
                // Update the text
                priceLabel.setText("$" + String.format("%.2f", currentPrice));
            }
        }
    }
}
