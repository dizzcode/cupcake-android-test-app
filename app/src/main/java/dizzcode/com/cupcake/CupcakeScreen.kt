
package dizzcode.com.cupcake

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dizzcode.com.cupcake.data.DataSource
import dizzcode.com.cupcake.ui.OrderSummaryScreen
import dizzcode.com.cupcake.ui.OrderViewModel
import dizzcode.com.cupcake.ui.SelectOptionScreen
import dizzcode.com.cupcake.ui.StartOrderScreen

/**
 * Start: Select the quantity of cupcakes from one of three buttons.
 * Flavor: Select the flavor from a list of choices.
 * Pickup: Select the pickup date from a list of choices.
 * Summary: Review the selections and either send or cancel the order.
 */
enum class CupcakeScreen(@StringRes val title: Int){
    Start(title = R.string.app_name),
    Flavor(title = R.string.choose_flavor),
    Pickup(title = R.string.choose_pickup_date),
    Summary(title = R.string.order_summary)
}

/**
 * Composable that displays the topBar and displays back button if back navigation is possible.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CupcakeAppBar(
    currentScreen: CupcakeScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(id = currentScreen.title)) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CupcakeApp(
    viewModel: OrderViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {

    val backStackEntry by navController.currentBackStackEntryAsState()

    val currentScreen = CupcakeScreen.valueOf(
        backStackEntry?.destination?.route ?: CupcakeScreen.Start.name
    )

    //canNavigateBack = For the canNavigateBack parameter, pass in a boolean expression checking if the
    // previousBackStackEntry property of navController is not equal to null.

    //navigateUp = To actually navigate back to the previous screen, call the navigateUp() method of navController.

    Scaffold(
        topBar = {
            CupcakeAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) {  innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        /**
         * Note : 01
         * NavHostController is a subclass of the NavController class
         * that provides additional functionality for use with a NavHost composable.
         */

        /**
         * Note : 02
         * NavHost
         *
         * 1) navController:
         * An instance of the NavHostController class.
         * You can use this object to navigate between screens,
         * for example, by calling the navigate() method to navigate to another destination.
         * You can obtain the NavHostController by calling rememberNavController()
         * from a composable function.
         *
         * 2) startDestination:
         * A string route defining the destination shown by default when the
         * app first displays the NavHost.
         * In the case of the Cupcake app, this should be the Start route.
         */

        /**
         * Note : 03
         * Handle routes in your NavHost | composable()
         *
         * The composable() function has two required parameters.
         *(The composable() function is an extension function of NavGraphBuilder.)
         *
         * 1) route:
         * A string corresponding to the name of a route.
         * This can be any unique string.
         * You'll use the name property of the CupcakeScreen enum's constants.
         *
         * 2) content:
         * Here you can call a composable that you want to display for the given route.
         */
        NavHost(
            navController = navController,
            startDestination = CupcakeScreen.Start.name,
            modifier = Modifier.padding(innerPadding)
        ){

            composable( route = CupcakeScreen.Start.name){
                StartOrderScreen(
                    quantityOptions = DataSource.quantityOptions,
                    onNextButtonClicked = {
                        viewModel.setQuantity(it)
                        navController.navigate(CupcakeScreen.Flavor.name)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(id = R.dimen.padding_medium))
                )
            }

            composable( route = CupcakeScreen.Flavor.name){
                val context = LocalContext.current

                SelectOptionScreen(
                    subtotal = uiState.price,
                    onNextButtonClicked = { navController.navigate(CupcakeScreen.Pickup.name) },
                    onCancelButtonClicked = {},
                    options = DataSource.flavors.map { id -> context.resources.getString(id) },
                    onSelectionChanged = { viewModel.setFlavor(it)},
                    modifier = Modifier.fillMaxHeight()
                )
            }

            composable( route = CupcakeScreen.Pickup.name){
                SelectOptionScreen(
                    subtotal = uiState.price,
                    onNextButtonClicked = { navController.navigate(CupcakeScreen.Summary.name) },
                    onCancelButtonClicked = {},
                    options = uiState.pickupOptions,
                    onSelectionChanged = { viewModel.setDate(it) },
                    modifier = Modifier.fillMaxHeight()
                )
            }

            composable( route = CupcakeScreen.Summary.name){
                val context = LocalContext.current

                OrderSummaryScreen(
                    orderUiState = uiState,
                    onCancelButtonClicked = {},
                    onSendButtonClicked = { subject: String, summary: String ->
                        shareOrder(context = context, subject = subject, summary = summary)
                    },
                    modifier = Modifier.fillMaxHeight()
                )
            }

        }
    }
}

//Navigate to another app
private fun shareOrder(context: Context, subject: String, summary: String){
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, summary)
    }

    context.startActivity(
        Intent.createChooser(
            intent,
            context.getString(R.string.new_cupcake_order)
        )
    )
}

private fun cancelOrderAndNavigateToStart(
    viewModel: OrderViewModel,
    navController: NavHostController
) {
    viewModel.resetOrder()
    navController.popBackStack(route = CupcakeScreen.Start.name, inclusive = false)
    /**
     * Note : 04
     * Pop to the start screen
     *
     * The popBackStack() method has two required parameters.
     *
     * 1) route:
     * The string representing the route of the destination you want to navigate back to.
     *
     * 2) inclusive:
     * A Boolean value that,
     * if true, also pops (removes) the specified route.
     * If false, popBackStack() will remove all destinations on top of—but not
     * including—the start destination, leaving it as the topmost screen visible
     * to the user.
     */
}
