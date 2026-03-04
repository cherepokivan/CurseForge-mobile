package com.curseforge.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val results: List<AddonDto>
)

@Serializable
data class AddonDto(
    val id: Long,
    val name: String,
    val summary: String,
    val author: String,
    val latestFileId: Long,
    val latestFileName: String,
    val downloadUrl: String? = null
)

@Serializable
data class FileInfoResponse(
    val fileId: Long,
    val fileName: String,
    val downloadUrl: String
)

data class Addon(
    val id: Long,
    val name: String,
    val description: String,
    val author: String,
    val latestFileId: Long,
    val latestFileName: String,
    val downloadUrl: String?
)

fun AddonDto.toDomain() = Addon(
    id = id,
    name = name,
    description = summary,
    author = author,
    latestFileId = latestFileId,
    latestFileName = latestFileName,
    downloadUrl = downloadUrl
)
