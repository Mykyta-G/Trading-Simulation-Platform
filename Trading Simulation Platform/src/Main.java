import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
// Temporarily comment out XChart imports until library is available
// import org.knowm.xchart.*;
// import org.knowm.xchart.style.Styler;
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
import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.border.Border;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public class Main {
    // Add profileManager to the Main class
    private static ProfileManager profileManager;

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

        // Modify Balance display position (adjust position due to profile panel)
        JLabel balanceIcon = new JLabel("💰");
        balanceIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 24));
        balanceIcon.setForeground(new Color(255, 215, 0));  // Gold color for better visibility
        balanceIcon.setBounds(screenSize.width - 150, 15, 30, 30);  // Adjusted position
        controlPanel.add(balanceIcon);

        JLabel BalanceView = new JLabel("$" + String.format("%.2f", balance[0]));
        BalanceView.setFont(new Font("Segoe UI", Font.BOLD, 22));
        BalanceView.setForeground(new Color(50, 168, 82));
        BalanceView.setBounds(screenSize.width - 110, 15, 100, 30);  // Adjusted position
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
        
        // Map to store purchase prices for each stock
        Map<String, java.util.List<Double>> purchasePrices = new HashMap<>();
        purchasePrices.put("IKEA", new ArrayList<>());
        purchasePrices.put("JULA", new ArrayList<>());
        purchasePrices.put("MAX", new ArrayList<>());

        // Add a Map for stock colors right after your other maps (line ~86):
        Map<String, Color> stockColors = new HashMap<>();
        stockColors.put("IKEA", new Color(0, 92, 185)); // IKEA blue
        stockColors.put("JULA", new Color(220, 53, 69)); // Red
        stockColors.put("MAX", new Color(40, 167, 69)); // Green

        // Initialize profile manager (without callback yet)
        profileManager = new ProfileManager(frame, stocksOwned, balance, currentPrices);

        // Add profile panel to the control panel
        JPanel profilePanel = profileManager.createProfilePanel();
        profilePanel.setBounds(screenSize.width - 350, 12, 200, 36);
        controlPanel.add(profilePanel);

        // Also fix the balance icon and label positions
        balanceIcon.setBounds(screenSize.width - 150, 15, 30, 30);  // Adjusted position
        BalanceView.setBounds(screenSize.width - 110, 15, 100, 30);  // Adjusted position

        // Create the main container with BorderLayout to use full width
        JPanel mainContainer = new JPanel(new BorderLayout(0, 0));
        mainContainer.setBackground(new Color(35, 47, 62));
        
        // Create left panel for stocks with fixed width
        JPanel stocksPanel = new JPanel();
        stocksPanel.setLayout(new BoxLayout(stocksPanel, BoxLayout.Y_AXIS));
        stocksPanel.setBackground(new Color(35, 47, 62));
        stocksPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));  // Added more padding
        
        // Set preferred size for stock panels - maximizing usable space
        int stockPanelWidth = 320;
        
        // Make stocks panel scrollable
        JScrollPane stocksScrollPane = new JScrollPane(stocksPanel);
        stocksScrollPane.setBackground(new Color(35, 47, 62));
        stocksScrollPane.setBorder(BorderFactory.createEmptyBorder());
        stocksScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        stocksScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        stocksScrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smoother scrolling
        
        // Set preferred size for the left panel - adequate width to fit content
        stocksScrollPane.setPreferredSize(new Dimension(stockPanelWidth, screenSize.height));
        
        // Customize the scroll bar
        stocksScrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(70, 80, 90);
                this.trackColor = new Color(45, 57, 72);
            }
        });

        // Add stocks to the left panel
        Map<String, JLabel> stockLabels = new HashMap<>();
        Map<String, JLabel> priceLabels = new HashMap<>(); // Direct mapping to price labels
        for (Map.Entry<String, Integer> entry : investables.entrySet()) {
            String stockName = entry.getKey();
            int stockPrice = entry.getValue();

            JPanel stockPanel = new JPanel();
            stockPanel.setLayout(new BorderLayout());
            stockPanel.setBackground(new Color(35, 47, 62));
            stockPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 70, 80)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)  // Added internal padding
            ));
            stockPanel.setMaximumSize(new Dimension(stockPanelWidth, 300)); // Match the new width

            // Left side: stock info
            JPanel infoPanel = new JPanel(new BorderLayout());
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
            
            // Create middle panel for current price
            JPanel middlePanel = new JPanel(new BorderLayout());
            middlePanel.setBackground(new Color(35, 47, 62));
            
            JLabel priceLabel = new JLabel("$" + String.format("%.2f", (double)stockPrice));
            priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 22)); // Increased font size
            priceLabel.setForeground(Color.WHITE);
            priceLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // Add vertical padding
            middlePanel.add(priceLabel, BorderLayout.WEST);
            priceLabels.put(stockName, priceLabel); // Store direct reference to price label
            
            // Create bottom panel for purchase info
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setBackground(new Color(35, 47, 62));
            
            // Add all panels to infoPanel
            infoPanel.add(topRow, BorderLayout.NORTH);
            infoPanel.add(middlePanel, BorderLayout.CENTER);
            infoPanel.add(bottomPanel, BorderLayout.SOUTH);
            
            // Create description panel
            JPanel descriptionPanel = new JPanel(new BorderLayout());
            descriptionPanel.setBackground(new Color(35, 47, 62));
            descriptionPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            
            JTextArea descriptionText = new JTextArea();
            descriptionText.setBackground(new Color(40, 52, 67));
            descriptionText.setForeground(new Color(200, 200, 200));
            descriptionText.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            descriptionText.setLineWrap(true);
            descriptionText.setWrapStyleWord(true);
            descriptionText.setEditable(false);
            descriptionText.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            
            // Set company descriptions
            String description = "";
            if (stockName.equals("IKEA")) {
                description = "IKEA is a multinational furniture retailer known for its ready-to-assemble furniture, kitchen appliances, and home accessories. Founded in Sweden, it has become the world's largest furniture retailer since 2008.";
            } else if (stockName.equals("JULA")) {
                description = "Jula is a Swedish chain of department stores offering hardware, tools, gardening supplies, and household items. The company has expanded throughout Scandinavia with a focus on DIY products at competitive prices.";
            } else if (stockName.equals("MAX")) {
                description = "MAX is Sweden's oldest hamburger chain, founded in 1968. The company focuses on high-quality, locally-sourced ingredients and was the first hamburger chain in the world to label its products with carbon emissions data.";
            }
            
            descriptionText.setText(description);
            descriptionPanel.add(descriptionText, BorderLayout.CENTER);
            
            // Always show description - no toggle needed anymore
            infoPanel.add(descriptionPanel, BorderLayout.SOUTH);
            
            // Add main info panel to stock panel
            stockPanel.add(infoPanel, BorderLayout.CENTER);
            
            // Create buttons panel at the bottom
            JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
            buttonPanel.setBackground(new Color(35, 47, 62));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
            
            // Create Buy button
            JButton buyButton = new JButton("Buy");
            buyButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            buyButton.setBackground(new Color(40, 167, 69)); // Green
            buyButton.setForeground(Color.WHITE);
            buyButton.setFocusPainted(false);
            buyButton.setOpaque(true);
            
            // Create rounded border and better styling
            buyButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(30, 130, 55), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            buttonPanel.add(buyButton);
            
            // Create Sell button
            JButton sellButton = new JButton("Sell");
            sellButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            sellButton.setBackground(new Color(220, 53, 69)); // Red
            sellButton.setForeground(Color.WHITE);
            sellButton.setEnabled(false);
            sellButton.setFocusPainted(false);
            sellButton.setOpaque(true);
            
            // Create rounded border and better styling
            sellButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 40, 50), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            buttonPanel.add(sellButton);
            
            // Add buttons panel below info panel
            stockPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            // Add ownership info panel at the bottom
            JPanel ownershipPanel = new JPanel(new BorderLayout(0, 2));
            ownershipPanel.setBackground(new Color(35, 47, 62));
            ownershipPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            
            // Create vertical panel for owned count and average price
            JPanel ownershipInfoPanel = new JPanel(new GridLayout(2, 1, 0, 2));
            ownershipInfoPanel.setBackground(new Color(35, 47, 62));
            
            JLabel ownedLabel = new JLabel("Owned: 0");
            ownedLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            ownedLabel.setForeground(Color.WHITE);
            ownershipInfoPanel.add(ownedLabel);
            
            // Add purchase info label
            JLabel purchaseInfoLabel = new JLabel("");
            purchaseInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            purchaseInfoLabel.setForeground(new Color(220, 220, 220));
            ownershipInfoPanel.add(purchaseInfoLabel);
            
            // Add ownership info panel to main ownership panel
            ownershipPanel.add(ownershipInfoPanel, BorderLayout.CENTER);
            
            // Add ownership panel to main panel - move to top for better visibility
            topRow.add(ownershipPanel, BorderLayout.EAST);
            
            // Add stock panel to main container
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
                        
                        // Store purchase price
                        purchasePrices.get(stockName).add(currentPrice);
                        
                        // Update purchase info display
                        updatePurchaseInfo(stockName, purchaseInfoLabel, purchasePrices.get(stockName));
                        
                        // Enable sell button if at least one stock is owned
                        sellButton.setEnabled(true);
                        
                        String message = "Bought 1 " + stockName + " for $" + String.format("%.2f", currentPrice);
                        System.out.println(message);
                        logger.log(message);
                        
                        // Record purchase in profile manager
                        if (profileManager.isLoggedIn()) {
                            profileManager.recordPurchase(stockName, currentPrice);
                        }
                        
                        // Update the chart without changing the price locally
                        // The price is fully managed by the chart panel now
                        chartPanel.updateStock(stockName);
                        
                        // Update the label with new price from chart directly
                        JLabel priceLabel = priceLabels.get(stockName);
                        if (priceLabel != null) {
                            priceLabel.setText("$" + String.format("%.2f", chartPanel.getCurrentPrice(stockName)));
                            priceLabel.repaint();
                        }
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
                        
                        // Remove the oldest purchase price (FIFO)
                        if (!purchasePrices.get(stockName).isEmpty()) {
                            purchasePrices.get(stockName).remove(0);
                        }
                        
                        // Update purchase info display
                        updatePurchaseInfo(stockName, purchaseInfoLabel, purchasePrices.get(stockName));
                        
                        // Disable sell button if no stocks left
                        if (owned == 0) {
                            sellButton.setEnabled(false);
                        }
                        
                        String message = "Sold 1 " + stockName + " for $" + String.format("%.2f", currentPrice);
                        System.out.println(message);
                        logger.log(message);
                        
                        // Record sale in profile manager
                        if (profileManager.isLoggedIn()) {
                            profileManager.recordSale(stockName, currentPrice);
                        }
                        
                        // Update the chart without changing the price locally
                        // The price is fully managed by the chart panel now
                        chartPanel.updateStock(stockName);
                        
                        // Update the label with new price from chart directly
                        JLabel priceLabel = priceLabels.get(stockName);
                        if (priceLabel != null) {
                            priceLabel.setText("$" + String.format("%.2f", chartPanel.getCurrentPrice(stockName)));
                            priceLabel.repaint();
                        }
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
        scrollPane.setPreferredSize(new Dimension(330, 240)); // Make this taller since we don't have tabs
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

        // Add stocks and console to the left container (directly, no tabbed pane)
        JPanel leftContainerPanel = new JPanel(new BorderLayout());
        leftContainerPanel.setBackground(new Color(35, 47, 62));
        leftContainerPanel.add(stocksScrollPane, BorderLayout.CENTER);
        leftContainerPanel.add(consolePanel, BorderLayout.SOUTH);
        
        // Create and configure the split pane for the proper layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftContainerPanel, chartPanel);
        splitPane.setDividerLocation(stockPanelWidth + 40); // Account for padding
        splitPane.setDividerSize(6); // Thinner divider
        splitPane.setBorder(null); // Remove border
        splitPane.setBackground(new Color(35, 47, 62));
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(false);
        
        // Style the divider to match the background color
        splitPane.setUI(new BasicSplitPaneUI() {
            @Override
            public BasicSplitPaneDivider createDefaultDivider() {
                BasicSplitPaneDivider divider = new BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        g.setColor(new Color(35, 47, 62)); // Match the background color
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                };
                divider.setBackground(new Color(35, 47, 62));
                divider.setBorder(null);
                return divider;
            }
        });
        
        // Prevent divider from being moved by making it non-resizable
        splitPane.setEnabled(false);
        
        // Add a component listener to maintain the divider position even when the window is resized
        splitPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                splitPane.setDividerLocation(stockPanelWidth + 40);
            }
        });
        
        // Add the split pane to the main content panel
        mainContainer.add(splitPane, BorderLayout.CENTER);
        
        // Add the main container to the content panel
        contentPanel.add(mainContainer, BorderLayout.CENTER);

        // Add panels to simulation screen
        simulationScreen.add(controlPanel, BorderLayout.NORTH);
        simulationScreen.add(contentPanel, BorderLayout.CENTER);

        // Create a timer to update the stock prices
        Timer priceUpdateTimer = new Timer(500, e -> {  // Faster updates (every 500ms instead of 1000ms)
            // Update prices using a more realistic algorithm
            for (String stockName : investables.keySet()) {
                // Get the current price directly from the chart for accuracy
                double price = chartPanel.getCurrentPrice(stockName);
                
                // Update our tracking maps with the actual chart price
                currentPrices.put(stockName, price);
                
                // Directly update the price label using our direct reference
                JLabel priceLabel = priceLabels.get(stockName);
                if (priceLabel != null) {
                    priceLabel.setText("$" + String.format("%.2f", price));
                    priceLabel.repaint();
                }
                
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
            
            // Update profile data if logged in
            if (profileManager.isLoggedIn()) {
                profileManager.saveUserData();
            }
            
            // Force UI update to ensure labels are refreshed
            stocksPanel.revalidate();
            stocksPanel.repaint();
            
            // Force repaint of each visible component in the hierarchy
            for (Component comp : stocksPanel.getComponents()) {
                if (comp.isVisible() && comp instanceof JPanel) {
                    comp.revalidate();
                    comp.repaint();
                }
            }
        });
        priceUpdateTimer.setInitialDelay(200); // Faster initial update
        priceUpdateTimer.start();

        // Add window listener to ensure the timer is restarted if the window is deactivated/reactivated
        // and save notes when application is closing
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                // Restart the chart and price updates when window is activated
                if (!priceUpdateTimer.isRunning()) {
                    priceUpdateTimer.restart();
                    chartPanel.startUpdating();
                }
            }
            
            @Override
            public void windowClosing(WindowEvent e) {
                // Save profile data when application is closing
                profileManager.onExit();
            }
        });

        // Toggle the deployable grid
        enterButton.addActionListener(e -> {
            // Show simulation screen
            cardLayout.show(mainPanel, "Simulation");
            
            // Ensure timers are running when entering simulation screen
            if (!priceUpdateTimer.isRunning()) {
                priceUpdateTimer.restart();
            }
            chartPanel.startUpdating(); // Always restart chart updates
            
            // Force update of all stock labels immediately
            for (String stockName : investables.keySet()) {
                double currentPrice = chartPanel.getCurrentPrice(stockName);
                JLabel priceLabel = priceLabels.get(stockName);
                if (priceLabel != null) {
                    priceLabel.setText("$" + String.format("%.2f", currentPrice));
                    priceLabel.repaint();
                }
            }
            
            // Force UI refresh
            stocksPanel.revalidate();
            stocksPanel.repaint();
            
            chartPanel.updateChart(); // Force an immediate update
        });
        backButton.addActionListener(e -> {
            // Save profile data before going back
            if (profileManager.isLoggedIn()) {
                profileManager.saveUserData();
            }
            
            // Show welcome screen
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

        // Now define the updateStockPanels callback after stocksPanel has been created
        profileManager.updateStockPanels = () -> {
            System.out.println("Updating stock panels with profile data");
            
            // Update UI for each stock based on profile data
            for (Map.Entry<String, Integer> entry : stocksOwned.entrySet()) {
                String stockName = entry.getKey();
                int quantity = entry.getValue();
                
                // Find the corresponding panel components
                for (Component component : stocksPanel.getComponents()) {
                    if (component instanceof JPanel) {
                        JPanel stockPanel = (JPanel) component;
                        boolean isTargetStock = false;
                        
                        // Look through all components to find the stock name
                        for (Component child : stockPanel.getComponents()) {
                            if (child instanceof JPanel) {
                                for (Component innerChild : ((JPanel) child).getComponents()) {
                                    if (innerChild instanceof JPanel) {
                                        for (Component leafComponent : ((JPanel) innerChild).getComponents()) {
                                            if (leafComponent instanceof JLabel) {
                                                JLabel label = (JLabel) leafComponent;
                                                if (label.getText().equals(stockName)) {
                                                    isTargetStock = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        if (isTargetStock) {
                            // Update owned count and enable/disable sell button
                            updatePanelForStock(stockPanel, stockName, quantity);
                            break;
                        }
                    }
                }
            }
            
            // Force UI update
            stocksPanel.revalidate();
            stocksPanel.repaint();
            
            // Update balance display
            BalanceView.setText("$" + String.format("%.2f", balance[0]));
        };
    }

    // Helper method to update purchase info display
    private static void updatePurchaseInfo(String stockName, JLabel infoLabel, java.util.List<Double> prices) {
        if (prices.isEmpty()) {
            infoLabel.setText("");
            return;
        }
        
        // Calculate average purchase price
        double total = 0;
        for (Double price : prices) {
            total += price;
        }
        double avgPrice = total / prices.size();
        
        // Display only the average purchase price (shorter text) with more emphasis
        infoLabel.setText("Avg: $" + String.format("%.2f", avgPrice));
        
        // Make the font bold and slightly larger for better visibility
        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        // Use a brighter color for better contrast
        infoLabel.setForeground(new Color(220, 220, 220));
    }

    private static void updateStockLabel(String stockName, double currentPrice, Map<String, JLabel> labels) {
        JLabel label = labels.get(stockName);
        if (label != null) {
            try {
                // Navigate through the container hierarchy to find the price label
                Container parent = label.getParent();
                while (!(parent instanceof JPanel && ((JPanel)parent).getLayout() instanceof BorderLayout)) {
                    parent = parent.getParent();
                    if (parent == null) return;
                }
                
                JPanel infoPanel = (JPanel)parent;
                
                // Check each component in the panel to find "middlePanel" 
                // which contains the price label
                Component[] components = infoPanel.getComponents();
                for (Component comp : components) {
                    if (comp instanceof JPanel) {
                        Component[] panelComps = ((JPanel)comp).getComponents();
                        
                        // Look through all components inside this panel
                        for (Component panelComp : panelComps) {
                            if (panelComp instanceof JLabel) {
                                JLabel compLabel = (JLabel)panelComp;
                                String text = compLabel.getText();
                                // Match labels that start with $ (price labels)
                                if (text != null && text.startsWith("$")) {
                                    // Found the price label, update it
                                    compLabel.setText("$" + String.format("%.2f", currentPrice));
                                    // Force the component to repaint
                                    compLabel.repaint();
                                    return; // Exit after finding and updating
                                }
                            }
                        }
                    }
                }
                
                // If we didn't find it with the first approach, try a more aggressive approach
                // by searching all nested components
                searchAndUpdatePriceLabel(infoPanel, currentPrice);
                
            } catch (Exception e) {
                System.err.println("Error updating stock label: " + e.getMessage());
            }
        }
    }
    
    // Helper method to recursively search for and update price labels
    private static void searchAndUpdatePriceLabel(Container container, double price) {
        Component[] components = container.getComponents();
        
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel)comp;
                String text = label.getText();
                if (text != null && text.startsWith("$")) {
                    // Found price label
                    label.setText("$" + String.format("%.2f", price));
                    label.repaint();
                    return;
                }
            }
            
            if (comp instanceof Container) {
                // Recursively search nested containers
                searchAndUpdatePriceLabel((Container)comp, price);
            }
        }
    }

    // Helper method to update a stock panel with current owned count
    private static void updatePanelForStock(JPanel stockPanel, String stockName, int quantity) {
        System.out.println("Updating panel for " + stockName + " with quantity: " + quantity);
        
        // Find the owned label and purchase info
        JLabel ownedLabel = null;
        JLabel purchaseInfoLabel = null;
        JButton sellButton = null;
        
        // Recursively search all child components for labels and buttons
        findAndUpdateComponents(stockPanel, stockName, quantity);
    }

    // New recursive method to find and update all relevant components in the panel
    private static void findAndUpdateComponents(Container container, String stockName, int quantity) {
        for (Component comp : container.getComponents()) {
            // Check if this component is a label with "Owned:" text
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                String text = label.getText();
                
                if (text != null) {
                    if (text.startsWith("Owned:")) {
                        System.out.println("Found Owned label for " + stockName + ", updating to: Owned: " + quantity);
                        label.setText("Owned: " + quantity);
                    } else if (text.equals(stockName)) {
                        System.out.println("Found stock name label: " + stockName);
                    } else if (text.startsWith("Avg:") || text.isEmpty()) {
                        // This is likely the purchase info label
                        if (profileManager.isLoggedIn()) {
                            double avgPurchase = profileManager.getAveragePurchasePrice(stockName);
                            if (avgPurchase > 0) {
                                System.out.println("Updating average purchase for " + stockName + ": $" + avgPurchase);
                                label.setText("Avg: $" + String.format("%.2f", avgPurchase));
                            } else {
                                label.setText("");
                            }
                        }
                    }
                }
            } 
            // Check if this is a "Sell" button
            else if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                if ("Sell".equals(button.getText())) {
                    System.out.println("Found Sell button for " + stockName + ", setting enabled: " + (quantity > 0));
                    button.setEnabled(quantity > 0);
                }
            }
            
            // Recursively search child containers
            if (comp instanceof Container) {
                findAndUpdateComponents((Container) comp, stockName, quantity);
            }
        }
    }
}
