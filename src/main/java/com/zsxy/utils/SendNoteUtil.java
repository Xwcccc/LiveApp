package com.zsxy.utils;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.stereotype.Component;

@Component
public class SendNoteUtil {

    //验证平台信息 开发者无需任何更改

    private static final String dysmsapi = "dysmsapi.aliyuncs.com";
    private String code;
    DefaultProfile profile = DefaultProfile.getProfile("default", "*****", "*****");
    IAcsClient client = new DefaultAcsClient(profile);

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    //这一步的两个参数,一个是要发送验证码的手机号

    public String sendNoteMessgae(String PhoneNumbers){
        String code = RandomUtil.randomNumbers(6);
        this.setCode(code);
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain(dysmsapi);
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        //接受验证码的手机号
        request.putQueryParameter("PhoneNumbers", PhoneNumbers);
        //签名
        request.putQueryParameter("SignName", "*****");
        //模板代码
        request.putQueryParameter("TemplateCode", "******8");
        //用户定义的验证码内容
        request.putQueryParameter("TemplateParam","{\"code\":"+"\""+code+"\"}");
        try {
            CommonResponse response = client.getCommonResponse(request);
            String returnStr = response.getData();
            System.out.println(returnStr);
            JSONObject jsonObject = JSONUtil.parseObj(returnStr);    
            //返回的信息
            return jsonObject.getStr("Message");
        } catch (ServerException e) {
            return e.getErrMsg();
        } catch (ClientException e) {
            return e.getErrMsg();
        }
    };

}
