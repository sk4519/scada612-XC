package cn.bp.scada.controller.scada;

import cn.bp.scada.common.utils.excel.ExcelUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ExcelExport {

    @RequestMapping(value="/exportExcel")
    public void exportExcel07 (HttpServletResponse response)  {
        String[] cellTitle = {"点检项目编号","点检项目名称","设备名称","点检类别名称","序号","备注","是否启用","是否紧急","创建人","创建时间"};
        String s = "";
        try {
            //需要导出的数据
            List<String[]> dataList = new ArrayList<String[]>();
            dataList.add(new String[]{"编号1","项目名称","二楼分拣线设备","周保养","1","备注1","Y","N","admin","2020-09-21 11:22:28"});
            dataList.add(new String[]{"编号2","项目名称2","一线罗希望内存自动装配","年保养","2","备注2","Y","N","admin","2020-09-21 11:22:28"});
              ExcelUtils.exportExcel07(response, cellTitle, dataList);
        }catch (Exception e) {
            e.printStackTrace();
        }

        return ;
    }
}
