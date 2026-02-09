package com.krilatokolo.wingeddriver

import com.krilatokolo.wingeddriver.common.ActivityStartedRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow

@Inject
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class ActivityStartedRepositoryImpl : ActivityStartedRepository {
   override val activityStarted = MutableStateFlow<Boolean>(false)
}
