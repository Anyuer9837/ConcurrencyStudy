package com.concurrencystudy.mapper;

import com.concurrencystudy.dao.UserHot;

public interface UserHotMapper {
    int deleteByPrimaryKey(Integer uid);

    int insert(UserHot record);

    int insertSelective(UserHot record);

    UserHot selectByPrimaryKey(Integer uid);

    int updateByPrimaryKeySelective(UserHot record);

    int updateByPrimaryKey(UserHot record);
}