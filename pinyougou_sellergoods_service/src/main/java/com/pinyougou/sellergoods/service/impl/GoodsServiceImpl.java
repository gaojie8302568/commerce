package com.pinyougou.sellergoods.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojogroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

   @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbBrandMapper brandMapper;
    @Autowired
    private TbSellerMapper sellerMapper;

    /**
     * 增加
     */
    @Override
    public void add(Goods goods) {

        goods.getGoods().setAuditStatus("0");//状态为:未审核
        goodsMapper.insert(goods.getGoods());//插入商品的基本信息
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());//将商品基本表的ID给商品扩展表
        goodsDescMapper.insert(goods.getGoodsDesc());//插入商品扩展表数据

          saveItem(goods);//插入sku商品数据

    }

    private void setItemValues(TbItem tbItem,Goods goods){
        //商品分类
        tbItem.setCategoryid(goods.getGoods().getCategory3Id());//三级分类ID
        tbItem.setCreateTime(new Date());//创建日期
        tbItem.setUpdateTime(new Date());//更新日期

        tbItem.setGoodsId(goods.getGoods().getId());//商品ID
        tbItem.setSellerId(goods.getGoods().getSellerId());//商家ID
        //分类名称
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
        tbItem.setCategory(itemCat.getName());
        //品牌名称
        TbBrand tbBrand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        tbItem.setBrand(tbBrand.getName());
        //商家名称(店名)
        TbSeller tbSeller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
        tbItem.setSeller(tbSeller.getNickName());
        //图片
        List<Map> imageList = JSON.parseArray( goods.getGoodsDesc().getItemImages(),Map.class);
        if(imageList.size()>0){
            tbItem.setImage((String) imageList.get(0).get("url"));
        }
    }
  //private开头的都是//提取出来的//插入sku列表数据
    private void saveItem(Goods goods){
        if("1".equals(goods.getGoods().getIsEnableSpec())){
            for (TbItem tbItem : goods.getItemList()) {
                //构建标题 spu名称+规格选项值
                String goodsName = goods.getGoods().getGoodsName();//spu名称
                Map<String,Object> map = JSON.parseObject( tbItem.getSpec());
                for (String key : map.keySet()) {
                    goodsName+=""+map.get(key);
                }
                tbItem.setTitle(goodsName);

                setItemValues(tbItem,goods);

                itemMapper.insert(tbItem);
            }
        }else {//没有启用规格
            TbItem tbItem = new TbItem();
            tbItem.setTitle(goods.getGoods().getGoodsName());//标题
            tbItem.setPrice(goods.getGoods().getPrice());//价格
            tbItem.setNum(99999);//库存数量
            tbItem.setStatus("1");//状态
            tbItem.setIsDefault("1");//默认
            tbItem.setSpec("{}");//规格
            setItemValues(tbItem,goods);

            itemMapper.insert(tbItem);
        }
    }


    /**
     * 修改
     */
    @Override
    public void update(Goods goods) {
        //跟新基本表数据
        goodsMapper.updateByPrimaryKey(goods.getGoods());
        //更新扩展表数据
        goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
        //删除原有的sku列表数据
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(goods.getGoods().getId());
        itemMapper.deleteByExample(example);
        //插入新的sku列表数据
        saveItem(goods);




    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Goods findOne(Long id) {
        Goods goods = new Goods();
        //查询商品表信息
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        goods.setGoods(tbGoods);
        //查询商品扩展表的信息
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        goods.setGoodsDesc(tbGoodsDesc);
        //读取sku列表
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<TbItem> tbItems = itemMapper.selectByExample(example);
        goods.setItemList(tbItems);

        return goods ;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setIsDelete("1");//表示逻辑删除
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();

            criteria.andIsDeleteIsNull();//指定条件为未逻辑删除的

        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
                //criteria.andSellerIdLike("%" + goods.getSellerId() + "%");
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }
            if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
            }

        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        for (Long id : ids) {
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setAuditStatus(status);
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
        
    }

    @Override
    public void updateIsMarketable(Long[] ids, String ismarketable) {
        for(Long id : ids){
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setIsMarketable(ismarketable);
            goodsMapper.updateByPrimaryKey(tbGoods);
        }

    }

    /**
     * 根据spu的id集合查询sku的列表
     * @param goodsIds
     * @param status
     * @return
     */
    public List<TbItem>  findItemListByGoodsIdListAndStatus(Long [] goodsIds,String status){
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo(status);//状态
        criteria.andGoodsIdIn(Arrays.asList(goodsIds));//指定条件;spuId集合
         return  itemMapper.selectByExample(example);
    }

}
