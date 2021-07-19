package cn.bp.scada.config.token;

import java.lang.annotation.*;
//此注解加哪里，哪里就不用验证token
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TokenNotValidation {
}
