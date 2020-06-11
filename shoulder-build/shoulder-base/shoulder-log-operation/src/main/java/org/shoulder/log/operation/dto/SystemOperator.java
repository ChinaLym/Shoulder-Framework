package org.shoulder.log.operation.dto;

import org.shoulder.core.context.BaseContextHolder;
import org.shoulder.core.util.IpUtils;
import org.shoulder.log.operation.constants.OpLogConstants;

/**
 * 有些操作（如定时任务）是非用户操作的，那么操作者就是系统了
 * @author lym
 */
public class SystemOperator implements Operator {

    private final String userId;
    private final String ip;
    private final String mac;

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public String getUserName() {
        return "system";
    }

    @Override
    public String getMac() {
        return mac;
    }

    @Override
    public String getPersonId() {
        return null;
    }

    @Override
    public String getTerminalType(){
        return OpLogConstants.TerminalType.SYSTEM;
    }

    public static SystemOperator getInstance(){
        return SingletonHolder.INSTANCE;
    }

    private SystemOperator(String userId, String ip, String mac){
        this.userId = userId;
        this.ip = ip;
        this.mac = mac;
    }

    private static class SingletonHolder{
        private static final SystemOperator INSTANCE =
                new SystemOperator("system." + BaseContextHolder.getServiceId(),
                        IpUtils.getIPFromCache(), IpUtils.getMACFromCache());
    }


}