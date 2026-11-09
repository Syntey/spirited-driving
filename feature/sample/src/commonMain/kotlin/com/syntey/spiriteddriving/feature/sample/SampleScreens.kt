package com.syntey.spiriteddriving.feature.sample

import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.feature.sample.firstScreen.FirstComponentFactory
import com.syntey.spiriteddriving.feature.sample.firstScreen.FirstScreen
import com.syntey.spiriteddriving.feature.sample.firstScreen.FirstScreenNavigation
import com.syntey.spiriteddriving.feature.sample.loginScreen.LoginComponentFactory
import com.syntey.spiriteddriving.feature.sample.loginScreen.LoginScreen
import com.syntey.spiriteddriving.feature.sample.loginScreen.LoginScreenNavigation
import com.syntey.spiriteddriving.feature.sample.picker.FruitPickerComponentFactory
import com.syntey.spiriteddriving.feature.sample.picker.PickerArgs
import com.syntey.spiriteddriving.feature.sample.picker.PickerNavigation
import com.syntey.spiriteddriving.feature.sample.picker.PickerScreen
import com.syntey.spiriteddriving.feature.sample.picker.PickerType
import com.syntey.spiriteddriving.feature.sample.picker.VegetablePickerComponentFactory
import com.syntey.spiriteddriving.feature.sample.profileScreen.ProfileComponentFactory
import com.syntey.spiriteddriving.feature.sample.profileScreen.ProfileScreen
import com.syntey.spiriteddriving.feature.sample.profileScreen.ProfileScreenNavigation
import com.syntey.spiriteddriving.feature.sample.secondScreen.SecondComponentFactory
import com.syntey.spiriteddriving.feature.sample.secondScreen.SecondScreen
import com.syntey.spiriteddriving.feature.sample.secondScreen.SecondScreenNavigation
import com.syntey.spiriteddriving.feature.sample.thirdScreen.ThirdComponentFactory
import com.syntey.spiriteddriving.feature.sample.thirdScreen.ThirdScreen
import com.syntey.spiriteddriving.feature.sample.thirdScreen.ThirdScreenArgs
import com.syntey.spiriteddriving.feature.sample.thirdScreen.ThirdScreenNavigation

/**
 * The screens this feature offers to the navigation hosts in `:shared`. Components stay internal, so each screen is
 * created here, behind its public interface.
 */
object SampleScreens {

    fun login(componentContext: AppComponentContext, navigation: LoginScreenNavigation): LoginScreen =
        LoginComponentFactory.createComponent(componentContext, navigation)

    fun first(componentContext: AppComponentContext, navigation: FirstScreenNavigation): FirstScreen =
        FirstComponentFactory.createComponent(componentContext, navigation)

    fun second(componentContext: AppComponentContext, navigation: SecondScreenNavigation): SecondScreen =
        SecondComponentFactory.createComponent(componentContext, navigation)

    fun third(componentContext: AppComponentContext, navigation: ThirdScreenNavigation, args: ThirdScreenArgs): ThirdScreen =
        ThirdComponentFactory.createComponent(componentContext, navigation, args)

    fun picker(componentContext: AppComponentContext, navigation: PickerNavigation, type: PickerType, args: PickerArgs): PickerScreen =
        when (type) {
            PickerType.Fruit -> FruitPickerComponentFactory.createComponent(componentContext, navigation, args)
            PickerType.Vegetable -> VegetablePickerComponentFactory.createComponent(componentContext, navigation, args)
        }

    fun profile(componentContext: AppComponentContext, navigation: ProfileScreenNavigation): ProfileScreen =
        ProfileComponentFactory.createComponent(componentContext, navigation)
}
