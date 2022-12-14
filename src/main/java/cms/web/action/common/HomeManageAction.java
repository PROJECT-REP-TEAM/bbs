package cms.web.action.common;

import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.type.TypeReference;

import cms.bean.ErrorView;
import cms.bean.PageForm;
import cms.bean.PageView;
import cms.bean.QueryResult;
import cms.bean.favorite.Favorites;
import cms.bean.follow.Follow;
import cms.bean.follow.Follower;
import cms.bean.like.Like;
import cms.bean.membershipCard.MembershipCardOrder;
import cms.bean.message.PrivateMessage;
import cms.bean.message.Remind;
import cms.bean.message.SubscriptionSystemNotify;
import cms.bean.message.SystemNotify;
import cms.bean.message.UnreadMessage;
import cms.bean.payment.PaymentLog;
import cms.bean.question.Answer;
import cms.bean.question.AnswerReply;
import cms.bean.question.Question;
import cms.bean.question.QuestionTag;
import cms.bean.question.QuestionTagAssociation;
import cms.bean.redEnvelope.GiveRedEnvelope;
import cms.bean.redEnvelope.ReceiveRedEnvelope;
import cms.bean.setting.SystemSetting;
import cms.bean.topic.Comment;
import cms.bean.topic.HideTagType;
import cms.bean.topic.Reply;
import cms.bean.topic.Tag;
import cms.bean.topic.Topic;
import cms.bean.topic.TopicUnhide;
import cms.bean.user.AccessUser;
import cms.bean.user.DisableUserName;
import cms.bean.user.FormCaptcha;
import cms.bean.user.PointLog;
import cms.bean.user.RefreshUser;
import cms.bean.user.ResourceEnum;
import cms.bean.user.RewardPointInfo;
import cms.bean.user.User;
import cms.bean.user.UserCustom;
import cms.bean.user.UserDynamic;
import cms.bean.user.UserGrade;
import cms.bean.user.UserInputValue;
import cms.bean.user.UserLoginLog;
import cms.bean.user.UserRole;
import cms.bean.user.UserRoleGroup;
import cms.service.favorite.FavoriteService;
import cms.service.follow.FollowService;
import cms.service.like.LikeService;
import cms.service.membershipCard.MembershipCardService;
import cms.service.message.PrivateMessageService;
import cms.service.message.RemindService;
import cms.service.message.SystemNotifyService;
import cms.service.payment.PaymentService;
import cms.service.question.AnswerService;
import cms.service.question.QuestionService;
import cms.service.question.QuestionTagService;
import cms.service.redEnvelope.RedEnvelopeService;
import cms.service.setting.SettingService;
import cms.service.template.TemplateService;
import cms.service.topic.CommentService;
import cms.service.topic.TagService;
import cms.service.topic.TopicService;
import cms.service.user.UserCustomService;
import cms.service.user.UserGradeService;
import cms.service.user.UserRoleService;
import cms.service.user.UserService;
import cms.utils.Base64;
import cms.utils.FileUtil;
import cms.utils.HtmlEscape;
import cms.utils.IpAddress;
import cms.utils.JsonUtils;
import cms.utils.RefererCompare;
import cms.utils.SHA;
import cms.utils.SecureLink;
import cms.utils.UUIDUtil;
import cms.utils.Verification;
import cms.utils.WebUtil;
import cms.utils.threadLocal.AccessUserThreadLocal;
import cms.web.action.AccessSourceDeviceManage;
import cms.web.action.CSRFTokenManage;
import cms.web.action.TextFilterManage;
import cms.web.action.favorite.FavoriteManage;
import cms.web.action.fileSystem.FileManage;
import cms.web.action.follow.FollowManage;
import cms.web.action.follow.FollowerManage;
import cms.web.action.like.LikeManage;
import cms.web.action.mediaProcess.MediaProcessQueueManage;
import cms.web.action.message.PrivateMessageManage;
import cms.web.action.message.RemindManage;
import cms.web.action.message.SubscriptionSystemNotifyManage;
import cms.web.action.message.SystemNotifyManage;
import cms.web.action.question.AnswerManage;
import cms.web.action.question.QuestionManage;
import cms.web.action.redEnvelope.RedEnvelopeManage;
import cms.web.action.setting.SettingManage;
import cms.web.action.sms.SmsManage;
import cms.web.action.topic.CommentManage;
import cms.web.action.topic.TopicManage;
import cms.web.action.user.RoleAnnotation;
import cms.web.action.user.UserManage;
import cms.web.action.user.UserRoleManage;
import cms.web.taglib.Configuration;

/**
 * ????????????
 *
 */
@Controller
public class HomeManageAction {

	@Resource TemplateService templateService;
	@Resource UserService userService;
	@Resource UserGradeService userGradeService;
	
	@Resource AccessSourceDeviceManage accessSourceDeviceManage;
	@Resource FileManage fileManage;
	
	@Resource CaptchaManage captchaManage;
	
	
	@Resource SettingService settingService;
	@Resource UserCustomService userCustomService;
	@Resource CommentService commentService;
	
	@Resource TagService tagService;
	@Resource TopicService topicService;
	@Resource TextFilterManage textFilterManage;
	
	
	@Resource SettingManage settingManage;
	
	@Resource SmsManage smsManage;
	
	@Resource CSRFTokenManage csrfTokenManage;
	@Resource UserManage userManage;
	@Resource PrivateMessageManage privateMessageManage;
	@Resource PrivateMessageService privateMessageService;
	@Resource SystemNotifyService systemNotifyService;
	@Resource SubscriptionSystemNotifyManage subscriptionSystemNotifyManage;
	@Resource SystemNotifyManage systemNotifyManage;
	@Resource RemindService remindService;
	@Resource TopicManage topicManage;
	@Resource RemindManage remindManage;
	
	@Resource OAuthManage oAuthManage;
	
	
	@Resource FavoriteService favoriteService;
	
	@Resource FavoriteManage favoriteManage;
	
	@Resource CommentManage commentManage;
	
	@Resource LikeService likeService;
	@Resource LikeManage likeManage;
	
	@Resource FollowService followService;
	@Resource FollowManage followManage;
	@Resource FollowerManage followerManage;
	
	@Resource UserRoleManage userRoleManage;
	@Resource PaymentService paymentService;
	@Resource MembershipCardService membershipCardService;
	
	@Resource UserRoleService userRoleService;
	
	@Resource QuestionManage questionManage;
	@Resource AnswerManage answerManage;
	
	@Resource QuestionService questionService;
	@Resource QuestionTagService questionTagService;
	@Resource AnswerService answerService;
	@Resource MediaProcessQueueManage mediaProcessQueueManage;
	@Resource RedEnvelopeService redEnvelopeService;
	@Resource RedEnvelopeManage redEnvelopeManage;
	
	
	//?  ?????????????????????
	//*  ??????0???????????????????????????
	//** ??????0?????????????????????
	private PathMatcher matcher = new AntPathMatcher(); 
	
	
	/**--------------------------------- ?????? -----------------------------------**/
	/**
	 * ???????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/home",method=RequestMethod.GET) 
	public String homeUI(ModelMap model,String userName,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		

	    Map<String,Object> returnValue = new HashMap<String,Object>();//?????????
	    
	    //??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();

	  	String _userName = accessUser.getUserName();
	  	//?????????????????????
	  	boolean flag = true;
	  	if(userName != null && !"".equals(userName.trim())){
	  		if(!userName.trim().equals(accessUser.getUserName())){
	  			_userName = userName.trim();
	  			flag = false;
	  		}
	  	}
	  	
	  	
	  	
	  	
	    //??????????????????
  		User new_user = userManage.query_cache_findUserByUserName(_userName);
  		if(new_user != null && new_user.getState().equals(1) && new_user.getCancelAccountTime().equals(-1L)){
  			List<UserGrade> userGradeList = userGradeService.findAllGrade_cache();//????????????????????????
			if(userGradeList != null && userGradeList.size() >0){
				for(UserGrade userGrade : userGradeList){
					if(new_user.getPoint() >= userGrade.getNeedPoint()){
						new_user.setGradeId(userGrade.getId());
						new_user.setGradeName(userGrade.getName());//?????????????????????????????????
						break;
					}
				} 
					
				
			}
			
			List<String> userRoleNameList = userRoleManage.queryUserRoleName(new_user.getUserName());
			
			
      		if(flag){
      			//????????????????????????
    			User viewUser = new User();
    			viewUser.setId(new_user.getId());
    			viewUser.setUserName(new_user.getUserName());//???????????????
    			viewUser.setAccount(new_user.getAccount());//??????
    			viewUser.setNickname(new_user.getNickname());//??????
    			viewUser.setState(new_user.getState());//????????????
    			viewUser.setEmail(new_user.getEmail());//????????????
    			viewUser.setIssue(new_user.getIssue());//??????????????????
    			viewUser.setRegistrationDate(new_user.getRegistrationDate());//????????????
    			viewUser.setPoint(new_user.getPoint());//????????????
    			viewUser.setGradeId(new_user.getGradeId());//??????Id
    			viewUser.setGradeName(new_user.getGradeName());//????????????
    			viewUser.setMobile(new_user.getMobile());//????????????
    			viewUser.setRealNameAuthentication(new_user.isRealNameAuthentication());//??????????????????
    			viewUser.setAvatarPath(fileManage.fileServerAddress()+new_user.getAvatarPath());//????????????
    			viewUser.setAvatarName(new_user.getAvatarName());//????????????
    			
    			if(userRoleNameList != null && userRoleNameList.size() >0){
    				viewUser.setUserRoleNameList(userRoleNameList);//???????????????????????????????????????
    			}
    			
      			model.addAttribute("user", viewUser);
          		returnValue.put("user", viewUser);
      		}else{//?????????????????????????????????????????????????????????
      			User other_user = new User();
      			other_user.setId(new_user.getId());//Id
      			other_user.setUserName(new_user.getUserName());//???????????????
      			other_user.setAccount(new_user.getAccount());//??????
      			other_user.setNickname(new_user.getNickname());//??????
      			other_user.setState(new_user.getState());//????????????
      			other_user.setPoint(new_user.getPoint());//????????????
      			other_user.setGradeId(new_user.getGradeId());//??????Id
      			other_user.setGradeName(new_user.getGradeName());//????????????
      			other_user.setAvatarPath(fileManage.fileServerAddress()+new_user.getAvatarPath());//????????????
      			other_user.setAvatarName(new_user.getAvatarName());//????????????
      			if(userRoleNameList != null && userRoleNameList.size() >0){
      				other_user.setUserRoleNameList(userRoleNameList);//???????????????????????????????????????
    			}
      			model.addAttribute("user", other_user);
          		returnValue.put("user", other_user);
      		}
      		
      		
      	}
     	if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(returnValue), "json", response);
			return null;
		}else{
			String dirName = templateService.findTemplateDir_cache();
			return "templates/"+dirName+"/"+accessSourceDeviceManage.accessDevices(request)+"/home";	
		}
	}	
	
	/**
	 * ??????????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/topicList",method=RequestMethod.GET) 
	public String topicListUI(ModelMap model,PageForm pageForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		
		
		//????????????????????????
		PageView<Topic> pageView = new PageView<Topic>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		//?????????
		int firstindex = (pageForm.getPage()-1)*pageView.getMaxresult();
		
		 //??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
    	
    		
		StringBuffer jpql = new StringBuffer("");
		//???????????????
		List<Object> params = new ArrayList<Object>();
		jpql.append(" and o.userName=?"+ (params.size()+1));
		params.add(accessUser.getUserName());
		
		jpql.append(" and o.status<?"+ (params.size()+1));
		params.add(100);
		
		jpql.append(" and o.isStaff=?"+ (params.size()+1));
		params.add(false);
		
		//???????????????and
		String _jpql = org.apache.commons.lang3.StringUtils.difference(" and", jpql.toString());
		
		//??????
		LinkedHashMap<String,String> orderby = new LinkedHashMap<String,String>();
		
		orderby.put("id", "desc");//??????id??????????????????
	
		
		QueryResult<Topic> qr = topicService.getScrollData(Topic.class, firstindex, pageView.getMaxresult(),_jpql, params.toArray(),orderby);
		if(qr.getResultlist() != null && qr.getResultlist().size() >0){
			for(Topic o :qr.getResultlist()){
    			o.setIpAddress(null);//IP???????????????
    		}
			List<Tag> tagList = tagService.findAllTag_cache();
			if(tagList != null && tagList.size() >0){
				for(Topic topic : qr.getResultlist()){
					for(Tag tag : tagList){
						if(topic.getTagId().equals(tag.getId())){
							topic.setTagName(tag.getName());
							break;
						}
					}
					
				}
			}	
		}
		
		//??????????????????????????????List
		pageView.setQueryResult(qr);		
    	
		model.addAttribute("pageView", pageView);
		
		if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(pageView), "json", response);
			return null;
		}else{
			String dirName = templateService.findTemplateDir_cache();
		    return "templates/"+dirName+"/"+accessSourceDeviceManage.accessDevices(request)+"/topicList";	
		}
	}
	
	/**
	 * ??????????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/commentList",method=RequestMethod.GET) 
	public String commentListUI(ModelMap model,PageForm pageForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		
		
		//????????????????????????
		PageView<Comment> pageView = new PageView<Comment>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		//?????????
		int firstindex = (pageForm.getPage()-1)*pageView.getMaxresult();
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
    		
		StringBuffer jpql = new StringBuffer("");
		//???????????????
		List<Object> params = new ArrayList<Object>();
		jpql.append(" and o.userName=?"+ (params.size()+1));
		params.add(accessUser.getUserName());
		
		jpql.append(" and o.isStaff=?"+ (params.size()+1));
		params.add(false);
		
		jpql.append(" and o.status<?"+ (params.size()+1));
		params.add(100);
		
		//???????????????and
		String _jpql = org.apache.commons.lang3.StringUtils.difference(" and", jpql.toString());
		
		//??????
		LinkedHashMap<String,String> orderby = new LinkedHashMap<String,String>();
		
		orderby.put("id", "desc");//??????id??????????????????
	
		
		QueryResult<Comment> qr = commentService.getScrollData(Comment.class, firstindex, pageView.getMaxresult(),_jpql, params.toArray(),orderby);
		if(qr.getResultlist() != null && qr.getResultlist().size() >0){
			List<Long> topicIdList = new ArrayList<Long>();
			for(Comment o :qr.getResultlist()){
    			o.setIpAddress(null);//IP???????????????
    			
    			o.setContent(textFilterManage.filterText(textFilterManage.specifyHtmlTagToText(o.getContent())));
    			if(!topicIdList.contains(o.getTopicId())){
    				topicIdList.add(o.getTopicId());
    			}
    		}
			List<Topic> topicList = topicService.findTitleByIdList(topicIdList);
			if(topicList != null && topicList.size() >0){
				for(Comment o :qr.getResultlist()){
					for(Topic topic : topicList){
						if(topic.getId().equals(o.getTopicId())){
							o.setTopicTitle(topic.getTitle());
							break;
						}
					}
					
				}
			}	
		}
		
		//??????????????????????????????List
		pageView.setQueryResult(qr);		
    	
		model.addAttribute("pageView", pageView);
		
		if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(pageView), "json", response);
			return null;
		}else{
			String dirName = templateService.findTemplateDir_cache();
		    return "templates/"+dirName+"/"+accessSourceDeviceManage.accessDevices(request)+"/commentList";	
		}
	}
	
	/**
	 * ??????????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/replyList",method=RequestMethod.GET) 
	public String replyListUI(ModelMap model,PageForm pageForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		
		
		//????????????????????????
		PageView<Reply> pageView = new PageView<Reply>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		//?????????
		int firstindex = (pageForm.getPage()-1)*pageView.getMaxresult();
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
    		
		StringBuffer jpql = new StringBuffer("");
		//???????????????
		List<Object> params = new ArrayList<Object>();
		jpql.append(" and o.userName=?"+ (params.size()+1));
		params.add(accessUser.getUserName());
		
		jpql.append(" and o.isStaff=?"+ (params.size()+1));
		params.add(false);
		
		jpql.append(" and o.status<?"+ (params.size()+1));
		params.add(100);
		
		//???????????????and
		String _jpql = org.apache.commons.lang3.StringUtils.difference(" and", jpql.toString());
		
		//??????
		LinkedHashMap<String,String> orderby = new LinkedHashMap<String,String>();
		
		orderby.put("id", "desc");//??????id??????????????????
	
		
		QueryResult<Reply> qr = topicService.getScrollData(Reply.class, firstindex, pageView.getMaxresult(),_jpql, params.toArray(),orderby);
		
		if(qr.getResultlist() != null && qr.getResultlist().size() >0){
			List<Long> topicIdList = new ArrayList<Long>();
			for(Reply o :qr.getResultlist()){
    			o.setIpAddress(null);//IP???????????????
    			
    			if(!topicIdList.contains(o.getTopicId())){
    				topicIdList.add(o.getTopicId());
    			}
    		}
			List<Topic> topicList = topicService.findTitleByIdList(topicIdList);
			if(topicList != null && topicList.size() >0){
				for(Reply o :qr.getResultlist()){
					for(Topic topic : topicList){
						if(topic.getId().equals(o.getTopicId())){
							o.setTopicTitle(topic.getTitle());
							break;
						}
					}
					
				}
			}	
		}
		
		//??????????????????????????????List
		pageView.setQueryResult(qr);		
    	
		model.addAttribute("pageView", pageView);
		
		if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(pageView), "json", response);
			return null;
		}else{
			String dirName = templateService.findTemplateDir_cache();
		    return "templates/"+dirName+"/"+accessSourceDeviceManage.accessDevices(request)+"/replyList";	
		}
	}
	
	
	/**
	 * ??????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/point",method=RequestMethod.GET) 
	public String pointUI(ModelMap model,PageForm pageForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		
		Map<String,Object> returnValue = new HashMap<String,Object>();//?????????

		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
		    		
    	
    	//????????????????????????
		PageView<PointLog> pageView = new PageView<PointLog>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		//?????????
		int firstIndex = (pageForm.getPage()-1)*pageView.getMaxresult();
		
		
		User user = userManage.query_cache_findUserByUserName(accessUser.getUserName());
		if(user != null){
			QueryResult<PointLog> qr =  userService.findPointLogPage(user.getId(),user.getUserName(),firstIndex, pageView.getMaxresult());
			//??????????????????????????????List
			pageView.setQueryResult(qr);
			request.setAttribute("pageView", pageView);
			returnValue.put("pageView", pageView);
			List<UserGrade> userGradeList = userGradeService.findAllGrade_cache();//????????????????????????
			if(userGradeList != null && userGradeList.size() >0){
				for(UserGrade userGrade : userGradeList){
					if(user.getPoint() >= userGrade.getNeedPoint()){
						user.setGradeId(userGrade.getId());
						user.setGradeName(userGrade.getName());//?????????????????????????????????
						break;
					}
				} 
			}
			
			//????????????????????????
			User viewUser = new User();
			viewUser.setId(user.getId());
			viewUser.setUserName(user.getUserName());//???????????????
			viewUser.setNickname(user.getNickname());//??????
			viewUser.setEmail(user.getEmail());//????????????
			viewUser.setIssue(user.getIssue());//??????????????????
			viewUser.setRegistrationDate(user.getRegistrationDate());//????????????
			viewUser.setPoint(user.getPoint());//????????????
			viewUser.setGradeId(user.getGradeId());//??????Id
			viewUser.setGradeName(user.getGradeName());//?????????????????????????????????
			
			
			model.addAttribute("user",viewUser);
			returnValue.put("user",viewUser);
		}
		
		SystemSetting systemSetting = settingService.findSystemSetting_cache();
		if(systemSetting != null){
			RewardPointInfo rewardPointInfo = new RewardPointInfo();
			rewardPointInfo.setTopic_rewardPoint(systemSetting.getTopic_rewardPoint());
			rewardPointInfo.setComment_rewardPoint(systemSetting.getComment_rewardPoint());
			rewardPointInfo.setReply_rewardPoint(systemSetting.getReply_rewardPoint());
			rewardPointInfo.setQuestion_rewardPoint(systemSetting.getQuestion_rewardPoint());
			rewardPointInfo.setAnswer_rewardPoint(systemSetting.getAnswer_rewardPoint());
			rewardPointInfo.setAnswerReply_rewardPoint(systemSetting.getAnswerReply_rewardPoint());

			model.addAttribute("rewardPointInfo",rewardPointInfo);
			returnValue.put("rewardPointInfo",rewardPointInfo);
			
		}
		
		if(isAjax == true){
			WebUtil.writeToWeb(JsonUtils.toJSONString(returnValue), "json", response);
			return null;
		}else{
			String dirName = templateService.findTemplateDir_cache();
			
			return "templates/"+dirName+"/"+accessSourceDeviceManage.accessDevices(request)+"/point";	
		}
		
		
	}
	
	
	

	
	/**----------------------------------- ???????????? ----------------------------------**/
	/**
	 * ???????????? ??????
	 * @param encryptionData ????????????
	 * @param secretKey ??????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/editUser",method=RequestMethod.GET) 
	public String editUserUI(ModelMap model,User formbean,String jumpUrl,
			String encryptionData,String secretKey,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {	
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		Map<String,Object> returnValue = new HashMap<String,Object>();//?????????
		 
		String dirName = templateService.findTemplateDir_cache();
		
		String accessPath = accessSourceDeviceManage.accessDevices(request);
	    
	    
	    
	    //????????????????????????
	  	boolean errorDisplay = false; 	
	  	if(model != null && model.get("error") != null){
	    	errorDisplay = true;
	    }

	  	//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	  		
		User user = userService.findUserByUserName(accessUser.getUserName());
		List<UserGrade> userGradeList = userGradeService.findAllGrade();//????????????????????????
		if(userGradeList != null && userGradeList.size() >0){
			for(UserGrade userGrade : userGradeList){
				if(user.getPoint() >= userGrade.getNeedPoint()){
					user.setGradeId(userGrade.getId());
					user.setGradeName(userGrade.getName());//?????????????????????????????????
					break;
				}
			} 
		}
		
		List<UserCustom> userCustomList = userCustomService.findAllUserCustom_cache();
		if(userCustomList != null && userCustomList.size() >0){		
			Iterator <UserCustom> it = userCustomList.iterator();  
			while(it.hasNext()){  
				UserCustom userCustom = it.next();
				if(userCustom.isVisible() == false){//???????????????
					it.remove();  
					continue;
				}
				if(userCustom.getValue() != null && !"".equals(userCustom.getValue().trim())){
					LinkedHashMap<String,String> itemValue = JsonUtils.toGenericObject(userCustom.getValue(), new TypeReference<LinkedHashMap<String,String>>(){});
					userCustom.setItemValue(itemValue);
				}
				
			}
		}
		
		List<UserInputValue> userInputValueList= userCustomService.findUserInputValueByUserName(user.getId());
		if(userInputValueList != null && userInputValueList.size() >0){
			for(UserCustom userCustom : userCustomList){
				for(UserInputValue userInputValue : userInputValueList){
					if(userCustom.getId().equals(userInputValue.getUserCustomId())){
						userCustom.addUserInputValue(userInputValue);
					}
				}
			}
		}
		
		if(errorDisplay == false){//????????????????????????
			model.addAttribute("userCustomList", userCustomList);
		}
		
		
		//????????????????????????
		User viewUser = new User();
		viewUser.setId(user.getId());
		viewUser.setUserName(user.getUserName());//???????????????
		viewUser.setAccount(user.getAccount());//??????
		viewUser.setNickname(user.getNickname());//??????
		viewUser.setAllowUserDynamic(user.getAllowUserDynamic());//??????????????????????????????
		viewUser.setEmail(user.getEmail());//????????????
		viewUser.setIssue(user.getIssue());//??????????????????
		viewUser.setRegistrationDate(user.getRegistrationDate());//????????????
		viewUser.setPoint(user.getPoint());//????????????
		viewUser.setGradeId(user.getGradeId());//??????Id
		viewUser.setGradeName(user.getGradeName());//?????????????????????????????????
		viewUser.setAvatarPath(fileManage.fileServerAddress()+user.getAvatarPath());//????????????
		viewUser.setAvatarName(user.getAvatarName());//????????????
		viewUser.setType(user.getType());
		viewUser.setPlatformUserId(user.getPlatformUserId());
		
		model.addAttribute("user",viewUser);

		
		//?????????????????????
		List<UserRole> validUserRoleList = new ArrayList<UserRole>();
		
		//??????????????????
		List<UserRole> userRoleList = userRoleService.findAllRole_cache();
		if(userRoleList != null && userRoleList.size() >0){
			List<UserRoleGroup> userRoleGroupList = userRoleManage.query_cache_findRoleGroupByUserName(user.getUserName());
			
			
			for(UserRole userRole : userRoleList){
				if(userRole.getDefaultRole()){//?????????????????????
					continue;
				}else{
					//????????????  ???,???,???,???,???,???,??????    
	                DateTime defaultTime = new DateTime(2999, 1, 1, 0, 0);// 2999???1???1???0???0???
	                Date validPeriodEnd = defaultTime.toDate();
					userRole.setValidPeriodEnd(validPeriodEnd);
				}
				
				if(userRoleGroupList != null && userRoleGroupList.size() >0){
					for(UserRoleGroup userRoleGroup : userRoleGroupList){
						if(userRole.getId().equals(userRoleGroup.getUserRoleId())){
							UserRole validUserRole = new UserRole();
							validUserRole.setId(userRole.getId());
							validUserRole.setName(userRole.getName());
							validUserRole.setValidPeriodEnd(userRoleGroup.getValidPeriodEnd());
							validUserRoleList.add(validUserRole);
						}
					}
				}
			}
		}
		model.addAttribute("userRoleList", validUserRoleList);
		
		
		
		
		
		if(isAjax){
			returnValue.put("user", viewUser);
			returnValue.put("userCustomList", userCustomList);
			returnValue.put("userRoleList", validUserRoleList);

			WebUtil.writeToWeb(JsonUtils.toJSONString(returnValue), "json", response);
			return null;
		}else{
			return "templates/"+dirName+"/"+accessPath+"/editUser";	
		}
		
	}
	
	
	/**
	 * ???????????? 
	 * @param oldPassword ?????????
	 * @param token ??????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/editUser",method=RequestMethod.POST) 
	public String editUser(ModelMap model,User formbean,String jumpUrl,
			String oldPassword,String token,
			RedirectAttributes redirectAttrs,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {	
		
		
	   
		
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		Map<String,String> error = new HashMap<String,String>();//??????
		SystemSetting systemSetting = settingService.findSystemSetting_cache();
		if(systemSetting.getCloseSite().equals(2)){
			error.put("user", ErrorView._21.name());//?????????????????????????????????
		}
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
		
		User user = userService.findUserByUserName(accessUser.getUserName());
		

		User new_user = new User();
		
		
		//????????????
		if(token != null && !"".equals(token.trim())){	
			String token_sessionid = csrfTokenManage.getToken(request);//????????????
			if(token_sessionid != null && !"".equals(token_sessionid.trim())){
				if(!token_sessionid.equals(token)){
					error.put("token", ErrorView._13.name());
				}
			}else{
				error.put("token", ErrorView._12.name());
			}
		}else{
			error.put("token", ErrorView._11.name());
		}
		
		
		List<UserCustom> userCustomList = userCustomService.findAllUserCustom_cache();

		
		//????????????????????????????????????
		if(userCustomList != null && userCustomList.size() >0){	
			for(UserCustom userCustom : userCustomList){
				//???????????????????????????????????????????????????
				List<UserInputValue> userInputValueList = new ArrayList<UserInputValue>();
				
				if(userCustom.isVisible() == true){//??????
					if(userCustom.getValue() != null && !"".equals(userCustom.getValue().trim())){
						LinkedHashMap<String,String> itemValue = JsonUtils.toGenericObject(userCustom.getValue(), new TypeReference<LinkedHashMap<String,String>>(){});
						userCustom.setItemValue(itemValue);
					}
					if(userCustom.getChooseType().equals(1)){//1.?????????
						
						String userCustom_value = request.getParameter("userCustom_"+userCustom.getId());
						if(userCustom_value != null && !"".equals(userCustom_value.trim())){
							UserInputValue userInputValue = new UserInputValue();
							userInputValue.setUserCustomId(userCustom.getId());
							userInputValue.setContent(userCustom_value.trim());
							userInputValueList.add(userInputValue);
							
							
							
							if(userCustom.getMaxlength() != null && userCustom_value.length() > userCustom.getMaxlength()){
								error.put("userCustom_"+userCustom.getId(), "????????????"+userCustom_value.length()+"?????????");
							}
							
							int fieldFilter = userCustom.getFieldFilter();//??????????????????    0.???  1.?????????????????????  2.?????????????????????  3.??????????????????????????????  4.?????????????????????  5.?????????????????????
							switch(fieldFilter){
								case 1 : //????????????
									if(Verification.isPositiveIntegerZero(userCustom_value.trim()) == false){
										error.put("userCustom_"+userCustom.getId(),ErrorView._804.name());//?????????????????????
									}
								  break; 
								case 2 : //????????????
									if(Verification.isLetter(userCustom_value.trim()) == false){
										error.put("userCustom_"+userCustom.getId(), ErrorView._805.name());//?????????????????????
									}
								  break;
								case 3 : //???????????????????????????
									if(Verification.isNumericLetters(userCustom_value.trim()) == false){
										error.put("userCustom_"+userCustom.getId(), ErrorView._806.name());//??????????????????????????????
									}
								  break;
								case 4 : //??????????????????
									if(Verification.isChineseCharacter(userCustom_value.trim()) == false){
										error.put("userCustom_"+userCustom.getId(), ErrorView._807.name());//?????????????????????
									}
								  break;
								case 5 : //?????????????????????
									if(userCustom_value.trim().matches(userCustom.getRegular())== false){
										error.put("userCustom_"+userCustom.getId(), ErrorView._808.name());//????????????
									}
								  break;
							//	default:
							}
						}else{
							if(userCustom.isRequired() == true){//????????????	
								error.put("userCustom_"+userCustom.getId(), ErrorView._809.name());//?????????
							}
							
						}	
						userCustom.setUserInputValueList(userInputValueList);
					}else if(userCustom.getChooseType().equals(2)){//2.?????????
						
						String userCustom_value = request.getParameter("userCustom_"+userCustom.getId());
	
						if(userCustom_value != null && !"".equals(userCustom_value.trim())){
							
							String itemValue = userCustom.getItemValue().get(userCustom_value.trim());
							if(itemValue != null ){
								UserInputValue userInputValue = new UserInputValue();
								userInputValue.setUserCustomId(userCustom.getId());
								userInputValue.setOptions(userCustom_value.trim());
								userInputValueList.add(userInputValue);
								
							}else{
								if(userCustom.isRequired() == true){//????????????	
									error.put("userCustom_"+userCustom.getId(), ErrorView._809.name());//?????????
								}
							}
							
						}else{
							if(userCustom.isRequired() == true){//????????????	
								error.put("userCustom_"+userCustom.getId(), ErrorView._809.name());//?????????
							}
						}
						userCustom.setUserInputValueList(userInputValueList);	
						
					}else if(userCustom.getChooseType().equals(3)){//3.?????????
						
						String[] userCustom_value_arr = request.getParameterValues("userCustom_"+userCustom.getId());
	
						if(userCustom_value_arr != null && userCustom_value_arr.length >0){
							for(String userCustom_value : userCustom_value_arr){
								
								if(userCustom_value != null && !"".equals(userCustom_value.trim())){
									
									String itemValue = userCustom.getItemValue().get(userCustom_value.trim());
									if(itemValue != null ){
										UserInputValue userInputValue = new UserInputValue();
										userInputValue.setUserCustomId(userCustom.getId());
										userInputValue.setOptions(userCustom_value.trim());
										userInputValueList.add(userInputValue);
									}
									
									
								}
							}
						}else{
							if(userCustom.isRequired() == true){//????????????	
								error.put("userCustom_"+userCustom.getId(), ErrorView._809.name());//?????????
							}
						}
						if(userInputValueList.size() == 0){
							if(userCustom.isRequired() == true){//????????????	
								error.put("userCustom_"+userCustom.getId(), ErrorView._809.name());//?????????
							}
						}
						userCustom.setUserInputValueList(userInputValueList);	
						
					}else if(userCustom.getChooseType().equals(4)){//4.????????????
						String[] userCustom_value_arr = request.getParameterValues("userCustom_"+userCustom.getId());

						if(userCustom_value_arr != null && userCustom_value_arr.length >0){
							for(String userCustom_value : userCustom_value_arr){
								
								if(userCustom_value != null && !"".equals(userCustom_value.trim())){
									
									String itemValue = userCustom.getItemValue().get(userCustom_value.trim());
									if(itemValue != null ){
										UserInputValue userInputValue = new UserInputValue();
										userInputValue.setUserCustomId(userCustom.getId());
										userInputValue.setOptions(userCustom_value.trim());
										userInputValueList.add(userInputValue);
									}
									
									
								}
							}
						}else{
							if(userCustom.isRequired() == true){//????????????	
								error.put("userCustom_"+userCustom.getId(), ErrorView._809.name());//?????????
							}
						}
						if(userInputValueList.size() == 0){
							if(userCustom.isRequired() == true){//????????????	
								error.put("userCustom_"+userCustom.getId(), ErrorView._809.name());//?????????
							}
						}
						userCustom.setUserInputValueList(userInputValueList);	
					}else if(userCustom.getChooseType().equals(5)){// 5.?????????
						String userCustom_value = request.getParameter("userCustom_"+userCustom.getId());

						if(userCustom_value != null && !"".equals(userCustom_value.trim())){
							UserInputValue userInputValue = new UserInputValue();
							userInputValue.setUserCustomId(userCustom.getId());
							userInputValue.setContent(userCustom_value);
							userInputValueList.add(userInputValue);
							
						}else{
							if(userCustom.isRequired() == true){//????????????	
								error.put("userCustom_"+userCustom.getId(), ErrorView._809.name());//?????????
							}
						}
						userCustom.setUserInputValueList(userInputValueList);
					}
				}
			}
		}
		
		//????????????
		if(user.getType() <=30){//????????????????????????????????????
			if(formbean.getPassword() != null && !"".equals(formbean.getPassword().trim())){
				if(formbean.getPassword().trim().length() != 64){//???????????????64???SHA256
					error.put("password", ErrorView._801.name());//??????????????????
				}else{
					new_user.setPassword(SHA.sha256Hex(formbean.getPassword().trim()+"["+user.getSalt()+"]"));
					new_user.setSecurityDigest(new Date().getTime());
					
					
				}
				
				//???????????????
				if(oldPassword != null && !"".equals(oldPassword.trim())){
					if(!user.getPassword().equals(SHA.sha256Hex(oldPassword.trim()+"["+user.getSalt()+"]"))){
						error.put("oldPassword", ErrorView._802.name());//???????????????
					}
				}else{
					error.put("oldPassword", ErrorView._803.name());//?????????????????????
				}
			}else{
				new_user.setPassword(user.getPassword());
				new_user.setSecurityDigest(user.getSecurityDigest());
			}
		}else{
			new_user.setSecurityDigest(user.getSecurityDigest());
		}
		
		
		if(formbean.getNickname() != null && !"".equals(formbean.getNickname().trim())){
			if(user.getNickname() == null || "".equals(user.getNickname().trim())){
				if(formbean.getNickname().length()>15){
					error.put("nickname", ErrorView._829.name());//??????????????????15?????????
				}
				User u = userService.findUserByNickname(formbean.getNickname().trim());
				if(u != null){
					error.put("nickname", ErrorView._830.name());//??????????????????
				}
			}else{
				error.put("nickname", ErrorView._831.name());//?????????????????????
			}
			
			User u1 = userService.findUserByUserName(formbean.getNickname().trim());
			if(u1 != null){
				error.put("nickname", ErrorView._833.name());//????????????????????????????????????
			}
			
			List<DisableUserName> disableUserNameList = userService.findAllDisableUserName_cache();
			if(disableUserNameList != null && disableUserNameList.size() >0){
				for(DisableUserName disableUserName : disableUserNameList){
					boolean flag = matcher.match(disableUserName.getName(), formbean.getNickname().trim());  //?????????: ant????????????   ?????????:??????URL
					if(flag){
						error.put("nickname", ErrorView._832.name());//????????????????????????
					}
				}
			}

			User u = userService.findUserByNickname(formbean.getNickname().trim());
			if(u != null){
				if(user.getNickname() == null || "".equals(user.getNickname()) || !formbean.getNickname().trim().equals(user.getNickname())){
					error.put("nickname",ErrorView._830.name());//??????????????????
				}
				
			}
			new_user.setNickname(formbean.getNickname().trim());
		}

		new_user.setId(user.getId());
		new_user.setUserName(user.getUserName());
		if(new_user.getNickname() == null || "".equals(new_user.getNickname().trim())){
			new_user.setNickname(user.getNickname());
		}
		if(formbean.getAllowUserDynamic() != null){
			new_user.setAllowUserDynamic(formbean.getAllowUserDynamic());//???????????????????????? 
		}
		
		
		new_user.setUserVersion(user.getUserVersion());
		
		
		//??????
		if(error.size() == 0){
			List<UserInputValue> userInputValueList= userCustomService.findUserInputValueByUserName(user.getId());
			
			//??????????????????????????????????????????
			List<UserInputValue> add_userInputValue = new ArrayList<UserInputValue>();
			//????????????????????????????????????Id??????
			List<Long> delete_userInputValueIdList = new ArrayList<Long>();
			if(userCustomList != null && userCustomList.size() >0){	
				for(UserCustom userCustom : userCustomList){
					List<UserInputValue> new_userInputValueList = userCustom.getUserInputValueList();
					if(new_userInputValueList != null && new_userInputValueList.size() >0){
						A:for(UserInputValue new_userInputValue : new_userInputValueList){
							if(userInputValueList != null && userInputValueList.size() >0){
								for(UserInputValue old_userInputValue : userInputValueList){
									if(new_userInputValue.getUserCustomId().equals(old_userInputValue.getUserCustomId())){
										
										
										if(new_userInputValue.getOptions().equals("-1")){
											
											if(new_userInputValue.getContent() == null){
												if(old_userInputValue.getContent() == null){
													userInputValueList.remove(old_userInputValue);
													continue A;
												}
											}else{
												if(new_userInputValue.getContent().equals(old_userInputValue.getContent())){
													userInputValueList.remove(old_userInputValue);
													continue A;
												}
											}
											
										}else{
											if(new_userInputValue.getOptions().equals(old_userInputValue.getOptions())){
												userInputValueList.remove(old_userInputValue);
												continue A;
											}
										}
									}	
								}
							}
							add_userInputValue.add(new_userInputValue);
						}
					}
				}
			}
			if(userInputValueList != null && userInputValueList.size() >0){
				for(UserInputValue old_userInputValue : userInputValueList){
					delete_userInputValueIdList.add(old_userInputValue.getId());
					
				}
			}
			
			
			int i = userService.updateUser2(new_user,add_userInputValue,delete_userInputValueIdList);
			userManage.delete_userState(new_user.getUserName());
			//????????????
			userManage.delete_cache_findUserById(new_user.getId());
			userManage.delete_cache_findUserByUserName(new_user.getUserName());

			if(i == 0){
				error.put("user", ErrorView._810.name());//??????????????????
			}else{
				//???????????????????????????????????????OAuth????????????
				if((formbean.getPassword() != null && !"".equals(formbean.getPassword().trim())) ||
						(formbean.getNickname() != null && !"".equals(formbean.getNickname().trim()))){

					User _user = userService.findUserByUserName(accessUser.getUserName());
					
					String _accessToken = WebUtil.getCookieByName(request, "cms_accessToken");
					if(_accessToken != null && !"".equals(_accessToken.trim())){
						//??????????????????
		    			oAuthManage.deleteAccessToken(_accessToken.trim());
					}
					String _refreshToken = WebUtil.getCookieByName(request, "cms_refreshToken");
					if(_refreshToken != null && !"".equals(_refreshToken.trim())){
						//??????????????????
		    			oAuthManage.deleteRefreshToken(_refreshToken);
					}
					
					//????????????
					String accessToken = UUIDUtil.getUUID32();
					//????????????
					String refreshToken = UUIDUtil.getUUID32();

					//??????cookie???????????????
					int maxAge = WebUtil.getCookieMaxAge(request, "cms_accessToken"); //???????????? ??????/???
					boolean rememberMe = maxAge >0 ? true :false;
					
					
					oAuthManage.addAccessToken(accessToken, new AccessUser(_user.getId(),_user.getUserName(),_user.getAccount(),_user.getNickname(),fileManage.fileServerAddress()+_user.getAvatarPath(),_user.getAvatarName(),_user.getSecurityDigest(),rememberMe,accessUser.getOpenId()));
					oAuthManage.addRefreshToken(refreshToken, new RefreshUser(accessToken,_user.getId(),_user.getUserName(),_user.getAccount(),_user.getNickname(),fileManage.fileServerAddress()+_user.getAvatarPath(),_user.getAvatarName(),_user.getSecurityDigest(),rememberMe,accessUser.getOpenId()));
					
					//?????????openId
					oAuthManage.addOpenId(accessUser.getOpenId(),refreshToken);
					
					//????????????????????????Cookie
					WebUtil.addCookie(response, "cms_accessToken", accessToken, maxAge);
					//????????????????????????Cookie
					WebUtil.addCookie(response, "cms_refreshToken", refreshToken, maxAge);
					AccessUserThreadLocal.set(new AccessUser(_user.getId(),_user.getUserName(),_user.getAccount(),_user.getNickname(),fileManage.fileServerAddress()+_user.getAvatarPath(),_user.getAvatarName(),_user.getSecurityDigest(),rememberMe,accessUser.getOpenId()));
					
					
					
					
					
				}
			}
		}
		
		Map<String,String> returnError = new HashMap<String,String>();//??????
		if(error.size() >0){
			//???????????????????????????????????????
    		for (Map.Entry<String,String> entry : error.entrySet()) {
    			if(ErrorView.get(entry.getValue()) != null){
    				returnError.put(entry.getKey(),  ErrorView.get(entry.getValue()));
    			}else{
    				returnError.put(entry.getKey(),  entry.getValue());
    			}
    			
			}
		}
		
		if(isAjax == true){
			
    		Map<String,Object> returnValue = new HashMap<String,Object>();//?????????
    		
    		if(error != null && error.size() >0){
    			returnValue.put("success", "false");
    			returnValue.put("error", returnError);
    		}else{
    			returnValue.put("success", "true");
    			
    		}
    		WebUtil.writeToWeb(JsonUtils.toJSONString(returnValue), "json", response);
			return null;
		}else{
			if(error != null && error.size() >0){//???????????????
				
				redirectAttrs.addFlashAttribute("error", returnError);//???????????????
				redirectAttrs.addFlashAttribute("userCustomList", userCustomList);
				String referer = request.getHeader("referer");	

				referer = StringUtils.removeStartIgnoreCase(referer,Configuration.getUrl(request));//????????????????????????????????????,??????????????????
				referer = StringUtils.substringBefore(referer, ".");//????????????????????????????????????????????????
				referer = StringUtils.substringBefore(referer, "?");//????????????????????????????????????????????????
				
				String queryString = request.getQueryString() != null && !"".equals(request.getQueryString().trim()) ? "?"+request.getQueryString() :"";
				return "redirect:/"+referer+queryString;
					
			}
			
			
			if(jumpUrl != null && !"".equals(jumpUrl.trim())){
				String url = Base64.decodeBase64URL(jumpUrl.trim());
				return "redirect:"+url;
			}else{//????????????
				model.addAttribute("message", "??????????????????");
				String referer = request.getHeader("referer");
				if(RefererCompare.compare(request, "login")){//???????????????????????????????????????
					referer = Configuration.getUrl(request);
				}
				model.addAttribute("urlAddress", referer);
				String dirName = templateService.findTemplateDir_cache();
				
				String accessPath = accessSourceDeviceManage.accessDevices(request);
				return "templates/"+dirName+"/"+accessPath+"/jump";	
			}
		}
		
		
		
		
	}
	

	
	/**
	 * ????????????
	 * @param model
	 * @param imgFile
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/updateAvatar",method=RequestMethod.POST)
	@ResponseBody//????????????ajax,?????????????????????
	@RoleAnnotation(resourceCode=ResourceEnum._2001000)
	public String updateAvatar(ModelMap model,MultipartFile imgFile,
			HttpServletRequest request,HttpServletResponse response)
			throws Exception {	
				
				
		Map<String,String> error = new HashMap<String,String>();//??????
		SystemSetting systemSetting = settingService.findSystemSetting_cache();
		if(systemSetting.getCloseSite().equals(2)){
			error.put("imgFile", ErrorView._21.name());//?????????????????????????????????
		}
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
		
		User user = userManage.query_cache_findUserByUserName(accessUser.getUserName());
		
		String _width = request.getParameter("width");
		String _height = request.getParameter("height");
		String _x = request.getParameter("x");
		String _y = request.getParameter("y");
		
		
		Integer width = null;//???
		Integer height = null;//???
		Integer x = 0;//??????X???
		Integer y = 0;//??????Y???
		
		Integer maxWidth = 200;//????????????
		Integer maxHeight = 200;//????????????
		
		
		if(_width != null && !"".equals(_width.trim())){
			if(Verification.isPositiveInteger(_width.trim())){
				if(_width.trim().length() >=8){
					error.put("width", ErrorView._1200.name());//????????????8?????????
				}else{
					width = Integer.parseInt(_width.trim());
				}
				
				
			}else{
				error.put("width", ErrorView._1210.name());//??????????????????0
			}
			
		}
		if(_height != null && !"".equals(_height.trim())){
			if(Verification.isPositiveInteger(_height.trim())){
				if(_height.trim().length() >=8){
					error.put("height", ErrorView._1200.name());//????????????8?????????
				}else{
					height = Integer.parseInt(_height.trim());
				}
				
			}else{
				error.put("height", ErrorView._1230.name());//??????????????????0 
			}
		}
		
		if(_x != null && !"".equals(_x.trim())){
			if(Verification.isPositiveIntegerZero(_x.trim())){
				if(_x.trim().length() >=8){
					error.put("x", ErrorView._1200.name());//????????????8?????????
				}else{
					x = Integer.parseInt(_x.trim());
				}
				
			}else{
				error.put("x", ErrorView._1250.name());//X????????????????????????0
			}
			
		}
		
		if(_y != null && !"".equals(_y.trim())){
			if(Verification.isPositiveIntegerZero(_y.trim())){
				if(_y.trim().length() >=8){
					error.put("y", ErrorView._1200.name());//????????????8?????????
				}else{
					y = Integer.parseInt(_y.trim());
				}
				
			}else{
				error.put("y", ErrorView._1270.name());//Y????????????????????????0
			}
			
		}
		
		
		//??????????????????
		String newFileName = "";
		DateTime dateTime = new DateTime(user.getRegistrationDate());     
		String date = dateTime.toString("yyyy-MM-dd");
		
		if(error.size()==0 && imgFile != null && !imgFile.isEmpty()){
			//??????????????????
			String fileName = imgFile.getOriginalFilename();
			
			//????????????
			Long size = imgFile.getSize();
			

			
			//????????????????????????
			List<String> formatList = new ArrayList<String>();
			formatList.add("gif");
			formatList.add("jpg");
			formatList.add("jpeg");
			formatList.add("bmp");
			formatList.add("png");
			//???????????????????????? ??????KB
			long imageSize = 3*1024L;
			
			if(size/1024 <= imageSize){
				
				//??????????????????;?????????????????????????????????????????????,??????????????????
				String pathDir = "file"+File.separator+"avatar"+File.separator + date +File.separator ;
				//????????????????????????
				fileManage.createFolder(pathDir);
				//100*100??????
				String pathDir_100 = "file"+File.separator+"avatar"+File.separator + date +File.separator +"100x100" +File.separator;
				//????????????????????????
				fileManage.createFolder(pathDir_100);
				
				
				if("blob".equalsIgnoreCase(imgFile.getOriginalFilename())){//Blob??????
					
					newFileName = UUIDUtil.getUUID32()+ ".png";
					
					BufferedImage bufferImage = ImageIO.read(imgFile.getInputStream());  
		            //????????????????????????  
		            int srcWidth = bufferImage.getWidth();  
		            int srcHeight = bufferImage.getHeight();  
					if(srcWidth > maxWidth){
						error.put("imgFile",ErrorView._1290.name());//??????????????????
					}
					if(srcHeight > maxHeight){
						error.put("imgFile",ErrorView._1300.name());//??????????????????
					}
					if(error.size() == 0){
						if(user.getAvatarName() != null && !"".equals(user.getAvatarName().trim())){
							String oldPathFile = pathDir +user.getAvatarName();
							//???????????????
							fileManage.deleteFile(oldPathFile);
							String oldPathFile_100 = pathDir_100+user.getAvatarName();
							//???????????????100*100
							fileManage.deleteFile(oldPathFile_100);
						}
						
						//????????????
						fileManage.writeFile(pathDir, newFileName,imgFile.getBytes());

						//??????100*100?????????
						fileManage.createImage(imgFile.getInputStream(),pathDir_100+newFileName,"png",100,100);
						
					}
				}else{//????????????
					//??????????????????
					boolean authentication = FileUtil.validateFileSuffix(imgFile.getOriginalFilename(),formatList);
			
					if(authentication){
						if(user.getAvatarName() != null && !"".equals(user.getAvatarName().trim())){
							String oldPathFile = pathDir +user.getAvatarName();
							//???????????????
							fileManage.deleteFile(oldPathFile);
							String oldPathFile_100 = pathDir_100+user.getAvatarName();
							//???????????????100*100
							fileManage.deleteFile(oldPathFile_100);
						}
						
						
						BufferedImage bufferImage = ImageIO.read(imgFile.getInputStream());  
			            //????????????????????????  
			            int srcWidth = bufferImage.getWidth();  
			            int srcHeight = bufferImage.getHeight();  
						
						//??????????????????
						String suffix = FileUtil.getExtension(fileName).toLowerCase();
						
						//??????????????????
						newFileName = UUIDUtil.getUUID32()+ "." + suffix;
						
						if(srcWidth <=200 && srcHeight <=200){	
							//????????????
							fileManage.writeFile(pathDir, newFileName,imgFile.getBytes());
							
							if(srcWidth <=100 && srcHeight <=100){
								//????????????
								fileManage.writeFile(pathDir_100, newFileName,imgFile.getBytes());
							}else{
								//??????100*100?????????
								fileManage.createImage(imgFile.getInputStream(),pathDir_100+newFileName,suffix,100,100);
								
							}
						}else{
							//??????200*200?????????
							fileManage.createImage(imgFile.getInputStream(),pathDir+newFileName,suffix,x,y,width,height,200,200);

							//??????100*100?????????
							fileManage.createImage(imgFile.getInputStream(),pathDir_100+newFileName,suffix,x,y,width,height,100,100);   
						}		
						
					}else{
						error.put("imgFile",ErrorView._1310.name());//?????????????????????????????????
					}
				}	
			}else{
				error.put("imgFile",ErrorView._1320.name());//??????????????????????????????
			}	
		}else{
			error.put("imgFile",ErrorView._1330.name());//??????????????????
		}
		

		if(error.size() ==0){
			userService.updateUserAvatar(accessUser.getUserName(), newFileName);
			//????????????
			userManage.delete_cache_findUserById(user.getId());
			userManage.delete_cache_findUserByUserName(user.getUserName());
			
			
			String accessToken = WebUtil.getCookieByName(request, "cms_accessToken");
			if(accessToken != null && !"".equals(accessToken.trim())){
				//??????????????????
    			oAuthManage.deleteAccessToken(accessToken.trim());
			}
		}
		
		
		
		
		
		Map<String,String> returnError = new HashMap<String,String>();//??????
		if(error.size() >0){
			//???????????????????????????????????????
    		for (Map.Entry<String,String> entry : error.entrySet()) {
    			if(ErrorView.get(entry.getValue()) != null){
    				returnError.put(entry.getKey(),  ErrorView.get(entry.getValue()));
    			}else{
    				returnError.put(entry.getKey(),  entry.getValue());
    			}
    			
			}
		}
		
		Map<String,Object> returnValue = new HashMap<String,Object>();//?????????

		if(error != null && error.size() >0){
			returnValue.put("success", "false");
			returnValue.put("error", returnError);
		}else{
			returnValue.put("success", "true");
		}
		return JsonUtils.toJSONString(returnValue);
	}
	
	
	
	/**-------------------------------------------- ???????????? ----------------------------------------------------**/
	/**
	 * ????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/realNameAuthentication",method=RequestMethod.GET) 
	public String realNameAuthenticationUI(ModelMap model,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		Map<String,Object> returnValue = new HashMap<String,Object>();//?????????
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
		
		User user = userService.findUserByUserName(accessUser.getUserName());
	    if(user != null){
	    	//????????????????????????
			User viewUser = new User();
			viewUser.setId(user.getId());
			viewUser.setUserName(user.getUserName());//???????????????
			viewUser.setNickname(user.getNickname());//??????
			viewUser.setMobile(user.getMobile());
			viewUser.setRealNameAuthentication(user.isRealNameAuthentication());
	    	
	    	model.addAttribute("user",viewUser);
		    returnValue.put("user",viewUser);
	    } 
		

		if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(returnValue), "json", response);
			return null;
		}else{
			String dirName = templateService.findTemplateDir_cache();
			String accessPath = accessSourceDeviceManage.accessDevices(request);
			return "templates/"+dirName+"/"+accessPath+"/realNameAuthentication";	
		}
	}
	

	/**
	 * ????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/phoneBinding",method=RequestMethod.GET) 
	public String phoneBindingUI(ModelMap model,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		Map<String,Object> returnValue = new HashMap<String,Object>();//?????????
	    String captchaKey = UUIDUtil.getUUID32();
	    model.addAttribute("captchaKey",captchaKey);
	    returnValue.put("captchaKey",captchaKey);

	    if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(returnValue), "json", response);
			return null;
	    }else{	
			String dirName = templateService.findTemplateDir_cache();   
		    return "templates/"+dirName+"/"+accessSourceDeviceManage.accessDevices(request)+"/phoneBinding";	
		}
	}
	
	/**
	 * ????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/phoneBinding",method=RequestMethod.POST) 
	public String phoneBinding(ModelMap model,String mobile,String smsCode,
			String captchaKey,String captchaValue,String jumpUrl,
			String token,RedirectAttributes redirectAttrs,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
	
	    Map<String,String> error = new HashMap<String,String>();
	    SystemSetting systemSetting = settingService.findSystemSetting_cache();
		if(systemSetting.getCloseSite().equals(2)){
			error.put("smsCode", ErrorView._21.name());//?????????????????????????????????
		}
	    //??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	   
	    //????????????
	    if(token != null && !"".equals(token.trim())){	
  			String token_sessionid = csrfTokenManage.getToken(request);//????????????
  			if(token_sessionid != null && !"".equals(token_sessionid.trim())){
  				if(!token_sessionid.equals(token)){
  					error.put("token", ErrorView._13.name());
  				}
  			}else{
  				error.put("token", ErrorView._12.name());
  			}
  		}else{
  			error.put("token", ErrorView._11.name());
  		}
  		
	    User new_user = userService.findUserByUserName(accessUser.getUserName());;
	    
	    if(mobile != null && !"".equals(mobile.trim())){
	    	if(mobile.trim().length() >18){
				error.put("mobile", ErrorView._854.name());//??????????????????
			}else{
				boolean mobile_verification = Verification.isPositiveInteger(mobile.trim());//?????????
				if(!mobile_verification){
					error.put("mobile", ErrorView._853.name());//?????????????????????
				}else{
					
		      		if(new_user != null){
		      			if(new_user.getMobile() != null && !"".equals(new_user.getMobile())){
		      				error.put("mobile", ErrorView._857.name());//??????????????????????????????
		      			}
		      			
		      		}else{
		      			error.put("mobile", ErrorView._859.name());//???????????????
		      		}
				}
			}
	    }else{
	    	error.put("mobile", ErrorView._851.name());//?????????????????????
	    }
	    
	    
	    if(smsCode != null && !"".equals(smsCode.trim())){
	    	if(smsCode.trim().length() >6){
				error.put("smsCode", ErrorView._855.name());//?????????????????????
			}else{
			    if(error.size() ==0){
			    	
			    	//?????????????????????????????????
		    		String numeric = smsManage.smsCode_generate(1,new_user.getPlatformUserId(), mobile.trim(),null);
		    		if(numeric != null){
		    			if(!numeric.equals(smsCode)){
		    				error.put("smsCode", ErrorView._850.name());//?????????????????????
		    			}
		    			
		    		}else{
		    			error.put("smsCode", ErrorView._856.name());//????????????????????????????????????
		    		}
			    }
			}
	    }else{
	    	error.put("smsCode", ErrorView._852.name());//???????????????????????????
	    }
	    
	    if(mobile != null && !"".equals(mobile.trim())){
	    	 //?????????????????????????????????
		    smsManage.smsCode_delete(1,new_user.getPlatformUserId(), mobile.trim());
		   
	    }
	    
	    if(error.size() ==0){
	    	userService.updateUserMobile(new_user.getUserName(),mobile.trim(),true);
	    	//????????????
			userManage.delete_cache_findUserById(new_user.getId());
			userManage.delete_cache_findUserByUserName(new_user.getUserName());
	    }
	    
		Map<String,String> returnError = new HashMap<String,String>();//??????
		if(error.size() >0){
			//???????????????????????????????????????
    		for (Map.Entry<String,String> entry : error.entrySet()) {
    			if(ErrorView.get(entry.getValue()) != null){
    				returnError.put(entry.getKey(),  ErrorView.get(entry.getValue()));
    			}else{
    				returnError.put(entry.getKey(),  entry.getValue());
    			}
    			
			}
		}
	    
	    if(isAjax == true){
			
    		Map<String,Object> returnValue = new HashMap<String,Object>();//?????????
    		
    		if(error != null && error.size() >0){
    			returnValue.put("success", "false");
    			returnValue.put("error", returnError);
    			returnValue.put("captchaKey", UUIDUtil.getUUID32());
    		}else{
    			returnValue.put("success", "true");
    		}
    		
    		WebUtil.writeToWeb(JsonUtils.toJSONString(returnValue), "json", response);
			return null;
		}else{
			String dirName = templateService.findTemplateDir_cache();
			
			String accessPath = accessSourceDeviceManage.accessDevices(request);
			if(error != null && error.size() >0){//???????????????
				for (Map.Entry<String,String> entry : returnError.entrySet()) {		 
					model.addAttribute("message",entry.getValue());//??????
		  			return "templates/"+dirName+"/"+accessPath+"/message";
				}
			}
			
			
			if(jumpUrl != null && !"".equals(jumpUrl.trim())){
				String url = Base64.decodeBase64URL(jumpUrl.trim());
				return "redirect:"+url;
			}else{//????????????
				model.addAttribute("message", "??????????????????");
				String referer = request.getHeader("referer");
				if(RefererCompare.compare(request, "login")){//???????????????????????????????????????
					referer = Configuration.getUrl(request);
				}
				model.addAttribute("urlAddress", referer);
				
				return "templates/"+dirName+"/"+accessPath+"/jump";	
			}
		}
	}
	
	/**
	 * ?????????????????????
	 * @param model
	 * @param mobile ??????
	 * @param module ??????  1.????????????  2.???????????????????????????  3.???????????????????????????
	 * @param captchaKey
	 * @param captchaValue
	 * @param token
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/getSmsCode",method=RequestMethod.POST) 
	@ResponseBody//????????????ajax,?????????????????????
	public String SMS_verificationCode(ModelMap model,String mobile,Integer module,
			String captchaKey,String captchaValue,String token,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		Map<String,String> error = new HashMap<String,String>();
		Map<String,Object> returnValue = new HashMap<String,Object>();//?????????
	    
	    //????????????
	    if(token != null && !"".equals(token.trim())){	
  			String token_sessionid = csrfTokenManage.getToken(request);//????????????
  			if(token_sessionid != null && !"".equals(token_sessionid.trim())){
  				if(!token_sessionid.equals(token)){
  					error.put("token", ErrorView._13.name());
  				}
  			}else{
  				error.put("token", ErrorView._12.name());
  			}
  		}else{
  			error.put("token", ErrorView._11.name());
  		}
	    
	    if(module == null || module <1 || module >3){
	    	error.put("message", "????????????");
	    }
	    SystemSetting systemSetting = settingService.findSystemSetting_cache();
		if(systemSetting.getCloseSite().equals(2)){
			error.put("message", ErrorView._21.name());//?????????????????????????????????
		}
	    
	    //???????????????
  		if(captchaKey != null && !"".equals(captchaKey.trim())){
  			//???????????????????????????
  		//?????????????????????????????????
			Integer original = settingManage.getSubmitQuantity("captcha", captchaKey.trim());
    		if(original != null){
    			settingManage.addSubmitQuantity("captcha", captchaKey.trim(),original+1);//?????????????????????????????????
    		}else{
    			settingManage.addSubmitQuantity("captcha", captchaKey.trim(),1);//?????????????????????????????????
    		}
  			
  			String _captcha = captchaManage.captcha_generate(captchaKey.trim(),"");
  			if(captchaValue != null && !"".equals(captchaValue.trim())){
  				if(_captcha != null && !"".equals(_captcha.trim())){
  					if(!_captcha.equalsIgnoreCase(captchaValue)){
  						error.put("captchaValue",ErrorView._15.name());//???????????????
  					}
  				}else{
  					error.put("captchaValue",ErrorView._17.name());//???????????????
  				}
  			}else{
  				error.put("captchaValue",ErrorView._16.name());//??????????????????
  			}
  			//???????????????
  			captchaManage.captcha_delete(captchaKey.trim());
  		}else{
  			error.put("captchaValue", ErrorView._14.name());//?????????????????????
  		}
  		
  		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	  	User new_user = userManage.query_cache_findUserByUserName(accessUser.getUserName());
	  	
		if(module.equals(1) || module.equals(3)){//1.????????????  3.???????????????????????????
  			if(mobile != null && !"".equals(mobile.trim())){
  				if(mobile.trim().length() >18){
  					error.put("mobile", "??????????????????");
  				}else{
  					boolean mobile_verification = Verification.isPositiveInteger(mobile.trim());//?????????
  					if(!mobile_verification){
  						error.put("mobile", "?????????????????????");
  					}
  				}
  			}else{
  				error.put("mobile", ErrorView._851.name());//?????????????????????
  			}
  		}else{//2.???????????????????????????
  	  		if(new_user == null){
  	  			error.put("message", "???????????????");
  	  		}else{
  	  			if(new_user.getMobile() != null && !"".equals(new_user.getMobile())){
  	  				mobile = new_user.getMobile();
  	  			}else{
  	  				error.put("message", "????????????????????????");
  	  			}
  	  		}
  		}
	    if(error.size() == 0){
	    	String randomNumeric = RandomStringUtils.randomNumeric(6);
	    	String errorInfo = smsManage.sendSms_code(new_user.getPlatformUserId(),mobile,randomNumeric);//6????????????
	    	if(errorInfo != null){
	    		error.put("smsCode", errorInfo);
	    	}else{
	    		//?????????????????????????????????
	    	    smsManage.smsCode_delete(module,new_user.getPlatformUserId(), mobile.trim());
	    		//?????????????????????????????????
	    		smsManage.smsCode_generate(module,new_user.getPlatformUserId(), mobile.trim(),randomNumeric);
	    		
	    	}
	    }
	   
	    
	    
	    Map<String,String> returnError = new HashMap<String,String>();//??????
		if(error.size() >0){
			//???????????????????????????????????????
    		for (Map.Entry<String,String> entry : error.entrySet()) {
    			if(ErrorView.get(entry.getValue()) != null){
    				returnError.put(entry.getKey(),  ErrorView.get(entry.getValue()));
    			}else{
    				returnError.put(entry.getKey(),  entry.getValue());
    			}
    			
			}
		}
		if(error != null && error.size() >0){
			returnValue.put("success", "false");
			returnValue.put("error", returnError);
			returnValue.put("captchaKey", UUIDUtil.getUUID32());
		}else{
			returnValue.put("success", "true");
		}
		
		return JsonUtils.toJSONString(returnValue);
	}
	
	
	
	
	/**
	 * ?????????????????? ??????????????????????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/updatePhoneBinding/step1",method=RequestMethod.GET) 
	public String updatePhoneBinding_1UI(ModelMap model,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		Map<String,Object> returnValue = new HashMap<String,Object>();//?????????
	    String captchaKey = UUIDUtil.getUUID32();
	    model.addAttribute("captchaKey",captchaKey);
	    returnValue.put("captchaKey",captchaKey);
	    
	    //??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	    
	    User user = userService.findUserByUserName(accessUser.getUserName());
	    if(user != null){
	    	model.addAttribute("mobile",user.getMobile());
		    returnValue.put("mobile",user.getMobile());
	    } 
   
	    if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(returnValue), "json", response);
			return null;
	    }else{	
			String dirName = templateService.findTemplateDir_cache();   
		    return "templates/"+dirName+"/"+accessSourceDeviceManage.accessDevices(request)+"/updatePhoneBinding_step1";	
		}
	}
	
	/**
	 * ?????????????????? ????????????????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/updatePhoneBinding/step1",method=RequestMethod.POST) 
	public String updatePhoneBinding_1(ModelMap model,String smsCode,
			String captchaKey,String captchaValue,String jumpUrl,
			String token,RedirectAttributes redirectAttrs,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
	    
	    Map<String,String> error = new HashMap<String,String>();
	    SystemSetting systemSetting = settingService.findSystemSetting_cache();
		if(systemSetting.getCloseSite().equals(2)){
			error.put("smsCode", ErrorView._21.name());//?????????????????????????????????
		}
	    //??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	   
	    //????????????
	    if(token != null && !"".equals(token.trim())){	
  			String token_sessionid = csrfTokenManage.getToken(request);//????????????
  			if(token_sessionid != null && !"".equals(token_sessionid.trim())){
  				if(!token_sessionid.equals(token)){
  					error.put("token", ErrorView._13.name());
  				}
  			}else{
  				error.put("token", ErrorView._12.name());
  			}
  		}else{
  			error.put("token", ErrorView._11.name());
  		}
  		
	    User new_user = userManage.query_cache_findUserByUserName(accessUser.getUserName());
  		if(new_user != null){
  			if(new_user.getMobile() == null || "".equals(new_user.getMobile())){
  				error.put("smsCode", ErrorView._858.name());//????????????????????????
  			}
  			
  			
  			if(error.size() ==0 && smsCode != null && !"".equals(smsCode.trim())){
  		    	if(smsCode.trim().length() >6){
  					error.put("smsCode", ErrorView._855.name());//?????????????????????
  				}else{
  				    if(error.size() ==0){
  				    	
  				    	//?????????????????????????????????
  			    		String numeric = smsManage.smsCode_generate(2,new_user.getPlatformUserId(), new_user.getMobile(),null);
  			    		if(numeric != null){
  			    			if(!numeric.equals(smsCode)){
  			    				error.put("smsCode", ErrorView._850.name());//?????????????????????
  			    			}
  			    			
  			    		}else{
  			    			error.put("smsCode", ErrorView._856.name());//????????????????????????????????????
  			    		}
  				    }
  				}
  		    }else{
  		    	error.put("smsCode", ErrorView._852.name());//???????????????????????????
  		    }
  	  			
  			 //?????????????????????????????????
  		    smsManage.smsCode_delete(2,new_user.getPlatformUserId(), new_user.getMobile());	
  		}else{
  			error.put("smsCode", ErrorView._859.name());//???????????????
  		}

  		if(error.size() ==0){
	    	smsManage.replaceCode_delete(new_user.getId(), new_user.getMobile());
	    	smsManage.replaceCode_generate(new_user.getId(), new_user.getMobile(),true);//?????????????????????????????????
	    }
  		
		Map<String,String> returnError = new HashMap<String,String>();//??????
		if(error.size() >0){
			//???????????????????????????????????????
    		for (Map.Entry<String,String> entry : error.entrySet()) {
    			if(ErrorView.get(entry.getValue()) != null){
    				returnError.put(entry.getKey(),  ErrorView.get(entry.getValue()));
    			}else{
    				returnError.put(entry.getKey(),  entry.getValue());
    			}
    			
			}
		}

		if(isAjax == true){
			
    		Map<String,Object> returnValue = new HashMap<String,Object>();//?????????
    		
    		if(error != null && error.size() >0){
    			returnValue.put("success", "false");
    			returnValue.put("error", returnError);
    			returnValue.put("captchaKey", UUIDUtil.getUUID32());
    		}else{
    			returnValue.put("success", "true");
    			returnValue.put("jumpUrl", "user/control/updatePhoneBinding/step2");
    		}
    		
    		WebUtil.writeToWeb(JsonUtils.toJSONString(returnValue), "json", response);
			return null;
		}else{
			
			if(error != null && error.size() >0){//???????????????
				
				redirectAttrs.addFlashAttribute("error", returnError);//???????????????
				
				String referer = request.getHeader("referer");	

				referer = StringUtils.removeStartIgnoreCase(referer,Configuration.getUrl(request));//????????????????????????????????????,??????????????????
				referer = StringUtils.substringBefore(referer, ".");//????????????????????????????????????????????????
				referer = StringUtils.substringBefore(referer, "?");//????????????????????????????????????????????????
				
				String queryString = request.getQueryString() != null && !"".equals(request.getQueryString().trim()) ? "?"+request.getQueryString() :"";
				return "redirect:/"+referer+queryString;
					
			}
			
			return "redirect:/user/control/updatePhoneBinding/step2";

		}
		
	}
	
	/**
	 * ?????????????????? ??????????????????????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/updatePhoneBinding/step2",method=RequestMethod.GET) 
	public String updatePhoneBinding_2UI(ModelMap model,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		Map<String,Object> returnValue = new HashMap<String,Object>();//?????????
	    String captchaKey = UUIDUtil.getUUID32();
	    model.addAttribute("captchaKey",captchaKey);
	    returnValue.put("captchaKey",captchaKey);

	    if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(returnValue), "json", response);
			return null;
	    }else{	
			String dirName = templateService.findTemplateDir_cache();   
		    return "templates/"+dirName+"/"+accessSourceDeviceManage.accessDevices(request)+"/updatePhoneBinding_step2";	
		}
	}
	
	/**
	 * ?????????????????? ????????????????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/updatePhoneBinding/step2",method=RequestMethod.POST) 
	public String updatePhoneBinding_2UI(ModelMap model,String mobile,String smsCode,
			String captchaKey,String captchaValue,String jumpUrl,
			String token,RedirectAttributes redirectAttrs,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
	   
		
		
		
	    Map<String,String> error = new HashMap<String,String>();
	    SystemSetting systemSetting = settingService.findSystemSetting_cache();
		if(systemSetting.getCloseSite().equals(2)){
			error.put("smsCode", ErrorView._21.name());//?????????????????????????????????
		}
	    //??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	   
	    //????????????
	    if(token != null && !"".equals(token.trim())){	
  			String token_sessionid = csrfTokenManage.getToken(request);//????????????
  			if(token_sessionid != null && !"".equals(token_sessionid.trim())){
  				if(!token_sessionid.equals(token)){
  					error.put("token", ErrorView._13.name());
  				}
  			}else{
  				error.put("token", ErrorView._12.name());
  			}
  		}else{
  			error.put("token", ErrorView._11.name());
  		}
  		
	    User new_user = userService.findUserByUserName(accessUser.getUserName());
  		
	    
	    if(mobile != null && !"".equals(mobile.trim())){
	    	if(mobile.trim().length() >18){
				error.put("mobile", ErrorView._854.name());//??????????????????
			}else{
				boolean mobile_verification = Verification.isPositiveInteger(mobile.trim());//?????????
				if(!mobile_verification){
					error.put("mobile", ErrorView._853.name());//?????????????????????
				}else{
					
					if(new_user != null){
		      			if(new_user.getMobile() != null && !"".equals(new_user.getMobile())){
		      				if(new_user.getMobile().equals(mobile.trim())){
		      					error.put("mobile", ErrorView._860.name());//?????????????????????????????????????????????
		      				}
		      			}
		      		}
				}
				
				if(new_user.getType().equals(20)){//???????????? 20: ????????????
		    		String platformUserId = userManage.thirdPartyUserIdToPlatformUserId(mobile.trim(),20);
		 			User mobile_user = userService.findUserByPlatformUserId(platformUserId);
		 			
		 	  		if(mobile_user != null){
		 	  			error.put("mobile", ErrorView._864.name());//?????????????????????
		 	
		 	  		}
		    	}
			}
	    }else{
	    	error.put("mobile", ErrorView._851.name());//?????????????????????
	    }
	    
	    
	    if(smsCode != null && !"".equals(smsCode.trim())){
	    	if(smsCode.trim().length() >6){
				error.put("smsCode", ErrorView._855.name());//?????????????????????
			}else{
			    if(error.size() ==0){
			    	
			    	//?????????????????????????????????
		    		String numeric = smsManage.smsCode_generate(3,new_user.getPlatformUserId(), mobile.trim(),null);
		    		if(numeric != null){
		    			if(!numeric.equals(smsCode)){
		    				error.put("smsCode", ErrorView._850.name());//?????????????????????
		    			}
		    			
		    		}else{
		    			error.put("smsCode", ErrorView._856.name());//????????????????????????????????????
		    		}
			    }
			}
	    }else{
	    	error.put("smsCode", ErrorView._852.name());//???????????????????????????
	    }
	    
	    //?????????????????????????????????
	    smsManage.smsCode_delete(3,new_user.getPlatformUserId(), mobile.trim());
	    
	    if(error.size() ==0){
	    	if(new_user != null){
	    		boolean allow = smsManage.replaceCode_generate(new_user.getId(), new_user.getMobile(),false);//?????????????????????????????????
		    	if(allow == false){
		    		error.put("smsCode", ErrorView._861.name());//???????????????????????????
		    	}
		    	//??????
		    	smsManage.replaceCode_delete(new_user.getId(), new_user.getMobile());
	    	}
	    	
	    }
	    
	    if(error.size() ==0){
	    	if(new_user.getType().equals(20)){//???????????? 20: ????????????
	    		String platformUserId = userManage.thirdPartyUserIdToPlatformUserId(mobile.trim(),20);
	    		userService.updateUserMobile(new_user.getUserName(),mobile.trim(),true,platformUserId);
	    		
	    	}else{
	    		userService.updateUserMobile(new_user.getUserName(),mobile.trim(),true);
	    	}
	    	
	    	
	    	//????????????
			userManage.delete_cache_findUserById(new_user.getId());
			userManage.delete_cache_findUserByUserName(new_user.getUserName());
	    }
	    
		Map<String,String> returnError = new HashMap<String,String>();//??????
		if(error.size() >0){
			//???????????????????????????????????????
    		for (Map.Entry<String,String> entry : error.entrySet()) {
    			if(ErrorView.get(entry.getValue()) != null){
    				returnError.put(entry.getKey(),  ErrorView.get(entry.getValue()));
    			}else{
    				returnError.put(entry.getKey(),  entry.getValue());
    			}
    			
			}
		}
	    
	    if(isAjax == true){
			
    		Map<String,Object> returnValue = new HashMap<String,Object>();//?????????
    		
    		if(error != null && error.size() >0){
    			returnValue.put("success", "false");
    			returnValue.put("error", returnError);
    			returnValue.put("captchaKey", UUIDUtil.getUUID32());
    		}else{
    			returnValue.put("success", "true");
    		}
    		
    		WebUtil.writeToWeb(JsonUtils.toJSONString(returnValue), "json", response);
			return null;
		}else{
			String dirName = templateService.findTemplateDir_cache();
			
			String accessPath = accessSourceDeviceManage.accessDevices(request);
			if(error != null && error.size() >0){//???????????????
				for (Map.Entry<String,String> entry : returnError.entrySet()) {		 
					model.addAttribute("message",entry.getValue());//??????
		  			return "templates/"+dirName+"/"+accessPath+"/message";
				}
			}
			
			
			if(jumpUrl != null && !"".equals(jumpUrl.trim())){
				String url = Base64.decodeBase64URL(jumpUrl.trim());
				return "redirect:"+url;
			}else{//????????????
				model.addAttribute("message", "????????????????????????");
				String referer = request.getHeader("referer");
				if(RefererCompare.compare(request, "login")){//???????????????????????????????????????
					referer = Configuration.getUrl(request);
				}
				model.addAttribute("urlAddress", referer);
				
				return "templates/"+dirName+"/"+accessPath+"/jump";	
			}
		}
		
	}
	
	
	
	/**----------------------------------- ???????????? ----------------------------------**/


	/**
	 * ????????????????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/userLoginLogList",method=RequestMethod.GET) 
	public String accountLogList(ModelMap model,PageForm pageForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		
		String dirName = templateService.findTemplateDir_cache();
		
		
		String accessPath = accessSourceDeviceManage.accessDevices(request);
	   
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
		
		//????????????????????????
		PageView<UserLoginLog> pageView = new PageView<UserLoginLog>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		//?????????
		int firstIndex = (pageForm.getPage()-1)*pageView.getMaxresult();

    	
		QueryResult<UserLoginLog> qr = userService.findUserLoginLogPage(accessUser.getUserId(),firstIndex,pageView.getMaxresult());
		if(qr != null && qr.getResultlist() != null && qr.getResultlist().size() >0){
			for(UserLoginLog userLoginLog : qr.getResultlist()){
				userLoginLog.setIpAddress(IpAddress.queryAddress(userLoginLog.getIp()));
			}
		}	
		
		//??????????????????????????????List
		pageView.setQueryResult(qr);

		if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(pageView), "json", response);
			return null;
		}else{
			model.addAttribute("pageView", pageView);
			return "templates/"+dirName+"/"+accessPath+"/userLoginLogList";	
		}
	}
	
	
	
	/**----------------------------------- ?????? ----------------------------------**/
	
	
	/**
	 * ????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/privateMessageList",method=RequestMethod.GET) 
	public String privateMessageList(ModelMap model,PageForm pageForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		
		String dirName = templateService.findTemplateDir_cache();
		
		
		String accessPath = accessSourceDeviceManage.accessDevices(request);
	   
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
		
		//????????????????????????
		PageView<PrivateMessage> pageView = new PageView<PrivateMessage>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		//?????????
		int firstIndex = (pageForm.getPage()-1)*pageView.getMaxresult();

		//??????Id??????
		Set<Long> userIdList = new HashSet<Long>();
		//????????????
		Map<Long,User> userMap = new HashMap<Long,User>();
		
		QueryResult<PrivateMessage> qr = privateMessageService.findPrivateMessageByUserId(accessUser.getUserId(),100,firstIndex,pageView.getMaxresult());
		if(qr != null && qr.getResultlist() != null && qr.getResultlist().size() >0){
			for(PrivateMessage privateMessage : qr.getResultlist()){
				userIdList.add(privateMessage.getSenderUserId());//???????????????Id 
				userIdList.add(privateMessage.getReceiverUserId());//???????????????Id

				privateMessage.setSendTime(new Timestamp(privateMessage.getSendTimeFormat()));
				if(privateMessage.getReadTimeFormat() != null){
					privateMessage.setReadTime(new Timestamp(privateMessage.getReadTimeFormat()));
				}
				privateMessage.setMessageContent(textFilterManage.filterText(privateMessage.getMessageContent()));
				
				
				
				
				
			}
		}
		
		if(userIdList != null && userIdList.size() >0){
			for(Long userId : userIdList){
				User user = userManage.query_cache_findUserById(userId);
				if(user != null){
					userMap.put(userId, user);
				}
			}
		}
		if(userMap != null && userMap.size() >0){
			if(qr != null && qr.getResultlist() != null && qr.getResultlist().size() >0){
				for(PrivateMessage privateMessage : qr.getResultlist()){
					User friend_user = userMap.get(privateMessage.getFriendUserId());
					if(friend_user != null){
						privateMessage.setFriendUserName(friend_user.getUserName());//????????????????????????
						if(friend_user.getCancelAccountTime().equals(-1L)){
							privateMessage.setFriendAccount(friend_user.getAccount());
							privateMessage.setFriendNickname(friend_user.getNickname());
							if(friend_user.getAvatarName() != null && !"".equals(friend_user.getAvatarName().trim())){
								privateMessage.setFriendAvatarPath(fileManage.fileServerAddress()+friend_user.getAvatarPath());//????????????????????????
								privateMessage.setFriendAvatarName(friend_user.getAvatarName());//????????????????????????
							}	
						}			
					}
					User sender_user = userMap.get(privateMessage.getSenderUserId());
					if(sender_user != null){
						privateMessage.setSenderUserName(sender_user.getUserName());//???????????????????????????
						privateMessage.setSenderAccount(sender_user.getAccount());
						privateMessage.setSenderNickname(sender_user.getNickname());
						if(sender_user.getAvatarName() != null && !"".equals(sender_user.getAvatarName().trim())){
							privateMessage.setSenderAvatarPath(fileManage.fileServerAddress()+sender_user.getAvatarPath());//?????????????????????
							privateMessage.setSenderAvatarName(sender_user.getAvatarName());//?????????????????????
						}
					}
					
				}
			}
			
			
			
		}
		
		
		
		

		//??????????????????????????????List
		pageView.setQueryResult(qr);
		if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(pageView), "json", response);
			return null;
		}else{
			model.addAttribute("pageView", pageView);
			return "templates/"+dirName+"/"+accessPath+"/privateMessageList";	
		}
	}
	
	
	/**
	 * ??????????????????
	 * @param model
	 * @param page ??????
	 * @param friendUserName ??????????????????
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/privateMessageChatList",method=RequestMethod.GET) 
	public String privateMessageChatList(ModelMap model,Integer page,String friendUserName,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		
		String dirName = templateService.findTemplateDir_cache();
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
		String accessPath = accessSourceDeviceManage.accessDevices(request);
		PageForm pageForm = new PageForm();
		pageForm.setPage(page);

		//????????????????????????
		PageView<PrivateMessage> pageView = new PageView<PrivateMessage>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		
		
		//????????????
		User chatUser = null;
		
		//??????Id??????
		Set<Long> userIdList = new HashSet<Long>();
		//????????????
		Map<Long,User> userMap = new HashMap<Long,User>();
		
		//????????????Id??????
		List<String> unreadPrivateMessageIdList = new ArrayList<String>();
		
		if(friendUserName != null && !"".equals(friendUserName.trim())){
			chatUser = userManage.query_cache_findUserByUserName(friendUserName.trim());
			if(chatUser != null){
				if(page == null){//???????????????????????????????????????
					//????????????Id??????????????????????????????
					Long count = privateMessageService .findPrivateMessageChatCountByUserId(accessUser.getUserId(),chatUser.getId(),100);
					//???????????????
					pageView.setTotalrecord(count);
					pageForm.setPage((int)pageView.getTotalpage());
					pageView = new PageView<PrivateMessage>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
					
				}
				//?????????
				int firstIndex = (pageForm.getPage()-1)*pageView.getMaxresult();
				
				QueryResult<PrivateMessage> qr = privateMessageService.findPrivateMessageChatByUserId(accessUser.getUserId(),chatUser.getId(),100,firstIndex,pageView.getMaxresult(),1);
				if(qr != null && qr.getResultlist() != null && qr.getResultlist().size() >0){
					for(PrivateMessage privateMessage : qr.getResultlist()){
						userIdList.add(privateMessage.getSenderUserId());//???????????????Id 
						userIdList.add(privateMessage.getReceiverUserId());//???????????????Id

						privateMessage.setSendTime(new Timestamp(privateMessage.getSendTimeFormat()));
						if(privateMessage.getReadTimeFormat() != null){
							privateMessage.setReadTime(new Timestamp(privateMessage.getReadTimeFormat()));
						}
						
						if(privateMessage.getStatus().equals(10)){
							unreadPrivateMessageIdList.add(privateMessage.getId());
						}
					}
				}
				
				if(userIdList != null && userIdList.size() >0){
					for(Long userId : userIdList){
						User user = userManage.query_cache_findUserById(userId);
						if(user != null){
							userMap.put(userId, user);
						}
					}
				}
				if(userMap != null && userMap.size() >0){
					if(qr != null && qr.getResultlist() != null && qr.getResultlist().size() >0){
						for(PrivateMessage privateMessage : qr.getResultlist()){
							User friend_user = userMap.get(privateMessage.getFriendUserId());
							if(friend_user != null){
								privateMessage.setFriendUserName(friend_user.getUserName());//????????????????????????
								privateMessage.setFriendAccount(friend_user.getAccount());
								privateMessage.setFriendNickname(friend_user.getNickname());
								if(friend_user.getAvatarName() != null && !"".equals(friend_user.getAvatarName().trim())){
									privateMessage.setFriendAvatarPath(fileManage.fileServerAddress()+friend_user.getAvatarPath());//????????????????????????
									privateMessage.setFriendAvatarName(friend_user.getAvatarName());//????????????????????????
								}
							}
							User sender_user = userMap.get(privateMessage.getSenderUserId());
							if(sender_user != null){
								privateMessage.setSenderUserName(sender_user.getUserName());//???????????????????????????
								if(sender_user.getCancelAccountTime().equals(-1L)){
									privateMessage.setSenderAccount(sender_user.getAccount());
									privateMessage.setSenderNickname(sender_user.getNickname());
									if(sender_user.getAvatarName() != null && !"".equals(sender_user.getAvatarName().trim())){
										privateMessage.setSenderAvatarPath(fileManage.fileServerAddress()+sender_user.getAvatarPath());//?????????????????????
										privateMessage.setSenderAvatarName(sender_user.getAvatarName());//?????????????????????
									}
								}
							}
							
						}
					}
					
					
					
				}
				//??????????????????????????????List
				pageView.setQueryResult(qr);
				
				if(unreadPrivateMessageIdList != null && unreadPrivateMessageIdList.size() >0){
					
					//??????????????????????????????
					privateMessageService.updatePrivateMessageStatus(accessUser.getUserId(), unreadPrivateMessageIdList);
					//??????????????????
					privateMessageManage.delete_cache_findUnreadPrivateMessageByUserId(accessUser.getUserId());
				}
				
				
			}
		}
		
		if(chatUser != null){
			if(chatUser.getCancelAccountTime().equals(-1L)){
				//????????????????????????
				User viewUser = new User();
				viewUser.setId(chatUser.getId());
				viewUser.setUserName(chatUser.getUserName());//???????????????
				viewUser.setAccount(chatUser.getAccount());//??????
				viewUser.setNickname(chatUser.getNickname());//??????
				viewUser.setRegistrationDate(chatUser.getRegistrationDate());//????????????
	
				List<UserGrade> userGradeList = userGradeService.findAllGrade_cache();//????????????????????????
				if(userGradeList != null && userGradeList.size() >0){
					for(UserGrade userGrade : userGradeList){
						if(chatUser.getPoint() >= userGrade.getNeedPoint()){
							viewUser.setGradeId(userGrade.getId());//??????Id
							viewUser.setGradeName(userGrade.getName());//?????????????????????????????????
							break;
						}
					} 
				}
	
				chatUser = viewUser;
			}else{
				chatUser = null;
			}
		}
		
		
		if(isAjax){
			Map<String,Object> returnValue = new HashMap<String,Object>();//?????????
			returnValue.put("chatUser", chatUser);
			returnValue.put("pageView", pageView);
			WebUtil.writeToWeb(JsonUtils.toJSONString(returnValue), "json", response);
			return null;
		}else{
			model.addAttribute("chatUser", chatUser);//????????????
			model.addAttribute("pageView", pageView);
			return "templates/"+dirName+"/"+accessPath+"/privateMessageChatList";	
		}
	}
	
	/**
	 * ??????????????????
	 * @param friendUserName ??????????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/addPrivateMessage",method=RequestMethod.GET) 
	public String addPrivateMessageUI(ModelMap model,String friendUserName,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	  	Map<String,Object> returnValue = new HashMap<String,Object>();
	  	FormCaptcha formCaptcha = new FormCaptcha();
		if(accessUser != null){
			boolean captchaKey = captchaManage.privateMessage_isCaptcha(accessUser.getUserName());//???????????????
			if(captchaKey ==true){
				formCaptcha.setShowCaptcha(true);
				formCaptcha.setCaptchaKey(UUIDUtil.getUUID32());
			}
		}
		
		if(friendUserName != null && !"".equals(friendUserName.trim())){		
			User user = userManage.query_cache_findUserByUserName(friendUserName.trim());
			if(user != null){
				returnValue.put("allowSendPrivateMessage",true);//???????????????
			}else{
				returnValue.put("allowSendPrivateMessage",false);//??????????????????
			}
		}else{
			returnValue.put("allowSendPrivateMessage",false);//??????????????????
		}
		
		if(isAjax){
			returnValue.put("formCaptcha", formCaptcha);
			WebUtil.writeToWeb(JsonUtils.toJSONString(returnValue), "json", response);
			return null;
		}else{
			model.addAttribute("formCaptcha", formCaptcha);
			model.addAttribute("allowSendPrivateMessage", returnValue.get("allowSendPrivateMessage"));
			String dirName = templateService.findTemplateDir_cache();
			String accessPath = accessSourceDeviceManage.accessDevices(request);		
			return "templates/"+dirName+"/"+accessPath+"/addPrivateMessage";	
		}
	}

	
	/**
	 * ??????  ??????
	 * @param model
	 * @param friendUserName ??????????????????
	 * @param messageContent ????????????
	 * @param jumpUrl ????????????
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/addPrivateMessage", method=RequestMethod.POST)
	@RoleAnnotation(resourceCode=ResourceEnum._6001000)
	public String addPrivateMessage(ModelMap model,String friendUserName,String messageContent,
			String token,String captchaKey,String captchaValue,String jumpUrl,
			RedirectAttributes redirectAttrs,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
		
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		
		
		Map<String,String> error = new HashMap<String,String>();
		SystemSetting systemSetting = settingService.findSystemSetting_cache();
		if(systemSetting.getCloseSite().equals(2)){
			error.put("privateMessage", ErrorView._21.name());//?????????????????????????????????
		}
			
		
		//????????????
		if(token != null && !"".equals(token.trim())){	
			String token_sessionid = csrfTokenManage.getToken(request);//????????????
			if(token_sessionid != null && !"".equals(token_sessionid.trim())){
				if(!token_sessionid.equals(token)){
					error.put("token", ErrorView._13.name());//????????????
				}
			}else{
				error.put("token", ErrorView._12.name());//????????????
			}
		}else{
			error.put("token", ErrorView._11.name());//????????????
		}
		
		//?????????
		boolean isCaptcha = captchaManage.privateMessage_isCaptcha(accessUser.getUserName());
		if(isCaptcha){//?????????????????????
			//???????????????
			if(captchaKey != null && !"".equals(captchaKey.trim())){
				//???????????????????????????
				//?????????????????????????????????
				Integer original = settingManage.getSubmitQuantity("captcha", captchaKey.trim());
	    		if(original != null){
	    			settingManage.addSubmitQuantity("captcha", captchaKey.trim(),original+1);//?????????????????????????????????
	    		}else{
	    			settingManage.addSubmitQuantity("captcha", captchaKey.trim(),1);//?????????????????????????????????
	    		}
				
				String _captcha = captchaManage.captcha_generate(captchaKey.trim(),"");
				if(captchaValue != null && !"".equals(captchaValue.trim())){
					if(_captcha != null && !"".equals(_captcha.trim())){
						if(!_captcha.equalsIgnoreCase(captchaValue)){
							error.put("captchaValue",ErrorView._15.name());//???????????????
						}
					}else{
						error.put("captchaValue",ErrorView._17.name());//???????????????
					}
				}else{
					error.put("captchaValue",ErrorView._16.name());//??????????????????
				}
				//???????????????
				captchaManage.captcha_delete(captchaKey.trim());	
			}else{
				error.put("captchaValue", ErrorView._14.name());//?????????????????????
			}
			
		}
		
		User friendUser = null;
		
		
		if(friendUserName != null && !"".equals(friendUserName.trim())){		
			friendUser = userManage.query_cache_findUserByUserName(friendUserName.trim());
			if(friendUser != null){
				if(friendUser.getState() >1){
					error.put("privateMessage", ErrorView._1000.name());//?????????????????????????????????
				}
				if(friendUser.getUserName().equals(accessUser.getUserName())){
					error.put("privateMessage", ErrorView._1010.name());//???????????????????????????
				}
			}else{
				error.put("privateMessage", ErrorView._910.name());//???????????????
			}
		}else{
			error.put("privateMessage", ErrorView._1020.name());//??????????????????????????????
		}
		
		if(messageContent != null && !"".equals(messageContent.trim())){
			if(messageContent.length() >1000){
				error.put("messageContent", ErrorView._1030.name());//????????????????????????1000?????????
			}
		}else{
			error.put("messageContent", ErrorView._1040.name());//????????????????????????
		}
		
		if(error.size() ==0){
			Long time = new Date().getTime();
			String content = WebUtil.urlToHyperlink(HtmlEscape.escape(messageContent.trim()));
			
			
			//?????????????????????
			PrivateMessage sender_privateMessage = new PrivateMessage();
			sender_privateMessage.setId(privateMessageManage.createPrivateMessageId(accessUser.getUserId()));
			sender_privateMessage.setUserId(accessUser.getUserId());//????????????Id
			sender_privateMessage.setFriendUserId(friendUser.getId());//??????????????????Id
			sender_privateMessage.setSenderUserId(accessUser.getUserId());//???????????????Id
			sender_privateMessage.setReceiverUserId(friendUser.getId());//???????????????Id
			sender_privateMessage.setMessageContent(content);//????????????
			sender_privateMessage.setStatus(20);//???????????? 20:??????
			sender_privateMessage.setSendTimeFormat(time);//?????????????????????
			sender_privateMessage.setReadTimeFormat(time);//?????????????????????
			Object sender_privateMessage_object = privateMessageManage.createPrivateMessageObject(sender_privateMessage);
			
			
			//?????????????????????
			PrivateMessage receiver_privateMessage = new PrivateMessage();
			receiver_privateMessage.setId(privateMessageManage.createPrivateMessageId(friendUser.getId()));
			receiver_privateMessage.setUserId(friendUser.getId());//????????????Id
			receiver_privateMessage.setFriendUserId(accessUser.getUserId());//??????????????????Id
			receiver_privateMessage.setSenderUserId(accessUser.getUserId());//???????????????Id
			receiver_privateMessage.setReceiverUserId(friendUser.getId());//???????????????Id
			receiver_privateMessage.setMessageContent(content);//????????????
			receiver_privateMessage.setStatus(10);//???????????? 10:??????
			receiver_privateMessage.setSendTimeFormat(time);//?????????????????????
			Object receiver_privateMessage_object = privateMessageManage.createPrivateMessageObject(receiver_privateMessage);
			
			privateMessageService.savePrivateMessage(sender_privateMessage_object, receiver_privateMessage_object);
			
			//??????????????????
			privateMessageManage.delete_cache_findUnreadPrivateMessageByUserId(accessUser.getUserId());
			privateMessageManage.delete_cache_findUnreadPrivateMessageByUserId(friendUser.getId());
			
			
			//?????????????????????????????????
			Integer original = settingManage.getSubmitQuantity("privateMessage", accessUser.getUserName());
    		if(original != null){
    			settingManage.addSubmitQuantity("privateMessage", accessUser.getUserName(),original+1);//?????????????????????????????????
    		}else{
    			settingManage.addSubmitQuantity("privateMessage", accessUser.getUserName(),1);//?????????????????????????????????
    		}
		}
		
		
		
		Map<String,String> returnError = new HashMap<String,String>();//??????
		if(error.size() >0){
			//???????????????????????????????????????
    		for (Map.Entry<String,String> entry : error.entrySet()) {
    			if(ErrorView.get(entry.getValue()) != null){
    				returnError.put(entry.getKey(),  ErrorView.get(entry.getValue()));
    			}else{
    				returnError.put(entry.getKey(),  entry.getValue());
    			}
    			
			}
		}
	    
	    if(isAjax == true){
			
    		Map<String,Object> returnValue = new HashMap<String,Object>();//?????????
    		
    		if(error != null && error.size() >0){
    			returnValue.put("success", "false");
    			returnValue.put("error", returnError);
    			if(isCaptcha){
    				returnValue.put("captchaKey", UUIDUtil.getUUID32());
    			}
    		}else{
    			returnValue.put("success", "true");
    		}
    		
    		WebUtil.writeToWeb(JsonUtils.toJSONString(returnValue), "json", response);
			return null;
		}else{
			String dirName = templateService.findTemplateDir_cache();
			
			String accessPath = accessSourceDeviceManage.accessDevices(request);

			
			if(error != null && error.size() >0){//???????????????
				
				redirectAttrs.addFlashAttribute("error", returnError);//???????????????
				
				PrivateMessage privateMessage = new PrivateMessage();
				privateMessage.setFriendUserName(friendUserName);
				privateMessage.setMessageContent(messageContent);
				redirectAttrs.addFlashAttribute("privateMessage", privateMessage);
				
				String referer = request.getHeader("referer");	

				referer = StringUtils.removeStartIgnoreCase(referer,Configuration.getUrl(request));//????????????????????????????????????,??????????????????
				referer = StringUtils.substringBefore(referer, ".");//????????????????????????????????????????????????
				referer = StringUtils.substringBefore(referer, "?");//????????????????????????????????????????????????
				
				String queryString = request.getQueryString() != null && !"".equals(request.getQueryString().trim()) ? "?"+request.getQueryString() :"";
				return "redirect:/"+referer+queryString;
					
			}
			
			
			
			
			if(jumpUrl != null && !"".equals(jumpUrl.trim())){
				String url = Base64.decodeBase64URL(jumpUrl.trim());
				return "redirect:"+url;
			}else{//????????????
				model.addAttribute("message", "??????????????????");
				String referer = request.getHeader("referer");
				if(RefererCompare.compare(request, "login")){//???????????????????????????????????????
					referer = Configuration.getUrl(request);
				}
				model.addAttribute("urlAddress", referer);
				
				return "templates/"+dirName+"/"+accessPath+"/jump";	
			}
		}
		
	}
	
	
	/**
	 * ????????????
	 * @param model
	 * @param jumpUrl ????????????   ??????post??????????????????
	 * @param token ????????????
	 * @param friendUserName ??????????????????
	 */
	@RequestMapping(value="/user/control/deletePrivateMessage", method=RequestMethod.POST)
	@RoleAnnotation(resourceCode=ResourceEnum._6002000)
	public String deletePrivateMessage(ModelMap model,String jumpUrl,String token,String friendUserName,
			RedirectAttributes redirectAttrs,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		Map<String,String> error = new HashMap<String,String>();//??????
		SystemSetting systemSetting = settingService.findSystemSetting_cache();
		if(systemSetting.getCloseSite().equals(2)){
			error.put("privateMessage", ErrorView._21.name());//?????????????????????????????????
		}
		
		//????????????
		if(token != null && !"".equals(token.trim())){	
			String token_sessionid = csrfTokenManage.getToken(request);//????????????
			if(token_sessionid != null && !"".equals(token_sessionid.trim())){
				if(!token_sessionid.equals(token)){
					error.put("token", ErrorView._13.name());
				}
			}else{
				error.put("token", ErrorView._12.name());
			}
		}else{
			error.put("token", ErrorView._11.name());
		}
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	  	
	  	//????????????
  		User chatUser = null;
  	
  		if(friendUserName != null && !"".equals(friendUserName.trim())){
  			chatUser = userService.findUserByUserName(friendUserName.trim());
  			if(chatUser == null){
  				error.put("privateMessage", ErrorView._910.name());//???????????????
  			}
  			
  		}else{
  			error.put("privateMessage", ErrorView._1020.name());//??????????????????????????????
  		}

  		
		if(error.size() == 0){
			int i = privateMessageService.softDeletePrivateMessage(accessUser.getUserId(),chatUser.getId());
			if(i == 0){
				error.put("privateMessage", ErrorView._1050.name());//??????????????????
			}
			
			//??????????????????
			privateMessageManage.delete_cache_findUnreadPrivateMessageByUserId(accessUser.getUserId());
		}
		
		Map<String,String> returnError = new HashMap<String,String>();//??????
		if(error.size() >0){
			//???????????????????????????????????????
    		for (Map.Entry<String,String> entry : error.entrySet()) {
    			if(ErrorView.get(entry.getValue()) != null){
    				returnError.put(entry.getKey(),  ErrorView.get(entry.getValue()));
    			}else{
    				returnError.put(entry.getKey(),  entry.getValue());
    			}
    			
			}
		}
		
		if(isAjax == true){
    		Map<String,Object> returnValue = new HashMap<String,Object>();//?????????
    		
    		if(error != null && error.size() >0){
    			returnValue.put("success", "false");
    			returnValue.put("error", returnError);
    		}else{
    			returnValue.put("success", "true");
    			
    		}

    		WebUtil.writeToWeb(JsonUtils.toJSONString(returnValue), "json", response);
			return null;
		}else{
			String dirName = templateService.findTemplateDir_cache();
			String accessPath = accessSourceDeviceManage.accessDevices(request);
			if(error != null && error.size() >0){//???????????????
				
				for (Map.Entry<String,String> entry : returnError.entrySet()) {		 
					model.addAttribute("message",entry.getValue());//??????
		  			return "templates/"+dirName+"/"+accessPath+"/message";
				}
					
			}
			
			
			if(jumpUrl != null && !"".equals(jumpUrl.trim())){
				String url = Base64.decodeBase64URL(jumpUrl.trim());
				return "redirect:"+url;
			}else{//????????????
				model.addAttribute("message", "??????????????????");
				String referer = request.getHeader("referer");
				if(RefererCompare.compare(request, "login")){//???????????????????????????????????????
					referer = Configuration.getUrl(request);
				}
				model.addAttribute("urlAddress", referer);
				
				return "templates/"+dirName+"/"+accessPath+"/jump";	
			}
		}
		
	}
	

	/**----------------------------------- ???????????? ----------------------------------**/
	
	/**
	 * ??????????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/systemNotifyList",method=RequestMethod.GET) 
	public String systemNotifyList(ModelMap model,PageForm pageForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		
		String dirName = templateService.findTemplateDir_cache();
		
		
		String accessPath = accessSourceDeviceManage.accessDevices(request);
	   
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	  	User user = userManage.query_cache_findUserById(accessUser.getUserId());
	  	
	  	//??????????????????
	  	this.pullSystemNotify(user.getId(),user.getRegistrationDate());
	  	
	  	
		//????????????????????????
		PageView<SubscriptionSystemNotify> pageView = new PageView<SubscriptionSystemNotify>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		//?????????
		int firstIndex = (pageForm.getPage()-1)*pageView.getMaxresult();

		//????????????Id??????
		Set<Long> systemNotifyIdList = new HashSet<Long>();
		//????????????????????????
		Map<Long,String> systemNotifyMap = new HashMap<Long,String>();
		//????????????????????????Id??????
		List<String> unreadSystemNotifyIdList = new ArrayList<String>();	
				
		QueryResult<SubscriptionSystemNotify> qr = systemNotifyService.findSubscriptionSystemNotifyByUserId(accessUser.getUserId(),100,firstIndex,pageView.getMaxresult());
		if(qr != null && qr.getResultlist() != null && qr.getResultlist().size() >0){
			for(SubscriptionSystemNotify subscriptionSystemNotify : qr.getResultlist()){
				systemNotifyIdList.add(subscriptionSystemNotify.getSystemNotifyId());
				
				if(subscriptionSystemNotify.getStatus().equals(10)){
					unreadSystemNotifyIdList.add(subscriptionSystemNotify.getId());
				}
			}
		}
		
	
		if(systemNotifyIdList != null && systemNotifyIdList.size() >0){
			for(Long systemNotifyId : systemNotifyIdList){
				SystemNotify systemNotify = systemNotifyManage.query_cache_findById(systemNotifyId);
				if(systemNotify != null){
					systemNotifyMap.put(systemNotifyId, systemNotify.getContent());
				}
			}
		}
		if(systemNotifyIdList != null && systemNotifyIdList.size() >0){
			if(qr != null && qr.getResultlist() != null && qr.getResultlist().size() >0){
				for(SubscriptionSystemNotify subscriptionSystemNotify : qr.getResultlist()){
					String content = systemNotifyMap.get(subscriptionSystemNotify.getSystemNotifyId());
					if(content != null){
						subscriptionSystemNotify.setContent(content);
					}
					
				}
			}
		}
		
		if(unreadSystemNotifyIdList != null && unreadSystemNotifyIdList.size() >0){
			//??????????????????????????????????????????
			systemNotifyService.updateSubscriptionSystemNotifyStatus(accessUser.getUserId(), unreadSystemNotifyIdList);
		}
		

		//??????????????????????????????List
		pageView.setQueryResult(qr);
		if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(pageView), "json", response);
			return null;
		}else{
			model.addAttribute("pageView", pageView);
			return "templates/"+dirName+"/"+accessPath+"/systemNotifyList";	
		}
	}
	
	/**
	 * ??????????????????
	 * @param userId ??????Id
	 * @param registrationDate ??????????????????
	 */
	private void pullSystemNotify(Long userId,Date registrationDate){
		List<Object> subscriptionSystemNotifyList = new ArrayList<Object>();
		//????????????????????????????????????????????????Id
		Long maxSystemNotifyId = systemNotifyService.findMaxSystemNotifyIdByUserId(userId);
		Map<Long,Date> systemNotifyMap = null;
		if(maxSystemNotifyId == null){//?????????????????????????????????
			systemNotifyMap = systemNotifyService.findSystemNotifyBySendTime(registrationDate);
		}else{
			systemNotifyMap = systemNotifyService.findSystemNotifyById(maxSystemNotifyId);
		}
		
		if(systemNotifyMap != null && systemNotifyMap.size() >0){
			for(Map.Entry<Long,Date> entry : systemNotifyMap.entrySet()){
				//????????????Id
				Long systemNotifyId = entry.getKey();
				SubscriptionSystemNotify subscriptionSystemNotify = new SubscriptionSystemNotify();
				subscriptionSystemNotify.setId(subscriptionSystemNotifyManage.createSubscriptionSystemNotifyId(systemNotifyId, userId));
				subscriptionSystemNotify.setSystemNotifyId(systemNotifyId);
				subscriptionSystemNotify.setUserId(userId);
				subscriptionSystemNotify.setStatus(10);
				subscriptionSystemNotify.setSendTime(entry.getValue());
				
				Object subscriptionSystemNotify_object = subscriptionSystemNotifyManage.createSubscriptionSystemNotifyObject(subscriptionSystemNotify);
				subscriptionSystemNotifyList.add(subscriptionSystemNotify_object);
			}
		}
		
		
		
		if(subscriptionSystemNotifyList != null && subscriptionSystemNotifyList.size() >0){
			try {
				systemNotifyService.saveSubscriptionSystemNotify(subscriptionSystemNotifyList);
			} catch (Exception e) {
				//e.printStackTrace();
			}
			
			//????????????
			systemNotifyManage.delete_cache_findMinUnreadSystemNotifyIdByUserId(userId);
			systemNotifyManage.delete_cache_findMaxReadSystemNotifyIdByUserId(userId);
		}
	}
	
	
	/**
	 * ??????????????????
	 * @param model
	 * @param jumpUrl ????????????   ??????post??????????????????
	 * @param token ????????????
	 * @param subscriptionSystemNotifyId ??????????????????Id
	 */
	@RequestMapping(value="/user/control/deleteSystemNotify", method=RequestMethod.POST)
	public String deleteSystemNotify(ModelMap model,String jumpUrl,String token,String subscriptionSystemNotifyId,
			RedirectAttributes redirectAttrs,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		Map<String,String> error = new HashMap<String,String>();//??????
		SystemSetting systemSetting = settingService.findSystemSetting_cache();
		if(systemSetting.getCloseSite().equals(2)){
			error.put("systemNotify", ErrorView._21.name());//?????????????????????????????????
		}
		
		//????????????
		if(token != null && !"".equals(token.trim())){	
			String token_sessionid = csrfTokenManage.getToken(request);//????????????
			if(token_sessionid != null && !"".equals(token_sessionid.trim())){
				if(!token_sessionid.equals(token)){
					error.put("token", ErrorView._13.name());
				}
			}else{
				error.put("token", ErrorView._12.name());
			}
		}else{
			error.put("token", ErrorView._11.name());
		}
		
		if(subscriptionSystemNotifyId == null || "".equals(subscriptionSystemNotifyId.trim())){
			error.put("systemNotify", ErrorView._1100.name());//??????????????????Id????????????
		}
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	  	
	 

		if(error.size() == 0){
			int i = systemNotifyService.softDeleteSubscriptionSystemNotify(accessUser.getUserId(),subscriptionSystemNotifyId);
			
			//????????????
			systemNotifyManage.delete_cache_findMinUnreadSystemNotifyIdByUserId(accessUser.getUserId());
			systemNotifyManage.delete_cache_findMaxReadSystemNotifyIdByUserId(accessUser.getUserId());
			if(i == 0){
				error.put("systemNotify", ErrorView._1110.name());//????????????????????????
			}
		}
		
		Map<String,String> returnError = new HashMap<String,String>();//??????
		if(error.size() >0){
			//???????????????????????????????????????
    		for (Map.Entry<String,String> entry : error.entrySet()) {
    			if(ErrorView.get(entry.getValue()) != null){
    				returnError.put(entry.getKey(),  ErrorView.get(entry.getValue()));
    			}else{
    				returnError.put(entry.getKey(),  entry.getValue());
    			}
    			
			}
		}
		
		if(isAjax == true){
    		Map<String,Object> returnValue = new HashMap<String,Object>();//?????????
    		
    		if(error != null && error.size() >0){
    			returnValue.put("success", "false");
    			returnValue.put("error", returnError);
    		}else{
    			returnValue.put("success", "true");
    			
    		}

    		WebUtil.writeToWeb(JsonUtils.toJSONString(returnValue), "json", response);
			return null;
		}else{
			String dirName = templateService.findTemplateDir_cache();
			String accessPath = accessSourceDeviceManage.accessDevices(request);
			if(error != null && error.size() >0){//???????????????
				
				for (Map.Entry<String,String> entry : returnError.entrySet()) {		 
					model.addAttribute("message",entry.getValue());//??????
		  			return "templates/"+dirName+"/"+accessPath+"/message";
				}
					
			}
			
			
			if(jumpUrl != null && !"".equals(jumpUrl.trim())){
				String url = Base64.decodeBase64URL(jumpUrl.trim());
				return "redirect:"+url;
			}else{//????????????
				model.addAttribute("message", "????????????????????????");
				String referer = request.getHeader("referer");
				if(RefererCompare.compare(request, "login")){//???????????????????????????????????????
					referer = Configuration.getUrl(request);
				}
				model.addAttribute("urlAddress", referer);
				
				return "templates/"+dirName+"/"+accessPath+"/jump";	
			}
		}
		
	}
	
	
	/**
	 * ??????????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/unreadMessageCount",method=RequestMethod.GET) 
	@ResponseBody//????????????ajax,?????????????????????
	public String unreadMessageCount(ModelMap model,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	  	
	  	//???????????????????????????
	  	followManage.pullFollow(accessUser.getUserId(),accessUser.getUserName());
	  	
	  	UnreadMessage unreadMessage = new UnreadMessage();
	  	
	  	//????????????????????????
	  	Long unreadPrivateMessageCount = privateMessageManage.query_cache_findUnreadPrivateMessageByUserId(accessUser.getUserId());
	  	unreadMessage.setPrivateMessageCount(unreadPrivateMessageCount);
	  	
	  	//??????????????????Id
	  	Long start_systemNotifyId = 0L;
	  	//??????????????????????????????
	  	Date start_systemNotifySendTime = null;
	  	
	  	//???????????????????????????Id
	  	Long minUnreadSystemNotifyId = systemNotifyManage.query_cache_findMinUnreadSystemNotifyIdByUserId(accessUser.getUserId());
	  	if(minUnreadSystemNotifyId == null){
	  		
	  		//???????????????????????????Id
	  		Long maxReadSystemNotifyId = systemNotifyManage.query_cache_findMaxReadSystemNotifyIdByUserId(accessUser.getUserId());
	  		if(maxReadSystemNotifyId != null){
	  			start_systemNotifyId = maxReadSystemNotifyId;
	  		}else{
	  			//????????????
	  	  		User user = userManage.query_cache_findUserByUserName(accessUser.getUserName());
	  	  		start_systemNotifySendTime = user.getRegistrationDate();
	  	  		
	  		}
	  		
	  		
	  	}else{
	  		start_systemNotifyId = minUnreadSystemNotifyId -1L;//-1?????????SQL?????????????????????????????????Id
	  	}
	  	if(start_systemNotifySendTime == null){
	  		//??????????????????????????????(??????????????????Id??????????????????Id,???????????????????????????Id??????)
		  	Long unreadSystemNotifyCount = systemNotifyManage.query_cache_findSystemNotifyCountBySystemNotifyId(start_systemNotifyId);
		  	unreadMessage.setSystemNotifyCount(unreadSystemNotifyCount);
	  	}else{
	  		Long unreadSystemNotifyCount = systemNotifyManage.query_cache_findSystemNotifyCountBySendTime(start_systemNotifySendTime);
	  		unreadMessage.setSystemNotifyCount(unreadSystemNotifyCount);
	  	}
	  	
	  	//??????????????????
	  	Long unreadRemindCount = remindManage.query_cache_findUnreadRemindByUserId(accessUser.getUserId());
	  	unreadMessage.setRemindCount(unreadRemindCount);
	  	
	  	

		
	  	
	  	return JsonUtils.toJSONString(unreadMessage);
	}
	
	
	/**
	 * ????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/remindList",method=RequestMethod.GET) 
	public String remindList(ModelMap model,PageForm pageForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		
		String dirName = templateService.findTemplateDir_cache();
		
		
		String accessPath = accessSourceDeviceManage.accessDevices(request);
	   
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	  	
	  	
		//????????????????????????
		PageView<Remind> pageView = new PageView<Remind>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		//?????????
		int firstIndex = (pageForm.getPage()-1)*pageView.getMaxresult();
		
		//??????Id??????
		Set<Long> userIdList = new HashSet<Long>();
		//????????????
		Map<Long,User> userMap = new HashMap<Long,User>();
		
		//????????????Id??????
		List<String> unreadRemindIdList = new ArrayList<String>();
		
		
		QueryResult<Remind> qr = remindService.findRemindByUserId(accessUser.getUserId(),100,firstIndex,pageView.getMaxresult());
		if(qr != null && qr.getResultlist() != null && qr.getResultlist().size() >0){
			for(Remind remind : qr.getResultlist()){
				userIdList.add(remind.getSenderUserId());//????????????Id
				
				if(remind .getStatus().equals(10)){
					unreadRemindIdList.add(remind.getId());
				}
				
				remind.setSendTime(new Timestamp(remind.getSendTimeFormat()));
				if(remind.getReadTimeFormat() != null){
					remind.setReadTime(new Timestamp(remind.getReadTimeFormat()));
				}
				
				if(remind.getTopicId() != null && remind.getTopicId() >0L){
					Topic topic = topicManage.queryTopicCache(remind.getTopicId());//????????????
					if(topic != null){
						remind.setTopicTitle(topic.getTitle());
					}
					
				}
				if(remind.getQuestionId() != null && remind.getQuestionId() >0L){
					Question question = questionManage.query_cache_findById(remind.getQuestionId());//????????????
					if(question != null){
						remind.setQuestionTitle(question.getTitle());
					}
					
				}
				
				
			}

			if(userIdList != null && userIdList.size() >0){
				for(Long userId : userIdList){
					User user = userManage.query_cache_findUserById(userId);
					if(user != null){
						userMap.put(userId, user);
					}
				}
			}
			if(userMap != null && userMap.size() >0){
				if(qr != null && qr.getResultlist() != null && qr.getResultlist().size() >0){
					for(Remind remind : qr.getResultlist()){
						
						User sender_user = userMap.get(remind.getSenderUserId());
						if(sender_user != null && sender_user.getCancelAccountTime().equals(-1L)){
							remind.setSenderUserName(sender_user.getUserName());//?????????????????????
							remind.setSenderAccount(sender_user.getAccount());//???????????????
							remind.setSenderNickname(sender_user.getNickname());
							if(sender_user.getAvatarName() != null && !"".equals(sender_user.getAvatarName().trim())){
								remind.setSenderAvatarPath(fileManage.fileServerAddress()+sender_user.getAvatarPath());//?????????????????????
								remind.setSenderAvatarName(sender_user.getAvatarName());//?????????????????????
							}
						}
						
					}
				}
			}
		}
		
		if(unreadRemindIdList != null && unreadRemindIdList.size() >0){
			//??????????????????????????????
			remindService.updateRemindStatus(accessUser.getUserId(), unreadRemindIdList);
			//??????????????????
			remindManage.delete_cache_findUnreadRemindByUserId(accessUser.getUserId());
		}
		
		//??????????????????????????????List
		pageView.setQueryResult(qr);
		
		if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(pageView), "json", response);
			return null;
		}else{
			model.addAttribute("pageView", pageView);
			return "templates/"+dirName+"/"+accessPath+"/remindList";	
		}
	}
	
	/**
	 * ????????????
	 * @param model
	 * @param jumpUrl ????????????   ??????post??????????????????
	 * @param token ????????????
	 * @param remindId ??????Id
	 */
	@RequestMapping(value="/user/control/deleteRemind", method=RequestMethod.POST)
	public String deleteRemind(ModelMap model,String jumpUrl,String token,String remindId,
			RedirectAttributes redirectAttrs,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		Map<String,String> error = new HashMap<String,String>();//??????
		SystemSetting systemSetting = settingService.findSystemSetting_cache();
		if(systemSetting.getCloseSite().equals(2)){
			error.put("remind", ErrorView._21.name());//?????????????????????????????????
		}
		
		//????????????
		if(token != null && !"".equals(token.trim())){	
			String token_sessionid = csrfTokenManage.getToken(request);//????????????
			if(token_sessionid != null && !"".equals(token_sessionid.trim())){
				if(!token_sessionid.equals(token)){
					error.put("token", ErrorView._13.name());
				}
			}else{
				error.put("token", ErrorView._12.name());
			}
		}else{
			error.put("token", ErrorView._11.name());
		}
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	  	
	  	
  		if(remindId == null || "".equals(remindId.trim())){
  			error.put("remind", ErrorView._1400.name());//???????????????
  		}
  		
		if(error.size() == 0){
			int i = remindService.softDeleteRemind(accessUser.getUserId(),remindId.trim());
			if(i == 0){
				error.put("remind", ErrorView._1050.name());//??????????????????
			}
			//??????????????????
			remindManage.delete_cache_findUnreadRemindByUserId(accessUser.getUserId());
		}
		
		Map<String,String> returnError = new HashMap<String,String>();//??????
		if(error.size() >0){
			//???????????????????????????????????????
    		for (Map.Entry<String,String> entry : error.entrySet()) {
    			if(ErrorView.get(entry.getValue()) != null){
    				returnError.put(entry.getKey(),  ErrorView.get(entry.getValue()));
    			}else{
    				returnError.put(entry.getKey(),  entry.getValue());
    			}
    			
			}
		}
		
		if(isAjax == true){
    		Map<String,Object> returnValue = new HashMap<String,Object>();//?????????
    		
    		if(error != null && error.size() >0){
    			returnValue.put("success", "false");
    			returnValue.put("error", returnError);
    		}else{
    			returnValue.put("success", "true");
    			
    		}

    		WebUtil.writeToWeb(JsonUtils.toJSONString(returnValue), "json", response);
			return null;
		}else{
			String dirName = templateService.findTemplateDir_cache();
			String accessPath = accessSourceDeviceManage.accessDevices(request);
			if(error != null && error.size() >0){//???????????????
				
				for (Map.Entry<String,String> entry : returnError.entrySet()) {		 
					model.addAttribute("message",entry.getValue());//??????
		  			return "templates/"+dirName+"/"+accessPath+"/message";
				}
					
			}
			
			
			if(jumpUrl != null && !"".equals(jumpUrl.trim())){
				String url = Base64.decodeBase64URL(jumpUrl.trim());
				return "redirect:"+url;
			}else{//????????????
				model.addAttribute("message", "??????????????????");
				String referer = request.getHeader("referer");
				if(RefererCompare.compare(request, "login")){//???????????????????????????????????????
					referer = Configuration.getUrl(request);
				}
				model.addAttribute("urlAddress", referer);
				
				return "templates/"+dirName+"/"+accessPath+"/jump";	
			}
		}
		
	}
	
	/**
	 * ??????????????????
	 * @param model
	 * @param pageForm
	 * @param topicId ??????Id
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/topicFavoriteList",method=RequestMethod.GET) 
	public String topicFavoriteList(ModelMap model,PageForm pageForm,Long topicId,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		
		String dirName = templateService.findTemplateDir_cache();
		
		
		String accessPath = accessSourceDeviceManage.accessDevices(request);
	   
		//????????????????????????
		PageView<Favorites> pageView = new PageView<Favorites>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	  	if(topicId != null && topicId > 0L){
	  		Topic topicInfo = topicManage.queryTopicCache(topicId);//????????????
	  		if(topicInfo != null && topicInfo.getUserName().equals(accessUser.getUserName())){
	  			//?????????
	  			int firstIndex = (pageForm.getPage()-1)*pageView.getMaxresult();
	  			
	  			
	  			QueryResult<Favorites> qr = favoriteService.findFavoritePageByTopicId(firstIndex,pageView.getMaxresult(),topicId);
	  			if(qr != null && qr.getResultlist() != null && qr.getResultlist().size() >0){
	  				for(Favorites favorites : qr.getResultlist()){
	  					Topic topic = topicManage.queryTopicCache(favorites.getTopicId());//????????????
	  					if(topic != null){
	  						favorites.setTopicTitle(topic.getTitle());
	  					}
	  					User user = userManage.query_cache_findUserByUserName(favorites.getUserName());
	  					if(user != null && user.getCancelAccountTime().equals(-1L)){
	  						favorites.setAccount(user.getAccount());
	  						favorites.setNickname(user.getNickname());
							if(user.getAvatarName() != null && !"".equals(user.getAvatarName().trim())){
								favorites.setAvatarPath(fileManage.fileServerAddress()+user.getAvatarPath());//????????????
								favorites.setAvatarName(user.getAvatarName());//????????????
							}
						}else{
							favorites.setUserName(null);
						}
	  				}
	  			}
	  			//??????????????????????????????List
	  			pageView.setQueryResult(qr);
	  		}
	  	}
	  	
	  	
		
		
		if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(pageView), "json", response);
			return null;
		}else{
			model.addAttribute("pageView", pageView);
			return "templates/"+dirName+"/"+accessPath+"/topicFavoriteList";	
		}
	}
	
	/**
	 * ??????????????????
	 * @param model
	 * @param pageForm
	 * @param questionId ??????Id
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/questionFavoriteList",method=RequestMethod.GET) 
	public String questionFavoriteList(ModelMap model,PageForm pageForm,Long questionId,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		
		String dirName = templateService.findTemplateDir_cache();
		
		
		String accessPath = accessSourceDeviceManage.accessDevices(request);
	   
		//????????????????????????
		PageView<Favorites> pageView = new PageView<Favorites>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	  	if(questionId != null && questionId > 0L){
	  		Question questionInfo = questionManage.query_cache_findById(questionId);//????????????
	  		if(questionInfo != null && questionInfo.getUserName().equals(accessUser.getUserName())){
	  			//?????????
	  			int firstIndex = (pageForm.getPage()-1)*pageView.getMaxresult();
	  			
	  			
	  			QueryResult<Favorites> qr = favoriteService.findFavoritePageByQuestionId(firstIndex,pageView.getMaxresult(),questionId);
	  			if(qr != null && qr.getResultlist() != null && qr.getResultlist().size() >0){
	  				for(Favorites favorites : qr.getResultlist()){
	  					Question question = questionManage.query_cache_findById(favorites.getQuestionId());//????????????
	  					if(question != null){
	  						favorites.setQuestionTitle(question.getTitle());
	  					}
	  					User user = userManage.query_cache_findUserByUserName(favorites.getUserName());
	  					if(user != null && user.getCancelAccountTime().equals(-1L)){
	  						favorites.setAccount(user.getAccount());
	  						favorites.setNickname(user.getNickname());
							if(user.getAvatarName() != null && !"".equals(user.getAvatarName().trim())){
								favorites.setAvatarPath(fileManage.fileServerAddress()+user.getAvatarPath());//????????????
								favorites.setAvatarName(user.getAvatarName());//????????????
							}
						}else{
							favorites.setUserName(null);
						}
	  				}
	  			}
	  			//??????????????????????????????List
	  			pageView.setQueryResult(qr);
	  		}
	  	}
	  	
	  	
		
		
		if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(pageView), "json", response);
			return null;
		}else{
			model.addAttribute("pageView", pageView);
			return "templates/"+dirName+"/"+accessPath+"/questionFavoriteList";	
		}
	}
	
	
	/**
	 * ???????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/favoriteList",method=RequestMethod.GET) 
	public String favoriteList(ModelMap model,PageForm pageForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		
		String dirName = templateService.findTemplateDir_cache();
		
		
		String accessPath = accessSourceDeviceManage.accessDevices(request);
	   
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	  	
	  	
		//????????????????????????
		PageView<Favorites> pageView = new PageView<Favorites>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		//?????????
		int firstIndex = (pageForm.getPage()-1)*pageView.getMaxresult();
		
		QueryResult<Favorites> qr = favoriteService.findFavoriteByUserId(accessUser.getUserId(),accessUser.getUserName(),firstIndex,pageView.getMaxresult());
		if(qr != null && qr.getResultlist() != null && qr.getResultlist().size() >0){
			for(Favorites favorites : qr.getResultlist()){
				if(favorites.getModule().equals(10)){//??????
					Topic topic = topicManage.queryTopicCache(favorites.getTopicId());//????????????
					if(topic != null){
						favorites.setTopicTitle(topic.getTitle());
					}
				}else if(favorites.getModule().equals(20)){//??????
					Question question = questionManage.query_cache_findById(favorites.getQuestionId());//????????????
					if(question != null){
						favorites.setQuestionTitle(question.getTitle());
					}
				}
				
			}
		}
		//??????????????????????????????List
		pageView.setQueryResult(qr);
		
		if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(pageView), "json", response);
			return null;
		}else{
			model.addAttribute("pageView", pageView);
			return "templates/"+dirName+"/"+accessPath+"/favoriteList";	
		}
	}
	
	
	/**
	 * ????????????
	 * @param model
	 * @param jumpUrl ????????????   ??????post??????????????????
	 * @param token ????????????
	 * @param remindId ??????Id
	 */
	@RequestMapping(value="/user/control/deleteFavorite", method=RequestMethod.POST)
	@RoleAnnotation(resourceCode=ResourceEnum._3002000)
	public String deleteFavorite(ModelMap model,String jumpUrl,String token,String favoriteId,
			RedirectAttributes redirectAttrs,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		Map<String,String> error = new HashMap<String,String>();//??????
		SystemSetting systemSetting = settingService.findSystemSetting_cache();
		if(systemSetting.getCloseSite().equals(2)){
			error.put("favorite", ErrorView._21.name());//?????????????????????????????????
		}
		
		//????????????
		if(token != null && !"".equals(token.trim())){	
			String token_sessionid = csrfTokenManage.getToken(request);//????????????
			if(token_sessionid != null && !"".equals(token_sessionid.trim())){
				if(!token_sessionid.equals(token)){
					error.put("token", ErrorView._13.name());
				}
			}else{
				error.put("token", ErrorView._12.name());
			}
		}else{
			error.put("token", ErrorView._11.name());
		}
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	  	Favorites favorites = null;
	  	//????????????Id
	  	String topicFavoriteId = null;
	  	//????????????Id
	  	String questionFavoriteId = null;
  		if(favoriteId == null || "".equals(favoriteId.trim())){
  			error.put("favorite", ErrorView._1530.name());//??????Id?????????
  		}else{
  			favorites  = favoriteService.findById(favoriteId.trim());
  			if(favorites != null){
  				if(favorites.getUserName().equals(accessUser.getUserName())){
  					if(favorites.getModule().equals(10)){//????????????
  						topicFavoriteId = favoriteManage.createTopicFavoriteId(favorites.getTopicId(), accessUser.getUserId());
  					}else if(favorites.getModule().equals(20)){//????????????
  						questionFavoriteId = favoriteManage.createQuestionFavoriteId(favorites.getQuestionId(), accessUser.getUserId());
  					}
  					
  				}else{
  					error.put("favorite", ErrorView._1560.name());//??????????????????????????????
  				}
  			}else{
  				error.put("favorite", ErrorView._1550.name());//???????????????
  			}
  		}
  		
		if(error.size() == 0){
			if(favorites.getModule().equals(10)){//????????????
				int i = favoriteService.deleteFavorite(favoriteId.trim(),topicFavoriteId, null);
				if(i == 0){
					error.put("favorite", ErrorView._1540.name());//??????????????????
				}
				//??????????????????
				favoriteManage.delete_cache_findTopicFavoriteById(topicFavoriteId);
				favoriteManage.delete_cache_findFavoriteCountByTopicId(favorites.getTopicId());
			}else if(favorites.getModule().equals(20)){//????????????
				int i = favoriteService.deleteFavorite(favoriteId.trim(),null,questionFavoriteId);
				if(i == 0){
					error.put("favorite", ErrorView._1540.name());//??????????????????
				}
				//??????????????????
				favoriteManage.delete_cache_findQuestionFavoriteById(questionFavoriteId);
				favoriteManage.delete_cache_findFavoriteCountByQuestionId(favorites.getQuestionId());
			}
			
		}
		
		Map<String,String> returnError = new HashMap<String,String>();//??????
		if(error.size() >0){
			//???????????????????????????????????????
    		for (Map.Entry<String,String> entry : error.entrySet()) {
    			if(ErrorView.get(entry.getValue()) != null){
    				returnError.put(entry.getKey(),  ErrorView.get(entry.getValue()));
    			}else{
    				returnError.put(entry.getKey(),  entry.getValue());
    			}
    			
			}
		}
		
		if(isAjax == true){
    		Map<String,Object> returnValue = new HashMap<String,Object>();//?????????
    		
    		if(error != null && error.size() >0){
    			returnValue.put("success", "false");
    			returnValue.put("error", returnError);
    		}else{
    			returnValue.put("success", "true");
    			
    		}

    		WebUtil.writeToWeb(JsonUtils.toJSONString(returnValue), "json", response);
			return null;
		}else{
			String dirName = templateService.findTemplateDir_cache();
			String accessPath = accessSourceDeviceManage.accessDevices(request);
			if(error != null && error.size() >0){//???????????????
				
				for (Map.Entry<String,String> entry : returnError.entrySet()) {		 
					model.addAttribute("message",entry.getValue());//??????
		  			return "templates/"+dirName+"/"+accessPath+"/message";
				}
					
			}
			
			
			if(jumpUrl != null && !"".equals(jumpUrl.trim())){
				String url = Base64.decodeBase64URL(jumpUrl.trim());
				return "redirect:"+url;
			}else{//????????????
				model.addAttribute("message", "??????????????????");
				String referer = request.getHeader("referer");
				if(RefererCompare.compare(request, "login")){//???????????????????????????????????????
					referer = Configuration.getUrl(request);
				}
				model.addAttribute("urlAddress", referer);
				
				return "templates/"+dirName+"/"+accessPath+"/jump";	
			}
		}
		
	}
	
	
	
	/**
	 * ??????????????????????????????(?????????'??????????????????','??????????????????','??????????????????'?????????)
	 * @param model
	 * @param pageForm
	 * @param topicId ??????Id
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/topicUnhideList",method=RequestMethod.GET) 
	public String topicUnhideList(ModelMap model,PageForm pageForm,Long topicId,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		
		String dirName = templateService.findTemplateDir_cache();
		
		
		String accessPath = accessSourceDeviceManage.accessDevices(request);
	   
		//????????????????????????
		PageView<TopicUnhide> pageView = new PageView<TopicUnhide>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	  	if(topicId != null && topicId > 0L){
	  		Topic topicInfo = topicManage.queryTopicCache(topicId);//????????????
	  		if(topicInfo != null && topicInfo.getUserName().equals(accessUser.getUserName())){
	  			//?????????
	  			int firstIndex = (pageForm.getPage()-1)*pageView.getMaxresult();
	  			
	  			
	  			QueryResult<TopicUnhide> qr = topicService.findTopicUnhidePageByTopicId(firstIndex,pageView.getMaxresult(),topicId);
	  			if(qr != null && qr.getResultlist() != null && qr.getResultlist().size() >0){
	  				for(TopicUnhide topicUnhide : qr.getResultlist()){
	  					User user = userManage.query_cache_findUserByUserName(topicUnhide.getUserName());
	  					if(user != null && user.getCancelAccountTime().equals(-1L)){
							topicUnhide.setAccount(user.getAccount());
							topicUnhide.setNickname(user.getNickname());
							topicUnhide.setAvatarPath(fileManage.fileServerAddress()+user.getAvatarPath());
							topicUnhide.setAvatarName(user.getAvatarName());
						}else{
							topicUnhide.setUserName(null);
						}
	  				}
	  			}
	  			//??????????????????????????????List
	  			pageView.setQueryResult(qr);
	  		}
	  	}
		
		if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(pageView), "json", response);
			return null;
		}else{
			model.addAttribute("pageView", pageView);
			return "templates/"+dirName+"/"+accessPath+"/topicUnhideList";	
		}
	}
	
	/**
	 * ??????????????????
	 * @param model
	 * @param pageForm
	 * @param topicId ??????Id
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/userDynamicList",method=RequestMethod.GET) 
	public String userDynamicList(ModelMap model,PageForm pageForm,String userName,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		
		String dirName = templateService.findTemplateDir_cache();
		
		
		String accessPath = accessSourceDeviceManage.accessDevices(request);
	   
		
		
		//????????????????????????
		PageView<UserDynamic> pageView = new PageView<UserDynamic>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	  	
	  	
	  	Long _userId = null;
	  	String _userName = null;
	  	if(userName != null && !"".equals(userName.trim())){
	  		if(userName.equals(accessUser.getUserName())){//??????
	  			_userId = accessUser.getUserId();
	  			_userName = accessUser.getUserName();
	  		}else{//????????????
	  			User user = userManage.query_cache_findUserByUserName(userName);
	  			if(user != null && user.getAllowUserDynamic() != null && user.getAllowUserDynamic() &&  user.getState().equals(1) && user.getCancelAccountTime().equals(-1L)){//????????????
	  				_userId = user.getId();
	  				_userName = user.getUserName();
	  			}
	  		}
	  	}else{
	  		_userId = accessUser.getUserId();
	  		_userName = accessUser.getUserName();
	  	}

	  	if(_userId != null){
	  		//?????????
  			int firstIndex = (pageForm.getPage()-1)*pageView.getMaxresult();
	  		QueryResult<UserDynamic> qr = userService.findUserDynamicPage(_userId,_userName,firstIndex,pageView.getMaxresult());
	  		
	  		if(qr != null && qr.getResultlist() != null && qr.getResultlist().size() >0){
				for(UserDynamic userDynamic : qr.getResultlist()){
					User user = userManage.query_cache_findUserByUserName(userDynamic.getUserName());
					if(user != null){
						userDynamic.setAccount(user.getAccount());
						userDynamic.setNickname(user.getNickname());
						userDynamic.setAvatarPath(fileManage.fileServerAddress()+user.getAvatarPath());
						userDynamic.setAvatarName(user.getAvatarName());
					}
					
				
					
					
					if(userDynamic.getTopicId() >0L){
						Topic topicInfo = topicManage.queryTopicCache(userDynamic.getTopicId());//????????????
						if(topicInfo != null){
							userDynamic.setTopicTitle(topicInfo.getTitle());
							userDynamic.setTopicViewTotal(topicInfo.getViewTotal());
							userDynamic.setTopicCommentTotal(topicInfo.getCommentTotal());
							
							List<String> topicRoleNameList = userRoleManage.queryAllowViewTopicRoleName(topicInfo.getTagId());
							
							
							if(topicRoleNameList != null && topicRoleNameList.size() >0){
								userDynamic.setAllowRoleViewList(topicRoleNameList);//???????????????????????????????????????
							}
							SystemSetting systemSetting = settingService.findSystemSetting_cache();
							//??????????????????MD5
							String topicContentDigest_link = "";
							String topicContentDigest_video = "";
							
							if(topicInfo.getContent() != null && !"".equals(topicInfo.getContent().trim())){
								//?????????????????????
								topicInfo.setContent(fileManage.processRichTextFilePath(topicInfo.getContent(),"topic"));
							}
							//?????????????????????
							if(topicInfo.getContent() != null && !"".equals(topicInfo.getContent().trim()) && systemSetting.getFileSecureLinkSecret() != null && !"".equals(systemSetting.getFileSecureLinkSecret().trim())){
								List<String> serverAddressList = fileManage.fileServerAllAddress();
								//???????????????????????????????????????
								Map<String,String> analysisFullFileNameMap = topicManage.query_cache_analysisFullFileName(topicInfo.getContent(),topicInfo.getId(),serverAddressList);
								if(analysisFullFileNameMap != null && analysisFullFileNameMap.size() >0){
									
									
									Map<String,String> newFullFileNameMap = new HashMap<String,String>();//???????????????????????? key: ?????????????????? value: ???????????????
									for (Map.Entry<String,String> entry : analysisFullFileNameMap.entrySet()) {

										newFullFileNameMap.put(entry.getKey(), SecureLink.createDownloadRedirectLink(entry.getKey(),entry.getValue(),topicInfo.getTagId(),systemSetting.getFileSecureLinkSecret()));
									}
									
									Integer topicContentUpdateMark = topicManage.query_cache_markUpdateTopicStatus(topicInfo.getId(), Integer.parseInt(RandomStringUtils.randomNumeric(8)));
									//????????????'?????????????????????????????????'Id
									String processFullFileNameId = topicManage.createProcessFullFileNameId(topicInfo.getId(),topicContentUpdateMark,newFullFileNameMap);
									
									topicInfo.setContent(topicManage.query_cache_processFullFileName(topicInfo.getContent(),"topic",newFullFileNameMap,processFullFileNameId,serverAddressList));
									
									topicContentDigest_link = cms.utils.MD5.getMD5(processFullFileNameId);
								}
								
								
							}
							
							
							//???????????????????????????
							if(topicInfo.getContent() != null && !"".equals(topicInfo.getContent().trim())){
								Integer topicContentUpdateMark = topicManage.query_cache_markUpdateTopicStatus(topicInfo.getId(), Integer.parseInt(RandomStringUtils.randomNumeric(8)));
								
								//????????????'???????????????'Id
								String processVideoPlayerId = mediaProcessQueueManage.createProcessVideoPlayerId(topicInfo.getId(),topicContentUpdateMark);
								
								//???????????????????????????
								String content = mediaProcessQueueManage.query_cache_processVideoPlayer(topicInfo.getContent(),processVideoPlayerId+"|"+topicContentDigest_link,topicInfo.getTagId(),systemSetting.getFileSecureLinkSecret());
								topicInfo.setContent(content);
								
								topicContentDigest_video = cms.utils.MD5.getMD5(processVideoPlayerId);
							}

							
							//??????????????????
							if(userDynamic.getModule().equals(100) && topicInfo.getContent() != null && !"".equals(topicInfo.getContent().trim())){
								//???????????????????????????
								List<Integer> visibleTagList = new ArrayList<Integer>();
								if(accessUser != null){
									//???????????????????????????????????????????????????????????????
									if(topicInfo.getIsStaff() == false && topicInfo.getUserName().equals(accessUser.getUserName())){
										visibleTagList.add(HideTagType.PASSWORD.getName());
										visibleTagList.add(HideTagType.COMMENT.getName());
										visibleTagList.add(HideTagType.GRADE.getName());
										visibleTagList.add(HideTagType.POINT.getName());
										visibleTagList.add(HideTagType.AMOUNT.getName());
									}else{
										//??????????????????
										Map<Integer,Object> analysisHiddenTagMap = topicManage.query_cache_analysisHiddenTag(topicInfo.getContent(),topicInfo.getId());
										if(analysisHiddenTagMap != null && analysisHiddenTagMap.size() >0){
											for (Map.Entry<Integer,Object> entry : analysisHiddenTagMap.entrySet()) {
												
												if(entry.getKey().equals(HideTagType.PASSWORD.getName())){//??????????????????
													//??????????????????Id
												  	String topicUnhideId = topicManage.createTopicUnhideId(accessUser.getUserId(), HideTagType.PASSWORD.getName(), userDynamic.getTopicId());
												  
													TopicUnhide topicUnhide = topicManage.query_cache_findTopicUnhideById(topicUnhideId);
											  		
											  		if(topicUnhide != null){
											  			visibleTagList.add(HideTagType.PASSWORD.getName());//??????????????????????????????
												  	}
												}else if(entry.getKey().equals(HideTagType.COMMENT.getName())){//??????????????????
													Boolean isUnhide = topicManage.query_cache_findWhetherCommentTopic(userDynamic.getTopicId(),accessUser.getUserName());
													if(isUnhide){
														visibleTagList.add(HideTagType.COMMENT.getName());//??????????????????????????????
													}
												}else if(entry.getKey().equals(HideTagType.GRADE.getName())){//??????????????????
													User _user = userManage.query_cache_findUserByUserName(accessUser.getUserName());
													if(_user != null){
														List<UserGrade> userGradeList = userGradeService.findAllGrade_cache();//????????????????????????
														if(userGradeList != null && userGradeList.size() >0){
															for(UserGrade userGrade : userGradeList){
																if(_user.getPoint() >= userGrade.getNeedPoint() && (Long)entry.getValue() <=userGrade.getNeedPoint()){
																	visibleTagList.add(HideTagType.GRADE.getName());//??????????????????????????????
																	
																	break;
																}
															} 
																
															
														}
													}
													
												}else if(entry.getKey().equals(HideTagType.POINT.getName())){//??????????????????
													//??????????????????Id
												  	String topicUnhideId = topicManage.createTopicUnhideId(accessUser.getUserId(), HideTagType.POINT.getName(), userDynamic.getTopicId());
												  
													TopicUnhide topicUnhide = topicManage.query_cache_findTopicUnhideById(topicUnhideId);
											  		
											  		if(topicUnhide != null){
											  			visibleTagList.add(HideTagType.POINT.getName());//??????????????????????????????
												  	}
												}else if(entry.getKey().equals(HideTagType.AMOUNT.getName())){//??????????????????
													//??????????????????Id
												  	String topicUnhideId = topicManage.createTopicUnhideId(accessUser.getUserId(), HideTagType.AMOUNT.getName(), userDynamic.getTopicId());
												  	TopicUnhide topicUnhide = topicManage.query_cache_findTopicUnhideById(topicUnhideId);
											  		
											  		if(topicUnhide != null){
											  			visibleTagList.add(HideTagType.AMOUNT.getName());//??????????????????????????????
												  	}
												}
												
											}
										
										
										
											//??????????????????????????????
											LinkedHashMap<Integer,Boolean> hideTagTypeMap = new LinkedHashMap<Integer,Boolean>();//key:??????????????????????????????  10.??????????????????  20.??????????????????  30.?????????????????? 40.?????????????????? 50.??????????????????  value:??????????????????????????????????????????	
											for (HideTagType hideTagType : HideTagType.values()) {//???????????????????????????
									            for (Map.Entry<Integer,Object> entry : analysisHiddenTagMap.entrySet()) {
													if(entry.getKey().equals(hideTagType.getName())){
														if(visibleTagList.contains(entry.getKey())){
															hideTagTypeMap.put(entry.getKey(), true);
															
														}else{
															hideTagTypeMap.put(entry.getKey(), false);
														}
														break;
													}
												}
									        }
											userDynamic.setHideTagTypeMap(hideTagTypeMap);
											
										}
										
										
										
									}
								}
								
								Integer topicContentUpdateMark = topicManage.query_cache_markUpdateTopicStatus(userDynamic.getTopicId(), Integer.parseInt(RandomStringUtils.randomNumeric(8)));
								
								//????????????'????????????'Id
								String processHideTagId = topicManage.createProcessHideTagId(userDynamic.getTopicId(),topicContentUpdateMark, visibleTagList);
								
								//??????????????????
								String content = topicManage.query_cache_processHiddenTag(topicInfo.getContent(),visibleTagList,processHideTagId+"|"+topicContentDigest_link+"|"+ topicContentDigest_video);
								userDynamic.setTopicContent(content);
								
								//?????????????????????????????????????????????????????????????????????????????????
								if(topicInfo.getIsStaff() == false && !topicInfo.getUserName().equals(accessUser.getUserName())){
									//?????????????????????????????????
									boolean flag = userRoleManage.isPermission(ResourceEnum._1001000,topicInfo.getTagId());
									if(!flag){//????????????????????????
										userDynamic.setTopicContent("");
									}
								}
								
							}
							
							
						}
					}
					

					if(userDynamic.getModule().equals(200)){//??????
						Comment comment = commentManage.query_cache_findByCommentId(userDynamic.getCommentId());
						if(comment != null){
							if(comment.getContent() != null && !"".equals(comment.getContent().trim())){
								//?????????????????????
								comment.setContent(fileManage.processRichTextFilePath(comment.getContent(),"comment"));
							}
							userDynamic.setCommentContent(comment.getContent());
						}
						
					}
					if(userDynamic.getModule().equals(300)){//????????????
						Comment comment = commentManage.query_cache_findByCommentId(userDynamic.getCommentId());
						if(comment != null){
							if(comment.getContent() != null && !"".equals(comment.getContent().trim())){
								//?????????????????????
								comment.setContent(fileManage.processRichTextFilePath(comment.getContent(),"comment"));
							}
							userDynamic.setCommentContent(comment.getContent());
						}
						Comment quoteComment = commentManage.query_cache_findByCommentId(userDynamic.getQuoteCommentId());
						if(quoteComment != null && quoteComment.getStatus().equals(20)){
							userDynamic.setQuoteCommentContent(textFilterManage.filterText(textFilterManage.specifyHtmlTagToText(quoteComment.getContent())));
						}
					}
					if(userDynamic.getModule().equals(400)){//??????
						Reply reply = commentManage.query_cache_findReplyByReplyId(userDynamic.getReplyId());
						if(reply != null){
							userDynamic.setReplyContent(reply.getContent());
						}
					}
					
					//??????
					if(userDynamic.getQuestionId() >0L){
						Question questionInfo = questionManage.query_cache_findById(userDynamic.getQuestionId());//????????????
						if(questionInfo != null){
							if(questionInfo.getContent() != null && !"".equals(questionInfo.getContent().trim())){
								//?????????????????????
								questionInfo.setContent(fileManage.processRichTextFilePath(questionInfo.getContent(),"question"));
							}
							userDynamic.setQuestionTitle(questionInfo.getTitle());
							userDynamic.setQuestionViewTotal(questionInfo.getViewTotal());
							userDynamic.setQuestionAnswerTotal(questionInfo.getAnswerTotal());
							userDynamic.setQuestionContent(questionInfo.getContent());
						}
						
					}
					if(userDynamic.getModule().equals(600)){//600.??????
						Answer answer = answerManage.query_cache_findByAnswerId(userDynamic.getAnswerId());
						if(answer != null){
							if(answer.getContent() != null && !"".equals(answer.getContent().trim())){
								//?????????????????????
								answer.setContent(fileManage.processRichTextFilePath(answer.getContent(),"answer"));
							}
							userDynamic.setAnswerContent(answer.getContent());
						}
						
					}
					if(userDynamic.getModule().equals(700)){//700.????????????
						AnswerReply answerReply = answerManage.query_cache_findReplyByReplyId(userDynamic.getAnswerReplyId());
						if(answerReply != null){
							userDynamic.setAnswerReplyContent(answerReply.getContent());
						}
					}
					
					
					
					
				}
			}

	  		//??????????????????????????????List
  			pageView.setQueryResult(qr);
	  		
	  	}
		
		if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(pageView), "json", response);
			return null;
		}else{
			model.addAttribute("pageView", pageView);
			return "templates/"+dirName+"/"+accessPath+"/userDynamicList";	
		}
	}
	
	/**
	 * ??????????????????
	 * @param model
	 * @param pageForm
	 * @param topicId ??????Id
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/topicLikeList",method=RequestMethod.GET) 
	public String topicLikeList(ModelMap model,PageForm pageForm,Long topicId,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		
		String dirName = templateService.findTemplateDir_cache();
		
		
		String accessPath = accessSourceDeviceManage.accessDevices(request);
	   
		//????????????????????????
		PageView<Like> pageView = new PageView<Like>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	  	if(topicId != null && topicId > 0L){
	  		Topic topicInfo = topicManage.queryTopicCache(topicId);//????????????
	  		if(topicInfo != null && topicInfo.getUserName().equals(accessUser.getUserName())){
	  			//?????????
	  			int firstIndex = (pageForm.getPage()-1)*pageView.getMaxresult();
	  			
	  			
	  			QueryResult<Like> qr = likeService.findLikePageByTopicId(firstIndex,pageView.getMaxresult(),topicId);
	  			if(qr != null && qr.getResultlist() != null && qr.getResultlist().size() >0){
	  				for(Like like : qr.getResultlist()){
	  					Topic topic = topicManage.queryTopicCache(like.getTopicId());//????????????
	  					if(topic != null){
	  						like.setTopicTitle(topic.getTitle());
	  					}
	  					User user = userManage.query_cache_findUserByUserName(like.getUserName());
	  					if(user != null && user.getCancelAccountTime().equals(-1L)){
  							like.setAccount(user.getAccount());
	  						like.setNickname(user.getNickname());
							if(user.getAvatarName() != null && !"".equals(user.getAvatarName().trim())){
								like.setAvatarPath(fileManage.fileServerAddress()+user.getAvatarPath());//????????????
								like.setAvatarName(user.getAvatarName());//????????????
							}
  						}else{
  							like.setUserName(null);
  						}
	  				}
	  			}
	  			//??????????????????????????????List
	  			pageView.setQueryResult(qr);
	  		}
	  	}
	  	
	  	
		
		
		if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(pageView), "json", response);
			return null;
		}else{
			model.addAttribute("pageView", pageView);
			return "templates/"+dirName+"/"+accessPath+"/topicLikeList";	
		}
	}
	
	
	/**
	 * ????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/likeList",method=RequestMethod.GET) 
	public String likeList(ModelMap model,PageForm pageForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		
		String dirName = templateService.findTemplateDir_cache();
		
		
		String accessPath = accessSourceDeviceManage.accessDevices(request);
	   
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	  	
	  	
		//????????????????????????
		PageView<Like> pageView = new PageView<Like>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		//?????????
		int firstIndex = (pageForm.getPage()-1)*pageView.getMaxresult();
		
		QueryResult<Like> qr = likeService.findLikeByUserId(accessUser.getUserId(),accessUser.getUserName(),firstIndex,pageView.getMaxresult());
		if(qr != null && qr.getResultlist() != null && qr.getResultlist().size() >0){
			for(Like like : qr.getResultlist()){
				Topic topic = topicManage.queryTopicCache(like.getTopicId());//????????????
				if(topic != null){
					like.setTopicTitle(topic.getTitle());
				}
			}
		}
		//??????????????????????????????List
		pageView.setQueryResult(qr);
		
		if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(pageView), "json", response);
			return null;
		}else{
			model.addAttribute("pageView", pageView);
			return "templates/"+dirName+"/"+accessPath+"/likeList";	
		}
	}
	
	
	/**
	 * ????????????
	 * @param model
	 * @param jumpUrl ????????????   ??????post??????????????????
	 * @param token ????????????
	 * @param likeId ??????Id
	 */
	@RequestMapping(value="/user/control/deleteLike", method=RequestMethod.POST)
	@RoleAnnotation(resourceCode=ResourceEnum._4002000)
	public String deleteLike(ModelMap model,String jumpUrl,String token,String likeId,
			RedirectAttributes redirectAttrs,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		Map<String,String> error = new HashMap<String,String>();//??????
		SystemSetting systemSetting = settingService.findSystemSetting_cache();
		if(systemSetting.getCloseSite().equals(2)){
			error.put("like", ErrorView._21.name());//?????????????????????????????????
		}
		
		//????????????
		if(token != null && !"".equals(token.trim())){	
			String token_sessionid = csrfTokenManage.getToken(request);//????????????
			if(token_sessionid != null && !"".equals(token_sessionid.trim())){
				if(!token_sessionid.equals(token)){
					error.put("token", ErrorView._13.name());
				}
			}else{
				error.put("token", ErrorView._12.name());
			}
		}else{
			error.put("token", ErrorView._11.name());
		}
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	  	Like like = null;
	  	//????????????Id
	  	String topicLikeId = null;
  		if(likeId == null || "".equals(likeId.trim())){
  			error.put("like", ErrorView._1730.name());//??????Id?????????
  		}else{
  			like  = likeService.findById(likeId.trim());
  			if(like != null){
  				if(like.getUserName().equals(accessUser.getUserName())){
  					topicLikeId = likeManage.createTopicLikeId(like.getTopicId(), accessUser.getUserId());
  				}else{
  					error.put("like", ErrorView._1760.name());//??????????????????????????????
  				}
  			}else{
  				error.put("like", ErrorView._1750.name());//???????????????
  			}
  		}
  		
		if(error.size() == 0){
			int i = likeService.deleteLike(likeId.trim(),topicLikeId);
			if(i == 0){
				error.put("like", ErrorView._1740.name());//??????????????????
			}
			//??????????????????
			likeManage.delete_cache_findTopicLikeById(topicLikeId);
			likeManage.delete_cache_findLikeCountByTopicId(like.getTopicId());
		}
		
		Map<String,String> returnError = new HashMap<String,String>();//??????
		if(error.size() >0){
			//???????????????????????????????????????
    		for (Map.Entry<String,String> entry : error.entrySet()) {
    			if(ErrorView.get(entry.getValue()) != null){
    				returnError.put(entry.getKey(),  ErrorView.get(entry.getValue()));
    			}else{
    				returnError.put(entry.getKey(),  entry.getValue());
    			}
    			
			}
		}
		
		if(isAjax == true){
    		Map<String,Object> returnValue = new HashMap<String,Object>();//?????????
    		
    		if(error != null && error.size() >0){
    			returnValue.put("success", "false");
    			returnValue.put("error", returnError);
    		}else{
    			returnValue.put("success", "true");
    			
    		}

    		WebUtil.writeToWeb(JsonUtils.toJSONString(returnValue), "json", response);
			return null;
		}else{
			String dirName = templateService.findTemplateDir_cache();
			String accessPath = accessSourceDeviceManage.accessDevices(request);
			if(error != null && error.size() >0){//???????????????
				
				for (Map.Entry<String,String> entry : returnError.entrySet()) {		 
					model.addAttribute("message",entry.getValue());//??????
		  			return "templates/"+dirName+"/"+accessPath+"/message";
				}
					
			}
			
			
			if(jumpUrl != null && !"".equals(jumpUrl.trim())){
				String url = Base64.decodeBase64URL(jumpUrl.trim());
				return "redirect:"+url;
			}else{//????????????
				model.addAttribute("message", "??????????????????");
				String referer = request.getHeader("referer");
				if(RefererCompare.compare(request, "login")){//???????????????????????????????????????
					referer = Configuration.getUrl(request);
				}
				model.addAttribute("urlAddress", referer);
				
				return "templates/"+dirName+"/"+accessPath+"/jump";	
			}
		}
		
	}
	/**
	 * ????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/followList",method=RequestMethod.GET) 
	public String followList(ModelMap model,PageForm pageForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		
		String dirName = templateService.findTemplateDir_cache();
		
		
		String accessPath = accessSourceDeviceManage.accessDevices(request);
	   
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	  	
	  	
		//????????????????????????
		PageView<Follow> pageView = new PageView<Follow>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		//?????????
		int firstIndex = (pageForm.getPage()-1)*pageView.getMaxresult();
		
		QueryResult<Follow> qr = followService.findFollowByUserName(accessUser.getUserId(),accessUser.getUserName(),firstIndex,pageView.getMaxresult());
		if(qr != null && qr.getResultlist() != null && qr.getResultlist().size() >0){
			for(Follow follow : qr.getResultlist()){
				User user = userManage.query_cache_findUserByUserName(follow.getFriendUserName());//????????????
				if(user != null && user.getCancelAccountTime().equals(-1L)){
					follow.setFriendAccount(user.getAccount());
					follow.setFriendNickname(user.getNickname());
					if(user.getAvatarName() != null && !"".equals(user.getAvatarName().trim())){
						follow.setFriendAvatarPath(fileManage.fileServerAddress()+user.getAvatarPath());
						follow.setFriendAvatarName(user.getAvatarName());
					}		
				}else{
					follow.setFriendUserName(null);
				}
			}
		}
		//??????????????????????????????List
		pageView.setQueryResult(qr);
		
		if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(pageView), "json", response);
			return null;
		}else{
			model.addAttribute("pageView", pageView);
			return "templates/"+dirName+"/"+accessPath+"/followList";	
		}
	}
	
	
	/**
	 * ????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/followerList",method=RequestMethod.GET) 
	public String followerList(ModelMap model,PageForm pageForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		
		String dirName = templateService.findTemplateDir_cache();
		
		
		String accessPath = accessSourceDeviceManage.accessDevices(request);
	   
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	  	
	  	
		//????????????????????????
		PageView<Follower> pageView = new PageView<Follower>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		//?????????
		int firstIndex = (pageForm.getPage()-1)*pageView.getMaxresult();
		
		QueryResult<Follower> qr = followService.findFollowerByUserName(accessUser.getUserId(),accessUser.getUserName(),firstIndex,pageView.getMaxresult());
		if(qr != null && qr.getResultlist() != null && qr.getResultlist().size() >0){
			for(Follower follower : qr.getResultlist()){
				User user = userManage.query_cache_findUserByUserName(follower.getFriendUserName());//????????????
				if(user != null && user.getCancelAccountTime().equals(-1L)){
					follower.setFriendAccount(user.getAccount());
					follower.setFriendNickname(user.getNickname());
					if(user.getAvatarName() != null && !"".equals(user.getAvatarName().trim())){
						follower.setFriendAvatarPath(fileManage.fileServerAddress()+user.getAvatarPath());
						follower.setFriendAvatarName(user.getAvatarName());
					}		
				}else{
					follower.setFriendUserName(null);
				}
			}
		}
		//??????????????????????????????List
		pageView.setQueryResult(qr);
		
		if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(pageView), "json", response);
			return null;
		}else{
			model.addAttribute("pageView", pageView);
			return "templates/"+dirName+"/"+accessPath+"/followerList";	
		}
	}
	
	/**
	 * ????????????
	 * @param model
	 * @param jumpUrl ????????????   ??????post??????????????????
	 * @param token ????????????
	 * @param followId ??????Id
	 */
	@RequestMapping(value="/user/control/deleteFollow", method=RequestMethod.POST)
	@RoleAnnotation(resourceCode=ResourceEnum._5002000)
	public String deleteFollow(ModelMap model,String jumpUrl,String token,String followId,
			RedirectAttributes redirectAttrs,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		Map<String,String> error = new HashMap<String,String>();//??????
		SystemSetting systemSetting = settingService.findSystemSetting_cache();
		if(systemSetting.getCloseSite().equals(2)){
			error.put("follow", ErrorView._21.name());//?????????????????????????????????
		}
		
		//????????????
		if(token != null && !"".equals(token.trim())){	
			String token_sessionid = csrfTokenManage.getToken(request);//????????????
			if(token_sessionid != null && !"".equals(token_sessionid.trim())){
				if(!token_sessionid.equals(token)){
					error.put("token", ErrorView._13.name());
				}
			}else{
				error.put("token", ErrorView._12.name());
			}
		}else{
			error.put("token", ErrorView._11.name());
		}
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	  	Follow follow = null;
	  	
	  	
	  	
  		if(followId == null || "".equals(followId.trim())){
  			error.put("followId", ErrorView._1830.name());//??????Id?????????
  		}else{
  			follow  = followService.findById(followId.trim());
  			if(follow != null){
  				if(!follow.getUserName().equals(accessUser.getUserName())){
  					error.put("follow", ErrorView._1860.name());//??????????????????????????????
  				}
  			}else{
  				error.put("follow", ErrorView._1850.name());//???????????????
  			}
  		}
  		
		if(error.size() == 0){
			String[] idGroup = followId.trim().split("-");
			//??????Id
		  	String followerId = idGroup[1]+"-"+idGroup[0];
			
			int i = followService.deleteFollow(followId.trim(),followerId);
			if(i == 0){
				error.put("follow", ErrorView._1840.name());//??????????????????
			}
			//??????????????????
			followManage.delete_cache_findById(followId.trim());
			followerManage.delete_cache_followerCount(follow.getFriendUserName());
			
			followManage.delete_cache_findAllFollow(accessUser.getUserName());
			followManage.delete_cache_followCount(accessUser.getUserName());
		}
		
		Map<String,String> returnError = new HashMap<String,String>();//??????
		if(error.size() >0){
			//???????????????????????????????????????
    		for (Map.Entry<String,String> entry : error.entrySet()) {
    			if(ErrorView.get(entry.getValue()) != null){
    				returnError.put(entry.getKey(),  ErrorView.get(entry.getValue()));
    			}else{
    				returnError.put(entry.getKey(),  entry.getValue());
    			}
    			
			}
		}
		
		if(isAjax == true){
    		Map<String,Object> returnValue = new HashMap<String,Object>();//?????????
    		
    		if(error != null && error.size() >0){
    			returnValue.put("success", "false");
    			returnValue.put("error", returnError);
    		}else{
    			returnValue.put("success", "true");
    			
    		}

    		WebUtil.writeToWeb(JsonUtils.toJSONString(returnValue), "json", response);
			return null;
		}else{
			String dirName = templateService.findTemplateDir_cache();
			String accessPath = accessSourceDeviceManage.accessDevices(request);
			if(error != null && error.size() >0){//???????????????
				
				for (Map.Entry<String,String> entry : returnError.entrySet()) {		 
					model.addAttribute("message",entry.getValue());//??????
		  			return "templates/"+dirName+"/"+accessPath+"/message";
				}
					
			}
			
			
			if(jumpUrl != null && !"".equals(jumpUrl.trim())){
				String url = Base64.decodeBase64URL(jumpUrl.trim());
				return "redirect:"+url;
			}else{//????????????
				model.addAttribute("message", "??????????????????");
				String referer = request.getHeader("referer");
				if(RefererCompare.compare(request, "login")){//???????????????????????????????????????
					referer = Configuration.getUrl(request);
				}
				model.addAttribute("urlAddress", referer);
				
				return "templates/"+dirName+"/"+accessPath+"/jump";	
			}
		}
		
	}
	
	/**
	 * ????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/balance",method=RequestMethod.GET) 
	public String balanceUI(ModelMap model,PageForm pageForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
	
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		Map<String,Object> returnValue = new HashMap<String,Object>();//?????????
		
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();

		
		User user = userService.findUserByUserName(accessUser.getUserName());

		model.addAttribute("deposit",user.getDeposit());//???????????????
		returnValue.put("deposit",user.getDeposit());
		if(user != null){
			//????????????????????????
			PageView<PaymentLog> pageView = new PageView<PaymentLog>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
			//?????????
			int firstIndex = (pageForm.getPage()-1)*pageView.getMaxresult();
			
			if(user != null){
				QueryResult<PaymentLog> qr =  paymentService.findPaymentLogPage(user.getId(),user.getUserName(),firstIndex, pageView.getMaxresult());
				
				//??????????????????????????????List
				pageView.setQueryResult(qr);
				model.addAttribute("pageView", pageView);
				returnValue.put("pageView", pageView);
			}
		}
		if(isAjax == true){
			WebUtil.writeToWeb(JsonUtils.toJSONString(returnValue), "json", response);
			return null;
		}else{
			String dirName = templateService.findTemplateDir_cache();
			
			String accessPath = accessSourceDeviceManage.accessDevices(request);
		   
			
			return "templates/"+dirName+"/"+accessPath+"/balance";	
		}
		
		
	}
	
	/**
	 * ?????????????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/membershipCardOrderList",method=RequestMethod.GET) 
	public String membershipCardOrderUI(ModelMap model,PageForm pageForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
	
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
			
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();

	
		//????????????????????????
		PageView<MembershipCardOrder> pageView = new PageView<MembershipCardOrder>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		//?????????
		int firstIndex = (pageForm.getPage()-1)*pageView.getMaxresult();
		
		
		QueryResult<MembershipCardOrder> qr =  membershipCardService.findMembershipCardOrderByUserName(accessUser.getUserName(),firstIndex, pageView.getMaxresult());
		
		//??????????????????????????????List
		pageView.setQueryResult(qr);
		model.addAttribute("pageView", pageView);
		
		
		if(isAjax == true){
			WebUtil.writeToWeb(JsonUtils.toJSONString(pageView), "json", response);
			return null;
		}else{
			String dirName = templateService.findTemplateDir_cache();
			
			String accessPath = accessSourceDeviceManage.accessDevices(request);
		   
			
			return "templates/"+dirName+"/"+accessPath+"/membershipCardOrderList";	
		}
		
		
	}
	
	
	
	
	/**
	 * ??????????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/questionList",method=RequestMethod.GET) 
	public String questionListUI(ModelMap model,PageForm pageForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		
		
		//????????????????????????
		PageView<Question> pageView = new PageView<Question>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		//?????????
		int firstindex = (pageForm.getPage()-1)*pageView.getMaxresult();
		
		 //??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
    	
    		
		StringBuffer jpql = new StringBuffer("");
		//???????????????
		List<Object> params = new ArrayList<Object>();
		jpql.append(" and o.userName=?"+ (params.size()+1));
		params.add(accessUser.getUserName());
		
		jpql.append(" and o.status<?"+ (params.size()+1));
		params.add(100);
		
		jpql.append(" and o.isStaff=?"+ (params.size()+1));
		params.add(false);
		
		//???????????????and
		String _jpql = org.apache.commons.lang3.StringUtils.difference(" and", jpql.toString());
		
		//??????
		LinkedHashMap<String,String> orderby = new LinkedHashMap<String,String>();
		
		orderby.put("id", "desc");//??????id??????????????????
	
		
		QueryResult<Question> qr = questionService.getScrollData(Question.class, firstindex, pageView.getMaxresult(),_jpql, params.toArray(),orderby);
		if(qr.getResultlist() != null && qr.getResultlist().size() >0){
			for(Question o :qr.getResultlist()){
    			o.setIpAddress(null);//IP???????????????
    		}
			
			List<QuestionTag> questionTagList = questionTagService.findAllQuestionTag_cache();
			
			if(questionTagList != null && questionTagList.size() >0){
				for(Question question : qr.getResultlist()){
					List<QuestionTagAssociation> questionTagAssociationList = questionManage.query_cache_findQuestionTagAssociationByQuestionId(question.getId());
					if(questionTagAssociationList != null && questionTagAssociationList.size() >0){
						for(QuestionTag questionTag : questionTagList){
							for(QuestionTagAssociation questionTagAssociation : questionTagAssociationList){
								if(questionTagAssociation.getQuestionTagId().equals(questionTag.getId())){
									questionTagAssociation.setQuestionTagName(questionTag.getName());
									question.addQuestionTagAssociation(questionTagAssociation);
									break;
								}
							}
						}
					}
				}
			}
			
			
			
			
		}
		
		//??????????????????????????????List
		pageView.setQueryResult(qr);		
    	
		model.addAttribute("pageView", pageView);
		
		if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(pageView), "json", response);
			return null;
		}else{
			String dirName = templateService.findTemplateDir_cache();
		    return "templates/"+dirName+"/"+accessSourceDeviceManage.accessDevices(request)+"/questionList";	
		}
	}
	
	
	
	
	/**
	 * ??????????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/answerList",method=RequestMethod.GET) 
	public String answerListUI(ModelMap model,PageForm pageForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		
		
		//????????????????????????
		PageView<Answer> pageView = new PageView<Answer>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		//?????????
		int firstindex = (pageForm.getPage()-1)*pageView.getMaxresult();
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
    		
		StringBuffer jpql = new StringBuffer("");
		//???????????????
		List<Object> params = new ArrayList<Object>();
		jpql.append(" and o.userName=?"+ (params.size()+1));
		params.add(accessUser.getUserName());
		
		jpql.append(" and o.isStaff=?"+ (params.size()+1));
		params.add(false);
		
		jpql.append(" and o.status<?"+ (params.size()+1));
		params.add(100);
		
		//???????????????and
		String _jpql = org.apache.commons.lang3.StringUtils.difference(" and", jpql.toString());
		
		//??????
		LinkedHashMap<String,String> orderby = new LinkedHashMap<String,String>();
		
		orderby.put("id", "desc");//??????id??????????????????
	
		
		QueryResult<Answer> qr = answerService.getScrollData(Answer.class, firstindex, pageView.getMaxresult(),_jpql, params.toArray(),orderby);
		if(qr.getResultlist() != null && qr.getResultlist().size() >0){
			List<Long> questionIdList = new ArrayList<Long>();
			for(Answer o :qr.getResultlist()){
    			o.setIpAddress(null);//IP???????????????
    			
    			o.setContent(textFilterManage.filterText(textFilterManage.specifyHtmlTagToText(o.getContent())));
    			if(!questionIdList.contains(o.getQuestionId())){
    				questionIdList.add(o.getQuestionId());
    			}
    		}
			List<Question> questionList = questionService.findTitleByIdList(questionIdList);
			if(questionList != null && questionList.size() >0){
				for(Answer o :qr.getResultlist()){
					for(Question question : questionList){
						if(question.getId().equals(o.getQuestionId())){
							o.setQuestionTitle(question.getTitle());
							break;
						}
					}
					
				}
			}	
		}
		
		//??????????????????????????????List
		pageView.setQueryResult(qr);		
    	
		model.addAttribute("pageView", pageView);
		
		if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(pageView), "json", response);
			return null;
		}else{
			String dirName = templateService.findTemplateDir_cache();
		    return "templates/"+dirName+"/"+accessSourceDeviceManage.accessDevices(request)+"/answerList";	
		}
	}
	
	/**
	 * ????????????????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/answerReplyList",method=RequestMethod.GET) 
	public String answerReplyListUI(ModelMap model,PageForm pageForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		
		
		//????????????????????????
		PageView<AnswerReply> pageView = new PageView<AnswerReply>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		//?????????
		int firstindex = (pageForm.getPage()-1)*pageView.getMaxresult();
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
    		
		StringBuffer jpql = new StringBuffer("");
		//???????????????
		List<Object> params = new ArrayList<Object>();
		jpql.append(" and o.userName=?"+ (params.size()+1));
		params.add(accessUser.getUserName());
		
		jpql.append(" and o.isStaff=?"+ (params.size()+1));
		params.add(false);
		
		jpql.append(" and o.status<?"+ (params.size()+1));
		params.add(100);
		
		//???????????????and
		String _jpql = org.apache.commons.lang3.StringUtils.difference(" and", jpql.toString());
		
		//??????
		LinkedHashMap<String,String> orderby = new LinkedHashMap<String,String>();
		
		orderby.put("id", "desc");//??????id??????????????????
	
		
		QueryResult<AnswerReply> qr = questionService.getScrollData(AnswerReply.class, firstindex, pageView.getMaxresult(),_jpql, params.toArray(),orderby);
		
		if(qr.getResultlist() != null && qr.getResultlist().size() >0){
			List<Long> questionIdList = new ArrayList<Long>();
			for(AnswerReply o :qr.getResultlist()){
    			o.setIpAddress(null);//IP???????????????
    			
    			if(!questionIdList.contains(o.getQuestionId())){
    				questionIdList.add(o.getQuestionId());
    			}
    		}
			List<Question> questionList = questionService.findTitleByIdList(questionIdList);
			if(questionList != null && questionList.size() >0){
				for(AnswerReply o :qr.getResultlist()){
					for(Question question : questionList){
						if(question.getId().equals(o.getQuestionId())){
							o.setQuestionTitle(question.getTitle());
							break;
						}
					}
					
				}
			}	
		}
		
		//??????????????????????????????List
		pageView.setQueryResult(qr);		
    	
		model.addAttribute("pageView", pageView);
		
		if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(pageView), "json", response);
			return null;
		}else{
			String dirName = templateService.findTemplateDir_cache();
		    return "templates/"+dirName+"/"+accessSourceDeviceManage.accessDevices(request)+"/answerReplyList";	
		}
	}
	
	
	
	
	/**
	 * ???????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/giveRedEnvelopeList",method=RequestMethod.GET) 
	public String giveRedEnvelopeListUI(ModelMap model,PageForm pageForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	  	
	  	
		//????????????????????????
		PageView<GiveRedEnvelope> pageView = new PageView<GiveRedEnvelope>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		//?????????
		int firstIndex = (pageForm.getPage()-1)*pageView.getMaxresult();
		
		
		StringBuffer jpql = new StringBuffer("");
		//???????????????
		List<Object> params = new ArrayList<Object>();

		
		jpql.append(" and o.userId=?"+ (params.size()+1));
		params.add(accessUser.getUserId());
		
		//???????????????and
		String _jpql = org.apache.commons.lang3.StringUtils.difference(" and", jpql.toString());
		
		
		//??????
		LinkedHashMap<String,String> orderby = new LinkedHashMap<String,String>();
		
		orderby.put("giveTime", "desc");//??????giveTime??????????????????
		QueryResult<GiveRedEnvelope> qr = redEnvelopeService.getScrollData(GiveRedEnvelope.class,firstIndex, pageView.getMaxresult(),_jpql, params.toArray(),orderby);
		
		if(qr.getResultlist() != null && qr.getResultlist().size() >0){
			for(GiveRedEnvelope giveRedEnvelope : qr.getResultlist()){
				if(giveRedEnvelope.getBindTopicId() != null && giveRedEnvelope.getBindTopicId() >0L){
					Topic topic = topicManage.queryTopicCache(giveRedEnvelope.getBindTopicId());//????????????
					if(topic != null){
						giveRedEnvelope.setBindTopicTitle(topic.getTitle());
					}
					
				}
				
			}
			
		}
		
		//??????????????????????????????List
		pageView.setQueryResult(qr);		
    	
		model.addAttribute("pageView", pageView);
		
		if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(pageView), "json", response);
			return null;
		}else{
			String dirName = templateService.findTemplateDir_cache();
		    return "templates/"+dirName+"/"+accessSourceDeviceManage.accessDevices(request)+"/giveRedEnvelopeList";	
		}
	}
	
	
	/**
	 * ???????????????????????????
	 * @param model
	 * @param pageForm
	 * @param giveRedEnvelopeId ?????????Id
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/redEnvelopeAmountDistributionList",method=RequestMethod.GET) 
	public String redEnvelopeAmountDistributionUI(ModelMap model,PageForm pageForm,String giveRedEnvelopeId,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		Map<String,Object> returnValue = new HashMap<String,Object>();//?????????
	    
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	  	
		//????????????????????????
		PageView<ReceiveRedEnvelope> pageView = new PageView<ReceiveRedEnvelope>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		//?????????
		int firstindex = (pageForm.getPage()-1)*pageView.getMaxresult();
		if(giveRedEnvelopeId != null && !"".equals(giveRedEnvelopeId.trim())){
			GiveRedEnvelope giveRedEnvelope = redEnvelopeService.findById(giveRedEnvelopeId);
			if(giveRedEnvelope != null && giveRedEnvelope.getUserId().equals(accessUser.getUserId())){
				//??????
				boolean sort = false;//true:?????? false:??????
				
				QueryResult<ReceiveRedEnvelope> qr = redEnvelopeManage.queryReceiveRedEnvelopeByCondition(giveRedEnvelope,false,firstindex, pageView.getMaxresult(),sort,false);
				    
				if(giveRedEnvelope.getBindTopicId() != null && giveRedEnvelope.getBindTopicId() >0L){
					Topic topic = topicManage.queryTopicCache(giveRedEnvelope.getBindTopicId());//????????????
					if(topic != null){
						giveRedEnvelope.setBindTopicTitle(topic.getTitle());
					}
					
				}
		
				//??????????????????????????????List
				pageView.setQueryResult(qr);
				model.addAttribute("pageView", pageView);	
				model.addAttribute("giveRedEnvelope", giveRedEnvelope);
				
				returnValue.put("pageView", pageView);
				returnValue.put("giveRedEnvelope", giveRedEnvelope);
			}
		}
		
		if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(returnValue), "json", response);
			return null;
		}else{
			String dirName = templateService.findTemplateDir_cache();
		    return "templates/"+dirName+"/"+accessSourceDeviceManage.accessDevices(request)+"/redEnvelopeAmountDistributionList";	
		}
	}
	
	
	/**
	 * ???????????????
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/user/control/receiveRedEnvelopeList",method=RequestMethod.GET) 
	public String receiveRedEnvelopeUI(ModelMap model,PageForm pageForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		
		//??????????????????
	  	AccessUser accessUser = AccessUserThreadLocal.get();
	  	
	  	
	  //????????????????????????
  		PageView<ReceiveRedEnvelope> pageView = new PageView<ReceiveRedEnvelope>(settingService.findSystemSetting_cache().getForestagePageNumber(),pageForm.getPage(),10,request.getRequestURI(),request.getQueryString());
		//?????????
  		int firstIndex = (pageForm.getPage()-1)*pageView.getMaxresult();
  		
  		
		QueryResult<ReceiveRedEnvelope> qr = redEnvelopeService.findReceiveRedEnvelopeByUserId(accessUser.getUserId(), firstIndex, pageView.getMaxresult());
		//??????????????????????????????List
		pageView.setQueryResult(qr);
		
		if(qr.getResultlist() != null && qr.getResultlist().size() >0){
			for(ReceiveRedEnvelope receiveRedEnvelope : qr.getResultlist()){
				User user = userManage.query_cache_findUserById(receiveRedEnvelope.getGiveUserId());
				if(user != null && user.getCancelAccountTime().equals(-1L)){
        			receiveRedEnvelope.setGiveNickname(user.getNickname());
        			receiveRedEnvelope.setGiveUserName(user.getUserName());
        			receiveRedEnvelope.setGiveAccount(user.getAccount());
        			if(user.getAvatarName() != null && !"".equals(user.getAvatarName().trim())){
        				receiveRedEnvelope.setGiveAvatarPath(fileManage.fileServerAddress()+user.getAvatarPath());
        				receiveRedEnvelope.setGiveAvatarName(user.getAvatarName());
        			}		
        		}
        		
        		//??????????????????????????????????????????
  				if(receiveRedEnvelope.getAmount() != null && receiveRedEnvelope.getAmount().compareTo(new BigDecimal("0")) ==0 && user != null){
  					GiveRedEnvelope giveRedEnvelope = redEnvelopeService.findById(receiveRedEnvelope.getGiveRedEnvelopeId());
  					if(giveRedEnvelope != null){
  						//????????????????????????????????????
      				    BigDecimal amount = redEnvelopeManage.queryReceiveRedEnvelopeAmount(giveRedEnvelope,user.getId());
      					if(amount != null && amount.compareTo(new BigDecimal("0")) >0 ){
      						redEnvelopeManage.unwrapRedEnvelope(receiveRedEnvelope,amount,user.getId(),user.getUserName());
      					}
  					}
  					
  				}
        		
			}
		}
  		
		
		
		model.addAttribute("pageView", pageView);
		
		if(isAjax){
			WebUtil.writeToWeb(JsonUtils.toJSONString(pageView), "json", response);
			return null;
		}else{
			String dirName = templateService.findTemplateDir_cache();
		    return "templates/"+dirName+"/"+accessSourceDeviceManage.accessDevices(request)+"/receiveRedEnvelopeList";	
		}
	}
	
}
