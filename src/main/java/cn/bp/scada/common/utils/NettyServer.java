package cn.bp.scada.common.utils;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import cn.bp.scada.common.utils.dbhelper.DBHelper;
import cn.bp.scada.modle.scada.ResultMessage;
import cn.bp.scada.service.scada.impl.OtherLostRequest;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.bp.scada.service.scada.impl.ScadaRequesDealService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ReferenceCountUtil;

/**
 * 用Netty开启socket服务器的类
 *
 * @author wuzhining
 */
@Component
public class NettyServer {

    @Resource
    private ScadaRequesDealService scadaRequesDealService;

    @Autowired
    private OtherLostRequest otherLostRequest;

    @Value("${netty.server.port}")
    private int PORT;
    @Resource
    private DBHelper db;

    private Logger LOG = LoggerFactory.getLogger(this.getClass());
    public CopyOnWriteArrayList clientList = new CopyOnWriteArrayList();
    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    public CopyOnWriteArrayList getClientList() {
        return this.clientList;
    }

    private NettyServer() {
        singleThreadExecutor.execute(new Runnable() {

            @Override
            public void run() {

                startServer();
            }
        });
    }

    /**
     * 开启socket服务器
     */
    public void startServer() {
        // 配置NIO线程组
        // boss线程组监听端口，worker线程组负责数据读写
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 服务器辅助启动类配置
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)//
                    .channel(NioServerSocketChannel.class)//
                    .option(ChannelOption.SO_BACKLOG, 1024)// 设置tcp缓冲区
                    .handler(new LoggingHandler(LogLevel.INFO))//
                    .childHandler(new ChildChannelHandler());// 客户端连接时触发

            // 绑定端口，同步等待绑定成功
            ChannelFuture f = bootstrap.bind(10000).sync();
            LOG.info("服务器准备就绪");

            // 等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            LOG.error("服务器启动失败");
            e.printStackTrace();
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 网络事件处理器
     */
    private class ChildChannelHandler extends ChannelInitializer<NioSocketChannel> {
        @Override
        protected void initChannel(NioSocketChannel ch) throws Exception {
            ConcurrentHashMap<String, Object> client = new ConcurrentHashMap<>();

            // TODO 后期用ip查询设备编号，并将设备编号跟socketChannel保存起来
            String[] ips = ch.remoteAddress().toString().replace("/", "").split(":");
            client.put("ip", ips[0]);
            client.put("client", ch);
            clientList.add(client);

            LOG.info(
                    "用户：" + ch.remoteAddress().toString().replace("/", "") + "上线，当前在线客户端数量为" + clientList.size() + "个");

            // 获取管道
            ChannelPipeline pipeline = ch.pipeline();

            //解决数据粘包问题
            // 定长解码类（根据数据长度分包）
            //pipeline.addLast(new LengthFieldBasedFrameDecoder(200*1024,0,2));
            //根据字符分包
            pipeline.addLast(new DelimiterBasedFrameDecoder(1024 * 1000 * 10, Unpooled.copiedBuffer("%".getBytes())));

            // 字符串解码
            pipeline.addLast(new StringDecoder(Charset.forName("UTF-8")));

            // 字符串编码
            pipeline.addLast(new StringEncoder(Charset.forName("UTF-8")));

            // 数据处理类
            pipeline.addLast(new NettyServerHandler());
        }
    }

    /**
     * NettyServer数据处理的实现类.
     */
    @Sharable
    class NettyServerHandler extends ChannelInboundHandlerAdapter {

        // 读取客户端发送的信息
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            try {

                InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
                String ip = insocket.getAddress().getHostAddress();
                int port = insocket.getPort();

                JSONObject resultJson = new JSONObject(msg.toString());
                String responseData = resultJson.get("obj").toString();
                JSONObject chesk = new JSONObject(responseData);
                String answerId = resultJson.getString("answerId");
                try {
                    if (!answerId.equals("") && answerId != null && !answerId.equals("null")) {
                        if (!("PQC").equals(chesk.get("result_message").toString())) {
                            LOG.info("接收数据：" + responseData);
                        }
                    } else {
                        if ("B03".equals(chesk.get("op_flag")) || "B19".equals(chesk.get("op_flag")) || "C02".equals(chesk.get("op_flag")) || "C03".equals(chesk.get("op_flag")))
                            LOG.debug("接收数据：" + responseData);
                        else
                            LOG.info("接收数据：" + responseData);
                    }
                } catch (Exception e) {
                    LOG.error("result_message没有传此字段" + e.getMessage());
                }


                String result = scadaRequesDealService.checkData(msg);

                if (result != null) {
                    ctx.write(result);
                    JSONObject resultJson1 = new JSONObject(result.toString());
                    if (!resultJson1.getString("op_flag_respon").equals("B03") && !resultJson1.getString("op_flag_respon").equals("B19") && !"C02".equals(resultJson1.get("op_flag_respon")) && !"C03".equals(resultJson1.get("op_flag_respon"))) {
                        LOG.info("发送数据：" + resultJson1);
                    } else {
                        LOG.debug("发送数据：" + resultJson1);
                    }
                    //访问远程数据，上报当前损失时间
                    JSONObject clientMessage = new JSONObject(msg.toString());
                    JSONObject msgObj = clientMessage.getJSONObject("obj");
                    String judgePosition = msgObj.get("op_flag").toString();
                    if (judgePosition.equals("B070")) {
                        otherLostRequest.requestLost("后测维修线体");
                    } /*else if (judgePosition.equals("S02")) {
                        if (msgObj.getString("device_sn").equals("ECDLC027")) {
                            otherLostRequest.requestLost("机箱组装A线体");
                        } else {
                            otherLostRequest.requestLost("机箱组装B线体");
                        }
                    }*/ else if (judgePosition.equals("C05")) {
                        otherLostRequest.requestLost("包装线体");
                    } else if (judgePosition.equals("B015")) {
                        if (msgObj.getString("device_sn").equals("ECDLC005")) {
                            otherLostRequest.requestLost("主板预组装A线体");
                        } else {
                            otherLostRequest.requestLost("主板预组装B线体");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            // Close the connection when an exception is raised.
            LOG.error(cause.getMessage());
            ctx.close();
        }

        // 客户端断开连接触发
        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            Iterator iterator = clientList.iterator();
            String ip = "";
            while (iterator.hasNext()) {
                Map<String, Object> map = (Map) iterator.next();
                if (ctx.channel() == (Channel) map.get("client")) {
                    ip = map.get("ip").toString();
                    clientList.remove(map);
                }
            }
            LOG.info(ip + "断开连接,当前在线客户端数量为" + clientList.size() + "个");
        }
    }
}
