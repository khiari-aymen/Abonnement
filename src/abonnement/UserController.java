
package abonnement;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import javax.mail.MessagingException;
import abonnement.abonData;
import com.stripe.exception.StripeException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class UserController implements Initializable {

    @FXML
    private Label label;


    @FXML
    private Button availableB_addBtn;


    @FXML
    private Button availableB_resetBtn;

    @FXML
    private ComboBox<String> typeabn;

    @FXML
    private TextField idabn;

    @FXML
    private TextField idclient;

   
   

    private TableColumn<abonData, String> availableB_col_busID;

    private TableColumn<abonData, String> availableB_col_location;

    private TableColumn<abonData, String> availableB_col_status;

    private TableColumn<abonData, String> availableB_col_price;

    private TableColumn<abonData, String> availableB_col_date;

    


//    DATABASE TOOLS
    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;
    private Statement statement;
    @FXML
    private AnchorPane availableB_form;
    @FXML
    public Button admin;
    @FXML
    private DatePicker Datedeb;
    @FXML
    private ComboBox<String> Duree;
    @FXML
    private TextField mailkh;
    @FXML
    private Label ajouterPointsFidelite;
    private Object abonnement;
    
    
   
    
    
    @FXML
    void admin(ActionEvent event) throws IOException {
    // Charger le fichier FXML de la nouvelle interface graphique
    FXMLLoader loader = new FXMLLoader(getClass().getResource("FXML.fxml"));
    Parent root = loader.load();

    // Créer une nouvelle scène pour la nouvelle interface graphique
    Scene scene = new Scene(root);

    // Récupérer la scène actuelle
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

    // Définir la nouvelle scène pour le stage actuel
    stage.setScene(scene);

    // Afficher la nouvelle interface graphique
    stage.show();
}


   
    
    @FXML
    public void availableBusAdd() {

        String addData = "INSERT INTO abonnement (abonId,clientId,type,dateDeb,duree) VALUES(?,?,?,?,?)";

        connect = AbonDB.connectDb();

        try {

            Alert alert;

            if (idabn.getText().isEmpty()
                    || idclient.getText().isEmpty()
                    || typeabn.getSelectionModel().getSelectedItem() == null
                    || Datedeb.getValue() == null
                    || Duree.getSelectionModel().getSelectedItem() == null)
                    {

                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Please fill all blank fields");
                alert.showAndWait();

            } else {

                
                String check = "SELECT abonId FROM abonnement WHERE abonId = '"
                        + idabn.getText() + "'";

                statement = connect.createStatement();
                result = statement.executeQuery(check);

                if (result.next()) {

                    alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error Message");
                    alert.setHeaderText(null);
                    alert.setContentText("ID Abn: " + idabn.getText() + " was already exist!");
                    alert.showAndWait();

                } else {

                    prepare = connect.prepareStatement(addData);
                    prepare.setString(1, idabn.getText());
                    prepare.setString(2, idclient.getText());
                    prepare.setString(3, (String)typeabn.getSelectionModel().getSelectedItem());
                    prepare.setString(4, String.valueOf(Datedeb.getValue()));
                    prepare.setString(5, (String)Duree.getSelectionModel().getSelectedItem());
                 
                    prepare.executeUpdate();
                    
                    // Calcul des points de fidélité et mise à jour de l'abonnement
                    int pointsFidelite = 0;
                    switch ((String)typeabn.getSelectionModel().getSelectedItem()) {
                        case "bronze":
                            pointsFidelite = 10;
                            break;
                        case "Silver":
                            pointsFidelite = 15;
                            break;
                        case "Gold":
                            pointsFidelite = 20;
                            break;
                        default:
                            break;
                    }

                    abonData ABN = new abonData();
                    ABN.setPointsFidelite(ABN.getPointsFidelite() + pointsFidelite);
                    ajouterPointsFidelite.setText("Points de fidélité : " + ABN.getPointsFidelite());

                    
                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Confirmation");
                    alert.setHeaderText(null);
                    alert.setContentText("Votre abonnement a été enregistré avec succès! \n\n NB: si vous avez besoin de modifier ou supprimer votre abonnement vous pouvez soumettre une réclamation.");
                    alert.showAndWait();
                    sendEmail(mailkh.getText());
                    //availableBShowBusData();
                    availableBusReset();

                } 
            }
        } catch (Exception e) {
            e.printStackTrace();
        /*} catch (MessagingException e) {*/
            e.printStackTrace();
            // show error dialog
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText("Failed to send confirmation email. Please check your email address and try again.");
            alert.showAndWait();
          }
     
      
   
        
    

  
    }
   
    
    
    private void sendEmail(String recipient) throws MessagingException {
        // set up the SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        // create a new session with an authenticator
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("thatway3a18@gmail.com", "zhapbdqpcrdesneb");
            }
        });

        // create a new message
        Message emailMessage = new MimeMessage(session);
        emailMessage.setFrom(new InternetAddress("thatway3a18@gmail.com"));
        emailMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        String htmlContent = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" style=\"font-family:arial, 'helvetica neue', helvetica, sans-serif\">\n"
                + " <head>\n"
                + "  <meta charset=\"UTF-8\">\n"
                + "  <meta content=\"width=device-width, initial-scale=1\" name=\"viewport\">\n"
                + "  <meta name=\"x-apple-disable-message-reformatting\">\n"
                + "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n"
                + "  <meta content=\"telephone=no\" name=\"format-detection\">\n"
                + "  <title>New message</title><!--[if (mso 16)]>\n"
                + "    <style type=\"text/css\">\n"
                + "    a {text-decoration: none;}\n"
                + "    </style>\n"
                + "    <![endif]--><!--[if gte mso 9]><style>sup { font-size: 100% !important; }</style><![endif]--><!--[if gte mso 9]>\n"
                + "<xml>\n"
                + "    <o:OfficeDocumentSettings>\n"
                + "    <o:AllowPNG></o:AllowPNG>\n"
                + "    <o:PixelsPerInch>96</o:PixelsPerInch>\n"
                + "    </o:OfficeDocumentSettings>\n"
                + "</xml>\n"
                + "<![endif]--><!--[if !mso]><!-- -->\n"
                + "  <link href=\"https://fonts.googleapis.com/css2?family=Barlow&display=swap\" rel=\"stylesheet\">\n"
                + "  <link href=\"https://fonts.googleapis.com/css2?family=Barlow+Condensed&display=swap\" rel=\"stylesheet\"><!--<![endif]-->\n"
                + "  <style type=\"text/css\">\n"
                + "@media only screen and (max-width:600px) {\n"
                + "  p,\n"
                + "ul li,\n"
                + "ol li,\n"
                + "a {\n"
                + "    line-height: 150%!important;\n"
                + "  }\n"
                + "\n"
                + "  h1,\n"
                + "h2,\n"
                + "h3,\n"
                + "h1 a,\n"
                + "h2 a,\n"
                + "h3 a {\n"
                + "    line-height: 120%;\n"
                + "  }\n"
                + "\n"
                + "  h1 {\n"
                + "    font-size: 46px!important;\n"
                + "    text-align: left;\n"
                + "  }\n"
                + "\n"
                + "  h2 {\n"
                + "    font-size: 28px!important;\n"
                + "    text-align: left;\n"
                + "  }\n"
                + "\n"
                + "  h3 {\n"
                + "    font-size: 20px!important;\n"
                + "    text-align: center;\n"
                + "  }\n"
                + "\n"
                + "  .es-header-body h1 a,\n"
                + ".es-content-body h1 a,\n"
                + ".es-footer-body h1 a {\n"
                + "    font-size: 46px!important;\n"
                + "    text-align: left;\n"
                + "  }\n"
                + "\n"
                + "  .es-header-body h2 a,\n"
                + ".es-content-body h2 a,\n"
                + ".es-footer-body h2 a {\n"
                + "    font-size: 28px!important;\n"
                + "    text-align: left;\n"
                + "  }\n"
                + "\n"
                + "  .es-header-body h3 a,\n"
                + ".es-content-body h3 a,\n"
                + ".es-footer-body h3 a {\n"
                + "    font-size: 20px!important;\n"
                + "    text-align: center;\n"
                + "  }\n"
                + "\n"
                + "  .es-menu td a {\n"
                + "    font-size: 12px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-header-body p,\n"
                + ".es-header-body ul li,\n"
                + ".es-header-body ol li,\n"
                + ".es-header-body a {\n"
                + "    font-size: 14px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-content-body p,\n"
                + ".es-content-body ul li,\n"
                + ".es-content-body ol li,\n"
                + ".es-content-body a {\n"
                + "    font-size: 14px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-footer-body p,\n"
                + ".es-footer-body ul li,\n"
                + ".es-footer-body ol li,\n"
                + ".es-footer-body a {\n"
                + "    font-size: 14px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-infoblock p,\n"
                + ".es-infoblock ul li,\n"
                + ".es-infoblock ol li,\n"
                + ".es-infoblock a {\n"
                + "    font-size: 12px!important;\n"
                + "  }\n"
                + "\n"
                + "  *[class=\"gmail-fix\"] {\n"
                + "    display: none!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-txt-c,\n"
                + ".es-m-txt-c h1,\n"
                + ".es-m-txt-c h2,\n"
                + ".es-m-txt-c h3 {\n"
                + "    text-align: center!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-txt-r,\n"
                + ".es-m-txt-r h1,\n"
                + ".es-m-txt-r h2,\n"
                + ".es-m-txt-r h3 {\n"
                + "    text-align: right!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-txt-l,\n"
                + ".es-m-txt-l h1,\n"
                + ".es-m-txt-l h2,\n"
                + ".es-m-txt-l h3 {\n"
                + "    text-align: left!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-txt-r img,\n"
                + ".es-m-txt-c img,\n"
                + ".es-m-txt-l img {\n"
                + "    display: inline!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-button-border {\n"
                + "    display: inline-block!important;\n"
                + "  }\n"
                + "\n"
                + "  a.es-button,\n"
                + "button.es-button {\n"
                + "    font-size: 18px!important;\n"
                + "    display: inline-block!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-adaptive table,\n"
                + ".es-left,\n"
                + ".es-right {\n"
                + "    width: 100%!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-content table,\n"
                + ".es-header table,\n"
                + ".es-footer table,\n"
                + ".es-content,\n"
                + ".es-footer,\n"
                + ".es-header {\n"
                + "    width: 100%!important;\n"
                + "    max-width: 600px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-adapt-td {\n"
                + "    display: block!important;\n"
                + "    width: 100%!important;\n"
                + "  }\n"
                + "\n"
                + "  .adapt-img {\n"
                + "    width: 100%!important;\n"
                + "    height: auto!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p0 {\n"
                + "    padding: 0!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p0r {\n"
                + "    padding-right: 0!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p0l {\n"
                + "    padding-left: 0!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p0t {\n"
                + "    padding-top: 0!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p0b {\n"
                + "    padding-bottom: 0!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p20b {\n"
                + "    padding-bottom: 20px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-mobile-hidden,\n"
                + ".es-hidden {\n"
                + "    display: none!important;\n"
                + "  }\n"
                + "\n"
                + "  tr.es-desk-hidden,\n"
                + "td.es-desk-hidden,\n"
                + "table.es-desk-hidden {\n"
                + "    width: auto!important;\n"
                + "    overflow: visible!important;\n"
                + "    float: none!important;\n"
                + "    max-height: inherit!important;\n"
                + "    line-height: inherit!important;\n"
                + "  }\n"
                + "\n"
                + "  tr.es-desk-hidden {\n"
                + "    display: table-row!important;\n"
                + "  }\n"
                + "\n"
                + "  table.es-desk-hidden {\n"
                + "    display: table!important;\n"
                + "  }\n"
                + "\n"
                + "  td.es-desk-menu-hidden {\n"
                + "    display: table-cell!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-menu td {\n"
                + "    width: 1%!important;\n"
                + "  }\n"
                + "\n"
                + "  table.es-table-not-adapt,\n"
                + ".esd-block-html table {\n"
                + "    width: auto!important;\n"
                + "  }\n"
                + "\n"
                + "  table.es-social {\n"
                + "    display: inline-block!important;\n"
                + "  }\n"
                + "\n"
                + "  table.es-social td {\n"
                + "    display: inline-block!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-desk-hidden {\n"
                + "    display: table-row!important;\n"
                + "    width: auto!important;\n"
                + "    overflow: visible!important;\n"
                + "    max-height: inherit!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p5 {\n"
                + "    padding: 5px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p5t {\n"
                + "    padding-top: 5px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p5b {\n"
                + "    padding-bottom: 5px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p5r {\n"
                + "    padding-right: 5px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p5l {\n"
                + "    padding-left: 5px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p10 {\n"
                + "    padding: 10px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p10t {\n"
                + "    padding-top: 10px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p10b {\n"
                + "    padding-bottom: 10px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p10r {\n"
                + "    padding-right: 10px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p10l {\n"
                + "    padding-left: 10px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p15 {\n"
                + "    padding: 15px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p15t {\n"
                + "    padding-top: 15px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p15b {\n"
                + "    padding-bottom: 15px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p15r {\n"
                + "    padding-right: 15px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p15l {\n"
                + "    padding-left: 15px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p20 {\n"
                + "    padding: 20px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p20t {\n"
                + "    padding-top: 20px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p20r {\n"
                + "    padding-right: 20px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p20l {\n"
                + "    padding-left: 20px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p25 {\n"
                + "    padding: 25px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p25t {\n"
                + "    padding-top: 25px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p25b {\n"
                + "    padding-bottom: 25px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p25r {\n"
                + "    padding-right: 25px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p25l {\n"
                + "    padding-left: 25px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p30 {\n"
                + "    padding: 30px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p30t {\n"
                + "    padding-top: 30px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p30b {\n"
                + "    padding-bottom: 30px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p30r {\n"
                + "    padding-right: 30px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p30l {\n"
                + "    padding-left: 30px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p35 {\n"
                + "    padding: 35px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p35t {\n"
                + "    padding-top: 35px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p35b {\n"
                + "    padding-bottom: 35px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p35r {\n"
                + "    padding-right: 35px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p35l {\n"
                + "    padding-left: 35px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p40 {\n"
                + "    padding: 40px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p40t {\n"
                + "    padding-top: 40px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p40b {\n"
                + "    padding-bottom: 40px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p40r {\n"
                + "    padding-right: 40px!important;\n"
                + "  }\n"
                + "\n"
                + "  .es-m-p40l {\n"
                + "    padding-left: 40px!important;\n"
                + "  }\n"
                + "}\n"
                + "</style>\n"
                + " </head>\n"
                + " <body style=\"width:100%;font-family:arial, 'helvetica neue', helvetica, sans-serif;-webkit-text-size-adjust:100%;-ms-text-size-adjust:100%;padding:0;Margin:0\">\n"
                + "  <div class=\"es-wrapper-color\" style=\"background-color:#102B3F\"><!--[if gte mso 9]>\n"
                + "			<v:background xmlns:v=\"urn:schemas-microsoft-com:vml\" fill=\"t\">\n"
                + "				<v:fill type=\"tile\" color=\"#102b3f\"></v:fill>\n"
                + "			</v:background>\n"
                + "		<![endif]-->\n"
                + "   <table class=\"es-wrapper\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px;padding:0;Margin:0;width:100%;height:100%;background-repeat:repeat;background-position:center top;background-color:#102B3F\">\n"
                + "     <tr>\n"
                + "      <td valign=\"top\" style=\"padding:0;Margin:0\">\n"
                + "       <table cellpadding=\"0\" cellspacing=\"0\" class=\"es-header\" align=\"center\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px;table-layout:fixed !important;width:100%;background-color:transparent;background-repeat:repeat;background-position:center top\">\n"
                + "         <tr>\n"
                + "          <td align=\"center\" style=\"padding:0;Margin:0\">\n"
                + "           <table bgcolor=\"#ffffff\" class=\"es-header-body\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px;background-color:#102B3F;width:600px\">\n"
                + "             <tr>\n"
                + "              <td class=\"es-m-p20r es-m-p20l\" align=\"left\" style=\"padding:0;Margin:0;padding-top:20px\">\n"
                + "               <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px\">\n"
                + "                 <tr>\n"
                + "                  <td class=\"es-m-p0r\" valign=\"top\" align=\"center\" style=\"padding:0;Margin:0;width:600px\">\n"
                + "                   <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" role=\"presentation\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px\">\n"
                + "                     <tr>\n"
                + "                      <td align=\"center\" class=\"es-m-txt-c\" style=\"padding:0;Margin:0;font-size:0px\"><a target=\"_blank\" href=\"https://viewstripo.email\" style=\"-webkit-text-size-adjust:none;-ms-text-size-adjust:none;mso-line-height-rule:exactly;text-decoration:underline;color:#E2CFEA;font-size:14px\"><img src=\"https://gitdqd.stripocdn.email/content/guids/CABINET_9205f05daca61de786ff466fa2baaf2b822bd2d7a16aa4f200b777462753f1b9/images/no_background_logo_1.png\" alt=\"Logo\" style=\"display:block;border:0;outline:none;text-decoration:none;-ms-interpolation-mode:bicubic\" title=\"Logo\" height=\"200\"></a></td>\n"
                + "                     </tr>\n"
                + "                   </table></td>\n"
                + "                 </tr>\n"
                + "               </table></td>\n"
                + "             </tr>\n"
                + "           </table></td>\n"
                + "         </tr>\n"
                + "       </table>\n"
                + "       <table class=\"es-content\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px;table-layout:fixed !important;width:100%\">\n"
                + "         <tr>\n"
                + "          <td align=\"center\" style=\"padding:0;Margin:0;background-image:url(https://gitdqd.stripocdn.email/content/guids/CABINET_6306d45fd9ea3b681ebe3a603101f0275312c7c136d6957f7ed43fa4b22490f7/images/frame_375_tsP.png);background-repeat:no-repeat;background-position:center top\" background=\"https://gitdqd.stripocdn.email/content/guids/CABINET_6306d45fd9ea3b681ebe3a603101f0275312c7c136d6957f7ed43fa4b22490f7/images/frame_375_tsP.png\">\n"
                + "           <table class=\"es-content-body\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px;background-color:transparent;width:600px\">\n"
                + "             <tr>\n"
                + "              <td class=\"es-m-p10b es-m-p20r es-m-p20l\" align=\"left\" style=\"padding:0;Margin:0;padding-top:20px\">\n"
                + "               <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px\">\n"
                + "                 <tr>\n"
                + "                  <td class=\"es-m-p0r es-m-p20b\" valign=\"top\" align=\"center\" style=\"padding:0;Margin:0;width:600px\">\n"
                + "                   <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" role=\"presentation\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px\">\n"
                + "                     <tr>\n"
                + "                      <td align=\"center\" class=\"es-m-txt-c\" style=\"padding:0;Margin:0\"><h1 style=\"Margin:0;line-height:55px;mso-line-height-rule:exactly;font-family:'Barlow Condensed', Arial, sans-serif;font-size:46px;font-style:normal;font-weight:normal;color:#E2CFEA\">Welcome to&nbsp;</h1></td>\n"
                + "                     </tr>\n"
                + "                     <tr>\n"
                + "                      <td align=\"center\" style=\"padding:0;Margin:0;font-size:0px\"><img class=\"adapt-img\" src=\"https://gitdqd.stripocdn.email/content/guids/CABINET_9205f05daca61de786ff466fa2baaf2b822bd2d7a16aa4f200b777462753f1b9/images/title_1.png\" alt=\"\" style=\"display:block;border:0;outline:none;text-decoration:none;-ms-interpolation-mode:bicubic\" width=\"300\"></td>\n"
                + "                     </tr>\n"
                + "                   </table></td>\n"
                + "                 </tr>\n"
                + "               </table></td>\n"
                + "             </tr>\n"
                + "             <tr>\n"
                + "              <td class=\"es-m-p20r es-m-p20l\" align=\"left\" style=\"padding:0;Margin:0;padding-top:30px;padding-bottom:30px\">\n"
                + "               <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px\">\n"
                + "                 <tr>\n"
                + "                  <td align=\"center\" valign=\"top\" style=\"padding:0;Margin:0;width:600px\">\n"
                + "                   <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:separate;border-spacing:0px;border-radius:10px\" role=\"presentation\">\n"
                + "                     <tr>\n"
                + "                      <td align=\"center\" style=\"padding:0;Margin:0;padding-top:15px;padding-bottom:15px;font-size:0\">\n"
                + "                       <table border=\"0\" width=\"100%\" height=\"100%\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px\">\n"
                + "                         <tr>\n"
                + "                          <td style=\"padding:0;Margin:0;border-bottom:1px solid #ffffff;background:unset;height:1px;width:100%;margin:0px\"></td>\n"
                + "                         </tr>\n"
                + "                       </table></td>\n"
                + "                     </tr>\n"
                + "                     <tr>\n"
                + "                      <td align=\"center\" style=\"padding:0;Margin:0;padding-top:20px;padding-bottom:20px\"><p style=\"Margin:0;-webkit-text-size-adjust:none;-ms-text-size-adjust:none;mso-line-height-rule:exactly;font-family:Barlow, sans-serif;line-height:29px;color:#E2CFEA;font-size:19px\">Bonjour</p><p style=\"Margin:0;-webkit-text-size-adjust:none;-ms-text-size-adjust:none;mso-line-height-rule:exactly;font-family:Barlow, sans-serif;line-height:29px;color:#E2CFEA;font-size:19px\">Nous avons bien reçu votre paiement pour votre abonnement de transport et nous vous remercions de votre confiance.<br>Nous sommes heureux de vous compter parmi nos abonnés.</p><p style=\"Margin:0;-webkit-text-size-adjust:none;-ms-text-size-adjust:none;mso-line-height-rule:exactly;font-family:Barlow, sans-serif;line-height:29px;color:#E2CFEA;font-size:19px\">Votre paiement a été traité avec succès et votre abonnement sera actif à partir du aujourd'hui.&nbsp;<br>N'hésitez pas à nous contacter si vous avez des questions ou des préoccupations concernant votre abonnement de transport. Nous sommes là pour vous aider.<br><span style=\"color:#FF0000\">NB: si vous avez&nbsp;besoin de modifier ou supprimer votre abonnement vous&nbsp;pouvez&nbsp;soumettre&nbsp;une&nbsp; reclamation.</span></p><p style=\"Margin:0;-webkit-text-size-adjust:none;-ms-text-size-adjust:none;mso-line-height-rule:exactly;font-family:Barlow, sans-serif;line-height:29px;color:#E2CFEA;font-size:19px\"><br></p></td>\n"
                + "                     </tr>\n"
                + "                     <tr>\n"
                + "                      <td align=\"center\" style=\"padding:0;Margin:0;padding-top:15px;padding-bottom:15px;font-size:0\">\n"
                + "                       <table border=\"0\" width=\"100%\" height=\"100%\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px\">\n"
                + "                         <tr>\n"
                + "                          <td style=\"padding:0;Margin:0;border-bottom:1px solid #ffffff;background:unset;height:1px;width:100%;margin:0px\"></td>\n"
                + "                         </tr>\n"
                + "                       </table></td>\n"
                + "                     </tr>\n"
                + "                   </table></td>\n"
                + "                 </tr>\n"
                + "               </table></td>\n"
                + "             </tr>\n"
                + "             <tr>\n"
                + "              <td class=\"es-m-p20r es-m-p20l\" align=\"left\" style=\"padding:0;Margin:0;padding-bottom:40px\">\n"
                + "               <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px\">\n"
                + "                 <tr>\n"
                + "                  <td align=\"center\" valign=\"top\" style=\"padding:0;Margin:0;width:600px\">\n"
                + "                   <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" role=\"presentation\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px\">\n"
                + "                     <tr>\n"
                + "                      <td align=\"left\" class=\"es-m-txt-c\" style=\"padding:0;Margin:0;padding-bottom:20px\"><h1 style=\"Margin:0;line-height:55px;mso-line-height-rule:exactly;font-family:'Barlow Condensed', Arial, sans-serif;font-size:46px;font-style:normal;font-weight:normal;color:#E2CFEA\">Trailler de l'appllication</h1></td>\n"
                + "                     </tr>\n"
                + "                     <tr>\n"
                + "                      <td align=\"center\" style=\"padding:0;Margin:0\"><a target=\"_blank\" href=\"https://www.youtube.com/watch?v=tGgWxgt6eFw\" style=\"-webkit-text-size-adjust:none;-ms-text-size-adjust:none;mso-line-height-rule:exactly;text-decoration:underline;color:#E2CFEA;font-size:16px\"><img class=\"adapt-img\" src=\"https://gitdqd.stripocdn.email/content/guids/videoImgGuid/images/image16781818119571880.png\" alt=\"ue5 missed the train &quot;That Way!&quot;\" width=\"600\" title=\"ue5 missed the train &quot;That Way!&quot;\" style=\"display:block;border:0;outline:none;text-decoration:none;-ms-interpolation-mode:bicubic\"></a></td>\n"
                + "                     </tr>\n"
                + "                     <tr>\n"
                + "                      <td align=\"center\" style=\"padding:0;Margin:0;padding-top:15px;padding-bottom:15px;font-size:0\">\n"
                + "                       <table border=\"0\" width=\"100%\" height=\"100%\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px\">\n"
                + "                         <tr>\n"
                + "                          <td style=\"padding:0;Margin:0;border-bottom:1px solid #ffffff;background:unset;height:1px;width:100%;margin:0px\"></td>\n"
                + "                         </tr>\n"
                + "                       </table></td>\n"
                + "                     </tr>\n"
                + "                   </table></td>\n"
                + "                 </tr>\n"
                + "               </table></td>\n"
                + "             </tr>\n"
                + "             <tr>\n"
                + "              <td align=\"left\" style=\"padding:0;Margin:0;padding-left:20px;padding-right:20px\">\n"
                + "               <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px\">\n"
                + "                 <tr>\n"
                + "                  <td align=\"center\" valign=\"top\" style=\"padding:0;Margin:0;width:560px\">\n"
                + "                   <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" role=\"presentation\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px\">\n"
                + "                     <tr>\n"
                + "                      <td align=\"left\" style=\"padding:0;Margin:0\"><p style=\"Margin:0;-webkit-text-size-adjust:none;-ms-text-size-adjust:none;mso-line-height-rule:exactly;font-family:Barlow, sans-serif;line-height:24px;color:#E2CFEA;font-size:16px\">Z.I Chotrana II - B.P. 160 Pôle Technologique 2083 Cite El Ghazala Raoued Ariana, Tunisie</p></td>\n"
                + "                     </tr>\n"
                + "                   </table></td>\n"
                + "                 </tr>\n"
                + "               </table></td>\n"
                + "             </tr>\n"
                + "             <tr>\n"
                + "              <td align=\"left\" style=\"padding:0;Margin:0;padding-left:20px;padding-right:20px\">\n"
                + "               <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px\">\n"
                + "                 <tr>\n"
                + "                  <td align=\"center\" valign=\"top\" style=\"padding:0;Margin:0;width:560px\">\n"
                + "                   <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" role=\"presentation\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px\">\n"
                + "                     <tr>\n"
                + "                      <td align=\"left\" style=\"padding:0;Margin:0\"><!--[if mso]><a href=\"https://goo.gl/maps/QkXLqhrU8wmpVMjg8\" target=\"_blank\" hidden>\n"
                + "	<v:roundrect xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:w=\"urn:schemas-microsoft-com:office:word\" esdevVmlButton href=\"https://goo.gl/maps/QkXLqhrU8wmpVMjg8\" \n"
                + "                style=\"height:41px; v-text-anchor:middle; width:110px\" arcsize=\"0%\" stroke=\"f\"  fillcolor=\"#ece508\">\n"
                + "		<w:anchorlock></w:anchorlock>\n"
                + "		<center style='color:#ffffff; font-family:Barlow, sans-serif; font-size:15px; font-weight:400; line-height:15px;  mso-text-raise:1px'>adresse</center>\n"
                + "	</v:roundrect></a>\n"
                + "<![endif]--><!--[if !mso]><!-- --><span class=\"msohide es-button-border\" style=\"border-style:solid;border-color:#2CB543;background:#ece508;border-width:0px;display:inline-block;border-radius:0px;width:auto;mso-hide:all\"><a href=\"https://goo.gl/maps/QkXLqhrU8wmpVMjg8\" class=\"es-button\" target=\"_blank\" style=\"-webkit-text-size-adjust: none; -ms-text-size-adjust: none; mso-line-height-rule: exactly; color: #FFFFFF; font-size: 18px; display: inline-block; background: #ece508; border-radius: 0px; font-family: Barlow, sans-serif; font-weight: normal; font-style: normal; line-height: 22px; width: auto; text-align: center; padding: 10px 20px 10px 20px; border-color: #ece508; text-decoration: none; mso-style-priority: 100;\"><img src=\"https://gitdqd.stripocdn.email/content/guids/CABINET_595f0350707e46559a04ac0b3b386af335970c500f58231eacd959c3cb292c0c/images/icons8_google_maps_96px.png\" alt=\"icon\" width=\"16\" style=\"display:inline-block;border:0;outline:none;text-decoration:none;-ms-interpolation-mode:bicubic;vertical-align:middle;margin-right:10px\" align=\"absmiddle\">adresse</a></span><!--<![endif]--></td>\n"
                + "                     </tr>\n"
                + "                     <tr>\n"
                + "                      <td align=\"center\" height=\"40\" style=\"padding:0;Margin:0\"></td>\n"
                + "                     </tr>\n"
                + "                   </table></td>\n"
                + "                 </tr>\n"
                + "               </table></td>\n"
                + "             </tr>\n"
                + "           </table></td>\n"
                + "         </tr>\n"
                + "       </table>\n"
                + "       <table cellpadding=\"0\" cellspacing=\"0\" class=\"es-content\" align=\"center\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px;table-layout:fixed !important;width:100%\">\n"
                + "         <tr>\n"
                + "          <td align=\"center\" style=\"padding:0;Margin:0\">\n"
                + "           <table bgcolor=\"#102b3f\" class=\"es-content-body\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px;background-color:#102B3F;width:600px\">\n"
                + "             <tr>\n"
                + "              <td class=\"es-m-p20r es-m-p20l\" align=\"left\" style=\"padding:0;Margin:0;padding-top:30px;padding-bottom:30px\">\n"
                + "               <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px\">\n"
                + "                 <tr>\n"
                + "                  <td align=\"center\" valign=\"top\" style=\"padding:0;Margin:0;width:600px\">\n"
                + "                   <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:separate;border-spacing:0px;border-radius:10px\">\n"
                + "                     <tr>\n"
                + "                      <td align=\"center\" style=\"padding:0;Margin:0;display:none\"></td>\n"
                + "                     </tr>\n"
                + "                   </table></td>\n"
                + "                 </tr>\n"
                + "               </table></td>\n"
                + "             </tr>\n"
                + "           </table></td>\n"
                + "         </tr>\n"
                + "       </table>\n"
                + "       <table cellpadding=\"0\" cellspacing=\"0\" class=\"es-footer\" align=\"center\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px;table-layout:fixed !important;width:100%;background-color:transparent;background-repeat:repeat;background-position:center top\">\n"
                + "         <tr>\n"
                + "          <td align=\"center\" style=\"padding:0;Margin:0\">\n"
                + "           <table bgcolor=\"#f7fff7\" class=\"es-footer-body\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px;background-color:#102B3F;width:600px\">\n"
                + "             <tr>\n"
                + "              <td align=\"left\" style=\"Margin:0;padding-left:20px;padding-right:20px;padding-top:40px;padding-bottom:40px\">\n"
                + "               <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px\">\n"
                + "                 <tr>\n"
                + "                  <td align=\"center\" valign=\"top\" style=\"padding:0;Margin:0;width:560px\">\n"
                + "                   <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px\">\n"
                + "                     <tr>\n"
                + "                      <td align=\"center\" style=\"padding:0;Margin:0;display:none\"></td>\n"
                + "                     </tr>\n"
                + "                   </table></td>\n"
                + "                 </tr>\n"
                + "               </table></td>\n"
                + "             </tr>\n"
                + "           </table></td>\n"
                + "         </tr>\n"
                + "       </table>\n"
                + "       <table cellpadding=\"0\" cellspacing=\"0\" class=\"es-content\" align=\"center\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px;table-layout:fixed !important;width:100%\">\n"
                + "         <tr>\n"
                + "          <td class=\"es-info-area\" align=\"center\" style=\"padding:0;Margin:0\">\n"
                + "           <table class=\"es-content-body\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px;background-color:transparent;width:600px\" bgcolor=\"#102b3f\">\n"
                + "             <tr>\n"
                + "              <td align=\"left\" style=\"padding:20px;Margin:0\">\n"
                + "               <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px\">\n"
                + "                 <tr>\n"
                + "                  <td align=\"left\" style=\"padding:0;Margin:0;width:560px\">\n"
                + "                   <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"mso-table-lspace:0pt;mso-table-rspace:0pt;border-collapse:collapse;border-spacing:0px\">\n"
                + "                     <tr>\n"
                + "                      <td align=\"center\" style=\"padding:0;Margin:0;display:none\"></td>\n"
                + "                     </tr>\n"
                + "                   </table></td>\n"
                + "                 </tr>\n"
                + "               </table></td>\n"
                + "             </tr>\n"
                + "           </table></td>\n"
                + "         </tr>\n"
                + "       </table></td>\n"
                + "     </tr>\n"
                + "   </table>\n"
                + "  </div>\n"
                + " </body>\n"
                + "</html>";
        emailMessage.setSubject("Payment Successfull");
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(htmlContent, "text/html");

        // Create a multipart message to include the message body part
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // Set the content of the message to be the multipart message
        emailMessage.setContent(multipart);

        Thread mailThread = new Thread(() -> {
            try {
                // send the message
                Transport.send(emailMessage);
            } catch (MessagingException ex) {
                Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        mailThread.start();
    }

    
    
    
    
    



    @FXML
    public void availableBusReset() {

        idabn.setText("");
        idclient.setText("");
        typeabn.getSelectionModel().clearSelection();
        Datedeb.setValue(null);
        Duree.getSelectionModel().clearSelection();

    }
    private String[] statusList1 = {"3 Mois","6 Mois","1 Ans"};

    public void comboBoxStatus1() {

        List<String> listS = new ArrayList<>();

        for (String data : statusList1) {
            listS.add(data);
        }

        ObservableList listStatus1 = FXCollections.observableArrayList(listS);
        Duree.setItems(listStatus1);

    }
    private String[] statusList = {"Silver","Gold","bronze"};

    public void comboBoxStatus() {

        List<String> listS = new ArrayList<>();

        for (String data : statusList) {
            listS.add(data);
        }

        ObservableList listStatus = FXCollections.observableArrayList(listS);
        typeabn.setItems(listStatus);

    }


    public void initialize(URL location, ResourceBundle resources) {


        comboBoxStatus();
        //availableBShowBusData();
        comboBoxStatus1();
       
        //busIdList();
        //LocationList();
   
        
    }
    
   
    

  

}

   




