package com.tigerjoys.shark.miai.service.impl;

import java.text.DecimalFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.shark.miai.common.delayqueue.Message;
import org.shark.miai.common.enums.IndexActivityAreaEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tigerjoys.nbs.common.ActionResult;
import com.tigerjoys.nbs.common.cache.CacheRedis;
import com.tigerjoys.nbs.common.utils.ExecutorServiceHelper;
import com.tigerjoys.nbs.common.utils.JsonHelper;
import com.tigerjoys.nbs.common.utils.MD5;
import com.tigerjoys.nbs.common.utils.Tools;
import com.tigerjoys.nbs.common.utils.sequence.IdGenerater;
import com.tigerjoys.nbs.mybatis.core.page.PageModel;
import com.tigerjoys.nbs.mybatis.core.sql.Projections;
import com.tigerjoys.nbs.mybatis.core.sql.Restrictions;
import com.tigerjoys.nbs.web.context.RequestHeader;
import com.tigerjoys.nbs.web.context.RequestUtils;
import com.tigerjoys.shark.miai.Const;
import com.tigerjoys.shark.miai.RedisCacheConst;
import com.tigerjoys.shark.miai.agent.IAnchorDefendAgent;
import com.tigerjoys.shark.miai.agent.IAnchorDynamicPriceAgent;
import com.tigerjoys.shark.miai.agent.IIOSUserSmsAgent;
import com.tigerjoys.shark.miai.agent.INeteaseAgent;
import com.tigerjoys.shark.miai.agent.IPayUserAgent;
import com.tigerjoys.shark.miai.agent.ISysConfigAgent;
import com.tigerjoys.shark.miai.agent.IUserAgent;
import com.tigerjoys.shark.miai.agent.IUserDiamondAgent;
import com.tigerjoys.shark.miai.agent.IUserFriendAgent;
import com.tigerjoys.shark.miai.agent.IUserOnlineStateAgent;
import com.tigerjoys.shark.miai.agent.IUserOrdinaryOnlineListAgent;
import com.tigerjoys.shark.miai.agent.IUserPointAgent;
import com.tigerjoys.shark.miai.agent.IUserSubscribeAnchorAgent;
import com.tigerjoys.shark.miai.agent.IUserWeekCardAgent;
import com.tigerjoys.shark.miai.agent.IVchatRoomSettlementAgent;
import com.tigerjoys.shark.miai.agent.constant.AgentRedisCacheConst;
import com.tigerjoys.shark.miai.agent.dto.UserBO;
import com.tigerjoys.shark.miai.agent.dto.VChatTCPInfoNotifyDto;
import com.tigerjoys.shark.miai.agent.dto.VChatVideoTCPDto;
import com.tigerjoys.shark.miai.agent.dto.VacuateConfigDto;
import com.tigerjoys.shark.miai.agent.dto.VchatRoomObscurationConfigDto;
import com.tigerjoys.shark.miai.agent.dto.result.DiamondResultDto;
import com.tigerjoys.shark.miai.agent.enums.AgentErrorCodeEnum;
import com.tigerjoys.shark.miai.agent.enums.AnchorOnlineStateEnum;
import com.tigerjoys.shark.miai.agent.enums.FirstPayTypeEnum;
import com.tigerjoys.shark.miai.agent.enums.UserPointAccountLogTypeEnum;
import com.tigerjoys.shark.miai.agent.enums.UserVideoChatRecordChannelTypeEnum;
import com.tigerjoys.shark.miai.agent.enums.VChatVideoStatusEnum;
import com.tigerjoys.shark.miai.agent.enums.VChatVideoTCPTypeEnum;
import com.tigerjoys.shark.miai.agent.enums.VchatRoomAvTypeEnum;
import com.tigerjoys.shark.miai.agent.enums.VchatRoomFreeVchartFalgEnum;
import com.tigerjoys.shark.miai.agent.neteasecheck.INeteaseTextCheck;
import com.tigerjoys.shark.miai.dto.dispatch.DoubleVChatVideoDto;
import com.tigerjoys.shark.miai.dto.service.AnchorEvaluationDto;
import com.tigerjoys.shark.miai.dto.service.AnchorToUserEvaluationDto;
import com.tigerjoys.shark.miai.dto.service.AnchorToUserEvaluationShowDto;
import com.tigerjoys.shark.miai.dto.service.BtnData;
import com.tigerjoys.shark.miai.dto.service.DialingCheckNewDto;
import com.tigerjoys.shark.miai.dto.service.DlgAndGoPage;
import com.tigerjoys.shark.miai.dto.service.DlgAndGoPageNew;
import com.tigerjoys.shark.miai.dto.service.EvaluationDto;
import com.tigerjoys.shark.miai.dto.service.MyPhoneDto;
import com.tigerjoys.shark.miai.dto.service.RecommendOnlineAnchorDto;
import com.tigerjoys.shark.miai.dto.service.UserBaseInfo;
import com.tigerjoys.shark.miai.enums.DlgAndGoPageEnum;
import com.tigerjoys.shark.miai.enums.ErrorCodeEnum;
import com.tigerjoys.shark.miai.enums.StaticWebUrlEnum;
import com.tigerjoys.shark.miai.inter.contract.IAnchorEvaluationLogContract;
import com.tigerjoys.shark.miai.inter.contract.IAnchorEvaluationStatisticsContract;
import com.tigerjoys.shark.miai.inter.contract.IAnchorIntimateRankingsContract;
import com.tigerjoys.shark.miai.inter.contract.IAnchorOnlineContract;
import com.tigerjoys.shark.miai.inter.contract.IAnchorRecvUserContract;
import com.tigerjoys.shark.miai.inter.contract.IAnchorToUserEvaluationLogContract;
import com.tigerjoys.shark.miai.inter.contract.IAppLabelContract;
import com.tigerjoys.shark.miai.inter.contract.IFreeVideoChatExperienceContract;
import com.tigerjoys.shark.miai.inter.contract.IRechargeOrderContract;
import com.tigerjoys.shark.miai.inter.contract.ISysConfigContract;
import com.tigerjoys.shark.miai.inter.contract.IUserBlacklistContract;
import com.tigerjoys.shark.miai.inter.contract.IUserFirstRechargeLogContract;
import com.tigerjoys.shark.miai.inter.contract.IUserVchatExperienceIncomeLogContract;
import com.tigerjoys.shark.miai.inter.contract.IVchatRoomContract;
import com.tigerjoys.shark.miai.inter.entity.AnchorEvaluationLogEntity;
import com.tigerjoys.shark.miai.inter.entity.AnchorOnlineEntity;
import com.tigerjoys.shark.miai.inter.entity.AnchorToUserEvaluationLogEntity;
import com.tigerjoys.shark.miai.inter.entity.AppLabelEntity;
import com.tigerjoys.shark.miai.inter.entity.FreeVideoChatExperienceEntity;
import com.tigerjoys.shark.miai.inter.entity.GuardVipCategoryEntity;
import com.tigerjoys.shark.miai.inter.entity.SysConfigEntity;
import com.tigerjoys.shark.miai.inter.entity.UserBlacklistEntity;
import com.tigerjoys.shark.miai.inter.entity.UserVchatExperienceIncomeLogEntity;
import com.tigerjoys.shark.miai.inter.entity.VchatRoomEntity;
import com.tigerjoys.shark.miai.service.IAnchorDialVideoService;
import com.tigerjoys.shark.miai.service.IChannelCheckService;
import com.tigerjoys.shark.miai.service.IVChatTextYXService;
import com.tigerjoys.shark.miai.service.IVChatVideoYXService;
import com.tigerjoys.shark.miai.utils.ServiceHelper;

@Service
public class VChatVideoYXServiceImpl implements IVChatVideoYXService {

	private static final int ROOM_CREATE_EXPIRE = 100;
	private static final String ROOM_ID = "roomId";
	private static final String PAY_FLAG = "payFlag";
	private static final String ORDER_ID = "orderId";
	private static final String ENTER_ROOM = "enterRoom";
	private static final String ANCHOR = "anchor";
	private static final String PAYUSER = "payUser";
	private static final String PAYPRICE = "payPrice";
	private static final String OTHERUSERID = "otherUserId";

	private String dlgShowInfo ="她无法伺候您\n换一个更懂你的小姐姐吧"; 
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private static int PAGESIZE = 10;
	
	private static int MULTIPLE_LIMIT = 1;
	
	
	private static int CHARGE_DIAMOND_SHOW = 2;
	
	
	private final int recommend_anchor_timeOut =  60*60*24;
	
	@Autowired
	private IUserAgent userAgent;

	@Autowired
	private INeteaseAgent neteaseAgent;

	@Autowired
	@Qualifier(RedisCacheConst.REDIS_PUBLIC_CACHE)
	private CacheRedis cacheRedis;

	@Autowired
	private IAnchorOnlineContract anchorOnlineContract;

	@Autowired
	private IVchatRoomContract vchatRoomContract;

	@Autowired
	private IUserDiamondAgent userDiamondAgent;
	
	
	@Autowired
	private ISysConfigAgent sysConfigAgent;
	
	@Autowired
	private IAppLabelContract appLabelContract;
	
	@Autowired
	private IUserOnlineStateAgent userOnlineStateAgent;
	
	@Autowired
	private IUserBlacklistContract userBlacklistContract;
	
	@Autowired
	private IAnchorEvaluationLogContract anchorEvaluationLogContract;
	
	@Autowired
	private IAnchorRecvUserContract anchorRecvUserContract;
	
	@Autowired
	private IAnchorToUserEvaluationLogContract anchorToUserEvaluationLogContract;
	
	@Autowired
	private IAnchorEvaluationStatisticsContract anchorEvaluationStatisticsContract;
	
	@Autowired
	private IAnchorIntimateRankingsContract anchorIntimateRankingsContract;
	
	@Autowired
	private IUserOrdinaryOnlineListAgent userOrdinaryOnlineListAgent;
	
	@Autowired
	private ISysConfigContract sysConfigContract;
	
	@Autowired
	private IPayUserAgent payUserAgent;
	
	@Autowired
	private IUserFriendAgent userFriendAgent;
	
	@Autowired
	private IFreeVideoChatExperienceContract freeVideoChatExperienceContract;
	
	@Autowired
	private IRechargeOrderContract rechargeOrderContract;
	
	@Autowired
	private IChannelCheckService channelCheckService;
	
	@Autowired
	private VchatRoomRecvExitSendMsgConsumer vchatRoomRecvExitSendMsgConsumer;
	
	@Autowired
	private IUserFirstRechargeLogContract userFirstRechargeLogContract;
	
	@Autowired
	private IUserVchatExperienceIncomeLogContract userVchatExperienceIncomeLogContract;
	
	@Autowired
	private IUserPointAgent userPointAgent;
	
	
	@Autowired
	private IVchatRoomSettlementAgent vchatRoomSettlementAgent;
	
	@Autowired
	private IAnchorDefendAgent anchorDefendAgent;
	
	@Autowired
	private IAnchorDialVideoService anchorDialVideoAgent;
	
	@Autowired
	private IUserSubscribeAnchorAgent userSubscribeAnchorAgent;
	
	@Autowired
	private IUserWeekCardAgent userWeekCardAgent;
	
	@Autowired
	private IAnchorDynamicPriceAgent  anchorDynamicPriceAgent ;
	
	@Autowired
	private IVChatTextYXService  vChatTextYXService ;
	
	@Autowired
	private IIOSUserSmsAgent iOSUserSmsAgent;
	
	
	@Autowired
	private INeteaseTextCheck neteaseTextCheck;
	
	

	public ActionResult getDialing(long userId, Long toUserId,Integer sponsor,int avType) throws Exception {

		if(userId == toUserId){
			return ActionResult.fail(ErrorCodeEnum.dialing_slef_error);
		}
		
		DoubleVChatVideoDto toUserVChat = null;
		UserBO toUser = userAgent.findById(toUserId);
		if(sponsor == 1){
			String autoFlag = getVchatCheckFalg(userId,toUserId);
			if(Tools.isNull(autoFlag)){
				return ActionResult.fail(ErrorCodeEnum.user_dial_check_error);
			}
			
			AnchorOnlineEntity anchor = null;
			long audienceId = 0;
			long anchorId = 0;
			UserBO anchorBo =userAgent.findById(toUserId);
			if (anchorBo.isWaiter()) {
				audienceId = userId;
				anchorId = toUserId;
			} else {
				UserBO userBo =userAgent.findById(userId);
				if (userBo.isWaiter()) {
					audienceId = toUserId;
					anchorId = userId;
				} else {
					return ActionResult.fail(ErrorCodeEnum.anchor_inexistence);
				}
			}
			anchor = anchorOnlineContract.findByProperty("userid", anchorId);
			if(Tools.isNull(anchor)){
				return ActionResult.fail(ErrorCodeEnum.anchor_inexistence);
			}
			
			UserBO audienceBo = userAgent.findById(audienceId);
			
			if(userId == anchorId){
				//增加拨打次数
				Calendar cal = Calendar.getInstance();
				int hour = cal.get(Calendar.HOUR_OF_DAY);
				if(hour>=4 && hour <9){
					//这个时间段不增加拨打次数
				}else{
					setAddAnchorDialingUserTimes(userId,avType);
				}
				
			}
			FreeVideoChatExperienceEntity freeVideoChat = null;
			if(avType == VchatRoomAvTypeEnum.video.getCode()){
				freeVideoChat = freeVideoChatExperienceContract.findByProperty("userid", audienceId);
			}
			long balance = userDiamondAgent.getDiamondBalance(audienceId);
			int roomPrice = (avType == VchatRoomAvTypeEnum.audio.getCode()? anchor.getAudio_price():anchor.getPrice());
			int anchorPrice = anchorDynamicPriceAgent.getAnchorDynamicPrice(audienceId,roomPrice);
			if(Tools.isNotNull(freeVideoChat) || userSubscribeAnchorAgent.checkSubscribe(audienceId, anchorId) || userWeekCardAgent.getDailyWeekCard(audienceId,anchorPrice<120?4:5)>0L){
				//不处理
			}else{
				if (balance < anchorPrice * MULTIPLE_LIMIT) {
					return ActionResult.fail(ErrorCodeEnum.hundredResponses_diamond_not_enough);
				}
			}
			VchatRoomObscurationConfigDto configDto = sysConfigAgent.obscurationConfig();
			long orderId = IdGenerater.generateId();
			
			/////////////////主播多用户处理//////////////////////
			long neteaseUserId =  anchorId;
			if( toUserId.equals(anchor.getUserid()) && anchor.getParent_userid() != 0){
				neteaseUserId = anchor.getParent_userid();
			}
			/////////////////主播多用户处理结束////////////////////
		
			Date current = new Date();
			VchatRoomEntity roomEntity = new VchatRoomEntity();
			roomEntity.setOrderId(orderId);
			roomEntity.setAnchorid(anchorId);
			roomEntity.setUserid(audienceId);
			roomEntity.setSponsor_user(userId);
			
			GuardVipCategoryEntity guardList = anchorDefendAgent.getCurrentAnchorDefendByUser(audienceId, anchorId);
			if(audienceBo.vipValue()>0 && Tools.isNotNull(guardList)){
				roomEntity.setPay_discount(0.9);
			}else if(audienceBo.vipValue()>0){
				roomEntity.setPay_discount(0.95);
			}else if(Tools.isNotNull(guardList)){
				roomEntity.setPay_discount(0.95);
			}else{
				roomEntity.setPay_discount(1d);
			}
			if(avType == VchatRoomAvTypeEnum.audio.getCode()){
				//roomEntity.setPrice(Tools.intValue(Math.round(anchor.getAudio_price()*roomEntity.getPay_discount())));
				//roomEntity.setSettle_price(Tools.intValue(Math.round(anchor.getAnchor_audio_price()*roomEntity.getPay_discount())));
				roomEntity.setPrice(anchorDynamicPriceAgent.getAnchorDynamicPrice(audienceId, anchor.getAudio_price()));
				roomEntity.setSettle_price(anchor.getAnchor_audio_price());
			}else {
				//roomEntity.setPrice(Tools.intValue(Math.round(anchor.getPrice()*roomEntity.getPay_discount())));
				//roomEntity.setSettle_price(Tools.intValue(Math.round(anchor.getAnchor_video_price()*roomEntity.getPay_discount())));
				roomEntity.setPrice(anchorDynamicPriceAgent.getAnchorDynamicPrice(audienceId, anchor.getPrice()));
				roomEntity.setSettle_price(anchor.getAnchor_video_price());
			}
			roomEntity.setTotal_price(0);
			roomEntity.setCreate_time(current);
			roomEntity.setFinish_time(current);
			roomEntity.setSys_duration(0);
			roomEntity.setVchat_duration(0);
			roomEntity.setAv_type(avType);
			roomEntity.setShut_user(0L);
			roomEntity.setWy_chatid(0L);
			roomEntity.setVideo_falg(0);
			roomEntity.setDuration(0);
			roomEntity.setShut_user(0L);
			roomEntity.setShut_info("");
			if("auto".equals(autoFlag)){
				roomEntity.setAuto_flag(1);
			}
			//音频不免费
			if(avType == VchatRoomAvTypeEnum.video.getCode()){
				if(Tools.isNotNull(freeVideoChat) &&  userId == anchorId){
					roomEntity.setFree_vchart_falg(VchatRoomFreeVchartFalgEnum.free_vchart.getCode());
				}else if(Tools.isNotNull(freeVideoChat) && userId == audienceId && configDto.getDailType() == 0){
					roomEntity.setFree_vchart_falg(VchatRoomFreeVchartFalgEnum.free_vchart.getCode());
				}else {
					roomEntity.setFree_vchart_falg(VchatRoomFreeVchartFalgEnum.no_free.getCode());
				}
			}else{
				roomEntity.setFree_vchart_falg(VchatRoomFreeVchartFalgEnum.no_free.getCode());
			}
			if(Tools.getDayTime() == Tools.getDayTime(audienceBo.getCreateTime())){
				roomEntity.setUser_flag(1);
			}else{
				roomEntity.setUser_flag(0);
			}
			
			if(avType==VchatRoomAvTypeEnum.video.getCode() && Tools.isNotNull(configDto.getFreeIncome()) && configDto.getFreeIncome() ==1){
				roomEntity.setFree_income_falg(1);
			}else{
				roomEntity.setFree_income_falg(0);
			}
			
			roomEntity.setLabor_id(anchor.getLabor_id());
			roomEntity.setOffset_flag(anchor.getDeduct());
			roomEntity.setSys_offset_duration(0);
			roomEntity.setFree_income_type(anchor.getAward());
			roomEntity.setSettlement_type(anchor.getSettlement());
			roomEntity.setIncome_falg(1);
			roomEntity.setWeekcard_duration(0);
			vchatRoomContract.insert(roomEntity);
			
			//用户拨打大V总计
			if(anchorId == toUserId){
				anchorOnlineContract.addDialNum(anchorId,avType);
			}
			//添加主播当天拨打列表
			if(userId == anchorId){
				userOrdinaryOnlineListAgent.addAnchorDialingUser(anchorId, audienceId);
			}
			//用户
			if(userId == audienceId){
				setDialingFalg(userId);
			}
			logger.info("getDialingTest:audienceId:"+audienceId+";neteaseUserId:"+neteaseUserId+";roomEntity.getId():"+roomEntity.getId()+";orderId:"+roomEntity.getPrice());
			setRoomRedis(audienceId, neteaseUserId, roomEntity.getId(), orderId, roomEntity.getPrice());
			toUserVChat = getRoomInfo(toUser, anchor, audienceId, roomEntity, orderId, balance,getLastTalk(userId,toUserId,avType));
		}else {
			String prefix = getPrefix(userId, toUserId);
			String roomId = cacheRedis.hget(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX + prefix + userId, ROOM_ID);
			String orderId = cacheRedis.hget(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX + prefix + userId, ORDER_ID);
			String anchorID = cacheRedis.hget(AgentRedisCacheConst.VCHAT_ROOM_CREATE_PREFIX + orderId, ANCHOR);
			String payuserId = cacheRedis.hget(AgentRedisCacheConst.VCHAT_ROOM_CREATE_PREFIX + orderId, PAYUSER);
			if(Tools.isNotNull(roomId) && Tools.isNotNull(orderId) && Tools.isNotNull(anchorID) && Tools.isNotNull(payuserId) ){
				VchatRoomEntity room = vchatRoomContract.findById(Long.valueOf(roomId));
				Long audienceId = Tools.parseLong(payuserId);
				long balance = userDiamondAgent.getDiamondBalance(audienceId);
				AnchorOnlineEntity anchor = anchorOnlineContract.findByProperty("userid", Long.parseLong(anchorID));
				toUserVChat = getRoomInfo(toUser, anchor, audienceId, room, Tools.parseLong(orderId), balance,getLastTalk(userId,toUserId,avType));
			}else{
				logger.info("dialing:roomId={};orderId={};anchorID={};payuserId={}",roomId,orderId,anchorID,payuserId);
				 return ActionResult.fail(ErrorCodeEnum.error.getCode(),"");
			}
		}
		return ActionResult.success(toUserVChat);
	}
	
	@Override
	public ActionResult dialingNewCheck(long userId, Long toUserId,long type,int avType) throws Exception {
		DialingCheckNewDto checkDto = new DialingCheckNewDto();

		if(userId == toUserId){
			return CheckFailReturnDesc("自己不能拔打自己");
		}

		UserBlacklistEntity userBlack = userBlacklistContract.getUserBlackList(toUserId,userId);
		if(Tools.isNotNull(userBlack)){
			return CheckFailReturnDesc("对方正在忙");
		}
		
		AnchorOnlineEntity anchor = null;
		long audienceId = 0;
		long anchorId = 0;
		UserBO otherBo =userAgent.findById(toUserId);
		UserBO userBo =userAgent.findById(userId);
		
		if(userBo.isWaiter() && otherBo.isWaiter()){
			return CheckFailReturnDesc("暂无权限拨打");
		}		
		if(Tools.isNull(otherBo)){
			return CheckFailReturnDesc("对方不在线");
		}
		
		RequestHeader header = RequestUtils.getCurrent().getHeader();
		/*
		if(!Const.IS_TEST && "com.tjhj.miyou".equals(otherBo.getPackageName())){
			return CheckFailReturnDesc("对方正在忙，请稍后再试");
		}
 		if(Tools.isNotNull(header)){
 			if(!Const.IS_TEST && "ios_miyou".equalsIgnoreCase(header.getChannel())){
 				return CheckFailReturnDesc("对方正在忙，请稍后再试");
 			}
 		}
 		*/
		
		if (otherBo.isWaiter()) {
			audienceId = userId;
			anchorId = toUserId;
		} else {
			if (userBo.isWaiter()) {
				audienceId = toUserId;
				anchorId = userId;
			} else {
				return ActionResult.fail(ErrorCodeEnum.anchor_inexistence);
			}
		}
		if(userId == 153302058427023872L && toUserId == 153310968754012416L || userId == 153310968754012416L && toUserId == 153302058427023872L ){
			return CheckFailReturnDesc("对方已下架");
		}
		
		if("com.duidui.duijiaoyou".equals(header.getPackageName())){
			UserBO audienceUser =userAgent.findById(audienceId);
			if(audienceUser.vipValue() == 0){
				return CheckTestIOSFailVIPDesc();
			}
		}
		
		
		if( header.getOs_type() == 2 && channelCheckService.checkChannel()){
			return CheckFailReturnDesc("对方正在忙，请稍后再试");
		}
		
		if( iOSUserSmsAgent.getUserIdList().containsKey(toUserId) || iOSUserSmsAgent.getUserIdList().containsKey(userId)){
			return CheckFailReturnDesc("对方正在忙，请稍后再试");
		}
		
	
		
		anchor = anchorOnlineContract.findByProperty("userid", anchorId);
		if(Tools.isNull(anchor)){
			return ActionResult.fail(ErrorCodeEnum.anchor_inexistence);
		}
		
		/*
		if(anchor.getState() != 1 ){
			return CheckFailReturnDesc("对方正在忙，请稍后再试");
		}
		*/
		if(anchor.getState() == -8 ){
			return CheckFailReturnDesc("对方正在忙，请稍后再试");
		}
		
		if( userId == anchorId  &&userOrdinaryOnlineListAgent.getAnchorForbidDialFlag(audienceId)){
			return CheckFailReturnDesc("对方正在忙，请稍后再试");
		}
		
		int dialType = (avType== VchatRoomAvTypeEnum.audio.getCode() ?anchor.getAudio_type():anchor.getVideo_type());
		if(dialType == 0){
			String info = (userId == audienceId?"该主播的":"您的")+(avType==1?"语音":"视频")+"服务已暂停";
			return CheckFailReturnDesc(info);
		}
		
		if(userId == anchorId){
			
			// 检查主播是否设置勿扰
			if( anchor.getDisturb()>0){
				return CheckFailReturnDesc("您处于勿扰状态，暂不能拨打");
			}
			
			// 检查对方是否设置勿扰
			if( otherBo.getDisturb()>0  || anchorRecvUserContract.checkDisturb(userId,toUserId) || anchorRecvUserContract.checkInvisibility(userId,toUserId) ){
				return CheckFailReturnDesc("该用户处于勿扰状态,暂不能拨打");
			}
			
			if(!Const.IS_TEST && getAnchorDialThreeNoRecv(anchorId,audienceId) && !userFirstRechargeLogContract.checkFirstRecharge(audienceId, FirstPayTypeEnum.diamond.getCode())){
				return CheckFailReturnDesc("该用户在忙，请稍后再试");
			}
			
			if (Const.ANCHOR_DIAL_FORBID_USERID_MAP.containsKey(userId)) {
				UserBO audienceUser =userAgent.findById(audienceId);
				if(audienceUser.getCityid() == 4146 || audienceUser.getCityid() == 4149) {
					if (audienceUser.getDegreeid() <= 3) {
						return CheckFailReturnDesc("该用户在忙，请稍后再试");
					}
				}
			}
			
			if(anchor.getDial_controll()>0){
				boolean dialControllFalg = false;
				if(anchor.getDial_controll()==1 ){
					if(!userFirstRechargeLogContract.checkFirstRecharge(audienceId, FirstPayTypeEnum.diamond.getCode())){
						dialControllFalg = true;
					}
				}else if(anchor.getDial_controll()==2 ){
					if(userFirstRechargeLogContract.checkFirstRecharge(audienceId, FirstPayTypeEnum.diamond.getCode())){
						dialControllFalg = true;
					}else{
						if(anchor.getDial_charge_user_level()>0){
							UserBO audienceUser =userAgent.findById(audienceId);
							if(Tools.isNotNull(audienceUser)){
								if(audienceUser.getDegreeid()>=anchor.getDial_charge_user_level()){
									dialControllFalg = true;
								}
							}
						}
					}
				}
				if(dialControllFalg){
					return CheckFailReturnDesc("该用户在忙，请稍后再试");
				}
			}
			//限制主播拨打用户次数
			Calendar cal = Calendar.getInstance();
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			if(hour>=4 && hour <9){
				//这个时间段不限制拨打次数
			}else{
				long dialTime = getAnchorDialingUserTimes(userId,avType);
				//增加拨打次数
				if(Tools.longValue(anchor.getDial_helper_num())>0){
					if(dialTime+1 > Tools.longValue(anchor.getDial_helper_num())){
						return CheckFailReturnDesc("当日拨打次数已用完");
					}
				}
			}
		}
		
		if(userId == audienceId){
			
			if(vChatTextYXService.checkShowVIPFragment(userBo)){
				return CheckFailVIPDesc();
			}
			
			if(anchor.getUser_dial_controll()>0){
				boolean userDialControllFalg = false;
				if(anchor.getUser_dial_controll()==1 ){
					if(!userFirstRechargeLogContract.checkFirstRecharge(audienceId, FirstPayTypeEnum.diamond.getCode())){
						userDialControllFalg = true;
					}
				}else if(anchor.getUser_dial_controll()==2 ){
					if(userFirstRechargeLogContract.checkFirstRecharge(audienceId, FirstPayTypeEnum.diamond.getCode())){
						userDialControllFalg = true;
					}
				}
				if(userDialControllFalg){
					return CheckFailReturnDesc("该主播在忙，请稍后再试");
				}
			}
		}

		
		int versionCode = RequestUtils.getCurrent().getHeader().getVersioncode();
		
		VchatRoomObscurationConfigDto configDto = sysConfigAgent.obscurationConfig();
		FreeVideoChatExperienceEntity freeVideoChat = freeVideoChatExperienceContract.findByProperty("userid", audienceId);
		
		boolean chargeChat = true;
		if(Tools.isNotNull(freeVideoChat) && userId == anchorId  && avType == VchatRoomAvTypeEnum.video.getCode()){
			chargeChat =false;
		}
		if(Tools.isNotNull(freeVideoChat) && userId == audienceId && configDto.getDailType() == 0 && avType == VchatRoomAvTypeEnum.video.getCode()){
			chargeChat =false;
		}
		//主播是假账户，用户拨打 ，如果没钱，提示 余额不足
		if(Tools.isNotNull(freeVideoChat) && userId == audienceId &&  anchor.getFlag() ==4  && avType == VchatRoomAvTypeEnum.video.getCode()){
			
			if( !channelCheckService.checkChannel() && getRecommentAnchorFalg(userId)){
				 List<RecommendOnlineAnchorDto> onlineList = getRecommndOnlineAnchorList(userId,avType);
				 if(Tools.isNotNull(onlineList)){
					 String hintInfo = "该大V处于在忙状态\n暂时不方便接听哦~\n请稍后再试~";
					 DlgAndGoPageNew dlgAndGoPage = new DlgAndGoPageNew();
					 dlgAndGoPage.setAndroidPageTag(DlgAndGoPageEnum.recommendAnchorDlg.getAndroidPage());
					 HashMap<String, Object> hsmp = new HashMap<>();
					 hsmp.put("showInfo", dlgShowInfo);
					 hsmp.put("anchorList",onlineList);
					 dlgAndGoPage.setAndroidPageParam(DlgAndGoPage.getTagParam(JsonHelper.toJson(hsmp)));
					 checkDto.setDlgAndGoPage(dlgAndGoPage);
					 checkDto.setStatus(0);
					 checkDto.setIosDigInfo(hintInfo);
					 return ActionResult.success(checkDto);
				 }else{
					 chargeChat =true;
				 }
			}else{
				 chargeChat =true;
			}
			
		}
		
		int roomPrice = avType == VchatRoomAvTypeEnum.audio.getCode()? anchor.getAudio_price():anchor.getPrice();
		int anchorPrice = anchorDynamicPriceAgent.getAnchorDynamicPrice(audienceId,roomPrice);
		if(userSubscribeAnchorAgent.checkSubscribe(audienceId, anchorId) || ( checkDialTimeLimit() && userWeekCardAgent.getDailyWeekCard(audienceId,anchorPrice<120?4:5)>0L)){
			chargeChat = false;
		}
		
		if(chargeChat){
			//先判断余额
			long balance = userDiamondAgent.getDiamondBalance(audienceId);
			
			if (balance < anchorPrice * MULTIPLE_LIMIT) {
				checkDto.setStatus(0);
				DlgAndGoPageNew dlgAndGoPage = new DlgAndGoPageNew();
				String hintInfo ="";
				if(audienceId == userId ){
					hintInfo = "你的钻石不足，快快充值\n享受与大V独处的时光吧！";
					SysConfigEntity sysConfig = sysConfigAgent.getSysConfig(com.tigerjoys.shark.miai.agent.constant.Const.HINT_INFO_DIAMOND_NOTENOUGH);
					if(Tools.isNotNull(sysConfig)){
						hintInfo = sysConfigAgent.replaceToN(sysConfig.getValue());
					}
					
					BtnData chargebtn = new BtnData();
					chargebtn.setBtnName("去充值");
					chargebtn.setAndroidPageTag(DlgAndGoPageEnum.webSingle.getAndroidPage());
					chargebtn.setAndroidPageParam(DlgAndGoPage.getTagParam(Const.WEB_SITE+"/api/income/zuanList","我的钱包"));
					dlgAndGoPage.setBtnDataList(Arrays.asList(chargebtn));
					
					dlgAndGoPage.setIosPageTag(DlgAndGoPageEnum.webSingle.getIosPage());
					dlgAndGoPage.setIosPageParam(DlgAndGoPage.getTagParam(0));
					
					checkDto.setiOSTag(1);
				}else{
					hintInfo = "对方钻石不足，不可以拔打!";
					BtnData cancelBtn = new BtnData();
					cancelBtn.setBtnName("确定");
					dlgAndGoPage.setBtnDataList(Arrays.asList(cancelBtn));
				}
				dlgAndGoPage.setHintInfo(hintInfo);
				checkDto.setIosDigInfo(hintInfo);
				checkDto.setDlgAndGoPage(dlgAndGoPage);
				return ActionResult.success(checkDto);
			}else if (balance < anchorPrice * CHARGE_DIAMOND_SHOW  && type ==1  && versionCode<37) {
				checkDto.setStatus(0);
				if(audienceId == userId ){
					DlgAndGoPageNew dlgAndGoPage = new DlgAndGoPageNew();
					String hintInfo = "你的通话时间不多了\n和小姐姐畅聊，请充值!";
					
					BtnData continueBtn = new BtnData();
					continueBtn.setBtnName("继续拨打");
					continueBtn.setAction(1);
					
					BtnData chargebtn = new BtnData();
					chargebtn.setBtnName("去充值");
					chargebtn.setAndroidPageTag(DlgAndGoPageEnum.webSingle.getAndroidPage());
					chargebtn.setAndroidPageParam(DlgAndGoPage.getTagParam(Const.WEB_SITE+"/api/income/zuanList","我的钱包"));
					dlgAndGoPage.setBtnDataList(Arrays.asList(continueBtn ,chargebtn));
				
					dlgAndGoPage.setIosPageTag(DlgAndGoPageEnum.webSingle.getIosPage());
					dlgAndGoPage.setIosPageParam(DlgAndGoPage.getTagParam(0));
					dlgAndGoPage.setHintInfo(hintInfo);
					checkDto.setIosDigInfo(hintInfo);
					checkDto.setDlgAndGoPage(dlgAndGoPage);
					checkDto.setiOSTag(2);
					return ActionResult.success(checkDto);
				}
				
			}
		}
		
		/////////////////主播多用户处理//////////////////////
		long neteaseUserId =  toUserId;
		if( toUserId.equals(anchor.getUserid()) && anchor.getParent_userid() != 0){
			neteaseUserId = anchor.getParent_userid();
		}
		if(userId == neteaseUserId){
			checkDto.setStatus(0);
			DlgAndGoPageNew dlgAndGoPage = new DlgAndGoPageNew();
			dlgAndGoPage.setHintInfo(ErrorCodeEnum.dialing_slef_error.getDesc());
			/////////////////
			BtnData cancelBtn = new BtnData();
			cancelBtn.setBtnName("确定");
			dlgAndGoPage.setBtnDataList(Arrays.asList(cancelBtn));
			////////////////
			checkDto.setDlgAndGoPage(dlgAndGoPage);
			checkDto.setIosDigInfo(ErrorCodeEnum.dialing_slef_error.getDesc());
			return ActionResult.success(checkDto);
		}
		/////////////////主播多用户处理结束//////////////////////
		int userStatus = userOnlineStateAgent.userOnlineState(neteaseUserId);
		//if(AnchorOnlineStateEnum.online.getCode() == userStatus ){
		//不判断状态
		if( true ){
			clearInitiativeDial(userId);
	
			//脚本
			String LUA = "local user1,user2 = KEYS[1],KEYS[2]; local ex1 = redis.call('exists',user1); local ex2 = redis.call('exists',user2); if(ex1 and tonumber(ex1) == 0 and ex2 and tonumber(ex2) == 0) then redis.call('set',user1,user1); redis.call('pexpire',user1,35000); redis.call('set',user2,user2); redis.call('pexpire',user2,35000); return 1; else return 0;end";
			String userIdStr = AgentRedisCacheConst.VCHAT_USER_ORDER_PREFIX +userId ;
			String toUserIdStr = AgentRedisCacheConst.VCHAT_USER_ORDER_PREFIX +neteaseUserId ; 
			String result = String.valueOf(cacheRedis.eval(LUA, 2, userIdStr, toUserIdStr));
			if("0".equals(result)){
				checkDto.setStatus(0);
				DlgAndGoPageNew dlgAndGoPage = new DlgAndGoPageNew();
				boolean flag = true;
				if(audienceId == userId){
					if( !channelCheckService.checkChannel() && getRecommentAnchorFalg(userId)){
						 List<RecommendOnlineAnchorDto> onlineList = getRecommndOnlineAnchorList(userId,avType);
						 if(Tools.isNotNull(onlineList)){
							 dlgAndGoPage.setAndroidPageTag(DlgAndGoPageEnum.recommendAnchorDlg.getAndroidPage());
							 HashMap<String, Object> hsmp = new HashMap<>();
							 hsmp.put("showInfo", dlgShowInfo);
							 hsmp.put("anchorList",onlineList);
							 dlgAndGoPage.setAndroidPageParam(DlgAndGoPage.getTagParam(JsonHelper.toJson(hsmp)));
							 flag = false;
						 }
					}
				}
				if(flag){
					dlgAndGoPage.setHintInfo(ErrorCodeEnum.user_calling_video_chat.getDesc());
					////////////
					BtnData cancelBtn = new BtnData();
					cancelBtn.setBtnName("确定");
					dlgAndGoPage.setBtnDataList(Arrays.asList(cancelBtn));
				}
				
				////////////////
				checkDto.setIosDigInfo(ErrorCodeEnum.user_calling_video_chat.getDesc());
				checkDto.setDlgAndGoPage(dlgAndGoPage);
				return ActionResult.success(checkDto);
			}
			checkDto.setNeteaseUserId(neteaseUserId);
			checkDto.setStatus(1);
			//设置主动标记
			setInitiativeDial(userId,toUserId);
			//设置通过标记
			setVchatCheckFalg(userId,toUserId,"check");
			return ActionResult.success(checkDto);
		}else{
			DlgAndGoPageNew dlgAndGoPage = new DlgAndGoPageNew();
			String hintInfo =(audienceId == userId?"该大V处于":"该用户处于")+AnchorOnlineStateEnum.getDescByCode(userStatus)+"状态\n暂时不方便接听哦~\n请稍后再试~";
			// 是否推荐弹窗标记
			boolean flag = true;
			if(audienceId == userId){
				if(!channelCheckService.checkChannel() && getRecommentAnchorFalg(userId)){
					 List<RecommendOnlineAnchorDto> onlineList = getRecommndOnlineAnchorList(userId,avType);
					 if(Tools.isNotNull(onlineList)){
						 dlgAndGoPage.setAndroidPageTag(DlgAndGoPageEnum.recommendAnchorDlg.getAndroidPage());
						 HashMap<String, Object> hsmp = new HashMap<>();
						 hsmp.put("showInfo", dlgShowInfo);
						 hsmp.put("anchorList",onlineList);
						 dlgAndGoPage.setAndroidPageParam(DlgAndGoPage.getTagParam(JsonHelper.toJson(hsmp)));
						 flag = false;
					 }
				}
			}
		
			if(flag){
				dlgAndGoPage.setHintInfo(hintInfo);
				BtnData cancelBtn = new BtnData();
				cancelBtn.setBtnName("确定");
				dlgAndGoPage.setBtnDataList(Arrays.asList(cancelBtn));
			}
			
			///////////
			checkDto.setDlgAndGoPage(dlgAndGoPage);
			checkDto.setStatus(0);
			checkDto.setIosDigInfo(hintInfo);
			return ActionResult.success(checkDto);
		}
	}
	
	/**
	 * 通话前检查失败返回数据
	 * @param msg
	 * @return
	 */
	public ActionResult CheckFailReturnDesc(String msg){
		DialingCheckNewDto checkDto = new DialingCheckNewDto();
		checkDto.setStatus(0);
		DlgAndGoPageNew dlgAndGoPage = new DlgAndGoPageNew();
		BtnData btn = new BtnData();
		btn.setBtnName("确定");
		dlgAndGoPage.setBtnDataList(Arrays.asList(btn));
		dlgAndGoPage.setHintInfo(msg);
		checkDto.setIosDigInfo(msg);
		checkDto.setDlgAndGoPage(dlgAndGoPage);
		return ActionResult.success(checkDto);
	}
	
	/**
	 * IOS提审VIP校验
	 * @return
	 */
	public ActionResult CheckTestIOSFailVIPDesc(){
		int os_Type = RequestUtils.getCurrent().getHeader().getOs_type();
		DialingCheckNewDto checkDto = new DialingCheckNewDto();
		checkDto.setStatus(0);
		DlgAndGoPageNew dlgAndGoPage = new DlgAndGoPageNew();
		String hintInfo = null;
		hintInfo = "您不是VIP用户，请开通VIP才可能通话";
		BtnData chargebtn = new BtnData();
		chargebtn.setBtnName("开通VIP");
		chargebtn.setAndroidPageTag("VIPFragment");
		chargebtn.setAndroidPageParam(DlgAndGoPage.getTagParam(0));
		BtnData cancelBtn = new BtnData();
		cancelBtn.setBtnName("取消");
		dlgAndGoPage.setBtnDataList(Arrays.asList(chargebtn,cancelBtn));
		dlgAndGoPage.setHintInfo(hintInfo);
		checkDto.setDlgAndGoPage(dlgAndGoPage);
		checkDto.setiOSTag(10000);
		return ActionResult.success(checkDto);
	}
	
	/**
	 *android客户端VIP校验
	 * @return
	 * @throws Exception 
	 */
	public ActionResult CheckFailVIPDesc() throws Exception{
		DialingCheckNewDto checkDto = new DialingCheckNewDto();
		checkDto.setStatus(0);
		int os_type = RequestUtils.getCurrent().getHeader().getOs_type();
		DlgAndGoPageNew dlgAndGoPage = new DlgAndGoPageNew();
		String	hintInfo = "还没有开通VIP特色服务功能哦\n开通后可以畅聊\n更获得100元话费";
		SysConfigEntity sysConfig = sysConfigAgent.getSysConfig(com.tigerjoys.shark.miai.agent.constant.Const.HINT_INFO_VIP_POP);
		if(Tools.isNotNull(sysConfig)){
			hintInfo = sysConfigAgent.replaceToN(sysConfig.getValue());
		}
		BtnData chargebtn = new BtnData();
		chargebtn.setBtnName("去开通");
		BtnData cancelbtn = new BtnData();
		cancelbtn.setBtnName("取消");
		
		if( os_type == 1){
			chargebtn.setAndroidPageTag(IndexActivityAreaEnum.webSingle.getAndroidPage());
			chargebtn.setAndroidPageParam(DlgAndGoPage.getTagParam(Const.WEB_SITE+StaticWebUrlEnum.USER_VIP_SERVICE_H5.getPath(),"VIP特色服务"));
			dlgAndGoPage.setBtnDataList(Arrays.asList(chargebtn));
		} else {
			chargebtn.setAndroidPageTag(IndexActivityAreaEnum.vipMemeberPage.getAndroidPage());
			chargebtn.setAndroidPageParam(DlgAndGoPage.getTagParam(0));
			dlgAndGoPage.setBtnDataList(Arrays.asList(cancelbtn,chargebtn));
		}

		dlgAndGoPage.setHintInfo(hintInfo);
		checkDto.setDlgAndGoPage(dlgAndGoPage);
		checkDto.setiOSTag(10000);
		return ActionResult.success(checkDto);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public ActionResult payOrderNew(long userId,Long otherId,int avType,int sponsor,Long serialNum,int state, Long wyChatId) throws Exception {
	
		Map<String, Object> data = new HashMap<>();
		data.put("serialNum", serialNum);
	
		Long orderId = serialNum;
		Long oldOtherId = otherId;
		//IOS这货之前一直没转serialNum值
		if(Tools.isNull(serialNum)){
			orderId = getUserOrderId(userId);
			if(Tools.isNull(orderId)){
				orderId = getUserOrderId(otherId);
			}
		}
		
		if(Tools.isNull(orderId)){
			logger.info("payOrder:"+serialNum+";通话订单号为空");
			data.put("state", 0);
			return ActionResult.success(data);
		}
		data.put("serialNum", orderId);
	
		VchatRoomEntity vchatRoom = vchatRoomContract.findByProperty("orderId", orderId);
		if(Tools.isNull(vchatRoom)){
			logger.info("payOrder:"+serialNum+";通话订单号不存在");
			data.put("state", 0);
			data.put("serialNum", orderId);
			return ActionResult.success(data);
		}
		if(Tools.isNull(vchatRoom.getWy_chatid()) || vchatRoom.getWy_chatid()==0 ){
			if(Tools.isNotNull(wyChatId) && wyChatId > 0L){
				vchatRoomContract.updateWyChatId(vchatRoom.getId(), wyChatId);
			}
		}
		
		if(oldOtherId.equals(vchatRoom.getAnchorid())){
			AnchorOnlineEntity anchor = anchorOnlineContract.findByProperty("userid", otherId);
			if(anchor.getParent_userid() > 0){
				// 主播子账号切换到主账号
				otherId = anchor.getParent_userid();   
			}
		}
		String prefix = getPrefix(userId, otherId);
		final Long otherIdStr =otherId;
		
		//state为0 是关闭通话
		if(state == 0){
			setVchatRoomCloseFlag(orderId);
			return recvExitNew(vchatRoom, userId, otherId,oldOtherId);
		}
		
		if(getVchatRoomCloseFlag(orderId)){
			return recvExitNew(vchatRoom, userId, otherId,oldOtherId);
		}
		
		DiamondResultDto<Map<String, Long>> result = null;
		
		if(vchatRoom.getSys_duration()>0){
			logger.info("payOrder:"+serialNum+";其中一方退出");
			data.put("state", 0);
			return ActionResult.success(data);
		}
		
			cacheRedis.hset(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX+ prefix + userId, ENTER_ROOM, "1");
			cacheRedis.set(AgentRedisCacheConst.VCHAT_USER_ORDER_PREFIX+userId,""+orderId);
			cacheRedis.expire(AgentRedisCacheConst.VCHAT_USER_ORDER_PREFIX + userId, ROOM_CREATE_EXPIRE);
			cacheRedis.zadd(AgentRedisCacheConst.VCHAT_USER_VIDEO_ONLINE, System.currentTimeMillis(), "" + userId);
			
			anchorOnlineContract.updateState(userId, AnchorOnlineStateEnum.talking.getCode(),false);

			boolean isUseridOnline = cacheRedis.exists(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX+ prefix + userId);
			boolean isAnchorOnline = cacheRedis.exists(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX+ prefix + otherId);
			if (!(isUseridOnline && isAnchorOnline)) {
				 data.put("state", 0);
				 data.put("note", "对方掉线");
				 logger.info("pay_ERROR:userId={},otherId={},其中一方掉线",userId,otherId);
				 String shutInfo = vchatRoom.getUserid().equals(userId)?"主播掉线":"用户掉线";
				 vchatRoomContract.userExit(vchatRoom.getId(),null,shutInfo);
				 if(vchatRoom.getSys_duration() == 0){
					int rows = closeVchatRoom(vchatRoom.getId(),vchatRoom.getOrderId(),shutInfo);
					if(rows > 0){
						checkoutPayOder(vchatRoom);
					}
				 }
				 return ActionResult.success(data);
			}
		boolean isUserid = cacheRedis.exists(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX+ prefix + userId);
		boolean isEnterRoom = cacheRedis.exists(AgentRedisCacheConst.VCHAT_ROOM_CREATE_PREFIX + orderId);
		if (isUserid) {
			cacheRedis.expire(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX + prefix + userId, ROOM_CREATE_EXPIRE);
		}
		if (isEnterRoom) {
			cacheRedis.expire(AgentRedisCacheConst.VCHAT_ROOM_CREATE_PREFIX + orderId, ROOM_CREATE_EXPIRE);
		}
		
		data.put("serialNum", orderId);
		data.put("state", 1);
		String text = "";
		String balanceStr = "";
		
		int rows =0;
		rows = vchatRoomContract.payYX(vchatRoom.getId(),Tools.isNull(vchatRoom.getPay_time()));
		//返回2 为第一次扣费，表示接听成功
		if( rows == 2){
			userSubscribeAnchorAgent.checkReturnSubscribeDiamond(vchatRoom.getUserid(), vchatRoom.getAnchorid(),vchatRoom.getAv_type() == 2?0:1 );
			anchorOnlineContract.updateState(userId, AnchorOnlineStateEnum.talking.getCode(),true);
			if(vchatRoom.getSponsor_user().equals(vchatRoom.getUserid())){
				anchorOnlineContract.addRecvNum(Tools.parseLong(vchatRoom.getAnchorid()),avType);
				vchatRoomContract.updateDialingFalg(vchatRoom.getId());
			}
			if(vchatRoom.getSponsor_user().equals(vchatRoom.getAnchorid())){
				userOrdinaryOnlineListAgent.addAnchorDialingRecvUser(vchatRoom.getAnchorid(), vchatRoom.getUserid());
			}
			if(vchatRoom.getFree_vchart_falg()>0){
				FreeVideoChatExperienceEntity freeVideoChat = freeVideoChatExperienceContract.findByProperty("userid", vchatRoom.getUserid());
				if(Tools.isNotNull(freeVideoChat)){
					// 设置关闭蒙层标记
					setCloseObscurationFalg(vchatRoom.getUserid(),vchatRoom.getId());
					freeVideoChatExperienceContract.deleteById(freeVideoChat.getId());
				}
			}
			
			String userText = "严谨出现色情、诈骗等违规内容，一经发现将封号处理，并上报上级有关部门\n未满18周岁不得使用本软件，请自行离开。\n  \n 禁止添加微信，QQ等第三方社交账号，如遭遇诈骗等情况，平台概不负责！";
			SysConfigEntity sysConfig = sysConfigAgent.getSysConfig(com.tigerjoys.shark.miai.agent.constant.Const.HINT_INFO_ENTER_VCHAT_USER);
			if(Tools.isNotNull(sysConfig)){
				userText = sysConfigAgent.replaceToN(sysConfig.getValue());
			}
			ExecutorServiceHelper.executor.execute(new SendMessageNotifyThread(vchatRoom.getUserid(),userText));
			String anchorText = "严谨出现色情、诈骗等违规内容，一经发现将封号处理，并上报上级有关部门。\n严禁主动诱导用户添加微信，或导入第三方色情赌博平台，如发现直接封号，不予提现。";
			sysConfig = sysConfigAgent.getSysConfig(com.tigerjoys.shark.miai.agent.constant.Const.HINT_INFO_ENTER_VCHAT_ANCHOR);
			if(Tools.isNotNull(sysConfig)){
				anchorText = sysConfigAgent.replaceToN(sysConfig.getValue());
			}
			ExecutorServiceHelper.executor.execute(new SendMessageNotifyThread(getRealUserId(vchatRoom.getAnchorid()),anchorText));
			String anchorText2 = "让新用户充值可获得最低30元的奖励";
			ExecutorServiceHelper.executor.execute(new SendMessageNotifyThread(getRealUserId(vchatRoom.getAnchorid()),anchorText2,"#f1ac19"));
			
		}
		vchatRoom = vchatRoomContract.findById(vchatRoom.getId());
		if(rows == 1 && vchatRoom.getFree_vchart_falg() == VchatRoomFreeVchartFalgEnum.free_vchart.getCode() ){
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, -3);
			PageModel pgModel = PageModel.getPageModel();
			pgModel.addQuery(Restrictions.eq("user_id", vchatRoom.getUserid()));
			pgModel.addQuery(Restrictions.eq("status",1));
			pgModel.addQuery(Restrictions.gt("update_time", Tools.getFormatDate(cal.getTime(), "yyyy-MM-dd HH:mm:ss")));
			if(rechargeOrderContract.count(pgModel) == 0 ){
				 if(vchatRoom.getSys_duration() == 0){
					 closeVchatRoom(vchatRoom.getId(),vchatRoom.getOrderId(),"体验退出");
				 }
				 data.put("state", 0);
				 //data.put("note", "免费聊结束");
				 data.put("note", "通话结束");
				 vchatRoomContract.userExit(vchatRoom.getId(),null,"体验退出");
				 return ActionResult.success(data);
			}else{
				vchatRoomContract.updateFreeVchartFalg(vchatRoom.getId(),VchatRoomFreeVchartFalgEnum.free_vchart_charge.getCode());
			}
		}
		if(rows > 0 && vchatRoom.getPrice() == 0){
			vchatRoomContract.updateFreeVchatDuration(vchatRoom.getId());
		}else if(rows > 0 && vchatRoom.getFree_vchart_falg() == VchatRoomFreeVchartFalgEnum.free_vchart.getCode() && vchatRoom.getVchat_duration() == 0 ){
			vchatRoomContract.updateFreeVchatDuration(vchatRoom.getId());
		}else if (rows > 0  && checkDialTimeLimit() && userWeekCardAgent.updateDailyWeekCardDuration(vchatRoom.getUserid(), (vchatRoom.getPrice()<120? 4:5))>0) {
			vchatRoomContract.updateWeekCardDuration(vchatRoom.getId());
			anchorIntimateRankingsContract.addIncome(vchatRoom.getAnchorid(), vchatRoom.getUserid(), vchatRoom.getPrice());
		}else if (rows > 0 ) {
				int price = Tools.intValue(Math.round(vchatRoom.getPrice()*vchatRoom.getPay_discount()));
				result = userDiamondAgent.mediaChatInMinute(avType, vchatRoom.getUserid(), vchatRoom.getAnchorid(),
						orderId, price, UserVideoChatRecordChannelTypeEnum.video_tencent);
				if(AgentErrorCodeEnum.success.getCode() == result.getCode()){
					vchatRoomContract.updateTotalPrice(vchatRoom.getId(),price);
					anchorIntimateRankingsContract.addIncome(vchatRoom.getAnchorid(), vchatRoom.getUserid(), price);
					while(vchatRoomContract.payYX(vchatRoom.getId(),false)>0){
						result = userDiamondAgent.mediaChatInMinute(avType, vchatRoom.getUserid(), vchatRoom.getAnchorid(),
						orderId, price, UserVideoChatRecordChannelTypeEnum.video_tencent);
						if(AgentErrorCodeEnum.success.getCode() == result.getCode()){
						vchatRoomContract.updateTotalPrice(vchatRoom.getId(),price);
						anchorIntimateRankingsContract.addIncome(vchatRoom.getAnchorid(), vchatRoom.getUserid(), price);
						} else {
							break;
						}
					}
					if (AgentErrorCodeEnum.success.getCode() == result.getCode()) {
						Long balance = result.getData().get("balance");
						if(Tools.isNotNull(balance)){
							if(balance>0){
								AnchorOnlineEntity anchor = anchorOnlineContract.findByProperty("userid", vchatRoom.getAnchorid());
								if(Tools.isNotNull(anchor)){
									if(anchor.getStar()>3){
										balanceStr = balance+"钻";
									}else{
										balanceStr = "有钻";	
									}
								}else{
									balanceStr = "有钻";	
								}
							}else{
								balanceStr = "无钻";	
							}
						}
						logger.info("pay_INFO:userId=" + userId + ";otherId:" + otherId + ";balance:" + balance);
						if (Tools.isNotNull(balance)) {
								if (balance >= price * 3 && balance < price * 4) {
									if (userId == vchatRoom.getUserid()) {
										data.put("note", "余额还剩3分钟可聊哦");
										DlgAndGoPageNew dlgAndGoPage = new DlgAndGoPageNew();
										dlgAndGoPage.setAndroidPageTag(DlgAndGoPageEnum.payDiamondDlg.getAndroidPage());
										dlgAndGoPage.setAndroidPageParam(DlgAndGoPage.getTagParam("你的通话不足三分钟\n为了畅聊 ,请充值",vchatRoom.getAv_type()==1? 1:0));
										data.put("dlgAndGoPage", dlgAndGoPage);
									}else{
										setUserPayShowNote(orderId, vchatRoom.getUserid(), "余额还剩3分钟可聊哦");
										setUserPayShowDlg(orderId, vchatRoom.getUserid(), "chargeQuickDlg");
									}
									sendTcpMsgUserNotEnough(getRealUserId(vchatRoom.getAnchorid()));
								} else if (balance >= price * 2 && balance < price * 3) {
									if (userId == vchatRoom.getUserid()) {
										data.put("note", "余额还剩2分钟可聊哦");
									}else{
										setUserPayShowNote(orderId, vchatRoom.getUserid(), "余额还剩2分钟可聊哦");
									}
									sendTcpMsgUserNotEnough(getRealUserId(vchatRoom.getAnchorid()));
								} else if (balance < price * 2) {
									if (userId == vchatRoom.getUserid()) {
										data.put("note", "余额还剩1分钟可聊哦");
									}else{
										setUserPayShowNote(orderId, vchatRoom.getUserid(), "余额还剩1分钟可聊哦");
									}
									sendTcpMsgUserNotEnough(getRealUserId(vchatRoom.getAnchorid()));
								}
						}
						VChatVideoTCPDto dto = new VChatVideoTCPDto();
						dto.setType(VChatVideoTCPTypeEnum.video.getCode());
						dto.setSubType(VChatVideoStatusEnum.user_balance.getCode());
						dto.setData(balanceStr);
						neteaseAgent.pushOneAttachMessage(vchatRoom.getUserid(),getRealUserId(vchatRoom.getAnchorid()), JsonHelper.toJson(dto)); // 主播用户可能是假账户
					}
				}
				//判断最后一次扣费是不否余额不足
				if(AgentErrorCodeEnum.not_enough.getCode() == result.getCode()){
					if(userId == vchatRoom.getUserid()){
						DlgAndGoPageNew dlgAndGoPage = new DlgAndGoPageNew();
						data.put("note", "您的余额不足，充值继续聊天~");
						String hintInfo = "你的钻石不足，快快充值\n享受与大V独处的时光吧！";
						SysConfigEntity sysConfig = sysConfigAgent.getSysConfig(com.tigerjoys.shark.miai.agent.constant.Const.HINT_INFO_DIAMOND_NOTENOUGH);
						if(Tools.isNotNull(sysConfig)){
							hintInfo = sysConfig.getValue();
						}
						
						BtnData chargebtn = new BtnData();
						chargebtn.setBtnName("去充值");
						chargebtn.setAndroidPageTag(DlgAndGoPageEnum.webSingle.getAndroidPage());
						chargebtn.setAndroidPageParam(DlgAndGoPage.getTagParam(Const.WEB_SITE+"/api/income/zuanList","我的钱包"));
						dlgAndGoPage.setBtnDataList(Arrays.asList(chargebtn));
						
						dlgAndGoPage.setAction(0);
					
						dlgAndGoPage.setIosBtnName("去充值");
						dlgAndGoPage.setIosPageTag(DlgAndGoPageEnum.webSingle.getIosPage());
						dlgAndGoPage.setIosPageParam(DlgAndGoPage.getTagParam(0));
						data.put("iOSTag", 1);
						dlgAndGoPage.setHintInfo(hintInfo);
						data.put("dlgAndGoPage", dlgAndGoPage);
						 if(vchatRoom.getSys_duration() == 0){
							 closeVchatRoom(vchatRoom.getId(),vchatRoom.getOrderId(),"余额不足退出");
						 }
					}
					data.put("state", 0);
					 vchatRoomContract.userExit(vchatRoom.getId(),null,"无钻");
				}else if(result.getCode()>0){
					data.put("state", 0);
					data.put("note", "数据异常");
					 if(vchatRoom.getSys_duration() == 0){
						 try{
							 closeVchatRoom(vchatRoom.getId(),vchatRoom.getOrderId(),"扣费异常退出");
						 }catch (Exception e) {
							 logger.info("pay_INFO:roomId=" + vchatRoom.getId() + "orderId:"+vchatRoom.getOrderId());
						}
					 }
					 vchatRoomContract.userExit(vchatRoom.getId(),null,"扣费异常");
					logger.info("pay_INFO:userId=" + userId + ";otherId:" + otherId + ";serialNum:" + serialNum+";result.code:"+result.getCode());
				}
			}else if (userId == vchatRoom.getUserid()) {
				String showNote = getUserPayShowNote(otherId,vchatRoom.getUserid());
				if(Tools.isNotNull(showNote)){
					data.put("note", showNote);
				}
				String dlg = getUserPayShowDlg(otherId,vchatRoom.getUserid());
				if(Tools.isNotNull(dlg)){
					if("chargeQuickDlg".equals(dlg)){
						DlgAndGoPageNew dlgAndGoPage = new DlgAndGoPageNew();
						dlgAndGoPage.setAndroidPageTag(DlgAndGoPageEnum.payDiamondDlg.getAndroidPage());
						dlgAndGoPage.setAndroidPageParam(DlgAndGoPage.getTagParam("你的通话不足三分钟\n为了畅聊 ,请充值",vchatRoom.getAv_type()==1? 1:0));
						data.put("dlgAndGoPage", dlgAndGoPage);
					}
				}
			}
		
		return ActionResult.success(data);
		
	}

	@Transactional(rollbackFor = Exception.class)
	public int closeVchatRoom(long roomId,long orderId,String shutInfo) throws Exception{
		int rows = vchatRoomContract.updateFinishTime(roomId);
		logger.info("closeVchatRoom:"+"roomId:"+roomId+";orderId:"+orderId+";shutInfo:"+shutInfo);
		if(rows>0 ){
			try{
				vchatRoomSettlementAgent.vchatRoomSettlement(roomId);
			}catch (Exception e){
				logger.info("pay_settlement:roomid="+roomId,e);
			}
			return 1;
		}else{
			return 0;
		}
		
	}
	
	

	@Transactional(rollbackFor = Exception.class)
	public ActionResult recvExitNew(VchatRoomEntity vchatRoom, long userId, Long otherId,Long oldOtherId) throws Exception {
		// state为0 是关闭通话
		Map<String, Object> data = new HashMap<>();
		data.put("state", 0);
		data.put("serialNum", vchatRoom.getOrderId());
		String prefix = getPrefix(userId, otherId);
		final Long otherIdStr = otherId;
		DiamondResultDto<Map<String, Long>> result = null;
		if(vchatRoom.getSys_duration() == 0){
			int rows = closeVchatRoom(vchatRoom.getId(),vchatRoom.getOrderId(),"客户端发起退出");
			if(rows > 0){
				checkoutPayOder(vchatRoom);
			}
		}
		
		//修改用户充值表
		try {
			payUserAgent.updateChat(vchatRoom);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		anchorOnlineContract.updateState(userId, AnchorOnlineStateEnum.online.getCode(),true);
		
		vchatRoom = vchatRoomContract.findById(vchatRoom.getId());
		
		if(vchatRoom.getVchat_duration()>=3){
			if(vchatRoom.getAv_type() ==1){
				userPointAgent.changePointAccount(vchatRoom.getUserid(), UserPointAccountLogTypeEnum.audio_chat.getCode(), 1, String.valueOf(vchatRoom.getId()), UserPointAccountLogTypeEnum.audio_chat.getDesc());
			}else{
				userPointAgent.changePointAccount(vchatRoom.getUserid(), UserPointAccountLogTypeEnum.video_chat.getCode(), 1, String.valueOf(vchatRoom.getId()), UserPointAccountLogTypeEnum.video_chat.getDesc());
			}
		}
		
		String orderIdStr = vchatRoom.getOrderId()+"";
		String otherUserId = getOtherUserId(userId, otherId);
		if (Tools.isNotNull(otherUserId)) {
			anchorOnlineContract.updateState(otherId, AnchorOnlineStateEnum.online.getCode(),true);
			cacheRedis.transaction(tx -> {
				tx.del(AgentRedisCacheConst.VCHAT_USER_ORDER_PREFIX + otherIdStr);
				tx.zrem(AgentRedisCacheConst.VCHAT_USER_VIDEO_ONLINE, "" + otherIdStr);
			});
			logger.info("pay_INFO:userId={},otherId={},删除对方信息", userId, otherId);
		}
		cacheRedis.transaction(tx -> {
			tx.del(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX + prefix + userId);
			tx.del(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX + prefix + otherIdStr);
			tx.del(AgentRedisCacheConst.VCHAT_ROOM_CREATE_PREFIX + orderIdStr);
			tx.del(AgentRedisCacheConst.VCHAT_USER_ORDER_PREFIX + userId);
			tx.zrem(AgentRedisCacheConst.VCHAT_USER_VIDEO_ONLINE, "" + userId);
		});
		
		if(vchatRoom.getPorn_falg() == 0){
			if (userId == vchatRoom.getUserid()) {
				String paramStr = DlgAndGoPage.getTagParam(vchatRoom.getAnchorid(),vchatRoom.getOrderId());
	
				long balance = userDiamondAgent.getDiamondBalance(vchatRoom.getUserid());
				
				
				if (balance > vchatRoom.getPrice()*CHARGE_DIAMOND_SHOW || vchatRoom.getPrice() == 0) {
					DlgAndGoPageNew dlgAndGoPage = new DlgAndGoPageNew();
					dlgAndGoPage.setAndroidPageTag(DlgAndGoPageEnum.vchatEndEvaluate.getAndroidPage());
					dlgAndGoPage.setAndroidPageParam(paramStr);
					dlgAndGoPage.setIosPageTag(DlgAndGoPageEnum.vchatEndEvaluate.getIosPage());
					data.put("iOSTag", 0);
					dlgAndGoPage.setIosPageParam(paramStr);
					data.put("dlgAndGoPage", dlgAndGoPage);
				}else if(balance <= vchatRoom.getPrice()*CHARGE_DIAMOND_SHOW){
					DlgAndGoPageNew dlgAndGoPage = new DlgAndGoPageNew();
					String hintInfo = "你的通话时间不多了\n和小姐姐畅聊，请充值";
					
					
					BtnData chargebtn = new BtnData();
					chargebtn.setBtnName("去充值");
					chargebtn.setAndroidPageTag(DlgAndGoPageEnum.webSingle.getAndroidPage());
					chargebtn.setAndroidPageParam(DlgAndGoPage.getTagParam(Const.WEB_SITE+"/api/income/zuanList","我的钱包"));
//					BtnData cancelBtn = new BtnData();
//					cancelBtn.setBtnName("取消");
//					dlgAndGoPage.setBtnDataList(Arrays.asList(chargebtn,cancelBtn));
					dlgAndGoPage.setBtnDataList(Arrays.asList(chargebtn));
					dlgAndGoPage.setIosBtnName("去充值");
					dlgAndGoPage.setIosPageTag(DlgAndGoPageEnum.webSingle.getIosPage());
					dlgAndGoPage.setIosPageParam(DlgAndGoPage.getTagParam(0));
					
					dlgAndGoPage.setHintInfo(hintInfo);
					data.put("iOSTag", 1);
					data.put("dlgAndGoPage", dlgAndGoPage);
				}
			}else{
				String tips = "标记非法拉人,经确认后给预奖励";
				SysConfigEntity config = sysConfigContract.findByProperty("name", Const.ANCHOR_TO_USER_EVALUATION_INFO);
				try {
					if (Tools.isNotNull(config)) {
						JSONObject ctrl = JsonHelper.toJsonObject(config.getValue());
						if (Tools.isNotNull(ctrl)) {
							tips = ctrl.getString("evaluationInfo");
						}
					}
				} catch (Exception e) {
					logger.info("json解析错误",e);
				}
				String incomeInfo = "";
				String incomeAmount =getVchatRoomSettlementIncome(vchatRoom.getOrderId()+"");
				if(Tools.isNotNull(incomeAmount)){
					incomeInfo="收益"+incomeAmount+"金币";
				}
				AnchorToUserEvaluationShowDto dto = new AnchorToUserEvaluationShowDto();
				//String incomeInfo = "本次通话"+vchatRoom.getVchat_duration()+"分钟,收益"+vchatRoom.getTotal_price()+"钻";
				if(vchatRoom.getFree_vchart_falg() == VchatRoomFreeVchartFalgEnum.free_vchart.getCode()){
					//incomeInfo = "本次通话"+vchatRoom.getVchat_duration()+"分钟,免费体验";
					//incomeInfo = "本次通话"+vchatRoom.getVchat_duration()+"分钟";
				}else if(vchatRoom.getFree_vchart_falg() == VchatRoomFreeVchartFalgEnum.free_vchart_charge.getCode()){
					//incomeInfo = "本次通话"+vchatRoom.getVchat_duration()+"分钟,收益"+vchatRoom.getTotal_price()+"钻(1分钟免费体验)";
					//incomeInfo = "本次通话"+vchatRoom.getVchat_duration()+"分钟,收益"+vchatRoom.getTotal_price()+"钻";
				}
				dto.setIncome(incomeInfo);
				/*
				RequestHeader header = RequestUtils.getCurrent().getHeader();
				if(!Const.IS_TEST && "App Store".equalsIgnoreCase(header.getChannel())){
					dto.setIncome("");	
				}
				*/
				dto.setOtherUserId(vchatRoom.getUserid());
				dto.setSerialNum(vchatRoom.getOrderId());
				dto.setOptionList(annchorToUserEvaluationList()); 
				dto.setTips(tips);
				String paramStr = DlgAndGoPage.getTagParam(JsonHelper.toJson(dto));
				DlgAndGoPageNew dlgAndGoPage = new DlgAndGoPageNew();
				dlgAndGoPage.setAndroidPageTag(DlgAndGoPageEnum.anchorToUserEvaluateDialog.getAndroidPage());
				dlgAndGoPage.setAndroidPageParam(paramStr);
				dlgAndGoPage.setIosPageTag(DlgAndGoPageEnum.anchorToUserEvaluateDialog.getIosPage());
				dlgAndGoPage.setIosPageParam(paramStr);
				data.put("dlgAndGoPage", dlgAndGoPage);
			}
		}else{
			//不需要弹框
			data.put("iOSTag", 2);
		}
		
		if (getPushRevcFalg(vchatRoom.getOrderId())) {
			
			Message msg = new Message(vchatRoom.getId()+"",otherId+","+oldOtherId+","+userId,2000);
			vchatRoomRecvExitSendMsgConsumer.put(msg);
		}
		
		return ActionResult.success(data);
	}
	
	
	
	
	
	public DoubleVChatVideoDto getRoomInfo(UserBO user, AnchorOnlineEntity anchor, long payUserId, VchatRoomEntity room,
			long orderId, long balance,String message) throws Exception {
		DoubleVChatVideoDto dto = new DoubleVChatVideoDto();
		dto.setOrderId(orderId);
		dto.setSeller(anchor.getUserid() == user.getUserid() ? false : true);
		//dto.setOtherBalance(balance + "钻");
		dto.setOtherBalance("有钻");
		dto.setOtherHeadUrl(Tools.isNull(user.getPhoto())?Const.getDefaultUserFace():Const.getCdn(user.getPhoto()));
		dto.setOtherId(user.getUserid());
		dto.setOtherName(user.getNickname());
		dto.setRoomId(room.getId());
		String msg = "";
		VacuateConfigDto vacuate = sysConfigAgent.vacuate();
		VchatRoomObscurationConfigDto obscuration = sysConfigAgent.obscurationConfig();
		//新用户
		/*
		if(!dto.isSeller()){
			if(userFirstRechargeLogContract.checkFirstRecharge(room.getUserid(),FirstPayTypeEnum.diamond.getCode())){
				dto.setTurnOffLight(1);
			}
		}
		*/
		RequestHeader header = RequestUtils.getCurrent().getHeader();
		GuardVipCategoryEntity guardList = anchorDefendAgent.getCurrentAnchorDefendByUser(room.getUserid(), room.getAnchorid());
		if(dto.isSeller()){
			if(Tools.isNotNull(guardList)){
				dto.setGuardIcon(Const.getCdn(guardList.getIcon_small()));
			}else{
				dto.setGuardIcon("");
			}
			dto.setVip(user.vipValue());
			dto.setCustom("");
		}else{
			if(Tools.isNotNull(guardList)){
				dto.setGuardIcon(Const.getCdn("/img/guardvip/extend_guard.png"));
			}else{
				dto.setGuardIcon(Const.getCdn("/img/guardvip/create_guard.png"));
			}
			String  anchorVideo = anchorDialVideoAgent.anchorDialingVideo(room.getAnchorid());
			if(room.getAnchorid().equals(room.getSponsor_user())){
				dto.setPlayVideoUrl(anchorVideo);
			}else{
				dto.setPlayVideoUrl("");
			}
			
			
		}
		if(room.getPrice()>0){
			if (dto.isSeller()) {
				DecimalFormat df = new DecimalFormat("#.##");
				String incomeMoney = df.format(room.getSettle_price()*vacuate.getDiamondToMoney());
				msg = "每分钟收入" + incomeMoney + "元";
				//dto.setInfo1("对方余额:"+dto.getOtherBalance());
				dto.setInfo1("");
				dto.setInfo2(message);
				dto.setShowFollow(userFriendAgent.isAttention(anchor.getUserid(),payUserId)?0:1);
				dto.setNormalLevel("LV"+user.getDegreeid());
			} else {
				msg = "每分钟支出" + room.getPrice() + "钻";
				dto.setShowFollow(userFriendAgent.isAttention(payUserId,anchor.getUserid())?0:1);
			}
		}
		if(room.getFree_vchart_falg() == VchatRoomFreeVchartFalgEnum.free_vchart.getCode()){
			
			if (dto.isSeller()){
				//msg = "体验用户";
				msg = "";
			}else{
				msg = "免费体验";
			}
			
			
			
			/*
			boolean showFalg = true;
			RequestHeader header = RequestUtils.getCurrent().getHeader();
			if(Tools.isNotNull(header)){
				if("Huawei_yoyo3".equalsIgnoreCase(header.getChannel())){
					showFalg = false;
				}
			}
			*/
			
			String cityCode = header.getCityCode();
			long userId = header.getUserid();
			UserBO userBo = userAgent.findById(userId);
			if("332".equals(cityCode) || userBo.getProvinceid()==3958L ){
				obscuration.setType(1);
			}
			if( "131".equals(cityCode) || userBo.getProvinceid()==3593L){
				obscuration.setType(1);
			}
			
			if (dto.isSeller() && obscuration.getType()>0){
				dto.setMongoliaText(obscuration.getAnchorText());
			}else if(obscuration.getType()>0 ){
				
				Map<Integer,String> videoPathMap = new HashMap<>();
				videoPathMap.put(1, obscuration.getVideoPath1());
				videoPathMap.put(2, obscuration.getVideoPath2());
				videoPathMap.put(3, obscuration.getVideoPath3());
				Random random = new Random();
				int index = 0;
			   if(anchor.getStar()<=3){
				   index = random.nextInt(2)+1;
				   dto.setMaskLayerUrl(ServiceHelper.getCdnVideo(videoPathMap.get(index)));
				   if(Const.IS_TEST){
					   dto.setMaskLayerUrl(Const.getCdn(videoPathMap.get(index)));
				   }
			   }else if(anchor.getStar()==4){
				   index = random.nextInt(3)+1;
				   dto.setMaskLayerUrl(ServiceHelper.getCdnVideo(videoPathMap.get(index)));
				   if(Const.IS_TEST){
					   dto.setMaskLayerUrl(Const.getCdn(videoPathMap.get(index)));
				   }
			   }else if(anchor.getStar()>=5){
				   index = random.nextInt(2)+2;
				   dto.setMaskLayerUrl(ServiceHelper.getCdnVideo(videoPathMap.get(index)));
				   if(Const.IS_TEST){
					   dto.setMaskLayerUrl(Const.getCdn(videoPathMap.get(index)));
				   }
			   }
				dto.setMongoliaText(obscuration.getUserText());
			}
		}
		dto.setIncomePayInfo(msg);
		
		if (Const.IOS_TEST_HIDE_INFO_MAP.containsKey(header.getPackageName())) {
			dto.setIncomePayInfo("");
			dto.setOtherBalance("");
		}
		
		if (iOSUserSmsAgent.getUserIdList().containsKey(RequestUtils.getCurrent().getUserid())) {
			dto.setIncomePayInfo("");
			dto.setOtherBalance("");
		}
		
		return dto;
	}
	
	
	
	
	
	/**
	 * 
	 * @param userId 	普通用户
	 * @param otherId	主播
	 * @param roomId	房间号
	 * @param orderId	房间ID
	 * @param price		钻石
	 */
	public void setRoomRedis(long userId, long otherId, long roomId, long orderId, int price) {
		String prefix = getPrefix(userId,otherId);
		cacheRedis.transaction(tx -> {
			//用户与订单关联信息 （可以覆盖）
			tx.set(AgentRedisCacheConst.VCHAT_USER_ORDER_PREFIX + userId, orderId + "");
			tx.expire(AgentRedisCacheConst.VCHAT_USER_ORDER_PREFIX + userId, ROOM_CREATE_EXPIRE);
			
			tx.set(AgentRedisCacheConst.VCHAT_USER_ORDER_PREFIX + otherId, orderId + "");
			tx.expire(AgentRedisCacheConst.VCHAT_USER_ORDER_PREFIX + otherId, ROOM_CREATE_EXPIRE);
			
			//用户信息
			tx.hset(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX + prefix + userId, ROOM_ID, roomId + "");
			tx.hset(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX + prefix + userId, PAY_FLAG, "1");
			tx.hset(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX + prefix + userId, ORDER_ID, orderId + "");
			tx.hset(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX + prefix + userId, ENTER_ROOM, "0");
			tx.hset(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX + prefix + userId, PAYPRICE, price + "");
			tx.hset(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX + prefix + userId, OTHERUSERID, otherId + "");
			tx.expire(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX + prefix + userId, ROOM_CREATE_EXPIRE);
			
			//主播信息
			tx.hset(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX + prefix + otherId, ROOM_ID, roomId + "");
			tx.hset(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX + prefix + otherId, PAY_FLAG, "0");
			tx.hset(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX + prefix + otherId, ORDER_ID, orderId + "");
			tx.hset(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX + prefix + otherId, ENTER_ROOM, "0");
			tx.hset(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX + prefix + otherId, PAYPRICE, price + "");
			tx.hset(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX + prefix + otherId, OTHERUSERID, userId + "");
			tx.expire(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX + prefix + otherId, ROOM_CREATE_EXPIRE);
			
			//订单与用户关联信息
			tx.hset(AgentRedisCacheConst.VCHAT_ROOM_CREATE_PREFIX + orderId, PAYUSER, userId + "");
			tx.hset(AgentRedisCacheConst.VCHAT_ROOM_CREATE_PREFIX + orderId, ANCHOR, otherId + "");
			tx.hset(AgentRedisCacheConst.VCHAT_ROOM_CREATE_PREFIX + orderId, PAYPRICE, price + "");
			tx.expire(AgentRedisCacheConst.VCHAT_ROOM_CREATE_PREFIX + orderId, ROOM_CREATE_EXPIRE);
			
			
		});
	}
	//修改删除数据
	public ActionResult exitRoom(long oldUserId,Long oldOtherId,int avType) throws Exception{
		HashMap<String, Object> outHsmp = new HashMap<>();
	    Long userid = getRealUserId(oldUserId);
	    Long otherId = getRealUserId(oldOtherId);
		 DlgAndGoPageNew dlgAndGoPage = new DlgAndGoPageNew();
		String prefix = getPrefix(userid,otherId);
		String orderId = cacheRedis.hget(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX + prefix + userid, ORDER_ID);
		if(Tools.isNotNull(orderId)){
			String payUserId = cacheRedis.hget(AgentRedisCacheConst.VCHAT_ROOM_CREATE_PREFIX+orderId, PAYUSER);
			String anchorId = cacheRedis.hget(AgentRedisCacheConst.VCHAT_ROOM_CREATE_PREFIX+orderId, ANCHOR);
		
			//主播主动关闭
			if(Tools.isNotNull(anchorId) && userid==Tools.parseLong(anchorId)){
				anchorOnlineContract.updateState(Tools.parseLong(anchorId), AnchorOnlineStateEnum.online.getCode(),true);
			}
			VchatRoomEntity vchatRoom = vchatRoomContract.findByProperty("orderId", Tools.parseLong(orderId));
			if(Tools.isNotNull(vchatRoom)){
				closeVchatRoom(vchatRoom.getId(),vchatRoom.getOrderId(),"对方拒绝退出");
				
				if(vchatRoom.getSponsor_user().equals(vchatRoom.getUserid())){
					if(getDialingFalg(userid)){
						vchatRoomContract.updateDialingFalg(vchatRoom.getId());
					}
				}
				
			}
			if(vchatRoom.getSponsor_user().equals(oldUserId)){
				//判断对方缓存是否为空
				String otherUserId = getOtherUserId(userid,otherId);
				if(Tools.isNotNull(otherUserId)){
						anchorOnlineContract.updateState(otherId, AnchorOnlineStateEnum.online.getCode(),true);
						cacheRedis.transaction(tx -> {
							tx.del(AgentRedisCacheConst.VCHAT_USER_ORDER_PREFIX + otherId);
							tx.zrem(AgentRedisCacheConst.VCHAT_USER_VIDEO_ONLINE, "" +otherId);
						
							tx.del(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX + prefix + payUserId);
							tx.del(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX + prefix + anchorId);
							tx.del(AgentRedisCacheConst.VCHAT_USER_ORDER_PREFIX +userid);
							tx.del(AgentRedisCacheConst.VCHAT_ROOM_CREATE_PREFIX + orderId);
							tx.zrem(AgentRedisCacheConst.VCHAT_USER_VIDEO_ONLINE, "" + userid);
						});
				}
				
			}
		
			////
			if(vchatRoom.getUserid() == oldUserId && vchatRoom.getSponsor_user().equals(oldUserId)){
				if(!channelCheckService.checkChannel() && getRecommentAnchorFalg(oldUserId)){
					 List<RecommendOnlineAnchorDto> onlineList = getRecommndOnlineAnchorList(oldUserId,avType);
					 if(Tools.isNotNull(onlineList)){
						 dlgAndGoPage.setAndroidPageTag(DlgAndGoPageEnum.recommendAnchorDlg.getAndroidPage());
						 HashMap<String, Object> hsmp = new HashMap<>();
						 hsmp.put("showInfo", dlgShowInfo);
						 hsmp.put("anchorList",onlineList);
						 dlgAndGoPage.setAndroidPageParam(DlgAndGoPage.getTagParam(JsonHelper.toJson(hsmp)));
					 }
				}
			}

			if (vchatRoom.getVchat_duration() == 0) {
				if (getPushUnCallFalg(vchatRoom.getOrderId())) {
					String msg = (vchatRoom.getAv_type()==1?"语音":"视频")+"通话未接听";
					Long recvUser = vchatRoom.getSponsor_user().equals(vchatRoom.getUserid())?vchatRoom.getAnchorid():vchatRoom.getUserid();
					neteaseAgent.pushOneMessage(vchatRoom.getSponsor_user(), recvUser, msg, true);
					// 主播为子账户的时修，发给主账户
					Long parentUserid = getRealUserId(vchatRoom.getAnchorid());
					if (!vchatRoom.getAnchorid().equals(parentUserid)) {
						neteaseAgent.pushOneMessage(vchatRoom.getUserid(), parentUserid,msg,false);
					}
				}
			}
		}
		logger.info("exitRoom:userId={},otherId={},orderId={}结束",userid,otherId,orderId);
		return ActionResult.success(dlgAndGoPage);
	}
	
	@Override
	public ActionResult userExit(long userId, Long otherId, long serialNum,int avType) throws Exception {
		long orderId = 0;
		if(serialNum>0){
			orderId = serialNum;
		}else{
			String prefix = getPrefix(userId,otherId);
			String orderIdStr = cacheRedis.hget(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX + prefix + userId, ORDER_ID);
			if(Tools.isNotNull(orderIdStr)){
				orderId = Tools.longValue(orderIdStr);
			}
		}
		
		VchatRoomEntity vchatRoom =  getVchatRoom(orderId);
		if(Tools.isNull(vchatRoom)){
			vchatRoom = getLastVchatRoom(userId,otherId,avType);
		}
		if(Tools.isNotNull(vchatRoom)){
			String info = vchatRoom.getUserid().equals(userId)?"用户":"主播";
			closeVchatRoom(vchatRoom.getId(),vchatRoom.getOrderId(),info+"主动退出");
			vchatRoomContract.userExit(vchatRoom.getId(), userId, info);
			return ActionResult.success();
		}
		
		return ActionResult.fail();
	}




	public ActionResult iosAudit(long userId, Long toUserId) throws Exception{
		UserBO toUser = userAgent.findById(toUserId);
		DoubleVChatVideoDto dto = new DoubleVChatVideoDto();
		dto.setOrderId(111111L);
		dto.setSeller( false );
		dto.setOtherBalance(2500 + "钻");
		dto.setOtherHeadUrl(Const.getCdn(toUser.getPhoto()));
		dto.setOtherId(toUser.getUserid());
		dto.setOtherName(toUser.getNickname());
		dto.setRoomId(1234);
		String msg = "";
		msg = "拔打中...";
		dto.setIncomePayInfo(msg);
		return ActionResult.success(dto);
	}




	@Override
	public ActionResult anchorEvaluationList(long serialNum) throws Exception {
		PageModel pageModel = PageModel.getPageModel();
		pageModel.addQuery(Restrictions.eq("state", 1));
		
		VchatRoomEntity room = getVchatRoom(serialNum);
		int avType = 2;
		if(Tools.isNotNull(room)){
			avType = room.getAv_type();
		}
		if(avType == 2){
			pageModel.addQuery(Restrictions.eq("type", 2));
		}else if(avType == 1){
			pageModel.addQuery(Restrictions.eq("type", 4));
		}
	
		List<AppLabelEntity> labelList = appLabelContract.load(pageModel);
		List<EvaluationDto> dtoList = new ArrayList<>();
		if(Tools.isNotNull(labelList)){
			labelList.forEach(v->{
				dtoList.add(EvaluationDto.preDto(v));
			});
		}
		AnchorEvaluationDto dto = new AnchorEvaluationDto();
		dto.setSelectNum(2);
		dto.setShowInfo("给她个评价吧，最多选"+dto.getSelectNum()+"个");
		dto.setEvaluationList(dtoList);
		return ActionResult.success(dto);
	}
	
	
	
	
	@Override
	public List<AnchorToUserEvaluationDto> annchorToUserEvaluationList() throws Exception {
		PageModel pageModel = PageModel.getPageModel();
		pageModel.addQuery(Restrictions.eq("state", 1));
		pageModel.addQuery(Restrictions.eq("type", 3));
		List<AppLabelEntity> labelList = appLabelContract.load(pageModel);
		List<AnchorToUserEvaluationDto> dtoList = new ArrayList<>();
		if(Tools.isNotNull(labelList)){
			labelList.forEach(v->{
				dtoList.add(AnchorToUserEvaluationDto.preDto(v));
			});
		}
		return dtoList;
	}


	@Override
	public ActionResult anchorEvaluation(long userId,Long anchorId,long serialNum,List<Long> evaluationIdList,String evaluationText) throws Exception {
		VchatRoomEntity room = getVchatRoom(serialNum);
		Map<Long,AppLabelEntity> labelList = appLabelContract.findById(evaluationIdList);
		AnchorEvaluationLogEntity logEntity = new AnchorEvaluationLogEntity();
		logEntity.setUserid(userId);
		logEntity.setAnchor_userid(anchorId);
		List<HashMap<String, String>> labelsList = new ArrayList<>();
		if(Tools.isNotNull(labelList)){
			for(Long re:evaluationIdList){
				AppLabelEntity label = labelList.get(re);
				if(Tools.isNotNull(label)){
					HashMap<String, String> hsmp = new HashMap<>();
					hsmp.put("desc", label.getName());
					hsmp.put("color", label.getColor());
					labelsList.add(hsmp);
					anchorEvaluationStatisticsContract.addEvaluationNum(anchorId, label.getId());
				}
			
			}
		}
		int avType = 2;
		if(Tools.isNotNull(room)){
			logEntity.setOrderid(room.getOrderId());
			avType = room.getAv_type();
		}else{
			logEntity.setOrderid(0L);
		}
		logEntity.setAv_type(avType);
		logEntity.setEvaluation_labels(JsonHelper.toJson(labelsList));
		if(Tools.isNotNull(evaluationText)){
			if (checkTextStatus(evaluationText) == 1) {
				logEntity.setEvaluation_text(evaluationText);
			}
			
		}
		
		logEntity.setCreate_time(new Date());
		anchorEvaluationLogContract.insert(logEntity);
		anchorOnlineContract.addEvaluationNum(anchorId,avType);
		
		return ActionResult.success();
	}
	
	
	public int checkTextStatus(String chatText) throws Exception {
		// 后台文字聊天控制
		SysConfigEntity config = sysConfigContract.findByProperty("name", AgentRedisCacheConst.EVALUATION_CHAT_CONTROL_PREFIX);
		HashMap<Integer, Integer> ctrMap = new HashMap<>();
		if(Tools.isNotNull(config)) {
			JSONArray ctrlList = JsonHelper.toJsonArray(config.getValue());
			if(Tools.isNotNull(ctrlList)) {
				for(int i=0 ; i<ctrlList.size();i++){
					Object object = ctrlList.get(i);
					ctrMap.put(Integer.valueOf(object.toString()), Integer.valueOf(object.toString()));
				}
			}
		}
		long orderId = IdGenerater.generateId();
		JSONObject jObject =JsonHelper.toJsonObject(neteaseTextCheck.check(orderId, chatText)) ;
		int code = jObject.getIntValue("code");
	    String msg = jObject.getString("msg");
	    int checkStatus = 0;
	    String taskId = "";
	    int typeGuangGo = 0;
	    List<Integer> checkTypeList = new ArrayList<>();
        if (code == 200) {
        	JSONObject resultObject = jObject.getJSONObject("result");
            int action = resultObject.getIntValue("action");
            taskId = resultObject.getString("taskId");
            JSONArray labelArray = resultObject.getJSONArray("labels");
            for (int i=0;i<labelArray.size();i++) {
            	JSONObject lObject = labelArray.getJSONObject(i);
                int label = lObject.getIntValue("label");
                int level = lObject.getIntValue("level");
                if(label >0){
                	 checkTypeList.add(label);
                }
            }
            if (action == 0) {
            	checkStatus = 1;
            	logger.info("neteaseTextCheck:"+String.format("taskId=%s，文本机器检测结果：通过", taskId));
            } else if (action == 1) {
            	checkStatus = 0;
            	logger.info("neteaseTextCheck:"+String.format("taskId=%s，文本机器检测结果：嫌疑，需人工复审，分类信息如下：%s", taskId, labelArray.toString()));
            } else if (action == 2) {
            	checkStatus = 0;
            	logger.info("neteaseTextCheck:"+String.format("taskId=%s，文本机器检测结果：不通过，分类信息如下：%s", taskId, labelArray.toString()));
            }
            if(checkStatus == 0){
            	boolean flag = false;
            	for(Integer re:checkTypeList){
            		if(Tools.isNotNull(ctrMap.get(re))){
            			flag = true;
            		}
            	}
            	if(flag){
            		checkStatus = 0;
            	}else{
            		checkStatus = 1;
            	}
            }
         	
            
        } else {
        	checkStatus = 0;
        	logger.info("neteaseTextCheck:"+String.format("ERROR: code=%s, msg=%s", code, msg));
        }
        return checkStatus;
	}
	
	
	@Override
	public ActionResult annchorToUserEvaluation(long userId, Long otherId, long serialNum, List<Long> typeList)
			throws Exception {
		VchatRoomEntity room = getVchatRoom(serialNum);
		Map<Long, AppLabelEntity> labelList = appLabelContract.findById(typeList);
		AnchorToUserEvaluationLogEntity logEntity = new AnchorToUserEvaluationLogEntity();
		logEntity.setAnchorid(userId);
		logEntity.setUserid(otherId);
		List<String> labelsList = new ArrayList<>();
		if (Tools.isNotNull(labelList)) {
			for (Long re : typeList) {
				AppLabelEntity label = labelList.get(re);
				if (Tools.isNotNull(label)) {
					labelsList.add(label.getName());
				}

			}
		}
		int avType = 2;
		if (Tools.isNotNull(room)) {
			logEntity.setOrderid(room.getOrderId());
			avType = room.getAv_type();
		} else {
			logEntity.setOrderid(0L);
		}
		logEntity.setAv_type(avType);
		logEntity.setEvaluation_labels(JsonHelper.toJson(labelsList));
		logEntity.setCreate_time(new Date());
		anchorToUserEvaluationLogContract.insert(logEntity);
		return ActionResult.success();
	}



	/**
	 * 最近已接听的通话记录
	 * @param userId
	 * @param otherId
	 * @param avType
	 * @return
	 * @throws Exception
	 */
	
	public String getLastTalk(long userId,long otherId,int avType ) throws Exception{
		/*
		PageModel pageModel = PageModel.getLimitModel(0, 1);
		pageModel.addQuery(Restrictions.or(Restrictions.and(Restrictions.eq("userid", userId),Restrictions.eq("anchorid", otherId)),
			        Restrictions.and(Restrictions.eq("userid", otherId), Restrictions.eq("anchorid", userId))));
		pageModel.addQuery(Restrictions.gt("vchat_duration", 0));
		pageModel.addQuery(Restrictions.eq("av_type", avType));
		pageModel.desc("id");
		List<VchatRoomEntity> list =  vchatRoomContract.load(pageModel);
		String content = "";
		if(Tools.isNotNull(list)){
			VchatRoomEntity room = list.get(0);
			long talkMinute = room.getVchat_duration();
			content = "上次通话:"+Tools.getFormatDate(room.getPay_create(), "yyyy-MM-dd HH:mm")+" 总共"+talkMinute+"分钟";
		}
		 return content;
		 */
		return "";
	}

	/**
	 * 最近的拨打记录（包含不接听的）
	 * @param userId
	 * @param otherId
	 * @param avType
	 * @return
	 * @throws Exception
	 */
	public VchatRoomEntity getLastVchatRoom(long userId, long otherId, int avType) throws Exception {
		PageModel pageModel = PageModel.getLimitModel(0, 1);
		pageModel.addQuery(Restrictions.or(
				Restrictions.and(Restrictions.eq("userid", userId), Restrictions.eq("anchorid", otherId)),
				Restrictions.and(Restrictions.eq("userid", otherId), Restrictions.eq("anchorid", userId))));
		if (avType > 0) {
			pageModel.addQuery(Restrictions.eq("av_type", avType));
		}
		pageModel.desc("id");
		List<VchatRoomEntity> list = vchatRoomContract.load(pageModel);
		if (Tools.isNotNull(list)) {
			return list.get(0);
		} else {
			return null;
		}
	}



	@Override
	public ActionResult getMyPhone(long userId, String stamp) throws Exception {
		long lastId = 0L;
		if (Tools.isNotNull(stamp)) {
			lastId = Long.parseLong(stamp);
		}
		PageModel pageModel = PageModel.getLimitModel(0, PAGESIZE + 1);
		pageModel.addQuery(Restrictions.or(Restrictions.and(Restrictions.eq("userid", userId)),
				Restrictions.and(Restrictions.eq("anchorid", userId))));
		if (lastId > 0) {
			pageModel.addQuery(Restrictions.lt("id", lastId));
		}
		pageModel.desc("id");
		List<VchatRoomEntity> list = vchatRoomContract.load(pageModel);
		int i = 0;
		List<MyPhoneDto> myPhoneList = new ArrayList<MyPhoneDto>();
		boolean flag = false;
		if (Tools.isNotNull(list)) {
			List<Long> orderList = list.stream().map(VchatRoomEntity::getOrderId).collect(Collectors.toList());
			Map<Long, UserVchatExperienceIncomeLogEntity> IncomeLogMap = null;
			if (Tools.isNotNull(orderList)) {
				IncomeLogMap = userVchatExperienceIncomeLogContract.findById(orderList);
			}
			if (Tools.isNull(IncomeLogMap)) {
				IncomeLogMap = new HashMap<>();
			}

			for (VchatRoomEntity re : list) {
				long otherId = 0;
				if (userId == re.getUserid()) {
					otherId = re.getAnchorid();
				} else {
					otherId = re.getUserid();
				}
				UserBO otherUser = userAgent.findById(otherId);
				if (Tools.isNotNull(otherUser)) {
					myPhoneList.add(MyPhoneDto.preDto(re, otherUser, IncomeLogMap.get(re.getOrderId())));
				}

				lastId = re.getId();
				if (++i >= PAGESIZE) {
					flag = true;
					break;
				}
			}
		}
		return ActionResult.success(myPhoneList, lastId + "", flag);
	}
	
	
	public List<RecommendOnlineAnchorDto> getRecommndOnlineAnchorList(long userId,int avType) throws Exception {
		List<RecommendOnlineAnchorDto> recommendOnlineList = new ArrayList<>();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, -3);
		PageModel pageModel =PageModel.getPageModel();
		pageModel.addProjection(Projections.distinct(Projections.property("anchorid")));
		pageModel.addQuery(Restrictions.eq("userid", userId));
		pageModel.addQuery(Restrictions.eq("av_type", avType));
		pageModel.addQuery(Restrictions.gt("create_time", Tools.getFormatDate(cal.getTime(), "yyyy-MM-dd HH:mm:ss")));
		List<Map<String, Object>> maps = vchatRoomContract.loadGroupBy(pageModel);
		List<Long> idList = new ArrayList<>();
		 if (Tools.isNotNull(maps)) {
			maps.stream().forEach(map -> {
				if (Tools.isNotNull(map)) {
					Long anchorid = Tools.parseLong(map.get("anchorid"));
					idList.add(anchorid);
				}
			});
		}
		pageModel.clearAll();
		pageModel = PageModel.getLimitModel(0,10);
		pageModel.addQuery(Restrictions.eq("online", AnchorOnlineStateEnum.online.getCode()));
		if(Tools.isNotNull(idList)){
			pageModel.addQuery(Restrictions.notin("userid", idList));
		}
		pageModel.addQuery(Restrictions.eq("state", 1));
		//pageModel.addQuery(Restrictions.eq("audio_type", 1));
		pageModel.addQuery(Restrictions.eq("flag", 0));
		pageModel.desc("avg_yesterday_recv");
		List<AnchorOnlineEntity> anchorList =  anchorOnlineContract.load(pageModel);
		if(Tools.isNotNull(anchorList)){
			if(anchorList.size()<3){
				return recommendOnlineList;
			}
			
				for(AnchorOnlineEntity v:anchorList){
					UserBaseInfo user = new UserBaseInfo();
					user.setUserId(v.getUserid());
					user.setPhoto(Const.getCdn(v.getPhoto()));
					user.setNickName(v.getNickname());
					RecommendOnlineAnchorDto dto = new RecommendOnlineAnchorDto();
					dto.setUserInfo(user);
					dto.setAttention(userFriendAgent.isAttention(userId,v.getUserid())?0:1);
					recommendOnlineList.add(dto);
				}
		}
		return recommendOnlineList;
	}
	
	
	/**
	 * 用户ID关联的定单号
	 * @param otherId
	 * @return
	 */
	private boolean getUserTalking(Long otherId){
		boolean flag = cacheRedis.exists(AgentRedisCacheConst.VCHAT_USER_ORDER_PREFIX + otherId);
		logger.info("pay_INFO:otherId={},获取在线状态:"+flag,otherId);
		return flag;
	}
	
	/**
	 * 根据用户ID，获得通知定单号
	 * @param userId
	 * @return
	 */
	private Long getUserOrderId(Long userId){
		Long orderId = null;
		String orderIdStr = cacheRedis.get(AgentRedisCacheConst.VCHAT_USER_ORDER_PREFIX + userId);
		if(Tools.isNotNull(orderIdStr)){
			orderId = Long.valueOf(orderIdStr);
		}
		return orderId;
	}
	
	/**
	 * 获的通话中对方ID
	 * @param userid
	 * @param otherId
	 * @return
	 */
	private String getOtherUserId(Long userid,Long otherId){
		String prefix = getPrefix(userid,otherId);
		return cacheRedis.hget(AgentRedisCacheConst.VCHAT_USER_ROOM_PREFIX + prefix + userid, OTHERUSERID);
	}
	
	/**
	 * 两人通话缓存前缀，根据两个用户ID数据进行排序
	 * @param userid
	 * @param otherId
	 * @return
	 */
	public static String getPrefix(Long userid,Long otherId){
		String prefix = "";
		if(Tools.longValue(userid)<Tools.longValue(otherId)){
			prefix=MD5.encode(""+userid+otherId).substring(20);
		}else{
			prefix=MD5.encode(""+otherId+userid).substring(20);
		}
		return prefix;
	}

	/**
	 * 通话结束扣费检查
	 * @param vchatRoom
	 * @throws Exception
	 */
	public  void checkoutPayOder(VchatRoomEntity vchatRoom) throws Exception{
		while(vchatRoomContract.payYXRecvExit(vchatRoom.getId())>0){
			int price = Tools.intValue(Math.round(vchatRoom.getPrice()*vchatRoom.getPay_discount()));
			DiamondResultDto<Map<String, Long>> result = userDiamondAgent.mediaChatInMinute(vchatRoom.getAv_type(), vchatRoom.getUserid(), vchatRoom.getAnchorid(),
					vchatRoom.getOrderId(), price, UserVideoChatRecordChannelTypeEnum.video_tencent);
			if(AgentErrorCodeEnum.success.getCode() == result.getCode()){
			vchatRoomContract.updateTotalPrice(vchatRoom.getId(),price);
			anchorIntimateRankingsContract.addIncome(vchatRoom.getAnchorid(), vchatRoom.getUserid(), price);
			} else {
				break;
			}
		}
	}
     
	 

	/**
	 * 获的用户的主账号ID
	 */
	@Override
	public Long getRealUserId(long userId) throws Exception {
		Long realUserId = userId;
		AnchorOnlineEntity anchor = anchorOnlineContract.findByProperty("userid", userId);
		if(Tools.isNotNull(anchor)){
			if(anchor.getParent_userid()>0){
				realUserId = anchor.getParent_userid();
			}
		}
		return realUserId;
	}
	
	/**
	 * 设置拨打标记
	 */
	public void setDialingFalg(long userId) throws Exception {
		cacheRedis.transaction(tx -> {
			//用户与订单关联信息 （可以覆盖）
			tx.set(AgentRedisCacheConst.VCHAT_USER_DIALING_FALG_PREFIX + userId, userId + "");
			tx.expire(AgentRedisCacheConst.VCHAT_USER_DIALING_FALG_PREFIX + userId, 10);
		});
	}

	/**
	 * IM消息已接听标记
	 * @param orderid
	 * @return
	 * @throws Exception
	 */
	public boolean getPushRevcFalg(long orderid) throws Exception {
		long rows = cacheRedis.setnx(AgentRedisCacheConst.VCHAT_IM_SEND_FALG+"RECV_"+orderid, ""+orderid);
		cacheRedis.expire(AgentRedisCacheConst.VCHAT_IM_SEND_FALG+"RECV_"+orderid, 60*5);
		return rows>0?true:false;
	}

	/**
	 * IM消息未接听标记
	 * @param orderid
	 * @return
	 * @throws Exception
	 */
	public boolean getPushUnCallFalg(long orderid) throws Exception {
		long rows = cacheRedis.setnx(AgentRedisCacheConst.VCHAT_IM_SEND_FALG+"UNCALL_"+orderid, ""+orderid);
		cacheRedis.expire(AgentRedisCacheConst.VCHAT_IM_SEND_FALG+"UNCALL_"+orderid, 60*5);
		return rows>0?true:false;
	}
	
	/**
	 * 用户拨打标记是否存在
	 */
	@Override
	public boolean getDialingFalg(long userId) throws Exception {
		return !cacheRedis.exists(AgentRedisCacheConst.VCHAT_USER_DIALING_FALG_PREFIX + userId);
	}
	
	
	public boolean getRecommentAnchorFalg(long userId) throws Exception {
		String key =AgentRedisCacheConst.RECOMMEND_ONLINE_ANCHOR_FALG_PREFIX+Tools.getFormatDate(new Date(), "yyyyMMdd")+"_"+userId;
		 long index = cacheRedis.incrBy(key, 1);
		 cacheRedis.expire(AgentRedisCacheConst.RECOMMEND_ONLINE_ANCHOR_FALG_PREFIX+Tools.getFormatDate(new Date(), "yyyyMMdd")+"_"+userId, 60);
		 long recommendRexpire =  cacheRedis.ttl(key);
			if(recommendRexpire == -1){
				cacheRedis.expire(key, recommend_anchor_timeOut);
			}
		 if(index <4){
			 return true;
		 }
		 Random random = new Random();
		 if(random.nextBoolean() && random.nextBoolean()){
			 return true;
		 }
		 return false;
	}
	
	/**
	 * 设置用户支付显示信息
	 * @param orderId
	 * @param userId
	 * @param msg
	 * @throws Exception
	 */
	public void setUserPayShowNote(long orderId,long userId,String msg) throws Exception {
		cacheRedis.transaction(tx -> {
			tx.set(AgentRedisCacheConst.VCHAT_USER_PAY_SHOW_NOTE_PREFIX +orderId+"_"+ userId, msg);
			tx.expire(AgentRedisCacheConst.VCHAT_USER_DIALING_FALG_PREFIX + userId, 70);
		});
	}
	/**
	 * 获取用户支付显示信息
	 * @param orderId
	 * @param userId
	 * @throws Exception
	 */
	public String getUserPayShowNote(long orderId,long userId) throws Exception {
		String key = AgentRedisCacheConst.VCHAT_USER_PAY_SHOW_NOTE_PREFIX +orderId+"_"+ userId;
		String msg = cacheRedis.get(key);
		cacheRedis.del(key);
		if(Tools.isNotNull(msg)){
			return msg;
		}
		return null;
	}
	
	/**
	 * 钱不够通知
	 * @param orderId
	 * @param userId
	 * @param msg
	 * @throws Exception
	 */
	public void sendTcpMsgUserNotEnough(long userId) throws Exception {
		String anchorText = "对方钻不足，请服务好用户，提高付费，获得40%提成";
		ExecutorServiceHelper.executor.execute(new SendMessageNotifyThread(userId,anchorText,"#f1ac19"));
	}
	
	/**
	 * 设置用户支付弹窗信息
	 * @param orderId
	 * @param userId
	 * @param msg
	 * @throws Exception
	 */
	public void setUserPayShowDlg(long orderId,long userId,String dlg) throws Exception {
		cacheRedis.transaction(tx -> {
			tx.set(AgentRedisCacheConst.VCHAT_USER_PAY_SHOW_DLG_PREFIX +orderId+"_"+ userId, dlg);
			tx.expire(AgentRedisCacheConst.VCHAT_USER_PAY_SHOW_DLG_PREFIX + userId, 70);
		});
	}
	/**
	 * 获取用户支付弹窗信息
	 * @param orderId
	 * @param userId
	 * @throws Exception
	 */
	public String getUserPayShowDlg(long orderId,long userId) throws Exception {
		String key = AgentRedisCacheConst.VCHAT_USER_PAY_SHOW_DLG_PREFIX +orderId+"_"+ userId;
		String msg = cacheRedis.get(key);
		if(Tools.isNotNull(msg)){
			cacheRedis.del(key);
			return msg;
		}
		return null;
	}
	
	
	public boolean getAnchorDialThreeNoRecv(long anchorId,long userId) throws Exception{
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, -1);
		PageModel pageModel = PageModel.getPageModel();
		pageModel.addQuery(Restrictions.eq("anchorid", anchorId));
		pageModel.addQuery(Restrictions.eqProperty("sponsor_user", "anchorid"));
		pageModel.addQuery(Restrictions.eq("userid", userId));
		pageModel.addQuery(Restrictions.eq("vchat_duration", 0));

		pageModel.addQuery(Restrictions.gt("create_time", Tools.getFormatDate(cal.getTime(), "yyyy-MM-dd HH:mm:ss")));
		long rows = vchatRoomContract.count(pageModel);
		if(rows>=5){
			return true;
		}
		return false;
	}
	
	/**
	 * 设置蒙层标记
	 * @param orderId
	 * @param userId
	 * @param msg
	 * @throws Exception
	 */
	public void setCloseObscurationFalg(long userId,long roomid) throws Exception {
		cacheRedis.transaction(tx -> {
			tx.set(AgentRedisCacheConst.VCHAT_CLOSE_OBSCURATION_PREFIX + userId, roomid+"");
			tx.expire(AgentRedisCacheConst.VCHAT_CLOSE_OBSCURATION_PREFIX + userId, 70);
		});
	}
	
	public String getCloseObscurationFalg(long userId) throws Exception {
		String key = AgentRedisCacheConst.VCHAT_CLOSE_OBSCURATION_PREFIX + userId;
		String msg = cacheRedis.get(key);
		if(Tools.isNotNull(msg)){
			cacheRedis.del(key);
			return msg;
		}
		return null;
	}
	
	/**
	 * 设置主动拨打用户记录，下次本用户拨打，请除之前的记录
	 * @param userId
	 * @param otherId
	 */
	public void setInitiativeDial(long userId,long otherId){
		cacheRedis.transaction(tx -> {
			tx.set(AgentRedisCacheConst.ROOM_CLOSE_INITIATIVE_DIAL_PREFIX + userId, otherId+"");
			tx.expire(AgentRedisCacheConst.ROOM_CLOSE_INITIATIVE_DIAL_PREFIX + userId, 35);
		});
	}
	
	/**
	 * 清除本用户主动拨打之前的记录
	 * @param userId
	 * @throws Exception
	 */
	public void clearInitiativeDial(long userId) throws Exception{
		String otherId = cacheRedis.get(AgentRedisCacheConst.ROOM_CLOSE_INITIATIVE_DIAL_PREFIX + userId);
		if(Tools.isNotNull(otherId)){
			cacheRedis.transaction(tx -> {
				tx.del(AgentRedisCacheConst.VCHAT_USER_ORDER_PREFIX + userId);
				tx.del(AgentRedisCacheConst.VCHAT_USER_ORDER_PREFIX + otherId);
				tx.del(AgentRedisCacheConst.ROOM_CLOSE_INITIATIVE_DIAL_PREFIX + userId);
			});
		}
	}
	
	/**
	 * 通话结束标记
	 * @param orderid
	 * @throws Exception
	 */
	public void setVchatRoomCloseFlag(long orderid) throws Exception {
		cacheRedis.transaction(tx -> {
			tx.set(AgentRedisCacheConst.VCHAT_ROOM_CLOSE_FALG_PREFIX + orderid, orderid + "");
			tx.expire(AgentRedisCacheConst.VCHAT_ROOM_CLOSE_FALG_PREFIX + orderid, 20);
		});
	}
	
	/**
	 * 获的通话结束状态
	 * @param orderid
	 * @return
	 * @throws Exception
	 */
	public boolean getVchatRoomCloseFlag(long orderid) throws Exception{
		return cacheRedis.exists(AgentRedisCacheConst.VCHAT_ROOM_CLOSE_FALG_PREFIX + orderid);
	}
	
	
	/**
	 * 设置核查拨打检查标记
	 * @param userid
	 * @param otherid
	 * @param dialType
	 * @throws Exception
	 */
	public void setVchatCheckFalg(long userid,long otherid,String dialType) throws Exception {
		cacheRedis.transaction(tx -> {
			tx.set(AgentRedisCacheConst.VCHAT_ROOM_CHECK_PASS_FALG_PREFIX + userid+"_"+otherid, dialType);
			tx.expire(AgentRedisCacheConst.VCHAT_ROOM_CHECK_PASS_FALG_PREFIX + userid+"_"+otherid, 45);
		});
	}
	
	/**
	 * 获取核查拨打检查标记
	 * @param userid
	 * @param otherid
	 * @return
	 * @throws Exception
	 */
	public String getVchatCheckFalg(long userid,long otherid) throws Exception{
		return cacheRedis.get(AgentRedisCacheConst.VCHAT_ROOM_CHECK_PASS_FALG_PREFIX + userid+"_"+otherid);
	}
	
	/**
	 * 主播当天拨打次数设置
	 * @param anchorId
	 * @param avType
	 * @return
	 * @throws Exception
	 */
	public long setAddAnchorDialingUserTimes(long anchorId,int avType) throws Exception {
		String key = AgentRedisCacheConst.VCHAT_ANCHOR_DIAL_TODAY_NUM+Tools.getFormatDate(new Date(), "yyyyMMdd")+"_"+avType+"_"+anchorId;
		 long recommendRexpire =  cacheRedis.ttl(key);
			if(recommendRexpire == -1){
				cacheRedis.expire(key, recommend_anchor_timeOut);
			}
		return cacheRedis.incrBy(key, 1);
	}
	
	/**
	 * 获得主播当天拨打次数
	 * @param anchorId
	 * @param avType
	 * @return
	 * @throws Exception
	 */
	public long getAnchorDialingUserTimes(long anchorId,int avType) throws Exception {
		String key = AgentRedisCacheConst.VCHAT_ANCHOR_DIAL_TODAY_NUM+Tools.getFormatDate(new Date(), "yyyyMMdd")+"_"+avType+"_"+anchorId;
		String value = cacheRedis.get(key);
		if(Tools.isNotNull(value)){
			return Tools.parseLong(value);
		}
		return 0L;
	}

	/**
	 * 根据订单号，获得房间实体
	 * @param serialNum
	 * @return
	 * @throws Exception
	 */
	public VchatRoomEntity getVchatRoom(long serialNum) throws Exception{
		if(serialNum == 0){
			return null;
		}
		return vchatRoomContract.findByProperty("orderId", serialNum);
	}
	
	public String getVchatRoomSettlementIncome(String orderid){
		return cacheRedis.get(AgentRedisCacheConst.VCHAT_ROOM_SETTLEMENT_INCOME_PREFIX + orderid);
	}
	
	private boolean checkDialTimeLimit(){
		LocalTime localTime = LocalTime.now();
		return localTime.getHour()>=8 && localTime.getHour()<20;
	}
	
	/**
	 * 关闭蒙层
	 * @param userId
	 */
	public void closeObscurationClient(long userId){
		try{
			
			FreeVideoChatExperienceEntity freeVideoChat = freeVideoChatExperienceContract.findByProperty("userid", userId);
			if(Tools.isNotNull(freeVideoChat)){
				freeVideoChatExperienceContract.deleteById(freeVideoChat.getId());
			}
			
			String roomid = getCloseObscurationFalg(userId);
			if(Tools.isNotNull(roomid)){
				VchatRoomEntity room = vchatRoomContract.findById(Long.valueOf(roomid));
				if(Tools.isNotNull(room)){
					if(room.getSys_duration() == 0){
						ExecutorServiceHelper.executor.execute(new SendMessageThread(userId));
						ExecutorServiceHelper.executor.execute(new SendMessageThread(getRealUserId(room.getAnchorid())));
					}
				}
				
			}
		}catch(Exception e){
			logger.info("发送关闭蒙层失败,userid:"+userId,e);
		}
	}
	
	private  class SendMessageThread implements Runnable{
		private long userId;
		
		public SendMessageThread(long userId){
			this.userId = userId;
		}
		@Override
		public void run() {
			try{
				VChatVideoTCPDto dto = new VChatVideoTCPDto();
				dto.setType(VChatVideoTCPTypeEnum.video.getCode());
				dto.setSubType(VChatVideoStatusEnum.close_obscuration.getCode());
				neteaseAgent.pushOneAttachMessage(userId,userId, JsonHelper.toJson(dto)); 
			}catch(Exception e){
					logger.info("发送关闭蒙层失败,userid:"+userId,e);
			}
		}
	}
	
	
	private  class SendMessageNotifyThread implements Runnable{
		private long userId;
		private String textColor;
		private String info;
		
		public SendMessageNotifyThread(long userId,String info){
			this.userId = userId;
			this.textColor = "#ffffff";
			this.info = info;
			
		}
		
		public SendMessageNotifyThread(long userId,String info,String textColor){
			this.userId = userId;
			this.info = info;
			this.textColor = textColor;
		}
		@Override
		public void run() {
			try{
				VChatTCPInfoNotifyDto notify = new VChatTCPInfoNotifyDto();
				notify.setText(info);
				notify.setTextColor(textColor);
				notify.setTextShadowColor("#333333");
				VChatVideoTCPDto dto = new VChatVideoTCPDto();
				dto.setType(VChatVideoTCPTypeEnum.video.getCode());
				dto.setSubType(VChatVideoStatusEnum.info_notify.getCode());
				dto.setData(JsonHelper.toJson(notify));
				
				neteaseAgent.pushOneAttachMessage(userId,userId, JsonHelper.toJson(dto)); 
				}catch(Exception e){
					logger.info("发送通告失败,userid:"+userId,e);
				}
		}
	}
	
	

	
}
