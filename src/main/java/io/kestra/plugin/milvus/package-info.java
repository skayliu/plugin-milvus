@PluginSubGroup(
    title = "Milvus plugin",
    description = """
            This sub-group of plugins contains tasks for using Milvus database.
            Milvus is one of the world’s leading open-source vector database projects. \
            Zilliz adopts the name Milvus for its open-source high-performance, \
            highly scalable vector database that runs efficiently across a wide range of environments, \
            from a laptop to large-scale distributed systems. \
            It is available as both open-source software and a cloud service.
        """,
    categories = PluginSubGroup.PluginCategory.DATABASE
)
package io.kestra.plugin.milvus;

import io.kestra.core.models.annotations.PluginSubGroup;