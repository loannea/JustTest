import cn.aheca.api.pdf.AhcaPdfService;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;


/**
 * {@code TestSign} TODO()
 * <p>TODO()</p>
 * @author zjw
 */
@Slf4j
public class TestCA
{

    public static void main(String[] args)
        throws Exception {


        String src = "C:\\Users\\DELL\\Desktop\\11\\2.pdf";
        String target = "C:\\Users\\DELL\\Desktop\\11\\3.pdf";



        String pdfPath = "C:\\Users\\DELL\\Desktop\\11\\1.pdf";
        String pdfDestPath = "C:\\Users\\DELL\\Desktop\\11\\2.pdf";
//        String imagePath = "C:\\Users\\DELL\\Desktop\\11\\svg2png.png";
//        String imagePath = "C:\\Users\\DELL\\Desktop\\11\\常爱民.png";
//        String imagePath = "C:\\Users\\DELL\\Desktop\\11\\常爱民.svg";
        String imagePath = "C:\\Users\\DELL\\Desktop\\11\\常爱民1.png";


        Map<String, Object> map = new HashMap<>(16);
        map.put("pdfPath", pdfPath);//需要签章的PDF文件路径
        map.put("pdfDestPath", pdfDestPath);//签完章的PDF文件路径
        map.put("imagePath", imagePath);//签名图片的路径,支持png,gif,jpg格式
        map.put("leftBottomX", 600f);// 图片的左下方x坐标 float类型
        map.put("leftBottomY", 750f);// 图片的左下方y坐标 float类型
//        map.put("imgWidth", 100f);// 图片的宽度 float类型
//        map.put("imgHeight", 100f);// 图片的高度 float类型
        map.put("imgWidth", 100f);// 图片的宽度 float类型
        map.put("imgHeight", 100f);// 图片的高度 float类型
        map.put("pageNum", 1);// 在第几页进行签章 int类型

        String url = "http://online.aheca.cn:88/ecms/signServer/handleSign";
        map.put("ecmsUrl",url);//事件证书系统服务地址
        map.put("appKey", "8570E780670E683748DAB3363ED7DE62");//事件证书访问授权key
        map.put("appSecret", "69F89842473E193D4602353D5AEBF39E");//事件证书访问授权secret
        map.put("tsaUrl", null);//时间戳地址:不需要请传null或不设置该值
        map.put("userName", "张三");//用户名称(姓名、企业名称) （最大128字符）
        map.put("idCard", "340000000000000000");//用户身份证号(签名人信息)（最大128字符）
        map.put("certOU", "1");//写入证书的其它信息1（最大128字符）
        map.put("certE", "2");//写入证书的其它信息2（最大64字符）


        map.put("reason", "测试PDF签名接口");//签名原因
        map.put("location", "南京CA");//签名地点
        long a = System.currentTimeMillis();

        Map<String, String> returnMap = new AhcaPdfService().pdfSignEventCert(map);
        long b = System.currentTimeMillis();
        System.out.println(b-a);
        System.out.println(returnMap.toString());
        System.out.println();

    }

}
