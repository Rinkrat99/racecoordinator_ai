package com.antigravity.repository;

import com.antigravity.context.DatabaseContext;
import com.antigravity.models.Model;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;

public class MongoRepository<T extends Model> {

  private final DatabaseContext databaseContext;
  private final String collectionName;
  private final Class<T> clazz;

  public MongoRepository(DatabaseContext databaseContext, String collectionName, Class<T> clazz) {
    this.databaseContext = databaseContext;
    this.collectionName = collectionName;
    this.clazz = clazz;
  }

  public MongoCollection<T> getCollection() {
    return databaseContext.getDatabase().getCollection(collectionName, clazz);
  }

  public List<T> findAll() {
    List<T> list = new ArrayList<>();
    getCollection().find().forEach(list::add);
    return list;
  }

  public T findByEntityId(String id) {
    return getCollection().find(Filters.eq("entity_id", id)).first();
  }

  public T findOne(Bson filter) {
    return getCollection().find(filter).first();
  }

  public List<T> find(Bson filter) {
    List<T> list = new ArrayList<>();
    getCollection().find(filter).forEach(list::add);
    return list;
  }

  public void insert(T entity) {
    getCollection().insertOne(entity);
  }

  public UpdateResult replace(String id, T entity) {
    return getCollection().replaceOne(Filters.eq("entity_id", id), entity);
  }

  public DeleteResult delete(String id) {
    return getCollection().deleteOne(Filters.eq("entity_id", id));
  }

  public String getNextSequence() {
    MongoCollection<Document> counters = databaseContext.getDatabase().getCollection("counters");
    Document counter =
        counters.findOneAndUpdate(
            Filters.eq("_id", collectionName),
            Updates.inc("seq", 1),
            new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER));
    return String.valueOf(counter.getInteger("seq"));
  }
}
