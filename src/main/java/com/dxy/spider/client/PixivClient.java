package com.dxy.spider.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dxy.spider.commons.Constants;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class PixivClient {

    private static int filtedCount = 0;
    private static HttpGet get;
    private static HttpPost post;
    private static CookieStore cookieStore;
    private static CloseableHttpResponse response;

    static {
        cookieStore = new BasicCookieStore();
    }

    /**
     * 登录前的预备方法，用于获取登录时的动态参数：post_key
     *
     * @return
     */
    private static String preLogin() {
        String post_keyStr = "";
        get = new HttpGet(Constants.PIXIV_BASE_URL);
        try (
                //创建client时传入一个cookieStore
                CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build()
        ) {
            response = client.execute(get);
            String responseContent = EntityUtils.toString(response.getEntity());

            //解析返回的网页，获取到post_key
            Document doc = Jsoup.parse(responseContent);
            Element post_key = doc.select("input[name=post_key]").get(0);
            post_keyStr = post_key.attr("value");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return post_keyStr;
    }

    /**
     * 登录
     */
    public static void login() {
        String post_keyStr = preLogin();
        post = new HttpPost(Constants.PIXIV_LOGIN_URL);
        try (
                //创建client时传入一个cookieStore
                CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build()
        ) {

            //准备参数
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("pixiv_id", Constants.USERNAME));
            params.add(new BasicNameValuePair("password", Constants.PASSWORD));
            params.add(new BasicNameValuePair("post_key", post_keyStr));
            post.setEntity(new UrlEncodedFormEntity(params, Charset.forName("UTF-8")));
            response = client.execute(post);
            String responseContent = EntityUtils.toString(response.getEntity());
            //解析返回的json
            JSONObject responseJson = JSON.parseObject(responseContent);
            JSONObject responseJsonBody = JSON.parseObject(responseJson.get("body").toString());
            if (responseJsonBody.containsKey("success")) {
                System.out.println("登录成功");
            } else {
                System.out.println(responseJsonBody.get("validation_errors"));
                throw new Exception("登录失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 执行搜索入口方法
     */
    public static void search() {
        int i = 1;
        boolean hasNextPage = true;
        while (hasNextPage) {
            String responseContent = getPage(buildSearchUrl(Constants.KEY_WORD, Constants.IS_R18, i));
            parseSearchResult(responseContent);
            hasNextPage = hasNextPage(responseContent);
            i++;
        }
    }

    /**
     * 发送HTTP/HTTPS请求并返回整个网页
     *
     * @param url
     * @return
     */
    public static String getPage(String url) {
        String responseContent = "";
        get = new HttpGet(url);
        try (
                //创建client时传入一个cookieStore
                CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build()
        ) {
            response = client.execute(get);
            responseContent = EntityUtils.toString(response.getEntity());
            System.out.println("responseContent:" + responseContent);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return responseContent;
    }

    /**
     * 解析搜索请求返回的结果
     *
     * @param responseContent
     */
    public static void parseSearchResult(String responseContent) {
        Document doc = Jsoup.parse(responseContent);
        Element dataListElement = doc.select("#js-mount-point-search-result-list").get(0);
        System.out.println("dataListElement:"+dataListElement);
        JSONArray myJsonArray = (JSONArray) JSONArray.parse(dataListElement.attr("data-items"));
        myJsonArray.forEach((Object json) -> {
            JSONObject jsonObject = (JSONObject) json;
            System.out.println("data-item:" + jsonObject.toJSONString());
            if (StringUtils.isEmpty(jsonObject.getBoolean("isAdContainer")) || !jsonObject.getBoolean("isAdContainer")) {
                String illustTitle = jsonObject.getString("illustTitle");
                int bookmarkCount = jsonObject.getInteger("bookmarkCount");
                String illustType = jsonObject.getString("illustType");
                int pageCount = jsonObject.getInteger("pageCount");
                String illustId = jsonObject.getString("illustId");
                //点赞数过滤
                if (bookmarkCount > Constants.STARS) {
                    filtedCount++;
                    //作品为图片
                    if (illustType.equals("0")) {
                        //创建文件夹（文件名不能有空格）
                        illustTitle.replaceAll(" ", "_");
                        String directoryPath = illustTitle + "_stars_" + bookmarkCount;
                        makeDirectory(directoryPath);
                        //只有一张图，访问图片主页
                        if (pageCount == 1) {
                            String mediumContent = (getPage(Constants.PIXIV_ILLUST_MEDIUM_URL + illustId));
                            //解析网页中的js脚本，过滤出大图的url
                            Document mangaDoc = Jsoup.parse(mediumContent);
                            Elements mangaElements = mangaDoc.getElementsByTag("script");
                            for (Element element : mangaElements) {
                                String data = element.data();
                                //包含所需数据的script标签以以下内容开头，其他忽略
                                if (data.startsWith("'use strict';var globalInitData")) {
                                    String imgUrl = data.substring(data.indexOf("regular") + 10, data.indexOf("original") - 3).replaceAll("\\\\", "");
                                    imgDownload(imgUrl, directoryPath + "/" + illustId + "_" + "0");
                                }
                            }
                        }
                        //多图，访问图片列表页
                        else if (pageCount > 1) {
                            String mangaContent = getPage(Constants.PIXIV_ILLUST_MANGA_URL + illustId);
                            Document mangaDoc = Jsoup.parse(mangaContent);
                            Elements mangaElements = mangaDoc.select("img[data-filter=manga-image]");
                            mangaElements.forEach(element -> {
                            });
                            for (int i = 0; i < mangaElements.size(); i++) {
                                Element mangaElement = mangaElements.get(i);
                                imgDownload(mangaElement.attr("data-src"), directoryPath + "/" + illustId + "_" + mangaElement.attr("data-index"));
                            }
                        } else {
                            System.out.println("作品张数异常");
                        }
                    }
                    //作品为视频
                    else if (illustType.equals("2")) {

                    }
                }
            }
        });
    }


    /**
     * 根据url将图片下载到本地
     *
     * @param url
     * @param filePathName
     */
    public static void imgDownload(String url, String filePathName) {
        get = new HttpGet(url);
        try (
                //创建client时传入一个cookieStore
                CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build()
        ) {
            get.setHeader("referer", url);
            response = client.execute(get);
            File storeFile = new File(Constants.IMG_DOWNLOAD_BASE_PATH + Constants.KEY_WORD + "/" + filePathName + url.substring(url.lastIndexOf(".")));
            FileOutputStream output = new FileOutputStream(storeFile);
            InputStream inputStream = response.getEntity().getContent();
            byte data[] = new byte[1024];
            int len;
            while ((len = inputStream.read(data)) != -1) {
                output.write(data, 0, len);
            }
            output.flush();
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 判断是否还有下一页
     *
     * @param responseContent
     * @return
     */
    public static boolean hasNextPage(String responseContent) {
        Document doc = Jsoup.parse(responseContent);
        Elements elementsZH= doc.select("a[title=继续]");
        Elements elementsJP = doc.select("a[title=次へ]");
        //可能是中文或者日文
        if (elementsZH.size() == 1 || elementsJP.size() == 1) {
            System.out.println("有下一页");
            return true;
        } else {
            System.out.println("没有下一页");
            System.out.println("共搜索出结果：" + doc.select(".count-badge").get(0).text());
            System.out.println("过滤出的结果：" + filtedCount + "件");
            return false;
        }
    }

    public static String buildSearchUrl(String word, boolean isR18, int pageNum) {
        return Constants.PIXIV_SEARCH_URL + "?word=" + word + (isR18 ? "&mode=r18" : "") + "&p=" + pageNum;
    }

    public static boolean makeDirectory(String path) {
        File file;
        try {
            file = new File(Constants.IMG_DOWNLOAD_BASE_PATH + Constants.KEY_WORD + "/" + path);
            if (!file.exists()) {
                return file.mkdirs();
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}