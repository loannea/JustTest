import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code FileTest} TODO()
 * <p>TODO()</p>
 * @author zjw
 */
public class FileTest {


    public static void main(String[] args) throws IOException {
        readWantedText("G:\\无锡日志\\aj-2020-02-15.log", "LogRecordAspect", "","G:\\无锡日志\\aj-2020-02-15-1.log");
}


    public static String readWantedText(String FileName, String wantedOne, String wantedTwo,String FileName1) {
        String Str = "";
        List<String> list = new ArrayList<>();
        try {
            //防止读取文件内容时乱码
            File file = new File(FileName);
            InputStreamReader read = new InputStreamReader(new FileInputStream(file), "UTF-8");
            BufferedReader br = new BufferedReader(read);
            //用于临时保存每次读取的内容
            String temp = "";
            while (temp != null) {
                temp = br.readLine();
                //筛选条件
                if (temp != null && temp.contains(wantedOne)) {
                    list.add(temp);
                }
            }


            File outFile = new File(FileName1);
            Writer writer = new FileWriter(outFile);
            BufferedWriter bw = new BufferedWriter(writer);
            list.stream().forEach(e->{
                try {
                    bw.write(e);
                    bw.newLine();
                    bw.flush();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            });
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Str;

    }

}
