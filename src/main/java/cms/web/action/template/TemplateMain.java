package cms.web.action.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import cms.bean.DataView;
import cms.bean.PageView;
import cms.bean.help.Help;
import cms.bean.help.HelpType;
import cms.bean.links.Links;
import cms.bean.membershipCard.MembershipCard;
import cms.bean.question.Answer;
import cms.bean.question.Question;
import cms.bean.question.QuestionTag;
import cms.bean.redEnvelope.GiveRedEnvelope;
import cms.bean.redEnvelope.ReceiveRedEnvelope;
import cms.bean.template.Advert;
import cms.bean.template.Column;
import cms.bean.template.CustomHTML;
import cms.bean.template.Forum;
import cms.bean.template.Layout;
import cms.bean.thirdParty.SupportLoginInterface;
import cms.bean.topic.Comment;
import cms.bean.topic.Tag;
import cms.bean.topic.Topic;
import cms.service.template.TemplateService;
import cms.utils.threadLocal.TemplateThreadLocal;
import cms.web.action.AccessSourceDeviceManage;
import cms.web.action.template.impl.Advertising_TemplateManage;
import cms.web.action.template.impl.Column_TemplateManage;
import cms.web.action.template.impl.CustomForum_TemplateManage;
import cms.web.action.template.impl.Favorite_TemplateManage;
import cms.web.action.template.impl.Feedback_TemplateManage;
import cms.web.action.template.impl.Follow_TemplateManage;
import cms.web.action.template.impl.Help_TemplateManage;
import cms.web.action.template.impl.Like_TemplateManage;
import cms.web.action.template.impl.Links_TemplateManage;
import cms.web.action.template.impl.MembershipCard_TemplateManage;
import cms.web.action.template.impl.QuestionTag_TemplateManage;
import cms.web.action.template.impl.Question_TemplateManage;
import cms.web.action.template.impl.RedEnvelope_TemplateManage;
import cms.web.action.template.impl.System_TemplateManage;
import cms.web.action.template.impl.Tag_TemplateManage;
import cms.web.action.template.impl.Topic_TemplateManage;

import org.springframework.stereotype.Component;



/**
 * ??????????????????
 * @author Administrator
 *
 */
@Component("templateMain")
public class TemplateMain {
	@Resource TemplateService templateService;
	@Resource AccessSourceDeviceManage accessSourceDeviceManage;
	
	@Resource Tag_TemplateManage tag_TemplateManage;//?????? -- ??????????????????
	@Resource Topic_TemplateManage topic_TemplateManage;//?????? -- ??????????????????
	@Resource QuestionTag_TemplateManage questionTag_TemplateManage;// ???????????? -- ??????????????????
	@Resource Question_TemplateManage question_TemplateManage;// ?????? -- ??????????????????
	@Resource Feedback_TemplateManage feedback_TemplateManage;
	@Resource Links_TemplateManage links_TemplateManage;// ???????????? -- ??????????????????
	
	@Resource MembershipCard_TemplateManage membershipCard_TemplateManage;// ????????? -- ??????????????????
	
	@Resource Column_TemplateManage column_TemplateManage;// ???????????? -- ??????????????????
	
	@Resource Help_TemplateManage help_TemplateManage;//???????????? -- ??????????????????
	@Resource Advertising_TemplateManage advertising_TemplateManage;//?????? -- ??????????????????
	
	@Resource Favorite_TemplateManage favorite_TemplateManage;//????????? -- ??????????????????
	@Resource Like_TemplateManage like_TemplateManage;//?????? -- ??????????????????
	@Resource Follow_TemplateManage follow_TemplateManage;//?????? -- ??????????????????
	
	@Resource CustomForum_TemplateManage customForum_TemplateManage;//??????????????? -- ??????????????????
	@Resource System_TemplateManage system_TemplateManage;//???????????? -- ??????????????????
	
	@Resource RedEnvelope_TemplateManage redEnvelope_TemplateManage;//?????? -- ??????????????????
	
	/**
	 * ??????????????????
	 * @param type ????????????
	 * @param layoutFile ????????????
	 * @return
	 * @throws Exception
	 */
	public Map<String,Object> list(Integer type,String layoutFile,HttpServletRequest request)throws Exception{
		Map<String,Object> root = new HashMap<String,Object>();
		String dirName = templateService.findTemplateDir_cache();//???????????????????????????

		//??????????????????????????????????????????
		List<String> referenceCode = new ArrayList<String>();
		
		
		List<Forum> list = templateService.findForum_cache(dirName, type,layoutFile);
		for(Forum forum : list){
			if(!referenceCode.contains(forum.getReferenceCode())){
				//??????   ???|??????????????? " ???????????? | ????????????"??????
				root.put(forum.getReferenceCode(), forum.getReferenceCode()+"|templates/"+dirName+"/"+accessSourceDeviceManage.accessDevices(request)+"/forum/"+forum.getModule()+".html");
				
			}
		
			referenceCode.add(forum.getModule());
		}
		return root;
	}
	/**
	 * ???????????????????????????
	 * @param forum
	 * @param submitParameter ????????????
	 * @param runtimeParameter ???????????????
	 * @return
	 */
	public Object templateObject(Forum forum,Map<String,Object> submitParameter,Map<String,Object> runtimeParameter){
		if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				
				List<Tag> value = tag_TemplateManage.tag_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("page")){//??????
				
				PageView<Topic> value = topic_TemplateManage.topic_page(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				
				List<Topic> value = topic_TemplateManage.topic_like_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("??????????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				Map<String,Object> value = topic_TemplateManage.topicUnhide_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("entityBean")){//????????????
				Topic value = topic_TemplateManage.content_entityBean(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				Map<String,Object> value = topic_TemplateManage.addTopic_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				Map<String,Object> value = topic_TemplateManage.editTopic_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("page")){//??????
				PageView<Comment> value = topic_TemplateManage.comment_page(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				Map<String,Object> value = topic_TemplateManage.addComment_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				Map<String,Object> value = topic_TemplateManage.quoteComment_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				Map<String,Object> value = topic_TemplateManage.editComment_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				Map<String,Object> value = topic_TemplateManage.replyComment_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("??????????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				Map<String,Object> value = topic_TemplateManage.editCommentReply_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("??????????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				List<QuestionTag> value = questionTag_TemplateManage.questionTag_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("page")){//??????
				PageView<Question> value = question_TemplateManage.question_page(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("entityBean")){//????????????
				Question value = question_TemplateManage.content_entityBean(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("page")){//??????
				PageView<Answer> value = question_TemplateManage.answer_page(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				Map<String,Object> value = question_TemplateManage.addQuestion_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				Map<String,Object> value = question_TemplateManage.appendQuestion_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				Map<String,Object> value = question_TemplateManage.addAnswer_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				Map<String,Object> value = question_TemplateManage.editAnswer_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				Map<String,Object> value = question_TemplateManage.replyAnswer_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("??????????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				Map<String,Object> value = question_TemplateManage.editAnswerReply_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				Map<String,Object> value = question_TemplateManage.adoptionAnswer_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("entityBean")){//????????????
				Long value = question_TemplateManage.answerCount_entityBean(forum, submitParameter, runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				
				List<Question> value = question_TemplateManage.question_like_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}
		
		
		
		else if(forum.getForumChildType().equals("???????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				Map<String,Object> value = favorite_TemplateManage.addFavorite_collection(forum, submitParameter, runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????????????????")){
			if(forum.getDisplayType().equals("entityBean")){//????????????
				Long value = favorite_TemplateManage.favoriteCount_entityBean(forum, submitParameter, runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("??????????????????????????????")){
			if(forum.getDisplayType().equals("entityBean")){//????????????
				Boolean value = favorite_TemplateManage.alreadyCollected_entityBean(forum, submitParameter, runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????????????????")){
			if(forum.getDisplayType().equals("entityBean")){//????????????
				Long value = favorite_TemplateManage.questionFavoriteCount_entityBean(forum, submitParameter, runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("??????????????????????????????")){
			if(forum.getDisplayType().equals("entityBean")){//????????????
				Boolean value = favorite_TemplateManage.alreadyFavoriteQuestion_entityBean(forum, submitParameter, runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("???????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				Map<String,Object> value = like_TemplateManage.addLike_collection(forum, submitParameter, runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("??????????????????")){
			if(forum.getDisplayType().equals("entityBean")){//????????????
				Long value = like_TemplateManage.likeCount_entityBean(forum, submitParameter, runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("?????????????????????????????????")){
			if(forum.getDisplayType().equals("entityBean")){//????????????
				Boolean value = like_TemplateManage.alreadyLiked_entityBean(forum, submitParameter, runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				Map<String,Object> value = follow_TemplateManage.addFollow_collection(forum, submitParameter, runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("entityBean")){//????????????
				Long value = follow_TemplateManage.followCount_entityBean(forum, submitParameter, runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("entityBean")){//????????????
				Long value = follow_TemplateManage.followerCount_entityBean(forum, submitParameter, runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("???????????????????????????")){
			if(forum.getDisplayType().equals("entityBean")){//????????????
				Boolean value = follow_TemplateManage.following_entityBean(forum, submitParameter, runtimeParameter);
				return value;
			}
			
		}else if(forum.getForumChildType().equals("???????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				List<MembershipCard> value = membershipCard_TemplateManage.membershipCard_collection(forum,submitParameter, runtimeParameter);
				return value;
			}	
		}else if(forum.getForumChildType().equals("???????????????")){
			if(forum.getDisplayType().equals("entityBean")){//????????????
				MembershipCard value =  membershipCard_TemplateManage.membershipCardContent_entityBean(forum, submitParameter, runtimeParameter);
				return value;
			}	
		}else if(forum.getForumChildType().equals("???????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				Map<String,Object> value = membershipCard_TemplateManage.buyMembershipCard_collection(forum, submitParameter, runtimeParameter);
				return value;
			}	
		}
		
		else if(forum.getForumChildType().equals("???????????????")){
			if(forum.getDisplayType().equals("entityBean")){//????????????
				GiveRedEnvelope value =  redEnvelope_TemplateManage.content_entityBean(forum, submitParameter, runtimeParameter);
				return value;
			}	
		}else if(forum.getForumChildType().equals("????????????????????????")){
			if(forum.getDisplayType().equals("page")){//??????
				PageView<ReceiveRedEnvelope> value = redEnvelope_TemplateManage.receiveRedEnvelopeUser_page(forum, submitParameter,runtimeParameter);
				return value;
			}	
		}else if(forum.getForumChildType().equals("?????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				Map<String,Object> value = redEnvelope_TemplateManage.addReceiveRedEnvelope_collection(forum, submitParameter, runtimeParameter);
				return value;
			}	
		}
		
		else if(forum.getForumChildType().equals("??????????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				Map<String,Object> value = feedback_TemplateManage.addFeedback_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("??????????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				List<Links> value = links_TemplateManage.links_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				List<Advert> value = advertising_TemplateManage.recommend_collection_image(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("??????????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				List<Column> value = column_TemplateManage.column_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("??????????????????")){
			if(forum.getDisplayType().equals("monolayer")){//??????
				DataView<Help> value = help_TemplateManage.help_monolayer(forum, submitParameter,runtimeParameter);
				return value;
			}else if(forum.getDisplayType().equals("page")){//??????
				PageView<Help> value = help_TemplateManage.help_page(forum, submitParameter,runtimeParameter);
				return value;
			}else if(forum.getDisplayType().equals("collection")){//??????
				List<Help> value = help_TemplateManage.help_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("??????????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				List<Help> value = help_TemplateManage.recommend_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("??????????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				List<HelpType> value = help_TemplateManage.type_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("??????????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				Map<Long,String> value = help_TemplateManage.navigation_collection(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("??????????????????")){
			if(forum.getDisplayType().equals("entityBean")){//????????????
				Help value = help_TemplateManage.content_entityBean(forum, submitParameter,runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("???????????????HTML")){
			if(forum.getDisplayType().equals("entityBean")){//????????????
				CustomHTML value = customForum_TemplateManage.customHTML_entityBean(forum, submitParameter, runtimeParameter);
				return value;
			}
		}else if(forum.getForumChildType().equals("???????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				List<String> value = system_TemplateManage.searchWord_collection(forum, submitParameter,runtimeParameter);
				return value;
			}	
		}else if(forum.getForumChildType().equals("???????????????")){
			if(forum.getDisplayType().equals("collection")){//??????
				List<SupportLoginInterface> value = system_TemplateManage.thirdPartyLogin_collection(forum, submitParameter,runtimeParameter);
				return value;
			}	
		}
		
		
		
		return null;	
	}
	
	
	/**
	 * ?????????(???????????????)[???????????????????????????????????????]
	 * @param quoteTemplate ????????????????????????
	 * @throws Exception
	 */
	public Map<String,Object> publicQuoteCall(String quoteTemplate,HttpServletRequest request)throws Exception{
		Map<String,Object> map = new HashMap<String,Object>();
		
		//???????????????????????????
		String dirName = templateService.findTemplateDir_cache();
		List<Layout> layoutList = templateService.findLayout_cache(dirName, 6);//?????????(???????????????)
	
		for(Layout layout : layoutList){
			//???????????????????????????????????????
			Forum forum = templateService.findForum_cache(dirName, layout.getReferenceCode());
			if(forum != null){
				//??????   ???|??????????????? " ???????????? | ????????????"??????
				map.put(forum.getReferenceCode(), forum.getReferenceCode()+"|templates/"+dirName+"/"+accessSourceDeviceManage.accessDevices(request)+"/forum/"+forum.getModule()+".html");  
			}
		}
		return map;
	}

	/**
	 * ?????????(??????????????????)[???????????????????????????????????????]
	 * @param quoteTemplate ??????????????????
	 * @throws Exception
	 */
	public Map<String,Object> newPublic(String quoteTemplate,HttpServletRequest request)throws Exception{
		Map<String,Object> map = new HashMap<String,Object>();
	
		
		//???????????????????????????
		String dirName = templateService.findTemplateDir_cache();
		//????????????????????????(??????????????????)
		List<Layout> publics = templateService.findLayout_cache(dirName, 5);//?????????(??????????????????)
		for(Layout layout : publics){
			map.put(layout.getReferenceCode(), layout.getReferenceCode()+"|templates/"+dirName+"/"+accessSourceDeviceManage.accessDevices(request)+"/public/"+layout.getLayoutFile()); 
			List<Forum> forumList = templateService.findForumByLayoutId_cache(dirName,layout.getId());
			for(Forum forum : forumList){
				map.put(forum.getReferenceCode(), forum.getReferenceCode()+"|templates/"+dirName+"/"+accessSourceDeviceManage.accessDevices(request)+"/forum/"+forum.getModule()+".html");  
			}
			//?????????????????????????????????
			TemplateThreadLocal.setLayoutFile(layout.getLayoutFile());
		}
		return map;
	}
}
