package application;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.text.Text;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class CarListView {

    private static TableView<Car> table;
    private static ObservableList<Car> allCars = FXCollections.observableArrayList();

    public static void showCarListView() {
        Stage stage = new Stage();
        stage.setTitle("Car List");
        stage.setMaximized(true);

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        TextField searchField = new TextField();
        searchField.setPromptText("Search by license plate, owner name, or car model...");
        searchField.setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff; -fx-border-color: #ced4da;-fx-text-fill: black;");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterCars(newValue));

        Button backButton = new Button("Back to Home");
        backButton.getStyleClass().add("button-secondary");
        backButton.setOnAction(e -> {
            stage.close();
            Main2.showMainWindow();
        });

        HBox topControls = new HBox(10, backButton, searchField);
        topControls.setAlignment(Pos.CENTER_LEFT);
        root.getChildren().add(topControls);

        table = createCarTable();
        root.getChildren().add(table);

        Scene scene = new Scene(root, 1000, 600);
        Main2.applyStylesheet(scene);
        stage.setScene(scene);
        stage.show();

        loadCars();
    }

    private static TableView<Car> createCarTable() {
        TableView<Car> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Car, String> licensePlateCol = new TableColumn<>("License Plate");
        TableColumn<Car, String> ownerNameCol = new TableColumn<>("Owner Name");
        TableColumn<Car, String> ownerContactCol = new TableColumn<>("Owner Contact");
        TableColumn<Car, String> carModelCol = new TableColumn<>("Car Model");
        TableColumn<Car, Integer> yearCol = new TableColumn<>("Year");
        TableColumn<Car, Void> actionsCol = new TableColumn<>("Actions");

        licensePlateCol.setCellValueFactory(new PropertyValueFactory<>("licensePlate"));
        ownerNameCol.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        ownerContactCol.setCellValueFactory(new PropertyValueFactory<>("ownerContact"));
        carModelCol.setCellValueFactory(new PropertyValueFactory<>("carModel"));
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));

        ownerNameCol.setCellFactory(tc -> new TableCell<Car, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    VBox container = new VBox(2);
                    for (String name : item.split("\n")) {
                        Text text = new Text(name.trim());
                        text.setFill(javafx.scene.paint.Color.WHITE);
                        container.getChildren().add(text);
                    }
                    setGraphic(container);
                }
            }
        });

        ownerContactCol.setCellFactory(tc -> new TableCell<Car, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    VBox container = new VBox(2);
                    for (String contact : item.split("\n")) {
                        Text text = new Text(contact.trim());
                        text.setFill(javafx.scene.paint.Color.WHITE);
                        container.getChildren().add(text);
                    }
                    setGraphic(container);
                }
            }
        });

        carModelCol.setCellFactory(tc -> new TableCell<Car, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    VBox container = new VBox(2);
                    for (String model : item.split("\n")) {
                        Text text = new Text(model.trim());
                        text.setFill(javafx.scene.paint.Color.WHITE);
                        container.getChildren().add(text);
                    }
                    setGraphic(container);
                }
            }
        });

        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button viewServicesBtn = new Button("View Services");
            private final Button addServiceBtn = new Button("Add Service");
            private final HBox buttonContainer = new HBox(5);

            {
                viewServicesBtn.setOnAction(event -> {
                    Car car = getTableView().getItems().get(getIndex());
                    viewServices(car);
                });

                addServiceBtn.setOnAction(event -> {
                    Car car = getTableView().getItems().get(getIndex());
                    addNewService(car);
                });

                viewServicesBtn.getStyleClass().add("button-primary");
                addServiceBtn.getStyleClass().add("button-secondary");
                
                buttonContainer.setAlignment(Pos.CENTER);
                buttonContainer.getChildren().addAll(viewServicesBtn, addServiceBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonContainer);
                }
            }
        });

        table.getColumns().addAll(licensePlateCol, ownerNameCol, ownerContactCol, carModelCol, yearCol, actionsCol);

        return table;
    }

    private static void loadCars() {
        String query = "SELECT * FROM Cars WHERE user_id = ?";
        Map<String, CarBuilder> carMap = new HashMap<>();

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, Login.getCurrentUserId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String licensePlate = rs.getString("license_plate");
                String ownerName = rs.getString("owner_name");
                String ownerContact = rs.getString("owner_contact");
                String carModel = rs.getString("car_model");
                int year = rs.getInt("year");
                int id = rs.getInt("id");

                carMap.computeIfAbsent(licensePlate, k -> new CarBuilder(licensePlate, year))
                      .addOwner(ownerName)
                      .addContact(ownerContact)
                      .addModel(carModel)
                      .addId(id);
            }

            allCars.clear();
            for (CarBuilder builder : carMap.values()) {
                allCars.add(builder.build());
            }

            Platform.runLater(() -> table.setItems(allCars));

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load cars: " + e.getMessage());
        }
    }

    private static void filterCars(String query) {
        if (query == null || query.isEmpty()) {
            table.setItems(allCars);
        } else {
            ObservableList<Car> filteredData = FXCollections.observableArrayList();
            for (Car car : allCars) {
                if (car.getLicensePlate().toLowerCase().contains(query.toLowerCase()) ||
                    car.getOwnerName().toLowerCase().contains(query.toLowerCase()) ||
                    car.getCarModel().toLowerCase().contains(query.toLowerCase())) {
                    filteredData.add(car);
                }
            }
            table.setItems(filteredData);
        }
    }

    private static void viewServices(Car car) {
        Stage serviceStage = new Stage();
        serviceStage.setTitle("Services for " + car.getLicensePlate());

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        TableView<Service> serviceTable = new TableView<>();
        serviceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Service, String> descriptionCol = new TableColumn<>("Description");
        TableColumn<Service, String> startDateCol = new TableColumn<>("Start Date");
        TableColumn<Service, String> statusCol = new TableColumn<>("Status");
        TableColumn<Service, Double> totalPriceCol = new TableColumn<>("Total Price");

        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        totalPriceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        totalPriceCol.setCellFactory(column -> new TableCell<Service, Double>() {
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

        serviceTable.getColumns().addAll(descriptionCol, startDateCol, statusCol, totalPriceCol);

        root.getChildren().add(serviceTable);

        Scene scene = new Scene(root, 800, 600);
        Main2.applyStylesheet(scene);
        serviceStage.setScene(scene);
        serviceStage.show();

        loadServices(car, serviceTable);
    }

    private static void loadServices(Car car, TableView<Service> serviceTable) {
        String query = 
        "SELECT m.description, m.maintenance_start, m.total_price, 'Ongoing' as status " +
        "FROM Maintenance m " +
        "WHERE m.car_id IN (" + String.join(",", car.getCarIds().stream().map(String::valueOf).toArray(String[]::new)) + ") " +
        "UNION ALL " +
        "SELECT cm.description, cm.maintenance_start, cm.total_price, 'Completed' as status " +
        "FROM Completed_Maintenance cm " +
        "WHERE cm.car_id IN (" + String.join(",", car.getCarIds().stream().map(String::valueOf).toArray(String[]::new)) + ") " +
        "ORDER BY maintenance_start DESC";

        ObservableList<Service> services = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String description = rs.getString("description");
                String startDate = rs.getString("maintenance_start");
                String status = rs.getString("status");
                double totalPrice = rs.getDouble("total_price");

                if (totalPrice == 0 && "Ongoing".equals(status)) {
                    totalPrice = calculateTotalPriceFromDescription(description);
                    updateMaintenanceTotalPrice(car.getCarIds().get(0), description, totalPrice);
                }

                Service service = new Service(description, startDate, status, totalPrice);
                services.add(service);
            }

            Platform.runLater(() -> serviceTable.setItems(services));

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load services: " + e.getMessage());
        }
    }
    private static double calculateTotalPriceFromDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        String[] services = description.split(",");
        
        for (String service : services) {
            service = service.trim();
            if (service.contains(":")) {
                try {
                    String priceStr = service.substring(service.indexOf(":") + 1).trim();
                    priceStr = priceStr.replaceAll("[^0-9.]", "");
                    double price = Double.parseDouble(priceStr);
                    total += price;
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    continue;
                }
            }
        }
        
        return total;
    }

    private static void updateMaintenanceTotalPrice(int carId, String description, double totalPrice) {
        String query = "UPDATE Maintenance SET total_price = ? WHERE car_id = ? AND description = ?";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setDouble(1, totalPrice);
            pstmt.setInt(2, carId);
            pstmt.setString(3, description);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private static void addNewService(Car car) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add New Service");
        dialog.setHeaderText("Enter service description for " + car.getLicensePlate());
        dialog.setContentText("Service Description:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            try {
                int carId = car.getCarIds().get(0);
                
                String query = "INSERT INTO maintenance (car_id, description, maintenance_start, user_id) VALUES (?, ?, GETDATE(), ?)";
                
                try (Connection conn = DatabaseConnection.connect();
                     PreparedStatement pstmt = conn.prepareStatement(query)) {
                    
                    pstmt.setInt(1, carId);
                    pstmt.setString(2, result.get().trim());
                    pstmt.setInt(3, Login.getCurrentUserId());
                    
                    int insertResult = pstmt.executeUpdate();
                    
                    if (insertResult > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", 
                            "New service added for car " + car.getLicensePlate() + "\nPlease check View Services to manage it.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to add new service");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Failed to add new service: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Cancelled", "Service addition cancelled or empty description provided.");
        }
    }

    private static class CarBuilder {
        private final String licensePlate;
        private final int year;
        private final List<String> ownerNames = new ArrayList<>();
        private final List<String> ownerContacts = new ArrayList<>();
        private final List<String> carModels = new ArrayList<>();
        private final List<Integer> carIds = new ArrayList<>();

        public CarBuilder(String licensePlate, int year) {
            this.licensePlate = licensePlate;
            this.year = year;
        }

        public CarBuilder addOwner(String name) {
            if (!ownerNames.contains(name)) {
                ownerNames.add(name);
            }
            return this;
        }

        public CarBuilder addContact(String contact) {
            if (!ownerContacts.contains(contact)) {
                ownerContacts.add(contact);
            }
            return this;
        }

        public CarBuilder addModel(String model) {
            if (!carModels.contains(model)) {
                carModels.add(model);
            }
            return this;
        }

        public CarBuilder addId(int id) {
            if (!carIds.contains(id)) {
                carIds.add(id);
            }
            return this;
        }

        public Car build() {
            return new Car(
                licensePlate,
                String.join("\n", ownerNames),
                String.join("\n", ownerContacts),
                String.join("\n", carModels),
                year,
                carIds
            );
        }
    }

    public static class Car {
        private final String licensePlate;
        private final String ownerName;
        private final String ownerContact;
        private final String carModel;
        private final int year;
        private final List<Integer> carIds;

        public Car(String licensePlate, String ownerName, String ownerContact, String carModel, int year, List<Integer> carIds) {
            this.licensePlate = licensePlate;
            this.ownerName = ownerName;
            this.ownerContact = ownerContact;
            this.carModel = carModel;
            this.year = year;
            this.carIds = carIds;
        }

        public String getLicensePlate() { return licensePlate; }
        public String getOwnerName() { return ownerName; }
        public String getOwnerContact() { return ownerContact; }
        public String getCarModel() { return carModel; }
        public int getYear() { return year; }
        public List<Integer> getCarIds() { return carIds; }
    }

    public static class Service {
        private final String description;
        private final String startDate;
        private final String status;
        private final double totalPrice;

        public Service(String description, String startDate, String status, double totalPrice) {
            this.description = description;
            this.startDate = startDate;
            this.status = status;
            this.totalPrice = totalPrice;
        }

        public String getDescription() { return description; }
        public String getStartDate() { return startDate; }
        public String getStatus() { return status; }
        public double getTotalPrice() { return totalPrice; }
    }
}

