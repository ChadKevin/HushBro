package com.invictus.hushbro.di

import com.invictus.hushbro.pipeline.Classifier
import com.invictus.hushbro.pipeline.PassThroughClassifier
import com.invictus.hushbro.pipeline.PassThroughRedactor
import com.invictus.hushbro.pipeline.PassThroughRuleEngine
import com.invictus.hushbro.pipeline.Redactor
import com.invictus.hushbro.pipeline.RuleEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Binds the notification processing pipeline's pluggable components.
 *
 * Each interface is currently bound to its temporary pass-through
 * implementation; later steps swap in the real redactor, rule engine and
 * classifier without touching the pipeline or its callers.
 *
 * [NotificationPipeline] itself is a @Singleton with an @Inject constructor and
 * is resolved by Hilt just-in-time like the other repositories.
 */
@Module
@InstallIn(SingletonComponent::class)
object PipelineModule {

    @Provides
    fun provideRedactor(): Redactor = PassThroughRedactor()

    @Provides
    fun provideRuleEngine(): RuleEngine = PassThroughRuleEngine()

    @Provides
    fun provideClassifier(): Classifier = PassThroughClassifier()
}