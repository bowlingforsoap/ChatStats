package models;

/**
 * Created by Strelchenko Vadym on 28.05.15.
 */

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import controllers.util.Utils;
import org.bson.Document;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Performs operations with data in MongoDB and manages MongoClient connection.
 */
public class DataFetcher {
    public static final String DEFAULT_STATS_HOST = "localhost";
    public static final String DEFAULT_STATS_DB = "admin_chat";
    public static final String DEFAULT_STATS_COLL = "chat_statistics_per_unit";

    public static final String DB_URI_KEY = "mongodb-uri";
    public static final String COLL_KEY = "collection";
    public static final String KEYS_TO_PARSE_KEY = "keys-to-parse";

    private static DataFetcher instance = new DataFetcher();
    //initialize desired fields
    //the date must be given first
    public static final String[] KEYS_TO_PARSE;

    static {
        List<String> keysToParse = play.Play.application().configuration().getStringList(DataFetcher.KEYS_TO_PARSE_KEY);
        KEYS_TO_PARSE = keysToParse.toArray(new String[keysToParse.size()]);
    }

    private MongoClient client;
    /**
     * Collection to work with.
     */
    private String collection;
    /**
     * DB to work with.
     */
    private String db;

    private DataFetcher() {
        try {
            String dbUriValue = play.Play.application().configuration().getString(DataFetcher.DB_URI_KEY);
            MongoClientURI dbUri = new MongoClientURI(dbUriValue);

            collection = play.Play.application().configuration().getString(DataFetcher.COLL_KEY);
            db = dbUri.getDatabase();
            client = new MongoClient(dbUri);
        } catch (MongoException e) {
            client = new MongoClient(DEFAULT_STATS_HOST);
        }
    }

    public static synchronized DataFetcher getInstance() throws Exception {
        if (instance == null) {
            instance = new DataFetcher();
        }
        return instance;
    }

    public void invalidate() {
        instance.client.close();
        instance = null;
    }

    /**
     * Fetch data from DB and write it to the CSV file.
     *
     * @param appId        the id of the app to fetch;
     * @param startingFrom specifies from which date to start fetching data. Date is calculated as follows: {@code new Date(Sytem.currentTimeMillis() - startingFrom)}.
     * @throws IOException
     */
    public void fetchData(String appId, long startingFrom) throws IOException {
        //initialize db and collection objects
        //if config values are not given, then use dafaults
        MongoDatabase adminChat = getDB();
        MongoCollection<Document> statistics = getCollection(adminChat);
        //define format expected in dygraphs js
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        //find all apps with the given appId, where 'created_at' field is greater than new Date(System.currentTimeMillis() - startingFrom))
        FindIterable<Document> appStats = statistics.find(new BasicDBObject("app_id", appId)
                .append("created_at", new BasicDBObject("$gte", new Date(System.currentTimeMillis() - startingFrom))))
                .sort(new BasicDBObject("created_at", 1));
        //BasicDBObject -> csv format
        //
        StringBuilder builder = new StringBuilder();
        for (Document statsEntry : appStats) {
            //parse date
            //must be in YYYYMMDD format
            builder.append(dateFormat.format(statsEntry.get(KEYS_TO_PARSE[0])));
            builder.append(",");
            //parse everything else
            for (int i = 1; i < KEYS_TO_PARSE.length - 1; i++) {
                builder.append(statsEntry.get(KEYS_TO_PARSE[i]));
                builder.append(",");
            }
            builder.append(statsEntry.get(KEYS_TO_PARSE[KEYS_TO_PARSE.length - 1]));
            builder.append("\n");
        }

        //dump to file
        Utils.writeDataToFile(builder.toString());
    }


    /**
     * Fetches all apps from DB. Uses aggregation.
     *
     * @return
     */
    public List<String> getAppsFromDB() {
        MongoDatabase adminChat = getDB();
        MongoCollection<Document> statistics = getCollection(adminChat);
        //group by appId
        AggregateIterable<Document> aggregateResult = statistics.aggregate(Arrays.asList(new BasicDBObject("$group", new BasicDBObject("_id", "$app_id"))));
        List<String> appsFromDB = new ArrayList<>();

        for (Document app : aggregateResult) {
            appsFromDB.add(app.getString("_id"));
        }
        return appsFromDB;
    }

    private MongoCollection<Document> getCollection(MongoDatabase adminChat) {
        MongoCollection<Document> statistics;
        if (collection == null || collection.length() == 0) {
            statistics = adminChat.getCollection(DEFAULT_STATS_COLL);
        } else {
            statistics = adminChat.getCollection(collection);
        }
        return statistics;
    }

    private MongoDatabase getDB() {
        MongoDatabase adminChat;
        if (db == null || db.length() == 0) {
            adminChat = instance.client.getDatabase(DEFAULT_STATS_DB);
        } else {
            adminChat = instance.client.getDatabase(db);
        }
        return adminChat;
    }

    //TODO: REMOVE
    //in case you need to generate some data
    /*public void insertData(int n) {
        MongoDatabase adminChat = instance.client.getDatabase(DEFAULT_STATS_DB);
        MongoCollection<Document> statistics = adminChat.getCollection(DEFAULT_STATS_COLL);
        Random randGenerator = new Random();
        List<Document> docs = new ArrayList<>(n);
        long hour = 1000 * 60 * 60;
        long day = 24 * hour;
        long month = 31 * day;
        long[] timeLengths = {hour, day, month};
        String[] apps = {"13065", "13066", "666"};
        Document doc;

        for (String app : apps) {
            for (long timeLenght : timeLengths) {
                for (int i = 0; i < n; i++) {
                    doc = new Document();
                    doc.put("_id", new ObjectId());
                    doc.put("created_at", new Date(System.currentTimeMillis() - timeLenght * randGenerator.nextInt(100) / 100));
                    doc.put("app_id", app);
                    doc.put("messages_per_unit", randGenerator.nextInt(1000));
                    doc.put("presences_per_unit", randGenerator.nextInt(1000));
                    doc.put("connections", new Long(randGenerator.nextInt(1000)));
                    doc.put("unique_connections", randGenerator.nextInt(100));
                    docs.add(doc);
                }
            }
        }
        statistics.insertMany(docs);
    }*/
}
