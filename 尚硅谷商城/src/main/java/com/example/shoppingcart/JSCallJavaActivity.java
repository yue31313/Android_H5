package com.example.shoppingcart;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.ArrayList;

/**
 */
public class JSCallJavaActivity extends Activity {
    /**
     * 服务器地址，用浏览器也可以打开
     */
//    private static final String url = "http://lol.zhangyoubao.com/mobiles/item/59318?v_=400609&size=middle&t=1449815418";
    private static final String url = "http://10.0.2.2:8080/jd/index.html";
    private WebView wv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ShoppingCanst.shopBeanlist = new ArrayList<ShopBean>();
        setContentView(R.layout.activity_realnet_jscalljava);
        /**
         * WebView的对象
         */
        wv = (WebView) findViewById(R.id.webview);
        /**
         * 如果访问的页面中有Javascript，则webview必须设置支持Javascript。
         */
        WebSettings setting = wv.getSettings();
        setting.setJavaScriptEnabled(true);

        /**
         * 设置支持显示缩放的按钮
         */
        setting.setBuiltInZoomControls(true);
        setting.setUseWideViewPort(true);
        wv.loadUrl(url);

        /**
         * js调用java
         * "android"是Key，js调用new Shop()的方法时 ，必须用此Key，key名字自己去
         */
        wv.addJavascriptInterface(new Shop(), "android");

        wv.setWebViewClient(new WebViewClient() {
        });

        wv.setWebViewClient(new WebViewClient());

    }

    private class Shop {
        /**
         * int shopid = 111;
         String shopImager = “http://xxx.jpg”;
         String shoreName = “商店名称”
         String shopName = “商品名称”
         String shopDes = “商品描述” 例如： 颜色，蓝色，尺寸：41
         int shopPrice = 200    这个是价格
         int shopNumber = 1;  //默认购买数量

         */

        public void addShoppingcart(int shopid,String shopImager,String shoreName,String shopName , String shopDes,int shopPrice, int shopNumber){
            String text = "添加到购物车：" + shopid+","+shopImager+","+shoreName+","+shopName+","+shopDes+","+shopPrice+","+shopNumber;
            System.out.println(text);
//            Toast.makeText(JSCallJavaActivity.this, text, Toast.LENGTH_SHORT).show();
            startMain(shopid, shoreName, shopName, shopDes, shopPrice, shopNumber);
        }


        public void shopping(int shopid,String shopImager,String shoreName,String shopName , String shopDes,int shopPrice, int shopNumber){
            String text = "添加到购物车：" + shopid+","+shopImager+","+shoreName+","+shopName+","+shopDes+","+shopPrice+","+shopNumber;
            System.out.println(text);
//            Toast.makeText(JSCallJavaActivity.this, text, Toast.LENGTH_SHORT).show();
            startMain(shopid, shoreName, shopName, shopDes, shopPrice, shopNumber);
        }
        /**
         * 必须加注解,不加注解，该方法无效
         *
         * @param itemid
         * @param videourl
         * @param itemtitle
         */
        public void playVideo(int itemid, String videourl, String itemtitle) {
            // 把系统所有的播放调起来
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(videourl), "video/*");
            startActivity(intent);
        }

        public void offlineVideo(int itemid, String videourl, String itemtitle, String itemdesc, String itempic) {
            Toast.makeText(JSCallJavaActivity.this, "下载视频：" + videourl, Toast.LENGTH_SHORT).show();
        }


    }

    private void startMain(int shopid, String shoreName, String shopName, String shopDes, int shopPrice, int shopNumber) {
        ShopBean bean = new ShopBean();
        bean.setShopId(shopid);
        bean.setShopPicture(R.drawable.shoes1);
        bean.setStoreName(shoreName);
        bean.setShopName(shopName);
        bean.setShopDescription(shopDes);
        bean.setShopPrice(shopPrice);
        bean.setShopNumber(shopNumber);
        bean.setChoosed(true);//是否被勾选

        ShoppingCanst.shopBeanlist.add(bean);


        Intent intent = new Intent(JSCallJavaActivity.this,MainActivity.class);
        startActivity(intent);
    }
}
