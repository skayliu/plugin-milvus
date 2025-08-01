package io.kestra.plugin.milvus.database;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.milvus.MilvusConnection;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.database.response.ListDatabasesResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@ToString
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@Schema(title = "This operation lists all the database names.")
@Plugin(
    examples = {
      @Example(
          title = "Send databases list request to a Milvus instance.",
          full = true,
          code =
              """
                id: list_milvus_database
                namespace: company.team

                tasks:
                  - id: databases_list
                    type: io.kestra.plugin.milvus.database.ListDatabases
                    url: "http://localhost:19530"
              """),
      @Example(
          title = "Send databases list request to a Milvus clusters of Zilliz Cloud.",
          full = true,
          code =
              """
                id: list_milvus_cloud_database
                namespace: company.team

                tasks:
                  - id: databases_list
                    type: io.kestra.plugin.milvus.database.ListDatabases
                    url: "https://cluster-id.serverless.cluster-region.cloud.zilliz.com"
                    token: "{{ secret('MILIVUS_API_KEY') }}"
              """)
    })
public class ListDatabases extends MilvusConnection implements RunnableTask<ListDatabases.Output> {

  @Override
  public Output run(RunContext runContext) throws Exception {
    MilvusClientV2 client = connect(runContext);

    ListDatabasesResp listDatabasesResp = client.listDatabases();
    java.util.List<String> dbNames = listDatabasesResp.getDatabaseNames();

    runContext.logger().info("Database {} is being listed.", dbNames);

    return Output.builder().dbNames(dbNames).build();
  }

  @Getter
  @Builder
  public static class Output implements io.kestra.core.models.tasks.Output {

    @Schema(title = "Output a list of all database names.")
    private java.util.List<String> dbNames;
  }
}
