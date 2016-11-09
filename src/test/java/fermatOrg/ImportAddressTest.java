package fermatOrg;

import fermatOrg.exceptions.AddressFormatException;
import fermatOrg.exceptions.IoWalletException;
import fermatOrg.network.NetworkType;
import fermatOrg.wallet.WatchOnlyWallet;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by rodrigo on 11/8/16.
 */
public class ImportAddressTest {
    private List<String> addressList;
    private final File inputFile;
    private  final File walletFile;
    private WatchOnlyWallet wallet;

    public ImportAddressTest() throws IoWalletException {
        inputFile = new File("inputFile.txt");
        walletFile = new File("walletTest");

        // clean up data
        if (inputFile.exists())
            inputFile.delete();

        if (walletFile.exists())
            walletFile.delete();

        //create the wallet.
        wallet = new WatchOnlyWallet(walletFile, NetworkType.TEST);

    }

    /**
     * sets the wallet and the import file
     * @throws Exception
     */
    @org.junit.Before
    public void setUp() throws Exception {
        /**
         * Added some testnet random addresses
         */
        addressList = new ArrayList<>();
        addressList.add("uTwvyZJ3PtGD6rSKvARAYk3H5PXbfvnQsJ");
        addressList.add("uTRzrTWXqaiDuDqRaX3ngjpp6Qv38cUH26");
        addressList.add("uLTfXL36DhLUBaQ4GfxQo7xP2gcK2oyPeP");
        addressList.add("uhze2PNXd9PC2wx5LCkpj9vM59pn8gF2LB");

        // creates the file and saves the addresses into disk
        FileWriter writer = new FileWriter(inputFile);
        for(String str: addressList) {
            writer.write(str + System.lineSeparator());
        }
        writer.close();
    }

    /**
     * Tries to import from a non existing file
     * @throws AddressFormatException
     * @throws IoWalletException
     */
    @Test(expected = IoWalletException.class)
    public void importNonExistingFile() throws AddressFormatException, IoWalletException {
        File badFile = new File("non existing file");
        wallet.importAddresses(badFile);
    }

    /**
     * succesfully import addresses
     */
    @Test
    public void importValidAddresses() throws AddressFormatException, IoWalletException {
        wallet.importAddresses(inputFile);

        Assert.assertTrue(wallet.getAddresses().size() == 4);
    }

    @Test
    public void importValidAddress() throws AddressFormatException {
        String address = "uPTjEnG3oJeJmNE9YP2ENp6i5rUvP1Ckap";
        wallet.importAddress(address);
        Assert.assertTrue(wallet.getAddresses().contains(address));
    }

    @Test (expected = AddressFormatException.class)
    public void importInvalidAddress() throws AddressFormatException {
        wallet.importAddress("BadAddress");
        Assert.assertTrue(wallet.getAddresses().size() == 4);
    }


    @org.junit.After
    public void cleanUp(){
        this.inputFile.delete();
        this.walletFile.delete();

        File event = new File("events");
        if (event.exists())
            event.delete();

    }



}
