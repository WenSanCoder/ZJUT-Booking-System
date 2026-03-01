package com.rainbowmaodie.zjutbookingsystem.common;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class PdfUtils {

    /**
     * 根据模板填充 PDF 文本域并生成新文件
     *
     * @param templatePath 模板路径
     * @param outputPath   输出路径
     * @param data         填充数据 Map
     * @throws IOException       IO异常
     * @throws DocumentException PDF处理异常
     */
    public void fillPdfTemplate(String templatePath, String outputPath, Map<String, String> data) throws IOException, DocumentException {
        PdfReader reader = null;
        PdfStamper stamper = null;
        try {
            // 确保输出目录存在
            File outFile = new File(outputPath);
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }

            reader = new PdfReader(templatePath);
            stamper = new PdfStamper(reader, new FileOutputStream(outputPath));
            AcroFields form = stamper.getAcroFields();

            // 设置中文字体 (使用 iText-Asian 提供的字体或系统字体)
            // 尝试加载 STSong-Light，UniGB-UCS2-H (iText-Asian)
            BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            
            // 重要：遍历并设置所有字段的字体，否则字段可能显示为空白
            for (String fieldName : form.getFields().keySet()) {
                form.setFieldProperty(fieldName, "textfont", bf, null);
            }

            // 填充数据
            for (Map.Entry<String, String> entry : data.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue() == null ? "" : entry.getValue();
                form.setField(key, value);
            }

            // 设置表单不可编辑
            stamper.setFormFlattening(true);
        } finally {
            if (stamper != null) {
                try {
                    stamper.close();
                } catch (Exception e) {
                    log.error("Error closing stamper", e);
                }
            }
            if (reader != null) {
                reader.close();
            }
        }
    }
}
