package io.kestra.plugin.milvus;

import io.kestra.core.junit.annotations.KestraTest;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.database.request.DropDatabaseReq;
import org.junit.jupiter.api.AfterEach;

@KestraTest
public abstract class MilvusTest {
  protected static final String URL = "http://localhost:19530";
  protected static final String DB_NAME = "kestra_test_db";

  protected MilvusClientV2 client() {
    return new MilvusClientV2(ConnectConfig.builder().uri(URL).build());
  }

  @AfterEach
  public void cleanAll() {
    client().dropDatabase(DropDatabaseReq.builder().databaseName(DB_NAME).build());
  }
}
