package com.dxy.spider.main;

import com.dxy.spider.client.PixivClient;

public class Launcher {
    public static void main(String[] args) {
        PixivClient.login();
        PixivClient.search();
    }
}
