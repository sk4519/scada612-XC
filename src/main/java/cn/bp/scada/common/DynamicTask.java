package cn.bp.scada.common;

import cn.bp.scada.common.utils.InsertRecord;
import cn.bp.scada.common.utils.data.DateUtils;
import cn.bp.scada.mapper.mes.E_STOP_REASONMapper;
import cn.bp.scada.mapper.mes.E_TIME_SETMapper;
import cn.bp.scada.modle.mes.E_TIME_SET;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

@Component
public class DynamicTask {
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    @Autowired
    private E_TIME_SETMapper timeSelect;
    @Autowired
    private E_STOP_REASONMapper stopRecord;
    @Autowired
    private DateUtils nowDate;
    @Autowired
    private InsertRecord insertRecord;

    private static final Logger logger = LoggerFactory.getLogger(DynamicTask.class);

    private ScheduledFuture<?> taskmor1;
    private ScheduledFuture<?> taskmor2;
    private ScheduledFuture<?> taskafter1;
    private ScheduledFuture<?> taskafter2;
    private ScheduledFuture<?> tasknight1;
    private ScheduledFuture<?> tasknight2;
    private ScheduledFuture<?> tasknigmor1;
    private ScheduledFuture<?> tasknigmor2;
    private ScheduledFuture<?> tasknigafter1;
    private ScheduledFuture<?> tasknigafter2;
    private ScheduledFuture<?> tasknignight1;
    private ScheduledFuture<?> tasknignight2;

    public int standardTime = 0;
    public String dayMorning1 = null;
    public String dayMorning2 = null;
    public String dayAfter1 = null;
    public String dayAfter2 = null;
    public String dayNight1 = null;
    public String dayNight2 = null;
    public String nigMorning1 = null;
    public String nigMorning2 = null;
    public String nigAfter1 = null;
    public String nigAfter2 = null;
    public String nigNight1 = null;
    public String nigNight2 = null;

    //    实例化一个线程池任务调度类,可以使用自定义的ThreadPoolTaskScheduler
    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    /**
     * 启动定时任务
     *
     * @return
     */
    @Bean
    public boolean startCron() {
        boolean flag = true;
        E_TIME_SET timeUse = timeSelect.selectNewTime();
        standardTime = timeUse.getStopRecordTimeSet();
        dayMorning1 = timeUse.getDayMorning1();
        dayMorning2 = timeUse.getDayMorning2();
        dayAfter1 = timeUse.getDayAfternoon1();
        dayAfter2 = timeUse.getDayAfternoon2();
        dayNight1 = timeUse.getDayNight1();
        dayNight2 = timeUse.getDayNight2();
        nigMorning1 = timeUse.getNightMorning1();
        nigMorning2 = timeUse.getNightMorning2();
        nigAfter1 = timeUse.getNightAfternoon1();
        nigAfter2 = timeUse.getNightAfternoon2();
        nigNight1 = timeUse.getNightNight1();
        nigNight2 = timeUse.getNightNight2();
        /**
         * 白班任务
         * */

        //白班早班任务1
        if (taskmor1 == null && dayMorning1 != null) {
            try {
                taskmor1 = threadPoolTaskScheduler.schedule(new myTask1("taskmor1"), new CronTrigger(insertRecord.cornValue(dayMorning1)));
                logger.info("早班上午taskmor1任务启动成功"+taskmor1);
            } catch (Exception e) {
                logger.info("早班上午taskmor1任务启动失败"+taskmor1);
                e.printStackTrace();
            }

        }
        //白班早班任务2
        if (taskmor2 == null && dayMorning2 != null) {
            taskmor2 = threadPoolTaskScheduler.schedule(new myTask2("taskmor2"), new CronTrigger(insertRecord.cornValue(dayMorning2)));
            logger.info("早班上午taskmor2任务启动成功"+taskmor2);
        } else {
            logger.info("早班上午taskmor1任务启动失败"+taskmor2);
        }

        //白班下午任务1
        if (taskafter1 == null && dayAfter1 != null) {
            taskafter1 = threadPoolTaskScheduler.schedule(new myTask3("taskafter1"), new CronTrigger(insertRecord.cornValue(dayAfter1)));
            logger.info("早班下午taskafter1任务启动成功"+taskafter1);
        } else {
            logger.info("早班下午taskafter1任务启动失败"+taskafter1);
        }

        //白班下午任务2
        if (taskafter2 == null && dayAfter2 != null) {
            taskafter2 = threadPoolTaskScheduler.schedule(new myTask4("taskafter2"), new CronTrigger(insertRecord.cornValue(dayAfter2)));
            logger.info("早班下午taskafter2任务启动成功"+taskafter2);
        } else {
            logger.info("早班下午taskafter2任务启动失败"+taskafter2);
        }

        //白班加班任务1
        if (tasknight1 == null && dayNight1 != null) {
            tasknight1 = threadPoolTaskScheduler.schedule(new myTask5("tasknight1"), new CronTrigger(insertRecord.cornValue(dayNight1)));
            logger.info("早班加班tasknight1任务启动成功"+tasknight1);
        } else {
            logger.info("早班上午tasknight1任务启动失败"+tasknight1);
        }

        //白班加班任务2
        if (tasknight2 == null && dayNight2 != null) {
            tasknight2 = threadPoolTaskScheduler.schedule(new myTask6("tasknight2"), new CronTrigger(insertRecord.cornValue(dayNight2)));
            logger.info("早班上午tasknight2任务启动成功"+tasknight2);
        } else {
            logger.info("早班上午tasknight2任务启动失败"+tasknight2);
        }

        /**
         * 夜班任务
         * */

        //夜班早班任务1
        if (tasknigmor1 == null && nigMorning1 != null) {
            tasknigmor1 = threadPoolTaskScheduler.schedule(new myTask7("tasknigmor1"), new CronTrigger(insertRecord.cornValue(nigMorning1)));
            logger.info("夜班上午taskmor1任务启动成功"+tasknigmor1);
        } else {
            logger.info("夜班上午tasknigmor1任务启动失败"+tasknigmor1);
        }

        //夜班早班任务2
        if (tasknigmor2 == null && nigMorning2 != null) {
            tasknigmor2 = threadPoolTaskScheduler.schedule(new myTask8("tasknigmor2"), new CronTrigger(insertRecord.cornValue(nigMorning2)));
            logger.info("夜班上午tasknigmor2任务启动成功"+tasknigmor2);
        } else {
            logger.info("夜班上午tasknigmor2任务启动失败"+tasknigmor2);
        }

        //夜班下午任务1
        if (tasknigafter1 == null && nigAfter1 != null) {
            tasknigafter1 = threadPoolTaskScheduler.schedule(new myTask9("tasknigafter1"), new CronTrigger(insertRecord.cornValue(nigAfter1)));
            logger.info("夜班下午tasknigafter1任务启动成功"+tasknigafter1);
        } else {
            logger.info("夜班下午tasknigafter1任务启动失败"+tasknigafter1);
        }

        //夜班下午任务2
        if (tasknigafter2 == null && nigAfter2 != null) {
            tasknigafter2 = threadPoolTaskScheduler.schedule(new myTask10("tasknigafter2"), new CronTrigger(insertRecord.cornValue(nigAfter2)));
            logger.info("夜班下午tasknigafter2任务启动成功"+tasknigafter2);
        } else {
            logger.info("夜班下午tasknigafter2任务启动失败"+tasknigafter2);
        }

        //夜班加班任务1
        if (tasknignight1 == null && nigNight1 != null) {
            tasknignight1 = threadPoolTaskScheduler.schedule(new myTask11("tasknignight1"), new CronTrigger(insertRecord.cornValue(nigNight1)));
            logger.info("夜班上午tasknignight1任务启动成功"+tasknignight1);
        } else {
            logger.info("夜班上午tasknignight1任务启动失败"+tasknignight1);
        }

        //夜班加班任务2
        if (tasknignight2 == null && nigNight2 != null) {
            tasknignight2 = threadPoolTaskScheduler.schedule(new myTask12("tasknight2"), new CronTrigger(insertRecord.cornValue(nigNight2)));
            logger.info("夜班上午tasknignight2任务启动成功"+tasknignight1);
        } else {
            logger.info("夜班上午tasknignight2任务启动失败"+tasknignight1);
        }

        return flag;
    }

    /**
     * 停止定时任务
     *
     * @return
     */
    public void stopCron() {
        if (taskmor1 != null) {
            taskmor1.cancel(true);
            taskmor1 = null;
        }
        if (taskmor2 != null) {
            taskmor2.cancel(true);
            taskmor2 = null;
        }
        if (taskafter1 != null) {
            taskafter1.cancel(true);
            taskafter1 = null;
        }
        if (taskafter2 != null) {
            taskafter2.cancel(true);
            taskafter2 = null;
        }
        if (tasknight1 != null) {
            tasknight1.cancel(true);
            tasknight1 = null;
        }

        if (tasknight2 != null) {
            tasknight2.cancel(true);
            tasknight2 = null;
        }
        if (tasknigmor1 != null) {
            tasknigmor1.cancel(true);
            tasknigmor1 = null;
        }
        if (tasknigmor2 != null) {
            tasknigmor2.cancel(true);
            tasknigmor2 = null;
        }
        if (tasknigafter1 != null) {
            tasknigafter1.cancel(true);
            tasknigafter1 = null;
        }
        if (tasknigafter2 != null) {
            tasknigafter2.cancel(true);
            tasknigafter2 = null;
        }
        if (tasknignight1 != null) {
            tasknignight1.cancel(true);
            tasknignight1 = null;
        }
        if (tasknignight2 != null) {
            tasknignight2.cancel(true);
            tasknignight2 = null;
        }

        if (taskmor1 == null && taskmor2 == null && taskafter1 == null && taskafter2 == null && tasknight1 == null && tasknight2 == null
                && tasknigmor1 == null && tasknigmor2 == null && tasknigafter1 == null && tasknigafter2 == null && tasknignight1 == null
                && tasknignight2 == null) {
            logger.info("DynamicTask所有任务已结束");
        } else {
            logger.info("DynamicTask任务结束失败");
        }
    }

    private class myTask1 implements Runnable {
        private String name;

        myTask1(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            insertRecord.insert(nowDate.getNowDate(), nowDate.getNowDate(), "白班");
            logger.info("白班上午1--定时任务执行完毕");
        }
    }

    private class myTask2 implements Runnable {
        private String name;

        myTask2(String name) {
            this.name = name;
        }

        @Override
        public void run() {

            //主板预组装A
            long stopTime1 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC005"));
            long timeClass1 = nowDate.HMSSTD(dayMorning2, dayMorning1);
            insertRecord.judgeRecord("ECDLC005", standardTime, stopTime1, timeClass1, nowDate.getNowDate(),"白班");
            logger.info("白班上午1--主板组装A--定时任务执行完毕");

            //机器组装A
            long stopTime5 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC027"));
            long timeClass5 = nowDate.HMSSTD(dayMorning2, dayMorning1);
            insertRecord.judgeRecord("ECDLC027", standardTime, stopTime5, timeClass5, nowDate.getNowDate(),"白班");
            logger.info("白班上午1--机器组装A--定时任务执行完毕");

            //主板预组装B
            long stopTime2 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC205"));
            long timeClass2 = nowDate.HMSSTD(dayMorning2, dayMorning1);
            insertRecord.judgeRecord("ECDLC205", standardTime, stopTime2, timeClass2, nowDate.getNowDate(),"白班");
            logger.info("白班上午1--主板组装B--定时任务执行完毕");

            //机器组装B
            long stopTime6 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC213"));
            long timeClass6 = nowDate.HMSSTD(dayMorning2, dayMorning1);
            insertRecord.judgeRecord("ECDLC213", standardTime, stopTime6, timeClass6, nowDate.getNowDate(),"白班");
            logger.info("白班上午1--机器组装B--定时任务执行完毕");

            //后测
            long stopTime3 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC034"));
            long timeClass3 = nowDate.HMSSTD(dayMorning2, dayMorning1);
            insertRecord.judgeRecord("ECDLC034", standardTime, stopTime3, timeClass3, nowDate.getNowDate(),"白班");
            logger.info("白班上午1--后测--定时任务执行完毕");
            //包装

            long stopTime4 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC026"));
            long timeClass4 = nowDate.HMSSTD(dayMorning2, dayMorning1);
            insertRecord.judgeRecord("ECDLC026", standardTime, stopTime4, timeClass4, nowDate.getNowDate(),"白班");
            logger.info("白班上午1--包装--定时任务执行完毕");

        }
    }

    private class myTask3 implements Runnable {
        private String name;

        myTask3(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            insertRecord.insert(nowDate.getNowDate(), nowDate.getNowDate(), "白班");
            logger.info("白班下午1--定时任务执行完毕");
        }
    }

    private class myTask4 implements Runnable {
        private String name;

        myTask4(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            Date nowTime=nowDate.getNowDate();//当前时间
            long timeClass = nowDate.HMSSTD(dayAfter2, dayAfter1);//相差时间
            //主板预组装A
            Date DayAfter1=stopRecord.selectNewTime1("ECDLC005");
            long stopTime1 = nowDate.getDatePoorMins(nowDate.getNowDate(), DayAfter1);
            insertRecord.judgeRecord("ECDLC005", standardTime, stopTime1, timeClass, nowTime,"白班");
            logger.info("白班下午1--主板预组装A--定时任务执行完毕"+DayAfter1);

            //机器组装A
            Date DayAfter7=stopRecord.selectNewTime1("ECDLC027");
            long stopTime7 = nowDate.getDatePoorMins(nowTime, DayAfter7);
            insertRecord.judgeRecord("ECDLC027", standardTime, stopTime7, timeClass, nowTime,"白班");
            logger.info("白班下午1--机器组装A--定时任务执行完毕"+nowTime+"--"+stopRecord.selectNewTime1("ECDLC027"));

            //主板预组装B
            Date DayAfter2=stopRecord.selectNewTime1("ECDLC205");
            long stopTime2 = nowDate.getDatePoorMins(nowTime, DayAfter2);
            insertRecord.judgeRecord("ECDLC205", standardTime, stopTime2, timeClass, nowTime,"白班");
            logger.info("白班下午1--主板预组装B--定时任务执行完毕");

            //机器组装B
            Date DayAfter8=stopRecord.selectNewTime1("ECDLC213");
            long stopTime8= nowDate.getDatePoorMins(nowTime, DayAfter8);
            insertRecord.judgeRecord("ECDLC213", standardTime, stopTime8, timeClass, nowTime,"白班");
            logger.info("白班下午1--主板组装B--定时任务执行完毕"+nowTime+"--"+DayAfter8);

            //后测
            Date DayAfter3=stopRecord.selectNewTime1("ECDLC034");
            long stopTime3 = nowDate.getDatePoorMins(nowTime, DayAfter3);
            insertRecord.judgeRecord("ECDLC034", standardTime, stopTime3, timeClass, nowTime,"白班");
            logger.info("白班下午1--后测--定时任务执行完毕"+DayAfter3);

            //包装
            Date DayAfter4=stopRecord.selectNewTime1("ECDLC026");
            long stopTime4 = nowDate.getDatePoorMins(nowTime, DayAfter4);
            insertRecord.judgeRecord("ECDLC026", standardTime, stopTime4, timeClass, nowTime,"白班");
            logger.info("白班下午1--包装--定时任务执行完毕"+DayAfter4);
        }
    }

    private class myTask5 implements Runnable {
        private String name;

        myTask5(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            insertRecord.insert(nowDate.getNowDate(), nowDate.getNowDate(), "白班");
            logger.info("白班加班1--定时任务执行完毕");
        }
    }

    private class myTask6 implements Runnable {
        private String name;

        myTask6(String name) {
            this.name = name;
        }

        @Override
        public void run() {

            //主板预组装A
            long stopTime1 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC005"));
            long timeClass1 = nowDate.HMSSTD(dayNight2, dayNight1);
            insertRecord.judgeRecord("ECDLC005", standardTime, stopTime1, timeClass1, nowDate.getNowDate(),"白班");
            logger.info("白班加班1--主板预组装A--定时任务执行完毕");

            //机器组装A
            long stopTime5 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC027"));
            long timeClass5 = nowDate.HMSSTD(dayNight2, dayNight1);
            insertRecord.judgeRecord("ECDLC027", standardTime, stopTime5, timeClass5, nowDate.getNowDate(),"白班");
            logger.info("白班加班1--机器组装A--定时任务执行完毕");

            //主板预组装B
            long stopTime2 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC205"));
            long timeClass2 = nowDate.HMSSTD(dayNight2, dayNight1);
            insertRecord.judgeRecord("ECDLC205", standardTime, stopTime2, timeClass2, nowDate.getNowDate(),"白班");
            logger.info("白班加班1--主板预组装B--定时任务执行完毕");

            //机器组装B
            long stopTime6 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC213"));
            long timeClass6 = nowDate.HMSSTD(dayNight2, dayNight1);
            insertRecord.judgeRecord("ECDLC213", standardTime, stopTime6, timeClass6, nowDate.getNowDate(),"白班");
            logger.info("白班加班1--机器组装B--定时任务执行完毕");

            //后测
            long stopTime3 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC034"));
            long timeClass3 = nowDate.HMSSTD(dayNight2, dayNight1);
            insertRecord.judgeRecord("ECDLC034", standardTime, stopTime3, timeClass3, nowDate.getNowDate(),"白班");
            logger.info("白班加班1--后测--定时任务执行完毕");

            //包装
            long stopTime4 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC026"));
            long timeClass4 = nowDate.HMSSTD(dayNight2, dayNight1);
            insertRecord.judgeRecord("ECDLC026", standardTime, stopTime4, timeClass4, nowDate.getNowDate(),"白班");
            logger.info("白班加班1--包装--定时任务执行完毕");
        }
    }

    private class myTask7 implements Runnable {
        private String name;

        myTask7(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            insertRecord.insert(nowDate.getNowDate(), nowDate.getNowDate(), "夜班");
            logger.info("夜班上午1--定时任务执行完毕");
        }
    }

    private class myTask8 implements Runnable {
        private String name;

        myTask8(String name) {
            this.name = name;
        }

        @Override
        public void run() {

            //主板预组装A
            long stopTime1 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC005"));
            long timeClass1 = nowDate.HMSSTD(nigMorning2, nigMorning1);
            insertRecord.judgeRecord("ECDLC005", standardTime, stopTime1, timeClass1, nowDate.getNowDate(),"夜班");
            logger.info("夜班上午1--主板预组装A--定时任务执行完毕");

            //机器组装A
            long stopTime5 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC027"));
            long timeClass5 = nowDate.HMSSTD(nigMorning2, nigMorning1);
            insertRecord.judgeRecord("ECDLC027", standardTime, stopTime5, timeClass5, nowDate.getNowDate(),"白班");
            logger.info("夜班上午1--机器组装A--定时任务执行完毕");

            //主板预组装B
            long stopTime2 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC205"));
            long timeClass2 = nowDate.HMSSTD(nigMorning2, nigMorning1);
            insertRecord.judgeRecord("ECDLC205", standardTime, stopTime2, timeClass2, nowDate.getNowDate(),"夜班");
            logger.info("夜班上午1--主板预组装B--定时任务执行完毕");


            //机器组装B
            long stopTime6 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC213"));
            long timeClass6 = nowDate.HMSSTD(nigMorning2, nigMorning1);
            insertRecord.judgeRecord("ECDLC213", standardTime, stopTime6, timeClass6, nowDate.getNowDate(),"白班");
            logger.info("夜班上午1--机器组装B--定时任务执行完毕");

            //后测
            long stopTime3 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC034"));
            long timeClass3 = nowDate.HMSSTD(nigMorning2, nigMorning1);
            insertRecord.judgeRecord("ECDLC034", standardTime, stopTime3, timeClass3, nowDate.getNowDate(),"夜班");
            logger.info("夜班上午1--后测--定时任务执行完毕");

            //包装
            long stopTime4 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC026"));
            long timeClass4 = nowDate.HMSSTD(nigMorning2, nigMorning1);
            insertRecord.judgeRecord("ECDLC026", standardTime, stopTime4, timeClass4, nowDate.getNowDate(),"夜班");
            logger.info("夜班上午1--包装--定时任务执行完毕");
        }
    }

    private class myTask9 implements Runnable {
        private String name;

        myTask9(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            insertRecord.insert(nowDate.getNowDate(), nowDate.getNowDate(), "夜班");
            logger.info("夜班下午1--定时任务执行完毕");
        }
    }

    private class myTask10 implements Runnable {
        private String name;

        myTask10(String name) {
            this.name = name;
        }

        @Override
        public void run() {

            //主板预组装A
            long stopTime1 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC005"));
            long timeClass1 = nowDate.HMSSTD(nigAfter2, nigAfter1);
            insertRecord.judgeRecord("ECDLC005", standardTime, stopTime1, timeClass1, nowDate.getNowDate(),"夜班");
            logger.info("夜班下午1--主板预组装A--定时任务执行完毕");

            //机器组装A
            long stopTime5 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC027"));
            long timeClass5 = nowDate.HMSSTD(nigAfter2, nigAfter1);
            insertRecord.judgeRecord("ECDLC027", standardTime, stopTime5, timeClass5, nowDate.getNowDate(),"白班");
            logger.info("夜班下午1--机器组装A--定时任务执行完毕");

            //主板预组装B
            long stopTime2 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC205"));
            long timeClass2 = nowDate.HMSSTD(nigAfter2, nigAfter1);
            insertRecord.judgeRecord("ECDLC205", standardTime, stopTime2, timeClass2, nowDate.getNowDate(),"夜班");
            logger.info("夜班下午1--主板预组装B--定时任务执行完毕");

            //机器组装B
            long stopTime6 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC213"));
            long timeClass6 = nowDate.HMSSTD(nigAfter2, nigAfter1);
            insertRecord.judgeRecord("ECDLC213", standardTime, stopTime6, timeClass6, nowDate.getNowDate(),"白班");
            logger.info("夜班下午1--机器组装B--定时任务执行完毕");

            //后测
            long stopTime3 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC034"));
            long timeClass3 = nowDate.HMSSTD(nigAfter2, nigAfter1);
            insertRecord.judgeRecord("ECDLC034", standardTime, stopTime3, timeClass3, nowDate.getNowDate(),"夜班");
            logger.info("夜班下午1--后测--定时任务执行完毕");

            //包装
            long stopTime4 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC026"));
            long timeClass4 = nowDate.HMSSTD(nigAfter2, nigAfter1);
            insertRecord.judgeRecord("ECDLC026", standardTime, stopTime4, timeClass4, nowDate.getNowDate(),"夜班");
            logger.info("夜班下午1--包装--定时任务执行完毕");
        }
    }

    private class myTask11 implements Runnable {
        private String name;

        myTask11(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            insertRecord.insert(nowDate.getNowDate(), nowDate.getNowDate(), "夜班");
            logger.info("夜班加班1--定时任务执行完毕");
        }
    }

    private class myTask12 implements Runnable {
        private String name;

        myTask12(String name) {
            this.name = name;
        }

        @Override
        public void run() {

            //主板预组装A
            long stopTime1 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC005"));
            long timeClass1 = nowDate.HMSSTD(nigNight2, dayNight1);
            insertRecord.judgeRecord("ECDLC005", standardTime, stopTime1, timeClass1, nowDate.getNowDate(),"夜班");
            logger.info("夜班加班1--主板预组装A--定时任务执行完毕");

            //机器组装A
            long stopTime5 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC027"));
            long timeClass5 = nowDate.HMSSTD(nigNight2, dayNight1);
            insertRecord.judgeRecord("ECDLC027", standardTime, stopTime5, timeClass5, nowDate.getNowDate(),"白班");
            logger.info("夜班加班1--机器组装A--定时任务执行完毕");

            //主板预组装B
            long stopTime2 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC205"));
            long timeClass2 = nowDate.HMSSTD(nigNight2, dayNight1);
            insertRecord.judgeRecord("ECDLC205", standardTime, stopTime2, timeClass2, nowDate.getNowDate(),"夜班");
            logger.info("夜班加班1--主板预组装B--定时任务执行完毕");

            //机器组装B
            long stopTime6 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC213"));
            long timeClass6 = nowDate.HMSSTD(nigNight2, dayNight1);
            insertRecord.judgeRecord("ECDLC213", standardTime, stopTime6, timeClass6, nowDate.getNowDate(),"白班");
            logger.info("夜班加班1--机器组装B--定时任务执行完毕");

            //后测
            long stopTime3 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC034"));
            long timeClass3 = nowDate.HMSSTD(nigNight2, dayNight1);
            insertRecord.judgeRecord("ECDLC034", standardTime, stopTime3, timeClass3, nowDate.getNowDate(),"夜班");
            logger.info("夜班加班1--后测--定时任务执行完毕");

            //包装
            long stopTime4 = nowDate.getDatePoorMins(nowDate.getNowDate(), stopRecord.selectNewTime1("ECDLC026"));
            long timeClass4 = nowDate.HMSSTD(nigNight2, dayNight1);
            insertRecord.judgeRecord("ECDLC026", standardTime, stopTime4, timeClass4, nowDate.getNowDate(),"夜班");
            logger.info("夜班加班1--包装--定时任务执行完毕");
        }
    }

}

