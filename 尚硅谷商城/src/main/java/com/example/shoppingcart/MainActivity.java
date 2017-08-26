package com.example.shoppingcart;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {

    /**
     * 商品总价格
     */
    public static final int TOTAL_PRICE = 10;
    /**
     * 选中商品
     */
    public static final int SELECTED_SHOP = 11;
    private CheckBox checkBox;
    private ListView listView;
    private TextView popTotalPrice;        //结算的价格
    private TextView popDelete;            //删除
    private TextView popRecycle;        //收藏
    private TextView popCheckOut;        //结算
    private LinearLayout layout;        //结算布局
    private ShopAdapter adapter;        //自定义适配器

    private List<ShopBean> shopBeansMain;        //当前内存中的购物车数据集合

    private boolean flag = true;        //全选或全取消

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();//初始化视图
        init();
    }

    //初始化UI界面
    private void initViews() {
        checkBox = (CheckBox) findViewById(R.id.all_check);
        listView = (ListView) findViewById(R.id.main_listView);
        popTotalPrice = (TextView) findViewById(R.id.shopTotalPrice);
        popDelete = (TextView) findViewById(R.id.delete);
        popRecycle = (TextView) findViewById(R.id.collection);
        popCheckOut = (TextView) findViewById(R.id.checkOut);
        layout = (LinearLayout) findViewById(R.id.price_relative);

        //设置点击事件
        ClickListener cl = new ClickListener();
        checkBox.setOnClickListener(cl);
        popDelete.setOnClickListener(cl);
        popCheckOut.setOnClickListener(cl);
        popRecycle.setOnClickListener(cl);
    }

    //初始化数据
    private void init() {
        getListData();
        //当前内存中的数据
        shopBeansMain = ShoppingCanst.shopBeanlist;
        //设置适配器
        adapter = new ShopAdapter(this, shopBeansMain, handler, R.layout.main_item);
        listView.setAdapter(adapter);
    }

    //获取集合数据,真实情况是要携带用户帐号和密码到服务器请求网络或者本地数据信息，并且解析成下面的集合
    private void getListData() {
//        ShoppingCanst.shopBeanlist = new ArrayList<ShopBean>();
//
//
//        ShopBean bean = new ShopBean();
//        bean.setShopId(1);
//        bean.setShopPicture(R.drawable.shoes1);
//        bean.setStoreName("花花公子");
//        bean.setShopName("斯米尔英伦风男鞋");
//        bean.setShopDescription("颜色：蓝色，尺码：41");
//        bean.setShopPrice(199);
//        bean.setShopNumber(1);
//        bean.setChoosed(false);//是否被勾选
//
//
//        ShoppingCanst.shopBeanlist.add(bean);
//
//
//        ShopBean bean2 = new ShopBean();
//        bean2.setShopId(2);
//        bean2.setShopPicture(R.drawable.shoes2);
//        bean2.setStoreName("木林森");
//        bean2.setShopName("Camel 骆驼男鞋");
//        bean2.setShopDescription("颜色：蓝色，尺码：41");
//        bean2.setShopPrice(399);
//        bean2.setShopNumber(1);
//        bean2.setChoosed(false);
//        ShoppingCanst.shopBeanlist.add(bean2);
//
//
//        ShopBean bean3 = new ShopBean();
//        bean3.setShopId(3);
//        bean3.setShopPicture(R.drawable.shoes3);
//        bean3.setStoreName("西瑞");
//        bean3.setShopName("雷艾新款男鞋子");
//        bean3.setShopDescription("颜色：黑色，尺码：41");
//        bean3.setShopPrice(198);
//        bean3.setShopNumber(1);
//        bean3.setChoosed(false);
//        ShoppingCanst.shopBeanlist.add(bean3);
//
//
//        ShopBean bean4 = new ShopBean();
//        bean4.setShopId(4);
//        bean4.setShopPicture(R.drawable.shoes4);
//        bean4.setStoreName("古奇天伦");
//        bean4.setShopName("奥康男鞋");
//        bean4.setShopDescription("颜色：蓝色，尺码：41");
//        bean4.setShopPrice(599);
//        bean4.setShopNumber(1);
//        bean4.setChoosed(false);
//        ShoppingCanst.shopBeanlist.add(bean4);
    }

    //事件点击监听器
    private final class ClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.all_check:        //全选
                    selectedAll();
                    break;
                case R.id.delete:            //删除
                    String shopIndex = deleteOrCheckOutShop();
                    if(shopIndex != null && shopIndex.length() >0){
                        showDialogDelete(shopIndex);
                    }else{
                        Toast.makeText(MainActivity.this,"购物车里面已经没有商品了",Toast.LENGTH_SHORT).show();
                    }
                    //需要同步服务器

                    break;
                case R.id.checkOut:            //结算
                    goCheckOut();
                    break;
            }
        }
    }

    //结算
    private void goCheckOut() {
        if(shopBeansMain != null && shopBeansMain.size() >0){
            String shopIndex = deleteOrCheckOutShop();
            Intent checkOutIntent = new Intent(MainActivity.this, CheckOutActivity.class);
            checkOutIntent.putExtra("shopIndex", shopIndex);
            System.out.println("shopIndex=="+shopIndex);
            startActivity(checkOutIntent);
        }else{
            Toast.makeText(MainActivity.this,"购物车里面已经没有商品了",Toast.LENGTH_SHORT).show();
        }

    }

    //全选或全取消
    private void selectedAll() {
        for (int i = 0; i < shopBeansMain.size(); i++) {
            ShopAdapter.getIsSelected().put(i, flag);//设置为不勾选
        }
        adapter.notifyDataSetChanged();//执行适配器的 getCount()-->getView();
        //把结算布局隐藏
        layout.setVisibility(View.GONE);
    }

    //删除或结算商品
    private String deleteOrCheckOutShop() {
        StringBuffer sb = new StringBuffer();
        if(shopBeansMain != null && shopBeansMain.size() >0){

            for (int i = 0; i < shopBeansMain.size(); i++) {
                if (ShopAdapter.getIsSelected().get(i)) {//谁被勾选了
                    sb.append(i);//当前列表的位置
                    sb.append(",");
                }
            }
            sb.deleteCharAt(sb.length() - 1);

        }
        System.out.println(sb.toString());
        return sb.toString();
    }

    //弹出对话框询问用户是否删除被选中的商品
    private void showDialogDelete(String str) {
        final String[] delShopIndex = str.split(",");//1,3
        new AlertDialog.Builder(MainActivity.this)
                .setMessage("您确定删除这" + delShopIndex.length + "商品吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {


                        if (delShopIndex != null && delShopIndex.length > 0) {

                            for (int i = delShopIndex.length-1; i >=0 ; i--) {

                                int index = Integer.valueOf(i);
                                //当前内存中也移除
                                shopBeansMain.remove(index);
                                //备份中的也移除
//					           ShoppingCanst.shopBeanlist.remove(index);

                            }

                        }
                        ShoppingCanst.shopBeanlist = shopBeansMain;
                        flag = false;
                        selectedAll();    //删除商品后，取消所有的选择
                        flag = true;    //刷新页面后，设置Flag为true，恢复全选功能
                    }
                }).setNegativeButton("取消", null)
                .create().show();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == TOTAL_PRICE) {    //更改选中商品的总价格
                float price = (Float) msg.obj;
                if (price > 0) {
                    popTotalPrice.setText("￥" + price);
                    layout.setVisibility(View.VISIBLE);//显示布局
                } else {
                    layout.setVisibility(View.GONE);
                }
            } else if (msg.what == SELECTED_SHOP) {
                //所有列表中的商品全部被选中，让全选按钮也被选中
                //flag记录是否全被选中
                //true所有条目全部被选中  false还有条目没有被选中
                flag = !(Boolean) msg.obj;
                checkBox.setChecked((Boolean) msg.obj);
            }
        }
    };

}
