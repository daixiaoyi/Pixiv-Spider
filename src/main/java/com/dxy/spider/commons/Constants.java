package com.dxy.spider.commons;

public class Constants {


    /**
     * P站预登陆url
     */
    public static final String PIXIV_BASE_URL = "https://accounts.pixiv.net/login?lang=en&source=pc&view_type=page&ref=wwwtop_accounts_index";

    /**
     * P站登录请求url
     */
    public static final String PIXIV_LOGIN_URL = "https://accounts.pixiv.net/api/login?lang=en";

    /**
     * P站搜索请求url
     */
    public static final String PIXIV_SEARCH_URL = "https://www.pixiv.net/search.php";

    /**
     * P站单图详情页url
     */
    public static final String PIXIV_ILLUST_MEDIUM_URL = "https://www.pixiv.net/member_illust.php?mode=medium&illust_id=";

    /**
     * P站多图详情页url
     */
    public static final String PIXIV_ILLUST_MANGA_URL = "https://www.pixiv.net/member_illust.php?mode=manga&illust_id=";

    /**
     * 图片本地保存根目录
     */
    public static final String IMG_DOWNLOAD_BASE_PATH = "C:/pixiv/search/";

    /**
     * 用户名
     */
    public static final String USERNAME = "";

    /**
     * 密码
     */
    public static final String PASSWORD = "";

    /**
     * 搜索关键词
     */
    public static final String KEY_WORD = "";

    /**
     * 是否只搜索r18结果
     */
    public static final boolean IS_R18 = true;

    /**
     * 点赞数（不低于）
     */
    public static final int STARS = 1000;
}
