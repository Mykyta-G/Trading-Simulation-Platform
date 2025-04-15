# 💹 Trading Simulation Platform

A modern, full-screen Java application that simulates stock trading with a sleek graphical user interface (GUI) using Swing. Track stock prices with realistic market simulation, invest with virtual money, and visualize price movements with dynamic charts.

---

## 🧠 Features

- 🏠 **Modern UI**:
  - Full-screen immersive interface without window decorations
  - Dark mode design with consistent styling
  - Responsive layout that adapts to your screen size
  
- 📊 **Realistic Stock Simulation**:
  - Dynamic stock prices that follow trend patterns
  - Market volatility simulation with randomized price changes
  - Occasional price jumps mimicking real market events
  - Different volatility and trends for each stock

- 💼 **Trading Dashboard**:
  - Live price updates for all available stocks
  - Clear buy/sell buttons for each stock
  - Balance tracking that updates instantly
  - Transaction history console showing all your trades
  - Colored transaction logs (green for buys, red for sells)

- 📈 **Interactive Stock Chart**:
  - Real-time line graph showing multiple stock prices
  - Color-coded stock lines for easy identification
  - Custom-styled checkboxes to toggle which stocks to display
  - Tooltips showing exact values when hovering
  - Dark theme matching the overall application style

- 🔄 **Advanced Stock Algorithm**:
  - Stock-specific trend directions and strength
  - Varying volatility levels for different stocks
  - Market event simulation with price jumps
  - Realistic long-term price behavior

---

## 📦 Technologies Used

- Java (JDK 8+)
- Swing (for GUI)
- [XChart](https://knowm.org/open-source/xchart/) (for real-time stock graphs)
- Git (version control)

---

## 🔧 How to Run

1. **Clone the repository:**

   ```bash
   git clone https://github.com/your-username/Trading-Simulation-Platform.git
   cd Trading-Simulation-Platform
   ```

2. **Install Dependencies:**
   
   Make sure you have the XChart library added to your classpath.

3. **Compile and Run:**

   ```bash
   javac *.java
   java Main
   ```

---

## 💡 How to Use

1. **Welcome Screen:**
   - Click "START TRADING" to begin trading
   - Use the "QUIT" button to exit the application

2. **Trading Dashboard:**
   - View all available stocks on the left panel
   - Monitor real-time price changes in the chart
   - Use the checkboxes below the chart to toggle which stocks to display

3. **Trading:**
   - Click "Buy" to purchase a stock at the current price
   - Click "Sell" to sell a stock you own
   - Watch your balance update in real-time at the top-right corner
   - View your transaction history in the console below the stocks

4. **Chart Interaction:**
   - Toggle stocks on/off using the checkboxes
   - Watch for market events in the transaction log
   - Notice how different stocks follow different trends

---

## 🚀 Future Enhancements

- Save/load functionality to preserve your portfolio
- More advanced trading options (limit orders, stop losses)
- Historical data views and performance tracking
- Additional stocks and market sectors
- Portfolio value charts and analytics

---

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 🤝 Contributing

Contributions are welcome! Feel free to submit a Pull Request.
