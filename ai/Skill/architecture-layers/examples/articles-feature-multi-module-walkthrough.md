# Example — adding the "Articles" feature end to end

A generic walkthrough showing the files the checklist produces for one feature, and where each lives. (The data + domain slice is shown in full in [`articles-data-and-domain.md`](articles-data-and-domain.md); the MVI contract is shown in full in [`articles-list-mvi.md`](articles-list-mvi.md). The composable, navigation, and test files follow the project's standard Compose, navigation, and testing conventions.)

```
settings.gradle.kts
  + include(":feature:articles")

feature/articles/build.gradle.kts                      # feature + compose plugins, namespace

core/domain/.../article/
  Article.kt                                           # domain model
  ArticleRepository.kt                                 # repository INTERFACE
  GetArticlesUseCase.kt                                # use case

core/network/.../article/
  ArticleDto.kt                                        # @Serializable DTO
  ArticleNetworkDataSource.kt                          # data-source interface (+ Retrofit impl)

core/database/.../article/
  ArticleEntity.kt                                     # @Entity
  ArticleDao.kt                                        # @Dao (Flow reads, suspend writes)

core/data/.../article/
  OfflineFirstArticleRepository.kt                     # repository IMPLEMENTATION (internal)
  ArticleMappers.kt                                    # asEntity() / asExternalModel()
  DataModule.kt                                        # @Binds repository -> interface

feature/articles/.../
  ArticlesUiState.kt / ArticlesUiAction.kt / ArticlesUiEffect.kt
  ArticlesViewModel.kt                                 # @HiltViewModel, internal
  ArticlesScreen.kt                                    # stateful + stateless composables
  ArticleUi.kt                                         # UI model + Article.toArticleUi(...)
  ArticlesNavigation.kt                                # NavKeys + articlesEntry(navigator)

app/.../
  + articlesEntry(navigator) registered in the NavDisplay back stack

feature/articles/src/test/.../
  ArticlesViewModelTest.kt                             # Turbine + fakes

feature/articles/src/androidTest/.../
  ArticlesScreenTest.kt                                # test tags
```

The feature's boundary rules live in the checklist's **Feature guardrails** section ([`../references/details.md`](../references/details.md)).
