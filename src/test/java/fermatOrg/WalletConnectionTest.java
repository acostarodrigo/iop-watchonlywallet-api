package fermatOrg;

import fermatOrg.exceptions.AddressFormatException;
import fermatOrg.exceptions.IoPBlockchainException;
import fermatOrg.exceptions.IoWalletException;
import fermatOrg.network.IoPBlockchain;
import fermatOrg.network.NetworkType;
import fermatOrg.wallet.WatchOnlyWallet;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rodrigo on 11/8/16.
 */
public class WalletConnectionTest {

    private  final File walletFile;
    private WatchOnlyWallet wallet;

    public WalletConnectionTest() throws IoWalletException, AddressFormatException {
        walletFile = new File("walletTest");


        if (walletFile.exists())
            //load an existing wallet
            wallet = WatchOnlyWallet.loadFromFile(walletFile);
        else
            //create the wallet.
            wallet = new WatchOnlyWallet(walletFile, NetworkType.PRODUCTION);

        wallet.importAddress("pRo2xwwWF2mKC78kwUCZiYaLeVUn4yCpbo"); //randomly selectec address from mainnet
        wallet.importAddress("p6YdVrQFGfp8AuhpnaqgAVHibNVd3LxbTU"); //zero balance address

    }

    @Test
    public void connectTest() throws IoPBlockchainException, AddressFormatException, IoWalletException, InterruptedException {
        IoPBlockchain blockchain = this.wallet.connect();

        Assert.assertTrue(blockchain.isConnected());
        Assert.assertTrue(blockchain.isSynced());

        Assert.assertTrue(wallet.getAddressBalance("pRo2xwwWF2mKC78kwUCZiYaLeVUn4yCpbo") != 0); //this address has funds.
        Assert.assertTrue(wallet.getAddressBalance("p6YdVrQFGfp8AuhpnaqgAVHibNVd3LxbTU") == 0); //this address has no funds.
    }

    @Test
    public void disconnectTest() throws IoPBlockchainException {
        IoPBlockchain blockchain = this.wallet.connect();

        Assert.assertTrue(blockchain.isConnected());
        Assert.assertTrue(blockchain.isSynced());

        blockchain.disconnect();
        Assert.assertTrue(!blockchain.isConnected());
    }

    @org.junit.After
    public void cleanUp(){
        this.walletFile.delete();

        File blockchain = new File("org.IoP.production");
        if (blockchain.exists())
            blockchain.delete();

    }
}
