package com.rainbowmaodie.zjutbookingsystem.common;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
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
     * @param images       图片数据 Map (key: 文本域名称, value: 图片绝对路径)
     * @throws IOException       IO异常
     * @throws DocumentException PDF处理异常
     */
    public void fillPdfTemplate(String templatePath, String outputPath, Map<String, String> data, Map<String, String> images) throws IOException, DocumentException {
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

            // 设置中文字体
            BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            
            // 设置字段字体和清除边框
            for (String fieldName : form.getFields().keySet()) {
                form.setFieldProperty(fieldName, "textfont", bf, null);
                // 清除文本域边框和背景 (0 代表无边框)
                form.setFieldProperty(fieldName, "bordercolor", null, null);
                form.setFieldProperty(fieldName, "bgcolor", null, null);
            }

            // 填充文字数据
            for (Map.Entry<String, String> entry : data.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue() == null ? "" : entry.getValue();
                form.setField(key, value);
            }

            // 填充图片数据 (首先尝试通过字段定位，如果失败则尝试硬编码坐标)
            if (images != null) {
                for (Map.Entry<String, String> entry : images.entrySet()) {
                    String fieldName = entry.getKey();
                    String imagePath = entry.getValue();
                    if (imagePath == null || imagePath.isEmpty()) continue;

                    File imgFile = new File(imagePath);
                    if (imgFile.exists()) {
                        System.out.println("DEBUG: [PdfUtils] Rendering image via Hardcoded Coordinates for: " + fieldName);
                        Image image = Image.getInstance(imagePath);
                        
                        float x, y, w, h;
                        int pageNo = 1;

                        // 根据你提供的控制台日志进行硬编码坐标补偿
                        if ("Sign".equals(fieldName)) {
                            x = 405.934f;
                            y = 193.538f;
                            w = 157.54f;
                            h = 21.01f;
                            System.out.println("DEBUG: [PdfUtils] Using HARDCODED coords for Sign field.");
                        } else {
                            // 其他字段尝试动态获取
                            java.util.List<AcroFields.FieldPosition> positions = form.getFieldPositions(fieldName);
                            if (positions == null || positions.isEmpty()) continue;
                            AcroFields.FieldPosition pos = positions.get(0);
                            pageNo = pos.page;
                            x = pos.position.getLeft();
                            y = pos.position.getBottom();
                            w = pos.position.getWidth();
                            h = pos.position.getHeight();
                        }
                        
                        image.scaleToFit(w, h);
                        image.setAbsolutePosition(x + (w - image.getScaledWidth()) / 2, 
                                                y + (h - image.getScaledHeight()) / 2);
                        
                        PdfContentByte over = stamper.getOverContent(pageNo);
                        over.addImage(image);
                        System.out.println("DEBUG: [PdfUtils] Image FORCED onto canvas at x=" + x + ", y=" + y);
                    }
                }
            }

            // 特别注意：对于已经填充了图片的 PDF，Flattening 可能会导致显示问题
            // 我们在关闭前再次确保内容被刷新
            form.setGenerateAppearances(true); 
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
