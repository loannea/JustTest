package com.loannea.signature.exception;


import lombok.Getter;
import lombok.Setter;

/**
 * {@code SignException} pdf异常
 * @author zjw
 */
@Setter
@Getter
public class PdfException extends RuntimeException
{

    private static final long serialVersionUID = 1L;

    /**
     * 错误消息
     */
    private String msg;

    /**
     * 错误编码
     */
    private String code = "500";


    public PdfException(String msg)
    {
        super(msg);
        this.msg = msg;
    }

    public PdfException(String msg, Throwable e)
    {
        super(msg, e);
        this.msg = msg;
    }

    public PdfException(String msg, String code)
    {
        super(msg);
        this.msg = msg;
        this.code = code;
    }

    public PdfException(String msg, String code, Throwable e)
    {
        super(msg, e);
        this.msg = msg;
        this.code = code;
    }

}
