package com.syntey.spiriteddriving.feature.injection

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module
@ComponentScan("com.syntey.spiriteddriving.feature")
@Configuration
class FeatureModule
