package application;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class viewServices {

    private static TableView<CarInMaintenance> table;

    public static void showViewServicesWindow() {
        Stage stage = new Stage();
        stage.setTitle("View Current Services");
        stage.setMaximized(true);

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        Button backButton = new Button("Back to Home");
        backButton.getStyleClass().add("button-secondary");
        backButton.setOnAction(e -> {
            stage.close();
            Main2.showMainWindow();
        });

        root.getChildren().add(backButton);

        table = createServicesTable();
        root.getChildren().add(table);

        Scene scene = new Scene(root, 1000, 600);
        Main2.applyStylesheet(scene);
        stage.setScene(scene);
        stage.show();

        loadOngoingServices(table);
    }

    private static TableView<CarInMaintenance> createServicesTable() {
        TableView<CarInMaintenance> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<CarInMaintenance, String> licensePlateCol = new TableColumn<>("License Plate");
        TableColumn<CarInMaintenance, String> ownerNameCol = new TableColumn<>("Owner Name");
        TableColumn<CarInMaintenance, String> descriptionCol = new TableColumn<>("Description");
        TableColumn<CarInMaintenance, Double> totalPriceCol = new TableColumn<>("Total Price");
        TableColumn<CarInMaintenance, Void> actionCol = new TableColumn<>("Actions");

        licensePlateCol.setCellValueFactory(new PropertyValueFactory<>("licensePlate"));
        ownerNameCol.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        totalPriceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        totalPriceCol.setCellFactory(column -> new TableCell<CarInMaintenance, Double>() {
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

        actionCol.setCellFactory(column -> new TableCell<CarInMaintenance, Void>() {
            private final Button completeButton = new Button("Complete");
            private final Button discountButton = new Button("Discount");
            private final HBox buttonsBox = new HBox(5, completeButton, discountButton);

            {
                completeButton.getStyleClass().add("button-primary");
                discountButton.getStyleClass().add("button-secondary");

                completeButton.setOnAction(event -> {
                    CarInMaintenance car = getTableView().getItems().get(getIndex());
                    completeService(car, getTableView());
                });

                discountButton.setOnAction(event -> {
                    CarInMaintenance car = getTableView().getItems().get(getIndex());
                    applyDiscount(car, getTableView());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonsBox);
            }
        });

        table.getColumns().addAll(licensePlateCol, ownerNameCol, descriptionCol, totalPriceCol, actionCol);

        return table;
    }

    private static void loadOngoingServices(TableView<CarInMaintenance> table) {
        String query = "SELECT c.license_plate, c.owner_name, m.description, m.maintenance_id, m.total_price " +
                       "FROM cars c JOIN maintenance m ON c.id = m.car_id WHERE m.user_id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement st = conn.prepareStatement(query)) {

            st.setInt(1, Login.getCurrentUserId());
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                String description = formatServiceString(rs.getString("description"));
                double totalPrice = rs.getDouble("total_price");
                if (totalPrice == 0) {
                    totalPrice = calculateTotalPrice(description);
                }

                CarInMaintenance car = new CarInMaintenance(
                    rs.getString("license_plate"),
                    rs.getString("owner_name"),
                    description,
                    rs.getInt("maintenance_id"),
                    totalPrice
                );
                table.getItems().add(car);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load ongoing services: " + e.getMessage());
        }
    }

    private static String formatServiceString(String description) {
        if (description == null) return "";
        return description.replace(", ", "\n");
    }

    private static double calculateTotalPrice(String description) {
        if (description == null) return 0.0;
        String[] services = description.split("[,\n]");
        double totalPrice = 0.0;

        for (String service : services) {
            service = service.trim();
            if (service.isEmpty() || service.toLowerCase().contains("discount")) continue;

            if (service.contains(":")) {
                String[] parts = service.split(":");
                if (parts.length == 2) {
                    try {
                        double price = Double.parseDouble(parts[1].trim().replaceAll("[^0-9.]", ""));
                        totalPrice += price;
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }

        return totalPrice;
    }

    private static void applyDiscount(CarInMaintenance car, TableView<CarInMaintenance> table) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Apply Discount");
        dialog.setHeaderText("Enter discount percentage for " + car.getLicensePlate());
        dialog.setContentText("Discount (%):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                double discountPercentage = Double.parseDouble(result.get());
                if (discountPercentage < 0 || discountPercentage > 100) {
                    showAlert(Alert.AlertType.WARNING, "Invalid Discount", "Please enter a percentage between 0 and 100.");
                    return;
                }

                double discountedPrice = car.getTotalPrice() * (1 - discountPercentage / 100);
                car.setTotalPrice(discountedPrice);

                String currentDescription = car.getDescription();
                String discountInfo = String.format("\nDiscount applied: %.2f%%", discountPercentage);
                String newDescription = currentDescription + discountInfo;
                car.setDescription(newDescription);

                updateMaintenanceWithDiscount(car.getMaintenanceId(), newDescription, discountedPrice);

                table.refresh();
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                    String.format("%.2f%% discount applied to service for %s", discountPercentage, car.getLicensePlate()));
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please enter a valid number for the discount percentage.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to apply discount: " + e.getMessage());
            }
        }
    }

    private static void updateMaintenanceWithDiscount(int maintenanceId, String newDescription, double discountedPrice) throws SQLException {
        String updateQuery = "UPDATE maintenance SET description = ?, total_price = ?, discounted_price = ? WHERE maintenance_id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement st = conn.prepareStatement(updateQuery)) {

            st.setString(1, newDescription);
            st.setDouble(2, discountedPrice);
            st.setDouble(3, discountedPrice);
            st.setInt(4, maintenanceId);
            st.executeUpdate();
        }
    }

    private static void completeService(CarInMaintenance car, TableView<CarInMaintenance> table) {
        try {
            showReceipt(car);

            String paymentType = choosePaymentType();
            if (paymentType == null) return;

            double paidAmount = 0;
            String paymentStatus = "Pending";

            double totalPrice = car.getTotalPrice();
            if (totalPrice == 0) {
                totalPrice = calculateTotalPrice(car.getDescription());
                car.setTotalPrice(totalPrice);
            }

            if ("Full Payment".equals(paymentType)) {
                paidAmount = totalPrice;
                paymentStatus = "Paid";
            } else {
                paidAmount = getInstallmentAmount(totalPrice);
                if (paidAmount == 0) return; 
            }

            moveToCompletedMaintenance(car.getMaintenanceId(), totalPrice, paymentStatus, paidAmount);
            table.getItems().remove(car);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Service has been marked as completed.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to complete service: " + e.getMessage());
        }
    }

    private static void showReceipt(CarInMaintenance car) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("Receipt for ").append(car.getLicensePlate()).append("\n\n");
        receipt.append("Services:\n").append(car.getDescription()).append("\n\n");
        receipt.append("Total Price: $").append(String.format("%.2f", car.getTotalPrice()));

        Alert receiptAlert = new Alert(Alert.AlertType.INFORMATION);
        receiptAlert.setTitle("Service Receipt");
        receiptAlert.setHeaderText(null);
        receiptAlert.setContentText(receipt.toString());
        receiptAlert.showAndWait();
    }

    private static String choosePaymentType() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Full Payment", "Full Payment", "Installment");
        dialog.setTitle("Payment Type");
        dialog.setHeaderText("Choose Payment Type");
        dialog.setContentText("Payment Type:");

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private static double getInstallmentAmount(double totalPrice) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Installment Payment");
        dialog.setHeaderText("Enter the initial installment amount");
        dialog.setContentText("Amount:");

        while (true) {
            Optional<String> result = dialog.showAndWait();
            if (result.isEmpty()) return 0;

            try {
                double amount = Double.parseDouble(result.get());
                if (amount <= 0 || amount > totalPrice) {
                    throw new IllegalArgumentException("Amount must be greater than 0 and less than or equal to the total price.");
                }
                return amount;
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number.");
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Amount", e.getMessage());
            }
        }
    }

    private static void moveToCompletedMaintenance(int maintenanceId, double totalPrice, String paymentStatus, double paidAmount) throws SQLException {
        String insertQuery = "INSERT INTO completed_maintenance " +
                             "(maintenance_id, car_id, user_id, maintenance_start, maintenance_end, description, payment_amount, payment_status, paid_amount, discounted_price, total_price) " +
                             "SELECT ?, car_id, user_id, maintenance_start, GETDATE(), description, ?, ?, ?, ?, ? " +
                             "FROM maintenance WHERE maintenance_id = ?";
        
        String deleteQuery = "DELETE FROM maintenance WHERE maintenance_id = ?";

        try (Connection conn = DatabaseConnection.connect()) {
            conn.setAutoCommit(false);

            try (PreparedStatement insertSt = conn.prepareStatement(insertQuery);
                 PreparedStatement deleteSt = conn.prepareStatement(deleteQuery)) {

                insertSt.setInt(1, maintenanceId);
                insertSt.setDouble(2, totalPrice);
                insertSt.setString(3, paymentStatus);
                insertSt.setDouble(4, paidAmount);
                insertSt.setDouble(5, totalPrice); 
                insertSt.setDouble(6, totalPrice);
                insertSt.setInt(7, maintenanceId);
                insertSt.executeUpdate();

                deleteSt.setInt(1, maintenanceId);
                deleteSt.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private static void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class CarInMaintenance {
        private final String licensePlate;
        private final String ownerName;
        private String description;
        private final int maintenanceId;
        private double totalPrice;

        public CarInMaintenance(String licensePlate, String ownerName, String description, int maintenanceId, double totalPrice) {
            this.licensePlate = licensePlate;
            this.ownerName = ownerName;
            this.description = description;
            this.maintenanceId = maintenanceId;
            this.totalPrice = totalPrice;
        }

        public String getLicensePlate() { return licensePlate; }
        public String getOwnerName() { return ownerName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public int getMaintenanceId() { return maintenanceId; }
        public double getTotalPrice() { return totalPrice; }
        public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    }
}

