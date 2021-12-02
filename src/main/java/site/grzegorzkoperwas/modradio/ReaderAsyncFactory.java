package site.grzegorzkoperwas.modradio;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;

public class ReaderAsyncFactory implements Callable<Reader>
{
    private Scanner source;
    public ReaderAsyncFactory(Scanner source) {
        this.source = source;
    }
    @Override
    public Reader call() throws NoSuchElementException {
        Reader reader = null;
        while (reader == null) {
            String filepath = "null";
            try {
                filepath = this.source.nextLine();
                reader = new Reader(filepath);
            } catch (IllegalStateException e) {
                File file = new File(filepath);
                file.delete();
                System.out.println("Failed to open " + filepath);
            }
        }
        System.out.println(reader);
        return reader;
    }
}
