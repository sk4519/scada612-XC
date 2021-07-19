package cn.bp.scada.service.scada.impl;

import cn.bp.scada.controller.scada.ScadaAndAgvMt;
import cn.bp.scada.modle.scada.ScadaRequest;
import cn.bp.scada.modle.scada.ScadaRespon;
import cn.bp.scada.service.scada.ScadaCameraService;
import cn.bp.scada.common.utils.PrimaryHelper;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.WebcamUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class ScadaCameraServiceImpl implements ScadaCameraService {
    @Value("${download.path}")
    private  String downPath;
    @Resource
    private PrimaryHelper ph;
    @Resource
    private ScadaAndAgvMt smt ;
    @Resource
    private JdbcTemplate template;

    private Logger LOG = LoggerFactory.getLogger(this.getClass());
    static BASE64Encoder encoder = new sun.misc.BASE64Encoder();
    static BASE64Decoder decoder = new sun.misc.BASE64Decoder();

    private static Map<String,Object> map= new HashMap<String,Object>();
    static {
        map.put("C213", "192.168.10.72");
        map.put("C013", "192.168.10.51");
        map.put("local", "10.50.7.183"); //用来本地测试
    }

    @Override
    public String getImageBinary(String Imgpath) {
        File f = new File(Imgpath);
        BufferedImage bi;
        try {
            bi = ImageIO.read(f);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "jpg", baos);
            byte[] bytes = baos.toByteArray();
            return encoder.encodeBuffer(bytes).trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    @Transactional
    public ScadaRespon base64StringToImage(JSONObject json) throws JSONException {
        ScadaRespon srx = new ScadaRespon();
        try {
            String base64String = json.get("con_sn").toString(); //转换后的图片二进制
            String pro_sn = json.get("pro_sn").toString();
            byte[] bytes1 = decoder.decodeBuffer(base64String);

            ByteArrayInputStream bais = new ByteArrayInputStream(bytes1);
            BufferedImage bi1 = ImageIO.read(bais);
            String path =downPath+ph.getDates();
            File file = new File(path);
            if(!file.exists()) {
                file.mkdirs();
            }

            String time = ph.getCameraDateTime();
            String imgPath=path+"/"+time+"-"+pro_sn+".jpg";
            File w2 = new File(imgPath);//可以是jpg,png,gif格式

            ImageIO.write(bi1, "jpg", w2);//不管输出什么格式图片，此处不需改动

            String dataPath =ph.getDates()+"/"+time+"-"+pro_sn+".jpg";
            //插入到数据库
            String sql = "INSERT INTO SCADA_CAMERA(id,SN,IMG_URL,CD_DT) VALUES(CAMERAID.NEXTVAL,?,?,sysdate) ";
            template.update(sql,pro_sn,dataPath);
            String querySql ="select id from (select id from scada_camera order by id desc) where rownum=1";
            Map<String, Object> stringObjectMap = template.queryForMap(querySql);
            srx.setItem_cd(stringObjectMap.get("id").toString());
            srx.setResult_flag("0");
        } catch (Exception e) {
            srx.setResult_flag("4");
            LOG.info("图片保存失败");
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly(); //手动回滚事务
        }

        return srx;
    }

    @Override
    @Transactional
    public ScadaRespon cameraPQC(String proSn, String work) throws Exception {

        ScadaRespon scadaRespon = new ScadaRespon();
        ScadaRequest srx = new ScadaRequest();
        srx.setOp_flag("Z001");
        srx.setPro_sn(proSn);
        srx.setDevice_sn("PQC");
        srx.setEt_ip(map.get(work).toString());
        LOG.info("PQC拍照发送给上位机信息："+srx);
        JSONObject deviceRespon = smt.toDevice(srx);

        if(deviceRespon.get("result_flag") .equals("OK")) {
            //调用保存照片接口
             scadaRespon = base64StringToImage(deviceRespon);
        } else if(deviceRespon.get("result_flag") .equals("NO")) {
            scadaRespon.setResult_flag("1");
        } else {
            scadaRespon.setResult_flag("2");
        }
        return scadaRespon;
    }

    @Override
    public String takePictures() {
        Webcam webcam = Webcam.getDefault();
        if(webcam == null) {
            return "没有找到摄像设备";
        }
        String filePath = "d:"+ "/picture/" + new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File path = new File(filePath);
        if (!path.exists()) {//如果文件不存在，则创建该目录
            path.mkdirs();
        }
        String time = new SimpleDateFormat("yyyMMdd_HHmmss").format(new Date());
        File file = new File(filePath + "/" + time + ".jpg");
        webcam.setViewSize(WebcamResolution.VGA.getSize());
        WebcamUtils.capture(webcam, file);
        return "拍照成功！";
    }

}
