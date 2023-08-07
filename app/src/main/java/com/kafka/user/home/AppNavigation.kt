package com.kafka.user.home

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.accompanist.navigation.material.bottomSheet
import com.kafka.reader.ReaderScreen
import com.kafka.search.SearchScreen
import com.kafka.user.playback.PlaybackViewModel
import com.sarahang.playback.ui.activityHiltViewModel
import com.sarahang.playback.ui.sheet.PlaybackSheet
import org.kafka.base.debug
import org.kafka.common.extensions.collectEvent
import org.kafka.homepage.Homepage
import org.kafka.item.detail.ItemDetail
import org.kafka.item.detail.description.DescriptionDialog
import org.kafka.item.files.Files
import org.kafka.library.LibraryScreen
import org.kafka.navigation.LocalNavigator
import org.kafka.navigation.NavigationEvent
import org.kafka.navigation.Navigator
import org.kafka.navigation.ROOT_SCREENS
import org.kafka.navigation.RootScreen
import org.kafka.navigation.Screen
import org.kafka.navigation.deeplink.Config
import org.kafka.webview.WebView
import org.rekhta.ui.auth.LoginScreen
import org.rekhta.ui.auth.feedback.FeedbackScreen
import org.rekhta.ui.auth.profile.ProfileScreen

@Composable
internal fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    navigator: Navigator = LocalNavigator.current
) {
    collectEvent(navigator.queue) { event ->
        when (event) {
            is NavigationEvent.Destination -> {
                // switch tabs first because of a bug in navigation that doesn't allow
                // changing tabs when destination is opened from a different tab
//                event.root?.route?.let {
//                    navController.navigate(it) {
//                        popUpTo(navController.graph.findStartDestination().id) {
//                            saveState = true
//                        }
//                        launchSingleTop = true
//                        restoreState = true
//                    }
//                }
                navController.navigate(event.route)
            }

            is NavigationEvent.Back -> {
                debug { "Back pressed" }
                navController.navigateUp()
            }

            else -> Unit
        }
    }

    NavHost(
        modifier = modifier.fillMaxSize(),
        navController = navController,
        startDestination = RootScreen.Home.route,
        enterTransition = { fadeIn(tween(200)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(200)) },
        popExitTransition = { fadeOut(tween(200)) }
    ) {
        addHomeRoot()
        addSearchRoot()
        addLibraryRoot()
    }
}

private fun NavGraphBuilder.addHomeRoot() {
    navigation(
        route = RootScreen.Home.route,
        startDestination = Screen.Home.createRoute(RootScreen.Home)
    ) {
        addHome(RootScreen.Home)
        addItemDetail(RootScreen.Home)
        addItemDescription(RootScreen.Home)
        addFiles(RootScreen.Home)
        addReader(RootScreen.Home)
        addLibrary(RootScreen.Home)
        addProfile(RootScreen.Home)
        addFeedback(RootScreen.Home)
        addSearch(RootScreen.Home)
        addLogin(RootScreen.Home)
        addPlayer(RootScreen.Home)
        addWebView(RootScreen.Home)
    }
}

private fun NavGraphBuilder.addSearchRoot() {
    navigation(
        route = RootScreen.Search.route,
        startDestination = Screen.Search.createRoute(RootScreen.Search)
    ) {
        addSearch(RootScreen.Search)
        addItemDetail(RootScreen.Search)
        addItemDescription(RootScreen.Search)
        addFiles(RootScreen.Search)
        addReader(RootScreen.Search)
        addPlayer(RootScreen.Search)
    }
}

private fun NavGraphBuilder.addLibraryRoot() {
    navigation(
        route = RootScreen.Library.route,
        startDestination = Screen.Library.createRoute(RootScreen.Library)
    ) {
        addLibrary(RootScreen.Library)
        addItemDetail(RootScreen.Library)
        addItemDescription(RootScreen.Library)
        addFiles(RootScreen.Library)
        addReader(RootScreen.Library)
        addSearch(RootScreen.Library)
        addPlayer(RootScreen.Library)
    }
}

private fun NavGraphBuilder.addHome(root: RootScreen) {
    composable(Screen.Home.createRoute(root)) {
        Homepage()
    }
}

private fun NavGraphBuilder.addSearch(root: RootScreen) {
    composable(Screen.Search.createRoute(root)) {
        SearchScreen()
    }
}

private fun NavGraphBuilder.addPlayer(root: RootScreen) {
    bottomSheet(Screen.Player.createRoute(root)) {
        val navigator = LocalNavigator.current
        val playbackViewModel = activityHiltViewModel<PlaybackViewModel>()

        PlaybackSheet(
            onClose = { navigator.goBack() },
            goToItem = { playbackViewModel.goToAlbum() },
            goToCreator = { playbackViewModel.goToCreator() }
        )
    }
}

private fun NavGraphBuilder.addLibrary(root: RootScreen) {
    composable(Screen.Library.createRoute(root)) {
        LibraryScreen()
    }
}

private fun NavGraphBuilder.addItemDetail(root: RootScreen) {
    composable(
        Screen.ItemDetail.createRoute(root),
        arguments = listOf(navArgument("itemId") { type = NavType.StringType }),
        deepLinks = listOf(navDeepLink { uriPattern = "${Config.BASE_URL}item/{itemId}" })
    ) {
        ItemDetail()
    }
}

private fun NavGraphBuilder.addItemDescription(root: RootScreen) {
    bottomSheet(Screen.ItemDescription.createRoute(root)) {
        DescriptionDialog()
    }
}

private fun NavGraphBuilder.addFiles(root: RootScreen) {
    composable(Screen.Files.createRoute(root)) {
        Files()
    }
}

private fun NavGraphBuilder.addReader(root: RootScreen) {
    composable(Screen.Reader.createRoute(root)) {
        ReaderScreen()
    }
}

private fun NavGraphBuilder.addLogin(root: RootScreen) {
    composable(Screen.Login.createRoute(root)) {
        LoginScreen()
    }
}

private fun NavGraphBuilder.addProfile(root: RootScreen) {
    dialog(
        route = Screen.Profile.createRoute(root),
        dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ProfileScreen()
    }
}

private fun NavGraphBuilder.addFeedback(root: RootScreen) {
    bottomSheet(route = Screen.Feedback.createRoute(root)) {
        FeedbackScreen()
    }
}

private fun NavGraphBuilder.addWebView(root: RootScreen) {
    composable(
        route = Screen.Web.createRoute(root),
        arguments = listOf(
            navArgument("url") { type = NavType.StringType }
        )
    ) {
        val navigator = LocalNavigator.current
        WebView(it.arguments?.getString("url").orEmpty(), navigator::goBack)
    }
}

// todo: app closes on back from player

@Stable
@Composable
internal fun NavController.currentScreenAsState(): State<RootScreen> {
    val selectedItem = remember { mutableStateOf<RootScreen>(RootScreen.Home) }
    val rootScreens = ROOT_SCREENS
    DisposableEffect(this) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            rootScreens.firstOrNull { rs -> destination.hierarchy.any { it.route == rs.route } }
                ?.let { selectedItem.value = it }
        }
        addOnDestinationChangedListener(listener)

        onDispose {
            removeOnDestinationChangedListener(listener)
        }
    }

    return selectedItem
}

@ExperimentalAnimationApi
private fun AnimatedContentTransitionScope<*>.defaultEnterTransition(
    initial: NavBackStackEntry,
    target: NavBackStackEntry,
): EnterTransition {
    val initialNavGraph = initial.destination.hostNavGraph
    val targetNavGraph = target.destination.hostNavGraph
    // If we're crossing nav graphs (bottom navigation graphs), we crossfade
    if (initialNavGraph.id != targetNavGraph.id) {
        return fadeIn()
    }
    // Otherwise we're in the same nav graph, we can imply a direction
    return fadeIn() + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start)
}

@ExperimentalAnimationApi
private fun AnimatedContentTransitionScope<*>.defaultExitTransition(
    initial: NavBackStackEntry,
    target: NavBackStackEntry,
): ExitTransition {
    val initialNavGraph = initial.destination.hostNavGraph
    val targetNavGraph = target.destination.hostNavGraph
    // If we're crossing nav graphs (bottom navigation graphs), we crossfade
    if (initialNavGraph.id != targetNavGraph.id) {
        return fadeOut()
    }
    // Otherwise we're in the same nav graph, we can imply a direction
    return fadeOut() + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start)
}

private val NavDestination.hostNavGraph: NavGraph
    get() = hierarchy.first { it is NavGraph } as NavGraph

