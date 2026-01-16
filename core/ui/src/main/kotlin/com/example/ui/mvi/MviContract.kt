/*
 * Copyright (c) 2023 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work
 * except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific language
 * governing permissions and limitations under the Licence.
 *
 * Origin: https://github.com/eu-digital-identity-wallet/eudi-app-android-wallet-ui/blob/main/ui-logic/src/main/java/eu/europa/ec/uilogic/mvi/MviContract.kt
 */

package com.example.ui.mvi

/**
 * Represents the immutable state of a UI screen at any given time.
 *
 * The [ViewState] should contain all the information necessary to render the UI.
 * In MVI, the UI is a passive reflection of this state.
 *
 * Implementations should typically be 'data classes' to ensure easy state
 * updates via the 'copy()' method.
 */
interface ViewState

/**
 * Represents a discrete user intent or an internal event that triggers a logic change.
 *
 * [ViewAction] captures everything from a button click to a lifecycle event.
 * These are sent from the UI to the ViewModel to be processed.
 *
 * Implementations should typically be 'sealed classes' or 'sealed interfaces'
 * to ensure exhaustive handling in 'when' expressions.
 */
interface ViewAction

/**
 * Represents a transient, one-time "fire-and-forget" event.
 *
 * Unlike [ViewState], a [ViewSideEffect] is not persisted and should only
 * be processed once (e.g., navigation, showing a Toast, or playing a sound).
 *
 * These are typically delivered via a 'Channel' or a 'SharedFlow' to ensure
 * they are not re-emitted during configuration changes like screen rotation.
 */
interface ViewSideEffect