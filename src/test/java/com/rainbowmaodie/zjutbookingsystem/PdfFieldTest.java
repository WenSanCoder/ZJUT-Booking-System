package com.rainbowmaodie.zjutbookingsystem;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class PdfFieldTest {
    public static void main(String[] args) throws Exception {
        PdfReader reader = new PdfReader("C:/Users/20979/IdeaProjects/ZJUT-Booking-System/src/main/empty.pdf");
        AcroFields fields = reader.getAcroFields();
        try (PrintWriter out = new PrintWriter(new FileWriter("C:/Users/20979/IdeaProjects/ZJUT-Booking-System/pdf_fields.txt"))) {
            for (String key : fields.getFields().keySet()) {
                out.println("Field: " + key);
            }
        }
        reader.close();
    }
}
