package cms.web.action.user;

import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import cms.bean.payment.PaymentLog;
import cms.bean.payment.PaymentVerificationLog;
import cms.bean.question.Answer;
import cms.bean.question.AnswerReply;
import cms.bean.question.Question;
import cms.bean.question.QuestionIndex;
import cms.bean.question.QuestionTag;
import cms.bean.question.QuestionTagAssociation;
import cms.bean.user.PointLog;

import com.fasterxml.jackson.core.type.TypeReference;

import cms.bean.PageForm;
import cms.bean.PageView;
import cms.bean.QueryResult;
import cms.bean.RequestResult;
import cms.bean.ResultCode;
import cms.bean.topic.Comment;
import cms.bean.topic.Reply;
import cms.bean.topic.Tag;
import cms.bean.topic.Topic;
import cms.bean.topic.TopicIndex;
import cms.bean.user.User;
import cms.bean.user.UserCustom;
import cms.bean.user.UserGrade;
import cms.bean.user.UserInputValue;
import cms.bean.user.UserRole;
import cms.bean.user.UserRoleGroup;
import cms.service.payment.PaymentService;
import cms.service.question.AnswerService;
import cms.service.question.QuestionIndexService;
import cms.service.question.QuestionService;
import cms.service.question.QuestionTagService;
import cms.service.setting.SettingService;
import cms.service.topic.CommentService;
import cms.service.topic.TagService;
import cms.service.topic.TopicIndexService;
import cms.service.topic.TopicService;
import cms.service.user.UserCustomService;
import cms.service.user.UserGradeService;
import cms.service.user.UserRoleService;
import cms.service.user.UserService;
import cms.utils.FileUtil;
import cms.utils.JsonUtils;
import cms.utils.SHA;
import cms.utils.UUIDUtil;
import cms.utils.Verification;
import cms.web.action.TextFilterManage;
import cms.web.action.fileSystem.FileManage;
import cms.web.action.lucene.TopicLuceneManage;
import cms.web.action.membershipCard.MembershipCardGiftTaskManage;
import cms.web.action.payment.PaymentManage;
import cms.web.action.question.QuestionManage;
import cms.web.action.topic.TopicManage;

/**
 * ????????????
 *
 */
@Controller
@RequestMapping("/control/user/manage") 
public class UserManageAction {
	private static final Logger logger = LogManager.getLogger(UserManageAction.class);
	
	
	@Resource(name="userServiceBean")
	private UserService userService;
	//????????????bean
	@Resource(name="userCustomServiceBean")
	private UserCustomService userCustomService;
	@Resource PointManage pointManage;
	@Resource SettingService settingService;
	
	@Resource(name = "userValidator") 
	private Validator validator; 
	@Resource UserManage userManage;
	
	@Resource UserGradeService userGradeService;
	
	@Resource TopicService topicService;
	@Resource CommentService commentService;
	@Resource TextFilterManage textFilterManage;
	@Resource TagService tagService;
	@Resource TopicManage topicManage;
	
	
	@Resource TopicLuceneManage topicLuceneManage;
	@Resource TopicIndexService topicIndexService;
	
	@Resource UserRoleService userRoleService;
	@Resource UserRoleManage userRoleManage;
	@Resource PaymentService paymentService;
	@Resource PaymentManage paymentManage;
	@Resource QuestionManage questionManage;
	
	@Resource QuestionService questionService;
	@Resource QuestionTagService questionTagService;
	@Resource AnswerService answerService;
	@Resource QuestionIndexService questionIndexService;
	@Resource FileManage fileManage;
	@Resource MessageSource messageSource;
	@Resource MembershipCardGiftTaskManage membershipCardGiftTaskManage;
	
	/**
	 * ???????????? ??????
	 */
	@ResponseBody
	@RequestMapping(params="method=show",method=RequestMethod.GET)
	public String show(ModelMap model2,Long id,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		//??????
		Map<String,String> error = new HashMap<String,String>();
		Map<String,Object> returnValue = new HashMap<String,Object>();
		
		if(id != null){
			User user = userService.findUserById(id);
			if(user != null){
				user.setPassword(null);//???????????????
				user.setAnswer(null);//???????????????????????????
				user.setSalt(null);//???????????????
				user.setAvatarPath(fileManage.fileServerAddress()+user.getAvatarPath());
				
				
				if(user.getType() >10){
					user.setPlatformUserId(userManage.platformUserIdToThirdPartyUserId(user.getPlatformUserId()));
				}
				
				
				List<UserGrade> userGradeList = userGradeService.findAllGrade();
				if(userGradeList != null && userGradeList.size() >0){
					for(UserGrade userGrade : userGradeList){//?????????????????? 
						if(user.getPoint() >= userGrade.getNeedPoint()){
							user.setGradeName(userGrade.getName());//?????????????????????????????????
							break;
						}
					} 
				}
				
				//?????????????????????
				List<UserRole> validUserRoleList = new ArrayList<UserRole>();
				
				//??????????????????
				List<UserRole> userRoleList = userRoleService.findAllRole();
				if(userRoleList != null && userRoleList.size() >0){
					List<UserRoleGroup> userRoleGroupList = userRoleService.findRoleGroupByUserName(user.getUserName());
					
					
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
				
				
				List<UserCustom> userCustomList = userCustomService.findAllUserCustom();
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
				
				returnValue.put("userRoleList", validUserRoleList);
				returnValue.put("userCustomList", userCustomList);
				returnValue.put("user",user);
			}else{
				error.put("user", "???????????????");
			}
		}else{
			error.put("id", "Id????????????");
		}
		
		
		if(error.size() >0){
			return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
		}else{
			return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,returnValue));
		}
	}
	
	
	
	/**
	 * ???????????? ??????????????????
	 */
	@ResponseBody
	@RequestMapping(params="method=add",method=RequestMethod.GET)
	public String addUI(ModelMap model,User user,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		Map<String,Object> returnValue = new HashMap<String,Object>();
		
		List<UserCustom> userCustomList = userCustomService.findAllUserCustom();
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
		
		
		//??????????????????
		List<UserRole> userRoleList = userRoleService.findAllRole();
		if(userRoleList != null && userRoleList.size() >0){
			for(UserRole userRole : userRoleList){
				if(userRole.getDefaultRole()){//?????????????????????
					userRole.setSelected(true);
				}else{
					//????????????  ???,???,???,???,???,???,??????    
	                DateTime defaultTime = new DateTime(2999, 1, 1, 0, 0);// 2999???1???1???0???0???
	                Date validPeriodEnd = defaultTime.toDate();
					userRole.setValidPeriodEnd(validPeriodEnd);
				}
			}
		}
		
		returnValue.put("userCustomList", userCustomList);
		returnValue.put("userRoleList", userRoleList);
		return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,returnValue));
	}
	
	
	/**
	 * ???????????? ????????????(?????????????????????)
	 * @param formbean
	 * @param userRolesId ??????Id
	 * @param result
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(params="method=add",method=RequestMethod.POST)
	public String add(User formbean,String[] userRolesId,BindingResult result,ModelMap model,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		//??????
		Map<String,Object> error = new HashMap<String,Object>();
		//????????????????????????????????????
		List<UserCustom> userCustomList = userCustomService.findAllUserCustom();
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
										error.put("userCustom_"+userCustom.getId(), "?????????????????????");
									}
								  break; 
								case 2 : //????????????
									if(Verification.isLetter(userCustom_value.trim()) == false){
										error.put("userCustom_"+userCustom.getId(), "?????????????????????");
									}
								  break;
								case 3 : //???????????????????????????
									if(Verification.isNumericLetters(userCustom_value.trim()) == false){
										error.put("userCustom_"+userCustom.getId(), "??????????????????????????????");
									}
								  break;
								case 4 : //??????????????????
									if(Verification.isChineseCharacter(userCustom_value.trim()) == false){
										error.put("userCustom_"+userCustom.getId(), "?????????????????????");
									}
								  break;
								case 5 : //?????????????????????
									if(userCustom_value.trim().matches(userCustom.getRegular())== false){
										error.put("userCustom_"+userCustom.getId(), "????????????");
									}
								  break;
							//	default:
							}
						}else{
							if(userCustom.isRequired() == true){//????????????	
								error.put("userCustom_"+userCustom.getId(), "?????????");
							}
							
						}	
						userCustom.setUserInputValueList(userInputValueList);
					}else if(userCustom.getChooseType().equals(2)){//2.????????????
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
									error.put("userCustom_"+userCustom.getId(), "?????????");
								}
							}
							
						}else{
							if(userCustom.isRequired() == true){//????????????	
								error.put("userCustom_"+userCustom.getId(), "?????????");
							}
						}
						userCustom.setUserInputValueList(userInputValueList);	
						
					}else if(userCustom.getChooseType().equals(3)){//3.????????????
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
								error.put("userCustom_"+userCustom.getId(), "?????????");
							}
						}
						if(userInputValueList.size() == 0){
							if(userCustom.isRequired() == true){//????????????	
								error.put("userCustom_"+userCustom.getId(), "?????????");
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
								error.put("userCustom_"+userCustom.getId(), "?????????");
							}
						}
						if(userInputValueList.size() == 0){
							if(userCustom.isRequired() == true){//????????????	
								error.put("userCustom_"+userCustom.getId(), "?????????");
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
								error.put("userCustom_"+userCustom.getId(), "?????????");
							}
						}
						userCustom.setUserInputValueList(userInputValueList);
					}
				}
			}
		}

		List<UserRoleGroup> userRoleGroupList = new ArrayList<UserRoleGroup>();
		List<UserRole> userRoleList = userRoleService.findAllRole();
		if(userRolesId != null && userRolesId.length >0){
			
			if(userRoleList != null && userRoleList.size() >0){
				for(String rolesId : userRolesId){
					if(rolesId != null && !"".equals(rolesId.trim())){
						for(UserRole userRole : userRoleList){
							userRole.setSelected(true);//??????????????????
							if(!userRole.getDefaultRole() && userRole.getId().equals(rolesId.trim())){//?????????????????????
								//????????????  ???,???,???,???,???,???,??????    
				                DateTime defaultTime = new DateTime(2999, 1, 1, 0, 0);// 2999???1???1???0???0???
				                Date validPeriodEnd = defaultTime.toDate();
				                
				                String validPeriodEnd_str = request.getParameter("validPeriodEnd_"+userRole.getId());
								
								if(validPeriodEnd_str != null && !"".equals(validPeriodEnd_str.trim())){
									boolean verification = Verification.isTime_minute(validPeriodEnd_str.trim());
									if(verification){
										DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");  
						                //????????????    
						                DateTime dateTime = DateTime.parse(validPeriodEnd_str.trim(), format);
						                validPeriodEnd = dateTime.toDate();
									}else{
										validPeriodEnd = null;
										error.put("validPeriodEnd_"+userRole.getId(), "????????????????????????");
									}
								}
								
								userRole.setValidPeriodEnd(validPeriodEnd);//??????????????????
								UserRoleGroup userRoleGroup = new UserRoleGroup();
								userRoleGroup.setUserName(formbean.getUserName() != null ?formbean.getUserName().trim() :formbean.getUserName());
								userRoleGroup.setUserRoleId(userRole.getId());
								userRoleGroup.setValidPeriodEnd(validPeriodEnd);
								userRoleGroupList.add(userRoleGroup);
							}
						}
					}
				}
				
				
			}
		}
		
		
		//????????????
		this.validator.validate(formbean, result); 
		if (result.hasErrors()) { 
			List<FieldError> fieldErrorList = result.getFieldErrors();
			if(fieldErrorList != null && fieldErrorList.size() >0){
				for(FieldError fieldError : fieldErrorList){
					error.put(fieldError.getField(), messageSource.getMessage(fieldError, null));
				}
			}
		}
		if(error.size() == 0){
			User user = new User();
			
			if(formbean.getType().equals(10)){//10:????????????????????????
				user.setAccount(formbean.getAccount().trim());
				user.setUserName(UUIDUtil.getUUID22());
				user.setIssue(formbean.getIssue().trim());
				//?????????????????????  ????????????????????????sha256  ??????sha256??????
				user.setAnswer(SHA.sha256Hex(SHA.sha256Hex(formbean.getAnswer().trim())));
				user.setPlatformUserId(user.getUserName());
			}else if(formbean.getType().equals(20)){//20: ????????????
				String id = UUIDUtil.getUUID22();
				user.setUserName(id);//???????????????
				user.setAccount(userManage.queryUserIdentifier(20)+"-"+id);//???????????????????????????????????????UUID
				user.setPlatformUserId(userManage.thirdPartyUserIdToPlatformUserId(formbean.getMobile().trim(),20));
			}
			
			
			user.setSalt(UUIDUtil.getUUID32());
			
			if(formbean.getNickname() != null && !"".equals(formbean.getNickname().trim())){
				user.setNickname(formbean.getNickname().trim());
			}
			
			//??????
			user.setPassword(SHA.sha256Hex(SHA.sha256Hex(formbean.getPassword().trim())+"["+user.getSalt()+"]"));
			user.setEmail(formbean.getEmail().trim());
			

			user.setRegistrationDate(new Date());
			user.setRemarks(formbean.getRemarks());
			user.setState(formbean.getState());
			
			user.setMobile(formbean.getMobile().trim());
			user.setRealNameAuthentication(formbean.isRealNameAuthentication());
			//????????????????????????
			user.setAllowUserDynamic(formbean.getAllowUserDynamic());
			user.setSecurityDigest(new Date().getTime());
			user.setType(formbean.getType());
			
			//???????????????????????????????????????????????????
			List<UserInputValue> all_userInputValueList = new ArrayList<UserInputValue>();
		
			if(userCustomList != null && userCustomList.size() >0){	
				for(UserCustom userCustom : userCustomList){
					all_userInputValueList.addAll(userCustom.getUserInputValueList());
				}
			}

			try {
				userService.saveUser(user,all_userInputValueList,userRoleGroupList);
			} catch (Exception e) {
				error.put("user", "??????????????????");
			//	e.printStackTrace();
			}
			//????????????
			if(user.getId() != null){
				userManage.delete_cache_findUserById(user.getId());
			}
			userManage.delete_cache_findUserByUserName(user.getUserName());
			userRoleManage.delete_cache_findRoleGroupByUserName(user.getUserName());
		}
		
		
		if(error.size() >0){
			return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
		}else{
			return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,null));
		}
	}
	
	/**
	 * ???????????? ??????????????????
	 */
	@ResponseBody
	@RequestMapping(params="method=edit",method=RequestMethod.GET)
	public String editUI(User formbean,ModelMap model,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		//??????
		Map<String,String> error = new HashMap<String,String>();
		Map<String,Object> returnValue = new HashMap<String,Object>();
				
		if(formbean.getId() != null){
			User user = userService.findUserById(formbean.getId());
			if(user != null){
				user.setPassword(null);//???????????????
				user.setAnswer(null);//???????????????????????????
				user.setSalt(null);//???????????????
				
				List<UserCustom> userCustomList = userCustomService.findAllUserCustom();
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
				//??????????????????
				List<UserRole> userRoleList = userRoleService.findAllRole();
				if(userRoleList != null && userRoleList.size() >0){
					List<UserRoleGroup> userRoleGroupList = userRoleService.findRoleGroupByUserName(user.getUserName());
					
					
					for(UserRole userRole : userRoleList){
						if(userRole.getDefaultRole()){//?????????????????????
							userRole.setSelected(true);
						}else{
							//????????????  ???,???,???,???,???,???,??????    
			                DateTime defaultTime = new DateTime(2999, 1, 1, 0, 0);// 2999???1???1???0???0???
			                Date validPeriodEnd = defaultTime.toDate();
							userRole.setValidPeriodEnd(validPeriodEnd);
						}
						
						if(userRoleGroupList != null && userRoleGroupList.size() >0){
							for(UserRoleGroup userRoleGroup : userRoleGroupList){
								if(userRole.getId().equals(userRoleGroup.getUserRoleId())){
									userRole.setSelected(true);
									userRole.setValidPeriodEnd(userRoleGroup.getValidPeriodEnd());
								}
							}
						}
					}
				}
				returnValue.put("userRoleList", userRoleList);
				
				returnValue.put("userCustomList", userCustomList);
			
				
				
				returnValue.put("user",user);
				
			}else{
				error.put("id", "???????????????");
			}
			
			
		}else{
			error.put("id", "??????Id????????????");
		}
		
		if(error.size() >0){
			return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
		}else{
			return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,returnValue));
		}	
	}
	/**
	 * ???????????? ??????
	 * @param formbean
	 * @param userRolesId ??????Id
	 * @param model
	 * @param pageForm
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(params="method=edit",method=RequestMethod.POST)
	public String edit(User formbean,String[] userRolesId,ModelMap model,PageForm pageForm,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		//??????
		Map<String,String> error = new HashMap<String,String>();
		User user = null;
		if(formbean.getId() != null){
			user = userService.findUserById(formbean.getId());
			if(user != null){
				if(!user.getUserVersion().equals(formbean.getUserVersion())){
					error.put("user", "????????????????????????");
				}
			}else{
				error.put("id", "???????????????");
			}
			
		}else{
			error.put("id", "Id????????????");
		}
		
		if(error.size() ==0){
			User new_user = new User();
			
			
			
			List<UserCustom> userCustomList = userCustomService.findAllUserCustom();
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
											error.put("userCustom_"+userCustom.getId(), "?????????????????????");
										}
									  break; 
									case 2 : //????????????
										if(Verification.isLetter(userCustom_value.trim()) == false){
											error.put("userCustom_"+userCustom.getId(), "?????????????????????");
										}
									  break;
									case 3 : //???????????????????????????
										if(Verification.isNumericLetters(userCustom_value.trim()) == false){
											error.put("userCustom_"+userCustom.getId(), "??????????????????????????????");
										}
									  break;
									case 4 : //??????????????????
										if(Verification.isChineseCharacter(userCustom_value.trim()) == false){
											error.put("userCustom_"+userCustom.getId(), "?????????????????????");
										}
									  break;
									case 5 : //?????????????????????
										if(userCustom_value.trim().matches(userCustom.getRegular())== false){
											error.put("userCustom_"+userCustom.getId(), "????????????");
										}
									  break;
								//	default:
								}
							}else{
								if(userCustom.isRequired() == true && user.getType() <=30){//????????????	
									error.put("userCustom_"+userCustom.getId(), "?????????");
								}
								
							}	
							userCustom.setUserInputValueList(userInputValueList);
						}else if(userCustom.getChooseType().equals(2)){//2.????????????
							String userCustom_value = request.getParameter("userCustom_"+userCustom.getId());
							
							if(userCustom_value != null && !"".equals(userCustom_value.trim())){
								
								String itemValue = userCustom.getItemValue().get(userCustom_value.trim());
								if(itemValue != null ){
									UserInputValue userInputValue = new UserInputValue();
									userInputValue.setUserCustomId(userCustom.getId());
									userInputValue.setOptions(userCustom_value.trim());
									userInputValueList.add(userInputValue);
									
								}else{
									if(userCustom.isRequired() == true && user.getType() <=30){//????????????	
										error.put("userCustom_"+userCustom.getId(), "?????????");
									}
								}
								
							}else{
								if(userCustom.isRequired() == true && user.getType() <=30){//????????????	
									error.put("userCustom_"+userCustom.getId(), "?????????");
								}
							}
							userCustom.setUserInputValueList(userInputValueList);	
							
						}else if(userCustom.getChooseType().equals(3)){//3.????????????
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
								if(userCustom.isRequired() == true && user.getType() <=30){//????????????	
									error.put("userCustom_"+userCustom.getId(), "?????????");
								}
							}
							if(userInputValueList.size() == 0){
								if(userCustom.isRequired() == true && user.getType() <=30){//????????????	
									error.put("userCustom_"+userCustom.getId(), "?????????");
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
								if(userCustom.isRequired() == true && user.getType() <=30){//????????????	
									error.put("userCustom_"+userCustom.getId(), "?????????");
								}
							}
							if(userInputValueList.size() == 0){
								if(userCustom.isRequired() == true && user.getType() <=30){//????????????	
									error.put("userCustom_"+userCustom.getId(), "?????????");
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
								if(userCustom.isRequired() == true && user.getType() <=30){//????????????	
									error.put("userCustom_"+userCustom.getId(), "?????????");
								}
							}
							userCustom.setUserInputValueList(userInputValueList);
						}
					}
				}
			}
			
			
			//????????????
			if(formbean.getNickname() != null && !"".equals(formbean.getNickname().trim())){
				if(formbean.getNickname().length()>15){
					error.put("nickname", "??????????????????15?????????");
				}
				
				
				User u = userService.findUserByNickname(formbean.getNickname().trim());
				if(u != null){
					if(user.getNickname() == null || "".equals(user.getNickname()) || !formbean.getNickname().trim().equals(user.getNickname())){
						error.put("nickname", "??????????????????");
					}
					
				}
				new_user.setNickname(formbean.getNickname().trim());
			}else{
				new_user.setNickname(null);
			}
			if(user.getType() <=30){//????????????????????????????????????
				if(formbean.getPassword() != null && !"".equals(formbean.getPassword().trim())){//??????
					if(formbean.getPassword().length()>30){
						error.put("password", "??????????????????30?????????");
					}
					//??????
					new_user.setPassword(SHA.sha256Hex(SHA.sha256Hex(formbean.getPassword().trim())+"["+user.getSalt()+"]"));
					new_user.setSecurityDigest(new Date().getTime());
				}else{
					new_user.setPassword(user.getPassword());
					new_user.setSecurityDigest(user.getSecurityDigest());
				}
				
				if(user.getType().equals(10)){
					if(formbean.getIssue() != null && !"".equals(formbean.getIssue().trim())){//??????????????????
						if(formbean.getIssue().length()>50){
							error.put("issue", "??????????????????????????????50?????????");
						}
						new_user.setIssue(formbean.getIssue().trim());
					}else{
						error.put("issue", "??????????????????????????????");
					}
					if(formbean.getAnswer() != null && !"".equals(formbean.getAnswer().trim())){//??????????????????
						if(formbean.getAnswer().length()>50){
							error.put("answer", "??????????????????????????????50?????????");
						}
						//?????????????????????  ????????????????????????sha256  ??????sha256??????
						new_user.setAnswer(SHA.sha256Hex(SHA.sha256Hex(formbean.getAnswer().trim())));
					}else{
						new_user.setAnswer(user.getAnswer());
					}
					
				}
			}else{
				new_user.setPassword(user.getPassword());
				if(user.getSecurityDigest() != null && !"".equals(user.getSecurityDigest())){
					new_user.setSecurityDigest(user.getSecurityDigest());
				}else{
					new_user.setSecurityDigest(new Date().getTime());
				}
				
				new_user.setIssue(user.getIssue());
				new_user.setAnswer(user.getAnswer());
			}
			
			if(formbean.getEmail() != null && !"".equals(formbean.getEmail().trim())){//??????
				if(Verification.isEmail(formbean.getEmail().trim()) == false){
					error.put("email", "Email???????????????");
				}
				if(formbean.getEmail().trim().length()>60){
					error.put("email", "Email??????????????????60?????????");
				}
				new_user.setEmail(formbean.getEmail().trim());
			}
			
			//????????????Id
			new_user.setPlatformUserId(user.getPlatformUserId());
			if(user.getType().equals(10)){//10:????????????????????????
				//??????
				if(formbean.getMobile() != null && !"".equals(formbean.getMobile().trim())){
			    	if(formbean.getMobile().trim().length() >18){
						error.put("mobile", "??????????????????");
					}else{
						boolean mobile_verification = Verification.isPositiveInteger(formbean.getMobile().trim());//?????????
						if(!mobile_verification){
							error.put("mobile", "?????????????????????");
						}else{
							new_user.setMobile(formbean.getMobile().trim());
						}
					}
			    }
				
			}else if(user.getType().equals(20)){//20: ????????????
				//??????
				if(formbean.getMobile() != null && !"".equals(formbean.getMobile().trim())){
			    	if(formbean.getMobile().trim().length() >18){
						error.put("mobile", "??????????????????");
					}else{
						boolean mobile_verification = Verification.isPositiveInteger(formbean.getMobile().trim());//?????????
						if(!mobile_verification){
							error.put("mobile", "?????????????????????");
						}else{
							
							if(!user.getMobile().equals(formbean.getMobile().trim())){
								String platformUserId = userManage.thirdPartyUserIdToPlatformUserId(formbean.getMobile().trim(),20);
								User mobile_user = userService.findUserByPlatformUserId(platformUserId);
								
					      		if(mobile_user != null){
					      			error.put("mobile", "?????????????????????");

					      		}
							}
							
							new_user.setPlatformUserId(userManage.thirdPartyUserIdToPlatformUserId(formbean.getMobile().trim(),20));
							new_user.setMobile(formbean.getMobile().trim());
						}
					}
			    }else{
			    	error.put("mobile", "????????????????????????");
			    }
			}
			
			
			
			//????????????
			new_user.setRealNameAuthentication(formbean.isRealNameAuthentication());
			//????????????????????????
			new_user.setAllowUserDynamic(formbean.getAllowUserDynamic());

			//????????????
			if(formbean.getState() == null){
				error.put("state", "????????????????????????");
			}else{
				if(formbean.getState() >2 || formbean.getState() <1){
					error.put("state", "??????????????????");
				}
				new_user.setState(formbean.getState());
			}
			
			
			List<UserRoleGroup> userRoleGroupList = new ArrayList<UserRoleGroup>();
			List<UserRole> userRoleList = userRoleService.findAllRole();
			if(userRolesId != null && userRolesId.length >0){
				
				if(userRoleList != null && userRoleList.size() >0){
					for(String rolesId : userRolesId){
						if(rolesId != null && !"".equals(rolesId.trim())){
							for(UserRole userRole : userRoleList){
								if(!userRole.getDefaultRole() && userRole.getId().equals(rolesId.trim())){//?????????????????????
									//????????????  ???,???,???,???,???,???,??????    
					                DateTime defaultTime = new DateTime(2999, 1, 1, 0, 0);// 2999???1???1???0???0???
					                Date validPeriodEnd = defaultTime.toDate();
					                
					                String validPeriodEnd_str = request.getParameter("validPeriodEnd_"+userRole.getId());
									
									if(validPeriodEnd_str != null && !"".equals(validPeriodEnd_str.trim())){
										boolean verification = Verification.isTime_minute(validPeriodEnd_str.trim());
										if(verification){
											DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");  
							                //????????????    
							                DateTime dateTime = DateTime.parse(validPeriodEnd_str.trim(), format);
							                validPeriodEnd = dateTime.toDate();
										}else{
											validPeriodEnd = null;
											error.put("validPeriodEnd_"+userRole.getId(), "????????????????????????");
										}
									}
									
									UserRoleGroup userRoleGroup = new UserRoleGroup();
									userRoleGroup.setUserName(user.getUserName());
									userRoleGroup.setUserRoleId(userRole.getId());
									userRoleGroup.setValidPeriodEnd(validPeriodEnd);
									userRoleGroupList.add(userRoleGroup);
								}
							}
						}
					}
					
					
				}
			}
			
			
			
			
			new_user.setId(user.getId());
			new_user.setUserName(user.getUserName());
			//??????
			new_user.setRemarks(formbean.getRemarks());
			new_user.setUserVersion(formbean.getUserVersion());
			if(error.size() ==0){
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
				
				userService.updateUser(new_user,add_userInputValue,delete_userInputValueIdList,userRoleGroupList);

				userManage.delete_userState(new_user.getUserName());
				
				//????????????
				userManage.delete_cache_findUserById(user.getId());
				userManage.delete_cache_findUserByUserName(user.getUserName());
				userRoleManage.delete_cache_findRoleGroupByUserName(user.getUserName());
			}
			
			
		}
		
		
		
		if(error.size() >0){
			return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
		}else{
			return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,null));
		}	
	}
	
	/**
	 * ??????  ??????
	 * @param userId ??????Id??????
	 */
	@ResponseBody
	@RequestMapping(params="method=delete",method=RequestMethod.POST)
	public String delete(ModelMap model,Long[] userId,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		//??????
		Map<String,String> error = new HashMap<String,String>();
		if(userId != null && userId.length >0){
			List<Long> idList = new ArrayList<Long>();
			List<Long> softDelete_userIdList = new ArrayList<Long>();//??????????????????Id??????
			List<User> softDelete_userList = new ArrayList<User>();//????????????????????????
			List<Long> physicalDelete_userIdList = new ArrayList<Long>();//??????????????????Id??????
			List<String> physicalDelete_userNameList = new ArrayList<String>();//??????????????????????????????
			List<User> physicalDelete_userList = new ArrayList<User>();//????????????????????????
			for(Long l :userId){
				if(l != null){
					idList.add(l);
				}
			}
			if(idList != null && idList.size() >0){
				List<User> userList = userService.findUserByUserIdList(idList);
				if(userList != null && userList.size() >0){
					for(User user : userList){		
						if(user.getState() <10){
							softDelete_userIdList.add(user.getId());
							softDelete_userList.add(user);
						}else{
							physicalDelete_userIdList.add(user.getId());
							physicalDelete_userNameList.add(user.getUserName());
							physicalDelete_userList.add(user);
						}
					}
					
					
					if(softDelete_userIdList.size() >0){//????????????
						int i = userService.markDelete(softDelete_userIdList);
						//????????????????????????
						for(User user : softDelete_userList){
							userManage.delete_userState(user.getUserName());
							//????????????
							userManage.delete_cache_findUserById(user.getId());
							userManage.delete_cache_findUserByUserName(user.getUserName());
							userRoleManage.delete_cache_findRoleGroupByUserName(user.getUserName());
						}
					}
					if(physicalDelete_userNameList.size() >0){//????????????
						for(User user : physicalDelete_userList){
							//????????????????????????
							topicManage.deleteTopicFile(user.getUserName(), false);
							
							//??????????????????
							topicManage.deleteCommentFile(user.getUserName(), false);
							
							//????????????????????????
							questionManage.deleteQuestionFile(user.getUserName(), false);
							
							//??????????????????
							questionManage.deleteAnswerFile(user.getUserName(), false);

							if(user.getAvatarName() != null && !"".equals(user.getAvatarName().trim())){
								DateTime dateTime = new DateTime(user.getRegistrationDate());     
								String date = dateTime.toString("yyyy-MM-dd");
								
								String pathFile = "file"+File.separator+"avatar"+File.separator + date +File.separator  +user.getAvatarName();
								//????????????
								fileManage.deleteFile(pathFile);
								
								String pathFile_100 = "file"+File.separator+"avatar"+File.separator + date +File.separator +"100x100" +File.separator+user.getAvatarName();
								//????????????100*100
								fileManage.deleteFile(pathFile_100);
							}
						}
						
						
						
						int i = userService.delete(physicalDelete_userIdList,physicalDelete_userNameList);
						
						for(User user : physicalDelete_userList){
							//????????????????????????
							topicIndexService.addTopicIndex(new TopicIndex(user.getUserName(),4));
							questionIndexService.addQuestionIndex(new QuestionIndex(user.getUserName(),4));
							
							//????????????????????????
							userManage.delete_userState(user.getUserName());
							//????????????
							userManage.delete_cache_findUserById(user.getId());
							userManage.delete_cache_findUserByUserName(user.getUserName());
							userRoleManage.delete_cache_findRoleGroupByUserName(user.getUserName());
							
						}

					}
					
				}
				
	
			}else{
				error.put("userId", "???????????????");
			}
		}else{
			error.put("userId", "??????Id????????????");
		}
		
		if(error.size() >0){
			return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
		}
		return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,null));
		
	}
	/**
	 * ??????
	 * @param model
	 * @param userId ??????Id??????
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(params="method=reduction",method=RequestMethod.POST)
	public String reduction(ModelMap model,Long[] userId,
			HttpServletResponse response) throws Exception {
		
		//??????
		Map<String,String> error = new HashMap<String,String>();
		if(userId != null && userId.length>0){
			
			List<User> userList = userService.findUserByUserIdList(Arrays.asList(userId));
			if(userList != null && userList.size() >0){
				
				for(User user :userList){
					if(user.getState().equals(11)){ //1:????????????   2:????????????   11: ??????????????????   12: ??????????????????
						user.setState(1);
					}else if(user.getState().equals(12)){
						user.setState(2);
					}
					
				}
				userService.reductionUser(userList);
				
				//????????????????????????
				for(User user :userList){
					userManage.delete_userState(user.getUserName());
					//????????????
					userManage.delete_cache_findUserById(user.getId());
					userManage.delete_cache_findUserByUserName(user.getUserName());
					userRoleManage.delete_cache_findRoleGroupByUserName(user.getUserName());
				}
				
				
				return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,null));
			}else{
				error.put("userId", "???????????????");
			}
		}else{
			error.put("userId", "??????Id????????????");
		}
		return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
	}
	

	
	/**
	 * ???????????????
	 * 
	 * @param pageForm
	 * @param model
	 * @param userName ????????????
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(params="method=allTopic",method=RequestMethod.GET)
	public String allTopic(PageForm pageForm,ModelMap model,String userName,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		//??????
		Map<String,String> error = new HashMap<String,String>();
		
		if(userName != null && !"".equals(userName.trim())){
			StringBuffer jpql = new StringBuffer("");
			//???????????????
			List<Object> params = new ArrayList<Object>();

			jpql.append(" and o.userName=?"+ (params.size()+1));
			params.add(userName.trim());
			
			//???????????????and
			String _jpql = org.apache.commons.lang3.StringUtils.difference(" and", jpql.toString());
			
			PageView<Topic> pageView = new PageView<Topic>(settingService.findSystemSetting_cache().getBackstagePageNumber(),pageForm.getPage(),10);
			//?????????
			int firstindex = (pageForm.getPage()-1)*pageView.getMaxresult();;	
			//??????
			LinkedHashMap<String,String> orderby = new LinkedHashMap<String,String>();
			
			orderby.put("id", "desc");//??????id??????????????????
			
			
			//?????????????????????
			QueryResult<Topic> qr = topicService.getScrollData(Topic.class, firstindex, pageView.getMaxresult(), _jpql, params.toArray(),orderby);
			if(qr != null && qr.getResultlist() != null && qr.getResultlist().size() >0){
				List<Tag> tagList = tagService.findAllTag();
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
				
				User user = null;
				for(Topic topic : qr.getResultlist()){
					if(user == null){
						user = userManage.query_cache_findUserByUserName(topic.getUserName());
					}
					if(user != null){
						topic.setAccount(user.getAccount());
						topic.setNickname(user.getNickname());
						if(user.getAvatarName() != null && !"".equals(user.getAvatarName().trim())){
							topic.setAvatarPath(fileManage.fileServerAddress()+user.getAvatarPath());
							topic.setAvatarName(user.getAvatarName());
						}		
					}
				}
				
			}

			pageView.setQueryResult(qr);
			
			return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,pageView));
		}else{
			error.put("userName", "????????????????????????");
		}
		
		
		return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
	}
	
	/**
	 * ???????????????
	 * @param pageForm
	 * @param model
	 * @param userName ????????????
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(params="method=allComment",method=RequestMethod.GET)
	public String allComment(PageForm pageForm,ModelMap model,String userName,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		//??????
		Map<String,String> error = new HashMap<String,String>();
		if(userName != null && !"".equals(userName.trim())){
			StringBuffer jpql = new StringBuffer("");
			//???????????????
			List<Object> params = new ArrayList<Object>();

			jpql.append(" and o.userName=?"+ (params.size()+1));
			params.add(userName.trim());
			
			//???????????????and
			String _jpql = org.apache.commons.lang3.StringUtils.difference(" and", jpql.toString());
			
			PageView<Comment> pageView = new PageView<Comment>(settingService.findSystemSetting_cache().getBackstagePageNumber(),pageForm.getPage(),10);
			//?????????
			int firstindex = (pageForm.getPage()-1)*pageView.getMaxresult();;	
			//??????
			LinkedHashMap<String,String> orderby = new LinkedHashMap<String,String>();
			
			orderby.put("id", "desc");//??????id??????????????????
			
			
			//?????????????????????
			QueryResult<Comment> qr = commentService.getScrollData(Comment.class, firstindex, pageView.getMaxresult(), _jpql, params.toArray(),orderby);
			if(qr != null && qr.getResultlist() != null && qr.getResultlist().size() >0){
				List<Long> topicIdList = new ArrayList<Long>();
				for(Comment o :qr.getResultlist()){
	    			o.setContent(textFilterManage.filterText(o.getContent()));
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
				User user = null;
				for(Comment comment : qr.getResultlist()){
					if(user == null){
						user = userManage.query_cache_findUserByUserName(comment.getUserName());
					}
					if(user != null){
						comment.setAccount(user.getAccount());
						comment.setNickname(user.getNickname());
						if(user.getAvatarName() != null && !"".equals(user.getAvatarName().trim())){
							comment.setAvatarPath(fileManage.fileServerAddress()+user.getAvatarPath());
							comment.setAvatarName(user.getAvatarName());
						}		
					}
				}
				
			}

			pageView.setQueryResult(qr);
			
			
			return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,pageView));
		}else{
			error.put("userName", "????????????????????????");
		}
		

		return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
	}
	
	/**
	 * ???????????????
	 * @param pageForm
	 * @param model
	 * @param userName ????????????
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(params="method=allReply",method=RequestMethod.GET)
	public String allReply(PageForm pageForm,ModelMap model,String userName,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		//??????
		Map<String,String> error = new HashMap<String,String>();
		if(userName != null && !"".equals(userName.trim())){
			StringBuffer jpql = new StringBuffer("");
			//???????????????
			List<Object> params = new ArrayList<Object>();

			jpql.append(" and o.userName=?"+ (params.size()+1));
			params.add(userName.trim());
			
			//???????????????and
			String _jpql = org.apache.commons.lang3.StringUtils.difference(" and", jpql.toString());
			
			PageView<Reply> pageView = new PageView<Reply>(settingService.findSystemSetting_cache().getBackstagePageNumber(),pageForm.getPage(),10);
			//?????????
			int firstindex = (pageForm.getPage()-1)*pageView.getMaxresult();;	
			//??????
			LinkedHashMap<String,String> orderby = new LinkedHashMap<String,String>();
			
			orderby.put("id", "desc");//??????id??????????????????
			
			
			//?????????????????????
			QueryResult<Reply> qr = commentService.getScrollData(Reply.class, firstindex, pageView.getMaxresult(), _jpql, params.toArray(),orderby);
			if(qr != null && qr.getResultlist() != null && qr.getResultlist().size() >0){
				List<Long> topicIdList = new ArrayList<Long>();
				for(Reply o :qr.getResultlist()){
	    				
	    			o.setContent(textFilterManage.filterText(o.getContent()));
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
				User user = null;
				for(Reply reply : qr.getResultlist()){
					if(user == null){
						user = userManage.query_cache_findUserByUserName(reply.getUserName());
					}
					if(user != null){
						reply.setAccount(user.getAccount());
						reply.setNickname(user.getNickname());
						if(user.getAvatarName() != null && !"".equals(user.getAvatarName().trim())){
							reply.setAvatarPath(fileManage.fileServerAddress()+user.getAvatarPath());
							reply.setAvatarName(user.getAvatarName());
						}		
					}
				}
			}

			pageView.setQueryResult(qr);
			
			
			return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,pageView));
		}else{
			error.put("userName", "????????????????????????");
		}
		

		return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
	}
	
	
	/**
	 * ???????????????
	 * 
	 * @param pageForm
	 * @param model
	 * @param userName ????????????
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(params="method=allQuestion",method=RequestMethod.GET)
	public String allQuestion(PageForm pageForm,ModelMap model,String userName,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		//??????
		Map<String,String> error = new HashMap<String,String>();
		if(userName != null && !"".equals(userName.trim())){
			StringBuffer jpql = new StringBuffer("");
			//???????????????
			List<Object> params = new ArrayList<Object>();

			jpql.append(" and o.userName=?"+ (params.size()+1));
			params.add(userName.trim());
			
			//???????????????and
			String _jpql = org.apache.commons.lang3.StringUtils.difference(" and", jpql.toString());
			
			PageView<Question> pageView = new PageView<Question>(settingService.findSystemSetting_cache().getBackstagePageNumber(),pageForm.getPage(),10);
			//?????????
			int firstindex = (pageForm.getPage()-1)*pageView.getMaxresult();;	
			//??????
			LinkedHashMap<String,String> orderby = new LinkedHashMap<String,String>();
			
			orderby.put("id", "desc");//??????id??????????????????
			
			//?????????????????????
			QueryResult<Question> qr = questionService.getScrollData(Question.class, firstindex, pageView.getMaxresult(), _jpql, params.toArray(),orderby);
			if(qr != null && qr.getResultlist() != null && qr.getResultlist().size() >0){
				List<QuestionTag> questionTagList = questionTagService.findAllQuestionTag();
				
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
				User user = null;
				for(Question question : qr.getResultlist()){
					if(user == null){
						user = userManage.query_cache_findUserByUserName(question.getUserName());
					}
					if(user != null){
						question.setAccount(user.getAccount());
						question.setNickname(user.getNickname());
						if(user.getAvatarName() != null && !"".equals(user.getAvatarName().trim())){
							question.setAvatarPath(fileManage.fileServerAddress()+user.getAvatarPath());
							question.setAvatarName(user.getAvatarName());
						}		
					}
				}
			}

			pageView.setQueryResult(qr);
			
			
			return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,pageView));
		}else{
			error.put("userName", "????????????????????????");
		}
		

		return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
	}
	
	/**
	 * ???????????????
	 * @param pageForm
	 * @param model
	 * @param userName ????????????
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(params="method=allAnswer",method=RequestMethod.GET)
	public String allAuditAnswer(PageForm pageForm,ModelMap model,String userName,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		//??????
		Map<String,String> error = new HashMap<String,String>();
		if(userName != null && !"".equals(userName.trim())){
			StringBuffer jpql = new StringBuffer("");
			//???????????????
			List<Object> params = new ArrayList<Object>();

			jpql.append(" and o.userName=?"+ (params.size()+1));
			params.add(userName.trim());
			
			//???????????????and
			String _jpql = org.apache.commons.lang3.StringUtils.difference(" and", jpql.toString());
			
			PageView<Answer> pageView = new PageView<Answer>(settingService.findSystemSetting_cache().getBackstagePageNumber(),pageForm.getPage(),10);
			//?????????
			int firstindex = (pageForm.getPage()-1)*pageView.getMaxresult();;	
			//??????
			LinkedHashMap<String,String> orderby = new LinkedHashMap<String,String>();
			
			orderby.put("id", "desc");//??????id??????????????????
			
			
			//?????????????????????
			QueryResult<Answer> qr = answerService.getScrollData(Answer.class, firstindex, pageView.getMaxresult(), _jpql, params.toArray(),orderby);
			if(qr != null && qr.getResultlist() != null && qr.getResultlist().size() >0){
				List<Long> questionIdList = new ArrayList<Long>();
				for(Answer o :qr.getResultlist()){
	    			o.setContent(textFilterManage.filterText(o.getContent()));
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
				User user = null;
				for(Answer answer : qr.getResultlist()){
					if(user == null){
						user = userManage.query_cache_findUserByUserName(answer.getUserName());
					}
					if(user != null){
						answer.setAccount(user.getAccount());
						answer.setNickname(user.getNickname());
						if(user.getAvatarName() != null && !"".equals(user.getAvatarName().trim())){
							answer.setAvatarPath(fileManage.fileServerAddress()+user.getAvatarPath());
							answer.setAvatarName(user.getAvatarName());
						}		
					}
				}
			}

			pageView.setQueryResult(qr);
			
			return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,pageView));
		}else{
			error.put("userName", "????????????????????????");
		}
		
		return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
	}
	
	/**
	 * ?????????????????????
	 * @param pageForm
	 * @param model
	 * @param userName ????????????
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(params="method=allAnswerReply",method=RequestMethod.GET)
	public String allAuditAnswerReply(PageForm pageForm,ModelMap model,String userName,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		//??????
		Map<String,String> error = new HashMap<String,String>();
		if(userName != null && !"".equals(userName.trim())){
			StringBuffer jpql = new StringBuffer("");
			//???????????????
			List<Object> params = new ArrayList<Object>();

			jpql.append(" and o.userName=?"+ (params.size()+1));
			params.add(userName.trim());
			
			//???????????????and
			String _jpql = org.apache.commons.lang3.StringUtils.difference(" and", jpql.toString());
			
			PageView<AnswerReply> pageView = new PageView<AnswerReply>(settingService.findSystemSetting_cache().getBackstagePageNumber(),pageForm.getPage(),10);
			//?????????
			int firstindex = (pageForm.getPage()-1)*pageView.getMaxresult();;	
			//??????
			LinkedHashMap<String,String> orderby = new LinkedHashMap<String,String>();
			
			orderby.put("id", "desc");//??????id??????????????????
			
			
			//?????????????????????
			QueryResult<AnswerReply> qr = answerService.getScrollData(AnswerReply.class, firstindex, pageView.getMaxresult(), _jpql, params.toArray(),orderby);
			if(qr != null && qr.getResultlist() != null && qr.getResultlist().size() >0){
				List<Long> questionIdList = new ArrayList<Long>();
				for(AnswerReply o :qr.getResultlist()){
	    				
	    			o.setContent(textFilterManage.filterText(o.getContent()));
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
				User user = null;
				for(AnswerReply answerReply : qr.getResultlist()){
					if(user == null){
						user = userManage.query_cache_findUserByUserName(answerReply.getUserName());
					}
					if(user != null){
						answerReply.setAccount(user.getAccount());
						answerReply.setNickname(user.getNickname());
						if(user.getAvatarName() != null && !"".equals(user.getAvatarName().trim())){
							answerReply.setAvatarPath(fileManage.fileServerAddress()+user.getAvatarPath());
							answerReply.setAvatarName(user.getAvatarName());
						}		
					}
				}
			}

			pageView.setQueryResult(qr);
			
			
			return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,pageView));
		}else{
			error.put("userName", "????????????????????????");
		}
		
		return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
	}
	
	
	
	
	
	
	
	/**
	 * ????????????
	 * @param model
	 * @param file
	 * @param id ??????Id
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(params="method=updateAvatar",method=RequestMethod.POST)
	public String updateAvatar(ModelMap model,MultipartFile file,Long id,
			HttpServletRequest request,HttpServletResponse response)
			throws Exception {	
		
		Map<String,String> error = new HashMap<String,String>();//??????
		if(id ==null){
			error.put("userId", "??????Id????????????");
		}
		if(file ==null || file.isEmpty()){
			error.put("file", "??????????????????");
		}
		String _width = request.getParameter("width");
		String _height = request.getParameter("height");
		String _x = request.getParameter("x");
		String _y = request.getParameter("y");
		
		
		Integer width = null;//???
		Integer height = null;//???
		Integer x = 0;//??????X???
		Integer y = 0;//??????Y???
		
		
		if(_width != null && !"".equals(_width.trim())){
			if(Verification.isPositiveInteger(_width.trim())){
				if(_width.trim().length() >=8){
					error.put("width", "????????????8?????????");//????????????8?????????
				}else{
					width = Integer.parseInt(_width.trim());
				}
				
				
			}else{
				error.put("width", "??????????????????0");//??????????????????0
			}
			
		}
		if(_height != null && !"".equals(_height.trim())){
			if(Verification.isPositiveInteger(_height.trim())){
				if(_height.trim().length() >=8){
					error.put("height", "????????????8?????????");//????????????8?????????
				}else{
					height = Integer.parseInt(_height.trim());
				}
				
			}else{
				error.put("height", "??????????????????0 ");//??????????????????0 
			}
		}
		
		if(_x != null && !"".equals(_x.trim())){
			if(Verification.isPositiveIntegerZero(_x.trim())){
				if(_x.trim().length() >=8){
					error.put("x", "????????????8?????????");//????????????8?????????
				}else{
					x = Integer.parseInt(_x.trim());
				}
				
			}else{
				error.put("x", "X????????????????????????0");//X????????????????????????0
			}
			
		}
		
		if(_y != null && !"".equals(_y.trim())){
			if(Verification.isPositiveIntegerZero(_y.trim())){
				if(_y.trim().length() >=8){
					error.put("y","????????????8?????????");//????????????8?????????
				}else{
					y = Integer.parseInt(_y.trim());
				}
				
			}else{
				error.put("y","Y????????????????????????0");//Y????????????????????????0
			}
			
		}
		
		
		
		String newFileName = "";
	
		User user = userService.findUserById(id);
		if(user != null){
			//??????????????????
			String fileName = file.getOriginalFilename();
			
			//????????????
			Long size = file.getSize();
			
			
			
			//???????????????????????? ??????KB
			long imageSize = 3*1024L;
			
			Integer maxWidth = 200;//????????????
			Integer maxHeight = 200;//????????????
			DateTime dateTime = new DateTime(user.getRegistrationDate());     
			String date = dateTime.toString("yyyy-MM-dd");
			
			//??????????????????;?????????????????????????????????????????????,??????????????????
			String pathDir = "file"+File.separator+"avatar"+File.separator + date +File.separator ;
			//????????????????????????
			fileManage.createFolder(pathDir);
			//100*100??????
			String pathDir_100 = "file"+File.separator+"avatar"+File.separator + date +File.separator +"100x100" +File.separator;
			//????????????????????????
			fileManage.createFolder(pathDir_100);
			
			if(size/1024 <= imageSize){
				if("blob".equalsIgnoreCase(fileName)){//Blob??????
					
					newFileName = UUIDUtil.getUUID32()+ ".jpg";
					
					BufferedImage bufferImage = ImageIO.read(file.getInputStream());  
		            //????????????????????????  
		            int srcWidth = bufferImage.getWidth();  
		            int srcHeight = bufferImage.getHeight();  
					if(srcWidth > maxWidth){
						error.put("file","??????????????????");
					}
					if(srcHeight > maxHeight){
						error.put("file","??????????????????");
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
						fileManage.writeFile(pathDir, newFileName,file.getBytes());

						//??????100*100?????????
						fileManage.createImage(file.getInputStream(),pathDir_100+newFileName,"jpg",100,100);
					}
				}else{
					
					
					//????????????????????????
					List<String> formatList = new ArrayList<String>();
					formatList.add("gif");
					formatList.add("jpg");
					formatList.add("jpeg");
					formatList.add("bmp");
					formatList.add("png");
					
					if(size/1024 <= imageSize){
						
						//??????????????????
						boolean authentication = FileUtil.validateFileSuffix(file.getOriginalFilename(),formatList);
				
						if(authentication){
							//????????????????????????
							fileManage.createFolder(pathDir);
							//????????????????????????
							fileManage.createFolder(pathDir_100);
							
							if(user.getAvatarName() != null && !"".equals(user.getAvatarName().trim())){
								String oldPathFile = pathDir + user.getAvatarName();
								//???????????????
								fileManage.deleteFile(oldPathFile);
								String oldPathFile_100 = pathDir_100 + user.getAvatarName();
								//???????????????100*100
								fileManage.deleteFile(oldPathFile_100);
							}

							BufferedImage bufferImage = ImageIO.read(file.getInputStream());  
				            //????????????????????????  
				            int srcWidth = bufferImage.getWidth();  
				            int srcHeight = bufferImage.getHeight();  
							
							//??????????????????
							String suffix = FileUtil.getExtension(fileName).toLowerCase();
							//??????????????????
							newFileName = UUIDUtil.getUUID32()+ "." + suffix;
							
							if(srcWidth <=200 && srcHeight <=200){	
								//????????????
								fileManage.writeFile(pathDir, newFileName,file.getBytes());
								
								if(srcWidth <=100 && srcHeight <=100){
									//????????????
									fileManage.writeFile(pathDir_100, newFileName,file.getBytes());
								}else{
									//??????100*100?????????
									fileManage.createImage(file.getInputStream(),pathDir_100+newFileName,suffix,100,100);
								}
							}else{
								//??????200*200?????????
								fileManage.createImage(file.getInputStream(),pathDir+newFileName,suffix,x,y,width,height,200,200);

								//??????100*100?????????
								fileManage.createImage(file.getInputStream(),pathDir_100+newFileName,suffix,x,y,width,height,100,100);
	    
							}	
						}else{
							error.put("file","?????????????????????????????????");//?????????????????????????????????
						}	
					}else{
						error.put("file","??????????????????????????????");//??????????????????????????????
					}
				}
				
				
			}else{
				error.put("file", "??????????????????????????????");
			}
		}else{
			error.put("user", "???????????????");
		}
		
		if(error.size() >0){
			return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
		}else{
			userService.updateUserAvatar(user.getUserName(), newFileName);
			//????????????
			userManage.delete_cache_findUserById(user.getId());
			userManage.delete_cache_findUserByUserName(user.getUserName());
			return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,null));
		}
		
		/**
		if(user == null){
			error.put("user", "???????????????");
		}
		
		String _width = request.getParameter("width");
		String _height = request.getParameter("height");
		String _x = request.getParameter("x");
		String _y = request.getParameter("y");
		
		
		Integer width = null;//???
		Integer height = null;//???
		Integer x = 0;//??????X???
		Integer y = 0;//??????Y???
		
		
		if(_width != null && !"".equals(_width.trim())){
			if(Verification.isPositiveInteger(_width.trim())){
				if(_width.trim().length() >=8){
					error.put("width", "????????????8?????????");//????????????8?????????
				}else{
					width = Integer.parseInt(_width.trim());
				}
				
				
			}else{
				error.put("width", "??????????????????0");//??????????????????0
			}
			
		}
		if(_height != null && !"".equals(_height.trim())){
			if(Verification.isPositiveInteger(_height.trim())){
				if(_height.trim().length() >=8){
					error.put("height", "????????????8?????????");//????????????8?????????
				}else{
					height = Integer.parseInt(_height.trim());
				}
				
			}else{
				error.put("height", "??????????????????0 ");//??????????????????0 
			}
		}
		
		if(_x != null && !"".equals(_x.trim())){
			if(Verification.isPositiveIntegerZero(_x.trim())){
				if(_x.trim().length() >=8){
					error.put("x", "????????????8?????????");//????????????8?????????
				}else{
					x = Integer.parseInt(_x.trim());
				}
				
			}else{
				error.put("x", "X????????????????????????0");//X????????????????????????0
			}
			
		}
		
		if(_y != null && !"".equals(_y.trim())){
			if(Verification.isPositiveIntegerZero(_y.trim())){
				if(_y.trim().length() >=8){
					error.put("y","????????????8?????????");//????????????8?????????
				}else{
					y = Integer.parseInt(_y.trim());
				}
				
			}else{
				error.put("y","Y????????????????????????0");//Y????????????????????????0
			}
			
		}
		//??????????????????
		String newFileName = "";
		if(error.size() ==0){
			
			DateTime dateTime = new DateTime(user.getRegistrationDate());     
			String date = dateTime.toString("yyyy-MM-dd");
			
			if(file != null && !file.isEmpty()){
				//??????????????????
				String fileName = file.getOriginalFilename();
				
				//????????????
				Long size = file.getSize();
				

				
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
					
					//??????????????????
					boolean authentication = FileUtil.validateFileSuffix(file.getOriginalFilename(),formatList);
			
					if(authentication){
						//??????????????????;?????????????????????????????????????????????,??????????????????
						String pathDir = "file"+File.separator+"avatar"+File.separator + date +File.separator ;
						//100*100??????
						String pathDir_100 = "file"+File.separator+"avatar"+File.separator + date +File.separator +"100x100" +File.separator;

						//????????????????????????
						fileManage.createFolder(pathDir);
						//????????????????????????
						fileManage.createFolder(pathDir_100);
						
						if(user.getAvatarName() != null && !"".equals(user.getAvatarName().trim())){
							String oldPathFile = pathDir + user.getAvatarName();
							//???????????????
							fileManage.deleteFile(oldPathFile);
							String oldPathFile_100 = pathDir_100 + user.getAvatarName();
							//???????????????100*100
							fileManage.deleteFile(oldPathFile_100);
						}

						BufferedImage bufferImage = ImageIO.read(file.getInputStream());  
			            //????????????????????????  
			            int srcWidth = bufferImage.getWidth();  
			            int srcHeight = bufferImage.getHeight();  
						
						//??????????????????
						String suffix = FileUtil.getExtension(fileName).toLowerCase();
						//??????????????????
						newFileName = UUIDUtil.getUUID32()+ "." + suffix;
						
						if(srcWidth <=200 && srcHeight <=200){	
							//????????????
							fileManage.writeFile(pathDir, newFileName,file.getBytes());
							
							if(srcWidth <=100 && srcHeight <=100){
								//????????????
								fileManage.writeFile(pathDir_100, newFileName,file.getBytes());
							}else{
								//??????100*100?????????
								fileManage.createImage(file.getInputStream(),pathDir_100+newFileName,suffix,100,100);
							}
						}else{
							//??????200*200?????????
							fileManage.createImage(file.getInputStream(),pathDir+newFileName,suffix,x,y,width,height,200,200);

							//??????100*100?????????
							fileManage.createImage(file.getInputStream(),pathDir_100+newFileName,suffix,x,y,width,height,100,100);
    
						}	
					}else{
						error.put("imgFile","?????????????????????????????????");//?????????????????????????????????
					}	
				}else{
					error.put("imgFile","??????????????????????????????");//??????????????????????????????
				}	
			}else{
				error.put("imgFile","??????????????????");//??????????????????
			}
		}
		

		if(error.size() ==0){
			userService.updateUserAvatar(user.getUserName(), newFileName);
			//????????????
			userManage.delete_cache_findUserById(user.getId());
			userManage.delete_cache_findUserByUserName(user.getUserName());
		}
		
		
		

		
		Map<String,Object> returnValue = new HashMap<String,Object>();//?????????

		if(error != null && error.size() >0){
			returnValue.put("success", "false");
			returnValue.put("error", error);
		}else{
			returnValue.put("success", "true");
		}
		return JsonUtils.toJSONString(returnValue);**/
	}
	
	
	
	/**
	 * ???????????? ??????
	 * @param model
	 * @param id ??????Id
	 * @param type ????????????  1:???????????????  2:????????? 3:??????
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(params="method=payment",method=RequestMethod.POST)
	public String payment(ModelMap model,Long id,Integer type,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		Map<String,String> error = new HashMap<String,String>();//??????
		
		if(id == null){
			error.put("payment", "????????????");
		}else{
			User user = userService.findUserById(id);
			if(user == null){
				error.put("payment", "???????????????");
			}
			String staffName = "";//????????????	
			Object obj  =  SecurityContextHolder.getContext().getAuthentication().getPrincipal(); 
			if(obj instanceof UserDetails){
				staffName =((UserDetails)obj).getUsername();
			}
			
			if(type != null){
				if(type.equals(1)){//1:???????????????
					BigDecimal paymentRunningNumber_amount = new BigDecimal("0");
					String paymentRunningNumber = null;//???????????????

					String _paymentRunningNumberAmount = request.getParameter("paymentRunningNumberAmount");
					String _paymentRunningNumber = request.getParameter("paymentRunningNumber");
					String paymentRunningNumber_remark = request.getParameter("paymentRunningNumber_remark");//??????
					
					if(_paymentRunningNumberAmount != null && !"".equals(_paymentRunningNumberAmount.trim())){
						if(_paymentRunningNumberAmount.trim().length() > 12){
							error.put("paymentRunningNumberAmount", "??????????????????12?????????");
						}else{
							boolean paymentRunningNumber_amountVerification = Verification.isAmount(_paymentRunningNumberAmount.trim());//??????
							if(paymentRunningNumber_amountVerification){
								paymentRunningNumber_amount = new BigDecimal(_paymentRunningNumberAmount.trim());
								if(paymentRunningNumber_amount.compareTo(new BigDecimal("0")) <=0){
									error.put("paymentRunningNumberAmount", "??????????????????0");	
								}
							
							}else{
								error.put("paymentRunningNumberAmount", "???????????????");	
							}
						}
					}
					if(_paymentRunningNumber != null && !"".equals(_paymentRunningNumber.trim())){
						if(_paymentRunningNumber.trim().length() >64){
							error.put("paymentRunningNumber", "?????????????????????64???");
						}else{
							paymentRunningNumber = _paymentRunningNumber.trim();
						}
						
						
					}
					
					if(error.size() == 0&& paymentRunningNumber != null){
						PaymentVerificationLog paymentVerificationLog = paymentService.findPaymentVerificationLogById(paymentRunningNumber);
						if(paymentVerificationLog != null){
							if(paymentVerificationLog.getPaymentModule().equals(5)){//5.????????????
								if(user.getUserName().equals(paymentVerificationLog.getUserName())){
									
									PaymentLog paymentLog = new PaymentLog();
									paymentLog.setPaymentRunningNumber(paymentVerificationLog.getId());//???????????????
									paymentLog.setPaymentModule(paymentVerificationLog.getPaymentModule());//???????????? 5.????????????
									paymentLog.setSourceParameterId(String.valueOf(paymentVerificationLog.getParameterId()));//??????Id 
									paymentLog.setOperationUserType(1);//??????????????????  0:??????  1: ??????  2:??????
									paymentLog.setOperationUserName(staffName);
									
									paymentLog.setAmount(paymentRunningNumber_amount);//??????
									paymentLog.setInterfaceProduct(-1);//????????????
									paymentLog.setTradeNo("");//?????????
									paymentLog.setUserName(paymentVerificationLog.getUserName());//????????????
									paymentLog.setRemark(paymentRunningNumber_remark);//??????
									paymentLog.setAmountState(1);//????????????  1:????????????  2:???????????? 
									Object new_paymentLog = paymentManage.createPaymentLogObject(paymentLog);
									
									userService.onlineRecharge(paymentRunningNumber,paymentVerificationLog.getUserName(),paymentRunningNumber_amount,new_paymentLog);
									
									//????????????
									userManage.delete_cache_findUserById(user.getId());
									userManage.delete_cache_findUserByUserName(user.getUserName());
									
									return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,null));
								}else{
									error.put("paymentRunningNumber", "??????????????????????????????");
								}
							}else{
								error.put("paymentRunningNumber", "??????????????????????????????");
							}
						}else{
							error.put("paymentRunningNumber", "??????????????????");
						}
					}

					
				}else if(type.equals(2)){//2:?????????
					BigDecimal deposit = new BigDecimal("0");//?????????

					String deposit_symbol = request.getParameter("deposit_symbol");//??????
					String _deposit = request.getParameter("deposit");
					String deposit_remark = request.getParameter("deposit_remark");//??????
					
					
					if("+".equals(deposit_symbol)){//??????
						deposit_symbol = "+";
					}else{//??????
						deposit_symbol = "-";
					}
					
					if(_deposit != null && !"".equals(_deposit.trim())){
						if(_deposit.trim().length() >=10){
							error.put("deposit", "????????????10??????");
						}
						
						
						boolean deposit_verification = Verification.isAmount(_deposit.trim());
						if(!deposit_verification){
							error.put("deposit", "????????????????????????");
						}else{
							deposit = new BigDecimal(_deposit.trim());//?????????
						}
					}
					
					if(error.size() == 0&& deposit_symbol != null && deposit.compareTo(new BigDecimal("0")) > 0){
							
						PaymentLog paymentLog = new PaymentLog();
						paymentLog.setPaymentRunningNumber(paymentManage.createRunningNumber(user.getId()));//???????????????
						paymentLog.setPaymentModule(5);//???????????? 5.????????????
						paymentLog.setSourceParameterId(String.valueOf(user.getId()));//??????Id 
						paymentLog.setOperationUserType(1);//??????????????????  0:??????  1: ??????  2:??????
						paymentLog.setOperationUserName(staffName);
						
						paymentLog.setAmount(deposit);//??????
						paymentLog.setInterfaceProduct(-1);//????????????
						paymentLog.setTradeNo("");//?????????
						paymentLog.setUserName(user.getUserName());//????????????
						paymentLog.setRemark(deposit_remark);//??????
						
						if("+".equals(deposit_symbol)){//???????????????
							paymentLog.setAmountState(1);//????????????  1:????????????  2:???????????? 
							Object new_paymentLog = paymentManage.createPaymentLogObject(paymentLog);
							userService.addUserDeposit(user.getUserName(),deposit,new_paymentLog);
						}else{//???????????????
							paymentLog.setAmountState(2);//????????????  1:????????????  2:???????????? 
							Object new_paymentLog = paymentManage.createPaymentLogObject(paymentLog);
							userService.subtractUserDeposit(user.getUserName(),deposit,new_paymentLog);
						}
						//????????????
						userManage.delete_cache_findUserById(user.getId());
						userManage.delete_cache_findUserByUserName(user.getUserName());
						
						return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,null));
					}
					
					
				}else if(type.equals(3)){//3:??????
					
					Long point = 0L;//??????

					String point_symbol = request.getParameter("point_symbol");//??????
					String _point = request.getParameter("point");//??????
					String point_remark = request.getParameter("point_remark");//??????
						
					if("+".equals(point_symbol)){//??????
						point_symbol = "+";
					}else{//??????
						point_symbol = "-";
					}
					
					if(_point != null && !"".equals(_point.trim())){
						if(_point.trim().length() >=10){
							error.put("point", "????????????10??????");
						}
						boolean point_verification = Verification.isPositiveInteger(_point.trim());//?????????
						if(!point_verification){
							error.put("point", "??????????????????");
						}else{
							point = Long.parseLong(_point.trim());
						}
						
					}
					
					if(error.size() == 0&& point_symbol != null && point > 0){
						
						PointLog pointLog = new PointLog();
						pointLog.setId(pointManage.createPointLogId(user.getId()));
						pointLog.setModule(600);//??????  600.????????????
						pointLog.setParameterId(user.getId());//??????Id 
						pointLog.setOperationUserType(1);//??????????????????  0:??????  1: ??????  2:??????
						pointLog.setOperationUserName(staffName);//??????????????????
						
						pointLog.setPoint(point);//??????
						pointLog.setUserName(user.getUserName());//????????????
						pointLog.setRemark(point_remark);
						
						
						if("+".equals(point_symbol)){//????????????
							pointLog.setPointState(1);//???????????? 1:???????????? 
							Object new_pointLog = pointManage.createPointLogObject(pointLog);
							userService.addUserPoint(user.getUserName(),point,new_pointLog);
						}else{//????????????
							pointLog.setPointState(2);//????????????  1:????????????  2:???????????? 
							Object new_pointLog = pointManage.createPointLogObject(pointLog);
							userService.subtractUserPoint(user.getUserName(),point,new_pointLog);
						}
						
						//????????????
						userManage.delete_cache_findUserById(user.getId());
						userManage.delete_cache_findUserByUserName(user.getUserName());
						
						//?????????????????????????????????(??????????????????)
						membershipCardGiftTaskManage.async_triggerMembershipCardGiftTask(user.getUserName());
						
						return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,null));
					}
				}
			}
		}
		
		return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
	}
	
	/**
	 * ???????????? ????????????
	 * @param model
	 * @param id ??????Id
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(params="method=cancelAccount",method=RequestMethod.POST)
	public String cancelAccount(ModelMap model,Long id,Integer type,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String,String> error = new HashMap<String,String>();//??????
		
		if(id != null){
			User user = userService.findUserById(id);
			if(user != null){
				if(user.getCancelAccountTime() == -1L){
					userService.cancelAccount(user.getUserName(),"::"+String.valueOf(user.getRegistrationDate().getTime()),new Date().getTime(),new Date().getTime());
					
					//????????????????????????
					userManage.delete_userState(user.getUserName());
					//????????????
					userManage.delete_cache_findUserById(user.getId());
					userManage.delete_cache_findUserByUserName(user.getUserName());
					userRoleManage.delete_cache_findRoleGroupByUserName(user.getUserName());
					return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,null));
					
				}else{
					error.put("account", "??????????????????");
				}
			}else{
				error.put("account", "???????????????");
			}
		}else{
			error.put("account", "????????????");
		}
		return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
	}
	
	
	/**
	 * ???????????? ?????????????????????
	 * @param model
	 * @param id ??????Id
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 
	@ResponseBody
	@RequestMapping(params="method=restoreAccount",method=RequestMethod.POST)
	public String restoreAccount(ModelMap model,Long id,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String,String> error = new HashMap<String,String>();//??????
		
		return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
	}*/
	
}
