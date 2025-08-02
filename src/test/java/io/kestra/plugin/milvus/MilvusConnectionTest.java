package io.kestra.plugin.milvus;

import io.kestra.core.junit.annotations.KestraTest;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;

@KestraTest
public abstract class MilvusConnectionTest {
  protected static final String URL = "http://localhost:19530";
  protected static final String DB_NAME = "kestra_test_db";
  protected static final String COLLECTION_NAME = "kestra_test_collection";

  protected MilvusClientV2 client() {
    return new MilvusClientV2(ConnectConfig.builder().uri(URL).build());
  }

//  @AfterEach
//  public void cleanAll() {
//    client().dropDatabase(DropDatabaseReq.builder().databaseName(DB_NAME).build());
//    client().close();
//  }
}
