package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {
        Map map = new HashMap();
      /*  Query query = new SimpleQuery("*:*");
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        ScoredPage<TbItem>  page =solrTemplate.queryForPage(query, TbItem.class);
         map.put("rows",page.getContent());*/

      //空格处理
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords",keywords.replace(" ",""));


        //1查询列表
        map.putAll(searchList(searchMap));
        //2分组查询 商品列表
        List<String> categoryList = searchCategoryList(searchMap);//关键字去掉空格
        map.put("categoryList", categoryList);
        //3查询品牌和规格列表
        String category = (String) searchMap.get("category");
        if (!category.equals("")) {
            map.putAll(searchBrandAndSpecList(category));
        } else {
            if (categoryList.size() > 0) {
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }


        return map;
    }
    /**
     *导入列表
     * @param list
     */
    @Override
    public void importList(List list) {

        solrTemplate.saveBeans(list);
        solrTemplate.commit();


    }

    /**
     * 删除商品列表
     * @param goodsIds  (spuID)
     */
    @Override
    public void deleteByGoodsIds(List goodsIds) {
        Query query = new SimpleQuery("*:*");
        Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
         query.addCriteria(criteria);
       solrTemplate.delete(query);
       solrTemplate.commit();
    }

    //查询列表
    private Map searchList(Map searchMap) {
        Map map = new HashMap();


        //高亮显示
        HighlightQuery query = new SimpleHighlightQuery();
        //构建高量选项对象
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");//获取高亮域
        highlightOptions.setSimplePrefix("<em style='color:red'>");//前缀
        highlightOptions.setSimplePostfix("</em>");//后缀
        query.setHighlightOptions(highlightOptions);//为查询对象设置高亮选项

        //1.1关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //1.2 按照商品分类来进行筛选过滤
        if (!"".equals(searchMap.get("category"))) {//如果用户选着了分类才进行筛选
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.3 按照品牌来进行筛选过滤
        if (!"".equals(searchMap.get("brand"))) {//如果用户选着了分类才进行筛选
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.4 按照规格来进行筛选过滤
        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
            for (String key : specMap.keySet()) {
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_spec" + key).is(specMap.get(key));
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
        //1.5 按照价格过滤
        if (!"".equals(searchMap.get("price"))) {
            String[] price = ((String) searchMap.get("price")).split("-");
            if (!price[0].equals("0")) {//如果最低价格不等于0
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!price[1].equals("*")) {//如果最高价格不等于*
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
        //1.6 分页
        Integer pageNo = (Integer) searchMap.get("pageNo");//提取页码
        if (pageNo == null) {
            pageNo = 1;//默认当前第一页
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");//每页的记录数
        if (pageSize == null) {
            pageSize = 20;//默认20
        }
        query.setOffset((pageNo - 1) * pageSize);//从第几条记录查询
        query.setRows(pageSize);//每页的记录数
       //1.7按照价格排序
        String sortValue = (String) searchMap.get("sort");//升序ASC 降序DESC
        String sortField = (String) searchMap.get("sortField");//排序字段
        if(sortValue != null && !sortField.equals("")){
              if(sortValue.equals("ASC")){
                  Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                  query.addSort(sort);
              }
            if(sortValue.equals("DESC")){
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }





        //高亮页对象
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //高亮入口集合(每条记录的高亮入口)
        List<HighlightEntry<TbItem>> highlighted = page.getHighlighted();
        for (HighlightEntry<TbItem> entry : highlighted) {
            //获取高亮列表(个数)
            List<HighlightEntry.Highlight> highlightList = entry.getHighlights();
            if (highlightList.size() > 0 && highlightList.get(0).getSnipplets().size() > 0) {
                TbItem item = entry.getEntity();
                item.setTitle(highlightList.get(0).getSnipplets().get(0));
            }
        }
        map.put("rows", page.getContent());
        map.put("totalPages",page.getTotalPages());//总页数
        map.put("total",page.getTotalElements());//总记录数


        return map;
    }

    /**
     * 分组查询(查询商品分类列表)
     *
     * @param searchMap
     * @return
     */
    private List searchCategoryList(Map searchMap) {
        List list = new ArrayList();

        Query query = new SimpleQuery("*:*");
        //关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");//group by 相当于分组语句
        query.setGroupOptions(groupOptions);
        //获取分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //获取分组结果对象
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //获取分组入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //获取分组入口集合
        List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
        for (GroupEntry<TbItem> entry : entryList) {
            String groupValue = entry.getGroupValue();
            list.add(groupValue);//将分组的结果存入集合中
        }
        return list;
    }


    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据 商品分类名称查询品牌和规格列表
     *
     * @return category 商品分类名称
     */
    private Map searchBrandAndSpecList(String category) {
        Map map = new HashMap();
        //1根据商品分类名称的到模版ID
        Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);

        if (templateId != null) {
            //2根据模版ID获品牌列表
            List beandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
            map.put("brandList", beandList);
            //根据模版ID获取规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
            map.put("specList", specList);
        }
        return map;
    }


}
