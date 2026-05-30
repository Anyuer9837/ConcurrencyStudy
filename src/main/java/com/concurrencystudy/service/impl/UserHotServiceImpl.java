package com.concurrencystudy.service.impl;

import com.concurrencystudy.dao.UserHot;
import com.concurrencystudy.mapper.UserHotMapper;
import com.concurrencystudy.service.UserHotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserHotServiceImpl implements UserHotService {

    @Autowired
    private UserHotMapper userHotMapper;

    @Override
    public UserHot getUserByUid(Integer uid) {
        return userHotMapper.selectByPrimaryKey(uid);
    }

    @Override
    public boolean updateHot(UserHot userHot) {
        // 1. 根据前端传过来的 uid，去数据库把该用户当前完整的数据查出来
        UserHot oldData = userHotMapper.selectByPrimaryKey(userHot.getUid());

        // 2. 安全校验：如果数据库压根没这个人，直接返回失败
        if (oldData == null) {
            return false;
        }

        // 3. 核心逻辑：取出旧的 hot 值，在旧值的基础上加 1
        int newHot = oldData.getHot() + 1;

        // 4. 把加完后的新值塞回对象里
        userHot.setHot(newHot);

        // 5. 调用原有的 Mapper 接口，把最新的 hot 值更新进数据库
        // 返回受影响行数 > 0 则代表成功
        return userHotMapper.updateByPrimaryKeySelective(userHot) > 0;
    }
}