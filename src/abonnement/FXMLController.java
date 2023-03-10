
package abonnement;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
//import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
//import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author MarcoMan 
 * PLEASE SUBSCRIBE OUR CHANNEL FOR SUPPORT AND HIT THE LIKE BUTTON THANKS : )
 */
public class FXMLController implements Initializable {

    @FXML
    private AnchorPane main_form;

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Button login;
    
    @FXML
    private Button skip;    
    
    @FXML
    private Button close;
    



    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;

    private double x = 0;
    private double y = 0;
            public void skip() {

        Alert alert;

        try {

                    // THEN WE WILL PROCEED TO DASHBOARD FORM : ) 
                    alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Information Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Entered As User");
                    alert.showAndWait();
                    skip.getScene().getWindow().hide();

                    Parent root;
                    root = FXMLLoader.load(getClass().getResource("User.fxml"));

                    Stage stage = new Stage();
                    Scene scene = new Scene(root);

                    root.setOnMousePressed((MouseEvent event) -> {
                        x = event.getSceneX();
                        y = event.getSceneY();
                    });

                    root.setOnMouseDragged((MouseEvent event) -> {
                        stage.setX(event.getScreenX() - x);
                        stage.setY(event.getScreenY() - y);
                    });

                    stage.initStyle(StageStyle.TRANSPARENT);

                    stage.setScene(scene);
                    stage.show();

         
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void login() {

        String sql = "SELECT * FROM admin WHERE username = ? and password = ?";

        connect = AbonDB.connectDb();

        Alert alert;

        try {

            if (username.getText().isEmpty() || password.getText().isEmpty()) {

                alert = new Alert(AlertType.ERROR);
                alert.setTitle("Ërror Message");
                alert.setHeaderText(null);
                alert.setContentText("Please fill all blank fields");
                alert.showAndWait();

            } else {

                prepare = connect.prepareStatement(sql);
                prepare.setString(1, username.getText());
                prepare.setString(2, password.getText());

                result = prepare.executeQuery();

                if (result.next()) {

                    getData.username = username.getText();

                    // THEN WE WILL PROCEED TO DASHBOARD FORM : ) 
                    alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Information Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Successfully Login!");
                    alert.showAndWait();
                    login.getScene().getWindow().hide();

                    Parent root;
                    root = FXMLLoader.load(getClass().getResource("Admin1.fxml"));

                    Stage stage = new Stage();
                    Scene scene = new Scene(root);

                    root.setOnMousePressed((MouseEvent event) -> {
                        x = event.getSceneX();
                        y = event.getSceneY();
                    });

                    root.setOnMouseDragged((MouseEvent event) -> {
                        stage.setX(event.getScreenX() - x);
                        stage.setY(event.getScreenY() - y);
                    });

                    stage.initStyle(StageStyle.TRANSPARENT);

                    stage.setScene(scene);
                    stage.show();

                } else {
//                    IF INCORRECT THE DATA YOU GAVE 
                    alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Wrong Username/Password");
                    alert.showAndWait();

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void close() {
        System.exit(0);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

}
