package cn.bp.scada.service.scada.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.bp.scada.controller.scada.ScadaPackController;
import cn.bp.scada.controller.scada.ScadaToAgvBan;
import cn.bp.scada.controller.sap.SapLabelPrint;
import cn.bp.scada.modle.scada.ResultMessage;
import cn.bp.scada.common.utils.SpringContextUtil;

/**
 * scada请求数据的处理服务类
 *
 * @author wuzhining
 */
@Service
public class ScadaRequesDealService {
    @Autowired
    private MachineStopInfoServicempl machineStopInfoServicempl;

    public int isRespon = 0;
    public String responMsg = null;
    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    public String checkData(Object msg) {
        String result = null;
        try {
            JSONObject clientMessage = new JSONObject(msg.toString());
            Object obj = null;
            ResultMessage resultMessage = new ResultMessage();

            JSONObject msgObj = clientMessage.getJSONObject("obj");
            String answerId = clientMessage.getString("answerId");
            if (!answerId.equals("") && answerId != null && !answerId.equals("null")) {
                switch (answerId) {
                    case "A001":
                        isRespon = 1;
                        responMsg = msgObj.toString();
                        break;

                    default:
                        LOG.info("上位机返回没有A001");
                        break;
                }

                result = null;

            } else {
                resultMessage.setType("ScadaRespon");

                switch (msgObj.get("op_flag").toString()) {
                    case "B01": //载具验证:1.主板上料
                        ScadaAssembleServiceImpl checkBcb = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = checkBcb.checkData(clientMessage.get("obj"));
                        break;

                    case "B011": //底托载具校验
                        ScadaAssembleServiceImpl checkBcb2 = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = checkBcb2.downCheck(clientMessage.get("obj"));
                        break;

                    case "B014": //主板下线载具验证 &铭牌贴附 &.散热器安装载具验证
                        ScadaAssembleServiceImpl checkBcb3 = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = checkBcb3.checkZhuTest(clientMessage.get("obj"));
                        break;

                    case "B0144": //主板下线第一把扫描枪载具验证
                        ScadaAssembleServiceImpl checkBcb44 = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = checkBcb44.mainDownBef(clientMessage.get("obj"));
                        break;

                    case "B015": //主板下线解绑
                        ScadaAssembleServiceImpl checkBcb4 = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = checkBcb4.saveMaterial2(clientMessage.get("obj"));
                        if (msgObj.getString("device_sn").equals("ECDLC005")) {
                        machineStopInfoServicempl.insertStopInfo("ECDLC005");
                            //otherLostRequest.requestLost("主板组装A线体");
                    } else {
                        machineStopInfoServicempl.insertStopInfo("ECDLC205");
                            //otherLostRequest.requestLost("主板组装B线体");
                    }

                        break;

                    case "B02": //公用的保存物料有：1.主板上料   散热器安装   底托上料
                        ScadaAssembleServiceImpl savePcbMaterial = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = savePcbMaterial.saveMaterial(clientMessage.get("obj"));
                        break;

                    case "B03": //设备异常信息
                        ScadaAssembleServiceImpl savePcbMaterial1 = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = savePcbMaterial1.devException(clientMessage.get("obj"));
                        break;

                    case "B04": //安规测试结果的上传
                        ScadaAssembleServiceImpl testWork = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = testWork.safetyTest(clientMessage.get("obj"));
                        break;

                    case "B05": //安规测试载具验证
                        ScadaAssembleServiceImpl testWorkCheck = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = testWorkCheck.checksafetyTest(clientMessage.get("obj"));
                        break;

                    case "B055": //人工安规测试（不良）载具验证
                        ScadaAssembleServiceImpl personWorkCheck = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = personWorkCheck.checksafetyTestPerson(clientMessage.get("obj"));
                        break;

                    case "B06": //前测载具验证
                        ScadaAssembleServiceImpl beforeTestCheck = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = beforeTestCheck.checkBeforeTest(clientMessage.get("obj"));
                        break;

                    case "B07": //前测修改测试状态接口
                        ScadaAssembleServiceImpl beforeTest = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = beforeTest.beforeTest(clientMessage.get("obj"));
                        break;

                    case "B066": //后测载具验证 （验证能不能进工作台）
                        LOG.info("进入B066方法调取==开始");
                        ScadaAssembleServiceImpl afterTestCheck = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = afterTestCheck.checkAfterTest(clientMessage.get("obj"));
                        LOG.info("进入B066方法调取--完成");
                        break;

                    case "B067": //后测 验证良品还是不良品还是未测
                        ScadaAssembleServiceImpl afterTestGood = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = afterTestGood.afterTestGood(clientMessage.get("obj"));
                        break;

                    case "B068": //后测 中间扫描枪验证流向（右边维修工位与测试台之间那把扫描枪）
                        ScadaAssembleServiceImpl afterTestCenter = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = afterTestCenter.afterTestCenter(clientMessage.get("obj"));
                        break;

                    case "B069": //后测 维修工作台验证
                        ScadaAssembleServiceImpl afterTestMaintain = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = afterTestMaintain.afterTestMaintain(clientMessage.get("obj"));
                        break;

                    case "B070": //后测最后一把解绑扫码枪
                        ScadaAssembleServiceImpl afterTestNoBind = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = afterTestNoBind.afterTestNoBind(clientMessage.get("obj"));
                        try {
                            machineStopInfoServicempl.insertStopInfo("ECDLC034");
                        }catch (Exception e) {
                            LOG.error(e.toString(),e);
                        }

                        break;

                    case "B017": //硬盘光驱物料验证
                        ScadaAssembleServiceImpl hdcheck = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = hdcheck.checkHdMaterial(clientMessage.get("obj"));
                        break;

                    case "B018": //硬盘光驱保存
                        ScadaAssembleServiceImpl hdSave = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = hdSave.saveHdMaterial(clientMessage.get("obj"));
                        break;

                    case "N01": //散热器打螺丝&主板打螺丝 保存过站（没有物料验证）
                        ScadaAssembleServiceImpl Srq = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = Srq.saveMaterialsalfNo(clientMessage.get("obj"));
                        break;

                    case "B013": //内存保存和物料验证
                        ScadaAssembleServiceImpl memorySave = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = memorySave.saveMemory(clientMessage.get("obj"));
                        break;

                    case "B012": //内存条载具验证
                        ScadaAssembleServiceImpl memoryCheck = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = memoryCheck.checkMemory(clientMessage.get("obj"));
                        break;

                    case "B08": //老化验证接口
                        ScadaAssembleServiceImpl oldTest = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = oldTest.oldTestCheck(clientMessage.get("obj"));
                        break;

                    case "B09": //标签打印保存
                        ScadaAssembleServiceImpl label = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = label.saveMaterialLabel(clientMessage.get("obj"));
                        break;

                    case "B016": //机箱上料组装接口
                        ScadaAssembleServiceImpl crate = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = crate.checkDataCrate(clientMessage.get("obj"));
                        break;

                    case "B18": //分拣区到二楼具体的工位(& agv点位）
                        ScadaSortMatImpl sortMat = SpringContextUtil.getBean(ScadaSortMatImpl.class);
                        obj = sortMat.checkData(clientMessage.get("obj"));
                        break;

                    case "B17": // 物料到设备(工位)后空箱传回一楼分拣位
                        ScadaSortMatImpl sortMat1 = SpringContextUtil.getBean(ScadaSortMatImpl.class);
                        obj = sortMat1.sortScan(clientMessage.get("obj"));
                        break;

                    case "B19": //物料料箱到达下料点，通知scada,组装线供料
                        ScadaToAgvSendImpl device = SpringContextUtil.getBean(ScadaToAgvSendImpl.class);
                        obj = device.matBoxDown(clientMessage.get("obj"));
                        break;

                    case "B22": //主板下料请求接料AGV &主板机告知可以空框可以回，去数据库修改状态&不可以回空框，修改状态
                        ScadaToAgvSendImpl master = SpringContextUtil.getBean(ScadaToAgvSendImpl.class);
                        obj = master.masterDown(clientMessage.get("obj"));
                        break;

                    case "B25": //机箱下料请求接料AGV &机箱机告知可以空框可以回，去数据库修改状态
                        ScadaToAgvSendImpl crateOn = SpringContextUtil.getBean(ScadaToAgvSendImpl.class);
                        obj = crateOn.crateDown(clientMessage.get("obj"));
                        break;

                    case "Z01": //散热器&主板打螺丝
                        ScadaAssembleServiceImpl autoScrews = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = autoScrews.autoScrew(clientMessage.get("obj"));
                        break;

                    case "S01": //到达上老化架吸掉移载区，上位机通知SCADA上老化架
                        ScadaToAgvOld agvOld = SpringContextUtil.getBean(ScadaToAgvOld.class);
                        obj = agvOld.sendAgvOld(clientMessage.get("obj"));
                        break;

                    case "S02": //PLC是否成功把产品吸到老化架，返回结果给SCADA
                        ScadaToAgvOld agvOldGo = SpringContextUtil.getBean(ScadaToAgvOld.class);
                        obj = agvOldGo.createAgvBan(clientMessage.get("obj"));
                        if (msgObj.getString("device_sn").equals("ECDLC027")) {
                            machineStopInfoServicempl.insertStopInfo("ECDLC027");
                        } else {
                            machineStopInfoServicempl.insertStopInfo("ECDLC213");
                        }
                        break;

                    case "Z02": //产线工位呼叫回收空框,7.创建回收空料箱任务
                        ScadaToAgvOld agvEmpty = SpringContextUtil.getBean(ScadaToAgvOld.class);
                        obj = agvEmpty.sendAgvSc(clientMessage.get("obj"));
                        break;

                    case "Z06": //滚筒设备收到空框，告知SCADA
                        ScadaToAgvOld agvEmptyIn = SpringContextUtil.getBean(ScadaToAgvOld.class);
                        obj = agvEmptyIn.sendAgvScIn(clientMessage.get("obj"));
                        break;

                    case "S012": //后测吸吊移栽上位机告诉SCADA产品是否成功吸走
                        ScadaToAgvBan scadaToAgvBan = SpringContextUtil.getBean(ScadaToAgvBan.class);
                        obj = scadaToAgvBan.afterXidiao(clientMessage.get("obj"));
                        break;

                    case "G01": //光驱或硬盘或其他设备叫料
                        ScadaToAgvOld SD = SpringContextUtil.getBean(ScadaToAgvOld.class);
                        obj = SD.sdCome(clientMessage.get("obj"));
                        break;

                    case "Y01": //光驱或硬盘载具验证
                        ScadaAssembleServiceImpl checkHD = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = checkHD.checkHdData(clientMessage.get("obj"));
                        break;
                    //ss
                    case "B101": //上位机通知那条线开启
                        ScadaPackController linePack = SpringContextUtil.getBean(ScadaPackController.class);
                        obj = linePack.packLine(clientMessage.get("obj"));
                        break;

                    case "B102": //上位机获取打印信息
                        ScadaPackController labelPack = SpringContextUtil.getBean(ScadaPackController.class);
                        obj = labelPack.getlable(clientMessage.get("obj"));
                        break;

                    case "B1022": //上位机取标后告知
                        ScadaPackController labelOver = SpringContextUtil.getBean(ScadaPackController.class);
                        obj = labelOver.labelOver(clientMessage.get("obj"));
                        break;

                    case "C02": //设备可以上空框或者可以送料时，上位机告知
                        ScadaAssembleServiceImpl isEmptyOrSned = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = isEmptyOrSned.isEmptyAndSend(clientMessage.get("obj"));
                        break;

                    case "C03": //上位机心跳
                        ScadaAssembleServiceImpl heart = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = heart.heartBeat(clientMessage.get("obj"));
                        break;

                    case "C04": //前测切换模式
                        ScadaToAgvOld xidiaoCg = SpringContextUtil.getBean(ScadaToAgvOld.class);
                        obj = xidiaoCg.xidiaoCg(clientMessage.get("obj"));
                        break;

                    case "C05": //包装线判定工单扫码枪
                        ScadaAssembleServiceImpl packMo = SpringContextUtil.getBean(ScadaAssembleServiceImpl.class);
                        obj = packMo.packMoCheck(clientMessage.get("obj"));
                        try {
                            machineStopInfoServicempl.insertStopInfo("ECDLC026");
                        }catch (Exception e) {
                            LOG.error(e.toString(),e);
                        }
                        break;
                    default:
                        break;
                }

                try {
                    String opflag = msgObj.get("op_flag").toString();
                    // 将mes系统的响应信息封装成json格式的数据包
                    resultMessage.setId("");
                    resultMessage.setAnswerId(clientMessage.getString("id"));
                    resultMessage.setFrom("9999");
                    resultMessage.setTo(clientMessage.getString("from"));

                    if (opflag.equals("S01") || opflag.equals("S02") || opflag.equals("S012")) { //吸吊移栽
                        resultMessage.setType("ScadaRespon_XIDIAOYIZAI"); //HC
                    } else if (opflag.equals("B18")) { //分拣线二楼扫描枪
                        resultMessage.setType("ScadaRespon_RollSort");
                    } else if (opflag.equals("B06") || opflag.equals("B07") || opflag.equals("B055")) { //前测载具验证
                        resultMessage.setType("ScadaRespon_FrontTest");
                    } else if (opflag.equals("B066") || opflag.equals("B067") || opflag.equals("B068") || opflag.equals("B069") || opflag.equals("B070")) {
                        resultMessage.setType("ScadaRespon_AfterTest");
                    } else if (opflag.equals("B101") || opflag.equals("B102") || opflag.equals("B1022") || opflag.equals("C05")) {
                        resultMessage.setType("ScadaRespon_AutoPacking");
                    }
                    resultMessage.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                    resultMessage.setObj(obj);

                    resultMessage.setOp_flag_respon(opflag);
                    result = new ObjectMapper().writeValueAsString(resultMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException je) {
            je.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


}
