import cn.aheca.api.pdf.AhcaPdfService;
import cn.aheca.api.pdf.dto.PdfSignInfo;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code TestCAVerify} TODO()
 * <p>TODO()</p>
 * @author zjw
 */
public class TestCAVerify {


    @SuppressWarnings("unchecked")
    public static void main(String[] args) {

        /*
         * 调用接口，实现已签章PDF完整性校验
         */
        Map<String, Object> map = new HashMap<>();
        String pdfPath = "C:\\Users\\DELL\\Desktop\\svg-pfx.pdf";


        pdfPath = "C:\\Users\\DELL\\Desktop\\svg-event.pdf";
        pdfPath = "C:\\Users\\DELL\\Desktop\\3-svg-ca-upt.pdf";

        pdfPath = "C:\\Users\\DELL\\Desktop\\1.pdf";

        try {
            pdfPath = Base64Tools.encodeBase64File(pdfPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        map.put("pdfPath", pdfPath);//待验证PDF路径
        Map<String, Object> returnMap2  = new AhcaPdfService().pdfSignVerify(map);
        //仅当返回结果为200时，表示验证成功，
        if(returnMap2.get("code").toString().equals("200")){
            System.out.println("签名验证全部成功");
            System.out.println();
            List<PdfSignInfo> list = (List<PdfSignInfo>)returnMap2.get("message");
            for(PdfSignInfo pdfSignInfo : list){
                System.out.println("签名顺序的修订号:"+pdfSignInfo.getRevisionNumber());//int类型,表示该文档中的第几个签名
                System.out.println("签名域:" + pdfSignInfo.getSignatureName());
                System.out.println("证书信息:"+pdfSignInfo.getBase64Cert());
                System.out.println("算法:"+pdfSignInfo.getDigestAlgorithm());
                System.out.println("签名原因:"+pdfSignInfo.getReason());
                System.out.println("签名地点:"+pdfSignInfo.getLocation());
                System.out.println("签名时间:"+pdfSignInfo.getSignDate());//Date类型
                System.out.println();
            }
        }else{
            System.out.println("验证失败:"+returnMap2.get("code"));
            System.out.println("验证失败:"+returnMap2.get("message"));
        }
    }

}


class Base64Tools {

   /**
    * 将文件转成base64 字符串
    *
    * @param path 文件路径
    * @return
    */
   public static String encodeBase64File(String path) {
       try {
//将文件 转换为字符串
           File file = new File(path);
           FileInputStream inputFile = new FileInputStream(file);
           byte[] buffer = new byte[(int) file.length()];
           inputFile.read(buffer);
           inputFile.close();
           System.err.println("加密");
//字符串加密
           return new BASE64Encoder().encode(buffer);
       } catch (Exception e) {

           e.printStackTrace();

       }
       return "ok";
   }

   /**
    * 将base64字符解码保存文件
    *
    * @param base64Code  加密的base64
    * @param targetPath 保存的文件夹路径名
    */
   public static void decoderBase64File(String base64Code, String targetPath) {
       try {
           byte[] buffer = new BASE64Decoder().decodeBuffer(base64Code);
           FileOutputStream out = new FileOutputStream(targetPath);
           out.write(buffer);
           out.close();
           System.err.println("解码");
       } catch (Exception e) {

           e.printStackTrace();
       }
   }
}

