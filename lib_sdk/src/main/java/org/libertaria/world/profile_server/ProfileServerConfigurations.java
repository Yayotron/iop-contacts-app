package org.libertaria.world.profile_server;


import org.libertaria.world.global.Version;
import org.libertaria.world.profile_server.engine.ProfSerDb;
import org.libertaria.world.profile_server.model.KeyEd25519;
import org.libertaria.world.profile_server.model.ProfServerData;
import org.libertaria.world.profile_server.model.Profile;
import org.libertaria.world.profile_server.protocol.IopProfileServer;

import java.io.File;
import java.util.List;

/**
 * Created by mati on 25/12/16.
 */

public interface ProfileServerConfigurations extends ProfSerDb{

    /**
     *  Main profile server.
     * @return
     */
    ProfServerData getMainProfileServer();

    /**
     * Setters
     */
    void setMainPsPrimaryPort(int primaryPort);
    void setMainPfClPort(int clPort);
    void setMainPsNonClPort(int nonClPort);

    /**
     * Plan contract deal at the first time.
     * @return
     */
    IopProfileServer.HostingPlanContract getMainProfileServerContract();

    /**
     *  Known profile servers
     * @return
     */
    List<ProfServerData> getKnownProfileServers();


    String getUsername();

    byte[] getUserPubKey();

    Version getProfileVersion() ;

    boolean isRegisteredInServer();

    boolean isIdentityCreated();

    void setHost(String host);

    void saveUserKeys(Object obj);

    Object getUserKeys();

    void setUsername(String username);


    void setIsRegistered(boolean isRegistered);

    void setIsCreated(boolean isCreated);


    File getUserImageFile();

    KeyEd25519 createUserKeys();

    KeyEd25519 createNewUserKeys();

    String getProfileType();

    void saveProfile(Profile profile);

    void setProfileType(String type);

    boolean isPairingEnable();

    Profile getProfile();

    byte[] getUserImage();

    void saveMainProfileServer(ProfServerData profServerData);


    // service stuff

    long getScheduleServiceTime();

    void saveScheduleServiceTime(long scheduleTime);

    String getBackupProfilePath();

    void saveBackupPatch(String fileName);

    void saveBackupPassword(String password);

    String getBackupPassword();

    boolean isScheduleBackupEnabled();

    void setScheduleBackupEnable(boolean enable);

    boolean getBackgroundServiceEnable();

    void setBackgroundServiceEnable(boolean enable);
}
