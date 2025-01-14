package dev.marcosalmeida.restdocs.apispec.postman

import dev.marcosalmeida.restdocs.apispec.model.HeaderDescriptor
import dev.marcosalmeida.restdocs.apispec.model.ResourceModel
import dev.marcosalmeida.restdocs.apispec.model.groupByPath
import dev.marcosalmeida.restdocs.apispec.postman.model.model.Body
import dev.marcosalmeida.restdocs.apispec.postman.model.model.Collection
import dev.marcosalmeida.restdocs.apispec.postman.model.model.Header
import dev.marcosalmeida.restdocs.apispec.postman.model.model.Info
import dev.marcosalmeida.restdocs.apispec.postman.model.model.Item
import dev.marcosalmeida.restdocs.apispec.postman.model.model.Query
import dev.marcosalmeida.restdocs.apispec.postman.model.model.Request
import dev.marcosalmeida.restdocs.apispec.postman.model.model.Response
import dev.marcosalmeida.restdocs.apispec.postman.model.model.Src
import dev.marcosalmeida.restdocs.apispec.postman.model.model.Variable
import java.net.URL

object PostmanCollectionGenerator {

    fun generate(
        resources: List<ResourceModel>,
        title: String = "API",
        version: String = "1.0.0",
        baseUrl: String = "http://localhost"
    ): Collection {
        return Collection().apply {
            info = Info().apply {
                this.name = title
                this.version = version
                this.schema = "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
            }
            item = collectItems(resources, baseUrl)
        }
    }

    private fun collectItems(
        resourceModels: List<ResourceModel>,
        url: String
    ): List<Item> {
        return resourceModels.groupByPath().values
            .flatMap { it.groupBy { models -> models.request.method }.values }
            .map { modelsWithSamePathAndMethod ->
                val firstModel = modelsWithSamePathAndMethod.first()
                Item().apply {
                    id = firstModel.operationId
                    name = firstModel.request.path
                    description = firstModel.description
                    request = toRequest(modelsWithSamePathAndMethod, url)
                    response = modelsWithSamePathAndMethod.map {
                        Response().apply {
                            id = it.operationId
                            name = it.operationId
                            originalRequest = toRequest(listOf(it), url)
                            code = it.response.status
                            body = it.response.example
                            header = it.response.headers.toItemHeader(it.response.contentType)
                                .ifEmpty { null }
                        }
                    }
                }
            }
    }

    private fun toRequest(modelsWithSamePathAndMethod: List<ResourceModel>, url: String): Request {
        val firstModel = modelsWithSamePathAndMethod.first()
        return Request().apply {
            method = firstModel.request.method
            this.url = toUrl(modelsWithSamePathAndMethod, url)
            body = firstModel.request.example?.let {
                Body().apply {
                    raw = it
                    mode = Body.Mode.RAW
                }
            }
            header = modelsWithSamePathAndMethod
                .flatMap { it.request.headers }
                .distinctBy { it.name }
                .toItemHeader(modelsWithSamePathAndMethod.map { it.request.contentType }.firstOrNull())
                .ifEmpty { null }
        }
    }

    private fun toUrl(modelsWithSamePathAndMethod: List<ResourceModel>, url: String): Url {
        val urlStartWithVariable = url.startsWith("{{")
        val baseUrl = when (urlStartWithVariable) {
            true -> URL("http://$url")
            else -> URL(url)
        }

        return Url().apply {
            protocol = when (urlStartWithVariable) {
                true -> null
                else -> baseUrl.protocol
            }
            host = baseUrl.host
            port = when (baseUrl.port) {
                -1 -> null
                else -> baseUrl.port.toString()
            }
            path = baseUrl.path + modelsWithSamePathAndMethod.first().request.path.replace(Regex("(?<!\\{)\\{([^}]+)\\}(?!\\})")) {
                it.value.replace('{', ':').removeSuffix("}")
            }
            variable = modelsWithSamePathAndMethod
                .flatMap { it.request.pathParameters }
                .distinctBy { it.name }
                .map {
                    Variable().apply {
                        key = it.name
                        description = it.description
                    }
                }
                .ifEmpty { null }
            query = modelsWithSamePathAndMethod
                .flatMap { it.request.requestParameters }
                .distinctBy { it.name }
                .map {
                    Query().apply {
                        key = it.name
                        description = it.description
                    }
                }
                .ifEmpty { null }
        }
    }

    private fun List<HeaderDescriptor>.toItemHeader(contentType: String?): List<Header> {
        return this.map {
            Header().apply {
                key = it.name
                value = it.example
                description = it.description
            }
        }.let {
            if (contentType != null && this.none { h -> h.name.equals("Content-Type", ignoreCase = true) })
                it + Header().apply {
                    key = "Content-Type"
                    value = contentType
                }
            else it
        }
    }
}
typealias Url = Src
