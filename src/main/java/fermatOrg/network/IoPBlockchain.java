package fermatOrg.network;

import ch.qos.logback.classic.Logger;
import com.google.common.base.Preconditions;
import fermatOrg.exceptions.IoPBlockchainException;
import fermatOrg.wallet.WatchOnlyWallet;
import fermatOrg.wallet.event.EventNotificationManager;
import fermatOrg.wallet.event.IncomingTransactionEvent;
import fermatOrg.wallet.event.IncomingTransactionListener;
import org.blockchainj.core.*;
import org.blockchainj.store.BlockStore;
import org.blockchainj.store.BlockStoreException;
import org.blockchainj.store.MemoryBlockStore;
import org.blockchainj.store.SPVBlockStore;
import org.blockchainj.wallet.Wallet;
import org.blockchainj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rodrigo on 11/3/16.
 * The IoP Blockchain connection. It takes care of finding peers on the IoP blockchain, download pending blocks as they are visible.
 * If we found transactions that belong to any of the watched address, we will store it and trigger the incoming transaction event.
 * Its better (in terms of performance) to only {@link WatchOnlyWallet#connect()}to the blockchain after all addresses have already been imported into the wallet with {@link WatchOnlyWallet#importAddresses(File)}
 */
public class IoPBlockchain implements  Blockchain{
    //class variables
    private PeerGroup peerGroup;
    private BlockStore blockStore;
    private BlockChain blockChain;

    // static members
    private static Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);


    // class constants
    private final Wallet wallet;
    private final Context context;
    private final File blockchainFile; //the file in which we are storing the blocks
    private final EventNotificationManager eventNotificationManager;

    /**
     * Constructor with the blockchainj wallet
     * @param wallet the blockchainj wallet with the imported addresses.
     */
    public IoPBlockchain(Wallet wallet, EventNotificationManager eventNotificationManager) {
        Preconditions.checkNotNull(wallet);
        Preconditions.checkNotNull(eventNotificationManager);

        this.wallet = wallet;
        this.eventNotificationManager = eventNotificationManager;
        this.context = wallet.getContext();
        this.blockchainFile = new File(wallet.getNetworkParameters().getId());

        //sets the log level the same as the wallet
        logger.setLevel(WatchOnlyWallet.getLogLevel());
    }

    /**
     * Connects to the blockchain and start downloading missing blocks.     *
     * It will try to connect first to any local IoP client, if not found, then it ill connect to remote peers thought DNS discovery.
     * Once all blocks are downloaded it will automatically disconnect.
     * @throws IoPBlockchainException If there is a connection problem
     */
    public void connect() throws IoPBlockchainException {
        try{
            configureConnection();

            if (!peerGroup.isRunning()){
                this.peerGroup.start();
                this.peerGroup.downloadBlockChain();
            }
        } catch (Exception e){
            throw new IoPBlockchainException("There was an error connecting to the IoP blockchain.", e);
        }

    }

    /**
     * Disconnects from the blockchain network. The {@link #connect()} methods disconnects automatically
     * after last block has been downloaded from blockchain.
     */
    public void disconnect(){
        this.peerGroup.stop();
    }

    /**
     * sets all needed objects to perform the connection
     * @throws BlockStoreException
     */
    private void configureConnection() throws BlockStoreException {
        if (blockStore == null)
            try {
                this.blockStore = new SPVBlockStore(context.getParams(), blockchainFile);
            } catch (BlockStoreException e) {
                this.blockStore = new MemoryBlockStore(context.getParams());
            }

            if (this.blockChain == null)
                this.blockChain = new BlockChain(this.context, this.wallet, this.blockStore);

        if (this.peerGroup == null) {
            this.peerGroup = new PeerGroup(context, blockChain);
            this.peerGroup.setConnectTimeoutMillis(30 * 1000); //30 seconds time out default.

            // add the wallet event listener
            IncomingEvent incomingEvent = new IncomingEvent();
            this.wallet.addCoinsReceivedEventListener(incomingEvent);

            this.peerGroup.addWallet(wallet);

        }
    }

    /**
     * it notifies if we are connected to the IoP blockchain or not.
     * @return true if we are connected.
     */
    public boolean isConnected(){
        if (peerGroup != null && peerGroup.isRunning()){
            if (peerGroup.getConnectedPeers().size() > 0)
                return true;
            else
                return  false;
        } else
            return false;
    }

    /**
     * returns the amount of blocks we have downloaded from the IoP blockchain
     * @return the amount of blocks we downloaded when this method was called.
     */
    public int getBlockchainHeight(){
        return peerGroup.getMostCommonChainHeight();
    }


    /**
     * The incoming blockchainj event declaration class
     */
    private class IncomingEvent implements WalletCoinsReceivedEventListener{
        /**
         * When a blockchainj coinsReceived event is triggered, I'm triggering the IncomingTransaction Event for every
         * watched address and let the listeners know.
         * @param wallet this wallet.
         * @param transaction the incoming transaction
         * @param coin current coints
         * @param coin1 new balance coins
         */
        public void onCoinsReceived(Wallet wallet, Transaction transaction, Coin coin, Coin coin1) {
            // create a new event for every coin sent to a watched only address of the wallet.
            for (TransactionOutput output : transaction.getOutputs()){
                if (output.isWatched(wallet)){
                    //event definition
                    IncomingTransactionEvent event = new IncomingTransactionEvent(
                            this,
                            output.getAddressFromP2PKHScript(context.getParams()).toBase58(),
                            WatchOnlyWallet.getNetworkType(context.getParams()),
                            coin1.getValue(),
                            transaction.getHashAsString());
                    try {
                        // I will add this event if not added before
                        if (!eventNotificationManager.getPendingNotificationEvents().contains(event))
                            eventNotificationManager.addNewEvent(event);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Verifies if we are Synced with the blockchain. A synced blockchain is means that we have the latest block that was broadcasted to the peers we are connected.     *
     * @return true if we have all blocks from the blockchain. If false, there might be transactions which we haven't heard off yet.
     */
    public boolean isSynced() {
        if (!this.isConnected())
            return false;

        // lets check the current blockheight againts what peers have to send me
        for (Peer peer : peerGroup.getConnectedPeers()){
            if (peer.getPeerBlockHeightDifference() > 10)
                return false;
        }

        return  true;
    }
}
