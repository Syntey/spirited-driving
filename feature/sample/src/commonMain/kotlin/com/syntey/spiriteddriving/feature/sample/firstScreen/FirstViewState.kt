package com.syntey.spiriteddriving.feature.sample.firstScreen

import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

data class FirstViewState(val createdAt: StringDesc = "".desc(), val counter: StringDesc = "".desc(), val randomPerson: StringDesc? = null)
