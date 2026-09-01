#!/usr/bin/env python3
"""Scaffold a full vertical feature slice across every layer.

Stamps out the boilerplate that "adding a feature" requires - domain model /
repository interface / use case, network DTO + data source + Retrofit API +
client + per-feature DI module, (offline-first) Room entity + DAO, data-layer
mappers + repository implementation + Hilt @Binds module, the MVI contract +
ViewModel + UI model, the Compose screen, and (multi-module) the feature
`build.gradle.kts`. The agent then fills in the *real* fields and logic instead
of retyping ~15 files of structure.

This is a faithful encoding of `references/layers.md`, `references/mvi.md`,
`references/adding-a-feature.md`, and `template.md` - those remain the source of
truth. Generated files carry TODO markers where project-specific detail is
needed (real model fields, core-module import packages, string resources).

Supports the two project-wide toggles the skill documents:
  --layout multi|single        Gradle modules vs packages in one module
  --mode offline-first|network-only   Room cache vs straight-from-network

Stdlib only; runs anywhere Python 3.8+ is available. Examples:

  python scaffold_feature.py --name listings --thing Property --screen Properties \\
      --package com.example --id-type String
  python scaffold_feature.py --name search --thing Result --layout single \\
      --mode network-only --app-module app --dry-run
"""
from __future__ import annotations

import argparse
import sys
from pathlib import Path


# --------------------------------------------------------------------------- #
# Helpers
# --------------------------------------------------------------------------- #

def lower_first(s: str) -> str:
    return s[:1].lower() + s[1:] if s else s


def pluralize(word: str) -> str:
    """Naive English pluralization; override with --plural when it guesses wrong."""
    if word.endswith("y") and (len(word) < 2 or word[-2] not in "aeiou"):
        return word[:-1] + "ies"
    if word.endswith(("s", "x", "z", "ch", "sh")):
        return word + "es"
    return word + "s"


# --------------------------------------------------------------------------- #
# Templates - placeholders are __TOKEN__; Kotlin's own ${} / {} pass through.
# --------------------------------------------------------------------------- #

CORE_IMPORTS_NOTE = (
    "// --- project-internal types: adjust these imports to your core packages ---\n"
)

DOMAIN_MODEL = """\
package __PKG_DOMAIN__.model

// Pure-Kotlin domain model: no Room / Retrofit / Compose annotations.
data class __Thing__(
    val id: __ID_TYPE__,
    val title: String,
    // TODO: add the real domain fields.
)
"""

DOMAIN_REPO_OFFLINE = """\
package __PKG_DOMAIN__.repository

import kotlinx.coroutines.flow.Flow
__CORE_IMPORTS__// import __PKG_DOMAIN__.model.__Thing__
// import <your Result package>.Result

interface __Thing__Repository {
    // Reads flow out of the single source of truth (the DB).
    val __things__: Flow<List<__Thing__>>
    // Refresh hits the network and writes through to the DB.
    suspend fun refresh(): Result<List<__Thing__>>
}
"""

DOMAIN_REPO_NETWORK = """\
package __PKG_DOMAIN__.repository

__CORE_IMPORTS__// import __PKG_DOMAIN__.model.__Thing__
// import <your Result package>.Result

interface __Thing__Repository {
    // Network-only: one-shot reads return Result; there is no Flow/refresh split.
    suspend fun get__Things__(): Result<List<__Thing__>>
    suspend fun get__Thing__(id: __ID_TYPE__): Result<__Thing__?>
}
"""

DOMAIN_USECASE_OFFLINE = """\
package __PKG_DOMAIN__.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
__CORE_IMPORTS__// import __PKG_DOMAIN__.model.__Thing__
// import __PKG_DOMAIN__.repository.__Thing__Repository
// import <your Result package>.Result

class Get__Thing__UseCase @Inject constructor(
    private val repository: __Thing__Repository,
) {
    operator fun invoke(): Flow<List<__Thing__>> = repository.__things__
    suspend fun refresh(): Result<List<__Thing__>> = repository.refresh()
}
"""

DOMAIN_USECASE_NETWORK = """\
package __PKG_DOMAIN__.usecase

import javax.inject.Inject
__CORE_IMPORTS__// import __PKG_DOMAIN__.model.__Thing__
// import __PKG_DOMAIN__.repository.__Thing__Repository
// import <your Result package>.Result

class Get__Thing__UseCase @Inject constructor(
    private val repository: __Thing__Repository,
) {
    suspend operator fun invoke(): Result<List<__Thing__>> = repository.get__Things__()
}
"""

NETWORK_DTO = """\
package __PKG_NETWORK__

import kotlinx.serialization.Serializable

// Mirrors the API shape; never leaks past the data layer.
@Serializable
data class __Thing__Dto(
    val id: __ID_TYPE__,
    val title: String,
    // TODO: mirror the real API fields.
)
"""

NETWORK_DATASOURCE = """\
package __PKG_NETWORK__

// The only network type the data layer depends on - fakeable in tests.
interface __Thing__NetworkDataSource {
    suspend fun get__Things__(): List<__Thing__Dto>      // direct object
    suspend fun get__Thing__(id: __ID_TYPE__): __Thing__Dto?  // null when not found
}
"""

NETWORK_API = """\
package __PKG_NETWORK__

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

// Retrofit API declaration - internal to the network module.
internal interface Retrofit__Thing__NetworkApi {
    // Direct object: a non-2xx response throws HttpException automatically.
    @GET("__things__.json")
    suspend fun get__Things__(): List<__Thing__Dto>

    // Response<T>: lets the client read the HTTP status code.
    @GET("__things__/{id}.json")
    suspend fun get__Thing__(@Path("id") id: __ID_TYPE__): Response<__Thing__Dto?>
}
"""

NETWORK_CLIENT = """\
package __PKG_NETWORK__

import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException

@Singleton
internal class Retrofit__Thing__ApiClient @Inject constructor(
    private val networkApi: Retrofit__Thing__NetworkApi,
) : __Thing__NetworkDataSource {

    override suspend fun get__Things__(): List<__Thing__Dto> =
        networkApi.get__Things__()

    // 403/404 -> the item doesn't exist (null); any other non-2xx is rethrown.
    override suspend fun get__Thing__(id: __ID_TYPE__): __Thing__Dto? {
        val response = networkApi.get__Thing__(id = id)
        return when {
            response.isSuccessful -> response.body()
            response.code() == 403 || response.code() == 404 -> null
            else -> throw HttpException(response)
        }
    }
}
"""

NETWORK_MODULE = """\
package __PKG_NETWORK__

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

// Per-feature network wiring. The shared Json / OkHttp / Retrofit instance is
// provided ONCE per project (see template.md "Hilt module") - this only adds the
// __Thing__ API and binds the data-source interface to its Retrofit client.
@Module
@InstallIn(SingletonComponent::class)
internal object __Thing__NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit__Thing__NetworkApi(retrofit: Retrofit): Retrofit__Thing__NetworkApi =
        retrofit.create(Retrofit__Thing__NetworkApi::class.java)

    @Provides
    @Singleton
    fun provide__Thing__NetworkDataSource(
        impl: Retrofit__Thing__ApiClient,
    ): __Thing__NetworkDataSource = impl
}
"""

DB_ENTITY = """\
package __PKG_DATABASE__

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "__things__")
data class __Thing__Entity(
    @PrimaryKey val id: __ID_TYPE__,
    val title: String,
    // TODO: mirror the persisted fields.
)
"""

DB_DAO = """\
package __PKG_DATABASE__

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface __Thing__Dao {
    @Query("SELECT * FROM __things__")
    fun getAll(): Flow<List<__Thing__Entity>>

    @Upsert
    suspend fun upsertAll(items: List<__Thing__Entity>)
}
"""

MAPPERS_OFFLINE = """\
package __PKG_DATA__

__CORE_IMPORTS__// import __PKG_DOMAIN__.model.__Thing__
// import __PKG_NETWORK__.__Thing__Dto
// import __PKG_DATABASE__.__Thing__Entity

// One direction per function (see references/layers.md 4.2).
fun __Thing__Dto.asEntity() = __Thing__Entity(id = id, title = title)
fun __Thing__Entity.asExternalModel() = __Thing__(id = id, title = title)
"""

MAPPERS_NETWORK = """\
package __PKG_DATA__

__CORE_IMPORTS__// import __PKG_DOMAIN__.model.__Thing__
// import __PKG_NETWORK__.__Thing__Dto

// Network-only collapses to a single hop: DTO -> domain.
fun __Thing__Dto.asExternalModel() = __Thing__(id = id, title = title)
"""

REPO_OFFLINE = """\
package __PKG_DATA__

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
__CORE_IMPORTS__// import __PKG_DOMAIN__.model.__Thing__
// import __PKG_DOMAIN__.repository.__Thing__Repository
// import __PKG_NETWORK__.__Thing__NetworkDataSource
// import __PKG_DATABASE__.__Thing__Dao
// import <your Result package>.Result
// import <runSuspendCatching + toErrorResult>

internal class OfflineFirst__Thing__Repository @Inject constructor(
    private val network: __Thing__NetworkDataSource,
    private val dao: __Thing__Dao,
) : __Thing__Repository {

    // Reads come only from the database (single source of truth).
    override val __things__: Flow<List<__Thing__>> =
        dao.getAll().map { entities -> entities.map(__Thing__Entity::asExternalModel) }

    // Refresh hits the network, writes to the DB; the DB Flow updates the UI.
    override suspend fun refresh(): Result<List<__Thing__>> =
        runSuspendCatching { network.get__Things__() }.fold(
            onSuccess = { dtos ->
                val entities = dtos.map(__Thing__Dto::asEntity)
                dao.upsertAll(entities)
                Result.Success(entities.map(__Thing__Entity::asExternalModel))
            },
            onFailure = { it.toErrorResult() },
        )
}
"""

REPO_NETWORK = """\
package __PKG_DATA__

import javax.inject.Inject
__CORE_IMPORTS__// import __PKG_DOMAIN__.model.__Thing__
// import __PKG_DOMAIN__.repository.__Thing__Repository
// import __PKG_NETWORK__.__Thing__NetworkDataSource
// import <your Result package>.Result
// import <runSuspendCatching + toErrorResult>

internal class Network__Thing__Repository @Inject constructor(
    private val network: __Thing__NetworkDataSource,
) : __Thing__Repository {

    override suspend fun get__Things__(): Result<List<__Thing__>> =
        runSuspendCatching { network.get__Things__() }.fold(
            onSuccess = { dtos -> Result.Success(dtos.map(__Thing__Dto::asExternalModel)) },
            onFailure = { it.toErrorResult() },
        )

    override suspend fun get__Thing__(id: __ID_TYPE__): Result<__Thing__?> =
        runSuspendCatching { network.get__Thing__(id) }.fold(
            onSuccess = { dto -> Result.Success(dto?.asExternalModel()) },
            onFailure = { it.toErrorResult() },
        )
}
"""

DATA_MODULE = """\
package __PKG_DATA__

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
__CORE_IMPORTS__// import __PKG_DOMAIN__.repository.__Thing__Repository

@Module
@InstallIn(SingletonComponent::class)
internal interface __Thing__DataModule {
    @Binds
    fun bind__Thing__Repository(impl: __REPO_NAME__): __Thing__Repository
}
"""

UI_MODEL = """\
package __PKG_FEATURE__.ui.__screen__

import androidx.compose.runtime.Immutable
__CORE_IMPORTS__// import __PKG_DOMAIN__.model.__Thing__
// import <your ResourceProvider package>.ResourceProvider

@Immutable
internal data class __Thing__Ui(
    val id: __ID_TYPE__,
    val title: String,
)

// Map domain -> UI off the main thread (the ViewModel calls this on a worker
// dispatcher). Format any user-facing strings via ResourceProvider, not literals.
internal fun __Thing__.to__Thing__Ui(resourceProvider: ResourceProvider): __Thing__Ui =
    __Thing__Ui(id = id, title = title)
"""

CONTRACT = """\
package __PKG_FEATURE__.ui.__screen__

import androidx.compose.runtime.Immutable
__CORE_IMPORTS__// import <:core:ui>.ViewState
// import <:core:ui>.ViewAction
// import <:core:ui>.ViewSideEffect

@Immutable
internal data class __Screen__UiState(
    val isLoading: Boolean = false,
    val __things__: List<__Thing__Ui> = emptyList(),
    val errorConfig: Any? = null, // TODO: replace Any? with your UiErrorConfig type
) // : ViewState

internal sealed interface __Screen__UiAction { // : ViewAction
    data object Init : __Screen__UiAction
    data object Refresh : __Screen__UiAction
    data class OnItemClick(val item: __Thing__Ui) : __Screen__UiAction
}

internal sealed interface __Screen__UiEffect { // : ViewSideEffect
    data class ShowSnackbar(val message: String) : __Screen__UiEffect
    data class NavigateToDetail(val id: __ID_TYPE__) : __Screen__UiEffect
}
"""

VIEWMODEL_OFFLINE = """\
package __PKG_FEATURE__.ui.__screen__

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
__CORE_IMPORTS__// import <:core:ui>.MviViewModel
// import <:core:domain>.usecase.Get__Thing__UseCase
// import <ResourceProvider>, <NetworkMonitor>, <@DefaultDispatcher>, <Result>, <R>

@HiltViewModel
internal class __Screen__ViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val get__Things__: Get__Thing__UseCase,
    private val networkMonitor: NetworkMonitor,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : MviViewModel<__Screen__UiAction, __Screen__UiState, __Screen__UiEffect>() {

    override fun setInitialState() = __Screen__UiState(isLoading = true)

    override fun handleAction(action: __Screen__UiAction) = when (action) {
        __Screen__UiAction.Init -> observe__Things__()
        __Screen__UiAction.Refresh -> refresh()
        is __Screen__UiAction.OnItemClick ->
            setEffect { __Screen__UiEffect.NavigateToDetail(action.item.id) }
    }

    private fun observe__Things__() {
        get__Things__()
            .map { items -> items.map { it.to__Thing__Ui(resourceProvider) } }
            .flowOn(defaultDispatcher) // map off the main thread
            .onEach { ui -> setState { copy(isLoading = false, __things__ = ui) } }
            .launchIn(viewModelScope)
    }

    private fun refresh() {
        viewModelScope.launch {
            when (get__Things__.refresh()) {
                is Result.Success -> Unit // DB Flow updates the list
                is Result.Error -> setEffect {
                    __Screen__UiEffect.ShowSnackbar(
                        resourceProvider.getString(R.string.refresh_failed),
                    )
                }
            }
        }
    }
}
"""

VIEWMODEL_NETWORK = """\
package __PKG_FEATURE__.ui.__screen__

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
__CORE_IMPORTS__// import <:core:ui>.MviViewModel
// import <:core:domain>.usecase.Get__Thing__UseCase
// import <ResourceProvider>, <NetworkMonitor>, <@DefaultDispatcher>, <Result>, <R>

@HiltViewModel
internal class __Screen__ViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val get__Things__: Get__Thing__UseCase,
    private val networkMonitor: NetworkMonitor,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : MviViewModel<__Screen__UiAction, __Screen__UiState, __Screen__UiEffect>() {

    override fun setInitialState() = __Screen__UiState(isLoading = true)

    override fun handleAction(action: __Screen__UiAction) = when (action) {
        __Screen__UiAction.Init -> load()
        __Screen__UiAction.Refresh -> load()
        is __Screen__UiAction.OnItemClick ->
            setEffect { __Screen__UiEffect.NavigateToDetail(action.item.id) }
    }

    // Network-only: every load goes to the network (no DB flow that auto-updates).
    private fun load() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            when (val result = get__Things__()) {
                is Result.Success -> {
                    val ui = withContext(defaultDispatcher) {
                        result.data.map { it.to__Thing__Ui(resourceProvider) }
                    }
                    setState { copy(isLoading = false, __things__ = ui) }
                }
                is Result.Error -> {
                    setState { copy(isLoading = false) }
                    setEffect {
                        __Screen__UiEffect.ShowSnackbar(
                            resourceProvider.getString(R.string.refresh_failed),
                        )
                    }
                }
            }
        }
    }
}
"""

SCREEN = """\
package __PKG_FEATURE__.ui.__screen__

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle

object __Screen__TestTags {
    const val LOADING = "__screen___loading"
    const val LIST = "__screen___list"
    const val EMPTY = "__screen___empty"
    const val ERROR = "__screen___error"
}

@Composable
internal fun __Screen__Screen(viewModel: __Screen__ViewModel) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                // is __Screen__UiEffect.NavigateToDetail -> onNavigateToDetail(effect.id)
                // is __Screen__UiEffect.ShowSnackbar -> /* show snackbar */
                else -> Unit
            }
        }
    }

    __Screen__Screen(state = state, onAction = viewModel::setAction)
}

@VisibleForTesting
@Composable
internal fun __Screen__Screen(
    state: __Screen__UiState,
    onAction: (__Screen__UiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center).testTag(__Screen__TestTags.LOADING),
            )

            state.errorConfig != null -> Text(
                text = state.errorConfig.toString(),
                modifier = Modifier.align(Alignment.Center).testTag(__Screen__TestTags.ERROR),
            )

            state.__things__.isEmpty() -> Text(
                text = "Nothing here yet", // TODO: stringResource(...)
                modifier = Modifier.align(Alignment.Center).testTag(__Screen__TestTags.EMPTY),
            )

            else -> LazyColumn(modifier = Modifier.testTag(__Screen__TestTags.LIST)) {
                // Stable identity key so Compose preserves item state across changes.
                items(state.__things__, key = { it.id }) { item ->
                    Text(
                        text = item.title,
                        modifier = Modifier.testTag("__screen___item_${item.id}"),
                    )
                    // TODO: real row composable; wire onAction(OnItemClick(item)).
                }
            }
        }
    }
}

@Preview
@Composable
private fun __Screen__ContentPreview() {
    __Screen__Screen(state = __Screen__UiState(__things__ = emptyList()), onAction = {})
}

@Preview
@Composable
private fun __Screen__LoadingPreview() {
    __Screen__Screen(state = __Screen__UiState(isLoading = true), onAction = {})
}

@Preview
@Composable
private fun __Screen__EmptyPreview() {
    __Screen__Screen(state = __Screen__UiState(__things__ = emptyList()), onAction = {})
}
"""

FEATURE_BUILD = """\
plugins {
    alias(libs.plugins.__CONV__.android.feature)
    alias(libs.plugins.__CONV__.android.compose)
}

android {
    namespace = "__PKG_FEATURE__"
}

dependencies {
    testImplementation(project(":core:testing"))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
"""


# --------------------------------------------------------------------------- #
# Layout resolution
# --------------------------------------------------------------------------- #

class Layout:
    """Maps each layer to a (module_dir, package) for the chosen --layout."""

    def __init__(self, args: argparse.Namespace):
        base = args.package
        name = args.name.lower()
        self.source_set = args.source_set
        if args.layout == "multi":
            self.dirs = {
                "domain": "core/domain", "network": "core/network",
                "database": "core/database", "data": "core/data",
                "feature": f"feature/{name}",
            }
            self.pkgs = {
                "domain": f"{base}.domain", "network": f"{base}.network",
                "database": f"{base}.database", "data": f"{base}.data",
                "feature": f"{base}.{name}",
            }
        else:  # single module: layers are packages under one app module
            app = args.app_module
            self.dirs = {k: app for k in ("domain", "network", "database", "data", "feature")}
            self.pkgs = {
                "domain": f"{base}.domain", "network": f"{base}.data.network",
                "database": f"{base}.data.database", "data": f"{base}.data",
                "feature": f"{base}.feature.{name}",
            }

    def path(self, layer: str, *parts: str) -> Path:
        pkg_path = self.pkgs[layer].replace(".", "/")
        return Path(self.dirs[layer], self.source_set, pkg_path, *parts)


# --------------------------------------------------------------------------- #
# Build the file plan
# --------------------------------------------------------------------------- #

def build_plan(args: argparse.Namespace, layout: Layout) -> dict[Path, str]:
    thing, screen = args.thing, args.screen
    things = args.plural or pluralize(lower_first(thing))
    repo_name = f"OfflineFirst{thing}Repository" if args.mode == "offline-first" else f"Network{thing}Repository"

    ctx = {
        "__Thing__": thing,
        "__thing__": lower_first(thing),
        "__Things__": args.plural_pascal or pluralize(thing),
        "__things__": things,
        "__Screen__": screen,
        "__screen__": lower_first(screen),
        "__ID_TYPE__": args.id_type,
        "__REPO_NAME__": repo_name,
        "__CONV__": args.conv,
        "__PKG_DOMAIN__": layout.pkgs["domain"],
        "__PKG_NETWORK__": layout.pkgs["network"],
        "__PKG_DATABASE__": layout.pkgs["database"],
        "__PKG_DATA__": layout.pkgs["data"],
        "__PKG_FEATURE__": layout.pkgs["feature"],
        "__CORE_IMPORTS__": CORE_IMPORTS_NOTE,
    }

    def render(t: str) -> str:
        for k, v in ctx.items():
            t = t.replace(k, v)
        return t

    offline = args.mode == "offline-first"
    plan: dict[Path, str] = {}

    # Domain
    plan[layout.path("domain", "model", f"{thing}.kt")] = render(DOMAIN_MODEL)
    plan[layout.path("domain", "repository", f"{thing}Repository.kt")] = render(
        DOMAIN_REPO_OFFLINE if offline else DOMAIN_REPO_NETWORK)
    plan[layout.path("domain", "usecase", f"Get{thing}UseCase.kt")] = render(
        DOMAIN_USECASE_OFFLINE if offline else DOMAIN_USECASE_NETWORK)

    # Network
    plan[layout.path("network", f"{thing}Dto.kt")] = render(NETWORK_DTO)
    plan[layout.path("network", f"{thing}NetworkDataSource.kt")] = render(NETWORK_DATASOURCE)
    plan[layout.path("network", f"Retrofit{thing}NetworkApi.kt")] = render(NETWORK_API)
    plan[layout.path("network", f"Retrofit{thing}ApiClient.kt")] = render(NETWORK_CLIENT)
    plan[layout.path("network", f"{thing}NetworkModule.kt")] = render(NETWORK_MODULE)

    # Database (offline-first only)
    if offline:
        plan[layout.path("database", f"{thing}Entity.kt")] = render(DB_ENTITY)
        plan[layout.path("database", f"{thing}Dao.kt")] = render(DB_DAO)

    # Data
    plan[layout.path("data", f"{thing}Mappers.kt")] = render(MAPPERS_OFFLINE if offline else MAPPERS_NETWORK)
    plan[layout.path("data", f"{repo_name}.kt")] = render(REPO_OFFLINE if offline else REPO_NETWORK)
    plan[layout.path("data", f"{thing}DataModule.kt")] = render(DATA_MODULE)

    # Feature / UI
    ui = ("feature", "ui", lower_first(screen))
    plan[layout.path(ui[0], ui[1], ui[2], f"{thing}Ui.kt")] = render(UI_MODEL)
    plan[layout.path(ui[0], ui[1], ui[2], f"{screen}Contract.kt")] = render(CONTRACT)
    plan[layout.path(ui[0], ui[1], ui[2], f"{screen}ViewModel.kt")] = render(
        VIEWMODEL_OFFLINE if offline else VIEWMODEL_NETWORK)
    plan[layout.path(ui[0], ui[1], ui[2], f"{screen}Screen.kt")] = render(SCREEN)

    # Feature build file (multi-module only)
    if args.layout == "multi":
        plan[Path(layout.dirs["feature"], "build.gradle.kts")] = render(FEATURE_BUILD)

    return plan


# --------------------------------------------------------------------------- #
# Main
# --------------------------------------------------------------------------- #

def main() -> int:
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("--name", required=True, help="Feature name, lower-case, e.g. listings")
    p.add_argument("--thing", required=True, help="Domain model name in PascalCase, e.g. Property")
    p.add_argument("--screen", default=None, help="Screen name in PascalCase (default: plural of --thing)")
    p.add_argument("--package", default="com.example", help="Org base package (default: com.example)")
    p.add_argument("--id-type", default="String", help="Type of the model id (default: String)")
    p.add_argument("--layout", choices=["multi", "single"], default="multi", help="Module layout (default: multi)")
    p.add_argument("--mode", choices=["offline-first", "network-only"], default="offline-first",
                   help="Data strategy (default: offline-first)")
    p.add_argument("--app-module", default="app", help="App module dir for --layout single (default: app)")
    p.add_argument("--conv", default="luximmo", help="Convention-plugin prefix for the feature build file (default: luximmo)")
    p.add_argument("--source-set", default="src/main/kotlin", help="Source set path (default: src/main/kotlin)")
    p.add_argument("--plural", default=None, help="Override plural camelCase state field (e.g. properties)")
    p.add_argument("--plural-pascal", default=None, help="Override plural PascalCase (e.g. Properties)")
    p.add_argument("--output", default=".", help="Repository root to write under (default: current dir)")
    p.add_argument("--force", action="store_true", help="Overwrite files that already exist")
    p.add_argument("--dry-run", action="store_true", help="Print the planned files, then exit")
    args = p.parse_args()

    if args.screen is None:
        args.screen = pluralize(args.thing)

    layout = Layout(args)
    plan = build_plan(args, layout)
    root = Path(args.output).resolve()

    if args.dry_run:
        for rel in sorted(plan):
            print(f"would write  {rel.as_posix()}")
        print(f"\n{len(plan)} files ({args.layout} layout, {args.mode}).")
        return 0

    wrote, skipped = [], []
    for rel, content in plan.items():
        dest = root / rel
        if dest.exists() and not args.force:
            skipped.append(rel)
            continue
        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_text(content, encoding="utf-8", newline="\n")
        wrote.append(rel)

    for rel in sorted(wrote):
        print(f"wrote   {rel.as_posix()}")
    for rel in sorted(skipped):
        print(f"skip    {rel.as_posix()} (exists; pass --force to overwrite)")

    print("\nNext steps the script can't do for you:")
    if args.layout == "multi":
        print(f'  - settings.gradle.kts: add  include(":feature:{args.name.lower()}")')
    print("  - Fill in the real model/DTO/entity fields and the UI mapper.")
    print("  - Fix the `// --- project-internal types` imports to your core packages")
    print("    (Result/AppError, runSuspendCatching/toErrorResult, MviViewModel,")
    print("     ViewState/Action/SideEffect, ResourceProvider, NetworkMonitor, @DefaultDispatcher).")
    print("  - Register the navigation entry + NavKey (see the compose-navigation-3 skill).")
    print("  - Add the refresh_failed string resource (or your own).")
    print("  - Add tests (see the unit-testing + compose-ui-testing skills).")
    return 0


if __name__ == "__main__":
    sys.exit(main())
