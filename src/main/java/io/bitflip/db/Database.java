package io.bitflip.db;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import io.bitflip.util.Console;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Getter;

public class Database {
    
    public static final DBObject SORT_CREATED_DESC = new BasicDBObject().append("created", 1);
    
    @Getter private boolean ready;
    private MongoClient mongo;
    private DB mongoDb;

    public Database() {
    }
    
    public boolean init() {
        Console.log(this, "Connecting to database...");
        
        try {
            mongo = new MongoClient();
            mongoDb = mongo.getDB("btc");
        } catch (Exception ex) {
            Console.log(this, "Unable to initialize database: " + ex);
            return false;
        }
        
        Console.log(this, "Database initialized!"); 
        return (ready = true);
    }
    
    public List<DBObject> find(String collection, DBObject query) {
        if (!ready) {
            Console.log(this, "Database not initialized; cannot execute query!");
            return null;
        }
        
        DBCollection mongoCollection = mongoDb.getCollection(collection);
        
        DBCursor cursor = mongoCollection.find(query);
        List<DBObject> cursorObjects = new LinkedList<>();
        
        while (cursor.hasNext()) {
            cursorObjects.add((DBObject)cursor.next());
        }
        
        return cursorObjects;
    }
    
    public DBObject findOne(String collection, DBObject query) {
        if (!ready) {
            Console.log(this, "Database not initialized; cannot execute query!");
            return null;
        }
        
        DBCollection mongoCollection = mongoDb.getCollection(collection);
        
        return mongoCollection.findOne(query);
    }
    
    public DBObject findOneLast(String collection, DBObject query) {
        if (!ready) {
            Console.log(this, "Database not initialized; cannot execute query!");
            return null;
        }
        
        DBCollection mongoCollection = mongoDb.getCollection(collection);
        
        DBCursor cursor = mongoCollection.find(query);
        cursor.sort(Database.SORT_CREATED_DESC);
        cursor.limit(1);
        
        return cursor.next();
    }
    
    public void update(String collection, List<DBObject> objects) {
        if (!ready) {
            Console.log(this, "Database not initialized; cannot execute query!");
            return;
        }
        
        DBCollection mongoCollection = mongoDb.getCollection(collection);
        
        DBObject queryObject;
        for (DBObject obj : objects) {
            queryObject = new BasicDBObject();
            queryObject.put("id", obj.get("id"));
            
            mongoCollection.update(queryObject, obj, false, false, com.mongodb.WriteConcern.SAFE);
        }
    }
    
    public static DBObject mapToDBObject(Map<String, Object> query) {
        DBObject queryObject = new BasicDBObject();
        
        for (String key : query.keySet()) {
            queryObject.put(key, query.get(key));
        }
        
        return queryObject;
    }
    
}
