package cms.web.action.common;

import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import cms.bean.user.AccessUser;
import cms.bean.user.RefreshUser;
import cms.bean.user.User;
import cms.bean.user.UserLoginLog;
import cms.bean.user.UserState;
import cms.service.user.UserService;
import cms.utils.IpAddress;
import cms.utils.UUIDUtil;
import cms.utils.WebUtil;
import cms.utils.threadLocal.AccessUserThreadLocal;
import cms.web.action.user.UserLoginLogManage;
import cms.web.action.user.UserManage;

/**
 * 开放授权管理
 *
 */
@Component("oAuthManage")
public class OAuthManage {
	@Resource OAuthManage oAuthManage;
	@Resource UserManage userManage;
	
	@Resource UserLoginLogManage userLoginLogManage;
	@Resource UserService userService;
	
	/**
	 * 添加刷新令牌
	 * @param refreshToken
	 * @param accessToken 访问令牌
	 */
	@CachePut(value="oAuthManage_cache_refreshToken",key="#refreshToken")
    public RefreshUser addRefreshToken(String refreshToken, RefreshUser refreshUser) {
		return refreshUser;
    }

    /**
     * 添加访问令牌
     * @param accessToken
     * @param user 仅userId userName两个字段有值
     */
	@CachePut(value="oAuthManage_cache_accessToken",key="#accessToken")
    public AccessUser addAccessToken(String accessToken, AccessUser accessUser) {
		return accessUser;
    }
    
	/**
     * 根据刷新令牌获取刷新用户
     * @param refreshToken
     * @return
     */
	@Cacheable(value="oAuthManage_cache_refreshToken",key="#refreshToken")
    public RefreshUser getRefreshUserByRefreshToken(String refreshToken) {
    	return null;
    }
    /**
     * 根据访问令牌获取用户
     * @param accessToken
     * @return
     */
	@Cacheable(value="oAuthManage_cache_accessToken",key="#accessToken")
    public AccessUser getAccessUserByAccessToken(String accessToken) {
    	return null;
    }
	/**
	 * 删除刷新令牌
	 * @param refreshToken
	 * @return
	 */
	@CacheEvict(value="oAuthManage_cache_refreshToken",key="#refreshToken")
	public void deleteRefreshToken(String refreshToken){
	}
	/**
	 * 删除访问令牌
	 * @param accessToken
	 * @return
	 */
	@CacheEvict(value="oAuthManage_cache_accessToken",key="#accessToken")
	public void deleteAccessToken(String accessToken){
	}
    
    
	/**
	 * 获取登录用户
	 */
	public AccessUser getUserName(HttpServletRequest request){
		String accessToken = WebUtil.getCookieByName(request, "cms_accessToken");
		if(accessToken != null && !"".equals(accessToken.trim())){
			AccessUser accessUser = oAuthManage.getAccessUserByAccessToken(accessToken.trim());
			return accessUser;
		}
		return null;
	}
	
    
	/**
	 * 令牌续期
	 * @param oldRefreshToken 旧刷新令牌号
	 * @param refreshUser 刷新令牌
	 * @param request
	 * @param response
	 */
	public void tokenRenewal(String oldRefreshToken,RefreshUser refreshUser,HttpServletRequest request,HttpServletResponse response){
		UserState userState = userManage.query_userState(refreshUser.getUserName().trim());//用户状态
		if(userState == null || !userState.getSecurityDigest().equals(refreshUser.getSecurityDigest())){
			return;
		}
		
		//访问令牌续期
		String new_accessToken = UUIDUtil.getUUID32();
		String new_refreshToken = UUIDUtil.getUUID32();
		
		//头像路径
		String avatarPath = null;
		//头像名称
		String avatarName = null;
		User user = userManage.query_cache_findUserById(refreshUser.getUserId());
		if(user != null){
			avatarPath = user.getAvatarPath();
			avatarName = user.getAvatarName();
		}
		
		
		
		
		oAuthManage.addAccessToken(new_accessToken, new AccessUser(refreshUser.getUserId(),refreshUser.getUserName(),avatarPath,avatarName, refreshUser.getSecurityDigest(),refreshUser.isRememberMe()));
		refreshUser.setAccessToken(new_accessToken);
		oAuthManage.addRefreshToken(new_refreshToken, refreshUser);
		
		//将旧的刷新令牌的accessToken设为0
		oAuthManage.addRefreshToken(oldRefreshToken, new RefreshUser("0",refreshUser.getUserId(),refreshUser.getUserName(),avatarPath,avatarName,refreshUser.getSecurityDigest(),refreshUser.isRememberMe()));
		AccessUserThreadLocal.set(new AccessUser(refreshUser.getUserId(),refreshUser.getUserName(),avatarPath,avatarName,refreshUser.getSecurityDigest(),refreshUser.isRememberMe()));
		//将访问令牌添加到Cookie
		WebUtil.addCookie(response, "cms_accessToken", new_accessToken, 0);
		//将刷新令牌添加到Cookie
		WebUtil.addCookie(response, "cms_refreshToken", new_refreshToken, 0);
		
		//写入登录日志
		UserLoginLog userLoginLog = new UserLoginLog();
		userLoginLog.setId(userLoginLogManage.createUserLoginLogId(user.getId()));
		userLoginLog.setIp(IpAddress.getClientIpAddress(request));
		userLoginLog.setTypeNumber(20);//续期
		userLoginLog.setUserId(user.getId());
		userLoginLog.setLogonTime(new Date());
		Object new_userLoginLog = userLoginLogManage.createUserLoginLogObject(userLoginLog);
		userService.saveUserLoginLog(new_userLoginLog);
	}
    
    

}
