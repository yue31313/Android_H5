package com.example.shoppingcart;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.example.layout.NoScrollListView;
import com.example.pay.PayResult;
import com.example.pay.SignUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class CheckOutActivity extends Activity {
	
	private Button sureCheckOut;		//确认购买
	private TextView addresseeName;		//收货人姓名
	private TextView smearedAddress;	//收货人区地址
	private TextView detailAddress;		//收货人详细地址
	private TextView checkOutAllPrice;	//结算的总金额
	private TextView title_left;		//title左标题,返回
	private TextView title_center;		//title中间标题
	private RelativeLayout addressRelative;	  //显示收货人信息的布局
	private NoScrollListView checkOutListView;//商品列表
	
	private CheckOutAdapter adapter;
	private List<ShopBean> list;			  //结算商品数据集合
	private List<AddressBean> addressList;	  //收货人地址数据集合
	
	private static int REQUESTCODE = 1;		  //跳转请求码
	private float allPrice = 0;				  //购买商品需要的总金额

	//--------------------下面是支付相关代码----------------------------

	/**
	 * 商品的描述信息
	 */
	private String desc = "";

	// 商户PID
	public static final String PARTNER = "2088911876712776";
	// 商户收款账号
	public static final String SELLER = "chenlei@atguigu.com";
	// 商户私钥，pkcs8格式
	public static final String RSA_PRIVATE = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALhvTVrEXv65ppIh\n" +
			"f32Ywf2AaYs53YGa4QuptRI08kY2u5FivZ055llp1cdQ8TysI3biY6je9Yyy8f/L\n" +
			"9k1jzE/nmKb7BjKN+mSrrdKyGmbkbc617igTgMyLqshvNDomwSRws5sj7EOQfGCE\n" +
			"vgbnFUmMM9ucMtS59rHTLys4KjiBAgMBAAECgYAw/4jae18NZz8EhZe8Q1AOTAY+\n" +
			"McJSQ1ZUZeKwEpbm5nWQW14qng4/ZOmhiTR9vPGzDNQTMiCkH4pBPeuShdfqUxSd\n" +
			"Oc0oK750PXcXue0VZEHTabt0Ue7OBACKWHU3cSF12UmJ2Qp99ECtLpNfKKlUYaCG\n" +
			"xCNBAE7jkRLnOoOh/QJBANrEuLI0XqLiGWGY6UCSnwUS3uhkf271fR5WTXRFnnyp\n" +
			"a8dMj0knJNJVph2cD830OeCnEvod4axqGv3MUC0RaCMCQQDX0rw+KzW5+f2goqn/\n" +
			"jYZsQRQRXcD4mfSfPVyS3kJo9VDYWDktLWGw3WktiBG18qgk/HqyFCsI9p3rzUYm\n" +
			"rrULAkAXR6PX4tbHQoQs7Ja1tmSRp6Zs2HTvG9SKq+hmHRp2bZj4hl8hC7dRYqfg\n" +
			"ssDDy9901pKgZxmivU/N/jXx9c49AkEAqpoFfWswrBMl3r3WfZEE6jvDPjsLGDiy\n" +
			"utGJhPs2KbuJLYsHn2OZLnLG+lvuDBKFwb7myi/RGOPBT61TP66oowJAY5sGwKJ+\n" +
			"KV4msEQn0hbZ1gVBeo3+aOzZnSVpjgxYOSpuAA7pNWvNfc0hBRNnw3ZXjpDWQAiV\n" +
			"qGzmejTlwkpFdg==";
	// 支付宝公钥
	public static final String RSA_PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC4b01axF7+uaaSIX99mMH9gGmL\n" +
			"Od2BmuELqbUSNPJGNruRYr2dOeZZadXHUPE8rCN24mOo3vWMsvH/y/ZNY8xP55im\n" +
			"+wYyjfpkq63Sshpm5G3Ote4oE4DMi6rIbzQ6JsEkcLObI+xDkHxghL4G5xVJjDPb\n" +
			"nDLUufax0y8rOCo4gQIDAQAB";

	private static final int SDK_PAY_FLAG = 1;

	private Handler mHandler = new Handler() {
		@SuppressWarnings("unused")
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case SDK_PAY_FLAG: {
					PayResult payResult = new PayResult((String) msg.obj);
					/**
					 * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/
					 * detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665&
					 * docType=1) 建议商户依赖异步通知
					 */
					String resultInfo = payResult.getResult();// 同步返回需要验证的信息

					String resultStatus = payResult.getResultStatus();
					// 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
					if (TextUtils.equals(resultStatus, "9000")) {
						Toast.makeText(CheckOutActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
					} else {
						// 判断resultStatus 为非"9000"则代表可能支付失败
						// "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
						if (TextUtils.equals(resultStatus, "8000")) {
							Toast.makeText(CheckOutActivity.this, "支付结果确认中", Toast.LENGTH_SHORT).show();

						} else {
							// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
							Toast.makeText(CheckOutActivity.this, "支付失败", Toast.LENGTH_SHORT).show();

						}
					}
					break;
				}
				default:
					break;
			}
		};
	};



	/**
	 * 支付宝支付
	 */
	private void alipay(float allPrice) {

		//得到价格
		String price = allPrice+"";

		if (TextUtils.isEmpty(PARTNER) || TextUtils.isEmpty(RSA_PRIVATE) || TextUtils.isEmpty(SELLER)) {


			new AlertDialog.Builder(this).setTitle("警告").setMessage("需要配置PARTNER | RSA_PRIVATE| SELLER")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialoginterface, int i) {
							//
							finish();
						}
					}).show();
			return;
		}


		if (TextUtils.isEmpty(price) ||TextUtils.isEmpty(desc)) {

			new AlertDialog.Builder(this).setTitle("提示").setMessage("需要填写报名费用 |费用用途")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialoginterface, int i) {
							//
//                            finish();
						}
					}).show();
			return;
		}


		/**
		 * 第一个参数：商品的名称（订单的信息）；第二参数是：商品的描述，第三个是商品的价格
		 */
		String orderInfo = getOrderInfo(desc, desc+price+"元", price);

		/**
		 * 特别注意，这里的签名逻辑需要放在服务端，切勿将私钥泄露在代码中！
		 */
		String sign = sign(orderInfo);
		try {
			/**
			 * 仅需对sign 做URL编码
			 */
			sign = URLEncoder.encode(sign, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		/**
		 * 完整的符合支付宝参数规范的订单信息
		 */
		final String payInfo = orderInfo + "&sign=\"" + sign + "\"&" + getSignType();

		Runnable payRunnable = new Runnable() {

			@Override
			public void run() {
				// 构造PayTask 对象
				PayTask alipay = new PayTask(CheckOutActivity.this);
				// 调用支付接口，获取支付结果
				String result = alipay.pay(payInfo, true);

				Message msg = new Message();
				msg.what = SDK_PAY_FLAG;
				msg.obj = result;
				mHandler.sendMessage(msg);
			}
		};

		// 必须异步调用
		Thread payThread = new Thread(payRunnable);
		payThread.start();
	}


	/**
	 * create the order info. 创建订单信息
	 *
	 */
	private String getOrderInfo(String subject, String body, String price) {

		// 签约合作者身份ID
		String orderInfo = "partner=" + "\"" + PARTNER + "\"";

		// 签约卖家支付宝账号
		orderInfo += "&seller_id=" + "\"" + SELLER + "\"";

		// 商户网站唯一订单号
		orderInfo += "&out_trade_no=" + "\"" + getOutTradeNo() + "\"";

		// 商品名称
		orderInfo += "&subject=" + "\"" + subject + "\"";

		// 商品详情
		orderInfo += "&body=" + "\"" + body + "\"";

		// 商品金额
		orderInfo += "&total_fee=" + "\"" + price + "\"";

		// 服务器异步通知页面路径
		orderInfo += "&notify_url=" + "\"" + "http://notify.msp.hk/notify.htm" + "\"";

		// 服务接口名称， 固定值
		orderInfo += "&service=\"mobile.securitypay.pay\"";

		// 支付类型， 固定值
		orderInfo += "&payment_type=\"1\"";

		// 参数编码， 固定值
		orderInfo += "&_input_charset=\"utf-8\"";

		// 设置未付款交易的超时时间
		// 默认30分钟，一旦超时，该笔交易就会自动被关闭。
		// 取值范围：1m～15d。
		// m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
		// 该参数数值不接受小数点，如1.5h，可转换为90m。
		orderInfo += "&it_b_pay=\"30m\"";

		// extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
		// orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

		// 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
		orderInfo += "&return_url=\"m.alipay.com\"";

		// 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
		// orderInfo += "&paymethod=\"expressGateway\"";

		return orderInfo;
	}

	/**
	 * sign the order info. 对订单信息进行签名
	 *
	 * @param content
	 *            待签名订单信息
	 */
	private String sign(String content) {
		return SignUtils.sign(content, RSA_PRIVATE);
	}

	/**
	 * get the sign type we use. 获取签名方式
	 *
	 */
	private String getSignType() {
		return "sign_type=\"RSA\"";
	}

	/**
	 * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
	 *
	 */
	private String getOutTradeNo() {
		SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss", Locale.getDefault());
		Date date = new Date();
		String key = format.format(date);

		Random r = new Random();
		key = key + r.nextInt();
		key = key.substring(0, 15);
		return key;
	}

	//-------------------支付相关代码-------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_checkout);
		initView();
		initData();
	}
	
	//初始化数据
	private void initData(){
		getData();

		getAddressData();//收货地址


		checkOutAllPrice.setText("总共有"+list.size()+"个商品       总价￥"+allPrice);

		showInfo(1);	//默认显示第一条地址信息
		
		adapter = new CheckOutAdapter(this, list, R.layout.checkout_item);

		checkOutListView.setAdapter(adapter);
	}

	/**
	 * 得到购物车里面的产品并且计算总价格
	 */
	private void getData() {
		list = new ArrayList<ShopBean>();
		String shopIndex = getIntent().getStringExtra("shopIndex");
		String[] shopIndexs = shopIndex.split(",");
		for(String s : shopIndexs){
			int position = Integer.valueOf(s);
			ShopBean bean = ShoppingCanst.shopBeanlist.get(position);
			allPrice += bean.getShopNumber()*bean.getShopPrice();
			desc += bean.getShopName()+",";//设置描述信息
			list.add(bean);
		}

		System.out.println("desc=="+desc);
	}

	//初始化UI界面
	private void initView(){
		sureCheckOut = (Button) findViewById(R.id.sureCheckOut);
		addresseeName = (TextView) findViewById(R.id.addresseeName);
		smearedAddress = (TextView) findViewById(R.id.smearedAddress);
		detailAddress = (TextView) findViewById(R.id.detailAddress);
		checkOutAllPrice = (TextView) findViewById(R.id.checkOutAllPrice);
		title_left = (TextView) findViewById(R.id.title_left);
		title_center = (TextView) findViewById(R.id.title_center);
		checkOutListView = (NoScrollListView) findViewById(R.id.checkOutListView);
		addressRelative = (RelativeLayout) findViewById(R.id.addressRelative);
		
		ClickListener cl = new ClickListener();
		title_left.setText(R.string.sureOrder);
		title_center.setText(R.string.checkOut);
		title_left.setOnClickListener(cl);
		sureCheckOut.setOnClickListener(cl);
		addressRelative.setOnClickListener(cl);
	}
	
	//显示收货人姓名地址等信息
	private void showInfo(int index){
		AddressBean bean = addressList.get(index);
		addresseeName.setText(bean.getName());
		smearedAddress.setText(bean.getSmearedAddress());
		detailAddress.setText(bean.getDetailAddress());
	}
	
	//获取收货人地址数据集合，真实情况这个数据来自服务器
	private void getAddressData(){
		ShoppingCanst.addressList = new ArrayList<AddressBean>();


		AddressBean bean = new AddressBean();
		bean.setName("阿福");
		bean.setSmearedAddress("贵州省黎平县");
		bean.setDetailAddress("黎平一中  15527196048");
		ShoppingCanst.addressList.add(bean);


		AddressBean bean2 = new AddressBean();
		bean2.setName("阿福");
		bean2.setSmearedAddress("北京昌平");
		bean2.setDetailAddress("北京昌平xxx家园  18140549110");
		ShoppingCanst.addressList.add(bean2);

		addressList = ShoppingCanst.addressList;
	}
	
	//修改地址
	private void updateAddress(){
		Intent intent = new Intent(CheckOutActivity.this,UpdateAddressActivity.class);
		startActivityForResult(intent, REQUESTCODE);
	}
	
	//事件点击监听器
	private final class ClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			int rid = v.getId();
			if(rid == R.id.sureCheckOut){	//确认点击按钮
//				Toast.makeText(getApplicationContext(), "结算完成，总共花费￥"+allPrice, Toast.LENGTH_LONG).show();
				alipay(allPrice);
			}else if(rid == R.id.addressRelative){	//修改地址
				updateAddress();
			}else if(rid == R.id.title_left){		//左标题返回
				finish();
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUESTCODE && resultCode == RESULT_OK){
			Bundle bundle = data.getExtras();
			handler.sendMessage(handler.obtainMessage(1, bundle.getInt("addressIndex")));
		}
	}
	
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == 1){		//异步更改地址	
				int tempIndex = (Integer)msg.obj;
				showInfo(tempIndex);
			}
		}
	};
}
