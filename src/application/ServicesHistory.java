package application;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.List;
import java.util.Arrays;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.Desktop;

public class ServicesHistory {
    private static TableView<ServiceRecord> table;
    private static ObservableList<ServiceRecord> allServices = FXCollections.observableArrayList();

    public static void showServicesHistoryWindow() {
        Stage stage = new Stage();
        stage.setTitle("Services History");
        stage.setMaximized(true);

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("main-background");

        HBox topControls = new HBox(10);
        topControls.setAlignment(Pos.CENTER_LEFT);

        Button addNewButton = new Button("+ Add New");
        addNewButton.getStyleClass().add("add-button");
        addNewButton.setOnAction(e -> {
            stage.close();
            addCar.showAddCarWindow();
        });

        Button backButton = new Button("Back to Home");
        backButton.getStyleClass().add("back-button");
        backButton.setOnAction(e -> {
            stage.close();
            Main2.showMainWindow();
        });

        TextField searchField = new TextField();
        searchField.setPromptText("Search by car, service description, date or status...");
        searchField.setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff; -fx-border-color: #ced4da; -fx-text-fill: black;");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterServices(newValue));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topControls.getChildren().addAll(addNewButton, searchField, spacer, backButton);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("custom-table");

        TableColumn<ServiceRecord, String> actionCol = new TableColumn<>("Action");
        TableColumn<ServiceRecord, Integer> idCol = new TableColumn<>("Id");
        TableColumn<ServiceRecord, String> carCol = new TableColumn<>("Car");
        TableColumn<ServiceRecord, String> serviceCol = new TableColumn<>("Service");
        TableColumn<ServiceRecord, String> startDateCol = new TableColumn<>("Start Date");
        TableColumn<ServiceRecord, String> endDateCol = new TableColumn<>("End Date");
        TableColumn<ServiceRecord, String> statusCol = new TableColumn<>("Status");
        TableColumn<ServiceRecord, Button> viewCol = new TableColumn<>("View");

        serviceCol.setCellFactory(tc -> {
            return new TableCell<ServiceRecord, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        VBox container = new VBox(2);
                        String[] services = formatServiceDisplay(item).split("\n");

                        for (String service : services) {
                            if (!service.trim().isEmpty()) {
                                Text text = new Text(service);
                                text.setFill(javafx.scene.paint.Color.WHITE);
                                container.getChildren().add(text);
                            }
                        }

                        container.setAlignment(Pos.CENTER_LEFT);
                        setGraphic(container);

                        setStyle("-fx-padding: 5 0 5 0;");
                    }
                }
            };
        });

        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("✎");
            private final Button deleteButton = new Button("✕");
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                editButton.getStyleClass().add("edit-button");
                deleteButton.getStyleClass().add("delete-button");
                buttons.setAlignment(Pos.CENTER);

                editButton.setOnAction(e -> editService(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(e -> deleteService(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ServiceRecord record = getTableView().getItems().get(getIndex());
                    boolean isCompleted = "Completed".equals(record.getStatus());
                    editButton.setDisable(isCompleted);
                    deleteButton.setDisable(isCompleted);
                    setGraphic(buttons);
                }
            }
        });

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        carCol.setCellValueFactory(new PropertyValueFactory<>("car"));
        serviceCol.setCellValueFactory(new PropertyValueFactory<>("service"));
        startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        viewCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewButton = new Button("🔍");

            {
                viewButton.getStyleClass().add("view-button");
                viewButton.setOnAction(e -> viewServiceDetails(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });

        table.getColumns().addAll(actionCol, idCol, carCol, serviceCol, startDateCol, endDateCol, statusCol, viewCol);


        try {
            loadServicesHistory();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load services history.");
        }

        table.setItems(allServices);
        root.getChildren().addAll(topControls, table);

        Scene scene = new Scene(root, 1000, 600);
        Main2.applyStylesheet2(scene);
        stage.setScene(scene);
        stage.show();

        table.prefWidthProperty().bind(scene.widthProperty().multiply(0.98));
        table.prefHeightProperty().bind(scene.heightProperty().multiply(0.9));
    }

    private static String formatServiceDisplay(String description) {
        if (description == null) return "";

        StringBuilder formatted = new StringBuilder();
        String[] services = description.split("[,\n]");

        for (String service : services) {
            service = service.trim();
            if (service.isEmpty()) continue;

            if (service.toLowerCase().contains("discount")) continue;

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

    private static void loadServicesHistory() throws SQLException {
        String query = "SELECT m.maintenance_id, c.license_plate, m.description, m.maintenance_start, NULL as maintenance_end, 'Pending' as status, m.total_price " +
                    "FROM maintenance m " +
                    "JOIN cars c ON m.car_id = c.id " +
                    "WHERE m.user_id = ? " +
                    "UNION " +
                    "SELECT cm.maintenance_id, c.license_plate, cm.description, cm.maintenance_start, cm.maintenance_end, 'Completed' as status, cm.total_price " +
                    "FROM completed_maintenance cm " +
                    "JOIN cars c ON cm.car_id = c.id " +
                    "WHERE cm.user_id = ? " +
                    "ORDER BY maintenance_start DESC";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement st = conn.prepareStatement(query)) {

            st.setInt(1, Login.getCurrentUserId());
            st.setInt(2, Login.getCurrentUserId());
            ResultSet rs = st.executeQuery();

            allServices.clear();
            while (rs.next()) {
                String startDate = formatDateTime(rs.getString("maintenance_start"));
                String endDate = rs.getString("maintenance_end") != null ?
                    formatDateTime(rs.getString("maintenance_end")) : null;

                allServices.add(new ServiceRecord(
                    rs.getInt("maintenance_id"),
                    rs.getString("license_plate"),
                    rs.getString("description"),
                    startDate,
                    endDate,
                    rs.getString("status"),
                    rs.getDouble("total_price")
                ));
            }

            table.setItems(allServices);
        }
    }

    private static String formatDateTime(String dateTime) {
        if (dateTime == null) return null;
        String[] parts = dateTime.split("\\.");
        String mainPart = parts[0];
        return mainPart.substring(0, mainPart.lastIndexOf(":"));
    }

    private static void editService(ServiceRecord service) {
        if ("Completed".equals(service.getStatus())) {
            showAlert(Alert.AlertType.INFORMATION, "Cannot Edit", "Completed services cannot be edited.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(service.getService());
        dialog.setTitle("Edit Service");
        dialog.setHeaderText("Edit service for " + service.getCar());
        dialog.setContentText("Enter new service description:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newDescription -> {
            try {
                double newTotalPrice = calculateTotalPrice(newDescription);
                updateServiceDescription(service.getId(), newDescription, newTotalPrice);
                service.setService(newDescription);
                service.setTotalPrice(newTotalPrice);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Service updated successfully.");

                loadServicesHistory();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update service.");
            }
        });
    }

    private static void deleteService(ServiceRecord service) {
        if ("Completed".equals(service.getStatus())) {
            showAlert(Alert.AlertType.INFORMATION, "Cannot Delete", "Completed services cannot be deleted.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Service");
        alert.setHeaderText("Delete service for " + service.getCar());
        alert.setContentText("Are you sure you want to delete this service?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                deleteServiceRecord(service.getId());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Service deleted successfully.");
                loadServicesHistory();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete service.");
            }
        }
    }

private static void viewServiceDetails(ServiceRecord service) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Service Details");
    alert.setHeaderText("Details for service #" + service.getId());

    String formattedService = service.getService().replace(", ", "\n");

    Button generatePdfButton = new Button("Generate PDF Receipt");
    generatePdfButton.setOnAction(e -> generatePdfReceipt(service));
    generatePdfButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000; -fx-font-size: 12px; -fx-padding: 5 10 5 10;");

    generatePdfButton.setOnMouseEntered(e -> generatePdfButton.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #000000; -fx-font-size: 12px; -fx-padding: 5 10 5 10;"));
    generatePdfButton.setOnMouseExited(e -> generatePdfButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000; -fx-font-size: 12px; -fx-padding: 5 10 5 10;"));

    VBox contentBox = new VBox(10);
    contentBox.setPadding(new Insets(10));
    contentBox.setStyle("-fx-background-color: #1a1f3c;");

    Label detailsLabel = new Label(
        "Car: " + service.getCar() + "\n\n" +
        "Service:\n" + formattedService + "\n\n" +
        "Start Date: " + service.getStartDate() + "\n" +
        "End Date: " + (service.getEndDate() != null ? service.getEndDate() : "Not completed") + "\n" +
        "Status: " + service.getStatus() + "\n" +
        "Total Price: $" + String.format("%.2f", service.getTotalPrice())
    );
    detailsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

    contentBox.getChildren().addAll(detailsLabel, generatePdfButton);

    DialogPane dialogPane = alert.getDialogPane();
    dialogPane.setContent(contentBox);

    dialogPane.lookup(".header-panel").setStyle("-fx-background-color: white; -fx-text-fill: black;");

    Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
    okButton.setStyle("-fx-background-color: #1a1f3c; -fx-text-fill: white;");
    okButton.setOnMouseEntered(e -> okButton.setStyle("-fx-background-color: #404a70; -fx-text-fill: white;"));
    okButton.setOnMouseExited(e -> okButton.setStyle("-fx-background-color: #1a1f3c; -fx-text-fill: white;"));

    dialogPane.setPrefWidth(350);

    alert.showAndWait();
}

    private static void updateServiceDescription(int id, String newDescription, double newTotalPrice) throws SQLException {
        String updateMaintenanceQuery = "UPDATE maintenance SET description = ?, total_price = ?, discounted_price = ? WHERE maintenance_id = ?";
        String updateCompletedMaintenanceQuery = "UPDATE completed_maintenance SET description = ?, total_price = ?, discounted_price = ? WHERE maintenance_id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stMaintenance = conn.prepareStatement(updateMaintenanceQuery);
             PreparedStatement stCompletedMaintenance = conn.prepareStatement(updateCompletedMaintenanceQuery)) {

            stMaintenance.setString(1, newDescription);
            stMaintenance.setDouble(2, newTotalPrice);
            stMaintenance.setDouble(3, newTotalPrice); 
            stMaintenance.setInt(4, id);
            int maintenanceUpdated = stMaintenance.executeUpdate();

            stCompletedMaintenance.setString(1, newDescription);
            stCompletedMaintenance.setDouble(2, newTotalPrice);
            stCompletedMaintenance.setDouble(3, newTotalPrice); 
            stCompletedMaintenance.setInt(4, id);
            int completedMaintenanceUpdated = stCompletedMaintenance.executeUpdate();

            if (maintenanceUpdated == 0 && completedMaintenanceUpdated == 0) {
                throw new SQLException("No records were updated.");
            }
        }
    }

    private static void deleteServiceRecord(int id) throws SQLException {
        String deleteMaintenanceQuery = "DELETE FROM maintenance WHERE maintenance_id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stMaintenance = conn.prepareStatement(deleteMaintenanceQuery)) {

            stMaintenance.setInt(1, id);
            int maintenanceDeleted = stMaintenance.executeUpdate();

            if (maintenanceDeleted == 0) {
                throw new SQLException("No records were deleted.");
            }
        }
    }

    private static void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static double calculateTotalPrice(String serviceDescription) {
        String[] services = serviceDescription.split(",");
        double totalPrice = 0.0;

        for (String service : services) {
            String[] parts = service.split(":");
            if (parts.length == 2) {
                try {
                    double price = Double.parseDouble(parts[1].trim().replaceAll("[^\\d.]", ""));
                    totalPrice += price;
                } catch (NumberFormatException e) {
                }
            }
        }

        return totalPrice;
    }

    private static void filterServices(String query) {
        if (query == null || query.isEmpty()) {
            table.setItems(allServices);
        } else {
            ObservableList<ServiceRecord> filteredData = FXCollections.observableArrayList();
            for (ServiceRecord service : allServices) {
                if (service.getCar().toLowerCase().contains(query.toLowerCase()) ||
                    service.getService().toLowerCase().contains(query.toLowerCase()) ||
                    service.getStartDate().contains(query.toLowerCase()) ||
                    service.getStatus().toLowerCase().contains(query.toLowerCase())) {
                    filteredData.add(service);
                }
            }
            table.setItems(filteredData);
        }
    }

    private static void generatePdfReceipt(ServiceRecord service) {
        String fileName = "receipt_" + service.getId() + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font shopNameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10);

            Paragraph shopName = new Paragraph("AutoCare Pro", shopNameFont);
            shopName.setAlignment(Element.ALIGN_CENTER);
            document.add(shopName);

            document.add(new Chunk("\n"));
            document.add(new LineSeparator(1f, 100f, BaseColor.BLACK, Element.ALIGN_CENTER, -1));

            Paragraph receiptTitle = new Paragraph("Service Receipt", titleFont);
            receiptTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(receiptTitle);

            document.add(new Paragraph("Service ID: " + service.getId(), normalFont));
            document.add(new Paragraph("Car: " + service.getCar(), normalFont));

            List<String> serviceDescriptionLines = Arrays.asList(service.getService().split(", "));
            document.add(new Paragraph("Service Description:", normalFont));
            for (String line : serviceDescriptionLines) {
                document.add(new Paragraph("- " + line, normalFont));
            }

            document.add(new Paragraph("Start Date: " + service.getStartDate(), normalFont));
            document.add(new Paragraph("End Date: " + (service.getEndDate() != null ? service.getEndDate() : "Not completed"), normalFont));
            document.add(new Paragraph("Status: " + service.getStatus(), normalFont));
            document.add(new Paragraph("Total Price: $" + String.format("%.2f", service.getTotalPrice()), normalFont));

            document.add(new Chunk("\n"));
            document.add(new LineSeparator(1f, 100f, BaseColor.BLACK, Element.ALIGN_CENTER, -1));
            document.add(new Paragraph("Thank you for choosing AutoCare Pro!", footerFont));
            document.add(new Paragraph("Receipt generated by: Mohammad Ali Shkeir ,Mostafa and Mohammad Karnib", footerFont));
            
            document.close();

            File pdfFile = new File(fileName);
            if (pdfFile.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "PDF Generated", "Receipt has been generated: " + fileName + "\nPlease open it manually.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate PDF: File not found");
            }
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate PDF: " + e.getMessage());
        }
    }


    public static class ServiceRecord {
        private final Integer id;
        private final String car;
        private String service;
        private final String startDate;
        private final String endDate;
        private final String status;
        private double totalPrice;

        public ServiceRecord(Integer id, String car, String service, String startDate, String endDate, String status, double totalPrice) {
            this.id = id;
            this.car = car;
            this.service = service;
            this.startDate = startDate;
            this.endDate = endDate;
            this.status = status;
            this.totalPrice = totalPrice;
        }

        public Integer getId() { return id; }
        public String getCar() { return car; }
        public String getService() { return service; }
        public void setService(String service) { this.service = service; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
        public String getStatus() { return status; }
        public double getTotalPrice() { return totalPrice; }
        public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    }
}

