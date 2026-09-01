#!/usr/bin/env python3
"""Scaffold a Compose screen following this skill's conventions.

Emits one `<Screen>Screen.kt` with the full set the guardrail requires:
  - a thin stateful entry point that collects state + handles one-time effects,
  - a `@VisibleForTesting` stateless body driven by `state` + `onAction`,
  - a `LazyColumn` keyed by a stable id,
  - a test-tags object,
  - `@Preview`s for content / loading / empty / error.

This encodes `template.md` + `references/details.md`; those remain the source of
truth. The screen references a `<Screen>UiState` / `<Screen>UiAction` contract and
a `<Thing>Ui` model - generate those with the architecture-layers feature
scaffold, or pass `--with-contract` to also emit a minimal contract + UI-model stub.

Stdlib only. Example:

  python scaffold_screen.py --screen Properties --thing Property \\
      --package com.example.listings --feature-root feature/listings
"""
from __future__ import annotations

import argparse
import sys
from pathlib import Path


def lower_first(s: str) -> str:
    return s[:1].lower() + s[1:] if s else s


def pluralize(word: str) -> str:
    """Naive English pluralization; override with --items when it guesses wrong."""
    if word.endswith("y") and (len(word) < 2 or word[-2] not in "aeiou"):
        return word[:-1] + "ies"
    if word.endswith(("s", "x", "z", "ch", "sh")):
        return word + "es"
    return word + "s"


SCREEN = '''\
package __PKG__.ui.__screenpkg__

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

// Stable test tags - the stateless body and UI tests both reference these so the
// two never drift. Drive every node a test needs to find through one of these.
object __Screen__TestTags {
    const val LOADING = "__screen___loading"
    const val LIST = "__screen___list"
    const val EMPTY = "__screen___empty"
    const val ERROR = "__screen___error"
}

@Composable
internal fun __Screen__Screen(viewModel: __Screen__ViewModel) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()

    // Collect one-time effects once; branch to navigation / snackbar handlers.
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
                modifier = Modifier
                    .align(Alignment.Center)
                    .testTag(__Screen__TestTags.LOADING),
            )

            state.errorConfig != null -> Text(
                text = state.errorConfig.toString(),
                modifier = Modifier
                    .align(Alignment.Center)
                    .testTag(__Screen__TestTags.ERROR),
            )

            state.__items__.isEmpty() -> Text(
                text = "Nothing here yet", // TODO: stringResource(...)
                modifier = Modifier
                    .align(Alignment.Center)
                    .testTag(__Screen__TestTags.EMPTY),
            )

            else -> LazyColumn(modifier = Modifier.testTag(__Screen__TestTags.LIST)) {
                // Stable identity key so Compose preserves item state across changes.
                items(state.__items__, key = { it.id }) { item ->
                    Text(
                        text = item.title,
                        modifier = Modifier.testTag("__screen___item_${item.id}"),
                    )
                    // TODO: real row composable + design-system spacing; wire onAction(OnItemClick(item)).
                }
            }
        }
    }
}

// --- Previews: one per meaningful state. Prefer a @LightDarkPreviews multi-preview. ---

@Preview
@Composable
private fun __Screen__ContentPreview() {
    // TODO: wrap in your design-system theme and feed sample __thing__Ui data.
    __Screen__Screen(state = __Screen__UiState(__items__ = emptyList()), onAction = {})
}

@Preview
@Composable
private fun __Screen__LoadingPreview() {
    __Screen__Screen(state = __Screen__UiState(isLoading = true), onAction = {})
}

@Preview
@Composable
private fun __Screen__EmptyPreview() {
    __Screen__Screen(state = __Screen__UiState(__items__ = emptyList()), onAction = {})
}

@Preview
@Composable
private fun __Screen__ErrorPreview() {
    // TODO: feed a sample errorConfig for the error state.
    __Screen__Screen(state = __Screen__UiState(), onAction = {})
}
'''

CONTRACT = '''\
package __PKG__.ui.__screenpkg__

import androidx.compose.runtime.Immutable

// Minimal MVI contract + UI model stub. Flesh out per the architecture-layers
// skill (State/Action/Effect + a <Thing>Ui mapped off the main thread).

@Immutable
internal data class __Thing__Ui(
    val id: __ID_TYPE__,
    val title: String,
)

@Immutable
internal data class __Screen__UiState(
    val isLoading: Boolean = false,
    val __items__: List<__Thing__Ui> = emptyList(),
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
'''


def render(template: str, args: argparse.Namespace, items: str) -> str:
    return (
        template
        .replace("__Screen__", args.screen)
        .replace("__screen__", lower_first(args.screen))
        .replace("__Thing__", args.thing)
        .replace("__thing__", lower_first(args.thing))
        .replace("__items__", items)
        .replace("__ID_TYPE__", args.id_type)
        .replace("__screenpkg__", lower_first(args.screen))
        .replace("__PKG__", args.package)
    )


def main() -> int:
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("--screen", required=True, help="Screen name in PascalCase, e.g. Properties")
    p.add_argument("--thing", required=True, help="UI model name in PascalCase, e.g. Property")
    p.add_argument("--package", required=True, help="Feature package, e.g. com.example.listings")
    p.add_argument("--feature-root", default=".", help="Module/source root to write under (default: current dir)")
    p.add_argument("--source-set", default="src/main/kotlin", help="Source set path (default: src/main/kotlin)")
    p.add_argument("--id-type", default="String", help="Type of the model id (default: String)")
    p.add_argument("--items", default=None, help="Plural state field name (default: <thing>s)")
    p.add_argument("--with-contract", action="store_true", help="Also emit a minimal contract + UI-model stub")
    p.add_argument("--force", action="store_true", help="Overwrite files that already exist")
    p.add_argument("--dry-run", action="store_true", help="Print planned files, then exit")
    args = p.parse_args()

    items = args.items or pluralize(lower_first(args.thing))
    pkg_path = args.package.replace(".", "/")
    base = Path(args.feature_root) / args.source_set / pkg_path / "ui" / lower_first(args.screen)

    files = {base / f"{args.screen}Screen.kt": render(SCREEN, args, items)}
    if args.with_contract:
        files[base / f"{args.screen}Contract.kt"] = render(CONTRACT, args, items)

    if args.dry_run:
        for dest in files:
            print(f"would write  {dest}")
        return 0

    for dest, content in files.items():
        if dest.exists() and not args.force:
            print(f"skip    {dest} (exists; pass --force to overwrite)")
            continue
        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_text(content, encoding="utf-8", newline="\n")
        print(f"wrote   {dest}")

    print(
        "\nNext: swap the TODO row composable + spacing tokens for real ones, wire the "
        "effect handlers (navigate/snackbar), and confirm every state has a @Preview. "
        "See references/details.md."
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
