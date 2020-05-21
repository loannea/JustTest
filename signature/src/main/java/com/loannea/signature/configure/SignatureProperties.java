package com.loannea.signature.configure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * {@code SignatureProperties} 属性
 * <p>自动获取配置文件中前缀为sinorock.signature的属性，把值传入对象参数</p>
 * @author zjw
 */
@ConfigurationProperties(prefix = "sinorock.signature")
@Data
public class SignatureProperties {


    /**
     * 签名证书服务器的服务器地址 默认是测试的
     */
    private String eventCertServerUrl="http://online.aheca.cn:88/ecms/signServer/handleSign";


    /**
     * 签名证书服务器的UniqueId 默认是测试的
     */
    private String eventCertServerUniqueId="8570E780670E683748DAB3363ED7DE62";

    /**
     * 签名证书服务器的SecretKey 默认是测试的
     */
    private String eventCertServerSecretKey="69F89842473E193D4602353D5AEBF39E";

    /**
     * pfx服务器证书的密码
     */
    private String pfxCertPassword="1";

}
