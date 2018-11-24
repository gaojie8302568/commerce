package com.pinyougou.cat.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cat.service.CatService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CatServiceImpl implements CatService {
    @Autowired
    private TbItemMapper tbItemMapper;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1根据skuid查询商品明细的sku对象
        TbItem item = tbItemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!item.getStatus().equals("1")) {
            throw new RuntimeException("商品未审核");
        }
        //2根据sku对象的到商家id
        String sellerId = item.getSellerId();
        //3根据商家id在购物车列表中查询购物车对象
        Cart cart = searchCartBySellerId(cartList, sellerId);
        if (cart == null) {
            //4如果购物车列表中不存在该商家的购物车
            //4.1创建一个新的购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());//商家名称
            TbOrderItem orderItem = createOrderItem(item, num);
            List<TbOrderItem> orderItemList = new ArrayList<>();//创建一个购物车明细列表
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            //4.2将新的购物车对象添加到购物车列表中
            cartList.add(cart);

        } else { //5如果购物车列表中存在该商家的购物车
            //判断该商品是否在该购物车的明细列表中存在
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if(orderItem==null){
                //5.1如果不存在,创建新的购物车明细对象,并添加到该购物车的明细列表中
                 orderItem=createOrderItem(item,num);
                 cart.getOrderItemList().add(orderItem);
            }else {
                //5.2如果存在,在原有的明细列表中的数量上添加数量,并更新金额
                orderItem.setNum(orderItem.getNum()+num);//更改商品在购物车列表中的数量
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*orderItem.getPrice().doubleValue()));//重新计算金额
                 //当明细的数量小于等于0,移除此明细
                  if(orderItem.getNum()<=0){
                   cart.getOrderItemList().remove(orderItem);
                  }
                  //当购物车的明细数量为0时,在购物车列表中移除此购物车
                  if(cart.getOrderItemList().size()==0){
                      cartList.remove(cart);
                  }
            }
        }
        return cartList;
    }

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 从redis中提取购物车列表
     * @param username
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中提取购物车:"+username);
      List<Cart>  cartList =(List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
         if(cartList==null){
             cartList=new ArrayList<>();
         }


        return cartList;
    }
    /**
     * 将购物车列表存入redis
     * @param username
     * @param cartList
     */
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("往redis中存入购物车:"+username);
       redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    /**
     * 合并购物车
     * @param cartList1
     * @param cartList2
     * @return
     */
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList2) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                addGoodsToCartList(cartList1,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return cartList1;
    }

    /**
     * 根据商家ID在购物车列表中查询购物车对象
     *
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {

        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }

    /**
     * 根据skuID在购物车明细列表中查询购物车明细
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().longValue() == itemId.longValue()) {
                return orderItem;
            }
        }
        return null;
    }


    /**
     * 创建购物车明细对象
     *
     * @param item
     * @param num
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        if(num<=0){
            throw new RuntimeException("数量非法");
        }
        TbOrderItem orderItem = new TbOrderItem();//创建新的购物车明细对象
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
        return orderItem;
    }


}
