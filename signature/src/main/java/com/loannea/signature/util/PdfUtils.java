package com.loannea.signature.util;


import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.loannea.signature.entity.PointBo;
import com.loannea.signature.constant.ErrorCodes.Pdf;
import com.loannea.signature.exception.PdfException;
import com.loannea.signature.listener.PdfRenderListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import sun.misc.BASE64Encoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * {@code PdfUtils} pdf工具类
 * <p>对pdf文档进行解析的工具类</p>
 * @author zjw
 */
@Slf4j
public final class PdfUtils
{

    /**
     * {@code getPosition} 获取关键字的位置
     * <p>解析pdf文档,获取关键字的定位信息</p>
     * @author zjw
     * @param pdfPath pdf文档路径
     * @param key 关键字
     * @param ponits
     * @return java.util.List<java.lang.Object[]> 依次:x坐标,y坐标,第几页
     * @throws IOException 异常
     */
    public static List<Object[]> getPosition(String pdfPath, String key, List<PointBo> ponits)
        throws PdfException,
        IOException
    {
        PdfReader pdfReader = vertifyPdf(pdfPath);
        // 总页数
        int pageNum = pdfReader.getNumberOfPages();
        // 情况数据
        PdfRenderListener.clearKeyInfo();
        // 新建一个PDF解析对象
        PdfReaderContentParser parser = new PdfReaderContentParser(pdfReader);
        PdfRenderListener listener;
        List<Object[]> keysInfo = null;
        // 解析PDF，并处理里面的文字
        for (int i = 1; i <= pageNum; i++ )
        {
            listener = new PdfRenderListener(key, i, ponits);
            parser.processContent(i, listener);
            keysInfo = PdfRenderListener.getKeyInfo();
        }
        closePdfReader(pdfReader);
        return keysInfo;
    }

    public static PdfReader vertifyPdf(String target)
    {
        try
        {
            PdfReader pdfReader = new PdfReader(target);
            return pdfReader;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw new PdfException(Pdf.Pdf_ERROR.getMsg(), Pdf.Pdf_ERROR.getCode());
        }
    }

    public static void closePdfReader(PdfReader pdfReader)
    {
        if (Objects.nonNull(pdfReader))
        {
            pdfReader.close();
        }
    }

    /**
     * {@code getPdfSize} 获取pdf页数
     * @author zjw
     * @param pdfPath pdf路径
     * @return int 页数
     */
    public static int getPdfSize(String pdfPath)
        throws IOException
    {
        PdfReader pdfReader = vertifyPdf(pdfPath);
        int pageNum = pdfReader.getNumberOfPages();
        closePdfReader(pdfReader);
        return pageNum;
    }

    /**
     * {@code getPosition} 将Byte数组转换成文件  前提：该目录结构存在
     * <p>将Byte数组转换成文件</p>
     * @param bytes   输入的文件的字节数组
     * @param newFile 输出文件的路径
     * @throws IOException 异常
     * @author zjw
     */

    public static void getFileByBytes(byte[] bytes, String newFile)
        throws IOException
    {
        File file = new File(newFile);
        try (FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos))
        {
            bos.write(bytes);
            fos.flush();
        }

    }

    /**
    * {@code moveFile} 文件替换
    * <p>文件替换</p>
    * @author zjw
    * @param inFile 原始文件 old
    * @param outFile 目标文件 new
    * @throws IOException 异常
    */
    public static void moveFile(String inFile, String outFile)
        throws IOException
    {
        // 原始的文件 移动后路径
        File oldFile = new File(inFile);
        if (oldFile.exists())
        {
            oldFile.delete();
        }

        File newFile = new File(outFile);

        FileInputStream nfis = new FileInputStream(outFile);
        FileOutputStream ofos = new FileOutputStream(oldFile);
        // 定义byte数组
        byte[] date = new byte[512];
        // 判断是否读到文件末尾
        while ((nfis.read(date)) > 0)
        {
            // 写数据
            ofos.write(date);
        }
        // 关闭流
        nfis.close();
        // 关闭流
        ofos.close();

//        newFile.delete();

    }

    /**
     * {@code convertToImage} pdf 转图片
     * @author zjw
     * @param file 文件
     * @return java.util.List<java.awt.image.BufferedImage>
     * @throws IOException 异常
     */
    public static List<BufferedImage> convertToImage(File file)
        throws IOException
    {
        PDDocument document = PDDocument.load(file);
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        List<BufferedImage> bufferedImageList = new ArrayList<>();

        for (int page = 0; page < document.getNumberOfPages(); page++ )
        {
            BufferedImage img = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
            bufferedImageList.add(img);
        }
        document.close();

        return bufferedImageList;
    }

    /**
     * {@code convertToImage} pdf流 转图片
     * @author zjw
     * @param input 文件流
     * @return java.util.List<java.awt.image.BufferedImage>
     * @throws IOException 异常
     */
    public static List<BufferedImage> convertToImageByte(byte[] input)
        throws IOException
    {
        PDDocument document = PDDocument.load(input);
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        List<BufferedImage> bufferedImageList = new ArrayList<>();

        for (int page = 0; page < document.getNumberOfPages(); page++ )
        {
            BufferedImage img = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
            bufferedImageList.add(img);
        }
        document.close();

        return bufferedImageList;
    }

    /**
     * {@code concat} 多图片合成
     * @author zjw
     * @param images 多图片合成
     * @return java.awt.image.BufferedImage
     * @throws IOException 异常
     */
    public static BufferedImage concat(List<BufferedImage> images)
        throws IOException
    {
        int heightTotal = 0;
        for (BufferedImage bImg : images)
        {
            heightTotal += bImg.getHeight();
        }

        int heightCurr = 0;
        BufferedImage concatImage = new BufferedImage(images.get(0).getWidth(), heightTotal,
            BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = concatImage.createGraphics();
        for (BufferedImage bImg : images)
        {
            g2d.drawImage(bImg, 0, heightCurr, null);
            heightCurr += bImg.getHeight();
        }
        g2d.dispose();

        return concatImage;
    }

    /**
     * {@code createPdf} 创建pdf文档
     * @author zjw
     * @param images 图片
     * @param dest   目标pdf
     * @param imgText  图片文字
     * @return void
     * @throws IOException 异常
     */
    public static void createPdf(List<String> images, String dest, String imgText)
        throws IOException,
        DocumentException
    {

        Image img = Image.getInstance(images.get(0));
        Document document = new Document(img);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(dest));
        document.open();

        writer.setPageEvent(new PdfPageEventHelper()
        {
            @Override
            public void onEndPage(PdfWriter writer, Document document)
            {
                PdfContentByte cb = writer.getDirectContent();
                cb.saveState();
                cb.beginText();
                BaseFont bf = null;
                try
                {
                    bf = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", false);
                }
                catch (Exception e)
                {
                    throw new RuntimeException("字体创建出现异常！");
                }
                cb.setFontAndSize(bf, 10);
                // Footer
                float y = document.bottom(-20);
                cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
                    "第 " + writer.getPageNumber() + " 页", (document.right() + document.left()) / 2,
                    y, 0);
                cb.endText();
                cb.restoreState();
            }
        });

        PdfContentByte cb = writer.getDirectContent();

        for (String image : images)
        {

            img = Image.getInstance(image);
            img.scaleToFit(523, 770);
            float offsetX = (523 - img.getScaledWidth()) / 2;
            float offsetY = (770 - img.getScaledHeight());
            img.setAbsolutePosition(36 + offsetX, 36 + offsetY);

            document.setPageSize(PageSize.A4);
            document.newPage();
            document.add(img);
            //
            BaseFont bf = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", false);
            cb.beginText();
            cb.setFontAndSize(bf, 12);
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, imgText,
                (document.right() + document.left()) / 2, offsetY, 0);
            cb.endText();
        }
        document.close();
    }

    /**
     * {@code imgToPdf} 指定图片的某一页转pdf
     * @author zjw
     * @param images  图片转pdf
     * @param page 在第几页添加页码
     * @param fileName
     @throws IOException 异常
     */
    public static void imgToPdf(List<Image> images, int page, String path, String fileName)
        throws IOException,
        DocumentException
    {
        Image img = images.get(0);
        Document document = new Document(img);
        File dir = new File(path);
        if (!dir.exists())
        {
            FileUtils.forceMkdir(dir);
        }
        File file = new File(path + fileName);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        writer.setPageEvent(new PdfPageEventHelper()
        {
            @Override
            public void onEndPage(PdfWriter writer, Document document)
            {
                PdfContentByte cb = writer.getDirectContent();
                cb.saveState();
                cb.beginText();
                BaseFont bf = null;
                try
                {
                    bf = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", false);
                }
                catch (Exception e)
                {
                    throw new RuntimeException("字体创建出现异常！");
                }
                cb.setFontAndSize(bf, 10);
                // Footer
                float y = document.bottom(-20);
                if (writer.getPageNumber() > page)
                {
                    cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
                        "第 " + (writer.getPageNumber() - page) + " 页",
                        (document.right() + document.left()) / 2, y, 0);
                }

                cb.endText();
                cb.restoreState();
            }
        });

        PdfContentByte cb = writer.getDirectContent();
        for (int i = 0; i < images.size(); i++ )
        {
            images.get(i).scaleToFit(523, 770);
            float offsetX = (523 - images.get(i).getScaledWidth()) / 2;
            float offsetY = (770 - images.get(i).getScaledHeight());
            images.get(i).setAbsolutePosition(36 + offsetX, 36 + offsetY);

            document.setPageSize(PageSize.A4);
            document.newPage();
            document.add(images.get(i));
            //
            BaseFont bf = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", false);
            cb.beginText();
            cb.setFontAndSize(bf, 12);
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "",
                (document.right() + document.left()) / 2, offsetY, 0);
            cb.endText();
        }
        document.close();
    }


    /**
     * 多个图片合成 PDF文件
     *
     * @param images
     * @param page
     * @param path
     * @param fileName
     * @throws IOException
     * @throws DocumentException
     */
    public static void imgsToPdf(List<Image> images, String path, String fileName) throws IOException, DocumentException {
        Image img = images.get(0);
        Document document = new Document(img);
        File dir = new File(path);
        if (!dir.exists()) {
            FileUtils.forceMkdir(dir);
        }
        File file = new File(path + fileName);

        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        PdfContentByte cb = writer.getDirectContent();
        for (int i = 0; i < images.size(); i++) {
            images.get(i).scaleToFit(523, 770);
            float offsetX = (523 - images.get(i).getScaledWidth()) / 2;
            float offsetY = (770 - images.get(i).getScaledHeight());
            images.get(i).setAbsolutePosition(36 + offsetX, 36 + offsetY);

            document.setPageSize(PageSize.A4);
            document.newPage();
            document.add(images.get(i));
            //
            BaseFont bf = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", false);
            cb.beginText();
            cb.setFontAndSize(bf, 12);
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "",
                    (document.right() + document.left()) / 2, offsetY, 0);
            cb.endText();
        }
        document.close();
    }


    /**
     * {@code readFile} 读取文件
     * @author zjw
     * @param fileAbsolutePath 文件路径
     * @return byte[]
     * @throws IOException 异常
     */
    public static byte[] readFile(String fileAbsolutePath)
            throws IOException {
        File file = new File(fileAbsolutePath);
        if (!file.exists()) {
            return null;
        }
        byte[] bytes;
        try {
            log.debug("read file: {}", fileAbsolutePath);
            bytes = FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            log.debug("read file: {}", fileAbsolutePath);
            e.printStackTrace();
            throw e;
        }

        return bytes;
    }


    /**
     * {@code readFile} 读取文件
     * @author zjw
     * @param fileAbsolutePath 文件路径
     * @return byte[]
     * @throws IOException 异常
     */
    public static String readFileToBase64(String fileAbsolutePath)
            throws IOException {
        File file = new File(fileAbsolutePath);
        if (!file.exists()) {
            return null;
        }
        byte[] bytes;
        try {
            bytes = FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        return new BASE64Encoder().encode(bytes).replace("\n", "").replace("\r", "");
    }


    public static void main(String[] args)
        throws IOException,
        DocumentException
    {
        // demo定位
//        List<Object[]> position = PdfUtils.getPosition("F:\\cert\\test.pdf", "[Upt0]");
//        float x = (Float)position.get(0)[0];
//        float y = (Float)position.get(0)[1];
//        int page = (Integer)position.get(0)[2];
//        System.out.println(x);
//        System.out.println(y);
//        System.out.println(page);

        // demo 图片转pdf 写反了
//        String DEST = "C:\\Users\\zjw\\Desktop\\1.pdf";
////        List<BufferedImage> bufferedImages = convertToImage(new File(DEST));
////        int idex = 0;
////        for (BufferedImage img : bufferedImages)
////        {
////            ImageIOUtil.writeImage(img, "C:\\Users\\zjw\\Desktop\\" + (idex++ ) + ".jpg", 300);
////        }

        // 1 min
        String DEST = "d:\\aj-data\\file\\\\3202\\320200\\2020\\5\\other\\3.18环境测试嗷嗷.pdf";
        List<BufferedImage> bufferedImages = convertToImage(new File(DEST));
        int idex = 0;
        for (BufferedImage img : bufferedImages)
        {
            ImageIOUtil.writeImage(img, "C:\\Users\\Administrator\\Desktop\\\\22222\\" + (idex++) + ".png", 300);
        }


        // demo pdf转图片 写反了
//        String[] IMAGES = {"C:\\Users\\DELL\\Desktop\\0.jpg"};
//        String DEST = "C:\\Users\\DELL\\Desktop\\1.pdf";
//
//        File file = new File(DEST);
//        file.getParentFile().mkdirs();
//        createPdf(Arrays.asList(IMAGES), DEST, "");

    }
}