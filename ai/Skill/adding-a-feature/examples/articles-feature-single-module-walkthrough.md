# Example — adding the "Articles" feature end to end (single-module)

The single-module variant of the [multi-module walkthrough](articles-feature-multi-module-walkthrough.md):
the same files, but organized as **packages inside one Gradle module** — no
`settings.gradle.kts` include and no per-feature `build.gradle.kts`. Layer boundaries are kept
by convention (see the single-module-structure skill), not by the build graph.

```
settings.gradle.kts                                     # unchanged — no new include
build.gradle.kts                                        # unchanged — one build file for the whole app

com.example.app/
  domain/article/
    Article.kt                                           # domain model
    ArticleRepository.kt                                 # repository INTERFACE
    GetArticlesUseCase.kt                                # use case

  data/network/article/
    ArticleDto.kt                                        # @Serializable DTO
    ArticleNetworkDataSource.kt                          # data-source interface (+ Retrofit impl)

  data/database/article/
    ArticleEntity.kt                                     # @Entity
    ArticleDao.kt                                        # @Dao (Flow reads, suspend writes)

  data/article/
    OfflineFirstArticleRepository.kt                     # repository IMPLEMENTATION
    ArticleMappers.kt                                    # asEntity() / asExternalModel()

  di/
    DataModule.kt                                        # @Binds repository -> interface

  feature/articles/
    ArticlesUiState.kt / ArticlesUiAction.kt / ArticlesUiEffect.kt
    ArticlesViewModel.kt                                 # @HiltViewModel
    ArticlesScreen.kt                                    # stateful + stateless composables
    ArticleUi.kt                                         # UI model + Article.toArticleUi(...)
    ArticlesNavigation.kt                                # NavKeys + articlesEntry(navigator)

  (app nav host)
    + articlesEntry(navigator) registered in the NavDisplay back stack

src/test/.../
  ArticlesViewModelTest.kt                              # Turbine + fakes

src/androidTest/.../
  ArticlesScreenTest.kt                                 # test tags
```

**What's different from the multi-module version**
- No `include(":feature:articles")` and no `feature/articles/build.gradle.kts` — it's one
  module, so there is nothing new to add to the build setup (checklist steps 1–2 don't apply).
- Files land in **packages** (`domain/article/`, `data/.../article/`, `feature/articles/`)
  instead of separate modules.
- `internal` only hides things from *outside the module*, so within this single module it is a
  weak boundary signal. Don't rely on it to keep a feature out of `data` — that's enforced by
  convention (and optionally a lint / Konsist rule), plus code review.

The feature's boundary rules live in the checklist's **Guardrails** section ([`../references/details.md`](../references/details.md)).
