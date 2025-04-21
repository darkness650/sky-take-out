package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    public static final String WX_LOGIN="https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties properties;
    @Autowired
    private UserMapper userMapper;
    @Override
    public User getUserByCode(UserLoginDTO userLoginDTO) {
        String openid=getWxUserId(userLoginDTO.getCode());
        if(openid==null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        User user=userMapper.getByOpenId(openid);
        if(user==null){
            user=User.builder().openid(openid).createTime(LocalDateTime.now()).build();
            userMapper.insert(user);
        }
        return user;
    }

    private String getWxUserId(String code)
    {
        Map<String,String> claims=new HashMap<>();
        claims.put("appid",properties.getAppid());
        claims.put("secret",properties.getSecret());
        claims.put("grant_type","authorization_code");
        claims.put("js_code",code);
        String json=HttpClientUtil.doGet(WX_LOGIN,claims);
        JSONObject jsonObject = JSON.parseObject(json);
        String openId=jsonObject.getString("openid");
        return openId;
    }

}
