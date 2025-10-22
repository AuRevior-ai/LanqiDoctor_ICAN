package com.lanqiDoctor.demo.util;

import com.hjq.http.exception.ResponseException;
import okhttp3.Response;

/**
 * 网络错误处理工具类
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class NetworkErrorHandler {
    
    /**
     * 从ResponseException中获取HTTP状态码
     */
    private static int getResponseCode(ResponseException e) {
        try {
            Response response = e.getResponse();
            return response != null ? response.code() : -1;
        } catch (Exception ex) {
            return -1;
        }
    }
    
    /**
     * 获取发送验证码错误提示
     */
    public static String getEmailCodeErrorMessage(Exception e) {
        if (e instanceof ResponseException) {
            ResponseException re = (ResponseException) e;
            int errorCode = getResponseCode(re);
            
            switch (errorCode) {
                case 400:
                    return "邮箱格式不正确，请检查后重试";
                case 409:
                    return "该邮箱已被注册，请直接登录或使用其他邮箱";
                case 422:
                    return "请求参数有误，请检查邮箱格式";
                case 429:
                    return "发送验证码过于频繁，请稍后再试";
                case 500:
                    return "服务器暂时无法处理请求，请稍后重试";
                case 503:
                    return "邮件服务暂时不可用，请稍后重试";
                case -1:
                    return "网络请求异常，请检查网络连接";
                default:
                    return "验证码发送失败，请稍后重试（错误码：" + errorCode + "）";
            }
        }
        
        return getNetworkErrorMessage(e);
    }
    
    /**
     * 获取注册错误提示
     */
    public static String getRegisterErrorMessage(Exception e) {
        if (e instanceof ResponseException) {
            ResponseException re = (ResponseException) e;
            int errorCode = getResponseCode(re);
            
            switch (errorCode) {
                case 400:
                    return "注册信息不完整或格式不正确，请检查后重试";
                case 409:
                    return "该邮箱已被注册，请直接登录";
                case 422:
                    return "验证码错误或已过期，请重新获取";
                case 429:
                    return "注册请求过于频繁，请稍后再试";
                case 500:
                    return "服务器暂时无法处理请求，请稍后重试";
                case -1:
                    return "网络请求异常，请检查网络连接";
                default:
                    return "注册失败，请稍后重试（错误码：" + errorCode + "）";
            }
        }
        
        return getNetworkErrorMessage(e);
    }
    
    /**
     * 获取登录错误提示
     */
    public static String getLoginErrorMessage(Exception e) {
        if (e instanceof ResponseException) {
            ResponseException re = (ResponseException) e;
            int errorCode = getResponseCode(re);
            
            switch (errorCode) {
                case 400:
                    return "登录信息不完整，请检查邮箱和密码";
                case 401:
                    return "邮箱或密码错误，请重新输入";
                case 403:
                    return "账户已被锁定，请联系客服";
                case 404:
                    return "账户不存在，请先注册";
                case 429:
                    return "登录尝试次数过多，请稍后再试";
                case 500:
                    return "服务器暂时无法处理请求，请稍后重试";
                case -1:
                    return "网络请求异常，请检查网络连接";
                default:
                    return "登录失败，请稍后重试（错误码：" + errorCode + "）";
            }
        }
        
        return getNetworkErrorMessage(e);
    }
    
    /**
     * 获取通用网络错误提示
     */
    private static String getNetworkErrorMessage(Exception e) {
        if (e instanceof java.net.ConnectException) {
            return "无法连接到服务器，请检查网络连接";
        } else if (e instanceof java.net.SocketTimeoutException) {
            return "网络请求超时，请检查网络后重试";
        } else if (e instanceof java.net.UnknownHostException) {
            return "无法解析服务器地址，请检查网络设置";
        } else if (e instanceof javax.net.ssl.SSLException) {
            return "安全连接失败，请检查网络设置";
        } else {
            return "网络请求失败，请稍后重试";
        }
    }
    
    /**
     * 获取详细的错误信息（用于调试）
     */
    public static String getDetailedErrorMessage(Exception e) {
        StringBuilder sb = new StringBuilder();
        
        if (e instanceof ResponseException) {
            ResponseException re = (ResponseException) e;
            int errorCode = getResponseCode(re);
            sb.append("HTTP错误码: ").append(errorCode);
            
            try {
                Response response = re.getResponse();
                if (response != null) {
                    sb.append(", 响应消息: ").append(response.message());
                }
            } catch (Exception ex) {
                sb.append(", 无法获取响应详情");
            }
        } else {
            sb.append("异常类型: ").append(e.getClass().getSimpleName());
            sb.append(", 错误信息: ").append(e.getMessage());
        }
        
        return sb.toString();
    }

    /**
     * 获取添加好友/家庭成员错误提示
     */
    public static String getAddFriendErrorMessage(Exception e) {
        if (e instanceof ResponseException) {
            ResponseException re = (ResponseException) e;
            int errorCode = getResponseCode(re);
            
            switch (errorCode) {
                case 400:
                    return "请求参数有误，请检查输入信息";
                case 401:
                    return "您的登录已过期，请重新登录";
                case 403:
                    return "没有权限执行此操作";
                case 404:
                    return "用户不存在，请检查邮箱地址";
                case 409:
                    return "该用户已是您的家庭成员";
                case 422:
                    return "输入信息格式错误，请检查后重试";
                case 429:
                    return "操作过于频繁，请稍后再试";
                case 500:
                    return "服务器暂时无法处理请求，请稍后重试";
                case -1:
                    return "网络请求异常，请检查网络连接";
                default:
                    return "添加家庭成员失败，请稍后重试（错误码：" + errorCode + "）";
            }
        }
        
        return getNetworkErrorMessage(e);
    }
}