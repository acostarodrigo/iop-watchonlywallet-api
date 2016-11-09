package fermatOrg.network;

import java.security.InvalidParameterException;

/**
 * Created by rodrigo on 11/1/16.
 * Lists the possible networks to use with Internet of People.
 * Production is the mainnet networks with real IoPs.
 * Test is equal to production but IoPs have no value.
 * RegTest is usually used locally by developers to test.
 */
public enum NetworkType {
    PRODUCTION ("production"),
    TEST ("test"),
    REGTEST ("regtest");

    private final String code;

    private NetworkType(String code){
        this.code = code;
    }


    public NetworkType getByCode(String code){
        switch (code.toLowerCase()){
            case "production":
                return PRODUCTION;
            case "test":
                return TEST;
            case "regtest":
                return REGTEST;
            default:
                throw new InvalidParameterException("Provided code " + code + " is not a valid Network type.");
        }
    }
}
