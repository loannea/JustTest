//import java.awt.*;
//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.security.PrivateKey;
//import java.security.cert.Certificate;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.apache.batik.bridge.BridgeContext;
//import org.apache.batik.bridge.DocumentLoader;
//import org.apache.batik.bridge.GVTBuilder;
//import org.apache.batik.bridge.UserAgent;
//import org.apache.batik.bridge.UserAgentAdapter;
//import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
//import org.apache.batik.gvt.GraphicsNode;
//import org.apache.batik.transcoder.Transcoder;
//import org.apache.batik.transcoder.TranscoderException;
//import org.apache.batik.transcoder.TranscoderInput;
//import org.apache.batik.transcoder.TranscoderOutput;
//import org.apache.batik.transcoder.image.PNGTranscoder;
//import org.apache.batik.util.XMLResourceDescriptor;
//import org.bouncycastle.asn1.esf.SignaturePolicyIdentifier;
//import org.w3c.dom.svg.SVGDocument;
//
//import com.itextpdf.awt.PdfGraphics2D;
//import com.itextpdf.text.BadElementException;
//import com.itextpdf.text.Image;
//import com.itextpdf.text.ImgTemplate;
//import com.itextpdf.text.pdf.PdfContentByte;
//import com.itextpdf.text.pdf.PdfDate;
//import com.itextpdf.text.pdf.PdfDictionary;
//import com.itextpdf.text.pdf.PdfName;
//import com.itextpdf.text.pdf.PdfReader;
//import com.itextpdf.text.pdf.PdfSignature;
//import com.itextpdf.text.pdf.PdfSignatureAppearance;
//import com.itextpdf.text.pdf.PdfStamper;
//import com.itextpdf.text.pdf.PdfString;
//import com.itextpdf.text.pdf.PdfTemplate;
//import com.itextpdf.text.pdf.security.BouncyCastleDigest;
//import com.itextpdf.text.pdf.security.DigestAlgorithms;
//import com.itextpdf.text.pdf.security.ExternalDigest;
//import com.itextpdf.text.pdf.security.ExternalSignature;
//import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
//import com.itextpdf.text.pdf.security.PdfPKCS7;
//import com.sinorock.signature.sign.MakeSignatureSinorock;
//
//import cn.aheca.api.pdf.PDFPdfPKCS7;
//import cn.aheca.api.pdf.PDFPrivateKeySignature;
//import cn.aheca.api.util.Base64;
//import cn.aheca.api.util.CertUtil;
//import cn.aheca.api.util.HexUtil;
//import lombok.extern.slf4j.Slf4j;
//import sun.font.FontDesignMetrics;
//
///**
// * {@code TestSign} TODO()
// * <p>TODO()</p>
// * @author zjw
// */
//@Slf4j
//public class TestCASign
//{
//
//
//
//
//    public static void main(String[] args)
//        throws Exception
//    {
//        String src = "C:\\Users\\DELL\\Desktop\\11\\1.pdf";
//        String target = "C:\\Users\\DELL\\Desktop\\11\\2-ca-svg.pdf";
//
//        float x = 400f;
//        float y = 200f;
//
//        String mode = "text";
//
//
//        try (InputStream inputStream = new FileInputStream(src))
//        {
//            OutputStream outputStream = new FileOutputStream(target);
//
//            log.info("========================处理基本信息=====================================");
//            PdfReader reader = new PdfReader(inputStream);
//            PdfStamper stamper = PdfStamper.createSignature(reader, outputStream, '\u0000', null,
//                true);
//            // 签章属性对象
//            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
//            appearance.setReason("setReason");
//            appearance.setLocation("setLocation");
////            Calendar calendar = Calendar.getInstance();
////            appearance.setSignDate(calendar);
//            Image image;
//            if ("svg".equals(mode))
//            {
////                image = getSvgImagO(stamper);
//                image = getSvgImag(stamper);
//            }
//            else if ("png".equals(mode))
//            {
//                String url = "C:\\Users\\DELL\\Desktop\\11\\1.png";
//                image = Image.getInstance(url);
//            }
//            else
//            {
//                image = getTextImage(stamper);
//            }
//
//            com.itextpdf.text.Rectangle rect;
//            rect = new com.itextpdf.text.Rectangle(x - image.getScaledWidth() / 2.0F,
//                y - image.getScaledHeight() / 2.0F, x + image.getScaledWidth() / 2.0F,
//                y + image.getScaledHeight() / 2.0F, 0);
//
//
//            String name = "111";
//
//            appearance.setImageScale(1);
//            appearance.setVisibleSignature(rect, 1, name);
//            appearance.setSignatureGraphic(image);
//            appearance.setCertificationLevel(0);
//            appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);
//            log.info("========================处理hash=====================================");
//            Collection<byte[]> crlBytes = null;
//            int estimatedSize = 8192;
//            CryptoStandard sigtype = CryptoStandard.CMS;
//            PdfSignature dic = new PdfSignature(PdfName.ADOBE_PPKLITE,
//                sigtype == CryptoStandard.CADES ? PdfName.ETSI_CADES_DETACHED : PdfName.ADBE_PKCS7_DETACHED);
//            dic.setReason(appearance.getReason());
//            dic.setLocation(appearance.getLocation());
//            dic.setSignatureCreator(appearance.getSignatureCreator());
//            dic.setContact(appearance.getContact());
//            dic.setDate(new PdfDate(appearance.getSignDate()));
//
//            appearance.setCryptoDictionary(dic);
//
//            HashMap<PdfName, Integer> exc = new HashMap();
//            exc.put(PdfName.CONTENTS, new Integer(estimatedSize * 2 + 2));
//            appearance.preClose(exc);
//
//
//            ExternalDigest externalDigest = new BouncyCastleDigest();
//            String hashAlgorithm = "SHA-1";
//            InputStream data = appearance.getRangeStream();
//
//
//            byte[] hash = DigestAlgorithms.digest(data,
//                externalDigest.getMessageDigest(hashAlgorithm));
//            byte[] ocsp = (byte[])null;
//            String digestAlgorithmOid = DigestAlgorithms.getAllowedDigests(hashAlgorithm);
//            byte[] sh = (new PDFPdfPKCS7()).getAuthenticatedAttributeBytes(hash, ocsp,
//                (Collection)crlBytes, sigtype, digestAlgorithmOid, externalDigest,
//                (SignaturePolicyIdentifier)null, (Certificate[])null);
//            String shHex = HexUtil.Byte2Hex(sh);
//
//            log.info(
//                "事件证书开始======================================================================");
//            Map<String, String> eventCert = MakeSignatureSinorock.getEventCert(shHex);
////            String signCert = (String)eventCert.get("signCert");
//            String signCert = (String)eventCert.get("signCert");
//            log.info("证书数据" + signCert);
//            String signData = (String)eventCert.get("signData");
//            log.info("签名结果数据" + signData);
//            // 获取签名数据
//            byte[] extSignature = Base64.decode(signData);
//            Certificate[] chain = (Certificate[])null;
//            CertUtil certUtil = new CertUtil();
//            chain = certUtil.returnChain(signCert);
//            appearance.setCertificate(chain[0]);
//            log.info(
//                "事件证书结束======================================================================");
//            String encryptionAlgorithm = certUtil.getEncryptionAlgorithm(Base64.decode(signCert));
//            String digestAlgorithm = "SHA1";
//            if (encryptionAlgorithm.equals("SM2"))
//            {
//                digestAlgorithm = "SM3";
//                hashAlgorithm = "SM3";
//                digestAlgorithmOid = DigestAlgorithms.getAllowedDigests(hashAlgorithm);
//                (new PDFPdfPKCS7()).getAuthenticatedAttributeBytes(hash, ocsp,
//                    (Collection)crlBytes, sigtype, digestAlgorithmOid, externalDigest,
//                    (SignaturePolicyIdentifier)null, (Certificate[])null);
//            }
//
//            PdfPKCS7 sgn = new PdfPKCS7((PrivateKey)null, chain, hashAlgorithm, (String)null,
//                externalDigest, false);
//            ExternalSignature externalSignature = new PDFPrivateKeySignature(encryptionAlgorithm,
//                digestAlgorithm, (String)null);
//            sgn.setExternalDigest(extSignature, (byte[])null,
//                externalSignature.getEncryptionAlgorithm());
//            byte[] encodedSig = sgn.getEncodedPKCS7(hash, null, ocsp, (Collection)crlBytes,
//                sigtype);
//
//            if (estimatedSize < encodedSig.length)
//            {
//                throw new IOException("Not enough space");
//            }
//            byte[] paddedSig = new byte[estimatedSize];
//            System.arraycopy(encodedSig, 0, paddedSig, 0, encodedSig.length);
//            PdfDictionary dic2 = new PdfDictionary();
//            dic2.put(PdfName.CONTENTS, (new PdfString(paddedSig)).setHexWriting(true));
////            appearance.setCertificate(chain[0]);
//            appearance.close(dic2);
//        }
//
//    }
//
//
//
//
////    public static void main(String[] args)
////            throws Exception
////    {
////        String src = "C:\\Users\\DELL\\Desktop\\11\\1.pdf";
////
////        PdfTemplate template = null;
////        try (InputStream inputStream = new FileInputStream(src))
////        {
////            PdfReader reader = new PdfReader(src);
////            //新建一个PDF解析对象
////            PdfReaderContentParser parser = new PdfReaderContentParser(reader);
////            //包含了PDF页面的信息，作为处理的对象
////            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream("C:\\Users\\DELL\\Desktop\\11\\2.pdf"));
////            Image image;
////            String text = "你好";
////            Font font = new Font("宋体", Font.PLAIN, 15);
////            FontDesignMetrics fontMetrics = FontDesignMetrics.getMetrics(font);
////            int fontWidth = fontMetrics.stringWidth(text);
////            int fontHeight = fontMetrics.getHeight();
////
////            float width = fontWidth;
////            float height = fontHeight;
////
////            // PdfContentByte is an object containing the user positioned text and graphic contents of a page. It knows how to apply the proper font encoding
////            PdfContentByte pcb = stamper.getOverContent(1);
////            // 获取pdf模板
////            template = pcb.createTemplate(width, height);
////            // 获取画笔
////            Graphics2D g2d = new PdfGraphics2D(template, fontWidth, fontHeight, true);
////            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
////            // 写字
////            g2d.setFont(font);
////            g2d.setColor(new Color(0xff000000));
////            // 写字
////            int dis = fontHeight - font.getSize();
////            int x1 = 0;
////            int y1 = (int)height - dis / 2;
////            // 设置抗锯齿
////            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
////                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
////            // 阴影
////            g2d.setPaint(new Color(0, 0, 0, 64));
////            g2d.drawString(text, x1, y1);
////            // 正文
////            g2d.setPaint(Color.BLACK);
////            g2d.drawString(text, x1, y1);
////            // 释放对象
////            g2d.dispose();
////
////
////        }
////
////        ByteBuffer buf2 = template.getInternalBuffer();
////        byte[] buffer = buf2.getBuffer();
////        String str= new String (buffer);
////        log.info(str);
////
////        PdfUtils.getFileByBytes(buffer, "C:\\Users\\DELL\\Desktop\\11\\2.png");
////
////    }
//
//
//
//    private static Image getTextImage(PdfStamper stamper)
//            throws BadElementException, IOException {
//        Image image;
//        String text = "你好";
//        Font font = new Font("宋体", Font.PLAIN, 15);
//        FontDesignMetrics fontMetrics = FontDesignMetrics.getMetrics(font);
//        int fontWidth = fontMetrics.stringWidth(text);
//        int fontHeight = fontMetrics.getHeight();
//
//        float width = fontWidth;
//        float height = fontHeight;
//
//        // PdfContentByte is an object containing the user positioned text and graphic contents of a page. It knows how to apply the proper font encoding
//        PdfContentByte pcb = stamper.getOverContent(1);
//        // 获取pdf模板
//        PdfTemplate template = pcb.createTemplate(width, height);
//        // 获取画笔
//        Graphics2D g2d = new PdfGraphics2D(template, fontWidth, fontHeight, true);
//        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        // 写字
//        g2d.setFont(font);
//        g2d.setColor(new Color(0xff000000));
//        // 写字
//        int dis = fontHeight - font.getSize();
//        int x1 = 0;
//        int y1 = (int)height - dis / 2;
//        // 设置抗锯齿
//        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
//            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//        // 阴影
//        g2d.setPaint(new Color(0, 0, 0, 64));
//        g2d.drawString(text, x1, y1);
//        // 正文
//        g2d.setPaint(Color.BLACK);
//        g2d.drawString(text, x1, y1);
//        // 释放对象
//        g2d.dispose();
//
//
//
//
//        image = new ImgTemplate(template);
//
////        ImageOutputStream stream = null;
//
////        ImageWriter writer = getWriter(im, formatName);
//
////        image = Image.getInstance("C:\\Users\\DELL\\Desktop\\11\\2.png");
//        return image;
//    }
//
//
//
//    //svg转为png
//    public static void convertSvg2Png(File svg, File png) throws IOException, TranscoderException
//    {
//
//        InputStream in = new FileInputStream(svg);
//        OutputStream out = new FileOutputStream(png);
//        out = new BufferedOutputStream(out);
//
//        Transcoder transcoder = new PNGTranscoder();
//        try {
//            TranscoderInput input = new TranscoderInput(in);
//            try {
//                TranscoderOutput output = new TranscoderOutput(out);
//                transcoder.transcode(input, output);
//            } finally {
//                out.close();
//            }
//        } finally {
//            in.close();
//        }
//    }
//
//
//
//
//    private static Image getSvgImag(PdfStamper stamper)
//            throws IOException,
//            BadElementException, TranscoderException {
//        Image image;
//        String svg = "C:\\Users\\DELL\\Desktop\\11\\1.svg";
//        String png = "C:\\Users\\DELL\\Desktop\\11\\svg2png.png";
//        File svgfile = new File (svg);
//        File pngfile = new File (png);
//        convertSvg2Png(svgfile,pngfile);
//        image = Image.getInstance(png);
//        return image;
//    }
//
//
//    private static Image getSvgImagO(PdfStamper stamper)
//        throws IOException,
//        BadElementException
//    {
//        Image image;
//        String signUrl = "C:\\Users\\DELL\\Desktop\\11\\1.svg";
//        String url = "file:" + signUrl;
//        float width = 140;
//        float height = 40;
//        final String parser = XMLResourceDescriptor.getXMLParserClassName();
//        PdfContentByte pcb = stamper.getOverContent(1);
//        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
//        UserAgent userAgent = new UserAgentAdapter();
//        DocumentLoader loader = new DocumentLoader(userAgent);
//        BridgeContext ctx = new BridgeContext(userAgent, loader);
//        ctx.setDynamicState(BridgeContext.STATIC);
//        GVTBuilder builder = new GVTBuilder();
//
//        PdfTemplate template = pcb.createTemplate(width, height);
//        // 生成awt Graphics2D
//        Graphics2D g2d = new PdfGraphics2D(template, width, height);
//        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        SVGDocument svgDocument = factory.createSVGDocument(url);
//        GraphicsNode graphNode = builder.build(ctx, svgDocument);
//        // 画svg到画布
//        graphNode.paint(g2d);
//        g2d.dispose();
//
//
//
//        image = new ImgTemplate(template);
//        return image;
//    }
//}
