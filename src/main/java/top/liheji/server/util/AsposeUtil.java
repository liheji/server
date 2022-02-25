package top.liheji.server.util;


import com.aspose.cells.Workbook;
import com.aspose.slides.Presentation;
import com.aspose.words.Document;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.regex.Pattern;

/**
 * @Time : 2022/2/21 15:50
 * @Author : Galaxy
 * @Create : IdeaJ
 * @Project : serverPlus
 * @Description :
 */
@Slf4j
public class AsposeUtil {
    public static boolean transToPdf(String sourcePath, String pdfPath) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(pdfPath);
            return transToPdf(sourcePath, out);
        } catch (Exception e) {
            return false;
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    public static boolean transToPdf(String sourcePath, OutputStream pdfStream) {
        File file = new File(sourcePath);
        if (!file.exists()) {
            return false;
        } else {
            if (Pattern.matches(".*\\.docx?$", sourcePath)) {
                return wordToPdf(file, pdfStream);
            } else if (Pattern.matches(".*\\.xlsx?$", sourcePath)) {
                return excelToPdf(file, pdfStream);
            } else if (Pattern.matches(".*\\.pptx?$", sourcePath)) {
                return pptToPdf(file, pdfStream);
            } else {
                return false;
            }
        }
    }

    private static boolean wordToPdf(File file, OutputStream pdfStream) {
        InputStream in = null;
        Document doc;
        try {
            new com.aspose.words.License().setLicense();
            in = new FileInputStream(file);
            doc = new Document(in);
            doc.save(pdfStream, com.aspose.words.SaveFormat.PDF);
        } catch (Exception e) {
            log.error(e.toString());
            return false;
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(pdfStream);
        }

        return true;
    }

    private static boolean excelToPdf(File file, OutputStream pdfStream) {
        InputStream in = null;
        try {
            new com.aspose.cells.License().setLicense();
            in = new FileInputStream(file);
            Workbook wb = new Workbook(in);
            wb.save(pdfStream, com.aspose.cells.SaveFormat.PDF);
        } catch (Exception e) {
            log.error(e.toString());
            return false;
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(pdfStream);
        }

        return true;
    }

    private static boolean pptToPdf(File file, OutputStream pdfStream) {
        InputStream in = null;
        try {
            new com.aspose.slides.License().setLicense();
            in = new FileInputStream(file);
            Presentation pres = new Presentation(in);
            pres.save(pdfStream, com.aspose.slides.SaveFormat.Pdf);
        } catch (Exception e) {
            log.error(e.toString());
            return false;
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(pdfStream);
        }

        return true;
    }
}