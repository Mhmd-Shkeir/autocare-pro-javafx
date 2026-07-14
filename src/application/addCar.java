package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class addCar {

    public static void showAddCarWindow() {
        Stage stage = new Stage();
        stage.setTitle("Add Car");
        stage.setMaximized(true);
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        TextField licensePlateField = new TextField();
        licensePlateField.setPromptText("Enter license plate number");
        TextField ownerNameField = new TextField();
        ownerNameField.setPromptText("Enter owner's name");
        TextField ownerContactField = new TextField();
        ownerContactField.setPromptText("Enter owner's contact (e.g., +9611234567)");
        TextField carModelField = new TextField();
        carModelField.setPromptText("Enter car model");
        TextField yearField = new TextField();
        yearField.setPromptText("Enter car manufacturing year");
        TextField maintenanceDescriptionField = new TextField();
        maintenanceDescriptionField.setPromptText("Enter maintenance description");

        grid.add(new Label("License Plate:"), 0, 0);
        grid.add(licensePlateField, 1, 0);
        grid.add(new Label("Owner Name:"), 0, 1);
        grid.add(ownerNameField, 1, 1);
        grid.add(new Label("Owner Contact:"), 0, 2);
        grid.add(ownerContactField, 1, 2);
        grid.add(new Label("Car Model:"), 0, 3);
        grid.add(carModelField, 1, 3);
        grid.add(new Label("Year:"), 0, 4);
        grid.add(yearField, 1, 4);
        grid.add(new Label("Maintenance Description:"), 0, 5);
        grid.add(maintenanceDescriptionField, 1, 5);

        Button addButton = new Button("Add Car");
        addButton.setStyle("-fx-background-color: #FFD700; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-cursor: hand;");

        Button backButton = new Button("Back to Home");
        backButton.setStyle("-fx-background-color: #2C3E50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-cursor: hand;");

        backButton.setOnAction(e -> {
            stage.close();
            Main2.showMainWindow();
        });

        HBox buttonBox = new HBox(15, backButton, addButton);
        buttonBox.setAlignment(Pos.CENTER);
        grid.add(buttonBox, 1, 6);

        addButton.setOnAction(e -> {
            String licensePlate = licensePlateField.getText().trim();
            String ownerName = ownerNameField.getText().trim();
            String ownerContact = ownerContactField.getText().trim();
            String carModel = carModelField.getText().trim();
            String yearText = yearField.getText().trim();
            String maintenanceDescription = maintenanceDescriptionField.getText().trim();

            if (licensePlate.isEmpty() || ownerName.isEmpty() || ownerContact.isEmpty() ||
                    carModel.isEmpty() || yearText.isEmpty() || maintenanceDescription.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Input Error", "All fields must be filled out.");
                return;
            }

            int year;
            try {
                year = Integer.parseInt(yearText);
                if (year < 1886 || year > 2100) {
                    showAlert(Alert.AlertType.WARNING, "Input Error", "Please enter a valid year.");
                    return;
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.WARNING, "Input Error", "Year must be a valid number.");
                return;
            }

            if (!ownerContact.matches("^(\\+961(1|3|7|8|76|70|71|81)\\d{6}|0(1|3|7|8|76|70|71|81)\\d{6}|(1|3|7|8|76|70|71|81)\\d{6})$")) {
                showAlert(Alert.AlertType.WARNING, "Input Error", "Owner contact must be a valid Lebanese phone number (e.g., +9611234567, 01234567, 81123456).");
                return;
            }

            try {
                if (licensePlateExists(licensePlate)) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("License Plate Found");
                    alert.setHeaderText(null);
                    alert.setContentText("This license plate already exists in the database.");
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            stage.close();
                            CarListView.showCarListView();
                        }
                    });
                    return;
                }

                addCarAndMaintenanceToDatabase(licensePlate, ownerName, ownerContact, carModel, year, maintenanceDescription);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Car added successfully!");

                stage.close();
                Main2.showMainWindow();  

            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add car. Please try again.");
            }
        });

        Scene scene = new Scene(grid, 500, 400);
        Main2.applyStylesheet(scene);
        stage.setScene(scene);
        stage.show();
    }

    private static boolean licensePlateExists(String licensePlate) throws SQLException {
        String query = "SELECT COUNT(*) FROM Cars WHERE license_plate = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, licensePlate);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    private static void addCarAndMaintenanceToDatabase(String licensePlate, String ownerName,
                                                     String ownerContact, String carModel, int year, String description) throws Exception {
        Connection conn = null;
        PreparedStatement carSt = null;
        PreparedStatement maintenanceSt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.connect();
            conn.setAutoCommit(false);

            String carQuery = "INSERT INTO cars (license_plate, owner_name, owner_contact, car_model, year, user_id) VALUES (?, ?, ?, ?, ?, ?)";
            carSt = conn.prepareStatement(carQuery, Statement.RETURN_GENERATED_KEYS);
            
            int userId = Login.getCurrentUserId();
            carSt.setString(1, licensePlate);
            carSt.setString(2, ownerName);
            carSt.setString(3, ownerContact);
            carSt.setString(4, carModel);
            carSt.setInt(5, year);
            carSt.setInt(6, userId);

            carSt.executeUpdate();
            
            rs = carSt.getGeneratedKeys();
            if (!rs.next()) {
                throw new Exception("Failed to get car ID");
            }
            int carId = rs.getInt(1);

            String maintenanceQuery = "INSERT INTO maintenance (car_id, description, maintenance_start, user_id) VALUES (?, ?, GETDATE(), ?)";
            maintenanceSt = conn.prepareStatement(maintenanceQuery);
            
            maintenanceSt.setInt(1, carId);
            maintenanceSt.setString(2, description);
            maintenanceSt.setInt(3, userId);

            maintenanceSt.executeUpdate();

            conn.commit();
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (Exception e) { }
            if (carSt != null) try { carSt.close(); } catch (Exception e) { }
            if (maintenanceSt != null) try { maintenanceSt.close(); } catch (Exception e) { }
            if (conn != null) try { conn.close(); } catch (Exception e) { }
        }
    }

    private static void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

