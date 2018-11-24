package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;
import util.HttpClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {
    @Value("${appid}")
    private String appid;
    @Value("${partner}")
    private String partner;
    @Value("${partnerkey}")
    private String partnerkey;


    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //1参数封装
        Map<String, String> param = new HashMap();//创键参数
        param.put("appid", appid);//公众号ID
        param.put("mch_id", partner);//商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串 //用于和密钥加密算法的
        param.put("body", "品优购");//商品描述
        param.put("out_trade_no", out_trade_no);//商户订单号
        param.put("total_fee", total_fee);//总金额(最小值分)
        param.put("spbill_create_ip", "127.0.0.1");//IP
        param.put("notify_url", "http://www.itcast.cn"); //回调地址
        param.put("trade_type", "NATIVE"); //交易类型

        try {
            //2发送请求
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("请求的参数:" + paramXml);

            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);
            client.setXmlParam(paramXml);
            client.post();

            //3获取结果
            String result = client.getContent();
            Map<String, String> mapResult = WXPayUtil.xmlToMap(result);
            System.out.println("结果是:" + mapResult);
            Map map = new HashMap();
            map.put("code_url", mapResult.get("code_url"));//生成支付二维码的链接
            map.put("out_trade_no", out_trade_no);
            map.put("total_fee", total_fee);
            return map;

        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }

    @Override
    public Map queryPayStatus(String out_trade_no) {
        //1封装参数
        Map param = new HashMap();
        param.put("appid",appid);
        param.put("mch_id",partner);
        param.put("out_trade_no",out_trade_no);
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        try {
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
            //2发送请求

            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
             httpClient.setHttps(true);
             httpClient.setXmlParam(paramXml);
             httpClient.post();
            //3获取结果
            String result = httpClient.getContent();
            Map<String, String> mapResult = WXPayUtil.xmlToMap(result);
            System.out.println("调用查询API返回结果为:"+mapResult);
             return mapResult;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
