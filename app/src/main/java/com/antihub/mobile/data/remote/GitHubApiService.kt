package com.antihub.mobile.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GitHubApiService {
    @GET("user")
    suspend fun getViewer(): GitHubUserDto

    @GET("user/repos")
    suspend fun getRepositories(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("sort") sort: String = "updated",
        @Query("direction") direction: String = "desc",
    ): List<GitHubRepoDto>

    @GET("notifications")
    suspend fun getNotifications(
        @Query("all") all: Boolean,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
    ): List<GitHubNotificationDto>

    @GET("search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
    ): GitHubRepoSearchResponseDto

    @GET("repos/{owner}/{repo}/issues")
    suspend fun getIssues(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("state") state: String = "open",
    ): List<GitHubIssueDto>

    @GET("repos/{owner}/{repo}/pulls")
    suspend fun getPullRequests(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("state") state: String = "open",
    ): List<GitHubPullRequestDto>

    @GET("repos/{owner}/{repo}/commits")
    suspend fun getCommits(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
    ): List<GitHubCommitDto>

    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getContent(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
    ): GitHubContentDto
}
