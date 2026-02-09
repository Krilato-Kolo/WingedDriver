package com.krilatokolo.wingeddriver.common

import kotlinx.coroutines.flow.Flow

interface ActivityStartedRepository {
   val activityStarted: Flow<Boolean>
}
