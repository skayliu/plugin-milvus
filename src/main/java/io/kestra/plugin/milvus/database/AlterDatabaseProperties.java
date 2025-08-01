package io.kestra.plugin.milvus.database;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.milvus.MilvusConnection;
import io.milvus.common.utils.JsonUtils;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.database.request.AlterDatabasePropertiesReq;
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
@Schema(title = "This operation alters a database’s properties.")
@Plugin(
    examples = {
      @Example(
          title = "Send database properties alter request to a Milvus instance.",
          full = true,
          code =
              """
                id: alter_milvus_database
                namespace: company.team

                inputs:
                  - id: database_name
                    type: STRING

                tasks:
                  - id: database_alter
                    type: io.kestra.plugin.milvus.database.PropertiesAlter
                    url: "http://localhost:19530"
                    databaseName: "{{ inputs.database_name }}"
                    properties:
                      database.replica.number: "2"
              """),
      @Example(
          title =
              "Send database properties alter request to a Milvus Dedicated clusters of Zilliz Cloud.",
          full = true,
          code =
              """
                id: alter_milvus_cloud_database
                namespace: company.team

                inputs:
                  - id: database_name
                    type: STRING

                tasks:
                  - id: database_alter
                    type: io.kestra.plugin.milvus.database.PropertiesAlter
                    url: "https://cluster-id.serverless.cluster-region.cloud.zilliz.com"
                    token: "{{ secret('MILIVUS_API_KEY') }}"
                    databaseName: "{{ inputs.database_name }}"
                    properties:
                      database.replica.number: "2"
              """)
    })
public class AlterDatabaseProperties extends MilvusConnection
    implements RunnableTask<AlterDatabaseProperties.Output> {

  @Schema(title = "The name of the database to alter.")
  @PluginProperty(dynamic = true)
  @NotBlank
  private String databaseName;

  @Schema(
      title = "The properties of the database, such as replica number, resource groups.",
      description =
          """
              The possible database properties are as follows:
                  database.replica.number - Number of replicas for the database.
                  database.resource_groups - Resource groups dedicated to the database.
                  database.diskQuota.mb - Disk quota allocated to the database in megabytes (MB).
                  database.max.collections - Maximum number of collections allowed in the database.
                  database.force.deny.writing - Whether to deny all write operations in the database.
                  database.force.deny.reading -  Whether to deny all read operations in the database.
          """)
  private Property<Map<String, String>> properties;

  @Override
  public Output run(RunContext runContext) throws Exception {
    MilvusClientV2 client = connect(runContext);
    try {
      String renderedDatabaseName = runContext.render(databaseName);

      Map<String, String> renderedProperties =
          runContext.render(properties).asMap(String.class, String.class);

      runContext
          .logger()
          .info(
              "Database {} is being altered with properties: {}.",
              renderedDatabaseName,
              JsonUtils.toJson(renderedProperties));

      AlterDatabasePropertiesReq alterDatabaseReq =
          AlterDatabasePropertiesReq.builder()
              .databaseName(renderedDatabaseName)
              .properties(renderedProperties)
              .build();
      client.alterDatabaseProperties(alterDatabaseReq);

      DescribeDatabaseResp descDBResp =
          client.describeDatabase(
              DescribeDatabaseReq.builder().databaseName(renderedDatabaseName).build());

      runContext
          .logger()
          .info(
              "Database {} has been altered, the properties are: {}",
              descDBResp.getDatabaseName(),
              descDBResp.getProperties());

      return Output.builder().success(true).properties(descDBResp.getProperties()).build();
    } finally {
      client.close();
    }
  }

  @Getter
  @Builder
  public static class Output implements io.kestra.core.models.tasks.Output {

    @Schema(title = "Indicates whether the database's properties alter was successful.")
    private Boolean success;

    @Schema(
        title = "Output the properties of the database, such as replica number, resource groups.")
    private Map<String, String> properties;
  }
}
