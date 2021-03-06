package com.tigerjoys.shark.miai.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.shark.miai.common.enums.AppNameEnum;
import org.shark.miai.common.util.QRCodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.tigerjoys.nbs.common.ActionResult;
import com.tigerjoys.nbs.common.beans.Produce;
import com.tigerjoys.nbs.common.utils.JsonHelper;
import com.tigerjoys.nbs.common.utils.Tools;
import com.tigerjoys.nbs.mybatis.core.page.PageModel;
import com.tigerjoys.nbs.mybatis.core.sql.Restrictions;
import com.tigerjoys.nbs.web.annotations.FilterHeader;
import com.tigerjoys.nbs.web.annotations.Login;
import com.tigerjoys.nbs.web.annotations.NoLogin;
import com.tigerjoys.nbs.web.annotations.TestEncrypt;
import com.tigerjoys.nbs.web.context.BeatContext;
import com.tigerjoys.nbs.web.context.RequestUtils;
import com.tigerjoys.shark.miai.Const;
import com.tigerjoys.shark.miai.agent.IProxyAgent;
import com.tigerjoys.shark.miai.agent.ISysConfigAgent;
import com.tigerjoys.shark.miai.agent.IUserAgent;
import com.tigerjoys.shark.miai.agent.IUserIncomeAgent;
import com.tigerjoys.shark.miai.agent.dto.UserBO;
import com.tigerjoys.shark.miai.agent.dto.VacuateConfigDto;
import com.tigerjoys.shark.miai.enums.ErrorCodeEnum;
import com.tigerjoys.shark.miai.inter.contract.IIosDistributionDetailContract;
import com.tigerjoys.shark.miai.inter.contract.IShareVipCardContract;
import com.tigerjoys.shark.miai.inter.entity.IosDistributionDetailEntity;
import com.tigerjoys.shark.miai.inter.entity.ShareVipCardEntity;
import com.tigerjoys.shark.miai.inter.entity.UserInviteMappingEntity;
import com.tigerjoys.shark.miai.service.IUserInviteService;
import com.tigerjoys.shark.miai.utils.ServiceHelper;

/**
 * 代理和分享相关接口
 * 
 * @author yangjunming
 *
 */
@Controller
@RequestMapping(value = "/web", produces = Produce.TEXT_HTML)
//@TestEncrypt("naihnwhQOxMW3SSDjwBCalG8cAb7Z0o2/Vb65AsQC87EDBZACqpfpBvRClXVo0ejLhaGHw2TzHgXMG2eOSXxpWAJJ6KIJ+uZ8xEnRGBS9zZ7wjsdi7CFCZv/nthy18Wide7w7SsKN1if0eLL6hhU1ebsJCsA0gpuijLhgsKp21epnm4J7Wjqk7f1xqGNyZLUOmFmw8UZRvZPgD3XcoHQUS1ZoaV5wr/tNjqXGlcrzqb7Cr+b0S785LS0YfjZsrzCOLn9VgHCjvtMJqE7KHohXxAWRsdLz3g1iBA/rz9hdaPwreQq9Fcz1aObr6Qr5DMUYRRp0Qjxj1fKSlym8/qPwDvTIx+1M3J40166MBUwb9GhCu0h7/aUHtiXBBNUeP6XCZjVLjOW4uXoxFPA9xZvgYIwRy05+gomCLXSuLtdFCC+Z/uGFEGUN87cyu4sjDK8MHGjrbpsDzzqKtfYkgg2ZQ7nneqiiTw8ILt2snWUfeWUaEIRdhKZHjrx0EPoxKyc")
public class WebProxyShareController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private IUserAgent userAgent;

	@Autowired
	private IProxyAgent proxyAgent;

	@Autowired
	private IUserInviteService userInviteService;

	
	@Autowired
	private IShareVipCardContract shareVipCardContract;
	
	@Autowired
	private IIosDistributionDetailContract iosDistributionDetailContract;
	
	@Autowired
	private IUserIncomeAgent userIncomeAgent;
	
	
	@Autowired
	private ISysConfigAgent sysConfigAgent;

	

	/**
	 * 我的分享页面
	 * 
	 * @return
	 * @throws Exception
	 */
	@Login
	@RequestMapping(value = "/BshareIndex", produces = Produce.TEXT_HTML)
	public String bShareIndex(Model model) throws Exception {
		BeatContext context = RequestUtils.getCurrent();
		Long userId = context.getUserid();
		
		UserBO uesr = userAgent.findById(userId);
		String path = RequestUtils.getCurrent().getHeader().getPackageName();
		UserInviteMappingEntity mapping = userInviteService.getUserDefaultInviteCode(userId,path);
		String shareUrl = Const.WEB_SITE + "/web/shareDownload/"
				+ (mapping == null ? "" : mapping.getInvitecode());
		int osType = RequestUtils.getCurrent().getHeader().getOs_type();
		//int osType = 1;
		String appName = "V密";
		if( 1 == osType){
			
			//String path = "com.ydwx.yoyo3";
			appName = getAppName(path);
		}else if( 2 == osType){
			path = RequestUtils.getCurrent().getHeader().getChannel();
			appName = getAppName(path);
		}
		logger.info("QRCode_SORUSE:"+shareUrl);
		String qrCode = QRCodeUtil.getBase64Str(shareUrl);
		model.addAttribute("QRCode", qrCode);
		model.addAttribute("inviteCode", mapping.getInvitecode());
		model.addAttribute("shareTitle", appName+"---邀您一起去约会");
		model.addAttribute("appName", appName);
		model.addAttribute("shareInfo", (uesr != null ? uesr.getNickname() : "") + "邀请您一起去约会哦，和女神来场浪漫邂逅~");
		model.addAttribute("shareUrl", shareUrl);
		model.addAttribute("inviteNum", mapping.getInvite_num());
		model.addAttribute("invitIncomes", Tools.formatDouble2PercentToString(userIncomeAgent.getInvitationIncomes(userId)));
		
		model.addAttribute("encrypt", RequestUtils.getCurrent().getHeaderEncrypt());
		String packageName = RequestUtils.getCurrent().getHeader().getPackageName();
		if(AppNameEnum.ios_com_yo_miliao.getPackageName().equals(packageName)
				|| AppNameEnum.ios_com_yo_miliaoliao.getPackageName().equals(packageName)
				|| AppNameEnum.ios_com_hailiao_hailiao.getPackageName().equals(packageName)
				|| AppNameEnum.ios_com_ailiao_ailiao.getPackageName().equals(packageName)
				|| AppNameEnum.ios_com_vmiliao_vmiliao.getPackageName().equals(packageName)
				|| AppNameEnum.ios_com_paopao_paopao.getPackageName().equals(packageName)
				){
			return "share/localShare_ios";
		}
		
		return "share/localShare";
		
	}

	/**
	 * IOS分享下载页
	 * 
	 * @return
	 * @throws Exception
	 */
	@FilterHeader
	@NoLogin
	@RequestMapping(value = "/shareDownload/{inviteCode}/{path}", produces = Produce.TEXT_HTML)
	public String shareDownloadIos(@PathVariable String inviteCode, @PathVariable String path, Model model)
			throws Exception {
		model.addAttribute("inviteCode", inviteCode);
		model.addAttribute("appName", getAppName(path));
		return "share/shareOut";

	}
	
	//------------------------------------------------------------------
	/**
	 * APK我的分享页面
	 * 
	 * @return
	 * @throws Exception
	 */
	@Login
	@RequestMapping(value = "/userShareApp", produces = Produce.TEXT_HTML)
	public String userShareApp(Model model) throws Exception {
		BeatContext context = RequestUtils.getCurrent();
		Long userId = context.getUserid();
		String path = RequestUtils.getCurrent().getHeader().getPackageName();
		String appName = Const.getNewAppName(path);
		if(AppNameEnum.andriod_com_tjhj_dvzs.getPackageName().equals(path) || AppNameEnum.ios_com_zdkj_lttool.getPackageName().equals(path)){
			appName = "V密-视频聊天";
		}
		UserBO uesr = userAgent.findById(userId);
		UserInviteMappingEntity mapping = userInviteService.getUserDefaultInviteCode(userId,path);
		String shareUrl = Const.WEB_SITE + "/web/userShareAppUrl/"+path+"/"
				+ (mapping == null ? "nocode" : mapping.getInvitecode())+"?kkk="+System.currentTimeMillis();
		
		logger.info("QRCode_SORUSE:"+shareUrl);
		String qrCode = QRCodeUtil.getBase64Str(shareUrl);
		model.addAttribute("QRCode", qrCode);
		model.addAttribute("inviteCode", mapping.getInvitecode());
		model.addAttribute("shareTitle", appName+"---邀您一起去约会");
		model.addAttribute("appName", appName);
		model.addAttribute("shareInfo", (uesr != null ? uesr.getNickname() : "") + "邀请您一起去约会哦，和女神来场浪漫邂逅~");
		model.addAttribute("shareUrl", shareUrl);
		model.addAttribute("inviteNum", mapping.getInvite_num());
		model.addAttribute("invitIncomes", Tools.formatDouble2PercentToString(userIncomeAgent.getInvitationIncomes(userId)));
		model.addAttribute("encrypt", RequestUtils.getCurrent().getHeaderEncrypt());
		
		VacuateConfigDto config = sysConfigAgent.vacuate();
		model.addAttribute("rechargeIncome", config.getRechargeIncome());
		model.addAttribute("proxyRatio", config.getProxyRatio());
		
		if( AppNameEnum.andriod_com_tjhj_miliao.getPackageName().equalsIgnoreCase(path)){
			String cpsUrl = Const.WEB_SITE + "/web/invite/cps/downUrl/"+path+"/"
					+ (mapping == null ? "nocode" : mapping.getInvitecode())+"?kkk="+System.currentTimeMillis();
			model.addAttribute("shareUrl", cpsUrl);
			model.addAttribute("shareTitle", "蜜聊-高颜值小姐姐和你视频聊天，邀请码:"+mapping.getInvitecode());
			model.addAttribute("shareInfo", "免费获得体验机会，填写邀请码："+mapping.getInvitecode());
			return "miliao_share/commission";
		}
		
		return "share/userShareApk";
		
	}

	/**
	 * APK分享下载页
	 * 
	 * @return
	 * @throws Exception
	 */
	@FilterHeader
	@NoLogin
	@RequestMapping(value = "/userShareAppUrl/{path}/{inviteCode}", produces = Produce.TEXT_HTML)
	public String userShareApk( @PathVariable String path,@PathVariable String inviteCode ,Model model)
			throws Exception {
		model.addAttribute("inviteCode", inviteCode);
		model.addAttribute("appKey", "kt4s9i");
		model.addAttribute("appName",  Const.getNewAppName(path));
		PageModel pageModel = PageModel.getPageModel();
		String fileFix = "4";   // 写死APP
		pageModel.addQuery(Restrictions.eq("file_prefix", fileFix));
		List<IosDistributionDetailEntity> detailPath = iosDistributionDetailContract.load(pageModel);
		if(Tools.isNotNull(detailPath)){
			for(IosDistributionDetailEntity re:detailPath){
				if(re.getOs_type() ==2){
					model.addAttribute("iosPath", ServiceHelper.getIOSPlist(re.getPlist_path()));
				}else{
					model.addAttribute("apkPath", ServiceHelper.getCdnVideo(re.getIos_app_path()));
				}
			}
			
		}
		
		return "share/userShareApkDown";

	}

	
	
	/**
	 * APK测试下载页
	 * 
	 * @return
	 * @throws Exception
	 */
	@FilterHeader
	@NoLogin
	@RequestMapping(value = "/testShareAppUrl/{fileFix}/{path}/{inviteCode}", produces = Produce.TEXT_HTML)
	public String testShareAppUrl( @PathVariable String fileFix,@PathVariable String path,@PathVariable String inviteCode ,Model model)
			throws Exception {
		model.addAttribute("inviteCode", inviteCode);
		model.addAttribute("appKey", "kt4s9i");
		model.addAttribute("appName",  Const.getNewAppName(path));
		PageModel pageModel = PageModel.getPageModel();
		pageModel.addQuery(Restrictions.eq("file_prefix", fileFix));
		List<IosDistributionDetailEntity> detailPath = iosDistributionDetailContract.load(pageModel);
		if(Tools.isNotNull(detailPath)){
			for(IosDistributionDetailEntity re:detailPath){
				if(re.getOs_type() ==2){
					model.addAttribute("iosPath", ServiceHelper.getIOSPlist(re.getPlist_path()));
				}else{
					model.addAttribute("apkPath", ServiceHelper.getCdnVideo(re.getIos_app_path()));
				}
			}
			
		}
		
		return "share/userShareApkDown";

	}
	
	//------------------------------------------------------------------


	/**
	 * 设置邀请码
	 * 
	 * @param body
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/shareInviteCode", method = RequestMethod.POST, produces = Produce.TEXT_ENCODE)
	@ResponseBody
	public ActionResult shareInviteCode(@RequestBody String body) throws Exception {
		BeatContext context = RequestUtils.getCurrent();
		Long userId = context.getUserid();
		JSONObject json = JsonHelper.toJsonObject(body);
		String inviteCode = json.getString("inviteCode");
		ActionResult result = proxyAgent.addInvitation(userId, inviteCode);
		return result;
	}
	
	private String getAppName(String path){
		Map<String, String> hsmp = new HashMap<>();
		hsmp.put("iOS_V0", "V密");
		hsmp.put("iOS_V6", "蜜聊");
		hsmp.put("iOS_V7", "密约");
		hsmp.put("iOS_V8", "蜜聊");
		hsmp.put("iOS_V9", "觅聊");
		hsmp.put("iOS_V10", "爱聊");
		hsmp.put("iOS_V13", "V密聊");
		hsmp.put("iOS_V14", "泡泡");
		hsmp.put("com.ydwx.yoyo", "V密");
		hsmp.put("com.tjhj.miliao", "蜜聊");
		hsmp.put("com.ydwx.yoyo3", "密约");
		return hsmp.get(path);
	}
	
	/**
	 * IOS分享下载页
	 * 
	 * @return
	 * @throws Exception
	 */
	@FilterHeader
	@NoLogin
	@RequestMapping(value = "/shareVipCard", produces = Produce.TEXT_JSON)
	@ResponseBody
	public ActionResult shareKTVCard(@RequestBody(required=false) String body,HttpServletRequest request,HttpServletResponse response)throws Exception {
		response.setHeader("Access-Control-Allow-Origin", Const.IS_TEST?"*":"http://www.imyoyo.com.cn");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers",
				"Origin, No-Cache, X-Requested-With, If-Modified-Since, Pragma, Last-Modified, Cache-Control, Expires, Content-Type, X-E4M-Wit");
		System.err.println("shareVipCard_data:"+body);
		if("options".equalsIgnoreCase(request.getMethod())){
			return ActionResult.success();
		}
		JSONObject json = JsonHelper.toJsonObject(body);
		String telNumStr = json.getString("telNum");
		String inviteCodeStr = json.getString("inviteCode");
		
		if(Tools.isNotNull(telNumStr) && Tools.isNotNull(inviteCodeStr)){
			String regex = "^((13[0-9])|(14[5,7])|(15[0-3,5-9])|(17[0,3,5-8])|(18[0-9])|166|198|199|(147))\\d{8}$";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(telNumStr);
	        boolean isMatch = m.matches();
	        if(!isMatch){
	        	return ActionResult.fail(ErrorCodeEnum.share_vip_moblie_error);
	        }
			Long inviteCode = Tools.parseLong(inviteCodeStr);
			if(inviteCode>=80001 && inviteCode<=81000){
				ShareVipCardEntity entity = shareVipCardContract.findByProperty("mobile", telNumStr);
				if(Tools.isNull(entity)){
					entity = new ShareVipCardEntity();
					entity.setMobile(telNumStr);
					entity.setStatus(0);
					shareVipCardContract.insert(entity);
				}
			}else{
				return ActionResult.fail(ErrorCodeEnum.share_vip_invite_error);
			}
		}
		
		return ActionResult.success();

	}
	//------------------------------------------------------------------
	/**
	 * IOS密聊分享页面
	 * 
	 * @return
	 * @throws Exception
	 */
	@Login
	@RequestMapping(value = "/shareShow", produces = Produce.TEXT_HTML)
	public String shareShow(Model model) throws Exception {
		BeatContext context = RequestUtils.getCurrent();
		Long userId = context.getUserid();
		String path = RequestUtils.getCurrent().getHeader().getPackageName();
		UserBO uesr = userAgent.findById(userId);
		String packageName = RequestUtils.getCurrent().getHeader().getPackageName();
		UserInviteMappingEntity mapping = userInviteService.getUserDefaultInviteCode(userId,path);
		String shareUrl = Const.WEB_SITE + "/web/shareOutUrl/"+packageName+"/"
				+ (mapping == null ? "" : mapping.getInvitecode());
		int osType = RequestUtils.getCurrent().getHeader().getOs_type();
		String appName = Const.getNewAppName(packageName);
		model.addAttribute("inviteCode", mapping.getInvitecode());
		model.addAttribute("shareTitle", appName+"---邀您一起去约会");
		model.addAttribute("appName", appName);
		model.addAttribute("shareInfo", (uesr != null ? uesr.getNickname() : "") + "邀请您一起去约会哦，和女神来场浪漫邂逅~");
		model.addAttribute("shareUrl", shareUrl);
		return "share/localShare_androidSimple";
		
	}

	
	/**
	 * IOS分享下载页
	 * 
	 * @return
	 * @throws Exception
	 */
	@FilterHeader
	@NoLogin
	@RequestMapping(value = "/shareOutUrl/{path}/{inviteCode}", produces = Produce.TEXT_HTML)
	public String shareOut( @PathVariable String path,@PathVariable String inviteCode ,Model model)
			throws Exception {
		model.addAttribute("inviteCode", inviteCode);
		model.addAttribute("appName",  Const.getNewAppName(path));
		return "share/shareOut_androidSimple";

	}
	//------------------------------------------------------------------
	
	/**
	 * IOS分享下载页
	 * 
	 * @return
	 * @throws Exception
	 */
	@FilterHeader
	@NoLogin
	@RequestMapping(value = "/distributionApp/{fileFix}", produces = Produce.TEXT_HTML)
	public String distributionApp( @PathVariable String fileFix ,Model model)
			throws Exception {
			PageModel pageModel = PageModel.getPageModel();
			pageModel.addQuery(Restrictions.eq("file_prefix", fileFix));
			List<IosDistributionDetailEntity> detailPath = iosDistributionDetailContract.load(pageModel);
			if(Tools.isNotNull(detailPath)){
				for(IosDistributionDetailEntity re:detailPath){
					if(re.getOs_type() ==2){
						model.addAttribute("distributionPath", ServiceHelper.getIOSPlist(re.getPlist_path()));
					}else{
						model.addAttribute("apkPath", ServiceHelper.getCdnVideo(re.getIos_app_path()));
					}
				}
				
			}
		
		
		return "share/distributionApp";

	}
	
	
	
	/**
	 * 蒲公英下载页
	 * 
	 * @return
	 * @throws Exception
	 */
	@FilterHeader
	@NoLogin
	@RequestMapping(value = "/pugongyingApp", produces = Produce.TEXT_HTML)
	public String pugongyinApp(Model model)
			throws Exception {
		
		return "share/pugongyingAppDown";

	}

	/**
	 * 蒲公英下载页2
	 * 
	 * @return
	 * @throws Exception
	 */
	@FilterHeader
	@NoLogin
	@RequestMapping(value = "/pugongyingAppUrl", produces = Produce.TEXT_HTML)
	public String pugongyingAppUrl(Model model)
			throws Exception {
		
		return "share/pugongyingAppUrl";

	}
	
	
	/**
	 * 密聊下载
	 * 
	 * @return
	 * @throws Exception
	 */
	@FilterHeader
	@NoLogin
	@RequestMapping(value = "/milaoAppUrl", produces = Produce.TEXT_HTML)
	public String milaoAppUrl(Model model)
			throws Exception {
		
		return "share/milao_wx_app_down";

	}
	
	/**
	 * 密聊新版下载
	 * 
	 * @return
	 * @throws Exception
	 */
	@FilterHeader
	@NoLogin
	@RequestMapping(value = "/milaoNewAppUrl", produces = Produce.TEXT_HTML)
	public String milaoNewAppUrl(Model model)
			throws Exception {
		
		return "share/milao_wx_app_down_QW";

	}
	
	
}
