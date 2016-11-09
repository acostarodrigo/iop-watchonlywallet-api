package fermatOrg.network;

import fermatOrg.exceptions.IoPBlockchainException;
import fermatOrg.wallet.event.IncomingTransactionListener;

/**
 * Created by rodrigo on 11/6/16.
 */
interface Blockchain  {

    /**
     * it notifies if we are connected to the IoP blockchain or not.
     * @return true if we are connected.
     */
    boolean isConnected();

    /**
     * returns the amount of blocks we have downloaded from the IoP blockchain
     * @return the amount of blocks we downloaded when this method was called.
     */
    public int getBlockchainHeight();

    /**
     * Connects to the blockchain and start downloading missing blocks.
     * It will try to connect first to any local IoP client, if not found, then it ill connect to remote peers thought DNS discovery.
     * Once connected it will download all pending blocks and check for transactions related to our addresses.
     * Then it will disconnect automatically.
     * @throws IoPBlockchainException If there is a connection problem
     */
    public void connect() throws IoPBlockchainException;

    /**
     * Disconnects from the blockchain network. The {@link #connect()} methods disconnects automatically
     * after last block has been downloaded from blockchain.
     */
    public void disconnect();

    /**
     * Verifies if we are Synced with the blockchain. A synced blockchain is means that we have the latest block that was broadcasted to the peers we are connected.     *
     * @return true if we have all blocks from the blockchain. If false, there might be transactions which we haven't heard off yet.
     */
    boolean isSynced();
}
