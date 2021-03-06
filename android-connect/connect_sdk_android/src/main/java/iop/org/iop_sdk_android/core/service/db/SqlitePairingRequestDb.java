package iop.org.iop_sdk_android.core.service.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.libertaria.world.core.services.pairing.PairingMessageType;
import org.libertaria.world.profile_server.imp.ProfileInformationImp;
import org.libertaria.world.profiles_manager.PairingRequest;
import org.libertaria.world.profiles_manager.PairingRequestsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by furszy on 6/6/17.
 */

public class SqlitePairingRequestDb extends AbstractSqliteDb<PairingRequest> implements PairingRequestsManager {

    private static final Logger log = LoggerFactory.getLogger(SqlitePairingRequestDb.class);

    public static final int DATABASE_VERSION = 4;

    public static final String DATABASE_NAME = "requests";
    public static final String PAIRING_TABLE_NAME = "pairing_request";
    public static final String PAIRING_COLUMN_ID = "id";
    public static final String PAIRING_COLUMN_SENDER_KEY = "senderPubKey";
    public static final String PAIRING_COLUMN_REMOTE_KEY = "remotePubKey";
    public static final String PAIRING_COLUMN_REMOTE_SERVER_ID = "remoteServerId";
    public static final String PAIRING_COLUMN_REMOTE_SERVER_HOST_ID = "remoteHost";
    public static final String PAIRING_COLUMN_SENDER_NAME = "senderName";
    public static final String PAIRING_COLUMN_TIMESTAMP = "timestamp";
    public static final String PAIRING_COLUMN_STATUS = "status";
    public static final String PAIRING_COLUMN_SENDER_PS_HOST = "sender_ps_host";
    public static final String PAIRING_COLUMN_PAIR_STATUS = "request_pair_status";
    public static final String PAIRING_COLUMN_REMOTE_NAME = "remoteName";
    public static final String PAIRING_COLUMN_REMOTE_ID = "remoteId";


    public static final int PAIRING_COLUMN_POS_ID = 0;
    public static final int PAIRING_COLUMN_POS_SENDER_KEY = 1;
    public static final int PAIRING_COLUMN_POS_REMOTE_KEY = 2;
    public static final int PAIRING_COLUMN_POS_REMOTE_SERVER_ID = 3;
    public static final int PAIRING_COLUMN_POS_REMOTE_SERVER_HOST_ID = 4;
    public static final int PAIRING_COLUMN_POS_SENDER_NAME = 5;
    public static final int PAIRING_COLUMN_POS_TIMESTAMP = 6;
    public static final int PAIRING_COLUMN_POS_STATUS = 7;
    public static final int PAIRING_COLUMN_POS_SENDER_PS_HOST = 8;
    public static final int PAIRING_COLUMN_POS_PAIR_STATUS = 9;
    public static final int PAIRING_COLUMN_POS_REMOTE_NAME = 10;
    public static final int PAIRING_COLUMN_POS_REMOTE_ID = 11;

    public SqlitePairingRequestDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + PAIRING_TABLE_NAME +
                        "(" +
                        PAIRING_COLUMN_ID + " INTEGER primary key autoincrement, " +
                        PAIRING_COLUMN_SENDER_KEY + " TEXT, " +
                        PAIRING_COLUMN_REMOTE_KEY + " TEXT, " +
                        PAIRING_COLUMN_REMOTE_SERVER_ID + " TEXT, " +
                        PAIRING_COLUMN_REMOTE_SERVER_HOST_ID + " TEXT, " +
                        PAIRING_COLUMN_SENDER_NAME + " TEXT, " +
                        PAIRING_COLUMN_TIMESTAMP + " LONG , " +
                        PAIRING_COLUMN_STATUS + " TEXT ," +
                        PAIRING_COLUMN_SENDER_PS_HOST + " TEXT ," +
                        PAIRING_COLUMN_PAIR_STATUS + " TEXT ," +
                        PAIRING_COLUMN_REMOTE_NAME + " TEXT ," +
                        PAIRING_COLUMN_REMOTE_ID + " INTEGER "
                        + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PAIRING_TABLE_NAME);
        onCreate(db);
    }

    @Override
    protected String getTableName() {
        return PAIRING_TABLE_NAME;
    }

    @Override
    public ContentValues buildContent(PairingRequest obj) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PAIRING_COLUMN_SENDER_KEY, obj.getSenderPubKey());
        contentValues.put(PAIRING_COLUMN_REMOTE_KEY, obj.getRemotePubKey());
        if (obj.getRemoteServerId() != null)
            contentValues.put(PAIRING_COLUMN_REMOTE_SERVER_ID, obj.getRemoteServerId());
        contentValues.put(PAIRING_COLUMN_SENDER_NAME, obj.getSenderName());
        contentValues.put(PAIRING_COLUMN_TIMESTAMP, obj.getTimestamp());
        contentValues.put(PAIRING_COLUMN_STATUS, obj.getStatus().getType());
        if (obj.getRemotePubKey() != null)
            contentValues.put(PAIRING_COLUMN_REMOTE_SERVER_HOST_ID, obj.getRemoteHost());
        contentValues.put(PAIRING_COLUMN_SENDER_PS_HOST, obj.getSenderPsHost());
        contentValues.put(PAIRING_COLUMN_PAIR_STATUS, obj.getPairStatus().name());
        contentValues.put(PAIRING_COLUMN_REMOTE_NAME, obj.getRemoteName());
        contentValues.put(PAIRING_COLUMN_REMOTE_ID, obj.getRemoteId());
        return contentValues;
    }

    @Override
    public PairingRequest buildFrom(Cursor cursor) {
        int id = cursor.getInt(PAIRING_COLUMN_POS_ID);
        String senderKey = cursor.getString(PAIRING_COLUMN_POS_SENDER_KEY);
        String senderName = cursor.getString(PAIRING_COLUMN_POS_SENDER_NAME);
        String remoteKey = cursor.getString(PAIRING_COLUMN_POS_REMOTE_KEY);
        String remoteServerId = cursor.getString(PAIRING_COLUMN_POS_REMOTE_SERVER_ID);
        long timestamp = cursor.getLong(PAIRING_COLUMN_POS_TIMESTAMP);
        PairingMessageType status = PairingMessageType.getByName(cursor.getString(PAIRING_COLUMN_POS_STATUS));
        String remotePsHost = cursor.getString(PAIRING_COLUMN_POS_REMOTE_SERVER_HOST_ID);
        String senderPsHost = cursor.getString(PAIRING_COLUMN_POS_SENDER_PS_HOST);
        String remoteName = cursor.getString(PAIRING_COLUMN_POS_REMOTE_NAME);
        int remoteId = cursor.getInt(PAIRING_COLUMN_POS_REMOTE_ID);
        ProfileInformationImp.PairStatus pairStatus = ProfileInformationImp.PairStatus.valueOf(cursor.getString(PAIRING_COLUMN_POS_PAIR_STATUS));
        return new PairingRequest(id, senderKey, remoteKey, remoteServerId, remotePsHost, senderName, timestamp, status, senderPsHost, remoteName, pairStatus, remoteId);
    }

    @Override
    public int savePairingRequest(PairingRequest pairingRequest) {
        return (int) insert(pairingRequest);
    }

    @Override
    public int saveOrUpdate(PairingRequest pairingRequest) {
        if (containsPairingRequest(pairingRequest.getSenderPubKey(), pairingRequest.getRemotePubKey())) {
            log.info("Pairing request exist, " + pairingRequest);
            PairingRequest storedRequest = getPairingRequest(pairingRequest.getSenderPubKey(), pairingRequest.getRemotePubKey());
            pairingRequest.setId((int) storedRequest.getId());
            update(PAIRING_COLUMN_ID, String.valueOf(storedRequest.getId()), pairingRequest);
            return (int) pairingRequest.getId();
        }
        return savePairingRequest(pairingRequest);
    }

    @Override
    public PairingRequest getPairingRequest(String senderPubKey, String remotePubkey) {
        // todo: do this ok with both values for more apps and not just one..
        return get(PAIRING_COLUMN_REMOTE_KEY, remotePubkey);
    }

    @Override
    public PairingRequest getPairingRequest(int pairingRequestId) {
        return get(PAIRING_COLUMN_ID, pairingRequestId);
    }

    @Override
    public PairingRequest getPairingRequestByExternalId(int externalPairId) {
        return get(PAIRING_COLUMN_REMOTE_ID, externalPairId);
    }

    public boolean containsPairingRequest(String senderPubKey, String remotePubkey) {
        // todo: do this ok with both values for more apps and not just one..
        return contains(PAIRING_COLUMN_REMOTE_KEY, remotePubkey);
    }

    @Override
    public List<PairingRequest> pairingRequests(String senderPubKey) {
        return list();
    }

    @Override
    public List<PairingRequest> openPairingRequests(String senderPubKey) {
        Cursor cursor = getData(PAIRING_COLUMN_STATUS + " != '" + PairingMessageType.PAIR_ACCEPT.getType() + "'");
        List<PairingRequest> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                list.add(buildFrom(cursor));
            } while (cursor.moveToNext());
        }
        return list;
    }

    @Override
    public boolean updateStatus(String senderPubKey, String remotePubKey, PairingMessageType status, ProfileInformationImp.PairStatus pairStatus) {
        //return updateFieldByKey(PAIRING_COLUMN_REMOTE_KEY,remotePubKey,PAIRING_COLUMN_STATUS,status.getType())==1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PAIRING_COLUMN_STATUS, status.getType());
        contentValues.put(PAIRING_COLUMN_PAIR_STATUS, pairStatus.name());
        return db.update(
                getTableName(),
                contentValues,
                PAIRING_COLUMN_REMOTE_KEY + "=? and " + PAIRING_COLUMN_SENDER_KEY + "=?",
                new String[]{remotePubKey, senderPubKey}
        ) == 1;

    }

    @Override
    public boolean updateStatus(int pairingRequestId, PairingMessageType status, ProfileInformationImp.PairStatus pairStatus) {
        //return updateFieldByKey(PAIRING_COLUMN_REMOTE_KEY,remotePubKey,PAIRING_COLUMN_STATUS,status.getType())==1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PAIRING_COLUMN_STATUS, status.getType());
        contentValues.put(PAIRING_COLUMN_PAIR_STATUS, pairStatus.name());
        return db.update(
                getTableName(),
                contentValues,
                PAIRING_COLUMN_ID + "=?",
                new String[]{String.valueOf(pairingRequestId)}
        ) == 1;

    }

    @Override
    public int disconnectPairingProfile(String senderPubKey, String remotePubKey) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows1 = db.delete(PAIRING_TABLE_NAME, PAIRING_COLUMN_SENDER_KEY + "=? and " + PAIRING_COLUMN_REMOTE_KEY + "=?", new String[]{senderPubKey, remotePubKey});
        int rows2 = db.delete(PAIRING_TABLE_NAME, PAIRING_COLUMN_SENDER_KEY + "=? and " + PAIRING_COLUMN_REMOTE_KEY + "=?", new String[]{remotePubKey, senderPubKey});
        Log.i("GENERAL", "ROWS 1 DELETE IN disconnectPairingProfile " + rows1);
        Log.i("GENERAL", "ROWS 2 DELETE IN disconnectPairingProfile " + rows2);
        db.close();
        return rows1 + rows2;
    }

    @Override
    public int removeRequest(String senderPubKey, String remotePubkey) {
        // todo: do this ok with both values for more apps and not just one..
        return delete(PAIRING_COLUMN_REMOTE_KEY, remotePubkey);
    }

    @Override
    public void delete(long id) {
        delete(PAIRING_COLUMN_ID, String.valueOf(id));
    }

}
