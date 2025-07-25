package io.kestra.plugin.milvus;

import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.runners.RunContext;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.exception.MilvusClientException;
import javax.naming.AuthenticationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
public abstract class MilvusConnection extends Task implements MilvusConnectionInterface {
  private String url;
  private Property<String> token;
  private Property<String> userName;
  private Property<String> password;
  private Property<String> dbName;

  protected MilvusClientV2 connect(RunContext runContext)
      throws MilvusClientException, AuthenticationException, IllegalVariableEvaluationException {

    String renderedUrl = runContext.render(url);
    String renderedToken = runContext.render(token).as(String.class).orElse(null);
    String renderedUserName = runContext.render(userName).as(String.class).orElse(null);
    String renderedPassword = runContext.render(password).as(String.class).orElse(null);
    String renderedDbName = runContext.render(dbName).as(String.class).orElse(null);

    ConnectConfig.ConnectConfigBuilder builder = ConnectConfig.builder();

    builder.uri(renderedUrl);

    if (token != null) {
      builder.token(renderedToken);
    }
    if (userName != null && password != null) {
      builder.username(renderedUserName);
      builder.password(renderedPassword);
    }
    if (dbName != null) {
      builder.dbName(renderedDbName);
    }

    ConnectConfig config = builder.build();

    try {
      return new MilvusClientV2(config);
    } catch (Exception e) {
      throw new AuthenticationException(
          "Unable to connect to Milvus with the provided configuration, please check your configuration.");
    }
  }
}
