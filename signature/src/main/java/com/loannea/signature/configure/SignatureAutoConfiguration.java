package com.loannea.signature.configure;

import com.loannea.signature.sign.MakeSignatureSinorock;
import com.loannea.signature.sign.SignUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * {@code SignatureAutoConfiguration} 签名自动配置
 * @author zjw
 */
@Configuration
@EnableConfigurationProperties(SignatureProperties.class)
public class SignatureAutoConfiguration {


    @Resource
    private SignatureProperties properties;

    @Bean
    @ConditionalOnMissingBean(MakeSignatureSinorock.class)
    public MakeSignatureSinorock initEventInfo() {
        MakeSignatureSinorock signatureSinorock = new MakeSignatureSinorock();
        signatureSinorock.setUrl(properties.getEventCertServerUrl());
        signatureSinorock.setSecretKey(properties.getEventCertServerSecretKey());
        signatureSinorock.setUniqueId(properties.getEventCertServerUniqueId());
        return signatureSinorock;
    }


    @Bean
    @ConditionalOnMissingBean(SignUtil.class)
    public SignUtil initSignUtil() {
        return new SignUtil();
    }



}
