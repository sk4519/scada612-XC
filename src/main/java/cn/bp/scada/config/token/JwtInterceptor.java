package cn.bp.scada.config.token;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * jwtToken校验拦截器
 */
public class JwtInterceptor extends HandlerInterceptorAdapter {
    public static final String USER_INFO_KEY = "user_info_key";

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,Object handler) throws Exception
    {
      /*  TokenNotValidation annotation = ((HandlerMethod)   handler).getMethodAnnotation(TokenNotValidation.class);
        // 如果有@TokenNotValidation ,则不验证token
        if (annotation != null) {
            return true;
        }*/
// 获取用户 token
        String token = request.getHeader(JwtUtils.getHeaderKey());
        if(StringUtils.isBlank(token)) { //空判断
            token = request.getParameter(JwtUtils.getHeaderKey());
        }

        // token为空
        if(StringUtils.isBlank(token)) {
            this.writerErrorMsg("401",
                    JwtUtils.getHeaderKey() + " 必须要带上",
                    response);
            return false;
        }

        //校验并解析token,如果token过期或改动，返回null
        Claims claims = JwtUtils.verifyAndGetClaimsByToken(token);
        if(null == claims) {
            this.writerErrorMsg("401",
                    JwtUtils.getHeaderKey() + " 失效，请重新登录",
                    response);
            return false;
        }
        //校验通过，设置用户信息到request里，在controller从request域获取用户信息
        request.setAttribute(USER_INFO_KEY,claims);
        return true;
    }

    /**
     * 利用response直接输出错误信息
     * @param code
     * @param msg
     * @param response
     * @throws IOException
     */
    private void writerErrorMsg (String code, String msg, HttpServletResponse response) throws IOException {
        JSONObject json = new JSONObject();
        json.put("code",code);
        json.put("msg",msg);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(json));
    }

}
