package com.concurrencystudy.service;
import com.concurrencystudy.dao.UserHot;

public interface UserHotService {
    UserHot getUserByUid(Integer uid);
    boolean updateHot(UserHot userHot);
}