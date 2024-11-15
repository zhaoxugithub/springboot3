
package com.serendipity.service;

import cn.hutool.core.collection.ListUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.CellExtra;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.serendipity.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyDataModelListenerV2 implements ReadListener<User> {

    private final UserServiceInter userServiceInter;

    private final List<User> userList = new ArrayList<>();


    public MyDataModelListenerV2(UserServiceInter userServiceInter) {
        this.userServiceInter = userServiceInter;
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        ReadListener.super.onException(exception, context);
    }

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        ReadListener.super.invokeHead(headMap, context);
    }

    @Override
    public void invoke(User user, AnalysisContext analysisContext) {
        userList.add(user);
    }

    @Override
    public void extra(CellExtra extra, AnalysisContext context) {
        ReadListener.super.extra(extra, context);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        ListUtil.partition(userList, 1000)
                .forEach(userServiceInter::addUsers);
    }

    @Override
    public boolean hasNext(AnalysisContext context) {
        return ReadListener.super.hasNext(context);
    }
}
