package cn.bp.scada.modle.scada;

import lombok.Data;

import java.io.Serializable;

@Data //使用lombox getset
public class User implements Serializable {

    private String user;
    private String password;

}
