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

public class Main {
    public static void main(String[] args) {

        // Use array to make balance mutable inside inner classes
        double[] balance = {1000.0};

        // Main frame
        JFrame frame = new JFrame("Stock Simulator");
        frame.setSize(1000, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // CardLayout to switch between screens
        CardLayout cardLayout = new CardLayout();
        JPanel mainPanel = new JPanel(cardLayout);

        // ------------------- Welcome Screen -------------------
        JPanel welcomeScreen = new JPanel();
        welcomeScreen.setLayout(null);

        JLabel welcomeText = new JLabel("Welcome to Stock Simulator", SwingConstants.CENTER);
        welcomeText.setFont(new Font("Arial", Font.BOLD, 40));
        welcomeText.setBounds(200, 50, 600, 100);
        welcomeScreen.add(welcomeText);

        JButton enterButton = new JButton("Kom in");
        enterButton.setBounds(200, 400, 600, 100);
        welcomeScreen.add(enterButton);

        // ------------------- Simulation Screen -------------------
        JPanel simulationScreen = new JPanel();
        simulationScreen.setLayout(new BorderLayout());

        // Top panel for controls
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(null);
        controlPanel.setPreferredSize(new Dimension(1000, 150));

        // Balance display
        JLabel BalanceView = new JLabel("Balance: $" + balance[0]);
        BalanceView.setFont(new Font("Arial", Font.BOLD, 20));
        BalanceView.setBounds(750, 10, 200, 50);
        controlPanel.add(BalanceView);

        // Back button
        JButton backButton = new JButton("Back to Menu");
        backButton.setBounds(10, 10, 150, 50);
        controlPanel.add(backButton);

        // Toggle grid panel when clicking "Invest"
        JButton Invest = new JButton("Invest");
        Invest.setBounds(10, 80, 150, 50);
        controlPanel.add(Invest);

        // Grid panel that appears when you click "Invest"
        JPanel DeployableGrid = new JPanel();
        DeployableGrid.setBounds(170, 10, 300, 130);
        DeployableGrid.setVisible(false);
        DeployableGrid.setLayout(new GridLayout(0, 1)); // Dynamic rows, 1 column
        DeployableGrid.setBackground(Color.LIGHT_GRAY);
        controlPanel.add(DeployableGrid);

        // Add control panel to simulation screen
        simulationScreen.add(controlPanel, BorderLayout.NORTH);

        // Create a single chart showing all three stocks
        StockChartPanel chartPanel = new StockChartPanel();
        chartPanel.addStock("IKEA", 300);
        chartPanel.addStock("JULA", 150);
        chartPanel.addStock("MAX", 200);
        simulationScreen.add(chartPanel, BorderLayout.CENTER);

        // Stocks and their prices
        Map<String, Integer> investables = new HashMap<>();
        investables.put("IKEA", 300);
        investables.put("JULA", 150);
        investables.put("MAX", 200);

        // Add this after your investables map (around line 77):
        Map<String, Double> currentPrices = new HashMap<>();
        currentPrices.put("IKEA", 300.0);
        currentPrices.put("JULA", 150.0);
        currentPrices.put("MAX", 200.0);

        // Add this after your investables map (around line 77):
        Map<String, Integer> stocksOwned = new HashMap<>();  // Track how many of each stock the user owns
        stocksOwned.put("IKEA", 0);
        stocksOwned.put("JULA", 0);
        stocksOwned.put("MAX", 0);

        // Add each stock label + Buy button to the deployable grid
        Map<String, JLabel> stockLabels = new HashMap<>();
        for (Map.Entry<String, Integer> entry : investables.entrySet()) {
            String stockName = entry.getKey();
            int stockPrice = entry.getValue();

            // Create a stock info panel for each stock
            JPanel stockPanel = new JPanel(new GridLayout(2, 2));
            stockPanel.setBackground(Color.LIGHT_GRAY);

            // Add stock name & price
            JLabel stockLabel = new JLabel(stockName + " - $" + stockPrice);
            stockPanel.add(stockLabel);
            stockLabels.put(stockName, stockLabel);
            
            // Add "Owned: X" label
            JLabel ownedLabel = new JLabel("Owned: 0");
            stockPanel.add(ownedLabel);
            
            // Add Buy button
            JButton buyButton = new JButton("Buy");
            stockPanel.add(buyButton);
            
            // Add Sell button
            JButton sellButton = new JButton("Sell");
            sellButton.setEnabled(false); // Disabled until you own at least one
            stockPanel.add(sellButton);
            
            // Add the panel to the grid
            DeployableGrid.add(stockPanel);
            
            // When Buy button is clicked
            buyButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Get current price
                    double currentPrice = currentPrices.get(stockName);
                    
                    if (balance[0] >= currentPrice) {
                        // Deduct current price and update balance label
                        balance[0] -= currentPrice;
                        balance[0] = Math.round(balance[0] * 100.0) / 100.0; // Round to 2 decimal places
                        BalanceView.setText("Balance: $" + String.format("%.2f", balance[0]));
                        
                        // Increase owned stocks
                        int owned = stocksOwned.get(stockName) + 1;
                        stocksOwned.put(stockName, owned);
                        ownedLabel.setText("Owned: " + owned);
                        
                        // Enable sell button if at least one stock is owned
                        sellButton.setEnabled(true);
                        
                        System.out.println("Bought " + stockName + " for $" + String.format("%.2f", currentPrice));
                        
                        // Simulate a price change when stock is bought
                        double randomChange = (Math.random() - 0.5) * 10; // Random price movement
                        currentPrice += randomChange;
                        currentPrices.put(stockName, currentPrice);
                        
                        // Update the stock in our chart
                        chartPanel.updateStock(stockName);
                        
                        // Update the label with new price
                        updateStockLabel(stockName, currentPrice, stockLabels);
                    } else {
                        // Show popup if not enough money
                        JOptionPane.showMessageDialog(null, "Not enough balance to buy " + stockName + "!");
                    }
                }
            });
            
            // When Sell button is clicked
            sellButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int owned = stocksOwned.get(stockName);
                    
                    if (owned > 0) {
                        // Get current price
                        double currentPrice = currentPrices.get(stockName);
                        
                        // Add price to balance
                        balance[0] += currentPrice;
                        balance[0] = Math.round(balance[0] * 100.0) / 100.0; // Round to 2 decimal places
                        BalanceView.setText("Balance: $" + String.format("%.2f", balance[0]));
                        
                        // Decrease owned stocks
                        owned--;
                        stocksOwned.put(stockName, owned);
                        ownedLabel.setText("Owned: " + owned);
                        
                        // Disable sell button if no stocks left
                        if (owned == 0) {
                            sellButton.setEnabled(false);
                        }
                        
                        System.out.println("Sold " + stockName + " for $" + String.format("%.2f", currentPrice));
                        
                        // Simulate a price change when stock is sold
                        double randomChange = (Math.random() - 0.5) * 10; // Random price movement
                        currentPrice += randomChange;
                        currentPrices.put(stockName, currentPrice);
                        
                        // Update the stock in our chart
                        chartPanel.updateStock(stockName);
                        
                        // Update the label with new price
                        updateStockLabel(stockName, currentPrice, stockLabels);
                    }
                }
            });
        }

        // Toggle the deployable grid
        Invest.addActionListener(e -> DeployableGrid.setVisible(!DeployableGrid.isVisible()));

        // Create a timer to update the stock prices in the deployable grid
        Timer priceUpdateTimer = new Timer(1000, e -> {
            // Only update labels if grid is visible
            if (DeployableGrid.isVisible()) {
                // Update prices randomly every second
                for (String stockName : investables.keySet()) {
                    double price = currentPrices.get(stockName);
                    double change = (Math.random() - 0.5) * 3; // Small random change
                    price += change;
                    currentPrices.put(stockName, price);
                    updateStockLabel(stockName, price, stockLabels);
                }
            }
        });
        priceUpdateTimer.start();

        // ------------------- Add Panels to Main -------------------
        mainPanel.add(welcomeScreen, "Welcome");
        mainPanel.add(simulationScreen, "Simulation");

        // Navigation buttons
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

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private static void updateStockLabel(String stockName, double currentPrice, Map<String, JLabel> labels) {
        JLabel label = labels.get(stockName);
        if (label != null) {
            label.setText(stockName + " - $" + String.format("%.2f", currentPrice));
        }
    }
}
