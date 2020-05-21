package com.loannea.signature.sign;


import cn.aheca.api.pdf.PDFPdfPKCS7;
import cn.aheca.api.pdf.PDFPrivateKeySignature;
import cn.aheca.api.util.CertUtil;
import cn.aheca.api.util.HexUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.itextpdf.text.pdf.PdfDate;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfSignature;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.codec.Base64;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

/**
 * {@code MakeSignatureSinorock} 证书签名
 * @author zjw
 */
@Slf4j
@Data
public class MakeSignatureSinorock
{
    /**
     * 服务器url
     */
    private  String url;

    private  String secretKey;

    private  String uniqueId;

     Map<String,String> getEventCert(String hexStr)
        throws IOException
    {
        Map<String, String> map = new HashMap<>(16);
        map.put("uniqueId", this.uniqueId);
        map.put("secretKey", this.secretKey);

        map.put("strData", hexStr);
        map.put("dataType", "1");
        map.put("certCN","sinorock");

        String json = JSONObject.toJSONString(map, SerializerFeature.WriteNullStringAsEmpty);
        log.debug(json);
        String url =this.url;

        StringBuilder param = new StringBuilder();

        Set<Entry<String, String>> entries = map.entrySet();

        List<Entry<String, String>> lst = new ArrayList<>(entries);

        for (int n = 0, size = lst.size(); n < size; n++ )
        {
            Entry<String, String> e = lst.get(n);
            if (n == 0)
            {
                param.append("?");
            }
            param.append(e.getKey()).append("=").append(e.getValue());
            if (n != size - 1)
            {
                param.append("&");
            }
        }
        log.info(param.toString());
        url = url + param.toString();
        log.info(url);
        HttpClient httpClient;
        PostMethod postMethod;
        int statusCode;
        String response = "";
        httpClient = new HttpClient();
        log.debug(url);
        postMethod = new PostMethod(url);
        statusCode = httpClient.executeMethod(postMethod);
        if (statusCode == HttpStatus.SC_OK)
        {
            String tmp;
            // 读取返回报文
            StringBuilder resp = new StringBuilder();

            try (InputStream inputStream = postMethod.getResponseBodyAsStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader))
            {
                while ((tmp = bufferedReader.readLine()) != null)
                {
                    resp.append(tmp).append("\n");
                }
                postMethod.releaseConnection();
            }
            Map map1 = JSONObject.parseObject(resp.toString(), Map.class);
            return map1;
        }
        log.info(response);
        return null;
    }

    public  void signDetachedBySinorock(PdfSignatureAppearance appearance)
            throws Exception {
        log.info("========================处理hash=====================================");
        Collection<byte[]> crlBytes = null;
        int estimatedSize = 8192;
        CryptoStandard sigtype = CryptoStandard.CMS;
        PdfSignature dic = new PdfSignature(PdfName.ADOBE_PPKLITE,
                sigtype == CryptoStandard.CADES ? PdfName.ETSI_CADES_DETACHED : PdfName.ADBE_PKCS7_DETACHED);
        dic.setReason(appearance.getReason());
        dic.setLocation(appearance.getLocation());
        dic.setSignatureCreator(appearance.getSignatureCreator());
        dic.setContact(appearance.getContact());
        dic.setDate(new PdfDate(appearance.getSignDate()));

        appearance.setCryptoDictionary(dic);

        HashMap<PdfName, Integer> exc = new HashMap<>(2);
        exc.put(PdfName.CONTENTS, estimatedSize * 2 + 2);
        appearance.preClose(exc);

        ExternalDigest externalDigest = new BouncyCastleDigest();
        String hashAlgorithm = "SHA-1";
        InputStream data = appearance.getRangeStream();

        byte[] hash = DigestAlgorithms.digest(data,
                externalDigest.getMessageDigest(hashAlgorithm));
        byte[] ocsp = null;
        String digestAlgorithmOid = DigestAlgorithms.getAllowedDigests(hashAlgorithm);
        byte[] sh = new PDFPdfPKCS7().getAuthenticatedAttributeBytes(hash, ocsp,
                crlBytes, sigtype, digestAlgorithmOid, externalDigest,
                null, null);
        String shHex = HexUtil.Byte2Hex(sh);

        log.info("============================事件证书开始==========================================");
        Map<String, String> eventCert = this.getEventCert(shHex);
        String signCert = Objects.requireNonNull(eventCert).get("signCert");
        log.info("证书数据:" + signCert);
        String signData = eventCert.get("signData");
        log.info("签名结果数据:" + signData);
        // 获取签名数据
        byte[] extSignature = cn.aheca.api.util.Base64.decode(signData);
        Certificate[] chain = (Certificate[])null;
        CertUtil certUtil = new CertUtil();
        chain = certUtil.returnChain(signCert);
        appearance.setCertificate(chain[0]);
        log.info("==============================事件证书结束========================================");
        String encryptionAlgorithm = certUtil.getEncryptionAlgorithm(Base64.decode(signCert));
        String digestAlgorithm = "SHA1";
        if ("SM2".equals(encryptionAlgorithm))
        {
            digestAlgorithm = "SM3";
            hashAlgorithm = "SM3";
            digestAlgorithmOid = DigestAlgorithms.getAllowedDigests(hashAlgorithm);
            (new PDFPdfPKCS7()).getAuthenticatedAttributeBytes(hash, ocsp,
                    crlBytes, sigtype, digestAlgorithmOid, externalDigest,
                    null, null);
        }

        PdfPKCS7 sgn = new PdfPKCS7(null, chain, hashAlgorithm, null,
                externalDigest, false);
        ExternalSignature externalSignature = new PDFPrivateKeySignature(encryptionAlgorithm,
                digestAlgorithm,null);
        sgn.setExternalDigest(extSignature, null,
                externalSignature.getEncryptionAlgorithm());
        byte[] encodedSig = sgn.getEncodedPKCS7(hash, null, ocsp,crlBytes,
                sigtype);

        if (estimatedSize < encodedSig.length)
        {
            throw new IOException("Not enough space");
        }
        byte[] paddedSig = new byte[estimatedSize];
        System.arraycopy(encodedSig, 0, paddedSig, 0, encodedSig.length);
        PdfDictionary dic2 = new PdfDictionary();
        dic2.put(PdfName.CONTENTS, (new PdfString(paddedSig)).setHexWriting(true));
        appearance.close(dic2);
    }


}
