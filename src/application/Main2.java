package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;



public class Main2 extends Application {
    private static Stage primaryStage;
    private static VBox root; 

    public static void applyStylesheet(Scene scene) {
        scene.getStylesheets().add(Main2.class.getResource("application.css").toExternalForm());
    }
    public static void applyStylesheet2(Scene scene) {
        scene.getStylesheets().add(Main2.class.getResource("application2.css").toExternalForm());
    }
    @Override
    public void start(Stage primaryStage) {
        Main2.primaryStage = primaryStage;
        Login.showLoginWindow();
    }

public static void showMainWindow() {
    primaryStage.setTitle("AutoCare Pro Dashboard");

    root = new VBox(20);
    root.setStyle("-fx-background-color: #111827;");
    root.setPadding(new Insets(20));

    HBox header = createHeader();

    VBox homeSection = createHomeSection();

    HBox menuCards = createMenuCards();
    HBox menuCards1 = createMenuCards1();

    root.getChildren().addAll(header, homeSection, menuCards,menuCards1);

    Scene scene = new Scene(root);
    primaryStage.setScene(scene);
    
    primaryStage.setMaximized(true);
    
    primaryStage.show();
}

    private static HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 0, 10, 0));
        
        Label logo = new Label("ACP.");
        logo.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        Button addCarBtn = createNavButton("Add Car", e -> showAddCarWindow());
        Button viewServicesBtn = createNavButton("View services", e -> showViewServicesWindow());
        Button viewPastServicesBtn = createNavButton("View Past services", e -> showViewPastServicesWindow());
        Button paymentsBtn = createNavButton("Payments", e -> showViewPaymentsWindow());
        Button serviceHistoryBtn = createNavButton("Service history", e -> showServicesHistoryWindow());
        
        Button carListBtn = createNavButton("Car List", e -> showCarListView());
        Button viewWinningsBtn = createNavButton("View Winnings", e -> showWinningsView());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #FCD34D; -fx-border-color: #FCD34D; -fx-border-radius: 4px; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> {
            Login.showLoginWindow();
            primaryStage.close();
        });
        
        header.getChildren().addAll(
            logo, 
            addCarBtn, 
            viewServicesBtn, 
            viewPastServicesBtn, 
            paymentsBtn, 
            serviceHistoryBtn,
            carListBtn,
            viewWinningsBtn,

            spacer, 
            
            logoutBtn
        );
        
        return header;
    }

    private static Button createNavButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button button = new Button(text);
        button.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #9CA3AF;
            -fx-cursor: hand;
            -fx-padding: 8px 12px;
            -fx-border-color: transparent;
            """);
        

        button.setOnMouseEntered(e -> {
            button.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: #FCD34D;
                -fx-cursor: hand;
                -fx-padding: 8px 12px;
                -fx-border-color: transparent;
                """);
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: #9CA3AF;
                -fx-cursor: hand;
                -fx-padding: 8px 12px;
                -fx-border-color: transparent;
                """);
        });
        
        button.setOnAction(e -> {
            primaryStage.close();  
            action.handle(e);  
            });
        
        return button;
    }

    private static VBox createHomeSection() {
        VBox homeSection = new VBox(10);
        homeSection.setAlignment(Pos.CENTER);
        homeSection.setPadding(new Insets(60, 0, 0, 0));

        Label title = new Label("Welcome to AutoCare Pro");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 48px; -fx-font-weight: bold;");

        Label subtitle = new Label("We are a team of talented WORKERS");
        subtitle.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 24px;");

        homeSection.getChildren().addAll(title, subtitle);
        return homeSection;
    }
    private static HBox createMenuCards() {
        HBox menuCards = new HBox(20);
        menuCards.setAlignment(Pos.CENTER);

        VBox addCar = createMenuCard("Add Car", "➕");
        VBox viewCurrentServices = createMenuCard("View Current\nServices", "🎯");
        VBox viewPastServices = createMenuCard("View Past\nServices", "📋");
        VBox viewPayments = createMenuCard("View Payments", "💎");


        menuCards.getChildren().addAll(addCar, viewCurrentServices, viewPastServices, viewPayments );
        return menuCards;
    }
    private static HBox createMenuCards1() {
        HBox menuCards = new HBox(20);
        menuCards.setAlignment(Pos.CENTER);

        VBox carServices = createMenuCard("Show car\nServices", "🔍");
        VBox viewWinnings = createMenuCard("Show \nWinnings", "💰");
        VBox showHistory = createMenuCard("Show Services\nHistory", "📚");

        menuCards.getChildren().addAll(showHistory,carServices,viewWinnings);
        return menuCards;
    }
    private static VBox createMenuCard(String text, String icon) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: rgba(31, 41, 55, 0.5); -fx-background-radius: 8px; -fx-border-color: #374151; -fx-border-radius: 8px;");
        card.setPrefSize(200, 200);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-text-fill: #FCD34D; -fx-font-size: 32px;");

        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-text-alignment: center;");
        textLabel.setWrapText(true);
        textLabel.setAlignment(Pos.CENTER);

        card.getChildren().addAll(iconLabel, textLabel);

        card.setOnMouseClicked(null);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: rgba(31, 41, 55, 0.8); -fx-background-radius: 8px; -fx-border-color: #FCD34D; -fx-border-radius: 8px;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: rgba(31, 41, 55, 0.5); -fx-background-radius: 8px; -fx-border-color: #374151; -fx-border-radius: 8px;"));

        return card;
    }

       
    
    
    
    
    
    
    
    
    private static void showHomeSection() {
        root.getChildren().clear();
        root.getChildren().addAll(createHeader(), createHomeSection(), createMenuCards());
    }

    private static void showAddCarWindow() {
        addCar.showAddCarWindow();

    }

    private static void showViewServicesWindow() {
        viewServices.showViewServicesWindow();

    }

    private static void showViewPastServicesWindow() {
        viewPastServices.showViewPastServicesWindow();

    }

    private static void showViewPaymentsWindow() {
        viewPayments.showViewPaymentsWindow();

    }

    private static void showServicesHistoryWindow() {
        ServicesHistory.showServicesHistoryWindow();

    }
    private static void showCarListView() {
        CarListView.showCarListView();
    }
    private static void showWinningsView() {
    	showWinnings.showWinningsWindow();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

