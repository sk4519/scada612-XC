package cn.bp.scada.controller.scada;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.bp.scada.service.scada.impl.ScadaRequesDealService;
import cn.bp.scada.common.utils.NettyServer;
import io.netty.channel.Channel;

@RestController
public class NettyServerController {
    @Resource
    NettyServer nettyServer;

    @Resource
    ScadaRequesDealService scadaRequesDealService;
    private Logger LOG = LoggerFactory.getLogger(this.getClass());
    private Lock lock = new ReentrantLock();

    @SuppressWarnings("all")
    @RequestMapping("/setToClient")
    public String setToClient(String ip, String msg) {
        CopyOnWriteArrayList<?> clientList = nettyServer.clientList;
        Iterator<?> iterator = clientList.iterator();
        Channel channel = null;
        while (iterator.hasNext()) {
            Map<String, Object> map = (Map) iterator.next();

            if (ip.equals(map.get("ip").toString())) {
                channel = (Channel) map.get("client");
            }
            ;
        }
        String result = null;
		lock.lock();
        try {

            if (channel != null) {
                channel.write(msg);
                channel.flush();
            }
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

            int counts = 0;
            while (true) {
                if (scadaRequesDealService.isRespon == 1) {
                    LOG.info("scadaRequesDealService.isRespon 为："+scadaRequesDealService.isRespon);
                    counts =  0;
                    result = scadaRequesDealService.responMsg;
                    break;
                } else {
                    try {
                        Thread.sleep(1000);
                        counts++;
                        LOG.info("发给上位机：" + ip + "，上位机无响应，等待" + counts + "秒");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (counts >= 6) {
                    LOG.info("进入了count>=6");
                    counts = 0;
                    scadaRequesDealService.isRespon = 1;
                }
            }

            scadaRequesDealService.isRespon = 0;
            scadaRequesDealService.responMsg = null;
        } finally {
            lock.unlock(); //释放锁
			LOG.info("释放锁，IP:"+ip);
        }
        return result;
    }
}
