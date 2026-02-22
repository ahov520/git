package com.antihub.mobile.domain.model

data class UserProfile(
    val login: String,
    val avatarUrl: String?,
    val bio: String?,
)

data class RepoItem(
    val id: Long,
    val name: String,
    val fullName: String,
    val description: String?,
    val stargazersCount: Int,
    val ownerLogin: String,
    val isPrivate: Boolean,
)

data class NotificationItem(
    val id: String,
    val title: String,
    val reason: String,
    val repositoryFullName: String,
    val unread: Boolean,
    val updatedAt: String,
)

data class TranslationResult(
    val translatedText: String,
    val detectedSourceLang: String?,
    val provider: String,
)
