package cn.bp.scada.modle.scada;


public class ScadaCamera {

  private String sn;
  private String imgUrl;
  private java.sql.Date cdDt;
  private String id;


  public String getSn() {
    return sn;
  }

  public void setSn(String sn) {
    this.sn = sn;
  }


  public String getImgUrl() {
    return imgUrl;
  }

  public void setImgUrl(String imgUrl) {
    this.imgUrl = imgUrl;
  }


  public java.sql.Date getCdDt() {
    return cdDt;
  }

  public void setCdDt(java.sql.Date cdDt) {
    this.cdDt = cdDt;
  }


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

}
