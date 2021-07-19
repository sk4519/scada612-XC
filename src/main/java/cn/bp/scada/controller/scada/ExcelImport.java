package cn.bp.scada.controller.scada;

import cn.bp.scada.common.utils.excel.ExcelUtils;
import cn.bp.scada.mapper.scada.Pack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@RestController
public class ExcelImport {

    @Autowired
    private ExcelUtils excelUtils;
    @Autowired Pack pack;

    @RequestMapping(value="/importExcel")
    public String InputExcel(@RequestParam(value = "excelfile") MultipartFile file, HttpServletRequest request,HttpServletResponse respon) {
        String s = "";
        if (!file.isEmpty()) {
            try {
                 HashMap<String, ArrayList<Map>> stringArrayListHashMap = excelUtils.analysisFile(file);
                 if(stringArrayListHashMap == null) {
                     return  "导入文件与模板不匹配，请下载模板填写内容";
                 }
                ArrayList<Map> arrayList = stringArrayListHashMap.get("dataList");
                 List<String> ckList = new ArrayList<>();
              for (int i = 0; i<arrayList.size(); i++) {
                  ckList.add(arrayList.get(i).get("CK_CD").toString());
                  arrayList.get(i);
                System.out.println(arrayList.get(i));
              }
                 List<String> strings = pack.seleCKCD(ckList);
              if(strings.size() > 0) {
                  return "项目编号不能重复";
              }
                pack.insertCheck(arrayList);

            s = "导入成功";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return s;
    }
}
