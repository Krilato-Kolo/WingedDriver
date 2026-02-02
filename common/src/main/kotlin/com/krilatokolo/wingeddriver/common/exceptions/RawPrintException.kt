package com.krilatokolo.wingeddriver.common.exceptions

import si.inova.kotlinova.core.outcome.CauseException

class RawPrintException(
   message: String? = null,
) : CauseException(message, isProgrammersFault = false)
