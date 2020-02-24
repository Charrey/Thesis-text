package test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class MyTestCase {

    protected Path resource(String path) {
        try {
            return Paths.get(this.getClass().getResource(path).toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        } catch (NullPointerException e) {
            throw new RuntimeException("Could not find resource: \"" + path + "\"");
        }
    }

}
