package com.loannea.signature.constant;


import lombok.Getter;


/**
 * {@code ErrorCodes} 错误代码
 * @author zjw
 */
public class ErrorCodes
{
    // 缺省500服务器错误
    public static final String DEFAULT = "50000";


    /**
     * 文书异常错误码
     */
    @Getter
    public enum Writ implements IError {
        /**
         * 文书记录不存在
         */
        PARAM_ERROR("0012", "参数错误,请联系管理员"),
        /**
         * 文书记录不存在
         */
        WRIT_NOT_EXIST("001", "文书记录不存在,请联系管理员"),
        /**
         * 未pdf生成文书
         */
        WRIT_NOT_CREATE("002", "pdf未生成文书"),
        /**
         * 附件记录不存在
         */
        ACCESSORY_NOT_EXIST("003", "附件记录不存在"),
        /**
         * 文件丢失
         */
        FILE_NOT_EXIST("004", "文件丢失,请联系管理员"),
        /**
         * 文书类型错误
         */
        WRIT_TYPE_ERROT("005", "文书类型错误,请联系管理员"),
        /**
         * 签章，证书丢失
         */
        SIGN_NOT_EXIST("jw_notExist", "签章或证书不存在,请联系管理员"),
        /**
         * 签章错误
         */
        SIGN_INIT_ERROR("007", "初始化签章信息出错,请联系管理员"),
        /**
         * 签章错误
         */
        SIGN_ERROR("008", "签章出错,请联系管理员"),
        /**
         * 签章类型出错
         */
        SIGN_TYPE_ERROR("009", "签章类型出错,请联系管理员"),
        /**
         * 文件移动时出错
         */
        FILE_MOVE_ERROR("010", "文件移动时出错,请联系管理员"),
        /**
         * 定位点丢失
         */
        POSITION_MISS("010", "定位点丢失,请联系管理员"),

        SIGN_IS_MISSING("jw_missing","签章丢失,请联系管理员"),

        /**
         * 证书损坏
         */
        CERT_BAD_ERROR("jw_type_60", "证书损坏,请联系管理员");


        private final String code;

        private final String msg;

        Writ(String code, String msg)
        {
            this.code = code;
            this.msg = msg;
        }
    }

    /**
     * 文书异常错误码
     */
    @Getter
    public enum Pdf implements IError {

        Pdf_ERROR("pdfError","pdf损坏");

        private final String code;

        private final String msg;

        Pdf(String code, String msg)
        {
            this.code = code;
            this.msg = msg;
        }
    }


}
