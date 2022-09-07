package com.example.demo.controller;

import com.example.demo.service.PdfService;
import com.example.demo.service.SigningService;
import com.example.demo.utils.CreateVisibleSignature;
import com.example.demo.utils.ShowSignature;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.security.KeyStore;

@RestController
@RequestMapping("/api")
@Slf4j
public class ConverterController {

    private final PdfService pdfService;
    private final SigningService signingService;

    @Value("${keystore.path}")
    private String keyStorePath;
    @Value("${keystore.password}")
    private String keyStorePassword;
    @Value("${keystore.certificate-alias}")
    private String certificateAlias;
    @Value("${timestamp-authority.url}")
    private String tsaUrl;

    public ConverterController(PdfService pdfService, SigningService signingService) {
        this.pdfService = pdfService;
        this.signingService = signingService;
    }


    @RequestMapping(path = "", produces = "application/json")
    public String testConverter() {

        return "OK";
    }

    @GetMapping(value = "/export", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity exportPdf() {
        try {
            byte[] pdfToSign = this.pdfService.generatePdf();
            byte[] signedPdf = this.signingService.signPdf(pdfToSign);

            return ResponseEntity.ok(signedPdf);
        } catch (IOException e) {
            log.error("Cannot generate PDF file", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Tạo file ảnh chữ ký
     * @return
     */
    @GetMapping(value = "/exportImage", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity exportImage() {
        try {
            byte[] signatureImage = this.pdfService.generatePdfwithImage("src/test.jpg");

            return ResponseEntity.ok(signatureImage);
        } catch (Exception e) {
            log.error("Cannot generate image file", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/signwithImage", produces = MediaType.APPLICATION_JSON_VALUE)
    public String signwithImage() {
        try {

            //keystone signature
            File ksFile = new File(keyStorePath);
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            char[] pin = keyStorePassword.toCharArray();
            try (InputStream is = new FileInputStream(ksFile))
            {
                keystore.load(is, pin);
            }

            boolean externalSig = true;
            // file cần ký
            String pathPdf = "src/testpdf.pdf";

            byte[] signatureImage = this.pdfService.generatePdfwithImage("src/signature.png");
//            InputStream imageStream = new ByteArrayInputStream(signatureImage);
            File documentFile = new File(pathPdf);
            CreateVisibleSignature signing = new CreateVisibleSignature(keystore, pin.clone());

            File signedDocumentFile;
            int page;
            try (InputStream imageStream = new ByteArrayInputStream(signatureImage);)
            {
                String name = documentFile.getName();
                String substring = name.substring(0, name.lastIndexOf('.'));
                signedDocumentFile = new File(documentFile.getParent(), substring + "_signed.pdf");
                // page is 1-based here
                page = 1;
                signing.setVisibleSignDesigner(pathPdf, 0, 0, -50, imageStream, page);
            }
            signing.setVisibleSignatureProperties("Phan Luong Thuan", "Signer's office", "Signing document", 0, page, true);
            signing.setExternalSigning(externalSig);
            signing.signPDF(documentFile, signedDocumentFile, tsaUrl);

            return "OK đã ký";
        } catch (Exception e) {
            log.error("Cannot generate signwithImage file", e);
            return "Ngủm rồi :(((";
        }
    }

    @GetMapping(value = "/verifySignature", produces = MediaType.APPLICATION_JSON_VALUE)
    public String verifySignature() {
        try {
            String pdfPath = "src/check.pdf";
            File inputFile = new File(pdfPath);
            PDDocument document = Loader.loadPDF(inputFile);
            ShowSignature showSignature = new ShowSignature();
            showSignature.showSignaturePDF(pdfPath,null);
            return "OK done";
        } catch (Exception e) {
            return "Fail during processing :(((";
        }
    }

}
