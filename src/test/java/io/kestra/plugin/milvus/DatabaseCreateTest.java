package io.kestra.plugin.milvus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.milvus.v2.service.database.request.DescribeDatabaseReq;
import io.milvus.v2.service.database.response.DescribeDatabaseResp;
import jakarta.inject.Inject;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class DatabaseCreateTest extends MilvusTest {

  @Inject private RunContextFactory runContextFactory;

  @Test
  public void testDatabaseCreate() throws Exception {
    RunContext runContext = runContextFactory.of(Map.of("databaseName", DB_NAME, "url", URL));

    DatabaseCreate.Output databaseOutput =
        DatabaseCreate.builder().url(URL).databaseName(DB_NAME).build().run(runContext);

    assertThat(databaseOutput.getSuccess(), is(Boolean.TRUE));

    DescribeDatabaseResp describeDatabase =
        client().describeDatabase(DescribeDatabaseReq.builder().databaseName(DB_NAME).build());

    assertThat(describeDatabase.getDatabaseName(), is(DB_NAME));
  }
}
