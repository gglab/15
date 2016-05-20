/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fifteen;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 *
 * @author glabg
 */
public class ShowSolution {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("No parameters!");
        }
        for (int i = 0; i < args.length; i++) {
            System.out.println("Printing Solution: " + args[i]);
            try (Stream<String> stream = Files.lines(Paths.get(args[i]), Charset.defaultCharset())) {
                stream.forEach(System.out::println);
            }
        }
    }
}
