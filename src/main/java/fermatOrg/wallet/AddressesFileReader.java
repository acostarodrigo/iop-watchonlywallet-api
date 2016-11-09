package fermatOrg.wallet;

import com.google.common.base.Preconditions;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rodrigo on 11/3/16.
 */
class AddressesFileReader {
    private final File inputFile;

    /**
     * constructor
     * @param inputFile the file that will be used to import addresses.
     */
    public AddressesFileReader(File inputFile) {
        // preconditions
        Preconditions.checkNotNull(inputFile);
        Preconditions.checkArgument(inputFile.exists());

        this.inputFile = inputFile;
    }

    /**
     * Will read all lines from the class inputFile and return the list of lines from the file.
     * At this point, the lines could be anything. We are not validating they are valid addresses on the current network.
     * @return a list of Strings representing each line on the input file.
     * @throws IOException If we can't read the file for any reason.
     */
    public List<String> importAddresses() throws IOException {
        List<String> lines = new ArrayList<String>();
        FileInputStream fis = new FileInputStream(this.inputFile);

        //Construct BufferedReader from InputStreamReader
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

        String line = null;
        while ((line = br.readLine()) != null) {
            lines.add(line);
        }

        br.close();

        return lines;
    }
}
