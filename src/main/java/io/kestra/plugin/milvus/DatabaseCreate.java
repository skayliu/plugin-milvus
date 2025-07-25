package io.kestra.plugin.milvus;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.database.request.CreateDatabaseReq;
import io.milvus.v2.service.database.request.DescribeDatabaseReq;
import io.milvus.v2.service.database.response.DescribeDatabaseResp;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@ToString
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@Schema(
    title = "This operation creates a database with a name and properties in a Milvus instance.")
@Plugin(
    examples = {
      @Example(
          title = "Send database creation request to a Milvus instance.",
          full = true,
          code =
              """
                id: create_milvus_database
                namespace: company.team

                inputs:
                  - id: database_name
                    type: STRING

                tasks:
                  - id: database_create
                    type: io.kestra.plugin.milvus.DatabaseCreate
                    url: "http://localhost:19530"
                    databaseName: "{{ inputs.database_name }}"
              """),
      @Example(
          title = "Send database creation request to a Milvus Dedicated clusters of Zilliz Cloud.",
          full = true,
          code =
              """
                id: create_milvus_cloud_database
                namespace: company.team

                inputs:
                  - id: database_name
                    type: STRING

                tasks:
                  - id: database_create
                    type: io.kestra.plugin.milvus.DatabaseCreate
                    url: "https://cluster-id.serverless.cluster-region.cloud.zilliz.com"
                    token: "{{ secret('MILIVUS_API_KEY') }}"
                    databaseName: "{{ inputs.database_name }}"
              """)
    })
public class DatabaseCreate extends MilvusConnection
    implements RunnableTask<DatabaseCreate.Output> {

  @Schema(title = "The name of the database to create.")
  @PluginProperty(dynamic = true)
  @NotBlank
  private String databaseName;

  @Schema(
      title =
          "The properties of the database, such as \"database.replica.number\", \"database.resource_groups\".")
  private io.kestra.core.models.property.Property<Map<String, String>> databaseProperties;

  @Override
  public Output run(RunContext runContext) throws Exception {
    MilvusClientV2 client = connect(runContext);

    String renderedDatabaseName = runContext.render(databaseName);

    Map<String, String> properties =
        runContext.render(databaseProperties).asMap(String.class, String.class);

    CreateDatabaseReq createDatabaseReq =
        CreateDatabaseReq.builder()
            .databaseName(renderedDatabaseName)
            .properties(properties)
            .build();

    client.createDatabase(createDatabaseReq);

    DescribeDatabaseResp descResp =
        client.describeDatabase(
            DescribeDatabaseReq.builder().databaseName(renderedDatabaseName).build());

    if (descResp.getDatabaseName().equals(renderedDatabaseName)) {
      runContext.logger().info("Database {} was created successfully.", renderedDatabaseName);
      return Output.builder().success(true).build();
    } else {
      runContext.logger().error("Database {} was create failed.", renderedDatabaseName);
      return Output.builder().success(false).build();
    }
  }

  @Getter
  @Builder
  public static class Output implements io.kestra.core.models.tasks.Output {

    @Schema(title = "Indicates whether the database creation was successful.")
    private Boolean success;
  }
}
