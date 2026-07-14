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
	
	public class Login {
	    private static int currentUserId = -1;
	
	    public static void showLoginWindow() {
	        Stage stage = new Stage();
	        stage.setTitle("Login");
	
	        GridPane grid = new GridPane();
	        grid.setAlignment(Pos.CENTER);
	        grid.setHgap(10);
	        grid.setVgap(10);
	        grid.setPadding(new Insets(25, 25, 25, 25));
	        grid.getStyleClass().add("main-background");
	
	        TextField usernameField = new TextField();
	        usernameField.setPromptText("Username");
	        PasswordField passwordField = new PasswordField();
	        passwordField.setPromptText("Password");
	
	        Button loginButton = new Button("Login");
	        loginButton.getStyleClass().addAll("styled-button", "login-button");
	        Button registerButton = new Button("Register");
	        registerButton.getStyleClass().addAll("styled-button", "register-button");
	
	        grid.add(new Label("Username:"), 0, 0);
	        grid.add(usernameField, 1, 0);
	        grid.add(new Label("Password:"), 0, 1);
	        grid.add(passwordField, 1, 1);
	
	        HBox hbBtn = new HBox(10);
	        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
	        hbBtn.getChildren().addAll(loginButton, registerButton);
	        grid.add(hbBtn, 1, 3);
	
	        loginButton.setOnAction(e -> {
	            String username = usernameField.getText();
	            String password = passwordField.getText();
	            if (authenticateUser(username, password)) {
	                stage.close();
	                Main2.showMainWindow();
	            } else {
	                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
	            }
	        });
	
	        registerButton.setOnAction(e -> {
	            stage.close();
	            showRegisterWindow();
	        });
	
	        Scene scene = new Scene(grid, 300, 200);
	        Main2.applyStylesheet(scene);
	        stage.setScene(scene);
	        stage.show();
	    }
	
	    private static boolean authenticateUser(String username, String password) {
	        String query = "SELECT user_id FROM Users WHERE username = ? AND password = ?";
	        try (Connection conn = DatabaseConnection.connect();
	             PreparedStatement pstmt = conn.prepareStatement(query)) {
	            pstmt.setString(1, username);
	            pstmt.setString(2, password);
	            ResultSet rs = pstmt.executeQuery();
	            if (rs.next()) {
	                currentUserId = rs.getInt("user_id");
	                return true;
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return false;
	    }
	
	    private static void showRegisterWindow() {
	        Stage stage = new Stage();
	        stage.setTitle("Register");
	
	        GridPane grid = new GridPane();
	        grid.setAlignment(Pos.CENTER);
	        grid.setHgap(10);
	        grid.setVgap(10);
	        grid.setPadding(new Insets(25, 25, 25, 25));
	        grid.getStyleClass().add("main-background");
	
	        TextField usernameField = new TextField();
	        usernameField.setPromptText("Username");
	        PasswordField passwordField = new PasswordField();
	        passwordField.setPromptText("Password");
	        TextField emailField = new TextField();
	        emailField.setPromptText("Email");
	
	        Button registerButton = new Button("Register");
	        registerButton.getStyleClass().addAll("styled-button", "register-button");
	
	        grid.add(new Label("Username:"), 0, 0);
	        grid.add(usernameField, 1, 0);
	        grid.add(new Label("Password:"), 0, 1);
	        grid.add(passwordField, 1, 1);
	        grid.add(new Label("Email:"), 0, 2);
	        grid.add(emailField, 1, 2);
	        grid.add(registerButton, 1, 3);
	
	        registerButton.setOnAction(e -> {
	            String username = usernameField.getText();
	            String password = passwordField.getText();
	            String email = emailField.getText();
	            if (registerUser(username, password, email)) {
	                showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "You can now login with your credentials.");
	                stage.close();
	                showLoginWindow();
	            } else {
	                showAlert(Alert.AlertType.ERROR, "Registration Failed", "Username or email already exists.");
	            }
	        });
	
	        Scene scene = new Scene(grid, 300, 250);
	        Main2.applyStylesheet(scene);
	        stage.setScene(scene);
	        stage.show();
	    }
	
	    private static boolean registerUser(String username, String password, String email) {
	        String query = "INSERT INTO Users (username, password, email) VALUES (?, ?, ?)";
	        try (Connection conn = DatabaseConnection.connect();
	             PreparedStatement pstmt = conn.prepareStatement(query)) {
	            pstmt.setString(1, username);
	            pstmt.setString(2, password); 
	            pstmt.setString(3, email);
	            int affectedRows = pstmt.executeUpdate();
	            return affectedRows > 0;
	        } catch (SQLException e) {
	            e.printStackTrace();
	            return false;
	        }
	    }
	
	    private static void showAlert(Alert.AlertType alertType, String title, String message) {
	        Alert alert = new Alert(alertType);
	        alert.setTitle(title);
	        alert.setHeaderText(null);
	        alert.setContentText(message);
	        alert.showAndWait();
	    }
	
	    public static int getCurrentUserId() {
	        return currentUserId;
	    }
	}
	
