package cn.bp.scada.controller.scada;

import cn.bp.scada.config.token.JwtInterceptor;
import cn.bp.scada.config.token.JwtUtils;
import cn.bp.scada.modle.scada.User;
import cn.bp.scada.common.utils.redis.RedisUtils;
import io.jsonwebtoken.Claims;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/jwt")
public class Login {

    @Autowired
    private RedisUtils redisUtil;

    @PostMapping("/login")
    public String login(User user, HttpServletResponse response) {
        JSONObject json = new JSONObject();
       try {
           //从redis获取用户名密码
           Object users = redisUtil.get("user");
           Object password = redisUtil.get("password");

           if(users .equals(user.getUser()) && password .equals(user.getPassword()) ) {
               Map<String, Object> userInfoMap = new HashMap<>();
               userInfoMap.put("userName", users);
               userInfoMap.put("password", password);
               String token = JwtUtils.generateToken(userInfoMap);
               json.put("token",token);
               json.put("code",0);
               json.put("msg","登录成功");
           }else {
               json.put("code","401");
               json.put("msg","用户名或密码错误");
           }
       }catch (Exception e) {
           e.printStackTrace();
           json.put("code","501");
       }
        return json.toString();
    }

    @GetMapping("/test") //用户只需携带首次登录时携带的token访问接口即可
    public String test(HttpServletRequest request) {
        // 登录成功后，从request中获取用户信息
        Claims claims = (Claims) request.getAttribute(JwtInterceptor.USER_INFO_KEY);
        if (null != claims) {
            return (String) claims.get("userName")+"，密码："+(String) claims.get("password");
        } else {
            return "fail";
        }
    }
}
