package cms.web.action.common;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.wltea.analyzer.lucene.IKAnalyzer;

import cms.bean.PageForm;
import cms.bean.PageView;
import cms.bean.QueryResult;
import cms.bean.fulltext.SearchResult;
import cms.bean.question.Question;
import cms.bean.topic.ImageInfo;
import cms.bean.topic.Tag;
import cms.bean.topic.Topic;
import cms.bean.user.ResourceEnum;
import cms.bean.user.User;
import cms.service.question.QuestionService;
import cms.service.setting.SettingService;
import cms.service.template.TemplateService;
import cms.service.topic.TagService;
import cms.service.topic.TopicService;
import cms.utils.HtmlEscape;
import cms.utils.JsonUtils;
import cms.utils.WebUtil;
import cms.web.action.AccessSourceDeviceManage;
import cms.web.action.TextFilterManage;
import cms.web.action.fileSystem.FileManage;
import cms.web.action.lucene.QuestionLuceneInit;
import cms.web.action.lucene.TopicLuceneInit;
import cms.web.action.lucene.TopicLuceneManage;
import cms.web.action.user.UserManage;
import cms.web.action.user.UserRoleManage;

/**
 * ??????
 *
 */
@Controller
public class SearchAction {
	@Resource TemplateService templateService;
	@Resource TopicService topicService;
	@Resource TopicLuceneManage topicLuceneManage;
	@Resource TagService tagService;
	@Resource AccessSourceDeviceManage accessSourceDeviceManage;
	@Resource SettingService settingService;
	@Resource UserManage userManage;
	@Resource UserRoleManage userRoleManage;
	@Resource TextFilterManage textFilterManage;
	@Resource QuestionService questionService;
	@Resource FileManage fileManage;
	
	
	/**
	 * ??????
	 * @param model
	 * @param keyword ?????????
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/search",method = RequestMethod.GET) 
	public String execute(ModelMap model,String keyword,PageForm pageForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		boolean isAjax = WebUtil.submitDataMode(request);//?????????Ajax??????????????????
		Map<String,String> error = new HashMap<String,String>();//??????
	    Map<String,Object> returnValue = new HashMap<String,Object>();//?????????

		if((keyword == null || "".equals(keyword.trim()))){
			error.put("message", "???????????????????????????");
		}
		PageView<SearchResult> pageView = new PageView<SearchResult>(settingService.findSystemSetting_cache().getForestagePageNumber(), pageForm.getPage(), 10,request.getRequestURI(),request.getQueryString());
		
		
		if(error.size() == 0){
			QueryResult<SearchResult> qr = this.findIndexByCondition(pageView.getCurrentpage(),pageView.getMaxresult(), keyword.trim(),20, 1,false);
			
			if(qr.getResultlist() != null && qr.getResultlist().size() >0){
				List<Long> topicIdList =  new ArrayList<Long>();//??????Id??????
				Map<Long,List<String>> tagRoleNameMap = new HashMap<Long,List<String>>();//?????????????????? key:??????Id ??????????????????
				Map<String,List<String>> userRoleNameMap = new HashMap<String,List<String>>();//?????????????????? key:????????????Id ??????????????????
				Map<Long,Boolean> userViewPermissionMap = new HashMap<Long,Boolean>();//?????????????????????????????????????????????  key:??????Id value:?????????????????????
				List<Long> questionIdList =  new ArrayList<Long>();//??????Id??????
				

				for(SearchResult searchResult : qr.getResultlist()){
					if(searchResult.getIndexModule().equals(10)){//????????????
						topicIdList.add(searchResult.getTopic().getId());
					}else if(searchResult.getIndexModule().equals(20)){//????????????
						questionIdList.add(searchResult.getQuestion().getId());
					}
				}
				
				List<Topic> topicList = null;
				if(topicIdList != null && topicIdList.size() >0){
					topicList = topicService.findByIdList(topicIdList);
				}
				List<Question> questionList = null;
				//??????
				if(questionIdList != null && questionIdList.size() >0){
					questionList = questionService.findByIdList(questionIdList);
				}
				
				Iterator<SearchResult> iter = qr.getResultlist().iterator();
				A:while (iter.hasNext()) {
					SearchResult searchResult = iter.next();
					if(searchResult.getIndexModule().equals(10)){//????????????
						if(topicIdList != null && topicIdList.size() >0){
							Topic old_topic = searchResult.getTopic();
							for(Topic t : topicList){
								if(old_topic.getId().equals(t.getId())){
									t.setTitle(old_topic.getTitle());
									t.setContent(old_topic.getContent());
									t.setIp(null);//IP?????????
									if(t.getPostTime().equals(t.getLastReplyTime())){//???????????????????????????????????????????????????????????????
										t.setLastReplyTime(null);
									}
									if(t.getIsStaff() == false){//??????
										User user = userManage.query_cache_findUserByUserName(t.getUserName());
										t.setAccount(user.getAccount());
										t.setNickname(user.getNickname());
										t.setAvatarPath(fileManage.fileServerAddress()+user.getAvatarPath());
										t.setAvatarName(user.getAvatarName());
										
										
										userRoleNameMap.put(t.getUserName(), null);
										
									}else{
										t.setAccount(t.getUserName());//????????????????????????????????????
									}
									searchResult.setTopic(t);
									continue A;
								}
							}
						}
					}else if(searchResult.getIndexModule().equals(20)){//????????????
						if(questionList != null && questionList.size() >0){
							Question old_question = searchResult.getQuestion();
							for(Question t : questionList){
								if(old_question.getId().equals(t.getId())){
									t.setTitle(old_question.getTitle());
									t.setContent(old_question.getContent());
									t.setIp(null);//IP?????????
									if(t.getPostTime().equals(t.getLastAnswerTime())){//???????????????????????????????????????????????????????????????
										t.setLastAnswerTime(null);
									}
									if(t.getIsStaff() == false){//??????
										User user = userManage.query_cache_findUserByUserName(t.getUserName());
										t.setAccount(user.getAccount());
										t.setNickname(user.getNickname());
										t.setAvatarPath(fileManage.fileServerAddress()+user.getAvatarPath());
										t.setAvatarName(user.getAvatarName());
										
										
										userRoleNameMap.put(t.getUserName(), null);
										
									}else{
										t.setAccount(t.getUserName());//????????????????????????????????????
									}
									searchResult.setQuestion(t);
									continue A;
								}
							}
						}
					}

					//??????SQL????????????????????????????????????
					iter.remove();
					
				}
				
				List<Tag> tagList = tagService.findAllTag_cache();
				if(tagList != null && tagList.size() >0 && topicIdList != null && topicIdList.size() >0){
					for(Topic t : topicList){
						for(Tag tag :tagList){
							if(t.getTagId().equals(tag.getId())){
								t.setTagName(tag.getName());
								tagRoleNameMap.put(t.getTagId(), null);
								userViewPermissionMap.put(t.getTagId(), null);
								break;
							}
							
						}
					}
				}
				
				
				/**
				//??????
				if(topicIdList != null && topicIdList.size() >0){
					List<Topic> topicList = topicService.findByIdList(topicIdList);
					if(topicList != null && topicList.size() >0){
						for(SearchResult searchResult : qr.getResultlist()){
							if(searchResult.getIndexModule().equals(10)){//????????????
							
								Topic old_t = searchResult.getTopic();
								for(Topic pi : topicList){
									if(pi.getId().equals(old_t.getId())){
										pi.setTitle(old_t.getTitle());
										pi.setContent(old_t.getContent());
										pi.setIp(null);//IP?????????
										if(pi.getPostTime().equals(pi.getLastReplyTime())){//???????????????????????????????????????????????????????????????
											pi.setLastReplyTime(null);
										}
										if(pi.getIsStaff() == false){//??????
											User user = userManage.query_cache_findUserByUserName(pi.getUserName());
											pi.setNickname(user.getNickname());
											pi.setAvatarPath(user.getAvatarPath());
											pi.setAvatarName(user.getAvatarName());
											
											
											userRoleNameMap.put(pi.getUserName(), null);
											
										}
										
										searchResult.setTopic(pi);
										break;
									}
								}
							}
						}
						
						List<Tag> tagList = tagService.findAllTag_cache();
						if(tagList != null && tagList.size() >0){
							for(Topic pi : topicList){
								for(Tag tag :tagList){
									if(pi.getTagId().equals(tag.getId())){
										pi.setTagName(tag.getName());
										tagRoleNameMap.put(pi.getTagId(), null);
										userViewPermissionMap.put(pi.getTagId(), null);
										break;
									}
									
								}
							}
						}			
					}	
				}			
				//??????
				if(questionIdList != null && questionIdList.size() >0){
					List<Question> questionList = questionService.findByIdList(questionIdList);
					if(questionList != null && questionList.size() >0){
						for(SearchResult searchResult : qr.getResultlist()){
							if(searchResult.getIndexModule().equals(20)){//????????????
								Question old_t = searchResult.getQuestion();
								for(Question pi : questionList){
									if(pi.getId().equals(old_t.getId())){
										pi.setTitle(old_t.getTitle());
										pi.setContent(old_t.getContent());
										pi.setIp(null);//IP?????????
										if(pi.getPostTime().equals(pi.getLastAnswerTime())){//???????????????????????????????????????????????????????????????
											pi.setLastAnswerTime(null);
										}
										if(pi.getIsStaff() == false){//??????
											User user = userManage.query_cache_findUserByUserName(pi.getUserName());
											pi.setNickname(user.getNickname());
											pi.setAvatarPath(user.getAvatarPath());
											pi.setAvatarName(user.getAvatarName());
											
											
											userRoleNameMap.put(pi.getUserName(), null);
											
										}
										searchResult.setQuestion(pi);
										break;
									}
								}
							}
						}
					}
				
				}
				**/
				
				if(tagRoleNameMap != null && tagRoleNameMap.size() >0){
					for (Map.Entry<Long, List<String>> entry : tagRoleNameMap.entrySet()) {
						List<String> roleNameList = userRoleManage.queryAllowViewTopicRoleName(entry.getKey());
						entry.setValue(roleNameList);
					}
				}
				
				if(userRoleNameMap != null && userRoleNameMap.size() >0){
					for (Map.Entry<String, List<String>> entry : userRoleNameMap.entrySet()) {
						List<String> roleNameList = userRoleManage.queryUserRoleName(entry.getKey());
						entry.setValue(roleNameList);
					}
				}
				if(userViewPermissionMap != null && userViewPermissionMap.size()>0){
					for (Map.Entry<Long,Boolean> entry : userViewPermissionMap.entrySet()) {
						//?????????????????????????????????
						boolean flag = userRoleManage.isPermission(ResourceEnum._1001000,entry.getKey());
						entry.setValue(flag);
					}
				}
				
				for(SearchResult searchResult : qr.getResultlist()){
					if(searchResult.getIndexModule().equals(10)){//????????????
						Topic topic = searchResult.getTopic();
						//???????????????????????????????????????
						for (Map.Entry<Long, List<String>> entry : tagRoleNameMap.entrySet()) {
							if(entry.getKey().equals(topic.getTagId())){
								List<String> roleNameList = entry.getValue();
								if(roleNameList != null && roleNameList.size() >0){
									topic.setAllowRoleViewList(roleNameList);
								}
								break;
							}
							
						}
						//????????????????????????
						for (Map.Entry<String, List<String>> entry : userRoleNameMap.entrySet()) {
							if(entry.getKey().equals(topic.getUserName())){
								List<String> roleNameList = entry.getValue();
								if(roleNameList != null && roleNameList.size() >0){
									topic.setUserRoleNameList(roleNameList);
								}
								break;
							}
						}
						
						//?????????????????????????????????????????????????????????????????????
						for (Map.Entry<Long,Boolean> entry : userViewPermissionMap.entrySet()) {
							if(entry.getKey().equals(topic.getTagId())){
								if(entry.getValue() != null && !entry.getValue()){
									topic.setImage(null);
									topic.setImageInfoList(new ArrayList<ImageInfo>());
									topic.setSummary("");
									topic.setContent("");
								}
								break;
							}
							
						}
					}else if(searchResult.getIndexModule().equals(20)){//????????????
						Question question = searchResult.getQuestion();
						//????????????????????????
						for (Map.Entry<String, List<String>> entry : userRoleNameMap.entrySet()) {
							if(entry.getKey().equals(question.getUserName())){
								List<String> roleNameList = entry.getValue();
								if(roleNameList != null && roleNameList.size() >0){
									question.setUserRoleNameList(roleNameList);
								}
								break;
							}
						}
					}
				}
				
			}
			
			pageView.setQueryResult(qr);
		}

		
		
		
		
		if(isAjax == true){
			returnValue.put("searchResultPage",pageView);
			
    		if(error != null && error.size() >0){
    			returnValue.put("success", "false");
    			returnValue.put("error", error);
    		}else{
    			returnValue.put("success", "true");
    			
    		}
    		WebUtil.writeToWeb(JsonUtils.toJSONString(returnValue), "json", response);
			return null;
		}else{
			model.addAttribute("keyword",keyword);
			model.addAttribute("searchResultPage",pageView);
			
			String dirName = templateService.findTemplateDir_cache();
			
			String accessPath = accessSourceDeviceManage.accessDevices(request);
			
			if(error != null && error.size() >0){//???????????????	
				for (Map.Entry<String,String> entry : error.entrySet()) {	 
					model.addAttribute("message",entry.getValue());//??????
		  			return "templates/"+dirName+"/"+accessPath+"/message";
		  			
				}		
			}
			return "templates/"+dirName+"/"+accessPath+"/search";
		}	
	}
	
	
	/**
	 * ????????????????????????
	 * @param firstIndex ????????????
	 * @param maxResult ????????????????????????
	 * @param keyword ?????????
	 * @param status ??????
	 * @param sortCondition ????????????
	 * @param isHide ??????????????????????????????
	 * @return
	 */
	private QueryResult<SearchResult> findIndexByCondition(int firstIndex, int maxResult,String keyword,Integer status,int sortCondition,boolean isHide){
		QueryResult<SearchResult> qr = new QueryResult<SearchResult>();
		//???????????????????????????   
	    List<SearchResult> searchResultList = new ArrayList<SearchResult>();  
		
		IndexSearcher searcher_topic =  TopicLuceneInit.INSTANCE.getSearcher();
		IndexSearcher searcher_question =  QuestionLuceneInit.INSTANCE.getSearcher();
		IndexSearcher indexSearcher = null;
		if(searcher_topic != null && searcher_question != null){
			Analyzer analyzer_keyword = new IKAnalyzer(); 
			MultiReader multiReader = null;
			try {
				multiReader = new MultiReader(searcher_topic.getIndexReader(),searcher_question.getIndexReader());
				indexSearcher = new IndexSearcher(multiReader);
				
				//??????????????????  
				//	String[] fieldName = {"name","sellprice","createdate"}; //"path"???????????????
				// BooleanClause.Occur[]??????,????????????????????????????????????,     
				// BooleanClause.Occur.MUST?????? and,     
				// BooleanClause.Occur.MUST_NOT??????not,     
				// BooleanClause.Occur.SHOULD??????or. 
				BooleanQuery.Builder query = new BooleanQuery.Builder();//????????????
				
				if(keyword != null && !"".equals(keyword.trim())){
					BooleanClause.Occur[] clauses = { BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD }; 
					Query keyword_parser = MultiFieldQueryParser.parse(new String[] {QueryParser.escape(keyword), QueryParser.escape(keyword)}, new String[] {"title", "content"}, clauses,analyzer_keyword);
					query.add(keyword_parser,BooleanClause.Occur.MUST);
				}
			
				
				if(status != null){
		        	//???????????? ????????????
			        Query status_query = IntPoint.newExactQuery("status", status);
			        query.add(status_query,BooleanClause.Occur.MUST);
		        }

		        //??????????????????  
		        int startIndex = firstIndex <= 1? 0 : (firstIndex-1) * maxResult;
				//??????????????????
				int endIndex = startIndex+maxResult;
				
				//??????   
				SortField[] sortFields = new SortField[2]; 
				//type???????????????????????? 
				//SortField.Type.SCORE ????????????(??????)?????? 
				//SortField.Type.DOC ??????????????? 
				//SortField.Type.AUTO ????????????int???long???float????????? 
				//SortField.Type.STRING ??????STRING?????? 
				//SortField.Type.FLOAT 
				//SortField.Type.LONG 
				//SortField.Type.DOUBLE 
				//SortField.Type.SHORT 
				//SortField.Type.CUSTOM ????????????????????? 
				//SortField.Type.BYTE
				sortFields[0] = new SortField("title",SortField.Type.SCORE);   
				if(sortCondition == 1){
					sortFields[1] = new SortField("postTime",SortField.Type.LONG,true);//false?????????true?????? //??????????????????   ???-->???
				}else if(sortCondition == 2){
					sortFields[1] = new SortField("postTime",SortField.Type.LONG,false);//false?????????true??????//??????????????????  ???-->???
				}else{
					//???1??????
					sortFields[1] = new SortField("postTime",SortField.Type.LONG,true);//false?????????true?????? //??????????????????   ???-->???
				}
				Sort sort = new Sort(sortFields);  

				
				 //????????????    
				SimpleHTMLFormatter simpleHtmlFormatter = new SimpleHTMLFormatter("<B>","</B>");//?????????????????????????????????????????????????????????????????????????????????   
				
				Highlighter highlighter = new Highlighter(simpleHtmlFormatter,new QueryScorer(query.build()));   
				highlighter.setTextFragmenter(new SimpleFragmenter(190));//??????????????????????????????.???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????   
				
				
				
			
				TopDocs topDocs = indexSearcher.search(query.build() ,endIndex,sort); 
				ScoreDoc[] scoreDocs = topDocs.scoreDocs;  
				
			    //?????????????????? 
			    for (int i = startIndex;i < endIndex && i<topDocs.totalHits; i++) {   
			    	
					Document targetDoc = indexSearcher.doc(scoreDocs[i].doc); //???????????????????????????????????????   
					String _indexModule = targetDoc.get("indexModule");
					String _id = targetDoc.get("id");
					String _title = targetDoc.get("title");
					String _content = targetDoc.get("content");
					
					Long id = Long.parseLong(_id);
					String title = "";
					String content = "";

					if (_title != null && !"".equals(_title)) {   
						//????????????
						_title = HtmlEscape.escape(_title);
		                TokenStream tokenStream = analyzer_keyword.tokenStream("title",new StringReader(_title));    
		                String highLightText = highlighter.getBestFragment(tokenStream, _title);//????????????  
		                if(highLightText != null && !"".equals(highLightText)){
		                	title = highLightText;
		                }else{
		                	title = _title;
		                }
		            }
					
					
					if (_content != null && !"".equals(_content)) { 
						if(isHide){//????????????????????????
							_content = textFilterManage.filterText(_content);
						}else{
							_content = textFilterManage.filterHideText(_content);
						}
						
		                TokenStream tokenStream = analyzer_keyword.tokenStream("content",new StringReader(_content));    
		                String highLightText = highlighter.getBestFragment(tokenStream, _content);
						//????????????  
		                if(highLightText != null && !"".equals(highLightText)){
		                	content = highLightText;
		                }else{
		                	if(_content.length() >190){
		                		content = _content.substring(0, 190);
		                	}else{
		                		content = _content;
		                	}
		                	
		                }
		            }
					
					if(_indexModule != null && "20".equals(_indexModule)){//????????????
						SearchResult searchResult = new SearchResult();
						searchResult.setIndexModule(20);
						Question question = new Question();
						question.setId(id);
						question.setTitle(title);
						question.setContent(content);
						searchResult.setQuestion(question);
						searchResultList.add(searchResult);
						
					}else{//????????????
						SearchResult searchResult = new SearchResult();
						searchResult.setIndexModule(10);
						Topic topic = new Topic();
						topic.setId(id);
						topic.setTitle(title);
						topic.setContent(content);
						searchResult.setTopic(topic);
						searchResultList.add(searchResult);
					}
					
			    }
			    //????????????????????????
				qr.setResultlist(searchResultList);
				qr.setTotalrecord(topDocs.totalHits);
			} catch (InvalidTokenOffsetsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();	
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			
			}finally{ 
				//????????????   
				TopicLuceneInit.INSTANCE.closeSearcher(searcher_topic);
				TopicLuceneInit.INSTANCE.closeSearcher(searcher_question);
			}
			
			
		
		}
		
		return qr;
	}
	
	
	
	
	
	
	
	
	
}
