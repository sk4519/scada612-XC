package cn.bp.scada.modle.scada;

import lombok.Data;

/**
 * 设备异常和后测回传上位机
 */

@Data
public class ScadaResponShow {
    private String result_flag;// OK/NG/PASS
    private String flow_code;// 流程码
    private String result_message;// 结果信息
    private String mo_num;//
    private String con_sn;
    private String status;
}
