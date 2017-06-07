package org.fermat.redtooth.core.services.pairing;

import org.apache.http.MethodNotSupportedException;
import org.fermat.redtooth.core.services.BaseMsg;
import org.fermat.redtooth.global.utils.SerializationUtils;

import java.io.Serializable;

/**
 * Created by furszy on 6/4/17.
 */

public class PairingMsg extends BaseMsg<PairingMsg> implements Serializable {

    private String name;

    public PairingMsg() {
    }

    public PairingMsg(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public PairingMsg decode(byte[] msg) throws Exception {
        return SerializationUtils.deserialize(msg,PairingMsg.class);
    }

    @Override
    public byte[] encode() throws Exception {
        // lazy encode to test the entire flow first..
        return SerializationUtils.serialize(this);
    }

    @Override
    public String getType() {
        return PairingMsgTypes.PAIR_REQUEST.getType();
    }
}