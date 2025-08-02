package io.kestra.plugin.milvus.database;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.plugin.milvus.MilvusConnectionTest;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.database.request.DescribeDatabaseReq;
import io.milvus.v2.service.database.request.DropDatabaseReq;
import io.milvus.v2.service.database.response.DescribeDatabaseResp;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class DatabaseTest extends MilvusConnectionTest {

  @Inject private RunContextFactory runContextFactory;

  @Test
  public void testCreateDatabase() throws Exception {
    RunContext runContext = runContextFactory.of(Map.of("databaseName", DB_NAME, "url", URL));

    CreateDatabase.Output databaseOutput =
        CreateDatabase.builder().url(URL).databaseName(DB_NAME).build().run(runContext);

    assertThat(databaseOutput.getSuccess(), is(Boolean.TRUE));

    DescribeDatabaseResp describeDatabase =
        client().describeDatabase(DescribeDatabaseReq.builder().databaseName(DB_NAME).build());

    assertThat(describeDatabase.getDatabaseName(), is(DB_NAME));
    assertThat(describeDatabase.getProperties(), is(Collections.emptyMap()));
  }

  @Test
  public void testDescribeDatabase() throws Exception {
    RunContext runContext = runContextFactory.of(Map.of("databaseName", DB_NAME, "url", URL));

    Map<String, String> properties = new HashMap<>();
    properties.put("database.replica.number", "1");

    CreateDatabase.Output databaseOutput =
        CreateDatabase.builder()
            .url(URL)
            .databaseName(DB_NAME)
            .properties(new Property<>(properties))
            .build()
            .run(runContext);

    assertThat(databaseOutput.getSuccess(), is(Boolean.TRUE));

    DescribeDatabase.Output describeDatabase =
        DescribeDatabase.builder().url(URL).databaseName(DB_NAME).build().run(runContext);

    assertThat(describeDatabase.getDatabaseName(), is(DB_NAME));

    assertThat(describeDatabase.getProperties(), is(properties));
  }

  @Test
  public void testListDatabases() throws Exception {
    RunContext runContext = runContextFactory.of(Map.of("databaseName", DB_NAME, "url", URL));

    CreateDatabase.Output databaseOutput =
        CreateDatabase.builder().url(URL).databaseName(DB_NAME).build().run(runContext);

    assertThat(databaseOutput.getSuccess(), is(Boolean.TRUE));

    ListDatabases.Output listOutput = ListDatabases.builder().url(URL).build().run(runContext);

    assertThat(listOutput.getDbNames(), hasItem(DB_NAME));
    assertThat(listOutput.getDbNames().size(), greaterThan(1));
  }

  @Test
  public void tesDropDatabases() throws Exception {
    RunContext runContext = runContextFactory.of(Map.of("databaseName", DB_NAME, "url", URL));

    CreateDatabase.Output databaseOutput =
        CreateDatabase.builder().url(URL).databaseName(DB_NAME).build().run(runContext);

    assertThat(databaseOutput.getSuccess(), is(Boolean.TRUE));

    DropDatabase.Output dropOutput =
        DropDatabase.builder().url(URL).databaseName(DB_NAME).build().run(runContext);

    assertThat(dropOutput.getSuccess(), is(true));
  }

  @Test
  public void testUseDatabase() throws Exception {

    RunContext runContext = runContextFactory.of(Map.of("databaseName", DB_NAME, "url", URL));

    ListDatabases.Output listOutput = ListDatabases.builder().url(URL).build().run(runContext);

    assertThat(DB_NAME, not(in(listOutput.getDbNames())));

    CreateDatabase.Output dbCreateOutput =
        CreateDatabase.builder().url(URL).databaseName(DB_NAME).build().run(runContext);

    assertThat(dbCreateOutput.getSuccess(), is(Boolean.TRUE));

    UseDatabase.Output dbUseOutput =
        UseDatabase.builder().url(URL).databaseName(DB_NAME).build().run(runContext);
    assertThat(dbUseOutput.getSuccess(), is(Boolean.TRUE));

    Map<String, String> properties = new HashMap<>();
    properties.put("database.replica.number", "1");

    AlterDatabaseProperties.Output alterOutput =
        AlterDatabaseProperties.builder()
            .url(URL)
            .properties(new Property<>(properties))
            .databaseName(DB_NAME)
            .build()
            .run(runContext);

    assertThat(alterOutput.getSuccess(), is(Boolean.TRUE));
    assertThat(alterOutput.getProperties(), is(properties));
  }

  @Test
  public void testDropCollection() throws Exception {
    client().dropCollection(DropCollectionReq.builder().collectionName(COLLECTION_NAME).build());
  }

  @Test
  public void testDropDatabase() throws Exception {
    client().dropDatabase(DropDatabaseReq.builder().databaseName(DB_NAME).build());
  }
}
