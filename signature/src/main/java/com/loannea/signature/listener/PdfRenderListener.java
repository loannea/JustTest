package com.loannea.signature.listener;


import com.itextpdf.awt.geom.Rectangle2D.Float;
import com.itextpdf.awt.geom.RectangularShape;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.loannea.signature.entity.PointBo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


/**
 * {@code PdfUtils} pdf解析监听
 * <p>对获取的pdf流进行详细的解析</p>
 * @author zjw
 */
@Slf4j
public class PdfRenderListener implements RenderListener
{

    /**
     * 关键字
     */
    private String key;

    /**
     * 页数
     */
    private int pageNo;

    /**
     * 定位点调整
     */
    private List<PointBo> pointBos;

    /**
     * 构造方法
     * @param key 关键字
     * @param pageNo 页数
     */
    public PdfRenderListener(String key, int pageNo, List<PointBo> pointBos)
    {
        this.key = key;
        this.pageNo = pageNo;
        this.pointBos = pointBos;
    }

    /**
     * 用来存放文字
     */
    private List<String> textList = new ArrayList<>();

    /**
     * 用来存放文字的Y坐标
     */
    private List<java.lang.Float> listY = new ArrayList<>();

    /**
     * 存放关键词出现的坐标  全局的
     */
    private static List<Object[]> KEYS_INFO = new ArrayList<>();

    /**
     * 获取关键字出现的坐标
     * @return List<Object [ ]>
     */
    public static List<Object[]> getKeyInfo()
    {
        return KEYS_INFO;
    }

    /**
     * 清空关键字容器
     */
    public static void clearKeyInfo()
    {
        KEYS_INFO.clear();
    }

    @Override
    public void beginTextBlock()
    {

    }

    @Override
    public void renderText(TextRenderInfo renderInfo)
    {

        String text = renderInfo.getText();
        log.info(text);
        if (text.length() > 0)
        {
            RectangularShape rectBase = renderInfo.getBaseline().getBoundingRectange();
            // 计算出文字的边框矩形
            float leftX = (float)rectBase.getMinX();
            float leftY = (float)(rectBase.getMinY() - 1);
            float rightX = (float)rectBase.getMaxX();
            float rightY = (float)(rectBase.getMaxY() - 1);
            Float rect = new Float(leftX, leftY, rightX - leftX, rightY - leftY);
            String tmp;
            if (listY.contains(rect.y))
            {
                int index = listY.indexOf(rect.y);
                tmp = textList.get(index) + text;
                textList.set(index, tmp);
                if (tmp.contains(key))
                {
                    // 如果已经查找一次，就不接着查了
                    boolean b = KEYS_INFO.stream().map(e -> (Integer)e[2]).anyMatch(
                        e -> e == pageNo);
                    if (!b)
                    {
                        Object[] resu = new Object[3];
                        resu[0] = rect.x;
                        resu[1] = rect.y;
                        // noinspection CheckStyle

                        for (PointBo e : pointBos)
                        {
                            if (tmp.contains(e.getName()))
                            {
                                resu[0] = rect.x + e.getX();
                                resu[1] = rect.y + e.getY();
                                break;
                            }
                        }
                        resu[2] = pageNo;
                        KEYS_INFO.add(resu);
                    }

                }
            }
            else
            {
                textList.add(text);
                listY.add(rect.y);
                tmp = text;
                if (tmp.contains(key))
                {
                    // 如果已经查找一次，就不接着查了
                    boolean b = KEYS_INFO.stream().map(e -> (Integer)e[2]).anyMatch(
                        e -> e == pageNo);
                    if (!b)
                    {
                        Object[] resu = new Object[3];
                        resu[0] = rect.x;
                        resu[1] = rect.y;

                        for (PointBo e : pointBos)
                        {
                            if (tmp.contains(e.getName()))
                            {
                                resu[0] = rect.x + e.getX();
                                resu[1] = rect.y + e.getY();
                                break;
                            }
                        }
                        resu[2] = pageNo;
                        KEYS_INFO.add(resu);
                    }

                }

            }
        }

    }

    @Override
    public void endTextBlock()
    {

    }

    @Override
    public void renderImage(ImageRenderInfo imageRenderInfo)
    {

    }
}
