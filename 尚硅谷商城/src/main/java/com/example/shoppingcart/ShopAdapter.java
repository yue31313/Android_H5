package com.example.shoppingcart;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

@SuppressLint("HandlerLeak")
public class ShopAdapter extends BaseAdapter {

    /**
     * 商品数量
     */
    private static final int SHOP_NUM = 1;
    /**
     * 更新对话框的数据
     */
    private static final int UPDATE_DIALOG_NUMBER = 2;
    /**
     * MainActivity中的Handler
     */
    private Handler mHandler;
    private int resourceId;                //适配器视图资源ID
    private Context context;            //上下午对象
    private List<ShopBean> shopBeansAdaptes;        //数据集合List
    private LayoutInflater inflater;    //布局填充器

    private static HashMap<Integer, Boolean> isSelected;

    private int number = 0;            //记录对话框中的数量
    private EditText editText;        //对话框中数量编辑器

    public ShopAdapter(Context context, List<ShopBean> list
            , Handler mHandler, int resourceId) {
        this.shopBeansAdaptes = list;
        this.context = context;
        this.mHandler = mHandler;
        this.resourceId = resourceId;
        inflater = LayoutInflater.from(context);
        isSelected = new HashMap<Integer, Boolean>();
        initDate();
    }

    // 初始化isSelected的数据
    private void initDate() {
        for (int i = 0; i < shopBeansAdaptes.size(); i++) {
            getIsSelected().put(i, false);//false在购物车里面没有被旋转
        }
    }

    public static HashMap<Integer, Boolean> getIsSelected() {
        return isSelected;
    }

    public static void setIsSelected(HashMap<Integer, Boolean> isSelected) {
        ShopAdapter.isSelected = isSelected;
    }

    @Override
    public int getCount() {
        return shopBeansAdaptes.size();
    }

    @Override
    public Object getItem(int position) {
        return shopBeansAdaptes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ShopBean bean = shopBeansAdaptes.get(position);
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(resourceId, null);
            holder = new ViewHolder();
            holder.shop_photo = (ImageView) convertView.findViewById(R.id.shop_photo);
            holder.shop_name = (TextView) convertView.findViewById(R.id.shop_name);
            holder.shop_description = (TextView) convertView.findViewById(R.id.shop_description);
            holder.shop_price = (TextView) convertView.findViewById(R.id.shop_price);
            holder.shop_number = (TextView) convertView.findViewById(R.id.shop_number);
            holder.shop_check = (CheckBox) convertView.findViewById(R.id.shop_check);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.shop_photo.setImageResource(bean.getShopPicture());
        holder.shop_name.setText(bean.getShopName());
        holder.shop_description.setText(bean.getShopDescription());
        holder.shop_price.setText("￥" + bean.getShopPrice());

        //购买数量
        holder.shop_number.setTag(position);
        //设置购买数量
        holder.shop_number.setText(String.valueOf(bean.getShopNumber()));
        //设置点击修改购买数量
        holder.shop_number.setOnClickListener(new ShopNumberClickListener());





        //设置是否勾选状态
        holder.shop_check.setTag(position);
        holder.shop_check.setChecked(getIsSelected().get(position));

        holder.shop_check.setOnCheckedChangeListener(new CheckBoxChangedListener());


        return convertView;
    }

    private final class ViewHolder {
        public ImageView shop_photo;        //商品图片
        public TextView shop_name;            //商品名称
        public TextView shop_description;    //商品描述
        public TextView shop_price;            //商品价格
        public TextView shop_number;        //商品数量
        public CheckBox shop_check;            //商品选择按钮
    }

    //数量TextView点击监听器
    private final class ShopNumberClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            //获取商品的数量
            String str = ((TextView) v).getText().toString();
            int shopNum = Integer.valueOf(str);
            showDialog(shopNum, (TextView) v);
        }
    }


    /**
     * 弹出对话框更改商品的数量
     *
     * @param shopNum 商品原来的数量
     * @param textNum Item中显示商品数量的控件
     */
    private void showDialog(int shopNum, final TextView textNum) {
        View view = inflater.inflate(R.layout.number_update, null);
        Button btnSub = (Button) view.findViewById(R.id.numSub);
        Button btnAdd = (Button) view.findViewById(R.id.numAdd);
        editText = (EditText) view.findViewById(R.id.edt);
        editText.setText(String.valueOf(shopNum));
        btnSub.setOnClickListener(new ButtonClickListener());
        btnAdd.setOnClickListener(new ButtonClickListener());
        number = shopNum;
        new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        Toast.makeText(context,"当前线程："+Thread.currentThread().getName(),Toast.LENGTH_SHORT).show();
                        //将用户更改的商品数量更新到服务器
                        int position = (Integer) textNum.getTag();//位置
                        shopBeansAdaptes.get(position).setShopNumber(number);//设置商品的数量
                        ////更改商品数量
//                        textNum.setText(number);//不能直接在对话框中更新
                        handler.sendMessage(handler.obtainMessage(SHOP_NUM, textNum));
                    }
                }).setNegativeButton("取消", null)
                .create().show();
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == SHOP_NUM) {        //更改商品数量
                ((TextView) msg.obj).setText(String.valueOf(number));
                //更改商品数量后，通知Activity更新需要付费的总金额
                mHandler.sendMessage(mHandler.obtainMessage(MainActivity.TOTAL_PRICE, getTotalPrice()));
            } else if (msg.what == UPDATE_DIALOG_NUMBER) {//更改对话框中的数量
                editText.setText(String.valueOf(number));
            }
        }
    };

    //Button点击监听器
    private final class ButtonClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.numSub) {
                if (number > 1) {
                    number--;
                    handler.sendEmptyMessage(UPDATE_DIALOG_NUMBER);
                }
            } else if (v.getId() == R.id.numAdd) {
                number++;
                handler.sendEmptyMessage(UPDATE_DIALOG_NUMBER);
            }
        }
    }

    //CheckBox选择改变监听器
    private final class CheckBoxChangedListener implements OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton cb, boolean flag) {

            int position = (Integer) cb.getTag();//位置

            getIsSelected().put(position, flag);//勾选和非勾选状态

            /**
             * 把列表中的修改某个对象为选中
             */
            ShopBean bean = shopBeansAdaptes.get(position);
            bean.setChoosed(flag);//勾选

            //发消息，和计算总价格，把价格发到MainActivity
            mHandler.sendMessage(mHandler.obtainMessage(MainActivity.TOTAL_PRICE, getTotalPrice()));
            //如果所有的物品全部被选中，则全选按钮也默认被选中
            mHandler.sendMessage(mHandler.obtainMessage(MainActivity.SELECTED_SHOP, isAllSelected()));
        }
    }

    /**
     * 计算选中商品的金额
     *
     * @return 返回需要付费的总金额
     */
    private float getTotalPrice() {
        ShopBean bean = null;
    float totalPrice = 0;
    for (int i = 0; i < shopBeansAdaptes.size(); i++) {
        bean = shopBeansAdaptes.get(i);//得到的是商品对象
        if (bean.isChoosed()) {//如果商品被选中，才计算价格
            totalPrice += bean.getShopNumber() * bean.getShopPrice();
        }
    }
    return totalPrice;
}

    /**
     * 判断是否购物车中所有的商品全部被选中
     *
     * @return  true所有条目全部被选中  false还有条目没有被选中
     */
    private boolean isAllSelected() {
        boolean flag = true;
        for (int i = 0; i < shopBeansAdaptes.size(); i++) {
            if (!getIsSelected().get(i)) {
                flag = false;
                break;
            }
        }
        return flag;
    }
}
