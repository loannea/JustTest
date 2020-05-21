package com.loannea.signature.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * {@code SignBo} 签章参数
 * @author zjw
 */
@Data
@Builder
public class SignBo {
    /**
     * 原文件
     */
    private String src;


    /**
     * 目标文件
     */
    private String target;

    /**
     * 签名信息
     */
    private SignatureInfo  signatureInfo;

    /**
     * 是否只有文字签名
     */
    private boolean isTextOnly;


    private String signTime;

    private boolean isSpecial = false;

    /**
     * 偏移量
     */
    private List<PointBo> pointBos;


    /**
     * 签名模式 0 服务器签名 1 事件证书签名
     */
    private  int signMode;
}
