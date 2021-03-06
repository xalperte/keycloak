package org.keycloak.events.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

import com.mongodb.DBObject;
import org.keycloak.events.Event;
import org.keycloak.events.EventQuery;
import org.keycloak.events.EventType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class MongoEventQuery implements EventQuery {

    private Integer firstResult;
    private Integer maxResults;
    private DBCollection audit;
    private final BasicDBObject query;

    public MongoEventQuery(DBCollection audit) {
        this.audit = audit;
        query = new BasicDBObject();
    }

    @Override
    public EventQuery type(EventType... types) {
        List<String> eventStrings = new LinkedList<String>();
        for (EventType e : types) {
            eventStrings.add(e.toString());
        }
        query.put("type", new BasicDBObject("$in", eventStrings));
        return this;
    }

    @Override
    public EventQuery realm(String realmId) {
        query.put("realmId", realmId);
        return this;
    }

    @Override
    public EventQuery client(String clientId) {
        query.put("clientId", clientId);
        return this;
    }

    @Override
    public EventQuery user(String userId) {
        query.put("userId", userId);
        return this;
    }

    @Override
    public EventQuery fromDate(String fromDate) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Long from = null;
        try {
            from = df.parse(fromDate).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        BasicDBObject time = query.containsField("time") ? (BasicDBObject) query.get("time") : new BasicDBObject();
        time.append("$gte", from);
        query.put("time", time);
        return this;
    }

    @Override
    public EventQuery toDate(String toDate) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Long to = null;
        try {
            to = df.parse(toDate).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        BasicDBObject time = query.containsField("time") ? (BasicDBObject) query.get("time") : new BasicDBObject();
        time.append("$lte", to);
        query.put("time", time);
        return this;
    }

    @Override
    public EventQuery ipAddress(String ipAddress) {
        query.put("ipAddress", ipAddress);
        return this;
    }

    @Override
    public EventQuery firstResult(int firstResult) {
        this.firstResult = firstResult;
        return this;
    }

    @Override
    public EventQuery maxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    @Override
    public List<Event> getResultList() {
        DBCursor cur = audit.find(query).sort(new BasicDBObject("time", -1));
        if (firstResult != null) {
            cur.skip(firstResult);
        }
        if (maxResults != null) {
            cur.limit(maxResults);
        }

        List<Event> events = new LinkedList<Event>();
        while (cur.hasNext()) {
            events.add(MongoEventStoreProvider.convert((BasicDBObject) cur.next()));
        }

        return events;
    }

}
