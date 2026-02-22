package com.antihub.mobile.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubUserDto(
    @SerialName("login") val login: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("bio") val bio: String? = null,
)

@Serializable
data class GitHubOwnerDto(
    @SerialName("login") val login: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)

@Serializable
data class GitHubRepoDto(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("description") val description: String? = null,
    @SerialName("private") val isPrivate: Boolean,
    @SerialName("stargazers_count") val stargazersCount: Int,
    @SerialName("owner") val owner: GitHubOwnerDto,
)

@Serializable
data class GitHubRepoSearchResponseDto(
    @SerialName("items") val items: List<GitHubRepoDto>,
)

@Serializable
data class GitHubSubjectDto(
    @SerialName("title") val title: String,
    @SerialName("type") val type: String,
)

@Serializable
data class GitHubRepositoryRefDto(
    @SerialName("full_name") val fullName: String,
)

@Serializable
data class GitHubNotificationDto(
    @SerialName("id") val id: String,
    @SerialName("unread") val unread: Boolean,
    @SerialName("reason") val reason: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("subject") val subject: GitHubSubjectDto,
    @SerialName("repository") val repository: GitHubRepositoryRefDto,
)

@Serializable
data class GitHubIssueDto(
    @SerialName("id") val id: Long,
    @SerialName("number") val number: Int,
    @SerialName("title") val title: String,
    @SerialName("state") val state: String,
    @SerialName("body") val body: String? = null,
)

@Serializable
data class GitHubPullRequestDto(
    @SerialName("id") val id: Long,
    @SerialName("number") val number: Int,
    @SerialName("title") val title: String,
    @SerialName("state") val state: String,
    @SerialName("body") val body: String? = null,
)

@Serializable
data class GitHubCommitDto(
    @SerialName("sha") val sha: String,
    @SerialName("html_url") val htmlUrl: String,
)

@Serializable
data class GitHubContentDto(
    @SerialName("name") val name: String,
    @SerialName("path") val path: String,
    @SerialName("type") val type: String,
    @SerialName("download_url") val downloadUrl: String? = null,
    @SerialName("content") val content: String? = null,
    @SerialName("encoding") val encoding: String? = null,
)
