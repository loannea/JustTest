package com.loannea.signature.entity;

import lombok.Data;

/**
 * {@code PointBo} 偏移量
 * @author zjw
 */
@Data
public class PointBo {

    /**
     * 定位点名字
     */
    private String name;

    /**
     * x
     */
    private  int  x;

    /**
     * y
     */
    private int y;
}
