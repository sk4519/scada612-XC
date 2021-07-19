package cn.bp.scada.controller.sap;

import cn.bp.scada.sap.SapConnUtils;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
@Slf4j
@RestController
public class SapDeleProdcMsg extends JdbcDaoSupport {

    @Resource
    public void setJb(JdbcTemplate jb) {
        super.setJdbcTemplate(jb);
    }

    @Autowired
    private SapConnUtils sapUtils;


    /**
     * 根据sn全部删除
     *
     * @return
     */
    @RequestMapping("/sapDeleProdcMsgAll")
    public String sapMaintProdcMsgAll(JSONObject json) {

        System.out.println("进入删除产品档案");
        try {
            JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
            JCoFunction function = sapUtils.getFunction("ZRFC_PP_DEL_CPDA");//获取到函数
            //入参
            JCoParameterList parameterList = function.getImportParameterList();
            //设置入参
            parameterList.setValue("P_SN", Integer.parseInt(json.getString("sn"))); //整机SN
            log.info("删除产品档案入参："+parameterList);
            function.execute(jCoDestination);

            //出参
            JCoParameterList list1 = function.getExportParameterList();
            log.info("出参:  " + list1.getString("RETURN_MSG"));//S 成功  E 失败
        } catch (Exception e) {
            log.error("删除产品档案失败",e.toString());
        }
        return "vue";
    }

    /**
     * 删除产品档案信息
     *
     * @return
     */
    @RequestMapping("/sapDeleProdcMsg")
    public String sapMaintProdcMsg(JSONObject json) {

        System.out.println("进入删除产品档案");
        try {
            JCoDestination jCoDestination = sapUtils.jCoDestination;//接收连接对象
            JCoFunction function = sapUtils.getFunction("ZRFC_PP_DEL_CPDA");//获取到函数
            //入参
            JCoParameterList parameterList = function.getImportParameterList();
            //设置入参
            parameterList.setValue("P_SN", Integer.parseInt(json.getString("sn"))); //整机SN
            if (json.getString("type").equals("LCQN")) {
                parameterList.setValue("P_QN", json.getString("P_QN")); //散部qN
            } else { //原厂QN
                if (json.getString("type").equals("")) {
                    parameterList.setValue("P_QN", json.getString("P_QN")); //散部qN
                } else {
                    parameterList.setValue("P_YCQN", json.getString("P_YCQN")); //原厂QN
                }

            }
			log.info("删除产品档案入参："+parameterList);
            function.execute(jCoDestination);

            //出参
            JCoParameterList list1 = function.getExportParameterList();
            log.info("出参:  " + list1.getString("RETURN_MSG"));//S 成功  E 失败
        } catch (Exception e) {
          log.error("删除产品档案失败",e.toString());
        }
        return "vue";
    }
}
