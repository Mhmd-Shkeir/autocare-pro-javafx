package application;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class showWinnings {

    private static TableView<Winnings> table;
    private static boolean isDailyView = true;

    public static void showWinningsWindow() {
        Stage stage = new Stage();
        stage.setTitle("Winnings Report");
        stage.setMaximized(true);

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Button backButton = new Button("Back to Home");
        backButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px;");
        backButton.setOnAction(e -> {
            stage.close();
            Main2.showMainWindow();
        });

       
        ToggleButton viewToggle = new ToggleButton("Switch to Monthly View");
        viewToggle.setStyle("-fx-background-color: #FCD34D; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px;");
        
        Label totalWinningsLabel = new Label();
        totalWinningsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #ffd700; -fx-padding: 10px 0;"); 

        viewToggle.setOnAction(e -> {
            isDailyView = !isDailyView;
            viewToggle.setText(isDailyView ? "Switch to Monthly View" : "Switch to Daily View");
            loadWinnings(totalWinningsLabel);
        });

        header.getChildren().addAll(backButton, viewToggle);

        table = createWinningsTable();

        root.getChildren().addAll(header, table, totalWinningsLabel);

        Scene scene = new Scene(root);
        Main2.applyStylesheet(scene);
        stage.setScene(scene);
        stage.show();

        loadWinnings(totalWinningsLabel);
    }

    private static TableView<Winnings> createWinningsTable() {
        TableView<Winnings> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Winnings, String> dateCol = new TableColumn<>("Date");
        TableColumn<Winnings, Double> handRentCol = new TableColumn<>("Hand Rent");
        TableColumn<Winnings, Double> otherServicesCol = new TableColumn<>("Other Services (5%)");
        TableColumn<Winnings, Double> totalWinningsCol = new TableColumn<>("Total Winnings");

        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        handRentCol.setCellValueFactory(new PropertyValueFactory<>("handRent"));
        otherServicesCol.setCellValueFactory(new PropertyValueFactory<>("otherServicesWinnings"));
        totalWinningsCol.setCellValueFactory(new PropertyValueFactory<>("totalWinnings"));

        handRentCol.setCellFactory(column -> new TableCell<Winnings, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item));
            }
        });

        otherServicesCol.setCellFactory(column -> new TableCell<Winnings, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item));
            }
        });

        totalWinningsCol.setCellFactory(column -> new TableCell<Winnings, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item));
            }
        });

        table.getColumns().addAll(dateCol, handRentCol, otherServicesCol, totalWinningsCol);
        return table;
    }

    private static void loadWinnings(Label totalWinningsLabel) {
        String query = "SELECT CAST(maintenance_end AS DATE) AS service_date, payment_amount, description " +
                       "FROM completed_maintenance " +
                       "WHERE payment_status = 'Paid' AND user_id = ? " +
                       "ORDER BY CAST(maintenance_end AS DATE) DESC";

        ObservableList<Winnings> winningsList = FXCollections.observableArrayList();
        final double[] overallTotal = {0.0};

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, Login.getCurrentUserId());
            ResultSet rs = pstmt.executeQuery();

            Map<String, Winnings> winningsMap = new HashMap<>();

            while (rs.next()) {
                Date date = rs.getDate("service_date");
                String description = rs.getString("description");

                Map<String, Double> servicePrices = parseServicePrices(description);
                double handRent = servicePrices.get("handRent");
                double otherServices = servicePrices.get("otherServices");
                double otherServicesWinnings = otherServices * 0.05; // 5% of other services to understand how it works

                double dailyTotal = handRent + otherServicesWinnings;

                String key = isDailyView 
                    ? date.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) 
                    : YearMonth.from(date.toLocalDate()).format(DateTimeFormatter.ofPattern("yyyy-MM"));

                Winnings winnings = winningsMap.getOrDefault(key, new Winnings(key, 0, 0, 0));
                winnings.addWinnings(handRent, otherServicesWinnings);
                winningsMap.put(key, winnings);

                overallTotal[0] += dailyTotal;
            }

            winningsList.addAll(winningsMap.values());
            winningsList.sort((a, b) -> b.getDate().compareTo(a.getDate()));

            Platform.runLater(() -> {
                table.setItems(winningsList);
                totalWinningsLabel.setText(String.format("Total Overall Winnings: $%.2f", overallTotal[0]));
            });

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load winnings data: " + e.getMessage());
        }
    }

    private static Map<String, Double> parseServicePrices(String description) {
        Map<String, Double> result = new HashMap<>();
        double handRent = 0.0, otherServices = 0.0;

        if (description != null) {
            String[] services = description.split("[,\n]");
            for (String service : services) {
                service = service.trim();
                if (service.isEmpty() || service.toLowerCase().contains("discount")) continue;

                if (service.contains(":")) {
                    String[] parts = service.split(":");
                    String priceStr = parts[1].trim().replaceAll("[^0-9.]", "");
                    try {
                        double price = Double.parseDouble(priceStr);
                        if (parts[0].toLowerCase().contains("hand rent")) handRent += price;
                        else otherServices += price;
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        result.put("handRent", handRent);
        result.put("otherServices", otherServices);
        return result;
    }

    private static void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class Winnings {
        private final String date;
        private double handRent, otherServicesWinnings, totalWinnings;

        public Winnings(String date, double handRent, double otherServicesWinnings, double totalWinnings) {
            this.date = date;
            this.handRent = handRent;
            this.otherServicesWinnings = otherServicesWinnings;
            this.totalWinnings = totalWinnings;
        }

        public void addWinnings(double handRent, double otherServicesWinnings) {
            this.handRent += handRent;
            this.otherServicesWinnings += otherServicesWinnings;
            this.totalWinnings = this.handRent + this.otherServicesWinnings;
        }

        public String getDate() { return date; }
        public double getHandRent() { return handRent; }
        public double getOtherServicesWinnings() { return otherServicesWinnings; }
        public double getTotalWinnings() { return totalWinnings; }
    }
}
