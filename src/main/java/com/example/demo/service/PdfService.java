package com.example.demo.service;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class PdfService {

    public byte[] generatePdf() throws IOException {
        PDDocument pdDocument = new PDDocument();
        PDPage pdPage = new PDPage();
        PDFont pdfFont = PDType1Font.TIMES_ROMAN;
        int fontSize = 14;

        try (PDPageContentStream contentStream = new PDPageContentStream(pdDocument, pdPage, PDPageContentStream.AppendMode.APPEND, true, true)) {
            contentStream.setFont(pdfFont, fontSize);
            contentStream.beginText();
            contentStream.newLineAtOffset(200, 685);
            String signerName = "ThuanPL";
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String signText = String.join("", "Sign by: ", signerName, "Create Time: ",
                    dateFormat.format(new Date()));
            contentStream.showText(signText);
            contentStream.endText();
        }

        pdDocument.addPage(pdPage);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        pdDocument.save(byteArrayOutputStream);
        pdDocument.close();

        return byteArrayOutputStream.toByteArray();
    }

    public byte[] generatePdfwithImage(String path) throws IOException {
        ImagePlus image = IJ.openImage(path);

        Font font = new Font("Times New Roman", Font.BOLD, 18);

        ImageProcessor ip = image.getProcessor();
        ip.setColor(Color.ORANGE);
        ip.setFont(font);
        String signerName = "ThuanPL";
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String signText = String.join("", "Ký bởi: ", signerName, "\nThời gian ký: ",
                dateFormat.format(new Date()));

        ip.drawString(signText, 0, 100);
        BufferedImage rawImage = image.getBufferedImage();
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        ImageIO.write(rawImage, "png", baos);
        byte[] imageInByte=baos.toByteArray();
        return imageInByte;
    }
}

