package dizzcode.com.cupcake.test

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import dizzcode.com.cupcake.CupcakeApp
import org.junit.Before
import org.junit.Rule

class CupcakeScreenNavigationTest {

    /** Note 01
     * UI tests in Compose require a Compose test rule.
     * The same is true of testing Jetpack Navigation.
     * However, testing navigation requires some additional setup through the Compose test rule.
     *
     * When testing Compose Navigation, you won't have access to the same NavHostController
     * that you do in the app code. However, you can use a TestNavHostController and configure
     * the test rule with this nav controller. In this section, you learn how to configure and
     * reuse the test rule for navigation tests.
     */

    @get: Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: TestNavHostController

    @Before
    fun setupCupcakeNavHost() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            CupcakeApp(navController = navController)
        }
    }
}
