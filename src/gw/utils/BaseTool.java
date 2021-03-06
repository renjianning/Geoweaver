package gw.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.soap.SOAPException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarOutputStream;

import gw.local.LocalSessionNixImpl;
import gw.local.LocalSessionWinImpl;


/**
 *Class BaseTool.java
 *@author ziheng
 *@time Aug 6, 2015 2:49:10 PM
 *Original aim is to support iGFDS.
 */
public class BaseTool {
	
	private static String _classpath = null;
	static Logger logger = Logger.getLogger(BaseTool.class);
	static String path_env = null;
	/**
	 * Judge whether an object is null
	 * @param obj
	 * @return
	 * true or false
	 */
	public static boolean isNull(Object obj){
		boolean isnull=false;
		if(obj==null || obj == "" || "".equals(obj)){
			isnull = true;
		}
		return isnull;
	}
	
	public static String array2String(String[] arr, String splitter) {
		
		if (arr.length > 0) {
			
		    StringBuilder nameBuilder = new StringBuilder();

		    for (String n : arr) {
		        nameBuilder.append(n).append(splitter);
		        // can also do the following
		        // nameBuilder.append("'").append(n.replace("'", "''")).append("',");
		    }

//		    nameBuilder.deleteCharAt(nameBuilder.length() - 1);

		    return nameBuilder.toString();
		    
		} else {
		    
			return "";
			
		}
		
		
	}
	
	public static String long2Date(long time) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		return sdf.format(time);
		
	}
	

	public static String toJSONString(Object value) {
		String json = null;
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

		try {
			json = ow.writeValueAsString(value);

		} catch (JsonGenerationException e) {
			e.printStackTrace();

		} catch (JsonMappingException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return json;
	}

	/**
	 * Write string to file
	 * @param content
	 * @param filepath
	 */
	public static void writeString2File(String content, String filepath){
		PrintWriter out;
		try {
			out = new PrintWriter(filepath);
			out.println(content);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Escape the reserved characters
	 * @param msg
	 * @return
	 */
	public static String escape(String msg){
		msg = msg.replaceAll("\\'", "").replaceAll("\\\n", "");
		return msg;
	}
	
	public static Document parseString(String xml){
		
		SAXReader reader = new SAXReader();
		
		Document document = null;
		
		try {
			
			InputStream stream = new ByteArrayInputStream(xml.trim().getBytes("UTF-8"));
			
			document = reader.read(stream);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
	    return document;
	    
	}
	/**
	 * Parse file from URL
	 * @param url
	 * @return
	 */
	public static Document parseURL(String url) 
    {
		
		URL myURL;
	       
        SAXReader reader = new SAXReader();
        Document document = null;
		try {

		    myURL = new URL(url);
			document = reader.read(myURL);
		} catch (Exception e) {
			e.printStackTrace();
		}
        return document;
    }
	
	
	
	/**
	 * Get Local Environment Path
	 */
	public static void getLocalPATHEnvironment() {
		
		if(isNull(path_env)) {
			
			if(OSValidator.isWindows()) {
				
				path_env = BaseTool.runCmdNoEnv("cmd.exe /C echo %PATH%");
				
			}else if(OSValidator.isMac() || OSValidator.isUnix()) {
				
				path_env = BaseTool.runCmdNoEnv("bash -c echo $PATH");
				
			}else {
				
				logger.error("This operating system is not supported as localhost.");
				
				
				
			}
			
			if(isNull(path_env)) {
				
				path_env = "/usr/bin/:/bin/:/usr/local/bin"; //use this default path
				
			}
			
//			logger.info("Got Path: " + path_env);
			
		}
		
	}
	
	/**
	 * Run the command without setting environment variables
	 * @param cmd
	 * @return
	 */
	public static String runCmdNoEnv(String cmd) {
		
		StringBuffer logrec = new StringBuffer();
		
		try {
//			String[] env = {
//					"PATH=.:/bin/:/usr/bin:/usr/local/bin:/opt/local/bin",
//					"LD_LIBRARY_PATH=.:/usr/lib:/usr/local/lib"};
			
			ProcessBuilder builder = new ProcessBuilder(cmd.split(" "));
			
			builder.directory(new File(SysDir.workspace));
			builder.redirectErrorStream(true);
			Process proc = builder.start(); // may throw IOException
//			Process proc = Runtime.getRuntime().exec(cmd.split(" "), env);
	        InputStream ips = proc.getInputStream();
	        BufferedReader brd = new BufferedReader(new InputStreamReader(ips));
	        String str = null;
	        proc.waitFor();
	        
	        int exit_value = proc.exitValue();
	       
	        do
	        {
	        	if ((str = brd.readLine()) != null)
	        	{
	        		logrec.append(str).append("\n");
	        	}
	        }
	        while ((str != null));
	       
	        System.out.println(logrec);
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
		return logrec.toString();
		
	}
	
	/**
	 * Run Local Command with local path environment
	 * @param command
	 * @return
	 */
	public static String runCmdEnv(String cmd){
		
		StringBuffer logrec = new StringBuffer();
		
		try {
			
			getLocalPATHEnvironment();
			
			ProcessBuilder builder = new ProcessBuilder(cmd.split(" "));
			if(!isNull(path_env)) {
				builder.environment().put("PATH", path_env);
				logger.info("Builder PATH Environment: " + builder.environment().get("PATH"));
			}
			builder.directory(new File(SysDir.workspace));
			builder.redirectErrorStream(true);
			Process proc = builder.start(); // may throw IOException
	        InputStream ips = proc.getInputStream();
	        BufferedReader brd = new BufferedReader(new InputStreamReader(ips));
	        String str = null;
	        proc.waitFor();
	        
	        int exit_value = proc.exitValue();
	        
	        do
	        {
	        	if ((str = brd.readLine()) != null)
	        	{
	        		System.out.println("PROC OUT: " + str);
	        		logrec.append(str).append("\n");
	        	}
	        }
	        while ((str != null));
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
		return logrec.toString();
		
	 }
	
//	 public boolean run(String script) throws InterruptedException, IOException{
//	// 	String scriptFile = "/usr/local/apache-tomcat-6.0.36/webapps/temp/"+getUUID()+".sh";
//         String scriptFile = SysDir.tempdir + getUUID()+".sh";
//         writeStringIntoFile(script, scriptFile);
//
//         Runtime.getRuntime().exec("chmod +x " + scriptFile).waitFor();
//         String[] env = {"GISBASE="+SysDir.GISBASE,
//                         "GISDBASE="+SysDir.GISDBASE,
//                         "HOME="+SysDir.HOME,
//                         "GISRC="+SysDir.GISRC,
//                         "GRASS_GUI="+SysDir.GRASS_GUI,
//                         "GIS_LOCK="+SysDir.GIS_LOCK,
//                         "PATH="+SysDir.PATH,
//                         "DYLD_LIBRARY_PATH="+SysDir.DYLD_LIBRARY_PATH,
//                         "LD_LIBRARY_PATH="+SysDir.LD_LIBRARY_PATH,
//                         "GRASS_PERL="+SysDir.GRASS_PERL,
//                         "GRASS_PAGER="+SysDir.GRASS_PAGER};
//         Runtime.getRuntime().exec("chmod +x " + scriptFile).waitFor();
//         Process proc = Runtime.getRuntime().exec(scriptFile, env);
//
//         //[GISBASE=/usr/lib/grass64/,
//         //GISDBASE=/usr/local/apache-tomcat-6.0.36/webapps/temp/gis1366695299419, 
//         //HOME=/usr/local/apache-tomcat-6.0.36/webapps/temp/gis1366695299419, 
//         //GISRC=/usr/local/apache-tomcat-6.0.36/webapps/temp/gis1366695299419/.grassrc, 
//         //GRASS_GUI=text, 
//         //GIS_LOCK=0, 
//         //PATH=/usr/lib/grass64//bin:/usr/lib/grass64//scripts:/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/bin:/usr/local/grasslib/bin:.,
//         //DYLD_LIBRARY_PATH=/usr/lib//lib:/usr/lib/grass64//lib:/lib:/usr/lib:/usr/local/lib:/usr/local/grasslib/lib, 
//         //LD_LIBRARY_PATH=/usr/lib//lib:/usr/lib/grass64//lib:/lib:/usr/lib:/usr/local/lib:/usr/local/grasslib/lib,
//         //GRASS_PERL=/usr/bin/perl, 
//         //GRASS_ORGANIZATION=LIESMARS, 
//         //GRASS_PAGER=more]
//
//       InputStream ips = proc.getInputStream();
//       InputStream eps = proc.getErrorStream();
//       BufferedReader brd = new BufferedReader(new InputStreamReader(ips));
//       BufferedReader ebrd = new BufferedReader(new InputStreamReader(eps));
//       String str = null; String estr = null;
//       proc.waitFor();
//       int exit_value = proc.exitValue();
//       StringBuffer logrec = new StringBuffer();
//       if (exit_value != 0)
//       {
//         err.append("The following error was generated while running the script:\n"  +scriptFile+ "\n");
//         do
//         {
//           if ((estr = ebrd.readLine()) == null)
//             continue;
//           err.append("PROC ERR: " + estr + "\n");
//           logrec.append( estr + "\n");
//         }
//         while ((str != null) || (estr != null));
//         System.out.println(err);
//         return false;
//       }
//
//       do
//       {
//         if ((str = brd.readLine()) != null)
//         {
//           System.out.println("PROC OUT: " + str);
//           logrec.append(str).append("\n");
//         }
//         if ((estr = ebrd.readLine()) == null)
//           continue;
//         System.out.println("PROC ERR: " + estr);
//         logrec.append(estr).append("\n");
//       }
//       while ((str != null) || (estr != null));
//       System.out.println(err);
//       log = logrec.toString();
//       return true;
// 	}

	
	/**
	 * Run Local Command
	 * @param command
	 * @return
	 */
	public static String runLocalNuxCommand(String command) {
		
		StringBuffer output = new StringBuffer();
		
		try {
			
			System.out.println("PATH enviroment: " + System.getenv("PATH"));
			
			ProcessBuilder builder = new ProcessBuilder();
    		
			builder.command("bash", "-c", command);
    		
    		builder.redirectErrorStream(true);
    		
    		Process process = builder.start();
    		
    		InputStream stdout = process.getInputStream ();
    		
            System.out.println("Local session established");
            
            BufferedReader input = new BufferedReader(new InputStreamReader(stdout));
            
            String line;
        	while ((line = input.readLine()) != null) {
        		output.append(line + "\n");
        	}

        	int exitVal = process.waitFor();
        	if (exitVal == 0) {
        		System.out.println("Success!");
        	} else {
        		//abnormal...
        		output.append("Failed");
        	}
            
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return output.toString();
		
		
	}
	
	/**
	 * Parse VCI date
	 * @param datestr
	 * @return
	 */
	public static Date parseVCIDate(String datestr){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date d = null;
		try {
			d = format.parse(datestr);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new RuntimeException("Fail to parse Date from string."+ e.getLocalizedMessage());
		}
		return d;
	}
	/**
	 * Get the root file path of CyberConnector
	 * @return
	 */
	public static String getWebAppRootPath(){
		
		String classpath = getClassPath();
		
		String rootpath = classpath;
		
		if(classpath.indexOf("WEB-INF")!=-1)
			
			rootpath = classpath.substring(0, classpath.indexOf("WEB-INF")) + "/";
		
		return rootpath;
	}
	
	/**
	 * Zip the files into a tar file
	 * @param files
	 * @param targetfile
	 */
	public static void tar(List<String> files, String targetfile) {
		
		try {
			
			// Output file stream
			FileOutputStream dest = new FileOutputStream( targetfile );
			
			// Create a TarOutputStream
			TarOutputStream out = new TarOutputStream( new BufferedOutputStream( dest ) );
			  
			// Files to tar
			for(String fp:files){
				 File f = new File(fp);
			     out.putNextEntry(new TarEntry(f, f.getName()));
			     BufferedInputStream origin = new BufferedInputStream(new FileInputStream( f ));
			     int count;
			     byte data[] = new byte[2048];
			  
			     while((count = origin.read(data)) != -1) {
			        out.write(data, 0, count);
			     }
			  
			     out.flush();
			     origin.close();
			}
			  
			out.close();
			
		}catch(Exception e) {
			
			e.printStackTrace();
			throw new RuntimeException("Fail to tar the files" + e.getLocalizedMessage());
			
		}
		
	}
	
	/**
	 * Get classpath
	 * @return
	 * class path
	 */
	public static String getClassPath(){
		if(isNull(_classpath)){
			String dir = new BaseTool().getClass().getClassLoader().getResource("").getPath();
			try {
				dir = URLDecoder.decode(dir,"utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			_classpath = dir;
		}
		return _classpath;
	}
	
	/**
     * 
     * @param savedir
     * @param imgUrl
     */
	public static void down(String savedir, String imgUrl) {
		File f = new File(savedir);
        byte[] buffer = new byte[8 * 1024];
        URL u;
        URLConnection connection = null;
        try {
                u = new URL(imgUrl);
                connection = u.openConnection();
        } catch (Exception e) {
                logger.info("ERR:" + imgUrl);
                return;
        }
        connection.setReadTimeout(1000000); //milliseconds
        InputStream is = null;
        FileOutputStream fos = null;
        try {
                f.createNewFile();
                is = connection.getInputStream();
                fos = new FileOutputStream(f);
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                }

        } catch (Exception e) {
                f.delete();
                if (fos != null) {
                        try {
                                fos.close();
                        } catch (IOException e1) {
                        	e1.printStackTrace();
                        }
                }
                if (is != null) {
                        try {
                                is.close();
                        } catch (IOException e1) {
                        	e1.printStackTrace();
                        }
                }
                throw new RuntimeException("Fail to download the image from the link.."+e.getClass().getName()+":"+e.getLocalizedMessage());
        }
        logger.info(imgUrl+" is downloaded!");
//        buffer = null;
        // System.gc();
	}
	
	/**
	 * Download file through URI
	 * @param uri
	 * @param tempurl
	 * @param tempdir
	 * @return
	 */
	public static String[] downloadURI(String uri, String tempurl, String tempdir){
		String tempName = uri.substring(uri.lastIndexOf("/")+1);
		String tempfilepath = tempdir+tempName;
		if(!uri.startsWith(tempurl)&&!new File(tempfilepath).exists()){
			logger.info("Begin dowloading the image from the link..");
			logger.info("File URI: "+uri);
			if(uri.startsWith("http")){
				down(tempfilepath, uri);
			}else{
//				wget(tempfilepath, uri);
				throw new RuntimeException("The input file url is not by http protocal.");
			}
	    	
	    	logger.info("File is saved to:" + tempfilepath);
	    	logger.info("Download ends successfully.");
		}else{
			logger.info("The file from the link "+uri+"already exists on the server..");
		}
		String[] urianddir = new String[2];
		urianddir[0] = tempurl+tempName;
		urianddir[1] = tempfilepath;
		return urianddir;
	}
	
	public static String cacheDataLocally(String url) {
		
		if(url.startsWith(SysDir.PREFIXURL)){
			
			return url;
			
		}
		
		String folderpath = BaseTool.getWebAppRootPath() + SysDir.upload_file_path + "/";
		
		String folderuri = SysDir.PREFIXURL + "/CyberConnector/" + SysDir.upload_file_path + "/";
		
		String[] fieldurianddir = downloadURI(url, folderuri, folderpath);
    	
		return fieldurianddir[0];
		
	}
	
	public static String reducePath(String path) {
		
		if(path.indexOf("..")!=-1) {
			
			Path filepath = Paths.get(path);
		    path = filepath.normalize().toString().replaceAll("\\\\", "/");
		}
		
		return path;
		
	}
	
	/**
	 * Cache Data on Server
	 * @param url
	 * @return
	 */
	public static String cacheData(String url){
		
		String resp = null;
		
		String cachedurl =null;
		
		if(url.startsWith(SysDir.CACHE_SERVICE_URL)){
			
			return url;
			
		}
		
		try {
			String req = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:cac=\"http://cache.cube.ws.csiss.gmu.edu\"> "+
		   " <soapenv:Header/> "+
		   " <soapenv:Body> "+
		   "   <cac:cacheElement> "+
		   "      <cac:rawDataURL>" + url + "</cac:rawDataURL> "+
		   "      <cac:lasting>whatever</cac:lasting> "+ //this option is meaningless for now
		   "   </cac:cacheElement> "+
		   " </soapenv:Body> "+
		   "</soapenv:Envelope>";
			
			SOAPClient client = new SOAPClient();
			
			client.setEndpoint(SysDir.CACHE_SERVICE_URL);
			
			client.setSoapmessage(req);
			client.send();
			resp = client.getRespmessage();
			
			Document doc = BaseTool.parseString(resp);
			
			if(doc==null){
				
				throw new RuntimeException("Fail to cache data onto server.");
				
			}
			
			Map map = new HashMap();
			
			map.put("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
			
			map.put("cache", "http://cache.cube.ws.csiss.gmu.edu");
			
			XPath cacheurlpath = DocumentHelper.createXPath("//soapenv:Envelope/soapenv:Body/cache:cacheResponse/cache:cacheURL");
			
			cacheurlpath.setNamespaceURIs(map);
			
			Node cachenode = cacheurlpath.selectSingleNode(doc);
			
			cachedurl = cachenode.getText();
			
		} catch (SOAPException e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("Fail to cache data on server. SOAP servcie failure.");
			
		}
		
		return cachedurl;
		
	}
	
	/**
	 * Read the string from a file
	 * @param path
	 * @return
	 */
	public static String readStringFromFile(String path){
		StringBuffer strLine = new StringBuffer();
		try{
			  // Open the file that is the first 
			  // command line parameter
			  FileInputStream fstream = new FileInputStream(path);
			  // Get the object of DataInputStream
			  DataInputStream in = new DataInputStream(fstream);
			  FileReader fr = new FileReader(path);
			  BufferedReader br = new BufferedReader(fr);
			  String str = null;
			  //Read File Line By Line
			  while ((str = br.readLine()) != null)   {
			  // Print the content on the console
				  strLine.append(str).append("\n");
//				  System.out.println (strLine);
			  }
			  //Close the input stream
			  in.close();
		}catch (Exception e){
			  //Catch exception if any
			  System.err.println("Error: " + e.getMessage());
	    }
		return strLine.toString().trim();
	}
	/**
	 * Get day number between two dates
	 * @param b
	 * @param e
	 * @return
	 */
	public int getDaysBetweenTwoDates(Date b, Date e){
		return (int)((b.getTime()-e.getTime())/(1000 * 60 * 60 * 24));
	}
	/**
	 * Parse string from input stream
	 * @param in
	 * @return
	 */
	public String parseStringFromInputStream(InputStream in){
	        String output = null;
	        try{
	                // WORKAROUND cut the parameter name "request" of the stream
	                BufferedReader br = new BufferedReader(new 
	                                InputStreamReader(in,"UTF-8"));
	                StringWriter sw = new StringWriter();
	                int k;
	                while ((k = br.read()) != -1) {
	                        sw.write(k);
	                }
	                output = sw.toString();
	
	        }catch(Exception e){
	                e.printStackTrace();
	        }finally{
	                try{
	                        in.close();
	                }catch(Exception e1){
	                        e1.printStackTrace();
	                }
	        }
	        return output;
	}
	/**
	 * Read document from string
	 * @param xmlstring
	 * @return
	 */
	public Document readDocumentFromString(String xmlstring){
        Document doc = null;
        try{
                doc  = DocumentHelper.parseText(xmlstring.trim());
        }catch(Exception e){
                throw new RuntimeException("Fail to read document from string:"+xmlstring);
        }
        return doc;
	}
	/**
	 * Read element from string
	 * @param xmlstring
	 * @return
	 */
	public  Element readElementFromString(String xmlstring){
	        Element ele = null;
	        try{
	                Document doc  = DocumentHelper.parseText(xmlstring.trim());
	                ele = doc.getRootElement();
	        }catch(Exception e){
	                throw new RuntimeException("Fail to read element from string:"+xmlstring);
	        }
	        return ele;
	}
	/**
     * Convert string to input stream
     * @param str
     * @return
     * @throws IOException 
     */
    public InputStream convertString2InputStream(String str) throws IOException{
        InputStream stream = IOUtils.toInputStream(str, "UTF-8");
        return stream;
    }
	/**
     * Modified into a safer method which can capture the fault response SOAP message
     * @param param
     * @param operationname
     * @param input_url
     * @return 
     */
    public  String AgentSOAP(String param, String operationname,String input_url){
        
    	String resp = null;
        
        try {
            
        	SOAPClient client = new SOAPClient();
            
            client.setEndpoint(input_url);
            
            client.setSoapmessage(param);
            
            client.send();
            
            resp =  client.getRespmessage();
            
        } catch (SOAPException ex) {
        
        	String errmsg = ex.getClass().getName()+ex.getLocalizedMessage();
            
        	throw new RuntimeException(errmsg);
        
        }
        
        return resp;
    }
    /**
     * Get the DATETIME format of current time
     * @return
     * DATETIME
     */
    public static String getCurrentMySQLDatetime(){
    	java.util.Date dt = new java.util.Date();
    	java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String currentTime = sdf.format(dt);
    	return currentTime;
    }
    /**
     * add by Ziheng Sun on 10/17/2015
     * @param address
     * @param error
     * @return
     */
    public boolean sendUserAErrorMail(String orderid, String address, String error){
    	
    	String content = this.readStringFromFile(this.getClassPath()+"error_notice.txt");
    	
    	content = content.replace("[error]", error).replace("[orderid]", orderid);
    	
    	return notifyUserByEmail(address, content);
    	
    }
    /**
     * add by Ziheng Sun on 10/17/2015
     * @param address
     * @param response
     * @return
     */
    public boolean sendUserAResultMail(String orderid, String address, String response){
    	
    	String content = this.readStringFromFile(this.getClassPath()+"resp_notice.txt");
    	
    	content = content.replace("[result]", response).replace("[orderid]", orderid);
    	
    	return notifyUserByEmail(address, content);
    }
    /**
     * add by Ziheng Sun on 10/17/2015
     * @param address
     * @param orderid
     * @return
     */
    public boolean sendUserAOrderNotice(String address, String orderid){
    	
    	String content = this.readStringFromFile(this.getClassPath()+"order_notice.txt");
    	
    	content = content.replace("[orderid]", orderid);
    	
    	logger.info(content);
    	
    	return notifyUserByEmail(address, content);
    }
	/**
     * Notify user by E-mail
     * Use .10 7001 notification service
     * @param email
     * @param content
     * @return 
     */
    public boolean notifyUserByEmail(String address, String content){
            boolean success =false;
            String req = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:not=\"http://NotificationService.grass.ws.laits.gmu.edu\">\n" +
                                "   <soapenv:Header/>\n" +
                                "   <soapenv:Body>\n" +
                                "      <not:notifyElement>\n" +
                                "	    <not:token>^||^</not:token>\n" +
                                "         <not:content>"+content+"</not:content>\n" +
                                "         <not:sendto>"+address.trim()+"</not:sendto>\n" +
                                "      </not:notifyElement>\n" +
                                "   </soapenv:Body>\n" +
                                "</soapenv:Envelope>";
            String datainforesp = AgentSOAP(req, SysDir.NOTIFICATION_EMAIL, SysDir.NOTIFICATION_EMAIL_SERVICE_URL);
//            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
//   <soapenv:Body>
//      <notifyResponse xmlns="http://NotificationService.grass.ws.laits.gmu.edu">
//         <return>Send</return>
//      </notifyResponse>
//   </soapenv:Body>
//</soapenv:Envelope>
            System.out.println("Parsing the response...");
            Document doc = readDocumentFromString(datainforesp);
            XPath xpath = doc.createXPath("//soapenv:Envelope/soapenv:Body/ser:notifyResponse/ser:return" );
            Map nsmap = new HashMap();
            nsmap.put("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
            nsmap.put("ser", "http://NotificationService.grass.ws.laits.gmu.edu");
            xpath.setNamespaceURIs(nsmap);
            Node node = xpath.selectSingleNode(doc);
            String txturl = node.getText();
//            List nodes = xpath.selectNodes(doc);
//            String txturl = null;
//            for(int i=0;i<nodes.size();i++){
//                Node node = (Node)nodes.get(i);
//                txturl = node.getText();
//            }
            if(isNull(txturl)||!txturl.equals("Send")){
                System.out.println("Cann't set notification to the address :"+address);
            }else{
                success = true;
                System.out.println("Notification E-mail is sent.");
            }
            return success;
    }
    /**
     * Post long time request
     * @param param
     * @param input_url
     * @return
     * 
     */
    public static String Longtime_POST(String param, String input_url){
    	String resp = null;
    	try{
    		URL url = new URL(input_url);	      
            HttpURLConnection con =(HttpURLConnection)url.openConnection();
            con.setDoOutput(true); 
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/xml");
            con.setConnectTimeout(36*60*60*1000); //extend the waiting time to 36 hours
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setUseCaches(false);

            PrintWriter xmlOut = new PrintWriter(con.getOutputStream());
            xmlOut.write(param);   
            xmlOut.flush();
            BufferedReader response = new BufferedReader(new InputStreamReader(con.getInputStream())); 
            String result = "";
            String line;
            while((line = response.readLine())!=null){
                result += "\n" + line;
            }
            resp =  result.toString();  
    	}catch(Exception e){
    		e.printStackTrace();
    		throw new RuntimeException("Cann't send messages to "+input_url+". Reason: "+e.getLocalizedMessage());
    	}
    	return resp;
    }
	/**
	 * send a HTTP POST request
	 * @param param
	 * @param input_url
	 * @return
	 */
	public static  String POST(String param,String input_url){
        try {
                URL url = new URL(input_url);	      
                HttpURLConnection con =(HttpURLConnection)url.openConnection();
                con.setDoOutput(true); 
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/xml");
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setUseCaches(false);

                PrintWriter xmlOut = new PrintWriter(con.getOutputStream());
                xmlOut.write(param);   
                xmlOut.flush();
                BufferedReader response = new BufferedReader(new InputStreamReader(con.getInputStream())); 
                String result = "";
                String line;
                while((line = response.readLine())!=null){
                    result += "\n" + line;
                }
                return result.toString();  
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Cann't send messages to "+input_url+". Reason: "+e.getLocalizedMessage());
            }
    }
	/**
	 * Main Entry
	 * @param args
	 */
	public static final void main(String[] args){
		BaseTool tool = new BaseTool();
//		tool.notifyUserByEmail("szhwhu@gmail.com", "A data product link.");
//		tool.sendUserAOrderNotice("szhwhu@gmail.com", "sdfdsfewewfrewrfewrvcvdfde");
//		tool.sendUserAResultMail("", "zsun@gmu.edu", "");
		
//		String url = BaseTool.cacheData("http://thredds.ucar.edu/thredds/fileServer/grib/NCEP/NDFD/NWS/CONUS/CONDUIT/NDFD_NWS_CONUS_conduit_2p5km_20170613_1800.grib2");
		
		String path = BaseTool.getWebAppRootPath();
		
		System.out.println(path);
		
	}
}
