import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Stock Simulator");
        frame.setSize(1000, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        CardLayout cardLayout = new CardLayout();
        JPanel mainPanel = new JPanel(cardLayout);

        // First Screen
        JPanel welcomeScreen = new JPanel();
        welcomeScreen.setLayout(null);

        JLabel welcomeText = new JLabel("Welcome to Stock Simulator", SwingConstants.CENTER);
        welcomeText.setFont(new Font("Arial", Font.BOLD, 40));
        welcomeText.setBounds(200, 50, 600, 100);
        welcomeScreen.add(welcomeText);

        JButton enterButton = new JButton("Kom in");
        enterButton.setBounds(200, 400, 600, 100);
        welcomeScreen.add(enterButton);

        // Second Screen
        JPanel simulationScreen = new JPanel();
        simulationScreen.setLayout(null);

        // Deployable Grid Panel
        JPanel DeployableGrid = new JPanel();
        DeployableGrid.setBounds(10, 150, 150, 200);
        DeployableGrid.setVisible(false);
        DeployableGrid.setLayout(new GridLayout(0, 1)); // Dynamic rows, 1 column
        DeployableGrid.setBackground(Color.LIGHT_GRAY);
        simulationScreen.add(DeployableGrid);

        // Create a HashMap to store stock names and their values
        Map<String, Integer> investables = new HashMap<>();
        investables.put("IKEA", 100);
        investables.put("JULA", 150);
        investables.put("MAX", 200);

        // Add each stock to the grid
        for (Map.Entry<String, Integer> entry : investables.entrySet()) {
            JLabel stockLabel = new JLabel(entry.getKey() + " - $" + entry.getValue());
            DeployableGrid.add(stockLabel);
        }

        // Buttons
        JButton backButton = new JButton("Back to Menu");
        backButton.setBounds(10, 10, 150, 50);
        simulationScreen.add(backButton);

        JButton Invest = new JButton("Invest");
        Invest.setBounds(10, 80, 150, 50);
        simulationScreen.add(Invest);
        Invest.addActionListener(e -> DeployableGrid.setVisible(!DeployableGrid.isVisible()));

        // Add panels to mainPanel
        mainPanel.add(welcomeScreen, "Welcome");
        mainPanel.add(simulationScreen, "Simulation");

        enterButton.addActionListener(e -> cardLayout.show(mainPanel, "Simulation"));
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "Welcome"));

        frame.add(mainPanel);
        frame.setVisible(true);
    }
}
