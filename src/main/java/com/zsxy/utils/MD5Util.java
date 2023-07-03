package com.zsxy.utils;

import cn.hutool.core.util.RandomUtil;
import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {

    public String getSalt(){
        return RandomUtil.randomString(6);
    }
    public boolean md5Compare(String pwd1, String pwd2,String salt)  {
        String mpwd2 = null;
        mpwd2 = DigestUtils.md5Hex(pwd2 + salt);
        return pwd1.equals(mpwd2);
    }
}
