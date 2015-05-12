package ccre.testing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ccre.saver.StorageProvider;

/**
 * Tests the StorageProvider class and the currently-registered storage
 * provider.
 * 
 * @author skeggsc
 */
public class TestFiles extends BaseTest {

    @Override
    public String getName() {
        return "StorageProvider tests";
    }

    @Override
    protected void runTest() throws Throwable {
        testNonexistent("test-fail.txt");
        testOutput("test-file.txt");
        testInput("test-file.txt");
        testOutput("test-file-noext");
        testInput("test-file-noext");
        testNonexistent("test-file-noext.txt");
    }

    private void testInput(String name) throws TestingException, IOException {
        InputStream input = StorageProvider.openInput(name);
        assertTrue(input != null, "openInput should not return null for an existing file!");
        // this could be removed... it's not guaranteed but I would like it to be true.
        assertTrue(input.available() > 0, "non-empty input streams should (hopefully) never be unavailable");
        // TODO: test mark() methods?
        assertIntsEqual(input.read(), 'H', "bad byte from file");
        assertIntsEqual(input.read(), 'e', "bad byte from file");
        assertIntsEqual(input.read(), 'l', "bad byte from file");
        assertIntsEqual(input.read(), 'l', "bad byte from file");
        assertIntsEqual(input.read(), 'o', "bad byte from file");
        assertIntsEqual(input.read(new byte[0]), 0, "empty read should be empty");
        byte[] data = new byte[1];
        assertIntsEqual(input.read(data), 1, "expected at least one byte from file");
        assertIntsEqual(data[0], ',', "bad byte from file");
        data = new byte[2];
        // this isn't guaranteed... remove? test better?
        assertIntsEqual(input.read(data), 2, "hoped for at least two bytes from file");
        assertIntsEqual(data[0], ' ', "bad byte from file");
        assertIntsEqual(data[1], 'W', "bad byte from file");

        // read a big chunk
        data = new byte[29];
        int index = 0;
        while (index < data.length) {
            int out = input.read(data, index, data.length - index);
            assertTrue(out >= 1, "should not have hit EOF yet...");
            index += out;
            assertTrue(index <= data.length, "too many bytes returned...");
        }
        byte[] expected = "orld!\nABCDd\nHello, World (2)!".getBytes();
        assertIntsEqual(data.length, index, "should have finished reading");
        assertIntsEqual(index, expected.length, "should have finished reading");
        for (int i = 0; i < data.length; i++) {
            assertIntsEqual(data[i], expected[i], "bad byte from file at " + i);
        }
        data = new byte[2];
        assertIntsEqual(input.read(data), 1, "expected exactly one byte (the last byte)");
        assertIntsEqual(data[0], '\n', "bad byte from file");
        // at end now
        assertIntsEqual(input.read(data), -1, "expected EOF from file");
        assertIntsEqual(input.read(data, 0, 2), -1, "expected EOF from file");
        assertIntsEqual(input.read(data, 0, 1), -1, "expected EOF from file");
        assertIntsEqual(input.read(data, 1, 1), -1, "expected EOF from file");
        assertIntsEqual(input.read(), -1, "expected EOF from file");
        input.close();
        try {
            input.read();
            assertFail("file closed... should have thrown an exception!");
        } catch (IOException ex) {
            // correct!
        }
        try {
            input.read(new byte[8]);
            assertFail("file closed... should have thrown an exception!");
        } catch (IOException ex) {
            // correct!
        }
        try {
            input.read(new byte[6], 3, 1);
            assertFail("file closed... should have thrown an exception!");
        } catch (IOException ex) {
            // correct!
        }
        // TODO: test skip?
    }

    private void testOutput(String name) throws TestingException, IOException {
        OutputStream output = StorageProvider.openOutput(name);
        assertTrue(output != null, "openOutput should not return null!");
        output.write("Hello, World!".getBytes());
        output.write('\n');
        output.write(0xFFFFFF00 + 'A');
        output.write("abcdABCDdone".getBytes(), 5, 4);
        output.flush(); // TODO: make this actually checked
        output.write('\n');
        output.write("Hello, World (2)!\n".getBytes());
        output.close();
        try {
            output.write("Failing...\n".getBytes()); // should fail!
            assertFail("Should have failed to write bytes!");
        } catch (IOException ex) {
            // correct!
        }
        output.close(); // extra closing shouldn't cause any issues.
        output.close();
        try {
            output.write('\n'); // should fail!
            assertFail("Should have failed to write byte!");
        } catch (IOException ex) {
            // correct!
        }
        output.close();
    }

    private void testNonexistent(String name) throws TestingException, IOException {
        InputStream input = StorageProvider.openInput(name);
        assertIdentityEqual(input, null, "Nonexistent file should result in a NULL return value!");
    }

}
