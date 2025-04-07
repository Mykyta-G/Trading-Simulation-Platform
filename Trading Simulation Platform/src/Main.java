import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

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
        simulationScreen.setLayout(null);

        // Grid panel that appears when you click "Invest"
        JPanel DeployableGrid = new JPanel();
        DeployableGrid.setBounds(10, 150, 150, 200);
        DeployableGrid.setVisible(false);
        DeployableGrid.setLayout(new GridLayout(0, 1)); // Dynamic rows, 1 column
        DeployableGrid.setBackground(Color.LIGHT_GRAY);
        simulationScreen.add(DeployableGrid);

        // Balance display
        JLabel BalanceView = new JLabel("Balance: $" + balance[0]);
        BalanceView.setFont(new Font("Arial", Font.BOLD, 20));
        BalanceView.setBounds(750, 10, 200, 50);
        simulationScreen.add(BalanceView);

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

            // When Buy button is clicked
            InvestButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (balance[0] >= stockPrice) {
                        // Deduct price and update balance label
                        balance[0] -= stockPrice;
                        BalanceView.setText("Balance: $" + balance[0]);
                        System.out.println("Bought " + stockName + " for $" + stockPrice);
                    } else {
                        // Show popup if not enough money
                        JOptionPane.showMessageDialog(null, "Not enough balance to buy " + stockName + "!");
                    }
                }
            });
        }

        // Back button
        JButton backButton = new JButton("Back to Menu");
        backButton.setBounds(10, 10, 150, 50);
        simulationScreen.add(backButton);

        // Toggle grid panel when clicking "Invest"
        JButton Invest = new JButton("Invest");
        Invest.setBounds(10, 80, 150, 50);
        simulationScreen.add(Invest);
        Invest.addActionListener(e -> DeployableGrid.setVisible(!DeployableGrid.isVisible()));

        // ------------------- Add Panels to Main -------------------
        mainPanel.add(welcomeScreen, "Welcome");
        mainPanel.add(simulationScreen, "Simulation");

        // Navigation buttons
        enterButton.addActionListener(e -> cardLayout.show(mainPanel, "Simulation"));
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "Welcome"));

        frame.add(mainPanel);
        frame.setVisible(true);
    }
}
