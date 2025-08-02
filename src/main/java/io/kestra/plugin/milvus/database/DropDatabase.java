package io.kestra.plugin.milvus.database;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.milvus.MilvusConnection;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.database.request.DescribeDatabaseReq;
import io.milvus.v2.service.database.request.DropDatabaseReq;
import io.milvus.v2.service.database.response.DescribeDatabaseResp;
import io.milvus.v2.service.database.response.ListDatabasesResp;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@ToString
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@Schema(
    title = "This operation drops a database with a name in a Milvus instance.",
    description =
        """
            Once a database is no longer needed, you can drop the database. Note that:
                Default databases cannot be dropped.
                Before dropping a database, you need to drop all collections in the database first.
        """)
@Plugin(
    examples = {
      @Example(
          title = "Send database drop request to a Milvus instance.",
          full = true,
          code =
              """
                id: drop_milvus_database
                namespace: company.team

                inputs:
                  - id: database_name
                    type: STRING

                tasks:
                  - id: database_drop
                    type: io.kestra.plugin.milvus.database.DropDatabase
                    url: "http://localhost:19530"
                    databaseName: "{{ inputs.database_name }}"
              """),
      @Example(
          title = "Send database drop request to a Milvus Dedicated clusters of Zilliz Cloud.",
          full = true,
          code =
              """
                id: drop_milvus_cloud_database
                namespace: company.team

                inputs:
                  - id: database_name
                    type: STRING

                tasks:
                  - id: database_drop
                    type: io.kestra.plugin.milvus.database.DropDatabase
                    url: "https://cluster-id.serverless.cluster-region.cloud.zilliz.com"
                    token: "{{ secret('MILIVUS_API_KEY') }}"
                    databaseName: "{{ inputs.database_name }}"
              """)
    })
public class DropDatabase extends MilvusConnection implements RunnableTask<DropDatabase.Output> {

  @Schema(title = "The name of the database to drop.")
  @PluginProperty(dynamic = true)
  @NotBlank
  private String databaseName;

  @Override
  public Output run(RunContext runContext) throws Exception {
    MilvusClientV2 client = connect(runContext);

    String renderedDatabaseName = runContext.render(databaseName);

    DescribeDatabaseResp descDBResp =
        client.describeDatabase(
            DescribeDatabaseReq.builder().databaseName(renderedDatabaseName).build());

    runContext.logger().info("Database {} is being dropped.", descDBResp.getDatabaseName());

    DropDatabaseReq dropDatabaseReq =
        DropDatabaseReq.builder().databaseName(renderedDatabaseName).build();

    client.dropDatabase(dropDatabaseReq);

    ListDatabasesResp listDatabasesResp = client.listDatabases();
    List<String> dbNames = listDatabasesResp.getDatabaseNames();

    boolean result =
        descDBResp.getDatabaseName().equals(renderedDatabaseName)
            && !dbNames.contains(renderedDatabaseName);

    runContext
        .logger()
        .info(
            "Database {} has {} been dropped.", descDBResp.getDatabaseName(), result ? "" : "not");

    return Output.builder().success(result).build();
  }

  @Getter
  @Builder
  public static class Output implements io.kestra.core.models.tasks.Output {

    @Schema(title = "Indicates whether the database drop was successful.")
    private Boolean success;
  }
}
