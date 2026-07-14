package application;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class viewPayments {

    public static void showViewPaymentsWindow() {
    	
        Stage stage = new Stage();
        stage.setTitle("View Payments");
        stage.setMaximized(true);

        VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setSpacing(10);

        Button backButton = new Button("Back to Home");
        backButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-cursor: hand;");
        backButton.setOnAction(e -> {
            stage.close();
            Main2.showMainWindow();
        });

        root.getChildren().add(backButton);

        TableView<Payment> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Payment, String> ownerNameCol = new TableColumn<>("Owner Name");
        TableColumn<Payment, Double> paymentAmountCol = new TableColumn<>("Payment Amount");
        TableColumn<Payment, String> paymentStatusCol = new TableColumn<>("Payment Status");
        TableColumn<Payment, Double> handRentCol = new TableColumn<>("Hand Rent");
        TableColumn<Payment, Double> otherServicesWinningsCol = new TableColumn<>("Other Services Winnings");
        TableColumn<Payment, Double> totalWinningsCol = new TableColumn<>("Total Winnings");

        ownerNameCol.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        paymentAmountCol.setCellValueFactory(new PropertyValueFactory<>("paymentAmount"));
        paymentStatusCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        handRentCol.setCellValueFactory(new PropertyValueFactory<>("handRent"));
        otherServicesWinningsCol.setCellValueFactory(new PropertyValueFactory<>("otherServicesWinnings"));
        totalWinningsCol.setCellValueFactory(new PropertyValueFactory<>("totalWinnings"));

        paymentAmountCol.setCellFactory(column -> new TableCell<Payment, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });

        handRentCol.setCellFactory(column -> new TableCell<Payment, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });

        otherServicesWinningsCol.setCellFactory(column -> new TableCell<Payment, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });

        totalWinningsCol.setCellFactory(column -> new TableCell<Payment, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });

        table.getColumns().addAll(ownerNameCol, paymentAmountCol, paymentStatusCol, handRentCol, otherServicesWinningsCol, totalWinningsCol);

        Label totalPaidLabel = new Label("Total Paid Amount: $0.00");
        Label totalWinningsLabel = new Label("Total Winnings: $0.00");

        root.getChildren().addAll(table, totalPaidLabel, totalWinningsLabel);

        try {
            populatePayments(table, totalPaidLabel, totalWinningsLabel);
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load payments.");
            alert.showAndWait();
        }

        Scene scene = new Scene(root, 900, 600);
        Main2.applyStylesheet(scene);
        stage.setScene(scene);
        stage.show();

        root.prefWidthProperty().bind(scene.widthProperty());
        root.prefHeightProperty().bind(scene.heightProperty());
        table.prefWidthProperty().bind(root.prefWidthProperty());
        table.prefHeightProperty().bind(root.prefHeightProperty());
    }

    private static Map<String, Double> parseServicePrices(String description) {
        Map<String, Double> result = new HashMap<>();
        double handRent = 0.0;
        double otherServices = 0.0;

        if (description != null) {
            String[] services = description.split("[,\n]");
            for (String service : services) {
                service = service.trim();
                if (service.isEmpty() || service.toLowerCase().contains("discount")) continue;

                if (service.contains(":")) {
                    String[] parts = service.split(":");
                    String serviceName = parts[0].trim();
                    String priceStr = parts[1].trim().replaceAll("[^0-9.]", "");

                    try {
                        double price = Double.parseDouble(priceStr);
                        if (serviceName.toLowerCase().contains("hand rent")) {
                            handRent += price;
                        } else {
                            otherServices += price;
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }

        result.put("handRent", handRent);
        result.put("otherServices", otherServices);
        return result;
    }

    private static void populatePayments(TableView<Payment> table, Label totalPaidLabel, Label totalWinningsLabel) throws SQLException {
        String query = "SELECT c.owner_name, cm.payment_amount, cm.payment_status, cm.description " +
                      "FROM completed_maintenance cm " +
                      "JOIN cars c ON cm.car_id = c.id " +
                      "WHERE cm.payment_status = 'Paid' AND cm.user_id = ?";

        double totalPaid = 0.0;
        double totalWinnings = 0.0;

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement st = conn.prepareStatement(query)) {

            st.setInt(1, Login.getCurrentUserId());
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                String ownerName = rs.getString("owner_name");
                double paymentAmount = rs.getDouble("payment_amount");
                String paymentStatus = rs.getString("payment_status");
                String description = rs.getString("description");

                Map<String, Double> servicePrices = parseServicePrices(description);
                double handRent = servicePrices.get("handRent");
                double otherServices = servicePrices.get("otherServices");
                double otherServicesWinnings = otherServices * 0.05; // 5% of other services same here
                double totalWinning = handRent + otherServicesWinnings;

                Payment payment = new Payment(ownerName, paymentAmount, paymentStatus, 
                                           handRent, otherServicesWinnings, totalWinning);
                table.getItems().add(payment);

                totalPaid += paymentAmount;
                totalWinnings += totalWinning;
            }

            totalPaidLabel.setText(String.format("Total Paid Amount: $%.2f", totalPaid));
            totalWinningsLabel.setText(String.format("Total Winnings: $%.2f", totalWinnings));
        }
    }

    public static class Payment {
        private final String ownerName;
        private final double paymentAmount;
        private final String paymentStatus;
        private final double handRent;
        private final double otherServicesWinnings;
        private final double totalWinnings;

        public Payment(String ownerName, double paymentAmount, String paymentStatus, 
                      double handRent, double otherServicesWinnings, double totalWinnings) {
            this.ownerName = ownerName;
            this.paymentAmount = paymentAmount;
            this.paymentStatus = paymentStatus;
            this.handRent = handRent;
            this.otherServicesWinnings = otherServicesWinnings;
            this.totalWinnings = totalWinnings;
        }

        public String getOwnerName() { return ownerName; }
        public double getPaymentAmount() { return paymentAmount; }
        public String getPaymentStatus() { return paymentStatus; }
        public double getHandRent() { return handRent; }
        public double getOtherServicesWinnings() { return otherServicesWinnings; }
        public double getTotalWinnings() { return totalWinnings; }
    }
}

