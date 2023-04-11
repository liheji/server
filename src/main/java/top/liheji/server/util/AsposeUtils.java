package top.liheji.server.util;


import com.aspose.cells.Workbook;
import com.aspose.slides.Presentation;
import com.aspose.words.Document;
import com.aspose.words.FontSettings;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.regex.Pattern;

/**
 * @author : Galaxy
 * @time : 2022/2/21 15:50
 * @create : IdeaJ
 * @project : serverPlus
 * @description : Aspose工具类
 */
@Slf4j
public class AsposeUtils {
    public static boolean transToPdf(String sourcePath, String pdfPath) {
        try {
            @Cleanup OutputStream out = new FileOutputStream(pdfPath);
            return transToPdf(sourcePath, out);
        } catch (Exception e) {
            log.error("文件转换错误：" + e);
            return false;
        }
    }

    public static boolean transToPdf(String sourcePath, OutputStream pdfStream) {
        if (SysUtils.isLinux()) {
            FontSettings.getDefaultInstance().setFontsFolder("/usr/share/fonts/windows", true);
        }

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
        Document doc;
        try {
            new com.aspose.words.License().setLicense();
            @Cleanup InputStream in = new FileInputStream(file);
            doc = new Document(in);
            doc.save(pdfStream, com.aspose.words.SaveFormat.PDF);
        } catch (Exception e) {
            log.error("Word文件转换失败：" + e);
            return false;
        }

        return true;
    }

    private static boolean excelToPdf(File file, OutputStream pdfStream) {
        try {
            new com.aspose.cells.License().setLicense();
            @Cleanup InputStream in = new FileInputStream(file);
            Workbook wb = new Workbook(in);
            wb.save(pdfStream, com.aspose.cells.SaveFormat.PDF);
        } catch (Exception e) {
            log.error("Excel文件转换失败：" + e);
            return false;
        }

        return true;
    }

    private static boolean pptToPdf(File file, OutputStream pdfStream) {
        try {
            new com.aspose.slides.License().setLicense();
            @Cleanup InputStream in = new FileInputStream(file);
            Presentation pres = new Presentation(in);
            pres.save(pdfStream, com.aspose.slides.SaveFormat.Pdf);
        } catch (Exception e) {
            log.error("Ppt文件转换失败：" + e);
            return false;
        }

        return true;
    }
}