/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 *
 * @author Aziza
 */
public class PDFMaker {
    private String fileName;
    private Document document;
    private PdfWriter writer;

    public PDFMaker(String fileName) {
        this.fileName = fileName;
        this.document = new Document();
        try {
            writer = PdfWriter.getInstance(this.document, new FileOutputStream(fileName));
        } catch (FileNotFoundException | DocumentException ex) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("An error occurred");
            alert.setContentText(ex.toString());
            alert.showAndWait();

        }
    }

    public PDFMaker() {
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public PdfWriter getWriter() {
        return writer;
    }

    public void setWriter(PdfWriter writer) {
        this.writer = writer;
    }
    
    public void closePDF(){
        document.close();
    }
    
    
}