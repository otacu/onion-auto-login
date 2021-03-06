package com.example.demo;

import com.example.demo.pojo.CharacterInfo;
import com.example.demo.util.BinaryImageUtil;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebElement;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OnionRunScript {
    static final String ORIGIN_CODE_FILE_PATH = "originCode.png";

    static final String TRANSFERED_CODE_FILE_PATH = "transferedCode.png";


    private static WebDriver driver;

    public static void main(String[] args) throws Exception {
        // 注意chromedriver的版本要和chrome浏览器的版本一致
        System.setProperty("webdriver.chrome.driver", "E:\\yjs\\application\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.get("https://cosyfans-passport.buykerbuyker.cn/en/login.html");
        // 页面最大化
        driver.manage().window().maximize();
        // 递归直到验证码输入无误
        recursion();
        //点击登录
        driver.findElement(By.className("login-btn")).click();
        Thread.sleep(3000);
        // 聚焦搜索框
        Actions focusSearchBoxAction = new Actions(driver);
        focusSearchBoxAction.moveToElement(driver.findElement(By.className("home-search-box")));
        focusSearchBoxAction.click();
        focusSearchBoxAction.build().perform();
        Thread.sleep(4000);
        // 输入关键字
        WebElement searchBoxElement = driver.findElement(By.xpath("//div[@class='overwrite-title-demo seek-form']/div/input"));
//        searchBoxElement.sendKeys("fanwai");
        searchBoxElement.sendKeys("skinyisi");
        WebElement confirmSearchElement = driver.findElement(By.xpath("//div[@class='overwrite-title-demo seek-form']/a"));
        confirmSearchElement.click();
        Thread.sleep(3000);
        // 点击商品
        List<WebElement> itemElementList = driver.findElements(By.xpath("//div[@class='vux-flexbox-item']"));
        itemElementList.get(0).findElement(By.xpath("div/a")).click();
        Thread.sleep(2000);
        // 加入购物车
        WebElement buyNowButton = driver.findElement(By.xpath("//a[@class='but buy-now']"));
        buyNowButton.click();
        Thread.sleep(1000);
        WebElement addToCartButton = driver.findElement(By.xpath("//div[@class='vux-popup-dialog vux-popup-bottom vux-popup-show']/div/div[3]/a"));
        addToCartButton.click();
        Thread.sleep(3000);
        // 跳转到购物车页面
        WebElement goToCartButton = driver.findElement(By.xpath("//div[@class='details-footer']/div[1]/a[2]"));
        goToCartButton.click();
        Thread.sleep(3000);
        // 选择商品
        WebElement sellectAllButton = driver.findElement(By.xpath("//div[@class='word-choice-box']"));
        sellectAllButton.click();
        Thread.sleep(1000);
        // 提交购物车
        WebElement checkOutButton = driver.findElement(By.xpath("//a[@class='sub-cart']"));
        checkOutButton.click();
        Thread.sleep(2000);
        // 确认下单
        WebElement placeOrderButton = driver.findElement(By.xpath("//div[@class='place-order-footer-c']/a"));
        placeOrderButton.click();
        Thread.sleep(2000);
        // 选择支付方式
        WebElement payWayButton = driver.findElement(By.xpath("//div[@class='vux-popup-dialog vux-popup-bottom vux-popup-show']/div/div/div[2]/ul/li[1]/a"));
        payWayButton.click();
        Thread.sleep(400000);
        driver.quit();

    }

    /**
     * 递归输入验证码
     *
     * @throws Exception
     */
    public static void recursion() throws Exception {
        // 将页面要点击的图片截屏
        WebElement codeImageElement = driver.findElement(By.id("myCanvas"));
        File originCodeFile = new File(ORIGIN_CODE_FILE_PATH);
        FileUtils.copyFile(elementSnapshot(codeImageElement), originCodeFile);
        // 图片二值化
        BinaryImageUtil.binaryImage(ORIGIN_CODE_FILE_PATH, TRANSFERED_CODE_FILE_PATH);
        // 图片转字符串
        Map<String, CharacterInfo> characterInfoMap = transferImageToString(TRANSFERED_CODE_FILE_PATH);
        // 输入账号
        driver.findElement(By.id("loginName")).sendKeys("15018421849");
        // 输入密码
        driver.findElement(By.xpath("//div[@class=\"login-cont\"]/div[2]/input")).sendKeys("qwe123");
        // 要点击的字符，从“Please click in order【DSH】”中截取出来
        String targetCode = driver.findElement(By.xpath("//div[@class=\"verification\"]/div[2]/div/div/strong")).getText();
        int start = targetCode.indexOf("【") + 1;
        int end = targetCode.indexOf("】");
        targetCode = targetCode.substring(start, end);
        // 点击图片中的字母
        org.openqa.selenium.Rectangle codeImageRectangle = codeImageElement.getRect();
        int clickNum = 0;
        for (int i = 0; i < targetCode.length(); i++) {
            String targetCharacter = String.valueOf(targetCode.charAt(i));
            CharacterInfo characterInfo = characterInfoMap.get(targetCharacter);
            int xOffset = codeImageRectangle.getX() + characterInfo.getX();
            int yOffset = codeImageRectangle.getY() + characterInfo.getY();
            Actions action = new Actions(driver);
            // 移到字母上点击，然后重置到起点位置
            action.moveByOffset(xOffset, yOffset).click().moveByOffset(-xOffset, -yOffset).perform();
            clickNum++;
        }
        // 是否验证通过
        WebElement verifySuccessTipsElement = driver.findElement(By.className("set"));
        String css = verifySuccessTipsElement.getAttribute("class");
        if (!css.contains("succeed") || clickNum < 3) {
            driver.navigate().refresh();
            recursion();
        }
    }

    /**
     * 部分截图（元素截图）
     * 有时候需要元素的截图，不需要整个截图
     *
     * @throws Exception
     */
    public static File elementSnapshot(WebElement element) throws Exception {
        //创建全屏截图
        RemoteWebElement wrapsDriver = (RemoteWebElement) element;
        File screen = ((TakesScreenshot) wrapsDriver.getWrappedDriver()).getScreenshotAs(OutputType.FILE);
        BufferedImage image = ImageIO.read(screen);
        //获取元素的高度、宽度
        int width = element.getSize().getWidth();
        int height = element.getSize().getHeight();

        //创建一个矩形使用上面的高度，和宽度
        Rectangle rect = new Rectangle(width, height);
        //元素坐标
        Point p = element.getLocation();
        BufferedImage img = image.getSubimage(p.getX(), p.getY(), rect.width, rect.height);
        ImageIO.write(img, "png", screen);
        return screen;
    }

    /**
     * 图片转字符串
     *
     * @param transferedFilePath 经过二值化的图片地址
     * @return 字符串
     */
    public static Map<String, CharacterInfo> transferImageToString(String transferedFilePath) {
        File transferedFile = new File(transferedFilePath);
        ITesseract instance = new Tesseract();//调用Tesseract
        URL url = ClassLoader.getSystemResource("tessdata");//获得Tesseract的文字库
        String tesspath = url.getPath().substring(1);
        instance.setDatapath(tesspath);//进行读取，默认是英文，如果要使用中文包，加上instance.setLanguage("chi_sim");
        instance.setLanguage("uppercase_letter");
        Map<String, CharacterInfo> characterInfoMap = new HashMap<String, CharacterInfo>();
        try {
            String code = instance.doOCR(transferedFile);
            System.out.println("识别结果：" + code);
            code = code.toUpperCase();
            // 去掉换行、空格等字符
            List<String> codeLetterList = new ArrayList<String>();
            List<String> allUppercaseLetterList = getAllUppercaseLetter();
            for (int j = 0; j < code.length(); j++) {
                String codeLetter = String.valueOf(code.charAt(j));
                if (allUppercaseLetterList.contains(codeLetter)) {
                    codeLetterList.add(codeLetter.toUpperCase());
                }
            }
            BufferedImage bi = ImageIO.read(transferedFile);
            int level = ITessAPI.TessPageIteratorLevel.RIL_SYMBOL;
            List<Rectangle> list = instance.getSegmentedRegions(bi, level);
            for (int i = 0; i < list.size(); i++) {
                CharacterInfo characterInfo = new CharacterInfo();
                String value = String.valueOf(codeLetterList.get(i));
                characterInfo.setValue(value);
                Rectangle rectangle = list.get(i);
                characterInfo.setX(rectangle.x + rectangle.width / 2);
                characterInfo.setY(rectangle.y + rectangle.height / 2);
                characterInfoMap.put(value, characterInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return characterInfoMap;
    }

    private static List<String> getAllUppercaseLetter() {
        List<String> letterList = new ArrayList<String>();
        char firstE = 'A', lastE = 'Z';
        int firstEnglish = (int) firstE;
        int lastEnglish = (int) lastE;
        for (int i = firstEnglish; i <= lastEnglish; ++i) {
            char uppercase = (char) i;
            letterList.add(String.valueOf(uppercase));
        }
        return letterList;
    }

}

