package org.kafka.domain.observers

import com.kafka.data.entities.User
import com.kafka.data.feature.item.auth.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.kafka.base.AppCoroutineDispatchers
import org.kafka.base.domain.SubjectInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveUser @Inject constructor(
    private val accountRepository: AccountRepository,
    private val dispatchers: AppCoroutineDispatchers
) : SubjectInteractor<Unit, User?>() {

    override fun createObservable(params: Unit): Flow<User?> {
        return accountRepository.observeCurrentUser()
            .map { if (it?.anonymous == true) null else it }
            .flowOn(dispatchers.io)
    }
}
