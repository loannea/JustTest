import com.loannea.signature.util.PdfUtils;
import lombok.Data;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * {@code PDFSavePNG} TODO()
 * <p>TODO()</p>
 * @author zjw
 */
public class PDFSavePNG {



    public static void main(String[] args) throws IOException {

        // 40.57
        long start = System.currentTimeMillis();
        int pdfSize = PdfUtils.getPdfSize("d:\\aj-data\\file\\\\3202\\320200\\2020\\5\\other\\3.18环境测试嗷嗷.pdf");
        // 打开来源 pdf
        ExecutorService service = Executors.newFixedThreadPool(pdfSize);
        CountDownLatch latch = new CountDownLatch(pdfSize);
        for (int i = 0; i < pdfSize; i++) {
            PdfConvertThread pdfConvertThread = new PdfConvertThread(latch);
            pdfConvertThread.setPageNumber(i);
//            pdfConvertThread.setPdfRenderer(pdfRenderer);
            Future<BufferedImage> future = service.submit(pdfConvertThread);
        }
        // 提取的页码
        int pageNumber = 0;
        service.shutdown();
        long end = System.currentTimeMillis();
        System.out.println("====================" + (end - start));

    }

}

@Data
class PdfConvertThread implements Callable<BufferedImage> {

    private int pageNumber = 0;

    private PDFRenderer pdfRenderer;

    private final CountDownLatch latch;

    public PdfConvertThread(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public BufferedImage call() throws Exception {
        BufferedImage buffImage =null;
        try{
            System.out.println(pageNumber);
            PDDocument pdfDocument = PDDocument.load(new File("d:\\aj-data\\file\\\\3202\\320200\\2020\\5\\other\\3.18环境测试嗷嗷.pdf"));
            PDFRenderer pdfRenderer = new PDFRenderer(pdfDocument);
            buffImage = pdfRenderer.renderImageWithDPI(pageNumber, 300, ImageType.RGB);
            System.out.println(buffImage.toString());
            ImageIOUtil.writeImage(buffImage, "C:\\Users\\Administrator\\Desktop\\\\22222\\" + pageNumber + ".png", 300);
            pdfDocument.close();
            latch.countDown();
            System.out.println(pageNumber+"over");
        }catch (Exception e){
            e.printStackTrace();
        }

        return buffImage;
    }

}


