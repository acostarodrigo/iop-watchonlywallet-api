package fermatOrg;

import ch.qos.logback.classic.Level;
import fermatOrg.exceptions.IoPBlockchainException;
import fermatOrg.exceptions.IoWalletException;
import fermatOrg.network.NetworkType;
import fermatOrg.wallet.WatchOnlyWallet;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Created by rodrigo on 11/8/16.
 */
public class LogLevelTest {
    private File walletFile;

    public LogLevelTest() {
        walletFile = new File("WalletTest");
        if (walletFile.exists())
            walletFile.delete();
    }

    @Test
    public void changeLogLevelTest() throws IoWalletException, IoPBlockchainException {



        WatchOnlyWallet watchOnlyWallet = new WatchOnlyWallet(walletFile, NetworkType.PRODUCTION);
        WatchOnlyWallet.setLogLevel(Level.OFF);
        watchOnlyWallet.connect();

        Assert.assertEquals(WatchOnlyWallet.getLogLevel(), Level.OFF);
        walletFile.delete();
    }

    @org.junit.After
    public void cleanUp(){
        if (walletFile.exists())
            walletFile.delete();

        File blockchain = new File("org.IoP.production");
        if (blockchain.exists())
            blockchain.delete();

    }
}
