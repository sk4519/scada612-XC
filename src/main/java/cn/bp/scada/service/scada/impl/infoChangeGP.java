/*
package cn.bp.scada.service.scada.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import javax.annotation.Resource;
import java.util.Map;

*/
/**
 *
 *
 *改配订单迁移
 * *//*

public class infoChangeGP extends JdbcDaoSupport {

    @Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }

    */
/**
     *档案信息迁移
     *
     * *//*

    public String productInfoChange(String sn, String firstOrder,String secondOrder){
        String sql="update set O_NO=? where "
        Map<String, Object> map = this.getJdbcTemplate().queryForMap(sql, matnr, maktx, menge1, meins1);
        return "";
    }

    */
/**sapLabelPrintBm
     *过站信息迁移/修改过站状态
     *
     * *//*

    public String stationInfoChange(String sn){
        return "";
    }

    */
/**
     *需要拆除改配条目状态修改
     *
     * *//*

    public String materialInfoChange(String sn,String material){
        return "";
    }

    */
/**
     *测试信息状态修改
     *
     * *//*

    public String testInfoChange(String sn){
        return "";
    }

}
*/
