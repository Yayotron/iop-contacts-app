package iop.org.iop_sdk_android.core.modules.chat;

import android.content.Context;

import org.libertaria.world.core.IoPConnect;
import org.libertaria.world.global.AbstractModule;
import org.libertaria.world.global.IntentMessage;
import org.libertaria.world.global.SystemContext;
import org.libertaria.world.global.Version;
import org.libertaria.world.global.exceptions.ProfileNotSupportAppServiceException;
import org.libertaria.world.profile_server.CantConnectException;
import org.libertaria.world.profile_server.CantSendMessageException;
import org.libertaria.world.profile_server.ProfileInformation;
import org.libertaria.world.profile_server.client.AppServiceCallNotAvailableException;
import org.libertaria.world.profile_server.engine.app_services.BaseMsg;
import org.libertaria.world.profile_server.engine.app_services.CallProfileAppService;
import org.libertaria.world.profile_server.engine.listeners.ProfSerMsgListener;
import org.libertaria.world.profile_server.model.Profile;
import org.libertaria.world.services.EnabledServices;
import org.libertaria.world.services.chat.ChatCallAlreadyOpenException;
import org.libertaria.world.services.chat.ChatCallClosedException;
import org.libertaria.world.services.chat.ChatModule;
import org.libertaria.world.services.chat.ChatMsgListener;
import org.libertaria.world.services.chat.RequestChatException;
import org.libertaria.world.services.chat.msg.ChatAcceptMsg;
import org.libertaria.world.services.chat.msg.ChatMsg;
import org.libertaria.world.services.chat.msg.ChatRefuseMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iop.org.iop_sdk_android.core.utils.EmptyListener;
import iop.org.iop_sdk_android.core.wrappers.IntentWrapperAndroid;
import world.libertaria.shared.library.services.chat.ChatIntentsConstants;

import static com.google.common.base.Preconditions.checkNotNull;
import static world.libertaria.shared.library.services.chat.ChatIntentsConstants.EXTRA_INTENT_CHAT_MSG;
import static world.libertaria.shared.library.services.chat.ChatIntentsConstants.EXTRA_INTENT_DETAIL;
import static world.libertaria.shared.library.services.chat.ChatIntentsConstants.EXTRA_INTENT_IS_LOCAL_CREATOR;
import static world.libertaria.shared.library.services.chat.ChatIntentsConstants.EXTRA_INTENT_LOCAL_PROFILE;
import static world.libertaria.shared.library.services.chat.ChatIntentsConstants.EXTRA_INTENT_REMOTE_PROFILE;

/**
 * Created by furszy on 7/20/17.
 */

public class ChatModuleImp extends AbstractModule implements ChatModule,ChatMsgListener {

    private Logger logger = LoggerFactory.getLogger(ChatModuleImp.class);

    private Context context;

    public ChatModuleImp(SystemContext context, IoPConnect ioPConnect) {
        super(context,ioPConnect,Version.newProtocolAcceptedVersion(), EnabledServices.CHAT);
    }

    @Override
    public void requestChat(final String localProfilePubKey, final ProfileInformation remoteProfileInformation, final ProfSerMsgListener<Boolean> readyListener) throws RequestChatException, ChatCallAlreadyOpenException {
        // first check if the chat is active or was requested
        try {
            CallProfileAppService call = getCall(localProfilePubKey, remoteProfileInformation.getHexPublicKey());
            if (call != null)
                throw new ChatCallAlreadyOpenException("chat call already open with " + remoteProfileInformation.getHexPublicKey());
            prepareCall(localProfilePubKey, remoteProfileInformation, new EmptyListener(readyListener));
        } catch (ProfileNotSupportAppServiceException e) {
            e.printStackTrace();
            readyListener.onMsgFail(0,0,e.toString());
        }
    }

    @Override
    public void acceptChatRequest(String localProfilePubKey, String remoteHexPublicKey, ProfSerMsgListener<Boolean> future) throws Exception {
        CallProfileAppService callProfileAppService = getCall(localProfilePubKey,remoteHexPublicKey);
        if (callProfileAppService!=null) {
            callProfileAppService.sendMsg(new ChatAcceptMsg(System.currentTimeMillis()), future);
        }else {
            throw new AppServiceCallNotAvailableException("Connection not longer available");
        }
    }

    @Override
    public void refuseChatRequest(String localProfilePubKey, String remoteHexPublicKey) {
        try {
            CallProfileAppService callProfileAppService = getCall(localProfilePubKey, remoteHexPublicKey);
            if (callProfileAppService == null) return;
            try {
                callProfileAppService.sendMsg(new ChatRefuseMsg(), null);
            } catch (Exception e) {
                e.printStackTrace();
                // do nothing..
            }
            callProfileAppService.dispose();
        } catch (ProfileNotSupportAppServiceException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param remoteProfileInformation
     * @param msg
     * @param msgListener
     * @throws Exception
     */
    @Override
    public void sendMsgToChat(String localProfilePubKey, ProfileInformation remoteProfileInformation, String msg, ProfSerMsgListener<Boolean> msgListener) throws Exception {
        CallProfileAppService callProfileAppService = null;
        try {
            callProfileAppService = getCall(localProfilePubKey,remoteProfileInformation.getHexPublicKey());
            if (callProfileAppService==null) throw new ChatCallClosedException("Chat connection is not longer available",remoteProfileInformation);
            callProfileAppService.sendMsg(new ChatMsg(msg), msgListener);
        }catch (AppServiceCallNotAvailableException e){
            e.printStackTrace();
            throw new ChatCallClosedException("Chat call not longer available",remoteProfileInformation);
        }
    }

    @Override
    public boolean isChatActive(String localProfilePubKey, String remotePk) {
        try {
            CallProfileAppService callProfileAppService = getCall(localProfilePubKey,remotePk);
            if (callProfileAppService!=null && callProfileAppService.isStablished()) {
                // check sending a ping
                callProfileAppService.ping();
                return true;
            }
        } catch (CantSendMessageException e) {
            e.printStackTrace();
        } catch (CantConnectException e) {
            e.printStackTrace();
        } catch (ProfileNotSupportAppServiceException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onChatConnected(Profile localProfile, String remoteProfilePubKey, boolean isLocalCreator) {
        IntentMessage intent = new IntentWrapperAndroid(ChatIntentsConstants.ACTION_ON_CHAT_CONNECTED);
        intent.put(EXTRA_INTENT_LOCAL_PROFILE, localProfile.getHexPublicKey());
        intent.put(EXTRA_INTENT_REMOTE_PROFILE, remoteProfilePubKey);
        intent.put(EXTRA_INTENT_IS_LOCAL_CREATOR, isLocalCreator);
        broadcastEvent(intent);
    }

    @Override
    public void onChatDisconnected(String remotePubKey,String reason) {
        logger.info("onChatDisconnected");
        IntentMessage intent = new IntentWrapperAndroid(ChatIntentsConstants.ACTION_ON_CHAT_DISCONNECTED);
        intent.put(EXTRA_INTENT_REMOTE_PROFILE, remotePubKey);
        intent.put(EXTRA_INTENT_DETAIL, reason);
        broadcastEvent(intent);
    }

    @Override
    public void onMsgReceived(String remotePubKey, BaseMsg msg) {
        IntentMessage intent = new IntentWrapperAndroid(ChatIntentsConstants.ACTION_ON_CHAT_MSG_RECEIVED);
        intent.put(EXTRA_INTENT_REMOTE_PROFILE, remotePubKey);
        intent.put(EXTRA_INTENT_CHAT_MSG, msg);
        broadcastEvent(intent);
    }

    @Override
    public boolean onPreCall(String localPublicKey, ProfileInformation remoteProfile) {
        return ioPConnect.getKnownProfile(localPublicKey,remoteProfile.getHexPublicKey())!=null;
    }
}
