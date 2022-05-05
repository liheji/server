package top.liheji.server.util;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : Galaxy
 * @time : 2021/10/29 22:29
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 验证码滑块坐标识别工具类
 */
public class SlideUtils {
    private static SlideUtils slide;
    private static final String OS = System.getProperty("os.name").toLowerCase();

    /**
     * SlideUtils 私有化构造器，只能创建一个
     */
    private SlideUtils() {
        File file;
        if (OS.contains("windows")) {
            file = FileUtils.resourceFile("data", "opencv_java346.dll");
        } else if (OS.contains("linux")) {
            file = new File(ConsoleUtils.SERVER_STATIC_PATH, "libopencv_java346.so");
        } else {
            throw new RuntimeException("Platform not supported！");
        }

        System.load(file.getAbsolutePath());
    }

    /**
     * 获取当前实例
     *
     * @return 单一实例
     */
    public static SlideUtils getInstance() {
        if (slide == null) {
            synchronized (SlideUtils.class) {
                if (slide == null) {
                    slide = new SlideUtils();
                }
            }
        }
        return slide;
    }

    public Map<String, Object> discernSlideImg(String slidePath, String bgPath) {
        Mat tmp = clearWhite(slidePath);
        Mat slideImg = new Mat();
        Imgproc.cvtColor(tmp, slideImg, Imgproc.COLOR_RGB2GRAY);

        tmp = slideImg;
        slideImg = new Mat();
        Imgproc.Canny(tmp, slideImg, 100, 200);

        tmp = Imgcodecs.imread(bgPath, 1);
        Mat bgImg = new Mat();
        Imgproc.Canny(tmp, bgImg, 100, 200);

        Mat slidePic = new Mat(), bgPic = new Mat();
        Imgproc.cvtColor(slideImg, slidePic, Imgproc.COLOR_GRAY2RGB);
        Imgproc.cvtColor(bgImg, bgPic, Imgproc.COLOR_GRAY2RGB);

        int th = slidePic.rows(), tw = slidePic.cols();
        Mat result = new Mat();
        Imgproc.matchTemplate(bgPic, slidePic, result, Imgproc.TM_CCOEFF_NORMED);

        Core.MinMaxLocResult piPei = Core.minMaxLoc(result);
        Point topLeft = piPei.maxLoc;
        Point bottomRight = new Point(topLeft.x + tw, topLeft.y + th);

        Map<String, Object> map = new HashMap<>();
        map.put("rows", bgImg.rows());
        map.put("cols", bgImg.cols());
        map.put("topLeft", topLeft);
        map.put("bottomRight", bottomRight);

        return map;
    }

    public Map<String, Object> discernGapImg(String gapPath, String bgPath) {
        Mat gapImg = Imgcodecs.imread(gapPath, 1);
        Mat bgImg = Imgcodecs.imread(bgPath, 1);

        //设置一个起始量,因为验证码一般不可能在左边，加快识别速度
        int left = 60;
        //像素色差
        int threshold = 60;
        int rows = gapImg.rows(), cols = gapImg.cols();
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = 0, maxY = 0;
        for (int x = 0; x < rows; x++) {
            for (int y = left; y < cols; y++) {
                double[] bgPx = bgImg.get(x, y);
                double[] gapPx = gapImg.get(x, y);
                if (bgPx == null || gapPx == null) {
                    continue;
                }

                if (Math.abs(bgPx[0] - gapPx[0]) < threshold &&
                        Math.abs(bgPx[1] - gapPx[1]) < threshold &&
                        Math.abs(bgPx[2] - gapPx[2]) < threshold
                ) {

                } else {
                    minX = Math.min(y, minX);

                    minY = Math.min(x, minY);
                    maxX = Math.max(y, maxX);
                    maxY = Math.max(x, maxY);
                }
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("rows", gapImg.rows());
        map.put("cols", gapImg.cols());
        map.put("topLeft", new Point(minX, minY));
        map.put("bottomRight", new Point(maxX, maxY));

        return map;
    }

    private Mat clearWhite(String filePath) {
        Mat image = Imgcodecs.imread(filePath, 1);
        int rows = image.rows(), cols = image.rows();

        int minX = 255, minY = 255, maxX = 0, maxY = 0;
        for (int x = 1; x < rows; x++) {
            for (int y = 1; y < cols; y++) {
                double[] px = image.get(x, y);
                if (px == null) {
                    continue;
                }
                if (px[0] == px[1] && px[0] == px[2]) {
                    continue;
                }

                if (x <= minX) {
                    minX = x;
                } else if (x >= maxX) {
                    maxX = x;
                }

                if (y <= minY) {
                    minY = y;
                } else if (y >= maxY) {
                    maxY = y;
                }
            }
        }

        return image.submat(new Range(minX, maxX), new Range(minY, maxY));
    }
}
