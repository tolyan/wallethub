import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;



/*
NOTE: This solution uses MapDB library, you can find it at https://github.com/jankotek/mapdb/

This task is rather trivial, all we need is to create data structure that maps dictionary to word count, it could be
mere java.util.Map, but size of a dictionary is not defined,so we can not rely on memory only structure
to store words count.
We need some kind of memory based cache with file-based storage. Enterprise level key/value storages
would be overkill, so I chose simple in-memory DB with file-based storage support:
MapDB https://github.com/jankotek/mapdb/
It allows us to store some part of our map structure in memory and keep the rest of it in file.

Computational complexity: first point to mention is read time for hashmap, usually it is considered constant time, but
in worst case scenario it is linear. In our case we have very large dictionary, thus size of buckets will grow and read
time will eventually degrade to linear, so I assume it as O(m) there m is size of the bucket.
So, in our algorithm we read file with checking values of map, thus on large data sets it will tend to O(n*m) where n is
amount of phrases and m is size of hash map bucket. Next we sort our map and on large data sets it will be O(n*m log n*m)
for time and O(log n*m) for space. After that we read k first entries of linked list for O(k) time. Considering k < n
overal time is O(n*m log n*m) and space is O(log n*m)
 */
public class TopPhrases {

    private static final Logger logger = Logger.getLogger(TopPhrases.class.getName());
    public static final int MAP_MAX_SIZE = 11000;
    public static final int COUNT_CAP = 10000;

    public static void main(String [] args){
        InputStream inputStream = TopPhrases.class.getClassLoader().getResourceAsStream("input.txt");

        getTopPhrases(inputStream, COUNT_CAP)
                .forEach((s, aLong) ->  System.out.println("phrase: " + s + "value: " +aLong));
    }


    public static Map<String, Long>  getTopPhrases(InputStream inputStream, int limit) {
        //file based DB to store evicted data
        DB diskDB = DBMaker
                .fileDB("./phrases.db")
                .checksumHeaderBypass() //use to bypass checks in test environment
                .make();
        HTreeMap<String, Long>valueMap =  diskDB.hashMap("diskValues")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.LONG)
                .createOrOpen();

        //fast in-memory db to store data.
        DB memoryDB = DBMaker
                .memoryDB()
                .make();

        //executors threads to perform eviction in background.
        ScheduledExecutorService executor =
                Executors.newScheduledThreadPool(2);

        //fast in-memory collection with limited size and eviction strategy of new entries,
        //thus high frequency words will stay in memory and low frequency words will be
        //evicted to file storage eventually.
        HTreeMap<String, Long> phrases = memoryDB.hashMap("allPhrases")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.LONG)
                .counterEnable()
                .expireExecutor(executor)
                .expireMaxSize(MAP_MAX_SIZE)
                .expireAfterCreate()
                .expireOverflow(valueMap)
                .create();

        //read the file and store phrases with count
        try {

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;

            while( (line=bufferedReader.readLine()) != null ) {
                String[] linePhrases = line.split("\\|");
                for (String phrase : linePhrases){
                    if (phrases.containsKey(phrase)) {
                        phrases.put(phrase, phrases.get(phrase) + 1L);
                    } else {
                        phrases.put(phrase, 1L);
                    }
                }
            }


        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, e.toString(), e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.toString(), e);
        }

        //sort map by value
        List<Map.Entry<String, Long>> list =
                new LinkedList<>( phrases.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<String, Long>>()
        {
            @Override
            public int compare( Map.Entry<String, Long> o1, Map.Entry<String, Long> o2 )
            {
                return ( o2.getValue() ).compareTo( o1.getValue() );
            }
        } );

        //and pick top phrases
        int cap = list.size() > COUNT_CAP ? COUNT_CAP : list.size();
        List<Map.Entry<String, Long>> capped = list.subList(0, cap);
        Map<String, Long> result = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : capped)
        {
            result.put( entry.getKey(), entry.getValue() );
        }


        diskDB.close();
        memoryDB.close();
        return result;
    }
}
