# OpenCode Agent Rules

## Purpose
- Follow these rules when developing or modifying this Android application.
- Prioritize security, correctness, maintainability, and usability.
- Make focused changes that match the user's request.

## Technology Stack
- Use Kotlin as the primary programming language.
- Build all application UI using Jetpack Compose.
- Follow the MVVM (Model-View-ViewModel) architecture pattern.
- Use Room for persistent structured local data.
- Use SharedPreferences for simple application settings such as theme.
- Use Kotlin Coroutines and Flow for asynchronous and reactive work.
- Prefer existing project dependencies over adding new libraries.

## UI and UX
- Design responsive layouts for small and large screens.
- Support phones, tablets, and different screen orientations.
- Avoid fixed sizes when adaptive layouts are more appropriate.
- Keep Compose UI files simple, focused, and easy to understand.
- Extract reusable composables when they improve clarity or reuse.
- Separate UI rendering from business logic and data access.
- Use consistent spacing, typography, colors, and components.
- Support light and dark themes and accessible text scaling.
- Handle loading, empty, success, and error states explicitly.
- Request user approval before introducing fancy UI elements.
- Fancy elements include elaborate animations, gradients, and decorative effects.
- Do not add unnecessary UI libraries or visual complexity.

## Architecture and Code Quality
- Keep UI, ViewModel, repository, and data layers separate.
- Expose immutable UI state from ViewModels.
- Use unidirectional data flow for UI events and state updates.
- Keep ViewModels independent of Compose UI components.
- Put database operations in repositories or data sources.
- Prefer small functions with clear names and single responsibilities.
- Avoid duplicate logic, unused code, and premature abstractions.
- Follow existing package structure and naming conventions.
- Add comments only where intent or behavior is not obvious.

## Local Storage
- Define Room entities, DAOs, and migrations carefully.
- Never run blocking database operations on the main thread.
- Preserve user data during schema migrations.
- Store only non-sensitive preferences in SharedPreferences.
- Do not store passwords, tokens, or secrets in plain preferences.
- Use an appropriate secure storage mechanism for sensitive values.

## Security First
- Treat all external and user-provided input as untrusted.
- Validate inputs and enforce authorization at trust boundaries.
- Follow least-privilege principles for permissions and data access.
- Never hardcode API keys, passwords, or private credentials.
- Avoid logging sensitive or personally identifiable information.
- Use HTTPS and secure network configuration for remote requests.
- Protect sensitive data at rest using appropriate encryption.
- Avoid exposing internal details in user-facing error messages.
- Review security implications before introducing dependencies.

## Agent Workflow
- Inspect relevant code and project conventions before editing.
- Explain major architectural changes and request approval first.
- Ask for clarification when requirements are ambiguous.
- Make the smallest safe change that fully addresses the task.
- Run relevant formatting, build, and tests when available.
- Do not claim tests passed unless they were actually executed.
- Summarize changed files, decisions, and remaining risks.
