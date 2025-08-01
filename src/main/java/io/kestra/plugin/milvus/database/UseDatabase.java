package io.kestra.plugin.milvus.database;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.milvus.MilvusConnection;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.database.request.DescribeDatabaseReq;
import io.milvus.v2.service.database.response.DescribeDatabaseResp;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@ToString
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@Schema(title = "This operation changes the database in use.")
@Plugin(
    examples = {
      @Example(
          title = "Send database change request to a Milvus instance.",
          full = true,
          code =
              """
                id: use_milvus_database
                namespace: company.team

                inputs:
                  - id: database_name
                    type: STRING

                tasks:
                  - id: database_use
                    type: io.kestra.plugin.milvus.database.UseDatabase
                    url: "http://localhost:19530"
                    databaseName: "{{ inputs.database_name }}"
              """),
      @Example(
          title = "Send database change request to a Milvus clusters of Zilliz Cloud.",
          full = true,
          code =
              """
                id: use_milvus_cloud_database
                namespace: company.team

                inputs:
                  - id: database_name
                    type: STRING

                tasks:
                  - id: database_use
                    type: io.kestra.plugin.milvus.database.UseDatabase
                    url: "https://cluster-id.serverless.cluster-region.cloud.zilliz.com"
                    token: "{{ secret('MILIVUS_API_KEY') }}"
                    databaseName: "{{ inputs.database_name }}"
              """)
    })
public class UseDatabase extends MilvusConnection implements RunnableTask<UseDatabase.Output> {

  @Schema(title = "The name of the target database.")
  @PluginProperty(dynamic = true)
  @NotBlank
  private String databaseName;

  @Override
  public Output run(RunContext runContext) throws Exception {
    MilvusClientV2 client = connect(runContext);
    try {
      String renderedDbName = runContext.render(databaseName);

      DescribeDatabaseResp descDBResp =
          client.describeDatabase(
              DescribeDatabaseReq.builder().databaseName(renderedDbName).build());

      runContext.logger().info("Database {} is being used.", descDBResp.getDatabaseName());

      client.useDatabase(renderedDbName);

      return Output.builder().databaseName(renderedDbName).success(true).build();
    } finally {
      client.close();
    }
  }

  @Getter
  @Builder
  public static class Output implements io.kestra.core.models.tasks.Output {

    @Schema(title = "Indicates whether the database used was successful.")
    private Boolean success;

    @Schema(title = "Output the name of the database.")
    private String databaseName;
  }
}
