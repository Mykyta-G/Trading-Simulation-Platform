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

        // Create the chart panel
        chartPanel = new XChartPanel<>(chart);
        add(chartPanel, BorderLayout.CENTER);

        // Create control panel for toggles
        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        add(controlPanel, BorderLayout.SOUTH);

        // Define stock colors
        stockColors.put("IKEA", Color.BLUE);
        stockColors.put("JULA", Color.RED);
        stockColors.put("MAX", Color.GREEN);

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

        // Add series to chart
        chart.addSeries(
                stockName,
                times.stream().mapToDouble(Double::doubleValue).toArray(),
                prices.stream().mapToDouble(Double::doubleValue).toArray()
        ).setMarker(SeriesMarkers.NONE).setLineColor(stockColors.getOrDefault(stockName, Color.BLACK));

        // Add toggle checkbox
        JCheckBox toggleBox = new JCheckBox(stockName, true);
        toggleBox.setForeground(stockColors.getOrDefault(stockName, Color.BLACK));
        toggleBox.addActionListener(e -> {
            stockVisibility.put(stockName, toggleBox.isSelected());
            updateChartVisibility();
        });
        controlPanel.add(toggleBox);

        // Refresh UI
        controlPanel.revalidate();
        chartPanel.repaint();
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
            randomChange += Math.sin(times.size() * 0.1) * 2; // Cyclical trend
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