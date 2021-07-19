package cn.bp.scada.modle.scada;

public class ResultMessage {
	private String head = "##";
	private String id;
	private String answerId;
	private String from;
	private String to;
	private String type;
	private Object obj;
	private String time;
	private String op_flag_respon;
	private String tail = "$";


	public String getOp_flag_respon() {
		return op_flag_respon;
	}

	public void setOp_flag_respon(String op_flag_respon) {
		this.op_flag_respon = op_flag_respon;
	}

	public String getHead() {
		return head;
	}

	public void setHead(String head) {
		this.head = head;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAnswerId() {
		return answerId;
	}

	public void setAnswerId(String answerId) {
		this.answerId = answerId;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}

	public String getTime() {
		return time;
	}

//	public T getObj() {
//		return obj;
//	}
//
//	public void setObj(T obj) {
//		this.obj = obj;
//	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getTail() {
		return tail;
	}

	public void setTail(String tail) {
		this.tail = tail;
	}

}
