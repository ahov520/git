package com.antihub.mobile.data.repository

import com.antihub.mobile.data.remote.GitHubApiService
import com.antihub.mobile.domain.model.NotificationItem
import com.antihub.mobile.domain.model.RepoItem
import com.antihub.mobile.domain.model.UserProfile
import com.antihub.mobile.domain.repository.GitHubRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubRepositoryImpl @Inject constructor(
    private val apiService: GitHubApiService,
) : GitHubRepository {

    override suspend fun getViewer(): Result<UserProfile> {
        return runCatching {
            val user = apiService.getViewer()
            UserProfile(
                login = user.login,
                avatarUrl = user.avatarUrl,
                bio = user.bio,
            )
        }
    }

    override suspend fun getRepositories(page: Int, perPage: Int): Result<List<RepoItem>> {
        return runCatching {
            apiService.getRepositories(page = page, perPage = perPage)
                .map { repo ->
                    RepoItem(
                        id = repo.id,
                        name = repo.name,
                        fullName = repo.fullName,
                        description = repo.description,
                        stargazersCount = repo.stargazersCount,
                        ownerLogin = repo.owner.login,
                        isPrivate = repo.isPrivate,
                    )
                }
        }
    }

    override suspend fun getNotifications(all: Boolean, page: Int): Result<List<NotificationItem>> {
        return runCatching {
            apiService.getNotifications(all = all, page = page, perPage = 30)
                .map { item ->
                    NotificationItem(
                        id = item.id,
                        title = item.subject.title,
                        reason = item.reason,
                        repositoryFullName = item.repository.fullName,
                        unread = item.unread,
                        updatedAt = item.updatedAt,
                    )
                }
        }
    }

    override suspend fun searchRepositories(query: String, page: Int): Result<List<RepoItem>> {
        return runCatching {
            apiService.searchRepositories(query = query, page = page, perPage = 30)
                .items
                .map { repo ->
                    RepoItem(
                        id = repo.id,
                        name = repo.name,
                        fullName = repo.fullName,
                        description = repo.description,
                        stargazersCount = repo.stargazersCount,
                        ownerLogin = repo.owner.login,
                        isPrivate = repo.isPrivate,
                    )
                }
        }
    }
}
