package com.krilatokolo.wingeddriver.detekt

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetProvider

class CustomRuleProvider : RuleSetProvider {
   override val ruleSetId = RuleSet.Id("custom")

   override fun instance(): RuleSet {
      return RuleSet(
         ruleSetId,
         listOf(
            ::UseActionLoggerInViewModels
         )
      )
   }
}
