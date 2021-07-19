<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>socket客户端操作界面</title>
</head>
<body bgcolor="#ffffc0">
	<%--<script type="text/javascript">

		//创建xhr对象
		function createXHR() {
			if (typeof XMLHttpRequest != "undefined") {
				return new XMLHttpRequest();
			} else if (typeof ActiveXObject != "undefined") {
				if (typeof arguments.callee.activeXString != "string") {
					var versions = [ "MSXML2.XMLHttp.6.0",
							"MSXML2.XMLHttp.3.0", "MSXML2.XMLHttp" ], i, len;

					for (i = 0, len = versions.length; i < len; i++) {
						try {
							new ActiveXObject(versions[i]);
							arguments.callee.activeXString = versions[i];
							break;
						} catch (ex) {

						}
					}
				}

				return new ActiveXObject(arguments.callee.activeXString);
			} else {
				throw new Error("NO XHR object available.")
			}
		}

		function getMethod(url) {
			var form = document.calcForm;
			var data = new FormData(form);
			console.info(data);

			var deviceNo=document.getElementById("DeviceNum").value;
			var plcAddress=document.getElementById("PLCAddress").value;
			var value=document.getElementById("Value").value;
			var data={
			    DeviceNum:DeviceNum,
				PLCAddress:PLCAddress,
				Value:Value
			}
			var msg="{'deviceNo':"+DeviceNum+",'PLCAddress':"+PLCAddress+",'value':"+Value+"}"
             if (url=="sendToClient"){
                 //url+="?data="+JSON.stringify(data);
                 url+="?deviceNo="+deviceNo+"&plcAddress="+plcAddress+"&value="+value;
            }

            var xhr = createXHR();
            xhr.onload = function() {
                if ((xhr.status >= 200 && xhr.status < 300)
						|| xhr.status == 304) {
                    //alert(xhr.responseText);
                    document.getElementById("serverMessage").value=xhr.responseText
                } else {
                    alert("Request was unsuccessful:" + xhr.status);
                }
            }
            xhr.open("get", "http://localhost:8080/scada/"+url, true);
            xhr.send(null);
		}

		function postMethod() {
			var form = document.calcForm;
			// var data = new FormData(form);
			var data1 = new FormData(document.forms[0]);
			var data2 = new FormData(document.getElementById("calcForm"));
			console.info(data);
			console.info(data1);
			console.info(data2);

            var DeviceNum=document.getElementById("DeviceNum").value;
            var PLCAddress=document.getElementById("PLCAddress").value;
            var Value=document.getElementById("Value").value;
            var data={
                DeviceNum:DeviceNum,
                PLCAddress:PLCAddress,
                Value:Value
            }

			var xhr = createXHR();
			xhr.onload = function() {
				if ((xhr.status >= 200 && xhr.status < 300)
						|| xhr.status == 304) {
					alert(xhr.responseText);
				} else {
					alert("Request was unsuccessful:" + xhr.status);
				}
			}
			xhr.open("post", "http://localhost:8080/scada/sendToService", true);
			xhr.setRequestHeader("Content-Type",
					"application/x-www-form-urlencoded")
			xhr.send(JSON.stringify(data));
		}
	</script>--%>
	<form id="calcForm" name="calcForm" action="caculator.action">
		<input type="text" name="serverMessage" id="serverMessage">
		<table align="left">
			<tbody>
				<tr>
					<td>设备编号：</td>
					<td><input type="text" name="DeviceNum" id="DeviceNum"></td>
				</tr>
				<tr>
					<td>PLC地址：</td>
					<td><input type="text" name="PLCAddress" id="PLCAddress"></td>
				</tr>
				<tr>
					<td>PLC值：</td>
					<td><input type="text" name="Value" id="Value"></td>
				</tr>
				<tr>
					<td><input type="button" onclick="getMethod('startSocketServer')"
						value="启动服务端" /> ${requestScope.message}</td>
					<td><input type="button" onclick="getMethod('sendToClient')" value="服务端发送数据" /></td>
				</tr>
				<tr>
					<td><input type="button" onclick="getMethod('startClient')"
					value="启动socket客户端" /></td>
					<td><input type="button" onclick="getMethod('alterAgvStatus')" value="更改AGV状态" /></td>
					<td><input type="button" onclick="getMethod('alterPilerStatus')" value="更改堆垛机状态" /></td>
				</tr>
			</tbody>
		</table>
	</form>
</body>
</html>