
package abonnement;

import Utils.PDFMaker;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import java.io.IOException;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableView;



public class AdminController implements Initializable {

    @FXML
    private Label label;




    @FXML
    private Button availableB_addBtn;


    @FXML
    private Button availableB_resetBtn;

  

    @FXML
    private TableView<abonData> availableB_tableView;

    @FXML
    private TextField availableB_search;

    @FXML
    private TableColumn<abonData, String> availableB_col_busID;

    @FXML
    private TableColumn<abonData, String> availableB_col_location;

    @FXML
    private TableColumn<abonData, String> availableB_col_status;

    @FXML
    private TableColumn<abonData, String> availableB_col_price;

    @FXML
    private TableColumn<abonData, String> availableB_col_date;

    


//    DATABASE TOOLS
    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;
    private Statement statement;
    @FXML
    private AnchorPane availableB_form;
    public Button admin;
    
    
    @FXML
    private Button availableB_deleteBtn;
    @FXML
    private Button availableB_updateBtn;
    @FXML
    private Button retour;
    @FXML
    private ComboBox<?> typeabn;
    @FXML
    private TextField idabn;
    @FXML
    private TextField idclient;
    @FXML
    private DatePicker Datedeb;
    @FXML
    private ComboBox<?> Duree;
    @FXML
    private Button pdfBtn;
    
    
   

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
                    || Duree.getSelectionModel().getSelectedItem() == null) {

                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Please fill all blank fields");
                alert.showAndWait();

            } else {

//                
                String check = "SELECT abonId FROM abonnement WHERE abonId = '"
                        + idabn.getText() + "'";

                statement = connect.createStatement();
                result = statement.executeQuery(check);

                if (result.next()) {

                    alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error Message");
                    alert.setHeaderText(null);
                    alert.setContentText("ID abn: " + idabn.getText() + " was already exist!");
                    alert.showAndWait();

                } else {

                    prepare = connect.prepareStatement(addData);
                    prepare.setString(1, idabn.getText());
                    prepare.setString(2, idclient.getText());
                    prepare.setString(3, (String)typeabn.getSelectionModel().getSelectedItem());
                    prepare.setString(4, String.valueOf(Datedeb.getValue()));
                    prepare.setString(5, (String)Duree.getSelectionModel().getSelectedItem());

                    prepare.executeUpdate();

                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Successfully Added!");
                    alert.showAndWait();

                    availableBShowBusData();
                    availableBusReset();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    @FXML
    public void availableBusReset() {

        idabn.setText("");
        idclient.setText("");
        typeabn.getSelectionModel().clearSelection();
        Datedeb.setValue(null);
        Duree.getSelectionModel().clearSelection();

    }
    private String[] statusList1  = {"3 Mois","6 Mois","1 Ans"};

    public void comboBoxStatus1() {

        List<String> listS = new ArrayList<>();

        for (String data : statusList1) {
            listS.add(data);
        }

        ObservableList listStatus = FXCollections.observableArrayList(listS);
        Duree.setItems(listStatus);

    }
    private String[] statusList = {"Silver","Gold","Bronze"};

    public void comboBoxStatus() {

        List<String> listS = new ArrayList<>();

        for (String data : statusList) {
            listS.add(data);
        }

        ObservableList listStatus = FXCollections.observableArrayList(listS);
        typeabn.setItems(listStatus);

    }

    public ObservableList<abonData> availableBusBusData() {

        ObservableList<abonData> busListData = FXCollections.observableArrayList();

        String sql = "SELECT * FROM abonnement";

        connect = AbonDB.connectDb();

        try {

            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();

            abonData busD;

            while (result.next()) {
                busD = new abonData(result.getInt("abonId"),
                        result.getInt("clientId"),
                        result.getString("type"),
                        result.getDate("dateDeb"),
                        result.getString("duree"));

                busListData.add(busD);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return busListData;

    }

    private ObservableList<abonData> availableBBusListData;

    public void availableBShowBusData() {

        availableBBusListData = availableBusBusData();

        availableB_col_busID.setCellValueFactory(new PropertyValueFactory<>("abonId"));
        availableB_col_location.setCellValueFactory(new PropertyValueFactory<>("clientId"));
        availableB_col_status.setCellValueFactory(new PropertyValueFactory<>("type"));
        availableB_col_price.setCellValueFactory(new PropertyValueFactory<>("dateDeb"));
        availableB_col_date.setCellValueFactory(new PropertyValueFactory<>("duree"));

        availableB_tableView.setItems(availableBBusListData);

    }

    @FXML
    public void avaialbleBSelectBusData() {

        abonData busD = availableB_tableView.getSelectionModel().getSelectedItem();
        int num = availableB_tableView.getSelectionModel().getSelectedIndex();

        if ((num - 1) < -1) {
            return;
        }

        idabn.setText(String.valueOf(busD.getAbonnId()));
        idclient.setText(String.valueOf(busD.getClientId()));
        Datedeb.setValue(LocalDate.parse(String.valueOf(busD.getDateDeb())));
       // availableB_status1.setValue(LocalDate.parse(String.valueOf(busD.getDuree())));

    }
    
    @FXML
    public void availableSearch(){
        
        FilteredList<abonData> filter = new FilteredList<>(availableBBusListData, e-> true);
        
        availableB_search.textProperty().addListener((Observable, oldValue, newValue) ->{
            
            filter.setPredicate(predicateabonDataBusData ->{
                
                if(newValue.isEmpty() || newValue == null){
                    return true;
                }
                
                String searchKey = newValue.toLowerCase();
//                NOTHING? THEN WE NEED TO DO THIS FIRST
                if(predicateabonDataBusData.getAbonnId().toString().contains(searchKey)){
//                    NOTE, IF INTEGER OR IF THE DATA TYPE IS NOT STRING, YOU MUST BE DO toString()
                    return true;
                }else if(predicateabonDataBusData.getClientId().toString().contains(searchKey)){
                    return true;
                }else if(predicateabonDataBusData.getType().contains(searchKey)){
                    return true;
                }else if(predicateabonDataBusData.getDuree().contains(searchKey)){
                    return true;
                }else if(predicateabonDataBusData.getDateDeb().toString().contains(searchKey)){
                    return true;
                }else return false;
                
            });
        });
        
        SortedList<abonData> sortList = new SortedList<>(filter);
        
        sortList.comparatorProperty().bind(availableB_tableView.comparatorProperty());
        availableB_tableView.setItems(sortList);
    }
    
    public void busIdList(){
        
        String busD = "SELECT * FROM abonnement WHERE type = 'Gold'";
        
        connect = AbonDB.connectDb();
        
        try{
            prepare = connect.prepareStatement(busD);
            result = prepare.executeQuery();
            
            ObservableList listB = FXCollections.observableArrayList();
            
            while(result.next()){
                
                listB.add(result.getString("abonId"));
            }

            
        }catch(Exception e){e.printStackTrace();}
        
    }
    
    public void LocationList(){
        
        String locationL = "SELECT * FROM abonnement WHERE type = 'Gold'";
        
        connect = AbonDB.connectDb();
        
        try{
            
            prepare = connect.prepareStatement(locationL);
            result = prepare.executeQuery();
            
            ObservableList listL = FXCollections.observableArrayList();
            
            while(result.next()){
                
                listL.add(result.getString("abonId"));
            }
            
  
            
        }catch(Exception e){e.printStackTrace();}
        
    }
    @FXML
    public void availableBusDelete(){
        
        String deleteData = "DELETE FROM abonnement WHERE abonId = '"
                +idabn.getText()+"'";
        
        connect = AbonDB.connectDb();
        
        try{
            
            Alert alert;
            
            if (idabn.getText().isEmpty()
                    || idclient.getText().isEmpty()
                    || typeabn.getSelectionModel().getSelectedItem() == null
                    || Datedeb.getValue() == null
                    || Duree.getSelectionModel().getSelectedItem() == null) {

                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Please select the item first");
                alert.showAndWait();

            } else {
                
                alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation Message");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to delete ID Abn: " + idabn.getText() + "?");
                
                Optional<ButtonType> option = alert.showAndWait();
                if(option.get().equals(ButtonType.OK)){
                    
                    statement = connect.createStatement();
                    statement.executeUpdate(deleteData);
                    
                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Successfully Deleted!");
                    alert.showAndWait();
                    
                    availableBShowBusData();
                    availableBusReset();
                    
                }else{
                    return;
                }
            }
//            NOW LETS PROCEED TO BOOKING TICKET : ) 
        }catch(Exception e){e.printStackTrace();}
        
    }

      @FXML
    public void availableBusUpdate() {

        String updateData = "UPDATE abonnement SET clientId = '"
                + idclient.getText() + "', type = '"
                + typeabn.getSelectionModel().getSelectedItem()
                + "', dateDeb = '" + Datedeb.getValue()
                + "', duree = '" + Duree.getSelectionModel().getSelectedItem()
                + "' WHERE abonId = '" + idabn.getText() + "'";

        connect = AbonDB.connectDb();

        Alert alert;

        try {

            if (idabn.getText().isEmpty()
                    || idclient.getText().isEmpty()
                    || typeabn.getSelectionModel().getSelectedItem() == null
                    || Datedeb.getValue()== null
                    || Duree.getSelectionModel().getSelectedItem() == null) {

                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Please select the item first");
                alert.showAndWait();

            } else {

                alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation Message");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to UPDATE Bus ID: " + idabn.getText() + "?");

                Optional<ButtonType> option = alert.showAndWait();

                if (option.get().equals(ButtonType.OK)) {

                    prepare = connect.prepareStatement(updateData);
                    prepare.executeUpdate();
                    
                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Successfully Updated!");
                    alert.showAndWait();

                    availableBShowBusData();
                    availableBusReset();
                    
                } else {
                    return;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
  
void retour(ActionEvent event) throws IOException {
    // Charger le fichier FXML de la nouvelle interface graphique
    FXMLLoader loader = new FXMLLoader(getClass().getResource("User.fxml"));
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



    public void initialize(URL location, ResourceBundle resources) {


        comboBoxStatus();
        availableBShowBusData();
        comboBoxStatus1();
        busIdList();
        LocationList();
   
        
    }

    @FXML
    private void pdfBtn(ActionEvent event) throws DocumentException, BadElementException, IOException {
        PDFMaker pdf = new PDFMaker( "listeabonnement.pdf");
        Document document = pdf.getDocument();
        PdfWriter writer = pdf.getWriter();

        document.open();

         Paragraph title0 = new Paragraph("That Way!", new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD));
         title0.setAlignment(Element.ALIGN_RIGHT);
         Paragraph title1 = new Paragraph("10/03/2023", new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD));
         title1.setAlignment(Element.ALIGN_LEFT);
         Paragraph title2 = new Paragraph("liste d'abonnement\n\n", new Font(Font.FontFamily.HELVETICA, 26, Font.BOLD));
         title2.setAlignment(Element.ALIGN_CENTER);
         
        
      
        
        PdfPTable table = new PdfPTable(5);
        PdfPCell header1 = new PdfPCell(new Phrase("id Abn"));
        table.addCell(header1);
        PdfPCell header2 = new PdfPCell(new Phrase("id client"));
        table.addCell(header2);
        PdfPCell header3 = new PdfPCell(new Phrase("Datedeb"));
        table.addCell(header3);
        PdfPCell header4 = new PdfPCell(new Phrase("duree"));
        table.addCell(header4);
        PdfPCell header5 = new PdfPCell(new Phrase("type"));
        table.addCell(header5);
        
        List<abonData> busListData = new ArrayList<>();
                String sql = "SELECT * FROM abonnement";

        connect = AbonDB.connectDb();

        try {

            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();

            abonData busD;

            while (result.next()) {
                busD = new abonData(result.getInt("abonId"),
                        result.getInt("clientId"),
                        result.getString("type"),
                        result.getDate("dateDeb"),
                        result.getString("duree"));

                busListData.add(busD);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        busListData.stream().map(entry -> {
            PdfPCell cell1 = new PdfPCell(new Phrase(entry.getAbonnId().toString()));
            table.addCell(cell1);
            PdfPCell cell2 = new PdfPCell(new Phrase(entry.getClientId().toString()) );
            table.addCell(cell2);
            PdfPCell cell3 = new PdfPCell(new Phrase(entry.getDateDeb().toString()));
            table.addCell(cell3);
            PdfPCell cell4 = new PdfPCell(new Phrase(entry.getDuree().toString()) );
            table.addCell(cell4);
            PdfPCell cell5 = new PdfPCell(new Phrase(entry.getType().toString()) );
            
            return cell5;
        }).forEachOrdered(cell2 -> {
            table.addCell(cell2);
        });

        PdfContentByte cb = writer.getDirectContent();
        cb.moveTo(document.leftMargin(), document.bottomMargin());
        cb.lineTo(document.right() - document.rightMargin(), document.bottomMargin());
        cb.stroke();

        Phrase footer = new Phrase("Abonnement information", new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL));
        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer, (document.right() - document.left()) / 2 + document.leftMargin(), document.bottom() - 10, 0);

        try {
            document.add(title0);
            document.add(title1);
            document.add(title2);
            
            document.add(table);
        } catch (DocumentException ex) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("An error occurred");
            alert.setContentText(ex.toString());
            alert.showAndWait();
        }

        pdf.closePDF();
        
        
    }
}



