import java.io.*;
import javax.swing.*;
import java.awt.*;
public class Main {
    public static void main(String[] args) {

        JFrame frame = new JFrame("Stock Simulator");
        frame.setSize(1000, 600);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        JLabel welcomeText = new JLabel("Welcome to Stock Simulator", SwingConstants.CENTER);
        welcomeText.setFont(new Font("Arial", Font.BOLD, 40)); // Fix: Doubled text size
        welcomeText.setBounds((1000 - 600) / 2, 50, 600, 100); // Fix: Increased width to fit text
        frame.add(welcomeText);






    }
}