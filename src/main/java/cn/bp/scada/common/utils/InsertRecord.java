package cn.bp.scada.common.utils;

import cn.bp.scada.mapper.mes.E_CLASS_SELECTMapper;
import cn.bp.scada.mapper.mes.E_STOP_REASONMapper;
import cn.bp.scada.modle.mes.E_CLASS_SELECT;
import cn.bp.scada.modle.mes.E_STOP_REASON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class InsertRecord {
    @Autowired
    private E_STOP_REASON setRecord;
    @Autowired
    private E_STOP_REASONMapper stopRecord;
    @Autowired
    private E_CLASS_SELECTMapper selectSetTime;
    @Autowired
    private E_CLASS_SELECTMapper E_CLASS_SELECTMapper;

    private static final Logger logger = LoggerFactory.getLogger(InsertRecord.class);

    //任务开始，插入对比数据
    public void insert(Date date1, Date date2, String className) {

        //主板预组装A
        if ((className.equals("白班") && selectSetTime.selectDayClassEnable("ECDLC005") != null)
                || (className.equals("夜班") && selectSetTime.selectNightClassEnable("ECDLC005") != null)) {
            setRecord.setTime(date1);
            setRecord.setStopStartTime(date2);
            setRecord.setMachineid("ECDLC005");
            setRecord.setLineName("主板预组装A");
            setRecord.setCalssName(className);
            setRecord.setRecordStatus((short) 0);
            stopRecord.insertnew1(setRecord);

        }
        //机器组装A
        if ((className.equals("白班") && selectSetTime.selectDayClassEnable("ECDLC027") != null)
                || (className.equals("夜班") && selectSetTime.selectNightClassEnable("ECDLC027") != null)) {
            setRecord.setTime(date1);
            setRecord.setStopStartTime(date2);
            setRecord.setMachineid("ECDLC027");
            setRecord.setLineName("机器组装A");
            setRecord.setCalssName(className);
            setRecord.setRecordStatus((short) 0);
            stopRecord.insertnew1(setRecord);
        }
        //主板预组装B
        if ((className.equals("白班") && selectSetTime.selectDayClassEnable("ECDLC205") != null)
                || (className.equals("夜班") && selectSetTime.selectNightClassEnable("ECDLC205") != null)) {
            setRecord.setTime(date1);
            setRecord.setStopStartTime(date2);
            setRecord.setMachineid("ECDLC205");
            setRecord.setLineName("主板预组装B");
            setRecord.setCalssName(className);
            setRecord.setRecordStatus((short) 0);
            stopRecord.insertnew1(setRecord);
        }
        //机器组装B
        if ((className.equals("白班") && selectSetTime.selectDayClassEnable("ECDLC213") != null)
                || (className.equals("夜班") && selectSetTime.selectNightClassEnable("ECDLC213") != null)) {
            setRecord.setTime(date1);
            setRecord.setStopStartTime(date2);
            setRecord.setMachineid("ECDLC213");
            setRecord.setLineName("机器组装B");
            setRecord.setCalssName(className);
            setRecord.setRecordStatus((short) 0);
            stopRecord.insertnew1(setRecord);
        }
        //后测
        if ((className.equals("白班") && selectSetTime.selectDayClassEnable("ECDLC034") != null)
                || (className.equals("夜班") && selectSetTime.selectNightClassEnable("ECDLC034") != null)) {
            setRecord.setTime(date1);
            setRecord.setStopStartTime(date2);
            setRecord.setMachineid("ECDLC034");
            setRecord.setLineName("后测");
            setRecord.setCalssName(className);
            setRecord.setRecordStatus((short) 0);
            stopRecord.insertnew1(setRecord);

        }
        //包装
        if ((className.equals("白班") && selectSetTime.selectDayClassEnable("ECDLC026") != null)
                || (className.equals("夜班") && selectSetTime.selectNightClassEnable("ECDLC026") != null)) {
            setRecord.setTime(date1);
            setRecord.setStopStartTime(date2);
            setRecord.setMachineid("ECDLC026");
            setRecord.setLineName("包装");
            setRecord.setCalssName(className);
            setRecord.setRecordStatus((short) 0);
            stopRecord.insertnew1(setRecord);

        }
    }

    //任务结束，判断最后逻辑
    public void judgeRecord(String machineID, long standardTime, long stopTime1, long timeClass1, Date date1, String classType) {
        logger.info("任务结束,判断数据:"+machineID+"--"+standardTime+"--"+stopTime1+"--"+timeClass1+"--"+date1+"--"+classType);
        if ((((classType.equals("白班") && selectSetTime.selectDayClassEnable(machineID) != null)
                || (classType.equals("夜班") && selectSetTime.selectNightClassEnable(machineID) != null)))
                && standardTime < stopTime1 && stopTime1 <= timeClass1) {
            setRecord.setTime(date1);
            setRecord.setStopEndTime(date1);
            setRecord.setRecordStatus((short) 1);
            setRecord.setStopTime((short) stopTime1);
            setRecord.setMachineid(machineID);
            try {
                stopRecord.updateNewTime(setRecord);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            stopRecord.deleteMaxRecord(machineID);
        }
    }

    //转换成Corn格式
    public String cornValue(String inValue) {
        String hjk = "0 " + inValue.split(":")[1] + " " + inValue.split(":")[0] + " * * ?";
        return hjk;
    }

    //与上一次时间进行比对判断
    public void judgeUPorIN(String machineID, long standardTime, long stopTime1, long timeClass1, Date nowtime, String classname) {
        String machinename = E_CLASS_SELECTMapper.selectLineName(machineID);
        String isRecord = null;
        if (classname.equals("白班")) {
            isRecord = selectSetTime.selectDayClassEnable(machineID);
        } else if (classname.equals("夜班")) {
            isRecord = selectSetTime.selectNightClassEnable(machineID);
        }
        Date lasttime = stopRecord.selectNewTime1(machineID);
        if (isRecord != null) {
            if (lasttime != null) {
                if (stopTime1 > timeClass1) {
                    //删除最新记录
                    stopRecord.deleteMaxRecord(machineID);
                    //插入一条新数据
                    setRecord.setTime(nowtime);
                    setRecord.setStopStartTime(nowtime);
                    setRecord.setMachineid(machineID);
                    setRecord.setLineName(machinename);
                    setRecord.setCalssName(classname);
                    setRecord.setRecordStatus((short) 0);
                    stopRecord.insertnew1(setRecord);
                    logger.info("插入新基础数据" + nowtime + "-" + machineID + "-" + classname + "-" + (short) 0);
                } else {
                    if (standardTime < stopTime1) {
                        //更新停线时间和状态
                        setRecord.setTime(nowtime);
                        setRecord.setStopEndTime(nowtime);
                        setRecord.setRecordStatus((short) 1);
                        setRecord.setStopTime((short) stopTime1);
                        setRecord.setMachineid(machineID);
                        stopRecord.updateNewTime(setRecord);
                        logger.info("记录一条停线记录：" + nowtime + "-" + machineID + "-" + classname + "-" + (short) 1);
                        //插入一行新的数据
                        setRecord.setTime(nowtime);
                        setRecord.setStopStartTime(nowtime);
                        setRecord.setMachineid(machineID);
                        setRecord.setLineName(machinename);
                        setRecord.setCalssName(classname);
                        setRecord.setRecordStatus((short) 0);
                        stopRecord.insertnew1(setRecord);
                        logger.info("插入新基础数据" + nowtime + "-" + machineID + "-" + classname + "-" + (short) 0);
                    } else {
                        //更新开始时间
                        setRecord.setStopStartTime(nowtime);
                        setRecord.setMachineid(machineID);
                        stopRecord.updateStopStartTime(setRecord);
                        logger.info("更新对比时间：" + nowtime + "-" + machineID + "-" + classname + "-" + (short) 0);
                    }
                }
            } else {
                //重新插入新数据做对比
                setRecord.setTime(nowtime);
                setRecord.setStopStartTime(nowtime);
                setRecord.setMachineid(machineID);
                setRecord.setLineName(machinename);
                setRecord.setCalssName(classname);
                setRecord.setRecordStatus((short) 0);
                stopRecord.insertnew1(setRecord);
                logger.info("插入新基础数据" + nowtime + "-" + machineID + "-" + classname + "-" + (short) 0);
            }
        } else {
            logger.info("本线体班次没有维护使用");
        }
    }
}


