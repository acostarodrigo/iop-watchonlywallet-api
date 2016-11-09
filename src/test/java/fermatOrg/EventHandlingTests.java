package fermatOrg;

import fermatOrg.exceptions.AddressFormatException;
import fermatOrg.exceptions.IoPBlockchainException;
import fermatOrg.exceptions.IoWalletException;
import fermatOrg.network.IoPBlockchain;
import fermatOrg.network.NetworkType;
import fermatOrg.wallet.WatchOnlyWallet;
import fermatOrg.wallet.event.IncomingTransactionEvent;
import fermatOrg.wallet.event.IncomingTransactionListener;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Created by rodrigo on 11/8/16.
 */
public class EventHandlingTests {
    private  final File walletFile;
    private WatchOnlyWallet wallet;

    public EventHandlingTests() throws IoWalletException, AddressFormatException {
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

    private class Listener implements IncomingTransactionListener{
        private IncomingTransactionEvent event;
        @Override
        public void incomingEvent(IncomingTransactionEvent incomingTransactionEvent) {
            Assert.assertNotNull(incomingTransactionEvent);

            Assert.assertEquals(IncomingTransactionEvent.Status.PENDING_NOTIFICATION, incomingTransactionEvent.getStatus());

            this.event = incomingTransactionEvent;
        }

        // returns the event.
        public IncomingTransactionEvent getEvent() {
            return event;
        }
    }

    @Test
    public void listenEventTest() throws IoPBlockchainException, AddressFormatException, IoWalletException, InterruptedException {
        Listener listener = new Listener();
        wallet.addIncomingTransactionListener(listener);
        IoPBlockchain blockchain = this.wallet.connect();
        Assert.assertTrue(blockchain.isConnected());
        Assert.assertTrue(blockchain.isSynced());
        Assert.assertTrue(wallet.getWalletBalance() != 0);


        IncomingTransactionEvent event = listener.getEvent();

        //I must have a valid event object
        Assert.assertNotNull(event);

        // the wallet must have at least one pending notification
        Assert.assertTrue(wallet.getPendingNotificationEvents().size() != 0);

        // I will confirm the event that I have
        wallet.confirmEventReception(event);

        // at leat that event is gone.
        Assert.assertTrue(!wallet.getPendingNotificationEvents().contains(event));
    }

    @org.junit.After
    public void cleanUp(){
        this.walletFile.delete();
        File blockchain = new File("org.IoP.production");
        if (blockchain.exists())
            blockchain.delete();

        File events = new File("events");
        if (events.exists())
            events.delete();

    }
}
