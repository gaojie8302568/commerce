package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

/**
 * Ʒ�ƽӿ�
 */
public interface BrandService {
    public List<TbBrand> findAll();

    /**
     * Ʒ�Ʒ�ҳ
     * @param pageNum ��ǰҳ��
     * @param pageSize ÿҳ��¼��
     * @return
     */
   public PageResult findPage(int pageNum, int pageSize);

    /**
     * ����Ʒ��
     * @param brand
     */
   public void add(TbBrand brand);

    /**
     * ����id��ѯʵ��
     * @param id
     * @return
     */
   public TbBrand findOne(Long id);

    /**
     * �޸�
     * @param brand
     */
   public  void update(TbBrand brand);

    /**
     * ɾ��
     * @param ids
     */
   public void delete(Long[] ids);


    /**
     * ������ѯ  Ʒ�Ʒ�ҳ
     * @param pageNum ��ǰҳ��
     * @param pageSize ÿҳ��¼��
     * @return
     */
   public PageResult findPage( TbBrand brand, int pageNum, int pageSize);

    /**
     * ���������б�
     * @return
     */
   public List<Map> selectOptionList();

}
