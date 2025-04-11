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
        int[] balance = {1000};

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
        DeployableGrid.setBounds(170, 10, 150, 130);
        DeployableGrid.setVisible(false);
        DeployableGrid.setLayout(new GridLayout(0, 1)); // Dynamic rows, 1 column
        DeployableGrid.setBackground(Color.LIGHT_GRAY);
        controlPanel.add(DeployableGrid);

        // Add control panel to simulation screen
        simulationScreen.add(controlPanel, BorderLayout.NORTH);

        // Create chart panel with a default stock
        StockChartPanel chartPanel = new StockChartPanel("IKEA");
        simulationScreen.add(chartPanel, BorderLayout.CENTER);

        // Map to keep track of chart panels for each stock
        Map<String, StockChartPanel> chartPanels = new HashMap<>();
        chartPanels.put("IKEA", chartPanel);

        // Stocks and their prices
        Map<String, Integer> investables = new HashMap<>();
        investables.put("IKEA", 300);
        investables.put("JULA", 150);
        investables.put("MAX", 200);

        // Add each stock label + Buy button to the deployable grid
        for (Map.Entry<String, Integer> entry : investables.entrySet()) {
            String stockName = entry.getKey();
            int stockPrice = entry.getValue();

            JLabel stockLabel = new JLabel(stockName + " - $" + stockPrice);
            DeployableGrid.add(stockLabel);

            JButton InvestButton = new JButton("Buy");
            DeployableGrid.add(InvestButton);

            // Create chart panel for this stock if not exists
            if (!chartPanels.containsKey(stockName)) {
                chartPanels.put(stockName, new StockChartPanel(stockName));
            }

            // When stock label is clicked, show its chart
            stockLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Remove current chart
                    simulationScreen.remove(simulationScreen.getComponent(1));
                    // Add new chart
                    simulationScreen.add(chartPanels.get(stockName), BorderLayout.CENTER);
                    simulationScreen.revalidate();
                    simulationScreen.repaint();
                }
            });

            // When Buy button is clicked
            InvestButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (balance[0] >= stockPrice) {
                        // Deduct price and update balance label
                        balance[0] -= stockPrice;
                        BalanceView.setText("Balance: $" + balance[0]);
                        System.out.println("Bought " + stockName + " for $" + stockPrice);

                        // Simulate a small price increase after purchase
                        StockChartPanel currentChart = chartPanels.get(stockName);
                        if (currentChart != null) {
                            currentChart.updateChart();
                        }
                    } else {
                        // Show popup if not enough money
                        JOptionPane.showMessageDialog(null, "Not enough balance to buy " + stockName + "!");
                    }
                }
            });
        }

        // Toggle the deployable grid
        Invest.addActionListener(e -> DeployableGrid.setVisible(!DeployableGrid.isVisible()));

        // ------------------- Add Panels to Main -------------------
        mainPanel.add(welcomeScreen, "Welcome");
        mainPanel.add(simulationScreen, "Simulation");

        // Navigation buttons
        enterButton.addActionListener(e -> cardLayout.show(mainPanel, "Simulation"));
        backButton.addActionListener(e -> {
            cardLayout.show(mainPanel, "Welcome");
            // Stop chart updates when going back to welcome screen
            for (StockChartPanel chart : chartPanels.values()) {
                chart.stopUpdating();
            }
        });

        frame.add(mainPanel);
        frame.setVisible(true);
    }
}
