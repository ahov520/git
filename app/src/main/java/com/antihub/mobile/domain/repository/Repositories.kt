package com.antihub.mobile.domain.repository

import com.antihub.mobile.domain.model.NotificationItem
import com.antihub.mobile.domain.model.RepoItem
import com.antihub.mobile.domain.model.TranslationResult
import com.antihub.mobile.domain.model.UserProfile

interface GitHubRepository {
    suspend fun getViewer(): Result<UserProfile>
    suspend fun getRepositories(page: Int = 1, perPage: Int = 30): Result<List<RepoItem>>
    suspend fun getNotifications(all: Boolean = false, page: Int = 1): Result<List<NotificationItem>>
    suspend fun searchRepositories(query: String, page: Int = 1): Result<List<RepoItem>>
}

interface TranslationRepository {
    suspend fun translate(
        text: String,
        sourceLang: String? = null,
        targetLang: String = "zh-CN",
    ): Result<TranslationResult>
}
