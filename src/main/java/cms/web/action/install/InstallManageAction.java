package cms.web.action.install;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


import cms.bean.install.Install;
import cms.utils.CommentedProperties;
import cms.utils.DruidTool;
import cms.utils.FileUtil;
import cms.utils.PathUtil;
import cms.utils.SHA;
import cms.utils.SqlFile;
import cms.utils.UUIDUtil;
import cms.utils.Verification;
import cms.utils.WebUtil;
import cms.web.action.fileSystem.localImpl.LocalFileManage;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.utils.AddrUtil;


@WebServlet("/install")
public class InstallManageAction extends HttpServlet{
	private static final long serialVersionUID = 4687049294910496944L;
	
	private static final Logger logger = LogManager.getLogger(InstallManageAction.class);
	 

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(!this.installSystem()){
			return;
		}
		
		String path = request.getContextPath();
		request.setAttribute("config_url", request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/");
		
		Install install = new Install();
		request.setAttribute("install", install);
		request.getRequestDispatcher("/WEB-INF/data/install/install.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String,String> error = new HashMap<String,String>();
		String databaseLink = "";
		
		Install install = new Install();
		if(!this.installSystem()){
			error.put("installSystem", "?????????????????????");
		}else{
			String databaseIP = request.getParameter("databaseIP");
			String databasePort = request.getParameter("databasePort");
			String databaseName = request.getParameter("databaseName");
			String databaseUser = request.getParameter("databaseUser");
			String databasePassword = request.getParameter("databasePassword");
			String userAccount = request.getParameter("userAccount");
			String userPassword = request.getParameter("userPassword");
			
			String cacheServer = request.getParameter("cacheServer");
			String memcacheIP = request.getParameter("memcacheIP");
			String memcachePort = request.getParameter("memcachePort");
			
			
			
			if(cacheServer != null && cacheServer.equals("memcache")){
				install.setCacheServer("memcache");
			}
			install.setDatabaseIP(databaseIP);
			install.setDatabasePort(databasePort);
			install.setDatabaseName(databaseName);
			install.setDatabaseUser(databaseUser);
			install.setDatabasePassword(databasePassword);
			install.setUserAccount(userAccount);
			install.setUserPassword(userPassword);
			install.setMemcacheIP(memcacheIP);
			install.setMemcachePort(memcachePort);
			
			if(install.getDatabaseIP() == null || "".equals(install.getDatabaseIP().trim())){
				error.put("databaseIP", "?????????IP????????????");
			}
			if(install.getDatabasePort() != null && !"".equals(install.getDatabasePort().trim())){
	
				boolean verification = Verification.isPositiveIntegerZero(install.getDatabasePort().trim());
				if(!verification){
					error.put("databasePort", "?????????????????????");	
				}
			}else{
				error.put("databasePort", "???????????????????????????");
			}
			if(install.getDatabaseName() == null || "".equals(install.getDatabaseName().trim())){
				error.put("databaseName", "???????????????????????????");
			}
			if(install.getDatabaseUser() == null || "".equals(install.getDatabaseUser().trim())){
				error.put("databaseUser", "??????????????????????????????");
			}
			if(install.getDatabasePassword() == null || "".equals(install.getDatabasePassword().trim())){
				error.put("databasePassword", "???????????????????????????");
			}
			if(install.getUserAccount() == null || "".equals(install.getUserAccount().trim())){
				error.put("userAccount", "????????????????????????????????????");
			}else{
				boolean verification = Verification.isNumericLettersUnderscore(install.getUserAccount().trim());
				if(!verification){
					error.put("userAccount", "????????????????????????26????????????????????????????????????");	
				}
			}
			if(install.getUserPassword() == null || "".equals(install.getUserPassword().trim())){
				error.put("userPassword", "????????????????????????????????????");
			}
			
			if(install.getCacheServer().equals("memcache")){
				if(install.getMemcacheIP() == null || "".equals(install.getMemcacheIP().trim())){
					error.put("memcacheIP", "???????????????IP????????????");
				}
				if(install.getMemcachePort() != null && !"".equals(install.getMemcachePort().trim())){
					
					boolean verification = Verification.isPositiveIntegerZero(install.getMemcachePort().trim());;
					if(!verification){
						error.put("memcachePort", "?????????????????????");	
					}
				}else{
					error.put("memcachePort", "?????????????????????????????????");
				}
			}
			
			
			//?????????????????????
			if(error.size() ==0){
				Connection conn = null;
				ResultSet rs = null;
				ResultSet rs2 = null;
				try {
					databaseLink = "jdbc:mysql://"+install.getDatabaseIP().trim()+":"+install.getDatabasePort().trim()+"/"+install.getDatabaseName().trim()+"?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&zeroDateTimeBehavior=CONVERT_TO_NULL&allowPublicKeyRetrieval=true&useSSL=false&rewriteBatchedStatements=true";
					//linux???5.7???????????????,???????????????java.sql.SQLException: No suitable driver found for
					DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());//????????????
				
					conn = DriverManager.getConnection(databaseLink, install.getDatabaseUser().trim(), install.getDatabasePassword().trim());	
					
					//???????????????
					rs = conn.getMetaData().getTables(conn.getCatalog(), "%", "%", new String[]{"TABLE"});
					int count = 0;
					while(rs.next()){ 
						count++;
						
					} 
				
					//??????????????????????????????????????????
					rs2 = conn.prepareStatement("show variables like 'char%'").executeQuery();
					
					String character_set_database = "";
					
					while(rs2.next()){ 
						if("character_set_database".equalsIgnoreCase(rs2.getString(1))){
							character_set_database = rs2.getString(2);
						}	
					}
					if(!"utf8mb4".equalsIgnoreCase(character_set_database)){
						error.put("databaseName", "??????????????????utf8mb4??????");
					}
					
					
					//??????????????????????????????????????????
					if(count >0){
						error.put("installSystem", "?????????????????????");
					}
	
				}catch (Exception e) {
					error.put("databaseLink", "?????????????????????");
					if (logger.isErrorEnabled()) {
    		            logger.error("???????????????????????????",e);
    		        }
				//	e.printStackTrace();
				}finally {
					if(rs != null){
						try {
							rs.close();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
						//	e.printStackTrace();
							error.put("databaseLink", "????????????????????????????????????");
						}
					}
					if(rs2 != null){
						try {
							rs2.close();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
						//	e.printStackTrace();
							error.put("databaseLink", "??????????????????????????????????????????");
						}
					}
					if(conn != null){
						try {
							conn.close();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
						//	e.printStackTrace();
							error.put("databaseLink", "?????????????????????");
						}
					}
				}
				
			}
			
			if(error.size() ==0){
				if(install.getCacheServer().equals("memcache")){
					//??????Memcache????????????
					MemcachedClientBuilder builder = new XMemcachedClientBuilder(  
					        AddrUtil.getAddresses(install.getMemcacheIP().trim()+":"+install.getMemcachePort().trim()));  
					//????????????  
			        builder.setFailureMode(true);  
			        //?????????????????????  
					builder.setCommandFactory( new BinaryCommandFactory());  
					MemcachedClient client = null;
					try {
						client = builder.build();
						
						client.set("hello", 10, "Hello,xmemcached");  
						
					} catch (Exception e) {
						error.put("cacheServer", "???????????????????????????");
						// TODO Auto-generated catch block
					//	e.printStackTrace();
					}finally {
						if(client != null){
							client.shutdown();//??????
						}
					}
				}
			}
			
			

			
			List<String> fileList = new ArrayList<String>();//???????????????
			fileList.add("WEB-INF"+File.separator+"data"+File.separator+"install"+File.separator+"status.txt");
			fileList.add("WEB-INF"+File.separator+"classes"+File.separator+"druid.properties");
			fileList.add("WEB-INF"+File.separator+"classes"+File.separator+"memcache.properties");
			fileList.add("WEB-INF"+File.separator+"data"+File.separator+"install"+File.separator+"web.xml");
			fileList.add("WEB-INF"+File.separator+"web.xml");
			
			for(String f : fileList){
				File file = new File(PathUtil.path()+File.separator+f);
				//??????????????????
				if(!file.canRead() || !file.canWrite()){
					error.put("filePermissions", "?????????????????? "+f +"  "+(file.canRead() == false ? "[?????????]":"")+(file.canWrite() == false ? "[?????????]":""));
				}
			}
		}
		
		if(error.size() ==0){
			
			String cacheName = "ehcache";
			if(install.getCacheServer().equals("memcache")){
				cacheName = "memcache";
    			//??????Memcache??????????????????
        		org.springframework.core.io.Resource memcache_resource = new ClassPathResource("/memcache.properties");//??????????????????
        		CommentedProperties memcache_props = new CommentedProperties();
        		
        		BufferedWriter memcache_bw = null;
        		try {
        			memcache_props.load(memcache_resource.getInputStream(),"utf-8");

        			memcache_props.setProperty("memcache.server_1", install.getMemcacheIP().trim());
        			memcache_props.setProperty("memcache.port_1", install.getMemcachePort().trim());
    				
        			memcache_bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(memcache_resource.getFile()),"UTF-8"));
    				
    				
    				memcache_props.store(memcache_bw);
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    			//	e.printStackTrace();
    				error.put("installSystem", "??????memcache.properties??????????????????");
    				if (logger.isErrorEnabled()) {
    		            logger.error("???????????????????????????memcache.properties??????????????????",e);
    		        }
    			}finally {
    				if(memcache_bw != null){
    					memcache_bw.close();
    				}
    				
				}
    		}
		
			//???????????????????????????
    		org.springframework.core.io.Resource database_resource = new ClassPathResource("/druid.properties");//??????????????????
    		CommentedProperties database_props = new CommentedProperties();
    		
    		BufferedWriter bw = null;
    		try {
    			//??????
    			String privateKey = "";
    			//??????
    			String publicKey = "";

    			Map<String, String> rsaKey = DruidTool.generateRsaKey();
    			if(rsaKey != null && rsaKey.size() >0){
    				for(Map.Entry<String, String> entry : rsaKey.entrySet()){
    					if("privateKey".equals(entry.getKey())){
    						privateKey = entry.getValue();
    					}else if("publicKey".equals(entry.getKey())){
    						publicKey = entry.getValue();
    					}
    				}
    			}

    			String encryptPassword = "";
    			try {
    				encryptPassword = DruidTool.encryptString(privateKey, install.getDatabasePassword().trim());
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	e.printStackTrace();
					error.put("installSystem", "???????????????????????????");
					if (logger.isErrorEnabled()) {
			            logger.error("????????????????????????????????????????????????",e);
			        }
				}

    			database_props.load(database_resource.getInputStream(),"utf-8");
    			database_props.setProperty("cacheName", cacheName);
    			database_props.setProperty("jdbc_driver", "com.mysql.cj.jdbc.Driver");
    			database_props.setProperty("jdbc_url", databaseLink);
    			database_props.setProperty("jdbc_user", install.getDatabaseUser().trim());
				database_props.setProperty("jdbc_password", encryptPassword);
				database_props.setProperty("jdbc_publickey", "config.decrypt=true;config.decrypt.key="+publicKey);
	
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(database_resource.getFile()),"UTF-8"));
				database_props.store(bw);
			} catch (IOException e) {
				// TODO Auto-generated catch block
			//	e.printStackTrace();
				error.put("installSystem", "??????druid.properties??????????????????");
				if (logger.isErrorEnabled()) {
		            logger.error("???????????????????????????druid.properties??????????????????",e);
		        }
			}finally {
				if(bw != null){
					bw.close();
				}
				
			}
		}
		
		
		if(error.size() ==0){
			Connection conn = null;
    		Statement stmt = null;
			try {
				databaseLink = "jdbc:mysql://"+install.getDatabaseIP().trim()+":"+install.getDatabasePort().trim()+"/"+install.getDatabaseName().trim()+"?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&zeroDateTimeBehavior=CONVERT_TO_NULL&allowPublicKeyRetrieval=true&useSSL=false&rewriteBatchedStatements=true";
				//linux???5.7???????????????,???????????????java.sql.SQLException: No suitable driver found for
				DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());//????????????
				
				conn = DriverManager.getConnection(databaseLink, install.getDatabaseUser().trim(), install.getDatabasePassword().trim());	
				
				//??????????????????????????????????????????
                conn.prepareStatement("set names utf8mb4").executeQuery();
		
				String path = PathUtil.path()+File.separator+"WEB-INF"+File.separator+"data"+File.separator+"install"+File.separator;
				//??????SQL????????????
				SqlFile.importSQL(conn,path+"structure_tables_mysql.sql","utf-8");
				
				
				//??????SQL????????????
				SqlFile.importSQL(conn,path+"data_tables_mysql.sql","utf-8");
				
				//?????????????????????
				//INSERT INTO `sysusers` (`userId`,`enabled`,`fullName`,`issys`,`userAccount`,`userDesc`,`userDuty`,`userPassword`) VALUES ('0e2abc06-a71a-40ed-b449-a55c1a5b6a68',b'1','fdsf',b'0','fdsfds',NULL,NULL,'5cf74b96bcc721bf1a97674550dff37e225d72766c3d5969e8638f57d8d4809e7e7b7b87f796582c')
				PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();//BCrypt????????????,Bcrypt???????????????72??????????????????72?????????????????????????????????
				
				// ????????????????????????????????????????????????
				String newPassword = passwordEncoder.encode(SHA.sha256Hex(install.getUserPassword().trim()));
				
				String sql = "INSERT INTO `sysusers` (`userId`,`enabled`,`fullName`,`issys`,`securityDigest`,`userAccount`,`userDesc`,`userDuty`,`userPassword`) VALUES ('"+UUIDUtil.getUUID32()+"',b'1','"+install.getUserAccount().trim()+"',b'1','"+UUIDUtil.getUUID32()+"','"+install.getUserAccount().trim()+"',NULL,'?????????','"+newPassword+"')";
				
				stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				conn.commit();  
				
				
			}catch (RuntimeException e) {
				error.put("installSystem", "?????????????????????");
				
			}catch (Exception e) {
				error.put("installSystem", "???????????????????????????????????????????????????");
				if (logger.isErrorEnabled()) {
		            logger.error("???????????????????????????????????????????????????",e);
		        }
		//		e.printStackTrace();
			}finally {
				if(stmt != null){
					try {
						stmt.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
					//	e.printStackTrace();
						if (logger.isErrorEnabled()) {
				            logger.error("????????????????????????????????????Statement??????",e);
				        }
					}
				}
				
				
				if(conn != null){
					try {
						conn.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
					//	e.printStackTrace();
						if (logger.isErrorEnabled()) {
				            logger.error("??????????????????????????????????????????",e);
				        }
					}
				}
			}
		}
		
		
		
		LocalFileManage localFileManage = new LocalFileManage();
		if(error.size() ==0){
    		//????????????
			Map<String,String> copyFileMap = new HashMap<String,String>();//key:???????????????  value:???????????????
			copyFileMap.put("WEB-INF"+File.separator+"data"+File.separator+"install"+File.separator+"web.xml", "WEB-INF"+File.separator);
			for (Map.Entry<String,String> entry : copyFileMap.entrySet()) {  
				localFileManage.copyFile(entry.getKey(), entry.getValue());
			}
		}
    	
		if(error.size() ==0){
			//????????????????????????
			FileUtil.writeStringToFile("WEB-INF"+File.separator+"data"+File.separator+"install"+File.separator+"status.txt","1","utf-8",false);
		}
		
		
		
		if(error != null && error.size() >0){
			request.setAttribute("error", error);
			request.setAttribute("install", install);
			request.getRequestDispatcher("/WEB-INF/data/install/install.jsp").forward(request, response);
		}else{
			WebUtil.writeToWeb("?????????????????????????????????????????????????????????????????????", "html", response);
		}
		
	}
	

	/**
	 * ????????????????????????
	 * @return
	 */
	private boolean installSystem(){
		
		//??????????????????
    	String version = FileUtil.readFileToString("WEB-INF"+File.separator+"data"+File.separator+"install"+File.separator+"status.txt","UTF-8");
    	if(version.equals("0")){
    		return true;
    	}else{
    		return false;
    	}
	}
}

