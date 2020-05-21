package com.loannea.signature.entity;


import java.security.PrivateKey;
import java.security.cert.Certificate;

import com.itextpdf.text.pdf.PdfSignatureAppearance;

import lombok.Builder;
import lombok.Data;


/**
 * {@code SignatureInfo} 签章信息实体类
 * <p>保存签章的基本信息</p>
 * @author zjw
 */
@Data
@Builder
public class SignatureInfo {

    /**
     * 签名的原因，显示在pdf签名属性中
     */
    private String reason;

    /**
     *  签名的地点，显示在pdf签名属性中
     */
    private String location;

    /**
     * 摘要算法名称，例如SHA-1
     */
    private String digestAlgorithm;

    /**
     * 图章路径
     */
    private String imagePath;

    /**
     * 表单域名称
     */
    private String fieldName;


    /**
     * 签字内容
     */
    private String textOnly;

    /**
     * 证书链
     */
    private Certificate[] chain;

    /**
     * 签名私钥
     */
    private PrivateKey pk;

    /**
     * 批准签章:将文档类型设置为已认证，而不是简单签名。
     */
    private int certificationLevel = PdfSignatureAppearance.NOT_CERTIFIED;

    /**
     * 表现形式：仅描述，仅图片，图片和描述，签章者和描述
     */
    private PdfSignatureAppearance.RenderingMode renderingMode = PdfSignatureAppearance.RenderingMode.GRAPHIC;

    /**
     * 文字定位点
     */
    private String positionText;

    /**
     * 创建者
     */
    private String creator;

    /**
     * 是否带时间
     */
    private boolean hasTime;

    /**
     * 图片类型  默认是R:人 G:公
     */
    private String imgType = "R";

}
