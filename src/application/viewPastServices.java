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
import java.sql.Statement;
import java.util.Optional;
import javafx.scene.text.Text;
import javafx.scene.control.TextInputDialog;

public class viewPastServices {

    public static void showViewPastServicesWindow() {
    	
        Stage stage = new Stage();
        stage.setTitle("View Past Services");
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

        TableView<CompletedService> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<CompletedService, String> licensePlateCol = new TableColumn<>("License Plate");
        TableColumn<CompletedService, String> ownerNameCol = new TableColumn<>("Owner Name");
        TableColumn<CompletedService, String> descriptionCol = new TableColumn<>("Description");
        TableColumn<CompletedService, String> maintenanceStartCol = new TableColumn<>("Start Date");
        TableColumn<CompletedService, String> maintenanceEndCol = new TableColumn<>("End Date");
        TableColumn<CompletedService, Double> paymentAmountCol = new TableColumn<>("Payment Amount");
        TableColumn<CompletedService, Double> paidAmountCol = new TableColumn<>("Paid Amount");
        TableColumn<CompletedService, String> paymentStatusCol = new TableColumn<>("Payment Status");
        TableColumn<CompletedService, Button> changeStatusCol = new TableColumn<>("Change Status");
        TableColumn<CompletedService, Button> addInstallmentCol = new TableColumn<>("Add Installment");


        licensePlateCol.setCellValueFactory(new PropertyValueFactory<>("licensePlate"));
        ownerNameCol.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        maintenanceStartCol.setCellValueFactory(new PropertyValueFactory<>("maintenanceStart"));
        maintenanceEndCol.setCellValueFactory(new PropertyValueFactory<>("maintenanceEnd"));
        paymentAmountCol.setCellValueFactory(new PropertyValueFactory<>("paymentAmount"));
        paidAmountCol.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
        paymentStatusCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        changeStatusCol.setCellValueFactory(new PropertyValueFactory<>("changeStatusButton"));
        addInstallmentCol.setCellValueFactory(new PropertyValueFactory<>("addInstallmentButton"));

        paymentAmountCol.setCellFactory(column -> new TableCell<CompletedService, Double>() {
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

        paidAmountCol.setCellFactory(column -> new TableCell<CompletedService, Double>() {
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

        descriptionCol.setCellFactory(tc -> {
            return new TableCell<CompletedService, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        Text text = new Text(item);
                        text.setFill(javafx.scene.paint.Color.WHITE);
                        text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(10));
                        setGraphic(text);
                    }
                }
            };
        });

        table.getColumns().addAll(
                licensePlateCol,
                ownerNameCol,
                descriptionCol,
                maintenanceStartCol,
                maintenanceEndCol,
                paymentAmountCol,
                paidAmountCol,
                paymentStatusCol,
                changeStatusCol,
                addInstallmentCol
        );

        root.getChildren().add(table);

        try {
            loadCompletedServices(table);
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load past services.");
            alert.showAndWait();
        }

        Scene scene = new Scene(root, 1200, 600);
        Main2.applyStylesheet(scene);
        stage.setScene(scene);
        stage.show();

        root.prefWidthProperty().bind(scene.widthProperty());
        root.prefHeightProperty().bind(scene.heightProperty());
        table.prefWidthProperty().bind(root.prefWidthProperty());
        table.prefHeightProperty().bind(root.prefHeightProperty());
    }

    private static void loadCompletedServices(TableView<CompletedService> table) throws SQLException {
        String query = "SELECT c.license_plate, c.owner_name, cm.description, cm.maintenance_start, " +
                       "cm.maintenance_end, cm.payment_amount, cm.payment_status, cm.maintenance_id, " +
                       "cm.paid_amount " +
                       "FROM completed_maintenance cm " +
                       "JOIN cars c ON cm.car_id = c.id " +
                       "WHERE cm.user_id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement st = conn.prepareStatement(query)) {

            st.setInt(1, Login.getCurrentUserId());
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                String licensePlate = rs.getString("license_plate");
                String ownerName = rs.getString("owner_name");
                String description = formatServiceString(rs.getString("description"));
                String maintenanceStart = formatDateTime(rs.getString("maintenance_start"));
                String maintenanceEnd = formatDateTime(rs.getString("maintenance_end"));
                double paymentAmount = rs.getDouble("payment_amount");
                String paymentStatus = rs.getString("payment_status");
                int maintenanceId = rs.getInt("maintenance_id");
                double paidAmount = rs.getDouble("paid_amount");

                CompletedService rowData = new CompletedService(
                        licensePlate, ownerName, description,
                        maintenanceStart, maintenanceEnd,
                        paymentAmount, paymentStatus, maintenanceId, paidAmount
                );

                Button changeStatusButton = new Button("Change Status");
                changeStatusButton.setOnAction(e -> changePaymentStatus(rowData, table));

                Button addInstallmentButton = new Button("Add Installment");
                addInstallmentButton.setOnAction(e -> addInstallmentPayment(rowData, table));


                if ("Paid".equalsIgnoreCase(paymentStatus) || paidAmount >= paymentAmount) {
                    changeStatusButton.setDisable(true);
                    addInstallmentButton.setDisable(true);
                }

                rowData.setChangeStatusButton(changeStatusButton);
                rowData.setAddInstallmentButton(addInstallmentButton);

                table.getItems().add(rowData);
            }
        }
    }

    private static void changePaymentStatus(CompletedService rowData, TableView<CompletedService> table) {
        if ("Paid".equalsIgnoreCase(rowData.getPaymentStatus())) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "This service is already marked as Paid and cannot be changed.");
            alert.showAndWait();
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Payment Status Change");
        confirmDialog.setHeaderText("Change Payment Status for " + rowData.getLicensePlate());
        confirmDialog.setContentText("Are you sure you want to change the status to Paid? This will mark the full amount as paid.");

        confirmDialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    updatePaymentStatus(rowData.getMaintenanceId(), "Paid");
                    refreshTable(table);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Payment status updated to Paid successfully!");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update payment status.");
                }
            }
        });
    }

    private static void updatePaymentStatus(int maintenanceId, String newStatus) throws SQLException {
        String query = "UPDATE completed_maintenance SET payment_status = ?, paid_amount = payment_amount WHERE maintenance_id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement st = conn.prepareStatement(query)) {

            st.setString(1, newStatus);
            st.setInt(2, maintenanceId);
            st.executeUpdate();
        }
    }

    private static String formatDateTime(String dateTime) {
        if (dateTime == null) return null;
        String[] parts = dateTime.split("\\.");
        String mainPart = parts[0];

        return mainPart.substring(0, mainPart.lastIndexOf(":"));
    }

    private static String formatServiceString(String description) {
        if (description == null) return "";

        String[] services = description.split("[,\n]");
        StringBuilder formatted = new StringBuilder();

        for (String service : services) {
            service = service.trim();
            if (service.isEmpty() || service.startsWith("Discount applied:")) continue;

            if (service.toLowerCase().contains("discount")) {
                formatted.append("\n").append(service).append("\n");
                continue;
            }

            if (service.contains(":")) {
                String[] parts = service.split(":");
                String serviceName = parts[0].trim();
                String price = parts[1].trim();

                if (!price.startsWith("$")) {
                    price = "$" + price;
                }
                formatted.append(serviceName).append(": ").append(price).append("\n");
            } else {
                formatted.append(service).append("\n");
            }
        }

        return formatted.toString().trim();
    }

    private static double calculateTotalFromDescription(String description) {
        if (description == null) return 0.0;

        String[] services = description.split("[,\n]");
        double total = 0.0;

        for (String service : services) {
            service = service.trim();
            if (service.isEmpty()) continue;

            int dollarIndex = service.indexOf("$");
            if (dollarIndex != -1) {
                try {
                    String priceStr = service.substring(dollarIndex + 1).trim();
                    priceStr = priceStr.replaceAll("[^0-9.]", "");
                    total += Double.parseDouble(priceStr);
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }

        return total;
    }

    public static class CompletedService {
        private final String licensePlate;
        private final String ownerName;
        private final String description;
        private final String maintenanceStart;
        private final String maintenanceEnd;
        private double paymentAmount;
        private String paymentStatus;
        private final int maintenanceId;
        private Button changeStatusButton;
        private double paidAmount;
        private Button addInstallmentButton;

        public CompletedService(String licensePlate, String ownerName, String description,
                                String maintenanceStart, String maintenanceEnd,
                                double paymentAmount, String paymentStatus, int maintenanceId, double paidAmount) {
            this.licensePlate = licensePlate;
            this.ownerName = ownerName;
            this.description = description;
            this.maintenanceStart = maintenanceStart;
            this.maintenanceEnd = maintenanceEnd;
            this.paymentAmount = paymentAmount;
            this.paymentStatus = paymentStatus;
            this.maintenanceId = maintenanceId;
            this.paidAmount = paidAmount;
        }

        public String getLicensePlate() {
            return licensePlate;
        }

        public String getOwnerName() {
            return ownerName;
        }

        public String getDescription() {
            return description;
        }

        public String getMaintenanceStart() {
            return maintenanceStart;
        }

        public String getMaintenanceEnd() {
            return maintenanceEnd;
        }

        public double getPaymentAmount() {
            return paymentAmount;
        }

        public void setPaymentAmount(double paymentAmount) {
            this.paymentAmount = paymentAmount;
        }

        public String getPaymentStatus() {
            return paymentStatus;
        }

        public void setPaymentStatus(String paymentStatus) {
            this.paymentStatus = paymentStatus;
        }

        public int getMaintenanceId() {
            return maintenanceId;
        }

        public Button getChangeStatusButton() {
            return changeStatusButton;
        }

        public void setChangeStatusButton(Button changeStatusButton) {
            this.changeStatusButton = changeStatusButton;
        }

        public double getPaidAmount() {
            return paidAmount;
        }

        public void setPaidAmount(double paidAmount) {
            this.paidAmount = paidAmount;
        }

        public Button getAddInstallmentButton() {
            return addInstallmentButton;
        }

        public void setAddInstallmentButton(Button addInstallmentButton) {
            this.addInstallmentButton = addInstallmentButton;
        }
    }

    private static void addInstallmentPayment(CompletedService rowData, TableView<CompletedService> table) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Installment Payment");
        dialog.setHeaderText("Enter the installment amount");
        dialog.setContentText("Amount:");

        dialog.showAndWait().ifPresent(amount -> {
            try {
                double installmentAmount = Double.parseDouble(amount);
                double remainingAmount = rowData.getPaymentAmount() - rowData.getPaidAmount();

                if (installmentAmount <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Amount", "The installment amount must be greater than zero.");
                    return;
                }

                if (installmentAmount > remainingAmount) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Amount", "The installment amount cannot exceed the remaining balance of $" + String.format("%.2f", remainingAmount));
                    return;
                }

                double newPaidAmount = updateInstallmentPayment(rowData.getMaintenanceId(), installmentAmount);
                rowData.setPaidAmount(newPaidAmount);

                if (newPaidAmount >= rowData.getPaymentAmount()) {
                    updatePaymentStatus(rowData.getMaintenanceId(), "Paid");
                    showAlert(Alert.AlertType.INFORMATION, "Payment Completed", "Full payment has been received. Status updated to Paid.");
                }

                refreshTable(table);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Installment payment of $" + String.format("%.2f", installmentAmount) + " has been added successfully.");
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid number for the installment amount.");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add installment payment.");
            }
        });
    }

    private static double updateInstallmentPayment(int maintenanceId, double installmentAmount) throws SQLException {
        String updatePaidAmountQuery = "UPDATE completed_maintenance SET paid_amount = paid_amount + ? WHERE maintenance_id = ?";
        String insertInstallmentQuery = "INSERT INTO Installment_Payments (maintenance_id, amount) VALUES (?, ?)";
        String getPaidAmountQuery = "SELECT paid_amount FROM completed_maintenance WHERE maintenance_id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement updateStmt = conn.prepareStatement(updatePaidAmountQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertInstallmentQuery);
             PreparedStatement getPaidAmountStmt = conn.prepareStatement(getPaidAmountQuery)) {

            conn.setAutoCommit(false);

            updateStmt.setDouble(1, installmentAmount);
            updateStmt.setInt(2, maintenanceId);
            updateStmt.executeUpdate();

            insertStmt.setInt(1, maintenanceId);
            insertStmt.setDouble(2, installmentAmount);
            insertStmt.executeUpdate();

            getPaidAmountStmt.setInt(1, maintenanceId);
            ResultSet rs = getPaidAmountStmt.executeQuery();
            double newPaidAmount = 0;
            if (rs.next()) {
                newPaidAmount = rs.getDouble("paid_amount");
            }

            conn.commit();
            return newPaidAmount;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static void refreshTable(TableView<CompletedService> table) {
        table.getItems().clear();
        try {
            loadCompletedServices(table);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to refresh past services.");
        }
    }
}

