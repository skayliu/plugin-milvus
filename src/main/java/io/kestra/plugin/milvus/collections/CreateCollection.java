package io.kestra.plugin.milvus.collections;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.milvus.MilvusConnection;
import io.milvus.param.MetricType;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DescribeCollectionReq;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@ToString
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@Schema(title = "This operation creates a collection either with default or customized settings.")
@Plugin(
    examples = {
      @Example(
          title = "Send collection creation request to a Milvus instance.",
          full = true,
          code =
              """
                  id: create_milvus_collection
                  namespace: company.team

                  inputs:
                    - id: db_name
                      type: STRING
                    - id: collection_name
                      type: STRING
                    - id: dimension
                      type: INT
                      defaults: 2

                  tasks:
                    - id: collection_create
                      type: io.kestra.plugin.milvus.collections.CreateCollection
                      url: "http://localhost:19530"
                      dbName: "{{ inputs.db_name }}"
                      collectionName: "{{ inputs.collection_name }}"
                      dimension: "{{ inputs.dimension }}"
                """),
      @Example(
          title =
              "Send collection creation request to a Milvus Dedicated clusters of Zilliz Cloud.",
          full = true,
          code =
              """
                  id: create_milvus_cloud_collection
                  namespace: company.team

                  inputs:
                    - id: collection_name
                      type: STRING
                    - id: dimension
                      type: INT
                      defaults: 2

                  tasks:
                    - id: collection_create
                      type: io.kestra.plugin.milvus.collections.CreateCollection
                      url: "https://cluster-id.serverless.cluster-region.cloud.zilliz.com"
                      token: "{{ secret('MILIVUS_API_KEY') }}"
                      collectionName: "{{ inputs.collection_name }}"
                      dimension: "{{ inputs.dimension }}"
                """)
    })
public class CreateCollection extends MilvusConnection
    implements RunnableTask<CreateCollection.Output> {

  @Schema(title = "The name of the collection to create.")
  @PluginProperty(dynamic = true)
  @NotBlank
  private String collectionName;

  @Schema(title = "Description of the collection, default to empty.")
  private Property<String> collectionDescription;

  @Schema(
      title = "The dimensionality of the collection field that holds vector embeddings.",
      description =
          """
              The value should be greater than 1 and is usually determined by the model you use to generate vector embeddings.
              This is required to set up a collection with default settings.
              Skip this parameter if you need to set up a collection with a customized schema.
            """)
  private Property<Integer> dimension;

  @Schema(
      title = "The name of the primary field in this collection.",
      description =
          """
              The value defaults to id. You can use another name you see fit.
              Skip this parameter if you need to set up a collection with a customized schema.
            """)
  @Builder.Default
  private Property<String> primaryFieldName = Property.ofValue("id");

  @Schema(
      title = "The data type of the primary field in this collection.",
      description =
          """
              The value defaults to DataType.Int64.
              Skip this parameter if you need to set up a collection with a customized schema.
            """)
  @Builder.Default
  private Property<DataType> idType = Property.ofValue(DataType.Int64);

  @Schema(
      title =
          "The maximum number of characters or elements allowed for string or array fields within the collection.",
      description =
          """
              This parameter is required if primaryFieldType is set to VarChar.
              The value defaults to 65535.
            """)
  private Property<Integer> maxLength;

  @Schema(
      title = "The name of the collection field to hold vector embeddings.",
      description =
          """
              The value defaults to vector. You can use another name you see fit.
              Skip this parameter if you need to set up a collection with a customized schema.
            """)
  private Property<String> vectorFieldName;

  @Schema(
      title =
          "The algorithm used for this collection to measure similarities between vector embeddings.",
      description =
          """
              The value defaults to IP. Possible values are L2, IP, and COSINE. For details on these metric types, refer to Similarity Metrics.
            """)
  @Builder.Default
  private Property<MetricType> metricType = Property.ofValue(MetricType.IP);

  @Schema(
      title =
          "Whether the primary field automatically increments upon data insertions into this collection.",
      description =
          """
              The value defaults to False. Setting this to True makes the primary field automatically increment.
              Skip this parameter if you need to set up a collection with a customized schema.
              The auto-generated IDs have a fixed length and cannot be altered.
            """)
  @Builder.Default
  private Property<Boolean> autoID = Property.ofValue(false);

  @Schema(
      title =
          "Whether to use a reserved JSON field named $meta to store undefined fields and their values in key-value pairs.",
      description =
          """
              The value defaults to True, indicating that the meta field is used.
              If you create a collection with a schema, configure this parameter using the CreateSchema method.
            """)
  @Builder.Default
  private Property<Boolean> enableDynamicField = Property.ofValue(true);

  @Schema(
      title = "The number of shards to create along with the collection.",
      description =
          """
              The value defaults to 1, indicating that one shard is to be created along with this collection.
            """)
  @Builder.Default
  private Property<Integer> numShards = Property.ofValue(1);

  @Schema(
      title = "The schema of this collection.",
      description =
          """
              Leaving it empty indicates this collection will be created with default settings. To set up a collection with a customized schema, you need to create a CollectionSchema object and reference it here.
            """)
  private Property<CreateCollectionReq.CollectionSchema> collectionSchema;

  @Schema(
      title = "The parameters for building the index on the vector field in this collection.",
      description =
          """
              To set up a collection with a customized schema and automatically load the collection to memory, create an IndexParams object with a list of IndexParam objects and reference it here.
              You should at least add an index for the vector field in this collection. You can also skip this parameter if you prefer to set up the index parameters later on.
            """)
  private Property<List<IndexParam>> indexParams;

  @Schema(
      title = "The number of partitions.",
      description =
          """
              Used when isPartitionKey is set to true in Field Schema. Default is 64.
            """)
  private Property<Integer> numPartitions;

  @Schema(
      title = "The consistency level of the collection.",
      description =
          """
              This applies to searches and queries within the collection if the search or query request lacks consistency.
            """)
  private Property<ConsistencyLevel> consistencyLevel;

  @Schema(title = "Extra collection properties in a hash map.")
  private Property<Map<String, String>> properties;

  @Override
  public Output run(RunContext runContext) throws Exception {
    MilvusClientV2 client = connect(runContext);

    String renderedCollectionName = runContext.render(collectionName);

    String renderedDescription =
        runContext.render(collectionDescription).as(String.class).orElse("");

    Integer renderedDimension = runContext.render(dimension).as(Integer.class).orElse(2);

    String renderedPrimaryFieldName =
        runContext.render(primaryFieldName).as(String.class).orElse("id");

    DataType renderedIdType = runContext.render(idType).as(DataType.class).orElse(DataType.Int64);

    Integer renderedMaxLength = runContext.render(maxLength).as(Integer.class).orElse(65535);

    Map<String, String> renderedProperties =
        runContext.render(properties).asMap(String.class, String.class);

    CreateCollectionReq.CreateCollectionReqBuilder<?, ?> builder = CreateCollectionReq.builder();
    builder.collectionName(renderedCollectionName);

    builder.dimension(renderedDimension);

    builder.description(renderedDescription);
    builder.primaryFieldName(renderedPrimaryFieldName);

    builder.idType(renderedIdType);

    builder.maxLength(renderedMaxLength);

    builder.properties(renderedProperties);

    client.createCollection(builder.build());

    //      CreateCollectionReq createCollectionReq =
    //          CreateCollectionReq.builder()
    //              .collectionName(renderedCollectionName)
    //              .description(renderedDescription)
    //              .dimension(renderedDimension)
    //              .primaryFieldName(renderedPrimaryFieldName)
    //              .idType(renderedIdType)
    //              .maxLength(renderedMaxLength)
    //              .vectorFieldName(vectorFieldName)
    //              .metricType(metricType)
    //              .autoID(autoID)
    //              .enableDynamicField(enableDynamicField)
    //              .numShards(numShards)
    //              .collectionSchema(collectionSchema)
    //              .indexParams(indexParams)
    //              .numPartitions(numPartitions)
    //              .consistencyLevel(consistencyLevel)
    //              .properties(properties)
    //              .build();

    DescribeCollectionResp describeCollectionResp =
        client.describeCollection(
            DescribeCollectionReq.builder().collectionName(renderedCollectionName).build());

    if (describeCollectionResp.getCollectionName().equals(renderedCollectionName)) {
      runContext.logger().info("Collection {} was created successfully.", renderedCollectionName);
      return Output.builder().collectionName(renderedCollectionName).success(true).build();
    } else {
      runContext.logger().error("Collection {} was create failed.", renderedCollectionName);
      return Output.builder().success(false).build();
    }
  }

  @Getter
  @Builder
  public static class Output implements io.kestra.core.models.tasks.Output {

    @Schema(title = "Indicates whether the collection creation was successful.")
    private Boolean success;

    @Schema(title = "The name of the current collection.")
    private String collectionName;
  }
}
