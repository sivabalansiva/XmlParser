package com.sivabalan.xmlparser;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class XMLWordCounter {

    private static final String STRING = "string";
    private static final String NAME = "name";

    private final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

    static XMLWordCounter newInstance() {
        return new XMLWordCounter();
    }

    public static void main(String[] args) {
        XMLWordCounter obj = newInstance();
        String url = obj.getUrl();
        try {
            List<String> stringValuesList = obj.getStringsValuesList(new File(url));
            long wordCount = obj.getWordCount(stringValuesList);
            System.out.println("Words count = " + wordCount);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private long getWordCount(List<String> stringValuesList) {
        if(stringValuesList == null || stringValuesList.isEmpty()) {
            return 0;
        }
        long wordCount = 0;
        for (String value: stringValuesList) {
            if(value == null || value.trim().length() == 0) {
                continue;
            }
            String[] temp = value.split(" ");
            wordCount += temp.length;
        }
        return wordCount;
    }

    private String getUrl()   {
        System.out.print("Enter the url...");
        Scanner scan = new Scanner(System.in);
        return scan.next();
    }

    private List<String> getStringsValuesList(File file) throws XMLStreamException {
        ArrayList<String> stringValuesList = new ArrayList<>();
        try {
            InputStream stream = new FileInputStream(file);
            XMLEventReader reader = xmlInputFactory.createXMLEventReader(stream);
            while(reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if(event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(STRING))    {
                    try {
                        String value = reader.getElementText();
                        stringValuesList.add(value);
                    } catch (XMLStreamException e) {
                        // we are ignoring this exception (getElementText expects string but was START_ELEMENT)
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println(file.toString() + " file not found.");
            e.printStackTrace();
        }
        return stringValuesList;
    }
}
