import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileNotes {
    private JTextArea notesTextArea;
    private JPanel notesPanel;
    private JLabel saveStatusLabel;
    private Timer autoSaveTimer;
    private String currentProfile = "default";
    private Map<String, Double> portfolioSummary;
    private Map<String, Integer> stocksOwned;
    private double balance;
    private boolean changesMade = false;
    private File saveDirectory;
    private JTextField profileNameField;
    
    public ProfileNotes(Map<String, Integer> stocksOwned, double[] balance) {
        this.stocksOwned = stocksOwned;
        this.balance = balance[0];
        
        // Create save directory
        saveDirectory = new File(System.getProperty("user.home"), "StockSimulator");
        if (!saveDirectory.exists()) {
            saveDirectory.mkdirs();
        }
        
        // Initialize portfolio summary
        portfolioSummary = new HashMap<>();
        
        // Create notes panel
        notesPanel = new JPanel(new BorderLayout(0, 10));
        notesPanel.setBackground(new Color(35, 47, 62));
        notesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(35, 47, 62));
        
        JLabel titleLabel = new JLabel("Trading Notes");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Create profile section
        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        profilePanel.setBackground(new Color(35, 47, 62));
        
        JLabel profileLabel = new JLabel("Profile:");
        profileLabel.setForeground(Color.WHITE);
        profileLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        profilePanel.add(profileLabel);
        
        // Create profile name field
        profileNameField = new JTextField(currentProfile, 10);
        profileNameField.setBackground(new Color(45, 55, 65));
        profileNameField.setForeground(Color.WHITE);
        profileNameField.setCaretColor(Color.WHITE);
        profileNameField.setFont(new Font("Segoe UI", Font.BOLD, 12));
        profileNameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 70, 80), 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        
        profilePanel.add(profileNameField);
        
        // Add save profile button
        JButton saveProfileButton = createButton("Save Profile", "Save current profile");
        saveProfileButton.addActionListener(e -> {
            String newProfile = profileNameField.getText().trim();
            if (!newProfile.isEmpty()) {
                if (changesMade) {
                    saveNotes(); // Save current notes first
                }
                currentProfile = newProfile;
                loadNotes(); // Load notes for this profile if exists
            }
        });
        profilePanel.add(saveProfileButton);
        
        headerPanel.add(profilePanel, BorderLayout.EAST);
        
        // Create notes text area
        notesTextArea = new JTextArea();
        notesTextArea.setBackground(new Color(25, 35, 45));
        notesTextArea.setForeground(Color.WHITE);
        notesTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notesTextArea.setLineWrap(true);
        notesTextArea.setWrapStyleWord(true);
        notesTextArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 70, 80), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Add document listener for changes
        notesTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                documentChanged();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                documentChanged();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                documentChanged();
            }
            
            private void documentChanged() {
                changesMade = true;
                saveStatusLabel.setText("Unsaved changes");
                saveStatusLabel.setForeground(new Color(255, 193, 7)); // Yellow warning
                
                // Reset auto-save timer
                if (autoSaveTimer.isRunning()) {
                    autoSaveTimer.restart();
                } else {
                    autoSaveTimer.start();
                }
            }
        });
        
        // Create scrollable container for text area
        JScrollPane scrollPane = new JScrollPane(notesTextArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Customize scrollbar
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(70, 80, 90);
                this.trackColor = new Color(45, 57, 72);
            }
        });
        
        // Create footer panel with buttons
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(35, 47, 62));
        
        // Add status label
        saveStatusLabel = new JLabel("No unsaved changes");
        saveStatusLabel.setForeground(new Color(40, 167, 69)); // Green
        saveStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerPanel.add(saveStatusLabel, BorderLayout.WEST);
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setBackground(new Color(35, 47, 62));
        
        // Add buttons for actions
        JButton summaryButton = createButton("Add Summary", "Insert portfolio summary");
        summaryButton.addActionListener(e -> insertPortfolioSummary());
        
        JButton saveButton = createButton("Save Notes", "Save notes to file");
        saveButton.addActionListener(e -> saveNotes());
        
        buttonsPanel.add(summaryButton);
        buttonsPanel.add(saveButton);
        footerPanel.add(buttonsPanel, BorderLayout.EAST);
        
        // Add components to main panel
        notesPanel.add(headerPanel, BorderLayout.NORTH);
        notesPanel.add(scrollPane, BorderLayout.CENTER);
        notesPanel.add(footerPanel, BorderLayout.SOUTH);
        
        // Create auto-save timer (save after 5 seconds of inactivity)
        autoSaveTimer = new Timer(5000, e -> {
            saveNotes();
            autoSaveTimer.stop();
        });
        autoSaveTimer.setRepeats(false);
        
        // Load notes for current profile
        loadNotes();
    }
    
    private JButton createButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(new Color(45, 55, 65));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setToolTipText(tooltip);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 70, 80), 1),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        return button;
    }
    
    private File getNotesFile() {
        return new File(saveDirectory, currentProfile + "-notes.txt");
    }
    
    public void updatePortfolioInfo(Map<String, Integer> stocksOwned, double balance, Map<String, Double> currentPrices) {
        this.stocksOwned = stocksOwned;
        this.balance = balance;
        
        // Update portfolio summary
        portfolioSummary.clear();
        for (Map.Entry<String, Integer> entry : stocksOwned.entrySet()) {
            String stockName = entry.getKey();
            int quantity = entry.getValue();
            
            if (quantity > 0 && currentPrices.containsKey(stockName)) {
                double price = currentPrices.get(stockName);
                portfolioSummary.put(stockName, price * quantity);
            }
        }
    }
    
    private void insertPortfolioSummary() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder summary = new StringBuilder();
        
        summary.append("\n--- Portfolio Summary (").append(sdf.format(new Date())).append(") ---\n");
        summary.append("Cash Balance: $").append(String.format("%.2f", balance)).append("\n");
        
        double totalPortfolioValue = balance;
        
        if (!portfolioSummary.isEmpty()) {
            summary.append("\nStocks owned:\n");
            
            for (Map.Entry<String, Double> entry : portfolioSummary.entrySet()) {
                String stockName = entry.getKey();
                double value = entry.getValue();
                int quantity = stocksOwned.get(stockName);
                
                summary.append("- ").append(stockName).append(": ")
                       .append(quantity).append(" shares, worth $")
                       .append(String.format("%.2f", value)).append("\n");
                
                totalPortfolioValue += value;
            }
        } else {
            summary.append("\nNo stocks owned.\n");
        }
        
        summary.append("\nTotal Portfolio Value: $").append(String.format("%.2f", totalPortfolioValue)).append("\n");
        summary.append("-------------------------------\n");
        
        notesTextArea.append(summary.toString());
    }
    
    public void saveNotes() {
        try {
            File notesFile = getNotesFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(notesFile))) {
                writer.write(notesTextArea.getText());
            }
            
            changesMade = false;
            saveStatusLabel.setText("Saved successfully");
            saveStatusLabel.setForeground(new Color(40, 167, 69)); // Green
        } catch (IOException e) {
            saveStatusLabel.setText("Error saving notes");
            saveStatusLabel.setForeground(new Color(220, 53, 69)); // Red
            e.printStackTrace();
        }
    }
    
    private void loadNotes() {
        File notesFile = getNotesFile();
        
        if (notesFile.exists()) {
            try {
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(notesFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }
                
                notesTextArea.setText(content.toString());
            } catch (IOException e) {
                notesTextArea.setText("Error loading notes: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Initialize with welcome message for new profile
            notesTextArea.setText("Welcome to your trading notes for profile: " + currentProfile + 
                                 "\n\nUse this space to track your trading strategy, record insights about stock movements, " +
                                 "and keep notes on your portfolio performance.\n\n" +
                                 "Click 'Add Summary' to insert your current portfolio details.");
            saveNotes(); // Save the initial content
        }
        
        changesMade = false;
        saveStatusLabel.setText("No unsaved changes");
        saveStatusLabel.setForeground(new Color(40, 167, 69)); // Green
    }
    
    public JPanel getNotesPanel() {
        return notesPanel;
    }
    
    public void onExit() {
        if (changesMade) {
            saveNotes();
        }
    }
} 