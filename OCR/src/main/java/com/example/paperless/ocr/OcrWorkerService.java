package com.example.paperless.ocr;

import lombok.extern.java.Log;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Log
public class OcrWorkerService {

    public String performOCR(File file) throws TesseractException {
        log.info("Starting OCR process for file: " + file.getAbsolutePath());

        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata/");

        try {
            String ocrResult = tesseract.doOCR(file);
            log.info("OCR process completed successfully for file: " + file.getAbsolutePath());
            return ocrResult;
        } catch (TesseractException e) {
            log.severe("Error during OCR process for file: " + file.getAbsolutePath() + ". " + e.getMessage());
            throw e;
        }
    }
}