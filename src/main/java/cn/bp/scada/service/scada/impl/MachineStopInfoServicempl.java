package cn.bp.scada.service.scada.impl;

import cn.bp.scada.common.utils.InsertRecord;
import cn.bp.scada.common.utils.data.DateUtils;
import cn.bp.scada.mapper.mes.E_CLASS_SELECTMapper;
import cn.bp.scada.mapper.mes.E_STOP_REASONMapper;
import cn.bp.scada.mapper.mes.E_TIME_SETMapper;
import cn.bp.scada.modle.mes.E_TIME_SET;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class MachineStopInfoServicempl {
    @Autowired
    private DateUtils nowDate;
    @Autowired
    private E_CLASS_SELECTMapper selectSetTime;
    @Autowired
    private E_TIME_SETMapper selectClassTime;
    @Autowired
    private E_STOP_REASONMapper stopRecord;
    @Autowired
    private InsertRecord insertRecord;

    private static final Logger logger = LoggerFactory.getLogger(MachineStopInfoServicempl.class);

    Date nowTime = null;
    Date lastTime = null;
    E_TIME_SET selectNewSet = null;

    public void insertStopInfo(String machineID) {
        nowTime = nowDate.getNowDate();//获取当前时间
        selectNewSet = selectClassTime.selectNewTime();//获取数据库设定时间
        long setTime = selectNewSet.getStopRecordTimeSet();//设定停线标准时间
        long nowTimeMin = nowDate.HMSDTD(nowTime);//获取分钟格式时间
        lastTime = stopRecord.selectNewTime1(machineID);
        long timediff = nowDate.getDatePoorMins(nowTime, lastTime);//获取两次时间差

        /* 判断此工站《白班》是否有排班*/
        if (selectSetTime.selectDayClassEnable(machineID) != null) {
            //获取时间段long值
            long getDayMorning1 = nowDate.HMSSTD(selectNewSet.getDayMorning1());
            long getDayMorning2 = nowDate.HMSSTD(selectNewSet.getDayMorning2());
            long getDayAfternoon1 = nowDate.HMSSTD(selectNewSet.getDayAfternoon1());
            long getDayAfternoon2 = nowDate.HMSSTD(selectNewSet.getDayAfternoon2());
            long getDayNight1 = nowDate.HMSSTD(selectNewSet.getDayNight1());
            long getDayNight2 = nowDate.HMSSTD(selectNewSet.getDayNight2());
            //判断调用时间所在区间《上午》
            if (getDayMorning1 < nowTimeMin && nowTimeMin < getDayMorning2) {
                if (getDayMorning1 == 0 && getDayMorning2 == 0) {
                    logger.info("白班上午没有维护时间");
                } else {
                    insertRecord.judgeUPorIN(machineID, setTime, timediff, getDayMorning2 - getDayMorning1, nowTime, "白班");
                }
            }
            //判断调用时间所在区间《下午》
            if (getDayAfternoon1 < nowTimeMin && nowTimeMin < getDayAfternoon2) {
                if (getDayAfternoon1 == 0 && getDayAfternoon2 == 0) {
                    logger.info("白班下午没有维护时间");
                } else {
                    insertRecord.judgeUPorIN(machineID, setTime, timediff, getDayAfternoon2 - getDayAfternoon1, nowTime, "白班");
                }
            }
            //判断调用时间所在区间《加班》
            if (getDayNight1 < nowTimeMin && nowTimeMin < getDayNight2) {
                if (getDayNight1 == 0 && getDayNight2 == 0) {
                    logger.info("白班加班没有维护时间");
                } else {
                    insertRecord.judgeUPorIN(machineID, setTime, timediff, getDayNight2 - getDayNight1, nowTime, "白班");
                }
            }
        } else {
            logger.debug("白班没有维护开班");
        }

        /* 判断此工站《夜班》是否有排班*/
        if (selectSetTime.selectNightClassEnable(machineID) != null) {
            long getNightMorning1 = nowDate.HMSSTD(selectNewSet.getNightMorning1());
            long getNightMorning2 = nowDate.HMSSTD(selectNewSet.getNightMorning2());
            long getNightAfternoon1 = nowDate.HMSSTD(selectNewSet.getNightAfternoon1());
            long getNightAfternoon2 = nowDate.HMSSTD(selectNewSet.getNightAfternoon2());
            long getNightNight1 = nowDate.HMSSTD(selectNewSet.getNightNight1());
            long getNightNight2 = nowDate.HMSSTD(selectNewSet.getNightNight2());
            //判断调用时间所在区间《上午》
            if (getNightMorning1 < nowTimeMin && nowTimeMin < getNightMorning2) {
                if (getNightMorning1 == 0 && getNightMorning2 == 0) {
                    logger.info("夜班上午没有维护时间");
                } else {
                    insertRecord.judgeUPorIN(machineID, setTime, timediff, getNightMorning2 - getNightMorning1, nowTime, "夜班");
                }
            }
            //判断调用时间所在区间《下午》
            if (getNightAfternoon1 < nowTimeMin && nowTimeMin < getNightAfternoon2) {
                if (getNightAfternoon1 == 0 && getNightAfternoon2 == 0) {
                    logger.info("夜班下午没有维护时间");
                } else {
                    insertRecord.judgeUPorIN(machineID, setTime, timediff, getNightAfternoon2 - getNightAfternoon1, nowTime, "夜班");
                }
            }
            //判断调用时间所在区间《加班》
            if (getNightNight1 < nowTimeMin && nowTimeMin < getNightNight2) {
            }
            if (getNightNight1 == 0 && getNightNight2 == 0) {
                logger.info("夜班加班没有维护时间");
            } else {
                insertRecord.judgeUPorIN(machineID, setTime, timediff, getNightNight2 - getNightNight1, nowTime, "夜班");
            }
        } else {
            logger.debug("夜班没有维护开班");
        }
    }
}


