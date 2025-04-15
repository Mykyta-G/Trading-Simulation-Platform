import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.awt.event.MouseAdapter;
import java.io.File;

public class StockChartPanel extends JPanel {
    private XYChart chart;
    private JPanel chartPanel;
    private JPanel controlPanel;

    // Data for each stock
    private Map<String, CopyOnWriteArrayList<Double>> stockPrices = new HashMap<>();
    private Map<String, CopyOnWriteArrayList<Double>> stockTimes = new HashMap<>();
    private Map<String, Boolean> stockVisibility = new HashMap<>();
    private Map<String, Color> stockColors = new HashMap<>();

    private Timer updateTimer;

    public StockChartPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(35, 47, 62));

        // Initialize chart with empty data
        chart = new XYChartBuilder()
                .width(800)
                .height(600)
                .title("Stock Prices")
                .xAxisTitle("Time")
                .yAxisTitle("Price ($)")
                .build();

        // Customize chart styling
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setYAxisDecimalPattern("$#,###.##");
        chart.getStyler().setPlotMargin(0);
        chart.getStyler().setPlotContentSize(0.95);
        chart.getStyler().setLegendVisible(true);
        
        // Modern styling
        chart.getStyler().setChartBackgroundColor(new Color(35, 47, 62));
        chart.getStyler().setPlotBackgroundColor(new Color(45, 57, 72));
        chart.getStyler().setChartFontColor(Color.WHITE);
        chart.getStyler().setAxisTickLabelsColor(Color.WHITE);
        chart.getStyler().setXAxisTitleColor(Color.WHITE);
        chart.getStyler().setYAxisTitleColor(Color.WHITE);
        chart.getStyler().setChartTitleBoxBackgroundColor(new Color(35, 47, 62));
        chart.getStyler().setChartTitleBoxVisible(true);
        chart.getStyler().setChartTitleBoxBorderColor(new Color(60, 70, 80));
        chart.getStyler().setLegendBackgroundColor(new Color(35, 47, 62));
        chart.getStyler().setLegendBorderColor(new Color(60, 70, 80));
        chart.getStyler().setLegendFont(new Font("Segoe UI", Font.BOLD, 12));
        chart.getStyler().setAxisTickLabelsFont(new Font("Segoe UI", Font.PLAIN, 12));
        chart.getStyler().setChartTitleFont(new Font("Segoe UI", Font.BOLD, 16));
        chart.getStyler().setAxisTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        chart.getStyler().setPlotGridLinesColor(new Color(60, 70, 80));
        chart.getStyler().setPlotBorderColor(new Color(60, 70, 80));
        chart.getStyler().setPlotGridLinesStroke(new BasicStroke(0.5f));
        chart.getStyler().setXAxisTicksVisible(true);
        chart.getStyler().setYAxisTicksVisible(true);
        chart.getStyler().setLegendSeriesLineLength(25);
        
        // Tooltip styling
        chart.getStyler().setToolTipsEnabled(true);
        chart.getStyler().setToolTipBackgroundColor(new Color(35, 47, 62));
        chart.getStyler().setToolTipBorderColor(new Color(60, 70, 80));
        chart.getStyler().setToolTipFont(new Font("Segoe UI", Font.PLAIN, 12));
        chart.getStyler().setToolTipType(Styler.ToolTipType.xAndYLabels);

        // Create the chart panel
        chartPanel = new XChartPanel<>(chart);
        add(chartPanel, BorderLayout.CENTER);

        // Create control panel for toggles with a dark theme
        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        controlPanel.setBackground(new Color(25, 35, 45));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(controlPanel, BorderLayout.SOUTH);

        // Define stock colors with more vibrant colors
        stockColors.put("IKEA", new Color(0, 92, 185));  // IKEA blue
        stockColors.put("JULA", new Color(220, 53, 69)); // Red
        stockColors.put("MAX", new Color(40, 167, 69));  // Green

        // Start updating the chart
        startUpdating();
    }

    public StockChartPanel(String stockName) {
        this(); // Call the default constructor

        // Initialize with a starting price based on stock name
        int initialPrice = 100;
        if (stockName.equals("IKEA")) {
            initialPrice = 300;
        } else if (stockName.equals("JULA")) {
            initialPrice = 150;
        } else if (stockName.equals("MAX")) {
            initialPrice = 200;
        }

        // Add the stock to the chart
        addStock(stockName, initialPrice);
    }

    public void addStock(String stockName, int initialPrice) {
        // Initialize data series for this stock
        CopyOnWriteArrayList<Double> prices = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<Double> times = new CopyOnWriteArrayList<>();

        prices.add((double) initialPrice);
        times.add(0.0);

        stockPrices.put(stockName, prices);
        stockTimes.put(stockName, times);
        stockVisibility.put(stockName, true);

        // Add series to chart with thicker lines
        Color stockColor = stockColors.getOrDefault(stockName, Color.BLACK);
        XYSeries series = chart.addSeries(
                stockName,
                times.stream().mapToDouble(Double::doubleValue).toArray(),
                prices.stream().mapToDouble(Double::doubleValue).toArray()
        );
        series.setMarker(SeriesMarkers.NONE);
        series.setLineColor(stockColor);
        series.setLineStyle(new BasicStroke(2.5f));

        // Create a custom styled checkbox
        JCheckBox toggleBox = new JCheckBox(stockName, true);
        toggleBox.setFont(new Font("Segoe UI", Font.BOLD, 12));
        toggleBox.setForeground(stockColors.getOrDefault(stockName, Color.WHITE));
        toggleBox.setBackground(new Color(25, 35, 45));
        toggleBox.setFocusPainted(false);
        toggleBox.setIcon(createCheckBoxIcon(false, stockColor));
        toggleBox.setSelectedIcon(createCheckBoxIcon(true, stockColor));
        
        // Add border and padding
        toggleBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 70, 80), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        toggleBox.addActionListener(e -> {
            stockVisibility.put(stockName, toggleBox.isSelected());
            updateChartVisibility();
        });
        
        controlPanel.add(toggleBox);

        // Refresh UI
        controlPanel.revalidate();
        chartPanel.repaint();
    }
    
    // Create a custom icon for checkboxes
    private Icon createCheckBoxIcon(boolean selected, Color color) {
        return new Icon() {
            private final int SIZE = 16;
            
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw the checkbox background
                if (selected) {
                    g2d.setColor(color);
                    g2d.fillRoundRect(x, y, SIZE, SIZE, 3, 3);
                    
                    // Draw checkmark
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawLine(x + 3, y + 8, x + 6, y + 11);
                    g2d.drawLine(x + 6, y + 11, x + 13, y + 4);
                } else {
                    g2d.setColor(new Color(60, 70, 80));
                    g2d.fillRoundRect(x, y, SIZE, SIZE, 3, 3);
                    g2d.setColor(new Color(80, 90, 100));
                    g2d.drawRoundRect(x, y, SIZE, SIZE, 3, 3);
                }
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() {
                return SIZE;
            }
            
            @Override
            public int getIconHeight() {
                return SIZE;
            }
        };
    }

    private void updateChartVisibility() {
        for (String stockName : stockPrices.keySet()) {
            XYSeries series = chart.getSeriesMap().get(stockName);
            if (series != null) {
                boolean isVisible = stockVisibility.getOrDefault(stockName, true);
                series.setShowInLegend(isVisible);
                series.setEnabled(isVisible);
            }
        }
        chartPanel.repaint();
    }

    private void startUpdating() {
        updateTimer = new Timer(1000, e -> {
            // Update all stock prices
            updateAllStocks();

            // Repaint the chart
            chartPanel.repaint();
        });
        updateTimer.start();
    }

    private void updateAllStocks() {
        // Update each stock's price
        for (String stockName : stockPrices.keySet()) {
            updateStockPrice(stockName);
        }
    }

    private void updateStockPrice(String stockName) {
        CopyOnWriteArrayList<Double> prices = stockPrices.get(stockName);
        CopyOnWriteArrayList<Double> times = stockTimes.get(stockName);

        if (prices == null || times == null || prices.isEmpty()) {
            return;
        }

        // Generate new price with random movement
        double lastPrice = prices.get(prices.size() - 1);
        double randomChange = (Math.random() - 0.5) * 5; // Random price movement

        // Add a trend factor based on stock name for more realistic movement
        if (stockName.equals("IKEA")) {
            randomChange += 0.1; // Slight upward trend
        } else if (stockName.equals("JULA")) {
            randomChange -= 0.05; // Slight downward trend
        } else if (stockName.equals("MAX")) {
            // Reduced growth to be more similar to other stocks
            randomChange += 0.08 + Math.sin(times.size() * 0.07) * 0.5; // Mild trend with small oscillation
        }

        double newPrice = Math.max(1, lastPrice + randomChange); // Ensure price stays positive

        // Add the new data points
        prices.add(newPrice);
        times.add(times.get(times.size() - 1) + 1);

        // Remove old data points if list gets too long
        if (prices.size() > 50) {
            prices.remove(0);
            times.remove(0);
        }

        // Update the chart data
        chart.updateXYSeries(
                stockName,
                times.stream().mapToDouble(Double::doubleValue).toArray(),
                prices.stream().mapToDouble(Double::doubleValue).toArray(),
                null
        );
        
        // Ensure y-axis scale is updated to reflect actual stock values
        double minPrice = Double.MAX_VALUE;
        double maxPrice = Double.MIN_VALUE;
        
        for (CopyOnWriteArrayList<Double> priceList : stockPrices.values()) {
            if (!priceList.isEmpty()) {
                for (Double price : priceList) {
                    minPrice = Math.min(minPrice, price);
                    maxPrice = Math.max(maxPrice, price);
                }
            }
        }
        
        // Add padding to min/max for better visualization
        double padding = (maxPrice - minPrice) * 0.1;
        chart.getStyler().setYAxisMin(Math.max(0, minPrice - padding));
        chart.getStyler().setYAxisMax(maxPrice + padding);
    }

    public void updateStock(String stockName) {
        if (stockPrices.containsKey(stockName)) {
            updateStockPrice(stockName);
            chartPanel.repaint();
        }
    }

    public void stopUpdating() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
    }

    // Add updateChart method for backward compatibility
    public void updateChart() {
        // Update all stocks
        updateAllStocks();
        chartPanel.repaint();
    }
}