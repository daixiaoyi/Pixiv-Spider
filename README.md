# Pixiv-Spider
P站的爬虫程序

目前只支持关键词搜索，以及r18和点赞数选项

如果你想了解该爬虫的设计思路，代码详解，可以参考我的博客：[基于Java的Pixiv.net（P站）爬虫](https://blog.csdn.net/coder_dai/article/details/81482712)
## 使用方法
### 1.配置参数
打开Constants.java

设置图片本地保存根目录（可直接使用默认路径）
```$xslt
    /**
     * 图片本地保存根目录
     */
    public static final String IMG_DOWNLOAD_BASE_PATH = "C:/pixiv/search/";
```
设置登录用户名和密码
```$xslt
    /**
     * 用户名
     */
    public static final String USERNAME = "";

    /**
     * 密码
     */
    public static final String PASSWORD = "";
```
设置搜索关键词
```$xslt
    /**
     * 搜索关键词
     */
    public static final String KEY_WORD = "";
```
设置是否只搜索r18结果（默认true）
```$xslt
    /**
     * 是否只搜索r18结果
     */
    public static final boolean IS_R18 = true;
```
设置点赞数（不低于设定值，默认1000）
```$xslt
    /**
     * 点赞数（不低于）
     */
    public static final int STARS = 1000;
```
### 2.启动
打开Launcher.java，执行main方法即可
```$xslt
public class Launcher {
    public static void main(String[] args) {
        PixivClient.login();
        PixivClient.search();
    }
}
```
