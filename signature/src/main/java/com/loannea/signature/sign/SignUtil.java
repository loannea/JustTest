package com.loannea.signature.sign;

import cn.aheca.api.pdf.AhcaPdfService;
import cn.aheca.api.pdf.dto.PdfSignInfo;
import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.ImgTemplate;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import com.loannea.signature.constant.DateCovertConstant;
import com.loannea.signature.constant.ErrorCodes;
import com.loannea.signature.entity.PointBo;
import com.loannea.signature.entity.SignBo;
import com.loannea.signature.entity.SignatureInfo;
import com.loannea.signature.exception.SignException;
import com.loannea.signature.util.Base64Tools;
import com.loannea.signature.util.PdfUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.svg.SVGDocument;
import sun.font.FontDesignMetrics;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * {@code SignUtil} 签章的工具类
 * <p>对pdf进行签名签批等功能</p>
 *  http://itext-general.2136553.n4.nabble.com/Itextshap-5-4-3-0-Creating-PDFStamper-show-error-quot-Append-mode-requires-a-document-without-errors-td4658900.html
 * 20200417 https://www.it1352.com/899322.html
 * @author zjw
 */
@Slf4j
public class SignUtil {

    @Autowired
    private MakeSignatureSinorock makeSignatureSinorock;

    /**
     * keystory密码
     */
    private static final char[] PASSWORD = "1".toCharArray();

    /**
     * {@code signBase} 签名 + 证书
     * @author zjw
     * @param signBo
     */
    private void signBase(SignBo signBo)
            throws Exception {
        String src = signBo.getSrc();
        SignatureInfo signatureInfo = signBo.getSignatureInfo();
        int signMode = signBo.getSignMode();
        List<PointBo> pointBos = signBo.getPointBos();
        String target = signBo.getTarget();
        // 定位信息
        List<Object[]> position = PdfUtils.getPosition(src, signatureInfo.getPositionText(), pointBos);
        float x = (Float) position.get(0)[0];
        float y = (Float) position.get(0)[1];
        // 如果有多页每页都填充
        for (Object[] e : position) {
            int page = (Integer) e[2];
            try (InputStream inputStream = new FileInputStream(src)) {
                ByteArrayOutputStream tempArrayOutputStream = new ByteArrayOutputStream();
                // 设置pdf 签名的属性
                PdfSignatureAppearance appearance = initPdfSignatureAppearance(inputStream,
                        tempArrayOutputStream, signatureInfo, signBo.isTextOnly(),signBo.isSpecial(),
                        x, y, page, signBo.getSignTime());

                if (signMode == 0) {
                    ExternalDigest digest = new BouncyCastleDigest();
                    // 签名算法
                    ExternalSignature signature = new PrivateKeySignature(signatureInfo.getPk(),
                            signatureInfo.getDigestAlgorithm(), null);

                    // 调用itext签名方法完成pdf签章 //数字签名格式，CMS,CADE
                    MakeSignature.signDetached(appearance, digest, signature,
                            signatureInfo.getChain(), null, null, null, 0, CryptoStandard.CMS);
                } else {
                    // 事件证书
                    makeSignatureSinorock.signDetachedBySinorock(appearance);
                }
                // 迁移
                byte[] byteArray = tempArrayOutputStream.toByteArray();
                PdfUtils.getFileByBytes(byteArray, target);
                PdfUtils.moveFile(src, target);

                // 进行验证
                PdfReader pdfReader = PdfUtils.vertifyPdf(target);
                PdfUtils.closePdfReader(pdfReader);
            }
        }
    }

    /**
     * {@code initPdfSignatureAppearance} 初始化设置签名信心
     * @author zjw
     * @param inputStream  原始流2
     * @param tempArrayOutputStream 临时流
     * @param signatureInfo  签名实体
     * @param isTextOnly  是否只有文字
     * @param x x
     * @param y y
     * @param page 页数
     * @param signTime
     * @return com.itextpdf.text.pdf.PdfSignatureAppearance
     */
    private PdfSignatureAppearance initPdfSignatureAppearance(InputStream inputStream,
                                                              ByteArrayOutputStream tempArrayOutputStream,
                                                              SignatureInfo signatureInfo,
                                                              boolean isTextOnly,
                                                              boolean isSpecial,
                                                              float x,
                                                              float y, int page, String signTime)
            throws Exception {

        log.info(
                "============================================初始化设置签名信息开始========================================================================");

        PdfReader reader = new PdfReader(inputStream);
        // zjw
        // 最后一个boolean参数是否允许被追加签名;false的话，pdf文件只允许被签名一次，多次签名，最后一次有效;true的话，pdf可以被追加签名，验签工具可以识别出每次签名之后文档是否被修改
        // zjw
        // http://itext-general.2136553.n4.nabble.com/Itextshap-5-4-3-0-Creating-PDFStamper-show-error-quot-Append-mode-requires-a-document-without-errors-td4658900.html
        PdfStamper stamper = PdfStamper.createSignature(reader, tempArrayOutputStream, '\u0000',
                null, false);
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason(signatureInfo.getReason());
        appearance.setLocation(signatureInfo.getLocation());
        Calendar calendar = Calendar.getInstance();
        appearance.setSignDate(calendar);
        // 设置证书
        appearance.setCertificationLevel(signatureInfo.getCertificationLevel());
        appearance.setRenderingMode(signatureInfo.getRenderingMode());

        Image image;

        // 如果是签字
        if (isTextOnly) {
            image = createTextImage(signatureInfo.getTextOnly(), stamper);
        } else {
            if (signatureInfo.isHasTime()) {
                // svg + 日期
                image = setFillContext(signatureInfo, stamper, signTime);
            } else {
                // svg
                image = setBaseFillContext(signatureInfo, stamper);
            }
        }

        appearance.setSignatureGraphic(image);
        com.itextpdf.text.Rectangle rect;

        // 用户直接输入文字的情况下
        if (isSpecial)
        {
            x = x - 38;
            y = y - 7;
            rect = new com.itextpdf.text.Rectangle(x, y, x + image.getScaledWidth(),
                    y + image.getScaledHeight(), 0);
        } else {
            rect = new com.itextpdf.text.Rectangle(x - image.getScaledWidth() / 2.0F,
                    y - image.getScaledHeight() / 2.0F, x + image.getScaledWidth() / 2.0F,
                    y + image.getScaledHeight() / 2.0F, 0);
        }
        String name = "sinorock::" + System.currentTimeMillis();
        appearance.setImageScale(1);
        appearance.setVisibleSignature(rect, page, name);
        appearance.setSignatureGraphic(image);
        rect.normalize();
        log.info(
                "============================================初始化设置签名信息结束========================================================================");
        return appearance;

    }

    /**
     * {@code signName} 签署名字
     *  <p>在指定的pdf文件中,根据文字定位在pdf上进行电子签名</p>
     * @author zjw
     * @param signBo
     * @throws Exception 异常
     */
    public void signName(SignBo signBo)
            throws Exception {
        signBase(signBo);
    }

    /**
     * {@code signTextEncodable} 签署意见
     * <p>在指定的pdf文件中,根据文字定位在pdf上将字写上去,并生成一份新的pdf文件</p>
     * @author zjw
     * @param src 输入文件路径
     * @param target 输出文件路径
     * @param textPosition 定位文字点
     * @param text 签署意见:已同意/拟同意/不同意
     * @param pointBos 偏移量
     * @throws Exception 可能异常,文件不存在
     */
    @Deprecated
    public void signText(String src, String target, String textPosition, String text,
                         List<PointBo> pointBos)
            throws Exception {
        try (InputStream inputStream = new FileInputStream(src)) {
            List<Object[]> position = PdfUtils.getPosition(src, textPosition, pointBos);
            float x = (Float) position.get(0)[0];
            float y = (Float) position.get(0)[1];
            int page = (Integer) position.get(0)[2];
            ByteArrayOutputStream tempArrayOutputStream = new ByteArrayOutputStream();
            PdfReader reader = new PdfReader(inputStream);
            PdfStamper stamper = new PdfStamper(reader, tempArrayOutputStream);
            BaseFont base = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.EMBEDDED);
            // 获取最底层的顶层
            PdfContentByte under = stamper.getOverContent(page);
            under.saveState();
            PdfGState gs = new PdfGState();
            under.setGState(gs);
            // 开始加入水印
            under.beginText();
            under.setFontAndSize(base, 15);
            under.setColorFill(com.itextpdf.text.BaseColor.BLACK);
            under.showTextAligned(Element.ALIGN_LEFT, text, x, y, 0);
            under.endText();
            // 添加水印文字结束
            under.setLineWidth(1f);
            under.stroke();
            stamper.close();
            byte[] byteArray = tempArrayOutputStream.toByteArray();
            String signTime = LocalDateTime.now().format(DateCovertConstant.simpleByChinese);
            PdfUtils.getFileByBytes(byteArray, target);
            PdfUtils.moveFile(src, target);
        } catch (Exception e) {
            log.error("签署意见失败：" + e.getMessage(), e);
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    /**
     * {@code signTextEncodable} 签署意见
     * <p>在指定的pdf文件中,根据文字定位在pdf上将字写上去,并生成一份新的pdf文件
     * 对 {@link  SignUtil#signText(String, String, String, String, List)}的升级
     * </p>
     * @author zjw
     * @param signBo
     * @throws Exception 可能异常,文件不存在
     */
    public void signTextSupportCovert(SignBo signBo)
            throws Exception {
        signBase(signBo);
    }

    /**
     * {@code setFillContext} 设置填充内容
     *  <p>生成一份带指定签名图片对象</p>
     * @author zjw
     * @param signatureInfo 签名信息实体
     * @return com.itextpdf.text.Image 图片对象
     * @throws IOException 异常
     * @throws BadElementException 异常
     */
    private Image setBaseFillContext(SignatureInfo signatureInfo, PdfStamper stamper)
            throws IOException,
            BadElementException,
            IOException {
        float width = 140;
        float height = 40;
        if ("G".equals(signatureInfo.getImgType())) {
            width = 150;
            height = 150;
        }
        final String parser = XMLResourceDescriptor.getXMLParserClassName();
        PdfContentByte pcb = stamper.getOverContent(1);
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        UserAgent userAgent = new UserAgentAdapter();
        DocumentLoader loader = new DocumentLoader(userAgent);
        BridgeContext ctx = new BridgeContext(userAgent, loader);
        ctx.setDynamicState(BridgeContext.STATIC);
        GVTBuilder builder = new GVTBuilder();

        PdfTemplate template = pcb.createTemplate(width, height);
        // 生成awt Graphics2D
        Graphics2D g2d = new PdfGraphics2D(template, width, height);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        String signUrl = signatureInfo.getImagePath().replace("\\", "\\\\");

        String url = "file:" + signUrl;
        SVGDocument svgDocument = factory.createSVGDocument(url);
        GraphicsNode graphNode = builder.build(ctx, svgDocument);
        // 画svg到画布
        graphNode.paint(g2d);
        g2d.dispose();
        @SuppressWarnings("UnnecessaryLocalVariable")
        ImgTemplate image = new ImgTemplate(template);
        return image;
    }

    /**
     * {@code setFillContext} 设置填充内容:图片+日期
     *  <p>生成一份带指定签名图片+日期的图片对象</p>
     * @author zjw
     * @param signatureInfo 签名信息实体
     * @param signTime 签名时间
     * @return com.itextpdf.text.Image 图片对象
     * @throws IOException 异常
     * @throws BadElementException 异常
     */
    private Image setFillContext(SignatureInfo signatureInfo, PdfStamper stamper, String signTime)
            throws IOException,
            DocumentException {
        // 读取图章图片
        float width = 120;
        float height = 40;
        final String parser = XMLResourceDescriptor.getXMLParserClassName();
        PdfContentByte pcb = stamper.getOverContent(1);
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        UserAgent userAgent = new UserAgentAdapter();
        DocumentLoader loader = new DocumentLoader(userAgent);
        BridgeContext ctx = new BridgeContext(userAgent, loader);
        ctx.setDynamicState(BridgeContext.STATIC);
        GVTBuilder builder = new GVTBuilder();

        // 获取要添加的字体信息
        if (Objects.isNull(signTime) || signTime.length() == 0) {
            signTime = LocalDateTime.now().format(DateCovertConstant.simpleByChinese);
        }
        Font font = new Font("宋体", Font.PLAIN, 15);
        FontDesignMetrics fontMetrics = FontDesignMetrics.getMetrics(font);
        int fontWidth = fontMetrics.stringWidth(signTime);
        int fontHeight = fontMetrics.getHeight();
        int totalHeight = (int) height + fontHeight;

        PdfTemplate template = pcb.createTemplate(width, totalHeight);
        // 生成awt Graphics2D
        Graphics2D g2d = new PdfGraphics2D(template, width, totalHeight, true);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        String signUrl = signatureInfo.getImagePath().replace("\\", "\\\\");

        String url = "file:" + signUrl;
        SVGDocument svgDocument = factory.createSVGDocument(url);
        GraphicsNode graphNode = builder.build(ctx, svgDocument);
        // 画svg到画布
        graphNode.paint(g2d);

        // 写字
        int dis = fontHeight - font.getSize();
        int x1 = ((int) width - fontWidth) / 2;
        int y1 = totalHeight - dis;
        g2d.setFont(font);
        g2d.setColor(new Color(0xff000000));

        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//         阴影
        g2d.setPaint(new Color(0, 0, 0, 64));
        g2d.drawString(signTime, x1, y1);

        // 正文
        g2d.setPaint(Color.BLACK);
        g2d.drawString(signTime, x1, y1);

        // 释放对象
        g2d.dispose();
        @SuppressWarnings("UnnecessaryLocalVariable")
        ImgTemplate image = new ImgTemplate(template);
        return image;
    }

    /**
     * {@code createTextImage} 纯文本创建图片信息
     * @author zjw
     * @param text 文字
     * @param stamper 编辑
     * @throws IOException 异常
     */
    private Image createTextImage(String text, PdfStamper stamper)
            throws Exception {

        Font font = new Font("宋体", Font.PLAIN, 15);
        FontDesignMetrics fontMetrics = FontDesignMetrics.getMetrics(font);
        int fontWidth = fontMetrics.stringWidth(text);
        int fontHeight = fontMetrics.getHeight();
//        int totalHeight = (int) height + fontHeight;

        float width = fontWidth;
        float height = fontHeight;

        PdfContentByte pcb = stamper.getOverContent(1);
        PdfTemplate template = pcb.createTemplate(width, height);
        // 生成awt Graphics2D
        Graphics2D g2d = new PdfGraphics2D(template, fontWidth, fontHeight, true);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 写字
        g2d.setFont(font);
        g2d.setColor(new Color(0xff000000));
        // 写字
        int dis = fontHeight - font.getSize();
        int x1 = 0;
        int y1 = (int) height - dis / 2;
        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//         阴影
        g2d.setPaint(new Color(0, 0, 0, 64));
        g2d.drawString(text, x1, y1);
        // 正文
        g2d.setPaint(Color.BLACK);
        g2d.drawString(text, x1, y1);
        // 释放对象
        g2d.dispose();
        @SuppressWarnings("UnnecessaryLocalVariable")
        ImgTemplate image = new ImgTemplate(template);
        return image;
    }

    /**
     * {@code setSignInfo} 设置签章信息
     * <p>设置签章</p>
     * @author zjw
     * @param certPath 证书路径
     * @param signPath 签章路径
     * @param isHasTime 是否待时间
     * @param positionText 定位文字
     * @param textOnly 是否只有文字
     * @param signMode  签名模式 0-服务器证书  1- 事件证书
     * @return net.sinorock.aj.modules.thirdparty.utils.SignatureInfo
     * @throws IOException 异常
     */
    public SignatureInfo getSignInfo(String certPath, String signPath, boolean isHasTime,
                                     String positionText, String textOnly, int signMode)
            throws Exception {

        SignatureInfo baseSignInfo = getBaseSignInfo(signPath, isHasTime, positionText, textOnly);

        if (signMode == 0) {
            Security.addProvider(new BouncyCastleProvider());
            KeyStore ks = KeyStore.getInstance("PKCS12");
            try {
//            FileChannel fileChannel = new RandomAccessFile(certPath, "rw").getChannel();
//            FileLock fileLock = fileChannel.tryLock();
                FileInputStream fileInputStream = new FileInputStream(certPath);
                ks.load(fileInputStream, PASSWORD);
                fileInputStream.close();
//            fileLock.release();
//            fileChannel.close();
//        } catch (OverlappingFileLockException e) {
//            throw new Exception("请稍等在试");
            } catch (IOException e) {
                String msg = e.getMessage();
                if (msg.contains("toDerInputStream rejects tag type 60")) {
                    throw new SignException(ErrorCodes.Writ.CERT_BAD_ERROR.getMsg(),
                            ErrorCodes.Writ.CERT_BAD_ERROR.getCode());
                }
            }
            Enumeration<String> enumeration = ks.aliases();
            String alias = null;
            PrivateKey pk = null;
            if (enumeration.hasMoreElements()) {
                alias = enumeration.nextElement();
                pk = (PrivateKey) ks.getKey(alias, PASSWORD);
                System.out.println(pk);
            }
            // 得到证书链
            Certificate[] chain = ks.getCertificateChain(alias);
            // noinspection UnnecessaryLocalVariable
            baseSignInfo.setChain(chain);
            baseSignInfo.setPk(pk);
        }
        return baseSignInfo;
    }

    /**
     * {@code setSignInfo} 设置签章信息
     * <p>设置签章</p>
     * @author zjw
     * @param isHasTime 是否待时间
     * @param signPath 签章路径
     * @param positionText 定位文字
     * @param textOnly 是否只有文字
     * @return net.sinorock.aj.modules.thirdparty.utils.SignatureInfo
     */
    public SignatureInfo getBaseSignInfo(String signPath, boolean isHasTime, String positionText,
                                         String textOnly) {
        SignatureInfo signInfo = SignatureInfo.builder().reason("安监执法系统").location(
                "安监执法系统").certificationLevel(PdfSignatureAppearance.NOT_CERTIFIED).positionText(
                positionText).hasTime(isHasTime).imagePath(signPath).renderingMode(
                PdfSignatureAppearance.RenderingMode.GRAPHIC).digestAlgorithm(
                DigestAlgorithms.SHA1).creator("安监执法系统").textOnly(textOnly).build();
        return signInfo;
    }

    /**
     * {@code vertify} 调用接口，实现已签章PDF完整性校验
     * @author zjw
     * @param pdfPath 待验证PDF路径
     * @return java.lang.Object
     */
    public List<PdfSignInfo> vertify(String pdfPath) {
        Map<String, Object> map = new HashMap<>(2);
        List<PdfSignInfo> list = new ArrayList<>();
        try {
            pdfPath = Base64Tools.encodeBase64File(pdfPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        map.put("pdfPath", pdfPath);
        Map<String, Object> returnMap2 = new AhcaPdfService().pdfSignVerify(map);
        // 仅当返回结果为200时，表示验证成功，
        if ("200".equals(returnMap2.get("code").toString())) {
            log.info("签名验证全部成功");
            System.out.println();
            list = (List<PdfSignInfo>) returnMap2.get("message");
            for (PdfSignInfo pdfSignInfo : list) {
                log.info("签名顺序的修订号:(int类型,表示该文档中的第几个签名)" + pdfSignInfo.getRevisionNumber());
                log.info("签名域:" + pdfSignInfo.getSignatureName());
                log.info("证书信息:" + pdfSignInfo.getBase64Cert());
                log.info("算法:" + pdfSignInfo.getDigestAlgorithm());
                log.info("签名原因:" + pdfSignInfo.getReason());
                log.info("签名地点:" + pdfSignInfo.getLocation());
                log.info("签名时间:" + pdfSignInfo.getSignDate());// Date类型
            }
        } else {
            log.info("验证失败:" + returnMap2.get("code"));
            log.info("验证失败:" + returnMap2.get("message"));
        }
        return list;
    }

    // 多线程测试
    public static void main(String[] args) {
        ExecutorService executorService = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        for (int threadNo = 0; threadNo < 10; threadNo++) {
            executorService.execute(new LoadCertInputStream());

        }
        executorService.shutdown();
    }

    public static class LoadCertInputStream implements Runnable {

        @Override
        public void run() {
            try {
//                SignUtil signUtil = new SignUtil();
//                getSignInfo("D:\\cert\\1.pfx", "D:\\cert\\1.svg",
//                        false, "", "",0 );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
