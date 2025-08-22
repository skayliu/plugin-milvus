package io.kestra.plugin.milvus.collection;

import static org.hamcrest.Matchers.*;

import io.kestra.core.runners.RunContextFactory;
import io.kestra.plugin.milvus.MilvusConnectionTest;
import io.kestra.plugin.milvus.database.*;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.database.request.CreateDatabaseReq;
import io.milvus.v2.service.database.request.DropDatabaseReq;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

public class CollectionTest extends MilvusConnectionTest {

  @Inject private RunContextFactory runContextFactory;

  @Test
  public void testCreateCollection() throws Exception {
    client()
        .createCollection(
            CreateCollectionReq.builder()
                .databaseName(DB_NAME)
                .collectionName(COLLECTION_NAME)
                .build());
  }

  @Test
  public void testCreateCollection2() throws Exception {
    client()
        .createCollection(
            CreateCollectionReq.builder()
                .databaseName("testdb")
                .collectionName("test_collection")
                .dimension(2)
                .build());
  }

  @Test
  public void testCreateCollection3() throws Exception {
    String collectionName = "java_sdk_example_simple_v3";
    client()
        .createCollection(
            CreateCollectionReq.builder()
                .databaseName("testdb")
                .collectionName(collectionName)
                .dimension(2)
                .build());
  }

  @Test
  public void testCreateCollection4() throws Exception {
    String collectionName = "java_sdk_example_simple_v3";
    String CLUSTER_ENDPOINT =
        "https://in03-698dd56f7c168b8.serverless.aws-eu-central-1.cloud.zilliz.com";
    String TOKEN =
        "a9ffde1eaf99477d1e2f967302b21e16c5bc62970aebbe4b0efe3aea7ea27337b980e80575310ff9dc33549a25c30265bbd9d549";

    // 1. Connect to Milvus server
    ConnectConfig connectConfig =
        ConnectConfig.builder().uri(CLUSTER_ENDPOINT).token(TOKEN).build();

    MilvusClientV2 milvusClientV2 = new MilvusClientV2(connectConfig);
    milvusClientV2.createCollection(
        CreateCollectionReq.builder()
            // .databaseName("default")
            .collectionName(collectionName)
            .dimension(2)
            .build());
  }

  @Test
  public void testDropCollection() throws Exception {
    client()
        .dropCollection(
            DropCollectionReq.builder()
                .databaseName(DB_NAME)
                .collectionName(COLLECTION_NAME)
                .build());
  }

  @Test
  public void testDropCollection2() throws Exception {
    client()
        .dropCollection(
            DropCollectionReq.builder()
                .databaseName("testdb")
                .collectionName("java_sdk_example_simple_v3")
                .build());
  }

  @Test
  public void testCreateDatabase() throws Exception {
    client().createDatabase(CreateDatabaseReq.builder().databaseName(DB_NAME).build());
  }

  @Test
  public void testDropDatabase() throws Exception {
    client().dropDatabase(DropDatabaseReq.builder().databaseName(DB_NAME).build());
  }
}
