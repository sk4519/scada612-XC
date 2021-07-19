package cn.bp.scada.modle.scada;

import lombok.Data;

import java.util.List;

/**
 * 统一设备应答主表
 */
@Data
public class ScadaRespon {

    private String result_flag;// OK/NG/PASS
    private String flow_code;// 流程码
    private String result_message;// 结果信息
    private String mo_num;//
    private String wo_num;//
    private String status; //老化架是否装满的标志
    private String con_sn;
    private List<Material> material;
    private String frame_stion; //老化架所在位置
    private String material_stion; //老化架产品所放位置
    private String work_sn; //返回物料分拣对应的工位编码给上位机
    private String op_flag;
    private String line_code;
    private String position;
    private String frame_status; //老化架是否可以拉走
    private String old_status; //是否把产品放到老化架上的标识字段
    private String mat_point;
    private String pro_mod; //型号（CE3000F:飞腾，  CE3000L:龙芯）
    private String zb_code; //主板编码

    private String item_nm;  //产品名称
    private String item_cd;  //产品型号
    private String jx_sn;  //序列号
    private String o_no;  //订单号
    private String sale_no;  //销售订单号
    private String pro_no;  //行项目号
    private String pro_nm;  //商品编码
    private String pro_cd;  //产品编码
    private String cpu_nm;  //cpu
    private String yp_nm;  //硬盘
    private String nc_nm;  //内存
    private String cd;  //光驱
    private String other;  //其他
    private String nvg_code;  //导航编码
    private String way;  //导轨
    private String net_card;   //网卡
    private String first;
    private String vreset;
    private String box_pn;

}
