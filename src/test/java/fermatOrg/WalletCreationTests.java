package fermatOrg;

import fermatOrg.exceptions.IoWalletException;
import fermatOrg.network.NetworkType;
import fermatOrg.wallet.WatchOnlyWallet;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Random;

/**
 * Created by rodrigo on 11/8/16.
 */
public class WalletCreationTests {
    private File walletFile;

    @org.junit.Before
    public void setUp() throws Exception {
        // generate a random file name
        Random randomGenerator = new Random();
        String fileName = "test" + randomGenerator.nextInt(100);
        this.walletFile= new File(fileName);

    }

    /**
     * Creates a new wallet and verify the file is created.
     */
    @Test
    public void createNewWalletTest(){

        // create a new wallet.
        WatchOnlyWallet watchOnlyWallet = null;
        try {
            watchOnlyWallet = new WatchOnlyWallet(walletFile, NetworkType.PRODUCTION);
        } catch (IoWalletException e) {
            e.printStackTrace();
        }

        // validate the wallet was created and saved.
        Assert.assertEquals(watchOnlyWallet.getWalletFile().exists(), true);

        // create a new wallet using the same file.
        // we are expecting an error
        WatchOnlyWallet watchOnlyWallet1 = null;
        try {
            watchOnlyWallet1 = new WatchOnlyWallet(walletFile, NetworkType.PRODUCTION);
        } catch (IoWalletException e) {
            //expected error
        }

        Assert.assertNull(watchOnlyWallet1);


        // load an existing wallet.
        WatchOnlyWallet watchOnlyWallet2 = null;
        try {
            watchOnlyWallet2 = WatchOnlyWallet.loadFromFile(walletFile);
        } catch (IoWalletException e) {
            e.printStackTrace();
        } finally {
            walletFile.delete();
        }

        Assert.assertEquals(watchOnlyWallet.getWalletFile(), watchOnlyWallet2.getWalletFile());

        // all done, delete file
        if (walletFile.exists())
            walletFile.delete();
    }
}
