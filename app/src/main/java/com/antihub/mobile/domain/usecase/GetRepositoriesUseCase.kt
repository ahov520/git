package com.antihub.mobile.domain.usecase

import com.antihub.mobile.domain.repository.GitHubRepository
import javax.inject.Inject

class GetRepositoriesUseCase @Inject constructor(
    private val gitHubRepository: GitHubRepository,
) {
    suspend operator fun invoke(page: Int = 1) = gitHubRepository.getRepositories(page = page)
}
