package io.kestra.plugin.milvus;

import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.property.Property;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public interface MilvusConnectionInterface {
  @Schema(
      title = "The URI of the Milvus instance",
      description =
          " For Example: http://localhost:19530 or https://cluster-id.serverless.cluster-region.cloud.zilliz.com")
  @NotBlank
  @PluginProperty(dynamic = true)
  String getUrl();

  @Schema(
      title = "A valid access token to access the specified Milvus instance.",
      description =
          """
              This can be used as a recommended alternative to setting user and password separately.
              When setting this field, notice that:
              A valid token should be a pair of username and password used to access the target cluster, \
              joined by a colon (:). For example, you can set this to `root:Milvus`, \
              which is the default credential of the root user.
              Use this if authentication has been enabled on the target Milvus instance. \
              To enable authentication, refer to Authenticate User Access.
          """)
  Property<String> getToken();

  @Schema(
      title = "A valid username used to connect to the specified Milvus instance.",
      description =
          """
              Use this if authentication has been enabled on the target Milvus instance. \
              To enable authentication, refer to Authenticate User Access.
              This should be used along with password.
          """)
  Property<String> getUserName();

  @Schema(
      title = "A valid password used to connect to the specified Milvus instance.",
      description =
          """
              Use this if authentication has been enabled on the target Milvus instance. \
              To enable authentication, refer to Authenticate User Access.
              This should be used along with user.
          """)
  Property<String> getPassword();

  @Schema(title = "The name of the database to which the target Milvus instance belongs.")
  Property<String> getDbName();
}
